<script setup lang="ts">
import { ref, computed, watch, reactive, onMounted } from 'vue'
import { useRouter } from '#imports'
import { useAuthStore } from '@/stores/auth'
import { useQuery } from '@vue/apollo-composable'
import { gql } from 'graphql-tag'
import { processActiveStories } from '@/utils/stories'
import { useStoryPlayback } from '@/composables/media/useStoryPlayback'
import { formatTimeAgo } from '@/utils/timeFormatter'

// Importy komponentów UI
import NavbarRight from '~/layouts/navbar/NavbarRight.vue'
import StorySidebar from '@/components/feed/stories/StorySidebar.vue'
import StoryMediaRenderer from '@/components/feed/stories/StoryMediaRenderer.vue'
import StoryOverlays from '@/components/feed/stories/StoryOverlays.vue'
import StoryFooter from '@/components/feed/stories/StoryFooter.vue'
import StoryViewersModal from '@/components/feed/stories/StoryViewersModal.vue'
import StoryTextElementRenderer from '@/components/feed/stories/StoryTextElementRenderer.vue'

// Ikony nawigacyjne, które faktycznie należą do tego widoku
import ChevronLeft from 'vue-material-design-icons/ChevronLeft.vue'
import ChevronRight from 'vue-material-design-icons/ChevronRight.vue'

const router = useRouter()
const authStore = useAuthStore()

const GET_ACTIVE_STORIES = gql`
  query GetActiveStories($currentUserId: ID!) {
    getActiveStories(currentUserId: $currentUserId) {
      id
      authorId
      author {
        id
        firstName
        lastName
        avatarId
        avatar
      }
      mediaUrl
      thumbMediaUrl
      mediaType
      text
      createdAt
      expiresAt
    }
  }
`
const { result } = useQuery(GET_ACTIVE_STORIES, {
  currentUserId: String(authStore.currentUserId)
})

const props = defineProps<{
  userId?: string
}>()

// --- GŁÓWNY STAN ---
const currentUserIndex = ref(0)
const currentStoryIndex = ref(0)
const showViewers = ref(false)

// --- COMPUTED: Podstawowe dane ---
const allUserStories = computed(() => {
  const rawStories = result.value?.getActiveStories ?? []
  return processActiveStories(rawStories, String(authStore.currentUserId))
})
const currentUserStories = computed(() => allUserStories.value[currentUserIndex.value] ?? null)
const currentStoryItem = computed(
  () => currentUserStories.value?.stories[currentStoryIndex.value] ?? null,
)
const storyItems = computed(() => currentUserStories.value?.stories ?? [])

const isOwner = computed(() => String(authStore.currentUserId) === currentUserStories.value?.userId)

const isVideo = computed(() => {
  const item = currentStoryItem.value
  if (!item) return false
  if (item.type === 'video') return true
  const url = item.imageUrl || ''
  return ['.mp4', '.webm', '.ogg', '.mov', '.avi'].some((ext) => url.toLowerCase().endsWith(ext))
})

// --- COMPOSABLE: Logika odtwarzania ---
const {
  isPaused,
  progress,
  isMusicPlaying,
  storyMusicMuted,
  togglePlay,
  toggleMasterMute,
  updateProgress,
  nextStory,
  prevStory,
  setVideoRef,
  setImageRef,
} = useStoryPlayback(
  currentUserIndex,
  currentStoryIndex,
  allUserStories,
  currentStoryItem,
  storyItems,
  isVideo,
)

// --- ZGRUPOWANY STAN DLA PODKOMPONENTÓW ---
const playbackState = reactive({
  // Przekazujemy ref'y, aby zachować reaktywność
  storyItems,
  currentStoryIndex,
  progress,
  isPaused,
  isVideo,
  isMusicPlaying,
  storyMusicMuted,
  currentItem: computed(() => {
    if (!currentStoryItem.value || !currentUserStories.value) {
      return {
        id: '',
        src: '',
        type: 'image' as const,
        user: { name: '', avatar: '' },
        createAt: '',
      }
    }
    const story = currentStoryItem.value
    return {
      id: story.id,
      src: story.imageUrl || '',
      type: story.type,
      user: {
        name: story.originalUserName || currentUserStories.value.userName,
        avatar: story.originalUserAvatar || currentUserStories.value.userAvatar,
      },
      createAt: formatTimeAgo(story.createdAt),
    }
  }),
  musicElement: computed(() =>
    (currentStoryItem.value?.elements as any[])?.find((el: any) => el.musicTitle && el.musicArtist),
  ),
})

// --- WATCHER: Reakcja na zmianę trasy ---
watch(
  () => [allUserStories.value, props.userId] as const,
  ([stories, userId]: readonly [any[], string | undefined]) => {
    if (stories && stories.length > 0 && userId) {
      const userIndex = stories.findIndex((us: any) => String(us.userId) === String(userId))
      if (userIndex !== -1 && currentUserIndex.value !== userIndex) {
        currentUserIndex.value = userIndex
        currentStoryIndex.value = 0
      }
    }
  },
  { immediate: true },
)
const isFirstMount = ref(true)

onMounted(() => {
  setTimeout(() => {
    isFirstMount.value = false
  }, 500)
})
</script>

<template>
  <div class="flex h-screen w-full bg-black overflow-hidden   select-none">
    <StorySidebar />

    <main class="flex-1 relative flex flex-col bg-black">
      <div class="absolute top-0 right-0 p-4 gap-3 z-30 hidden md:flex">
        <NavbarRight />
      </div>

      <div class="flex-1 flex flex-col items-center justify-center w-full h-full relative p-0">
        <div
          class="flex flex-col h-full md:aspect-9/16 md:w-auto relative group md:mt-4 w-full aspect-auto"
        >
          <div
            @click.stop="prevStory"
            v-if="currentStoryIndex > 0 || currentUserIndex > 0"
            class="absolute -left-28 top-0 bottom-0 w-32 z-40 cursor-pointer hidden md:flex items-center justify-center group/btn"
          >
            <button
              class="w-13 h-13 bg-gray-400 group-hover/btn:bg-gray-200 rounded-full flex items-center justify-center text-gray-800 transition-all duration-300 shadow-xl backdrop-blur-sm group-hover/btn:-translate-x-3"
            >
              <ChevronLeft :size="32" />
            </button>
          </div>

          <div
            @click.stop="nextStory"
            v-if="
              currentStoryIndex < storyItems.length - 1 ||
              currentUserIndex < allUserStories.length - 1
            "
            class="absolute -right-28 top-0 bottom-0 w-32 z-40 cursor-pointer hidden md:flex items-center justify-center group/btn"
          >
            <button
              class="w-13 h-13 bg-gray-400 group-hover/btn:bg-gray-200 rounded-full flex items-center justify-center text-gray-800 transition-all duration-300 shadow-xl backdrop-blur-sm group-hover/btn:translate-x-3"
            >
              <ChevronRight :size="32" />
            </button>
          </div>

          <div
            class="relative flex-1 bg-gray-900 rounded-none md:rounded-xl overflow-hidden shadow-2xl flex flex-col"
            :class="{ 'media-init-animation': isFirstMount }"
          >
            <div
              class="absolute inset-y-0 left-0 w-[20%] z-20 cursor-pointer"
              @click.stop="prevStory"
            ></div>
            <div
              class="absolute inset-y-0 right-0 w-[20%] z-20 cursor-pointer"
              @click.stop="nextStory"
            ></div>
            <div
              class="absolute inset-y-0 left-[20%] right-[20%] z-10 cursor-pointer"
              @click.stop="togglePlay"
            ></div>

            <StoryMediaRenderer
              :current-item="(playbackState.currentItem as any)"
              :is-video="isVideo"
              :set-video-ref="setVideoRef"
              :set-image-ref="setImageRef"
              :update-progress="updateProgress"
              :next-story="nextStory"
            />

            <StoryOverlays
              v-bind="(playbackState as any)"
              :toggle-play="togglePlay"
              :toggle-master-mute="toggleMasterMute"
            />

            <template v-if="currentStoryItem?.elements">
              <StoryTextElementRenderer
                v-for="element in currentStoryItem.elements.filter((e) => e.type === 'text')"
                :key="element.id"
                :element="element"
                :style="{
                  position: 'absolute',
                  left: `${element.x}%`,
                  top: `${element.y}%`,
                  width: element.width ? `${element.width}%` : 'auto',
                  height: element.height ? `${element.height}%` : 'auto',
                  transform: `rotate(${element.rotation}deg) scale(${element.scale})`,
                  zIndex: 25,
                }"
              />
            </template>

            <div
              v-if="currentStoryItem?.sharedPostInfo"
              class="absolute cursor-pointer z-30 border-2 border-transparent hover:border-white/50 rounded transition"
              :style="{
                top: `${currentStoryItem.sharedPostInfo.y}%`,
                left: `${currentStoryItem.sharedPostInfo.x}%`,
                width: `${currentStoryItem.sharedPostInfo.width}%`,
                height: `${currentStoryItem.sharedPostInfo.height}%`,
              }"
              @click.stop="router.push(`/post/${currentStoryItem.sharedPostInfo.postId}`)"
            ></div>

            <div
              v-for="(tag, idx) in currentStoryItem?.userTags || []"
              :key="`user-tag-${tag.userId}-${idx}`"
              class="absolute cursor-pointer z-30 border-2 border-transparent hover:border-white/60 rounded-full transition"
              :style="{
                top: `${tag.y}%`,
                left: `${tag.x}%`,
                width: `${tag.width}%`,
                height: `${tag.height}%`,
              }"
              @click.stop="router.push(`/profile/${tag.userId}`)"
            ></div>

            <a
              v-if="currentStoryItem?.sharedLinkInfo"
              :href="currentStoryItem.sharedLinkInfo.url"
              target="_blank"
              rel="noopener noreferrer"
              @click.stop
              class="absolute cursor-pointer z-30 border-2 border-transparent hover:border-white/50 rounded transition"
              :style="{
                top: `${currentStoryItem.sharedLinkInfo.y}%`,
                left: `${currentStoryItem.sharedLinkInfo.x}%`,
                width: `${currentStoryItem.sharedLinkInfo.width}%`,
                height: `${currentStoryItem.sharedLinkInfo.height}%`,
              }"
            ></a>
          </div>

          <StoryViewersModal
            :show="showViewers"
            :interactions="currentStoryItem?.interactions ?? []"
            @update:show="showViewers = $event"
            @update:is-paused="isPaused = $event"
          />
        </div>

        <StoryFooter
          :is-owner="isOwner"
          :current-story-item="(currentStoryItem as any)"
          :current-item="(playbackState.currentItem as any)"
          v-model:is-paused="isPaused"
          v-model:show-viewers="showViewers"
        />
      </div>
    </main>
  </div>
</template>

<style scoped>
.custom-scrollbar::-webkit-scrollbar {
  width: 8px;
}
.custom-scrollbar::-webkit-scrollbar-track {
  background: transparent;
}
.custom-scrollbar::-webkit-scrollbar-thumb {
  background-color: #bcc0c4;
  border-radius: 4px;
}
.media-init-animation {
  animation: scale-from-top-left 0.3s cubic-bezier(0.25, 0.46, 0.45, 0.94) forwards; /* Ease-out quad */
  transform-origin: top left;
}

@keyframes scale-from-top-left {
  0% {
    transform: scale(0.4);
    opacity: 0;
  }
  100% {
    transform: scale(1);
    opacity: 1;
  }
}
</style>
