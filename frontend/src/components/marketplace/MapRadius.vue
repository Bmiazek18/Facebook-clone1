<script setup lang="ts">
import { onMounted, onUnmounted, ref } from 'vue'
import L from 'leaflet'
import 'leaflet/dist/leaflet.css'

const emit = defineEmits<{
  'update:radius': [radius: number]
  apply: [radius: number, lat: number, lon: number, cityName: string]
  close: []
}>()

// --- STAN KOMPONENTU ---
const currentCenter = ref<[number, number]>([52.0593, 19.2003])
const selectedCityName = ref<string>('Łęczyca')
const searchQuery = ref<string>('Łęczyca')

const radiusOptions: number[] = [1, 2, 5, 10, 20, 40, 60, 80, 100, 250, 500]
const radiusKm = ref<number>(500)

// Flagi UI
const isRadiusDropdownOpen = ref<boolean>(false)
const showSuggestions = ref<boolean>(false)
const isSearching = ref<boolean>(false)

// Ref-y do elementów DOM
const radiusDropdownRef = ref<HTMLElement | null>(null)
const searchContainerRef = ref<HTMLElement | null>(null)
const mapContainer = ref<HTMLElement | null>(null)

// Wyszukiwanie
interface SearchResult {
  name: string
  fullName: string
  type: string
  lat: number
  lon: number
}
const searchResults = ref<SearchResult[]>([])
let debounceTimer: ReturnType<typeof setTimeout> | null = null

// Mapa
let map: L.Map | null = null
let circle: L.Circle | null = null
let marker: L.Marker | null = null

// --- CYKL ŻYCIA ---
onMounted(() => {
  if (!mapContainer.value) return

  map = L.map(mapContainer.value, {
    zoomControl: false,
    attributionControl: false,
    dragging: true,
    touchZoom: true,
    scrollWheelZoom: true,
    doubleClickZoom: true,
  }).setView(currentCenter.value, 6)

  L.tileLayer('https://{s}.basemaps.cartocdn.com/rastertiles/voyager/{z}/{x}/{y}{r}.png', {
    maxZoom: 19,
  }).addTo(map)

  const redIcon = L.divIcon({
    className: 'custom-pin',
    html: `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="#ef4444" stroke="#ffffff" stroke-width="1.5" class="w-10 h-10 drop-shadow-md"><path d="M12 2C8.13 2 5 5.13 5 9c0 5.25 7 13 7 13s7-7.75 7-13c0-3.87-3.13-7-7-7zm0 9.5c-1.38 0-2.5-1.12-2.5-2.5s1.12-2.5 2.5-2.5 2.5 1.12 2.5 2.5-1.12 2.5-2.5 2.5z"/></svg>`,
    iconSize: [40, 40],
    iconAnchor: [20, 40],
  })

  marker = L.marker(currentCenter.value, { icon: redIcon }).addTo(map)
  drawCircle()

  // Zabezpieczenie przed "szarą mapą" w modalu
  setTimeout(() => {
    map?.invalidateSize()
  }, 150)

  document.addEventListener('click', handleClickOutside)
})

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)
  if (map) map.remove()
})

// --- LOGIKA MAPY ---
const drawCircle = () => {
  if (!map) return
  const radiusMeters = radiusKm.value * 1000

  if (circle) {
    circle.setLatLng(currentCenter.value)
    circle.setRadius(radiusMeters)
  } else {
    circle = L.circle(currentCenter.value, {
      color: '#64748b',
      weight: 1,
      fillColor: '#3b82f6',
      fillOpacity: 0.1,
      radius: radiusMeters,
    }).addTo(map)
  }

  if (circle && radiusKm.value > 0) {
    map.fitBounds(circle.getBounds(), { padding: [30, 30] })
  }
}

// --- LOGIKA WYSZUKIWANIA (MIASTA I GMINY, BEZ WOJEWÓDZTW/POWIATÓW) ---
const handleInput = () => {
  showSuggestions.value = true
  isRadiusDropdownOpen.value = false

  if (debounceTimer) clearTimeout(debounceTimer)

  if (searchQuery.value.trim().length < 2) {
    searchResults.value = []
    isSearching.value = false
    return
  }

  isSearching.value = true
  debounceTimer = setTimeout(async () => {
    try {
      // Zwiększony limit wyników z API do 20, aby mieć z czego odrzucać śmieci
      const res = await fetch(`https://nominatim.openstreetmap.org/search?q=${encodeURIComponent(searchQuery.value)}&format=json&addressdetails=1&limit=20&accept-language=pl`)
      const data = await res.json()

      const filteredData = data.filter((item: any) => {
        // Akceptujemy małe miejscowości (place) oraz duże miasta/gminy (administrative boundary)
        const isPlace = item.class === 'place' && ['city', 'town', 'village'].includes(item.type)
        const isBoundary = item.class === 'boundary' && item.type === 'administrative'

        if (!isPlace && !isBoundary) return false

        // Sprawdzamy główną, pierwszą nazwę znalezionego obiektu
        const primaryName = (item.name || item.display_name.split(',')[0]).toLowerCase()

        // Odrzucamy wyniki, jeśli główny obiekt to wprost województwo lub powiat
        if (primaryName.includes('województwo') || primaryName.includes('powiat')) {
          return false
        }

        return true
      })

      // Zabezpieczenie przed duplikatami (żeby np. Warszawa nie pojawiła się 2 razy)
      const uniqueNames = new Set()
      const uniqueResults: SearchResult[] = []

      for (const item of filteredData) {
        const cityName = item.name || item.display_name.split(',')[0]

        if (!uniqueNames.has(cityName)) {
          uniqueNames.add(cityName)
          uniqueResults.push({
            name: cityName,
            fullName: item.display_name,
            lat: parseFloat(item.lat),
            lon: parseFloat(item.lon),
            type: 'Miasto' // Dodajemy dopisek "Miasto" tak jak na Facebooku
          })
        }
      }

      // Wyświetlamy max 5 czystych wyników
      searchResults.value = uniqueResults.slice(0, 5)

    } catch (error) {
      console.error("Błąd pobierania danych z Nominatim API", error)
    } finally {
      isSearching.value = false
    }
  }, 350)
}

const selectLocation = (location: SearchResult) => {
  searchQuery.value = location.name
  selectedCityName.value = location.name
  currentCenter.value = [location.lat, location.lon]
  showSuggestions.value = false

  if (map && marker) {
    map.setView(currentCenter.value, 10)
    marker.setLatLng(currentCenter.value)
    drawCircle()
  }
}

// --- DROPDOWNY I OBSŁUGA ZDARZEŃ ---
const toggleRadiusDropdown = () => {
  isRadiusDropdownOpen.value = !isRadiusDropdownOpen.value
  showSuggestions.value = false
}

const selectRadius = (option: number) => {
  radiusKm.value = option
  isRadiusDropdownOpen.value = false
  drawCircle()
  emit('update:radius', option)
}

const handleClickOutside = (event: MouseEvent) => {
  const target = event.target as Node
  if (radiusDropdownRef.value && !radiusDropdownRef.value.contains(target)) {
    isRadiusDropdownOpen.value = false
  }
  if (searchContainerRef.value && !searchContainerRef.value.contains(target)) {
    showSuggestions.value = false
  }
}
</script>

<template>


    <div class="bg-white w-full max-w-[600px] rounded-2xl shadow-2xl flex flex-col relative overflow-hidden">


      <!-- Treść Modala -->
      <div class="p-4 space-y-4 bg-white z-0 relative flex-1">
        <p class="text-[14px] text-gray-600">{{ $t('marketplace.wyszukajNaPodstawieMiejscowosci') }}</p>

        <!-- Sekcja: Wyszukiwarka Lokalizacji -->
        <div class="relative z-50" ref="searchContainerRef">
          <div
            class="flex items-center border rounded-xl px-3 py-2.5 bg-white transition-shadow duration-200"
            :class="showSuggestions ? 'border-[#0866FF] shadow-[0_0_0_1px_#0866FF]' : 'border-gray-300 hover:border-gray-400'"
          >
            <!-- Ikona Pina -->
            <div class="px-2 text-gray-600 shrink-0">
              <svg xmlns="http://www.w3.org/2000/svg" class="h-[22px] w-[22px]" fill="currentColor" viewBox="0 0 24 24">
                <path d="M12 2C8.13 2 5 5.13 5 9c0 5.25 7 13 7 13s7-7.75 7-13c0-3.87-3.13-7-7-7zm0 9.5c-1.38 0-2.5-1.12-2.5-2.5s1.12-2.5 2.5-2.5 2.5 1.12 2.5 2.5-1.12 2.5-2.5 2.5z"/>
              </svg>
            </div>

            <div class="flex flex-col flex-1 pl-1">
              <span class="text-[12px] font-medium leading-none mb-1" :class="showSuggestions ? 'text-[#0866FF]' : 'text-gray-500'">{{ $t('post.location') }}</span>
              <input
                v-model="searchQuery"
                @input="handleInput"
                @focus="handleInput"
                type="text"
                class="w-full text-gray-900 bg-transparent border-none p-0 focus:ring-0 focus:outline-none text-[16px] leading-tight placeholder-gray-400"
                :placeholder="$t('marketplace.wpiszNazweMiejscowosci')"
              />
            </div>

            <!-- Loader -->
            <div v-if="isSearching" class="pr-2 shrink-0">
              <svg class="animate-spin h-5 w-5 text-[#0866FF]" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
              </svg>
            </div>
          </div>

          <!-- Wyniki wyszukiwania -->
          <div
            v-if="showSuggestions && searchResults.length > 0"
            class="absolute top-full left-0 w-full mt-1 bg-white rounded-xl shadow-lg border border-gray-100 overflow-hidden z-50 max-h-64 overflow-y-auto"
          >
            <ul class="py-1">
              <li
                v-for="(result, index) in searchResults"
                :key="index"
                @click="selectLocation(result)"
                class="px-4 py-2 hover:bg-gray-100 cursor-pointer flex flex-col"
              >
                <span class="text-[16px] font-medium text-gray-900 leading-tight">{{ result.name }}</span>
                <span class="text-[14px] text-gray-500 mt-0.5">{{ result.type }}</span>
              </li>
            </ul>
          </div>
        </div>

        <!-- Sekcja: Promień -->
        <div ref="radiusDropdownRef" class="relative z-40">
          <div
            @click="toggleRadiusDropdown"
            class="flex items-center border rounded-xl px-4 py-2.5 bg-white cursor-pointer transition-shadow duration-200"
            :class="isRadiusDropdownOpen ? 'border-[#0866FF] shadow-[0_0_0_1px_#0866FF]' : 'border-gray-300 hover:border-gray-400'"
          >
            <div class="flex flex-col flex-1">
              <span class="text-[12px] font-medium leading-none mb-1" :class="isRadiusDropdownOpen ? 'text-[#0866FF]' : 'text-gray-500'">{{ $t('marketplace.promien') }}</span>
              <span class="text-[16px] text-gray-900 leading-tight">{{ $t('marketplace.radiuskmKm') }}</span>
            </div>

            <div class="text-gray-800 transition-transform duration-200" :class="{ 'rotate-180': isRadiusDropdownOpen }">
              <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="currentColor" viewBox="0 0 24 24">
                <path d="M7 10l5 5 5-5H7z" />
              </svg>
            </div>
          </div>

          <!-- Wyniki promienia -->
          <div
            v-if="isRadiusDropdownOpen"
            class="absolute top-full left-0 w-full mt-1 bg-white rounded-xl shadow-lg border border-gray-100 z-50 overflow-hidden max-h-60 overflow-y-auto"
          >
            <ul class="py-1">
              <li
                v-for="option in radiusOptions"
                :key="option"
                @click="selectRadius(option)"
                class="px-4 py-2.5 flex justify-between items-center hover:bg-gray-100 cursor-pointer text-gray-900"
              >
                <span class="text-[16px] font-medium">{{ $t('marketplace.optionKm') }}</span>
                <span v-if="radiusKm === option" class="text-[#0866FF]">
                  <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" viewBox="0 0 20 20" fill="currentColor">
                    <path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd" />
                  </svg>
                </span>
              </li>
            </ul>
          </div>
        </div>

        <!-- Sekcja: Mapa -->
        <div class="relative mt-2 z-10 rounded-xl overflow-hidden shadow-sm bg-gray-100 border border-gray-200">
          <div ref="mapContainer" class="w-full h-[320px]"></div>

          <!-- Przycisk lokalizatora (ikona nawigacji z rogu mapy) -->
          <div class="absolute top-3 right-3 z-[400] bg-white rounded-lg shadow-md p-2.5 cursor-pointer hover:bg-gray-50 border border-gray-200">
             <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 text-black" viewBox="0 0 24 24" fill="currentColor">
               <path d="M21 3L3 10.53v.98l6.84 2.65L12.48 21h.98L21 3z"/>
             </svg>
          </div>
        </div>

      </div>

      <!-- Stopka z przyciskiem Zastosuj -->
      <div class="px-4 py-3 border-t border-gray-200 bg-white flex justify-end z-10 relative">
        <button
          @click="emit('apply', radiusKm, currentCenter[0], currentCenter[1], selectedCityName)"
          class="bg-[#0866FF] hover:bg-[#075ce5] text-white font-semibold py-2 px-8 rounded-lg transition-colors text-[15px]"
        >{{ $t('marketplace.zastosuj') }}</button>
      </div>

    </div>

</template>

<style scoped>
/* Prawidłowy wygląd czerwonego markera Leaflet */
:deep(.leaflet-marker-icon.custom-pin) {
  background: transparent !important;
  border: none !important;
}

/* Dostosowanie suwaka, aby nie był uciążliwy dla estetyki list */
::-webkit-scrollbar {
  width: 6px;
}
::-webkit-scrollbar-track {
  background: transparent;
}
::-webkit-scrollbar-thumb {
  background: #cbd5e1;
  border-radius: 10px;
}
</style>
