<script setup lang="ts">
import { ref, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'

import Heart from 'vue-material-design-icons/Heart.vue'

import Earth from 'vue-material-design-icons/Earth.vue'
import ThumbUp from 'vue-material-design-icons/ThumbUp.vue'
import Close from 'vue-material-design-icons/Close.vue'
import PostImageGallery from './PostImageGallery.vue'
import PlayerVideo from './PlayerVideo.vue'
import { useTheme } from '@/composables/useTheme'
import { usePostReactions } from '@/composables/usePostReactions'
import { useVideoAutoplay } from '@/composables/useVideoAutoplay'
import BaseModal from './BaseModal.vue'
import PostModal from './PostModal.vue'
import ShareAsPostModal from './ShareAsPostModal.vue'
import PostHeader from './post/PostHeader.vue'
import PostActions from './post/PostActions.vue'
import { useStoryShareStore } from '@/stores/storyShare'
import { usePostsStore, type SharedPost } from '@/stores/posts'
import type { PostData } from '@/types/StoryElement'
import { getUserById, type User } from '@/data/users'

useI18n()

import type { Post } from '@/data/posts'

// ... other imports

const props = defineProps<{
  post?: Post
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

interface CardBackground { id: number; class: string; textClass?: string }
const cardBackgrounds: CardBackground[] = [
  { id: 0, class: 'bg-white', textClass: 'text-black' },
  { id: 1, class: 'bg-gradient-to-b from-blue-500 to-blue-700', textClass: 'text-white' },
  { id: 2, class: 'bg-gradient-to-tr from-pink-500 via-red-500 to-yellow-500', textClass: 'text-white' },
  { id: 3, class: 'bg-gradient-to-br from-purple-900 via-indigo-800 to-blue-900', textClass: 'text-white' },
  { id: 4, class: 'bg-red-500', textClass: 'text-white' },
  { id: 5, class: 'bg-gradient-to-r from-green-400 to-teal-500', textClass: 'text-white' },
];

const currentBackground = computed(() => {
  return cardBackgrounds.find(bg => bg.id === props.post?.selectedCardBgId) ?? cardBackgrounds[0]
})

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

const processedContent = computed(() => {
  const content = displayData.value.content;
  if (!content) return [];

  // Regex łapie dwie rzeczy:
  // 1. ([@\d+]) -> Wzmianka o użytkowniku np. [@123]
  // 2. (#[\w\u00C0-\u017F]+) -> Hashtag (w tym polskie znaki)
  const combinedRegex = /(\[@\d+\]|#[\w\u00C0-\u017F]+)/g;

  return content.split(combinedRegex)
    .filter(part => part) // Filtrujemy puste stringi i undefined
    .map((part) => {
      // Przypadek 1: User Mention [@123]
      if (part.startsWith('[@') && part.endsWith(']')) {
        return {
          type: 'mention',
          value: part,
          userId: part.slice(2, -1) // Wyciągamy samo ID jako string lub int
        };
      }

      // Przypadek 2: Hashtag #słowo
      if (part.startsWith('#')) {
        return {
          type: 'hashtag',
          value: part,
          hashtag: part.substring(1),
        };
      }

      // Przypadek 3: Zwykły tekst
      return {
        type: 'text',
        value: part
      };
    });
});

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

// Handlers for PostSettingPopper actions
const handleDeletePost = (postId: number) => {
  if (props.sharedPost) {
    postsStore.removeSharedPost(String(postId)); // Assuming sharedPost.id is string
  } else if (props.post) {
    postsStore.removePost(props.post.id);
  }
};

const handleEditPost = (postId: number) => {
  console.log('Edit Post:', postId);
  // Implement actual edit logic here (e.g., open an edit modal)
};

const handleHidePost = (postId: number) => {
  console.log('Hide Post:', postId);
  // Implement actual hide logic here
};

// Reactions - using composable
const { userReaction, likesCount, getReactionIcon, handleReaction } = usePostReactions(24)


// Konfiguracja wyglądu reakcji
const getReactionConfig = (type: string) => {
  switch (type) {
    // 1. IKONY Z TŁEM (Like, Love)
    case 'like':
      return {
        mode: 'icon',
        component: ThumbUp,
        wrapperClass: 'bg-[#1877F2]', // Niebieskie tło
        color: '#FFFFFF'
      }
    case 'love':
      return {
        mode: 'icon',
        component: Heart,
        wrapperClass: 'bg-[#F3425F]', // Czerwone tło
        color: '#FFFFFF'
      }

    // 2. ZWYKŁE EMOTKI (Haha, Wow, Sad, Angry)
    // Tło jest 'bg-white', aby przykryć ikonę pod spodem, ale dla oka to "sama emotka"
    case 'haha':
      return { mode: 'emoji', char: '😆', wrapperClass: 'bg-white dark:bg-[#242526]' }
    case 'wow':
      return { mode: 'emoji', char: '😮', wrapperClass: 'bg-white dark:bg-[#242526]' }
    case 'sad':
      return { mode: 'emoji', char: '😢', wrapperClass: 'bg-white dark:bg-[#242526]' }
    case 'angry':
      return { mode: 'emoji', char: '😡', wrapperClass: 'bg-white dark:bg-[#242526]' }

    // Domyślny fallback
    default:
      return {
        mode: 'icon',
        component: ThumbUp,
        wrapperClass: 'bg-[#1877F2]',
        color: '#FFFFFF'
      }
  }
}
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
        :author-id="(props.post as any)?.authorId"
        :tagged-users="props.post?.taggedUsers"
        :date="props.post?.date"
        :privacy="props.post?.privacy"
        :post-id="props.post?.id"
        @delete-post="handleDeletePost"
        @edit-post="handleEditPost"
        @hide-post="handleHidePost"
      />


<div class="px-4 py-1 pb-3 text-[15px] leading-normal whitespace-pre-line"
     :class="{
       [currentBackground.class]: props.post?.selectedCardBgId !== undefined, // Apply background if ID is set
       [currentBackground.textClass || 'text-theme-text']: props.post?.selectedCardBgId !== undefined, // Apply text color if ID is set
       'p-4 h-[383px] flex items-center justify-center text-center': props.post?.selectedCardBgId !== undefined, // Enforce height and centering if ID is set
       'text-xl': props.post?.selectedCardBgId !== undefined && displayData.content.length <= 80,
       'text-base': props.post?.selectedCardBgId !== undefined && displayData.content.length > 80,
       'text-theme-text': props.post?.selectedCardBgId === undefined, // Default text color if no card background
     }"
>
  <template v-for="(part, index) in processedContent" :key="index">

    <router-link
      v-if="part.type === 'hashtag'"
      :to="{ name: 'hashtag', params: { hashtag: part.hashtag } }"
      class="text-blue-500 hover:underline"
      :class="{ 'text-white ': props.post?.selectedCardBgId > 0 }"
    >
      {{ part.value }}
    </router-link>

    <router-link
      v-else-if="part.type === 'mention'"
      :to="{ name: 'userProfile', params: { userId: part.userId } }"
      class="text-blue-500 hover:underline"
      :class="{ 'text-white': props.post?.selectedCardBgId > 0 }"
    >
      @{{ getUserById(parseInt(part.userId))?.name }}
    </router-link>

    <span v-else :class="{ ' text-[30px]': props.post?.selectedCardBgId > 0 }">{{ part.value }}</span>

  </template>
</div>
      <!-- Video -->
      <div v-if="displayData.videoUrl" ref="videoContainerRef" class="w-full">
        <PlayerVideo :settings="true" :lightbox="true" ref="videoRef" :url="displayData.videoUrl" />
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

        <div class="flex items-center gap-1.5 cursor-pointer group" v-if="likesCount > 0">

          <div class="flex items-center relative pl-1">

            <div
              v-if="likesCount > 1 || (likesCount === 1 && !userReaction)"
              class="relative z-10 rounded-full w-[20px] h-[20px] flex items-center justify-center bg-[#1877F2]"
            >
               <ThumbUp :size="12" fillColor="#FFFFFF" />
            </div>

            <div
              v-if="userReaction"
              class="relative z-0 rounded-full w-[20px] h-[20px] flex items-center justify-center ring-2 ring-white dark:ring-[#242526]"
              :class="[
                getReactionConfig(userReaction).wrapperClass,
                likesCount > 1 ? '-ml-[4px]' : '' // Przesunięcie w lewo
              ]"
            >
              <component
                v-if="getReactionConfig(userReaction).mode === 'icon'"
                :is="getReactionConfig(userReaction).component"
                :size="10"
                :fillColor="getReactionConfig(userReaction).color"
              />

              <span
                v-else
                class="text-[20px] leading-none  select-none"
              >
                {{ getReactionConfig(userReaction).char }}
              </span>
            </div>
          </div>

          <span class="text-theme-text-secondary group-hover:underline leading-snug ml-0.5">
            <template v-if="userReaction">
              <span v-if="likesCount === 1">Ty</span>
              <span v-else-if="likesCount === 2">Ty i Anna Kowalska</span>
              <span v-else>Ty, Anna Kowalska i {{ likesCount - 2 }} innych</span>
            </template>

            <template v-else>
              <span>Anna Kowalska</span>
              <span v-if="likesCount > 1"> i {{ likesCount - 1 }} innych</span>
            </template>
          </span>
        </div>

        <div v-else></div>

        <div class="flex items-center gap-3 text-theme-text-secondary">
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

    <BaseModal v-if="isModalOpen" @close="toggleModal">
      <PostModal v-if="props.post" :post="props.post" />
    </BaseModal>
    <ShareAsPostModal :is-open="isShareAsPostModalOpen" :post="postData" @close="isShareAsPostModalOpen = false" @share="handleShareAsPost"/>
  </div>
</template>
