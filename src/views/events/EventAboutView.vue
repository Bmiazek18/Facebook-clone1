<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed, defineProps } from 'vue'
import 'leaflet/dist/leaflet.css'
import L from 'leaflet'

import iconRetinaUrl from 'leaflet/dist/images/marker-icon-2x.png'
import iconUrl from 'leaflet/dist/images/marker-icon.png'
import shadowUrl from 'leaflet/dist/images/marker-shadow.png'

L.Icon.Default.mergeOptions({
  iconRetinaUrl,
  iconUrl,
  shadowUrl,
})

import InformationIcon from 'vue-material-design-icons/Information.vue'
import StarIcon from 'vue-material-design-icons/Star.vue'
import ShareVariantIcon from 'vue-material-design-icons/ShareVariant.vue'
import EventAboutDetails from '@/components/events/EventAboutDetails.vue';

import type { Event as EventType } from '@/data/events'
import { useStickySidebar } from '@/composables/useStickySidebar'

const props = defineProps<{
  eventDetails: EventType | undefined
}>()

// --- MAP CONFIG ---
const mapContainerRef = ref<HTMLDivElement | null>(null)
let mapInstance: L.Map | null = null
const mapCenter: L.LatLngTuple = [54.371661, 18.619082] // Default to Gdańsk

const redIcon = new L.Icon({
  iconUrl: 'https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/markers/marker-icon-2x-red.png',
  shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/0.7.7/images/marker-shadow.png',
  iconSize: [25, 41],
  iconAnchor: [12, 41],
  popupAnchor: [1, -34],
  shadowSize: [41, 41],
})

// --- STICKY SIDEBAR LOGIC ---
const rightSectionRef = ref<HTMLDivElement | null>(null)
const { stickyTop } = useStickySidebar(rightSectionRef, 56, 16)

onMounted(() => {
  if (mapContainerRef.value) {
    const eventCoords = props.eventDetails?.coordinates || mapCenter
    mapInstance = L.map(mapContainerRef.value, {
      center: eventCoords,
      zoom: 15,
      zoomControl: false,
      scrollWheelZoom: false,
      doubleClickZoom: false,
      touchZoom: false,
      boxZoom: false,
      keyboard: false,
      dragging: false,
    })

    L.tileLayer('https://{s}.basemaps.cartocdn.com/rastertiles/voyager/{z}/{x}/{y}{r}.png', {
      attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors &copy; <a href="https://carto.com/attributions">CARTO</a>',
      maxZoom: 20,
    }).addTo(mapInstance)

    const markerCoords = props.eventDetails?.coordinates || mapCenter
    const markerName = props.eventDetails?.locationName || 'Event Location'
    L.marker(markerCoords, { icon: redIcon })
      .addTo(mapInstance)
      .bindTooltip(markerName, { direction: 'top', offset: [0, -40] })

    L.control.zoom({ position: 'bottomright' }).addTo(mapInstance)
  }
})

onUnmounted(() => {
  if (mapInstance) {
    mapInstance.remove()
    mapInstance = null
  }
})

// --- Hardcoded Data for UI ---
const friends = [
  { name: 'Mikołaj Niedziela', avatar: 'https://i.pravatar.cc/150?u=1' },
  { name: 'Wojtek Piotrowski', avatar: 'https://i.pravatar.cc/150?u=2' },
  { name: 'Magda Chłopecka', avatar: 'https://i.pravatar.cc/150?u=3' },
]

const organizers = [
  { name: 'Technikalia', type: 'Wydarzenie', role: '92 minionych wydarzeń', logo: 'https://placehold.co/100x100/1e293b/FFF?text=T.26' },
  { name: 'Politechnika Gdańska', type: 'Szkoła wyższa', role: '361 minionych wydarzeń', logo: 'https://placehold.co/100x100/white/000?text=PG' },
]
</script>

<template>
  <div class="grid grid-cols-1 max-w-[1200px] mx-auto lg:grid-cols-5 gap-4 mt-4">
    <div class="lg:col-span-3 space-y-4">
      <EventAboutDetails :event-details="props.eventDetails" />

      <div class="bg-theme-bg-secondary rounded-lg shadow-sm p-4">
        <div class="flex justify-between items-center mb-4">
          <h2 class="text-xl font-bold">Poznaj organizatorów</h2>
        </div>
        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div v-for="org in organizers" :key="org.name" class="border border-theme-border rounded-lg p-4 flex flex-col items-center text-center">
            <img :src="org.logo" class="w-20 h-20 rounded-full mb-3 object-cover border border-theme-bg-subtle" />
            <h3 class="font-bold text-lg">{{ org.name }}</h3>
            <p class="text-xs text-theme-text-secondary mt-1">{{ org.role }} · {{ org.type }}</p>
            <div class="mt-4 w-full pt-4 border-t border-theme-bg-subtle">
              <button class="w-full bg-theme-bg-subtle hover:bg-theme-hover text-theme-text py-2 rounded font-semibold text-sm flex items-center justify-center gap-2 transition">
                <InformationIcon :size="18" /> Dowiedz się więcej
              </button>
            </div>
          </div>
        </div>
      </div>
      <div class="h-[2000px]"></div>
    </div>

    <div
      ref="rightSectionRef"
      class="lg:col-span-2 space-y-4 sticky z-10 self-start"
      :style="{ top: `${stickyTop}px` }"
    >
      <div class="bg-theme-bg-secondary rounded-lg shadow-sm ">
        <div class="w-full h-[300px] rounded-lg overflow-hidden border border-theme-border relative isolate z-0">
          <div ref="mapContainerRef" class="w-full h-full bg-theme-bg-subtle"></div>
        </div>
        <div class="mt-3 p-4">
          <div class="font-semibold text-theme-text">{{ eventDetails?.locationName || eventDetails?.location || 'Brak lokalizacji' }}</div>
          <div class="text-sm text-theme-text-secondary">{{ eventDetails?.address || 'Brak adresu' }}</div>
        </div>
      </div>

      <div class="bg-theme-bg-secondary rounded-lg shadow-sm p-4">
        <h3 class="text-lg font-bold mb-4">Goście</h3>
        <div class="flex justify-around text-center mb-4">
          <div>
            <div class="text-xl font-bold text-theme-text">{{ eventDetails?.guestsGoing || 0 }}</div>
            <div class="text-xs text-theme-text-secondary">Wezmę udział</div>
          </div>
          <div>
            <div class="text-xl font-bold text-theme-text">{{ eventDetails?.guestsInterested || 0 }}</div>
            <div class="text-xs text-theme-text-secondary">Zainteresowani</div>
          </div>
        </div>
        <hr class="border-theme-bg-subtle my-4" />
        <h4 class="text-sm font-semibold mb-3">Wybierz się ze znajomymi</h4>
        <ul class="space-y-3">
          <li v-for="friend in friends" :key="friend.name" class="flex items-center justify-between">
            <div class="flex items-center gap-3">
              <img :src="friend.avatar" class="w-9 h-9 rounded-full bg-theme-bg-subtle" />
              <span class="text-sm font-medium text-theme-text">{{ friend.name }}</span>
            </div>
            <button class="bg-theme-bg-subtle hover:bg-theme-hover text-theme-text px-3 py-1.5 rounded text-sm font-semibold transition">
              Zaproś
            </button>
          </li>
        </ul>
      </div>

      <div class="bg-theme-bg-secondary rounded-lg shadow-sm p-4">
        <h3 class="text-lg font-bold mb-4">Popularne wśród znajomych</h3>
        <div class="flex gap-3">
          <div class="w-16 h-16 bg-theme-bg-subtle rounded-lg shrink-0 overflow-hidden">
            <img src="https://placehold.co/100x100/orange/white?text=K" class="w-full h-full object-cover" />
          </div>
          <div>
            <div class="text-red-600 text-xs font-bold uppercase">Śr, 7 sty o 18:00</div>
            <div class="font-bold text-sm leading-tight mt-0.5">LUBLIN Warsztaty "Kuchenne duety"</div>
            <div class="text-xs text-theme-text-secondary mt-1">Restauracja Giuseppe</div>
            <div class="text-xs text-theme-text-secondary mt-1 flex items-center gap-1">
              <img src="https://i.pravatar.cc/150?u=5" class="w-4 h-4 rounded-full" />
              Wioleta jest zainteresowana
            </div>
          </div>
        </div>
        <div class="mt-3 flex gap-2">
          <button class="flex-1 bg-theme-bg-subtle hover:bg-theme-hover text-theme-text py-1.5 rounded text-sm font-semibold flex items-center justify-center gap-1 transition">
            <StarIcon :size="16" /> Zainteresowany(a)
          </button>
          <button class="bg-theme-bg-subtle hover:bg-theme-hover text-theme-text px-3 py-1.5 rounded transition">
            <ShareVariantIcon :size="16" />
          </button>
        </div>
      </div>
    </div>
  </div>
</template>
