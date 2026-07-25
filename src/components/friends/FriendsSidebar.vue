<template>
  <div
    class="w-[360px] bg-theme-bg-secondary shadow-lg h-screen fixed left-0 top-[50px] pt-4 px-2 flex flex-col overflow-y-auto z-10"
  >
    <div class="flex justify-between items-center px-2 mb-4">
      <h1 class="text-[24px] font-bold text-theme-text">{{ t('friends.friends') }}</h1>

      <!-- Implementacja przez v-dropdown -->
      <!-- placement i distance dbają o idealne dopasowanie okienka pod ikoną -->
      <v-dropdown placement="bottom-end" :distance="12" strategy="fixed">
        <!-- Domyślny slot działa jako Trigger (wyzwalacz) -->
        <button
          class="bg-theme-bg-tertiary w-9 h-9 rounded-full flex items-center justify-center cursor-pointer hover:bg-theme-bg-hover transition-colors focus:outline-none aria-expanded:bg-[#e7f3ff] aria-expanded:text-[#1877f2]"
        >
          <CogIcon :size="20" />
        </button>

        <!-- Slot na zawartość menu (Dostosuj nazwę slotu np. #popper lub #content zależnie od Twojej biblioteki) -->
        <template #popper>
          <div
            class="w-[340px] bg-white dark:bg-theme-bg-secondary p-4 text-theme-text rounded-xl shadow-xl border border-gray-100 dark:border-zinc-700"
          >
            <h2 class="text-[18px] font-bold text-gray-900 dark:text-white mb-1">
              {{ t('friends.notificationSettings') }}
            </h2>
            <p class="text-[14px] text-gray-500 dark:text-gray-400 leading-tight mb-4">
              {{ t('friends.notificationSettingsDesc') }}
            </p>

            <hr class="border-gray-200 dark:border-zinc-700 mb-4" />

            <div class="flex items-center justify-between">
              <div class="flex items-center gap-3">
                <!-- Ikona powiadomienia w kółku -->
                <div
                  class="w-10 h-10 bg-gray-100 dark:bg-theme-bg-tertiary rounded-full flex items-center justify-center text-gray-700 dark:text-gray-300"
                >
                  <BellOutlineIcon :size="22" />
                </div>
                <span class="text-[16px] font-medium text-gray-900 dark:text-white">
                  {{ t('friends.showNotificationDots') }}
                </span>
              </div>

              <!-- Przełącznik (Toggle Switch) -->
              <label class="relative inline-flex items-center cursor-pointer select-none">
                <input type="checkbox" v-model="showNotificationDots" class="sr-only peer" />
                <div
                  class="w-11 h-6 bg-gray-200 peer-focus:outline-none rounded-full peer dark:bg-zinc-700 peer-checked:after:translate-x-full rtl:peer-checked:after:-translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:start-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all dark:border-gray-600 peer-checked:bg-[#1877f2]"
                ></div>
              </label>
            </div>
          </div>
        </template>
      </v-dropdown>
    </div>

    <!-- Reszta Twojego menu bez zmian -->
    <div class="flex flex-col gap-1">
      <NuxtLink
        v-for="(item, index) in menuItems"
        :key="index"
        :to="item.path"
        v-slot="{ isActive }"
      >
        <div
          class="flex items-center justify-between px-2 py-2 hover:bg-theme-bg-tertiary rounded-lg cursor-pointer group transition-colors"
          :class="{ 'bg-theme-bg-tertiary': isActive }"
        >
          <div class="flex items-center gap-3">
            <div
              class="rounded-full p-1.5"
              :class="[
                isActive
                  ? 'bg-[#1877f2] text-white'
                  : 'bg-theme-bg-tertiary text-theme-text group-hover:bg-theme-bg-secondary',
              ]"
            >
              <component :is="item.icon" :size="20" />
            </div>
            <span class="text-[17px] font-medium text-theme-text">{{ t(item.label) }}</span>
          </div>
          <ChevronRightIcon v-if="!isActive" :size="24" class="text-theme-text-secondary" />
        </div>
      </NuxtLink>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useI18n } from 'vue-i18n'

// Icons
import CogIcon from 'vue-material-design-icons/Cog.vue'
import AccountGroupIcon from 'vue-material-design-icons/AccountGroup.vue'
import GiftIcon from 'vue-material-design-icons/Gift.vue'
import ChevronRightIcon from 'vue-material-design-icons/ChevronRight.vue'
import BellOutlineIcon from 'vue-material-design-icons/BellOutline.vue'

const { t } = useI18n()

// Zostawiamy tylko stan samego przełącznika kropki
const showNotificationDots = ref(true)

const menuItems = [
  { label: 'friends.home', icon: AccountGroupIcon, path: '/friends' },
  { label: 'friends.birthdays', icon: GiftIcon, path: '/friends/birthday' },
]
</script>
