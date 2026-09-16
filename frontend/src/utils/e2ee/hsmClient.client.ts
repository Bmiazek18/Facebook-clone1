import { client, ready } from '@serenity-kit/opaque'
import { backupChatHistoryToVault, restoreChatHistoryFromVault } from './signalService.client'

interface RegisterStartResponse {
  registrationResponse: string
}

interface LoginStartResponse {
  loginResponse: string
  loginId: string
  encryptedHistory: string
}

interface LoginFinishResponse {
  success: boolean
}

async function stretchPin(pin: string, userId: string): Promise<string> {
  const encoder = new TextEncoder()
  const pinBytes = encoder.encode(pin)
  const saltBytes = encoder.encode(userId)

  const baseKey = await window.crypto.subtle.importKey(
    'raw',
    pinBytes,
    { name: 'PBKDF2' },
    false,
    ['deriveBits']
  )

  const derivedBits = await window.crypto.subtle.deriveBits(
    {
      name: 'PBKDF2',
      salt: saltBytes,
      iterations: 600000,
      hash: 'SHA-256'
    },
    baseKey,
    256
  )

  const bytes = new Uint8Array(derivedBits)
  let binary = ''
  for (let i = 0; i < bytes.byteLength; i++) {
    binary += String.fromCharCode(bytes[i] || 0)
  }
  return window.btoa(binary)
}

const vaultCache = new Map<string, boolean>()

export async function setupVaultPin(pin: string, userId: string, chatHistory: string) {
  await ready
  const stretched = await stretchPin(pin, userId)

  const { clientRegistrationState, registrationRequest } = client.startRegistration({
    password: stretched
  })

  const apiResp = await fetch('/api/hsm/register-start', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ userId, registrationRequest })
  })

  if (!apiResp.ok) {
    throw new Error('Nie udało się rozpocząć rejestracji.')
  }

  const { registrationResponse }: RegisterStartResponse = await apiResp.json()

  const { registrationRecord, exportKey } = client.finishRegistration({
    password: stretched,
    registrationResponse,
    clientRegistrationState
  })

  const encryptedHistory = await backupChatHistoryToVault(exportKey, chatHistory)

  const finishResp = await fetch('/api/hsm/register-finish', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      userId,
      registrationRecord,
      encryptedHistory
    })
  })

  if (!finishResp.ok) {
    throw new Error('Nie udało się zakończyć rejestracji.')
  }

  vaultCache.set(userId, true)
  if (typeof window !== 'undefined') {
    localStorage.setItem(`e2ee_has_vault_${userId}`, 'true')
  }
}

export async function unlockVaultAndRestoreHistory(pin: string, userId: string) {
  await ready
  const stretched = await stretchPin(pin, userId)

  const { clientLoginState, startLoginRequest } = client.startLogin({
    password: stretched
  })

  const startResp = await fetch('/api/hsm/login/start', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ userId, startLoginRequest })
  })

  if (!startResp.ok) {
    throw new Error('Błędny PIN lub konto zablokowane.')
  }

  const { loginResponse, loginId, encryptedHistory }: LoginStartResponse = await startResp.json()

  const result = client.finishLogin({
    password: stretched,
    loginResponse,
    clientLoginState
  })

  if (result === undefined) {
    throw new Error('Niepoprawny PIN.')
  }

  const { finishLoginRequest, exportKey } = result

  const finishResp = await fetch('/api/hsm/login/finish', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ loginId, finishLoginRequest })
  })

  if (!finishResp.ok) {
    throw new Error('Niepoprawny PIN.')
  }

  const _: LoginFinishResponse = await finishResp.json()

  vaultCache.set(userId, true)
  if (typeof window !== 'undefined') {
    localStorage.setItem(`e2ee_has_vault_${userId}`, 'true')
  }

  if (!encryptedHistory) {
    return ''
  }

  return restoreChatHistoryFromVault(exportKey, encryptedHistory)
}

export async function hasVaultOnServer(userId: string, force = false): Promise<boolean> {
  if (!userId) return false

  if (!force) {
    if (vaultCache.has(userId)) {
      return vaultCache.get(userId)!
    }
    if (typeof window !== 'undefined') {
      const stored = localStorage.getItem(`e2ee_has_vault_${userId}`)
      if (stored !== null) {
        const hasVault = stored === 'true'
        vaultCache.set(userId, hasVault)
        return hasVault
      }
    }
  }

  try {
    const resp = await fetch('/graphql', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        query: `
          query GetVault($userId: ID!) {
            vault(userId: $userId) {
              opaqueRecord
            }
          }
        `,
        variables: { userId }
      })
    })

    if (!resp.ok) return false
    const json = await resp.json()
    const hasVault = !!json.data?.vault?.opaqueRecord
    vaultCache.set(userId, hasVault)
    if (typeof window !== 'undefined') {
      localStorage.setItem(`e2ee_has_vault_${userId}`, hasVault ? 'true' : 'false')
    }
    return hasVault
  } catch {
    return false
  }
}
