import { defineStore } from 'pinia'
import { ref, computed, watch } from 'vue'
import { useAuthStore } from '@/stores/auth'
import gql from 'graphql-tag'
import type { Notification } from '@/types/Notification'
import { getApolloClient } from '@/utils/apollo'

export const useNotificationsStore = defineStore('notifications', () => {
  const authStore = useAuthStore()
  const notifications = ref<Notification[]>([])
  const loading = ref(false)

  const GET_NOTIFICATIONS = gql`
    query GetNotifications($userId: ID!) {
      getNotifications(userId: $userId) {
        id
        userId
        title
        message
        createdAt
        read
        sender {
          id
          firstName
          lastName
          avatarId
          avatar
        }
      }
    }
  `

  const MARK_NOTIFICATION_AS_READ = gql`
    mutation MarkNotificationAsRead($id: ID!) {
      markNotificationAsRead(id: $id)
    }
  `

  const refetch = async () => {
    const userId = authStore.currentUserId
    if (!userId) return
    loading.value = true
    try {
      const client = getApolloClient()
      const res = await client.query({
        query: GET_NOTIFICATIONS,
        variables: { userId: String(userId) },
        fetchPolicy: 'network-only',
      })
      notifications.value = (res.data?.getNotifications || []).map((n: any) => ({ ...n }))
    } catch (err) {
      console.error('Failed to fetch notifications:', err)
    } finally {
      loading.value = false
    }
  }

  const markAsRead = async (id: string) => {
    try {
      const client = getApolloClient()
      await client.mutate({
        mutation: MARK_NOTIFICATION_AS_READ,
        variables: { id: String(id) },
      })
      const notif = notifications.value.find((n) => String(n.id) === String(id))
      if (notif) {
        notif.read = true
      }
    } catch (err) {
      console.error('Failed to mark notification as read:', err)
    }
  }

  const unreadCount = computed(() => {
    return notifications.value.filter((n) => !n.read).length
  })

  const handleNewNotificationEvent = () => {
    refetch()
  }

  if (import.meta.client) {
    window.addEventListener('new-notification', handleNewNotificationEvent)
    watch(
      () => authStore.currentUserId,
      (newId) => {
        if (newId) {
          refetch()
        } else {
          notifications.value = []
        }
      },
      { immediate: true },
    )
  }

  return {
    notifications,
    loading,
    refetch,
    markAsRead,
    unreadCount,
  }
})

export default useNotificationsStore
