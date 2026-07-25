import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { processPostsIntoReels } from '@/utils/reels'
import { useAuthStore } from '@/stores/auth'

export const usePostsStore = defineStore('posts', () => {
  const authStore = useAuthStore()
  const posts = ref<any[]>([])

  const currentUser = {
    id: computed(() => authStore.currentUserId),
    name: 'Bartosz Miazek',
    avatar: 'https://scontent-waw2-1.xx.fbcdn.net/v/t39.30808-1/295055057_582985040112298_215415809791370036_n.jpg'
  }

  const getPostById = (id: string | number) => posts.value.find((p) => String(p.id) === String(id))
  
  const addPost = (post: any) => {
    posts.value.unshift(post)
  }

  const removePost = (id: string) => {
    posts.value = posts.value.filter((p) => String(p.id) !== String(id))
  }

  return {
    posts,
    currentUser,
    getPostById,
    addPost,
    removePost,
  }
})

export const useStoriesStore = defineStore('stories', () => {
  const userStories = ref<any[]>([])
  const allUserStories = computed(() => userStories.value)
  const getUserStories = (userId: string) => {
    return userStories.value.find((us) => String(us.userId) === String(userId)) || null
  }
  return {
    userStories,
    allUserStories,
    getUserStories,
  }
})

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
