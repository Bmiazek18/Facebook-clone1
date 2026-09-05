import { ref } from 'vue'
import { usersApi } from '@/api/users'

export interface CachedUser {
  id: string
  name: string
  avatar: string
  note?: string
}

export const usersCache = ref<Record<string, CachedUser>>({})
const inFlightPromises = new Map<string, Promise<CachedUser>>()

export function useUserCache() {
  async function getOrFetchUser(userId: string): Promise<CachedUser> {
    const cleanId = String(userId).replace('user_', '')

    // Nil / self-XOR conversation ids are never real users
    if (!cleanId || cleanId === '00000000-0000-4000-8000-000000000000') {
      return {
        id: cleanId,
        name: 'Użytkownik',
        avatar: '/default-avatar.png'
      }
    }

    // 1. Check local reactive cache
    if (usersCache.value[cleanId]) {
      return usersCache.value[cleanId]
    }

    // 2. Check if a request for this user is already in-flight (Deduplication)
    if (inFlightPromises.has(cleanId)) {
      return inFlightPromises.get(cleanId)!
    }

    // 3. Initiate new fetch and store promise in inFlightPromises
    const fetchPromise = (async () => {
      try {
        const u = await usersApi.getUserById(cleanId)
        if (u) {
          const avatarUrl = u.avatar || '/default-avatar.png'
          const mapped: CachedUser = {
            id: String(u.id),
            name: `${u.firstName || ''} ${u.lastName || ''}`.trim() || 'Użytkownik',
            avatar: avatarUrl,
            note: u.note || ''
          }
          usersCache.value[cleanId] = mapped
          return mapped
        }
      } catch (e) {
        console.warn('User not found or unreachable:', cleanId)
      }

      const fallback: CachedUser = {
        id: cleanId,
        name: 'Użytkownik',
        avatar: '/default-avatar.png'
      }
      usersCache.value[cleanId] = fallback
      return fallback
    })()

    inFlightPromises.set(cleanId, fetchPromise)

    try {
      return await fetchPromise
    } finally {
      inFlightPromises.delete(cleanId)
    }
  }

  async function preloadUsers(userIds: (string | number)[]) {
    const uniqueIds = Array.from(new Set(userIds.map(id => String(id).replace('user_', ''))))
      .filter(id => id && !usersCache.value[id] && !inFlightPromises.has(id))

    if (uniqueIds.length === 0) return

    await Promise.allSettled(uniqueIds.map(id => getOrFetchUser(id)))
  }

  return {
    usersCache,
    getOrFetchUser,
    preloadUsers
  }
}
