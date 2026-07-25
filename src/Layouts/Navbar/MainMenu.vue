<template>
  <div
    class="max-h-[calc(100vh-4rem)] sm:w-[608px] w-full bg-theme-bg p-4 rounded-b-2xl shadow-2xl   text-[#1c1e21] overflow-hidden flex flex-col"
  >
    <HoverScrollbar max-height="calc(100vh - 100px)">
      <div class="w-full mx-auto flex flex-col sm:flex-row gap-4">
        <div
          class="w-full sm:w-3/5 min-w-0 bg-white dark:bg-[#242526] rounded-lg shadow-[0_1px_2px_rgba(0,0,0,0.1)] p-4"
        >
          <h1 class="text-2xl font-bold mb-4 px-2 text-theme-text">Menu</h1>

          <div class="relative mb-6 px-2">
            <span class="absolute inset-y-0 left-5 flex items-center">
              <MagnifyIcon :size="20" class="text-gray-500" />
            </span>
            <input
              v-model="searchQuery"
              type="text"
              placeholder="Wyszukaj w menu"
              class="w-full bg-[#f0f2f5] dark:bg-gray-700 rounded-full py-2 pl-10 pr-4 focus:outline-none border-none placeholder:text-gray-500 text-theme-text"
            />
          </div>

          <div
            v-for="section in filteredMenu"
            :key="section.title"
            class="mb-4 border-b border-gray-200 dark:border-gray-700 pb-4 last:border-0"
          >
            <h2 class="text-[17px] font-semibold mb-2 px-2 text-theme-text">{{ section.title }}</h2>

            <NuxtLink
              :to="item.to ? item.to : '/'"
              v-for="item in section.items"
              :key="item.name"
              class="flex items-start gap-3 p-2 hover:bg-gray-100 dark:hover:bg-gray-700 rounded-lg cursor-pointer transition group"
            >
              <div class="flex-shrink-0">
                <component :is="item.icon" :size="32" :class="item.iconColor" />
              </div>
              <div>
                <p class="font-medium text-[15px] leading-tight text-theme-text">{{ item.name }}</p>
                <p class="text-[13px] text-[#65676b] dark:text-gray-400 leading-snug mt-0.5">
                  {{ item.description }}
                </p>
              </div>
            </NuxtLink>
          </div>

          <div v-if="filteredMenu.length === 0" class="text-center py-10 text-gray-500">
            Nie znaleziono wyników dla "{{ searchQuery }}"
          </div>
        </div>

        <div
          class="w-full sm:w-2/5 bg-white dark:bg-[#242526] rounded-lg shadow-[0_1px_2px_rgba(0,0,0,0.1)] p-4 sm:sticky lg:top-0 h-fit"
        >
          <h2 class="text-xl font-bold mb-4 px-2 text-theme-text">Utwórz</h2>

          <div
            v-for="(group, idx) in createMenu"
            :key="idx"
            :class="{ 'border-t border-gray-200 dark:border-gray-700 mt-4 pt-2': idx > 0 }"
          >
            <NuxtLink
              :to="item.to"
              v-for="item in group"
              :key="item.name"
              class="flex items-center gap-3 p-2 hover:bg-gray-100 dark:hover:bg-gray-700 rounded-lg cursor-pointer transition"
            >
              <div
                class="bg-[#e4e6eb] dark:bg-gray-700 p-2 rounded-full flex items-center justify-center"
              >
                <component :is="item.icon" :size="20" class="text-black dark:text-white" />
              </div>
              <span class="font-medium text-[15px] text-theme-text">{{ item.name }}</span>
            </NuxtLink>
          </div>
        </div>
      </div>
    </HoverScrollbar>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, markRaw } from 'vue'

// Importy ikon
import MagnifyIcon from 'vue-material-design-icons/Magnify.vue'
import CalendarStarIcon from 'vue-material-design-icons/CalendarStar.vue'
import AccountGroupIcon from 'vue-material-design-icons/AccountGroup.vue'
import NewspaperVariantIcon from 'vue-material-design-icons/NewspaperVariant.vue'
import FlagIcon from 'vue-material-design-icons/Flag.vue'
import ControllerClassicIcon from 'vue-material-design-icons/ControllerClassic.vue'
import PlayCircleIcon from 'vue-material-design-icons/PlayCircle.vue'
import StorefrontIcon from 'vue-material-design-icons/Storefront.vue'
import CreditCardOutlineIcon from 'vue-material-design-icons/CreditCardOutline.vue'
import HistoryIcon from 'vue-material-design-icons/History.vue'
import BookmarkIcon from 'vue-material-design-icons/Bookmark.vue'
import BullhornIcon from 'vue-material-design-icons/Bullhorn.vue'
import PencilIcon from 'vue-material-design-icons/Pencil.vue'
import BookOpenVariantIcon from 'vue-material-design-icons/BookOpenVariant.vue'
import PlusBoxIcon from 'vue-material-design-icons/PlusBox.vue'
import ShoppingIcon from 'vue-material-design-icons/Shopping.vue'
import HeartIcon from 'vue-material-design-icons/Heart.vue'
import HoverScrollbar from '@/components/common/HoverScrollbar.vue'

const searchQuery = ref('')

const fullMenuData = [
  {
    title: 'Społecznościowe',
    items: [
      {
        name: 'Wydarzenia',
        description: 'Organizuj oraz wyszukuj wydarzenia i inne aktywności online i w pobliżu.',
        icon: markRaw(CalendarStarIcon),
        iconColor: 'text-red-500',
        to: '/event',
      },
      {
        name: 'Znajomi',
        description: 'Wyszukuj znajomych lub osoby, które możesz znać.',
        icon: markRaw(AccountGroupIcon),
        iconColor: 'text-blue-500',
      },
      {
        name: 'Grupy',
        description: 'Nawiąż kontakt z osobami, które podzielają Twoje zainteresowania.',
        icon: markRaw(AccountGroupIcon),
        iconColor: 'text-blue-600',
      },
      {
        name: 'Aktualności',
        description: 'Zobacz więcej niedawnych postów od znajomych, z grup, stron i nie tylko.',
        icon: markRaw(NewspaperVariantIcon),
        iconColor: 'text-blue-400',
      },
      {
        name: 'Strony',
        description: 'Odkrywaj firmy na Facebooku i kontaktuj się z nimi.',
        icon: markRaw(FlagIcon),
        iconColor: 'text-orange-500',
      },
    ],
  },
  {
    title: 'Rozrywka',
    items: [
      {
        name: 'Wideo z grami',
        description: 'Oglądaj transmisje z ulubionych gier i łącz się ze streamerami.',
        icon: markRaw(ControllerClassicIcon),
        iconColor: 'text-blue-500',
      },
      {
        name: 'Graj w gry',
        description: 'Graj w ulubione gry.',
        icon: markRaw(ControllerClassicIcon),
        iconColor: 'text-blue-300',
      },
      {
        name: 'Rolki',
        description: 'Sekcja rolek spersonalizowana pod kątem Twoich zainteresowań i kontaktów.',
        icon: markRaw(PlayCircleIcon),
        iconColor: 'text-pink-600',
      },
    ],
  },
  {
    title: 'Zakupy',
    items: [
      {
        name: 'Zamówienia i płatności',
        description: 'Bezproblemowa i bezpieczna metoda płatności w aplikacjach.',
        icon: markRaw(CreditCardOutlineIcon),
        iconColor: 'text-blue-800',
      },
      {
        name: 'Marketplace',
        description: 'Kupuj i sprzedawaj w swojej społeczności.',
        icon: markRaw(StorefrontIcon),
        iconColor: 'text-blue-500',
      },
    ],
  },
  {
    title: 'Osobiste',
    items: [
      {
        name: 'Wspomnienia',
        description: 'Przeglądaj swoje stare zdjęcia, filmy i posty na Facebooku.',
        icon: markRaw(HistoryIcon),
        iconColor: 'text-blue-400',
      },
      {
        name: 'Zapisane',
        description: 'Znajduj posty, zdjęcia i filmy zapisane na później.',
        icon: markRaw(BookmarkIcon),
        iconColor: 'text-purple-600',
      },
    ],
  },
]

const createMenu = [
  [
    { name: 'Opublikuj', icon: markRaw(PencilIcon), to: '/' },
    { name: 'Relacja', icon: markRaw(BookOpenVariantIcon), to: '/' },
    { name: 'Rolka', icon: markRaw(PlayCircleIcon), to: '/create/reel' },
    { name: 'Aktualizacja z życia', icon: markRaw(PlusBoxIcon), to: '/' },
  ],
  [
    { name: 'Strona', icon: markRaw(FlagIcon), to: '/' },
    { name: 'Reklama', icon: markRaw(BullhornIcon), to: '/' },
    { name: 'Grupa', icon: markRaw(AccountGroupIcon), to: '/' },
    { name: 'Zdarzenie', icon: markRaw(PlusBoxIcon), to: '/' },
    { name: 'Ogłoszenie w Marketplace', icon: markRaw(ShoppingIcon), to: '/' },
    { name: 'Zbiórka pieniędzy', icon: markRaw(HeartIcon), to: '/' },
  ],
]

const filteredMenu = computed(() => {
  if (!searchQuery.value) return fullMenuData

  return fullMenuData
    .map((section) => ({
      ...section,
      items: section.items.filter(
        (item) =>
          item.name.toLowerCase().includes(searchQuery.value.toLowerCase()) ||
          item.description.toLowerCase().includes(searchQuery.value.toLowerCase()),
      ),
    }))
    .filter((section) => section.items.length > 0)
})
</script>
