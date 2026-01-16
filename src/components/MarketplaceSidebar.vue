<script setup>
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
  <aside class=" w-[340px] mt-[56px] bg-white min-h-screen font-sans">
    <div class="px-4 py-2">

      <div class="flex items-center gap-3 mb-4">
        <button class="p-2 -ml-2 hover:bg-gray-100 rounded-full transition">
          <ArrowLeft class="text-2xl text-gray-700" />
        </button>

        <div class="flex flex-col">
          <span class="text-sm text-gray-500 leading-tight">Marketplace</span>
          <h1 class="text-2xl font-bold text-gray-900 leading-tight">Sprzedaż</h1>
        </div>
      </div>
      <button class="w-full bg-[#EBF5FF] text-[#0064D1] font-semibold py-2 px-4 rounded-md mb-3 hover:bg-blue-100 transition flex justify-center items-center gap-1">
        <span class="text-xl leading-none mb-0.5">+</span> Utwórz nowe ogłoszenie
      </button>

      <router-link to="/marketplace/you/dashboard" class="block mb-1">
        <div class="flex items-center px-2 py-2 rounded-lg bg-gray-100 hover:bg-gray-200 transition cursor-pointer">
          <div class="w-9 h-9 bg-[#1877F2] rounded-full flex items-center justify-center mr-3 shrink-0">
            <ViewDashboard class="text-white text-xl" />
          </div>
          <span class="font-medium text-[17px] text-gray-900">Pulpit sprzedawcy</span>
        </div>
      </router-link>

      <nav>
        <router-link
          v-for="(item, index) in menuItems"
          :key="index"
          :to="item.to"
          class="flex items-center px-2 py-2 rounded-lg hover:bg-gray-100 transition-colors cursor-pointer mb-1"
        >
          <div v-if="item.type === 'standard'" class="w-9 h-9 bg-gray-200 rounded-full flex items-center justify-center mr-3 shrink-0">
            <component :is="item.icon" class="text-black text-lg" />
          </div>

          <div v-else-if="item.type === 'profile'" class="w-9 h-9 mr-3 shrink-0">
            <img
              :src="item.avatarUrl"
              alt="Profil"
              class="w-full h-full rounded-full object-cover border border-gray-200"
            />
          </div>

          <span class="font-medium text-[17px] text-gray-900">{{ item.label }}</span>
        </router-link>
      </nav>

    </div>
  </aside>
</template>

