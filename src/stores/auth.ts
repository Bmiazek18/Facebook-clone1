import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { User } from '@/data/users'
import { getUserById } from '@/data/users'

export const useAuthStore = defineStore('auth', () => {
  // Current logged in user ID (hardcoded to 1 for now, Bartosz Miazek)
  const currentUserId = ref<number>(1)

  // Get current user data
  const currentUser = computed((): User | undefined => {
    return getUserById(currentUserId.value)
  })

  // Set current user
  const setCurrentUser = (userId: number) => {
    currentUserId.value = userId
  }

  // Logout (reset to null/0)
  const logout = () => {
    currentUserId.value = 0
  }

  return {
    currentUserId,
    currentUser,
    setCurrentUser,
    logout,
  }
})
