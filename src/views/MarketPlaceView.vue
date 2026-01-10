<script setup>
import { ref } from 'vue';

// Import ikon z pakietu vue-material-design-icons
import Magnify from 'vue-material-design-icons/Magnify.vue';
import Cog from 'vue-material-design-icons/Cog.vue';
import Storefront from 'vue-material-design-icons/Storefront.vue';
import Bell from 'vue-material-design-icons/Bell.vue';

import TagOutline from 'vue-material-design-icons/TagOutline.vue';
import MapMarker from 'vue-material-design-icons/MapMarker.vue';

// Ikony dla konkretnych kategorii
import Car from 'vue-material-design-icons/Car.vue'; // Pojazdy
import HomeCity from 'vue-material-design-icons/HomeCity.vue'; // Wynajem nieruchomości
import Paperclip from 'vue-material-design-icons/Paperclip.vue'; // Art. biurowe (zastępczo, lub TagOutline)
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

const listings = ref([
  {
    id: 1,
    title: '1978 Ural 6x6',
    price: 'PLN 8,500',
    location: 'Olsztyn',
    image: 'https://placehold.co/400x400/555/FFF?text=Ural+6x6',
    isFree: false
  },
  {
    id: 2,
    title: 'Sukienki nowe z metkami',
    price: 'BEZPŁATNE',
    location: 'Mielec',
    image: 'https://placehold.co/400x500/834/FFF?text=Sukienka',
    isFree: true
  },
  {
    id: 3,
    title: '2014 Kia ceed',
    price: 'PLN 34,499',
    location: 'Kraków • 215 km',
    image: 'https://placehold.co/400x300/D22/FFF?text=Kia+Ceed',
    isFree: false
  },
  {
    id: 4,
    title: 'Felgi BBS RS 771 5x100 16',
    price: 'PLN 800',
    location: 'Sopot',
    image: 'https://placehold.co/400x300/333/FFF?text=Felgi',
    isFree: false
  },
  {
    id: 5,
    title: '2000 BMW e46 330ci',
    price: 'PLN 11,500',
    location: 'Przemyśl • 115 km',
    image: 'https://placehold.co/400x300/444/FFF?text=BMW+E46',
    isFree: false
  },
  {
    id: 6,
    title: '1992 BMW E36',
    price: 'PLN 8,500',
    location: 'Siemianowice Śląskie',
    image: 'https://placehold.co/400x300/111/FFF?text=BMW+Drift',
    isFree: false
  },
  {
    id: 7,
    title: 'Kupię BMW e36 coupe czerwoną',
    price: 'PLN 25,000',
    location: 'Radom',
    image: 'https://placehold.co/400x300/900/FFF?text=BMW+Coupe',
    isFree: false
  },
  {
    id: 8,
    title: '2008 Mercedes-Benz s320 w221',
    price: 'PLN 24,000',
    location: 'Warszawa',
    image: 'https://placehold.co/400x300/CCC/000?text=Mercedes',
    isFree: false
  },
  {
    id: 9,
    title: 'Gaz 69',
    price: 'PLN 10',
    location: 'Gdańsk',
    image: 'https://placehold.co/400x300/EEE/333?text=Białka',
    isFree: false
  },
  {
    id: 10,
    title: 'Honda CRF 450',
    price: 'PLN 6,000',
    location: 'Chojnice',
    image: 'https://placehold.co/400x300/F00/FFF?text=Honda',
    isFree: false
  }
]);


// Import components
import BaseModal from '@/components/BaseModal.vue';
import MapRadius from '@/components/MapRadius.vue';

// Modal state
const showLocationModal = ref(false);
const selectedRadius = ref(402); // Domyślny promień

const openLocationModal = () => {
  showLocationModal.value = true;
};

const closeLocationModal = () => {
  showLocationModal.value = false;
};

const handleRadiusUpdate = (radius) => {
  selectedRadius.value = radius;
};

const handleApply = (radius) => {
  selectedRadius.value = radius;
  closeLocationModal();
};

</script>

<template>
  <div class="flex min-h-screen bg-[#F0F2F5] font-sans text-gray-900">

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
        <a href="#" class="flex items-center px-2 py-2 bg-blue-50 text-blue-600 rounded-lg group">
          <div class="p-2 bg-blue-500 text-white rounded-full mr-3">
            <Storefront :size="20" />
          </div>
          <span class="font-medium text-[15px]">Przeglądaj wszystkie</span>
        </a>

        <a href="#" class="flex items-center px-2 py-2 hover:bg-gray-100 rounded-lg transition cursor-pointer">
          <div class="p-2 bg-gray-200 rounded-full mr-3">
            <Bell :size="20" />
          </div>
          <span class="font-medium text-[15px]">Powiadomienia</span>
        </a>

        <a href="#" class="flex items-center px-2 py-2 hover:bg-gray-100 rounded-lg transition cursor-pointer">
          <div class="p-2 bg-gray-200 rounded-full mr-3">
            <WalletShopping :size="20" />
          </div>
          <span class="font-medium text-[15px]">Kupno</span>
        </a>

        <a href="#" class="flex items-center px-2 py-2 hover:bg-gray-100 rounded-lg transition cursor-pointer">
          <div class="p-2 bg-gray-200 rounded-full mr-3">
            <TagOutline :size="20" />
          </div>
          <span class="font-medium text-[15px]">Sprzedaż</span>
        </a>
      </nav>

      <div class="px-4 py-3 border-b border-gray-300">
        <button class="w-full bg-blue-100 text-blue-600 font-semibold py-2 rounded-lg hover:bg-blue-200 transition text-sm flex justify-center items-center">
          <span class="mr-1 text-lg">+</span> Utwórz nowe ogłoszenie
        </button>
      </div>

      <div class="px-4 py-4 border-b border-gray-300">
        <h3 class="font-semibold text-[17px] mb-2">Lokalizacja</h3>
        <button @click="openLocationModal" class="text-blue-600 text-sm hover:underline flex items-center">
          Łęczyca gmina - W promieniu {{ selectedRadius }} km
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

    <main class="ml-[360px] w-full p-6">

      <div class="flex justify-between items-center mt-20">
        <h2 class="text-xl font-bold">Propozycje na dziś</h2>
        <div @click="openLocationModal" class="text-blue-600 text-sm flex items-center cursor-pointer hover:underline">
          <MapMarker :size="16" class="mr-1" />
          Łęczyca (Gmina) • {{ selectedRadius }} km
        </div>
      </div>

      <div class="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 xl:grid-cols-5 gap-4">

        <div v-for="item in listings" :key="item.id" class="cursor-pointer group">
          <div class="aspect-square w-full rounded-lg overflow-hidden bg-gray-200 mb-2 relative">
            <img
              :src="item.image"
              alt="Listing"
              class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-300"
            />
          </div>

          <div class="mb-0.5">
            <span class="font-semibold text-lg" :class="item.isFree ? 'text-green-700' : 'text-gray-900'">
              {{ item.price }}
            </span>
          </div>

          <div class="text-[15px] leading-snug text-gray-900 font-medium mb-0.5 truncate">
            {{ item.title }}
          </div>

          <div class="text-[13px] text-gray-500 truncate">
            {{ item.location }}
          </div>
        </div>

      </div>
    </main>

  </div>

  <!-- Location Modal -->
  <BaseModal v-if="showLocationModal" @close="closeLocationModal" title="Wybierz lokalizację">
    <MapRadius @update:radius="handleRadiusUpdate" @apply="handleApply" />
  </BaseModal>
</template>

<style>
/* Opcjonalnie: ukrycie domyślnego scrollbara dla ładniejszego wyglądu */
::-webkit-scrollbar {
  width: 8px;
}
::-webkit-scrollbar-track {
  background: #f1f1f1;
}
::-webkit-scrollbar-thumb {
  background: #ccc;
  border-radius: 4px;
}
::-webkit-scrollbar-thumb:hover {
  background: #aaa;
}
</style>
