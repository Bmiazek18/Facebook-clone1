<script setup lang="ts">
import { ref } from 'vue';

// Mock data for listings
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

</script>

<template>
  <div>

      <div class="flex justify-between items-center mt-20">
        <h2 class="text-xl font-bold">Propozycje na dziś</h2>
      </div>

      <div class="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 xl:grid-cols-5 gap-4">

        <RouterLink
          v-for="item in listings"
          :key="item.id"
          :to="`/marketplace/item/${item.id}`"
          class="cursor-pointer group block"
        >
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
        </RouterLink>

      </div>
  </div>
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
