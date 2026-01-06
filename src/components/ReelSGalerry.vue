<template>
  <div class="py-4 pl-4 bg-white rounded-none lg:rounded-lg shadow-md max-w-4xl mx-0 lg:mx-auto border border-gray-200">

    <div class="flex justify-between items-center mb-4 pr-4">
      <div class="flex items-center text-gray-800">
        <div class="bg-red-500 p-1.5 rounded-md mr-2 text-white">
          <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor" class="w-5 h-5">
            <path fill-rule="evenodd" d="M1.5 6a2.25 2.25 0 012.25-2.25h16.5A2.25 2.25 0 0122.5 6v12a2.25 2.25 0 01-2.25 2.25H3.75A2.25 2.25 0 011.5 18V6zM3 16.06V18c0 .414.336.75.75.75h16.5A.75.75 0 0021 18v-1.94l-2.69-2.689a1.5 1.5 0 00-2.12 0l-.88.879.97.97a.75.75 0 11-1.06 1.06l-5.16-5.159a1.5 1.5 0 00-2.12 0L3 16.061zm10.125-7.81a1.125 1.125 0 112.25 0 1.125 1.125 0 01-2.25 0z" clip-rule="evenodd" />
          </svg>
        </div>
        <h2 class="text-xl font-bold">Rolki i krótkie filmy</h2>
      </div>
      <button class="text-gray-500 hover:bg-gray-100 p-2 rounded-full transition">
        <DotsVerticalIcon :size="24" />
      </button>
    </div>

    <div class="relative group/container">

      <button
        v-if="!isStart"
        @click="scrollLeft"
        class="absolute top-1/2 left-4 transform -translate-y-1/2 p-3 bg-white rounded-full shadow-lg border hover:bg-gray-50 transition duration-150 z-20 flex"
      >
        <ChevronLeftIcon :size="24" fillColor="#4B5563" />
      </button>

      <div
        ref="carouselRef"
        class="flex overflow-x-scroll pb-4 scrollbar-hide snap-x snap-mandatory"
        style="scroll-behavior: smooth;"
      >
        <div
          v-for="reel in reels"
          :key="reel.id"
          class="shrink-0 mr-3 relative w-[180px] aspect-9/16 rounded-xl overflow-hidden cursor-pointer group snap-start shadow-md hover:shadow-lg transition-all duration-300 transform hover:scale-[1.02] bg-black"
          @click="openReel(reel.id)"
        >
          <video
            class="w-full h-full object-cover"
            :src="reel.videoSrc"
            muted
            loop
            playsinline
            preload="metadata"
            @mouseenter="handleMouseEnter"
            @mouseleave="handleMouseLeave"
          ></video>
        </div>

        <div
          v-if="reels.length > 0"
          class="flex flex-col items-center justify-center cursor-pointer w-[180px] aspect-9/16 border border-gray-200 rounded-xl shadow-sm hover:bg-gray-100 transition duration-200 bg-gray-50 shrink-0 snap-start"
          @click="openReel(reels[0]!.id)"
        >
          <div class="bg-white p-3 rounded-full shadow-sm mb-3">
             <ArrowRightIcon :size="24" fillColor="#4B5563" />
          </div>
          <span class="text-gray-700 font-semibold text-sm">Zobacz wszystkie</span>
        </div>

        <div class="shrink-0" style="width: 1rem;"></div>
      </div>

      <button
        v-if="!isEnd"
        @click="scrollRight"
        class="absolute top-1/2 right-4 transform -translate-y-1/2 p-3 bg-white rounded-full shadow-lg border hover:bg-gray-50 transition duration-150 z-20 flex"
      >
        <ChevronRightIcon :size="24" fillColor="#4B5563" />
      </button>

    </div>
  </div>
</template>

<script setup lang="ts">
import { useCarousel } from '../composables/useCarousel';
import { useRouter } from 'vue-router';
import { useReelsStore } from '@/stores/reels';

// --- IMPORT IKON ---
import DotsVerticalIcon from 'vue-material-design-icons/DotsVertical.vue';
import ChevronRightIcon from 'vue-material-design-icons/ChevronRight.vue';
import ChevronLeftIcon from 'vue-material-design-icons/ChevronLeft.vue';
import ArrowRightIcon from 'vue-material-design-icons/ArrowRight.vue';

const router = useRouter();
const reelsStore = useReelsStore();

const {
  carouselRef,
  isStart,
  isEnd,
  scrollLeft,
  scrollRight,
} = useCarousel(3);

// Pobierz reels ze store
const reels = reelsStore.reels;

const handleMouseEnter = async (event: Event) => {
  const videoElement = event.target as HTMLVideoElement;

  try {
    videoElement.currentTime = 0;

    await videoElement.play();
  } catch {
    // Obsługa błędu (np. brak zgody użytkownika na autoplay)
    console.warn('Nie można odtworzyć wideo automatycznie.');
  }
};

const handleMouseLeave = (event: Event) => {
  const videoElement = event.target as HTMLVideoElement;
  videoElement.pause();
  videoElement.currentTime = 0;
};

const openReel = (reelId: string) => {
  router.push({ name: 'reel', params: { id: reelId } });
};
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
