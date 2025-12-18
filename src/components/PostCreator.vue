<script setup lang="ts">
import { ref, computed, nextTick, watch, onUnmounted } from 'vue';

// --- IKONY ---
import LockIcon from 'vue-material-design-icons/Lock.vue';
import ChevronDownIcon from 'vue-material-design-icons/ChevronDown.vue';
import FormatColorTextIcon from 'vue-material-design-icons/FormatColorText.vue';
import EmoticonHappyIcon from 'vue-material-design-icons/EmoticonHappy.vue';
import ImageMultipleIcon from 'vue-material-design-icons/ImageMultiple.vue';
import AccountGroupIcon from 'vue-material-design-icons/AccountGroup.vue';
import EmoticonIcon from 'vue-material-design-icons/Emoticon.vue';
import MapMarkerIcon from 'vue-material-design-icons/MapMarker.vue';
import EarthIcon from 'vue-material-design-icons/Earth.vue';
import DotsHorizontalIcon from 'vue-material-design-icons/DotsHorizontal.vue';
import PencilIcon from 'vue-material-design-icons/Pencil.vue';
import CloseIcon from 'vue-material-design-icons/Close.vue';

// --- LEAFLET (MAPA) ---
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';

// --- TYPY ---
import type { PostData } from '@/types/StoryElement';
import type { User } from '@/data/users';

// Interfejs zgodny z Twoim JSON-em:
// { "title": "Gdańsk", "subtitle": "Twoja obecna lokalizacja", "type": "city", "lat": "54.3706858", "lon": "18.6129831" }
export interface Location {
  title: string;
  subtitle: string;
  type: string;
  lat: string;
  lon: string;
}

const props = defineProps<{
  sharedPost?: PostData | null;
  taggedUsers?: User[];
  selectedLocation?: Location | null;
}>();

const emit = defineEmits<{
  (e: 'navigate', viewName: string): void;
  (e: 'back'): void;
  (e: 'publish', content: string): void;
  (e: 'close'): void;
  (e: 'updateHeight'): void;
  (e: 'openTagUsers'): void;
  (e: 'openLocation'): void;
  (e: 'removeLocation'): void;
}>();

// --- STAN ---
const userName = ref('Bartosz Miazek');
const profilePicUrl = ref('https://i.pravatar.cc/150?img=12');
const postContent = ref('');
const selectedImage = ref<string | null>(null);
const fileInput = ref<HTMLInputElement | null>(null);

// Referencje do mapy
const mapContainer = ref<HTMLElement | null>(null);
let mapInstance: L.Map | null = null;

const isPublishButtonDisabled = computed(() => {
  if (props.sharedPost) return false;
  return !postContent.value.trim() && !selectedImage.value && !props.selectedLocation;
});

const locationTypeLabel = computed(() => {
  if (!props.selectedLocation?.type) return 'MIEJSCE';
  const type = props.selectedLocation.type.toLowerCase();
  if (type === 'city') return 'MIEJSCOWOŚĆ';
  if (type === 'country') return 'KRAJ';
  return type.toUpperCase();
});

// --- LOGIKA MAPY (LEAFLET) ---
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

    // @ts-ignore
    if (container._leaflet_id) {
      // @ts-ignore
      container._leaflet_id = null;
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
  }, 200);
};

watch(() => props.selectedLocation, (newVal) => {
  if (newVal) {
    initMap();
    emit('updateHeight');
  }
}, { immediate: true, deep: true });

onUnmounted(() => {
  if (mapInstance) {
    mapInstance.remove();
    mapInstance = null;
  }
});

// --- OBSŁUGA INTERFEJSU ---
const openPrivacySelector = () => emit('navigate', 'privacy');
const handleImageClick = () => fileInput.value?.click();

const handleImageSelect = (event: Event) => {
  const target = event.target as HTMLInputElement;
  const file = target.files?.[0];
  if (file && file.type.startsWith('image/')) {
    const reader = new FileReader();
    reader.onload = async (e) => {
      selectedImage.value = e.target?.result as string;
      await nextTick();
      emit('updateHeight');
    };
    reader.readAsDataURL(file);
  }
};

const removeImage = () => {
  selectedImage.value = null;
  if (fileInput.value) fileInput.value.value = '';
  nextTick(() => emit('updateHeight'));
};

const removeLocation = () => {
  emit('removeLocation');
};

const handlePublish = () => {
  emit('publish', postContent.value);
  emit('close');
  postContent.value = '';
  selectedImage.value = null;
};
</script>

<template>
  <div class="post-creator-card p-0 min-h-[200px]">

    <div class="flex items-center mb-4">
      <img :src="profilePicUrl" :alt="userName" class="w-10 h-10 rounded-full mr-3 object-cover" />
      <div class="flex flex-col">
        <div class="text-[15px] leading-tight mb-1 text-gray-900">
          <span class="font-bold">{{ userName }}</span>

          <template v-if="props.selectedLocation">
             jest w: <span class="font-bold">{{ props.selectedLocation.title }}</span>
          </template>

          <template v-if="props.taggedUsers && props.taggedUsers.length">
            <span class="font-normal text-gray-600"> z: </span>
            <span class="font-bold">
              <template v-for="(user, idx) in props.taggedUsers" :key="user.id">
                <span v-if="idx > 0">, </span>
                {{ user.name }}
              </template>
            </span>
          </template>
        </div>

        <div
            class="flex items-center bg-gray-200 px-2 py-0.5 rounded-md text-xs font-semibold text-gray-700 w-fit cursor-pointer hover:bg-gray-300 transition-colors"
            @click="openPrivacySelector"
        >
          <lock-icon :size="12" class="mr-1" />
          <span>Tylko ja</span>
          <chevron-down-icon :size="12" class="ml-1" />
        </div>
      </div>
    </div>

    <div class="relative mb-2">
      <textarea
        v-model="postContent"
        :placeholder="sharedPost ? 'Powiedz coś o tym...' : (props.selectedLocation ? 'O czym myślisz, Bartosz?' : 'Co słychać?')"
        class="w-full min-h-[60px] border-none resize-none text-2xl placeholder-gray-500 focus:ring-0 focus:outline-none p-0 pt-2"
        :class="{'text-base': postContent.length > 80}"
      ></textarea>

      <div class="absolute bottom-2 left-0 text-[#fe5b70] cursor-pointer" title="Stylizacja tekstu">
         <format-color-text-icon :size="24" />
      </div>
      <div class="absolute bottom-2 right-0 text-gray-500 cursor-pointer" title="Dodaj emoji">
        <emoticon-happy-icon :size="24" />
      </div>
    </div>

    <div v-if="props.selectedLocation" class="relative mb-4 border border-gray-300 rounded-xl overflow-hidden shadow-sm bg-gray-100">

        <div
          @click="removeLocation"
          class="absolute top-2 right-2 z-[401] bg-white w-8 h-8 rounded-full flex items-center justify-center cursor-pointer shadow-md hover:bg-gray-100 transition"
        >
          <close-icon :size="18" class="text-gray-600" />
        </div>

        <div ref="mapContainer" id="my-map-container" class="h-44 w-full z-0 bg-gray-200 relative"></div>

        <div class="bg-[#f0f2f5] p-3 flex items-center border-t border-gray-200">
            <div class="w-10 h-10 rounded-full bg-[#f3425f] flex items-center justify-center flex-shrink-0 text-white mr-3">
                <map-marker-icon :size="24" />
            </div>
            <div class="flex flex-col overflow-hidden">
                <span class="font-semibold text-gray-500 text-[11px] uppercase tracking-wide truncate">
                  {{ locationTypeLabel }}
                </span>
                <span class="font-bold text-gray-900 text-[15px] leading-tight truncate">
                  {{ props.selectedLocation.title }}
                </span>
                <span class="text-xs text-gray-500 truncate mt-0.5">
                  {{ props.selectedLocation.subtitle }}
                </span>
            </div>
        </div>
    </div>

    <div v-if="selectedImage" class="relative mb-4 bg-gray-100 rounded-lg overflow-hidden border border-gray-200">
      <img :src="selectedImage" class="w-full max-h-60 object-cover" />
      <div class="absolute top-2 left-2 bg-white flex items-center gap-1 px-2 py-1 rounded shadow text-blue-600 text-sm font-medium cursor-pointer hover:bg-gray-50">
        <PencilIcon :size="16" class="mr-1" />
        Edytuj
      </div>
      <button
        @click="removeImage"
        class="absolute top-2 right-2 bg-white text-gray-700 p-1.5 rounded-full shadow hover:bg-gray-100 transition"
      >
        <CloseIcon :size="20" />
      </button>
    </div>

    <input ref="fileInput" type="file" accept="image/*" class="hidden" @change="handleImageSelect" />

    <div v-if="sharedPost" class="mb-4 border border-gray-200 rounded-lg overflow-hidden">
      <img v-if="sharedPost.imageUrl" :src="sharedPost.imageUrl" class="w-full h-48 object-cover" />
      <div class="p-3 bg-gray-50">
        <div class="flex items-center gap-2 mb-2">
          <img :src="sharedPost.authorAvatar" class="w-8 h-8 rounded-full object-cover" />
          <div>
            <p class="font-semibold text-gray-900 text-sm">{{ sharedPost.authorName }}</p>
            <div class="flex items-center gap-1 text-xs text-gray-500">
              <earth-icon :size="10" />
              <span>Publiczny</span>
            </div>
          </div>
        </div>
        <p class="text-gray-800 text-sm line-clamp-3">{{ sharedPost.content }}</p>
      </div>
    </div>

    <hr class="my-4 border-gray-200">

    <div class="flex justify-between items-center p-3 border border-gray-300 rounded-lg mb-4 shadow-sm">
      <span class="font-medium text-sm text-gray-700">Dodaj do posta</span>
      <div class="flex space-x-1 sm:space-x-3">
        <image-multiple-icon :size="24" class="text-[#45bd62] cursor-pointer hover:bg-gray-100 p-0.5 rounded transition" @click="handleImageClick" title="Zdjęcie/film" />
        <account-group-icon :size="24" class="text-[#1877f2] cursor-pointer hover:bg-gray-100 p-0.5 rounded transition" @click="emit('openTagUsers')" title="Oznacz znajomych" />
        <emoticon-icon :size="24" class="text-[#f7b928] cursor-pointer hover:bg-gray-100 p-0.5 rounded transition" title="Nastrój/aktywność" />
        <map-marker-icon :size="24" class="text-[#f3425f] cursor-pointer hover:bg-gray-100 p-0.5 rounded transition" @click="emit('openLocation')" title="Lokalizacja" />
        <div class="bg-[#1877f2] text-white text-[10px] font-bold px-1 rounded flex items-center cursor-pointer hover:opacity-90">GIF</div>
        <dots-horizontal-icon :size="24" class="text-gray-500 cursor-pointer hover:bg-gray-100 p-0.5 rounded transition" />
      </div>
    </div>

    <button
      :disabled="isPublishButtonDisabled"
      class="w-full py-2 rounded-lg font-bold text-base transition-colors duration-200"
      :class="{
        'bg-[#1877f2] text-white hover:bg-blue-700': !isPublishButtonDisabled,
        'bg-gray-200 text-gray-400 cursor-not-allowed': isPublishButtonDisabled
      }"
      @click="handlePublish"
    >
      Opublikuj
    </button>
  </div>
</template>

<style scoped>
.line-clamp-3 {
  display: -webkit-box;
  -webkit-line-clamp: 3;
  line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

/* Fix CSS dla Leaflet: ustawienie z-index na 0, żeby popupy Vue były nad mapą */
:deep(.leaflet-container) {
    z-index: 1111;
    font-family: inherit;
    background-color: #e5e7eb; /* Tło zanim załadują się kafelki */
}
:deep(.leaflet-pane) {
    z-index: 1111;
}
:deep(.leaflet-top), :deep(.leaflet-bottom) {
    z-index: 1111;
}
</style>
