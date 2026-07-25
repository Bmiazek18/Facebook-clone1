<script setup lang="ts">
import { computed } from 'vue'
import { useCreatePostStore } from '@/stores/createPost'
import PencilIcon from 'vue-material-design-icons/Pencil.vue'
import CloseIcon from 'vue-material-design-icons/Close.vue'
import PlayIcon from 'vue-material-design-icons/Play.vue'

const createPostStore = useCreatePostStore()

// Wykrywanie wideo (fallback)
const isVideo = (url: string) =>
  /\.(mp4|webm|ogg)$/i.test(url) || url.startsWith('blob:') || url.startsWith('data:video')

// Wspólny format dla wszystkich mediów
const mediaItems = computed(() => {
  const items = []
  if (createPostStore.postData.postVideoUrl) {
    items.push({
      type: 'video',
      url: createPostStore.postData.postVideoUrl,
      index: -1,
      progress: null,
    })
  }

  createPostStore.postData.images.forEach((img, index) => {
    // Używamy zdefiniowanego typu, jeśli istnieje, w przeciwnym razie sprawdzamy po regexie
    const isVid = img.type === 'video' || (img.type === undefined && isVideo(img.url))
    items.push({
      type: isVid ? 'video' : 'image',
      url: img.url,
      index,
      progress: img.progress ?? null,
    })
  })

  if (createPostStore.postData.gif) {
    items.push({
      type: 'gif',
      url: createPostStore.postData.gif,
      index: -2,
      progress: null,
    })
  }
  return items
})

const handleEdit = (item: any) => {
  if (item.type === 'video') {
    createPostStore.editVideo(item.index)
  } else {
    createPostStore.editImage(item.index)
  }
}

const handleRemove = (item: any) => {
  if (item.type === 'video' && item.index === -1) {
    createPostStore.postData.postVideoUrl = null
  } else if (item.type === 'video') {
    createPostStore.removeSelectedImage(item.index)
  } else if (item.type === 'image') {
    createPostStore.removeSelectedImage(item.index)
  } else if (item.type === 'gif') {
    createPostStore.postData.gif = null
  }
}

// Trik: doklejenie #t=0.001 zmusza odtwarzacz HTML5 do wyrenderowania 1. klatki jako "plakatu"
const getVideoPosterUrl = (url: string) => {
  if (url.includes('#')) return url
  return `${url}#t=0.001`
}
</script>

<template>
  <div class="space-y-4 mb-4">
    <div
      v-for="(item, i) in mediaItems"
      :key="i"
      class="relative bg-gray-900 rounded-xl overflow-hidden border border-theme-border w-full shadow-sm group"
    >
      <!-- ================= VIDEO (Miniaturka w tle) ================= -->
      <div
        v-if="item.type === 'video'"
        class="relative aspect-video w-full bg-black flex items-center justify-center"
      >
        <video
          :src="getVideoPosterUrl(item.url)"
          class="w-full h-full object-cover block pointer-events-none"
          preload="metadata"
          muted
          playsinline
        ></video>

        <!-- NAKŁADKA A: W trakcie uploadu (Kółko z progresem) -->
        <div
          v-if="item.progress !== null && item.progress !== undefined && item.progress < 100"
          class="absolute inset-0 flex items-center justify-center bg-black/40 backdrop-blur-xs"
        >
          <div
            class="relative flex items-center justify-center w-16 h-16 bg-black/60 rounded-full shadow-lg border border-white/20"
          >
            <!-- Okrągły pasek postępu SVG -->
            <svg class="w-14 h-14 transform -rotate-90" viewBox="0 0 36 36">
              <path
                class="text-white/20"
                stroke-width="3"
                stroke="currentColor"
                fill="none"
                d="M18 2.0845 a 15.9155 15.9155 0 0 1 0 31.831 a 15.9155 15.9155 0 0 1 0 -31.831"
              />
              <path
                class="text-white transition-all duration-300"
                stroke-width="3"
                stroke-dasharray="100, 100"
                :stroke-dashoffset="100 - item.progress"
                stroke-linecap="round"
                stroke="currentColor"
                fill="none"
                d="M18 2.0845 a 15.9155 15.9155 0 0 1 0 31.831 a 15.9155 15.9155 0 0 1 0 -31.831"
              />
            </svg>
            <span class="absolute text-xs font-bold text-white"
              >{{ Math.round(item.progress) }}%</span
            >
          </div>
        </div>

        <!-- NAKŁADKA B: Gotowe do odtworzenia (Przycisk Play z FB) -->
        <div
          v-else
          @click="handleEdit(item)"
          class="absolute inset-0 flex items-center justify-center cursor-pointer group-hover:bg-black/10 transition-colors"
        >
          <div
            class="w-16 h-16 bg-black/60 hover:bg-black/80 transition-all scale-100 hover:scale-105 rounded-full border-2 border-white flex items-center justify-center shadow-lg"
          >
            <!-- translate-x-0.5 optycznie środkuje trójkąt wewnątrz koła -->
            <PlayIcon :size="36" class="text-white translate-x-0.5" />
          </div>
        </div>
      </div>

      <!-- ================= ZDJĘCIE / GIF ================= -->
      <div v-else class="relative w-full">
        <img :src="item.url" class="w-full h-auto block" />

        <!-- NAKŁADKA A: W trakcie uploadu (Kółko z progresem) -->
        <div
          v-if="item.progress !== null && item.progress !== undefined && item.progress < 100"
          class="absolute inset-0 flex items-center justify-center bg-black/40 backdrop-blur-xs"
        >
          <div
            class="relative flex items-center justify-center w-16 h-16 bg-black/60 rounded-full shadow-lg border border-white/20"
          >
            <!-- Okrągły pasek postępu SVG -->
            <svg class="w-14 h-14 transform -rotate-90" viewBox="0 0 36 36">
              <path
                class="text-white/20"
                stroke-width="3"
                stroke="currentColor"
                fill="none"
                d="M18 2.0845 a 15.9155 15.9155 0 0 1 0 31.831 a 15.9155 15.9155 0 0 1 0 -31.831"
              />
              <path
                class="text-white transition-all duration-300"
                stroke-width="3"
                stroke-dasharray="100, 100"
                :stroke-dashoffset="100 - item.progress"
                stroke-linecap="round"
                stroke="currentColor"
                fill="none"
                d="M18 2.0845 a 15.9155 15.9155 0 0 1 0 31.831 a 15.9155 15.9155 0 0 1 0 -31.831"
              />
            </svg>
            <span class="absolute text-xs font-bold text-white"
              >{{ Math.round(item.progress) }}%</span
            >
          </div>
        </div>
      </div>

      <!-- Przycisk "Edytuj" (Dokładnie jak na Zrzucie Ekranu) -->
      <button
        v-if="item.type !== 'gif'"
        @click.stop="handleEdit(item)"
        class="absolute top-3 left-3 bg-white hover:bg-gray-100 text-gray-900 text-sm font-semibold px-3 py-1.5 rounded-lg shadow-md flex items-center gap-1.5 transition z-10 cursor-pointer"
      >
        <PencilIcon :size="16" />
        <span>Edytuj</span>
      </button>

      <!-- Przycisk "X" (Zamknij) -->
      <button
        @click.stop="handleRemove(item)"
        class="absolute top-3 right-3 bg-white hover:bg-gray-100 text-gray-700 p-2 rounded-full shadow-md transition z-10 cursor-pointer flex items-center justify-center"
      >
        <CloseIcon :size="18" />
      </button>
    </div>
  </div>
</template>
