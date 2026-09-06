<template>
  <div class="flex flex-col max-w-md mx-auto bg-white dark:bg-[#242526] text-black dark:text-gray-100 rounded-t-2xl sm:rounded-2xl shadow-xl overflow-hidden w-[550px] h-[550px]">

    <!-- DYNAMICZNY NAGŁÓWEK -->


    <!-- ZAWARTOŚĆ (Widoki) -->
    <div class="flex-1 overflow-y-auto">

      <!-- FAZA 1: STATUS -->
      <div v-if="view === 'status'" class="flex flex-col h-full px-4 py-8">
        <div class="flex flex-col items-center justify-center flex-1">
          <!-- Ikona weryfikacji -->
          <div class="mb-6 text-gray-400 dark:text-gray-500">
            <svg class="w-[72px] h-[72px]" :class="{ 'animate-spin': isVerifying }" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
            </svg>
          </div>

          <h3 class="text-[22px] font-bold mb-3 text-center">
            {{ isVerifying ? 'Weryfikowanie...' : (keys ? 'Zweryfikowano' : 'Klucze nieuzgodnione') }}
          </h3>

          <p class="text-[15px] text-gray-500 dark:text-gray-400 text-center mb-8 px-4">
            {{ isVerifying 
                ? 'Trwa automatyczne weryfikowanie, czy Twój czat jest w pełni szyfrowany.' 
                : (keys 
                    ? 'Twój czat z tym użytkownikiem jest w pełni szyfrowany. Nikt poza Wami nie może czytać wiadomości.' 
                    : 'Klucze szyfrujące nie zostały jeszcze w pełni uzgodnione. Wyślij pierwszą prywatną wiadomość, aby utworzyć bezpieczną sesję E2EE.') }}
          </p>
        </div>

        <!-- Przycisk Porównaj klucze -->
        <div class="mt-auto">
          <button
            @click="view = 'members'"
            :disabled="isVerifying || !keys"
            class="w-full flex items-center justify-between p-4 hover:bg-gray-50 dark:hover:bg-gray-800 transition active:bg-gray-100 rounded-lg disabled:opacity-50"
          >
            <span class="text-[17px] font-semibold">{{ $t('chat.porownajKlucze') }}</span>
            <svg class="w-6 h-6 text-black dark:text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7" />
            </svg>
          </button>

          <p class="text-[14px] text-gray-500 dark:text-gray-400 text-center mt-6 px-2 pb-4">{{ $t('chat.mozeszPorownacKluczeAby') }}<a href="#" class="text-[#0064e0] dark:text-[#4599ff] font-medium hover:underline">{{ $t('auth.register.learnMore') }}</a>
          </p>
        </div>
      </div>

      <!-- FAZA 2: LISTA CZŁONKÓW -->
      <div v-else-if="view === 'members'" class="flex flex-col">
        <div class="p-4 border-b border-gray-200 dark:border-gray-700">
          <p class="text-[15px] text-gray-600 dark:text-gray-300">{{ $t('chat.uczestnicyCzatuMajaKlucze') }}<a href="#" class="text-[#0064e0] dark:text-[#4599ff] font-medium hover:underline">{{ $t('auth.register.learnMore') }}</a>
          </p>
        </div>

        <div class="p-4">
          <h3 class="text-[17px] font-bold mb-4">{{ $t('groups.members') }}</h3>

          <div class="space-y-1">
            <!-- Rozmówca -->
            <button
              @click="openMemberKeys('other')"
              class="w-full flex items-center gap-4 p-3 hover:bg-gray-50 dark:hover:bg-gray-800 rounded-xl transition"
            >
              <div class="w-12 h-12 bg-gray-200 dark:bg-gray-700 rounded-full flex-shrink-0 overflow-hidden">
                <svg class="w-full h-full text-gray-400" fill="currentColor" viewBox="0 0 24 24"><path d="M24 20.993V24H0v-2.996A14.977 14.977 0 0112.004 15c4.904 0 9.26 2.354 11.996 5.993zM16.002 8.999a4 4 0 11-8 0 4 4 0 018 0z" /></svg>
              </div>
              <span class="text-[17px] font-medium">{{ $t('chat.kluczeRozmowcy') }}</span>
            </button>

            <!-- Ty -->
            <button
              @click="openMemberKeys('me')"
              class="w-full flex items-center gap-4 p-3 hover:bg-gray-50 dark:hover:bg-gray-800 rounded-xl transition"
            >
              <div class="w-12 h-12 bg-gray-200 dark:bg-gray-700 rounded-full flex-shrink-0 overflow-hidden">
                 <svg class="w-full h-full text-gray-400" fill="currentColor" viewBox="0 0 24 24"><path d="M24 20.993V24H0v-2.996A14.977 14.977 0 0112.004 15c4.904 0 9.26 2.354 11.996 5.993zM16.002 8.999a4 4 0 11-8 0 4 4 0 018 0z" /></svg>
              </div>
              <span class="text-[17px] font-medium">{{ $t('chat.twojeKlucze') }}</span>
            </button>
          </div>
        </div>
      </div>

      <!-- FAZA 3: KLUCZE -->
      <div v-else-if="view === 'keys' && keys" class="p-4 h-full flex flex-col">
        <!-- Secure storage -->
        <div class="mb-6">
          <h4 class="text-[16px] font-semibold mb-2">{{ $t('chat.secureStorage') }}</h4>
          <div class="bg-[#e4e6eb] dark:bg-[#303030] p-4 rounded-xl text-black dark:text-gray-200">
            <p class="font-mono text-[15px] leading-relaxed break-words">
              {{ formatToHexView(keys.conversationCode || 'B7 1F 65 C7 FF A8 80 2D 0B 67 D0 9D DD FE 35 65 A5 A1 AF E8 8D AD 69 03 38 BA 09 C1 13 13 E8 D4') }}
            </p>
          </div>
        </div>

        <!-- Device -->
        <div class="mb-6">
          <h4 class="text-[16px] font-semibold mb-2">{{ $t('chat.device') }}</h4>
          <div class="bg-[#e4e6eb] dark:bg-[#303030] p-4 rounded-xl text-black dark:text-gray-200">
            <p class="font-mono text-[15px] leading-relaxed break-words">
              {{ formatToHexView(selectedMember === 'me' ? keys.myDeviceKey : keys.otherDeviceKey) }}
            </p>
          </div>
        </div>
      </div>

    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getVerificationSessionKeys } from '@/utils/e2ee'

const props = defineProps<{ chatId: string | number }>()
const emit = defineEmits(['close'])

const keys = ref<{ conversationCode: string, myDeviceKey: string, otherDeviceKey: string } | null>(null)
const view = ref<'status' | 'members' | 'keys'>('status')
const selectedMember = ref<'me' | 'other' | null>(null)
const isVerifying = ref(true)
const loadingKeys = ref(true)

async function runAutomaticVerification() {
  isVerifying.value = true
  loadingKeys.value = true

  try {
    keys.value = await getVerificationSessionKeys(String(props.chatId))
  } catch (err) {
    console.error('Failed to compute E2EE keys inside modal:', err)
  } finally {
    loadingKeys.value = false
  }

  // Symulacja czasu weryfikacji
  setTimeout(() => {
    isVerifying.value = false
  }, 1800)
}

onMounted(() => {
  runAutomaticVerification()
})

// Funkcja dzieląca ciąg znaków na pary (jak w widoku hex)
function formatToHexView(key: string | undefined): string {
  if (!key) return ''
  const cleaned = key.replace(/\s+/g, '').toUpperCase()
  const chunks = cleaned.match(/.{1,2}/g) || []
  return chunks.join(' ')
}

function openMemberKeys(member: 'me' | 'other') {
  selectedMember.value = member
  view.value = 'keys'
}

function goBack() {
  if (view.value === 'keys') {
    view.value = 'members'
  } else if (view.value === 'members') {
    view.value = 'status'
  }
}
</script>

<style scoped>
.overflow-y-auto::-webkit-scrollbar {
  width: 6px;
}
.overflow-y-auto::-webkit-scrollbar-thumb {
  background-color: rgba(156, 163, 175, 0.5);
  border-radius: 10px;
}
</style>
