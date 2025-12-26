<template>
  <div class="settings-view  max-w-xl mx-auto bg-white dark:bg-gray-900 text-gray-900 dark:text-white shadow-lg rounded-xl">
<div class="w-full flex leading-7 pb-2">
 <button
      @click="handleBackClick"
      class="  rounded-lg  transition duration-150 mb-3 -mx-2 px-2"
      aria-label="Wyświetlanie i ułatwienia dostępu"
    >
      <span class="h-9 w-9 hover:bg-gray-100 dark:hover:bg-gray-800 border border-gray-300 dark:border-gray-600 rounded-full flex items-center justify-center mr-3 shrink-0">
        <ArrowLeftIcon class="text-xl text-gray-700 dark:text-gray-300" />
      </span>

    </button>
  <span class="text-gray-900 w-full dark:text-white font-bold text-[24px]">Wyświetlanie i ułatwienia dostępu</span>
</div>



    <section class="setting-group mb-4">
      <div class="setting-header flex items-start mb-3">
        <span class="h-9 w-9 border border-gray-300 dark:border-gray-600 rounded-full flex items-center justify-center mr-3 mt-1 shrink-0">
          <moon-waning-icon class="text-xl text-gray-700 dark:text-gray-300" :size="20" />
        </span>
        <div class="text-content">
          <h3 class="text-xl font-semibold">Tryb ciemny</h3>
          <p class="text-sm text-gray-500 dark:text-gray-400 mt-1">Dostosuj wygląd, aby zmniejszyć odblask i dać odpocząć oczom.</p>
        </div>
      </div>

      <div class="options-list pl-12 space-y-2">
        <label class="option-item flex items-center justify-between py-2 cursor-pointer hover:bg-gray-50 dark:hover:bg-gray-800 rounded-lg -mx-2 px-2 transition duration-100">
          <span class="text-base">Wył.</span>
          <input type="radio" v-model="darkMode" @click="setTheme('light')" value="light" class="hidden">
          <div :class="['w-5 h-5 rounded-full border-2 shrink-0', darkMode === 'light' ? 'border-black' : 'border-gray-400 dark:border-gray-600']">
            <div v-if="darkMode === 'light'" class="w-3 h-3 bg-black rounded-full mx-auto my-auto mt-0.5"></div>
          </div>
        </label>

        <label class="option-item flex items-center justify-between py-2 cursor-pointer hover:bg-gray-50 dark:hover:bg-gray-800 rounded-lg -mx-2 px-2 transition duration-100">
          <span class="text-base">Wł.</span>
          <input type="radio" v-model="darkMode" @click="setTheme('dark')" value="dark" class="hidden">
           <div :class="['w-5 h-5 rounded-full border-2 shrink-0', darkMode === 'dark' ? 'border-black' : 'border-gray-400 dark:border-gray-600']">
            <div v-if="darkMode === 'dark'" class="w-3 h-3 bg-black rounded-full mx-auto my-auto mt-0.5"></div>
          </div>
        </label>

        <label class="option-item flex items-start justify-between py-2 cursor-pointer">
          <div class="flex-1 pr-4">
            <span class="text-base block">Automatycznie</span>
            <p class="text-sm text-gray-500 dark:text-gray-400 mt-1">
              Automatycznie dostosowuje wyświetlanie na podstawie ustawień systemowych urządzenia.
            </p>
          </div>
          <input type="radio" v-model="darkMode" @click="setTheme('system')" value="system" class="hidden">
          <div :class="['w-5 h-5 rounded-full border-2 shrink-0 mt-1', darkMode === 'system' ? 'border-black' : 'border-gray-400 dark:border-gray-600']">
            <div v-if="darkMode === 'system'" class="w-3 h-3 bg-black rounded-full mx-auto my-auto mt-0.5"></div>
          </div>
        </label>
      </div>
    </section>



    <section class="setting-group mb-4">
      <div class="setting-header flex items-start mb-3">
        <span class="h-9 w-9 border border-gray-300 dark:border-gray-600 rounded-full flex items-center justify-center mr-3 mt-1 shrink-0">
          <format-size-decrease-icon class="text-xl text-gray-700 dark:text-gray-300" :size="20" />
        </span>
        <div class="text-content">
          <h3 class="text-xl font-semibold">Tryb kompaktowy</h3>
          <p class="text-sm text-gray-500 dark:text-gray-400 mt-1">Zmniejsz rozmiar czcionki, aby zmieścić więcej treści na ekranie.</p>
        </div>
      </div>

      <div class="options-list pl-12 space-y-2">
        <label class="option-item flex items-center justify-between py-2 cursor-pointer hover:bg-gray-50 dark:hover:bg-gray-800 rounded-lg -mx-2 px-2 transition duration-100">
          <span class="text-base">Wył.</span>
          <input type="radio" v-model="compactMode" value="off" class="hidden">
          <div :class="['w-5 h-5 rounded-full border-2 shrink-0', compactMode === 'off' ? 'border-black' : 'border-gray-400 dark:border-gray-600']">
            <div v-if="compactMode === 'off'" class="w-3 h-3 bg-black rounded-full mx-auto my-auto mt-0.5"></div>
          </div>
        </label>

        <label class="option-item flex items-center justify-between py-2 cursor-pointer hover:bg-gray-50 dark:hover:bg-gray-800 rounded-lg -mx-2 px-2 transition duration-100">
          <span class="text-base">Wł.</span>
          <input type="radio" v-model="compactMode" value="on" class="hidden">
          <div :class="['w-5 h-5 rounded-full border-2 shrink-0', compactMode === 'on' ? 'border-black' : 'border-gray-400 dark:border-gray-600']">
            <div v-if="compactMode === 'on'" class="w-3 h-3 bg-black rounded-full mx-auto my-auto mt-0.5"></div>
          </div>
        </label>
      </div>
    </section>



    <a href="#" class="setting-link flex items-center py-3 hover:bg-gray-100 dark:hover:bg-gray-800 rounded-lg -mx-2 px-2 transition duration-150">
      <span class="h-9 w-9 border border-gray-300 dark:border-gray-600 rounded-full flex items-center justify-center mr-3 shrink-0">
        <keyboard-icon class="text-xl text-gray-700 dark:text-gray-300" :size="20" />
      </span>
      <span class="grow text-base font-medium">Klawiatura</span>
      <chevron-right-icon class="text-gray-400 ml-4 shrink-0" :size="24" />
    </a>



    <a href="#" class="setting-link flex items-center py-3 hover:bg-gray-100 dark:hover:bg-gray-800 rounded-lg -mx-2 px-2 transition duration-150">
      <span class="h-9 w-9 border border-gray-300 dark:border-gray-600 rounded-full flex items-center justify-center mr-3 shrink-0">
        <human-male-height-icon class="text-xl text-gray-700 dark:text-gray-300" :size="20" />
      </span>
      <span class="grow text-base font-medium">Ustawienia dostępności</span>

    </a>

  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';

// Importy ikon

import KeyboardIcon from 'vue-material-design-icons/Keyboard.vue';
import HumanMaleHeightIcon from 'vue-material-design-icons/HumanMaleHeight.vue';
import ChevronRightIcon from 'vue-material-design-icons/ChevronRight.vue';
import ArrowLeftIcon from 'vue-material-design-icons/ArrowLeft.vue'; // Dodana ikona
import { useTheme } from '@/composables/useTheme';
const { themeState, setTheme } = useTheme();




const darkMode = ref<'light' | 'dark' | 'system'>(themeState.value);
const compactMode = ref<'off' | 'on'>('off');

const emit = defineEmits(['back']);

const handleBackClick = (): void => {

  emit('back');
};
</script>
