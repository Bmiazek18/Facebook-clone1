<template>
  <Teleport to="body">
    <!-- Ekran ładowania danych z GraphQL (tylko tryb posta) -->
    <div
      v-if="!isStandalone && loadingFeed"
      class="fixed inset-0 z-[500] flex items-center justify-center bg-black text-white"
    >
      <p class="text-xl animate-pulse">{{ $t('media.ladowanieGalerii') }}</p>
    </div>

    <!-- Jeśli zapytanie się wykonało, ale brak posta -->
    <div
      v-else-if="!isStandalone && !currentPost && postResult"
      class="fixed inset-0 z-[500] flex flex-col items-center justify-center gap-4 bg-black text-white"
    >
      <p class="text-xl text-red-400">{{ $t('media.nieZnalezionoZdjeciaW') }}</p>
      <button
        @click="router.back()"
        class="px-4 py-2 bg-gray-800 rounded hover:bg-gray-700 transition-colors"
      >{{ $t('common.close') }}</button>
    </div>

    <!-- GŁÓWNY WIDOK GALERII -->
    <div
      v-else-if="galleryReady"
      class="fixed inset-0 z-[500] flex bg-black overflow-hidden select-none text-theme-text"
    >
      <div
        class="absolute top-0 left-0 right-0 h-14 z-[510] flex items-center justify-between px-4 pointer-events-none"
      >
        <div class="flex items-center gap-3 pointer-events-auto">
          <button
            @click="router.back()"
            class="p-2 bg-[#242526]/60 hover:bg-[#3A3B3C]/80 text-white rounded-full transition-colors"
            :aria-label="$t('common.close')"
          >
            <Close :size="24" />
          </button>
          <div
            class="w-10 h-10 bg-[#1877F2] rounded-full flex items-center justify-center text-white font-black text-2xl tracking-tighter shadow-sm cursor-pointer"
          >
            f
          </div>
        </div>
        <NavbarRight class="text-white pointer-events-auto" />
      </div>

      <div class="flex flex-1 w-full h-full relative">
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

        <div
          v-if="!isFullScreen && (currentPost || isStandalone)"
          class="w-[360px] h-full bg-theme-bg-secondary text-theme-text border-l border-theme-border shrink-0 flex flex-col z-10 shadow-xl"
        >
          <HoverScrollbar maxHeight="100%" class="flex-1 overflow-y-auto">
            <div class="pt-16">
              <div class="flex items-start justify-between p-4 border-t border-theme-border">
                <div class="flex items-center gap-2.5">
                  <UserAvatar
                    v-if="resolvedAuthor"
                    :user="resolvedAuthor"
                    :size="40"
                    class="shrink-0 hover:brightness-95 border border-theme-border"
                  />
                  <div class="flex flex-col">
                    <div
                      class="font-semibold text-[15px] text-theme-text leading-5 cursor-pointer hover:underline"
                      @click="goToAuthorProfile"
                    >
                      {{ resolvedAuthor?.name || 'Użytkownik' }}
                    </div>
                    <div class="flex items-center text-[13px] text-theme-text-secondary font-normal mt-0.5">
                      <template v-if="isStandalone">
                        <span>{{ standaloneLabel }}</span>
                      </template>
                      <template v-else-if="currentPost">
                        <FormattedDate :date="currentPost.date" />
                        <span class="mx-1 font-bold">·</span>
                        <Earth :size="14" class="text-theme-text-secondary" />
                      </template>
                    </div>
                  </div>
                </div>

                <button class="text-theme-text-secondary hover:bg-theme-hover rounded-full p-2 transition-colors">
                  <DotsHorizontal :size="20" />
                </button>
              </div>
            </div>

            <template v-if="currentPost">
              <div class="px-4 pb-3 text-[15px] text-theme-text whitespace-pre-wrap leading-normal">
                {{ currentPost.content }}
              </div>

              <div
                class="mx-4 flex items-center justify-between py-2.5 text-theme-text-secondary text-[14px] border-b border-theme-border"
              >
                <div class="flex items-center cursor-pointer hover:underline select-none">
                  <div
                    class="bg-[#1877F2] rounded-full w-[18px] h-[18px] flex items-center justify-center mr-1.5 shadow-sm"
                  >
                    <ThumbUp class="text-white" :size="10" />
                  </div>
                  <span class="text-theme-text-secondary font-normal">{{ likesCount || 0 }}</span>
                </div>

                <div class="flex items-center gap-3 text-[14px] text-theme-text-secondary select-none">
                  <div
                    v-if="commentsCount > 0"
                    class="flex items-center gap-1 cursor-pointer hover:underline"
                  >
                    <span>{{ commentsCount }}</span>
                    <CommentTextMultiple :size="16" class="text-theme-text-secondary" />
                  </div>
                  <div
                    v-if="sharesCount > 0"
                    class="flex items-center gap-1 cursor-pointer hover:underline"
                  >
                    <span>{{ sharesCount }}</span>
                    <Share :size="16" class="text-theme-text-secondary transform scale-x-[-1]" />
                  </div>
                </div>
              </div>

              <PostActions :post="currentPost" @comment="focusCommentInput" />

              <div class="flex justify-between items-center px-4 pt-2.5 pb-1">
                <CommentFilter />
              </div>

              <EmptyState v-if="commentsList.length === 0" />

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
            </template>

            <div v-else class="px-4 pb-4 text-[15px] text-theme-text-secondary leading-normal">
              {{ standaloneDescription }}
            </div>
          </HoverScrollbar>

          <div
            v-if="currentPost"
            class="p-3 border-t border-theme-border bg-theme-bg-secondary sticky bottom-0 z-20 shadow-[0_-2px_6px_rgba(0,0,0,0.02)]"
          >
            <CommentReplyInput ref="commentInputRef" :post-id="currentPost.id" />
          </div>
        </div>

        <div
          v-if="!isFullScreen && (currentPost || isStandalone)"
          class="hidden md:block w-20 border-l border-theme-border bg-theme-bg-secondary shrink-0"
        ></div>
      </div>
    </div>
  </Teleport>
</template>

<script setup lang="ts">
import { ref, computed, watch, provide, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { gql } from 'graphql-tag'
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
import NavbarRight from '@/components/navbar/NavbarRight.vue'
import UserAvatar from '@/components/common/UserAvatar.vue'
import type { Comment } from '@/types/Post'
import type { ImageTagType } from '@/types/Post'

const config = useRuntimeConfig()

import CommentFilter from '@/components/profile/CommentFilter.vue'
import FormattedDate from '@/components/common/FormattedDate.vue'
import PostActions from '~/components/feed/post/PostActions.vue'
import EmptyState from '~/components/feed/comment/EmptyState.vue'

import { useAuthStore } from '@/stores/auth'
import { useComments } from '@/composables/feed/useComments'
import { useProfilePhotoPost } from '@/composables/feed/useProfilePhotoPost'

const GET_POST_BY_ID_QUERY = gql`
  query GetPostById($postId: ID!) {
    getPostById(postId: $postId) {
      id
      authorId
      author {
        id
        firstName
        lastName
        avatarId
        avatar
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
      taggedUsers {
        id
        firstName
        lastName
      }
      media {
        src
        altText
        backgroundColor
        tags {
          id
          x
          y
          userId
          user {
            id
            firstName
            lastName
          }
        }
      }
      reactions {
        reactionType
        userIds
        users {
          id
          firstName
          lastName
        }
      }
    }
  }
`

function extractPhotoId(src: string): string {
  if (!src) return ''
  if (src.includes('/files/') || src.includes('/media/')) {
    const marker = src.includes('/media/') ? '/media/' : '/files/'
    const filename = src.split(marker).pop() || ''
    return filename.split('?')[0] || ''
  }
  return src.split('/').pop() || src
}

const route = useRoute()
const router = useRouter()
const isFullScreen = ref(false)

const authStore = useAuthStore()
const currentUserId = computed(() => String(authStore.currentUserId))

const standaloneSrc = computed(() => {
  const raw = route.query.src
  return raw ? String(raw) : ''
})
const isStandalone = computed(() => !!standaloneSrc.value)
const standaloneType = computed(() => {
  const t = String(route.query.type || 'photo')
  return t === 'cover' ? 'cover' : t === 'avatar' ? 'avatar' : 'photo'
})
const standaloneUserId = computed(() => String(route.query.userId || ''))
const standaloneName = computed(() => String(route.query.name || ''))

const standaloneLabel = computed(() => {
  if (standaloneType.value === 'cover') return 'Zdjęcie w tle'
  if (standaloneType.value === 'avatar') return 'Zdjęcie profilowe'
  return 'Zdjęcie'
})

const standaloneDescription = computed(() => {
  const name = standaloneName.value || resolvedAuthor.value?.name || 'Użytkownik'
  if (standaloneType.value === 'cover') return `Zdjęcie w tle użytkownika ${name}`
  if (standaloneType.value === 'avatar') return `Zdjęcie profilowe użytkownika ${name}`
  return `Zdjęcie użytkownika ${name}`
})

const postId = computed(() => {
  if (isStandalone.value) return ''
  if (route.query.set) return String(route.query.set).replace(/^a\./, '')
  return String(route.params.postId || route.query.postId || '')
})

const { data: postResult, pending: loadingFeed } = await useLazyAsyncQuery(
  GET_POST_BY_ID_QUERY,
  computed(() => ({
    postId: postId.value,
  })),
  { enabled: computed(() => !isStandalone.value && !!postId.value) },
)

const localPost = ref<any>(null)
const { fetchCommentsForPost } = useComments()
const { resolveProfilePhotoPost } = useProfilePhotoPost()
const resolvingStandalone = ref(false)

watch(
  () => postResult.value?.getPostById,
  async (newPost) => {
    if (newPost) {
      const cloned = JSON.parse(JSON.stringify(newPost))
      localPost.value = cloned
      if (!cloned.comments) {
        await fetchCommentsForPost(localPost.value)
      }
    } else if (!isStandalone.value) {
      localPost.value = null
    }
  },
  { immediate: true },
)

// Tryb avatar/cover → podłącz prawdziwy post (komentarze / reakcje)
const hydrateStandalonePost = async () => {
  if (!isStandalone.value || !standaloneSrc.value) return
  resolvingStandalone.value = true
  try {
    const kind = standaloneType.value === 'cover' ? 'cover' : 'avatar'
    const post = await resolveProfilePhotoPost({
      userId: standaloneUserId.value || currentUserId.value,
      kind,
      src: standaloneSrc.value,
    })
    if (post) {
      // Zawsze przejdź na tryb posta (komentarze / reakcje jak w zwykłym /photo/)
      router.replace({
        path: '/photo',
        query: {
          fbid:
            String(post.media?.[0]?.src || standaloneSrc.value)
              .split('/')
              .pop()
              ?.split('?')[0] || '0',
          set: `a.${post.id}`,
        },
      })
    }
  } catch (e) {
    console.warn('Nie udało się podłączyć posta do zdjęcia profilowego:', e)
  } finally {
    resolvingStandalone.value = false
  }
}

onMounted(() => {
  hydrateStandalonePost()
})

watch(
  () => [standaloneSrc.value, standaloneType.value, standaloneUserId.value],
  () => {
    hydrateStandalonePost()
  },
)

const currentPost = computed(() => localPost.value)

const galleryReady = computed(() => {
  if (isStandalone.value) return !!standaloneSrc.value
  return !!currentPost.value
})

provide(
  'allPosts',
  computed(() => (currentPost.value ? [currentPost.value] : [])),
)

const commentsList = computed<Comment[]>(() => currentPost.value?.comments || [])

const imageIndexParam = computed(() => {
  const fbidVal = String(route.query.fbid || route.params.imageIndex || '0')
  if (currentPost.value?.media) {
    const idx = currentPost.value.media.findIndex(
      (m: any) => extractPhotoId(m.src) === fbidVal || m.src === fbidVal,
    )
    if (idx !== -1) return idx
  }
  return Number(fbidVal) || 0
})

const currentImageIndex = ref(imageIndexParam.value)

const resolvedAuthor = computed(() => {
  if (isStandalone.value) {
    const id = standaloneUserId.value || currentUserId.value
    const name =
      standaloneName.value ||
      (String(id) === currentUserId.value ? authStore.currentUser?.name : '') ||
      'Użytkownik'
    const avatar =
      standaloneType.value === 'avatar'
        ? standaloneSrc.value
        : authStore.currentUser && String(id) === currentUserId.value
          ? authStore.currentUser.avatar
          : undefined
    return { id, name, avatar }
  }

  const postAuthor = currentPost.value?.author
  if (!postAuthor) return null
  return {
    ...postAuthor,
    name: `${postAuthor.firstName} ${postAuthor.lastName}`,
  }
})

const goToAuthorProfile = () => {
  const id = resolvedAuthor.value?.id
  if (id) router.push(`/profile/${id}`)
}

const likesCount = computed(() => {
  if (currentPost.value?.reactions) {
    return currentPost.value.reactions.reduce(
      (sum: number, r: any) => sum + (r.userIds?.length || 0),
      0,
    )
  }
  return 0
})

const commentsCount = computed(() => currentPost.value?.commentCount || 0)
const sharesCount = computed(() => currentPost.value?.shareCount || 0)

const currentImage = computed(():
  | { src: string; altText?: string; tags?: ImageTagType[] }
  | undefined => {
  if (isStandalone.value) {
    return {
      src: standaloneSrc.value,
      altText: standaloneLabel.value,
    }
  }
  return currentPost.value?.media?.[currentImageIndex.value]
})

const getMediaUrl = (src: string) => {
  if (!src) return ''
  if (
    src.startsWith('http://localhost/files/') ||
    src.startsWith('http://localhost/videos/') ||
    src.startsWith('http://localhost/media/')
  ) {
    src = src.replace('http://localhost/', config.public.apiUrl + '/')
  }
  if (/^(http:\/\/|https:\/\/|blob:|data:)/.test(src)) return src

  const baseUrl = config.public.apiUrl
  return src.startsWith('/') ? `${baseUrl}${src}` : `${baseUrl}/${src}`
}

const resolvedMedia = computed(() => {
  if (!currentImage.value) return undefined
  return {
    ...currentImage.value,
    src: getMediaUrl(currentImage.value.src),
  }
})

const hasPrevImage = computed(() => !isStandalone.value && currentImageIndex.value > 0)
const hasNextImage = computed(() => {
  if (isStandalone.value || !currentPost.value) return false
  return currentImageIndex.value < (currentPost.value.media?.length || 0) - 1
})

const updateUrl = (index: number) => {
  if (isStandalone.value) return
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
    if (isStandalone.value) {
      currentImageIndex.value = 0
      return
    }
    if (newFbid !== undefined && newFbid !== null) {
      const fbidStr = String(newFbid)
      if (newPost && (newPost as any).media) {
        const idx = (newPost as any).media.findIndex(
          (m: any) => extractPhotoId(m.src) === fbidStr || m.src === fbidStr,
        )
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
  { immediate: true },
)

const commentInputRef = ref<any>(null)
const focusCommentInput = () => {
  commentInputRef.value?.focusInput()
}
</script>
