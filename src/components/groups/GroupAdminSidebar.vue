<template>
  <div class="w-[360px] h-screen bg-white dark:bg-theme-bg-secondary border-r border-theme-border flex flex-col shrink-0">
    <!-- Header -->
    <div class="p-4 border-b border-theme-border flex items-center justify-between">
      <div>
        <h2 class="text-xl font-bold text-theme-text">Panel administratora</h2>
        <p class="text-xs text-theme-text-secondary mt-0.5">Zarządzaj swoją grupą</p>
      </div>
    </div>

    <!-- Navigation List -->
    <div class="flex-1 py-3 px-2 overflow-y-auto no-scrollbar space-y-4">
      <!-- Top Menu Items -->
      <div class="space-y-0.5">
        <NuxtLink
          v-for="item in topMenu"
          :key="item.title"
          :to="item.route"
          class="group flex items-center space-x-3 px-3 py-2 rounded-lg transition-colors text-theme-text font-medium text-[15px] hover:bg-theme-hover"
          :class="[isRouteActive(item.route) ? 'bg-blue-50 text-[#0866FF] dark:bg-blue-900/20 dark:text-blue-400 font-semibold' : '']"
        >
          <div
            class="p-1.5 rounded-full transition-colors"
            :class="[isRouteActive(item.route) ? 'bg-blue-100 dark:bg-blue-900/40 text-[#0866FF] dark:text-blue-400' : 'bg-gray-100 dark:bg-[#3a3b3c] text-theme-text group-hover:bg-theme-hover-strong']"
          >
            <component :is="item.icon" :size="20" class="shrink-0" />
          </div>
          <span class="truncate">{{ item.title }}</span>
        </NuxtLink>
      </div>

      <!-- Menu Sections -->
      <div v-for="section in menuSections" :key="section.key" class="space-y-1">
        <div class="px-3 mb-1 text-[12px] font-semibold text-theme-text-secondary uppercase tracking-wider">
          {{ section.title }}
        </div>
        <div class="space-y-0.5">
          <NuxtLink
            v-for="item in section.items"
            :key="item.title"
            :to="item.route"
            class="group flex items-center space-x-3 px-3 py-2 rounded-lg transition-colors text-theme-text font-medium text-[15px] hover:bg-theme-hover"
            :class="[isRouteActive(item.route) ? 'bg-blue-50 text-[#0866FF] dark:bg-blue-900/20 dark:text-blue-400 font-semibold' : '']"
          >
            <div
              class="p-1.5 rounded-full transition-colors"
              :class="[isRouteActive(item.route) ? 'bg-blue-100 dark:bg-blue-900/40 text-[#0866FF] dark:text-blue-400' : 'bg-gray-100 dark:bg-[#3a3b3c] text-theme-text group-hover:bg-theme-hover-strong']"
            >
              <component :is="item.icon" :size="20" class="shrink-0" />
            </div>
            <div class="flex flex-col min-w-0 flex-1">
              <span class="truncate leading-tight">{{ item.title }}</span>
              <span v-if="item.subtitle" class="text-[12px] text-theme-text-secondary truncate mt-0.5">
                {{ item.subtitle }}
              </span>
            </div>
          </NuxtLink>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, markRaw } from 'vue'
import { useRoute } from 'vue-router'

// Importy wszystkich ikon
import HomeIcon from 'vue-material-design-icons/Home.vue'
import LayersIcon from 'vue-material-design-icons/Layers.vue'
import CogIcon from 'vue-material-design-icons/Cog.vue'
import AccountPlusIcon from 'vue-material-design-icons/AccountPlus.vue'
import ShieldOutlineIcon from 'vue-material-design-icons/ShieldOutline.vue'
import HelpCircleOutlineIcon from 'vue-material-design-icons/HelpCircleOutline.vue'
import ClockOutlineIcon from 'vue-material-design-icons/ClockOutline.vue'
import AlertOutlineIcon from 'vue-material-design-icons/AlertOutline.vue'
import CalendarIcon from 'vue-material-design-icons/Calendar.vue'
import HistoryIcon from 'vue-material-design-icons/History.vue'
import BookOpenOutlineIcon from 'vue-material-design-icons/BookOpenOutline.vue'
import MessageAlertOutlineIcon from 'vue-material-design-icons/MessageAlertOutline.vue'
import PlusBoxOutlineIcon from 'vue-material-design-icons/PlusBoxOutline.vue'
import TrendingUpIcon from 'vue-material-design-icons/TrendingUp.vue'
import ThumbUpOutlineIcon from 'vue-material-design-icons/ThumbUpOutline.vue'
import ShieldCheckOutlineIcon from 'vue-material-design-icons/ShieldCheckOutline.vue'
import AccountGroupIcon from 'vue-material-design-icons/AccountGroup.vue'

const route = useRoute()
const groupId = computed(() => route.params.id as string)

const isRouteActive = (itemRoute?: string) => {
  if (!itemRoute || itemRoute === '#') return false
  if (itemRoute === `/groups/${groupId.value}`) {
    return route.path === itemRoute
  }
  return route.path.startsWith(itemRoute)
}

const topMenu = computed(() => [
  { title: 'Strona główna społeczności', icon: markRaw(HomeIcon), route: `/groups/${groupId.value}` },
  { title: 'Podsumowanie', icon: markRaw(LayersIcon), route: `/groups/${groupId.value}/overview` }
])

const menuSections = computed(() => [
  {
    key: 'adminTools',
    title: 'Narzędzia dla administratora',
    items: [
      { title: 'Asystent administratora', subtitle: '1 działanie, 1 kryterium', icon: markRaw(CogIcon), route: `/groups/${groupId.value}/admin_assistant` },
      { title: 'Prośby o dołączenie', subtitle: '0 nowych wpisów dziś', icon: markRaw(AccountPlusIcon), route: `/groups/${groupId.value}/member-requests` },
      { title: 'Prośby dotyczące odznaki', subtitle: '0 nowych wpisów dziś', icon: markRaw(ShieldOutlineIcon), route: '#' },
      { title: 'Pytania dotyczące członkostwa', icon: markRaw(HelpCircleOutlineIcon), route: '#' },
      { title: 'Oczekujące posty', subtitle: '0 nowych wpisów dziś', icon: markRaw(ClockOutlineIcon), route: '#' },
      { title: 'Potencjalny spam', subtitle: '0 nowych wpisów dziś', icon: markRaw(AlertOutlineIcon), route: '#' },
      { title: 'Zaplanowane posty', icon: markRaw(CalendarIcon), route: '#' },
      { title: 'Dziennik aktywności', icon: markRaw(HistoryIcon), route: `/groups/${groupId.value}/admin_activities` },
      { title: 'Reguły grupy', icon: markRaw(BookOpenOutlineIcon), route: `/groups/${groupId.value}/manage_rules` },
      { title: 'Materiały zgłoszone przez członków grupy', icon: markRaw(MessageAlertOutlineIcon), route: '#' }
    ]
  },
  {
    key: 'settings',
    title: 'Ustawienia',
    items: [
      { title: 'Ustawienia grupy', subtitle: 'Zarządzaj dyskusjami, uprawnieniami i rolami', icon: markRaw(CogIcon), route: `/groups/${groupId.value}/edit` },

    ]
  },
  {
    key: 'stats',
    title: 'Statystyki',
    items: [
      { title: 'Wzrost', icon: markRaw(TrendingUpIcon), route: '#' },
      { title: 'Aktywność', icon: markRaw(ThumbUpOutlineIcon), route: '#' },
      { title: 'Administratorzy i moderatorzy', icon: markRaw(ShieldCheckOutlineIcon), route: '#' },
      { title: 'Członkowie', icon: markRaw(AccountGroupIcon), route: `/groups/${groupId.value}/members` }
    ]
  },
  {
    key: 'help',
    title: 'Pomoc',
    items: [
      { title: 'Centrum pomocy', icon: markRaw(HelpCircleOutlineIcon), route: '#' }
    ]
  }
])
</script>

<style scoped>
.no-scrollbar::-webkit-scrollbar {
  display: none;
}
.no-scrollbar {
  -ms-overflow-style: none;
  scrollbar-width: none;
}
</style>
