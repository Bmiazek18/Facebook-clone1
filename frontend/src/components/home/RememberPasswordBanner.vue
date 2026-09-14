<template>
  <div
    v-if="isVisible"
    class="w-full bg-theme-bg-secondary border border-theme-border rounded-lg shadow-sm p-4 mt-4 relative mx-auto max-w-[calc(100%-8px)] sm:max-w-full transition-all duration-200"
  >
    <!-- Close Button -->
    <button
      @click="handleDismiss"
      class="absolute top-3 right-3 p-1.5 rounded-full hover:bg-theme-hover text-theme-text-secondary transition-colors"
      :title="t('common.close') || 'Zamknij'"
    >
      <CloseIcon :size="20" />
    </button>

    <div class="flex items-start gap-3.5 pr-6">
      <!-- Profile Picture with Key Badge -->
      <div class="relative shrink-0 mt-0.5">
        <img
          :src="userAvatar"
          :alt="userName"
          class="w-12 h-12 rounded-full object-cover border border-theme-border"
        />
        <div
          class="absolute -bottom-1 -right-1 w-5 h-5 bg-[#1877F2] rounded-full flex items-center justify-center text-white ring-2 ring-white dark:ring-[#242526]"
        >
          <svg class="w-3 h-3" viewBox="0 0 24 24" fill="currentColor">
            <path
              d="M12.65 10C11.83 7.67 9.61 6 7 6c-3.31 0-6 2.69-6 6s2.69 6 6 6c2.61 0 4.83-1.67 5.65-4H17v4h4v-4h2v-4H12.65zM7 14c-1.1 0-2-.9-2-2s.9-2 2-2 2 .9 2 2-.9 2-2 2z"
            />
          </svg>
        </div>
      </div>

      <!-- Text Content -->
      <div class="flex-1">
        <h3 class="text-[17px] font-bold text-theme-text leading-tight mb-1">
          {{ t('auth.rememberPasswordTitle') || 'Zapamiętaj hasło' }}
        </h3>
        <p class="text-[14px] text-theme-text-secondary leading-snug mb-3">
          {{
            t('auth.rememberPasswordDesc') ||
            'Kiedy następnym razem zalogujesz się w tej przeglądarce, kliknij zdjęcie profilowe, zamiast wpisywać hasło.'
          }}
        </p>

        <!-- Actions -->
        <div class="flex items-center gap-2">
          <button
            @click="handleSave"
            class="bg-[#1877F2] hover:bg-[#166FE5] text-white font-semibold text-[14px] px-5 py-1.5 rounded-md transition-colors"
          >
            {{ t('auth.rememberPasswordOk') || 'OK' }}
          </button>
          <button
            @click="handleDismiss"
            class="bg-theme-bg-tertiary hover:bg-theme-hover text-theme-text font-semibold text-[14px] px-4 py-1.5 rounded-md transition-colors border border-theme-border"
          >
            {{ t('auth.rememberPasswordNotNow') || 'Nie teraz' }}
          </button>
        </div>
      </div>
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
