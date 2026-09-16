<template>
  <div
    v-if="isVisible"
    class="w-full max-w-[420px] bg-white dark:bg-[#242526] rounded-2xl shadow-sm border border-[#E4E6EB] dark:border-[#393A3B] p-4 relative select-none mx-auto my-2 transition-all duration-200"
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
        <h3 class="text-[17px] font-bold text-[#050505] dark:text-[#E4E6EB] leading-tight mb-1">
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

const isVisible = ref(false)
const isLoading = ref(false)
const isSuccess = ref(false)

const currentUserId = computed(() => {
  return String(authStore.currentUserId || '').replace(/^user_/, '')
})

const checkStatus = async () => {
  if (!isSupported()) {
    isVisible.value = false
    return
  }

  const perm = getPermission()
  if (perm === 'granted' || perm === 'denied') {
    isVisible.value = false
    return
  }

  // Sprawdź w localStorage lub na serwerze czy użytkownik odrzucił prompt
  if (currentUserId.value) {
    const localDismissed = localStorage.getItem(`push_prompt_dismissed_${currentUserId.value}`) === 'true'
    if (localDismissed) {
      isVisible.value = false
      return
    }

    try {
      const status = await fetchPushPromptStatus(currentUserId.value)
      if (status.dismissed || status.enabled) {
        isVisible.value = false
        return
      }
    } catch {
      // Fallback
    }
  }

  isVisible.value = true
}

const handleEnablePush = async () => {
  if (!currentUserId.value) return
  isLoading.value = true

  try {
    const granted = await requestPermissionAndRegister(currentUserId.value)
    if (granted) {
      isSuccess.value = true
      emit('enabled')
      setTimeout(() => {
        isVisible.value = false
      }, 1500)
    } else {
      isVisible.value = false
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
  if (currentUserId.value) {
    await dismissPushPrompt(currentUserId.value)
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
