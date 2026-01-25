<script setup lang="ts">
import { ref, computed, watch, nextTick, onMounted, onBeforeUnmount } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useStoriesStore } from '@/stores/stories';
import type { ReactionType } from '@/types/Story';

// --- IMPORT IKON ---
import ChevronLeft from 'vue-material-design-icons/ChevronLeft.vue';
import ChevronRight from 'vue-material-design-icons/ChevronRight.vue';
import ChevronUp from 'vue-material-design-icons/ChevronUp.vue';
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

import { useI18n } from 'vue-i18n';
import ActiveStoriesList from '@/components/stories/ActiveStoriesList.vue';
import { getUserById } from '@/data/users';
import type { User } from '@/data/users';
import type { DefineComponent } from 'vue';


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
    component?: DefineComponent<object, object, unknown>;
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
const showViewers = ref(false);

const storyAudioPlayer = new Audio();
storyAudioPlayer.loop = true;
const isMusicPlaying = ref(false);
const storyMusicMuted = ref(false);

const musicElement = computed(() => {
  return currentStoryItem.value?.elements?.find(el => el.musicTitle && el.musicArtist);
});

const reactionEmojis: Record<ReactionType, string> = {
    like: '👍',
    love: '❤️',
    haha: '😆',
    wow: '😮',
    sad: '😢',
    angry: '😡',
};

const isOwner = computed(() => {
    return storiesStore.currentUserId === currentUserStories.value?.userId;
});

const viewersWithReactions = computed(() => {
    if (!currentStoryItem.value?.interactions) return [];

    return currentStoryItem.value.interactions
        .map(interaction => {

            const viewer = getUserById(Number(interaction.userId));
            return { viewer, reaction: interaction.reaction };
        })
        .filter((item): item is { viewer: User; reaction: ReactionType | null } => item.viewer !== undefined);
});


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
  const storyItem = currentUserStories.value.stories[currentStoryIndex.value];
  console.log('currentStoryItem.sharedPostInfo', storyItem?.sharedPostInfo);
  return storyItem;
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

  const story = currentStoryItem.value;
  const isBirthday = story.type === 'birthday';
console.log('viewersWithReactions', currentStoryItem.value);
  return {
    id: story.id,
    src: story.imageUrl || '',
    type: story.type,
    user: {
      name: isBirthday ? 'Urodziny' : (story.originalUserName || currentUserStories.value.userName),
      avatar: isBirthday ? 'https://emojicdn.elk.sh/🎂?style=twitter' : (story.originalUserAvatar || currentUserStories.value.userAvatar),
      time: isBirthday && story.originalUserName ? story.originalUserName : formatTimeAgo(story.createdAt),
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

watch(() => props.userId, (newUserId) => {
  if (newUserId) {
    const userIndex = allUserStories.value.findIndex(us => us.userId === newUserId);
    if (userIndex !== -1) {
      currentUserIndex.value = userIndex;
      currentStoryIndex.value = 0; // Reset to first story
    }
  }
});

// Cleanup on unmount
onBeforeUnmount(() => {
  clearImageTimers();
  storyAudioPlayer.pause();
  storyAudioPlayer.src = '';
});

// Watch for story changes to play music
watch(currentStoryItem, (newItem) => {
  if (newItem?.musicUrl) {
    storyAudioPlayer.src = newItem.musicUrl;
    storyAudioPlayer.play().catch(e => console.error("Autoplay music blocked:", e));
    isMusicPlaying.value = true;
  } else {
    storyAudioPlayer.pause();
    storyAudioPlayer.src = '';
    isMusicPlaying.value = false;
  }
});

// Watch for pause state changes
watch(isPaused, (paused) => {
  if (isMusicPlaying.value) {
    if (paused) {
      storyAudioPlayer.pause();
    } else {
      storyAudioPlayer.play().catch(e => console.error("Autoplay music blocked:", e));
    }
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

const toggleMasterMute = () => {
    storyMusicMuted.value = !storyMusicMuted.value;
    storyAudioPlayer.muted = storyMusicMuted.value;

    if (videoRef.value) {
        videoRef.value.muted = storyMusicMuted.value;
    }
    isMuted.value = storyMusicMuted.value;
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

    <aside class="w-[360px] shrink-0 bg-theme-bg-secondary  shadow-xl flex flex-col h-full z-20 hidden md:flex border-r border-gray-200 dark:border-gray-700">
      <div class="pt-4 px-4 pb-2">
        <div class="flex items-center gap-3 mb-4">
            <RouterLink to="/" class="w-10 h-10 bg-gray-200 dark:bg-gray-700 rounded-full flex items-center justify-center cursor-pointer hover:bg-gray-300 dark:hover:bg-gray-600 transition">
                 <Close :size="24" class="text-black dark:text-gray-200" />
            </RouterLink>
            <div class="w-10 h-10 text-blue-600 cursor-pointer">
                <Facebook :size="40" />
            </div>
        </div>
        <h1 class="text-2xl font-bold text-black dark:text-gray-200 tracking-tight">{{ t('story.stories') }}</h1>
        <div class="flex gap-4 mt-1 text-[15px] font-medium text-blue-600">
          <span class="cursor-pointer hover:underline">{{ t('story.archive') }}</span>
          <span class="cursor-pointer hover:underline">{{ t('story.settings') }}</span>
        </div>
      </div>
      <div class="px-2 py-2">
         <h2 class="px-2 text-[17px] font-semibold text-black dark:text-gray-200 mb-2">{{ t('story.yourStory') }}</h2>
         <RouterLink to="/stories/create">


         <div class="flex items-center gap-3 p-2  rounded-lg cursor-pointer transition">
            <div class="relative">
              <div class="w-[60px] h-[60px] bg-gray-100 dark:bg-gray-700 rounded-full flex items-center justify-center">
                 <div class="w-10 h-10 rounded-full flex items-center justify-center text-blue-500">
                    <svg viewBox="0 0 24 24" width="24" height="24" fill="currentColor"><path d="M11 11V5h2v6h6v2h-6v6h-2v-6H5v-2z"></path></svg>
                 </div>
              </div>
            </div>
            <div>
              <p class="font-semibold text-black dark:text-gray-200 text-[15px]">{{ t('story.createStory') }}</p>
              <p class="text-[13px] text-gray-500 dark:text-gray-400">{{ t('story.sharePhotoOrWriteSomething') }}</p>
            </div>

         </div>
           </RouterLink>
      </div>
      <div class="px-4 py-2 text-[17px] font-semibold text-black dark:text-gray-200 mt-2">{{ t('story.allStories') }}</div>
      <div class="flex-1 overflow-y-auto custom-scrollbar px-2 pb-4">
    <ActiveStoriesList />
      </div>
    </aside>

    <main class="flex-1 relative flex flex-col bg-theme-bg">
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

<div
    v-else-if="currentItem.type === 'birthday'"
    class="absolute inset-0 w-full h-full z-0 overflow-hidden flex flex-col items-center justify-center text-center select-none"
    style="background: radial-gradient(circle at center, #60a5fa 0%, #2563eb 100%);"
    @click="togglePlay"
>
    <div class="absolute inset-0 pointer-events-none opacity-80">
        <div class="absolute top-20 left-10 w-24 h-32 bg-green-400 rounded-[50%] rotate-[-15deg] shadow-lg animate-float-slow opacity-90 before:content-[''] before:absolute before:bottom-[-10px] before:left-1/2 before:-translate-x-1/2 before:w-1 before:h-12 before:bg-white/30">
             <div class="absolute top-4 left-4 w-4 h-8 bg-white/20 rounded-full rotate-[-15deg]"></div>
        </div>
        <div class="absolute top-32 left-24 w-16 h-20 bg-blue-300 rounded-[50%] rotate-[-5deg] shadow-md animate-float-medium before:content-[''] before:absolute before:bottom-[-8px] before:left-1/2 before:-translate-x-1/2 before:w-0.5 before:h-10 before:bg-white/30"></div>

        <div class="absolute top-40 right-10 w-28 h-36 bg-blue-500 rounded-[50%] rotate-[10deg] shadow-xl animate-float-fast before:content-[''] before:absolute before:bottom-[-12px] before:left-1/2 before:-translate-x-1/2 before:w-1 before:h-16 before:bg-white/30">
            <div class="absolute top-5 left-6 w-6 h-10 bg-white/10 rounded-full rotate-[10deg]"></div>
        </div>

        <div class="absolute bottom-32 left-10 w-20 h-26 bg-indigo-400 rounded-[50%] rotate-[5deg] shadow-lg blur-[1px] animate-float-slow"></div>
        <div class="absolute -bottom-10 right-20 w-32 h-40 bg-teal-400 rounded-[50%] rotate-[-10deg] shadow-lg blur-[2px] animate-float-medium"></div>

        <svg class="absolute top-1/4 right-1/4 animate-pulse" width="24" height="24" viewBox="0 0 24 24" fill="white"><path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z"/></svg>
        <svg class="absolute bottom-1/3 left-10 text-yellow-300 animate-spin-slow" width="30" height="30" viewBox="0 0 24 24" fill="currentColor"><path d="M12,2L14.5,9.5L22,9.5L16,14L18.5,21.5L12,17L5.5,21.5L8,14L2,9.5L9.5,9.5L12,2Z"/></svg>
        <div class="absolute top-1/2 left-4 text-green-300 text-4xl font-bold opacity-60">~</div>
        <div class="absolute top-20 right-1/3 text-white text-3xl font-bold opacity-40">*</div>
    </div>

    <div class="relative z-10 flex flex-col items-center animate-scale-in">
        <div class="w-48 h-48 rounded-full border-4 border-white/20 p-1 mb-8 shadow-2xl bg-white/10 backdrop-blur-sm">
            <img
                :src="currentItem.user.avatar"
                class="w-full h-full rounded-full object-cover shadow-inner"
                alt="Birthday User"
            />
        </div>

        <h2 class="text-white text-2xl font-bold mb-2 drop-shadow-md px-4">
            {{ currentItem.user.name }} {{ t('story.hasBirthday') || 'ma dziś urodziny' }}
        </h2>
        <div class="text-4xl mb-4">🎉</div>
        <p class="text-white/90 text-sm font-medium max-w-[250px] leading-relaxed drop-shadow-sm">
            {{ t('story.sendWishes') || 'Wyślij wiadomość i świętujcie razem!' }}
        </p>
    </div>
</div>

<img
    v-else
    ref="imageRef"
    :src="currentItem.src"
    class="absolute inset-0 w-full h-full object-cover z-0"
    @click="togglePlay"
    alt="Story"
/>



                <!-- Shared Post Overlay -->
                <div
                    v-if="currentStoryItem?.sharedPostInfo"
                    class="absolute cursor-pointer"
                    :style="{
                        top: `${currentStoryItem.sharedPostInfo.y}%`,
                        left: `${currentStoryItem.sharedPostInfo.x}%`,
                        width: `${currentStoryItem.sharedPostInfo.width}%`,
                        height: `${currentStoryItem.sharedPostInfo.height}%`,
                    }"
                    @click="router.push({ name: 'post', params: { id: currentStoryItem.sharedPostInfo.postId } })"
                >

                </div>

                <!-- Shared Link Overlay -->
                <div
                    v-if="currentStoryItem?.sharedLinkInfo"
                    class="absolute cursor-pointer"
                    :style="{
                        top: `${currentStoryItem.sharedLinkInfo.y}%`,
                        left: `${currentStoryItem.sharedLinkInfo.x}%`,
                        width: `${currentStoryItem.sharedLinkInfo.width}%`,
                        height: `${currentStoryItem.sharedLinkInfo.height}%`,
                    }"
                    @click="window.open(currentStoryItem.sharedLinkInfo.url, '_blank')"
                >

                </div>

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
                                        <div v-if="musicElement" class="flex items-center gap-1 text-[13px] font-medium text-white/90">
                                            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor" class="w-3 h-3">
                                              <path d="M18.75 3.375c.621 0 1.125.504 1.125 1.125v3.75h-4.5V4.5c0-.621.504-1.125 1.125-1.125h2.25zM12.75 3.375c.621 0 1.125.504 1.125 1.125v3.75h-4.5V4.5c0-.621.504-1.125 1.125-1.125h2.25zM6.75 3.375c.621 0 1.125.504 1.125 1.125v3.75h-4.5V4.5c0-.621.504-1.125 1.125-1.125h2.25zM18.75 8.25h2.25c.621 0 1.125.504 1.125 1.125v10.5a3 3 0 01-3 3H3.375a3 3 0 01-3-3V9.375c0-.621.504-1.125 1.125-1.125h2.25h15z" />
                                            </svg>
                                            <span>{{ musicElement.musicArtist }} - {{ musicElement.musicTitle }}</span>
                                        </div>
                                    </div>                    </div>
                    <div class="flex items-center gap-4 text-white drop-shadow-md">
                        <div class="cursor-pointer hover:opacity-80 transition" @click="togglePlay">
                             <Play v-if="isPaused" :size="24"/>
                             <Pause v-else :size="24"/>
                        </div>
                        <div v-if="isVideo || isMusicPlaying" class="cursor-pointer hover:opacity-80 transition" @click="toggleMasterMute">
                            <VolumeMute v-if="storyMusicMuted" :size="24" />
                            <VolumeHigh v-else :size="24" />
                        </div>
                        <div class="cursor-pointer hover:opacity-80">
                            <DotsHorizontal :size="24" />
                        </div>
                    </div>
                </div>

                <!-- <div v-if="isPaused" class="absolute inset-0 z-10 flex items-center justify-center pointer-events-none">
                     <div class="bg-black/40 p-5 rounded-full backdrop-blur-sm">
                         <Play :size="48" class="text-white opacity-90" />
                     </div>
                </div> -->
            </div>



        </div>
<div class="shrink-0 w-full md:w-[650px] flex flex-col items-center justify-end gap-3 pt-4 pb-2 z-30">
        <template v-if="isOwner">
    <div class="flex items-center gap-4">
        <div
            @click="showViewers = true; isPaused = true"
            class="flex flex-col items-center justify-center cursor-pointer text-white hover:opacity-80 transition"
        >
            <ChevronUp :size="32" />
            <span class="text-sm font-medium">{{ viewersWithReactions.length }} {{ t('createLive.viewers') }}</span>
        </div>

    </div>
</template>
        <template v-else>
            <div class="w-full flex items-end justify-between gap-3">
                <div class="relative flex-1 h-[44px]">
                   <input v-model="messageInput" type="text" :placeholder="currentItem.type === 'birthday' ? 'Złóż życzenia' : t('story.sendMessage')" class="w-full h-full bg-black border-[2px] border-white rounded-full px-6 text-white placeholder-gray-300 focus:outline-none focus:border-gray-200 transition text-[16px] font-normal tracking-wide"/>
                </div>
                <div v-if="currentItem.type !== 'birthday'" class="flex items-center gap-2 pb-0.5">
                    <div v-for="(reaction, idx) in reactions" :key="idx" class="cursor-pointer hover:scale-125 active:scale-95 transition-transform duration-200 origin-bottom">
                         <div v-if="reaction.type === 'icon'" class="w-[38px] h-[38px] rounded-full flex items-center justify-center shadow-lg border-2 border-transparent" :class="reaction.class">
                              <component :is="reaction.component" :size="22" />
                         </div>
                         <div v-else class="text-[36px] leading-none drop-shadow-md filter hover:brightness-110 select-none">{{ reaction.content }}</div>
                    </div>
                </div>
            </div>
        </template>
</div>

        <!-- Viewers List Overlay -->
        <div v-if="showViewers" class="absolute inset-0 z-50 flex flex-col items-center justify-end bg-black/60 backdrop-blur-sm" @click.self="showViewers = false; isPaused = false">
             <div class="bg-white dark:bg-[#242526] w-full md:w-[400px] rounded-t-xl h-[60vh] flex flex-col shadow-2xl animate-slide-up">
                 <div class="p-4 border-b border-gray-200 dark:border-gray-700 flex items-center justify-between">
                     <h3 class="font-bold text-lg dark:text-white">{{ t('createLive.viewers') }}</h3>
                     <button @click="showViewers = false; isPaused = false" class="p-2 hover:bg-gray-100 dark:hover:bg-gray-700 rounded-full">
                         <Close :size="24" class="dark:text-white" />
                     </button>
                 </div>
                 <div class="flex-1 overflow-y-auto p-2">
                     <div v-if="viewersWithReactions.length === 0" class="flex flex-col items-center justify-center h-full text-gray-500">
                         <p>{{ t('search.noResults') }}</p> <!-- Using existing key for 'No X' or just generic empty -->
                     </div>
                     <div v-else class="flex flex-col gap-2">
                         <div v-for="{ viewer, reaction } in viewersWithReactions" :key="viewer.id" class="flex items-center justify-between p-2 hover:bg-gray-100 dark:hover:bg-gray-700 rounded-lg cursor-pointer">
                            <div class="flex items-center gap-3">
                                <img :src="viewer.avatar" class="w-10 h-10 rounded-full object-cover" />
                                <span class="font-medium dark:text-white">{{ viewer.name }}</span>
                            </div>
                            <div v-if="reaction" class="text-2xl">
                                {{ reactionEmojis[reaction] }}
                            </div>
                         </div>
                     </div>
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

@keyframes slide-up {
  from { transform: translateY(100%); }
  to { transform: translateY(0); }
}
.animate-slide-up {
  animation: slide-up 0.3s ease-out;
}
/* Animacje dla widoku urodzinowego */
@keyframes float {
  0%, 100% { transform: translateY(0) rotate(var(--r, 0deg)); }
  50% { transform: translateY(-20px) rotate(var(--r, 0deg)); }
}

.animate-float-slow {
  animation: float 6s ease-in-out infinite;
  --r: -5deg;
}

.animate-float-medium {
  animation: float 5s ease-in-out infinite;
  animation-delay: 1s;
  --r: 5deg;
}

.animate-float-fast {
  animation: float 4s ease-in-out infinite;
  animation-delay: 2s;
  --r: 10deg;
}

.animate-spin-slow {
  animation: spin 8s linear infinite;
}

@keyframes scale-in {
  from { transform: scale(0.8); opacity: 0; }
  to { transform: scale(1); opacity: 1; }
}

.animate-scale-in {
  animation: scale-in 0.5s cubic-bezier(0.175, 0.885, 0.32, 1.275) forwards;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}
</style>
