<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserMedia, type UserMediaItem } from '@/composables/feed/useUserMedia'
import { useAuthStore } from '@/stores/auth'

definePageMeta({
  keepScroll: true,
})

// Ikony
import DotsHorizontal from 'vue-material-design-icons/DotsHorizontal.vue'
import Pencil from 'vue-material-design-icons/Pencil.vue'
import Plus from 'vue-material-design-icons/Plus.vue'
import Play from 'vue-material-design-icons/Play.vue'
import ArrowLeft from 'vue-material-design-icons/ArrowLeft.vue'
import ImageMultiple from 'vue-material-design-icons/ImageMultiple.vue'
import TagOutline from 'vue-material-design-icons/TagOutline.vue'
import VideoOutline from 'vue-material-design-icons/VideoOutline.vue'
import FolderMultipleOutline from 'vue-material-design-icons/FolderMultipleOutline.vue'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const profileUserId = computed(() => (route.params.userId as string) || String(authStore.currentUserId))
const isOwnProfile = computed(() => String(profileUserId.value) === String(authStore.currentUserId))

// Stany zakładek
type TabType = 'photos' | 'tagged' | 'videos' | 'albums'
const activeTab = ref<TabType>('photos')
const selectedAlbum = ref<string | null>(null)

// Modal podglądu (Lightbox)
const activeMediaModal = ref<UserMediaItem | null>(null)

const {
  mediaItems,
  albums,
  loading,
  hasMore,
  totalCount,
  fetchMedia,
  loadMore,
  fetchAlbums,
} = useUserMedia()

function switchTab(tab: TabType) {
  activeTab.value = tab
  selectedAlbum.value = null
  if (tab === 'photos') {
    fetchMedia(profileUserId.value, 'PHOTOS')
  } else if (tab === 'tagged') {
    fetchMedia(profileUserId.value, 'TAGGED')
  } else if (tab === 'videos') {
    fetchMedia(profileUserId.value, 'VIDEOS')
  } else if (tab === 'albums') {
    fetchAlbums(profileUserId.value)
  }
}

function openAlbum(albumName: string) {
  selectedAlbum.value = albumName
  fetchMedia(profileUserId.value, 'ALBUM', albumName)
}

function closeAlbum() {
  selectedAlbum.value = null
  fetchAlbums(profileUserId.value)
}

function openMedia(item: UserMediaItem) {
  activeMediaModal.value = item
}

function closeMedia() {
  activeMediaModal.value = null
}

function isVideo(url: string, type: string) {
  if (type === 'VIDEO') return true
  const lower = url.toLowerCase()
  return lower.endsWith('.mp4') || lower.endsWith('.webm') || lower.endsWith('.mov') || lower.contains('/video/')
}

watch(
  () => profileUserId.value,
  (newId) => {
    if (newId) {
      switchTab(activeTab.value)
    }
  },
  { immediate: true }
)

onMounted(() => {
  fetchAlbums(profileUserId.value)
})
</script>

<template>
  <div class="bg-[#F0F2F5] p-4 space-y-4 antialiased text-[#050505]">
    <div class="bg-white rounded-xl shadow-sm p-4">
      <!-- Nagłówek -->
      <div class="flex items-center justify-between mb-2">
        <div class="flex items-center gap-2">
          <button
            v-if="selectedAlbum"
            @click="closeAlbum"
            class="p-2 hover:bg-gray-100 rounded-full transition-colors"
          >
            <ArrowLeft :size="20" />
          </button>
          <h2 class="text-[20px] font-bold">
            {{ selectedAlbum ? selectedAlbum : 'Zdjęcia i multimedia' }}
          </h2>
          <span v-if="totalCount > 0 && !selectedAlbum" class="text-sm font-normal text-gray-500">
            ({{ totalCount }})
          </span>
        </div>

        <div class="flex items-center gap-2">
          <NuxtLink
            v-if="isOwnProfile"
            to="/addAlbum"
            class="text-[#1877F2] hover:bg-blue-50 px-3 py-2 rounded-md font-medium text-[15px] transition-colors flex items-center gap-1"
          >
            <Plus :size="18" />
            <span>{{ $t('profile.utworzAlbum') }}</span>
          </NuxtLink>
          <button class="p-2 bg-[#E4E6EB] hover:bg-[#D8DADF] rounded-full transition-colors">
            <DotsHorizontal :size="16" />
          </button>
        </div>
      </div>

      <!-- Pasek zakładek i filtrów -->
      <div v-if="!selectedAlbum" class="flex items-center justify-between border-b border-gray-200 mb-4 overflow-x-auto">
        <div class="flex gap-1 min-w-max">
          <button
            @click="switchTab('photos')"
            class="px-4 py-3 text-[15px] font-semibold transition-all relative flex items-center gap-1.5"
            :class="activeTab === 'photos' ? 'text-[#1877F2]' : 'text-[#65676B] hover:bg-gray-50'"
          >
            <ImageMultiple :size="18" />
            <span>{{ $t('profile.twojeZdjecia') }}</span>
            <div
              v-if="activeTab === 'photos'"
              class="absolute bottom-0 left-0 right-0 h-[3px] bg-[#1877F2]"
            ></div>
          </button>

          <button
            @click="switchTab('tagged')"
            class="px-4 py-3 text-[15px] font-semibold transition-all relative flex items-center gap-1.5"
            :class="activeTab === 'tagged' ? 'text-[#1877F2]' : 'text-[#65676B] hover:bg-gray-50'"
          >
            <TagOutline :size="18" />
            <span>{{ $t('profile.zdjeciaZOznaczeniem') }}</span>
            <div
              v-if="activeTab === 'tagged'"
              class="absolute bottom-0 left-0 right-0 h-[3px] bg-[#1877F2]"
            ></div>
          </button>

          <button
            @click="switchTab('videos')"
            class="px-4 py-3 text-[15px] font-semibold transition-all relative flex items-center gap-1.5"
            :class="activeTab === 'videos' ? 'text-[#1877F2]' : 'text-[#65676B] hover:bg-gray-50'"
          >
            <VideoOutline :size="18" />
            <span>{{ $t('profile.filmyIRolki') }}</span>
            <div
              v-if="activeTab === 'videos'"
              class="absolute bottom-0 left-0 right-0 h-[3px] bg-[#1877F2]"
            ></div>
          </button>

          <button
            @click="switchTab('albums')"
            class="px-4 py-3 text-[15px] font-semibold transition-all relative flex items-center gap-1.5"
            :class="activeTab === 'albums' ? 'text-[#1877F2]' : 'text-[#65676B] hover:bg-gray-50'"
          >
            <FolderMultipleOutline :size="18" />
            <span>{{ $t('profile.albumy') }}</span>
            <div
              v-if="activeTab === 'albums'"
              class="absolute bottom-0 left-0 right-0 h-[3px] bg-[#1877F2]"
            ></div>
          </button>
        </div>
      </div>

      <!-- STAN ŁADOWANIA -->
      <div v-if="loading && mediaItems.length === 0" class="grid grid-cols-3 sm:grid-cols-4 md:grid-cols-5 gap-2 py-4">
        <div v-for="i in 10" :key="i" class="aspect-square bg-gray-200 animate-pulse rounded-md"></div>
      </div>

      <!-- WIDOK ALBUMÓW -->
      <div
        v-else-if="activeTab === 'albums' && !selectedAlbum"
        class="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 gap-4"
      >
        <NuxtLink v-if="isOwnProfile" to="/addAlbum" class="cursor-pointer group">
          <div
            class="aspect-square w-full bg-[#E4E6EB] hover:bg-[#D8DADF] transition rounded-xl flex items-center justify-center border border-gray-200"
          >
            <Plus :size="36" class="text-[#65676B]" />
          </div>
          <div class="mt-2 pl-1">
            <h4 class="text-[15px] font-semibold text-[#050505]">{{ $t('profile.utworzAlbum') }}</h4>
          </div>
        </NuxtLink>

        <div
          v-for="album in albums"
          :key="album.name"
          @click="openAlbum(album.name)"
          class="cursor-pointer group"
        >
          <div
            class="aspect-square w-full rounded-xl overflow-hidden border border-gray-200 bg-gray-100 hover:brightness-95 transition relative flex items-center justify-center"
          >
            <img
              v-if="album.coverUrl"
              :src="album.coverUrl"
              class="w-full h-full object-cover"
              loading="lazy"
            />
            <FolderMultipleOutline v-else :size="48" class="text-gray-400" />
          </div>
          <div class="mt-2 pl-1">
            <h4 class="text-[15px] font-semibold text-[#050505] leading-tight truncate">
              {{ album.name }}
            </h4>
            <p class="text-[13px] text-[#65676B] mt-0.5">{{ $t('profile.albumCountElementow') }}</p>
          </div>
        </div>

        <div v-if="albums.length === 0 && !isOwnProfile" class="col-span-full py-12 text-center text-gray-500">{{ $t('profile.brakDostepnychAlbumow') }}</div>
      </div>

      <!-- WIDOK SIATKI MEDIÓW (ZDJĘCIA, OZNACZENIA, WIDEO, LUB WNĘTRZE ALBUMU) -->
      <div v-else>
        <div
          v-if="mediaItems.length > 0"
          class="grid grid-cols-3 sm:grid-cols-4 md:grid-cols-5 lg:grid-cols-6 gap-2"
        >
          <div
            v-for="item in mediaItems"
            :key="item.id"
            @click="openMedia(item)"
            class="relative aspect-square group overflow-hidden rounded-md border border-gray-200 bg-gray-100 cursor-pointer"
          >
            <video
              v-if="isVideo(item.mediaUrl, item.mediaType)"
              :src="item.mediaUrl"
              class="w-full h-full object-cover transition-transform duration-200 group-hover:scale-105"
            ></video>
            <img
              v-else
              :src="item.mediaUrl"
              :alt="item.altText || 'Zdjęcie użytkownika'"
              class="w-full h-full object-cover transition-transform duration-200 group-hover:scale-105"
              loading="lazy"
            />

            <!-- Wskaźnik wideo -->
            <div
              v-if="isVideo(item.mediaUrl, item.mediaType)"
              class="absolute bottom-2 right-2 bg-black/60 text-white p-1 rounded-full pointer-events-none"
            >
              <Play :size="16" />
            </div>

            <!-- Nakładka hover -->
            <div
              class="absolute inset-0 bg-black/20 opacity-0 group-hover:opacity-100 transition-opacity flex items-center justify-center text-white"
            >
              <span class="text-xs font-semibold px-2 py-1 bg-black/50 rounded">{{ $t('profile.pokazPost') }}</span>
            </div>
          </div>
        </div>

        <!-- PUSTY STAN -->
        <div v-else-if="!loading" class="py-12 text-center text-gray-500">
          <p class="text-[16px] font-semibold">{{ $t('profile.brakMultimediowWTej') }}</p>
          <p class="text-sm mt-1 text-gray-400">{{ $t('profile.dodajNowyPostZe') }}</p>
        </div>

        <!-- PRZYCISK ZAŁADUJ WIĘCEJ -->
        <div v-if="hasMore" class="text-center mt-6">
          <button
            @click="loadMore(profileUserId)"
            :disabled="loading"
            class="px-6 py-2.5 bg-[#E4E6EB] hover:bg-[#D8DADF] text-[#050505] font-semibold rounded-lg text-sm transition-colors"
          >
            {{ loading ? 'Wczytywanie...' : 'Załaduj więcej zdjęć' }}
          </button>
        </div>
      </div>
    </div>

    <!-- MODAL PEŁNOEKRANOWY (LIGHTBOX / PODGLĄD) -->
    <div
      v-if="activeMediaModal"
      class="fixed inset-0 z-50 bg-black/90 backdrop-blur-sm flex items-center justify-center p-4"
      @click.self="closeMedia"
    >
      <div class="relative max-w-5xl w-full max-h-[90vh] flex flex-col md:flex-row bg-[#18191A] rounded-xl overflow-hidden shadow-2xl">
        <!-- Obszar mediów po lewej -->
        <div class="flex-1 bg-black flex items-center justify-center min-h-[300px] md:min-h-[500px]">
          <video
            v-if="isVideo(activeMediaModal.mediaUrl, activeMediaModal.mediaType)"
            :src="activeMediaModal.mediaUrl"
            controls
            autoplay
            class="max-h-[85vh] max-w-full object-contain"
          ></video>
          <img
            v-else
            :src="activeMediaModal.mediaUrl"
            class="max-h-[85vh] max-w-full object-contain"
          />
        </div>

        <!-- Panel boczny informacji po prawej -->
        <div class="w-full md:w-80 bg-white p-4 flex flex-col justify-between">
          <div>
            <div class="flex items-center justify-between pb-3 border-b border-gray-200">
              <h3 class="font-bold text-gray-900">{{ $t('profile.szczegolyZdjecia') }}</h3>
              <button
                @click="closeMedia"
                class="p-1 hover:bg-gray-100 rounded-full text-gray-500"
              >
                ✕
              </button>
            </div>

            <div class="mt-4 space-y-2 text-sm text-gray-700">
              <p v-if="activeMediaModal.albumName">
                <span class="font-semibold text-gray-500">{{ $t('profile.album') }}</span> {{ activeMediaModal.albumName }}
              </p>
              <p v-if="activeMediaModal.createdAt">
                <span class="font-semibold text-gray-500">{{ $t('profile.dataDodania') }}</span>
                {{ new Date(activeMediaModal.createdAt).toLocaleDateString('pl-PL') }}
              </p>
              <p v-if="activeMediaModal.altText">
                <span class="font-semibold text-gray-500">{{ $t('profile.opis') }}</span> {{ activeMediaModal.altText }}
              </p>
            </div>
          </div>

          <div class="pt-4 border-t border-gray-200 mt-6">
            <button
              @click="router.push(`/posts/${activeMediaModal.postId}`)"
              class="w-full py-2 px-4 bg-[#1877F2] hover:bg-[#166FE5] text-white font-semibold rounded-lg text-sm transition-colors text-center"
            >{{ $t('profile.przejdzDoPosta') }}</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
