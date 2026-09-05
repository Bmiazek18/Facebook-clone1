import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { processPostsIntoReels } from '@/utils/reels'
import { useAuthStore } from '@/stores/auth'
import { useMutation } from '@vue/apollo-composable'
import { gql } from 'graphql-tag'
import { VOTE_ON_POLL_MUTATION } from '@/graphql/groups'
import {
  encodeStoryMetadata,
  parseStoryMetadata,
  type StoryMetadata,
} from '@/utils/storyMetadata'

const CREATE_STORY_MUTATION = gql`
  mutation CreateStory($input: CreateStoryInput!) {
    createStory(input: $input) {
      id
      authorId
      mediaUrl
      thumbMediaUrl
      mediaType
      text
      createdAt
      expiresAt
    }
  }
`

/**
 * Konwersja Data URL (Base64) na plik Blob przy użyciu natywnego fetch API
 */
async function dataURLtoBlob(dataUrl: string): Promise<Blob> {
  const res = await fetch(dataUrl)
  return await res.blob()
}

// ---------------------------------------------------------------------------
// POSTS STORE
// ---------------------------------------------------------------------------
export const usePostsStore = defineStore('posts', () => {
  const authStore = useAuthStore()
  const posts = ref<any[]>([])

  const currentUser = computed(() => ({
    id: authStore.currentUserId,
    name: authStore.currentUser?.name || 'Bartosz Miazek',
    avatar:
      authStore.currentUser?.avatar ||
      'https://scontent-waw2-1.xx.fbcdn.net/v/t39.30808-1/295055057_582985040112298_215415809791370036_n.jpg',
  }))

  const getPostById = (id: string | number) => posts.value.find((p) => String(p.id) === String(id))

  const addPost = (post: any) => {
    posts.value.unshift(post)
  }

  const removePost = (id: string) => {
    posts.value = posts.value.filter((p) => String(p.id) !== String(id))
  }

  const { mutate: voteOnPollMutate } = useMutation(VOTE_ON_POLL_MUTATION)

  const voteOnPoll = async (postId: string, optionId: string, userId: string) => {
    try {
      const result = await voteOnPollMutate({
        postId,
        optionId,
        userId,
      })
      if (result?.data?.voteOnPoll) {
        const updatedPost = result.data.voteOnPoll
        const postIndex = posts.value.findIndex((p) => String(p.id) === String(postId))
        if (postIndex !== -1) {
          posts.value[postIndex] = {
            ...posts.value[postIndex],
            context: updatedPost.context,
          }
        }
      }
    } catch (e) {
      console.error('Failed to vote on poll:', e)
    }
  }

  return {
    posts,
    currentUser,
    getPostById,
    addPost,
    removePost,
    voteOnPoll,
  }
})

// ---------------------------------------------------------------------------
// STORIES STORE
// ---------------------------------------------------------------------------
export const useStoriesStore = defineStore('stories', () => {
  const authStore = useAuthStore()
  const userStories = ref<any[]>([])
  const allUserStories = computed(() => userStories.value)
  const currentUserId = computed(() => String(authStore.currentUserId || ''))

  const getUserStories = (userId: string) => {
    return userStories.value.find((us) => String(us.userId) === String(userId)) || null
  }

  const { mutate: createStoryMutate } = useMutation(CREATE_STORY_MUTATION)

  const normalizeTusFilePath = (uploadUrl: string) => {
    let serverPath = uploadUrl || ''
    if (serverPath.includes('/files/')) {
      serverPath = '/files/' + serverPath.split('/files/').pop()
    }
    const qIdx = serverPath.indexOf('?')
    if (qIdx !== -1) serverPath = serverPath.slice(0, qIdx)
    const plusIdx = serverPath.indexOf('+')
    if (plusIdx !== -1) serverPath = serverPath.slice(0, plusIdx)
    return serverPath
  }

  /**
   * Pomocnicze wysyłanie pliku przez Tus (opakowane w obietnicę)
   */
  const uploadFileWithTus = async (blob: Blob): Promise<string> => {
    const tus = await import('tus-js-client')
    return new Promise((resolve, reject) => {
      const file = new File([blob], `story-${Date.now()}.png`, { type: blob.type })
      const upload = new tus.Upload(file, {
        endpoint: '/files/',
        retryDelays: [0, 3000, 5000, 10000],
        chunkSize: 2 * 1024 * 1024,
        metadata: {
          filename: file.name,
          filetype: file.type,
        },
        onError: (error) => {
          console.error('Tus story upload error:', error)
          reject(error)
        },
        onSuccess: () => {
          const serverPath = normalizeTusFilePath(upload.url || '')
          resolve(serverPath)
        },
      })
      upload.start()
    })
  }

  const upsertStoryInStore = (authorId: string, newStory: any) => {
    const createdAt = newStory.createdAt ? Date.parse(newStory.createdAt) || Date.now() : Date.now()
    const expiresAt = newStory.expiresAt
      ? Date.parse(newStory.expiresAt) || createdAt + 24 * 60 * 60 * 1000
      : createdAt + 24 * 60 * 60 * 1000

    const meta = parseStoryMetadata(newStory.text)

    const storyItem = {
      id: newStory.id,
      userId: String(authorId),
      type: ((newStory.mediaType || 'IMAGE').toLowerCase() === 'video' ? 'video' : 'image') as
        | 'image'
        | 'video',
      imageUrl: newStory.mediaUrl,
      thumbnailUrl: newStory.thumbMediaUrl || undefined,
      createdAt,
      expiresAt,
      viewCount: 0,
      interactions: [],
      elements: [],
      sharedPostInfo: meta?.sharedPostInfo || undefined,
      sharedLinkInfo: meta?.sharedLinkInfo || undefined,
      userTags: meta?.userTags || undefined,
    }

    const existingUserStoriesIndex = userStories.value.findIndex(
      (us) => String(us.userId) === String(authorId)
    )

    if (existingUserStoriesIndex !== -1) {
      userStories.value[existingUserStoriesIndex].stories.push(storyItem)
      userStories.value[existingUserStoriesIndex].hasUnviewedStories = true
    } else {
      const user = authStore.currentUser
      userStories.value.unshift({
        userId: String(authorId),
        userName: user?.name || 'Ty',
        userAvatar: user?.avatar || '/default-avatar.png',
        stories: [storyItem],
        hasUnviewedStories: false,
      })
    }
  }

  const addStory = async (
    authorId: string,
    storyData: {
      imageUrl?: string
      text?: string
      sharedPostInfo?: StoryMetadata['sharedPostInfo']
      sharedLinkInfo?: StoryMetadata['sharedLinkInfo']
      userTags?: StoryMetadata['userTags']
    },
  ) => {
    try {
      let mediaUrl = storyData.imageUrl || ''

      // Jeśli obraz jest przekazany jako Base64 / DataURL, wysyłamy go na serwer przez Tus
      if (mediaUrl.startsWith('data:')) {
        const blob = await dataURLtoBlob(mediaUrl)
        mediaUrl = await uploadFileWithTus(blob)
      }

      const mediaType = mediaUrl ? 'IMAGE' : 'TEXT'

      const hasMeta =
        !!storyData.sharedPostInfo ||
        !!storyData.sharedLinkInfo ||
        (storyData.userTags && storyData.userTags.length > 0)

      const textPayload = hasMeta
        ? encodeStoryMetadata({
            v: 1,
            sharedPostInfo: storyData.sharedPostInfo || null,
            sharedLinkInfo: storyData.sharedLinkInfo || null,
            userTags: storyData.userTags || [],
          })
        : storyData.text || ''

      const result = await createStoryMutate({
        input: {
          authorId,
          mediaUrl,
          mediaType,
          text: textPayload,
        },
      })

      const createdStory = result?.data?.createStory
      if (createdStory) {
        upsertStoryInStore(authorId, {
          ...createdStory,
          // Keep local metadata even if server echoes text differently
          text: textPayload || createdStory.text,
        })
      }

      return createdStory
    } catch (e) {
      console.error('Failed in addStory:', e)
      throw e
    }
  }

  return {
    userStories,
    allUserStories,
    currentUserId,
    getUserStories,
    addStory,
  }
})

// ---------------------------------------------------------------------------
// REELS STORE
// ---------------------------------------------------------------------------
export const useReelsStore = defineStore('reels', () => {
  const authStore = useAuthStore()
  const postsStore = usePostsStore()
  const reels = computed(() => processPostsIntoReels(postsStore.posts, String(authStore.currentUserId)))
  const getReelById = (id: string) => reels.value.find((r) => r.id === id)

  return {
    reels,
    getReelById,
  }
})
