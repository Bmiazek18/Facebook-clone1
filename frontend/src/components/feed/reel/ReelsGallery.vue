<template>
  <div
    class="w-full min-h-[420px] py-4 my-4 pl-4 bg-theme-bg-secondary rounded-none lg:rounded-lg shadow-md border border-theme-border flex flex-col box-border"
  >
    <div class="flex justify-between items-center mb-4 pr-4 shrink-0">
      <div class="flex items-center text-gray-800">
        <div class="bg-red-500 p-1.5 rounded-md mr-2 text-white">
          <svg
            xmlns="http://www.w3.org/2000/svg"
            viewBox="0 0 24 24"
            fill="currentColor"
            class="w-5 h-5"
          >
            <path
              fill-rule="evenodd"
              d="M1.5 6a2.25 2.25 0 012.25-2.25h16.5A2.25 2.25 0 0122.5 6v12a2.25 2.25 0 01-2.25 2.25H3.75A2.25 2.25 0 011.5 18V6zM3 16.06V18c0 .414.336.75.75.75h16.5A.75.75 0 0021 18v-1.94l-2.69-2.689a1.5 1.5 0 00-2.12 0l-.88.879.97.97a.75.75 0 11-1.06 1.06l-5.16-5.159a1.5 1.5 0 00-2.12 0L3 16.061zm10.125-7.81a1.125 1.125 0 112.25 0 1.125 1.125 0 01-2.25 0z"
              clip-rule="evenodd"
            />
          </svg>
        </div>
        <h2 class="text-xl font-bold text-theme-text">{{ $t('feed.rolkiIKrotkieFilmy') }}</h2>
      </div>
      <button class="text-theme-text-secondary hover:bg-theme-bg-hover p-2 rounded-full transition">
        <DotsVerticalIcon :size="24" :fillColor="dotsFillColor" />
      </button>
    </div>

    <div class="relative group/container grow flex flex-col justify-center">
      <button
        v-if="!isStart"
        @click.stop="scrollLeft"
        class="absolute top-1/2 left-4 transform -translate-y-1/2 p-3 bg-theme-bg-secondary rounded-full shadow-lg border border-theme-border hover:bg-theme-bg-hover transition duration-150 z-20 flex cursor-pointer"
      >
        <ChevronLeftIcon :size="24" :fillColor="chevronFillColor" />
      </button>

      <div
        ref="carouselRef"
        class="flex overflow-x-auto pb-4 scrollbar-hide snap-x snap-mandatory"
        style="scroll-behavior: smooth"
      >
        <div
          v-for="reel in reels"
          :key="reel.id"
          class="shrink-0 mr-3 relative w-[180px] aspect-[9/16] rounded-xl overflow-hidden cursor-pointer group snap-start shadow-md hover:shadow-lg transition-all duration-300 bg-black"
          @click="openReel(reel.id)"
        >
      <video
  class="w-full h-full object-cover"
  :src="`${reel.videoSrc}#t=0.001`"
  muted
  loop
  playsinline
  preload="metadata"
  @mouseenter="handleMouseEnter"
  @mouseleave="handleMouseLeave"
></video>
          <div
            class="absolute inset-0 flex items-center justify-center pointer-events-none opacity-0 group-hover:opacity-100 transition-opacity"
          ></div>
        </div>

        <div
          v-if="reels.length > 0"
          class="flex flex-col items-center justify-center cursor-pointer w-[180px] aspect-[9/16] border border-theme-border rounded-xl shadow-sm hover:bg-theme-bg-hover transition duration-200 bg-theme-bg-tertiary shrink-0 snap-start"
          @click="openReel(reels[0]?.id)"
        >
          <div class="bg-theme-bg-secondary p-3 rounded-full shadow-sm mb-3">
            <ArrowRightIcon :size="24" :fillColor="chevronFillColor" />
          </div>
          <span class="text-theme-text font-semibold text-sm">{{ $t('feed.zobaczWszystkie') }}</span>
        </div>

        <div class="shrink-0 w-4"></div>
      </div>

      <button
        v-if="!isEnd"
        @click.stop="scrollRight"
        class="absolute top-1/2 right-4 transform -translate-y-1/2 p-3 bg-theme-bg-secondary rounded-full shadow-lg border border-theme-border hover:bg-theme-bg-hover transition duration-150 z-20 flex cursor-pointer"
      >
        <ChevronRightIcon :size="24" :fillColor="chevronFillColor" />
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useCarousel } from '@/composables/media/useCarousel'
import { useTheme } from '@/composables/shared/useTheme'

// --- IMPORT IKON ---
import DotsVerticalIcon from 'vue-material-design-icons/DotsVertical.vue'
import ChevronRightIcon from 'vue-material-design-icons/ChevronRight.vue'
import ChevronLeftIcon from 'vue-material-design-icons/ChevronLeft.vue'
import ArrowRightIcon from 'vue-material-design-icons/ArrowRight.vue'

const props = defineProps<{
  reels?: any[]
}>()

const router = useRouter()
const { isDark } = useTheme()

const chevronFillColor = computed(() => (isDark.value ? '#B0B3B8' : '#4B5563'))
const dotsFillColor = computed(() => (isDark.value ? '#B0B3B8' : '#65676B'))

const { carouselRef, isStart, isEnd, scrollLeft, scrollRight } = useCarousel(3)

const reels = computed(() => props.reels ?? [])

const handleMouseEnter = async (event: Event) => {
  const videoElement = event.target as HTMLVideoElement
  try {
    // Resetuj czas i odtwarzaj
    videoElement.currentTime = 0
    await videoElement.play()
  } catch (e) {
    // Ignoruj błędy autoplay (częste w przeglądarkach)
  }
}

const handleMouseLeave = (event: Event) => {
  const videoElement = event.target as HTMLVideoElement
  videoElement.pause()
  videoElement.currentTime = 0
}

const openReel = (reelId: string) => {
  router.push(`/reel/${reelId}`)
}
</script>

<style scoped>
.scrollbar-hide {
  -ms-overflow-style: none;
  scrollbar-width: none;
}

.scrollbar-hide::-webkit-scrollbar {
  display: none;
}
</style>
