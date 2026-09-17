<template>
  <div
    v-if="isVisible"
    class="w-full  bg-theme-bg-secondary  rounded-xl shadow-md p-5 relative mx-auto my-3 transition-all duration-200"
  >
    <!-- Top Bar: Facebook Icon, Center Screen & Close Button -->
    <div class="flex items-start justify-between mb-1">
      <!-- FB Logo -->
      <svg class="w-8 h-8 text-[#1877F2] shrink-0" viewBox="0 0 24 24" fill="currentColor">
        <path
          d="M24 12.073c0-6.627-5.373-12-12-12s-12 5.373-12 12c0 5.99 4.388 10.954 10.125 11.854v-8.385H7.078v-3.47h3.047V9.43c0-3.007 1.792-4.669 4.533-4.669 1.312 0 2.686.235 2.686.235v2.953H15.83c-1.491 0-1.956.925-1.956 1.874v2.25h3.328l-.532 3.47h-2.796v8.385C19.612 23.027 24 18.062 24 12.073z"
        />
      </svg>

      <!-- Center Monitor Illustration -->
      <div class="flex justify-center -mt-1">
        <div class="flex flex-col items-center">
          <!-- Monitor Frame -->
          <div class="w-24 h-16 bg-[#3a3b3c] rounded-[4px] p-1 shadow-sm flex flex-col justify-between">
            <!-- Screen Area -->
            <div class="w-full h-full bg-white dark:bg-[#18191a] rounded-[2px] p-1 flex gap-1 items-center">
              <!-- Left Profile Box -->
              <div class="w-6 h-8 border border-gray-200 dark:border-gray-600 rounded flex flex-col items-center justify-end pb-0.5">
                <div class="w-2.5 h-2.5 rounded-full bg-gray-300 dark:bg-gray-500 mb-0.5"></div>
                <div class="w-4 h-2 rounded-t-full bg-gray-300 dark:bg-gray-500"></div>
              </div>
              <!-- Right Mock Inputs -->
              <div class="flex-1 flex flex-col gap-1 pr-0.5">
                <span class="text-[6px] font-bold text-[#1877F2] leading-none">facebook</span>
                <div class="w-full h-1.5 border border-gray-300 dark:border-gray-600 rounded-[1px]"></div>
                <div class="w-full h-1.5 bg-[#1877F2] rounded-[1px]"></div>
                <div class="w-3/4 h-1.5 bg-[#42b72a] rounded-[1px]"></div>
              </div>
            </div>
          </div>
          <!-- Monitor Stand -->
          <div class="w-2.5 h-2 bg-[#3a3b3c]"></div>
          <div class="w-10 h-1 bg-[#3a3b3c] rounded-t-sm"></div>
        </div>
      </div>

      <!-- Close Button -->
      <button
        @click="handleDismiss"
        class="text-gray-500 hover:text-gray-700 dark:text-gray-400 dark:hover:text-gray-200 transition-colors p-1"
        :title="t('common.close') || 'Zamknij'"
      >
        <CloseIcon :size="24" />
      </button>
    </div>

    <!-- Text Content -->
    <div class="mt-3 mb-4">
      <h3 class="text-[17px] font-bold text-theme-text leading-tight mb-1.5">
        {{ t('auth.rememberPasswordTitle') || 'Zapamiętaj hasło' }}
      </h3>
      <p class="text-[14px] text-theme-text-secondary leading-normal">
        {{
          t('auth.rememberPasswordDesc') ||
          'Kiedy następnym razem zalogujesz się w tej przeglądarce, kliknij zdjęcie profilowe, zamiast wpisywać hasło.'
        }}
      </p>
    </div>

    <!-- Actions -->
    <div class="flex items-center gap-3">
      <button
        @click="handleSave"
        class="flex-1 bg-[#E7F3FF] hover:bg-[#DBEAFA] dark:bg-[#263951] dark:hover:bg-[#2d4361] text-[#1877F2] dark:text-[#4599FF] font-semibold text-[15px] py-2 rounded-md transition-colors"
      >
        {{ t('auth.rememberPasswordOk') || 'OK' }}
      </button>
      <button
        @click="handleDismiss"
        class="flex-1 bg-[#E4E6EB] hover:bg-[#D8DADF] dark:bg-[#3A3B3C] dark:hover:bg-[#4E4F50] text-[#050505] dark:text-[#E4E6EB] font-semibold text-[15px] py-2 rounded-md transition-colors"
      >
        {{ t('auth.rememberPasswordNotNow') || 'Nie teraz' }}
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import CloseIcon from 'vue-material-design-icons/Close.vue'
import { useAuthStore } from '@/stores/auth'
import { useNotify } from '@/composables/shared/useNotify'
import DefaultAvatar from '@/assets/images/default_avatar.png'

const { t } = useI18n()
const authStore = useAuthStore()
const notify = useNotify()

const isVisible = ref(false)

const currentUser = computed(() => authStore.currentUser)
const userAvatar = computed(() => currentUser.value?.avatar || DefaultAvatar)
const userName = computed(() => currentUser.value?.name || 'Użytkownik')

onMounted(() => {
  if (typeof window === 'undefined') return
  const userId = authStore.currentUserId
  if (!userId || String(userId) === '0') return

  const isSaved = localStorage.getItem(`fb_saved_login_${userId}`)
  const isDismissed = localStorage.getItem(`fb_dismiss_save_login_${userId}`)

  if (!isSaved && !isDismissed) {
    isVisible.value = true
  }
})

const handleSave = () => {
  const userId = authStore.currentUserId
  if (!userId) return

  try {
    const savedAccounts = JSON.parse(localStorage.getItem('fb_saved_accounts') || '[]')
    const user = authStore.currentUser

    const accountData = {
      id: String(userId),
      name: user?.name || userName.value,
      avatar: user?.avatar || '',
      email: (user as any)?.email || '',
      savedAt: new Date().toISOString()
    }

    const existingIdx = savedAccounts.findIndex((a: any) => String(a.id) === String(userId))
    if (existingIdx >= 0) {
      savedAccounts[existingIdx] = accountData
    } else {
      savedAccounts.push(accountData)
    }

    localStorage.setItem('fb_saved_accounts', JSON.stringify(savedAccounts))
    localStorage.setItem(`fb_saved_login_${userId}`, 'true')
  } catch (err) {
    console.error('Failed to save login info in localStorage:', err)
  }

  isVisible.value = false
  notify.success(t('auth.savedLoginSuccess') || 'Zapisano dane logowania w tej przeglądarce.')
}

const handleDismiss = () => {
  const userId = authStore.currentUserId
  if (userId) {
    localStorage.setItem(`fb_dismiss_save_login_${userId}`, 'true')
  }
  isVisible.value = false
}
</script>
