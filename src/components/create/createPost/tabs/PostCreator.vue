<script setup lang="ts">
import { ref, computed, toRefs } from 'vue'
import { useCreatePostStore } from '@/stores/createPost'
import { useEventsStore } from '@/stores/events'
import { useAuthStore } from '@/stores/auth'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'

// --- COMPOSABLES ---
import { useLinkPreview } from '@/composables/shared/useLinkPreview'

// --- KOMPONENTY ---
import PostCreatorToolbar from '../PostCreatorToolbar.vue'
import LinkPreviewCard from '../item/LinkPreviewCard.vue'
import MediaPreview from '../item/MediaPreview.vue'
import MapPreview from '@/components/MapPreview.vue'
import CreatePoll from '../item/CreatePoll.vue'
import PostItem from '@/components/feed/post/PostItem.vue'
import HoverScrollbar from '@/components/common/HoverScrollbar.vue'
import PostCreatorHeader from '../item/PostCreatorHeader.vue'
import PostCreatorEditor from '../item/PostCreatorEditor.vue'
import SharedEventCard from '../item/SharedEventCard.vue'

// --- TYPY ---
import type { PostData } from '@/types/StoryElement'
import { type Post, type PostMedia } from '@/types/Post'

const props = defineProps<{
  sharedPost?: PostData | null
  sharedEventId?: string
}>()

const emit = defineEmits<{
  (e: 'publish', content: string): void
  (e: 'close'): void
}>()

const { t } = useI18n()
const createPostStore = useCreatePostStore()
const eventsStore = useEventsStore()
const authStore = useAuthStore()
const router = useRouter()

// --- STAN UŻYTKOWNIKA (Przed mutacją) ---
const currentUser = computed(() => authStore.currentUser)



const {
  content: postContent,
  privacy: selectedPrivacy,
  location: selectedLocation,
  gif: selectedGif,
  images: selectedImages,
  postVideoUrl,
} = toRefs(createPostStore.postData)

const { initialView } = toRefs(createPostStore.uiState)

// --- LOGIKA LINK PREVIEW ---
const { linkPreview, isLoadingPreview, fetchLinkMetadata, removeLinkPreview, resetLinkPreview } =
  useLinkPreview()

// --- PRZYPISANIE WSPÓŁDZIELONYCH DANYCH ---
const sharedEvent = computed(() =>
  props.sharedEventId ? eventsStore.getEventById(props.sharedEventId) : null,
)
const sharedPost = computed(() => createPostStore.postData.sharedPost)
const sharedPostAsPost = computed<Post | null>(() => {
  console.log('Mapping sharedPost to Post:', sharedPost.value)
  if (!sharedPost.value) return null

  return {
    id: sharedPost.value.id,
    authorId: sharedPost.value.author.id,
    content: sharedPost.value.content,
    date: new Date(sharedPost.value.timestamp).toLocaleDateString(),
    timestamp: sharedPost.value.timestamp,
    media: sharedPost.value.media,
    context: { privacy: 'public', taggedUsersIds: [] },
    stats: { reactions: 0, comments: 0, shares: 0 },
    reactions: {},
  } as Post
})

const displayAvatar = computed(() => {
  if (createPostStore.postData.isAnonymous) return '/img/anonymous-avatar.png'
  return currentUser.value?.avatar || (currentUser.value as any)?.avatarId || '/default-avatar.png'
})

const displayName = computed(() => {
  return createPostStore.postData.isAnonymous
    ? t('post.anonymousUser') || 'Anonim'
    : currentUser.value?.name || `${(currentUser.value as any)?.firstName || ''} ${(currentUser.value as any)?.lastName || ''}`.trim()
})

// --- VALIDATION ---
const isPublishButtonDisabled = computed(() => {
  if (sharedPost.value || props.sharedEventId) return false

  const isUploading = selectedImages.value.some(
    (img) => img.progress !== null && img.progress !== undefined,
  )
  if (isUploading) return true

  const hasContent = postContent.value.trim().length > 0
  const hasMedia =
    selectedImages.value.length > 0 || !!selectedGif.value || !!(postVideoUrl && postVideoUrl.value)
  const hasLocation = !!selectedLocation.value
  const hasLink = !!linkPreview.value

  return !(hasContent || hasMedia || hasLocation || hasLink)
})

// --- METODY ---

const handlePublish = async () => {
  if (isPublishButtonDisabled.value) return

  const isShare = !!sharedPost.value

  try {
    await createPostStore.publishPost()
    if (isShare) {
      router.push('/profile')
    }
  } catch (err) {
    console.error('Failed to publish post:', err)
  }

  emit('close')
  createPostStore.reset()
  resetLinkPreview()
}

const fileInput = ref<HTMLInputElement | null>(null)
createPostStore.triggerImageSelector = () => fileInput.value?.click()

const handleImageSelect = (event: Event) => {
  const target = event.target as HTMLInputElement
  const files = target.files
  if (files) {
    for (let i = 0; i < files.length; i++) {
      const file = files[i]
      if (file && (file.type.startsWith('image/') || file.type.startsWith('video/'))) {
        createPostStore.uploadVideoInChunks(file)
      }
    }
    target.value = ''
    resetLinkPreview()
  }
}

const handleDetectUrl = (url: string) => {
  if (
    !linkPreview.value &&
    selectedImages.value.length === 0 &&
    !selectedGif.value
  ) {
    fetchLinkMetadata(url)
  }
}


</script>

<template>
  <div class="post-creator-card p-0 min-h-[200px]">
    <div v-if="createPostStore.postData.targetType === 'Group'" class="my-3">
      <label
        class="flex items-center justify-between w-full p-4 bg-gray-100 dark:bg-theme-bg-tertiary rounded-xl cursor-pointer transition-colors hover:bg-gray-200 dark:hover:bg-theme-bg-hover"
      >
        <span class="text-base font-medium text-gray-700 dark:text-theme-text select-none">
          {{ t('post.anonymousPost') || 'Publikuj anonimowo' }}
        </span>
        <div class="relative inline-flex items-center cursor-pointer">
          <input type="checkbox" v-model="createPostStore.postData.isAnonymous" class="sr-only peer" />
          <div
            class="w-11 h-6 bg-gray-400 rounded-full peer dark:bg-gray-600 peer-checked:bg-blue-600 peer-checked:after:translate-x-full after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:rounded-full after:h-5 after:w-5 after:transition-all"
          ></div>
        </div>
      </label>
    </div>

    <PostCreatorHeader
      @open-feeling-selector="createPostStore.navigateTo('feeling')"
      @navigate-privacy="createPostStore.navigateTo('privacy')"
    />

    <HoverScrollbar :maxHeight="'360px'">
      <PostCreatorEditor
        :has-shared-post="!!sharedPost"
        @detect-url="handleDetectUrl"
      />

      <CreatePoll v-if="initialView == 'poll'" />

      <LinkPreviewCard
        :preview="linkPreview as any"
        :loading="isLoadingPreview"
        @remove="removeLinkPreview"
      />

      <MapPreview
        :selectedLocation="selectedLocation"
        @removeLocation="createPostStore.postData.location = null"
        v-if="selectedLocation"
      />

      <MediaPreview />

      <input
        ref="fileInput"
        type="file"
        accept="image/*,video/mp4"
        class="hidden"
        @change="handleImageSelect"
        multiple
      />

      <div v-if="sharedPostAsPost" class="mb-4 rounded-lg overflow-hidden">
        <PostItem :post="sharedPostAsPost" :is-shared="true" />
      </div>

      <SharedEventCard :shared-event="sharedEvent as any" />
    </HoverScrollbar>

    <PostCreatorToolbar />

    <button
      :disabled="isPublishButtonDisabled"
      class="w-full py-2 rounded-lg font-[15px] text-base transition-colors duration-200"
      :class="
        isPublishButtonDisabled
          ? 'bg-gray-200 text-gray-400 cursor-not-allowed dark:bg-gray-700 dark:text-gray-500'
          : 'bg-[#1877f2] text-white hover:bg-blue-700'
      "
      @click="handlePublish"
    >
      {{ t('post.publish') }}
    </button>
  </div>
</template>
