<script setup>
import { ref } from 'vue';

// Import ikon z vue-material-design-icons
// sidebar moved to MarketplaceSidebar.vue
import Star from 'vue-material-design-icons/Star.vue';
import Pencil from 'vue-material-design-icons/Pencil.vue';
import EyeOutline from 'vue-material-design-icons/EyeOutline.vue'; // Oko
import BookmarkOutline from 'vue-material-design-icons/BookmarkOutline.vue'; // Zapisane
import ShareVariant from 'vue-material-design-icons/ShareVariant.vue'; // Udostępnienie
import AccountGroup from 'vue-material-design-icons/AccountGroup.vue'; // Obserwujący
import ChevronDown from 'vue-material-design-icons/ChevronDown.vue';

// Dane do menu bocznego

// Dane do sekcji "Twoje ogłoszenia"
const listingStats = [
  { label: 'Wymagane działanie', value: 0 },
  { label: 'Aktywne i oczekujące', value: 0 },
  { label: 'Sprzedane lub brak na stanie', value: 0 },
  { label: 'Wersje robocze', value: 0 },
  { label: 'Do odnowienia', value: 0 },
  { label: 'Do usunięcia i ponownego zamieszczenia', value: 0 },
];

// Dane do sekcji "Statystyki dotyczące Marketplace"
const marketStats = [
  { label: 'Kliknięcia w ogłoszeniach', value: 0, icon: EyeOutline },
  { label: 'Zdarzenia zapisania ogłoszenia', value: 0, icon: BookmarkOutline },
  { label: 'Udostępnienia ogłoszenia', value: 0, icon: ShareVariant },
  { label: 'Obserwujący w Marketplace', value: 0, icon: AccountGroup },
];
</script>

<template>
  <div class="max-w-5xl mx-auto mt-10 space-y-6">

    <section class="bg-white rounded-lg shadow-sm border border-gray-200 p-4">
      <h2 class="text-lg font-bold mb-3">Podsumowanie</h2>
      <div class="border border-gray-200 rounded-lg p-4 w-full md:w-1/2">
        <div class="flex items-center mb-1">
          <span class="text-2xl font-bold mr-2">0</span>
          <Star class="text-black" />
        </div>
        <div class="text-sm text-gray-600">Ocena sprzedawcy</div>
        <div class="text-xs text-gray-400 mt-1">0 ocen</div>
      </div>
    </section>

    <section class="bg-white rounded-lg shadow-sm border border-gray-200 p-4">
      <div class="flex justify-between items-center mb-4">
        <h2 class="text-lg font-bold">Twoje ogłoszenia</h2>
        <div class="space-x-2">
          <button class="px-3 py-1.5 text-blue-600 font-semibold bg-blue-50 rounded hover:bg-blue-100 text-sm">
            <Pencil class="inline-block mr-1 align-middle text-sm" /> Promuj ogłoszenia
          </button>
          <button class="px-3 py-1.5 text-blue-600 font-semibold bg-blue-50 rounded hover:bg-blue-100 text-sm">
            <Pencil class="inline-block mr-1 align-middle text-sm" /> Utwórz nowe ogłoszenie
          </button>
        </div>
      </div>

      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-3">
        <div
          v-for="(stat, index) in listingStats"
          :key="index"
          class="border border-gray-200 rounded-lg p-3 flex flex-col justify-center min-h-[80px]"
        >
          <span class="text-xl font-bold">{{ stat.value }}</span>
          <span class="text-sm text-gray-600 leading-tight mt-1">{{ stat.label }}</span>
        </div>
      </div>

      <div class="mt-4 text-center border-t border-gray-100 pt-3">
        <a href="#" class="text-blue-600 font-medium text-sm hover:underline">Zobacz wszystkie ogłoszenia</a>
      </div>
    </section>

    <section class="bg-white rounded-lg shadow-sm border border-gray-200 p-4">
      <div class="flex justify-between items-center mb-4">
        <h2 class="text-lg font-bold">Statystyki dotyczące Marketplace</h2>
        <button class="bg-gray-100 hover:bg-gray-200 text-gray-700 px-3 py-1.5 rounded text-sm font-medium flex items-center">
          Ostatnie 7 dni <ChevronDown class="ml-1" />
        </button>
      </div>

      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-3">
        <div
          v-for="(stat, index) in marketStats"
          :key="index"
          class="border border-gray-200 rounded-lg p-4 flex flex-col min-h-[100px]"
        >
          <div class="mb-2">
             <component :is="stat.icon" class="text-gray-500" />
          </div>
          <span class="text-xl font-bold">{{ stat.value }}</span>
          <span class="text-sm text-gray-600 mt-1 leading-tight">{{ stat.label }}</span>
        </div>
      </div>

      <div class="mt-4 text-center border-t border-gray-100 pt-3">
        <a href="#" class="text-blue-600 font-medium text-sm hover:underline">Zobacz więcej statystyk</a>
      </div>
    </section>

    <div class="fixed bottom-6 right-6 bg-white p-3 rounded-full shadow-lg cursor-pointer hover:bg-gray-50 border border-gray-200">
      <Pencil class="text-gray-700" />
    </div>

  </div>
</template>
