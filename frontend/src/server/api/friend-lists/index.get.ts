import { defineEventHandler } from 'h3'
import { getRedisClient } from '../../utils/redis'
import { getValidAccessToken } from '../../utils/session'

export interface CustomFriendList {
  id: string
  name: string
  description?: string
  icon?: string
  isSystem?: boolean
  memberIds: string[]
  createdAt: number
}

const DEFAULT_LISTS: Omit<CustomFriendList, 'createdAt'>[] = [
  {
    id: 'close_friends',
    name: 'Bliscy znajomi',
    description: 'Najbliżsi znajomi, z którymi dzielisz się wyjątkowymi chwilami.',
    icon: 'star',
    isSystem: true,
    memberIds: [],
  },
  {
    id: 'restricted',
    name: 'Ograniczony dostęp',
    description: 'Znajomi z ograniczonym dostępem. Widzą tylko posty publiczne lub te, w których ich oznaczasz.',
    icon: 'lock',
    isSystem: true,
    memberIds: [],
  },
  {
    id: 'acquaintances',
    name: 'Dalsi znajomi',
    description: 'Osoby, z którymi chcesz rzadziej dzielić się publikowanymi postami.',
    icon: 'group',
    isSystem: true,
    memberIds: [],
  },
]

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
  const redis = getRedisClient()
  const storageKey = `user:friend_lists:${userId}`

  try {
    const raw = await redis.get(storageKey)
    if (raw) {
      const parsed: CustomFriendList[] = JSON.parse(raw)
      const existingMap = new Map(parsed.map((l) => [l.id, l]))
      const merged: CustomFriendList[] = DEFAULT_LISTS.map((dl) => {
        if (existingMap.has(dl.id)) {
          const existing = existingMap.get(dl.id)!
          return {
            ...dl,
            memberIds: existing.memberIds || [],
            createdAt: existing.createdAt || Date.now(),
          }
        }
        return { ...dl, createdAt: Date.now() }
      })

      parsed.forEach((l) => {
        if (!merged.some((m) => m.id === l.id)) {
          merged.push(l)
        }
      })
      return { success: true, lists: merged }
    } else {
      const initial = DEFAULT_LISTS.map((dl) => ({ ...dl, createdAt: Date.now() }))
      await redis.set(storageKey, JSON.stringify(initial))
      return { success: true, lists: initial }
    }
  } catch (err: any) {
    console.error('BFF: Failed to get friend lists from Redis:', err)
    const fallback = DEFAULT_LISTS.map((dl) => ({ ...dl, createdAt: Date.now() }))
    return { success: true, lists: fallback }
  }
})
