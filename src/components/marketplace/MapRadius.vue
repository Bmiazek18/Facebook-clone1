<script setup lang="ts">
import { onMounted, onUnmounted, ref } from 'vue'
import L from 'leaflet'
import 'leaflet/dist/leaflet.css'

// Emit
const emit = defineEmits<{
  'update:radius': [radius: number]
  apply: [radius: number, lat: number, lon: number, cityName: string]
}>()

const cities = [
  { name: 'Łęczyca', coords: [52.0593, 19.2003] as [number, number], text: 'Łęczyca (gmina)' },
  { name: 'Warszawa', coords: [52.2297, 21.0122] as [number, number], text: 'Warszawa' },
  { name: 'Szczecin', coords: [53.4285, 14.5528] as [number, number], text: 'Szczecin' }
]

const selectedCityIndex = ref(0)
const currentCenter = ref<[number, number]>([52.0593, 19.2003])

const radiusOptions: number[] = [1, 2, 5, 10, 20, 40, 60, 80, 100, 250, 500]
const radiusKm = ref<number>(20) // Domyślnie 20 km

const isDropdownOpen = ref<boolean>(false)
const dropdownRef = ref<HTMLElement | null>(null)
const mapContainer = ref<HTMLElement | null>(null)

let map: L.Map | null = null
let circle: L.Circle | null = null
let marker: L.Marker | null = null

onMounted(() => {
  if (!mapContainer.value) return

  map = L.map(mapContainer.value, {
    zoomControl: false,
    attributionControl: false,
    dragging: false,
    touchZoom: false,
    scrollWheelZoom: false,
    doubleClickZoom: false,
    boxZoom: false,
    keyboard: false,
  }).setView(currentCenter.value, 10)

  L.tileLayer('https://{s}.basemaps.cartocdn.com/light_all/{z}/{x}/{y}{r}.png', {
    maxZoom: 19,
  }).addTo(map)

  // Czerwony Marker
  const redIcon = L.divIcon({
    className: 'custom-pin',
    html: `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="#ef4444" class="w-10 h-10 drop-shadow-md"><path d="M12 2C8.13 2 5 5.13 5 9c0 5.25 7 13 7 13s7-7.75 7-13c0-3.87-3.13-7-7-7zm0 9.5c-1.38 0-2.5-1.12-2.5-2.5s1.12-2.5 2.5-2.5 2.5 1.12 2.5 2.5-1.12 2.5-2.5 2.5z"/></svg>`,
    iconSize: [40, 40],
    iconAnchor: [20, 40],
  })

  marker = L.marker(currentCenter.value, { icon: redIcon }).addTo(map)

  drawCircle()

  // Nasłuchiwanie kliknięć w całe okno, aby zamknąć dropdown
  document.addEventListener('click', handleClickOutside)
})

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)
})

const changeCity = () => {
  const city = cities[selectedCityIndex.value]
  if (!city) return
  currentCenter.value = [...city.coords]
  if (map) {
    map.setView(currentCenter.value, 10)
    if (marker) {
      marker.setLatLng(currentCenter.value)
    }
    drawCircle()
  }
}

const drawCircle = () => {
  if (!map) return

  const radiusMeters = radiusKm.value * 1000

  if (circle) {
    circle.setLatLng(currentCenter.value)
    circle.setRadius(radiusMeters)
  } else {
    circle = L.circle(currentCenter.value, {
      color: '#3b82f6',
      fillColor: '#3b82f6',
      fillOpacity: 0.1,
      weight: 1,
      radius: radiusMeters,
    }).addTo(map)
  }

  if (circle) {
    map.fitBounds(circle.getBounds())
  }
}

// --- Logika Dropdownu ---
const toggleDropdown = () => {
  isDropdownOpen.value = !isDropdownOpen.value
}

const selectRadius = (value: number) => {
  radiusKm.value = value
  isDropdownOpen.value = false
  drawCircle()
  emit('update:radius', value)
}

const handleClickOutside = (event: MouseEvent) => {
  if (dropdownRef.value && !dropdownRef.value.contains(event.target as Node)) {
    isDropdownOpen.value = false
  }
}

const apply = () => {
  const city = cities[selectedCityIndex.value]
  if (!city) return
  emit('apply', radiusKm.value, currentCenter.value[0], currentCenter.value[1], city.name)
  console.log('Wybrano promień:', radiusKm.value, 'Miasto:', city.name, 'Coords:', currentCenter.value)
}
</script>

<template>
  <div class="w-full mx-auto bg-white   text-gray-800 min-h-[600px]">
    <div class="space-y-4 p-4 relative">
      <div class="flex flex-col border border-gray-300 rounded-xl px-4 py-2.5 shadow-sm bg-white gap-1">
        <div class="flex items-center">
          <div class="mr-4 text-gray-500">
            <svg
              xmlns="http://www.w3.org/2000/svg"
              class="h-6 w-6"
              viewBox="0 0 20 20"
              fill="currentColor"
            >
              <path
                fill-rule="evenodd"
                d="M5.05 4.05a7 7 0 119.9 9.9L10 18.9l-4.95-4.95a7 7 0 010-9.9zM10 11a2 2 0 100-4 2 2 0 000 4z"
                clip-rule="evenodd"
              />
            </svg>
          </div>
          <div class="flex flex-col flex-1">
            <span class="text-xs text-gray-500 font-normal">Lokalizacja (Miasto)</span>
            <select
              v-model="selectedCityIndex"
              @change="changeCity"
              class="w-full font-medium text-gray-900 bg-transparent border-none p-0 focus:ring-0 focus:outline-none text-[15px]"
            >
              <option v-for="(city, idx) in cities" :key="idx" :value="idx">
                {{ city.text }}
              </option>
            </select>
          </div>
        </div>
      </div>

      <div ref="dropdownRef" class="relative">
        <div
          @click="toggleDropdown"
          class="flex justify-between items-center border rounded-xl px-4 py-2.5 shadow-sm bg-white cursor-pointer transition-colors"
          :class="
            isDropdownOpen
              ? 'border-blue-600 ring-1 ring-blue-600'
              : 'border-gray-300 hover:border-gray-400'
          "
        >
          <div class="flex flex-col">
            <span class="text-xs text-blue-600 font-medium" v-if="isDropdownOpen">Promień</span>
            <span class="text-xs text-gray-500 font-normal" v-else>Promień</span>

            <span class="text-base font-medium text-gray-900 leading-tight">{{ radiusKm }} km</span>
          </div>

          <div
            class="text-gray-600 transition-transform duration-200"
            :class="{ 'rotate-180': isDropdownOpen }"
          >
            <svg
              xmlns="http://www.w3.org/2000/svg"
              class="h-5 w-5"
              viewBox="0 0 20 20"
              fill="currentColor"
            >
              <path
                fill-rule="evenodd"
                d="M5.293 7.293a1 1 0 011.414 0L10 10.586l3.293-3.293a1 1 0 111.414 1.414l-4 4a1 1 0 01-1.414 0l-4-4a1 1 0 010-1.414z"
                clip-rule="evenodd"
              />
            </svg>
          </div>
        </div>

        <div
          v-if="isDropdownOpen"
          class="absolute top-full left-0 w-full mt-2 bg-white rounded-xl shadow-xl border border-gray-100 z-50 overflow-hidden max-h-80 overflow-y-auto"
        >
          <ul>
            <li
              v-for="option in radiusOptions"
              :key="option"
              @click="selectRadius(option)"
              class="px-4 py-3 flex justify-between items-center hover:bg-gray-50 cursor-pointer text-gray-900"
            >
              <span class="text-base">{{ option }} km</span>

              <span v-if="radiusKm === option" class="text-blue-600">
                <svg
                  xmlns="http://www.w3.org/2000/svg"
                  class="h-5 w-5"
                  viewBox="0 0 20 20"
                  fill="currentColor"
                >
                  <path
                    fill-rule="evenodd"
                    d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z"
                    clip-rule="evenodd"
                  />
                </svg>
              </span>
            </li>
          </ul>
        </div>
      </div>

      <div class="relative mt-4 z-0">
        <div
          ref="mapContainer"
          class="w-full h-[400px] rounded-2xl border border-gray-200 overflow-hidden"
        ></div>
      </div>
    </div>

    <div class="p-4 border-t border-gray-200 flex justify-end bg-white">
      <button
        @click="apply"
        class="bg-[#1d64f2] hover:bg-blue-700 text-white font-semibold py-2 px-6 rounded-lg shadow-sm transition-colors"
      >
        Zastosuj
      </button>
    </div>
  </div>
</template>

<style scoped>
.custom-pin {
  background: transparent;
  border: none;
}
/* Stylowanie paska przewijania dla listy (opcjonalne, dla estetyki) */
::-webkit-scrollbar {
  width: 6px;
}
::-webkit-scrollbar-track {
  background: #f1f1f1;
}
::-webkit-scrollbar-thumb {
  background: #cbd5e1;
  border-radius: 3px;
}
</style>
