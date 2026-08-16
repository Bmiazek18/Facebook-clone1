import { server as opaqueServer, ready } from '@serenity-kit/opaque'
import { randomUUID } from 'crypto'
import { getValidAccessToken } from '../../../utils/session'

interface VaultResponse {
  failedAttempts: number
  opaqueRecord: string
  encryptedHistory: string
}

interface LoginRequest {
  userId: string
  startLoginRequest: string
}

const SERVER_SETUP = process.env.OPAQUE_SERVER_SETUP
const JAVA_API_URL = process.env.JAVA_API_URL ?? 'http://localhost:8080'

export default defineEventHandler(async (event) => {
  await ready

  const { userId, startLoginRequest } = await readBody<LoginRequest>(event)

  if (!userId || !startLoginRequest) {
    throw createError({
      statusCode: 400,
      statusMessage: 'Brak wymaganych danych.'
    })
  }

  if (!SERVER_SETUP) {
    throw createError({
      statusCode: 500,
      statusMessage: 'Brak OPAQUE_SERVER_SETUP.'
    })
  }

  const accessToken = await getValidAccessToken(event)
  if (!accessToken) {
    throw createError({ statusCode: 401, statusMessage: 'Unauthorized' })
  }

  const headers: HeadersInit = {
    Authorization: `Bearer ${accessToken}`,
    'Content-Type': 'application/json'
  }

  try {
    const vault = await $fetch<VaultResponse>(`${JAVA_API_URL}/api/vaults/${userId}`, { headers })

    if (!vault?.opaqueRecord) {
      throw createError({
        statusCode: 404,
        statusMessage: 'Nie znaleziono sejfu użytkownika.'
      })
    }

    if (vault.failedAttempts >= 10) {
      throw createError({
        statusCode: 429,
        statusMessage: 'HSM LOCKOUT: Konto zostało zablokowane.'
      })
    }

    await $fetch<void>(`${JAVA_API_URL}/api/vaults/${userId}/attempts`, {
      method: 'POST',
      headers,
      body: { attempts: vault.failedAttempts + 1 }
    })

    const { loginResponse, serverLoginState } = opaqueServer.startLogin({
      serverSetup: SERVER_SETUP,
      registrationRecord: vault.opaqueRecord,
      startLoginRequest,
      userIdentifier: userId
    })

    const loginId = randomUUID()

    await useStorage('cache').setItem(
      `opaque_login:${loginId}`,
      { state: serverLoginState, userId },
      { ttl: 300 }
    )

    return {
      loginResponse,
      loginId,
      encryptedHistory: vault.encryptedHistory || ''
    }
  } catch (err: unknown) {
    if (typeof err === 'object' && err !== null && 'statusCode' in err) {
      throw err
    }

    console.error('OPAQUE Start Login Error:', err)

    throw createError({
      statusCode: 500,
      statusMessage: 'Błąd podczas logowania OPAQUE.'
    })
  }
})
