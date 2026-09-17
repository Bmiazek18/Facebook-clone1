<template>
  <div
    v-if="isVisible"
    class="w-[calc(100%-1.5rem)] mx-auto my-2 bg-[#f8f9fb] dark:bg-theme-bg-tertiary rounded-2xl shadow-sm border border-theme-border p-4 relative select-none transition-all duration-200"
  >
    <!-- Przycisk zamknięcia (X) -->
    <button
      @click="handleDismiss"
      type="button"
      class="absolute top-3.5 right-3.5 text-[#65676B] hover:text-[#050505] dark:text-[#B0B3B8] dark:hover:text-white p-1 rounded-full transition-colors cursor-pointer"
      :title="t('common.close') || 'Zamknij'"
    >
      <svg class="w-5 h-5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round">
        <line x1="18" y1="6" x2="6" y2="18"></line>
        <line x1="6" y1="6" x2="18" y2="18"></line>
      </svg>
    </button>

    <!-- Stan sukcesu -->
    <div v-if="isSuccess" class="flex items-center gap-3 py-2 pr-6">
      <div class="w-9 h-9 rounded-full bg-green-500/15 text-green-600 dark:text-green-400 flex items-center justify-center shrink-0">
        <CheckIcon :size="22" />
      </div>
      <div>
        <h4 class="text-[15px] font-bold text-green-700 dark:text-green-400 leading-tight mb-0.5">
          {{ t('notifications.pushEnabledTitle') || 'Powiadomienia włączone!' }}
        </h4>
        <p class="text-[13px] text-green-600/90 dark:text-green-400/80 leading-snug">
          {{ t('notifications.pushEnabledDesc') || 'Będziesz otrzymywać powiadomienia w czasie rzeczywistym.' }}
        </p>
      </div>
    </div>

    <!-- Główna zawartość -->
    <div v-else>
      <div class="pr-6 mb-4">
        <h3 class="text-[17px] font-semibold text-theme-text leading-tight mb-1">
          {{ t('notifications.pushDisabledTitle') || 'Powiadomienia push są wyłączone' }}
        </h3>
        <p class="text-[15px] text-[#65676B] dark:text-[#B0B3B8] leading-snug">
          {{ t('notifications.pushDisabledDesc') || 'Włącz powiadomienia, aby być na bieżąco' }}
        </p>
      </div>

      <!-- Akcje -->
      <div class="flex items-center gap-3">
        <button
          @click="handleEnablePush"
          :disabled="isLoading"
          type="button"
          class="flex-1 bg-[#E7F3FF] hover:bg-[#DBEAFA] dark:bg-[#263951] dark:hover:bg-[#2d4361] text-[#0064E0] dark:text-[#4599FF] font-semibold text-[15px] py-2 rounded-xl transition-colors cursor-pointer text-center inline-flex items-center justify-center gap-2 disabled:opacity-50"
        >
          <span v-if="isLoading" class="w-4 h-4 border-2 border-current border-t-transparent rounded-full animate-spin"></span>
          <span>{{ t('notifications.enable') || 'Włącz' }}</span>
        </button>

        <button
          @click="handleDismiss"
          type="button"
          class="flex-1 bg-[#E4E6EB] hover:bg-[#D8DADF] dark:bg-[#3A3B3C] dark:hover:bg-[#4E4F50] text-[#050505] dark:text-[#E4E6EB] font-semibold text-[15px] py-2 rounded-xl transition-colors cursor-pointer text-center"
        >
          {{ t('notifications.notNow') || 'Nie teraz' }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { useWebPush } from '@/composables/shared/useWebPush'
import { useAuthStore } from '@/stores/auth'

import CloseIcon from 'vue-material-design-icons/Close.vue'
import BellRingOutlineIcon from 'vue-material-design-icons/BellRingOutline.vue'
import CheckIcon from 'vue-material-design-icons/Check.vue'

const emit = defineEmits<{
  (e: 'enabled'): void
  (e: 'dismiss'): void
}>()

const { t } = useI18n()
const authStore = useAuthStore()
const { isSupported, getPermission, requestPermissionAndRegister, fetchPushPromptStatus, dismissPushPrompt } = useWebPush()

const currentUserId = computed(() => {
  return String(authStore.currentUserId || authStore.originalUserId || '').replace(/^user_/, '').trim()
})

const getIsSynchronouslyVisible = (): boolean => {
  if (!import.meta.client || typeof window === 'undefined') return false
  if (!isSupported()) return false

  const perm = getPermission()
  if (perm !== 'default') return false

  const uid = currentUserId.value
  if (uid) {
    const localDismissed = localStorage.getItem(`push_prompt_dismissed_${uid}`) === 'true'
    if (localDismissed) return false
  } else {
    const globalDismissed = localStorage.getItem('push_prompt_dismissed_global') === 'true'
    if (globalDismissed) return false
  }

  return true
}

// Stan początkowy ustalany natychmiast synchronicznie od początku bez mignięć
const isVisible = ref(getIsSynchronouslyVisible())
const isLoading = ref(false)
const isSuccess = ref(false)

const checkStatus = async () => {
  const syncVal = getIsSynchronouslyVisible()
  isVisible.value = syncVal
  if (!syncVal) return

  // Weryfikacja w tle z serwerem (cicha, bez mignięć i opóźnień w UI)
  if (currentUserId.value) {
    try {
      const status = await fetchPushPromptStatus(currentUserId.value)
      if (status.dismissed || status.enabled) {
        isVisible.value = false
        localStorage.setItem(`push_prompt_dismissed_${currentUserId.value}`, 'true')
      }
    } catch {
      // Ignorujemy ewentualny błąd sieci, zostajemy przy stanie lokalnym
    }
  }
}

const handleEnablePush = async () => {
  const uid = currentUserId.value
  if (!uid) return
  isLoading.value = true

  try {
    const granted = await requestPermissionAndRegister(uid)
    if (granted) {
      isSuccess.value = true
      localStorage.setItem(`push_prompt_dismissed_${uid}`, 'true')
      emit('enabled')
      setTimeout(() => {
        isVisible.value = false
      }, 1500)
    } else {
      isVisible.value = false
      localStorage.setItem(`push_prompt_dismissed_${uid}`, 'true')
    }
  } catch (err) {
    console.error('Failed to enable desktop push notifications:', err)
    isVisible.value = false
  } finally {
    isLoading.value = false
  }
}

const handleDismiss = async () => {
  isVisible.value = false
  emit('dismiss')
  const uid = currentUserId.value
  if (uid) {
    localStorage.setItem(`push_prompt_dismissed_${uid}`, 'true')
    await dismissPushPrompt(uid)
  } else {
    localStorage.setItem('push_prompt_dismissed_global', 'true')
  }
}

watch(
  () => currentUserId.value,
  () => {
    checkStatus()
  }
)

onMounted(() => {
  checkStatus()
})
</script>
