<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useAuthStore } from '@/stores/auth'
import {
  hasLocalPrivateKey,
  initIdentityKeys,
  deleteLocalPrivateKey,
  setupVaultPin,
  unlockVaultAndRestoreHistory,
  hasVaultOnServer
} from '@/utils/e2ee'

const authStore = useAuthStore()
const currentUserId = computed(() => String(authStore.currentUserId || '').replace(/^user_/, ''))

const isVisible = ref(false)
const mode = ref<'setup' | 'restore'>('setup')
const pin = ref('')
const pinConfirm = ref('')
const errorMsg = ref('')
const successMsg = ref('')
const loading = ref(false)

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
      isVisible.value = false
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

    successMsg.value = 'Bezpieczna pamięć PIN została skonfigurowana!'
    setTimeout(() => {
      isVisible.value = false
      successMsg.value = ''
    }, 2000)
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
    // Historia jest odszyfrowywana lokalnie; pełny import IDB można dociągnąć osobno.
    void historyJson

    await initIdentityKeys()
    successMsg.value = 'Urządzenie zweryfikowane! Odzyskano sejf.'
    setTimeout(() => {
      isVisible.value = false
      successMsg.value = ''
      window.location.reload()
    }, 2000)
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
  setTimeout(() => {
    checkE2eeState()
  }, 1000)
})
</script>

<template>
 < <div
    v-if="isVisible"

      class="fixed inset-0 z-9999 flex items-center justify-center bg-gray-200/80 dark:bg-black/80 px-2"
  >>
    <div class="relative w-full max-w-[440px] bg-white dark:bg-[#242526] rounded-[24px] shadow-2xl p-8 overflow-hidden text-center transition-all">

      <!-- Przycisk zamknięcia (X) -->
      <button
        @click="isVisible = false"
        class="absolute top-4 right-4 w-9 h-9 bg-gray-100 dark:bg-gray-800 hover:bg-gray-200 dark:hover:bg-gray-700 rounded-full flex items-center justify-center transition-colors text-gray-500"
      >
        <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
        </svg>
      </button>

      <!-- Graficzna ikona z gwiazdkami -->
      <div class="mx-auto w-[104px] h-[44px] bg-[#E8F0FE] dark:bg-[#1A73E8]/20 text-[#1A73E8] dark:text-[#669DF6] rounded-full flex items-center justify-center text-[26px] font-bold tracking-widest mb-6">
        <span style="transform: translateY(3px);">***_</span>
      </div>

      <!-- Nagłówki -->
      <h2 class="text-[22px] font-bold text-black dark:text-white mb-3 leading-tight">
        {{ mode === 'restore' ? 'Podaj kod PIN, aby przywrócić czaty' : 'Skonfiguruj bezpieczną pamięć' }}
      </h2>
      <p class="text-[15px] text-gray-600 dark:text-gray-400 px-2 mb-8 leading-relaxed">
        {{
          mode === 'restore'
            ? 'Brakuje niektórych wiadomości. Podaj kod PIN, aby przywrócić historię czatu.'
            : 'Utwórz 6-cyfrowy kod PIN, aby móc bezpiecznie odzyskać historię czatów na innych urządzeniach.'
        }}
      </p>

      <!-- Wprowadzanie PIN (Pojedynczy ukryty input + stylizowane boksy dla płynności wpisywania) -->
      <div class="relative w-max mx-auto mb-6">
        <input
          v-model="pin"
          type="tel"
          maxlength="6"
          class="absolute inset-0 w-full h-full opacity-0 z-10 cursor-text"
          :disabled="loading"
        />
        <div class="flex gap-2">
          <div
            v-for="i in 6"
            :key="i"
            class="w-[48px] h-[58px] rounded-[10px] flex items-center justify-center text-3xl font-bold transition-all"
            :class="[
              pin.length === i - 1
                ? 'border-2 border-[#1A73E8] bg-white dark:bg-[#1c1d1e] shadow-sm'
                : 'border-2 border-transparent bg-[#F4F5F7] dark:bg-[#3a3b3c]',
            ]"
          >
            <span v-if="pin[i - 1]" class="text-black dark:text-white">•</span>
            <span v-else class="text-[#202124] dark:text-gray-400">-</span>
          </div>
        </div>
      </div>

      <!-- Potwierdzenie PIN (tylko dla trybu 'setup') -->
      <div v-if="mode === 'setup'" class="relative w-max mx-auto mb-8">
        <p class="text-xs font-semibold text-gray-500 uppercase tracking-wider mb-2 text-left">{{ $t('chat.potwierdzPin') }}</p>
        <input
          v-model="pinConfirm"
          type="tel"
          maxlength="6"
          class="absolute inset-0 top-6 w-full h-full opacity-0 z-10 cursor-text"
          :disabled="loading"
        />
        <div class="flex gap-2">
          <div
            v-for="i in 6"
            :key="i"
            class="w-[48px] h-[58px] rounded-[10px] flex items-center justify-center text-3xl font-bold transition-all"
            :class="[
              pinConfirm.length === i - 1
                ? 'border-2 border-[#1A73E8] bg-white dark:bg-[#1c1d1e] shadow-sm'
                : 'border-2 border-transparent bg-[#F4F5F7] dark:bg-[#3a3b3c]',
            ]"
          >
            <span v-if="pinConfirm[i - 1]" class="text-black dark:text-white">•</span>
            <span v-else class="text-[#202124] dark:text-gray-400">-</span>
          </div>
        </div>
      </div>

      <!-- Komunikaty Błędów/Sukcesu -->
      <div v-if="errorMsg" class="mb-4 text-red-500 text-sm font-medium">
        {{ errorMsg }}
      </div>
      <div v-if="successMsg" class="mb-4 text-green-500 text-sm font-medium">
        {{ successMsg }}
      </div>

      <!-- Akcje -->
      <div class="mt-4 flex flex-col gap-4">

        <button
          @click="mode === 'restore' ? handleRestore() : handleSetup()"
          class="w-full py-3.5 bg-[#1A73E8] hover:bg-blue-700 text-white font-semibold rounded-[12px] transition shadow-md flex items-center justify-center gap-2"
          :disabled="loading || pin.length < 6"
          :class="{ 'opacity-50 cursor-not-allowed': pin.length < 6 }"
        >
          <span v-if="loading" class="w-5 h-5 border-2 border-white border-t-transparent rounded-full animate-spin"></span>
          <span>{{ mode === 'restore' ? 'Weryfikuj PIN' : 'Zapisz i aktywuj sejf' }}</span>
        </button>

        <!-- Odpowiednik napisu "Zamiast tego użyj kodu jednorazowego" (Resetowanie) -->
        <button
          v-if="mode === 'restore'"
          @click="handleReset"
          class="text-[#1A73E8] dark:text-[#669DF6] font-semibold text-[15px] hover:underline"
          :disabled="loading"
        >{{ $t('chat.zamiastTegoZresetujSzyfrowanie') }}</button>

      </div>
    </div>
  </div>
</template>
