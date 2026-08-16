import { getValidAccessToken } from '../../utils/session'

const JAVA_API_URL = process.env.JAVA_API_URL ?? 'http://localhost:8080'

export default defineEventHandler(async (event) => {
  const { userId, registrationRecord, encryptedHistory } = await readBody(event)

  if (!userId || !registrationRecord) {
    throw createError({ statusCode: 400, statusMessage: 'Brak wymaganych danych.' })
  }

  const accessToken = await getValidAccessToken(event)
  if (!accessToken) {
    throw createError({ statusCode: 401, statusMessage: 'Unauthorized' })
  }

  try {

    await $fetch(`${JAVA_API_URL}/api/vaults/${userId}`, {
      method: 'PUT',
      headers: {
        Authorization: `Bearer ${accessToken}`,
        'Content-Type': 'application/json'
      },
      body: {
        opaqueRecord: registrationRecord,
        encryptedHistory: encryptedHistory ?? '',
        failedAttempts: 0
      }
    })

    return { success: true }
  } catch (err) {
    console.error('Błąd register-finish:', err)
    throw createError({ statusCode: 500, statusMessage: 'Nie udało się zapisać Vault w Javie' })
  }
})
