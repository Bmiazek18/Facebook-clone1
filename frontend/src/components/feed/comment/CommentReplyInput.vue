<script setup lang="ts">
import { ref, computed, watch, nextTick, inject, type Ref } from 'vue'
import EmoticonHappyOutline from 'vue-material-design-icons/EmoticonHappyOutline.vue'
import CameraOutline from 'vue-material-design-icons/CameraOutline.vue'
import FileGifBox from 'vue-material-design-icons/FileGifBox.vue'
import Send from 'vue-material-design-icons/Send.vue'
import ChevronDown from 'vue-material-design-icons/ChevronDown.vue'
import CloseIcon from 'vue-material-design-icons/Close.vue'
import GifSelector from '@/components/create/createPost/tabs/GifSelector.vue'
import { useAuthStore } from '@/stores/auth'
import { useComments } from '@/composables/feed/useComments'
import type { Post } from '@/types/Post'
import LazyEmojiPicker from '@/components/common/LazyEmojiPicker.vue'
import { useCommentsStore } from '@/stores/comments'
import { Dropdown as VDropdown } from 'floating-vue'
import MentionInput from '@/components/MentionInput.vue'
import type { LinkPreviewData } from '@/types/Post'

const props = defineProps<{
  postId: string
  parentId?: number | null
}>()

const authStore = useAuthStore()
const { addComment } = useComments()
const allPosts = inject<Ref<Post[]>>('allPosts', ref([]))
const post = computed(() => allPosts.value.find((p) => String(p.id) === String(props.postId)))
const commentsStore = useCommentsStore()

const postContent = ref('')
const mentionInputRef = ref<InstanceType<typeof MentionInput> | null>(null)

// --- LINK PREVIEW STATE ---
const linkPreview = ref<LinkPreviewData | null>(null)
const isPreviewDismissed = ref(false)
const isLoadingPreview = ref(false)
let linkCheckTimeout: ReturnType<typeof setTimeout> | null = null

const taggedUser = computed(() => {
  if (commentsStore.activeReplyInput === props.parentId) {
    return commentsStore.replyingToUser
  }
  return null
})

watch(
  taggedUser,
  (newUser) => {
    if (newUser && !postContent.value.includes(`[@${newUser.id}]`)) {
      postContent.value = `[@${newUser.id}] ` + postContent.value
      nextTick(() => {
        mentionInputRef.value?.renderContentEditable() // Explicitly call renderContentEditable
        mentionInputRef.value?.moveCursorToEnd()
      })
    }
  },
  { immediate: true },
)

watch(postContent, (newVal) => {
  if (linkCheckTimeout) clearTimeout(linkCheckTimeout)
  linkCheckTimeout = setTimeout(() => {
    const urlMatch = newVal.match(/(https?:\/\/[^\s]+)/g)
    if (urlMatch && urlMatch.length > 0 && !isPreviewDismissed.value) {
      fetchLinkMetadata(urlMatch[0])
    }
  }, 500)
})

const fetchLinkMetadata = async (url: string) => {
  if (isLoadingPreview.value) return
  isLoadingPreview.value = true
  try {
    const response = await fetch(
      `${import.meta.env.VITE_API_URL || 'http://localhost:8080'}/linkguard/graphql`,
      {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          query: `query ScrapeOg($url: String!) { scrapeOg(url: $url) { title description image siteName } }`,
          variables: { url },
        }),
      },
    )
    const result = await response.json()
    if (!response.ok || result.errors?.length)
      throw new Error(result.errors?.[0]?.message || 'LinkGuard unavailable')
    const data = result.data.scrapeOg
    console.log('Pobrane dane Open Graph (komentarz):', data)

    linkPreview.value = {
      url: url,
      domain: data.domain || new URL(url).hostname,
      title: data.title || 'Link Preview',
      description: data.description || '',
      image: data.image || undefined,
    }
  } catch (error) {
    console.error('Błąd podczas pobierania metadanych linku (komentarz):', error)
    // Fallback if API fails
    linkPreview.value = {
      url: url,
      domain: new URL(url).hostname,
      title: url,
      description: '',
    }
  } finally {
    isLoadingPreview.value = false
  }
}

const removeLinkPreview = () => {
  linkPreview.value = null
  isPreviewDismissed.value = true
}

const selectedImage = ref<string | null>(null)
const fileInput = ref<HTMLInputElement | null>(null)

const selectGif = (gif: string) => {
  selectedImage.value = gif
  removeLinkPreview() // Remove link preview if GIF is selected
}

const addEmoji = (emoji: { native: string }) => {
  mentionInputRef.value?.addEmoji(emoji)
}

const onFileChange = (event: Event) => {
  const target = event.target as HTMLInputElement
  const file = target.files?.[0]
  if (file) {
    const reader = new FileReader()
    reader.onload = (e) => {
      selectedImage.value = e.target?.result as string
      removeLinkPreview() // Remove link preview if image is selected
    }
    reader.readAsDataURL(file)
  }
}

const removeImage = () => {
  selectedImage.value = null
  if (fileInput.value) {
    fileInput.value.value = ''
  }
}

const emit = defineEmits<{
  (e: 'onCommentSubmitted'): void
}>()

const submitComment = () => {
  if (authStore.currentUser) {
    const nameParts = (authStore.currentUser.name || '').split(' ')
    const firstName = nameParts[0] || ''
    const lastName = nameParts.slice(1).join(' ') || ''

    const newComment = {
      id: Date.now(),
      authorId: authStore.currentUser.id,
      author: {
        id: authStore.currentUser.id,
        firstName,
        lastName,
        avatar: authStore.currentUser.avatar || null,
      },
      content: postContent.value,
      date: new Date().toISOString(),
      timestamp: Date.now(),
      likesCount: 0,
      reactions: {},
      image: selectedImage.value === null ? undefined : selectedImage.value,
      linkPreview: linkPreview.value || undefined,
      replies: [],
    }
    addComment(post.value, newComment, props.parentId || null)
    postContent.value = ''
    selectedImage.value = null
    linkPreview.value = null
    isPreviewDismissed.value = false
    emit('onCommentSubmitted')
  }
}

const focusInput = () => {
  mentionInputRef.value?.focus()
}

defineExpose({ focusInput })
</script>

<template>
  <div class="flex items-start w-full  ">
    <div class="relative shrink-0 mr-1.5 group cursor-pointer">
      <img
        class="w-8 h-8 rounded-full object-cover"
        :src="authStore.currentUser?.avatar"
        alt="Avatar"
      />
      <div
        class="absolute -bottom-1 -right-1 bg-[#e4e6eb] rounded-full w-4 h-4 flex items-center justify-center border-[2px] border-white text-black"
      >
        <ChevronDown :size="10" />
      </div>
    </div>

    <div class="grow rounded-[18px] px-3 py-2 relative bg-theme-comment-bg transition-colors">
      <MentionInput
        ref="mentionInputRef"
        v-model="postContent"
        placeholder="Napisz komentarz..."
        @focus="commentsStore.setReplyInputFocus()"
        @blur="commentsStore.clearReplyInputFocus()"
      />

      <div v-if="selectedImage" class="relative mt-3 mb-1 px-1 group inline-block">
        <div class="relative inline-block">
          <img
            :src="selectedImage"
            class="rounded-xl border border-black/5 dark:border-white/10 max-h-[220px] w-[40%] w-auto object-cover shadow-sm bg-white dark:bg-gray-800"
            alt="Załączony obraz"
          />

          <button
            @click="removeImage"
            class="absolute top-2 right-2 w-7 h-7 flex items-center justify-center bg-white text-gray-700 hover:text-gray-900 rounded-full shadow-md border border-gray-100 transition-all opacity-0 group-hover:opacity-100 hover:scale-105 active:scale-95 cursor-pointer z-10"
            title="Usuń obraz"
            type="button"
          >
            <CloseIcon :size="16" />
          </button>
        </div>
      </div>

      <div class="flex justify-between items-center mt-1 text-gray-500">
        <div class="flex items-center space-x-0 -ml-1">
          <VDropdown :distance="10">
            <button
              class="hover:bg-[rgba(0,0,0,0.05)] p-1 rounded-full transition-colors"
              title="Wstaw emoji"
            >
              <EmoticonHappyOutline :size="18" />
            </button>
            <template #popper>
              <LazyEmojiPicker @select="addEmoji" />
            </template>
          </VDropdown>

          <input
            type="file"
            ref="fileInput"
            @change="onFileChange"
            accept="image/*"
            class="hidden"
          />
          <button
            @click="fileInput?.click()"
            class="hover:bg-[rgba(0,0,0,0.05)] p-1 rounded-full transition-colors"
            title="Dołącz zdjęcie"
          >
            <CameraOutline :size="18" />
          </button>

          <VDropdown :distance="10">
            <button
              class="hover:bg-[rgba(0,0,0,0.05)] p-1 rounded-full transition-colors"
              title="Wstaw GIF"
            >
              <FileGifBox :size="18" />
            </button>
            <template #popper>
              <GifSelector @select="selectGif" />
            </template>
          </VDropdown>
        </div>

        <button
          @click="submitComment"
          class="p-1 rounded-full transition-colors cursor-pointer hover:bg-[rgba(0,0,0,0.05)]"
          :class="
            postContent.length > 0 || selectedImage || linkPreview
              ? 'text-blue-500'
              : 'text-gray-300 pointer-events-none'
          "
        >
          <Send :size="16" class="ml-0.5" />
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.material-design-icon {
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>
