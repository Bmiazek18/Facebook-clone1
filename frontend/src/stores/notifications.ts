import { defineStore } from 'pinia'
import { ref, computed, watch } from 'vue'
import { useAuthStore } from '@/stores/auth'
import type { Notification } from '@/types/Notification'
import { notificationsApi } from '@/api/notifications'

export const useNotificationsStore = defineStore('notifications', () => {
  const authStore = useAuthStore()
  const notifications = ref<Notification[]>([])
  const loading = ref(false)

  const refetch = async () => {
    const userId = authStore.currentUserId
    if (!userId) return
    loading.value = true
    try {
      notifications.value = await notificationsApi.getNotifications(userId)
    } catch (err) {
      console.error('Failed to fetch notifications:', err)
    } finally {
      loading.value = false
    }
  }

  const markAsRead = async (id: string) => {
    try {
      await notificationsApi.markAsRead(id)
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
