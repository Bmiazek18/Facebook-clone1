import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { User } from '@/utils/users'
import { getUserById } from '@/utils/users'
import { useConversationsStore } from '@/stores/conversations'

import { clearAllMessages } from '@/utils/indexedDb'

export const useAuthStore = defineStore('auth', () => {
  const getInitialUserId = (): string | number => {
    if (typeof window !== 'undefined') {
      const stored = localStorage.getItem('auth-current-user-id')
      if (stored && /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i.test(stored)) {
        return stored
      }
    }
    return '1e4332f6-5a7a-3210-b5fb-fb92c7c60cce' // Jan Wiśniewski
  }

  const currentUserId = ref<string | number>(getInitialUserId())

  // Get current user data
  const currentUser = computed((): User | undefined => {
    return getUserById(currentUserId.value)
  })

  // Set current user
  const setCurrentUser = async (userId: string | number) => {
    currentUserId.value = userId
    if (typeof window !== 'undefined') {
      localStorage.setItem('auth-current-user-id', String(userId))
      try {
        const conversationsStore = useConversationsStore()
        conversationsStore.clearState()
        console.log('State and IndexedDB cleared on user change successfully.')
      } catch (err) {
        console.error('Failed to clear state on user change:', err)
      }
    }
  }

  // Logout (reset to null/0)
  const logout = () => {
    currentUserId.value = 0
    if (typeof window !== 'undefined') {
      localStorage.removeItem('auth-current-user-id')
      try {
        const conversationsStore = useConversationsStore()
        conversationsStore.clearState()
        console.log('State and IndexedDB cleared on logout successfully.')
      } catch (err) {
        console.error('Failed to clear state on logout:', err)
      }
    }
  }

  return {
    currentUserId,
    currentUser,
    setCurrentUser,
    logout,
  }
})
