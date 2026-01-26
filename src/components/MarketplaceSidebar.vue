<script setup lang="ts">
// Dodajemy import strzałki
import ArrowLeft from 'vue-material-design-icons/ArrowLeft.vue';
import ViewDashboard from 'vue-material-design-icons/ViewDashboard.vue';
import TagMultiple from 'vue-material-design-icons/TagMultiple.vue';
import Bell from 'vue-material-design-icons/Bell.vue';
import ChartBar from 'vue-material-design-icons/ChartBar.vue';

// Definicja elementów menu
const menuItems = [
  {
    label: 'Twoje ogłoszenia',
    icon: TagMultiple,
    to: '/marketplace/you/listings',
    type: 'standard'
  },
  {
    label: 'Powiadomienia',
    icon: Bell,
    to: '/marketplace/you/notifications',
    type: 'standard'
  },
  {
    label: 'Statystyki',
    icon: ChartBar,
    to: '/marketplace/you/insights',
    type: 'standard'
  },
  {
    label: 'Profil w Marketplace',
    to: '/marketplace/you/profile',
    type: 'profile',
    avatarUrl: 'https://i.pravatar.cc/150?img=11'
  },
];
</script>

<template>
  <aside class=" w-85 mt-14 bg-theme-bg-secondary min-h-screen font-sans">
    <div class="px-4 py-2">

      <div class="flex items-center gap-3 mb-4">
        <button class="p-2 -ml-2 hover:bg-theme-hover rounded-full transition">
          <ArrowLeft class="text-2xl text-theme-text" />
        </button>

        <div class="flex flex-col">
          <span class="text-sm text-theme-text-secondary leading-tight">Marketplace</span>
          <h1 class="text-2xl font-bold text-theme-text leading-tight">Sprzedaż</h1>
        </div>
      </div>
      <button class="w-full bg-theme-bg-tertiary text-theme-primary font-semibold py-2 px-4 rounded-md mb-3 hover:bg-theme-hover transition flex justify-center items-center gap-1">
        <span class="text-xl leading-none mb-0.5">+</span> Utwórz nowe ogłoszenie
      </button>

      <router-link to="/marketplace/you/dashboard" class="block mb-1">
        <div class="flex items-center px-2 py-2 rounded-lg bg-theme-bg-tertiary hover:bg-theme-hover transition cursor-pointer">
          <div class="w-9 h-9 bg-theme-primary rounded-full flex items-center justify-center mr-3 shrink-0">
            <ViewDashboard class="text-white text-xl" />
          </div>
          <span class="font-medium text-[17px] text-theme-text">Pulpit sprzedawcy</span>
        </div>
      </router-link>

      <nav>
        <router-link
          v-for="(item, index) in menuItems"
          :key="index"
          :to="item.to"
          class="flex items-center px-2 py-2 rounded-lg hover:bg-theme-hover transition-colors cursor-pointer mb-1"
        >
          <div v-if="item.type === 'standard'" class="w-9 h-9 bg-theme-bg-tertiary rounded-full flex items-center justify-center mr-3 shrink-0">
            <component :is="item.icon" class="text-theme-text text-lg" />
          </div>

          <div v-else-if="item.type === 'profile'" class="w-9 h-9 mr-3 shrink-0">
            <img
              :src="item.avatarUrl"
              alt="Profil"
              class="w-full h-full rounded-full object-cover border border-theme-border"
            />
          </div>

          <span class="font-medium text-[17px] text-theme-text">{{ item.label }}</span>
        </router-link>
      </nav>

    </div>
  </aside>
</template>

