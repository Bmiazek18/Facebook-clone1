import { ref } from 'vue'
import { useAuthStore } from '@/stores/auth'
import type { User } from '@/types/User'

export function useFriendSearch() {
  const authStore = useAuthStore()
  const config = useRuntimeConfig()
  const users = ref<User[]>([])
  const isLoading = ref(false)

  function mapUser(u: { id?: string; userId?: string; firstName?: string; lastName?: string; avatarId?: string }): User {
    const id = String(u.id || u.userId || '')
    const fullName = `${u.firstName || ''} ${u.lastName || ''}`.trim() || `User ${id}`
    const avatarUrl = u.avatarId
      ? `${config.public.storageUrl}/avatars/${u.avatarId}`
      : `https://ui-avatars.com/api/?name=${encodeURIComponent(fullName)}&background=random&color=fff`

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
    isLoading.value = true
    try {
      const response = await fetch(config.public.apiUrl + '/', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          query: `
            query GetFriendSuggestions($currentUserId: ID!) {
              getFriendSuggestions(currentUserId: $currentUserId) {
                userId
                user {
                  id
                  firstName
                  lastName
                  avatarId
                }
              }
            }
          `,
          variables: { currentUserId: String(authStore.currentUserId) },
        }),
      })
      const resJson = await response.json()
      if (resJson.data?.getFriendSuggestions) {
        users.value = resJson.data.getFriendSuggestions.map((s: any) =>
          mapUser({ ...s.user, id: s.user?.id || s.userId }),
        )
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
      const response = await fetch(config.public.apiUrl + '/', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          query: `
            query SearchUsers($query: String!, $currentUserId: ID) {
              searchUsers(query: $query, currentUserId: $currentUserId) {
                id
                firstName
                lastName
                avatarId
              }
            }
          `,
          variables: {
            query: queryText,
            currentUserId: String(authStore.currentUserId),
          },
        }),
      })
      const resJson = await response.json()
      if (resJson.data?.searchUsers) {
        users.value = resJson.data.searchUsers.map((u: any) => mapUser(u))
      }
    } catch (e) {
      console.warn('Failed to search users:', e)
    } finally {
      isLoading.value = false
    }
  }

  return { users, isLoading, loadSuggestions, searchUsers }
}
