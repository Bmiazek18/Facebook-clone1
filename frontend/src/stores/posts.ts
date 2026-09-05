import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { feedApi } from '@/api/feed'
import { processPostsIntoReels } from '@/utils/reels'

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

  const voteOnPoll = async (postId: string, optionId: string, userId: string) => {
    try {
      const updatedPost = await feedApi.voteOnPoll(postId, optionId, userId)
      if (updatedPost) {
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

  const reels = computed(() => processPostsIntoReels(posts.value, String(authStore.currentUserId)))
  const getReelById = (id: string) => reels.value.find((r) => r.id === id)

  return {
    posts,
    reels,
    currentUser,
    getPostById,
    getReelById,
    addPost,
    removePost,
    voteOnPoll,
  }
})
