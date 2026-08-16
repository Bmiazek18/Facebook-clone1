<script setup lang="ts">
import { ref, computed } from 'vue'
import { useConversationsStore } from '@/stores/conversations'

const emit = defineEmits<{
  (e: 'apply'): void
  (e: 'cancel'): void
}>()

const conv = useConversationsStore()

// Zapisujemy ID motywu, który był włączony w momencie otwarcia modala
const initialThemeId = ref(conv.selectedThemeId)

const selectedThemeId = computed({
  get: () => conv.selectedThemeId,
  set: (v: string) => conv.setSelectedTheme(v),
})

const selectedTheme = computed(() => conv.selectedTheme)
const setSelectedTheme = (id: string) => conv.setSelectedTheme(id)

// Sortowanie używa teraz "initialThemeId" - dzięki temu lista jest zamrożona,
// a wybrany początkowo motyw jest zawsze przypięty do samej góry.
const sortedThemes = computed(() => {
  const themes = [...conv.themes]
  const initialIndex = themes.findIndex(t => t.id === initialThemeId.value)

  if (initialIndex > 0) {
    const [activeTheme] = themes.splice(initialIndex, 1)
    themes.unshift(activeTheme)
  }

  return themes
})

const selectTheme = (id: string) => setSelectedTheme(id)

const applyTheme = () => {
  emit('apply')
}

const cancelTheme = () => {
  // Jeśli użytkownik anuluje, przywracamy początkowy motyw w store
  if (selectedThemeId.value !== initialThemeId.value) {
    setSelectedTheme(initialThemeId.value)
  }
  emit('cancel')
}

// Sprawdzamy, czy użytkownik wybrał nowy motyw (inny niż początkowy)
const hasUnsavedChanges = computed(() => selectedThemeId.value !== initialThemeId.value)
</script>

<template>
  <div class="bg-white rounded-[24px] shadow-2xl w-full max-w-4xl p-4 flex flex-col h-[700px]">

    <!-- Główna zawartość (Kolumny) -->
    <div class="flex flex-1 overflow-hidden gap-4 mb-4">

      <!-- Lewa kolumna: Lista motywów -->
      <div class="w-1/2 flex flex-col overflow-y-auto custom-scrollbar pr-2">
        <div class="space-y-0.5">
          <template v-for="(theme, index) in sortedThemes" :key="theme.id">
            <div
              @click="selectTheme(theme.id)"
              class="flex items-center p-3 rounded-[14px] cursor-pointer transition-colors duration-200"
              :class="selectedThemeId === theme.id ? 'bg-[#f0f4f9]' : 'hover:bg-gray-50'"
            >
              <!-- Ikona motywu -->
              <div class="relative w-[50px] h-[50px] shrink-0 mr-4">
                <img
                  :src="theme.image"
                  alt=""
                  class="w-full h-full rounded-full object-cover"
                />
              </div>

              <!-- Teksty -->
              <div class="flex-1 min-w-0">
                <h3 class="font-semibold text-gray-950 truncate text-[16px] leading-tight">
                  {{ theme.title }}
                </h3>
                <p v-if="theme.subtitle" class="text-[13px] text-gray-500 truncate mt-[3px]">
                  {{ theme.subtitle }}
                </p>
              </div>

              <!-- Znacznik wyboru (Checkmark) -->
              <div class="ml-2 w-7 h-7 flex items-center justify-center shrink-0">
                <svg
                  v-if="selectedThemeId === theme.id"
                  class="w-[26px] h-[26px] text-[#0064e0]"
                  fill="none"
                  stroke="currentColor"
                  stroke-width="2.5"
                  viewBox="0 0 24 24"
                >
                  <path stroke-linecap="round" stroke-linejoin="round" d="M5 13l4 4L19 7" />
                </svg>
              </div>
            </div>

            <!-- Separator pod pierwszym (początkowym) elementem -->
            <hr v-if="index === 0" class="border-gray-200 mx-2 my-2" />
          </template>
        </div>
      </div>

      <!-- Prawa kolumna: Podgląd czatu -->
      <div class="w-1/2 relative rounded-[32px] overflow-hidden bg-gray-100 flex flex-col border border-gray-100">
        <!-- Tło motywu -->
        <div class="absolute inset-0 z-0">
          <img
            :src="selectedTheme?.backgroundImage"
            alt="Theme Background"
            class="w-full h-full object-cover"
          />
        </div>

        <!-- Zawartość czatu (dymki od góry) -->
        <div class="relative z-10 flex flex-col justify-start h-full p-4 pt-6 space-y-1">

          <div class="flex justify-end">
            <div
              class="px-4 py-2.5 text-white text-[15px] leading-snug rounded-2xl rounded-br-[4px] max-w-[75%] shadow-sm transition-colors duration-300"
              :style="{ backgroundColor: selectedTheme?.sentBubbleColor || '#0064e0' }"
            >
              Masz wiele motywów do wyboru, a każdy jest nieco inny.
            </div>
          </div>

          <div class="flex justify-end mb-3">
            <div
              class="px-4 py-2.5 text-white text-[15px] leading-snug rounded-2xl rounded-tr-[4px] max-w-[75%] shadow-sm transition-colors duration-300"
              :style="{ backgroundColor: selectedTheme?.sentBubbleColor || '#0064e0' }"
            >
              Wiadomości przesyłane do innych osób będą oznaczane tym kolorem.
            </div>
          </div>

          <div class="flex justify-start">
            <div
              class="bg-white text-gray-900 px-4 py-2.5 text-[15px] leading-snug rounded-2xl rounded-bl-[4px] max-w-[75%] shadow-sm mt-3"
            >
              Wiadomości od znajomych będą tak wyglądać.
            </div>
          </div>

          <div
            class="w-full text-center text-[12px] font-medium my-3"
            :style="{ color: selectedTheme?.timestampColor || '#000000' }"
          >
            20:52
          </div>

          <div class="flex justify-end">
            <div
              class="px-4 py-2.5 text-white text-[15px] leading-snug rounded-2xl rounded-br-[4px] max-w-[75%] shadow-sm transition-colors duration-300"
              :style="{ backgroundColor: selectedTheme?.sentBubbleColor || '#0064e0' }"
            >
              Kliknij przycisk Wybierz, aby wybrać ten motyw.
            </div>
          </div>

        </div>
      </div>

    </div>

    <!-- Przyciski akcji (Dół) -->
    <div class="flex gap-3 pt-2 shrink-0">
      <button
        @click="cancelTheme"
        class="flex-1 bg-[#e4e6eb] hover:bg-[#d8dadf] text-gray-900 font-semibold text-[15px] py-2.5 rounded-xl transition-colors cursor-pointer"
      >
        Anuluj
      </button>

      <!-- Przycisk "Wybierz" uaktywnia się dopiero po kliknięciu nowego motywu -->
      <button
        @click="applyTheme"
        class="flex-1 font-semibold text-[15px] py-2.5 rounded-xl transition-colors"
        :class="hasUnsavedChanges
          ? 'bg-[#0064e0] hover:bg-[#0053ba] text-white cursor-pointer'
          : 'bg-[#e4e6eb] text-[#bcc0c4] cursor-not-allowed'"
      >
        Wybierz
      </button>
    </div>

  </div>
</template>

<style scoped>
/* Stylizacja grubszego, pigułkowego Scrollbara ze zrzutu ekranu */
.custom-scrollbar::-webkit-scrollbar {
  width: 8px;
}
.custom-scrollbar::-webkit-scrollbar-track {
  background: transparent;
}
.custom-scrollbar::-webkit-scrollbar-thumb {
  background-color: #8c8c8c;
  border-radius: 10px;
}
.custom-scrollbar::-webkit-scrollbar-thumb:hover {
  background-color: #737373;
}
</style>
