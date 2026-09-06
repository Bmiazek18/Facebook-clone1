<script setup lang="ts">
import { ref, computed, inject, toRef, type Ref, onMounted, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'

import 'floating-vue/dist/style.css'

import BaseModal from '@/components/common/BaseModal.vue'
import PostModal from './PostModal.vue'
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
import { useComments } from '@/composables/feed/useComments'
import { usePostReactions } from '@/composables/feed/usePostReactions'
import { useAuthStore } from '@/stores/auth'
import { useGroupsStore } from '@/stores/groups'
import { useImpressionTracker } from '@/composables/analytics/useImpressionTracker'
import PostPoll from '@/components/common/PostPoll.vue'
import Briefcase from 'vue-material-design-icons/Briefcase.vue'
import School from 'vue-material-design-icons/School.vue'
import Heart from 'vue-material-design-icons/Heart.vue'
import Home from 'vue-material-design-icons/Home.vue'
import Airplane from 'vue-material-design-icons/Airplane.vue'
import Flag from 'vue-material-design-icons/Flag.vue'

const getLifeEventCategory = (category?: string) => {
  switch (category) {
    case 'work':
      return { icon: Briefcase, gradient: 'from-[#1877F2] to-[#0A4EA3]' }
    case 'education':
      return { icon: School, gradient: 'from-[#00A400] to-[#006000]' }
    case 'relationship':
      return { icon: Heart, gradient: 'from-[#F02849] to-[#AD1029]' }
    case 'home':
      return { icon: Home, gradient: 'from-[#F7B928] to-[#C9910D]' }
    case 'travel':
      return { icon: Airplane, gradient: 'from-[#2ABBA7] to-[#167D6F]' }
    default:
      return { icon: Flag, gradient: 'from-[#8E24AA] to-[#5C1371]' }
  }
}

import type { Post } from '@/types/Post'
import ShareAsMessageModal from '@/components/feed/ShareAsMessageModal.vue'
import ReactionPanel from '../ReactionPanel.vue'
import CommentItem from '../comment/CommentItem.vue'
import CommentReplyInput from '../comment/CommentReplyInput.vue'

const config = useRuntimeConfig()

const getMediaUrl = (src: string) => {
  if (!src) return ''
  if (src.startsWith('http://localhost/files/') || src.startsWith('http://localhost/videos/') || src.startsWith('http://localhost/media/')) {
    src = src.replace('http://localhost/', config.public.apiUrl + '/')
  }
  if (
    src.startsWith('http://') ||
    src.startsWith('https://') ||
    src.startsWith('blob:') ||
    src.startsWith('data:')
  ) {
    return src
  }
  const baseUrl = config.public.apiUrl
  if (src.startsWith('/')) {
    return `${baseUrl}${src}`
  }
  return `${baseUrl}/${src}`
}
const getPhotoId = (mediaItem: any, index: number): string => {
  if (!mediaItem || !mediaItem.src) return String(index)
  const src = mediaItem.src
  if (src.includes('/files/') || src.includes('/media/')) {
    const marker = src.includes('/media/') ? '/media/' : '/files/'
    const parts = src.split(marker)
    const filename = parts[parts.length - 1]
    const qIdx = filename.indexOf('?')
    if (qIdx !== -1) return filename.substring(0, qIdx)
    return filename
  }
  const segments = src.split('/')
  return segments[segments.length - 1] || src
}
const props = withDefaults(
  defineProps<{
    post?: Post
    isShared?: boolean
    isGroup?: boolean
    hideCloseButton?: boolean
    isInModal?: boolean
    shouldPostActionVisible?: boolean
  }>(),
  {
    post: () => ({} as any),
    shouldPostActionVisible: true,
  }
)

const groupsStore = useGroupsStore()
const group = computed(() =>
  props.post?.groupId ? groupsStore.getGroupById(props.post.groupId) : undefined,
)

defineEmits<{
  (e: 'delete', postId: string): void
}>()

const router = useRouter()
const storyShareStore = useStoryShareStore()
const { fetchCommentsForPost } = useComments()
const allPosts = inject<Ref<Post[]>>('allPosts', ref([]))
const authStore = useAuthStore()

const { userReaction, likesCount, topReactions } = usePostReactions(toRef(props, 'post'))

const isModalOpen = ref(false)
const isShareAsPostModalOpen = ref(false)
const isReactionModalOpen = ref(false)

const toggleModal = async () => {
  if (props.isInModal) return
  isModalOpen.value = !isModalOpen.value

  if (isModalOpen.value && props.post?.id) {
    await fetchCommentsForPost(props.post, 5)
  }
}

const toggleReactionModal = () => {
  console.log('Toggling reaction modal', isReactionModalOpen.value)
  isReactionModalOpen.value = !isReactionModalOpen.value
}

// Helper to count reactions from the map

const postData = computed<Post>(() => {
  return {
    id: String(props.post?.id || Date.now()),
    authorId: props.post?.authorId ?? 0,
    author: props.post?.author,
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
    detectedLanguage: props.post?.detectedLanguage,
  }
})

const { t } = useI18n()

const shareToStory = () => {
  // Convert to PostData for story share
  const author = postData.value.author

  const storyPostData = {
    id: postData.value.id,
    author: {
      name: [author?.firstName, author?.lastName].filter(Boolean).join(' ') || 'Unknown',
      avatar: author?.avatar || '/default-avatar.png',
      id: postData.value.authorId,
    },
    content: postData.value.content,
    media: postData.value.media, // Simplified
    timestamp: postData.value.timestamp,
  }

  storyShareStore.setPostToShare(storyPostData)
  router.push('/stories/create')
}

const shareAsMyPost = () => {
  isShareAsPostModalOpen.value = true
}



const isShareAsMessageModalOpen = ref(false)
const shareToMessage = () => {
  isShareAsMessageModalOpen.value = true
}

const handleEditPost = (postId: number) => {
  console.log('Edit Post:', postId)
}

const handleHidePost = (postId: number) => {
  console.log('Hide Post:', postId)
}

const goToMarketplaceItem = (itemId: string) => {
  router.push(`/marketplace/item/${itemId}`)
}

const openMessenger = (itemId: string) => {
  console.log('Open messenger for item:', itemId)
}

const originalPost = computed(() => {
  if (props.post?.sharedPost) {
    return props.post.sharedPost
  }
  if (props.post?.sharedContent?.type === 'post' && props.post.sharedContent.originalId) {
    return allPosts.value.find((p) => String(p.id) === String(props.post?.sharedContent?.originalId))
  }
  if (props.post?.targetType === 'post' && props.post.targetId) {
    return allPosts.value.find((p) => String(p.id) === String(props.post?.targetId))
  }
  return undefined
})

const postToShare = computed(() => {
  return originalPost.value || props.post
})

const activePoll = computed(() => props.post?.poll || props.post?.context?.poll)

const totalPollVotes = computed(() => {
  if (activePoll.value && activePoll.value.options) {
    return activePoll.value.options.reduce((sum: number, option: any) => sum + (option.votes?.length || 0), 0)
  }
  return 0
})

const fetchedLinkPreview = ref<any>(null)
const isLoadingPreview = ref(false)

const detectAndFetchOgLink = async () => {
  if (props.post?.linkPreview) return

  const content = props.post?.content
  if (!content) return

  const urlMatch = content.match(/(https?:\/\/[^\s]+)/g)
  if (urlMatch && urlMatch.length > 0) {
    const url = urlMatch[0]
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
      if (response.ok && !result.errors?.length) {
        const data = result.data.scrapeOg
        fetchedLinkPreview.value = {
          url: url,
          domain: data.domain || new URL(url).hostname,
          title: data.title || 'Link Preview',
          description: data.description || '',
          image: data.image || undefined,
        }
      } else {
        fetchedLinkPreview.value = {
          url: url,
          domain: new URL(url).hostname,
          title: url,
          description: '',
        }
      }
    } catch (error) {
      console.error('Failed to fetch OG link preview for post:', error)
      fetchedLinkPreview.value = {
        url: url,
        domain: new URL(url).hostname,
        title: url,
        description: '',
      }
    } finally {
      isLoadingPreview.value = false
    }
  }
}

watch(
  [() => props.post?.content, () => props.post?.id],
  () => {
    fetchedLinkPreview.value = null
    detectAndFetchOgLink()
  },
  { immediate: true }
)

const postElementRef = ref<HTMLElement | null>(null)
const { observePostElement } = useImpressionTracker()

onMounted(() => {
  if (postElementRef.value && props.post?.id) {
    const authorId = props.post.author?.id || (props.post as any).authorId
    const contentType = props.post.media && props.post.media.length > 0
      ? ((props.post.media[0] as any)?.type === 'video' ? 'video' : 'photo')
      : 'text'
    observePostElement(postElementRef.value, String(props.post.id), authorId, contentType)
  }
})
</script>

<template>
  <div
    ref="postElementRef"
    class="w-full bg-theme-bg-secondary rounded-lg shadow-sm"
    :class="{ 'border border-theme-border': isShared, 's dark:shadow-lg': !props.post }"
  >
    <template v-if="!isShared">
      <PostHeader
        :post="post"
        :is-shared="isShared"
        :is-group="isGroup"
        :is-anonymous="post.isAnonymous"
        :hide-close-button="hideCloseButton"
        @edit-post="handleEditPost"
        @hide-post="handleHidePost"
      />
      <!-- Standard display vs Life Event Display -->
      <template v-if="post.isLifeEvent">
        <div class="px-4 pb-3">
          <div
            class="border border-theme-border rounded-xl overflow-hidden bg-theme-bg flex flex-col"
          >
            <!-- Banner z gradientem -->
            <div
              class="w-full h-44 bg-gradient-to-r flex flex-col items-center justify-center p-6 text-center text-white relative select-none"
              :class="getLifeEventCategory(post.lifeEventCategory).gradient"
            >
              <div
                class="w-14 h-14 rounded-full bg-white/20 backdrop-blur-md border border-white/30 flex items-center justify-center shadow-lg mb-2"
              >
                <component :is="getLifeEventCategory(post.lifeEventCategory).icon" :size="28" />
              </div>
              <h3 class="font-extrabold text-[20px] tracking-tight leading-tight drop-shadow-md">
                {{ (post as any).title || post.content }}
              </h3>
              <p class="text-[12px] text-white/90 font-medium mt-1 drop-shadow-sm" v-if="post.date">
                {{
                  new Date(post.date).toLocaleDateString('pl-PL', {
                    day: 'numeric',
                    month: 'long',
                    year: 'numeric',
                  })
                }}
              </p>
            </div>

            <!-- Zdjęcie (jeśli istnieje) -->
            <NuxtLink
              v-if="post.media && post.media.length > 0"
              :to="`/photo/?fbid=${getPhotoId(post.media[0], 0)}&set=a.${post.id}`"
              class="block w-full border-t border-theme-border aspect-[1.9/1] bg-black overflow-hidden"
            >
              <img
                :src="getMediaUrl(post.media[0].src)"
                class="w-full h-full object-cover cursor-pointer hover:opacity-95 transition-opacity"
              />
            </NuxtLink>

            <!-- Opis (jeśli istnieje oraz tytuł był zdefiniowany oddzielnie) -->
            <div
              class="p-4 bg-theme-bg-secondary flex flex-col border-t border-theme-border"
              v-if="post.content && (post as any).title"
            >
              <p class="text-[14px] text-theme-text leading-relaxed whitespace-pre-line">
                {{ post.content }}
              </p>
            </div>
          </div>
        </div>
      </template>

      <template v-else>
        <!-- Post content and translation -->
        <PostContent :post="post" />
        <PostLinkPreview v-if="post.linkPreview || fetchedLinkPreview" :link-preview="post.linkPreview || fetchedLinkPreview" />
        <PostPoll v-if="activePoll" :poll="activePoll" :post-id="post.id" />

        <MapPreview
          v-if="post.context?.location && (!post.media || post.media.length === 0)"
          :selected-location="post.context?.location"
        />

        <!-- Marketplace data section -->
        <PostMarketplaceCard
          v-if="(post as any).marketplaceData"
          :marketplace-data="(post as any).marketplaceData"
          @open-messenger="openMessenger"
        />

        <!-- Media display (video/images) -->
        <PostMediaDisplay :post="post" @image-click="goToMarketplaceItem" />
      </template>
    </template>

    <template v-else>
      <!-- Media display for shared posts - PIERWSZE -->
      <PostMediaDisplay :post="post" @image-click="goToMarketplaceItem" />

      <!-- PostHeader - PO mediach -->
      <PostHeader
        :post="post"
        :is-shared="isShared"
        @edit-post="handleEditPost"
        @hide-post="handleHidePost"
      />

      <!-- Post content - PO nagłówku -->
      <PostContent :post="post" />
      <PostLinkPreview v-if="post.linkPreview || fetchedLinkPreview" :link-preview="post.linkPreview || fetchedLinkPreview" />

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
      <PostReactions
        v-if="!isShared"
        :post-id="post.id"
        :user-reaction="userReaction"
        :likes-count="likesCount"
        :top-reactions="topReactions"
        :reactions="post.reactions"
        :reaction-user-names="post.reactionUserNames"
        :comments-count="post.commentCount ?? post.stats?.comments ?? 0"
        :shares-count="activePoll ? totalPollVotes : (post.shareCount ?? post.stats?.shares ?? 0)"
        :has-poll="!!activePoll"
        @show-reaction-details="toggleReactionModal"
        @show-comments="toggleModal"
      />

      <PostActions
        v-if="!isShared && props.shouldPostActionVisible"
        :post="post"
        @comment="toggleModal"
        @share-as-post="shareAsMyPost"
        @share-to-story="shareToStory"
        @share-to-message="shareToMessage"
      />

      <!-- New block for group comments -->
      <div v-if="isGroup && post.comments && post.comments.length > 0" class="px-4 pb-2">
        <div @click="toggleModal" class="text-sm text-gray-500 cursor-pointer hover:underline mb-2">{{ $t('feed.zobaczWiecejKomentarzy') }}</div>
        <CommentItem
          v-for="comment in post.comments.slice(0, 2)"
          :key="comment.id"
          :comment="comment"
          :post-autor="post.authorId.toString()"
          :depth="0"
          :post-id="post.id"
        />
        <!-- CommentReplyInput below the comments -->
        <CommentReplyInput :post-id="post.id" class="mt-2" />
      </div>
    </template>

    <BaseModal
      v-if="isModalOpen"
      @close="toggleModal"
      :title="`Post ${[post.author?.firstName, post.author?.lastName].filter(Boolean).join(' ')}`"
    >
      <PostModal v-if="props.post" :post="props.post" />
    </BaseModal>

    <BaseModal  noHeader v-if="isReactionModalOpen"  @close="toggleReactionModal">
      <ReactionPanel :reactions="post.reactions" :reaction-details="post.rawReactions"  @close="toggleReactionModal" />
    </BaseModal>

    <BaseModal
      :title="t('post.sendTo')"
      v-if="isShareAsMessageModalOpen"
      @close="isShareAsMessageModalOpen = false"
    >
      <ShareAsMessageModal :share-url="'/post/' + (props.post?.id || '')" @close="isShareAsMessageModalOpen = false" />
    </BaseModal>

    <ShareAsPostModal
      :is-open="isShareAsPostModalOpen"
      :post="postToShare"
      @close="isShareAsPostModalOpen = false"
    />
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
