<script setup lang="ts">
import { ref, watch, computed, provide, type ComponentPublicInstance } from 'vue'
import { useRouter, onBeforeRouteLeave, type RouteLocation } from 'vue-router'
import { useWindowVirtualizer } from '@tanstack/vue-virtual'
import { GET_INITIAL_SHELL_DATA, GET_FEED_POSTS } from '@/graphql/home'
import { processActiveStories } from '@/utils/stories'
import { processPostsIntoReels } from '@/utils/reels'

// --- COMPONENTS ---
import CreateBox from '@/components/create/createPost/CreateBox.vue'
import PostItem from '@/components/feed/post/PostItem.vue'
import PostSkeleton from '@/components/feed/post/PostSkeleton.vue'
import PeopleYouMayKnow from '@/components/friends/PeopleYouMayKnow.vue'
import StoriesList from '@/components/feed/stories/list/StoriesList.vue'
import LeftSidebar from '@/components/home/LeftSidebar.vue'
import RightSidebar from '@/components/home/RightSidebar.vue'
import ReelsGallery from '@/components/feed/reel/ReelsGallery.vue'
import ConfirmationModal from '@/components/common/ConfirmationModal.vue'
import { useCreatePostStore } from '~/stores/createPost'
import { useAuthStore } from '@/stores/auth'
import { usePostsStore, useStoriesStore } from '@/composables/feed/useAppState'
import { getApolloClient } from '@/utils/apollo'

const authStore = useAuthStore()
const currentUserId = computed(() => String(authStore.currentUserId || '123'))
const postsPerPage = 5

const postsStore = usePostsStore()
const storiesStore = useStoriesStore()

// 1. Initial Shell Data (Stories, Friends, Birthdays) - Splash Screen Phase
const { data: result, error } = await useAsyncQuery<any>(GET_INITIAL_SHELL_DATA, {
  currentUserId: currentUserId.value
})

// Synchronize initial shell data
watch(
  result,
  (newVal) => {
    if (newVal?.getActiveStories) {
      storiesStore.userStories = processActiveStories(newVal.getActiveStories, String(currentUserId.value))
    }
  },
  { immediate: true }
)

const friends = computed(() => result.value?.getFriends ?? [])
const birthdayUsers = computed(() => result.value?.getBirthdayUsers ?? [])

const allPosts = computed(() => postsStore.posts)
const allStories = computed(() => storiesStore.allUserStories)
const allReels = computed(() => processPostsIntoReels(postsStore.posts, String(currentUserId.value)))

provide('allPosts', allPosts)
provide('allStories', allStories)

// Helper to format GraphQL post data into frontend model
const formatPost = (post: any) => {
  let formattedReactions: Record<string, number[]> = {}
  let reactionUserNames: Record<string, string[]> = {}

  if (Array.isArray(post.reactions)) {
    post.reactions.forEach((r: any) => {
      const type = r.reactionType.toLowerCase()
      formattedReactions[type] = (r.userIds ?? []).map(String)
      if (Array.isArray(r.users)) {
        reactionUserNames[type] = r.users.map((u: any) =>
          [u.firstName, u.lastName].filter(Boolean).join(' ') || 'Użytkownik'
        )
      }
    })
  } else if (post.reactions) {
    formattedReactions = post.reactions
  }

  return {
    ...post,
    reactions: formattedReactions,
    rawReactions: post.reactions,
    reactionUserNames,
    stats: {
      reactions: Object.values(formattedReactions).flat().length,
      comments: post.commentCount ?? 0,
      shares: post.shareCount ?? 0,
    }
  }
}

// 2. Async Feed Loading (Non-blocking posts stream)
const isFeedLoading = ref(postsStore.posts.length === 0)
const feedError = ref<any>(null)
const isFetchingMore = ref(false)
const hasMore = ref(true)

const fetchInitialFeed = async () => {
  if (postsStore.posts.length > 0) {
    isFeedLoading.value = false
    return
  }

  isFeedLoading.value = true
  try {
    const apolloClient = getApolloClient()
    const { data } = await apolloClient.query({
      query: GET_FEED_POSTS,
      variables: {
        currentUserId: currentUserId.value,
        limit: postsPerPage,
        offset: 0
      },
      fetchPolicy: 'network-only'
    })

    const initialPosts = (data?.getFeed ?? []).filter(Boolean)
    postsStore.posts = initialPosts.map(formatPost)
    if (initialPosts.length < postsPerPage) {
      hasMore.value = false
    }
  } catch (err) {
    console.error('Failed to load initial feed:', err)
    feedError.value = err
  } finally {
    isFeedLoading.value = false
  }
}

onMounted(() => {
  fetchInitialFeed()
})

const loadMorePosts = async () => {
  if (isFetchingMore.value || !hasMore.value || isFeedLoading.value) return

  isFetchingMore.value = true

  try {
    const apolloClient = getApolloClient()
    const { data: fetchMoreResult } = await apolloClient.query({
      query: GET_FEED_POSTS,
      variables: {
        currentUserId: currentUserId.value,
        limit: postsPerPage,
        offset: allPosts.value.length
      }
    })

    const newPosts = (fetchMoreResult?.getFeed ?? []).filter(Boolean)
    if (newPosts.length === 0) {
      hasMore.value = false
      return
    }

    const formattedNewPosts = newPosts.map(formatPost)
    postsStore.posts = [...postsStore.posts, ...formattedNewPosts]
  } catch (e) {
    console.error('Błąd paginacji Apollo:', e)
  } finally {
    isFetchingMore.value = false
  }
}

// =========================================
// 3. PRZYGOTOWANIE STRUKTURY LISTY
// =========================================
const PEOPLE_INDEX = 3

type ProcessedItem =
  | { type: 'post'; data: (typeof allPosts.value)[number]; id: string }
  | { type: 'peopleYouMayKnow'; id: string }
  | { type: 'reelsGallery'; id: string }

const processedList = computed(() => {
  const list: ProcessedItem[] = []

  allPosts.value.forEach((post: any, index: number) => {
    if (!post) return
    list.push({ type: 'post', data: post, id: `post-${post.id || index}` })

    if (index === PEOPLE_INDEX) {
      list.push({ type: 'peopleYouMayKnow', id: 'people-you-may-know' })
    }
    if (index === PEOPLE_INDEX + 1) {
      list.push({ type: 'reelsGallery', id: 'reels-gallery' })
    }
  })

  return list
})

// =========================================
// 4. TANSTACK VIRTUAL
// =========================================
const rowVirtualizer = useWindowVirtualizer({
  get count() {
    return processedList.value.length
  },
  estimateSize: () => 650,
  overscan: 15,
  scrollMargin: 400,
  getScrollElement: () => (typeof window !== 'undefined' ? window : null),
})

const totalSize = computed(() => rowVirtualizer.value?.getTotalSize() ?? 0)
const scrollMarginOffset = computed(() => rowVirtualizer.value?.options?.scrollMargin ?? 0)

const virtualEntries = computed(() => {
  if (!rowVirtualizer.value) return []
  return rowVirtualizer.value
    .getVirtualItems()
    .map((virtualRow) => ({
      virtualRow,
      item: processedList.value[virtualRow.index],
    }))
    .filter((entry) => !!entry.item)
})

const measureElement = (el: Element | ComponentPublicInstance | null) => {
  if (!el || !rowVirtualizer.value) return
  const node = '$el' in el ? (el.$el as HTMLElement) : (el as HTMLElement)
  rowVirtualizer.value.measureElement(node)
}

watch(
  () => rowVirtualizer.value?.getVirtualItems(),
  (rows) => {
    if (!rows || !rows.length || isFetchingMore.value) return
    const lastRow = rows[rows.length - 1]

    if (lastRow && lastRow.index >= processedList.value.length - 3) {
      loadMorePosts()
    }
  }
)

// =========================================
// 5. ROUTING & CONFIRMATION
// =========================================
const router = useRouter()
const createPostStore = useCreatePostStore()
const showConfirmModal = ref(false)
const pendingRoute = ref<RouteLocation | null>(null)

onBeforeRouteLeave((to, from, next) => {
  if (createPostStore.hasUnsavedChanges) {
    pendingRoute.value = to
    showConfirmModal.value = true
    next(false)
  } else {
    next()
  }
})

const handleConfirmLeave = () => {
  createPostStore.reset()
  showConfirmModal.value = false
  if (pendingRoute.value) router.push(pendingRoute.value)
}

const handleCancelLeave = () => {
  showConfirmModal.value = false
  pendingRoute.value = null
}
</script>

<template>
  <div class="flex w-full min-h-screen bg-theme-bg text-theme-text relative">
    <div class="flex-1 w-full">
      <div
        class="grid grid-cols-1 lg:grid-cols-[1fr_350px] xl:grid-cols-[360px_680px_350px] min-[1500px]:grid-cols-[1fr_680px_1fr] w-full max-w-[1450px] xl:max-w-none mt-14 mx-auto justify-between min-[1750px]:justify-center lg:px-6 xl:px-4 gap-4"
      >
        <!-- Zmiana tutaj: ukrywanie lewego paska na mniejszych ekranach (widoczny dopiero od rozmiaru xl) -->
        <div class="hidden xl:block">
          <LeftSidebar />
        </div>

        <div id="MiddleSection" class="w-full min-w-0 flex justify-center lg:justify-start xl:justify-center">
          <div id="PostsSection" class="w-full lg:max-w-[700px] xl:max-w-[680px] px-2 sm:px-4 lg:px-0">
            <CreateBox />
            <StoriesList :stories="allStories" />

            <div v-if="error || feedError" class="p-4 bg-red-50 text-red-600 rounded-lg text-center my-4">{{ $t('home.wystapilBladPodczasLadowania') }}</div>

            <!-- Feed Skeleton Loading State -->
            <div v-else-if="isFeedLoading && allPosts.length === 0" class="space-y-4 pt-2">
              <PostSkeleton v-for="n in 3" :key="n" />
            </div>

            <div
              v-else
              :style="{
                height: `${totalSize}px`,
                width: '100%',
                position: 'relative',
              }"
            >
              <div
                v-for="entry in virtualEntries"
                :key="String(entry.virtualRow.key)"
                :data-index="entry.virtualRow.index"
                :ref="measureElement"
                :style="{
                  position: 'absolute',
                  top: 0,
                  left: 0,
                  width: '100%',
                  transform: `translateY(${entry.virtualRow.start - scrollMarginOffset}px)`,
                  contain: 'content',
                  willChange: 'transform',
                }"
              >
                <div v-if="entry.item" class="mb-4">
                  <PostItem v-if="entry.item.type === 'post' && entry.item.data" :post="entry.item.data" />
                  <PeopleYouMayKnow v-else-if="entry.item.type === 'peopleYouMayKnow'" />
                  <ReelsGallery v-else-if="entry.item.type === 'reelsGallery'" :reels="allReels" />
                </div>
              </div>
            </div>

            <div v-if="isFetchingMore" class="pt-2 pb-10 space-y-4">
              <PostSkeleton v-for="n in 1" :key="n" />
            </div>

            <div
              v-if="!hasMore && !isFetchingMore"
              class="text-center py-20 text-theme-text-secondary opacity-50 text-sm"
            >{{ $t('home.nieMaWiecejPostow') }}</div>
          </div>
        </div>

        <div id="RightSection" class="hidden lg:block sticky top-0 overflow-y-auto no-scrollbar">
          <RightSidebar :friends="friends" :birthday-users="birthdayUsers" />
        </div>
      </div>
    </div>
  </div>

  <ConfirmationModal
    v-if="showConfirmModal"
    @confirm="handleConfirmLeave"
    @cancel="handleCancelLeave"
  />
</template>
