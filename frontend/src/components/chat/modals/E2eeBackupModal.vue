<script setup lang="ts">
import { ref, onMounted, computed, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { useAuthStore } from '@/stores/auth'
import {
  hasLocalPrivateKey,
  initIdentityKeys,
  deleteLocalPrivateKey,
  setupVaultPin,
  unlockVaultAndRestoreHistory,
  hasVaultOnServer
} from '@/utils/e2ee'
import { useE2eePin } from '@/composables/chat/useE2eePin'

const { setHasPin } = useE2eePin()

const props = withDefaults(
  defineProps<{
    modelValue?: boolean
    forceOpen?: boolean
    initialMode?: 'setup' | 'restore'
  }>(),
  {
    modelValue: undefined,
    forceOpen: false,
    initialMode: undefined
  }
)

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
  (e: 'close'): void
  (e: 'pin-saved'): void
}>()

const { t } = useI18n()
const authStore = useAuthStore()
const currentUserId = computed(() => String(authStore.currentUserId || '').replace(/^user_/, ''))

const isVisible = ref(props.forceOpen || props.modelValue || false)
const mode = ref<'setup' | 'restore'>(props.initialMode || 'setup')
const pin = ref('')
const pinConfirm = ref('')
const errorMsg = ref('')
const successMsg = ref('')
const loading = ref(false)

watch(
  () => props.modelValue,
  (val) => {
    if (val !== undefined) {
      isVisible.value = val
    }
  }
)

watch(
  () => props.initialMode,
  (val) => {
    if (val) {
      mode.value = val
    }
  }
)

function closeModal() {
  isVisible.value = false
  emit('update:modelValue', false)
  emit('close')
}

// Atrapa funkcji eksportu historii
async function exportLocalChatHistory(): Promise<string> {
  return JSON.stringify({ exportedAt: Date.now(), note: 'signal-vault-v1' })
}

async function checkE2eeState() {
  if (!currentUserId.value) return

  loading.value = true
  try {
    await initIdentityKeys()
    const hasLocal = await hasLocalPrivateKey()
    const hasVault = await hasVaultOnServer(currentUserId.value)

    if (!hasLocal && hasVault) {
      mode.value = 'restore'
      isVisible.value = true
    } else if (hasLocal && !hasVault) {
      mode.value = 'setup'
      isVisible.value = true
    } else if (!hasLocal && !hasVault) {
      mode.value = 'setup'
      isVisible.value = true
    } else {
      if (props.modelValue === undefined && !props.forceOpen) {
        isVisible.value = false
      }
    }
  } catch (err) {
    console.error('Failed checking E2EE PIN backup state:', err)
  } finally {
    loading.value = false
  }
}

async function handleSetup() {
  if (pin.value.length !== 6 || !/^\d+$/.test(pin.value)) {
    errorMsg.value = 'PIN musi składać się z 6 cyfr.'
    return
  }
  if (pin.value !== pinConfirm.value) {
    errorMsg.value = 'Kody PIN nie są zgodne.'
    return
  }

  errorMsg.value = ''
  loading.value = true

  try {
    await initIdentityKeys()
    const history = await exportLocalChatHistory()
    await setupVaultPin(pin.value, currentUserId.value, history)

    setHasPin(true)
    successMsg.value = 'Bezpieczna pamięć PIN została skonfigurowana!'
    setTimeout(() => {
      closeModal()
      emit('pin-saved')
      successMsg.value = ''
    }, 1500)
  } catch (err) {
    console.error('Failed to back up E2EE vault:', err)
    errorMsg.value = 'Błąd podczas tworzenia kopii zapasowej na serwerze.'
  } finally {
    loading.value = false
  }
}

async function handleRestore() {
  if (pin.value.length !== 6 || !/^\d+$/.test(pin.value)) {
    errorMsg.value = 'PIN musi składać się z 6 cyfr.'
    return
  }

  errorMsg.value = ''
  loading.value = true

  try {
    const historyJson = await unlockVaultAndRestoreHistory(pin.value, currentUserId.value)
    void historyJson // Wykorzystaj przywróconą historię

    await initIdentityKeys()
    setHasPin(true)
    successMsg.value = 'Urządzenie zweryfikowane! Odzyskano sejf.'
    setTimeout(() => {
      closeModal()
      emit('pin-saved')
      successMsg.value = ''
      window.location.reload()
    }, 1500)
  } catch (err) {
    console.error('Failed to restore vault:', err)
    errorMsg.value = 'Niepoprawny kod PIN. Spróbuj ponownie.'
  } finally {
    loading.value = false
  }
}

async function handleReset() {
  if (
    !confirm(
      'Czy na pewno chcesz zresetować szyfrowanie? Spowoduje to utratę lokalnych kluczy Signal. Nowe wiadomości będą szyfrowane nowym kluczem.'
    )
  ) {
    return
  }

  errorMsg.value = ''
  loading.value = true
  try {
    await deleteLocalPrivateKey()
    await initIdentityKeys()
    mode.value = 'setup'
    pin.value = ''
    pinConfirm.value = ''
    errorMsg.value = ''
  } catch (err) {
    errorMsg.value = 'Błąd podczas resetowania szyfrowania.'
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  if (props.forceOpen) {
    isVisible.value = true
    if (props.initialMode) mode.value = props.initialMode
    return
  }
  if (props.modelValue === undefined) {
    setTimeout(() => {
      checkE2eeState()
    }, 1000)
  }
})

defineExpose({
  checkE2eeState,
  open: (m: 'setup' | 'restore' = 'setup') => {
    mode.value = m
    isVisible.value = true
    pin.value = ''
    pinConfirm.value = ''
    errorMsg.value = ''
    successMsg.value = ''
  }
})
</script>

<template>
  <div
    v-if="isVisible"
    class="fixed inset-0 z-[9999] flex items-center justify-center bg-gray-600/60 dark:bg-black/80 px-4 backdrop-blur-sm"
  >
    <!-- Modal Container - Adjusted shadow and background to match image -->
    <div class="relative w-full max-w-[460px] bg-white dark:bg-[#1C1B1F] rounded-[28px] shadow-lg p-7 overflow-hidden text-center transition-all border border-gray-100 dark:border-gray-800">

      <!-- Close Button - Just the X icon as in the image -->
      <button
        @click="closeModal"
        class="absolute top-5 right-5 w-10 h-10 flex items-center justify-center transition-colors text-gray-600 dark:text-gray-400 hover:text-black dark:hover:text-white rounded-full hover:bg-gray-100 dark:hover:bg-gray-800"
      >
        <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
        </svg>
      </button>

      <!-- Graphical Icon (***_) - Styled to match the image's "glassy pill" look -->
      <div class="mx-auto w-[120px] h-[54px] bg-gradient-to-b from-[#D2E3FC] to-[#AECBFA] dark:from-[#3C4043] dark:to-[#202124] rounded-full flex items-center justify-center mb-8 shadow-inner relative overflow-hidden backdrop-blur-sm">
        <div class="absolute inset-0 bg-white/20 dark:bg-black/10 rounded-full blur-[2px]"></div>
        <span class="text-[#1A73E8] dark:text-[#8AB4F8] text-[32px] font-mono font-bold tracking-[0.15em] relative z-10" style="transform: translateY(2px);">***_</span>
      </div>

      <!-- Headers - Adjusted typography to match image -->
      <h2 class="text-[24px] font-extrabold text-[#1C1B1F] dark:text-[#E6E1E5] mb-4 leading-tight px-2">
        {{ mode === 'restore' ? 'Podaj kod PIN, aby przywrócić czaty' : 'Utwórz kod PIN, aby uniknąć utraty historii czatów' }}
      </h2>
      <p class="text-[16px] text-gray-700 dark:text-[#CAC4D0] px-4 mb-10 leading-relaxed font-normal">
        {{
          mode === 'restore'
            ? 'Brakuje niektórych wiadomości. Podaj kod PIN, aby przywrócić historię czatu.'
            : 'Ten kod PIN umożliwi Ci dostęp do historii czatów, jeśli użyjesz innej przeglądarki lub urządzenia.'
        }}
      </p>

      <!-- PIN Input - Styled inputs to exactly match image -->
      <div class="relative w-max mx-auto mb-10">
        <input
          v-model="pin"
          type="tel"
          maxlength="6"
          class="absolute inset-0 w-full h-full opacity-0 z-10 cursor-text"
          :disabled="loading"
          autofocus
        />
        <div class="flex gap-2.5">
          <div
            v-for="i in 6"
            :key="i"
            class="w-[56px] h-[70px] rounded-[16px] flex items-center justify-center transition-all duration-100"
            :class="[
              pin.length === i - 1
                ? 'border-[3px] border-[#1A73E8] dark:border-[#8AB4F8] bg-white dark:bg-[#1C1B1F] shadow-md'
                : 'border-2 border-transparent bg-[#F1F3F4] dark:bg-[#2A2B2F]',
            ]"
          >
            <span v-if="pin[i - 1]" class="text-4xl font-sans text-black dark:text-white leading-none">•</span>
            <span v-else class="text-4xl font-light text-gray-500 dark:text-gray-500 leading-none" style="transform: translateY(-2px);">-</span>
          </div>
        </div>
      </div>

      <!-- PIN Confirmation (setup mode only) -->
      <div v-if="mode === 'setup'" class="relative w-max mx-auto mb-10">
        <p class="text-sm font-semibold text-gray-600 dark:text-gray-400 tracking-wide mb-3 text-center">Powtórz kod PIN</p>
        <input
          v-model="pinConfirm"
          type="tel"
          maxlength="6"
          class="absolute inset-0 top-8 w-full h-full opacity-0 z-10 cursor-text"
          :disabled="loading"
        />
        <div class="flex gap-2.5">
          <div
            v-for="i in 6"
            :key="i"
            class="w-[56px] h-[70px] rounded-[16px] flex items-center justify-center transition-all duration-100"
            :class="[
              pinConfirm.length === i - 1
                ? 'border-[3px] border-[#1A73E8] dark:border-[#8AB4F8] bg-white dark:bg-[#1C1B1F] shadow-md'
                : 'border-2 border-transparent bg-[#F1F3F4] dark:bg-[#2A2B2F]',
            ]"
          >
            <span v-if="pinConfirm[i - 1]" class="text-4xl font-sans text-black dark:text-white leading-none">•</span>
            <span v-else class="text-4xl font-light text-gray-500 dark:text-gray-500 leading-none" style="transform: translateY(-2px);">-</span>
          </div>
        </div>
      </div>

      <!-- Error/Success Messages -->
      <div class="h-6 mb-4">
        <div v-if="errorMsg" class="text-red-500 text-sm font-medium animate-pulse">
          {{ errorMsg }}
        </div>
        <div v-if="successMsg" class="text-green-600 dark:text-green-400 text-sm font-medium">
          {{ successMsg }}
        </div>
      </div>

      <!-- Actions -->
      <div class="mt-2 flex flex-col gap-5">

        <button
          @click="mode === 'restore' ? handleRestore() : handleSetup()"
          class="w-full py-4 bg-[#1A73E8] hover:bg-blue-700 dark:bg-[#8AB4F8] dark:text-[#1C1B1F] dark:hover:bg-blue-300 text-white font-bold rounded-full transition-colors flex items-center justify-center gap-3 text-[16px]"
          :disabled="loading || pin.length < 6"
          :class="{ 'opacity-50 cursor-not-allowed': pin.length < 6 }"
        >
          <span v-if="loading" class="w-5 h-5 border-2 border-white dark:border-black border-t-transparent rounded-full animate-spin"></span>
          <span>{{ mode === 'restore' ? 'Weryfikuj PIN' : 'Zapisz i aktywuj sejf' }}</span>
        </button>

        <!-- Reset Encryption Link -->
        <button
          v-if="mode === 'restore'"
          @click="handleReset"
          class="text-[#1A73E8] dark:text-[#8AB4F8] font-semibold text-[15px] hover:underline transition-all"
          :disabled="loading"
        >Zamiast tego zresetuj szyfrowanie</button>

      </div>
    </div>
  </div>
</template>

<style scoped>
/* Zapewnia, że font mono dla gwiazdek wygląda dobrze */
.font-mono {
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, "Liberation Mono", "Courier New", monospace;
}
</style>
