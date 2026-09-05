<template>
  <div class="max-w-xl mx-auto bg-theme-bg-secondary text-theme-text p-2">
    <!-- Header -->
    <div class="w-full flex items-center pb-3 border-b border-theme-border mb-2">
      <button
        @click="emit('back')"
        class="rounded-full p-2 -ml-2 hover:bg-theme-hover transition duration-150 mr-2 cursor-pointer"
        aria-label="Wróć"
      >
        <ArrowLeftIcon class="text-2xl text-theme-text" />
      </button>
      <span class="text-theme-text font-bold text-lg leading-snug">
        Wszystkie profile i strony
      </span>
    </div>

    <!-- Lista kont -->
    <div class="space-y-1 py-1">
      <!-- Główny profil użytkownika -->
      <button
        @click="selectUser"
        class="w-full flex items-center justify-between p-2 rounded-lg hover:bg-theme-hover transition duration-150 cursor-pointer text-left"
        :class="{ 'bg-theme-hover/60': !authStore.isActingAsPage }"
      >
        <div class="flex items-center space-x-3">
          <img
            :src="authStore.originalUser?.avatar || 'https://i.pravatar.cc/150?img=1'"
            class="h-10 w-10 bg-theme-bg-tertiary rounded-full object-cover shrink-0"
            :alt="authStore.originalUser?.name || 'Profil użytkownika'"
          />
          <div>
            <div class="font-semibold text-theme-text text-[15px]">
              {{ authStore.originalUser?.name || 'Mój Profil' }}
            </div>
            <div class="text-xs text-theme-text-secondary">
              Profil osobisty
            </div>
          </div>
        </div>
        <div v-if="!authStore.isActingAsPage" class="w-2.5 h-2.5 bg-blue-500 rounded-full mr-2"></div>
      </button>

      <!-- Strony użytkownika -->
      <div v-if="authStore.userPages.length > 0" class="pt-2">
        <div class="text-xs font-semibold text-theme-text-secondary px-2 mb-1 uppercase tracking-wider">
          Twoje strony ({{ authStore.userPages.length }})
        </div>
        <button
          v-for="page in authStore.userPages"
          :key="page.id"
          @click="selectPage(page)"
          class="w-full flex items-center justify-between p-2 rounded-lg hover:bg-theme-hover transition duration-150 cursor-pointer text-left"
          :class="{ 'bg-theme-hover/60': authStore.isActingAsPage && authStore.activePageId === page.id }"
        >
          <div class="flex items-center space-x-3">
            <img
              :src="page.avatar || 'https://i.pravatar.cc/150?img=2'"
              class="h-10 w-10 bg-theme-bg-tertiary rounded-full object-cover shrink-0"
              :alt="page.name"
            />
            <div>
              <div class="font-semibold text-theme-text text-[15px]">
                {{ page.name }}
              </div>
              <div class="text-xs text-theme-text-secondary">
                {{ page.category || 'Strona' }}
              </div>
            </div>
          </div>
          <div
            v-if="authStore.isActingAsPage && authStore.activePageId === page.id"
            class="w-2.5 h-2.5 bg-blue-500 rounded-full mr-2"
          ></div>
        </button>
      </div>

      <!-- Przycisk Utwórz nową stronę -->
      <NuxtLink
        to="/pages/creation"
        class="w-full flex items-center space-x-3 p-2 mt-2 rounded-lg hover:bg-theme-hover transition duration-150 cursor-pointer text-left text-blue-600 dark:text-blue-400 font-medium text-sm"
      >
        <div class="w-10 h-10 rounded-full bg-blue-100 dark:bg-blue-900/40 flex items-center justify-center shrink-0">
          <PlusIcon :size="22" class="text-blue-600 dark:text-blue-400" />
        </div>
        <span>Utwórz nową stronę</span>
      </NuxtLink>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useAuthStore } from '@/stores/auth'
import ArrowLeftIcon from 'vue-material-design-icons/ArrowLeft.vue'
import PlusIcon from 'vue-material-design-icons/Plus.vue'
import type { Page } from '@/types/Page'

const authStore = useAuthStore()
const emit = defineEmits(['back', 'selected'])

const selectUser = () => {
  authStore.switchToUser()
  emit('selected')
}

const selectPage = (page: Page) => {
  authStore.switchToPage(page)
  emit('selected')
}
</script>
