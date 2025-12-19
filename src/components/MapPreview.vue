<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount, watch, nextTick, computed } from 'vue';
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';
import MapMarkerIcon from 'vue-material-design-icons/MapMarker.vue';
import CloseIcon from 'vue-material-design-icons/Close.vue';


export interface Location {
  title: string;
  subtitle: string;
  type: string;
  lat: string;
  lon: string;
}

const props = defineProps<{
  selectedLocation?: Location | null;
}>();

const emit = defineEmits<{
  (e: 'removeLocation'): void;
  (e: 'loaded'): void;
}>();

const mapContainer = ref<HTMLElement | null>(null);
let mapInstance: L.Map | null = null;

const locationTypeLabel = computed(() => {
  if (!props.selectedLocation?.type) return 'MIEJSCE';
  const type = props.selectedLocation.type.toLowerCase();
  if (type === 'city') return 'MIEJSCOWOŚĆ';
  if (type === 'country') return 'KRAJ';
  return type.toUpperCase();
});

const initMap = async () => {
  if (!props.selectedLocation) return;
  const lat = parseFloat(props.selectedLocation.lat);
  const lng = parseFloat(props.selectedLocation.lon);
  if (isNaN(lat) || isNaN(lng)) return;

  if (mapInstance) {
    mapInstance.remove();
    mapInstance = null;
  }

  await nextTick();

  setTimeout(() => {
    const container = mapContainer.value;
    if (!container) return;

    // clear any existing leaflet id so Leaflet can re-initialize on this element
    const anyContainer = container as unknown as { _leaflet_id?: unknown };
    if (anyContainer._leaflet_id) {
      anyContainer._leaflet_id = undefined;
    }

    mapInstance = L.map(container, {
      center: [lat, lng],
      zoom: 13,
      zoomControl: false,
      attributionControl: false,
      dragging: false,
      scrollWheelZoom: false,
      doubleClickZoom: false
    });

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png').addTo(mapInstance);

    const defaultIcon = L.icon({
      iconUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon.png',
      shadowUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-shadow.png',
      iconSize: [25, 41],
      iconAnchor: [12, 41],
      popupAnchor: [1, -34]
    });

    L.marker([lat, lng], { icon: defaultIcon }).addTo(mapInstance);

    mapInstance.invalidateSize();
    emit('loaded');
  }, 200);
};

watch(() => props.selectedLocation, (newVal) => {
  if (newVal) {
    initMap();
    emit('loaded');
  }
}, { immediate: true, deep: true });

onMounted(() => {
  initMap();
});

onBeforeUnmount(() => {
  if (mapInstance) {
    mapInstance.remove();
    mapInstance = null;
  }
});
</script>

<template>
  <div v-if="props.selectedLocation" class="relative mb-4 border border-gray-300 rounded-xl overflow-hidden shadow-sm bg-gray-100">
    <div @click="$emit('removeLocation')" class="absolute top-2 right-2 z-50 bg-white w-8 h-8 rounded-full flex items-center justify-center cursor-pointer shadow-md hover:bg-gray-100 transition">
      <CloseIcon :size="18" class="text-gray-600" />
    </div>

    <div ref="mapContainer" class="h-44 w-full z-0 bg-gray-200 relative"></div>

    <div class="bg-[#f0f2f5] p-3 flex items-center border-t border-gray-200">
      <div class="w-10 h-10 rounded-full bg-[#f3425f] flex items-center justify-center shrink-0 text-white mr-3">
        <MapMarkerIcon :size="24" />
      </div>
      <div class="flex flex-col overflow-hidden">
        <span class="font-semibold text-gray-500 text-[11px] uppercase tracking-wide truncate">
          {{ locationTypeLabel }}
        </span>
        <span class="font-bold text-gray-900 text-[15px] leading-tight truncate">
          {{ props.selectedLocation?.title }}
        </span>
        <span class="text-xs text-gray-500 truncate mt-0.5">
          {{ props.selectedLocation?.subtitle }}
        </span>
      </div>
    </div>
  </div>
</template>

<style scoped>
:deep(.leaflet-container) {
    z-index: 1111;
    font-family: inherit;
    background-color: #e5e7eb;
}
:deep(.leaflet-pane) {
    z-index: 1111;
}
:deep(.leaflet-top), :deep(.leaflet-bottom) {
    z-index: 1111;
}
</style>
