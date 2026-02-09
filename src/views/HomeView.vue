<script setup lang="ts">
import { ref, watch, computed } from 'vue'
import { useRouter, onBeforeRouteLeave, type RouteLocation } from 'vue-router'
import { useVirtualList, useInfiniteScroll } from '@vueuse/core'

// Stores
import { usePostsStore } from '@/stores/posts'
import { useCreatePostStore } from '@/stores/createPost'

// Components
import CreateBox from '@/components/create/createPost/CreateBox.vue'
import PostItem from '@/components/feed/post/PostItem.vue'
import PeopleYouMayKnow from '@/components/friends/PeopleYouMayKnow.vue'
import StoriesList from '@/components/feed/stories/list/StoriesList.vue'
import LeftSidebar from '@/components/home/LeftSidebar.vue'
import RightSidebar from '@/components/home/RightSidebar.vue'
import ConfirmationModal from '@/components/common/ConfirmationModal.vue'
import ReelsGallery from '@/components/ReelsGallery.vue'
import PostModal from '@/components/feed/PostModal.vue'
import BaseModal from '@/components/common/BaseModal.vue'

// Data / Utils
import { getPostById } from '@/data/posts'
import { getUserById } from '@/data/users'

// --- LOGIKA DANYCH ---
const postsStore = usePostsStore()
const localPosts = ref([...postsStore.posts])

// Synchronizacja ze storem
watch(
  () => postsStore.posts,
  (newPosts) => {
    localPosts.value = [...newPosts]
  },
  { deep: true }
)

// --- INFINITE SCROLL ---
const postsPerPage = 5
const displayedCount = ref(5)
const isLoading = ref(false)

const loadMorePosts = async () => {
  // Jeśli już ładujemy lub nie ma więcej postów - przerwij
  if (isLoading.value || displayedCount.value >= localPosts.value.length) return

  isLoading.value = true

  // Symulacja opóźnienia API
  await new Promise(resolve => setTimeout(resolve, 1000))

  displayedCount.value += postsPerPage
  isLoading.value = false
}

// --- PRZYGOTOWANIE LISTY (Wstrzykiwanie komponentów) ---
const peopleYouMayKnowIndex = Math.floor(Math.random() * 5) + 2

const processedList = computed(() => {
  const list = []
  // Pracujemy tylko na wycinku danych (Infinite Scroll)
  const currentPosts = localPosts.value.slice(0, displayedCount.value)

  currentPosts.forEach((post, index) => {
    list.push({ type: 'post', data: post, id: `post-${post.id}` })

    if (index === peopleYouMayKnowIndex) {
      list.push({ type: 'peopleYouMayKnow', id: 'people-you-may-know' })
    }
    if (index === peopleYouMayKnowIndex + 1) {
      list.push({ type: 'reelsGallery', id: 'reels-gallery' })
    }
  })

  return list
})

// --- VIRTUAL LIST ---
const { list: virtualItems, containerProps, wrapperProps } = useVirtualList(processedList, {
  itemHeight: 600, // Szacowana wysokość posta
  overscan: 5
})

// Podpinamy infinite scroll pod kontener listy wirtualnej
useInfiniteScroll(
  containerProps.ref,
  loadMorePosts,
  { distance: 400 }
)

// --- ROUTING & MODALS ---
const router = useRouter()
const route = router.currentRoute
const post = computed(() => getPostById(String(route.value.params.id)))

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
  <div class="w-full bg-theme-bg text-theme-text min-h-screen relative flex">

    <div id="LeftSection" class="hidden lg:block shrink-0">
      <LeftSidebar />
      <div class="w-[360px]"></div>
    </div>

    <div
      ref="scrollContainer"
      v-bind="containerProps"
      class="flex-1 h-screen overflow-y-auto"
    >
      <div
        v-bind="wrapperProps"
        class="flex flex-col md:grid md:grid-cols-[5fr_2fr] w-full 3xl:max-w-[1500px] max-w-full mt-14 mx-auto px-0 lg:px-4"
      >
        <div id="MiddleSection" class="flex justify-center w-full min-w-0">
          <div id="PostsSection" class="w-full md:max-w-[700px] lg:mx-0 mx-0">
            <CreateBox />
            <StoriesList />

            <div v-for="item in virtualItems" :key="item.data.id">
              <div class="mb-4">
                <PostItem v-if="item.data.type === 'post'" :post="item.data.data" />
                <PeopleYouMayKnow v-else-if="item.data.type === 'peopleYouMayKnow'" />
                <ReelsGallery v-else-if="item.data.type === 'reelsGallery'" />
              </div>
            </div>

            <div v-if="isLoading" class="py-10 flex justify-center">
              <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-theme-primary"></div>
            </div>

            <div v-if="displayedCount >= localPosts.length" class="text-center py-20 text-theme-text-secondary opacity-50">
              Nie ma więcej postów.
            </div>
          </div>
        </div>

        <div class="hidden md:block pl-4">
          <div class="sticky top-4">
            <RightSidebar />
          </div>
        </div>
      </div>
    </div>

    <ConfirmationModal
      v-if="showConfirmModal"
      @confirm="handleConfirmLeave"
      @cancel="handleCancelLeave"
    />

    <BaseModal
      v-if="post"
      :title="getUserById(post.authorId)?.name"
      @close="router.push('/')"
    >
      <PostModal :post="post" />
    </BaseModal>

    <router-view />
  </div>
</template>

<style scoped>
/* Ukrywamy scrollbar dla czystszego wyglądu, jeśli trzeba */
.custom-scrollbar::-webkit-scrollbar {
  width: 8px;
}
.custom-scrollbar::-webkit-scrollbar-thumb {
  background: rgba(0,0,0,0.1);
  border-radius: 10px;
}
</style>
