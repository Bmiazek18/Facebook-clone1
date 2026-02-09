import { ref, watch, onMounted, onBeforeUnmount, nextTick, computed, type Ref } from 'vue';
import { useStoriesStore } from '@/stores/stories';
import { useRouter, useRoute } from 'vue-router';
import type { Story } from '@/types/Story';
import type { UserStory } from '@/stores/stories';

export function useStoryPlayback(
  currentUserIndex: Ref<number>,
  currentStoryIndex: Ref<number>,
  allUserStories: Ref<UserStory[]>,
  currentStoryItem: Ref<Story | null>,
  storyItems: Ref<Story[]>,
  isVideo: Ref<boolean>,
) {
  const storiesStore = useStoriesStore();
  const router = useRouter();
  const route = useRoute();

  const videoRef = ref<HTMLVideoElement | null>(null);
  const imageRef = ref<HTMLImageElement | null>(null);
  const isPaused = ref(false);
  const isMuted = ref(true); // Default to muted (Autoplay policy)
  const progress = ref(0);

  const storyAudioPlayer = new Audio();
  storyAudioPlayer.loop = true;
  const isMusicPlaying = ref(false);
  const storyMusicMuted = ref(false);

  // Timer logic
  const IMAGE_DURATION = 10000;
  let imageTimer: number | null = null;
  let imageProgressInterval: number | null = null;
  const preloadedImages = new Map<string, HTMLImageElement>();
  
  const currentUserStories = computed(() => {
    if (allUserStories.value.length === 0) return null;
    return allUserStories.value[currentUserIndex.value];
  });
  
  const preloadImages = () => {
    if (currentUserStories.value) {
      for (let i = currentStoryIndex.value; i < currentUserStories.value.stories.length; i++) {
        const story = currentUserStories.value.stories[i];
        if (story?.type === 'image' && story.imageUrl && !preloadedImages.has(story.imageUrl)) {
          const img = new Image();
          img.src = story.imageUrl;
          preloadedImages.set(story.imageUrl, img);
        }
      }
    }
    // Preload next user
    if (currentUserIndex.value < allUserStories.value.length - 1) {
      const nextUser = allUserStories.value[currentUserIndex.value + 1];
      if (nextUser) {
        for (let i = 0; i < Math.min(3, nextUser.stories.length); i++) {
          const story = nextUser.stories[i];
          if (story?.type === 'image' && story.imageUrl && !preloadedImages.has(story.imageUrl)) {
            const img = new Image();
            img.src = story.imageUrl;
            preloadedImages.set(story.imageUrl, img);
          }
        }
      }
    }
  };

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

  const startImageTimer = () => {
    clearImageTimers();
    if (progress.value >= 100) progress.value = 0;

    const startTime = Date.now();
    const startProgress = progress.value;
    const remainingTime = ((100 - startProgress) / 100) * IMAGE_DURATION;

    imageProgressInterval = window.setInterval(() => {
      if (!isPaused.value) {
        const elapsed = Date.now() - startTime;
        const addedPercent = (elapsed / IMAGE_DURATION) * 100;
        progress.value = Math.min(startProgress + addedPercent, 100);
      }
    }, 100);

    imageTimer = window.setTimeout(() => {
      if (!isPaused.value) {
        nextStory();
      }
    }, remainingTime);
  };

  const attemptVideoPlay = async () => {
    if (videoRef.value) {
      try {
        await videoRef.value.play();
        isPaused.value = false;
      } catch (e) {
        console.warn("Autoplay blocked or interrupted:", e);
        isPaused.value = true;
      }
    }
  };

  const togglePlay = () => {
    if (isVideo.value && videoRef.value) {
      if (videoRef.value.paused) {
        attemptVideoPlay();
      } else {
        videoRef.value.pause();
        isPaused.value = true;
      }
    } else {
      isPaused.value = !isPaused.value;
      if (isPaused.value) {
        clearImageTimers();
      } else {
        startImageTimer();
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
      progress.value = (videoRef.value.currentTime / videoRef.value.duration) * 100;
    }
  };

  const nextStory = () => {
    if (currentStoryIndex.value < storyItems.value.length - 1) {
      currentStoryIndex.value++;
    } else if (currentUserIndex.value < allUserStories.value.length - 1) {
      currentUserIndex.value++;
      currentStoryIndex.value = 0;
    } else {
      isPaused.value = true;
    }
  };

  const prevStory = () => {
    if (currentStoryIndex.value > 0) {
      currentStoryIndex.value--;
    } else if (currentUserIndex.value > 0) {
      currentUserIndex.value--;
      const prevUserStories = allUserStories.value[currentUserIndex.value];
      if (prevUserStories) {
        currentStoryIndex.value = prevUserStories.stories.length - 1;
      }
    }
  };

  onMounted(async () => {
    preloadImages();
    clearImageTimers();
    progress.value = 0;
    isPaused.value = false;

    await nextTick();

    if (currentStoryItem.value) {
      storiesStore.markStoryAsViewed(currentStoryItem.value.id, storiesStore.currentUserId);
    }

    if (isVideo.value) {
       if (videoRef.value) {
           videoRef.value.load();
           attemptVideoPlay();
       }
    } else {
       startImageTimer();
    }
  });

  onBeforeUnmount(() => {
    clearImageTimers();
    storyAudioPlayer.pause();
    storyAudioPlayer.src = '';
  });

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

  watch(isPaused, (paused) => {
    if (isMusicPlaying.value) {
      if (paused) {
        storyAudioPlayer.pause();
      } else {
        storyAudioPlayer.play().catch(console.error);
      }
    }
  });

  watch(currentStoryItem, async (newItem, oldItem) => {
    if (newItem && newItem.id !== oldItem?.id) {
      clearImageTimers();
      progress.value = 0;
      isPaused.value = false;

      await nextTick();
      preloadImages();

      const newUserId = allUserStories.value[currentUserIndex.value]?.userId;
      if (newUserId && route.params.userId !== newUserId) {
          router.replace({ name: 'userStories', params: { userId: newUserId } });
      }

      if (isVideo.value && videoRef.value) {
          videoRef.value.load();
          attemptVideoPlay();
      } else {
          startImageTimer();
      }

      storiesStore.markStoryAsViewed(newItem.id, storiesStore.currentUserId);
    }
  }, { deep: true });

  const setVideoRef = (el: any) => {
    videoRef.value = el;
  }

  const setImageRef = (el: any) => {
    imageRef.value = el;
  }

  return {
    videoRef,
    imageRef,
    setVideoRef,
    setImageRef,
    isPaused,
    isMuted,
    progress,
    isMusicPlaying,
    storyMusicMuted,
    togglePlay,
    toggleMasterMute,
    updateProgress,
    nextStory,
    prevStory,
  };
}
