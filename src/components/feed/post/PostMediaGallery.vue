<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue'
import MediaItem from '@/components/feed/MediaItem.vue'
import type { ImageTagType } from '@/types/Post'

const props = defineProps<{
  media: {
    src: string
    altText?: string
    tags?: ImageTagType[]
    backgroundColor?: string
  }[]
  postId: string | number
}>()

const isHorizontal = ref(false)

// Tło dla pojedynczego obrazka jest wyliczane na backendzie i przesyłane w właściwości backgroundColor
const dominantColor = computed(() => {
  if (props.media && props.media.length === 1 && props.media[0]?.backgroundColor) {
    return props.media[0].backgroundColor
  }
  return 'transparent'
})

const getMediaUrl = (src: string) => {
  if (!src) return ''
  if (src.startsWith('http://localhost/files/') || src.startsWith('http://localhost/videos/') || src.startsWith('http://localhost/media/')) {
    src = src.replace('http://localhost/', 'http://localhost:8080/')
  }
  if (
    src.startsWith('http://') ||
    src.startsWith('https://') ||
    src.startsWith('blob:') ||
    src.startsWith('data:')
  ) {
    return src
  }
  const baseUrl = import.meta.env.VITE_API_URL || 'http://localhost:8080'
  if (src.startsWith('/')) {
    return `${baseUrl}${src}`
  }
  return `${baseUrl}/${src}`
}

function getPhotoId(mediaItem: any, index: number): string {
  if (!mediaItem || !mediaItem.src) return String(index)
  const src = mediaItem.src
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
}

function checkOrientation() {
  if (typeof window === 'undefined' || !props.media || props.media.length === 0) return
  const rawSrc = props.media[0]?.src
  if (!rawSrc) return

  const firstMediaSrc = getMediaUrl(rawSrc)
  const img = new Image()
  img.onload = () => {
    isHorizontal.value = img.naturalWidth > img.naturalHeight
  }
  img.src = firstMediaSrc
}

watch(
  () => props.media,
  () => { checkOrientation() },
  { deep: true }
)

onMounted(() => {
  checkOrientation()
})
</script>

<template>
  <div
    :style="`background-color: ${ media.length === 1 ? dominantColor : 'transparent' };`"
    class="transition-colors duration-500 ease-in-out relative"
  >
    <div
      :class="[
        'w-full mx-auto overflow-hidden flex items-center justify-center relative z-10 h-[680px]',

        media.length === 1 && !isHorizontal && !media[0].src.includes('mp4') ? 'max-w-[max(412.5px,_calc(-243.75px_+_75vh))]' : ''
      ]"
    >
      <div
        v-if="media.length === 1"
        class="w-full h-full flex items-center justify-center overflow-hidden"
      >
        <MediaItem
          v-if="media[0]"
          :media="media[0]"
          :post-id="postId"
          :is-single-video="media[0].src.includes('mp4')"
          :index="0"
          class="block w-full h-full relative object-contain object-center"
        />
      </div>

      <div v-else-if="media.length === 2" class="grid grid-cols-2 gap-1 w-full h-full">
        <MediaItem
          v-for="(item, idx) in media"
          :key="idx"
          :media="item"
          :post-id="postId"
          :index="idx"
          class="block w-full h-full bg-black/5 relative"
        />
      </div>

      <div v-else-if="media.length === 3" class="flex flex-col gap-1 w-full h-full">
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

      <div v-else-if="media.length === 4" class="flex flex-col gap-1 w-full h-full">
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

      <div v-else class="grid grid-cols-2 gap-1 w-full h-full">
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
