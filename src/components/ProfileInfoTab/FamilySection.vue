<script setup lang="ts">
import type { User } from '@/data/users'

defineProps<{
  profileUser: User
}>()
</script>

<template>
  <div class="space-y-6">
    <div>
      <h3 class="font-bold text-theme-text mb-3">Związek</h3>
      <div v-if="profileUser.relationshipStatus" class="flex items-center">
        <div class="w-10 h-10 rounded-full bg-gray-300 mr-3 overflow-hidden">
          <img v-if="profileUser.partnerAvatar" :src="profileUser.partnerAvatar" class="w-full h-full object-cover">
        </div>
        <div>
          <div class="font-bold text-theme-text">{{ profileUser.partnerName || 'Nieznany' }}</div>
          <div class="text-sm text-gray-500">{{ profileUser.relationshipStatus }} <span v-if="profileUser.relationshipSince">od {{ profileUser.relationshipSince }}</span></div>
        </div>
      </div>
      <div v-else class="text-gray-500">Brak informacji o związkach</div>
    </div>

    <div>
      <h3 class="font-bold text-theme-text mb-3">Członkowie rodziny</h3>
      <div v-if="profileUser.familyMembers && profileUser.familyMembers.length > 0" class="space-y-2">
        <div v-for="(member, idx) in profileUser.familyMembers" :key="idx" class="flex items-center">
          <span class="text-gray-400 text-xl mr-3">👥</span>
          <span><strong>{{ member.name }}</strong> - {{ member.relationship }}</span>
        </div>
      </div>
      <div v-else class="flex items-center text-gray-500">
        <span class="text-gray-400 text-xl mr-3">👪</span> Brak członków rodziny do wyświetlenia
      </div>
    </div>
  </div>
</template>
