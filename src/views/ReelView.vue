<template>
  <div class="flex h-[calc(100vh-50px)] bg-black overflow-hidden font-sans mt-[50px]">

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
          <div class="flex items-end gap-2 md:gap-4 h-full max-h-[90vh] lg:max-h-[850px]">

            <div class="relative h-full aspect-[9/16] bg-[#222] rounded-lg shadow-2xl overflow-hidden flex-shrink">

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
              >
                <source :src="reel.videoSrc" type="video/mp4" />
              </video>

              <div class="absolute inset-0 pointer-events-none bg-linear-to-b from-transparent via-transparent to-black/60"></div>

              <div v-if="currentIndex !== index || !isPlaying" class="absolute inset-0 flex items-center justify-center pointer-events-none">
                <PlayIcon :size="48" class="text-white/50" />
              </div>

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
      :class="isCommentsOpen ? 'right-[416px]' : 'right-4'"
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

    <div
      v-if="isCommentsOpen && currentReel"
      class="fixed right-0 top-[50px] bottom-0 w-full sm:w-[350px] md:w-[400px] bg-white border-l border-[#dadde1] flex flex-col transition-all duration-300 animate-slide-in z-40"
    >
      </div>

  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, type ComponentPublicInstance } from 'vue';
import { useRouter } from 'vue-router';
import { useStoryShareStore } from '@/stores/storyShare';

// --- Imports ---
import ChevronUpIcon from 'vue-material-design-icons/ChevronUp.vue';
import ChevronDownIcon from 'vue-material-design-icons/ChevronDown.vue';
import ThumbUpIcon from 'vue-material-design-icons/ThumbUp.vue';
import CommentIcon from 'vue-material-design-icons/Comment.vue';
import ShareIcon from 'vue-material-design-icons/Share.vue';
import DotsHorizontalIcon from 'vue-material-design-icons/DotsHorizontal.vue';
import MusicNoteIcon from 'vue-material-design-icons/MusicNote.vue';
import PlayIcon from 'vue-material-design-icons/Play.vue';

const router = useRouter();
const storyShareStore = useStoryShareStore();

const isCommentsOpen = ref(false);
const isPlaying = ref(true);
const scrollContainerRef = ref<HTMLDivElement | null>(null);
const currentIndex = ref(0);
const videoRefs = ref<(HTMLVideoElement | null)[]>([]);
const reelRefs = ref<(HTMLElement | null)[]>([]);

const setVideoRef = (el: Element | ComponentPublicInstance | null, index: number) => {
  videoRefs.value[index] = el as HTMLVideoElement | null;
};

const setReelRef = (el: Element | ComponentPublicInstance | null, index: number) => {
  reelRefs.value[index] = el as HTMLElement | null;
};

const reels = ref([
  {
    id: 'reel-1',
    videoSrc: 'https://www.w3schools.com/html/mov_bbb.mp4',
    poster: 'https://images.unsplash.com/photo-1517836357463-d25dfeac3438?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80',
    likes: '224',
    comments: '1',
    shares: '0',
    caption: 'Cieszę się, że jestem jedną z nich',
    hashtags: '#dc #siłownia #treningnasiłowni #trenerpersonalny #fyp #ćwiczenia',
    music: 'RAYE - WHERE IS MY HUSBAND!',
    user: {
      id: 'user-2',
      name: 'Paulina Banaszek',
      avatar: 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?ixlib=rb-4.0.3&auto=format&fit=crop&w=100&q=80',
      isFollowing: false
    },
    isLiked: false
  },
  {
    id: 'reel-2',
    videoSrc: 'https://www.w3schools.com/html/movie.mp4',
    poster: 'https://images.unsplash.com/photo-1571019614242-c5c5dee9f50b?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80',
    likes: '1.2k',
    comments: '45',
    shares: '12',
    caption: 'Nowy trening na dziś! 💪',
    hashtags: '#fitness #workout #gym #motivation',
    music: 'Eye of the Tiger - Survivor',
    user: {
      id: 'user-3',
      name: 'Fitness Master',
      avatar: 'https://images.unsplash.com/photo-1568602471122-7832951cc4c5?ixlib=rb-4.0.3&auto=format&fit=crop&w=100&q=80',
      isFollowing: true
    },
    isLiked: true
  },
  {
    id: 'reel-3',
    videoSrc: 'https://test-videos.co.uk/vids/bigbuckbunny/mp4/h264/720/Big_Buck_Bunny_720_10s_1MB.mp4',
    poster: 'https://images.unsplash.com/photo-1518611012118-696072aa579a?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80',
    likes: '856',
    comments: '23',
    shares: '8',
    caption: 'Wieczorny spacer z widokiem 🌅',
    hashtags: '#nature #sunset #relax #weekend',
    music: 'Sunset Lover - Petit Biscuit',
    user: {
      id: 'user-4',
      name: 'Travel Diary',
      avatar: 'https://images.unsplash.com/photo-1494790108377-be9c29b29330?ixlib=rb-4.0.3&auto=format&fit=crop&w=100&q=80',
      isFollowing: false
    },
    isLiked: false
  },
  {
    id: 'reel-4',
    videoSrc: 'https://samplelib.com/lib/preview/mp4/sample-5s.mp4',
    poster: 'https://images.unsplash.com/photo-1504674900247-0877df9cc836?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80',
    likes: '3.4k',
    comments: '128',
    shares: '67',
    caption: 'Przepis na idealne śniadanie 🍳',
    hashtags: '#food #cooking #breakfast #recipe',
    music: 'Good Morning - Kanye West',
    user: {
      id: 'user-5',
      name: 'Chef Anna',
      avatar: 'https://images.unsplash.com/photo-1438761681033-6461ffad8d80?ixlib=rb-4.0.3&auto=format&fit=crop&w=100&q=80',
      isFollowing: true
    },
    isLiked: false
  }
]);

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

const shareToStory = (reel: typeof reels.value[0]) => {
  storyShareStore.setReelToShare({
    id: reel.id,
    authorName: reel.user.name,
    authorAvatar: reel.user.avatar,
    videoSrc: reel.videoSrc,
    poster: reel.poster,
    caption: reel.caption,
  });
  router.push('createReel');
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
  // Play first video
  setTimeout(() => {
    const firstVideo = videoRefs.value[0];
    if (firstVideo) {
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
</style>
