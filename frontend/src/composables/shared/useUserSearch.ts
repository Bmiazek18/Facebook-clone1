import { ref, watch } from 'vue'
import type { Ref } from 'vue'
import type { User } from '@/utils/users'
import { usersApi } from '@/api/users'

export function useUserSearch(searchTerm: Ref<string>) {
  const matchingUsers = ref<User[]>([])
  const isLoading = ref(false)

  watch(
    searchTerm,
    async (newSearchTerm) => {
      if (!newSearchTerm) {
        matchingUsers.value = []
        return
      }

      isLoading.value = true
      try {
        const users = await usersApi.searchUsers(newSearchTerm, '')
        if (users) {
          matchingUsers.value = users.map((u: any) => ({
            id: u.id,
            name: `${u.firstName} ${u.lastName}`,
            avatar: u.avatar || '/default-avatar.png'
          }))
        }
      } catch (e) {
        console.error('Failed to search users via GraphQL:', e)
      } finally {
        isLoading.value = false
      }
    },
    { immediate: true },
  )

  return { matchingUsers, isLoading }
}
