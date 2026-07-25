<template>
  <Teleport to="body">
    <!-- Ekran ładowania danych z GraphQL -->
    <div v-if="loadingFeed" class="fixed inset-0 z-[500] flex items-center justify-center bg-black text-white">
      <p class="text-xl animate-pulse">Ładowanie galerii...</p>
    </div>

    <!-- Jeśli zapytanie się wykonało, ale brak posta -->
    <div v-else-if="!currentPost && feedResult" class="fixed inset-0 z-[500] flex flex-col items-center justify-center gap-4 bg-black text-white">
      <p class="text-xl text-red-400">Nie znaleziono zdjęcia w feedzie.</p>
      <button @click="router.back()" class="px-4 py-2 bg-gray-800 rounded hover:bg-gray-700 transition-colors">
        Zamknij
      </button>
    </div>

    <!-- GŁÓWNY WIDOK GALERII -->
    <div
      v-else-if="currentPost"
      class="fixed inset-0 z-[500] flex bg-black overflow-hidden select-none text-[#050505]"
    >
      <!-- ============================================== -->
      <!-- WARSTWA SZEFA: PRZYCISKI INTERFEJSU (OVERLAY) -->
      <!-- ============================================== -->
      <div class="absolute top-0 left-0 right-0 h-14 z-[510] flex items-center justify-between px-4 pointer-events-none">
        <div class="flex items-center gap-3 pointer-events-auto">
          <button
            @click="router.back()"
            class="p-2 bg-[#242526]/60 hover:bg-[#3A3B3C]/80 text-white rounded-full transition-colors"
            :aria-label="$t('common.close')"
          >
            <Close :size="24" />
          </button>
          <div class="w-10 h-10 bg-[#1877F2] rounded-full flex items-center justify-center text-white font-black text-2xl tracking-tighter shadow-sm cursor-pointer">
            f
          </div>
        </div>
        <NavbarRight class="text-white pointer-events-auto" />
      </div>

      <!-- ============================================== -->
      <!-- GŁÓWNY UKŁAD LEWO / PRAWO -->
      <!-- ============================================== -->
      <div class="flex flex-1 w-full h-full relative">
        <!-- LEWO: OBSZAR MULTIMEDIÓW -->
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

        <!-- PRAWO: PANEL BOCZNY (Social) -->
        <div
          v-if="!isFullScreen && currentPost"
          class="w-[360px] h-full bg-white shrink-0 flex flex-col z-10 shadow-xl"
        >
          <HoverScrollbar maxHeight="100%" class="flex-1 overflow-y-auto">
            <!-- Nagłówek Autora -->
            <div class="pt-16">
              <div class="flex items-start justify-between p-4 border-t border-theme-border">
                <div class="flex items-center gap-2.5">
                  <UserAvatar
                    v-if="resolvedAuthor"
                    :user="resolvedAuthor"
                    :size="40"
                    class="shrink-0 hover:brightness-95 border border-gray-200"
                  />
                  <div class="flex flex-col">
                    <div class="font-semibold text-[15px] text-[#050505] leading-5 cursor-pointer hover:underline">
                      {{ resolvedAuthor?.name || 'Użytkownik' }}
                    </div>
                    <div class="flex items-center text-[13px] text-[#65676B] font-normal mt-0.5">
                      <FormattedDate :date="currentPost.date" />
                      <span class="mx-1 font-bold">·</span>
                      <Earth :size="14" class="text-[#65676B]" />
                    </div>
                  </div>
                </div>

                <button class="text-[#65676B] hover:bg-gray-100 rounded-full p-2 transition-colors">
                  <DotsHorizontal :size="20" />
                </button>
              </div>
            </div>

            <!-- Treść postu -->
            <div class="px-4 pb-3 text-[15px] text-[#050505] whitespace-pre-wrap leading-normal">
              {{ currentPost.content }}
            </div>

            <!-- Statystyki Polubień / Komentarzy -->
            <div class="mx-4 flex items-center justify-between py-2.5 text-[#65676B] text-[14px] border-b border-theme-border">
              <!-- Lewa strona: Lajki -->
              <div class="flex items-center cursor-pointer hover:underline select-none">
                <div class="bg-[#1877F2] rounded-full w-[18px] h-[18px] flex items-center justify-center mr-1.5 shadow-sm">
                  <ThumbUp class="text-white" :size="10" />
                </div>
                <span class="text-[#65676B] font-normal">{{ likesCount || 0 }}</span>
              </div>

              <!-- Prawa strona: Komentarze i udostępnienia -->
              <div class="flex items-center gap-3 text-[14px] text-[#65676B] select-none">
                <div v-if="commentsCount > 0" class="flex items-center gap-1 cursor-pointer hover:underline">
                  <span>{{ commentsCount }}</span>
                  <CommentTextMultiple :size="16" class="text-[#8C9199]" />
                </div>
                <div v-if="sharesCount > 0" class="flex items-center gap-1 cursor-pointer hover:underline">
                  <span>{{ sharesCount }}</span>
                  <Share :size="16" class="text-[#8C9199] transform scale-x-[-1]" />
                </div>
              </div>
            </div>

            <!-- Przyciski Interakcji (Lubię to, Komentarz, Udostępnij) -->
            <PostActions :post="currentPost" />

            <!-- Filtrowanie komentarzy -->
            <div class="flex justify-between items-center px-4 pt-2.5 pb-1">
              <CommentFilter />
            </div>

            <!-- Pusty stan - komentarze wymuszone jako puste -->
            <EmptyState v-if="commentsList.length === 0" />

            <!-- Lista komentarzy (obecnie pusta z założenia) -->
            <div v-if="commentsList.length > 0" class="pt-1 px-4 pb-4">
              <CommentItem
                v-for="comment in commentsList"
                :key="comment.id"
                :comment="comment"
                :post-autor="currentPost.authorId?.toString()"
                :depth="0"
                :post-id="currentPost.id"
              />
            </div>
          </HoverScrollbar>

          <!-- Input dodawania komentarza na dole -->
          <div class="p-3 border-t border-[#CED0D4] bg-white sticky bottom-0 z-20 shadow-[0_-2px_6px_rgba(0,0,0,0.02)]">
            <CommentReplyInput :post-id="currentPost.id" />
          </div>
        </div>

        <!-- Dodatkowy margines boczny dla desktopu -->
        <div
          v-if="!isFullScreen && currentPost"
          class="hidden md:block w-20 border-l border-theme-border bg-white shrink-0"
        ></div>
      </div>
    </div>
  </Teleport>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import Close from 'vue-material-design-icons/Close.vue'
import ThumbUp from 'vue-material-design-icons/ThumbUp.vue'
import Earth from 'vue-material-design-icons/Earth.vue'
import DotsHorizontal from 'vue-material-design-icons/DotsHorizontal.vue'
import CommentTextMultiple from 'vue-material-design-icons/CommentTextMultiple.vue'
import Share from 'vue-material-design-icons/Share.vue'

import HoverScrollbar from '@/components/common/HoverScrollbar.vue'
import CommentItem from '@/components/feed/comment/CommentItem.vue'
import GalleryMediaViewer from '@/components/common/GalleryMediaViewer.vue'
import CommentReplyInput from '@/components/feed/comment/CommentReplyInput.vue'
import NavbarRight from '@/layouts/Navbar/NavbarRight.vue'
import type { Comment } from '@/types/Post'
import type { ImageTagType } from '@/types/Post'

import CommentFilter from '@/components/profile/CommentFilter.vue'
import FormattedDate from '@/components/common/FormattedDate.vue'
import PostActions from '~/components/feed/post/PostActions.vue'
import EmptyState from '~/components/feed/comment/EmptyState.vue'

const GET_FEED_QUERY = gql`
  query GetFeed($currentUserId: ID!, $limit: Int, $offset: Int) {
    getFeed(currentUserId: $currentUserId, limit: $limit, offset: $offset) {
      id
      authorId
      author {
        id
        firstName
        lastName
        avatarId
      }
      content
      date
      timestamp
      isAnonymous
      targetId
      targetType
      commentCount
      shareCount
      visibility
      allowedUserIds
      media {
        src
        altText
      }
      reactions {
        reactionType
        userIds
      }
    }
  }
`

const route = useRoute()
const router = useRouter()
const isFullScreen = ref(false)
const currentUserId = ref('123') // Dummy ID

// POBIERANIE DANYCH Z BACKENDU
const { data: feedResult, pending: loadingFeed } = await useLazyAsyncQuery(
  GET_FEED_QUERY,
  computed(() => ({
    currentUserId: currentUserId.value,
    limit: 10,
    offset: 0
  })),
  { enabled: computed(() => !!currentUserId.value) }
)

// LOGIKA KOMPONENTU
function extractPhotoId(src: string): string {
  if (!src) return ''
  if (src.includes('/files/')) {
    const filename = src.split('/files/').pop() || ''
    return filename.split('?')[0]
  }
  return src.split('/').pop() || src
}

const postId = computed(() => {
  if (route.query.set) return String(route.query.set).replace(/^a\./, '')
  return String(route.params.postId || route.query.postId || '')
})

const currentPost = computed(() => {
  const feed = feedResult.value?.getFeed || []
  return feed.find((p: any) => String(p.id) === String(postId.value)) || null
})

// Wymuszona pusta lista - tak jak ustalone
const commentsList = computed<Comment[]>(() => [])

const imageIndexParam = computed(() => {
  const fbidVal = String(route.query.fbid || route.params.imageIndex || '0')
  if (currentPost.value?.media) {
    const idx = currentPost.value.media.findIndex((m: any) => extractPhotoId(m.src) === fbidVal || m.src === fbidVal)
    if (idx !== -1) return idx
  }
  return Number(fbidVal) || 0
})

const currentImageIndex = ref(imageIndexParam.value)


const resolvedAuthor = computed(() => {
  const postAuthor = currentPost.value?.author
  if (!postAuthor) return null
  return {
    ...postAuthor,
    name: `${postAuthor.firstName} ${postAuthor.lastName}`
  }
})

const likesCount = computed(() => {
  if (currentPost.value?.reactions) {
    return currentPost.value.reactions.reduce((sum: number, r: any) => sum + (r.userIds?.length || 0), 0)
  }
  return 0
})

const commentsCount = computed(() => currentPost.value?.commentCount || 0)
const sharesCount = computed(() => currentPost.value?.shareCount || 0)

const currentImage = computed((): { src: string; altText?: string; tags?: ImageTagType[] } | undefined => {
  return currentPost.value?.media?.[currentImageIndex.value]
})

const getMediaUrl = (src: string) => {
  if (!src) return ''
  if (src.startsWith('http://localhost/files/') || src.startsWith('http://localhost/videos/')) {
    src = src.replace('http://localhost/', 'http://localhost:8080/')
  }
  if (/^(http:\/\/|https:\/\/|blob:|data:)/.test(src)) return src

  const baseUrl = import.meta.env.VITE_API_URL || 'http://localhost:8080'
  return src.startsWith('/') ? `${baseUrl}${src}` : `${baseUrl}/${src}`
}

const resolvedMedia = computed(() => {
  if (!currentImage.value) return undefined
  return {
    ...currentImage.value,
    src: getMediaUrl(currentImage.value.src),
  }
})

const hasPrevImage = computed(() => currentImageIndex.value > 0)
const hasNextImage = computed(() => {
  if (!currentPost.value) return false
  return currentImageIndex.value < (currentPost.value.media?.length || 0) - 1
})

const updateUrl = (index: number) => {
  if (route.query.set || route.path.includes('/photo')) {
    let photoId = String(index)
    if (currentPost.value?.media?.[index]) {
      photoId = extractPhotoId(currentPost.value.media[index].src)
    }
    router.replace({ path: '/photo', query: { fbid: photoId, set: `a.${postId.value}` } })
  } else {
    router.replace({ params: { postId: postId.value, imageIndex: index } })
  }
}

const goToPrevImage = () => {
  if (hasPrevImage.value) updateUrl(--currentImageIndex.value)
}
const goToNextImage = () => {
  if (hasNextImage.value) updateUrl(++currentImageIndex.value)
}

watch(
  () => [route.params.imageIndex, route.query.fbid, currentPost.value],
  ([newIndex, newFbid, newPost]) => {
    if (newFbid !== undefined && newFbid !== null) {
      const fbidStr = String(newFbid)
      if (newPost && (newPost as any).media) {
        const idx = (newPost as any).media.findIndex((m: any) => extractPhotoId(m.src) === fbidStr || m.src === fbidStr)
        if (idx !== -1) {
          currentImageIndex.value = idx
          return
        }
      }
      currentImageIndex.value = Number(newFbid) || 0
    } else {
      currentImageIndex.value = Number(newIndex) || 0
    }
  },
  { immediate: true }
)
</script>
