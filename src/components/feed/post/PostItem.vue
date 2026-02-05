<script setup lang="ts">
import { ref, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'

import 'floating-vue/dist/style.css'

import BaseModal from '@/components/common/BaseModal.vue'
import PostModal from '@/components/feed/PostModal.vue'
import ShareAsPostModal from '@/components/feed/ShareAsPostModal.vue'
import PostHeader from './PostHeader.vue'
import PostActions from './PostActions.vue'
import PostContent from './PostContent.vue'
import PostLinkPreview from './PostLinkPreview.vue'
import PostReactions from './PostReactions.vue'
import PostSharedContent from './PostSharedContent.vue'
import PostMarketplaceCard from './PostMarketplaceCard.vue'
import PostMediaDisplay from './PostMediaDisplay.vue'
import MapPreview from '@/components/MapPreview.vue'
import { useStoryShareStore } from '@/stores/storyShare'
import { usePostsStore } from '@/stores/posts'
import { usePostReactions } from '@/composables/usePostReactions'
import { useGroupsStore } from '@/stores/groups';

import type { Post } from '@/types/Post';
import ShareAsMessageModal from '@/components/feed/ShareAsMessageModal.vue'
import { getUserById } from '@/data/users';
import ReactionPanel from '../ReactionPanel.vue';

const props = defineProps<{
  post: Post
  isShared?: boolean
  isGroup?:boolean
}>()

const groupsStore = useGroupsStore();
const group = computed(() => props.post.groupId ? groupsStore.getGroupById(props.post.groupId) : undefined);

 defineEmits<{
  (e: 'delete', postId: string): void
}>()

const router = useRouter()
const storyShareStore = useStoryShareStore()
const postsStore = usePostsStore()

const { userReaction, likesCount,topReactions } = usePostReactions(String(props.post.id))

const isModalOpen = ref(false)
const isShareAsPostModalOpen = ref(false)
const isReactionModalOpen = ref(false);

const toggleModal = () => {
    isModalOpen.value = !isModalOpen.value
}

const toggleReactionModal = () => {
    console.log('Toggling reaction modal', isReactionModalOpen.value);
    isReactionModalOpen.value = !isReactionModalOpen.value;
};

// Helper to count reactions from the map


const postData = computed<Post>(() => {
  return {
    id: String(props.post?.id || Date.now()),
    authorId: props.post?.authorId ?? 0,
    stats: {
        comments: props.post?.stats?.comments ?? 0,
        shares: props.post?.stats?.shares ?? 0,
    },
    reactions: props.post?.reactions ?? {},
    content: props.post?.content,
    media: props.post?.media ?? {},
    context: props.post?.context ?? { privacy: 'public' },
    date: props.post?.date ?? '',
    timestamp: props.post?.timestamp ?? Date.now(),
    comments: props.post?.comments ?? [],
    selectedCardBgId: props.post?.selectedCardBgId ?? 0,
    sharedContent: props.post?.sharedContent,
    detectedLanguage: props.post?.detectedLanguage
  }
})

const { t } = useI18n()

const shareToStory = () => {
  // Convert to PostData for story share
  // We need to fetch author details here since PostData still needs them for display
  const author = getUserById(postData.value.authorId);

  const storyPostData = {
      id: postData.value.id,
      author: {
          name: author?.name || 'Unknown',
          avatar: author?.avatar || '',
          id: postData.value.authorId
      },
      content: postData.value.content,
      imageUrl: postData.value.media.images?.[0]?.src, // Simplified
      timestamp: postData.value.timestamp
  };

  storyShareStore.setPostToShare(storyPostData)
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
  if (props.post.sharedContent?.type === 'post' && props.post.sharedContent.originalId) {
    return postsStore.getPostById(props.post.sharedContent.originalId);
  }
  return undefined;
});


const postToShare = computed(() => {
  return originalPost.value || props.post;
});
</script>

<template>
  <div class="w-full bg-theme-bg-secondary rounded-lg"
       :class="{ 'border border-theme-border': isShared,  'shadow-sm dark:shadow-lg': !props.post}">



    <template v-if="!isShared">
          <PostHeader
            :post="post"
            :is-shared="isShared"
            :group="isGroup ? undefined : group"
            :is-anonymous="post.isAnonymous"
            @edit-post="handleEditPost"
            @hide-post="handleHidePost"
          />
    <!-- Post content and translation -->
    <PostContent :post="post" />
    <PostLinkPreview v-if="post.linkPreview" :link-preview="post.linkPreview" />

    <MapPreview v-if="post.context.location && (!post.media || post.media.length === 0)" :selected-location="post.context.location" />

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
    <PostLinkPreview v-if="post.linkPreview" :link-preview="post.linkPreview" />

    <!-- Marketplace data section dla udostępnionych postów -->
    <PostMarketplaceCard
      v-if="(post as any).marketplaceData"
      :marketplace-data="(post as any).marketplaceData"
      @open-messenger="openMessenger"
    />
    </template>

    <!-- Shared content (posts, reels, events) -->
    <template v-if="!isShared">
      <PostSharedContent :post="post" />
    </template>

    <template v-if="post">
      <PostReactions v-if="!isShared"
        :post-id="post.id"
        :user-reaction="userReaction"
        :likes-count="likesCount"
        :top-reactions="topReactions"
        :reactions="post.reactions"
        :comments-count="post.stats.comments"
        :shares-count="post.stats.shares"
        @show-reaction-details="toggleReactionModal"
      />

      <PostActions v-if="!isShared"
        :post-id="post.id"
        @comment="toggleModal"
        @share-as-post="shareAsMyPost"
        @share-to-story="shareToStory"
        @share-to-message="shareToMessage"
      />
    </template>

    <BaseModal v-if="isModalOpen" @close="toggleModal" :title="`Post ${getUserById(post.authorId)?.name}`">
      <PostModal v-if="props.post" :post="props.post" />
    </BaseModal>

    <BaseModal v-if="isReactionModalOpen" @close="toggleReactionModal" title="Reakcje">
      <ReactionPanel :reactions="post.reactions" />
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
