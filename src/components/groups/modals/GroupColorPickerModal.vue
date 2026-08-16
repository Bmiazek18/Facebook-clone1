<script setup lang="ts">
import { ref } from 'vue'

// Import ikony z kleszczem / fajką oraz krzyżyka zamknięcia
import CheckIcon from 'vue-material-design-icons/Check.vue'
import CloseIcon from 'vue-material-design-icons/Close.vue'

const props = defineProps<{
  initialColor?: string
}>()

const emit = defineEmits<{
  (e: 'close'): void
  (e: 'save', color: string): void
}>()

// Przykładowe kolory odpowiadające palecie z obrazka
const predefinedColors = ref<string[]>([
  '#2d88ff', '#0099ff', '#0084c7', '#1877f2', '#2eA043',
  '#1f883d', '#009688', '#8a3ffc', '#ff4d6d', '#f34235',
  '#f57c00', '#e91e63', '#ea4c89', '#d97706', '#b45309'
])

// Aktualnie wyznaczony kolor (domyślnie pierwszy na liście lub przekazany w props)
const selectedColor = ref<string>(props.initialColor || predefinedColors.value[0])
const originalColor = ref<string>(selectedColor.value)

// Referencja do ukrytego natywnego pola wyboru koloru (<input type="color">)
const colorInputRef = ref<HTMLInputElement | null>(null)

// Wyzwalanie wyboru własnego koloru
const triggerCustomColorPicker = () => {
  colorInputRef.value?.click()
}

// Obsługa wyboru własnego koloru z natywnego okna
const handleCustomColorChange = (event: Event) => {
  const target = event.target as HTMLInputElement
  if (target && target.value) {
    selectedColor.value = target.value
    // Jeśli wybranego koloru nie ma w palecie, dodajemy go jako aktywny
    if (!predefinedColors.value.includes(target.value)) {
      predefinedColors.value.unshift(target.value)
    }
  }
}

// Zapisywanie wybranego koloru
const handleSave = () => {
  emit('save', selectedColor.value)
}
</script>

<template>
  <div class="bg-white dark:bg-[#242526] text-[#050505] dark:text-[#e4e6eb] border border-gray-200 dark:border-[#3e4042] w-full max-w-sm overflow-hidden select-none shadow-xl">



    <!-- Treść modala -->
    <div class="p-6">

      <!-- Opis na górze -->
      <p class="text-[15px] text-[#65676b] dark:text-[#b0b3b8] leading-relaxed mb-4">
        Ustaw kolor przycisków, nagłówków i tła w grupie.
      </p>

      <!-- Nagłówek sekcji + Przycisk "Dodaj kolor" -->
      <div class="flex items-center justify-between mb-5">
        <h3 class="text-[17px] font-bold">
          Kolor podstawowy
        </h3>

        <button
          type="button"
          @click="triggerCustomColorPicker"
          class="text-[15px] font-semibold text-[#1877f2] dark:text-[#4599ff] hover:bg-[#e7f3ff]/50 dark:hover:bg-[#3a3b3c] px-2 py-1 rounded-md transition-colors"
        >
          Dodaj kolor
        </button>

        <!-- Ukryty natywny Input typu color -->
        <input
          ref="colorInputRef"
          type="color"
          class="sr-only"
          :value="selectedColor"
          @input="handleCustomColorChange"
        />
      </div>

      <!-- Siatka kółek z kolorami (5 w rządku) -->
      <div class="grid grid-cols-5 gap-y-4 gap-x-3 justify-items-center mb-2">
        <button
          v-for="color in predefinedColors"
          :key="color"
          type="button"
          @click="selectedColor = color"
          class="relative w-10 h-10 rounded-full transition-transform active:scale-95 flex items-center justify-center focus:outline-none shadow-sm"
          :style="{ backgroundColor: color }"
        >
          <!-- Zewnętrzny pierścień + ikona Checkboxa dla aktywnego koloru -->
          <template v-if="selectedColor === color">
            <span class="absolute -inset-1 rounded-full border-2 border-[#1877f2] dark:border-[#4599ff]"></span>
            <CheckIcon :size="20" class="text-white drop-shadow-sm z-10" />
          </template>
        </button>
      </div>

    </div>

    <!-- Stopka z przyciskami (oddzielona dolną linią) -->
    <div class="px-6 py-4 border-t border-gray-200 dark:border-[#3e4042] flex items-center justify-end gap-3">
      <button
        type="button"
        @click="emit('close')"
        class="px-4 py-2 rounded-lg text-[15px] font-semibold text-[#1877f2] dark:text-[#4599ff] hover:bg-[#e7f3ff]/50 dark:hover:bg-[#3a3b3c] transition-colors"
      >
        Anuluj
      </button>

      <button
        type="button"
        @click="handleSave"
        :disabled="selectedColor === originalColor"
        class="px-6 py-2 rounded-xl text-[15px] font-semibold transition-colors shadow-sm"
        :class="[
          selectedColor !== originalColor
            ? 'bg-[#1877f2] text-white hover:bg-[#166fe5] cursor-pointer'
            : 'bg-gray-200 dark:bg-[#3a3b3c] text-[#8c939d] dark:text-[#808285] cursor-not-allowed'
        ]"
      >
        Zapisz
      </button>
    </div>

  </div>
</template>
