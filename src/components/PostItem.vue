<script setup lang="ts">
import { ref, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'

import Earth from 'vue-material-design-icons/Earth.vue'
import ThumbUp from 'vue-material-design-icons/ThumbUp.vue'
import DotsHorizontal from 'vue-material-design-icons/DotsHorizontal.vue'
import Close from 'vue-material-design-icons/Close.vue'
import MessageOutline from 'vue-material-design-icons/MessageOutline.vue'
import ShareIcon from 'vue-material-design-icons/ShareVariant.vue'
import BookOpenPageVariant from 'vue-material-design-icons/BookOpenPageVariant.vue'
import Pencil from 'vue-material-design-icons/Pencil.vue'
import ReactionButton from './ReactionButton.vue'
import { useTheme } from '@/composables/useTheme'
import ProfilePopper from './ProfilePopper.vue'
import Modal from './Modal.vue'
import PostModal from './PostModal.vue'
import ShareAsPostModal from './ShareAsPostModal.vue'
import { useStoryShareStore } from '@/stores/storyShare'
import { usePostsStore, type SharedPost } from '@/stores/posts'
import type { PostData } from '@/types/StoryElement'

useI18n()

interface PostProp {
  id: number
  content: string
  imageUrl: string
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
const isShareMenuOpen = ref(false)
const isShareAsPostModalOpen = ref(false)

const toggleModal = () => {
    isModalOpen.value = !isModalOpen.value
}

const toggleShareMenu = () => {
    isShareMenuOpen.value = !isShareMenuOpen.value
}

const closeShareMenu = () => {
    isShareMenuOpen.value = false
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
      sharedBy: props.sharedPost.sharedBy,
      comment: props.sharedPost.comment
    }
  }
  return {
    authorName: props.post?.authorName || 'Bartosz Miazek',
    authorAvatar: props.post?.authorAvatar || 'https://scontent-waw2-1.xx.fbcdn.net/v/t39.30808-1/295055057_582985040112298_215415809791370036_n.jpg',
    content: props.post?.content || '',
    imageUrl: props.post?.imageUrl || '',
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
    imageUrl: displayData.value.imageUrl,
    timestamp: Date.now()
  }
})

const shareToStory = () => {
  storyShareStore.setPostToShare(postData.value)
  router.push('/stories/create')
  closeShareMenu()
}

const shareAsMyPost = () => {
  closeShareMenu()
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

// Reaction handling
const currentUser = {
  name: 'Bartosz Miazek',
  avatar: 'https://scontent-waw2-1.xx.fbcdn.net/v/t39.30808-1/295055057_582985040112298_215415809791370036_n.jpg'
}

const userReaction = ref<string | null>(null)
const baseLikesCount = 24

const defaultReactionIcon = { src: 'https://fonts.gstatic.com/s/e/notoemoji/latest/1f44d/512.gif', bg: 'bg-blue-500' }

const reactionIcons: Record<string, { src: string, bg: string }> = {
  like: defaultReactionIcon,
  love: { src: 'https://fonts.gstatic.com/s/e/notoemoji/latest/2764_fe0f/512.gif', bg: 'bg-red-500' },
  haha: { src: 'https://fonts.gstatic.com/s/e/notoemoji/latest/1f606/512.gif', bg: 'bg-yellow-400' },
  wow: { src: 'https://fonts.gstatic.com/s/e/notoemoji/latest/1f62f/512.gif', bg: 'bg-yellow-400' },
  sad: { src: 'https://fonts.gstatic.com/s/e/notoemoji/latest/1f622/512.gif', bg: 'bg-yellow-400' },
  angry: { src: 'https://fonts.gstatic.com/s/e/notoemoji/latest/1f621/512.gif', bg: 'bg-orange-500' }
}

const getReactionIcon = (reaction: string | null): { src: string, bg: string } => {
  if (!reaction) return defaultReactionIcon
  return reactionIcons[reaction] ?? defaultReactionIcon
}

const likesCount = computed(() => baseLikesCount + (userReaction.value ? 1 : 0))

const handleReaction = (reaction: string | null) => {
  userReaction.value = reaction
}
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
      <div class="px-4 pt-3 pb-1">
        <div class="flex items-start">
          <button class="mr-2.5">
            <img
              class="rounded-full w-10 h-10 object-cover "
              :src="displayData.authorAvatar"
              :alt="displayData.authorName"
            >
          </button>

          <div class="flex-1 min-w-0 mt-0.5">
            <ProfilePopper :name="displayData.authorName" class="font-semibold text-theme-text text-[15px] hover:underline cursor-pointer leading-tight block" />
            <div class="flex items-center text-[13px] text-theme-text-secondary mt-0.5">
              <span class="hover:underline cursor-pointer">17 grudnia</span>
              <span class="mx-1">·</span>
              <Earth :size="12" fillColor="#65676B" v-tooltip="'Publiczne'" />
            </div>
          </div>

          <div class="flex items-center -mr-2">
            <button class="rounded-full p-2 hover:bg-theme-hover transition-colors">
              <DotsHorizontal :size="20" :fillColor="isDark ? '#B0B3B8' : '#65676B'" />
            </button>
            <button class="rounded-full p-2 hover:bg-theme-hover transition-colors">
              <Close :size="20" :fillColor="isDark ? '#B0B3B8' : '#65676B'" />
            </button>
          </div>
        </div>
      </div>

      <div class="px-4 py-1 pb-3 text-[15px] text-theme-text leading-normal whitespace-pre-line">
        {{ displayData.content }}
      </div>

      <router-link to="/photo" class="block w-full bg-black/5">
        <img
          class="w-full h-auto object-cover max-h-[600px] cursor-pointer"
          :src="displayData.imageUrl"
          alt="Post content"
        >
      </router-link>
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



    <div class="px-2 py-1 flex items-center justify-between relative z-10">
        <div class="flex-1">
             <ReactionButton @react="handleReaction" />
        </div>
        <button
            @click="toggleModal"
            class="flex-1 flex items-center justify-center gap-2 h-9 rounded hover:bg-theme-hover transition-colors cursor-pointer text-theme-text-secondary font-semibold text-[15px]"
        >
            <MessageOutline :size="20" class="text-gray-500 dark:text-gray-400" />
            <span>{{ $t('home.comment') }}</span>
        </button>
        <div class="flex-1 relative">
            <button
                class="w-full flex items-center justify-center gap-2 h-9 rounded hover:bg-theme-hover transition-colors cursor-pointer text-theme-text-secondary font-semibold text-[15px]"
                @click="toggleShareMenu"
            >
                <ShareIcon :size="20" class="text-gray-500 dark:text-gray-400" />
                <span>{{ $t('actions.share') }}</span>
            </button>

             <Transition
                enter-active-class="transition ease-out duration-100"
                enter-from-class="transform opacity-0 scale-95"
                enter-to-class="transform opacity-100 scale-100"
                leave-active-class="transition ease-in duration-75"
                leave-from-class="transform opacity-100 scale-100"
                leave-to-class="transform opacity-0 scale-95"
              >
                <div
                  v-if="isShareMenuOpen"
                  class="absolute bottom-full right-0 mb-2 w-[300px] bg-theme-bg-secondary rounded-lg shadow-xl border border-theme-border z-50 overflow-hidden"
                >
                  <div class="p-2">
                    <button
                      @click="shareAsMyPost"
                      class="w-full px-3 py-2 flex items-center gap-3 hover:bg-theme-hover rounded-lg transition-colors text-left"
                    >
                      <div class="w-9 h-9 bg-gray-200 dark:bg-gray-700 rounded-full flex items-center justify-center">
                        <Pencil :size="20" :fillColor="isDark ? '#E4E6EB' : '#050505'" />
                      </div>
                      <div class="flex-1">
                        <p class="text-[15px] font-medium text-theme-text">Udostępnij teraz</p>
                        <p class="text-xs text-theme-text-secondary">Udostępnij w aktualnościach</p>
                      </div>
                    </button>
                    <button
                      @click="shareToStory"
                      class="w-full px-3 py-2 flex items-center gap-3 hover:bg-theme-hover rounded-lg transition-colors text-left mt-1"
                    >
                      <div class="w-9 h-9 bg-gray-200 dark:bg-gray-700 rounded-full flex items-center justify-center">
                        <BookOpenPageVariant :size="20" :fillColor="isDark ? '#E4E6EB' : '#050505'" />
                      </div>
                      <div class="flex-1">
                        <p class="text-[15px] font-medium text-theme-text">Udostępnij w relacji</p>
                      </div>
                    </button>
                  </div>
                </div>
              </Transition>
        </div>
    </div>

    <div v-if="isShareMenuOpen" class="fixed inset-0 z-0" @click="closeShareMenu"></div>

    <Modal v-if="isModalOpen" @close="toggleModal"><PostModal/></Modal>
    <ShareAsPostModal :is-open="isShareAsPostModalOpen" :post="postData" @close="isShareAsPostModalOpen = false" @share="handleShareAsPost"/>
  </div>
</template>
