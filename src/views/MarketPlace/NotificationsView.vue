<script setup lang="ts">
import { ref, computed } from 'vue';

import DotsHorizontal from 'vue-material-design-icons/DotsHorizontal.vue';

// Mock data for seller notifications



</script>

<template>
  <div class="max-w-4xl mx-auto">
    <div class="bg-white rounded-t-lg shadow-sm border-b border-gray-200 p-4 flex justify-between items-center">
      <h1 class="text-2xl font-bold">Powiadomienia sprzedawcy</h1>
      <button class="p-2 hover:bg-gray-100 rounded-full">
        <DotsHorizontal class="text-gray-500" />
      </button>
    </div>

    <div class="bg-white rounded-b-lg shadow-sm border-x border-b border-gray-200">


      <div class="p-4">
        <div class="space-y-3">
          <div
            v-for="notification in notifications"
            :key="notification.id"
            class="flex items-start p-3 rounded-lg hover:bg-gray-50 transition-colors cursor-pointer group relative"
          >
            <div class="flex-1">
              <div class="flex items-start justify-between">
                <div>
                  <h3 class="font-medium text-gray-900">{{ notification.title }}</h3>
                  <p class="text-sm text-gray-600 mt-1">{{ notification.message }}</p>
                  <span class="text-xs text-gray-500 mt-2 block">{{ notification.time }}</span>
                </div>
                <div v-if="notification.unread" class="w-3 h-3 bg-blue-500 rounded-full ml-4"></div>
              </div>
            </div>

            <button class="absolute right-2 top-1/2 -translate-y-1/2 bg-white shadow rounded-full p-1 opacity-0 group-hover:opacity-100 transition-opacity border border-gray-200">
              <DotsHorizontal :size="20" class="text-gray-600" />
            </button>
          </div>
        </div>

        <div v-if="filteredNotifications.length === 0" class="p-8 text-center text-gray-500">
          Brak powiadomień
        </div>
      </div>
    </div>
  </div>
</template>
