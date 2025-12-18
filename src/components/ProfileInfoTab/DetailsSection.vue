<script setup lang="ts">
import type { User } from '@/data/users'

defineProps<{
  profileUser: User
}>()
</script>

<template>
  <div class="space-y-6">
    <div class="mb-6">
      <h3 class="font-bold text-theme-text mb-2">Informacje o: {{ profileUser.name }}</h3>
      <div v-if="profileUser.bioDetails" class="flex items-start">
        <span class="text-gray-400 text-xl mr-3">📄</span>
        <p class="text-theme-text">{{ profileUser.bioDetails }}</p>
      </div>
      <div v-else class="flex items-center text-gray-500">
        <span class="text-gray-400 text-xl mr-3">📄</span> Brak dodatkowych szczegółów do wyświetlenia
      </div>
    </div>

    <div class="mb-6">
      <h3 class="font-bold text-theme-text mb-2">Wymowa imienia i nazwiska</h3>
      <div v-if="profileUser.namePronounciation" class="flex items-center">
        <span class="text-gray-400 text-xl mr-3">🎧</span>
        <span class="text-theme-text">{{ profileUser.namePronounciation }}</span>
      </div>
      <div v-else class="flex items-center text-gray-500">
        <span class="text-gray-400 text-xl mr-3">🎧</span> Brak wymowy nazwiska do wyświetlenia
      </div>
    </div>

    <div class="mb-6">
      <h3 class="font-bold text-theme-text mb-2">Inne imiona i nazwiska</h3>
      <div v-if="profileUser.otherNames && profileUser.otherNames.length > 0" class="flex items-start">
        <span class="text-gray-400 text-xl mr-3">Aa</span>
        <p class="text-theme-text">{{ profileUser.otherNames.join(', ') }}</p>
      </div>
      <div v-else class="flex items-center text-gray-500">
        <span class="text-gray-400 text-xl mr-3">Aa</span> Brak innych nazw do wyświetlenia
      </div>
    </div>

    <div>
      <h3 class="font-bold text-theme-text mb-2">Ulubione cytaty</h3>
      <div v-if="profileUser.favoriteQuotes && profileUser.favoriteQuotes.length > 0" class="space-y-2">
        <div v-for="(quote, idx) in profileUser.favoriteQuotes" :key="idx" class="flex items-start">
          <span class="text-gray-400 text-xl mr-3">❝</span>
          <span class="text-theme-text italic">{{ quote }}</span>
        </div>
      </div>
      <div v-else class="flex items-center text-gray-500">
        <span class="text-gray-400 text-xl mr-3">❝</span> Brak ulubionych cytatów do wyświetlenia
      </div>
    </div>
  </div>
</template>
