<template>
  <div
    class="settings-view max-w-xl mx-auto bg-theme-bg-secondary text-theme-text  p-2"
  >
    <!-- Nagłówek -->
    <div class="w-full flex items-center pb-4">
      <button
        @click="handleBackClick"
        class="rounded-full p-2 -ml-2 hover:bg-gray-100 dark:hover:bg-theme-hover transition duration-150 mr-2"
        aria-label="Wróć"
      >
        <ArrowLeftIcon class="text-2xl text-theme-text" />
      </button>
      <span class="text-theme-text font-bold text-xl leading-snug">
        Wyświetlanie i ułatwienia dostępu
      </span>
    </div>

    <!-- Tryb ciemny -->
    <section class="setting-group mb-6">
      <div class="setting-header flex items-start mb-3">
        <span
          class="h-9 w-9 bg-theme-bg-tertiary dark:bg-theme-bg-tertiary rounded-full flex items-center justify-center mr-3 shrink-0 text-theme-text"
        >
          <MoonWaningCrescentIcon :size="20" />
        </span>
        <div class="text-content">
          <h3 class="text-base font-bold text-theme-text leading-tight">Tryb ciemny</h3>
          <p class="text-sm text-gray-500 dark:text-gray-400 mt-0.5 leading-snug">
            Dostosuj wygląd Facebooka, aby zmniejszyć odblask i dać odpocząć oczom.
          </p>
        </div>
      </div>

      <div class="options-list pl-12 space-y-1">
        <!-- Wył. -->
        <label
          class="option-item flex items-center justify-between py-2.5 cursor-pointer hover:bg-gray-100 dark:hover:bg-theme-hover rounded-lg px-2 -mx-2 transition-colors"
        >
          <span class="text-[15px] font-medium text-theme-text">Wył.</span>
          <input type="radio" v-model="mode" value="light" class="hidden" />
          <div
            :class="[
              'w-5 h-5 rounded-full border-2 flex items-center justify-center transition-all',
              mode === 'light'
                ? 'border-gray-900 dark:border-white'
                : 'border-gray-400 dark:border-gray-500',
            ]"
          >
            <div
              v-if="mode === 'light'"
              class="w-2.5 h-2.5 bg-gray-900 dark:bg-white rounded-full"
            ></div>
          </div>
        </label>

        <!-- Wł. -->
        <label
          class="option-item flex items-center justify-between py-2.5 cursor-pointer hover:bg-gray-100 dark:hover:bg-theme-hover rounded-lg px-2 -mx-2 transition-colors"
        >
          <span class="text-[15px] font-medium text-theme-text">Wł.</span>
          <input type="radio" v-model="mode" value="dark" class="hidden" />
          <div
            :class="[
              'w-5 h-5 rounded-full border-2 flex items-center justify-center transition-all',
              mode === 'dark'
                ? 'border-gray-900 dark:border-white'
                : 'border-gray-400 dark:border-gray-500',
            ]"
          >
            <div
              v-if="mode === 'dark'"
              class="w-2.5 h-2.5 bg-gray-900 dark:bg-white rounded-full"
            ></div>
          </div>
        </label>

        <!-- Automatycznie -->
        <label
          class="option-item flex items-start justify-between py-2.5 cursor-pointer hover:bg-gray-100 dark:hover:bg-theme-hover rounded-lg px-2 -mx-2 transition-colors"
        >
          <div class="flex-1 pr-4">
            <span class="text-[15px] font-medium text-theme-text block">Automatycznie</span>
            <p class="text-sm text-gray-500 dark:text-gray-400 mt-0.5 leading-snug">
              Automatycznie dostosujemy wyświetlanie na podstawie ustawień systemowych urządzenia.
            </p>
          </div>
          <input type="radio" v-model="mode" value="auto" class="hidden" />
          <div
            :class="[
              'w-5 h-5 rounded-full border-2 flex items-center justify-center mt-0.5 transition-all shrink-0',
              mode === 'auto'
                ? 'border-gray-900 dark:border-white'
                : 'border-gray-400 dark:border-gray-500',
            ]"
          >
            <div
              v-if="mode === 'auto'"
              class="w-2.5 h-2.5 bg-gray-900 dark:bg-white rounded-full"
            ></div>
          </div>
        </label>
      </div>
    </section>

    <!-- Tryb kompaktowy -->
    <section class="setting-group mb-6">
      <div class="setting-header flex items-start mb-3">
        <span
          class="h-9 w-9 bg-gray-200 dark:bg-gray-700 rounded-full flex items-center justify-center mr-3 shrink-0 text-theme-text"
        >
          <FormatSizeDecreaseIcon :size="20" />
        </span>
        <div class="text-content">
          <h3 class="text-base font-bold text-theme-text leading-tight">Tryb kompaktowy</h3>
          <p class="text-sm text-gray-500 dark:text-gray-400 mt-0.5 leading-snug">
            Zmniejsz rozmiar czcionki, aby zmieścić więcej treści na ekranie.
          </p>
        </div>
      </div>

      <div class="options-list pl-12 space-y-1">
        <label
          class="option-item flex items-center justify-between py-2.5 cursor-pointer hover:bg-gray-100 dark:hover:bg-theme-hover rounded-lg px-2 -mx-2 transition-colors"
        >
          <span class="text-[15px] font-medium text-theme-text">Wył.</span>
          <input type="radio" v-model="compactMode" value="off" class="hidden" />
          <div
            :class="[
              'w-5 h-5 rounded-full border-2 flex items-center justify-center transition-all',
              compactMode === 'off'
                ? 'border-gray-900 dark:border-white'
                : 'border-gray-400 dark:border-gray-500',
            ]"
          >
            <div
              v-if="compactMode === 'off'"
              class="w-2.5 h-2.5 bg-gray-900 dark:bg-white rounded-full"
            ></div>
          </div>
        </label>

        <label
          class="option-item flex items-center justify-between py-2.5 cursor-pointer hover:bg-gray-100 dark:hover:bg-theme-hover rounded-lg px-2 -mx-2 transition-colors"
        >
          <span class="text-[15px] font-medium text-theme-text">Wł.</span>
          <input type="radio" v-model="compactMode" value="on" class="hidden" />
          <div
            :class="[
              'w-5 h-5 rounded-full border-2 flex items-center justify-center transition-all',
              compactMode === 'on'
                ? 'border-gray-900 dark:border-white'
                : 'border-gray-400 dark:border-gray-500',
            ]"
          >
            <div
              v-if="compactMode === 'on'"
              class="w-2.5 h-2.5 bg-gray-900 dark:bg-white rounded-full"
            ></div>
          </div>
        </label>
      </div>
    </section>

    <!-- Dolne odnośniki -->
    <div class="space-y-1">
      <a
        href="#"
        class="flex items-center py-2.5 hover:bg-gray-100 dark:hover:bg-theme-hover rounded-lg px-2 -mx-2 transition duration-150"
      >
        <span
          class="h-9 w-9 bg-gray-200 dark:bg-gray-700 rounded-full flex items-center justify-center mr-3 shrink-0 text-theme-text"
        >
          <KeyboardIcon :size="20" />
        </span>
        <span class="grow text-[15px] font-bold text-theme-text">Klawiatura</span>
        <ChevronRightIcon :size="24" class="text-gray-400" />
      </a>

      <a
        href="#"
        class="flex items-center py-2.5 hover:bg-gray-100 dark:hover:bg-theme-hover rounded-lg px-2 -mx-2 transition duration-150"
      >
        <span
          class="h-9 w-9 bg-gray-200 dark:bg-gray-700 rounded-full flex items-center justify-center mr-3 shrink-0 text-theme-text"
        >
          <HumanMaleHeightIcon :size="20" />
        </span>
        <span class="grow text-[15px] font-bold text-theme-text">Ustawienia dostępności</span>
      </a>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useTheme } from '@/composables/shared/useTheme'

// --- IKONY ---
import KeyboardIcon from 'vue-material-design-icons/Keyboard.vue'
import HumanMaleHeightIcon from 'vue-material-design-icons/HumanMaleHeight.vue'
import ChevronRightIcon from 'vue-material-design-icons/ChevronRight.vue'
import ArrowLeftIcon from 'vue-material-design-icons/ArrowLeft.vue'
import MoonWaningCrescentIcon from 'vue-material-design-icons/MoonWaningCrescent.vue'
import FormatSizeDecreaseIcon from 'vue-material-design-icons/FormatSize.vue'

// --- LOGIKA ---
const { mode } = useTheme()

const compactMode = ref<'off' | 'on'>('off')

const emit = defineEmits(['back'])
const handleBackClick = () => emit('back')
</script>
