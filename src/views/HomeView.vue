<script setup lang="ts">
import { ref, watch, computed } from 'vue'
import { useRouter, onBeforeRouteLeave, type RouteLocation } from 'vue-router'
import { useVirtualizer } from '@tanstack/vue-virtual'

// --- STORES ---
import { usePostsStore } from '@/stores/posts'
import { useCreatePostStore } from '@/stores/createPost'

// --- COMPONENTS ---
import CreateBox from '@/components/create/createPost/CreateBox.vue'
import PostItem from '@/components/feed/post/PostItem.vue'
import PostSkeleton from '@/components/feed/post/PostSkeleton.vue'
import PeopleYouMayKnow from '@/components/friends/PeopleYouMayKnow.vue'
import StoriesList from '@/components/feed/stories/list/StoriesList.vue'
import LeftSidebar from '@/components/home/LeftSidebar.vue'
import RightSidebar from '@/components/home/RightSidebar.vue'
import ConfirmationModal from '@/components/common/ConfirmationModal.vue'
import ReelsGallery from '@/components/feed/reel/ReelsGallery.vue';
import PostModal from '@/components/feed/post/PostModal.vue'
import BaseModal from '@/components/common/BaseModal.vue'

// --- DATA / UTILS ---
import { getPostById } from '@/data/posts'
import { getUserById } from '@/data/users'

// =========================================
// 1. DANE I STORE
// =========================================
const postsStore = usePostsStore()
const localPosts = ref([...postsStore.posts])

// Synchronizacja lokalnej listy z Pinia Store
watch(
  () => postsStore.posts,
  (newPosts) => {
    localPosts.value = [...newPosts]
  },
  { deep: true }
)

// =========================================
// 2. LOGIKA INFINITE SCROLL
// =========================================
const postsPerPage = 5
const displayedCount = ref(5)
const isLoading = ref(false)

const loadMorePosts = async () => {
  // Blokada: jeśli już ładujemy lub wyświetliliśmy wszystko
  if (isLoading.value || displayedCount.value >= localPosts.value.length) return

  isLoading.value = true

  // Symulacja opóźnienia API (np. pobieranie z serwera)
  await new Promise(resolve => setTimeout(resolve, 3000))

  displayedCount.value += postsPerPage
  isLoading.value = false
}

// =========================================
// 3. PRZYGOTOWANIE LISTY (Wstrzykiwanie elementów)
// =========================================
const peopleYouMayKnowIndex = Math.floor(Math.random() * 5) + 2

const processedList = computed(() => {
  const list: any[] = []
  // Bierzemy tylko tyle postów, ile załadowaliśmy do tej pory
  const currentPosts = localPosts.value.slice(0, displayedCount.value)

  currentPosts.forEach((post, index) => {
    list.push({ type: 'post', data: post, id: `post-${post.id}` })

    // Wstrzykiwanie "Znajomych" i "Rolek" w losowe/określone miejsca
    if (index === peopleYouMayKnowIndex) {
      list.push({ type: 'peopleYouMayKnow', id: 'people-you-may-know' })
    }
    if (index === peopleYouMayKnowIndex + 1) {
      list.push({ type: 'reelsGallery', id: 'reels-gallery' })
    }
  })

  return list
})

// =========================================
// 4. TANSTACK VIRTUAL (Konfiguracja)
// =========================================
const parentRef = ref<HTMLElement | null>(null)

const rowVirtualizer = useVirtualizer({
  // WAŻNE: count musi być getterem, aby reagować na zmiany długości tablicy
  get count() {
    return processedList.value.length
  },
  getScrollElement: () => parentRef.value,
  estimateSize: () => 600, // Szacowana wysokość elementu
  overscan: 10, // Renderuj 10 elementów poza ekranem dla płynności
})

const virtualRows = computed(() => rowVirtualizer.value.getVirtualItems())
const totalSize = computed(() => rowVirtualizer.value.getTotalSize())

// Funkcja mierząca rzeczywistą wysokość wyrenderowanego elementu
const measureElement = (el: Element | ComponentPublicInstance | null) => {
  if (!el) return
  const node = '$el' in el ? (el.$el as HTMLElement) : (el as HTMLElement)
  rowVirtualizer.value.measureElement(node)
}

// TRIGGER ŁADOWANIA
// Obserwujemy wyrenderowane wiersze. Jeśli zbliżamy się do końca -> ładuj więcej.
watch(
  () => virtualRows.value,
  (rows) => {
    if (!rows.length) return

    const lastRow = rows[rows.length - 1]
    const totalItems = processedList.value.length

    // Jeśli użytkownik widzi 5. element od końca listy i nie ładujemy obecnie danych
    if (!isLoading.value && lastRow.index >= totalItems - 5) {
      loadMorePosts()
    }
  },
  { deep: true }
)

// =========================================
// 5. ROUTING & MODALS (Zabezpieczenia)
// =========================================
const router = useRouter()
const route = router.currentRoute
const post = computed(() => getPostById(String(route.value.params.id)))

const createPostStore = useCreatePostStore()
const showConfirmModal = ref(false)
const pendingRoute = ref<RouteLocation | null>(null)

// Guard: Czy masz niezapisany post?
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
  <div class="flex w-full h-screen overflow-hidden bg-theme-bg text-theme-text relative">

    <div id="LeftSection" class="hidden lg:block shrink-0 w-[360px] ">
      <LeftSidebar />
    </div>

    <div
      ref="parentRef"
      class="flex-1 h-screen overflow-y-auto custom-scrollbar"
    >
      <div class="flex flex-col md:grid md:grid-cols-[1fr_350px] lg:grid-cols-[5fr_2fr] w-full 3xl:max-w-[1500px] max-w-full mt-14 mx-auto px-0 lg:px-4 gap-4">

        <div id="MiddleSection" class="flex justify-center w-full min-w-0">
          <div id="PostsSection" class="w-full md:max-w-[700px] px-2 md:px-0">

            <CreateBox />
            <StoriesList />

            <div
              :style="{
                height: `${totalSize}px`,
                width: '100%',
                position: 'relative',
              }"
            >
              <div
                v-for="virtualRow in virtualRows"
                :key="virtualRow.key"
                :data-index="virtualRow.index"
                :ref="measureElement"
                :style="{
                  position: 'absolute',
                  top: 0,
                  left: 0,
                  width: '100%',
                  transform: `translateY(${virtualRow.start}px)`,
                }"
              >
                <div class="mb-4">
                  <PostItem
                    v-if="processedList[virtualRow.index].type === 'post'"
                    :post="processedList[virtualRow.index].data"
                  />
                  <PeopleYouMayKnow
                    v-else-if="processedList[virtualRow.index].type === 'peopleYouMayKnow'"
                  />
                  <ReelsGallery
                    v-else-if="processedList[virtualRow.index].type === 'reelsGallery'"
                  />
                </div>
              </div>
            </div>

            <div v-if="isLoading" class="pt-2 pb-10 space-y-4">
              <PostSkeleton v-for="n in 2" :key="n" />
            </div>

            <div v-if="!isLoading && displayedCount >= localPosts.length" class="text-center py-20 text-theme-text-secondary opacity-50">
              Nie ma więcej postów.
            </div>

          </div>
        </div>

        <div class="hidden md:block pl-2">
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
/* Stylizacja scrollbara (opcjonalna, dla estetyki) */
.custom-scrollbar::-webkit-scrollbar {
  width: 8px;
}
.custom-scrollbar::-webkit-scrollbar-thumb {
  background: rgba(0,0,0,0.2);
  border-radius: 10px;
}
.custom-scrollbar::-webkit-scrollbar-track {
  background: transparent;
}
</style>
