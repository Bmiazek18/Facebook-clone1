import { ref } from 'vue'
import { usersApi } from '@/api/users'

export const usersCache = ref<Record<string, { id: string, name: string, avatar: string, note?: string }>>({})

export function useUserCache() {
  async function getOrFetchUser(userId: string) {
    const cleanId = String(userId).replace('user_', '')
    // Nil / self-XOR conversation ids are never real users
    if (!cleanId || cleanId === '00000000-0000-4000-8000-000000000000') {
      return {
        id: cleanId,
        name: 'Użytkownik',
        avatar: '/default-avatar.png'
      }
    }
    if (usersCache.value[cleanId]) {
      return usersCache.value[cleanId]
    }

    try {
      const u = await usersApi.getUserById(cleanId)
      if (u) {
        const avatarUrl = u.avatar || '/default-avatar.png'
        const mapped = {
          id: u.id,
          name: `${u.firstName} ${u.lastName}`,
          avatar: avatarUrl,
          note: u.note || ''
        }
        usersCache.value[cleanId] = mapped
        return mapped
      }
    } catch (e) {
      // Missing users (stale inbox rows, deleted accounts) are expected — use fallback.
      console.warn('User not found or unreachable:', cleanId)
    }

    const fallback = {
      id: cleanId,
      name: 'Użytkownik',
      avatar: '/default-avatar.png'
    }
    usersCache.value[cleanId] = fallback
    return fallback
  }

  return {
    usersCache,
    getOrFetchUser
  }
}
