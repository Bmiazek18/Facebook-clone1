import { server as opaqueServer, ready } from '@serenity-kit/opaque'
import { getValidAccessToken } from '../../utils/session'

const SERVER_SETUP = process.env.OPAQUE_SERVER_SETUP

export default defineEventHandler(async (event) => {
  await ready
  const { userId, registrationRequest } = await readBody(event)

  if (!userId || !registrationRequest) {
    throw createError({ statusCode: 400, statusMessage: 'Brak wymaganych danych.' })
  }

  if (!SERVER_SETUP) {
    throw createError({ statusCode: 500, statusMessage: 'Brak OPAQUE_SERVER_SETUP' })
  }

  const accessToken = await getValidAccessToken(event)
  if (!accessToken) {
    throw createError({ statusCode: 401, statusMessage: 'Unauthorized' })
  }

  try {
    const { registrationResponse } = opaqueServer.createRegistrationResponse({
      serverSetup: SERVER_SETUP,
      userIdentifier: userId,
      registrationRequest
    })

    return { registrationResponse }
  } catch (err) {
    console.error('Błąd register-start:', err)
    throw createError({ statusCode: 500, statusMessage: 'Błąd rejestracji OPAQUE' })
  }
})
