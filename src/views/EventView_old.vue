<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed } from 'vue';
import { useEventsStore } from '@/stores/events';
import ImageWithGradient from '@/components/ImageWithGradient.vue';
import EventsSidebar from '@/components/events/EventsSidebar.vue';
// --- LEAFLET IMPORTS (Czysty Leaflet) ---
import 'leaflet/dist/leaflet.css';
import L from 'leaflet';

// Fix dla ikon (Standardowy problem w Vue/Vite)
import iconRetinaUrl from 'leaflet/dist/images/marker-icon-2x.png';
import iconUrl from 'leaflet/dist/images/marker-icon.png';
import shadowUrl from 'leaflet/dist/images/marker-shadow.png';

// Reset domyślnych ikon
// @ts-expect-error - Leaflet internal property
delete L.Icon.Default.prototype._getIconUrl;
L.Icon.Default.mergeOptions({
  iconRetinaUrl,
  iconUrl,
  shadowUrl,
});

// Import Icons (Material Design)
import MapMarkerIcon from 'vue-material-design-icons/MapMarker.vue';
import StarIcon from 'vue-material-design-icons/Star.vue';
import CheckCircleIcon from 'vue-material-design-icons/CheckCircle.vue';
import EmailIcon from 'vue-material-design-icons/Email.vue';
import ShareVariantIcon from 'vue-material-design-icons/ShareVariant.vue';
import DotsHorizontalIcon from 'vue-material-design-icons/DotsHorizontal.vue';
import AccountGroupIcon from 'vue-material-design-icons/AccountGroup.vue';
import EarthIcon from 'vue-material-design-icons/Earth.vue';
import InformationIcon from 'vue-material-design-icons/Information.vue';

// --- PINIA STORE ---
const eventsStore = useEventsStore();

// --- EVENT SELECTION STATE ---
const selectedEventId = ref<string | null>(null);

// Get all events from store
const allEvents = computed(() => eventsStore.events);

// Get the currently selected event details
const currentEvent = computed(() => {
  if (allEvents.value.length === 0) {
    return {
      id: '0',
      name: 'Brak Wydarzenia',
      startDate: new Date().toISOString().split('T')[0],
      startTime: '00:00',
      type: 'offline' as const,
      privacy: 'public' as const,
      location: { name: '', lat: 0, lng: 0 },
      description: 'Brak dostępnych wydarzeń',
      images: [],
      hosts: ['Brak'],
    };
  }
  
  if (!selectedEventId.value) {
    return allEvents.value[0]!;
  }
  
  const event = eventsStore.getEventById(selectedEventId.value);
  return event || allEvents.value[0]!;
});

// Initialize with first event
onMounted(() => {
  if (allEvents.value.length > 0 && !selectedEventId.value) {
    selectedEventId.value = allEvents.value[0]?.id ?? null;
  }
});

// Handle event selection
const selectEvent = (eventId: string) => {
  selectedEventId.value = eventId;
};

// --- KONFIGURACJA MAPY ---
const mapContainerRef = ref<HTMLDivElement | null>(null);
let mapInstance: L.Map | null = null;

// Watcher do aktualizacji mapy gdy zmienia się event
const updateMapMarker = () => {
  if (!mapInstance || !currentEvent.value.location) return;
  
  const { lat, lng, name } = currentEvent.value.location;
  if (lat === 0 && lng === 0) return; // Skip online events
  
  // Clear existing layers
  mapInstance.eachLayer((layer) => {
    if (layer instanceof L.Marker) {
      mapInstance!.removeLayer(layer);
    }
  });
  
  // Set new center and add marker
  mapInstance.setView([lat, lng], 15);
  L.marker([lat, lng], { icon: redIcon })
    .addTo(mapInstance)
    .bindTooltip(name, { direction: 'top', offset: [0, -40] });
};

// --- LOGIKA "FACEBOOK DUAL STICKY" ---
const rightSectionRef = ref(null);
const stickyTop = ref(0); // Dynamiczna wartość CSS 'top'

// Konfiguracja marginesów
const HEADER_OFFSET = 56; // Wysokość nawigacji
const BOTTOM_OFFSET = 16;  // Margines od dołu ekranu

let lastScrollY = window.scrollY;

const updateStickyPosition = () => {
    if (!rightSectionRef.value) return;

    const currentScrollY = window.scrollY;
    const scrollDiff = currentScrollY - lastScrollY;

    const viewportHeight = window.innerHeight;
    const sidebarHeight = rightSectionRef.value.offsetHeight;

    if (sidebarHeight + HEADER_OFFSET + BOTTOM_OFFSET < viewportHeight) {
        stickyTop.value = HEADER_OFFSET;
        lastScrollY = currentScrollY;
        return;
    }

    let newTop = stickyTop.value - scrollDiff;

    const maxTop = HEADER_OFFSET;
    const minTop = viewportHeight - sidebarHeight - BOTTOM_OFFSET;

    if (newTop > maxTop) {
        newTop = maxTop;
    } else if (newTop < minTop) {
        newTop = minTop;
    }

    stickyTop.value = newTop;
    lastScrollY = currentScrollY;
};

let resizeObserver = null;


onMounted(() => {
    // Inicjalizacja pozycji
    stickyTop.value = HEADER_OFFSET;
    lastScrollY = window.scrollY;

    window.addEventListener('scroll', updateStickyPosition, { passive: true });
    window.addEventListener('resize', updateStickyPosition);

    if (rightSectionRef.value) {
        resizeObserver = new ResizeObserver(() => {
            updateStickyPosition();
        });
        resizeObserver.observe(rightSectionRef.value);
    }
  if (mapContainerRef.value) {
    // 1. Inicjalizacja mapy
    mapInstance = L.map(mapContainerRef.value, {
      center: mapCenter,
      zoom: 15,
      zoomControl: false,    // Ukrywa przyciski +/-
      scrollWheelZoom: false,// Blokuje zoom kółkiem myszy
      doubleClickZoom: false,// Blokuje zoom podwójnym kliknięciem
      touchZoom: false,      // Blokuje zoom "uszczypnięciem" na mobile
      boxZoom: false,        // Blokuje zoom zaznaczaniem obszaru (Shift+Drag)
      keyboard: false,        // Blokuje skróty klawiszowe +/-
      dragging: false         // Wyłącz domyślne kontrolki (dodamy własne niżej)
    });

    // 2. Styl Mapy: CartoDB Voyager (Jasny, nowoczesny styl)
    L.tileLayer('https://{s}.basemaps.cartocdn.com/rastertiles/voyager/{z}/{x}/{y}{r}.png', {
      attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors &copy; <a href="https://carto.com/attributions">CARTO</a>',
      maxZoom: 20
    }).addTo(mapInstance);

    // 3. Dodanie Czerwonego Markera
    L.marker(mapCenter, { icon: redIcon })
      .addTo(mapInstance)
      .bindTooltip("Camper Park PG", { direction: 'top', offset: [0, -40] });

    // 4. Kontrolki Zoomu na dole po prawej
    L.control.zoom({ position: 'bottomright' }).addTo(mapInstance);
  }
});

onUnmounted(() => {
    window.removeEventListener('scroll', updateStickyPosition);
    window.removeEventListener('resize', updateStickyPosition);
    if (resizeObserver) resizeObserver.disconnect();
  if (mapInstance) {
    mapInstance.remove();
    mapInstance = null;
  }
});

// --- DANE (MOCK DATA) ---
const eventDetails = {
  date: 'Sobota, 16 maja 2026 o 15:00',
  title: 'Technikalia.26',
  locationName: 'Camper Park Politechniki Gdańskiej',
  address: 'ul. Towarowa 40, 80-218 Gdańsk',
  responses: 203,
  hosts: ['Technikalia', 'Politechnika Gdańska', 'SSPG - Samorząd Studentów PG'],
  description: 'Technikalia.26 💙 - Robimy to dla Was kolejny rok! 🔥 Widzimy się 16 maja 2026 na Camperparku! ⛺ To już 16. edycja Festiwalu Studentów Politechniki Gdańskiej...',
  guestsGoing: 84,
  guestsInterested: 119
};

const friends = [
  { name: 'Mikołaj Niedziela', avatar: 'https://i.pravatar.cc/150?u=1' },
  { name: 'Wojtek Piotrowski', avatar: 'https://i.pravatar.cc/150?u=2' },
  { name: 'Magda Chłopecka', avatar: 'https://i.pravatar.cc/150?u=3' },
];

const organizers = [
  { name: 'Technikalia', type: 'Wydarzenie', role: '92 minionych wydarzeń', logo: 'https://placehold.co/100x100/1e293b/FFF?text=T.26' },
  { name: 'Politechnika Gdańska', type: 'Szkoła wyższa', role: '361 minionych wydarzeń', logo: 'https://placehold.co/100x100/white/000?text=PG' },
];
</script>

<template>
  <div class="flex min-h-screen bg-[#F0F2F5] font-sans text-gray-900 pb-10">
    <EventsSidebar  />
    <div class="flex-1">
      <div class="h-[350px] relative flex justify-center items-center overflow-hidden shadow-sm">
        <ImageWithGradient image-url="https://scontent-waw2-2.xx.fbcdn.net/v/t39.30808-6/605641384_1198742319135884_3466586650420032965_n.jpg?_nc_cat=100&ccb=1-7&_nc_sid=75d36f&_nc_ohc=R3C_HNibSrQQ7kNvwEX05px&_nc_oc=AdlyauSPccO6HMnjYEJpYdB15CvSS90fA4ha03m6GcJipc2fSg43Cpvtd3nUP-eRgc-dP0Sq4BeUbYeyKb-qXKgh&_nc_zt=23&_nc_ht=scontent-waw2-2.xx&_nc_gid=RgfkCre1pHX6OyeIgMXppQ&oh=00_Afqg6cMgSjoaWH4QHNrGCLBvYjJ93PYIclsChWeZDCWHuw&oe=695D7012" class="w-full h-full object-cover " />
      </div>

      <div class="max-w-[1250px] mx-auto px-4 sm:px-0 relative">

        <div class="bg-white px-6 pb-4 pt-0 shadow-sm border-b border-gray-300 sm:rounded-b-lg -mt-4 relative z-10 mx-0 sm:mx-4">
          <div class="flex flex-col md:flex-row gap-4 pt-6">
            <div class="bg-white border border-gray-200 rounded-lg shadow-sm w-16 h-16 flex flex-col items-center justify-center flex-shrink-0 overflow-hidden text-center -mt-10 md:mt-0 relative z-20">
              <div class="h-5 w-full bg-red-600 text-white text-[10px] font-bold flex items-center justify-center uppercase tracking-wide">Maj</div>
              <div class="text-2xl font-bold text-gray-800 leading-none pt-1">16</div>
            </div>
            <div class="flex-grow">
              <div class="text-red-600 text-sm font-semibold uppercase">{{ eventDetails.date }}</div>
              <h1 class="text-3xl font-bold text-gray-900 mt-1">{{ eventDetails.title }}</h1>
              <div class="text-gray-500 text-sm mt-1 font-medium">
                {{ eventDetails.locationName }}, {{ eventDetails.address }}
              </div>
            </div>
          </div>

          <div class="flex flex-col md:flex-row justify-between items-center mt-6 border-t border-gray-200 pt-4 gap-4">
            <div class="flex gap-6 text-sm font-semibold text-gray-600">
              <button class="text-blue-600 border-b-2 border-blue-600 pb-4 -mb-4 px-1">Informacje</button>
              <button class="hover:bg-gray-100 rounded px-2 py-1 transition">Dyskusja</button>
            </div>
            <div class="flex gap-2 w-full md:w-auto">
              <button class="flex-1 md:flex-none flex items-center justify-center gap-2 bg-gray-200 hover:bg-gray-300 text-gray-800 px-4 py-2 rounded-md font-semibold text-sm transition">
                <StarIcon :size="20" /> Zainteresowany(a)
              </button>
              <button class="flex-1 md:flex-none flex items-center justify-center gap-2 bg-gray-200 hover:bg-gray-300 text-gray-800 px-4 py-2 rounded-md font-semibold text-sm transition">
                <CheckCircleIcon :size="20" /> Wezmę udział
              </button>
              <button class="flex-1 md:flex-none flex items-center justify-center gap-2 bg-gray-200 hover:bg-gray-300 text-gray-800 px-4 py-2 rounded-md font-semibold text-sm transition">
                <EmailIcon :size="20" /> Zaproś
              </button>
              <button class="bg-gray-200 hover:bg-gray-300 text-gray-800 px-3 py-2 rounded-md transition">
                <ShareVariantIcon :size="20" />
              </button>
              <button class="bg-gray-200 hover:bg-gray-300 text-gray-800 px-3 py-2 rounded-md transition">
                <DotsHorizontalIcon :size="20" />
              </button>
            </div>
          </div>
        </div>

        <div class="grid grid-cols-1 lg:grid-cols-5 gap-4 mt-4 mx-0 sm:mx-4">

          <div class="lg:col-span-3 space-y-4">
            <div class="bg-white rounded-lg shadow-sm p-4">
              <h2 class="text-xl font-bold mb-4">Szczegółowe informacje</h2>
              <ul class="space-y-4 text-gray-700">
                <li class="flex items-start gap-3">
                  <AccountGroupIcon class="text-gray-500 mt-1" />
                  <span>{{ eventDetails.responses }} użytkowników odpowiedziało</span>
                </li>
                <li class="flex items-start gap-3">
                  <AccountGroupIcon class="text-gray-500 mt-1" />
                  <div>
                    Wydarzenie <span class="font-semibold">{{ eventDetails.hosts[0] }}</span>, <span class="font-semibold">{{ eventDetails.hosts[1] }}</span> i <span class="font-semibold">{{ eventDetails.hosts[2] }}</span>
                  </div>
                </li>
                <li class="flex items-start gap-3">
                  <MapMarkerIcon class="text-gray-500 mt-1" />
                  <div>
                    {{ eventDetails.locationName }}
                    <div class="text-sm text-gray-500">{{ eventDetails.address }}</div>
                  </div>
                </li>
                <li class="flex items-start gap-3">
                  <EarthIcon class="text-gray-500 mt-1" />
                  <div>Publiczne · Każdy na Facebooku i poza nim</div>
                </li>
              </ul>
              <div class="mt-6 text-sm text-gray-800 leading-relaxed whitespace-pre-line">
                {{ eventDetails.description }} <span class="font-semibold cursor-pointer hover:underline">... Wyświetl więcej</span>
              </div>
              <div class="mt-4">
                <span class="inline-block bg-gray-100 hover:bg-gray-200 rounded-full px-3 py-1 text-sm font-semibold text-gray-700 mr-2 mb-2 cursor-pointer">Gdańsk</span>
              </div>
            </div>

            <div class="bg-white rounded-lg shadow-sm p-4">
              <div class="flex justify-between items-center mb-4">
                <h2 class="text-xl font-bold">Poznaj organizatorów</h2>
              </div>
              <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div v-for="org in organizers" :key="org.name" class="border border-gray-200 rounded-lg p-4 flex flex-col items-center text-center">
                  <img :src="org.logo" class="w-20 h-20 rounded-full mb-3 object-cover border border-gray-100" />
                  <h3 class="font-bold text-lg">{{ org.name }}</h3>
                  <p class="text-xs text-gray-500 mt-1">{{ org.role }} · {{ org.type }}</p>
                  <div class="mt-4 w-full pt-4 border-t border-gray-100">
                     <button class="w-full bg-gray-100 hover:bg-gray-200 text-gray-800 py-2 rounded font-semibold text-sm flex items-center justify-center gap-2 transition">
                       <InformationIcon :size="18" /> Dowiedz się więcej
                     </button>

                  </div>
                </div>
              </div>
            </div>
            <div class="h-[2000px]">sss</div>
          </div>

         <div
      ref="rightSectionRef"
      class="lg:col-span-2 space-y-4 sticky z-10 self-start"
      :style="{ top: `${stickyTop}px` }"
  >

            <div class="bg-white rounded-lg shadow-sm p-4">
               <div class="text-lg font-bold mb-2">Lokalizacja</div>

               <div class="w-full h-48 rounded-lg overflow-hidden border border-gray-200 relative isolate z-0">
                  <div ref="mapContainerRef" class="w-full h-full bg-gray-100"></div>
               </div>

               <div class="mt-3">
                 <div class="font-semibold text-gray-900">{{ eventDetails.locationName }}</div>
                 <div class="text-sm text-gray-500">{{ eventDetails.address }}</div>
               </div>
            </div>

            <div class="bg-white rounded-lg shadow-sm p-4">
              <h3 class="text-lg font-bold mb-4">Goście</h3>
              <div class="flex justify-around text-center mb-4">
                <div>
                  <div class="text-xl font-bold text-gray-900">{{ eventDetails.guestsGoing }}</div>
                  <div class="text-xs text-gray-500">Wezmę udział</div>
                </div>
                <div>
                  <div class="text-xl font-bold text-gray-900">{{ eventDetails.guestsInterested }}</div>
                  <div class="text-xs text-gray-500">Zainteresowani</div>
                </div>
              </div>
              <hr class="border-gray-100 my-4" />
              <h4 class="text-sm font-semibold mb-3">Wybierz się ze znajomymi</h4>
              <ul class="space-y-3">
                <li v-for="friend in friends" :key="friend.name" class="flex items-center justify-between">
                  <div class="flex items-center gap-3">
                    <img :src="friend.avatar" class="w-9 h-9 rounded-full bg-gray-300" />
                    <span class="text-sm font-medium text-gray-900">{{ friend.name }}</span>
                  </div>
                  <button class="bg-gray-100 hover:bg-gray-200 text-gray-800 px-3 py-1.5 rounded text-sm font-semibold transition">
                    Zaproś
                  </button>
                </li>
              </ul>
            </div>

             <div class="bg-white rounded-lg shadow-sm p-4">
               <h3 class="text-lg font-bold mb-4">Popularne wśród znajomych</h3>
               <div class="flex gap-3">
                  <div class="w-16 h-16 bg-gray-200 rounded-lg flex-shrink-0 overflow-hidden">
                      <img src="https://placehold.co/100x100/orange/white?text=K" class="w-full h-full object-cover" />
                  </div>
                  <div>
                     <div class="text-red-600 text-xs font-bold uppercase">Śr, 7 sty o 18:00</div>
                     <div class="font-bold text-sm leading-tight mt-0.5">LUBLIN Warsztaty "Kuchenne duety"</div>
                     <div class="text-xs text-gray-500 mt-1">Restauracja Giuseppe</div>
                     <div class="text-xs text-gray-500 mt-1 flex items-center gap-1">
                        <img src="https://i.pravatar.cc/150?u=5" class="w-4 h-4 rounded-full" />
                        Wioleta jest zainteresowana
                     </div>
                  </div>
               </div>
               <div class="mt-3 flex gap-2">
                  <button class="flex-1 bg-gray-100 hover:bg-gray-200 text-gray-700 py-1.5 rounded text-sm font-semibold flex items-center justify-center gap-1 transition">
                     <StarIcon :size="16" /> Zainteresowany(a)
                  </button>
                  <button class="bg-gray-100 hover:bg-gray-200 text-gray-700 px-3 py-1.5 rounded transition">
                     <ShareVariantIcon :size="16" />
                  </button>
               </div>
            </div>

          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style>
/* Fix dla mapy w kontenerach z zaokrąglonymi rogami */
.leaflet-container {
    z-index: 0;
    font-family: inherit;
}
/* Opcjonalne zmniejszenie stopki licencji dla estetyki */
.leaflet-control-attribution {
  font-size: 8px;
  background-color: rgba(255,255,255,0.7) !important;
}
</style>
