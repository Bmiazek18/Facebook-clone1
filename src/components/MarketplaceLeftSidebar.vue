<script setup>
import { defineEmits, defineProps } from 'vue';
import Magnify from 'vue-material-design-icons/Magnify.vue';
import Cog from 'vue-material-design-icons/Cog.vue';
import Storefront from 'vue-material-design-icons/Storefront.vue';
import Bell from 'vue-material-design-icons/Bell.vue';
import TagOutline from 'vue-material-design-icons/TagOutline.vue';
import MapMarker from 'vue-material-design-icons/MapMarker.vue';
import Shopping from 'vue-material-design-icons/Shopping.vue';

const props = defineProps({
  selectedRadius: { type: Number, default: 402 }
});
const emit = defineEmits(['open-location']);

import Car from 'vue-material-design-icons/Car.vue'; // Pojazdy
import HomeCity from 'vue-material-design-icons/HomeCity.vue'; // Wynajem nieruchomości
import Paw from 'vue-material-design-icons/Paw.vue'; // Art. dla zwierząt
import HomeVariant from 'vue-material-design-icons/HomeVariant.vue'; // Art. domowe
import HammerWrench from 'vue-material-design-icons/HammerWrench.vue'; // Art. remontowe
import Run from 'vue-material-design-icons/Run.vue'; // Art. sportowe
import Heart from 'vue-material-design-icons/Heart.vue'; // Dla rodziny
import Home from 'vue-material-design-icons/Home.vue'; // Domy na sprzedaż
import Cellphone from 'vue-material-design-icons/Cellphone.vue'; // Elektronika
import Brush from 'vue-material-design-icons/Brush.vue'; // Hobby

import TshirtCrew from 'vue-material-design-icons/TshirtCrew.vue'; // Odzież
import Shovel from 'vue-material-design-icons/Shovel.vue'; // Ogród
import TagTextOutline from 'vue-material-design-icons/TagTextOutline.vue'; // Ogłoszenia drobne
import MovieOpen from 'vue-material-design-icons/MovieOpen.vue'; // Rozrywka
import GamepadVariant from 'vue-material-design-icons/GamepadVariant.vue'; // Zabawki i gry

// Pełna lista kategorii zgodna ze zrzutem ekranu
const categories = [
  { name: 'Pojazdy', icon: Car },
  { name: 'Wynajem nieruchomości', icon: HomeCity },
  { name: 'Artykuły biurowe', icon: TagOutline },
  { name: 'Artykuły dla zwierząt domowych', icon: Paw },
  { name: 'Artykuły domowe', icon: HomeVariant },
  { name: 'Artykuły remontowe', icon: HammerWrench },
  { name: 'Artykuły sportowe', icon: Run },
  { name: 'Darmowe przedmioty', icon: TagOutline },
  { name: 'Dla rodziny', icon: Heart },
  { name: 'Domy na sprzedaż', icon: Home },
  { name: 'Elektronika', icon: Cellphone },
  { name: 'Hobby', icon: Brush },

  { name: 'Odzież', icon: TshirtCrew },
  { name: 'Ogród i otoczenie', icon: Shovel },
  { name: 'Ogłoszenia drobne', icon: TagTextOutline },
  { name: 'Rozrywka', icon: MovieOpen },
  { name: 'Zabawki i gry', icon: GamepadVariant },
];

const openLocation = () => emit('open-location');
</script>

<template>
  <aside class="w-[360px] bg-white h-screen fixed left-0 top-0 overflow-y-auto border-r border-gray-200 shadow-sm z-10 flex flex-col">

      <div class="p-4 flex justify-between items-center sticky top-0 bg-white z-20">
        <h1 class="text-2xl font-bold">Marketplace</h1>
        <button class="p-2 bg-gray-100 rounded-full hover:bg-gray-200 transition">
          <Cog :size="20" />
        </button>
      </div>

      <div class="px-4 pb-2">
        <div class="relative bg-gray-100 rounded-full h-10 flex items-center px-3">
          <Magnify :size="20" class="text-gray-500 mr-2" />
          <input
            type="text"
            placeholder="Wyszukaj w Marketplace"
            class="bg-transparent border-none outline-none text-sm w-full placeholder-gray-500"
          >
        </div>
      </div>

      <nav class="px-2 mt-2 space-y-1">
        <router-link to="/marketplace" class="flex items-center px-2 py-2 bg-blue-50 text-blue-600 rounded-lg group">
          <div class="p-2 bg-blue-500 text-white rounded-full mr-3">
            <Storefront :size="20" />
          </div>
          <span class="font-medium text-[15px]">Przeglądaj wszystkie</span>
        </router-link>

        <router-link to="/marketplace/notifications" class="flex items-center px-2 py-2 hover:bg-gray-100 rounded-lg transition cursor-pointer">
          <div class="p-2 bg-gray-200 rounded-full mr-3">
            <Bell :size="20" />
          </div>
          <span class="font-medium text-[15px]">Powiadomienia</span>
        </router-link>

        <router-link to="/marketplace" class="flex items-center px-2 py-2 hover:bg-gray-100 rounded-lg transition cursor-pointer">
            <div class="p-2 bg-gray-200 rounded-full mr-3">
            <Shopping :size="20" />
          </div>
          <span class="font-medium text-[15px]">Kupno</span>
        </router-link>

        <router-link to="/marketplace/you/dashboard" class="flex items-center px-2 py-2 hover:bg-gray-100 rounded-lg transition cursor-pointer">
          <div class="p-2 bg-gray-200 rounded-full mr-3">
            <TagOutline :size="20" />
          </div>
          <span class="font-medium text-[15px]">Sprzedaż</span>
        </router-link>
      </nav>

      <div class="px-4 py-3 border-b border-gray-300">
        <RouterLink to="/marketplace/create/item" class="w-full bg-blue-100 text-blue-600 font-semibold py-2 rounded-lg hover:bg-blue-200 transition text-sm flex justify-center items-center">
          <span class="mr-1 text-lg">+</span> Utwórz nowe ogłoszenie
        </RouterLink>
      </div>

      <div class="px-4 py-4 border-b border-gray-300">
        <h3 class="font-semibold text-[17px] mb-2">Lokalizacja</h3>
        <button @click="openLocation" class="text-blue-600 text-sm hover:underline flex items-center">
          <MapMarker :size="16" class="mr-1" />
          Łęczyca gmina - W promieniu {{ props.selectedRadius }} km
        </button>
      </div>


      <div class="px-2 py-4">
        <h3 class="font-semibold text-[17px] px-2 mb-2">Kategorie</h3>
        <ul class="space-y-1">
          <li v-for="(cat, index) in categories" :key="index">
            <a href="#" class="flex items-center px-2 py-2 hover:bg-gray-100 rounded-lg cursor-pointer">
              <div class="mr-3">
                <component :is="cat.icon" :size="20" class="text-gray-800" />
              </div>
              <span class="font-medium text-[15px]">{{ cat.name }}</span>
            </a>
          </li>
        </ul>
      </div>
    </aside>
</template>

<style scoped>
/* keep sidebar styles local if needed */
</style>
