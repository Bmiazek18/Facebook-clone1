<template>


    <div class="bg-white w-full h-[90vh] sm:max-w-[800px] sm:h-[650px] sm:rounded-xl shadow-2xl flex flex-col overflow-hidden relative animate-in slide-in-from-bottom-10 duration-300">



      <div class="p-3 border-b border-gray-200 shrink-0 bg-white z-20 relative">
        <div class="bg-[#f0f2f5] rounded-full flex items-center px-3 py-2.5 border border-transparent focus-within:border-[#1877f2] transition-colors group">
          <magnify-icon class="text-[#65676b] mr-2 group-focus-within:text-[#1877f2]" :size="20" />
          <input
            v-model="searchQuery"
            @keydown.enter="searchLocation"
            type="text"
            placeholder="Szukaj według miejscowości, dzielnicy..."
            class="bg-transparent border-none focus:ring-0 text-[15px] w-full text-[#050505] placeholder-[#65676b] p-0 leading-5"
          />
        </div>
      </div>

      <div class="flex-1 relative bg-[#e4e6eb] w-full h-full overflow-hidden">
        <div ref="mapContainer" class="w-full h-full z-10 absolute inset-0"></div>

        <button
           @click.stop="locateUser"
           class="absolute top-4 right-4 z-400 bg-white w-10 h-10 rounded-lg shadow-[0_2px_8px_rgba(0,0,0,0.15)] flex items-center justify-center cursor-pointer hover:bg-gray-50 transition text-gray-700 active:bg-gray-100"
           title="Pokaż moją lokalizację"
        >
           <crosshairs-gps-icon :size="22" />
        </button>

        <div v-if="isLoadingMap" class="absolute inset-0 bg-white/60 z-500 flex items-center justify-center backdrop-blur-[2px]">
           <div class="animate-spin rounded-full h-10 w-10 border-[3px] border-gray-200 border-t-[#1877f2]"></div>
        </div>
      </div>

      <div class="p-4 bg-white border-t border-gray-200 shrink-0 z-20 relative shadow-[0_-4px_12px_rgba(0,0,0,0.05)]">
        <div class="flex items-center gap-2 mb-3 text-[13px] text-[#65676b]">
          <map-marker-icon :size="16" />
          <span>Kliknij mapę, aby wybrać określoną lokalizację.</span>
        </div>

        <div class="border border-gray-300 rounded-lg px-3 py-2 mb-4 hover:border-gray-400 focus-within:border-[#1877f2] focus-within:ring-1 focus-within:ring-[#1877f2] transition bg-white">
          <label class="block text-[12px] text-[#65676b] mb-0.5">Nazwa lokalizacji</label>
          <input
            v-model="tempLocation"
            type="text"
            class="w-full text-[15px] border-none p-0 focus:ring-0 text-[#050505] font-semibold leading-tight placeholder-gray-400"
            placeholder="Wybierz punkt na mapie"
          />
        </div>

        <div class="flex gap-3 justify-end">
          <button
            @click="close"
            class="px-5 py-2 font-semibold text-[#1877f2] hover:bg-gray-100 rounded-lg transition"
          >
            Anuluj
          </button>
          <button
            @click="confirm"
            class="px-10 py-2 font-semibold text-white bg-[#1877f2] hover:bg-[#166fe5] rounded-lg transition shadow-sm active:transform active:scale-[0.98]"
          >
            Zapisz
          </button>
        </div>
      </div>

    </div>

</template>

<script setup lang="ts">
import { ref, watch, nextTick, onBeforeUnmount, onMounted } from 'vue';
import 'leaflet/dist/leaflet.css';
import L from 'leaflet';

// Ikony UI
import MagnifyIcon from 'vue-material-design-icons/Magnify.vue';
import MapMarkerIcon from 'vue-material-design-icons/MapMarker.vue';
import CrosshairsGpsIcon from 'vue-material-design-icons/CrosshairsGps.vue';

// Czerwona ikona markera - używamy SVG zamiast zdalnego URL'a
const redIcon = L.divIcon({
  html: `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="#ef4444" width="32" height="32">
    <path d="M12 2C6.48 2 2 6.48 2 12c0 5.17 3.61 9.48 8.35 9.93.5.08 1.03.13 1.65.13 6.63 0 12-5.37 12-12S18.63 2 12 2zm0 18c-.62 0-1.15-.05-1.65-.13C7.56 19.28 4.5 16.08 4.5 12c0-4.14 3.36-7.5 7.5-7.5s7.5 3.36 7.5 7.5-3.36 7.5-7.5 7.5z"/>
  </svg>`,
  iconSize: [32, 32],
  iconAnchor: [16, 32],
  popupAnchor: [0, -32],
  className: 'leaflet-div-icon-red'
});

const userLocationIcon = L.divIcon({
  className: 'user-location-marker-container',
  html: '<div class="user-location-dot"></div>',
  iconSize: [20, 20],
  iconAnchor: [10, 10]
});

// --- PROPS & EMITS ---
const props = defineProps({
  show: Boolean,
  initialLocation: String
});
const emit = defineEmits(['close', 'confirm']);

// --- STATE ---
const mapContainer = ref<HTMLDivElement | null>(null);
const searchQuery = ref('');
const tempLocation = ref('');
const isLoadingMap = ref(false);

let mapInstance: L.Map | null = null;
let markerInstance: L.Marker | null = null;
let userMarkerInstance: L.Marker | null = null;

// --- WATCHERS ---
watch(() => props.show, async (val) => {
  if (val) {
    tempLocation.value = props.initialLocation || '';
    searchQuery.value = '';
    await nextTick();
    // Dodajemy timeout aby się upewnić że DOM jest gotowy
    setTimeout(() => {
      initMap();
    }, 50);
  } else {
    destroyMap();
  }
});
onMounted(() => {

    initMap();

});
// --- MAP FUNCTIONS ---
const initMap = () => {
  if (mapInstance) return;
  if (!mapContainer.value) return;

  // Domyślnie Warszawa (lub ostatnia znana pozycja)
  const defaultCoords: L.LatLngTuple = [52.2297, 21.0122];

  mapInstance = L.map(mapContainer.value, {
    zoomControl: false,
    attributionControl: false // Ukrywamy stopkę "Leaflet" dla czystszego wyglądu (jak na screenie)
  }).setView(defaultCoords, 13);

  // Warstwa CartoDB Voyager (Jasny styl)
  L.tileLayer('https://{s}.basemaps.cartocdn.com/rastertiles/voyager/{z}/{x}/{y}{r}.png', {
    attribution: '&copy; OpenStreetMap &copy; CARTO',
    subdomains: 'abcd',
    maxZoom: 20
  }).addTo(mapInstance);

  // Zoom w lewym górnym rogu
  L.control.zoom({ position: 'topleft' }).addTo(mapInstance);

  // Kliknięcie w mapę
  mapInstance.on('click', async (e) => {
    const { lat, lng } = e.latlng;
    updateMarker(lat, lng);
    await fetchAddress(lat, lng);
  });

  // Jeśli jest już wpisana lokalizacja, spróbujmy ją znaleźć przy otwarciu
  if (props.initialLocation) {
      searchQuery.value = props.initialLocation;
      searchLocation();
  }
};

const destroyMap = () => {
  if (mapInstance) {
    mapInstance.remove();
    mapInstance = null;
    markerInstance = null;
    userMarkerInstance = null;
  }
};

const locateUser = () => {
  if (!navigator.geolocation) {
    alert("Twoja przeglądarka nie obsługuje geolokalizacji.");
    return;
  }

  isLoadingMap.value = true;

  navigator.geolocation.getCurrentPosition(
    async (position) => {
      const lat = position.coords.latitude;
      const lng = position.coords.longitude;

      // Sprawdzenie czy mapa jest zainicjalizowana
      if (!mapInstance) {
        console.error('Map instance is not initialized');
        isLoadingMap.value = false;
        return;
      }

      mapInstance.setView([lat, lng], 16);

      if (userMarkerInstance) {
        userMarkerInstance.setLatLng([lat, lng]);
      } else {
        userMarkerInstance = L.marker([lat, lng], { icon: userLocationIcon }).addTo(mapInstance);
      }

      updateMarker(lat, lng);
      await fetchAddress(lat, lng);
      isLoadingMap.value = false;
    },
    (error) => {
      console.error(error);
      isLoadingMap.value = false;
      // Cicha porażka lub alert, zależnie od preferencji
    }
  );
};

const updateMarker = (lat: number, lng: number) => {
  if (!mapInstance) {
    console.error('Map instance is not initialized');
    return;
  }

  if (markerInstance) {
    markerInstance.setLatLng([lat, lng]);
  } else {
    markerInstance = L.marker([lat, lng], { icon: redIcon }).addTo(mapInstance);
  }
  mapInstance.panTo([lat, lng]);
};

// --- API CALLS ---
const fetchAddress = async (lat: number, lng: number) => {
  try {
    isLoadingMap.value = true;
    const response = await fetch(`https://nominatim.openstreetmap.org/reverse?format=json&lat=${lat}&lon=${lng}`);
    const data = await response.json();

    const address = data.address;
    let locationName = '';

    // Logika budowania nazwy (Ulica + Numer lub Miasto)
    if (address.road) locationName += address.road;
    if (address.house_number) locationName += ` ${address.house_number}`;

    if (address.city || address.town || address.village) {
       const city = address.city || address.town || address.village;
       locationName += locationName ? `, ${city}` : city;
    } else if (address.county) {
        locationName += locationName ? `, ${address.county}` : address.county;
    }

    tempLocation.value = locationName || data.display_name.split(',')[0];

  } catch (error) {
    console.error("Błąd adresu:", error);
    tempLocation.value = `${lat.toFixed(5)}, ${lng.toFixed(5)}`;
  } finally {
    isLoadingMap.value = false;
  }
};

const searchLocation = async () => {
  if (!searchQuery.value) return;

  try {
    isLoadingMap.value = true;
    const response = await fetch(`https://nominatim.openstreetmap.org/search?format=json&q=${encodeURIComponent(searchQuery.value)}&limit=1`);
    const data = await response.json();

    if (data && data.length > 0) {
      const { lat, lon, display_name } = data[0];
      const latitude = parseFloat(lat);
      const longitude = parseFloat(lon);

      // Sprawdzenie czy mapa jest zainicjalizowana
      if (!mapInstance) {
        console.error('Map instance is not initialized');
        isLoadingMap.value = false;
        return;
      }

      updateMarker(latitude, longitude);
      mapInstance.setView([latitude, longitude], 15);

      // Ustawienie nazwy w inpucie
      const parts = display_name.split(',');
      tempLocation.value = parts.length > 1 ? `${parts[0]}, ${parts[1]}` : parts[0];
    }
  } catch (error) {
    console.error("Błąd wyszukiwania:", error);
  } finally {
    isLoadingMap.value = false;
  }
};

// --- ACTIONS ---
const close = () => {
  emit('close');
};

const confirm = () => {
  emit('confirm', tempLocation.value);
  close();
};

onBeforeUnmount(() => {
  destroyMap();
});
</script>

<style scoped>
/* Style Leaflet dla komponentu */
:deep(.leaflet-pane) { z-index: 10; }
:deep(.leaflet-top), :deep(.leaflet-bottom) { z-index: 20; }

/* Custom Marker (GPS User) */
:deep(.user-location-dot) {
  width: 18px;
  height: 18px;
  background-color: #4285F4;
  border: 3px solid white;
  border-radius: 50%;
  box-shadow: 0 1px 4px rgba(0,0,0,0.4);
  animation: pulse-ring 2s infinite;
}

@keyframes pulse-ring {
  0% { box-shadow: 0 0 0 0 rgba(66, 133, 244, 0.4); }
  70% { box-shadow: 0 0 0 10px rgba(66, 133, 244, 0); }
  100% { box-shadow: 0 0 0 0 rgba(66, 133, 244, 0); }
}

/* Dostosowanie Zoom Controls do stylu "Clean" */
:deep(.leaflet-control-zoom) {
  border: none !important;
  box-shadow: 0 2px 8px rgba(0,0,0,0.15) !important;
  margin-top: 16px !important;
  margin-left: 16px !important;
}
:deep(.leaflet-control-zoom-in),
:deep(.leaflet-control-zoom-out) {
  background-color: white !important;
  color: #5f6368 !important;
  border-bottom: 1px solid #f0f2f5 !important;
  width: 36px !important;
  height: 36px !important;
  line-height: 36px !important;
  font-size: 18px !important;
  font-weight: 400 !important;
  border-radius: 8px 8px 0 0 !important;
}
:deep(.leaflet-control-zoom-out) {
  border-radius: 0 0 8px 8px !important;
  border-bottom: none !important;
}
:deep(.leaflet-control-zoom-in:hover),
:deep(.leaflet-control-zoom-out:hover) {
  background-color: #f8f9fa !important;
  color: #1a73e8 !important;
}
</style>
