<script setup lang="ts">
import { ref, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'

import Earth from 'vue-material-design-icons/Earth.vue'
import ThumbUp from 'vue-material-design-icons/ThumbUp.vue'
import Close from 'vue-material-design-icons/Close.vue'
import PostImageGallery from './PostImageGallery.vue'
import PlayerVideo from './PlayerVideo.vue'
import { useTheme } from '@/composables/useTheme'
import { usePostReactions } from '@/composables/usePostReactions'
import { useVideoAutoplay } from '@/composables/useVideoAutoplay'
import Modal from './Modal.vue'
import PostModal from './PostModal.vue'
import ShareAsPostModal from './ShareAsPostModal.vue'
import PostHeader from './post/PostHeader.vue'
import PostActions from './post/PostActions.vue'
import { useStoryShareStore } from '@/stores/storyShare'
import { usePostsStore, type SharedPost } from '@/stores/posts'
import type { PostData } from '@/types/StoryElement'

useI18n()

interface PostProp {
  id: number
  content: string
  imageUrl?: string
  images?: string[]
  videoUrl?: string
  authorName?: string
  authorAvatar?: string
}

const props = defineProps<{
  post?: PostProp
  sharedPost?: SharedPost
}>()

const emit = defineEmits<{
  (e: 'delete', postId: string): void
}>()

const router = useRouter()
const storyShareStore = useStoryShareStore()
const postsStore = usePostsStore()

const isModalOpen = ref(false)
const isShareAsPostModalOpen = ref(false)

const toggleModal = () => {
    isModalOpen.value = !isModalOpen.value
}

const { isDark } = useTheme()

// Determine if this is a shared post view
const isSharedPostView = computed(() => !!props.sharedPost)

// Get display data based on whether it's a shared post or regular post
const displayData = computed(() => {
  if (props.sharedPost) {
    return {
      authorName: props.sharedPost.originalPost.authorName,
      authorAvatar: props.sharedPost.originalPost.authorAvatar,
      content: props.sharedPost.originalPost.content,
      imageUrl: props.sharedPost.originalPost.imageUrl,
      images: props.sharedPost.originalPost.images || (props.sharedPost.originalPost.imageUrl ? [props.sharedPost.originalPost.imageUrl] : []),
      videoUrl: props.sharedPost.originalPost.videoUrl,
      sharedBy: props.sharedPost.sharedBy,
      comment: props.sharedPost.comment
    }
  }
  return {
    authorName: props.post?.authorName || 'Bartosz Miazek',
    authorAvatar: props.post?.authorAvatar || 'https://scontent-waw2-1.xx.fbcdn.net/v/t39.30808-1/295055057_582985040112298_215415809791370036_n.jpg',
    content: props.post?.content || '',
    imageUrl: props.post?.imageUrl || '',
    images: props.post?.images || (props.post?.imageUrl ? [props.post.imageUrl] : []),
    videoUrl: props.post?.videoUrl,
    sharedBy: null,
    comment: null
  }
})

// Convert to PostData format for sharing
const postData = computed<PostData>(() => {
  if (props.sharedPost) {
    return props.sharedPost.originalPost
  }
  return {
    id: String(props.post?.id || 0),
    authorName: displayData.value.authorName,
    authorAvatar: displayData.value.authorAvatar,
    content: displayData.value.content,
    imageUrl: displayData.value.images[0] || displayData.value.imageUrl,
    images: displayData.value.images,
    timestamp: Date.now()
  }
})

const shareToStory = () => {
  storyShareStore.setPostToShare(postData.value)
  router.push('/stories/create')
}

const shareAsMyPost = () => {
  isShareAsPostModalOpen.value = true
}

const handleShareAsPost = (comment: string) => {
  postsStore.addSharedPost(postData.value, comment)
  isShareAsPostModalOpen.value = false
  router.push('/profile')
}

const handleDelete = () => {
  if (props.sharedPost) {
    emit('delete', props.sharedPost.id)
  }
}

// Reactions - using composable
const { userReaction, likesCount, getReactionIcon, handleReaction } = usePostReactions(24)

// Video autoplay - using composable
const videoContainerRef = ref<HTMLElement | null>(null)
const videoRef = ref<InstanceType<typeof PlayerVideo> | null>(null)
useVideoAutoplay(videoContainerRef)
</script>

<template>
  <div class="w-full bg-theme-bg-secondary rounded-lg my-4 shadow-sm dark:shadow-lg ">

    <div v-if="isSharedPostView && displayData.sharedBy" class="px-4 pt-3 pb-2">
      <div class="flex items-center gap-2 mb-1">
        <img
          class="rounded-full w-10 h-10 object-cover border border-theme-border/20 cursor-pointer"
          :src="displayData.sharedBy.avatar"
          :alt="displayData.sharedBy.name"
        >
        <div class="flex-1 min-w-0">
          <div class="flex items-center flex-wrap">
            <span class="font-semibold text-theme-text text-[15px] hover:underline cursor-pointer">
              {{ displayData.sharedBy.name }}
            </span>
          </div>
          <div class="flex items-center text-xs text-theme-text-secondary mt-0.5">
             <span class="hover:underline cursor-pointer">25 czerwca</span>
             <span class="mx-1">·</span>
             <Earth :size="12" fillColor="#65676B" />
          </div>
        </div>

        <button @click="handleDelete" class="rounded-full p-2 hover:bg-theme-hover transition-colors">
          <Close :size="20" :fillColor="isDark ? '#E4E6EB' : '#65676B'" />
        </button>
      </div>

      <p v-if="displayData.comment" class="text-[15px] text-theme-text mb-2 leading-normal">
        {{ displayData.comment }}
      </p>
    </div>

    <div v-if="!isSharedPostView">
      <PostHeader
        :author-name="displayData.authorName"
        :author-avatar="displayData.authorAvatar"
      />

      <div class="px-4 py-1 pb-3 text-[15px] text-theme-text leading-normal whitespace-pre-line">
        {{ displayData.content }}
      </div>

      <!-- Video -->
      <div v-if="displayData.videoUrl" ref="videoContainerRef" class="w-full">
        <PlayerVideo ref="videoRef" :url="displayData.videoUrl" />
      </div>

      <!-- Images -->
      <PostImageGallery v-else-if="displayData.images.length > 0" :images="displayData.images" :post-id="props.post?.id ?? 0" />
    </div>

    <div v-else class="mx-3 mb-3 border border-theme-border rounded-lg overflow-hidden cursor-pointer hover:bg-gray-50 dark:hover:bg-gray-800/30 transition-colors">

      <router-link to="/photo" class="block w-full border-b border-theme-border/30">
        <img
          class="w-full h-auto object-cover max-h-[500px]"
          :src="displayData.imageUrl"
          alt="Shared content"
        >
      </router-link>

      <div class="p-3 bg-gray-50 dark:bg-[#18191A] dark:border-t dark:border-gray-700">
        <div class="flex items-center gap-2 mb-1">
           <img
              class="rounded-full w-5 h-5 object-cover"
              :src="displayData.authorAvatar"
           >
           <div class="text-[13px] font-semibold text-theme-text leading-none">
              {{ displayData.authorName }}
           </div>
        </div>

        <div class="text-[14px] font-bold text-theme-text leading-tight mb-0.5 line-clamp-1">
          {{ displayData.content.substring(0, 50) }}{{ displayData.content.length > 50 ? '...' : '' }}
        </div>
        <div class="text-[13px] text-theme-text-secondary line-clamp-1">
           LZPN ODDZIAŁ BIAŁA PODLASKA
        </div>
      </div>
    </div>

    <div class="px-4 pt-2.5">
      <div class="flex items-center justify-between text-theme-text-secondary text-[15px]">
        <div class="flex items-center gap-1.5 cursor-pointer hover:underline">
          <!-- Reaction icon based on user's reaction or default like -->
          <div
            class="rounded-full p-0.5 flex items-center justify-center w-[18px] h-[18px]"

          >
            <img
              v-if="userReaction"
              :src="getReactionIcon(userReaction).src"
              class="w-5 h-5"
              alt=""
            >

          </div>
<ThumbUp fillColor="#0000FF" :size="18" />


          <span class="text-theme-text-secondary hover:underline">
            <template v-if="userReaction">
              <span >Ty</span><span v-if="likesCount > 1">, Anna Kowalska i {{ likesCount - 2 }} {{ likesCount - 2 === 1 ? 'osoba' : 'osoby' }}</span>
            </template>
            <template v-else>
              <span >Anna Kowalska</span> i {{ likesCount - 1 }} {{ likesCount - 1 === 1 ? 'osoba' : 'innych osób' }}
            </template>
          </span>
        </div>
        <div class="flex items-center gap-3">
          <span class="hover:underline cursor-pointer">3 komentarze</span>
          <span class="hover:underline cursor-pointer">1 udostępnienie</span>
        </div>
      </div>
    </div>

    <PostActions
      @react="handleReaction"
      @comment="toggleModal"
      @share-as-post="shareAsMyPost"
      @share-to-story="shareToStory"
    />

    <Modal v-if="isModalOpen" @close="toggleModal"><PostModal/></Modal>
    <ShareAsPostModal :is-open="isShareAsPostModalOpen" :post="postData" @close="isShareAsPostModalOpen = false" @share="handleShareAsPost"/>
  </div>
</template>
