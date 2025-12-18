<script setup lang="ts">
import type { User } from '@/data/users'

defineProps<{
  profileUser: User
}>()
</script>

<template>
  <div class="space-y-6">
    <div>
      <h3 class="font-bold text-theme-text mb-3">Praca</h3>
      <div v-if="profileUser.job" class="flex items-center mb-2">
        <span class="text-gray-400 text-2xl mr-3">💼</span>
        <div>
          <div><strong>{{ profileUser.company }}</strong></div>
          <div class="text-sm text-gray-500">{{ profileUser.job }}</div>
        </div>
      </div>
      <div v-else class="text-gray-500 flex items-center">
        <span class="text-gray-400 text-xl mr-3">💼</span> Brak miejsc pracy do wyświetlenia
      </div>
    </div>

    <div>
      <h3 class="font-bold text-theme-text mb-3">Szkoła wyższa</h3>
      <div v-if="profileUser.education" class="flex items-center mb-2">
        <span class="text-gray-400 text-2xl mr-3">🎓</span>
        <div>
          <div><strong>{{ profileUser.school }}</strong></div>
          <div class="text-sm text-gray-500">{{ profileUser.education }}</div>
        </div>
      </div>
      <div v-else class="text-gray-500 flex items-center">
        <span class="text-gray-400 text-xl mr-3">🎓</span> Brak szkół do wyświetlenia
      </div>
    </div>
  </div>
</template>
