<script setup lang="ts">
import { ref, watch, onMounted } from 'vue'
import MediaItem from '@/components/feed/MediaItem.vue'
import type { ImageTagType } from '@/types/Post'

const props = defineProps<{
  media: {
    src: string
    altText?: string
    tags?: ImageTagType[]
  }[]
  postId: string | number
}>()

// Reaktywna zmienna przechowująca kolor tła (jako fallback ustawiony Twój #654)
const dominantColor = ref('rgb(101, 85, 68)')

function getPhotoId(mediaItem: any, index: number): string {
  if (!mediaItem || !mediaItem.src) return String(index)
  const src = mediaItem.src
  if (src.includes('/files/')) {
    const parts = src.split('/files/')
    const filename = parts[parts.length - 1]
    const qIdx = filename.indexOf('?')
    if (qIdx !== -1) return filename.substring(0, qIdx)
    return filename
  }
  const segments = src.split('/')
  return segments[segments.length - 1] || src
}

// Funkcja analizująca pierwszy obrazek i wyciągająca kolor
function analyzeDominantColor() {
  // Blokada dla SSR (wykonuj tylko w przeglądarce) oraz gdy brak mediów
  if (typeof window === 'undefined' || !props.media || props.media.length === 0) return

  const firstMediaSrc = props.media[0]?.src
  if (!firstMediaSrc) return

  const img = new Image()
  // Ważne dla zdjęć z zewnętrznych serwerów (CORS)
  img.crossOrigin = 'Anonymous'
  img.src = firstMediaSrc

  img.onload = () => {
    const canvas = document.createElement('canvas')
    const ctx = canvas.getContext('2d')
    if (!ctx) return

    canvas.width = 1
    canvas.height = 1

    // Rysujemy obraz skurczony do 1x1 px – przeglądarka sama go uśredni
    ctx.drawImage(img, 0, 0, 1, 1)

    // Pobieramy dane o kolorze tego jednego piksela
    const imgData = ctx.getImageData(0, 0, 1, 1).data
    const r = imgData[0]
    const g = imgData[1]
    const b = imgData[2]

    // Aktualizujemy kolor (możesz też dodać opacity, np. rgba)
    dominantColor.value = `rgb(${r}, ${g}, ${b})`
  }

  img.onerror = () => {
    // W razie błędu ładowania lub blokady CORS przywróć fallback
    dominantColor.value = 'rgb(101, 85, 68)'
  }
}

// Obserwuj zmiany w propsach (np. gdy zmienia się post lub zestaw zdjęć)
watch(
  () => props.media,
  () => {
    analyzeDominantColor()
  },
  { deep: true, immediate: true }
)
</script>

<template>
  <!-- Podmiana klasy bg-[#654] na dynamiczny styl z płynnym przejściem kolorów -->
  <div
    :style="{ backgroundColor: media.length === 1 ? dominantColor : 'transparent' }"
    class="transition-colors duration-500 ease-in-out"
  >
    <div
      :class="[
        'w-full mx-auto h-[680px]',
        media.length === 1 ? 'max-w-[max(412.5px,_calc(-243.75px_+_75vh))]' : ''
      ]"
    >
      <!-- 1 media -->
      <div v-if="media.length === 1" class="w-full h-full">
        <MediaItem
          v-if="media[0]"
          :media="media[0]"
          :post-id="postId"
          :index="0"
          class="block w-full h-full bg-black/5 relative"
        />
      </div>

      <!-- 2 media -->
      <div v-else-if="media.length === 2" class="grid grid-cols-2 gap-1 h-full">
        <MediaItem
          v-for="(item, idx) in media"
          :key="idx"
          :media="item"
          :post-id="postId"
          :index="idx"
          class="block w-full h-full bg-black/5 relative"
        />
      </div>

      <!-- 3 media -->
      <div v-else-if="media.length === 3" class="flex flex-col gap-1 h-full">
        <MediaItem
          v-if="media[0]"
          :media="media[0]"
          :post-id="postId"
          :index="0"
          class="w-full flex-[2] bg-black/5 relative"
        />
        <div class="grid grid-cols-2 gap-1 flex-1">
          <MediaItem
            v-if="media[1]"
            :media="media[1]"
            :post-id="postId"
            :index="1"
            class="bg-black/5 w-full h-full relative"
          />
          <MediaItem
            v-if="media[2]"
            :media="media[2]"
            :post-id="postId"
            :index="2"
            class="bg-black/5 w-full h-full relative"
          />
        </div>
      </div>

      <!-- 4 media -->
      <div v-else-if="media.length === 4" class="flex flex-col gap-1 h-full">
        <MediaItem
          v-if="media[0]"
          :media="media[0]"
          :post-id="postId"
          :index="0"
          class="block w-full flex-[2] bg-black/5 relative"
        />
        <div class="grid grid-cols-3 gap-1 flex-1">
          <MediaItem
            v-for="(item, idx) in media.slice(1)"
            :key="idx"
            :media="item"
            :post-id="postId"
            :index="idx + 1"
            class="block w-full h-full bg-black/5 relative"
          />
        </div>
      </div>

      <!-- 5+ media -->
      <div v-else class="grid grid-cols-2 gap-1 h-full">
        <div class="flex flex-col gap-1 h-full">
          <MediaItem
            v-if="media[0]"
            :media="media[0]"
            :post-id="postId"
            :index="0"
            class="flex-1 bg-black/5 relative"
          />
          <MediaItem
            v-if="media[1]"
            :media="media[1]"
            :post-id="postId"
            :index="1"
            class="flex-1 bg-black/5 relative"
          />
        </div>
        <div class="flex flex-col gap-1 h-full">
          <MediaItem
            v-if="media[2]"
            :media="media[2]"
            :post-id="postId"
            :index="2"
            class="flex-1 bg-black/5 relative"
          />
          <MediaItem
            v-if="media[3]"
            :media="media[3]"
            :post-id="postId"
            :index="3"
            class="flex-1 bg-black/5 relative"
          />
          <NuxtLink
            v-if="media[4]"
            :to="`/photo/?fbid=${getPhotoId(media[4], 4)}&set=a.${postId}`"
            class="flex-1 bg-black/5 relative block"
          >
            <MediaItem
              :media="media[4]"
              :post-id="postId"
              :index="4"
              class="w-full h-full bg-black/5 relative"
            />
            <div
              v-if="media.length > 5"
              class="absolute inset-0 bg-black/50 flex items-center justify-center cursor-pointer"
            >
              <span class="text-white text-3xl font-bold">+{{ media.length - 5 }}</span>
            </div>
          </NuxtLink>
        </div>
      </div>
    </div>
  </div>
</template>
