import { ref } from 'vue'
import { usersApi } from '@/api/users'

export interface UserMediaItem {
  id: string
  userId: string
  postId: string
  mediaUrl: string
  mediaType: string
  albumName?: string
  altText?: string
  createdAt: string
  timestamp: number
}

export interface UserAlbum {
  name: string
  count: number
  coverUrl?: string
}

export function useUserMedia() {
  const mediaItems = ref<UserMediaItem[]>([])
  const albums = ref<UserAlbum[]>([])
  const loading = ref(false)
  const hasMore = ref(false)
  const totalCount = ref(0)
  const offset = ref(0)
  const currentFilter = ref('ALL')
  const currentAlbum = ref('')

  async function fetchMedia(userId: string, filter = 'ALL', albumName = '', reset = true, limit = 24) {
    if (reset) {
      offset.value = 0
      mediaItems.value = []
    }
    currentFilter.value = filter
    currentAlbum.value = albumName
    loading.value = true

    try {
      const res = await usersApi.getUserMedia(userId, filter, albumName, limit, offset.value)
      if (res) {
        if (reset) {
          mediaItems.value = res.items || []
        } else {
          mediaItems.value.push(...(res.items || []))
        }
        totalCount.value = res.totalCount || 0
        hasMore.value = res.hasMore || false
        offset.value += (res.items || []).length
      }
    } catch (e) {
      console.error('Failed to fetch user media:', e)
    } finally {
      loading.value = false
    }
  }

  async function loadMore(userId: string, limit = 24) {
    if (loading.value || !hasMore.value) return
    await fetchMedia(userId, currentFilter.value, currentAlbum.value, false, limit)
  }

  async function fetchAlbums(userId: string) {
    try {
      albums.value = await usersApi.getUserAlbums(userId)
    } catch (e) {
      console.error('Failed to fetch user albums:', e)
    }
  }

  return {
    mediaItems,
    albums,
    loading,
    hasMore,
    totalCount,
    fetchMedia,
    loadMore,
    fetchAlbums,
  }
}
