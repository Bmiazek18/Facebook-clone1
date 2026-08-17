import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { PostData, ReelData } from '@/types/StoryElement'

export const useStoryShareStore = defineStore('storyShare', () => {
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
    pendingPost.value = null // Clear after getting
    return post
  }

  const getPendingReel = () => {
    const reel = pendingReel.value
    pendingReel.value = null // Clear after getting
    return reel
  }

  const clearPendingPost = () => {
    pendingPost.value = null
  }

  const clearPendingReel = () => {
    pendingReel.value = null
  }

  return {
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
