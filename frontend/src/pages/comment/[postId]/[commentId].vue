<template>
  <Teleport to="body">
    <div
      v-if="!loading && currentPost"
      class="fixed inset-0 z-[500] flex bg-black overflow-hidden select-none text-[#050505]"
    >
      <!-- PRZYCISKI INTERFEJSU (OVERLAY) -->
      <div class="absolute top-0 left-0 right-0 h-14 z-[510] flex items-center justify-between px-4 pointer-events-none">
        <div class="flex items-center gap-3 pointer-events-auto">
          <button
            @click="router.back()"
            class="p-2 bg-[#242526]/60 hover:bg-[#3A3B3C]/80 text-white rounded-full transition-colors"
          >
            <Close :size="24" />
          </button>
          <div class="w-10 h-10 bg-[#1877F2] rounded-full flex items-center justify-center text-white font-black text-2xl tracking-tighter">
            f
          </div>
        </div>
        <NavbarRight class="text-white" />
      </div>

      <!-- GŁÓWNY UKŁAD -->
      <div class="flex flex-1 w-full h-full relative">
        <!-- LEWO: GALERIA -->
        <div class="flex-1 h-full bg-black flex items-center justify-center relative">
          <GalleryMediaViewer
            v-if="resolvedMedia"
            v-model:is-full-screen="isFullScreen"
            :media="resolvedMedia"
            :has-prev="hasPrevImage"
            :has-next="hasNextImage"
            @prev="goToPrevImage"
            @next="goToNextImage"
            class="w-full h-full"
          />
        </div>

        <!-- PRAWO: PANEL BOCZNY -->
        <div
          v-if="!isFullScreen"
          class="w-[360px] h-full bg-white shrink-0 flex flex-col z-10 shadow-xl"
        >
          <HoverScrollbar maxHeight="100%" class="flex-1 overflow-y-auto">
            <!-- Autor -->
            <div class="pt-16">
              <div class="flex items-start justify-between p-4 border-t border-theme-border">
                <div class="flex items-center gap-2.5">
                  <UserAvatar v-if="currentPost.author" :user="currentPost.author" :size="40" />
                  <div class="flex flex-col">
                    <div class="font-semibold text-[15px] text-[#050505] hover:underline cursor-pointer">
                      {{ currentPost.author?.firstName }} {{ currentPost.author?.lastName }}
                    </div>
                    <div class="flex items-center text-[13px] text-[#65676B] mt-0.5">
                      <FormattedDate :date="currentPost.date" />
                      <span class="mx-1 font-bold">·</span>
                      <Earth :size="14" />
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <!-- Treść -->
            <div class="px-4 pb-3 text-[15px] text-[#050505] whitespace-pre-wrap">
              {{ currentPost.content }}
            </div>

            <!-- Statystyki -->
            <div class="mx-4 flex items-center justify-between py-2.5 text-[#65676B] text-[14px]">
              <div class="flex items-center cursor-pointer hover:underline">
                <div class="bg-[#1877F2] rounded-full w-[18px] h-[18px] flex items-center justify-center mr-1.5">
                  <ThumbUp class="text-white" :size="10" />
                </div>
                <span>{{ likesCount }}</span>
              </div>

              <div class="flex items-center gap-3">
                <div v-if="currentPost.commentCount > 0" class="flex items-center gap-1">
                  <span>{{ currentPost.commentCount }}</span>
                  <CommentTextMultiple :size="16" />
                </div>
                <div v-if="currentPost.shareCount > 0" class="flex items-center gap-1">
                  <span>{{ currentPost.shareCount }}</span>
                  <Share :size="16" class="transform scale-x-[-1]" />
                </div>
              </div>
            </div>

            <PostActions :post="currentPost" />

            <div class="flex justify-between items-center px-4 pt-2.5 pb-1">
              <CommentFilter />
            </div>

            <!-- Komentarze -->
            <div v-if="currentPost.comments?.length > 0" class="pt-1 px-4 pb-4">
              <CommentItem
                v-for="comment in currentPost.comments"
                :key="comment.id"
                :comment="comment"
                :post-autor="currentPost.authorId.toString()"
                :depth="0"
                :post-id="currentPost.id"
              />
            </div>
            <EmptyState v-else />
          </HoverScrollbar>

          <!-- Input dodawania komentarza -->
          <div class="p-3 border-t border-[#CED0D4] bg-white sticky bottom-0 z-20">
            <CommentReplyInput :post-id="currentPost.id" />
          </div>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { feedApi } from '@/api/feed'

import Close from 'vue-material-design-icons/Close.vue'
import ThumbUp from 'vue-material-design-icons/ThumbUp.vue'
import Earth from 'vue-material-design-icons/Earth.vue'
import CommentTextMultiple from 'vue-material-design-icons/CommentTextMultiple.vue'
import Share from 'vue-material-design-icons/Share.vue'

import HoverScrollbar from '@/components/common/HoverScrollbar.vue'
import CommentItem from '@/components/feed/comment/CommentItem.vue'
import GalleryMediaViewer from '@/components/common/GalleryMediaViewer.vue'
import CommentReplyInput from '@/components/feed/comment/CommentReplyInput.vue'
import NavbarRight from '@/components/navbar/NavbarRight.vue'
import CommentFilter from '@/components/profile/CommentFilter.vue'
import FormattedDate from '@/components/common/FormattedDate.vue'
import PostActions from '~/components/feed/post/PostActions.vue'
import EmptyState from '~/components/feed/comment/EmptyState.vue'
import { useComments } from '@/composables/feed/useComments'
definePageMeta({ showMainLayout: false, isPopup: true })

const route = useRoute()
const router = useRouter()
const isFullScreen = ref(false)
const loading = ref(false)
const currentPost = ref<any>(null)
const { fetchCommentsForPost } = useComments()

const postId = computed(() => String(route.params.postId || ''))

const fetchPost = async () => {
  if (!postId.value) return
  loading.value = true
  try {
    const post = await feedApi.getPost(postId.value)
    if (post) {
      currentPost.value = post
      await fetchCommentsForPost(currentPost.value)
    } else {
      currentPost.value = null
    }
  } catch (err) {
    console.error('Failed to fetch post:', err)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchPost()
})

watch(postId, () => {
  fetchPost()
})

// Prosty indeks zdjęcia bezpośrednio z parametrów URL (np. /posts/123/photos/2)
const currentImageIndex = computed({
  get: () => Number(route.params.imageIndex || 0),
  set: (val) => {
    router.replace({ params: { ...route.params, imageIndex: String(val) } })
  }
})

// Obliczanie polubień na podstawie danych z API
const likesCount = computed(() => {
  if (!currentPost.value?.reactions) return 0
  return currentPost.value.reactions.reduce((sum: number, r: any) => sum + (r.userIds?.length || 0), 0)
})

// Rozwiązanie adresu URL multimediów
const getMediaUrl = (src: string) => {
  if (!src) return ''
  const baseUrl = import.meta.env.VITE_API_URL || 'http://localhost:8080'
  return src.startsWith('/') ? `${baseUrl}${src}` : src
}

const resolvedMedia = computed(() => {
  const mediaItem = currentPost.value?.media?.[currentImageIndex.value]
  if (!mediaItem) return undefined
  return {
    ...mediaItem,
    src: getMediaUrl(mediaItem.src)
  }
})

// Nawigacja po zdjęciach
const hasPrevImage = computed(() => currentImageIndex.value > 0)
const hasNextImage = computed(() => {
  if (!currentPost.value?.media) return false
  return currentImageIndex.value < currentPost.value.media.length - 1
})

const goToPrevImage = () => {
  if (hasPrevImage.value) currentImageIndex.value--
}

const goToNextImage = () => {
  if (hasNextImage.value) currentImageIndex.value++
}
</script>
