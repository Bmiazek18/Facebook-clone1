<template>
  <!-- Shared Post -->
  <div v-if="post.sharedContent?.type === 'post' && originalPost" class="mx-3 mb-3 mt-2 rounded-lg overflow-hidden">
    <PostItem :post="originalPost" :is-shared="true" />
  </div>

  <!-- Shared Reel -->
  <div v-if="post.sharedContent?.type === 'reel' && sharedReel"
       class="mb-3 mt-2 relative w-auto h-[800px] bg-gradient-to-b from-[#5c6b55] to-[#2e3b2b] rounded-xl overflow-hidden shadow-lg border border-gray-700/30 group cursor-pointer"
       @click="router.push(`/reel/${sharedReel.id}`)">

    <div class="absolute inset-0 w-full h-full">
      <video
        ref="reelVideoRef"
        class="w-[70%] mx-auto h-full object-cover block"
        :src="sharedReel.videoSrc"
        :poster="sharedReel.poster"
        autoplay
        loop
        playsinline
        :muted="isReelMuted"
      ></video>
      <div class="absolute inset-0 bg-gradient-to-b from-black/40 via-transparent to-black/60 pointer-events-none"></div>
    </div>

    <div class="absolute top-4 left-4 flex items-center gap-3 z-20 pointer-events-none">
      <div class="w-10 h-10 rounded-full border-2 border-white/20 overflow-hidden bg-gray-300">
        <img :src="reelAuthor?.avatar" alt="Avatar" class="w-full h-full object-cover" />
      </div>

      <div class="flex flex-col text-white drop-shadow-md">
        <span class="font-bold text-sm leading-tight flex items-center gap-1">
          {{ reelAuthor?.name }} <span class="text-gray-300 font-normal text-xs opacity-90">• Obserwuj</span>
        </span>
        <div class="flex items-center gap-1 text-xs text-gray-200 opacity-80 mt-0.5">
          <Earth :size="12" class="text-gray-200"/>
        </div>
      </div>
    </div>

    <!-- Control buttons -->
    <div class="absolute top-4 right-4 z-20 flex gap-2">
      <button
        @click.stop="toggleReelMute"
        class="p-2 bg-black/20 backdrop-blur-sm rounded-full text-white hover:bg-black/40 transition-colors"
      >
        <VolumeHigh v-if="!isReelMuted" :size="20" />
        <VolumeMute v-else :size="20" />
      </button>
    </div>

    <div class="absolute bottom-6 left-4 right-16 z-20">
      <div class="flex items-center bg-black/30 backdrop-blur-md self-start px-3 py-2 rounded-full max-w-[220px] text-white border border-white/10">
        <div class="shrink-0 mr-2 flex items-center justify-center">
          <MusicNote :size="16" class="animate-pulse-slow" />
        </div>

        <div class="overflow-hidden w-full relative h-[16px] flex items-center">
          <div class="w-full overflow-hidden mask-[linear-gradient(90deg,transparent_0%,white_10%,white_90%,transparent_100%)]">
            <div class="animate-marquee whitespace-nowrap text-xs font-medium tracking-wide">
              {{ reelAuthor?.name }} • Oryginalny dźwięk &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; {{ reelAuthor?.name }} • Oryginalny dźwięk &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>

  <!-- Shared Event -->
  <div v-if="post.sharedContent?.type === 'event' && sharedEvent"
       class="mb-4 mt-2 overflow-hidden cursor-pointer bg-[#f0f2f5] border-b border-[#dadde1] transition-colors group"
       @click="router.push(`/event/${sharedEvent.id}`)">

    <div class="relative w-full aspect-[1.91/1] bg-gray-100 dark:bg-[#3A3B3C]">
      <img
        v-if="sharedEvent.images && sharedEvent.images[0]"
        :src="sharedEvent.images[0]"
        alt="Event cover"
        class="w-full h-full object-cover"
      />
    </div>

    <div class="p-3 flex justify-between items-center">
      <div class="flex-1 min-w-0 pr-3">
        <div class="text-[#F02849] text-[13px] font-semibold uppercase mb-0.5 tracking-wide leading-none">
          {{ sharedEvent.date || 'SOB, 16 MAJ O 15:00' }}
        </div>
        <h3 class="text-[#050505] dark:text-[#E4E6EB] font-bold text-[17px] leading-snug truncate mb-0.5">
          {{ sharedEvent.title || sharedEvent.name }}
        </h3>
        <div class="text-[#65676B] dark:text-[#B0B3B8] text-[13px] truncate">
          {{ sharedEvent.locationName || sharedEvent.location }}
        </div>
      </div>

      <button
        @click.stop="toggleInterest"
        class="shrink-0 h-9 px-3 rounded-[6px] text-[15px] font-semibold flex items-center justify-center gap-1.5 transition-colors z-10 border border-transparent"
        :class="[
          isInterested
            ? 'bg-[#E7F3FF] text-[#1877F2] hover:bg-[#DBEBFF]'
            : 'bg-[#E4E6EB] dark:bg-[#3A3B3C] text-[#050505] dark:text-[#E4E6EB] hover:bg-[#D8DADF] dark:hover:bg-[#4E4F50]'
        ]"
      >
        <component
          :is="isInterested ? Star : StarOutline"
          :size="20"
        />

        <span class="hidden sm:inline">
          {{ isInterested ? 'Interesuję się' : 'Zainteresowany(a)' }}
        </span>

        <ChevronDown
          v-if="isInterested"
          :size="20"
        />
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, defineAsyncComponent } from 'vue'
import { useRouter } from 'vue-router'

import ChevronDown from 'vue-material-design-icons/ChevronDown.vue'
import StarOutline from 'vue-material-design-icons/StarOutline.vue'
import Star from 'vue-material-design-icons/Star.vue'
import Earth from 'vue-material-design-icons/Earth.vue'
import VolumeHigh from 'vue-material-design-icons/VolumeHigh.vue'
import VolumeMute from 'vue-material-design-icons/VolumeMute.vue'
import MusicNote from 'vue-material-design-icons/MusicNote.vue'

import { usePostsStore } from '@/stores/posts'
import { useEventsStore } from '@/stores/events'
import { useReelsStore } from '@/stores/reels'
import type { Post } from '@/types/Post'

// Import PostItem for recursive rendering
const PostItem = defineAsyncComponent(() => import('./PostItem.vue'))

const props = defineProps<{
  post: Post
}>()

const router = useRouter()
const postsStore = usePostsStore()
const eventsStore = useEventsStore()
const reelsStore = useReelsStore()

// Shared content computed properties
const originalPost = computed(() => {
  if (props.post.sharedContent?.type === 'post' && props.post.sharedContent.originalId) {
    return postsStore.getPostById(props.post.sharedContent.originalId)
  }
  return undefined
})

const sharedEvent = computed(() => {
  if (props.post.sharedContent?.type === 'event' && props.post.sharedContent.originalId) {
    return eventsStore.getEventById(props.post.sharedContent.originalId)
  }
  return undefined
})

const sharedReel = computed(() => {
  if (props.post.sharedContent?.type === 'reel' && props.post.sharedContent.originalId) {
    return reelsStore.getReelById(props.post.sharedContent.originalId)
  }
  return undefined
})

// Add computed for reel author to keep template clean
import { getUserById } from '@/data/users';
const reelAuthor = computed(() => {
    if (sharedReel.value) {
        return getUserById(sharedReel.value.authorId);
    }
    return null;
});

// Reel controls
const isReelMuted = ref(true)
const reelVideoRef = ref<HTMLVideoElement | null>(null)

const toggleReelMute = () => {
  isReelMuted.value = !isReelMuted.value
  if (reelVideoRef.value) {
    reelVideoRef.value.muted = isReelMuted.value
  }
}

// Event interest
const isInterested = ref(false)

const toggleInterest = () => {
  isInterested.value = !isInterested.value
}
</script>

<style scoped>
.animate-marquee {
  display: inline-block;
  white-space: nowrap;
  animation: scroll-left 12s linear infinite;
  padding-left: 100%;
}

@keyframes scroll-left {
  0% {
    transform: translateX(0);
  }
  100% {
    transform: translateX(-100%);
  }
}
</style>
