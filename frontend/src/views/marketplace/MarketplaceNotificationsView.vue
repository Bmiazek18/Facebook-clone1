<script setup lang="ts">
import { ref } from 'vue'

import DotsHorizontal from 'vue-material-design-icons/DotsHorizontal.vue'

const notifications = ref([
  {
    id: 1,
    type: 'listing_group',
    // Nowe zdjęcie zestawu sportowego
    image: 'http://googleusercontent.com/image_collection/image_retrieval/2748515228577376638_0',
    textPrefix: 'Produkt',
    highlight1: 'Koszulki Zina, getry, jak nowe',
    textMiddle: 'został niedawno wystawiony na sprzedaż w grupie',
    highlight2: 'Giełda Sędziego Piłkarskiego',
    textSuffix: 'za $1.00.',
    time: '5 dni',
    unread: false,
    category: 'Wcześniejsze',
  },
  {
    id: 2,
    type: 'standard',
    // Nowe zdjęcie BMW
    image: 'http://googleusercontent.com/image_collection/image_retrieval/5295048630903438718_0',
    textPrefix: 'Użytkownik Marek polubił Twoje ogłoszenie',
    highlight1: 'BMW E46 Części',
    textMiddle: '',
    highlight2: '',
    textSuffix: '.',
    time: '6 dni',
    unread: true,
    category: 'Wcześniejsze',
  },
])
</script>

<template>
  <div class="max-w-2xl mt-14 mx-auto">
    <div class="bg-theme-bg-secondary rounded-t-lg shadow-sm p-4 flex justify-between items-center">
      <h1 class="text-2xl font-bold">{{ $t('notifications_page.title') }}</h1>
    </div>

    <div class="bg-theme-bg-secondary rounded-b-lg shadow-sm">
      <div class="p-4">
        <h2 class="text-[17px] font-semibold text-theme-text mb-3">{{ $t('notifications_page.earlier') }}</h2>

        <div class="space-y-2">
          <div
            v-for="notification in notifications"
            :key="notification.id"
            class="flex items-start p-2 rounded-lg hover:bg-theme-hover transition-colors cursor-pointer group relative"
          >
            <div class="relative shrink-0 mr-3">
              <img
                :src="notification.image"
                :alt="$t('marketplace.product')"
                class="w-14 h-14 rounded-md object-cover border border-theme-border"
              />
              <div
                v-if="notification.unread"
                class="absolute -top-1 -right-1 w-3 h-3 bg-theme-primary rounded-full border-2 border-theme-bg-secondary"
              ></div>
            </div>

            <div class="flex-1 pr-8">
              <p class="text-[15px] leading-snug text-theme-text">
                {{ notification.textPrefix }}
                <span class="font-bold">{{ notification.highlight1 }}</span>
                {{ notification.textMiddle }}
                <span v-if="notification.highlight2" class="font-bold">{{
                  notification.highlight2
                }}</span>
                {{ notification.textSuffix }}
              </p>
              <span
                class="text-xs font-medium text-theme-primary block mt-1"
                v-if="notification.unread"
                >{{ $t('notifications_page.new') }}</span
              >
              <span class="text-xs text-theme-text-secondary block mt-1" v-else>{{
                notification.time
              }}</span>
            </div>

            <button
              class="absolute right-2 top-1/2 -translate-y-1/2 bg-theme-bg-secondary shadow rounded-full p-1 opacity-0 group-hover:opacity-100 transition-opacity border border-theme-border"
            >
              <DotsHorizontal :size="20" class="text-theme-text" />
            </button>
          </div>
        </div>
      </div>

      <div v-if="notifications.length === 0" class="p-8 text-center text-theme-text-secondary">{{ $t('notifications_page.noNotifications') }}</div>
    </div>
  </div>
</template>
