import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { useQuery, useMutation } from '@vue/apollo-composable'
import { useAuthStore } from '@/stores/auth'
import gql from 'graphql-tag'
import type { Notification } from '@/types/Notification'

export const useNotificationsStore = defineStore('notifications', () => {
  const authStore = useAuthStore()
  const notifications = ref<Notification[]>([])

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

  const variables = computed(() => ({
    userId: String(authStore.currentUserId)
  }))

  const { onResult, loading, refetch } = useQuery(GET_NOTIFICATIONS, variables, () => ({
    enabled: !!authStore.currentUserId,
    fetchPolicy: 'cache-and-network',
  }))

  onResult((queryResult) => {
    notifications.value = (queryResult.data?.getNotifications || []).map((n: any) => ({ ...n }))
  })

  const { mutate: markAsReadMutation } = useMutation(MARK_NOTIFICATION_AS_READ)

  const markAsRead = async (id: string) => {
    try {
      await markAsReadMutation({ id })
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
