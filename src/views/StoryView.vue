<script setup lang="ts">
import { ref, computed, watch, nextTick, onMounted, onBeforeUnmount } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useStoriesStore } from '@/stores/stories';
import type { UserStories, StoryItem } from '@/types/Story';

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
import NavbarRight from '@/layouts/Navbar/NavbarRight.vue';
import { useI18n } from 'vue-i18n';
import ActiveStoriesList from '@/components/stories/ActiveStoriesList.vue';


const { t } = useI18n();
const route = useRoute();
const router = useRouter();
const storiesStore = useStoriesStore();

// Props
const props = defineProps<{
  userId?: string;
}>();

// --- TYPY DANYCH ---
interface Reaction {
    type: 'icon' | 'emoji';
    component?: any;
    content?: string;
    class?: string;
}



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
const currentUserIndex = ref(0);
const currentStoryIndex = ref(0);
const videoRef = ref<HTMLVideoElement | null>(null);
const imageRef = ref<HTMLImageElement | null>(null);
const isPaused = ref(false);
const isMuted = ref(true);
const progress = ref(0);
const messageInput = ref('');

// Timer for images (10 seconds)
const IMAGE_DURATION = 10000; // 10 seconds in milliseconds
let imageTimer: number | null = null;
let imageProgressInterval: number | null = null;

// Preloaded images cache
const preloadedImages = new Map<string, HTMLImageElement>();

// Preload images function
const preloadImages = () => {
  // Preload current user's remaining stories
  if (currentUserStories.value) {
    for (let i = currentStoryIndex.value; i < currentUserStories.value.stories.length; i++) {
      const story = currentUserStories.value.stories[i];
      if (story && story.type === 'image' && story.imageUrl && !preloadedImages.has(story.imageUrl)) {
        const img = new Image();
        img.src = story.imageUrl;
        preloadedImages.set(story.imageUrl, img);
      }
    }
  }

  // Preload next user's first few stories
  if (currentUserIndex.value < allUserStories.value.length - 1) {
    const nextUser = allUserStories.value[currentUserIndex.value + 1];
    if (nextUser) {
      for (let i = 0; i < Math.min(3, nextUser.stories.length); i++) {
        const story = nextUser.stories[i];
        if (story && story.type === 'image' && story.imageUrl && !preloadedImages.has(story.imageUrl)) {
          const img = new Image();
          img.src = story.imageUrl;
          preloadedImages.set(story.imageUrl, img);
        }
      }
    }
  }
};

// Load stories from store
const allUserStories = computed(() => storiesStore.allUserStories);

// Current user stories
const currentUserStories = computed(() => {
  if (allUserStories.value.length === 0) return null;
  return allUserStories.value[currentUserIndex.value];
});

// Current story item
const currentStoryItem = computed(() => {
  if (!currentUserStories.value) return null;
  return currentUserStories.value.stories[currentStoryIndex.value];
});

// Story items array (for progress bars)
const storyItems = computed(() => {
  if (!currentUserStories.value) return [];
  return currentUserStories.value.stories;
});

// Current item (for compatibility with template)
const currentItem = computed(() => {
  if (!currentStoryItem.value || !currentUserStories.value) {
    return {
      src: '',
      type: 'image' as const,
      user: {
        name: '',
        avatar: '',
        time: ''
      }
    };
  }

  return {
    id: currentStoryItem.value.id,
    src: currentStoryItem.value.imageUrl || '',
    type: currentStoryItem.value.type,
    user: {
      name: currentUserStories.value.userName || '',
      avatar: currentUserStories.value.userAvatar || '',
      time: formatTimeAgo(currentStoryItem.value.createdAt)
    }
  };
});

// Format time ago
const formatTimeAgo = (timestamp: number) => {
  const now = Date.now();
  const diff = now - timestamp;
  const hours = Math.floor(diff / (1000 * 60 * 60));

  if (hours < 1) return 'Just now';
  if (hours === 1) return '1h ago';
  return `${hours}h ago`;
};

// Initialize - find user by ID if provided
onMounted(() => {
  if (props.userId) {
    const userIndex = allUserStories.value.findIndex(us => us.userId === props.userId);
    if (userIndex !== -1) {
      currentUserIndex.value = userIndex;
    }
  }

  // Mark first story as viewed
  if (currentStoryItem.value) {
    storiesStore.markStoryAsViewed(currentStoryItem.value.id, storiesStore.currentUserId);
  }

  // Start preloading images
  preloadImages();
});

// Cleanup on unmount
onBeforeUnmount(() => {
  clearImageTimers();

});
 onMounted(() => {
    if (isVideo.value && videoRef.value) {
        // Video story
        videoRef.value.load();
        try {  videoRef.value.play(); } catch (e) { console.error("Autoplay blocked:", e); }
    } else {
        // Image story - start 10 second timer
        startImageTimer();
    }
});



// Watch for userId changes (when navigating between users)
watch(() => props.userId, (newUserId) => {
  if (newUserId) {
    const userIndex = allUserStories.value.findIndex(us => us.userId === newUserId);
    if (userIndex !== -1) {
      currentUserIndex.value = userIndex;
      currentStoryIndex.value = 0;
    }
  }
});

// Watch for story changes to mark as viewed
watch([currentUserIndex, currentStoryIndex], () => {
  if (currentStoryItem.value) {
    storiesStore.markStoryAsViewed(currentStoryItem.value.id, storiesStore.currentUserId);
  }
});

// Watch for user index changes to update URL
watch(currentUserIndex, (newUserIndex) => {
  clearImageTimers();
  progress.value = 0;
  const newUserId = allUserStories.value[newUserIndex]?.userId;
  if (newUserId && route.params.userId !== newUserId) {
    router.replace({ name: 'userStories', params: { userId: newUserId } });
  }

  // Preload images for new user
  preloadImages();
  startImageTimer();
});

// --- LOGIKA ODTWARZACZA ---

// Clear timers
const clearImageTimers = () => {
  if (imageTimer !== null) {
    clearTimeout(imageTimer);
    imageTimer = null;
  }
  if (imageProgressInterval !== null) {
    clearInterval(imageProgressInterval);
    imageProgressInterval = null;
  }
};

// Start image timer (10 seconds)
const startImageTimer = () => {
  clearImageTimers();
  progress.value = 0;

  const startTime = Date.now();

  // Update progress every 100ms
  imageProgressInterval = window.setInterval(() => {
    if (!isPaused.value) {
      const elapsed = Date.now() - startTime;
      const percent = Math.min((elapsed / IMAGE_DURATION) * 100, 100);
      progress.value = percent;
    }
  }, 100);

  // Move to next story after 10 seconds
  imageTimer = window.setTimeout(() => {
    if (!isPaused.value) {
      nextStory();
    }
  }, IMAGE_DURATION);
};

// Detect if current item is video or image
const isVideo = computed(() => {
  const item = currentStoryItem.value;
  if (!item) return false;

  // Check type field
  if (item.type === 'video') return true;

  // Check URL extension
  const url = item.imageUrl || '';
  const videoExtensions = ['.mp4', '.webm', '.ogg', '.mov', '.avi'];
  return videoExtensions.some(ext => url.toLowerCase().endsWith(ext));
});

watch(currentStoryIndex, async () => {
    clearImageTimers();
    progress.value = 0;
    isPaused.value = false;
    await nextTick();

    // Preload upcoming images
    preloadImages();

    if (isVideo.value && videoRef.value) {
        // Video story
        videoRef.value.load();
        try { await videoRef.value.play(); } catch (e) { console.error("Autoplay blocked:", e); }
    } else {
        // Image story - start 10 second timer
        startImageTimer();
    }
});

const togglePlay = () => {
    if (isVideo.value && videoRef.value) {
        // Video control
        if (videoRef.value.paused) {
            videoRef.value.play();
            isPaused.value = false;
        } else {
            videoRef.value.pause();
            isPaused.value = true;
        }
    } else {
        // Image control - pause/resume timer
        isPaused.value = !isPaused.value;

        if (isPaused.value) {
            clearImageTimers();
        } else {
            // Resume with remaining time
            const remainingPercent = 100 - progress.value;
            const remainingTime = (remainingPercent / 100) * IMAGE_DURATION;

            const startTime = Date.now();
            const startProgress = progress.value;

            imageProgressInterval = window.setInterval(() => {
                if (!isPaused.value) {
                    const elapsed = Date.now() - startTime;
                    const newProgress = startProgress + (elapsed / IMAGE_DURATION) * 100;
                    progress.value = Math.min(newProgress, 100);
                }
            }, 100);

            imageTimer = window.setTimeout(() => {
                if (!isPaused.value) {
                    nextStory();
                }
            }, remainingTime);
        }
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
        // Move to next story of current user
        currentStoryIndex.value++;
    } else {
        // Current user's stories finished - check if there are more users
        if (currentUserIndex.value < allUserStories.value.length - 1) {
            // Move to next user's first story
            currentUserIndex.value++;
            currentStoryIndex.value = 0;
        } else {
            // No more users - end of all stories
            console.log(t('story.endOfStory'));
            isPaused.value = true;
        }
    }
};

const prevStory = () => {
    if (currentStoryIndex.value > 0) {
        // Move to previous story of current user
        currentStoryIndex.value--;
    } else if (currentUserIndex.value > 0) {
        // Move to previous user's last story
        currentUserIndex.value--;
        const prevUserStories = allUserStories.value[currentUserIndex.value];
        if (prevUserStories) {
            currentStoryIndex.value = prevUserStories.stories.length - 1;
        }
    }
};
console.log(currentItem.value.src)
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
    <ActiveStoriesList />
      </div>
    </aside>

    <main class="flex-1 relative flex flex-col bg-black">
      <RouterLink to="/" class="absolute top-4 left-4 z-30 w-10 h-10 bg-gray-700/50 hover:bg-gray-600 rounded-full flex items-center justify-center text-white transition md:hidden">
        <Close :size="24" />
      </RouterLink>

      <div class="absolute top-0 right-0 p-4 gap-3 z-30 hidden md:flex">
          <div class="nav-icon-btn"><Apps :size="20" /></div>
          <div class="nav-icon-btn"><FacebookMessenger :size="20" /></div>
          <div class="nav-icon-btn"><Bell :size="20" /></div>
          <div class="nav-icon-btn"><AccountCircle :size="28" class="-m-1" /></div>
      </div>

      <div class="flex-1 flex flex-col items-center justify-center w-full h-full relative p-0 md:p-4">

        <button @click="prevStory" v-if="currentStoryIndex > 0 || currentUserIndex > 0" class="absolute left-4 lg:left-24 z-20 w-12 h-12 bg-gray-700/50 hover:bg-gray-600 rounded-full flex items-center justify-center text-white transition"><ChevronLeft :size="32" /></button>

        <div class="flex flex-col h-[90vh] w-full md:aspect-9/16  md:w-auto ">

            <div class="relative flex-1 bg-gray-900 rounded-none md:rounded-xl overflow-hidden shadow-2xl flex flex-col group border-0 md:border md:border-gray-800">
                <!-- Video Story -->
                <video
                    v-if="isVideo"
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

                <!-- Image Story -->
                <img
                    v-else
                    ref="imageRef"
                    :src="currentItem.src"
                    class="absolute inset-0 w-full h-full object-cover z-0"
                    @click="togglePlay"
                    alt="Story"
                />

                <div class="absolute inset-0 bg-gradient-to-b from-black/40 via-transparent to-black/20 pointer-events-none z-10"></div>

                <div class="absolute top-2 left-2 right-2 flex gap-1 z-20 h-1">
                    <div v-for="(item, index) in storyItems" :key="item.id" class="flex-1 bg-white/30 rounded-full overflow-hidden h-full backdrop-blur-sm">
                        <div class="h-full bg-white transition-all duration-100 ease-linear" :style="{ width: index < currentStoryIndex ? '100%' : (index === currentStoryIndex ? progress + '%' : '0%') }"></div>
                    </div>
                </div>

                <div class="absolute top-5 left-4 right-4 flex justify-between items-start z-20">
                    <div class="flex items-center gap-3">
                        <div class="w-10 h-10 rounded-full border border-gray-400 overflow-hidden bg-gray-500 flex items-center justify-center">
                             <img v-if="currentItem.user.avatar" :src="currentItem.user.avatar" alt="User avatar" class="w-full h-full object-cover" />
                             <AccountCircle v-else :size="42" class="text-gray-300"/>
                        </div>
                        <div class="flex flex-col text-white drop-shadow-md leading-tight">
                            <div class="flex items-center gap-2">
                                 <span class="font-semibold text-[15px] hover:underline cursor-pointer">{{ currentItem.user.name }}</span>
                                 <span class="text-white/80 text-[13px] font-light">{{ currentItem.user.time }}</span>
                            </div>
                        </div>
                    </div>
                    <div class="flex items-center gap-4 text-white drop-shadow-md">
                        <div class="cursor-pointer hover:opacity-80 transition" @click="togglePlay">
                             <Play v-if="isPaused" :size="24"/>
                             <Pause v-else :size="24"/>
                        </div>
                        <div v-if="isVideo" class="cursor-pointer hover:opacity-80 transition" @click="toggleMute">
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
<div class="shrink-0 w-full md:w-[650px] flex items-end justify-between gap-3 pt-4 pb-2 z-30">
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
        <button @click="nextStory" v-if="currentStoryIndex < storyItems.length - 1 || currentUserIndex < allUserStories.length - 1" class="absolute right-4 lg:right-24 z-20 w-12 h-12 bg-gray-700/50 hover:bg-gray-600 rounded-full flex items-center justify-center text-white transition"><ChevronRight :size="32" /></button>
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
