<template>
  <div class="w-full h-full relative" @mouseenter="showTags = true" @mouseleave="showTags = false">
    <template v-if="resolvedIsVideo">
      <!-- Wideo po zakończeniu transkodowania ABR -->
      <PlayerVideo
        v-if="transcodingFinished"
        :settings="true"
        :lightbox="true"
        :url="getMediaUrl(media.src)"
        :post-id="postId"
        :index="index"
        :is-single-video="isSingleVideo"
        class="w-full h-full"
      />
      <!-- Karta ładowania/przetwarzania wideo w trakcie ABR -->
      <div
        v-else
        class="w-full h-full bg-black/90 flex flex-col items-center justify-center text-white p-4 text-center rounded-lg border border-theme-border shadow-inner"
      >
        <svg
          class="animate-spin -ml-1 mr-3 h-8 w-8 text-blue-500 mb-3"
          xmlns="http://www.w3.org/2000/svg"
          fill="none"
          viewBox="0 0 24 24"
        >
          <circle
            class="opacity-25"
            cx="12"
            cy="12"
            r="10"
            stroke="currentColor"
            stroke-width="4"
          ></circle>
          <path
            class="opacity-75"
            fill="currentColor"
            d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
          ></path>
        </svg>
        <div class="text-[15px] font-semibold text-gray-200">{{ $t('feed.przetwarzanieWideo') }}</div>
        <div class="text-[12px] text-gray-400 mt-1">{{ $t('feed.strumienAbrHlsJest') }}</div>
      </div>
    </template>
    <div
      v-else
      class="block bg-black/5 relative w-full h-full"
    >
      <NuxtLink
        :to="`/photo/?fbid=${photoId}&set=a.${postId}`"
        class="block w-full h-full"
      >
        <img
          v-if="!isImageError"
          class="w-full h-full object-cover cursor-pointer"
          :src="getMediaUrl(media.src)"
          :alt="media.altText || 'Post content'"
          @error="handleImageError"
        />
        <div
          v-else
          class="w-full h-full bg-gray-100 dark:bg-gray-800/50 flex flex-col items-center justify-center text-gray-500 dark:text-gray-400 p-4 text-center rounded-lg border border-theme-border shadow-inner"
        >
          <svg
            class="w-10 h-10 text-gray-400 mb-2"
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
            xmlns="http://www.w3.org/2000/svg"
          >
            <path
              stroke-linecap="round"
              stroke-linejoin="round"
              stroke-width="2"
              d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z"
            ></path>
          </svg>
          <div class="text-[13px] font-semibold">{{ $t('feed.zdjecieNiedostepne') }}</div>
          <div class="text-[10px] opacity-75 mt-0.5">{{ $t('feed.plikGraficznyNieZostal') }}</div>
        </div>
      </NuxtLink>
      <template v-if="media.tags?.length && !isImageError">
        <ImageTag
          v-for="tag in media.tags"
          :key="tag.id"
          :tag="tag"
          :force-show="showTags"
          class="absolute"
          :style="{ left: `${tag.x}%`, top: `${tag.y}%` }"
        />
      </template>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch, computed } from 'vue'
import ImageTag from '@/components/media/ImageTag.vue'
import type { ImageTagType } from '@/types/Post'
import PlayerVideo from '../media/PlayerVideo.vue'

const props = defineProps<{
  media: {
    src: string
    altText?: string
    tags?: ImageTagType[]
  }
  postId: string | number
  index: number
  isSingleVideo?: boolean
}>()

const showTags = ref(false)

const photoId = computed(() => {
  const src = props.media.src
  if (!src) return String(props.index)
  if (src.includes('/files/') || src.includes('/media/')) {
    const marker = src.includes('/media/') ? '/media/' : '/files/'
    const parts = src.split(marker)
    const filename = parts[parts.length - 1]
    const qIdx = filename.indexOf('?')
    if (qIdx !== -1) return filename.substring(0, qIdx)
    return filename
  }
  const segments = src.split('/')
  return segments[segments.length - 1] || src
})

const resolvedIsVideo = ref(false)
const transcodingFinished = ref(false)
const isImageError = ref(false)

const handleImageError = () => {
  isImageError.value = true
}

watch(
  () => props.media?.src,
  () => {
    isImageError.value = false
  },
)

const config = useRuntimeConfig()

const getMediaUrl = (src: string) => {
  if (!src) return ''
  if (src.startsWith('http://localhost/files/') || src.startsWith('http://localhost/videos/') || src.startsWith('http://localhost/media/')) {
    src = src.replace('http://localhost/', config.public.apiUrl + '/')
  }
  if (
    src.startsWith('http://') ||
    src.startsWith('https://') ||
    src.startsWith('blob:') ||
    src.startsWith('data:')
  ) {
    return src
  }
  const baseUrl = config.public.apiUrl
  if (src.startsWith('/')) {
    return `${baseUrl}${src}`
  }
  return `${baseUrl}/${src}`
}

const checkIsVideo = async () => {
  const src = props.media.src
  if (!src) {
    resolvedIsVideo.value = false
    transcodingFinished.value = false
    return
  }

  if (src.toLowerCase().endsWith('.mp4') || src.toLowerCase().endsWith('.webm')) {
    resolvedIsVideo.value = true
    transcodingFinished.value = true
    return
  }

  if (src.includes('/files/') || src.includes('/media/')) {
    const marker = src.includes('/media/') ? '/media/' : '/files/'
    let fileId = src.substring(src.lastIndexOf(marker) + marker.length)
    const qIdx = fileId.indexOf('?')
    const queryParams = qIdx !== -1 ? fileId.substring(qIdx) : ''
    if (qIdx !== -1) {
      fileId = fileId.substring(0, qIdx)
    }
    const plusIdx = fileId.indexOf('+')
    if (plusIdx !== -1) {
      fileId = fileId.substring(0, plusIdx)
    }
    const baseUrl = import.meta.env.VITE_API_URL || 'http://localhost:8080'

    try {
      const typeResp = await fetch(`${baseUrl}/videos/info/${fileId}`)
      if (typeResp.ok) {
        const info = await typeResp.json()
        if (info.isVideo) {
          resolvedIsVideo.value = true

          const hlsUrl = `${baseUrl}/videos/${fileId}/master.m3u8${queryParams}`
          const hlsResp = await fetch(hlsUrl, { method: 'HEAD' })
          const contentType = hlsResp.headers.get('content-type') || ''
          if (hlsResp.ok && !contentType.includes('text/html')) {
            transcodingFinished.value = true
          } else {
            transcodingFinished.value = false
          }
          return
        }
      }
    } catch (e) {
      console.warn('Failed to check media info:', e)
    }
  }

  resolvedIsVideo.value = false
  transcodingFinished.value = false
}

onMounted(() => {
  checkIsVideo()
})

watch(
  () => props.media.src,
  () => {
    checkIsVideo()
  },
)
</script>
