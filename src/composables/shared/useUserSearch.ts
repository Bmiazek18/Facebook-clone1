import { ref, watch } from 'vue'
import type { Ref } from 'vue'
import { useApolloClient } from '@vue/apollo-composable'
import { gql } from 'graphql-tag'
import type { User } from '@/utils/users'

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
        const apolloClient = useApolloClient().resolveClient()
        const res = await apolloClient.query({
          query: gql`
            query SearchUsers($query: String!) {
              searchUsers(query: $query) {
                id
                firstName
                lastName
                avatarId
              }
            }
          `,
          variables: { query: newSearchTerm },
          fetchPolicy: 'network-only'
        })

        if (res.data && res.data.searchUsers) {
          const apiUrl = import.meta.env.VITE_API_URL || 'http://localhost:8080'
          matchingUsers.value = res.data.searchUsers.map((u: any) => ({
            id: u.id,
            name: `${u.firstName} ${u.lastName}`,
            avatar: u.avatarId
              ? `${apiUrl}/api/users/avatar/${u.avatarId}`
              : `${apiUrl}/api/users/avatar/default-avatar.svg`
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
