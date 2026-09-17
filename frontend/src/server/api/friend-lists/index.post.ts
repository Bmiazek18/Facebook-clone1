import { defineEventHandler, readBody } from 'h3'
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
  const userId = await resolveUserId(event)
  const body = await readBody<{ name: string; memberIds?: string[] }>(event)

  if (!body?.name?.trim()) {
    event.node.res.statusCode = 400
    return { success: false, message: 'Nazwa listy jest wymagana.' }
  }

  const redis = getRedisClient()
  const storageKey = `user:friend_lists:${userId}`

  try {
    const raw = await redis.get(storageKey)
    const existingLists: CustomFriendList[] = raw ? JSON.parse(raw) : []

    const newList: CustomFriendList = {
      id: `custom_${Date.now()}_${Math.random().toString(36).substr(2, 6)}`,
      name: body.name.trim(),
      description: 'Niestandardowa lista utworzona przez Ciebie.',
      icon: 'custom',
      isSystem: false,
      memberIds: body.memberIds ? [...new Set(body.memberIds.map(String))] : [],
      createdAt: Date.now(),
    }

    existingLists.push(newList)
    await redis.set(storageKey, JSON.stringify(existingLists))

    return { success: true, list: newList, lists: existingLists }
  } catch (err: any) {
    console.error('BFF: Failed to create friend list in Redis:', err)
    event.node.res.statusCode = 500
    return { success: false, message: 'Błąd zapisu na serwerze.' }
  }
})
