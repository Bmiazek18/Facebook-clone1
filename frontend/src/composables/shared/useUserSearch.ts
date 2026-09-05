import { ref, watch } from 'vue'
import type { Ref } from 'vue'
import { gql } from 'graphql-tag'
import type { User } from '@/utils/users'
import { getApolloClient } from '@/utils/apollo'

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
        const apolloClient = getApolloClient()
        const res = await apolloClient.query({
          query: gql`
            query SearchUsers($query: String!) {
              searchUsers(query: $query) {
                id
                firstName
                lastName
                avatarId
                avatar
              }
            }
          `,
          variables: { query: newSearchTerm },
          fetchPolicy: 'network-only'
        })

        if (res.data && res.data.searchUsers) {
          matchingUsers.value = res.data.searchUsers.map((u: any) => ({
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
