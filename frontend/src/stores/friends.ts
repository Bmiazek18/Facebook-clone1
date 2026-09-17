import { defineStore } from 'pinia'
import { ref } from 'vue'
import { usersApi } from '@/api/users'
import { useAuthStore } from '@/stores/auth'

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

  const fetchFriendsData = async (force = false) => {
    const userId = authStore.currentUserId
    if (!userId || String(userId) === '0') return

    if (isLoaded.value && !force) {
      return
    }

    isLoading.value = true
    try {
      const [reqList, sugList] = await Promise.all([
        usersApi.getFriendRequests(userId).catch(() => []),
        usersApi.getFriendSuggestions(userId).catch(() => [])
      ])

      friendRequests.value = (reqList || []).map((item: any) => {
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
      })

      friendSuggestions.value = (sugList || []).map((item: any) => {
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
      })

      isLoaded.value = true
    } catch (err) {
      console.error('Failed to fetch friends data:', err)
    } finally {
      isLoading.value = false
    }
  }

  const confirmRequest = async (senderId: string | number) => {
    const sId = String(senderId)
    try {
      const res = await usersApi.acceptFriendRequest(sId, authStore.currentUserId)
      if (res?.success) {
        friendRequests.value = friendRequests.value.filter(r => r.id !== sId)
      }
    } catch (err) {
      console.error('Failed to accept friend request:', err)
    }
  }

  const deleteRequest = (senderId: string | number) => {
    const sId = String(senderId)
    friendRequests.value = friendRequests.value.filter(r => r.id !== sId)
  }

  const addSuggestion = async (receiverId: string | number) => {
    const rId = String(receiverId)
    try {
      const res = await usersApi.sendFriendRequest(authStore.currentUserId, rId)
      if (res?.success) {
        friendSuggestions.value = friendSuggestions.value.filter(s => s.id !== rId)
      }
    } catch (err) {
      console.error('Failed to send friend request:', err)
    }
  }

  const deleteSuggestion = (receiverId: string | number) => {
    const rId = String(receiverId)
    friendSuggestions.value = friendSuggestions.value.filter(s => s.id !== rId)
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
