import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { PostData } from '@/types/StoryElement'

export const useStoryShareStore = defineStore('storyShare', () => {
  const pendingPost = ref<PostData | null>(null)

  const setPostToShare = (post: PostData) => {
    pendingPost.value = post
  }

  const getPendingPost = () => {
    const post = pendingPost.value
    pendingPost.value = null // Clear after getting
    return post
  }

  const clearPendingPost = () => {
    pendingPost.value = null
  }

  return {
    pendingPost,
    setPostToShare,
    getPendingPost,
    clearPendingPost,
  }
})
