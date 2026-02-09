<script setup lang="ts">
import { ref } from 'vue';

interface Listing {
  id: number;
  title: string;
  price: string;
  location: string;
  subInfo?: string; // np. przebieg, powierzchnia, stan
  image: string;
  isFree?: boolean;
}

const listings = ref<Listing>([
  // MOTORYZACJA
  { id: 1, title: '2001 BMW series 7 E38 Individual', price: 'PLN 55,900', location: 'Szczecin', subInfo: '999k km', image: 'https://picsum.photos/id/1071/600/600' },
  { id: 2, title: '2011 BMW 5 series f10 f11 xDrive', price: 'PLN 40,000', location: 'Warszawa', subInfo: '370k km', image: 'https://picsum.photos/id/183/600/600' },
  { id: 3, title: '2002 Subaru Forester S-Turbo', price: 'PLN 49,999', location: 'Tarnów', subInfo: '304k km', image: 'https://picsum.photos/id/133/600/600' },
  { id: 4, title: '2013 Mercedes-Benz Actros 1845', price: 'PLN 20,000', location: 'Katowice', subInfo: '980k km', image: 'https://picsum.photos/id/191/600/600' },

  // NIERUCHOMOŚCI
  { id: 5, title: 'Dom 70 m2 na zgłoszenie całoroczny', price: 'PLN 99,999', location: 'Końskie', subInfo: 'Nowy, drewniany', image: 'https://picsum.photos/id/1020/600/600' },
  { id: 6, title: 'Działka 30 arów nad Jeziorem Miedwie', price: 'PLN 240,000', location: 'Wierzbno', subInfo: 'Budowlana', image: 'https://picsum.photos/id/1015/600/600' },
  { id: 7, title: 'Dom: 5 sypialni, 2 łazienki, ogród', price: 'PLN 550,000', location: 'Puszcza Mariańska', subInfo: '220 m2', image: 'https://picsum.photos/id/1031/600/600' },

  // ELEKTRONIKA
  { id: 8, title: 'Acer Nitro 5 - RTX 3060, 16GB RAM', price: 'PLN 1,450', location: 'Warszawa', subInfo: 'Używany - stan bdb', image: 'https://picsum.photos/id/180/600/600' },
  { id: 9, title: 'Telewizor Telefunken 32" SmartTV', price: 'PLN 400', location: 'Lublin', subInfo: 'Gwarancja 12 msc', image: 'https://picsum.photos/id/250/600/600' },
  { id: 10, title: 'Philipp Plein Watch PWBAA0822 Nowy', price: 'PLN 2,199', location: 'Rawa Mazowiecka', subInfo: 'Oryginał, komplet', image: 'https://picsum.photos/id/175/600/600' },
  { id: 11, title: 'Xiaomi Redmi Note 13 Pro 5G', price: 'PLN 750', location: 'Pabianice', subInfo: '8/256 GB, czarny', image: 'https://picsum.photos/id/160/600/600' },

  // INNE / HOBBY
  { id: 12, title: 'Rowery holenderskie - różne modele', price: 'PLN 100', location: 'Skierniewice', subInfo: 'Cena za sztukę', image: 'https://picsum.photos/id/146/600/600' },
  { id: 13, title: '2022 Suzuki Hayabusa Gen 3', price: 'PLN 68,000', location: 'Perzów', subInfo: '7.9k km', image: 'https://picsum.photos/id/211/600/600' },
  { id: 14, title: 'Koła Alu Porsche 17 cali z adapterami', price: 'PLN 2,700', location: 'Poręba', subInfo: 'Opony lato gratis', image: 'https://picsum.photos/id/1072/600/600' },
  { id: 15, title: 'Likwidacja warsztatu - zestaw narzędzi', price: 'PLN 1,111', location: 'Suchowola', subInfo: 'Klucze, szafki, kompresor', image: 'https://picsum.photos/id/367/600/600' },
  { id: 16, title: 'Sukienki nowe z metkami - wyprzedaż', price: 'BEZPŁATNE', location: 'Mielec', isFree: true, image: 'https://picsum.photos/id/21/600/600' },
  { id: 17, title: '2006 Mitsubishi Colt Drifting Project', price: 'PLN 3,500', location: 'Grójec', subInfo: 'Klatka, kubełek', image: 'https://picsum.photos/id/133/600/600' },
  { id: 18, title: 'Ural 6x6 Wojskowy z zapasów', price: 'PLN 8,500', location: 'Olsztyn', subInfo: 'Sprawny, bez dokumentów', image: 'https://picsum.photos/id/20/600/600' },
]);
</script>

<template>
  <div class="max-w-[1900px] flex flex-col mx-auto px-4 py-8">
    <div class="flex justify-between items-center mb-6 border-b border-gray-100 dark:border-zinc-800 pb-4">
      <h2 class="text-xl font-bold text-gray-900 dark:text-white">Propozycje na dziś</h2>

      <a href="#" class="flex items-center text-[#1877F2] hover:bg-gray-100 dark:hover:bg-zinc-800 px-2 py-1 rounded-md transition-colors duration-200">
        <span class="text-[15px] font-medium">Łęczyca (Gmina) • 500 km</span>
        <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 ml-1" viewBox="0 0 20 20" fill="currentColor">
          <path fill-rule="evenodd" d="M5.05 4.05a7 7 0 119.9 9.9L10 18.9l-4.95-4.95a7 7 0 010-9.9zM10 11a2 2 0 100-4 2 2 0 000 4z" clip-rule="evenodd" />
        </svg>
      </a>
    </div>

    <div class="grid grid-cols-2 sm:grid-cols-5 md:grid-cols-5  xxl:grid-cols-7 gap-x-4 gap-y-6">
      <RouterLink
        v-for="item in listings"
        :key="item.id"
        :to="`/marketplace/item/${item.id}`"
        class="cursor-pointer"
      >
        <div class="aspect-square w-full rounded-xl overflow-hidden bg-gray-100 dark:bg-zinc-800 mb-2 relative">
          <img
            :src="item.image"
            class="w-full h-full object-cover"
            loading="lazy"
          />
          <div v-if="item.isFree" class="absolute top-2 left-2 bg-green-600 text-white text-[10px] font-bold px-2 py-1 rounded shadow-sm uppercase tracking-wider">
            Okazja
          </div>
        </div>

        <div class="px-0.5 overflow-hidden">
          <p class="font-bold text-[17px] leading-tight mb-0.5" :class="item.isFree ? 'text-green-600' : 'text-gray-900 dark:text-white'">
            {{ item.price }}
          </p>

          <div class="max-h-[58px] overflow-hidden leading-tight">
            <h3 class="text-[15px] text-gray-800 dark:text-zinc-300 line-clamp-2">
              {{ item.title }}
            </h3>

            <p class="text-[13px] text-gray-500 dark:text-zinc-500 truncate mt-0.5">
              {{ item.location }}
            </p>

            <p v-if="item.subInfo" class="text-[13px] text-gray-400 dark:text-zinc-600 truncate mt-0.5">
              {{ item.subInfo }}
            </p>
          </div>
        </div>
      </RouterLink>
    </div>
  </div>
</template>

<style scoped>
/* Ograniczenie tytułu do 2 linii */
.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

/* Usunięte nadmiarowe p-0.5 z HTML, aby tekst był równo z obrazkiem */
</style>
