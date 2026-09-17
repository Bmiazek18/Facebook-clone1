import { defineEventHandler } from 'h3'
import { getRedisClient } from '../../utils/redis'
import { getValidAccessToken } from '../../utils/session'
import type { CustomFriendList } from './index.get'

async function resolveUserId(event: any): Promise<string> {
  const token = await getValidAccessToken(event)
  if (token) {
    try {
      const payload = JSON.parse(Buffer.from(token.split('.')[1], 'base64').toString('utf8'))
      if (payload?.sub) return payload.sub
    } catch {}
  }
  const xUserId = event.node.req.headers['x-user-id']
  if (xUserId) return String(xUserId)
  return 'default'
}

export default defineEventHandler(async (event) => {
  const listId = event.context.params?.id
  if (!listId) {
    event.node.res.statusCode = 400
    return { success: false, message: 'ID listy jest wymagane.' }
  }

  const userId = await resolveUserId(event)
  const redis = getRedisClient()
  const storageKey = `user:friend_lists:${userId}`

  try {
    const raw = await redis.get(storageKey)
    let existingLists: CustomFriendList[] = raw ? JSON.parse(raw) : []

    const target = existingLists.find((l) => l.id === listId)
    if (target?.isSystem) {
      event.node.res.statusCode = 400
      return { success: false, message: 'Nie można usunąć listy systemowej.' }
    }

    existingLists = existingLists.filter((l) => l.id !== listId)
    await redis.set(storageKey, JSON.stringify(existingLists))

    return { success: true, lists: existingLists }
  } catch (err: any) {
    console.error('BFF: Failed to delete friend list from Redis:', err)
    event.node.res.statusCode = 500
    return { success: false, message: 'Błąd usuwania listy z serwera.' }
  }
})
