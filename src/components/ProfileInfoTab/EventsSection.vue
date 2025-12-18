<script setup lang="ts">
import type { User } from '@/data/users'

defineProps<{
  profileUser: User
}>()
</script>

<template>
  <div>
    <h3 class="font-bold text-theme-text mb-3">Wydarzenia z życia</h3>
    <div v-if="profileUser.lifeEvents && profileUser.lifeEvents.length > 0" class="space-y-3">
      <div v-for="(event, idx) in profileUser.lifeEvents" :key="idx" class="flex items-start p-3 bg-gray-50 rounded-lg">
        <span class="text-gray-400 text-xl mr-3">⭐</span>
        <div>
          <div class="font-bold text-theme-text">{{ event.event }}</div>
          <div class="text-sm text-gray-500">{{ new Date(event.date).toLocaleDateString('pl-PL', { year: 'numeric', month: 'long', day: 'numeric' }) }}</div>
        </div>
      </div>
    </div>
    <div v-else class="flex items-center text-gray-500">
      <span class="text-gray-400 text-xl mr-3">⭐</span> Brak aktualizacji z życia do wyświetlenia
    </div>
  </div>
</template>
