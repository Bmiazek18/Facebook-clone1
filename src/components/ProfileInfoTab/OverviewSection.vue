<script setup lang="ts">
import type { User } from '@/data/users'

defineProps<{
  profileUser: User
}>()
</script>

<template>
  <div class="space-y-6">
    <div>
      <div v-if="profileUser.job || profileUser.education" class="space-y-3">
        <div v-if="profileUser.job" class="flex items-center">
          <span class="text-gray-400 text-xl mr-3">💼</span>
          <span>{{ profileUser.job }} <span v-if="profileUser.company">w <strong>{{ profileUser.company }}</strong></span></span>
        </div>
        <div v-if="profileUser.education" class="flex items-center">
          <span class="text-gray-400 text-xl mr-3">🎓</span>
          <span>Studiował(a): <strong>{{ profileUser.education }}</strong> <span v-if="profileUser.school">w <strong>{{ profileUser.school }}</strong></span></span>
        </div>
      </div>
      <div v-else class="flex items-center text-gray-500">
        <span class="text-gray-400 text-xl mr-3">💼</span> Brak miejsc pracy do wyświetlenia
      </div>
    </div>

    <div class="space-y-3">
      <div v-if="profileUser.location" class="flex items-center">
        <span class="text-gray-400 text-xl mr-3">🏠</span>
        <span>Mieszka w: <strong>{{ profileUser.location }}</strong></span>
      </div>
      <div v-if="profileUser.hometown" class="flex items-center">
        <span class="text-gray-400 text-xl mr-3">📍</span>
        <span>Z: <strong>{{ profileUser.hometown }}</strong></span>
      </div>
      <div v-if="!profileUser.location && !profileUser.hometown" class="flex items-center text-gray-500">
        <span class="text-gray-400 text-xl mr-3">📍</span> Brak miejsc zamieszkania do wyświetlenia
      </div>
    </div>

    <div class="flex items-center">
      <span class="text-gray-400 text-xl mr-3">❤️</span>
      <span v-if="profileUser.relationshipStatus">
        {{ profileUser.relationshipStatus }}
        <span v-if="profileUser.partnerName"> z użytkownikiem <strong>{{ profileUser.partnerName }}</strong></span>
      </span>
      <span v-else>Brak informacji o związkach</span>
    </div>
  </div>
</template>
