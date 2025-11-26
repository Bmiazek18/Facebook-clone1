<template>
  <div class="language-switcher flex items-center space-x-2 px-2 py-1">
    <button
      v-for="lang in availableLanguages"
      :key="lang"
      @click="changeLanguage(lang)"
      :class="[
        'px-3 py-1 rounded-full text-sm font-medium transition-colors',
        currentLanguage() === lang
          ? 'bg-blue-500 text-white'
          : 'bg-gray-200 text-gray-800 hover:bg-gray-300'
      ]"
    >
      {{ lang === 'pl' ? '🇵🇱 PL' : '🇬🇧 EN' }}
    </button>
  </div>
</template>

<script setup lang="ts">
import { useLanguage } from '@/composables/useLanguage'

const { changeLanguage, currentLanguage, availableLanguages } = useLanguage()
</script>

<style scoped>
.language-switcher {
  display: flex;
  gap: 0.5rem;
}
</style>
