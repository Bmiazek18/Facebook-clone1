<script setup lang="ts">
import type { User } from '@/data/users'

defineProps<{
  profileUser: User
}>()
</script>

<template>
  <div class="space-y-8">
    <div>
      <h3 class="font-bold text-theme-text mb-3">Dane kontaktowe</h3>
      <div v-if="profileUser.phone || profileUser.email || profileUser.website" class="space-y-2">
        <div v-if="profileUser.phone" class="flex items-center"><span class="mr-2">📞</span> {{ profileUser.phone }}</div>
        <div v-if="profileUser.email" class="flex items-center"><span class="mr-2">✉️</span> {{ profileUser.email }}</div>
        <div v-if="profileUser.website" class="flex items-center"><span class="mr-2">🔗</span> <a :href="profileUser.website" target="_blank" class="text-blue-600 hover:underline">{{ profileUser.website }}</a></div>
      </div>
      <div v-else class="flex items-center text-gray-500">
        <span class="text-gray-400 text-xl mr-3">👤</span> Brak danych kontaktowych do wyświetlenia
      </div>
    </div>

    <div>
      <h3 class="font-bold text-theme-text mb-3">Witryny i linki społecznościowe</h3>
      <div v-if="profileUser.website" class="flex items-center">
        <span class="text-gray-400 text-xl mr-3">🔗</span>
        <a :href="profileUser.website" target="_blank" class="text-blue-600 hover:underline">{{ profileUser.website }}</a>
      </div>
      <div v-else class="flex items-center text-gray-500">
        <span class="text-gray-400 text-xl mr-3">🔗</span> Brak linków do wyświetlenia
      </div>
    </div>

    <div>
      <h3 class="font-bold text-theme-text mb-3">Podstawowe informacje</h3>
      <div class="space-y-4">
        <div class="flex items-start">
          <span class="text-gray-400 text-xl mr-3">👤</span>
          <div>
            <div class="text-theme-text font-semibold">{{ profileUser.gender || 'Nie podano' }}</div>
            <div class="text-xs text-gray-500">Płeć</div>
          </div>
        </div>
        <div class="flex items-start">
          <span class="text-gray-400 text-xl mr-3">🎂</span>
          <div>
            <div class="text-theme-text font-semibold">
              {{ profileUser.birthDate ? new Date(profileUser.birthDate).toLocaleDateString('pl-PL', { day: 'numeric', month: 'long' }) : 'Nie podano' }}
            </div>
            <div class="text-xs text-gray-500">Data urodzenia</div>
            <div v-if="profileUser.birthDate" class="text-theme-text font-semibold mt-1">
              {{ new Date(profileUser.birthDate).getFullYear() }}
            </div>
            <div v-if="profileUser.birthDate" class="text-xs text-gray-500">Rok urodzenia</div>
          </div>
        </div>
        <div class="flex items-start">
          <span class="text-gray-400 text-xl mr-3">💬</span>
          <div>
            <div class="text-theme-text font-semibold">{{ profileUser.languages || 'Polski' }}</div>
            <div class="text-xs text-gray-500">Języki</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
