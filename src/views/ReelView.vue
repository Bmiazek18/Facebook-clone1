<template>
  <div class="flex h-screen pt-[50px] bg-black overflow-hidden font-sans">

    <div class="flex-1 flex justify-center relative transition-all duration-300">

      <!-- Scrollable reels container -->
      <div
        ref="scrollContainerRef"
        class="h-full overflow-y-scroll snap-y snap-mandatory scrollbar-hide"
        @scroll="handleScroll"
      >
        <div
          v-for="(reel, index) in reels"
          :key="reel.id"
          :ref="el => setReelRef(el, index)"
          class="h-full flex items-center justify-center snap-start snap-always"
        >
          <div class="flex items-end gap-4">
            <!-- Video container -->
            <div class="relative w-[647px] aspect-9/16 bg-[#222] rounded-lg shadow-2xl overflow-hidden">

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
                <PlayIcon :size="64" class="text-white/50" />
              </div>

              <div class="absolute bottom-4 left-4 right-4 text-white z-10">
                <div class="flex items-center gap-2 mb-2">
                  <img :src="reel.user.avatar" class="w-8 h-8 rounded-full object-cover border border-white/20" />
                  <span class="font-bold text-[15px] hover:underline cursor-pointer">{{ reel.user.name }}</span>
                  <button v-if="!reel.user.isFollowing" class="ml-1 text-white font-bold text-[15px] hover:underline">• Obserwuj</button>
                </div>
                <p class="text-[15px] leading-snug line-clamp-2 pr-4 text-shadow-sm">
                  {{ reel.caption }} <span class="text-gray-300">{{ reel.hashtags }}</span>
                </p>
                <div class="flex items-center gap-2 mt-2 text-sm font-medium">
                   <MusicNoteIcon :size="14" />
                   <marquee class="max-w-[200px]">{{ reel.music }}</marquee>
                </div>
              </div>

            </div>

            <!-- Side buttons -->
            <div class="flex flex-col items-center gap-4 text-white pb-4">

              <div class="flex flex-col items-center gap-1 cursor-pointer group">
                <div class="p-3 bg-[#3a3b3c] group-hover:bg-[#4e4f50] rounded-full transition-colors">
                   <ThumbUpIcon :size="24" :fillColor="reel.isLiked ? '#1877f2' : 'white'" />
                </div>
                <span class="text-xs font-bold text-gray-300">{{ reel.likes }}</span>
              </div>

              <div @click="toggleComments" class="flex flex-col items-center gap-1 cursor-pointer group">
                <div class="p-3 bg-[#3a3b3c] group-hover:bg-[#4e4f50] rounded-full transition-colors"
                     :class="{'bg-white text-black hover:bg-white': isCommentsOpen}">
                   <CommentIcon :size="24" :fillColor="isCommentsOpen ? 'black' : 'white'" />
                </div>
                <span class="text-xs font-bold text-gray-300">{{ reel.comments }}</span>
              </div>

              <div @click="shareToStory(reel)" class="flex flex-col items-center gap-1 cursor-pointer group">
                <div class="p-3 bg-[#3a3b3c] group-hover:bg-[#4e4f50] rounded-full transition-colors">
                   <ShareIcon :size="24" />
                </div>
                <span class="text-xs font-bold text-gray-300">{{ reel.shares }}</span>
              </div>

              <div class="mt-1 cursor-pointer group">
                 <div class="p-3 bg-[#3a3b3c] group-hover:bg-[#4e4f50] rounded-full transition-colors">
                    <DotsHorizontalIcon :size="24" />
                 </div>
              </div>
            </div>

          </div>
        </div>
      </div>

    </div>

    <!-- Navigation arrows -->
    <div
      class="fixed top-1/2 -translate-y-1/2 flex flex-col gap-4 z-30 transition-all duration-300"
      :class="isCommentsOpen ? 'right-[416px]' : 'right-4'"
    >
      <button
        @click="goToReel('up')"
        :disabled="!canGoUp"
        class="w-12 h-12 bg-[#3a3b3c] hover:bg-[#4e4f50] rounded-full flex items-center justify-center text-white transition-colors border border-gray-700 disabled:opacity-30 disabled:cursor-not-allowed"
      >
        <ChevronUpIcon :size="32" />
      </button>
      <button
        @click="goToReel('down')"
        :disabled="!canGoDown"
        class="w-12 h-12 bg-[#3a3b3c] hover:bg-[#4e4f50] rounded-full flex items-center justify-center text-white transition-colors border border-gray-700 disabled:opacity-30 disabled:cursor-not-allowed"
      >
        <ChevronDownIcon :size="32" />
      </button>
    </div>

    <div
      v-if="isCommentsOpen && currentReel"
      class="w-[400px] bg-white border-l border-[#dadde1] flex flex-col transition-all duration-300 animate-slide-in relative z-40"
    >
      <button @click="toggleComments" class="absolute top-2 right-2 text-gray-500 hover:bg-gray-100 p-1 rounded-full z-50">
         <CloseIcon :size="24" />
      </button>

      <div class="p-4 border-b border-gray-200 overflow-y-auto max-h-[30vh]">
        <div class="flex gap-3 mb-3">
           <img :src="currentReel.user.avatar" class="w-10 h-10 rounded-full object-cover border border-gray-200" />
           <div class="flex flex-col">
             <div class="flex flex-wrap items-center gap-1 leading-tight">
               <span class="font-bold text-[#050505] text-[15px] hover:underline cursor-pointer">
                 {{ currentReel.user.name }}
               </span>
               <CheckDecagramIcon :size="14" class="text-blue-500" />
               <span class="text-[#1877f2] font-semibold text-[15px] cursor-pointer hover:underline">
                 • Obserwuj
               </span>
             </div>
             <div class="flex items-center gap-1 text-[#65676b] text-[12px] mt-0.5">
               <MusicNoteIcon :size="12" />
               <span>{{ currentReel.music }}</span>
             </div>
           </div>
        </div>

        <div class="text-[15px] text-[#050505] leading-normal whitespace-pre-line pl-[52px]">
          {{ currentReel.caption }}
          <br />
          <span class="text-[#1877f2] cursor-pointer hover:underline">
             {{ currentReel.hashtags }}
          </span>
        </div>
      </div>

      <div class="flex-1 flex flex-col items-center justify-center text-center p-8 bg-white">
        <div class="mb-4 opacity-50">
           <FileDocumentIcon :size="72" class="text-[#bcc0c4]" />
        </div>
        <h3 class="text-[#65676b] font-bold text-[20px] mb-2">
          Nie ma jeszcze komentarzy
        </h3>
        <p class="text-[#65676b] text-[15px]">
          Bądź pierwszą osobą, która to skomentuje.
        </p>
      </div>

      <div class="p-4 border-t border-[#dadde1] bg-white sticky bottom-0">
        <div class="flex items-center gap-2 bg-[#f0f2f5] rounded-full px-4 py-2.5">
          <input
            type="text"
            placeholder="Napisz komentarz..."
            class="bg-transparent w-full text-[15px] outline-none placeholder-[#65676b] text-black"
          />
          <div class="flex gap-3 text-[#65676b] cursor-pointer">
             <StickerEmojiIcon :size="20" />
             GIF
             <EmoticonHappyIcon :size="20" />
          </div>
        </div>
      </div>

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
import CloseIcon from 'vue-material-design-icons/Close.vue';
import MusicNoteIcon from 'vue-material-design-icons/MusicNote.vue';
import FileDocumentIcon from 'vue-material-design-icons/FileDocument.vue';
import StickerEmojiIcon from 'vue-material-design-icons/StickerEmoji.vue';
import PlayIcon from 'vue-material-design-icons/Play.vue';
import CheckDecagramIcon from 'vue-material-design-icons/CheckDecagram.vue';
import EmoticonHappyIcon from 'vue-material-design-icons/EmoticonHappy.vue';

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
