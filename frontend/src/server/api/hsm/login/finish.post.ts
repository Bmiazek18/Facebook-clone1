import { server as opaqueServer, ready } from '@serenity-kit/opaque'
import { getValidAccessToken } from '../../../utils/session'

interface LoginFinishRequest {
  loginId: string
  finishLoginRequest: string
}

interface CachedOpaqueSession {
  state: string
  userId: string
}

const JAVA_API_URL = process.env.JAVA_API_URL ?? 'http://localhost:8080'

export default defineEventHandler(async (event) => {
  await ready

  const { loginId, finishLoginRequest } = await readBody<LoginFinishRequest>(event)

  if (!loginId || !finishLoginRequest) {
    throw createError({
      statusCode: 400,
      statusMessage: 'Brak wymaganych danych.'
    })
  }

  const cacheKey = `opaque_login:${loginId}`
  const cachedData = await useStorage('cache').getItem<CachedOpaqueSession>(cacheKey)

  if (!cachedData?.state || !cachedData?.userId) {
    throw createError({
      statusCode: 400,
      statusMessage: 'Sesja logowania wygasła, nie istnieje lub została już użyta.'
    })
  }

  await useStorage('cache').removeItem(cacheKey)

  try {
    opaqueServer.finishLogin({
      finishLoginRequest,
      serverLoginState: cachedData.state
    })

    const accessToken = await getValidAccessToken(event)
    if (accessToken) {
      await $fetch<void>(`${JAVA_API_URL}/api/vaults/${cachedData.userId}/attempts`, {
        method: 'POST',
        headers: {
          Authorization: `Bearer ${accessToken}`,
          'Content-Type': 'application/json'
        },
        body: { attempts: 0 }
      }).catch((err) => {
        console.error(`Nie udało się wyzerować prób dla usera ${cachedData.userId}:`, err)
      })
    }

    return { success: true }
  } catch (err: unknown) {
    console.error('Błąd weryfikacji OPAQUE (zły PIN):', err)

    throw createError({
      statusCode: 401,
      statusMessage: 'Niepoprawny PIN.'
    })
  }
})
