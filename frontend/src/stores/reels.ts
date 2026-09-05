import { defineStore } from 'pinia'
import { computed } from 'vue'
import { processPostsIntoReels } from '@/utils/reels'
import { useAuthStore } from '@/stores/auth'
import { usePostsStore } from '@/stores/posts'

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
