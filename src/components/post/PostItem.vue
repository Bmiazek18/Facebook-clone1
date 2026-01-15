<script setup lang="ts">
import { ref, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'

import 'floating-vue/dist/style.css'

import ChevronDown from 'vue-material-design-icons/ChevronDown.vue'

import StarOutline from 'vue-material-design-icons/StarOutline.vue'
import Earth from 'vue-material-design-icons/Earth.vue'

import VolumeHigh from 'vue-material-design-icons/VolumeHigh.vue'
import VolumeMute from 'vue-material-design-icons/VolumeMute.vue'
import PostImageGallery from '../PostImageGallery.vue'
import PlayerVideo from '../PlayerVideo.vue'
import Star from 'vue-material-design-icons/Star.vue'

import BaseModal from '../BaseModal.vue'
import PostModal from '../PostModal.vue'
import ShareAsPostModal from '../ShareAsPostModal.vue'
import PostHeader from './PostHeader.vue'
import PostActions from './PostActions.vue'
import PostTranslation from './PostContent.vue'
import PostReactions from './PostReactions.vue'
import PostMarketplaceCard from './PostMarketplaceCard.vue'
import { useStoryShareStore } from '@/stores/storyShare'
import { usePostsStore } from '@/stores/posts'
import { useEventsStore } from '@/stores/events'
import { useReelsStore } from '@/stores/reels'

import { useVideoAutoplay } from '@/composables/useVideoAutoplay'

import { getUserById } from '@/data/users'
import { processContent } from '@/utils/contentProcessor'
import type { Post } from '@/types/Post';
import ShareAsMessageModal from '../ShareAsMessageModal.vue'


const props = defineProps<{
  post: Post
  isShared?: boolean
}>()

 defineEmits<{
  (e: 'delete', postId: string): void
}>()

const router = useRouter()
const storyShareStore = useStoryShareStore()
const postsStore = usePostsStore()
const eventsStore = useEventsStore()
const reelsStore = useReelsStore()

const isModalOpen = ref(false)
const isShareAsPostModalOpen = ref(false)

// Reel controls
const isReelMuted = ref(true)

const reelVideoRef = ref<HTMLVideoElement | null>(null)

const toggleModal = () => {
    isModalOpen.value = !isModalOpen.value
}

const isInterested = ref(false)

const toggleInterest = () => {
  isInterested.value = !isInterested.value

}

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
    id: String(props.post?.id || Date.now()),
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
    sharedFromId: props.post?.sharedFromId,
    sharedEventId: props.post?.sharedEventId,
    createdEvent: props.post?.createdEvent
  }
})

const { t } = useI18n()

const processedContent = computed(() => {
  return processContent(props.post.content);
});

const shareToStory = () => {
  storyShareStore.setPostToShare(postData.value)
  router.push('/stories/create')
}

const shareAsMyPost = () => {
  isShareAsPostModalOpen.value = true
}

const handleShareAsPost = (comment: string) => {

  postsStore.sharePost(postToShare.value, comment)
  isShareAsPostModalOpen.value = false
  router.push('/profile')
}


const isShareAsMessageModalOpen = ref(false);
const shareToMessage = () => {
  isShareAsMessageModalOpen.value = true;
};


const handleEditPost = (postId: number) => {
  console.log('Edit Post:', postId);

};

const handleHidePost = (postId: number) => {
  console.log('Hide Post:', postId);

};

const goToMarketplaceItem = (itemId: string) => {
  router.push(`/marketplace/item/${itemId}`);
};

const openMessenger = (itemId: string) => {
  console.log('Open messenger for item:', itemId);
};

const originalPost = computed(() => {
  if (props.post.sharedFromId) {
    return postsStore.getPostById(props.post.sharedFromId);
  }
  return undefined;
});

const sharedEvent = computed(() => {
  if (props.post.sharedEventId) {
    return eventsStore.getEventById(props.post.sharedEventId);
  }
  return undefined;
});

const sharedReel = computed(() => {
  if (props.post.sharedReelId) {
    return reelsStore.getReelById(props.post.sharedReelId);
  }
  return undefined;
});

// Reel control functions
const toggleReelMute = () => {
  isReelMuted.value = !isReelMuted.value;
  if (reelVideoRef.value) {
    reelVideoRef.value.muted = isReelMuted.value;
  }
};

const postToShare = computed(() => {
  return originalPost.value || props.post;
});

const videoContainerRef = ref<HTMLElement | null>(null)
const videoRef = ref<InstanceType<typeof PlayerVideo> | null>(null)
useVideoAutoplay(videoContainerRef)
</script>

<template>
  <div class="w-full bg-theme-bg-secondary rounded-lg"
       :class="{ 'border border-theme-border': isShared, 'my-4': !isShared , 'shadow-sm dark:shadow-lg': !props.post}">

    <template v-if="!isShared">
    <PostHeader
      :post="post"
      :is-shared="isShared"
      @edit-post="handleEditPost"
      @hide-post="handleHidePost"
    />

    <!-- Post content and translation -->
    <PostTranslation v-if="!isShared" :post="post" />

    <!-- Marketplace data section -->
    <PostMarketplaceCard
      v-if="(post as any).marketplaceData"
      :marketplace-data="(post as any).marketplaceData"
      @open-messenger="openMessenger"
    />
    <div v-if="!post.sharedFromId">

      <div v-if="post.videoUrl" ref="videoContainerRef" class="w-full">
        <PlayerVideo :settings="true" :lightbox="true" ref="videoRef" :url="post.videoUrl" />
      </div>
      <PostImageGallery v-else-if="post.images && post.images.length > 0" :images="post.images" :post-id="Number(post.id) ?? 0"  @click="goToMarketplaceItem((post as any).marketplaceData.itemId)"/>
    </div>
    </template>
    <template v-else>
        <div v-if="!post.sharedFromId">
            <!-- Video -->
            <div v-if="post.videoUrl" ref="videoContainerRef" class="w-full">
                <PlayerVideo :settings="true" :lightbox="true" ref="videoRef" :url="post.videoUrl" />
            </div>

            <!-- Images -->
            <PostImageGallery v-else-if="post.images && post.images.length > 0" :images="post.images" :post-id="Number(post.id) ?? 0" />
        </div>
    <PostHeader
      :post="post"
      :is-shared="isShared"
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
    </template>

    <div v-if="post.sharedFromId && originalPost" class="mx-3 mb-3 mt-2 rounded-lg overflow-hidden">
        <PostItem :post="originalPost" :is-shared="true" />
    </div>
<div v-if="post.sharedReelId && sharedReel"
         class=" mb-3 mt-2 relative w-auto h-[800px]  bg-gradient-to-b from-[#5c6b55] to-[#2e3b2b] rounded-xl overflow-hidden shadow-lg border border-gray-700/30 group cursor-pointer"
         @click="router.push(`/reel/${sharedReel.id}`)">

        <div class="absolute inset-0 w-full h-full">
             <video
              ref="reelVideoRef"
              class="w-[70%] mx-auto h-full object-cover block"
              :src="sharedReel.videoSrc"
              :poster="sharedReel.poster"
              autoplay
              loop
              playsinline
              :muted="isReelMuted"
            ></video>
            <div class="absolute inset-0 bg-gradient-to-b from-black/40 via-transparent to-black/60 pointer-events-none"></div>
        </div>

        <div class="absolute top-4 left-4 flex items-center gap-3 z-20 pointer-events-none">
          <div class="w-10 h-10 rounded-full border-2 border-white/20 overflow-hidden bg-gray-300">
             <img :src="sharedReel.user.avatar" alt="Avatar" class="w-full h-full object-cover" />
          </div>

          <div class="flex flex-col text-white drop-shadow-md">
            <span class="font-bold text-sm leading-tight flex items-center gap-1">
              {{ sharedReel.user.name }} <span class="text-gray-300 font-normal text-xs opacity-90">• Obserwuj</span>
            </span>
            <div class="flex items-center gap-1 text-xs text-gray-200 opacity-80 mt-0.5">

              <earth :size="12" class="text-gray-200"/>
            </div>
          </div>
        </div>

        <!-- Control buttons -->
        <div class="absolute top-4 right-4 z-20 flex gap-2">
          <button
            @click.stop="toggleReelMute"
            class="p-2 bg-black/20 backdrop-blur-sm rounded-full text-white hover:bg-black/40 transition-colors"
          >
            <VolumeHigh v-if="!isReelMuted" :size="20" />
            <VolumeMute v-else :size="20" />
          </button>


        </div>


<div class="absolute bottom-6 left-4 right-16 z-20">
          <div class="flex items-center bg-black/30 backdrop-blur-md self-start px-3 py-2 rounded-full max-w-[220px] text-white border border-white/10">

            <div class="shrink-0 mr-2 flex items-center justify-center">
               <music-note :size="16" class="animate-pulse-slow" />
            </div>

            <div class="overflow-hidden w-full relative h-[16px] flex items-center">
               <div class="w-full overflow-hidden mask-[linear-gradient(90deg,transparent_0%,white_10%,white_90%,transparent_100%)]">

                  <div class="animate-marquee whitespace-nowrap text-xs font-medium tracking-wide">
                     {{ sharedReel.user.name }} • Oryginalny dźwięk &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; {{ sharedReel.user.name }} • Oryginalny dźwięk &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                  </div>

               </div>
            </div>

          </div>
        </div>

    </div>
<div v-if="post.sharedEventId && sharedEvent"
         class=" mb-4 mt-2  overflow-hidden cursor-pointer  bg-[#f0f2f5] border-b border-[#dadde1] transition-colors group"
         @click="router.push(`/event/${sharedEvent.id}`)">

      <div class="relative w-full aspect-[1.91/1] bg-gray-100 dark:bg-[#3A3B3C]">
        <img
          v-if="sharedEvent.images && sharedEvent.images[0]"
          :src="sharedEvent.images[0]"
          alt="Event cover"
          class="w-full h-full object-cover"
        />
      </div>

      <div class="p-3 flex justify-between items-center">
        <div class="flex-1 min-w-0 pr-3">
          <div class="text-[#F02849] text-[13px] font-semibold uppercase mb-0.5 tracking-wide leading-none">
            {{ sharedEvent.date || 'SOB, 16 MAJ O 15:00' }}
          </div>
          <h3 class="text-[#050505] dark:text-[#E4E6EB] font-bold text-[17px] leading-snug truncate mb-0.5">
            {{ sharedEvent.title || sharedEvent.name }}
          </h3>
          <div class="text-[#65676B] dark:text-[#B0B3B8] text-[13px] truncate">
             {{ sharedEvent.locationName || sharedEvent.location }}
          </div>
          </div>

       <button
          @click.stop="toggleInterest"
          class="shrink-0 h-9 px-3 rounded-[6px] text-[15px] font-semibold flex items-center justify-center gap-1.5 transition-colors z-10 border border-transparent"
          :class="[
            isInterested
              ? 'bg-[#E7F3FF] text-[#1877F2] hover:bg-[#DBEBFF]'
              : 'bg-[#E4E6EB] dark:bg-[#3A3B3C] text-[#050505] dark:text-[#E4E6EB] hover:bg-[#D8DADF] dark:hover:bg-[#4E4F50]'
          ]"
        >
          <component
            :is="isInterested ? Star : StarOutline"
            :size="20"
          />

          <span class="hidden sm:inline">
            {{ isInterested ? 'Interesuję się' : 'Zainteresowany(a)' }}
          </span>

          <ChevronDown
            v-if="isInterested"
            :size="20"
          />
        </button>
      </div>
    </div>

    <PostReactions v-if="!isShared"
      :post-id="post.id"
      :comments-count="post.commentsCount"
      :shares-count="post.sharesCount"
    />

    <PostActions v-if="!isShared"
      @comment="toggleModal"
      @share-as-post="shareAsMyPost"
      @share-to-story="shareToStory"
      @share-to-message="shareToMessage"
    />

    <BaseModal v-if="isModalOpen" @close="toggleModal" :title="`Post ${post.authorName}`">
      <PostModal v-if="props.post" :post="props.post" />
    </BaseModal>
    <BaseModal :title="t('post.sendTo')" v-if="isShareAsMessageModalOpen" @close="isShareAsMessageModalOpen = false">
      <ShareAsMessageModal  />
    </BaseModal>

    <ShareAsPostModal :is-open="isShareAsPostModalOpen" :post="postToShare" @close="isShareAsPostModalOpen = false" @share="handleShareAsPost"  />
  </div>
</template>
<style scoped>
  .animate-marquee {
  display: inline-block;
  white-space: nowrap;
  animation: scroll-left 12s linear infinite;
  padding-left: 100%;
}

@keyframes scroll-left {
  0% {
    transform: translateX(0);
  }
  100% {
    transform: translateX(-100%);
  }
}
</style>
