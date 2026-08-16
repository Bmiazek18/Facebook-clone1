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

  if (!encryptedHistory) {
    return ''
  }

  return restoreChatHistoryFromVault(exportKey, encryptedHistory)
}

export async function hasVaultOnServer(userId: string): Promise<boolean> {
  try {
    const resp = await fetch(`/api/vaults/${userId}`, { method: 'GET' })
    return resp.ok
  } catch {
    return false
  }
}
