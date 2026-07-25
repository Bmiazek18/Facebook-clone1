import { ref } from 'vue'
import { useApolloClient } from '@vue/apollo-composable'
import { gql } from 'graphql-tag'

export const usersCache = ref<Record<string, { id: string, name: string, avatar: string }>>({})

export function useUserCache() {
  async function getOrFetchUser(userId: string) {
    const cleanId = String(userId).replace('user_', '')
    if (usersCache.value[cleanId]) {
      return usersCache.value[cleanId]
    }

    try {
      const apolloClient = useApolloClient().resolveClient()
      const res = await apolloClient.query({
        query: gql`
          query GetUserById($userId: ID!) {
            getUserById(userId: $userId) {
              id
              firstName
              lastName
              avatarId
            }
          }
        `,
        variables: { userId: cleanId }
      })
      if (res.data && res.data.getUserById) {
        const u = res.data.getUserById
        const apiUrl = import.meta.env.VITE_API_URL || 'http://localhost:8080'
        const avatarUrl = u.avatarId
          ? `${apiUrl}/api/users/avatar/${u.avatarId}`
          : `${apiUrl}/api/users/avatar/default-avatar.svg`
        const mapped = {
          id: u.id,
          name: `${u.firstName} ${u.lastName}`,
          avatar: avatarUrl
        }
        usersCache.value[cleanId] = mapped
        return mapped
      }
    } catch (e) {
      console.error('Failed to fetch user via GraphQL:', e)
    }

    const apiUrl = import.meta.env.VITE_API_URL || 'http://localhost:8080'
    const fallback = {
      id: cleanId,
      name: 'Użytkownik',
      avatar: `${apiUrl}/api/users/avatar/default-avatar.svg`
    }
    usersCache.value[cleanId] = fallback
    return fallback
  }

  return {
    usersCache,
    getOrFetchUser
  }
}
