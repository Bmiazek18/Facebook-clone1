import { defineEventHandler, getCookie } from 'h3'
import { getValidAccessToken } from '../../utils/session'
import { getRedisClient } from '../../utils/redis'

export default defineEventHandler(async (event) => {
  const token = await getValidAccessToken(event)
  if (!token) {
    return { success: false, expiresAt: null }
  }

  const sessionId = getCookie(event, 'bff_session')
  if (!sessionId) {
    return { success: true, expiresAt: null }
  }

  const redis = getRedisClient()
  const sessionKey = `session:${sessionId}`
  const sessionStr = await redis.get(sessionKey)
  const session = sessionStr ? JSON.parse(sessionStr) : null

  return {
    success: true,
    expiresAt: session ? session.expiresAt : null
  }
})
