<script setup lang="ts">
import { ref, watch } from 'vue'

const props = defineProps<{
  initialColor?: string
}>()

const emit = defineEmits<{
  (e: 'close'): void
  (e: 'add-color', color: string): void
}>()

// Wpisany kod HEX (bez znaku #)
const hexInput = ref<string>('')
// Wybrany aktywny kolor HEX (pełny, np. #e03131)
const currentColor = ref<string>(props.initialColor || '#e03131')
// Poziom jasności suwaka (0 - 100)
const brightness = ref<number>(50)

// Aktualizacja przy ręcznym wpisaniu kodu HEX
const handleHexInput = () => {
  let cleaned = hexInput.value.replace('#', '').trim()
  if (cleaned.length === 6 && /^[0-9A-Fa-f]{6}$/.test(cleaned)) {
    currentColor.value = `#${cleaned}`
  }
}

// Zapisywanie wybranego koloru
const handleAddColor = () => {
  if (currentColor.value) {
    emit('add-color', currentColor.value)
  }
}
</script>

<template>
  <div class="bg-white dark:bg-[#242526] text-[#050505] dark:text-[#e4e6eb] border border-gray-200 dark:border-[#3e4042] rounded-2xl w-full max-w-lg overflow-hidden select-none shadow-xl font-sans">

    <!-- Treść główna -->
    <div class="p-6">
      <div class="flex flex-col sm:flex-row gap-6">

        <!-- Lewa strona: Paleta kolorów (Canvas / Gradient) -->
        <div class="flex-1 flex flex-col gap-4">
          <!-- Obszar wyboru barwy (Gradient Spectrum) -->
          <div
            class="relative w-full h-56 rounded-xl overflow-hidden cursor-crosshair shadow-inner"
            style="background: linear-gradient(to top, #000, transparent), linear-gradient(to right, #00ffff, #0000ff, #ff00ff, #ff0000, #ffff00, #00ff00, #00ffff);"
          >
            <!-- Wskaźnik wybranego punktu (kółko ze strzałkami) -->
            <div
              class="absolute w-10 h-10 -ml-5 -mt-5 rounded-full border-2 border-white shadow-md flex items-center justify-center pointer-events-none transition-transform active:scale-110"
              :style="{ left: '50%', top: '50%', backgroundColor: currentColor }"
            >
              <!-- Ikona 4-kierunkowych strzałek wewnątrz wskaźnika -->
              <svg class="w-5 h-5 text-white drop-shadow-sm" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2.5">
                <path stroke-linecap="round" stroke-linejoin="round" d="M8 9l4-4 4 4m0 6l-4 4-4-4M9 8L5 12l4 4m6-8l4 4-4 4" />
              </svg>
            </div>
          </div>

          <!-- Suwak jasności / odcienia na dole -->
          <div class="relative w-full flex items-center">
            <input
              v-model="brightness"
              type="range"
              min="0"
              max="100"
              class="w-full h-1.5 bg-gradient-to-r from-black via-red-900 to-pink-200 rounded-lg appearance-none cursor-pointer accent-[#1877f2]"
            />
          </div>
        </div>

        <!-- Prawa strona: Instrukcja i Input HEX -->
        <div class="w-full sm:w-56 flex flex-col justify-start">
          <p class="text-[14px] text-[#65676b] dark:text-[#b0b3b8] leading-snug mb-2">{{ $t('groups.ustawNiestandardowyKolorSzesnastkowy') }}</p>

          <!-- Input dla kodu HEX -->
          <div class="relative border border-gray-300 dark:border-[#525355] rounded-xl px-3 py-3 focus-within:border-[#1877f2] dark:focus-within:border-[#4599ff] focus-within:ring-1 focus-within:ring-[#1877f2] dark:focus-within:ring-[#4599ff] transition-all">
            <input
              v-model="hexInput"
              @input="handleHexInput"
              type="text"
              :placeholder="$t('groups.kodSzesnastkowy')"
              maxlength="7"
              class="w-full bg-transparent text-[15px] text-[#050505] dark:text-[#e4e6eb] placeholder-[#8c939d] dark:placeholder-[#808285] font-normal focus:outline-none"
            />
          </div>
        </div>

      </div>
    </div>

    <!-- Stopka z przyciskami (Anuluj / Dodaj kolor) -->
    <div class="px-6 py-4 border-t border-gray-200 dark:border-[#3e4042] flex items-center justify-end gap-3">
      <button
        type="button"
        @click="emit('close')"
        class="px-4 py-2 rounded-lg text-[15px] font-semibold text-[#1877f2] dark:text-[#4599ff] hover:bg-[#e7f3ff]/50 dark:hover:bg-[#3a3b3c] transition-colors cursor-pointer"
      >{{ $t('common.cancel') }}</button>

      <button
        type="button"
        @click="handleAddColor"
        :disabled="!currentColor"
        :class="[
          'px-6 py-2 rounded-xl text-[15px] font-semibold transition-colors shadow-sm',
          currentColor
            ? 'bg-[#1877f2] text-white hover:bg-[#166fe5] cursor-pointer'
            : 'bg-[#e4e6eb] dark:bg-[#3a3b3c] text-[#8c939d] dark:text-[#808285] cursor-not-allowed'
        ]"
      >{{ $t('groups.dodajKolor') }}</button>
    </div>

  </div>
</template>
