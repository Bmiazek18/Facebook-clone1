<template>

  <div class="flex h-[calc(100vh)] bg-black overflow-hidden font-sans">

    <!-- Header with Close button and NavbarRight -->
    <div class="absolute top-0 left-0 right-0 z-50 flex items-center justify-between px-4 py-3">
      <button @click="goBack" class="p-2 hover:bg-white/10 rounded-full transition-colors">
        <CloseIcon :size="28" fillColor="#FFFFFF" />
      </button>
      <NavbarRight />
    </div>

    <div class="flex-1 flex justify-center relative transition-all duration-300">

      <div
        ref="scrollContainerRef"
        class="h-full w-full overflow-y-scroll snap-y snap-mandatory scrollbar-hide"
        @scroll="handleScroll"
      >
        <div
          v-for="(reel, index) in reels"
          :key="reel.id"
          :ref="el => setReelRef(el, index)"
          class="h-full w-full flex items-center justify-center snap-start snap-always py-4"
        >
          <div class="flex items-end gap-2 md:gap-4 h-full md:max-h-[90vh] max-h-[850px]">

            <div
              class="relative h-full aspect-9/16 bg-[#222] rounded-lg shadow-2xl overflow-hidden shrink"
              @mouseenter="isVideoHovered = true"
              @mouseleave="isVideoHovered = false"
            >

              <video
                :ref="el => setVideoRef(el, index)"
                class="w-full h-full object-cover cursor-pointer"
                :poster="reel.poster"
                :src="reel.videoSrc"
                loop
                muted
                autoplay
                playsinline
                @click="togglePlay(index)"
                @timeupdate="() => updateProgress(index)"
              >
                <source :src="reel.videoSrc" type="video/mp4" />
              </video>

              <!-- Play/Pause and Volume controls -->
              <Transition name="fade">
                <div v-if="currentIndex === index && isVideoHovered" class="absolute top-4 left-4 flex gap-2 z-20">
                <button
                  @click.stop="togglePlay(index)"
                  class="p-2 bg-black/50 hover:bg-black/70 rounded-full transition-colors backdrop-blur-sm"
                >
                  <PlayIcon v-if="!isPlaying" :size="24" fillColor="#FFFFFF" />
                  <PauseIcon v-else :size="24" fillColor="#FFFFFF" />
                </button>

                <!-- Volume control with slider -->
                <div
                  class="relative flex items-center gap-2"
                  @mouseenter="showVolumeSlider = true"
                  @mouseleave="showVolumeSlider = false"
                >
                  <button
                    @click.stop="toggleMute"
                    class="p-2 bg-black/50 hover:bg-black/70 rounded-full transition-colors backdrop-blur-sm shrink-0"
                  >
                    <VolumeHighIcon v-if="!isMuted" :size="24" fillColor="#FFFFFF" />
                    <VolumeMuteIcon v-else :size="24" fillColor="#FFFFFF" />
                  </button>

                  <!-- Volume slider -->
                  <Transition name="fade-slide">
                    <div
                      v-if="showVolumeSlider"
                      class="flex items-center gap-2 bg-black/70 backdrop-blur-sm rounded-full px-3 py-2"
                      @click.stop
                    >
                      <input
                        type="range"
                        min="0"
                        max="1"
                        step="0.01"
                        :value="volume"
                        @input="changeVolume"
                        @click.stop
                        class="w-20 h-1 bg-white/30 rounded-lg appearance-none cursor-pointer volume-slider"
                      />
                      <span class="text-white text-xs font-medium min-w-8">{{ Math.round(volume * 100) }}%</span>
                    </div>
                  </Transition>
                </div>
                </div>
              </Transition>

              <!-- Progress bar -->
              <Transition name="fade">
                <div v-if="currentIndex === index && isVideoHovered" class="absolute bottom-0 left-0 right-0 h-1 bg-white/30 z-20">
                  <div
                    class="h-full bg-white transition-all duration-100"
                    :style="{ width: `${videoProgress[index] || 0}%` }"
                  ></div>
                </div>
              </Transition>

              <div class="absolute inset-0 pointer-events-none bg-linear-to-b from-transparent via-transparent to-black/60"></div>

              <div class="absolute bottom-4 left-4 right-4 text-white z-10">
                <div class="flex items-center gap-2 mb-2">
                  <img :src="reel.user.avatar" class="w-8 h-8 rounded-full object-cover border border-white/20" />
                  <span class="font-bold text-[14px] md:text-[15px] hover:underline cursor-pointer truncate">{{ reel.user.name }}</span>
                  <button v-if="!reel.user.isFollowing" class="ml-1 text-white font-bold text-[14px] hover:underline whitespace-nowrap">• Obserwuj</button>
                </div>
                <p class="text-[13px] md:text-[15px] leading-snug line-clamp-2 pr-4 text-shadow-sm">
                  {{ reel.caption }} <span class="text-gray-300">{{ reel.hashtags }}</span>
                </p>
                <div class="flex items-center gap-2 mt-2 text-xs font-medium">
                   <MusicNoteIcon :size="14" />
                   <div class="overflow-hidden w-full">
                     <marquee class="max-w-[150px]">{{ reel.music }}</marquee>
                   </div>
                </div>
              </div>
            </div>

            <div class="flex flex-col items-center gap-3 md:gap-4 text-white pb-2">
              <div class="flex flex-col items-center gap-1 cursor-pointer group">
                <div class="p-2 md:p-3 bg-[#3a3b3c] group-hover:bg-[#4e4f50] rounded-full transition-colors">
                   <ThumbUpIcon :size="20" :fillColor="reel.isLiked ? '#1877f2' : 'white'" />
                </div>
                <span class="text-[10px] md:text-xs font-bold text-gray-300">{{ reel.likes }}</span>
              </div>

              <div @click="toggleComments" class="flex flex-col items-center gap-1 cursor-pointer group">
                <div class="p-2 md:p-3 bg-[#3a3b3c] group-hover:bg-[#4e4f50] rounded-full transition-colors"
                     :class="{'bg-white text-black hover:bg-white': isCommentsOpen}">
                   <CommentIcon :size="20" :fillColor="isCommentsOpen ? 'black' : 'white'" />
                </div>
                <span class="text-[10px] md:text-xs font-bold text-gray-300">{{ reel.comments }}</span>
              </div>

              <div @click="shareToStory(reel)" class="flex flex-col items-center gap-1 cursor-pointer group">
                <div class="p-2 md:p-3 bg-[#3a3b3c] group-hover:bg-[#4e4f50] rounded-full transition-colors">
                   <ShareIcon :size="20" />
                </div>
                <span class="text-[10px] md:text-xs font-bold text-gray-300">{{ reel.shares }}</span>
              </div>

              <div class="mt-1 cursor-pointer group">
                 <div class="p-2 md:p-3 bg-[#3a3b3c] group-hover:bg-[#4e4f50] rounded-full transition-colors">
                    <DotsHorizontalIcon :size="20" />
                 </div>
              </div>
            </div>

          </div>
        </div>
      </div>

    </div>

    <div
      class="fixed top-1/2 -translate-y-1/2 hidden sm:flex flex-col gap-4 z-30 transition-all duration-300"
      :class="{
        'right-[366px] sm:right-[366px] md:right-[416px] lg:right-[506px]': isCommentsOpen,
        'right-4': !isCommentsOpen
      }"
    >
      <button
        @click="goToReel('up')"
        :disabled="!canGoUp"
        class="w-10 h-10 md:w-12 md:h-12 bg-[#3a3b3c] hover:bg-[#4e4f50] rounded-full flex items-center justify-center text-white transition-colors border border-gray-700 disabled:opacity-30"
      >
        <ChevronUpIcon :size="28" />
      </button>
      <button
        @click="goToReel('down')"
        :disabled="!canGoDown"
        class="w-10 h-10 md:w-12 md:h-12 bg-[#3a3b3c] hover:bg-[#4e4f50] rounded-full flex items-center justify-center text-white transition-colors border border-gray-700 disabled:opacity-30"
      >
        <ChevronDownIcon :size="28" />
      </button>
    </div>

    <!-- Right info panel -->
    <ReelInfoPanel
      v-if="isCommentsOpen && currentReel"
      :reel="currentReel"
      class="fixed right-0 top-0 bottom-0 w-full sm:w-[350px] md:w-[400px] lg:w-[490px] transition-all duration-300 animate-slide-in z-40"
    />

    <!-- Share Modal -->
    <BaseModal v-if="showShareModal" @close="closeShareModal" title="Udostępnij">
      <StoryShareModal
        :reel="selectedReelToShare"
        @close="closeShareModal"
      />
    </BaseModal>

  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, type ComponentPublicInstance, watch } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { useReelsStore } from '@/stores/reels';

// --- Imports ---
import ChevronUpIcon from 'vue-material-design-icons/ChevronUp.vue';
import ChevronDownIcon from 'vue-material-design-icons/ChevronDown.vue';
import ThumbUpIcon from 'vue-material-design-icons/ThumbUp.vue';
import CommentIcon from 'vue-material-design-icons/Comment.vue';
import ShareIcon from 'vue-material-design-icons/Share.vue';
import DotsHorizontalIcon from 'vue-material-design-icons/DotsHorizontal.vue';
import MusicNoteIcon from 'vue-material-design-icons/MusicNote.vue';
import PlayIcon from 'vue-material-design-icons/Play.vue';
import PauseIcon from 'vue-material-design-icons/Pause.vue';
import VolumeHighIcon from 'vue-material-design-icons/VolumeHigh.vue';
import VolumeMuteIcon from 'vue-material-design-icons/VolumeMute.vue';
import CloseIcon from 'vue-material-design-icons/Close.vue';
import NavbarRight from '@/components/NavbarRight.vue';
import BaseModal from '@/components/BaseModal.vue';
import StoryShareModal from '@/components/StoryShareModal.vue';
import ReelInfoPanel from '@/components/reel/ReelInfoPanel.vue';

const router = useRouter();
const route = useRoute();
const reelsStore = useReelsStore();

const props = defineProps<{
  id?: string;
}>();

const isCommentsOpen = ref(false);
const isPlaying = ref(true);
const isMuted = ref(true);
const volume = ref(1);
const showVolumeSlider = ref(false);
const isVideoHovered = ref(false);
const showShareModal = ref(false);
const selectedReelToShare = ref<typeof reels.value[0] | null>(null);
const videoProgress = ref<number[]>([]);
const scrollContainerRef = ref<HTMLDivElement | null>(null);
const currentIndex = ref(0);
const videoRefs = ref<(HTMLVideoElement | null)[]>([]);
const reelRefs = ref<(HTMLElement | null)[]>([]);

// Pobierz wszystkie reels ze store
const reels = computed(() => reelsStore.reels);

// Ustaw początkowy index na podstawie ID z route
onMounted(() => {
  if (props.id) {
    const index = reelsStore.getReelIndex(props.id);
    if (index !== -1) {
      currentIndex.value = index;
      // Poczekaj na następny tick aby DOM się wyrenderował
      setTimeout(() => {
        scrollToReel(index);
      }, 100);
    }
  }

  // Preload następnego video dla płynnej animacji
  preloadNextVideo();
});

// Preload następnego video
const preloadNextVideo = () => {
  if (currentIndex.value < reels.value.length - 1) {
    const nextReel = reels.value[currentIndex.value + 1];
    if (nextReel) {
      const video = document.createElement('video');
      video.src = nextReel.videoSrc;
      video.preload = 'auto';
    }
  }
};

// Watch zmiany currentIndex i aktualizuj URL + preload
watch(currentIndex, (newIndex) => {
  const newReel = reels.value[newIndex];
  if (newReel && route.params.id !== newReel.id) {
    router.replace({ name: 'reel', params: { id: newReel.id } });
  }
  preloadNextVideo();
});

const setVideoRef = (el: Element | ComponentPublicInstance | null, index: number) => {
  videoRefs.value[index] = el as HTMLVideoElement | null;
};

const setReelRef = (el: Element | ComponentPublicInstance | null, index: number) => {
  reelRefs.value[index] = el as HTMLElement | null;
};

const currentReel = computed(() => reels.value[currentIndex.value]!);

const canGoUp = computed(() => currentIndex.value > 0);
const canGoDown = computed(() => currentIndex.value < reels.value.length - 1);

const toggleComments = () => {
  isCommentsOpen.value = !isCommentsOpen.value;
};

const togglePlay = (index?: number) => {
  const idx = index ?? currentIndex.value;
  const video = videoRefs.value[idx];
  if (video) {
    if (video.paused) {
      video.play();
      isPlaying.value = true;
    } else {
      video.pause();
      isPlaying.value = false;
    }
  }
};

const toggleMute = () => {
  const video = videoRefs.value[currentIndex.value];
  if (video) {
    video.muted = !video.muted;
    isMuted.value = video.muted;
    if (!video.muted && volume.value === 0) {
      volume.value = 0.5;
      video.volume = 0.5;
    }
  }
};

const changeVolume = (event: Event) => {
  const target = event.target as HTMLInputElement;
  const newVolume = parseFloat(target.value);
  volume.value = newVolume;

  const video = videoRefs.value[currentIndex.value];
  if (video) {
    video.volume = newVolume;
    video.muted = newVolume === 0;
    isMuted.value = newVolume === 0;
  }
};

const updateProgress = (index: number) => {
  const video = videoRefs.value[index];
  if (video) {
    const progress = (video.currentTime / video.duration) * 100;
    videoProgress.value[index] = progress;
  }
};

const goBack = () => {
  router.back();
};

const shareToStory = (reel: typeof reels.value[0]) => {
  selectedReelToShare.value = reel;
  showShareModal.value = true;
};

const closeShareModal = () => {
  showShareModal.value = false;
  selectedReelToShare.value = null;
};

const scrollToReel = (index: number) => {
  const reelElement = reelRefs.value[index];
  if (reelElement && scrollContainerRef.value) {
    reelElement.scrollIntoView({ behavior: 'smooth' });
  }
};

const goToReel = (direction: 'up' | 'down') => {
  if (direction === 'up' && !canGoUp.value) return;
  if (direction === 'down' && !canGoDown.value) return;

  const newIndex = direction === 'up' ? currentIndex.value - 1 : currentIndex.value + 1;
  scrollToReel(newIndex);
};

const handleScroll = () => {
  if (!scrollContainerRef.value) return;

  const container = scrollContainerRef.value;
  const scrollTop = container.scrollTop;
  const itemHeight = container.clientHeight;
  const newIndex = Math.round(scrollTop / itemHeight);

  if (newIndex !== currentIndex.value && newIndex >= 0 && newIndex < reels.value.length) {
    // Pause previous video
    const prevVideo = videoRefs.value[currentIndex.value];
    if (prevVideo) {
      prevVideo.pause();
    }

    currentIndex.value = newIndex;

    // Play new video
    const newVideo = videoRefs.value[newIndex];
    if (newVideo) {
      newVideo.play().catch(() => {});
      isPlaying.value = true;
    }
  }
};

const handleKeydown = (event: KeyboardEvent) => {
  if (event.key === 'ArrowUp') {
    event.preventDefault();
    goToReel('up');
  } else if (event.key === 'ArrowDown') {
    event.preventDefault();
    goToReel('down');
  } else if (event.key === ' ') {
    event.preventDefault();
    togglePlay();
  }
};

onMounted(() => {
  // Initialize progress array
  videoProgress.value = new Array(reels.value.length).fill(0);

  // Play first video and set initial volume
  setTimeout(() => {
    const firstVideo = videoRefs.value[0];
    if (firstVideo) {
      firstVideo.volume = volume.value;
      firstVideo.play().catch(() => isPlaying.value = false);
    }
  }, 100);

  window.addEventListener('keydown', handleKeydown);
});

onUnmounted(() => {
  window.removeEventListener('keydown', handleKeydown);
});
</script>

<style scoped>
video::-webkit-media-controls {
  display: none !important;
}

/* Hide scrollbar but keep functionality */
.scrollbar-hide {
  -ms-overflow-style: none;
  scrollbar-width: none;
}
.scrollbar-hide::-webkit-scrollbar {
  display: none;
}

@keyframes slideIn {
  from { transform: translateX(100%); opacity: 0; }
  to { transform: translateX(0); opacity: 1; }
}
.animate-slide-in {
  animation: slideIn 0.3s cubic-bezier(0.16, 1, 0.3, 1);
}

.text-shadow-sm {
  text-shadow: 0 1px 2px rgba(0,0,0,0.5);
}

/* Volume slider styling */
.volume-slider::-webkit-slider-thumb {
  appearance: none;
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background: white;
  cursor: pointer;
  box-shadow: 0 0 2px rgba(0,0,0,0.5);
}

.volume-slider::-moz-range-thumb {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background: white;
  cursor: pointer;
  border: none;
  box-shadow: 0 0 2px rgba(0,0,0,0.5);
}

.volume-slider::-webkit-slider-runnable-track {
  background: linear-gradient(to right, white 0%, white var(--value), rgba(255,255,255,0.3) var(--value), rgba(255,255,255,0.3) 100%);
  height: 4px;
  border-radius: 2px;
}

/* Fade slide animation */
.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: all 0.2s ease;
}

.fade-slide-enter-from {
  opacity: 0;
  transform: translateX(-10px);
}

.fade-slide-leave-to {
  opacity: 0;
  transform: translateX(-10px);
}

/* Fade animation for controls */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
