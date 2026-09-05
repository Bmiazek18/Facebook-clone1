import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { feedApi } from '@/api/feed'
import type { PostData, ReelData } from '@/types/StoryElement'
import {
  encodeStoryMetadata,
  parseStoryMetadata,
  type StoryMetadata,
} from '@/utils/storyMetadata'

async function dataURLtoBlob(dataUrl: string): Promise<Blob> {
  const res = await fetch(dataUrl)
  return await res.blob()
}

export const useStoriesStore = defineStore('stories', () => {
  const authStore = useAuthStore()
  const userStories = ref<any[]>([])
  const allUserStories = computed(() => userStories.value)
  const currentUserId = computed(() => String(authStore.currentUserId || ''))

  // Story sharing state (for cross-page story creation)
  const pendingPost = ref<PostData | null>(null)
  const pendingReel = ref<ReelData | null>(null)

  const setPostToShare = (post: PostData) => {
    pendingPost.value = post
  }

  const setReelToShare = (reel: ReelData) => {
    pendingReel.value = reel
  }

  const getPendingPost = () => {
    const post = pendingPost.value
    pendingPost.value = null
    return post
  }

  const getPendingReel = () => {
    const reel = pendingReel.value
    pendingReel.value = null
    return reel
  }

  const clearPendingPost = () => {
    pendingPost.value = null
  }

  const clearPendingReel = () => {
    pendingReel.value = null
  }

  const getUserStories = (userId: string) => {
    return userStories.value.find((us) => String(us.userId) === String(userId)) || null
  }

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

      const createdStory = await feedApi.createStory({
        authorId,
        mediaUrl,
        mediaType,
        text: textPayload,
      })

      if (createdStory) {
        upsertStoryInStore(authorId, {
          ...createdStory,
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
    pendingPost,
    pendingReel,
    setPostToShare,
    setReelToShare,
    getPendingPost,
    getPendingReel,
    clearPendingPost,
    clearPendingReel,
  }
})
