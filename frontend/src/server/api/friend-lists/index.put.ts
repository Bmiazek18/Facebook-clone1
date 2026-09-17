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
  const body = await readBody<{
    listId?: string
    action?: 'toggle' | 'add' | 'remove' | 'rename' | 'syncAll'
    targetUserId?: string | number
    newName?: string
    lists?: CustomFriendList[]
  }>(event)

  const redis = getRedisClient()
  const storageKey = `user:friend_lists:${userId}`

  try {
    if (body?.action === 'syncAll' && Array.isArray(body.lists)) {
      await redis.set(storageKey, JSON.stringify(body.lists))
      return { success: true, lists: body.lists }
    }

    const raw = await redis.get(storageKey)
    const existingLists: CustomFriendList[] = raw ? JSON.parse(raw) : []
    const list = existingLists.find((l) => l.id === body?.listId)

    if (!list) {
      event.node.res.statusCode = 404
      return { success: false, message: 'Nie znaleziono podanej listy.' }
    }

    const targetUserStr = body.targetUserId ? String(body.targetUserId) : ''

    if (body.action === 'toggle' && targetUserStr) {
      if (list.memberIds.includes(targetUserStr)) {
        list.memberIds = list.memberIds.filter((id) => id !== targetUserStr)
      } else {
        list.memberIds.push(targetUserStr)
      }
    } else if (body.action === 'add' && targetUserStr) {
      if (!list.memberIds.includes(targetUserStr)) {
        list.memberIds.push(targetUserStr)
      }
    } else if (body.action === 'remove' && targetUserStr) {
      list.memberIds = list.memberIds.filter((id) => id !== targetUserStr)
    } else if (body.action === 'rename' && body.newName?.trim()) {
      list.name = body.newName.trim()
    }

    await redis.set(storageKey, JSON.stringify(existingLists))
    return { success: true, list, lists: existingLists }
  } catch (err: any) {
    console.error('BFF: Failed to update friend list in Redis:', err)
    event.node.res.statusCode = 500
    return { success: false, message: 'Błąd aktualizacji listy na serwerze.' }
  }
})
