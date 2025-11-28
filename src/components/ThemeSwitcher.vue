<script setup lang="ts">
import { useTheme } from '@/composables/useTheme'
import Sun from 'vue-material-design-icons/WeatherSunny.vue'
import Moon from 'vue-material-design-icons/MoonFull.vue'

const { isDark, toggleTheme } = useTheme()
</script>

<template>
  <button
    @click="toggleTheme"
  class="rounded-full bg-gray-200 bg-theme-hover p-2 hover:bg-gray-300 hover:bg-theme-hover mx-1 cursor-pointer transition-colors duration-200"
    :title="isDark ? 'Light mode' : 'Dark mode'"
  >
    <Sun v-if="isDark" :size="23" fillColor="#FBBF24" />
    <Moon v-else :size="23" fillColor="#1F2937" />
  </button>
</template>
