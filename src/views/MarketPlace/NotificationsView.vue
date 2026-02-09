<script setup lang="ts">


import DotsHorizontal from 'vue-material-design-icons/DotsHorizontal.vue';

// Mock data for seller notifications



</script>

<template>
  <div class="max-w-4xl mx-auto">
    <div class="bg-theme-bg-secondary rounded-t-lg shadow-sm border-b border-theme-border p-4 flex justify-between items-center">
      <h1 class="text-2xl font-bold">Powiadomienia sprzedawcy</h1>
      <button class="p-2 hover:bg-theme-hover rounded-full">
        <DotsHorizontal class="text-theme-text-secondary" />
      </button>
    </div>

    <div class="bg-theme-bg-secondary rounded-b-lg shadow-sm border-x border-b border-theme-border">


      <div class="p-4">
        <div class="space-y-3">
          <div
            v-for="notification in notifications"
            :key="notification.id"
            class="flex items-start p-3 rounded-lg hover:bg-theme-hover transition-colors cursor-pointer group relative"
          >
            <div class="flex-1">
              <div class="flex items-start justify-between">
                <div>
                  <h3 class="font-medium text-theme-text">{{ notification.title }}</h3>
                  <p class="text-sm text-theme-text-secondary mt-1">{{ notification.message }}</p>
                  <span class="text-xs text-theme-text-secondary mt-2 block">{{ notification.time }}</span>
                </div>
                <div v-if="notification.unread" class="w-3 h-3 bg-theme-primary rounded-full ml-4"></div>
              </div>
            </div>

            <button class="absolute right-2 top-1/2 -translate-y-1/2 bg-theme-bg-secondary shadow rounded-full p-1 opacity-0 group-hover:opacity-100 transition-opacity border border-theme-border">
              <DotsHorizontal :size="20" class="text-theme-text" />
            </button>
          </div>
        </div>

        <div v-if="filteredNotifications.length === 0" class="p-8 text-center text-theme-text-secondary">
          Brak powiadomień
        </div>
      </div>
    </div>
  </div>
</template>
