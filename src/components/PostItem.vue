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
import { usePostsStore } from '@/stores/posts'

import { getUserById } from '@/data/users'

useI18n()

import type { Post } from '@/types/Post';
import ShareAsMessageModal from './ShareAsMessageModal.vue'

// ... other imports

const props = defineProps<{
  post: Post
  isShared?: boolean
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

const postData = computed<Post>(() => {
  return {
    id: String(props.post?.id || Date.now()), // Use current timestamp as ID if not available
    authorName: props.post?.authorName,
    authorAvatar: props.post?.authorAvatar,
    content: props.post?.content,
    imageUrl: props.post?.imageUrl,
    images: props.post?.images,
    videoUrl: props.post?.videoUrl,
    authorId: props.post?.authorId ?? 0,
    date: props.post?.date ?? '',
    likesCount: props.post?.likesCount ?? 0,
    commentsCount: props.post?.commentsCount ?? 0,
    sharesCount: props.post?.sharesCount ?? 0,
    timestamp: props.post?.timestamp ?? Date.now(),
    taggedUsers: props.post?.taggedUsers ?? [],
    location: props.post?.location,
    gif: props.post?.gif,
    isLiked: props.post?.isLiked ?? false,
    likedType: props.post?.likedType ?? null,
    reactionCount: props.post?.reactionCount ?? 0,
    commentCount: props.post?.commentCount ?? 0,
    comments: props.post?.comments ?? [],
    selectedCardBgId: props.post?.selectedCardBgId ?? 0,
    privacy: props.post?.privacy ?? '',
    feeling: props.post?.feeling,
    activity: props.post?.activity,
  }
})

const { t } = useI18n()

const displayFeeling = computed(() => {
  if (props.post.feeling) {
    return ` ${t('post.feelingWith')} ${props.post.feeling.label} ${props.post.feeling.emoji}`;
  }
  return '';
});

const displayActivity = computed(() => {
  if (props.post.activity) {
    return ` — ${props.post.activity.parent} ${props.post.activity.item.label}`;
  }
  return '';
});

const processedContent = computed(() => {
  const content = props.post.content;
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
  console.log('Sharing as post with comment:' + comment);
  postsStore.sharePost(postToShare.value, comment)
  isShareAsPostModalOpen.value = false
  router.push('/profile')
}

const handleDelete = () => {
  emit('delete', props.post.id)
}
const isShareAsMessageModalOpen = ref(false);
const shareToMessage = () => {
  isShareAsMessageModalOpen.value = true;
};
// Handlers for PostSettingPopper actions
const handleDeletePost = (postId: string) => {
    postsStore.removePost(postId);
}

const handleEditPost = (postId: number) => {
  console.log('Edit Post:', postId);
  // Implement actual edit logic here (e.g., open an edit modal)
};

const handleHidePost = (postId: number) => {
  console.log('Hide Post:', postId);
  // Implement actual hide logic here
};

const originalPost = computed(() => {
  if (props.post.sharedFromId) {
    return postsStore.getPostById(props.post.sharedFromId);
  }
  return undefined;
});

const postToShare = computed(() => {
  return originalPost.value || props.post;
});

// Reactions - using composable
const { userReaction, likesCount, handleReaction } = usePostReactions(24)


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
  <div class="w-full bg-theme-bg-secondary rounded-lg"
       :class="isShared ? 'border border-theme-border' : 'my-4 shadow-sm dark:shadow-lg'">

    <PostHeader
      :author-name="post.authorName"
      :author-avatar="post.authorAvatar"
      :author-id="post.authorId"
      :tagged-users="post.taggedUsers"
      :date="post.date"
      :privacy="post.privacy"
      :post-id="post.id"
      :feeling="post.feeling"
      @delete-post="handleDeletePost"
      @edit-post="handleEditPost"
      @hide-post="handleHidePost"
    />

    <div v-if="post.content" class="px-4 py-1 pb-3 text-[15px] leading-normal whitespace-pre-line"
         :class="{
       [((currentBackground as CardBackground).class ?? '')]: (props.post?.selectedCardBgId ?? 0) !== 0, // Apply background if ID is set
       [((currentBackground as CardBackground).textClass ?? 'text-theme-text')]: (props.post?.selectedCardBgId ?? 0) !== 0, // Apply text color if ID is set
       'p-4 h-[383px] flex items-center justify-center text-center': (props.post?.selectedCardBgId ?? 0) !== 0, // Enforce height and centering if ID is set
       'text-xl': (props.post?.selectedCardBgId ?? 0) !== 0 && props.post?.content.length <= 80,
       'text-base': (props.post?.selectedCardBgId ?? 0) !== 0 && props.post?.content.length > 80,
       'text-theme-text': (props.post?.selectedCardBgId ?? 0) === 0, // Default text color if no card background
         }"
    >
      <template v-for="(part, index) in processedContent" :key="index">
        <router-link
          v-if="part.type === 'hashtag'"
          :to="{ name: 'hashtag', params: { hashtag: part.hashtag } }"
          class="text-blue-500 hover:underline"
          :class="{ 'text-white ': (post.selectedCardBgId ?? 0) > 0 }"
        >
          {{ part.value }}
        </router-link>
        <router-link
          v-else-if="part.type === 'mention'"
          :to="{ name: 'userProfile', params: { userId: part.userId } }"
          class="text-blue-500 hover:underline"
          :class="{ 'text-white': (post.selectedCardBgId ?? 0) > 0 }"
        >
          @{{ getUserById(parseInt(part.userId || ''))?.name }}
        </router-link>
        <span v-else :class="{ ' text-[30px]': (post.selectedCardBgId ?? 0) > 0 }">{{ part.value }}</span>
      </template>
    </div>

    <div v-if="!post.sharedFromId">
      <!-- Video -->
      <div v-if="post.videoUrl" ref="videoContainerRef" class="w-full">
        <PlayerVideo :settings="true" :lightbox="true" ref="videoRef" :url="post.videoUrl" />
      </div>

      <!-- Images -->
      <PostImageGallery v-else-if="post.images && post.images.length > 0" :images="post.images" :post-id="post.id ?? ''" />
    </div>

    <div v-if="post.sharedFromId && originalPost" class="mx-3 mb-3 mt-2 rounded-lg overflow-hidden">
        <PostItem :post="originalPost" :is-shared="true" />
    </div>

    <div v-if="!isShared" class="mx-3 mb-3 mt-2 rounded-lg overflow-hidden">
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
              class="relative z-0 rounded-full w-5 h-5 flex items-center justify-center ring-2 ring-white dark:ring-[#242526]"
              :class="[
                getReactionConfig(userReaction).wrapperClass,
                likesCount > 1 ? '-ml-1' : '' // Przesunięcie w lewo
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
              <span v-if="likesCount === 1">{{ t('post.likedByYou') }}</span>
              <span v-else-if="likesCount === 2">{{ t('post.likedByYouAnd', { name: 'Anna Kowalska' }) }}</span>
              <span v-else>{{ t('post.likedByYouAndOthers', { name: 'Anna Kowalska', count: likesCount - 2 }) }}</span>
            </template>

            <template v-else>
              <span>{{ t('post.likedBy', { name: 'Anna Kowalska' }) }}</span>
              <span v-if="likesCount > 1"> {{ t('post.andOthers', { count: likesCount - 1 }) }}</span>
            </template>
          </span>
        </div>

        <div v-else></div>

        <div class="flex items-center gap-3 text-theme-text-secondary">
          <span class="hover:underline cursor-pointer">{{ t('post.commentsCount', { count: post.commentsCount }) }}</span>
          <span class="hover:underline cursor-pointer">{{ t('post.sharesCount', { count: post.sharesCount }) }}</span>
        </div>
      </div>
    </div>

    <PostActions v-if="!isShared"
      @react="handleReaction"
      @comment="toggleModal"
      @share-as-post="shareAsMyPost"
      @share-to-story="shareToStory"
      @share-to-message="shareToMessage"
    />

    <BaseModal v-if="isModalOpen" @close="toggleModal">
      <PostModal v-if="props.post" :post="props.post" />
    </BaseModal>
    <BaseModal :title="t('post.sendTo')" v-if="isShareAsMessageModalOpen" @close="isShareAsMessageModalOpen = false">
      <ShareAsMessageModal  />
    </BaseModal>

    <ShareAsPostModal :is-open="isShareAsPostModalOpen" :post="postToShare" @close="isShareAsPostModalOpen = false" @share="handleShareAsPost"/>
  </div>
</template>
