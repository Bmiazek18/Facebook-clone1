import { ref } from 'vue'
import { gql } from 'graphql-tag'
import { getApolloClient } from '@/utils/apollo'

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

const GET_USER_MEDIA_QUERY = gql`
  query GetUserMedia($userId: ID!, $filter: String, $albumName: String, $limit: Int, $offset: Int) {
    getUserMedia(userId: $userId, filter: $filter, albumName: $albumName, limit: $limit, offset: $offset) {
      items {
        id
        userId
        postId
        mediaUrl
        mediaType
        albumName
        altText
        createdAt
        timestamp
      }
      totalCount
      hasMore
    }
  }
`

const GET_USER_ALBUMS_QUERY = gql`
  query GetUserAlbums($userId: ID!) {
    getUserAlbums(userId: $userId) {
      name
      count
      coverUrl
    }
  }
`

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
      const client = getApolloClient()
      const { data } = await client.query({
        query: GET_USER_MEDIA_QUERY,
        variables: {
          userId: String(userId),
          filter: filter,
          albumName: albumName || null,
          limit: limit,
          offset: offset.value,
        },
        fetchPolicy: 'network-only',
      })

      if (data?.getUserMedia) {
        const res = data.getUserMedia
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
      const client = getApolloClient()
      const { data } = await client.query({
        query: GET_USER_ALBUMS_QUERY,
        variables: { userId: String(userId) },
        fetchPolicy: 'network-only',
      })
      if (data?.getUserAlbums) {
        albums.value = data.getUserAlbums
      }
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
