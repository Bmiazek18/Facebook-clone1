<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';
// Import ikon - dodano nowe ikony potrzebne do odwzorowania UI
import Close from 'vue-material-design-icons/Close.vue';
import MapMarker from 'vue-material-design-icons/MapMarker.vue';
import FacebookMessenger from 'vue-material-design-icons/FacebookMessenger.vue';
import Bookmark from 'vue-material-design-icons/Bookmark.vue';
import ShareVariant from 'vue-material-design-icons/ShareVariant.vue';
import DotsHorizontal from 'vue-material-design-icons/DotsHorizontal.vue';
import Speedometer from 'vue-material-design-icons/Speedometer.vue';
import CarShiftPattern from 'vue-material-design-icons/CarShiftPattern.vue'; // Jako skrzynia biegów
import Palette from 'vue-material-design-icons/Palette.vue';
import MultiMediaLightbox from '@/components/MessageBox/MediaLightbox.vue';
import StoryShareModal from '@/components/stories/StoryShareModal.vue';
import BaseModal from '@/components/common/BaseModal.vue';
import SellerModal from '@/components/marketplace/SellerModal.vue';
import NavbarRight from '@/Layouts/Navbar/NavbarRight.vue';
import HoverScrollbar from '@/components/common/HoverScrollbar.vue';

const route = useRoute();
const router = useRouter();

// Mock data
const items = [
  {
    id: '1',
    title: '2001 BMW x3',
    price: 'PLN 6,500',
    location: 'Warszawa, Polska',
    coordinates: { lat: 52.2297, lng: 21.0122 }, // Warszawa centrum
    description: 'Sprzedam gruza BMW e46 2.0 LPG\nZimowy wojownik\nBlacha jak na zdjęciu\nSilnik na benzynie i gazie dobrze chodzi\nZawieszenie do wymiany łączniki stabilizatora\nPoprzedni właściciel robił swap z 1.9 na 2.0r6',
    images: [
      'https://placehold.co/800x600/555/FFF?text=BMW+X3+1',
      'https://placehold.co/800x600/666/FFF?text=BMW+X3+2',
      'https://placehold.co/800x600/777/FFF?text=BMW+X3+3',
    ],
    category: 'Pojazdy',
    condition: 'Używane',
    details: {
      mileage: '295,000 km',
      transmission: 'Ręczna skrzynia biegów',
      color: 'Grey'
    },
    seller: {
      name: 'Paweł Ja',
      avatar: 'https://i.pravatar.cc/150?img=12',
      memberSince: '2020',
    },
    postedDate: '2 dni temu',
  },
  {
    id: '2',
    title: 'Sukienki nowe z metkami',
    price: 'BEZPŁATNE',
    location: 'Mielec, Polska',
    coordinates: { lat: 50.2873, lng: 21.4243 }, // Mielec
    description: 'Sukienki damskie rozmiar S/M. Nowe z metkami, nigdy nie noszone. Odbiór osobisty.',
    images: [
      'https://placehold.co/800x600/834/FFF?text=Sukienka+1',
      'https://placehold.co/800x600/945/FFF?text=Sukienka+2',
    ],
    category: 'Odzież',
    condition: 'Nowe',
    details: {
      size: 'S/M',
      brand: 'Różne marki'
    },
    seller: {
      name: 'Anna Nowak',
      avatar: 'https://i.pravatar.cc/150?img=5',
      memberSince: '2019',
    },
    postedDate: '1 dzień temu',
  },
  {
    id: '3',
    title: '2014 Kia ceed',
    price: 'PLN 34,499',
    location: 'Kraków, Polska',
    coordinates: { lat: 50.0647, lng: 19.9450 }, // Kraków
    description: 'Kia Ceed 2014, przebieg 180 000 km. Pierwszy właściciel, serwisowana w ASO. Bezwypadkowa.',
    images: [
      'https://placehold.co/800x600/D22/FFF?text=Kia+Ceed+1',
      'https://placehold.co/800x600/E33/FFF?text=Kia+Ceed+2',
      'https://placehold.co/800x600/F44/FFF?text=Kia+Ceed+3',
      'https://placehold.co/800x600/055/FFF?text=Kia+Ceed+4',
    ],
    category: 'Pojazdy',
    condition: 'Używane',
    details: {
      mileage: '180,000 km',
      transmission: 'Automatyczna',
      color: 'Czerwony',
      year: '2014'
    },
    seller: {
      name: 'Piotr Wiśniewski',
      avatar: 'https://i.pravatar.cc/150?img=8',
      memberSince: '2021',
    },
    postedDate: '5 dni temu',
  },
];

const isGalleryOpen = ref(true);
const currentImageIndex = ref(0);
const mapContainer = ref<HTMLElement | null>(null);
const isShareModalOpen = ref(false);
const isSellerModalOpen = ref(false);
let mapInstance: L.Map | null = null;

const currentItem = computed(() => {
  const itemId = route.params.id as string;
  // Fallback dla demo, żeby pokazać BMW jeśli ID nie pasuje lub jest puste
  return items.find(item => item.id === itemId) || items[0];
});

const mediaForGallery = computed(() => {
  return currentItem.value?.images.map((imageUrl, index) => ({
    id: index,
    type: 'image' as const,
    imageUrl,
  })) || [];
});

const openShareModal = () => {
  isShareModalOpen.value = true;
};

const handleShareComplete = () => {
  // Modal zamknie się automatycznie po udostępnieniu
  // StoryShareModal sam obsługuje dodawanie posta do store
  isShareModalOpen.value = false;
};

onMounted(() => {
  if (mapContainer.value && currentItem.value?.coordinates) {
    const { lat, lng } = currentItem.value.coordinates;

    mapInstance = L.map(mapContainer.value, {
      center: [lat, lng],
      zoom: 13,
      zoomControl: false,
      dragging: false,
      scrollWheelZoom: false,
      doubleClickZoom: false,
      touchZoom: false,
    });

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>',
    }).addTo(mapInstance);

     L.circleMarker([lat, lng], {
      radius: 50,
      fillColor: '#3b82f6',
      fillOpacity: 0.2,
      color: '#3b82f6',
      weight: 2,
      opacity: 0.5,
    }).addTo(mapInstance);

    L.circleMarker([lat, lng], {
      radius: 6,
      fillColor: '#2563eb',
      fillOpacity: 1,
      color: '#ffffff',
      weight: 2,
    }).addTo(mapInstance);
  }
});

onUnmounted(() => {
  if (mapInstance) {
    mapInstance.remove();
    mapInstance = null;
  }
});

const goBack = () => {
  router.push('/marketplace');
};

const openSellerModal = () => {
  isSellerModalOpen.value = true;
};
</script>

<template>
  <div
    class="fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-80 overflow-y-auto"
  >
    <div class="w-full relative flex flex-col lg:flex-row h-full rounded-lg bg-white overflow-hidden">

      <button
        @click="goBack"
        class="absolute top-4 left-4 p-2 text-white rounded-full hover:bg-white/10 z-[60] bg-black/30 backdrop-blur-sm transition-colors"
        aria-label="Zamknij"
      >
        <Close :size="24" fillColor="#FFFFFF" />
      </button>

      <div class="w-full lg:flex-1 bg-black flex items-center justify-center relative min-h-[300px] lg:min-h-full">
         <MultiMediaLightbox
            v-if="isGalleryOpen"
            v-model="isGalleryOpen"
            :media="mediaForGallery"
            :startIndex="currentImageIndex"
            :fullscreen="false"
            class="w-full h-full"
        />
      </div>

      <div
        class="w-full lg:w-[360px] xl:w-[400px] flex flex-col bg-white border-l border-gray-200 h-full max-h-full"
      >
        <div class="w-full flex justify-end py-2 px-4 border-b border-gray-200 shrink-0">
          <NavbarRight />
        </div>

        <div class="flex-1 overflow-hidden relative">
            <HoverScrollbar class="h-full overflow-y-auto">
              <div class="p-4 pb-10">

                <h1 class="text-2xl font-bold text-gray-900 mb-1">
                  {{ currentItem.title }}
                </h1>
                <div class="text-lg font-semibold text-gray-900 mb-1">
                    {{ currentItem.price }}
                </div>
                <div class="text-xs text-gray-500 mb-4">
                  Opublikowano {{ currentItem.postedDate }} w: {{ currentItem.location.split(',')[0] }}
                </div>

                <div class="flex gap-2 mb-6">
                    <button class="flex-1 flex items-center justify-center gap-2 bg-blue-100 hover:bg-blue-200 text-blue-700 font-semibold py-2 px-4 rounded-md transition-colors">
                        <FacebookMessenger :size="20" />
                        Wyślij wiadomość
                    </button>
                    <button class="bg-gray-100 hover:bg-gray-200 p-2 rounded-md text-gray-700 transition-colors">
                        <Bookmark :size="24" />
                    </button>
                    <button
                      @click="openShareModal"
                      class="bg-gray-100 hover:bg-gray-200 p-2 rounded-md text-gray-700 transition-colors"
                      title="Udostępnij"
                    >
                        <ShareVariant :size="24" />
                    </button>
                    <button class="bg-gray-100 hover:bg-gray-200 p-2 rounded-md text-gray-700 transition-colors">
                        <DotsHorizontal :size="24" />
                    </button>
                </div>

                <div class="mb-6">
                    <h2 class="text-lg font-bold text-gray-900 mb-3">Informacje o pojeździe</h2>
                    <div class="space-y-3">
                        <div class="flex items-center gap-3 text-gray-900">
                            <Speedometer :size="20" class="text-gray-500" />
                            <span class="text-[15px]">Przebieg {{ currentItem.details?.mileage }}</span>
                        </div>
                        <div class="flex items-center gap-3 text-gray-900">
                            <CarShiftPattern :size="20" class="text-gray-500" />
                            <span class="text-[15px]">{{ currentItem.details?.transmission }}</span>
                        </div>
                        <div class="flex items-center gap-3 text-gray-900">
                            <Palette :size="20" class="text-gray-500" />
                            <span class="text-[15px]">Kolor karoserii: {{ currentItem.details?.color }}</span>
                        </div>
                    </div>
                </div>

                <hr class="border-gray-200 my-4" />

                <div class="mb-6">
                  <h2 class="text-lg font-bold text-gray-900 mb-2">Opis sprzedawcy</h2>
                  <p class="text-gray-900 text-[15px] whitespace-pre-wrap leading-relaxed">
                    {{ currentItem.description }}
                  </p>
                  <button class="text-gray-900 font-semibold text-[15px] mt-2 hover:underline">Wyświetl mniej</button>
                </div>

                <div class="mb-6">
                    <div
                      ref="mapContainer"
                      class="rounded-lg overflow-hidden h-32 w-full relative bg-gray-100 mb-2 border border-gray-200"
                    >
                    </div>
                    <div class="font-bold text-gray-900 text-[15px]">{{ currentItem.location }}</div>
                    <div class="text-xs text-gray-500">Lokacja jest przybliżona</div>
                </div>

                <hr class="border-gray-200 my-4" />

               <div class="flex justify-between items-baseline mb-4">
                      <h2 class="text-[20px] font-bold text-gray-900">Informacje o sprzedawcy</h2>
                      <button class="text-[#0064d1] font-semibold text-[15px] hover:underline">
                        Informacje o sprzedawcy
                      </button>
                  </div>

                  <div @click="openSellerModal" class="flex items-center gap-3 mb-3 cursor-pointer">
                    <img
                      class="rounded-full w-12 h-12 object-cover bg-gray-200"
                      :src="currentItem.seller.avatar"
                      alt="Seller avatar"
                    >
                    <div class="flex flex-col justify-center">
                      <div class="font-semibold text-[17px] text-gray-900">
                        {{ currentItem.seller.name }}
                      </div>
                    </div>
                  </div>

                  <div @click="openSellerModal" class="flex items-center gap-3 mb-6 cursor-pointer">
                    <div class="w-6 h-6 rounded-full bg-gray-500 flex items-center justify-center shrink-0">
                         <Facebook :size="16" class="text-white relative top-[1px]" />
                    </div>
                    <div class="text-[15px] text-gray-900">
                        Użytkownik Facebooka od {{ currentItem.seller.memberSince }}
                    </div>
                  </div>

                  <button class="w-full bg-[#0064d1] hover:bg-[#0055b3] text-white font-bold py-2 px-4 rounded-lg transition-colors flex items-center justify-center gap-2 h-10">
                     <FacebookMessenger :size="20" />
                     Wyślij wiadomość do sprzedawcy
                  </button>
                </div>
            </HoverScrollbar>
        </div>
      </div>
    </div>

    <!-- Story Share Modal -->
    <!-- Seller Modal -->
    <BaseModal v-if="isSellerModalOpen" @close="isSellerModalOpen = false" :title="currentItem?.seller?.name">
      <SellerModal :profile="currentItem?.seller" @close="isSellerModalOpen = false" />
    </BaseModal>

    <BaseModal v-if="isShareModalOpen" @close="isShareModalOpen = false">
      <StoryShareModal
        :marketplaceItem="currentItem ? {
          id: currentItem.id,
          title: currentItem.title,
          price: currentItem.price,
          location: currentItem.location,
          images: currentItem.images,
          description: currentItem.description
        } : null"
        @close="handleShareComplete"
      />
    </BaseModal>
  </div>
</template>
