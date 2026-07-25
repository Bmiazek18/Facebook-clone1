<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useCreatePostStore } from '@/stores/createPost'

import MapMarkerIcon from 'vue-material-design-icons/MapMarker.vue'
import MapMarkerOutlineIcon from 'vue-material-design-icons/MapMarkerOutline.vue'
import OfficeBuildingIcon from 'vue-material-design-icons/OfficeBuilding.vue'
import TreeIcon from 'vue-material-design-icons/Tree.vue'
import NavigationIcon from 'vue-material-design-icons/Navigation.vue'
import CloseIcon from 'vue-material-design-icons/Close.vue'
import MagnifyIcon from 'vue-material-design-icons/Magnify.vue'
import type { PostLocation } from '@/types/Post'

interface Suggestion {
  name: string
  full_address: string
  place_formatted: string
  feature_type: string
  mapbox_id: string
}



const createPostStore = useCreatePostStore()
const searchQuery = ref('')
const suggestions = ref<PostLocation[]>([])
const nearbyPlaces = ref<PostLocation[]>([])

const currentCity = ref<PostLocation | null>(null)
const selectedLocation = ref<PostLocation | null>(null)
const loading = ref(false)
const sessionToken = ref<string | null>(null)
let debounceTimeout: ReturnType<typeof setTimeout>

const MAPBOX_TOKEN = import.meta.env.VITE_MAPBOX_KEY as string

const ensureSessionToken = () => {
  if (!sessionToken.value) {
    sessionToken.value = Math.random().toString(36).substring(2, 15)
  }
}

// --- POPRAWIONA FUNKCJA POBIERAJĄCA ATRAKCJE ---
const fetchNearbyPOIs = async (lat: number, lon: number) => {
  ensureSessionToken()
  try {
    const categories =
      'tourist_attraction,monument,museum,park,beach,landmark,historic,place_of_worship'

    // Zamiast nazwy ulicy, używamy ogólnego zapytania. Mapbox z proximity znajdzie POI w okolicy.
    const query = encodeURIComponent('ciekawe miejsca')

    const url =
      `https://api.mapbox.com/search/searchbox/v1/suggest` +
      `?q=${query}` +
      `&access_token=${MAPBOX_TOKEN}` +
      `&language=pl` +
      `&limit=10` +
      `&poi_category=${categories}` +
      `&proximity=${lon},${lat}` + // Kluczowy parametr naprawiający rozrzut wyników
      `&session_token=${sessionToken.value}`

    const res = await fetch(url)
    if (!res.ok) return

    const data = await res.json()

    if (data.suggestions) {
      nearbyPlaces.value = data.suggestions
        .filter(
          (item: Suggestion) => item.feature_type !== 'place' && item.feature_type !== 'locality',
        )
        .map((item: Suggestion) => ({
          title: item.name,
          subtitle: item.place_formatted || item.full_address || '',
          type: 'attraction',
          lat: null,
          lon: null,
          searchbox_id: item.mapbox_id,
        }))
    }
  } catch (e) {
    console.error('Nearby POIs Fetch Error:', e)
  }
}

const searchLocations = async () => {
  if (searchQuery.value.length < 2) return
  ensureSessionToken()
  loading.value = true

  try {
    let proximityParam = ''
    // Preferujemy lokalizację z GPS, ale jeśli jej brak, jako domyślną możemy
    // ustawić środek Polski, by wyniki były bardziej sensowne niż z całego świata
    const lat = currentCity.value?.lat || '52.06'
    const lon = currentCity.value?.lon || '19.25'
    proximityParam = `&proximity=${lon},${lat}`

    const query = encodeURIComponent(searchQuery.value)

    // ZAPYTANIE 1: Główne lokalizacje (miasta, dzielnice)
    const placesUrl =
      `https://api.mapbox.com/search/searchbox/v1/suggest` +
      `?q=${query}&access_token=${MAPBOX_TOKEN}&language=pl&limit=4` +
      `&types=place,locality,neighborhood,district` +
      proximityParam +
      `&session_token=${sessionToken.value}`

    // ZAPYTANIE 2: Atrakcje (POI)
    const categories =
      'tourist_attraction,monument,museum,park,beach,landmark,historic,place_of_worship,stadium,sports'
    const poiUrl =
      `https://api.mapbox.com/search/searchbox/v1/suggest` +
      `?q=${query}&access_token=${MAPBOX_TOKEN}&language=pl&limit=6` +
      `&types=poi&poi_category=${categories}` +
      proximityParam +
      `&session_token=${sessionToken.value}`

    const [placesRes, poiRes] = await Promise.all([fetch(placesUrl), fetch(poiUrl)])

    const placesData = await placesRes.json()
    const poiData = await poiRes.json()

    const placesSuggestions = placesData.suggestions || []
    const poiSuggestions = poiData.suggestions || []

    // --- LOGIKA SORTOWANIA I ODSKIEWANIA ---
    let combinedSuggestions: Suggestion[] = []

    // Sprawdzamy, czy użytkownik wpisał dokładną nazwę jakiegoś POI
    // (np. "Stadion Narodowy" i w wynikach POI mamy "Stadion Narodowy")
    const searchLower = searchQuery.value.toLowerCase().trim()

    // Szukamy, czy w wynikach POI jest miejsce, którego nazwa idealnie pasuje do zapytania
    const exactPoiMatch = poiSuggestions.some((poi: Suggestion) =>
      poi.name.toLowerCase().includes(searchLower),
    )

    // Jeśli znaleźliśmy dokładne POI, to jest bardzo duże prawdopodobieństwo,
    // że użytkownik nie szukał miasta. Dajemy POI na samą górę.
    if (exactPoiMatch) {
      // Ograniczamy liczbę zwracanych miejsc typu "miasto", bo są pewnie nietrafione
      // Bierzemy tylko jedno miasto, jeśli w ogóle jakieś w miarę pasuje
      const topPlace = placesSuggestions.length > 0 ? [placesSuggestions[0]] : []
      combinedSuggestions = [...poiSuggestions, ...topPlace]
    } else {
      // Jeśli nie wpisano dokładnej nazwy POI (np. wpisano "Gdańsk"),
      // to standardowo faworyzujemy miasta na górze
      combinedSuggestions = [...placesSuggestions, ...poiSuggestions]
    }

    // Opcjonalnie: Usuwamy duplikaty (czasami POI i dzielnica mogą mieć to samo Mapbox ID)
    const uniqueIds = new Set()
    const finalSuggestions = combinedSuggestions.filter((item) => {
      if (!uniqueIds.has(item.mapbox_id)) {
        uniqueIds.add(item.mapbox_id)
        return true
      }
      return false
    })

    if (finalSuggestions.length > 0) {
      suggestions.value = finalSuggestions.map((item: Suggestion) => {
        let type: PostLocation['type'] = 'place'
        if (['place', 'locality'].includes(item.feature_type)) type = 'city'
        else if (['neighborhood', 'district'].includes(item.feature_type)) type = 'district'
        else if (['poi', 'address'].includes(item.feature_type)) type = 'attraction'

        return {
          title: item.name,
          subtitle: item.full_address || item.place_formatted || '',
          type,
          lat: null,
          lon: null,
          searchbox_id: item.mapbox_id,
        }
      })
    } else {
      suggestions.value = []
    }
  } catch (e) {
    console.error('Search Box Error', e)
  } finally {
    loading.value = false
  }
}

const retrieveLocationCoordinates = async (item: PostLocation) => {
  if (!item.searchbox_id) return item
  ensureSessionToken()

  try {
    const url =
      `https://api.mapbox.com/search/searchbox/v1/retrieve/${item.searchbox_id}` +
      `?access_token=${MAPBOX_TOKEN}` +
      `&session_token=${sessionToken.value}`

    const res = await fetch(url)
    const data = await res.json()

    if (data.features && data.features.length > 0) {
      const feature = data.features[0]
      const [longitude, latitude] = feature.geometry.coordinates
      item.lat = String(latitude)
      item.lon = String(longitude)
      sessionToken.value = null // Resetujemy token po pobraniu detali (zgodnie z dokumentacją Mapbox)
    }
    return item
  } catch (e) {
    console.error('Retrieve Error', e)
    return item
  }
}

const handleSelect = async (loc: PostLocation) => {
  loading.value = true
  selectedLocation.value = loc
  let locationToSet = loc

  // Jeśli wybrana lokalizacja nie ma jeszcze współrzędnych (bo pochodzi z suggest API), dociągamy je
  if (!loc.lat || !loc.lon) {
    locationToSet = await retrieveLocationCoordinates(loc)
  }

  createPostStore.postData.location = locationToSet
  loading.value = false
  createPostStore.navigateBack()
}

const onInput = () => {
  clearTimeout(debounceTimeout)
  if (!searchQuery.value) {
    suggestions.value = []
    return
  }
  debounceTimeout = setTimeout(searchLocations, 400)
}

// --- GPS INIT ---
const initLocation = () => {
  if (!navigator.geolocation) return

  const geoOptions = {
    enableHighAccuracy: false,
    timeout: 8000,
    maximumAge: 3600000,
  }

  navigator.geolocation.getCurrentPosition(
    async (pos) => {
      try {
        const { latitude, longitude } = pos.coords

        const url =
          `https://api.mapbox.com/search/searchbox/v1/reverse` +
          `?longitude=${longitude}` +
          `&latitude=${latitude}` +
          `&access_token=${MAPBOX_TOKEN}` +
          `&types=locality,place,address` +
          `&language=pl`

        const res = await fetch(url)
        const data = await res.json()

        if (data.features && data.features.length > 0) {
          const feature = data.features[0]
          const props = feature.properties

          const cityName = props.name || props.place_formatted || props.full_address

          currentCity.value = {
            title: cityName,
            subtitle: 'Twoja obecna lokalizacja',
            type: 'current',
            lat: latitude.toString(),
            lon: longitude.toString(),
          }
          selectedLocation.value = currentCity.value

          // Przekazujemy TYLKO współrzędne, bez nazwy adresu
          await fetchNearbyPOIs(latitude, longitude)
        }
      } catch (e) {
        console.error('Mapbox Reverse Geocoding Error', e)
      }
    },
    (err) => console.warn(`Geolocation error: ${err.message}`),
    geoOptions,
  )
}

onMounted(initLocation)
</script>

<template>
  <div class="h-[700px] w-full mx-auto flex flex-col overflow-hidden bg-white">
    <div class="px-4 py-3">
      <div
        class="relative flex items-center bg-gray-100 rounded-lg px-3 py-1 focus-within:bg-white focus-within:ring-2 focus-within:ring-[#1877f2] transition-all"
      >
        <magnify-icon :size="20" class="text-gray-500" />
        <input
          v-model="searchQuery"
          @input="onInput"
          type="text"
          placeholder="Gdzie jesteś?"
          class="w-full bg-transparent border-none focus:ring-0 py-2 px-2 text-[15px] outline-none"
        />
        <button
          v-if="searchQuery"
          @click="
            searchQuery = '',
            suggestions = []
          "
          class="text-gray-400"
        >
          <close-icon :size="18" class="bg-gray-300 rounded-full p-0.5 text-white" />
        </button>
      </div>
    </div>

    <div class="flex-1 overflow-y-auto px-2 pb-24 custom-scrollbar">
      <div v-if="currentCity && !searchQuery" class="mb-4">
        <p class="text-[11px] font-bold text-gray-500 uppercase tracking-wider mb-2 px-4">
          Twoja lokalizacja
        </p>
        <div
          @click="handleSelect(currentCity)"
          class="flex items-center gap-4 p-3 hover:bg-gray-100 cursor-pointer rounded-xl transition mx-2"
          :class="{ 'bg-blue-50': selectedLocation?.title === currentCity.title }"
        >
          <div
            class="w-10 h-10 rounded-full bg-blue-100 text-[#1877f2] flex items-center justify-center"
          >
            <navigation-icon :size="22" />
          </div>
          <div class="flex-1">
            <p class="font-bold text-[15px] text-gray-900 leading-tight">
              Użyj obecnej lokalizacji
            </p>
            <p class="text-[13px] text-gray-500">{{ currentCity.title }}</p>
          </div>
        </div>
      </div>

      <div v-if="nearbyPlaces.length > 0 && !searchQuery" class="mb-4">
        <p class="text-[11px] font-bold text-gray-500 uppercase tracking-wider mb-2 px-4 pt-2">
          Ciekawe miejsca w pobliżu
        </p>
        <ul class="space-y-1 mx-2">
          <li
            v-for="(loc, index) in nearbyPlaces"
            :key="'nearby-' + index"
            @click="handleSelect(loc)"
            class="flex items-center gap-4 p-3 hover:bg-gray-100 cursor-pointer rounded-xl transition"
          >
            <div
              class="w-10 h-10 rounded-full bg-orange-50 text-orange-600 flex items-center justify-center shrink-0"
            >
              <map-marker-outline-icon :size="22" />
            </div>
            <div class="flex-1 overflow-hidden">
              <p class="font-bold text-[15px] text-gray-900 truncate leading-tight">
                {{ loc.title }}
              </p>
              <p class="text-[13px] text-gray-500 truncate mt-0.5">{{ loc.subtitle }}</p>
            </div>
          </li>
        </ul>
      </div>

      <div v-if="suggestions.length > 0">
        <p class="text-[11px] font-bold text-gray-500 uppercase tracking-wider mb-2 px-4 pt-2">
          Propozycje
        </p>
        <ul class="space-y-1 mx-2">
          <li
            v-for="(loc, index) in suggestions"
            :key="'suggest-' + index"
            @click="handleSelect(loc)"
            class="flex items-center gap-4 p-3 hover:bg-gray-100 cursor-pointer rounded-xl transition"
            :class="{ 'bg-blue-50': selectedLocation?.title === loc.title }"
          >
            <div
              class="w-10 h-10 rounded-full bg-gray-100 text-gray-600 flex items-center justify-center shrink-0"
            >
              <map-marker-icon v-if="loc.type === 'city'" :size="22" />
              <office-building-icon v-else-if="loc.type === 'district'" :size="22" />
              <tree-icon v-else-if="loc.type === 'park'" :size="22" />
              <map-marker-outline-icon v-else :size="22" />
            </div>

            <div class="flex-1 overflow-hidden text-left">
              <p class="font-bold text-[15px] text-gray-900 truncate leading-tight">
                {{ loc.title }}
              </p>
              <p class="text-[13px] text-gray-500 truncate mt-0.5">{{ loc.subtitle }}</p>
            </div>
          </li>
        </ul>
      </div>

      <div v-if="loading" class="py-10 flex justify-center">
        <div
          class="w-6 h-6 border-2 border-gray-200 border-t-[#1877f2] rounded-full animate-spin"
        ></div>
      </div>
    </div>

    <div
      class="p-4 bg-white border-t border-gray-100 sticky bottom-0 w-full shadow-[0_-4px_10px_rgba(0,0,0,0.03)]"
    >
      <button
        :disabled="!selectedLocation || loading"
        class="w-full py-3 rounded-lg font-bold text-white transition-all text-[16px] bg-[#1877f2] disabled:bg-gray-200 disabled:text-gray-400"
      >
        <span v-if="loading">Pobieranie danych...</span>
        <span v-else>Opublikuj z tą lokalizacją</span>
      </button>
    </div>
  </div>
</template>

<style scoped>
.custom-scrollbar::-webkit-scrollbar {
  width: 4px;
}
.custom-scrollbar::-webkit-scrollbar-thumb {
  background: #e5e7eb;
  border-radius: 10px;
}
</style>
