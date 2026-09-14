import { ref } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { usersApi } from '@/api/users'
import type { User } from '@/types/User'

export function useFriendSearch() {
  const authStore = useAuthStore()
  const config = useRuntimeConfig()
  const users = ref<User[]>([])
  const isLoading = ref(false)

  function mapUser(u: { id?: string; userId?: string; firstName?: string; lastName?: string; avatar?: string; avatarId?: string }): User {
    const id = String(u.id || u.userId || '')
    const fullName = `${u.firstName || ''} ${u.lastName || ''}`.trim() || `User ${id}`
    const avatarUrl = u.avatar || (u.avatarId
      ? `${config.public.storageUrl}/avatars/${u.avatarId}`
      : `https://ui-avatars.com/api/?name=${encodeURIComponent(fullName)}&background=random&color=fff`)

    return {
      id,
      name: fullName,
      avatar: avatarUrl,
      bio: '',
      location: '',
      website: '',
      joinDate: '',
      followersCount: 0,
      followingCount: 0,
      friendsCount: 0,
      postsCount: 0,
      cover: '',
      status: 'offline',
    }
  }

  const loadSuggestions = async () => {
    if (!authStore.currentUserId) return
    isLoading.value = true
    try {
      const suggestions = await usersApi.getFriendSuggestions(String(authStore.currentUserId))
      if (suggestions && suggestions.length > 0) {
        users.value = suggestions.map((s: any) =>
          mapUser({ ...s.user, id: s.user?.id || s.userId }),
        )
      } else {
        users.value = []
      }
    } catch (e) {
      console.warn('Failed to load friend suggestions:', e)
    } finally {
      isLoading.value = false
    }
  }

  const searchUsers = async (queryText: string) => {
    if (!queryText.trim()) {
      await loadSuggestions()
      return
    }
    isLoading.value = true
    try {
      const results = await usersApi.searchUsers(queryText, String(authStore.currentUserId))
      if (results && results.length > 0) {
        users.value = results.map((u: any) => mapUser(u))
      } else {
        users.value = []
      }
    } catch (e) {
      console.warn('Failed to search users:', e)
    } finally {
      isLoading.value = false
    }
  }

  return { users, isLoading, loadSuggestions, searchUsers }
}

