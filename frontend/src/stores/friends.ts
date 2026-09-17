import { defineStore } from 'pinia'
import { ref } from 'vue'
import { usersApi, GET_FRIEND_REQUESTS_QUERY, GET_FRIEND_SUGGESTIONS } from '@/api/users'
import { useAuthStore } from '@/stores/auth'
import { getApolloClient } from '@/utils/apollo'

export interface FriendItem {
  id: string
  name: string
  commonFriends: number
  imageUrl: string
  isFriend: boolean
}

export const useFriendsStore = defineStore('friends', () => {
  const authStore = useAuthStore()

  const friendRequests = ref<FriendItem[]>([])
  const friendSuggestions = ref<FriendItem[]>([])
  const isLoaded = ref(false)
  const isLoading = ref(false)

  const mapToFriendItem = (item: any): FriendItem => {
    const u = item.user
    const avatarUrl = u?.avatar
      || `https://ui-avatars.com/api/?name=${encodeURIComponent(
          (u?.firstName || '') + ' ' + (u?.lastName || '')
        )}&background=EBF4FF&color=1877F2&bold=true`

    return {
      id: String(item.userId || item.id),
      name: u ? `${u.firstName || ''} ${u.lastName || ''}`.trim() || `User ${item.userId}` : `User ${item.userId}`,
      commonFriends: Number(item.mutualFriendsCount || 0),
      imageUrl: avatarUrl,
      isFriend: false,
    }
  }

  // Sprawdza synchronicznie, czy dane są już obecne w cache Apollo Client
  const checkApolloCache = (userId: string | number) => {
    try {
      const client = getApolloClient()
      const reqCache = client.readQuery<{ getFriendRequests: any[] }>({
        query: GET_FRIEND_REQUESTS_QUERY,
        variables: { currentUserId: String(userId) },
      })
      const sugCache = client.readQuery<{ getFriendSuggestions: any[] }>({
        query: GET_FRIEND_SUGGESTIONS,
        variables: { currentUserId: String(userId) },
      })

      if (reqCache?.getFriendRequests && sugCache?.getFriendSuggestions) {
        friendRequests.value = reqCache.getFriendRequests.map(mapToFriendItem)
        friendSuggestions.value = sugCache.getFriendSuggestions.map(mapToFriendItem)
        isLoaded.value = true
        return true
      }
    } catch {
      // Brak w cache Apollo
    }
    return false
  }

  const fetchFriendsData = async (force = false) => {
    const userId = authStore.currentUserId
    if (!userId || String(userId) === '0') return

    // Jeśli nie wymuszamy odświeżenia, sprawdź najpierw pamięć podręczną Apollo
    if (!force) {
      const hasCached = checkApolloCache(userId)
      if (hasCached) {
        return
      }
    }

    isLoading.value = true
    try {
      const fetchPolicy = force ? 'network-only' : 'cache-first'
      const [reqList, sugList] = await Promise.all([
        usersApi.getFriendRequests(userId, fetchPolicy).catch(() => []),
        usersApi.getFriendSuggestions(userId, fetchPolicy).catch(() => [])
      ])

      friendRequests.value = (reqList || []).map(mapToFriendItem)
      friendSuggestions.value = (sugList || []).map(mapToFriendItem)
      isLoaded.value = true
    } catch (err) {
      console.error('Failed to fetch friends data via Apollo:', err)
    } finally {
      isLoading.value = false
    }
  }

  const confirmRequest = async (senderId: string | number) => {
    const sId = String(senderId)
    const userId = String(authStore.currentUserId)
    
    // Optymistyczna aktualizacja lokalna
    friendRequests.value = friendRequests.value.filter(r => r.id !== sId)

    // Aktualizacja w cache Apollo Client
    try {
      const client = getApolloClient()
      const cached = client.readQuery<{ getFriendRequests: any[] }>({
        query: GET_FRIEND_REQUESTS_QUERY,
        variables: { currentUserId: userId },
      })
      if (cached?.getFriendRequests) {
        client.writeQuery({
          query: GET_FRIEND_REQUESTS_QUERY,
          variables: { currentUserId: userId },
          data: {
            getFriendRequests: cached.getFriendRequests.filter(
              (r: any) => String(r.userId || r.id) !== sId
            )
          }
        })
      }
    } catch {}

    try {
      await usersApi.acceptFriendRequest(sId, userId)
    } catch (err) {
      console.error('Failed to accept friend request:', err)
    }
  }

  const deleteRequest = (senderId: string | number) => {
    const sId = String(senderId)
    const userId = String(authStore.currentUserId)

    friendRequests.value = friendRequests.value.filter(r => r.id !== sId)

    try {
      const client = getApolloClient()
      const cached = client.readQuery<{ getFriendRequests: any[] }>({
        query: GET_FRIEND_REQUESTS_QUERY,
        variables: { currentUserId: userId },
      })
      if (cached?.getFriendRequests) {
        client.writeQuery({
          query: GET_FRIEND_REQUESTS_QUERY,
          variables: { currentUserId: userId },
          data: {
            getFriendRequests: cached.getFriendRequests.filter(
              (r: any) => String(r.userId || r.id) !== sId
            )
          }
        })
      }
    } catch {}
  }

  const addSuggestion = async (receiverId: string | number) => {
    const rId = String(receiverId)
    const userId = String(authStore.currentUserId)

    friendSuggestions.value = friendSuggestions.value.filter(s => s.id !== rId)

    try {
      const client = getApolloClient()
      const cached = client.readQuery<{ getFriendSuggestions: any[] }>({
        query: GET_FRIEND_SUGGESTIONS,
        variables: { currentUserId: userId },
      })
      if (cached?.getFriendSuggestions) {
        client.writeQuery({
          query: GET_FRIEND_SUGGESTIONS,
          variables: { currentUserId: userId },
          data: {
            getFriendSuggestions: cached.getFriendSuggestions.filter(
              (s: any) => String(s.userId || s.id) !== rId
            )
          }
        })
      }
    } catch {}

    try {
      await usersApi.sendFriendRequest(userId, rId)
    } catch (err) {
      console.error('Failed to send friend request:', err)
    }
  }

  const deleteSuggestion = (receiverId: string | number) => {
    const rId = String(receiverId)
    const userId = String(authStore.currentUserId)

    friendSuggestions.value = friendSuggestions.value.filter(s => s.id !== rId)

    try {
      const client = getApolloClient()
      const cached = client.readQuery<{ getFriendSuggestions: any[] }>({
        query: GET_FRIEND_SUGGESTIONS,
        variables: { currentUserId: userId },
      })
      if (cached?.getFriendSuggestions) {
        client.writeQuery({
          query: GET_FRIEND_SUGGESTIONS,
          variables: { currentUserId: userId },
          data: {
            getFriendSuggestions: cached.getFriendSuggestions.filter(
              (s: any) => String(s.userId || s.id) !== rId
            )
          }
        })
      }
    } catch {}
  }

  return {
    friendRequests,
    friendSuggestions,
    isLoaded,
    isLoading,
    fetchFriendsData,
    confirmRequest,
    deleteRequest,
    addSuggestion,
    deleteSuggestion
  }
})
