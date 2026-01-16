<script setup lang="ts">
import { ref, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'

import 'floating-vue/dist/style.css'

import BaseModal from '../BaseModal.vue'
import PostModal from '../PostModal.vue'
import ShareAsPostModal from '../ShareAsPostModal.vue'
import PostHeader from './PostHeader.vue'
import PostActions from './PostActions.vue'
import PostContent from './PostContent.vue'
import PostReactions from './PostReactions.vue'
import PostSharedContent from './PostSharedContent.vue'
import PostMarketplaceCard from './PostMarketplaceCard.vue'
import PostMediaDisplay from './PostMediaDisplay.vue'
import { useStoryShareStore } from '@/stores/storyShare'
import { usePostsStore } from '@/stores/posts'

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

const isModalOpen = ref(false)
const isShareAsPostModalOpen = ref(false)

const toggleModal = () => {
    isModalOpen.value = !isModalOpen.value
}

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

const postToShare = computed(() => {
  return originalPost.value || props.post;
});
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
    <PostContent :post="post" />

    <!-- Marketplace data section -->
    <PostMarketplaceCard
      v-if="(post as any).marketplaceData"
      :marketplace-data="(post as any).marketplaceData"
      @open-messenger="openMessenger"
    />

    <!-- Media display (video/images) -->
    <PostMediaDisplay
      :post="post"
      @image-click="goToMarketplaceItem"
    />
    </template>

    <template v-else>
    <!-- Media display for shared posts - PIERWSZE -->
    <PostMediaDisplay
      :post="post"
      @image-click="goToMarketplaceItem"
    />

    <!-- PostHeader - PO mediach -->
    <PostHeader
      :post="post"
      :is-shared="isShared"
      @edit-post="handleEditPost"
      @hide-post="handleHidePost"
    />

    <!-- Post content - PO nagłówku -->
    <PostContent :post="post" />

    <!-- Marketplace data section dla udostępnionych postów -->
    <PostMarketplaceCard
      v-if="(post as any).marketplaceData"
      :marketplace-data="(post as any).marketplaceData"
      @open-messenger="openMessenger"
    />
    </template>

    <!-- Shared content (posts, reels, events) -->
    <PostSharedContent :post="post" />

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
