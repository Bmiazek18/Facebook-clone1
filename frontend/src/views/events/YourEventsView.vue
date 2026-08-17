<script setup lang="ts">
import EventsSidebar from '@/components/events/EventsSidebar.vue'
import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore()
</script>

<template>
  <div class="flex h-screen bg-gray-100">
    <EventsSidebar />
    <div class="flex-1 p-4 overflow-y-auto">
      <div class="space-y-4">
        <div class="p-4 bg-white rounded-lg shadow-md">
          <h2 class="text-xl font-semibold">Zainteresowania</h2>
          <p class="text-gray-500"></p>
        </div>
        <div class="grid grid-cols-1 gap-4"></div>
      </div>
    </div>
  </div>
</template>
