<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useCreatePostStore } from '@/stores/createPost';

// --- IKONY (Material Design Icons) ---
import MapMarkerIcon from 'vue-material-design-icons/MapMarker.vue';
import MapMarkerOutlineIcon from 'vue-material-design-icons/MapMarkerOutline.vue';
import OfficeBuildingIcon from 'vue-material-design-icons/OfficeBuilding.vue';
import TreeIcon from 'vue-material-design-icons/Tree.vue';
import NavigationIcon from 'vue-material-design-icons/Navigation.vue';
import CloseIcon from 'vue-material-design-icons/Close.vue';
import MagnifyIcon from 'vue-material-design-icons/Magnify.vue';
import type { PostLocation } from '@/types/Post';
interface Suggestion {
  name: string;
  full_address: string;
  place_formatted: string;
  feature_type: string;
  mapbox_id: string;
}

const emit = defineEmits<{
  (e: 'back'): void;
}>();

const createPostStore = useCreatePostStore();
const searchQuery = ref('');
const suggestions = ref<PostLocation[]>([]);

const currentCity = ref<PostLocation | null>(null);
const selectedLocation = ref<PostLocation | null>(null);
const dynamicCenter = ref<{lat: string, lon: string} | null>(null);
const loading = ref(false);
const sessionToken = ref<string | null>(null);
let debounceTimeout: ReturnType<typeof setTimeout>;

// --- SESJA I WYSZUKIWANIE ---
const MAPBOX_TOKEN = import.meta.env.VITE_MAPBOX_KEY as string;
const ensureSessionToken = () => {
  if (!sessionToken.value) {
    sessionToken.value = Math.random().toString(36).substring(2);
  }
};

const searchLocations = async () => {
  if (searchQuery.value.length < 2) return;

  ensureSessionToken();
  loading.value = true;

  try {
    const types = 'country,region,district,locality,place,poi,neighborhood,address';


    const url = `https://api.mapbox.com/search/searchbox/v1/suggest` +
      `?q=${encodeURIComponent(searchQuery.value)}` +
      `&access_token=${MAPBOX_TOKEN}` +
      `&language=pl` +
      `&limit=10` +
      `&types=${types}` +
      `&session_token=${sessionToken.value}`;

    const res = await fetch(url);
    const data = await res.json();

    if (!data.suggestions) return;

    suggestions.value = data.suggestions.map((item: Suggestion) => {
      const featureType = item.feature_type;
      let type: PostLocation['type'] = 'place';

      if (featureType === 'place' || featureType === 'locality') type = 'city';
      else if (featureType === 'neighborhood' || featureType === 'district') type = 'district';
      else if (featureType === 'poi' || featureType === 'address') type = 'attraction';

      return {
        title: item.name,
        subtitle: item.full_address || item.place_formatted || '',
        type,
        lat: null,
        lon: null,
        searchbox_id: item.mapbox_id
      };
    });
  } catch (e) {
    console.error('Mapbox Search Box Error', e);
  } finally {
    loading.value = false;
  }
};

// Pobieranie współrzędnych dla wybranego elementu z listy sugestii
const retrieveLocationCoordinates = async (item: PostLocation) => {
  if (!item.searchbox_id) return item;

  try {
    const url = `https://api.mapbox.com/search/searchbox/v1/retrieve/${item.searchbox_id}` +
      `?access_token=${MAPBOX_TOKEN}` +
      `&session_token=${sessionToken.value}`;

    const res = await fetch(url);
    const data = await res.json();

    if (data.features && data.features.length > 0) {
      const feature = data.features[0];
      const [longitude, latitude] = feature.geometry.coordinates;

      item.lat = String(latitude);
      item.lon = String(longitude);

      // Reset sesji po udanym pobraniu punktu docelowego
      sessionToken.value = null;
    }
    return item;
  } catch (e) {
    console.error('Mapbox Retrieve Error', e);
    return item;
  }
};

// Główna funkcja obsługująca kliknięcie w lokalizację
const handleSelect = async (loc: PostLocation) => {
  loading.value = true;

  // Jeśli lokalizacja nie ma jeszcze koordynatów (pochodzi z suggest), pobierz je
  if (!loc.lat || !loc.lon) {
    const updatedLoc = await retrieveLocationCoordinates(loc);
    selectedLocation.value = updatedLoc;
  } else {
    // Jeśli już ma (np. z GPS), po prostu ją zaznacz
    selectedLocation.value = loc;
  }

  loading.value = false;
};

const onInput = () => {
  selectedLocation.value = null;
  clearTimeout(debounceTimeout);
  if (!searchQuery.value) {
    suggestions.value = [];
    return;
  }
  debounceTimeout = setTimeout(searchLocations, 400);
};

const handleConfirm = () => {
  if (selectedLocation.value) {
    createPostStore.setLocation(selectedLocation.value);
  }
  emit('back');
};

// --- GPS ---
const initLocation = () => {
  if (!navigator.geolocation) return;

  // Opcje geolokalizacji dla lepszej stabilności
  const geoOptions = {
    enableHighAccuracy: false, // Szybszy odczyt (Wi-Fi/BTS) zamiast czekania na GPS
    timeout: 8000,             // Max 8 sekund oczekiwania
    maximumAge: 3600000        // Akceptuj lokalizację sprzed godziny (cache)
  };

  navigator.geolocation.getCurrentPosition(
    async (pos) => {
      try {
        const { latitude, longitude } = pos.coords;

        // Ustawiamy środek dla lepszych wyników wyszukiwania (proximity)
        dynamicCenter.value = {
          lat: latitude.toString(),
          lon: longitude.toString()
        };

        // Używamy /reverse z Search Box API (spójność z /suggest i /retrieve)
        const url = `https://api.mapbox.com/search/searchbox/v1/reverse` +
          `?longitude=${longitude}` +
          `&latitude=${latitude}` +
          `&access_token=${MAPBOX_TOKEN}` +
          `&types=locality,place` +
          `&language=pl`;

        const res = await fetch(url);
        const data = await res.json();

        if (data.features && data.features.length > 0) {
          const feature = data.features[0];
          const props = feature.properties;

          currentCity.value = {
            title: props.name || props.place_formatted,
            subtitle: 'Twoja obecna lokalizacja',
            type: 'current',
            lat: latitude.toString(),
            lon: longitude.toString()
          };

          // Automatycznie zaznaczamy obecną lokalizację na start
          selectedLocation.value = currentCity.value;
        }
      } catch (e) {
        console.error("Mapbox Reverse Geocoding Error", e);
      }
    },
    (err) => {
      // Obsługa kCLErrorLocationUnknown oraz odmowy dostępu
      console.warn(`Geolocation error (${err.code}): ${err.message}`);
    },
    geoOptions
  );
};

onMounted(initLocation);
</script>

<template>
  <div class=" h-[700px] w-full  mx-auto flex flex-col overflow-hidden ">



    <div class="px-4 py-3">
      <div class="relative flex items-center bg-gray-100 rounded-lg px-3 py-1 focus-within:bg-white focus-within:ring-2 focus-within:ring-[#1877f2] transition-all">
        <magnify-icon :size="20" class="text-gray-500" />
        <input
          v-model="searchQuery"
          @input="onInput"
          type="text"
          placeholder="Gdzie jesteś?"
          class="w-full bg-transparent border-none focus:ring-0 py-2 px-2 text-[15px] outline-none"
        />
        <button v-if="searchQuery" @click="searchQuery = ''; suggestions = []" class="text-gray-400">
          <close-icon :size="18" class="bg-gray-300 rounded-full p-0.5 text-white" />
        </button>
      </div>
    </div>

    <div class="flex-1 overflow-y-auto px-2 pb-24 custom-scrollbar">

      <div v-if="currentCity && !searchQuery" class="mb-4">
        <p class="text-[11px] font-bold text-gray-500 uppercase tracking-wider mb-2 px-4">Twoja lokalizacja</p>
        <div @click="handleSelect(currentCity)"
             class="flex items-center gap-4 p-3 hover:bg-gray-100 cursor-pointer rounded-xl transition mx-2"
             :class="{'bg-blue-50': selectedLocation?.title === currentCity.title}">
          <div class="w-10 h-10 rounded-full bg-blue-100 text-[#1877f2] flex items-center justify-center">
            <navigation-icon :size="22" />
          </div>
          <div class="flex-1">
            <p class="font-bold text-[15px] text-gray-900 leading-tight">Użyj obecnej lokalizacji</p>
            <p class="text-[13px] text-gray-500">{{ currentCity.title }}</p>
          </div>
          <navigation-icon v-if="selectedLocation?.title === currentCity.title" :size="20" class="text-[#1877f2]" />
        </div>
      </div>

      <div v-if="suggestions.length > 0">
        <p class="text-[11px] font-bold text-gray-500 uppercase tracking-wider mb-2 px-4 pt-2">Propozycje</p>
        <ul class="space-y-1 mx-2">
          <li v-for="(loc, index) in suggestions" :key="index" @click="handleSelect(loc)"
              class="flex items-center gap-4 p-3 hover:bg-gray-100 cursor-pointer rounded-xl transition"
              :class="{'bg-blue-50': selectedLocation?.title === loc.title}">

            <div class="w-10 h-10 rounded-full bg-gray-100 text-gray-600 flex items-center justify-center shrink-0">
              <map-marker-icon v-if="loc.type === 'city'" :size="22" />
              <office-building-icon v-else-if="loc.type === 'district'" :size="22" />
              <tree-icon v-else-if="loc.type === 'park'" :size="22" />
              <map-marker-outline-icon v-else :size="22" />
            </div>

            <div class="flex-1 overflow-hidden text-left">
              <p class="font-bold text-[15px] text-gray-900 truncate leading-tight">{{ loc.title }}</p>
              <p class="text-[13px] text-gray-500 truncate mt-0.5">{{ loc.subtitle }}</p>
            </div>

            <div v-if="loading && selectedLocation?.title === loc.title" class="w-5 h-5 border-2 border-gray-300 border-t-blue-500 rounded-full animate-spin"></div>
            <close-icon v-else-if="selectedLocation?.title === loc.title" :size="20" class="text-gray-400" />
          </li>
        </ul>
      </div>

      <div v-if="loading && suggestions.length === 0" class="py-10 flex justify-center">
        <div class="w-6 h-6 border-2 border-gray-200 border-t-[#1877f2] rounded-full animate-spin"></div>
      </div>
    </div>

    <div class="p-4 bg-white border-t border-gray-100 sticky bottom-0 w-full shadow-[0_-4px_10px_rgba(0,0,0,0.03)]">
      <button
        @click="handleConfirm"
        :disabled="!selectedLocation || !selectedLocation.lat"
        class="w-full py-3 rounded-lg font-bold text-white transition-all text-[16px]"
        :class="selectedLocation && selectedLocation.lat ? 'bg-[#1877f2] hover:bg-blue-700 active:scale-[0.98]' : 'bg-gray-200 cursor-not-allowed text-gray-400'"
      >
        <span v-if="loading">Pobieranie danych...</span>
        <span v-else>Opublikuj z tą lokalizacją</span>
      </button>
    </div>
  </div>
</template>

<style scoped>
.custom-scrollbar::-webkit-scrollbar { width: 4px; }
.custom-scrollbar::-webkit-scrollbar-thumb { background: #e5e7eb; border-radius: 10px; }
</style>
