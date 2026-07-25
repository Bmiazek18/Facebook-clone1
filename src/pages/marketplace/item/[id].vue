<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import L from 'leaflet'
import 'leaflet/dist/leaflet.css'
// Import ikon
import Close from 'vue-material-design-icons/Close.vue'
import FacebookMessenger from 'vue-material-design-icons/FacebookMessenger.vue'
import Bookmark from 'vue-material-design-icons/Bookmark.vue'
import ShareVariant from 'vue-material-design-icons/ShareVariant.vue'
import DotsHorizontal from 'vue-material-design-icons/DotsHorizontal.vue'
import Speedometer from 'vue-material-design-icons/Speedometer.vue'
import CarShiftPattern from 'vue-material-design-icons/CarShiftPattern.vue'
import Palette from 'vue-material-design-icons/Palette.vue'

import MultiMediaLightbox from '@/components/chat/messageBox/MediaLightbox.vue'
import StoryShareModal from '@/components/feed/stories/StoryShareModal.vue'
import BaseModal from '@/components/common/BaseModal.vue'
import SellerModal from '@/components/marketplace/SellerModal.vue'
import NavbarRight from '@/layouts/Navbar/NavbarRight.vue'
import HoverScrollbar from '@/components/common/HoverScrollbar.vue'
import CustomTextarea from '~/components/common/CustomTextarea.vue'

// Importy Apollo GraphQL
import { useQuery } from '@vue/apollo-composable'
import gql from 'graphql-tag'

const route = useRoute()
const router = useRouter()

const isGalleryOpen = ref(true)
const currentImageIndex = ref(0)
const mapContainer = ref<HTMLElement | null>(null)
const isShareModalOpen = ref(false)
const isSellerModalOpen = ref(false)
let mapInstance: L.Map | null = null

const loadedItem = ref<any>(null)

// Zmienna do obsługi rozwijania opisu
const isDescriptionExpanded = ref(false)

// --- ZMIENNE DLA MODALA WIADOMOŚCI ---
const isMessageModalOpen = ref(false)
const messageText = ref('Czy ten przedmiot jest dostępny?')
const quickReplies = [
  'Czy dostępność nie uległa zmianie?',
  'Czy mogę zaplanować termin obejrzenia pojazdu w ramach rozm...',
  'Jaka jest historia pojazdu?',
  'Ilu było poprzednich właścicieli?'
]

const currentItem = computed(() => loadedItem.value)

// --- ZAPYTANIE APOLLO GRAPHQL ---
const GET_LISTING = gql`
  query GetListing($id: ID!) {
    getListing(id: $id) {
      id
      title
      price
      category
      condition
      description
      latitude
      longitude
      createdAt
    }
  }
`

const itemId = computed(() => route.params.id as string)

const { onResult, onError, loading } = useQuery(
  GET_LISTING,
  () => ({ id: itemId.value }),
  () => ({
    enabled: !!itemId.value,
  })
)

// Inicjalizacja i aktualizacja mapy
const updateMapWithItem = async (itemData: any) => {
  await nextTick()

  if (mapInstance && itemData?.coordinates) {
    const { lat, lng } = itemData.coordinates

    mapInstance.invalidateSize()
    mapInstance.setView([lat, lng], 13)

    L.circleMarker([lat, lng], {
      radius: 50,
      fillColor: '#3b82f6',
      fillOpacity: 0.2,
      color: '#3b82f6',
      weight: 2,
      opacity: 0.5,
    }).addTo(mapInstance)

    L.circleMarker([lat, lng], {
      radius: 6,
      fillColor: '#2563eb',
      fillOpacity: 1,
      color: '#ffffff',
      weight: 2,
    }).addTo(mapInstance)
  }
}

// Obsługa odpowiedzi GraphQL
onResult((res) => {
  const item = res.data?.getListing
  if (item) {
    loadedItem.value = {
      id: item.id,
      title: item.title,
      price: Number(item.price) === 0 ? 'BEZPŁATNE' : `PLN ${Number(item.price).toLocaleString()}`,
      location: 'Łęczyca, Polska',
      coordinates: { lat: item.latitude || 52.0689, lng: item.longitude || 19.3824 },
      description: item.description || 'Brak opisu.',
      images: [
        `https://picsum.photos/seed/${item.id}/800/600`,
        `https://picsum.photos/seed/${item.id}_2/800/600`,
      ],
      category: item.category === 'VEHICLES' ? 'Pojazdy' : item.category === 'ELECTRONICS' ? 'Elektronika' : item.category,
      condition: item.condition === 'NEW' ? 'Nowe' : 'Używane',
      details: {
        mileage: item.category === 'VEHICLES' ? '180,000 km' : undefined,
      },
      seller: {
        name: 'Bartosz Miazek',
        avatar: 'https://i.pravatar.cc/150?img=12',
        memberSince: '2015',
      },
      postedDate: 'Właśnie teraz',
    }

    updateMapWithItem(loadedItem.value)
  }
})

onError((err) => {
  console.error('Failed to fetch listing details via GraphQL:', err)
})

const mediaForGallery = computed(() => {
  return (
    currentItem.value?.images.map((imageUrl: string, index: number) => ({
      id: index,
      type: 'image' as const,
      imageUrl,
    })) || []
  )
})

const openShareModal = () => {
  isShareModalOpen.value = true
}

const handleShareComplete = () => {
  isShareModalOpen.value = false
}

const handleSendMessage = () => {
  console.log('Sending message:', messageText.value)
  isMessageModalOpen.value = false
  messageText.value = 'Czy ten przedmiot jest dostępny?'
}

onMounted(() => {
  if (mapContainer.value) {
    const defaultLat = 52.0689
    const defaultLng = 19.3824

    mapInstance = L.map(mapContainer.value, {
      center: [defaultLat, defaultLng],
      zoom: 13,
      zoomControl: false,
      dragging: false,
      scrollWheelZoom: false,
      doubleClickZoom: false,
      touchZoom: false,
    })

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>',
    }).addTo(mapInstance)

    if (loadedItem.value) {
      updateMapWithItem(loadedItem.value)
    }
  }
})

onUnmounted(() => {
  if (mapInstance) {
    mapInstance.remove()
    mapInstance = null
  }
})

const goBack = () => {
  router.push('/marketplace')
}

const openSellerModal = () => {
  isSellerModalOpen.value = true
}
</script>

<template>
  <div class="fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-80 overflow-y-auto">
    <div class="w-full relative flex flex-col lg:flex-row h-full rounded-lg bg-theme-bg-secondary overflow-hidden">
      <button
        @click="goBack"
        class="absolute top-4 left-4 p-2 text-white rounded-full hover:bg-white/10 z-[60] bg-black/30 backdrop-blur-sm transition-colors"
        aria-label="Zamknij"
      >
        <Close :size="24" fillColor="#FFFFFF" />
      </button>

      <!-- Stan ładowania -->
      <div v-if="loading" class="w-full h-full flex items-center justify-center absolute inset-0 z-[70] bg-theme-bg-secondary/90 backdrop-blur-sm">
        <span class="text-theme-text font-semibold">Ładowanie przedmiotu...</span>
      </div>

      <div class="w-full lg:flex-1 bg-black flex items-center justify-center relative min-h-[300px] lg:min-h-full">
        <MultiMediaLightbox
          v-if="isGalleryOpen && currentItem"
          v-model="isGalleryOpen"
          :media="mediaForGallery"
          :startIndex="currentImageIndex"
          :fullscreen="false"
          class="w-full h-full"
        />
      </div>

      <div class="w-[33%] flex flex-col bg-theme-bg-secondary border-l border-theme-border h-full max-h-full">
        <div class="w-full flex justify-end py-2 px-4 border-b border-theme-border shrink-0">
          <NavbarRight />
        </div>

        <div class="flex-1 overflow-hidden relative">
          <HoverScrollbar class="h-full overflow-y-auto">
            <div class="p-4 pb-10">
              <h1 class="text-2xl font-bold text-theme-text mb-1">
                {{ currentItem?.title || 'Brak tytułu' }}
              </h1>
              <div class="text-lg font-semibold text-theme-text mb-1">
                {{ currentItem?.price || '...' }}
              </div>
              <div class="text-xs text-theme-text-secondary mb-4">
                Opublikowano {{ currentItem?.postedDate || 'niedawno' }} w:
                {{ currentItem?.location?.split(',')[0] || '...' }}
              </div>

              <div class="flex gap-2 mb-6">
                <button
                  @click="isMessageModalOpen = true"
                  class="flex-1 flex items-center justify-center gap-2 bg-theme-bg-tertiary hover:bg-theme-hover text-black font-semibold py-2 px-4 rounded-md transition-colors"
                >
                  <FacebookMessenger :size="20" />
                  Wyślij wiadomość
                </button>
                <button class="bg-theme-bg hover:bg-theme-hover p-2 rounded-md text-black transition-colors">
                  <Bookmark :size="24" />
                </button>
                <button
                  @click="openShareModal"
                  class="bg-theme-bg hover:bg-theme-hover p-2 rounded-md text-black transition-colors"
                  title="Udostępnij"
                >
                  <ShareVariant :size="24" />
                </button>
                <button class="bg-theme-bg hover:bg-theme-hover p-2 rounded-md text-black transition-colors">
                  <DotsHorizontal :size="24" />
                </button>
              </div>

              <div class="mb-6" v-if="currentItem?.category === 'Pojazdy'">
                <h2 class="text-lg font-bold text-theme-text mb-3">Informacje o pojeździe</h2>
                <div class="space-y-3">
                  <div class="flex items-center gap-3 text-theme-text">
                    <Speedometer :size="20" class="text-theme-text-secondary" />
                    <span class="text-[15px]">Przebieg {{ currentItem?.details?.mileage || 'Brak danych' }}</span>
                  </div>
                  <div class="flex items-center gap-3 text-theme-text">
                    <CarShiftPattern :size="20" class="text-theme-text-secondary" />
                    <span class="text-[15px]">{{ currentItem?.details?.transmission || 'Brak danych' }}</span>
                  </div>
                  <div class="flex items-center gap-3 text-theme-text">
                    <Palette :size="20" class="text-theme-text-secondary" />
                    <span class="text-[15px]">Kolor karoserii: {{ currentItem?.details?.color || 'Brak danych' }}</span>
                  </div>
                </div>
              </div>

              <hr class="border-theme-border my-4" />

              <!-- SEKCJA OPISU SPRZEDAWCY -->
              <div class="mb-6">
                <h2 class="text-[20px] font-bold text-theme-text mb-3">Opis sprzedawcy</h2>
                <div class="text-[15px] text-theme-text leading-relaxed whitespace-pre-wrap break-words">
                  <template v-if="isDescriptionExpanded || (currentItem?.description?.length || 0) <= 150">
                    {{ currentItem?.description }}
                  </template>
                  <template v-else>
                    {{ currentItem?.description?.substring(0, 150) }}..
                  </template>
                </div>
                <button
                  v-if="(currentItem?.description?.length || 0) > 150 && !isDescriptionExpanded"
                  @click="isDescriptionExpanded = true"
                  class="font-bold text-theme-text mt-1 hover:underline text-[15px]"
                >
                  Wyświetl więcej
                </button>
              </div>

              <hr class="border-theme-border my-4" />

              <div class="mb-6">
                <div
                  ref="mapContainer"
                  class="rounded-lg overflow-hidden h-32 w-full relative bg-theme-bg mb-2 border border-theme-border z-0"
                ></div>

                <div class="font-bold text-theme-text text-[15px]">{{ currentItem?.location }}</div>
                <div class="text-xs text-theme-text-secondary">Lokacja jest przybliżona</div>
              </div>

              <hr class="border-theme-border my-4" />

              <div class="flex justify-between items-baseline mb-4">
                <h2 class="text-[20px] font-bold text-theme-text">Informacje o sprzedawcy</h2>
                <button class="text-theme-primary font-semibold text-[15px] hover:underline">
                  Informacje o sprzedawcy
                </button>
              </div>

              <div @click="openSellerModal" class="flex items-center gap-3 mb-3 cursor-pointer">
                <img
                  class="rounded-full w-12 h-12 object-cover bg-theme-bg"
                  :src="currentItem?.seller?.avatar || 'https://via.placeholder.com/150'"
                  alt="Seller avatar"
                />
                <div class="flex flex-col justify-center">
                  <div class="font-semibold text-[17px] text-theme-text">
                    {{ currentItem?.seller?.name || 'Wczytywanie...' }}
                  </div>
                </div>
              </div>

              <div @click="openSellerModal" class="flex items-center gap-3 mb-6 cursor-pointer">
                <div class="w-6 h-6 rounded-full bg-theme-text-secondary flex items-center justify-center shrink-0">
                  <Facebook :size="16" class="text-theme-text relative top-[1px]" />
                </div>
                <div class="text-[15px] text-theme-text">
                  Użytkownik Facebooka od {{ currentItem?.seller?.memberSince || '...' }}
                </div>
              </div>

              <button
                @click="isMessageModalOpen = true"
                class="w-full bg-theme-primary hover:bg-theme-primary-hover text-white font-bold py-2 px-4 rounded-lg transition-colors flex items-center justify-center gap-2 h-10"
              >
                <FacebookMessenger :size="20" />
                Wyślij wiadomość do sprzedawcy
              </button>
            </div>
          </HoverScrollbar>
        </div>
      </div>
    </div>

    <!-- Seller Modal -->
    <BaseModal
      v-if="isSellerModalOpen"
      @close="isSellerModalOpen = false"
      :title="currentItem?.seller?.name"
    >
      <SellerModal :profile="currentItem?.seller" @close="isSellerModalOpen = false" />
    </BaseModal>

    <!-- Story Share Modal -->
    <BaseModal v-if="isShareModalOpen" @close="isShareModalOpen = false">
      <StoryShareModal
        :marketplaceItem="
          currentItem
            ? {
                id: currentItem.id,
                title: currentItem.title,
                price: currentItem.price,
                location: currentItem.location,
                images: currentItem.images,
                description: currentItem.description,
              }
            : null
        "
        @close="handleShareComplete"
      />
    </BaseModal>

    <!-- Modal Wysyłania Wiadomości -->
    <BaseModal v-if="isMessageModalOpen" :title="'Wyślij wiadomość'" @close="isMessageModalOpen = false">
      <div class="w-[548px] max-w-[95vw] bg-theme-bg-secondary rounded-lg shadow-xl flex flex-col overflow-hidden">
        <div class="p-4 flex gap-4 items-center">
          <img
            :src="currentItem?.images?.[0]"
            alt="Miniatura pojazdu"
            class="w-16 h-16 rounded-xl object-cover border border-theme-border"
          />
          <div>
            <h3 class="font-bold text-theme-text text-lg leading-tight">{{ currentItem?.title }}</h3>
            <p class="text-theme-text-secondary text-sm">{{ currentItem?.price }}</p>
          </div>
        </div>

        <div class="p-4 pt-0">
          <div class="flex flex-wrap gap-2 mb-4">
            <button
              v-for="(reply, idx) in quickReplies"
              :key="idx"
              @click="messageText = reply"
              class="bg-theme-bg-tertiary hover:bg-theme-hover text-theme-text px-4 py-2 rounded-full text-[14px] font-medium transition-colors"
            >
              {{ reply }}
            </button>
          </div>

          <CustomTextarea
            v-model="messageText"
            label="Wpisz wiadomość do sprzedawcy"
          />

          <p class="text-[13px] text-theme-text-secondary mt-3">
            Nie udostępniaj adresu e-mail, numeru telefonu ani informacji finansowych.
          </p>
        </div>

        <div class="p-4 border-t border-theme-border flex justify-end items-center gap-4">
          <button
            @click="isMessageModalOpen = false"
            class="text-theme-primary font-semibold hover:underline px-4 py-2"
          >
            Anuluj
          </button>
          <button
            @click="handleSendMessage"
            class="bg-[#0084ff] hover:bg-[#0070d6] text-white font-semibold py-2 px-6 rounded-lg flex items-center justify-center gap-2 transition-colors"
          >
            <FacebookMessenger :size="20" fillColor="#FFFFFF" />
            Wyślij wiadomość
          </button>
        </div>
      </div>
    </BaseModal>
  </div>
</template>
