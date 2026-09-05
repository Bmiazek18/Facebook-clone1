import { usePostsStore } from '@/stores/posts'

export const useReelsStore = () => {
  const postsStore = usePostsStore()
  return {
    reels: postsStore.reels,
    getReelById: postsStore.getReelById,
  }
}
