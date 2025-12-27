<script setup lang="ts">
import { ref, computed, watch, nextTick } from 'vue';

// --- IMPORT IKON ---
import ChevronLeft from 'vue-material-design-icons/ChevronLeft.vue';
import ChevronRight from 'vue-material-design-icons/ChevronRight.vue';
import Close from 'vue-material-design-icons/Close.vue';
import DotsHorizontal from 'vue-material-design-icons/DotsHorizontal.vue';
import VolumeHigh from 'vue-material-design-icons/VolumeHigh.vue';
import VolumeMute from 'vue-material-design-icons/VolumeMute.vue';
import Play from 'vue-material-design-icons/Play.vue';
import Pause from 'vue-material-design-icons/Pause.vue';
import Facebook from 'vue-material-design-icons/Facebook.vue';
import FacebookMessenger from 'vue-material-design-icons/FacebookMessenger.vue';
import Bell from 'vue-material-design-icons/Bell.vue';
import Apps from 'vue-material-design-icons/Apps.vue';
import AccountCircle from 'vue-material-design-icons/AccountCircle.vue';
import ThumbUp from 'vue-material-design-icons/ThumbUp.vue';
import Heart from 'vue-material-design-icons/Heart.vue';
import NavbarRight from '@/components/NavbarRight.vue';
import { useI18n } from 'vue-i18n';

const { t } = useI18n();

// --- TYPY DANYCH ---
interface StoryUser {
  id: number;
  name: string;
  time: string;
  isUnseen: boolean;
  isActive?: boolean;
}

interface StoryItem {
    id: number;
    type: 'video';
    src: string;
}

interface Reaction {
    type: 'icon' | 'emoji';
    component?: any;
    content?: string;
    class?: string;
}

// --- DANE: Pasek Boczny ---
const stories = ref<StoryUser[]>([
  { id: 1, name: 'Dariusz Blacha', time: '11 min', isUnseen: false, isActive: true },
  { id: 2, name: 'Wiktoria Celińska-Mysław', time: 'Urodziny • dziś', isUnseen: true },
  { id: 3, name: 'Patryk Stosio', time: '2 nowe • 13 godz.', isUnseen: true },
  { id: 4, name: 'Patrycja Rosłoń', time: '1 nowa • 21 godz.', isUnseen: true },
  { id: 5, name: 'Mateusz Chrobok', time: '1 nowa • 23 godz.', isUnseen: true },
]);

// --- DANE: Wideo (Linki Google CDN - ZAWSZE DZIAŁAJĄ) ---
const storyItems = ref<StoryItem[]>([
    {
        id: 1,
        type: 'video',
        src: 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4'
    },
    {
        id: 2,
        type: 'video',
        src: 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerJoyrides.mp4'
    }
]);

// --- DANE: Reakcje ---
const reactions: Reaction[] = [
    { type: 'icon', component: ThumbUp, class: 'bg-blue-600 text-white p-1.5' },
    { type: 'icon', component: Heart, class: 'bg-red-500 text-white p-1.5' },
    { type: 'emoji', content: '🥰' },
    { type: 'emoji', content: '😆' },
    { type: 'emoji', content: '😮' },
    { type: 'emoji', content: '😢' },
    { type: 'emoji', content: '😡' },
];

// --- STAN APLIKACJI ---
const currentStoryIndex = ref(0);
const videoRef = ref<HTMLVideoElement | null>(null);
const isPaused = ref(false);
const isMuted = ref(true);
const progress = ref(0);
const messageInput = ref('');

const currentItem = computed(() => storyItems.value[currentStoryIndex.value]);

// --- LOGIKA ODTWARZACZA ---
watch(currentStoryIndex, async () => {
    progress.value = 0;
    isPaused.value = false;
    await nextTick();
    if (videoRef.value) {
        videoRef.value.load();
        try { await videoRef.value.play(); } catch (e) { console.error("Autoplay blocked:", e); }
    }
});

const togglePlay = () => {
    if (!videoRef.value) return;
    if (videoRef.value.paused) {
        videoRef.value.play();
        isPaused.value = false;
    } else {
        videoRef.value.pause();
        isPaused.value = true;
    }
};

const toggleMute = () => {
    if(!videoRef.value) return;
    videoRef.value.muted = !videoRef.value.muted;
    isMuted.value = videoRef.value.muted;
};

const updateProgress = () => {
    if (videoRef.value && videoRef.value.duration) {
        const percent = (videoRef.value.currentTime / videoRef.value.duration) * 100;
        progress.value = percent;
    }
};

const nextStory = () => {
    if (currentStoryIndex.value < storyItems.value.length - 1) {
        currentStoryIndex.value++;
    } else {
        console.log(t('story.endOfStory'));
        isPaused.value = true;
    }
};

const prevStory = () => {
    if (currentStoryIndex.value > 0) {
        currentStoryIndex.value--;
    }
};
</script>

<template>
  <div class="flex h-screen w-full bg-black overflow-hidden font-sans select-none">

    <aside class="w-[360px] shrink-0 bg-white shadow-xl flex flex-col h-full z-20 hidden md:flex border-r border-gray-200">
      <div class="pt-4 px-4 pb-2">
        <div class="flex items-center gap-3 mb-4">
            <RouterLink to="/" class="w-10 h-10 bg-gray-200 rounded-full flex items-center justify-center cursor-pointer hover:bg-gray-300 transition">
                 <Close :size="24" class="text-black" />
            </RouterLink>
            <div class="w-10 h-10 text-blue-600 cursor-pointer">
                <Facebook :size="40" />
            </div>
        </div>
        <h1 class="text-2xl font-bold text-black tracking-tight">{{ t('story.stories') }}</h1>
        <div class="flex gap-4 mt-1 text-[15px] font-medium text-blue-600">
          <span class="cursor-pointer hover:underline">{{ t('story.archive') }}</span>
          <span class="cursor-pointer hover:underline">{{ t('story.settings') }}</span>
        </div>
      </div>
      <div class="px-2 py-2">
         <h2 class="px-2 text-[17px] font-semibold text-black mb-2">{{ t('story.yourStory') }}</h2>
         <RouterLink to="/stories/create">


         <div class="flex items-center gap-3 p-2  rounded-lg cursor-pointer transition">
            <div class="relative">
              <div class="w-[60px] h-[60px] bg-gray-100 rounded-full flex items-center justify-center">
                 <div class="w-10 h-10 rounded-full flex items-center justify-center text-blue-500">
                    <svg viewBox="0 0 24 24" width="24" height="24" fill="currentColor"><path d="M11 11V5h2v6h6v2h-6v6h-2v-6H5v-2z"></path></svg>
                 </div>
              </div>
            </div>
            <div>
              <p class="font-semibold text-black text-[15px]">{{ t('story.createStory') }}</p>
              <p class="text-[13px] text-gray-500">{{ t('story.sharePhotoOrWriteSomething') }}</p>
            </div>

         </div>
           </RouterLink>
      </div>
      <div class="px-4 py-2 text-[17px] font-semibold text-black mt-2">{{ t('story.allStories') }}</div>
      <div class="flex-1 overflow-y-auto custom-scrollbar px-2 pb-4">
        <div v-for="story in stories" :key="story.id" class="flex items-center gap-3 p-2 rounded-lg cursor-pointer transition mb-1" :class="story.isActive ? 'bg-gray-100' : 'hover:bg-gray-50'">
          <div class="relative p-[2px] rounded-full shrink-0" :class="story.isUnseen && !story.isActive ? 'bg-blue-600' : 'bg-transparent'">
            <div class="bg-white p-[2px] rounded-full">
               <div class="w-[52px] h-[52px] rounded-full bg-gray-300 overflow-hidden relative border border-gray-200 flex items-center justify-center">
                   <AccountCircle :size="68" class="text-gray-500 mt-2"/>
               </div>
            </div>
          </div>
          <div class="flex flex-col min-w-0">
            <span class="font-semibold text-[15px] text-gray-900 leading-tight truncate">{{ story.name }}</span>
            <span class="text-[13px] text-gray-500 font-normal mt-0.5 truncate" :class="story.isUnseen ? 'text-blue-600 font-medium' : ''">{{ story.time }}</span>
          </div>
        </div>
      </div>
    </aside>

    <main class="flex-1 relative flex flex-col bg-black">
      <div class="absolute top-0 right-0 p-4 flex gap-3 z-30">
          <div class="nav-icon-btn"><Apps :size="20" /></div>
          <div class="nav-icon-btn"><FacebookMessenger :size="20" /></div>
          <div class="nav-icon-btn"><Bell :size="20" /></div>
          <div class="nav-icon-btn"><AccountCircle :size="28" class="-m-1" /></div>
      </div>

      <div class="flex-1 flex flex-col items-center justify-center w-full h-full relative p-4">

        <button @click="prevStory" v-if="currentStoryIndex > 0" class="absolute left-4 lg:left-24 z-20 w-12 h-12 bg-gray-700/50 hover:bg-gray-600 rounded-full flex items-center justify-center text-white transition"><ChevronLeft :size="32" /></button>

        <div class="flex flex-col h-[90vh] aspect-[9/16] max-h-[900px]">

            <div class="relative flex-1 bg-gray-900 rounded-xl overflow-hidden shadow-2xl flex flex-col group border border-gray-800">
                <video
                    ref="videoRef"
                    :src="currentItem.src"
                    class="absolute inset-0 w-full h-full object-cover z-0"
                    autoplay
                    muted
                    playsinline
                    @timeupdate="updateProgress"
                    @ended="nextStory"
                    @click="togglePlay"
                ></video>

                <div class="absolute inset-0 bg-gradient-to-b from-black/40 via-transparent to-black/20 pointer-events-none z-10"></div>

                <div class="absolute top-2 left-2 right-2 flex gap-1 z-20 h-1">
                    <div v-for="(item, index) in storyItems" :key="item.id" class="flex-1 bg-white/30 rounded-full overflow-hidden h-full backdrop-blur-sm">
                        <div class="h-full bg-white transition-all duration-100 ease-linear" :style="{ width: index < currentStoryIndex ? '100%' : (index === currentStoryIndex ? progress + '%' : '0%') }"></div>
                    </div>
                </div>

                <div class="absolute top-5 left-4 right-4 flex justify-between items-start z-20">
                    <div class="flex items-center gap-3">
                        <div class="w-10 h-10 rounded-full border border-gray-400 overflow-hidden bg-gray-500 flex items-center justify-center">
                             <AccountCircle :size="42" class="text-gray-300"/>
                        </div>
                        <div class="flex flex-col text-white drop-shadow-md leading-tight">
                            <div class="flex items-center gap-2">
                                 <span class="font-semibold text-[15px] hover:underline cursor-pointer">Dariusz Blacha</span>
                                 <span class="text-white/80 text-[13px] font-light">11 min</span>
                            </div>
                        </div>
                    </div>
                    <div class="flex items-center gap-4 text-white drop-shadow-md">
                        <div class="cursor-pointer hover:opacity-80 transition" @click="togglePlay">
                             <Play v-if="isPaused" :size="24"/>
                             <Pause v-else :size="24"/>
                        </div>
                        <div class="cursor-pointer hover:opacity-80 transition" @click="toggleMute">
                            <VolumeMute v-if="isMuted" :size="24" />
                            <VolumeHigh v-else :size="24" />
                        </div>
                        <div class="cursor-pointer hover:opacity-80">
                            <DotsHorizontal :size="24" />
                        </div>
                    </div>
                </div>

                <div v-if="isPaused" class="absolute inset-0 z-10 flex items-center justify-center pointer-events-none">
                     <div class="bg-black/40 p-5 rounded-full backdrop-blur-sm">
                         <Play :size="48" class="text-white opacity-90" />
                     </div>
                </div>
            </div>



        </div>
<div class="shrink-0 w-[650px] flex items-end justify-between gap-3 pt-4 pb-2 z-30">
                <div class="relative flex-1 h-[44px]">
                   <input v-model="messageInput" type="text" :placeholder="t('story.sendMessage')" class="w-full h-full bg-black border-[2px] border-white rounded-full px-6 text-white placeholder-gray-300 focus:outline-none focus:border-gray-200 transition text-[16px] font-normal tracking-wide"/>
                </div>
                <div class="flex items-center gap-2 pb-0.5">
                    <div v-for="(reaction, idx) in reactions" :key="idx" class="cursor-pointer hover:scale-125 active:scale-95 transition-transform duration-200 origin-bottom">
                         <div v-if="reaction.type === 'icon'" class="w-[38px] h-[38px] rounded-full flex items-center justify-center shadow-lg border-2 border-transparent" :class="reaction.class">
                              <component :is="reaction.component" :size="22" />
                         </div>
                         <div v-else class="text-[36px] leading-none drop-shadow-md filter hover:brightness-110 select-none">{{ reaction.content }}</div>
                    </div>
                </div>
            </div>
        <button @click="nextStory" v-if="currentStoryIndex < storyItems.length - 1" class="absolute right-4 lg:right-24 z-20 w-12 h-12 bg-gray-700/50 hover:bg-gray-600 rounded-full flex items-center justify-center text-white transition"><ChevronRight :size="32" /></button>
      </div>
    </main>
  </div>
</template>

<style scoped>
.custom-scrollbar::-webkit-scrollbar { width: 8px; }
.custom-scrollbar::-webkit-scrollbar-track { background: transparent; }
.custom-scrollbar::-webkit-scrollbar-thumb { background-color: #bcc0c4; border-radius: 4px; }
.custom-scrollbar:hover::-webkit-scrollbar-thumb { background-color: #a0a4a8; }

</style>
