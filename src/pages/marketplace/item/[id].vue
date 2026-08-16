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
import Fuel from 'vue-material-design-icons/GasStation.vue'
import PlayCircle from 'vue-material-design-icons/PlayCircle.vue'
import Facebook from 'vue-material-design-icons/Facebook.vue'

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

  if (!mapContainer.value) return

  if (!mapInstance) {
    mapInstance = L.map(mapContainer.value, {
      zoomControl: false,
      dragging: false,
      scrollWheelZoom: false,
      doubleClickZoom: false,
      touchZoom: false,
    })

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>',
    }).addTo(mapInstance)
  }

  if (itemData?.coordinates) {
    const { lat, lng } = itemData.coordinates

    mapInstance.invalidateSize()
    mapInstance.setView([lat, lng], 13)

    L.circleMarker([lat, lng], {
      radius: 40,
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

onResult((res) => {
  const item = res.data?.getListing
  if (item) {
    loadedItem.value = {
      id: item.id,
      title: item.title,
      price: Number(item.price) === 0 ? 'BEZPŁATNE' : `PLN ${Number(item.price).toLocaleString()}`,
      location: 'Radom, Polska',
      coordinates: { lat: item.latitude || 51.4027, lng: item.longitude || 21.1471 },
      description: item.description || 'Brak opisu.',
      images: [
        `https://picsum.photos/seed/${item.id}/800/600`,
        `https://picsum.photos/seed/${item.id}_2/800/600`,
      ],
      category: item.category === 'VEHICLES' ? 'Pojazdy' : item.category,
      condition: item.condition === 'NEW' ? 'Nowe' : 'Używane',
      details: {
        mileage: '380 km',
        transmission: 'Ręczna skrzynia biegów',
        color: 'Black · Kolor wnętrza: Black',
        fuel: 'Diesel',
      },
      seller: {
        name: 'Igor Kucharski',
        avatar: 'https://placehold.co/150',
        memberSince: '2017',
      },
      postedDate: '3 dni temu',
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

      <!-- Przycisk powrotu -->
      <button
        @click="goBack"
        class="absolute top-4 left-4 p-2 text-white rounded-full hover:bg-white/10 z-[60] bg-black/30 backdrop-blur-sm transition-colors"
        aria-label="Zamknij"
      >
        <Close :size="24" fillColor="#FFFFFF" />
      </button>

      <!-- Lewa część z galerią lub szkieletem -->
      <div
        class="w-full lg:flex-1 flex items-center justify-center relative min-h-[300px] lg:min-h-full"
        :class="loading ? 'bg-[#303030]' : 'bg-black'"
      >
        <MultiMediaLightbox
          v-if="!loading && isGalleryOpen && currentItem"
          v-model="isGalleryOpen"
          :media="mediaForGallery"
          :startIndex="currentImageIndex"
          :fullscreen="false"
          class="w-full h-full"
        />
      </div>

      <!-- Prawa część z opisem -->
      <div class="w-[38%] min-w-[420px] flex flex-col bg-theme-bg-secondary border-l border-theme-border h-full max-h-full">

        <!-- Górny Navbar -->
        <div class="w-full flex justify-end py-2 px-4 border-b border-theme-border shrink-0 bg-white">
          <NavbarRight v-if="!loading" />
          <div v-else class="h-10 w-full flex justify-end items-center animate-pulse">
            <div class="h-10 w-10 bg-gray-200 rounded-full"></div>
          </div>
        </div>

        <!-- SKELETON ŁADOWANIA -->
        <div v-if="loading" class="flex-1 flex flex-col bg-white p-4 animate-pulse">
          <div class="h-4 w-[70%] bg-gray-200 rounded mb-2"></div>
          <div class="h-6 w-[40%] bg-gray-200 rounded mb-4"></div>
          <div class="h-10 w-full bg-gray-200 rounded mb-6"></div>
        </div>

        <!-- PRAWDZIWA TREŚĆ -->
        <div v-else class="flex-1 overflow-hidden relative bg-white">
          <HoverScrollbar class="h-full overflow-y-auto">
            <div class="p-4 pb-16">

              <!-- Tytuł i cena -->
              <h1 class="text-2xl font-bold text-gray-900 mb-0.5">
                {{ currentItem?.title }}
              </h1>
              <div class="text-xl font-bold text-gray-900 mb-1">
                {{ currentItem?.price }}
              </div>
              <div class="text-[13px] text-gray-500 mb-4">
                Opublikowano {{ currentItem?.postedDate }} w: {{ currentItem?.location }}
              </div>

              <!-- Przyciski akcji (Wiadomość, Zakładka, Udostępnij, Opcje) -->
              <div class="flex items-center gap-2 mb-6">
                <button
                  @click="isMessageModalOpen = true"
                  class="flex-1 flex items-center justify-center gap-2 bg-[#E4E6EB] hover:bg-[#D8DADF] text-gray-900 font-semibold py-2.5 px-4 rounded-lg transition-colors text-[15px]"
                >
                  <FacebookMessenger :size="20" />
                  Wyślij wiadomość
                </button>
                <button class="bg-[#E4E6EB] hover:bg-[#D8DADF] p-2.5 rounded-lg text-gray-900 transition-colors">
                  <Bookmark :size="20" />
                </button>
                <button
                  @click="openShareModal"
                  class="bg-[#E4E6EB] hover:bg-[#D8DADF] p-2.5 rounded-lg text-gray-900 transition-colors"
                >
                  <ShareVariant :size="20" />
                </button>
                <button class="bg-[#E4E6EB] hover:bg-[#D8DADF] p-2.5 rounded-lg text-gray-900 transition-colors">
                  <DotsHorizontal :size="20" />
                </button>
              </div>

              <!-- Informacje o pojeździe (Grid 2 kolumny) -->
              <div class="mb-6" v-if="currentItem?.category === 'Pojazdy'">
                <h2 class="text-lg font-bold text-gray-900 mb-3">Informacje o pojeździe</h2>
                <div class="grid grid-cols-2 gap-y-3 gap-x-4 text-gray-800">
                  <div class="flex items-start gap-2.5">
                    <Speedometer :size="20" class="text-gray-500 shrink-0 mt-0.5" />
                    <span class="text-[14px]">Przebieg {{ currentItem?.details?.mileage }}</span>
                  </div>
                  <div class="flex items-start gap-2.5">
                    <CarShiftPattern :size="20" class="text-gray-500 shrink-0 mt-0.5" />
                    <span class="text-[14px]">{{ currentItem?.details?.transmission }}</span>
                  </div>
                  <div class="flex items-start gap-2.5">
                    <Palette :size="20" class="text-gray-500 shrink-0 mt-0.5" />
                    <span class="text-[14px]">Kolor karoserii: {{ currentItem?.details?.color }}</span>
                  </div>
                  <div class="flex items-start gap-2.5">
                    <Fuel :size="20" class="text-gray-500 shrink-0 mt-0.5" />
                    <span class="text-[14px]">Typ paliwa: {{ currentItem?.details?.fuel }}</span>
                  </div>
                </div>
                <button class="text-[#0064D1] font-semibold text-[15px] hover:underline mt-3 block">
                  Zobacz więcej
                </button>
              </div>

              <hr class="border-gray-200 my-4" />

              <!-- SEKCJA SPONSOROWANE -->
              <div class="mb-6">
                <h2 class="text-lg font-bold text-gray-900 mb-3">Sponsorowane</h2>
                <div class="grid grid-cols-3 gap-2">
                  <!-- Karta 1 -->
                  <div class="flex flex-col cursor-pointer group">
                    <div class="w-full aspect-square rounded-lg overflow-hidden bg-gray-100 relative mb-1.5 border border-gray-200">
                      <img src="https://picsum.photos/seed/ad1/300/300" alt="Ad" class="w-full h-full object-cover group-hover:scale-105 transition-transform" />
                      <div class="absolute top-2 left-2 bg-white/90 rounded-full p-1 shadow-sm">
                        <span class="text-[10px] font-bold text-orange-600 px-1">allegro</span>
                      </div>
                    </div>
                    <div class="flex justify-between items-start">
                      <span class="text-[13px] font-bold text-gray-900 truncate">Allegro</span>
                      <button class="text-gray-400 hover:text-gray-600"><DotsHorizontal :size="16" /></button>
                    </div>
                    <span class="text-[12px] text-gray-500">Sprawdź!</span>
                  </div>
                  <!-- Karta 2 -->
                  <div class="flex flex-col cursor-pointer group">
                    <div class="w-full aspect-square rounded-lg overflow-hidden bg-gray-100 relative mb-1.5 border border-gray-200 flex items-center justify-center">
                      <img src="https://picsum.photos/seed/ad2/300/300" alt="Ad" class="w-full h-full object-cover group-hover:scale-105 transition-transform" />
                      <div class="absolute inset-0 flex items-center justify-center bg-black/20">
                        <PlayCircle :size="36" fillColor="#FFFFFF" />
                      </div>
                    </div>
                    <div class="flex justify-between items-start">
                      <span class="text-[13px] font-bold text-gray-900 truncate">Loopy's World Gd...</span>
                      <button class="text-gray-400 hover:text-gray-600"><DotsHorizontal :size="16" /></button>
                    </div>
                    <span class="text-[12px] text-gray-500 truncate">Morze frajdy w Loopy's World</span>
                  </div>
                  <!-- Karta 3 -->
                  <div class="flex flex-col cursor-pointer group">
                    <div class="w-full aspect-square rounded-lg overflow-hidden bg-gray-100 relative mb-1.5 border border-gray-200">
                      <img src="https://picsum.photos/seed/ad3/300/300" alt="Ad" class="w-full h-full object-cover group-hover:scale-105 transition-transform" />
                    </div>
                    <div class="flex justify-between items-start">
                      <span class="text-[13px] font-bold text-gray-900 truncate">Muller PL</span>
                      <button class="text-gray-400 hover:text-gray-600"><DotsHorizontal :size="16" /></button>
                    </div>
                    <span class="text-[12px] text-gray-500">Sprawdź!</span>
                  </div>
                </div>
              </div>

              <hr class="border-gray-200 my-4" />

              <!-- OPIS SPRZEDAWCY -->
              <div class="mb-6">
                <h2 class="text-lg font-bold text-gray-900 mb-3">Opis sprzedawcy</h2>
                <div class="text-[15px] text-gray-900 leading-relaxed whitespace-pre-wrap break-words">
                  <template v-if="isDescriptionExpanded || (currentItem?.description?.length || 0) <= 250">
                    {{ currentItem?.description }}
                  </template>
                  <template v-else>
                    {{ currentItem?.description?.substring(0, 250) }}...
                  </template>
                </div>
                <button
                  @click="isDescriptionExpanded = !isDescriptionExpanded"
                  class="font-bold text-gray-900 mt-1 hover:underline text-[15px]"
                >
                  {{ isDescriptionExpanded ? 'Wyświetl mniej' : 'Wyświetl więcej' }}
                </button>
              </div>

              <!-- MAPA -->
              <div class="mb-6">
                <div
                  ref="mapContainer"
                  class="rounded-lg overflow-hidden h-32 w-full relative bg-gray-100 mb-2 border border-gray-200 z-0"
                ></div>
                <div class="text-[14px] text-gray-900 font-medium">{{ currentItem?.location }}</div>
                <div class="text-xs text-gray-500">Lokacja jest przybliżona</div>
              </div>

              <hr class="border-gray-200 my-4" />

              <!-- INFORMACJE O SPRZEDAWCY -->
              <div class="flex justify-between items-baseline mb-4">
                <h2 class="text-lg font-bold text-gray-900">Informacje o sprzedawcy</h2>
                <button @click="openSellerModal" class="text-[#0064D1] font-semibold text-[15px] hover:underline">
                  Informacje o sprzedawcy
                </button>
              </div>

              <div @click="openSellerModal" class="flex items-center gap-3 mb-3 cursor-pointer">
                <img
                  class="rounded-full w-12 h-12 object-cover bg-gray-200"
                  :src="currentItem?.seller?.avatar"
                  alt="Seller avatar"
                />
                <div class="flex flex-col justify-center">
                  <div class="font-bold text-[17px] text-gray-900 hover:underline">
                    {{ currentItem?.seller?.name }}
                  </div>
                </div>
              </div>

              <div @click="openSellerModal" class="flex items-center gap-3 mb-6 cursor-pointer">
                <div class="w-6 h-6 rounded-full bg-[#E4E6EB] flex items-center justify-center shrink-0">
                  <Facebook :size="14" class="text-gray-700" />
                </div>
                <div class="text-[15px] text-gray-800">
                  Użytkownik Facebooka od {{ currentItem?.seller?.memberSince }}
                </div>
              </div>

              <!-- PRZYCISK GŁÓWNY NA DOLE -->
              <button
                @click="isMessageModalOpen = true"
                class="w-full bg-[#0064D1] hover:bg-[#0052ad] text-white font-bold py-2.5 px-4 rounded-lg transition-colors flex items-center justify-center gap-2 h-11 text-[16px] shadow-sm"
              >
                <FacebookMessenger :size="20" fillColor="#FFFFFF" />
                Wyślij wiadomość do sprzedawcy
              </button>

              <div class="text-center mt-4 text-[12px] text-gray-500 leading-relaxed px-2">
                <a href="#" class="text-[#0064D1] hover:underline font-semibold">Dowiedz się więcej</a> about purchasing from consumers, including your limited consumer rights and Facebook's role as an intermediary.
              </div>
            </div>
          </HoverScrollbar>
        </div>
      </div>
    </div>

    <!-- Modale (Seller, Share, Message) pozostały bez zmian -->
    <BaseModal v-if="isSellerModalOpen" @close="isSellerModalOpen = false" :title="currentItem?.seller?.name">
      <SellerModal :profile="currentItem?.seller" @close="isSellerModalOpen = false" />
    </BaseModal>

    <BaseModal v-if="isShareModalOpen" @close="isShareModalOpen = false" title="Udostępnij">
      <StoryShareModal :marketplaceItem="currentItem" @close="handleShareComplete" />
    </BaseModal>

    <BaseModal v-if="isMessageModalOpen" :title="'Wyślij wiadomość'" @close="isMessageModalOpen = false">
      <div class="w-[548px] max-w-[95vw] bg-white rounded-lg shadow-xl flex flex-col overflow-hidden">
        <div class="p-4 flex gap-4 items-center">
          <img :src="currentItem?.images?.[0]" alt="" class="w-16 h-16 rounded-xl object-cover border border-gray-200" />
          <div>
            <h3 class="font-bold text-gray-900 text-lg leading-tight">{{ currentItem?.title }}</h3>
            <p class="text-gray-500 text-sm">{{ currentItem?.price }}</p>
          </div>
        </div>
        <div class="p-4 pt-0">
          <div class="flex flex-wrap gap-2 mb-4">
            <button
              v-for="(reply, idx) in quickReplies"
              :key="idx"
              @click="messageText = reply"
              class="bg-gray-100 hover:bg-gray-200 text-gray-900 px-4 py-2 rounded-full text-[14px] font-medium transition-colors"
            >
              {{ reply }}
            </button>
          </div>
          <CustomTextarea v-model="messageText" label="Wpisz wiadomość do sprzedawcy" />
          <p class="text-[13px] text-gray-500 mt-3">Nie udostępniaj adresu e-mail, numeru telefonu ani informacji finansowych.</p>
        </div>
        <div class="p-4 border-t border-gray-200 flex justify-end items-center gap-4">
          <button @click="isMessageModalOpen = false" class="text-[#0064D1] font-semibold hover:underline px-4 py-2">Anuluj</button>
          <button @click="handleSendMessage" class="bg-[#0084ff] hover:bg-[#0070d6] text-white font-semibold py-2 px-6 rounded-lg flex items-center justify-center gap-2 transition-colors">
            <FacebookMessenger :size="20" fillColor="#FFFFFF" />
            Wyślij wiadomość
          </button>
        </div>
      </div>
    </BaseModal>
  </div>
</template>
