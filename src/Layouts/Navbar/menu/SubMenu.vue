<template>
  <div class="settings-view max-w-xl mx-auto bg-white dark:bg-gray-900 text-gray-900 dark:text-white shadow-lg rounded-xl p-4">
    <div class="w-full flex items-center leading-7 pb-4">
      <button
        @click="handleBackClick"
        class="rounded-lg transition duration-150 p-2 -ml-2 hover:bg-gray-100 dark:hover:bg-gray-800"
        aria-label="Wróć"
      >
        <span class="h-9 w-9 border border-gray-300 dark:border-gray-600 rounded-full flex items-center justify-center">
          <ArrowLeftIcon class="text-gray-700 dark:text-gray-300" />
        </span>
      </button>
      <span class="ml-4 text-gray-900 dark:text-white font-bold text-[24px]">Wyświetlanie i ułatwienia dostępu</span>
    </div>

    <section class="setting-group mb-8">
      <div class="setting-header flex items-start mb-4">
        <span class="h-9 w-9 border border-gray-300 dark:border-gray-600 rounded-full flex items-center justify-center mr-3 mt-1 shrink-0">
          <MoonWaningCrescentIcon class="text-gray-700 dark:text-gray-300" :size="20" />
        </span>
        <div class="text-content">
          <h3 class="text-xl font-semibold">Tryb ciemny</h3>
          <p class="text-sm text-gray-500 dark:text-gray-400 mt-1">
            Dostosuj wygląd, aby zmniejszyć odblask i dać odpocząć oczom.
          </p>
        </div>
      </div>

      <div class="options-list pl-12 space-y-2">
        <label class="option-item flex items-center justify-between py-3 cursor-pointer hover:bg-gray-50 dark:hover:bg-gray-800 rounded-lg -mx-2 px-2 transition-colors">
          <span class="text-base">Wył.</span>
          <input type="radio" v-model="mode" value="light" class="hidden">
          <div :class="['w-5 h-5 rounded-full border-2 flex items-center justify-center', mode === 'light' ? 'border-blue-500' : 'border-gray-400 dark:border-gray-600']">
            <div v-if="mode === 'light'" class="w-2.5 h-2.5 bg-blue-500 rounded-full"></div>
          </div>
        </label>

        <label class="option-item flex items-center justify-between py-3 cursor-pointer hover:bg-gray-50 dark:hover:bg-gray-800 rounded-lg -mx-2 px-2 transition-colors">
          <span class="text-base">Wł.</span>
          <input type="radio" v-model="mode" value="dark" class="hidden">
          <div :class="['w-5 h-5 rounded-full border-2 flex items-center justify-center', mode === 'dark' ? 'border-blue-500' : 'border-gray-400 dark:border-gray-600']">
            <div v-if="mode === 'dark'" class="w-2.5 h-2.5 bg-blue-500 rounded-full"></div>
          </div>
        </label>

        <label class="option-item flex items-start justify-between py-3 cursor-pointer hover:bg-gray-50 dark:hover:bg-gray-800 rounded-lg -mx-2 px-2 transition-colors">
          <div class="flex-1 pr-4">
            <span class="text-base block">Automatycznie</span>
            <p class="text-sm text-gray-500 dark:text-gray-400 mt-1">
              Automatycznie dostosowuje wyświetlanie na podstawie ustawień systemowych urządzenia.
            </p>
          </div>
          <input type="radio" v-model="mode" value="auto" class="hidden">
          <div :class="['w-5 h-5 rounded-full border-2 flex items-center justify-center mt-1', mode === 'auto' ? 'border-blue-500' : 'border-gray-400 dark:border-gray-600']">
            <div v-if="mode === 'auto'" class="w-2.5 h-2.5 bg-blue-500 rounded-full"></div>
          </div>
        </label>
      </div>
    </section>

    <section class="setting-group mb-8">
      <div class="setting-header flex items-start mb-4">
        <span class="h-9 w-9 border border-gray-300 dark:border-gray-600 rounded-full flex items-center justify-center mr-3 mt-1 shrink-0">
          <FormatSizeDecreaseIcon class="text-gray-700 dark:text-gray-300" :size="20" />
        </span>
        <div class="text-content">
          <h3 class="text-xl font-semibold">Tryb kompaktowy</h3>
          <p class="text-sm text-gray-500 dark:text-gray-400 mt-1">Zmniejsz rozmiar czcionki, aby zmieścić więcej treści na ekranie.</p>
        </div>
      </div>

      <div class="options-list pl-12 space-y-2">
        <label class="option-item flex items-center justify-between py-3 cursor-pointer hover:bg-gray-50 dark:hover:bg-gray-800 rounded-lg -mx-2 px-2 transition-colors">
          <span class="text-base">Wył.</span>
          <input type="radio" v-model="compactMode" value="off" class="hidden">
          <div :class="['w-5 h-5 rounded-full border-2 flex items-center justify-center', compactMode === 'off' ? 'border-blue-500' : 'border-gray-400 dark:border-gray-600']">
            <div v-if="compactMode === 'off'" class="w-2.5 h-2.5 bg-blue-500 rounded-full"></div>
          </div>
        </label>

        <label class="option-item flex items-center justify-between py-3 cursor-pointer hover:bg-gray-50 dark:hover:bg-gray-800 rounded-lg -mx-2 px-2 transition-colors">
          <span class="text-base">Wł.</span>
          <input type="radio" v-model="compactMode" value="on" class="hidden">
          <div :class="['w-5 h-5 rounded-full border-2 flex items-center justify-center', compactMode === 'on' ? 'border-blue-500' : 'border-gray-400 dark:border-gray-600']">
            <div v-if="compactMode === 'on'" class="w-2.5 h-2.5 bg-blue-500 rounded-full"></div>
          </div>
        </label>
      </div>
    </section>

    <div class="space-y-1">
      <a href="#" class="flex items-center py-3 hover:bg-gray-100 dark:hover:bg-gray-800 rounded-lg -mx-2 px-2 transition duration-150">
        <span class="h-9 w-9 border border-gray-300 dark:border-gray-600 rounded-full flex items-center justify-center mr-3 shrink-0">
          <KeyboardIcon :size="20" class="text-gray-700 dark:text-gray-300" />
        </span>
        <span class="grow text-base font-medium">Klawiatura</span>
        <ChevronRightIcon :size="24" class="text-gray-400" />
      </a>

      <a href="#" class="flex items-center py-3 hover:bg-gray-100 dark:hover:bg-gray-800 rounded-lg -mx-2 px-2 transition duration-150">
        <span class="h-9 w-9 border border-gray-300 dark:border-gray-600 rounded-full flex items-center justify-center mr-3 shrink-0">
          <HumanMaleHeightIcon :size="20" class="text-gray-700 dark:text-gray-300" />
        </span>
        <span class="grow text-base font-medium">Ustawienia dostępności</span>
      </a>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { useTheme } from '@/composables/useTheme';

// --- IKONY ---
import KeyboardIcon from 'vue-material-design-icons/Keyboard.vue';
import HumanMaleHeightIcon from 'vue-material-design-icons/HumanMaleHeight.vue';
import ChevronRightIcon from 'vue-material-design-icons/ChevronRight.vue';
import ArrowLeftIcon from 'vue-material-design-icons/ArrowLeft.vue';
import MoonWaningCrescentIcon from 'vue-material-design-icons/MoonWaningCrescent.vue';
import FormatSizeDecreaseIcon from 'vue-material-design-icons/FormatSize.vue';

// --- LOGIKA ---
// Upewnij się, że useTheme() zwraca 'mode' z useColorMode
const { mode } = useTheme();

const compactMode = ref<'off' | 'on'>('off');

const emit = defineEmits(['back']);
const handleBackClick = () => emit('back');
</script>
