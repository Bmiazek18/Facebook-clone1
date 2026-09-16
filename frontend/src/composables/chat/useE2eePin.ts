import { ref, watch, computed } from 'vue'
import { hasVaultOnServer } from '@/utils/e2ee'
import { useAuthStore } from '@/stores/auth'

// Współdzielony, reaktywny stan w całej aplikacji
const hasPin = ref<boolean>(true)
const isLoaded = ref<boolean>(false)
let lastCheckedUserId: string | null = null

export function useE2eePin() {
  const authStore = useAuthStore()
  const currentUserId = computed(() => String(authStore.currentUserId || '').replace(/^user_/, ''))

  const initPinState = () => {
    const userId = currentUserId.value
    if (!userId || userId === '0' || userId === '1') {
      hasPin.value = true
      isLoaded.value = true
      return
    }

    // 1. Synchroniczny odczyt z pamięci podręcznej (brak migania i czekania)
    if (typeof window !== 'undefined') {
      const cached = localStorage.getItem(`e2ee_has_vault_${userId}`)
      if (cached !== null) {
        hasPin.value = cached === 'true'
        isLoaded.value = true
      }
    }

    // 2. Weryfikacja w tle tylko raz na sesję użytkownika
    if (lastCheckedUserId !== userId) {
      lastCheckedUserId = userId
      hasVaultOnServer(userId)
        .then((exists) => {
          hasPin.value = exists
          isLoaded.value = true
        })
        .catch(() => {})
    }
  }

  const setHasPin = (val: boolean) => {
    hasPin.value = val
    isLoaded.value = true
    const userId = currentUserId.value
    if (userId && typeof window !== 'undefined') {
      localStorage.setItem(`e2ee_has_vault_${userId}`, val ? 'true' : 'false')
    }
  }

  const refreshPinStatus = async (force = true) => {
    const userId = currentUserId.value
    if (!userId || userId === '0' || userId === '1') return true
    const exists = await hasVaultOnServer(userId, force)
    hasPin.value = exists
    isLoaded.value = true
    return exists
  }

  // Obserwator użytkownika
  watch(
    () => currentUserId.value,
    () => {
      initPinState()
    },
    { immediate: true }
  )

  return {
    hasPin,
    isLoaded,
    setHasPin,
    refreshPinStatus
  }
}
