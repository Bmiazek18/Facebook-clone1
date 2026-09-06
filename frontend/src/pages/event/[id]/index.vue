<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed } from 'vue'
import 'leaflet/dist/leaflet.css'
import L from 'leaflet'
import StarOutlineIcon from 'vue-material-design-icons/StarOutline.vue'
import ReplyIcon from 'vue-material-design-icons/Reply.vue' // Będzie służyć jako ikona udostępniania

// Zaktualizowani znajomi ze screena
const friends = [
  { name: 'Przemek Krasucki', avatar: 'https://i.pravatar.cc/150?u=10' },
  { name: 'Mikołaj Niedziela', avatar: 'https://i.pravatar.cc/150?u=1' },
  { name: 'Mateusz Piszcz', avatar: 'https://i.pravatar.cc/150?u=11' },
]

// Nowe dane do sekcji "Popularne wśród znajomych"
const popularEvents = [
  {
    id: 1,
    date: 'Sob, 14 mar o 09:00',
    title: 'XXIV Studencki Turniej Negocjacyjny - Eliminacje Gdańsk',
    location: 'ul. Jana Bażyńskiego 6, 80-952 Gdansk, Poland',
    friend: 'Mateusz',
    friendAvatar: 'https://i.pravatar.cc/150?u=11',
    image: 'https://placehold.co/100x100/333/FFF?text=STN',
  },
  {
    id: 2,
    date: 'Sob, 14 mar – 15 mar',
    title: 'BELMONDAWG NA WIXAPOLU',
    location: 'Crackhouse',
    friend: 'Bartosz',
    friendAvatar: 'https://i.pravatar.cc/150?u=12',
    image: 'https://placehold.co/100x100/e6cda3/000?text=BEL',
  },
]
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
import EventAboutDetails from '@/components/events/EventAboutDetails.vue'

import type { Event as EventType } from '@/types/Event'
import { useStickySidebar } from '@/composables/ui/useStickySidebar'
import SuggestedEvents from '@/components/events/SuggestedEvents.vue'

const props = defineProps<{
  eventDetails: EventType | undefined
}>()

// --- MAP CONFIG ---
const mapContainerRef = ref<HTMLDivElement | null>(null)
let mapInstance: L.Map | null = null
const mapCenter: L.LatLngTuple = [54.371661, 18.619082] // Default to Gdańsk

const redIcon = new L.Icon({
  iconUrl:
    'https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/markers/marker-icon-2x-red.png',
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
    const coords = props.eventDetails?.coordinates
    const eventCoords: L.LatLngTuple = (coords && coords.length === 2) ? [coords[0], coords[1]] : mapCenter
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
      attribution:
        '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors &copy; <a href="https://carto.com/attributions">CARTO</a>',
      maxZoom: 20,
    }).addTo(mapInstance)

    const markerCoords: L.LatLngTuple = (coords && coords.length === 2) ? [coords[0], coords[1]] : mapCenter
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

const organizers = [
  {
    name: 'Technikalia',
    type: 'Wydarzenie',
    role: '92 minionych wydarzeń',
    logo: 'https://placehold.co/100x100/1e293b/FFF?text=T.26',
    description: '',
  },
  {
    name: 'Politechnika Gdańska',
    type: 'Szkoła wyższa',
    role: '361 minionych wydarzeń',
    logo: 'https://placehold.co/100x100/white/000?text=PG',
    description: '',
  },
]
</script>

<template>
  <div class="grid grid-cols-1 max-w-[1200px] mx-auto lg:grid-cols-5 gap-6 mt-6">
    <div class="lg:col-span-3 space-y-6">
      <EventAboutDetails :event-details="props.eventDetails" />

      <div class="bg-theme-bg-secondary rounded-xl shadow-sm p-6">
        <h2 class="text-[20px] font-bold mb-6">{{ $t('events.poznajOrganizatorow') }}</h2>

        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div
            v-for="org in organizers"
            :key="org.name"
            class="border border-theme-border rounded-xl p-6 flex flex-col items-center text-center bg-theme-bg-subtle/20"
          >
            <div
              class="w-24 h-24 rounded-full overflow-hidden mb-4 border-4 border-theme-bg-secondary shadow-sm shrink-0"
            >
              <img :src="org.logo" class="w-full h-full object-cover" />
            </div>

            <h3 class="font-bold text-[17px] leading-tight">{{ org.name }}</h3>
            <p class="text-[12px] text-theme-text-secondary mt-1">
              {{ org.role }} · Strona · {{ org.type }}
            </p>

            <div class="w-full border-t border-theme-border my-5"></div>

            <p class="text-[14px] text-theme-text-secondary line-clamp-2 mb-6 h-10">
              {{ org.description || 'Organizator wydarzenia ' + (eventDetails?.name || '') }}
            </p>

            <div class="w-full mt-auto">
              <button
                v-if="org.name.includes('Technikalia')"
                class="w-full bg-[#E4E6EB] dark:bg-white/10 hover:bg-[#D8DADF] dark:hover:bg-white/20 text-theme-text py-2.5 rounded-lg font-semibold text-[15px] flex items-center justify-center gap-2 transition"
              >
                <InformationIcon :size="20" />{{ $t('auth.register.learnMore') }}</button>
              <button
                v-else
                class="w-full bg-[#E4E6EB] dark:bg-white/10 hover:bg-[#D8DADF] dark:hover:bg-white/20 text-theme-text py-2.5 rounded-lg font-semibold text-[15px] flex items-center justify-center gap-2 transition"
              >
                <ShareVariantIcon :size="20" class="scale-x-[-1]" />{{ $t('events.skontaktujSieZNami') }}</button>
            </div>
          </div>
        </div>
      </div>
      <SuggestedEvents />
      <div
        class="h-[1000px] bg-gradient-to-b from-transparent to-theme-bg-subtle/10 rounded-lg"
      ></div>
    </div>

    <div
      ref="rightSectionRef"
      class="lg:col-span-2 space-y-4 sticky z-10 self-start"
      :style="{ top: `${stickyTop}px` }"
    >
      <div class="bg-theme-bg-secondary rounded-xl shadow-sm overflow-hidden">
        <div class="w-full h-[400px] relative isolate z-0">
          <div ref="mapContainerRef" class="w-full h-full bg-theme-bg-subtle"></div>
        </div>
        <div class="p-4">
          <div class="font-bold text-theme-text text-[17px]">
            {{ eventDetails?.locationName || eventDetails?.location || 'Brak lokalizacji' }}
          </div>
          <div class="text-[14px] text-theme-text-secondary mt-0.5">
            {{ eventDetails?.address || 'Brak adresu' }}
          </div>
        </div>
      </div>

      <div class="bg-theme-bg-secondary rounded-xl shadow-sm p-4">
        <div class="flex justify-between items-center mb-5">
          <h3 class="text-[20px] font-bold text-theme-text">{{ $t('events.goscie') }}</h3>
          <button class="text-[#0866FF] hover:underline text-[15px] font-medium transition-colors">{{ $t('home.showAll') }}</button>
        </div>

        <div class="flex justify-around text-center mb-3">
          <div class="flex-1">
            <div class="text-[20px] font-bold text-theme-text">
              {{ eventDetails?.guestsGoing || 15 }}
            </div>
            <div class="text-[14px] text-theme-text-secondary">{{ $t('events.wezmaUdzial') }}</div>
          </div>
          <div class="flex-1">
            <div class="text-[20px] font-bold text-theme-text">
              {{ eventDetails?.guestsInterested || 58 }}
            </div>
            <div class="text-[14px] text-theme-text-secondary">{{ $t('events.zainteresowani') }}</div>
          </div>
        </div>

        <div class="border-t border-theme-border my-4"></div>

        <h4 class="text-[17px] font-bold mb-4">{{ $t('events.wybierzSieZeZnajomymi') }}</h4>
        <ul class="space-y-4">
          <li
            v-for="friend in friends"
            :key="friend.name"
            class="flex items-center justify-between"
          >
            <div class="flex items-center gap-3">
              <img
                :src="friend.avatar"
                class="w-10 h-10 rounded-full object-cover shadow-sm border border-theme-border"
              />
              <span
                class="text-[15px] font-semibold text-theme-text cursor-pointer hover:underline"
              >
                {{ friend.name }}
              </span>
            </div>
            <button
              class="bg-[#E4E6EB] dark:bg-white/10 hover:bg-[#D8DADF] dark:hover:bg-white/20 text-theme-text px-4 py-1.5 rounded-md text-[15px] font-semibold transition"
            >{{ $t('groups.invite') }}</button>
          </li>
        </ul>

        <button
          class="w-full mt-5 bg-[#E7F3FF] dark:bg-[#E7F3FF]/10 hover:bg-[#DBE7F2] dark:hover:bg-[#E7F3FF]/20 text-[#0866FF] dark:text-[#E7F3FF] font-semibold py-2 rounded-lg text-[15px] transition-colors"
        >{{ $t('events.wyslijWiadomosciDoZnajomych') }}</button>
      </div>

      <div class="bg-theme-bg-secondary rounded-xl shadow-sm p-4">
        <h3 class="text-[20px] font-bold mb-5">{{ $t('events.popularneWsrodZnajomych') }}</h3>

        <div class="space-y-6">
          <div v-for="item in popularEvents" :key="item.id" class="flex gap-3">
            <div
              class="w-16 h-16 bg-theme-bg-subtle rounded-lg shrink-0 overflow-hidden border border-theme-border"
            >
              <img
                :src="item.image"
                class="w-full h-full object-cover hover:scale-105 transition-transform"
              />
            </div>

            <div class="flex-1 min-w-0">
              <div class="text-[#E41E3F] text-[13px] font-medium uppercase tracking-tight mb-1">
                {{ item.date }}
              </div>
              <div class="font-bold text-[15px] leading-tight mb-1">{{ item.title }}</div>
              <div class="text-[14px] text-theme-text-secondary leading-tight">
                {{ item.location }}
              </div>

              <div class="text-[13px] text-theme-text-secondary mt-2 flex items-center gap-1.5">
                <img
                  :src="item.friendAvatar"
                  class="w-4 h-4 rounded-full border border-theme-border"
                />
                <span class="truncate">{{ $t('events.itemFriendJestZainteresowany') }}</span>
              </div>

              <div class="flex gap-2 mt-3">
                <button
                  class="flex-1 bg-[#E4E6EB] dark:bg-white/10 hover:bg-[#D8DADF] dark:hover:bg-white/20 text-theme-text py-1.5 rounded-lg font-semibold text-[15px] flex items-center justify-center gap-1.5 transition"
                >
                  <StarOutlineIcon :size="20" />{{ $t('events.zainteresowanyA') }}</button>
                <button
                  class="bg-[#E4E6EB] dark:bg-white/10 hover:bg-[#D8DADF] dark:hover:bg-white/20 text-theme-text px-4 py-1.5 rounded-lg transition flex items-center justify-center"
                >
                  <ReplyIcon :size="20" class="rotate-0 scale-x-[-1] opacity-70" />
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
