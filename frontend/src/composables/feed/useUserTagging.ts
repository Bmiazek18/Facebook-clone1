import { ref, watch, computed } from 'vue'
import type { Ref } from 'vue'
import { gql } from 'graphql-tag'
import type { User } from '@/utils/users'
import { getApolloClient } from '@/utils/apollo'

export function useUserTagging() {
  const searchTerm = ref<string | null>(null)
  const matchingUsers = ref<User[]>([])
  const showUserDropdown = computed(
    () => matchingUsers.value.length > 0 && searchTerm.value !== null,
  )

  watch(searchTerm, async (newSearchTerm) => {
    if (newSearchTerm === null) {
      matchingUsers.value = []
      return
    }

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
        variables: { query: newSearchTerm || '' },
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
      console.error('Failed to tag search users via GraphQL:', e)
    }
  })

  function triggerUserTagging(textBeforeCaret: string) {
    const match = textBeforeCaret.match(/@([^\s]*)$/)
    if (match) {
      searchTerm.value = match[1]
    } else {
      searchTerm.value = null
    }
  }

  function selectUser(contentRef: Ref<string>, user: User) {
    const currentContent = contentRef.value
    const newContent = currentContent.replace(/@([^\s]*)$/, `[@${user.id}] `)
    contentRef.value = newContent
    searchTerm.value = null
  }

  return {
    matchingUsers,
    showUserDropdown,
    triggerUserTagging,
    selectUser,
    searchTerm, // Also return searchTerm to be able to reset it if needed
  }
}
