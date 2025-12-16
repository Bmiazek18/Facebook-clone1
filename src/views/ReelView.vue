<template>
  <div class="flex h-screen pt-[50px] bg-black overflow-hidden font-sans">

    <div class="flex-1 flex justify-center items-center relative transition-all duration-300">

      <div class="relative">

        <div class="relative w-[360px] md:w-[400px] aspect-[9/16] bg-[#222] rounded-lg shadow-2xl overflow-hidden z-10">

          <video
            ref="videoRef"
            class="w-full h-full object-cover cursor-pointer"
            :poster="reel.poster"
            :src="reel.videoSrc"
            loop
            muted
            playsinline
            @click="togglePlay"
          >
            <source :src="reel.videoSrc" type="video/mp4" />
          </video>

          <div class="absolute inset-0 pointer-events-none bg-gradient-to-b from-transparent via-transparent to-black/60"></div>

          <div v-if="!isPlaying" class="absolute inset-0 flex items-center justify-center pointer-events-none">
            <PlayIcon :size="64" class="text-white/50" />
          </div>

          <div class="absolute bottom-4 left-4 right-4 text-white z-10">
            <div class="flex items-center gap-2 mb-2">
              <img :src="reel.user.avatar" class="w-8 h-8 rounded-full object-cover border border-white/20" />
              <span class="font-bold text-[15px] hover:underline cursor-pointer">{{ reel.user.name }}</span>
              <button class="ml-1 text-white font-bold text-[15px] hover:underline">• Obserwuj</button>
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
        <div class="absolute -right-[70px] bottom-0 flex flex-col items-center gap-4 text-white z-20 pb-4">

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

          <div class="flex flex-col items-center gap-1 cursor-pointer group">
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


        <div class="absolute -right-[250px] top-1/2 -translate-y-1/2 flex flex-col gap-4 z-0">
          <button class="w-12 h-12 bg-[#3a3b3c] hover:bg-[#4e4f50] rounded-full flex items-center justify-center text-white transition-colors border border-gray-700">
            <ChevronUpIcon :size="32" />
          </button>
          <button class="w-12 h-12 bg-[#3a3b3c] hover:bg-[#4e4f50] rounded-full flex items-center justify-center text-white transition-colors border border-gray-700">
            <ChevronDownIcon :size="32" />
          </button>
        </div>

      </div>
    </div>

    <div
      v-if="isCommentsOpen"
      class="w-[400px] bg-white border-l border-[#dadde1] flex flex-col transition-all duration-300 animate-slide-in relative z-40"
    >
      <button @click="toggleComments" class="absolute top-2 right-2 text-gray-500 hover:bg-gray-100 p-1 rounded-full z-50">
         <CloseIcon :size="24" />
      </button>

      <div class="p-4 border-b border-gray-200 overflow-y-auto max-h-[30vh]">
        <div class="flex gap-3 mb-3">
           <img :src="reel.user.avatar" class="w-10 h-10 rounded-full object-cover border border-gray-200" />
           <div class="flex flex-col">
             <div class="flex flex-wrap items-center gap-1 leading-tight">
               <span class="font-bold text-[#050505] text-[15px] hover:underline cursor-pointer">
                 {{ reel.user.name }}
               </span>
               <CheckDecagramIcon :size="14" class="text-blue-500" />
               <span class="text-[#1877f2] font-semibold text-[15px] cursor-pointer hover:underline">
                 • Obserwuj
               </span>
             </div>
             <div class="flex items-center gap-1 text-[#65676b] text-[12px] mt-0.5">
               <MusicNoteIcon :size="12" />
               <span>{{ reel.music }}</span>
             </div>
           </div>
        </div>

        <div class="text-[15px] text-[#050505] leading-normal whitespace-pre-line pl-[52px]">
          {{ reel.caption }}
          <br />
          <span class="text-[#1877f2] cursor-pointer hover:underline">
             {{ reel.hashtags }}
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
import { ref, onMounted } from 'vue';

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

const isCommentsOpen = ref(false);
const isPlaying = ref(true);
const videoRef = ref<HTMLVideoElement | null>(null);

const toggleComments = () => {
  isCommentsOpen.value = !isCommentsOpen.value;
};

const togglePlay = () => {
  if (videoRef.value) {
    if (videoRef.value.paused) {
      videoRef.value.play();
      isPlaying.value = true;
    } else {
      videoRef.value.pause();
      isPlaying.value = false;
    }
  }
};

const reel = ref({
  id: 'reel-1',
  videoSrc: 'https://assets.mixkit.co/videos/preview/mixkit-young-woman-training-in-a-gym-with-a-machine-43956-large.mp4',
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
});

onMounted(() => {
  if (videoRef.value) {
    videoRef.value.play().catch(() => isPlaying.value = false);
  }
});
</script>

<style scoped>
video::-webkit-media-controls {
  display: none !important;
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
