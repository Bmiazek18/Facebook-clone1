<script setup lang="ts">
import { ref } from 'vue'

interface FriendRequest {
  id: string | number
  name: string
  avatarUrl: string
  timeAgo: string
}

// Props z domyślnymi danymi testowymi
const props = withDefaults(
  defineProps<{
    requests?: FriendRequest[]
    title?: string
  }>(),
  {
    title: 'Zaproszenia do grona...',
    requests: () => [
      {
        id: 1,
        name: 'Vanessa Matusiak',
        avatarUrl: 'https://i.pravatar.cc/150?img=47', // Przykładowy avatar
        timeAgo: '4 dni',
      },
    ],
  }
)

const emit = defineEmits<{
  (e: 'accept', requestId: string | number): void
  (e: 'remove', requestId: string | number): void
  (e: 'see-all'): void
}>()

const handleAccept = (id: string | number) => {
  emit('accept', id)
}

const handleRemove = (id: string | number) => {
  emit('remove', id)
}
</script>

<template>
  <div class="w-full">
    <!-- Nagłówek -->
    <div class="flex items-center justify-between mb-4">
      <h3 class="text-[17px] font-semibold text-theme-text-secondary">{{ $t('friends.friendRequests') }}</h3>
      <button
        @click="emit('see-all')"
        class="text-[#0064d1] text-[15px] hover:bg-theme-hover rounded-md px-2 py-2 cursor-pointer whitespace-nowrap transition-colors"
      >{{ $t('friends.zobaczWszystko') }}</button>
    </div>

    <!-- Lista zaproszeń -->
    <div class="space-y-4">
      <div
        v-for="request in requests"
        :key="request.id"
        class="flex items-start gap-3"
      >
        <!-- Avatar (po lewej, wyśrodkowany pionowo do przycisków) -->
        <div class="relative shrink-0 pt-1">
          <img
            :src="request.avatarUrl"
            :alt="request.name"
            class="w-15 h-15 rounded-full object-cover shadow-sm"
          />
        </div>

        <!-- Treść (Imię, czas i przyciski akcji) -->
        <div class="flex-1 min-w-0">
          <!-- Imię i czas -->
          <div class="flex items-baseline justify-between mb-2">
            <span class="font-semibold text-gray-900 text-[15px] truncate">
              {{ request.name }}
            </span>
            <span class="text-gray-500 text-sm ml-2 shrink-0">
              {{ request.timeAgo }}
            </span>
          </div>

          <!-- Przyciski akcji -->
          <div class="flex flex-row gap-2">
            <button
              @click="handleAccept(request.id)"
              class="w-full py-2 px-4 bg-[#0866FF] hover:bg-blue-600 active:bg-blue-700 text-white font-semibold text-sm rounded-lg transition-colors shadow-sm"
            >{{ $t('notifications_page.confirm') }}</button>
            <button
              @click="handleRemove(request.id)"
              class="w-full py-2 px-4 bg-[#E4E6EB] hover:bg-gray-300 active:bg-gray-400 text-gray-900 font-semibold text-sm rounded-lg transition-colors"
            >{{ $t('notifications_page.delete') }}</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
