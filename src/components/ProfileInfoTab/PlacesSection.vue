<script setup lang="ts">
import type { User } from '@/data/users'

defineProps<{
  profileUser: User
}>()
</script>

<template>
  <div class="space-y-6">
    <div>
      <h3 class="font-bold text-theme-text mb-3">Miejsca zamieszkania</h3>

      <div v-if="profileUser.location" class="flex items-center mb-4">
        <div class="bg-gray-200 p-2 rounded-full mr-3">
          <span class="text-gray-500">📍</span>
        </div>
        <div>
          <div class="font-bold text-theme-text">{{ profileUser.location }}</div>
          <div class="text-sm text-gray-500">Aktualne miejsce zamieszkania</div>
        </div>
      </div>

      <div v-if="profileUser.hometown" class="flex items-center mb-4">
        <div class="bg-gray-200 p-2 rounded-full mr-3">
          <span class="text-gray-500">📍</span>
        </div>
        <div>
          <div class="font-bold text-theme-text">{{ profileUser.hometown }}</div>
          <div class="text-sm text-gray-500">Miasto rodzinne</div>
        </div>
      </div>

      <div v-if="!profileUser.location && !profileUser.hometown" class="text-gray-500">
        Brak miejsc do wyświetlenia
      </div>
    </div>
  </div>
</template>
