<script setup lang="ts">
import { ref } from 'vue'
import { useQuery } from '@vue/apollo-composable'
import { gql } from '@apollo/client/core'
import MarketplaceLeftSidebar from '@/components/marketplace/MarketplaceLeftSidebar.vue'
import BaseModal from '@/components/common/BaseModal.vue'
import MapRadius from '@/components/marketplace/MapRadius.vue'

// Stan modalu lokalizacji
const showLocationModal = ref(false)
const selectedRadius = ref(402) // Domyślny promień w km
const currentLat = ref(52.0593) // Szerokość geograficzna
const currentLon = ref(19.2003) // Długość geograficzna
const currentCityName = ref('Łęczyca') // Nazwa miasta
const searchQuery = ref('') // Fraza wyszukiwania

const openLocationModal = () => {
  showLocationModal.value = true
}

const closeLocationModal = () => {
  showLocationModal.value = false
}

const handleRadiusUpdate = (radius: number) => {
  selectedRadius.value = radius
}

const handleApply = (radius: number, lat?: number, lon?: number, cityName?: string) => {
  selectedRadius.value = radius
  if (lat !== undefined && lon !== undefined) {
    currentLat.value = lat
    currentLon.value = lon
  }
  if (cityName !== undefined) {
    currentCityName.value = cityName
  }
  closeLocationModal()
  // W Apollo nie musimy wywoływać funkcji fetch ręcznie – zmiana zmiennych automatycznie odświeży dane!
}

const handleSearch = (q: string) => {
  searchQuery.value = q
}

// Definicja zapytania GraphQL
const GET_NEARBY_LISTINGS = gql`
  query GetNearbyListings($lat: Float!, $lon: Float!, $radius: Float, $query: String) {
    getNearbyListings(lat: $lat, lon: $lon, radius: $radius, query: $query) {
      id
      title
      price
      category
      condition
      description
      latitude
      longitude
      createdAt
    }
  }
`

// Użycie Apollo useQuery – automatycznie zarządza cashem i zapobiega niepotrzebnym powtórnym strzałom do API
const { result, loading, error } = useQuery(GET_NEARBY_LISTINGS, () => ({
  lat: currentLat.value,
  lon: currentLon.value,
  radius: selectedRadius.value * 1000.0, // Przeliczenie na metry
  query: searchQuery.value,
}), {
  // Opcje cache: jeśli zmienne się nie zmieniły, Apollo bierze dane z pamięci podręcznej
  fetchPolicy: 'cache-first',
  // Zapobiega ponownemu odpytywaniu przy zmianie focusu okna
  refetchOnWindowFocus: false,
})

// Przekształcenie danych z Apollo na format interfejsu widoku
const listings = computed(() => {
  const dataList = result.value?.getNearbyListings || []
  return dataList.map((item: any) => ({
    id: item.id,
    title: item.title,
    price: Number(item.price) === 0 ? 'BEZPŁATNE' : `PLN ${Number(item.price).toLocaleString()}`,
    location: currentCityName.value,
    subInfo: item.condition === 'NEW' ? 'Nowy' : 'Używany',
    image: `https://picsum.photos/seed/${item.id}/600/600`,
    isFree: Number(item.price) === 0,
  }))
})
</script>

<template>
  <div class="flex h-screen overflow-hidden bg-theme-bg text-theme-text">
    <MarketplaceLeftSidebar
      :selectedRadius="selectedRadius"
      :selectedCityName="currentCityName"
      @open-location="openLocationModal"
      @update:search="handleSearch"
    />

    <main class="flex-1 h-full mt-[56px] overflow-y-auto relative">
      <div class="max-w-[1900px] flex flex-col mx-auto px-4 py-4">
        <div class="flex justify-between items-center border-b border-gray-100 dark:border-zinc-800 pb-4">
          <h2 class="text-xl font-bold text-gray-900 dark:text-white">Propozycje na dziś</h2>

          <button
            @click="openLocationModal"
            class="flex items-center text-[#1877F2] hover:bg-gray-100 dark:hover:bg-zinc-800 px-2 py-1 rounded-md transition-colors duration-200 cursor-pointer"
          >
            <span class="text-[15px] font-medium">{{ currentCityName }} • {{ selectedRadius }} km</span>
            <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 ml-1" viewBox="0 0 20 20" fill="currentColor">
              <path fill-rule="evenodd" d="M5.05 4.05a7 7 0 119.9 9.9L10 18.9l-4.95-4.95a7 7 0 010-9.9zM10 11a2 2 0 100-4 2 2 0 000 4z" clip-rule="evenodd" />
            </svg>
          </button>
        </div>

        <!-- Stan ładowania -->
        <div v-if="loading && listings.length === 0" class="text-center py-10 text-gray-500">
          Ładowanie ogłoszeń...
        </div>

        <!-- Stan błędu -->
        <div v-if="error" class="text-center py-10 text-red-500">
          Nie udało się pobrać ogłoszeń. Spróbuj ponownie później.
        </div>

        <!-- Siatka ogłoszeń -->
        <div
          v-if="!loading || listings.length > 0"
          class="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 2xl:grid-cols-6 gap-x-4 gap-y-6"
        >
          <NuxtLink
            v-for="item in listings"
            :key="item.id"
            :to="`/marketplace/item/${item.id}`"
            class="cursor-pointer"
          >
            <div class="aspect-square w-full rounded-xl overflow-hidden bg-gray-100 dark:bg-zinc-800 mb-2 relative">
              <img :src="item.image" class="w-full h-full object-cover" loading="lazy" />
              <div
                v-if="item.isFree"
                class="absolute top-2 left-2 bg-green-600 text-white text-[10px] font-bold px-2 py-1 rounded shadow-sm uppercase tracking-wider"
              >
                Okazja
              </div>
            </div>

            <div class="px-0.5 overflow-hidden">
              <p
                class="font-bold text-[17px] leading-tight mb-0.5"
                :class="item.isFree ? 'text-green-600' : 'text-gray-900 dark:text-white'"
              >
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
          </NuxtLink>
        </div>
      </div>
    </main>
  </div>

  <!-- Modal Lokalizacji -->
  <BaseModal v-if="showLocationModal" @close="closeLocationModal" title="Wybierz lokalizację">
    <MapRadius @update:radius="handleRadiusUpdate" @apply="handleApply" />
  </BaseModal>
</template>

<style scoped>
.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>
