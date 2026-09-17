import { apiClient } from './client'
import gql from 'graphql-tag'
import { GET_FRIENDS, GET_FRIEND_SUGGESTIONS } from '@/graphql/friends'
import { GET_SEARCH_HISTORY, SEARCH_USERS, RECORD_SEARCH, DELETE_SEARCH_HISTORY_ITEM } from '@/graphql/search'

export { GET_FRIENDS, GET_FRIEND_SUGGESTIONS }

export const GENERATE_TICKET_MUTATION = gql`
  mutation GenerateTicket($userId: ID!) {
    generateTicket(userId: $userId)
  }
`

export const GET_USER_MEDIA_QUERY = gql`
  query GetUserMedia($userId: ID!, $filter: String, $albumName: String, $limit: Int, $offset: Int) {
    getUserMedia(userId: $userId, filter: $filter, albumName: $albumName, limit: $limit, offset: $offset) {
      items {
        id
        src
        altText
        mediaType
        createdAt
        postId
        tags {
          id
          x
          y
          userId
          user {
            id
            firstName
            lastName
          }
        }
      }
      totalCount
      hasMore
    }
  }
`

export const GET_USER_ALBUMS_QUERY = gql`
  query GetUserAlbums($userId: ID!) {
    getUserAlbums(userId: $userId) {
      id
      name
      coverSrc
      count
    }
  }
`

export const CREATE_PROFILE_PHOTO_POST_MUTATION = gql`
  mutation CreateProfilePhotoPost($input: CreatePostInput!) {
    createPost(input: $input) {
      id
      authorId
      content
      date
      timestamp
      commentCount
      shareCount
      media {
        src
        altText
        backgroundColor
      }
      reactions {
        reactionType
        userIds
      }
    }
  }
`

export const GET_USER_BY_ID_SIMPLE_QUERY = gql`
  query GetUserByIdSimple($userId: ID!) {
    getUserById(userId: $userId) {
      id
      firstName
      lastName
      avatar
      note
    }
  }
`

export const GET_USER_FULL_PROFILE_QUERY = gql`
  query GetUserProfileFull($userId: ID!) {
    getUserById(userId: $userId) {
      id
      firstName
      lastName
      avatarId
      avatar
      coverId
      cover
      city
      location
      hometown
      education
      school
      bio
      gender
      birthDate
      languages
      pronouns
      highSchool
      job
      company
      work
      phone
      website
      relationshipStatus
      relationshipSince
      partnerName
      partnerAvatar
      bioDetails
      namePronunciation
      otherNames
      favoriteQuotes
      createdAt
      updatedAt
      note
    }
  }
`

export const GET_PROFILE_FRIENDS_QUERY = gql`
  query ProfileFriends($userId: ID!, $currentUserId: ID!) {
    getFriends(userId: $userId) {
      id
      firstName
      lastName
      avatar
      avatarId
      mutualFriendsCount(currentUserId: $currentUserId)
    }
  }
`

export const SEND_FRIEND_REQUEST_MUTATION = gql`
  mutation SendFriendRequest($senderId: ID!, $receiverId: ID!) {
    sendFriendRequest(senderId: $senderId, receiverId: $receiverId) {
      success
      message
    }
  }
`

export const GET_ACTIVE_STATUSES_QUERY = gql`
  query GetActiveStatuses($userIds: [ID!]!) {
    getActiveStatuses(userIds: $userIds) {
      userId
      active
      lastActiveText
    }
  }
`

export const UPDATE_PROFILE_MUTATION = gql`
  mutation UpdateProfile($userId: ID!, $input: UpdateProfileInput!) {
    updateProfile(userId: $userId, input: $input) {
      id
      firstName
      lastName
      avatar
      cover
      city
      location
      hometown
      education
      school
      bio
      gender
      birthDate
      languages
      pronouns
      highSchool
      job
      company
      work
      phone
      website
      relationshipStatus
      relationshipSince
      partnerName
      note
    }
  }
`

export const GET_FRIEND_REQUESTS_QUERY = gql`
  query GetFriendRequests($currentUserId: ID!) {
    getFriendRequests(currentUserId: $currentUserId) {
      userId
      mutualFriendsCount
      user {
        id
        firstName
        lastName
        avatarId
        avatar
      }
    }
  }
`

export const ACCEPT_FRIEND_REQUEST_MUTATION = gql`
  mutation AcceptFriendRequest($senderId: ID!, $receiverId: ID!) {
    acceptFriendRequest(senderId: $senderId, receiverId: $receiverId) {
      success
      message
    }
  }
`

export const usersApi = {
  async searchUsers(query: string, currentUserId?: string | number) {
    const data = await apiClient.query<{ searchUsers: any[] }>(
      SEARCH_USERS,
      { query, currentUserId: currentUserId ? String(currentUserId) : undefined },
      { fetchPolicy: 'network-only' }
    )
    return data?.searchUsers || []
  },

  async getSearchHistory(userId?: string | number) {
    const data = await apiClient.query<{ getSearchHistory: any[] }>(
      GET_SEARCH_HISTORY,
      { userId: userId ? String(userId) : undefined },
      { fetchPolicy: 'network-only' }
    )
    return data?.getSearchHistory || []
  },

  async recordSearch(searchedUserId: string | number, searchingUserId?: string | number) {
    const data = await apiClient.mutate<{ recordSearch: boolean }>(
      RECORD_SEARCH,
      {
        searchedUserId: String(searchedUserId),
        searchingUserId: searchingUserId ? String(searchingUserId) : undefined
      }
    )
    return data?.recordSearch
  },

  async deleteSearchHistoryItem(searchedUserId: string | number, searchingUserId?: string | number) {
    const data = await apiClient.mutate<{ deleteSearchHistoryItem: boolean }>(
      DELETE_SEARCH_HISTORY_ITEM,
      {
        searchedUserId: String(searchedUserId),
        searchingUserId: searchingUserId ? String(searchingUserId) : undefined
      }
    )
    return data?.deleteSearchHistoryItem
  },

  async getFriends(userId: string | number, filterType?: string, limit?: number, offset?: number) {
    const data = await apiClient.query<{ getFriends: any[] }>(
      GET_FRIENDS,
      {
        userId: String(userId),
        filterType: filterType || undefined,
        limit: limit || undefined,
        offset: offset || undefined
      },
      { fetchPolicy: 'network-only' }
    )
    return data?.getFriends || []
  },

  async getFriendSuggestions(currentUserId: string | number, fetchPolicy: 'cache-first' | 'network-only' | 'no-cache' = 'cache-first') {
    const data = await apiClient.query<{ getFriendSuggestions: any[] }>(
      GET_FRIEND_SUGGESTIONS,
      { currentUserId: String(currentUserId) },
      { fetchPolicy }
    )
    return data?.getFriendSuggestions || []
  },

  async generateTicket(userId: string | number): Promise<string> {
    const data = await apiClient.mutate<{ generateTicket: string }>(
      GENERATE_TICKET_MUTATION,
      { userId: String(userId) }
    )
    if (!data?.generateTicket) {
      throw new Error('Failed to generate ticket')
    }
    return data.generateTicket
  },

  async getUserMedia(
    userId: string | number,
    filter = 'ALL',
    albumName = '',
    limit = 24,
    offset = 0
  ) {
    const data = await apiClient.query<{ getUserMedia: any }>(
      GET_USER_MEDIA_QUERY,
      {
        userId: String(userId),
        filter,
        albumName,
        limit,
        offset
      },
      { fetchPolicy: 'network-only' }
    )
    return data?.getUserMedia || { items: [], totalCount: 0, hasMore: false }
  },

  async getUserAlbums(userId: string | number) {
    const data = await apiClient.query<{ getUserAlbums: any[] }>(
      GET_USER_ALBUMS_QUERY,
      { userId: String(userId) },
      { fetchPolicy: 'network-only' }
    )
    return data?.getUserAlbums || []
  },

  async createProfilePhotoPost(input: {
    authorId: string | number
    content?: string
    media?: Array<{ src: string; altText?: string; backgroundColor?: string }>
  }) {
    const data = await apiClient.mutate<{ createPost: any }>(
      CREATE_PROFILE_PHOTO_POST_MUTATION,
      { input }
    )
    return data?.createPost || null
  },

  async getUserById(userId: string | number) {
    const cleanId = String(userId).replace('user_', '')
    const data = await apiClient.query<{ getUserById: any }>(
      GET_USER_BY_ID_SIMPLE_QUERY,
      { userId: cleanId },
      { errorPolicy: 'ignore' }
    )
    return data?.getUserById || null
  },

  async getUserProfile(userId: string | number) {
    const cleanId = String(userId).replace('user_', '')
    const data = await apiClient.query<{ getUserById: any }>(
      GET_USER_FULL_PROFILE_QUERY,
      { userId: cleanId },
      { fetchPolicy: 'cache-first' }
    )
    return data?.getUserById || null
  },

  async getProfileFriends(userId: string | number, currentUserId?: string | number) {
    const data = await apiClient.query<{ getFriends: any[] }>(
      GET_PROFILE_FRIENDS_QUERY,
      {
        userId: String(userId),
        currentUserId: currentUserId ? String(currentUserId) : '1'
      },
      { fetchPolicy: 'network-only' }
    )
    return data?.getFriends || []
  },

  async sendFriendRequest(senderId: string | number, receiverId: string | number) {
    const data = await apiClient.mutate<{ sendFriendRequest: { success: boolean; message?: string } }>(
      SEND_FRIEND_REQUEST_MUTATION,
      {
        senderId: String(senderId),
        receiverId: String(receiverId)
      }
    )
    return data?.sendFriendRequest
  },

  async getActiveStatuses(userIds: (string | number)[], fetchPolicy: 'cache-first' | 'network-only' | 'no-cache' = 'cache-first') {
    const validIds = (userIds || []).map((id) => String(id).trim()).filter((id) => id.length > 0)
    if (validIds.length === 0) return []
    const data = await apiClient.query<{ getActiveStatuses: any[] }>(
      GET_ACTIVE_STATUSES_QUERY,
      { userIds: validIds },
      { fetchPolicy }
    )
    return data?.getActiveStatuses || []
  },

  async updateProfile(userId: string | number, input: any) {
    const data = await apiClient.mutate<{ updateProfile: any }>(
      UPDATE_PROFILE_MUTATION,
      {
        userId: String(userId),
        input
      }
    )
    return data?.updateProfile || null
  },

  async getFriendRequests(currentUserId: string | number, fetchPolicy: 'cache-first' | 'network-only' | 'no-cache' = 'cache-first') {
    const data = await apiClient.query<{ getFriendRequests: any[] }>(
      GET_FRIEND_REQUESTS_QUERY,
      { currentUserId: String(currentUserId) },
      { fetchPolicy }
    )
    return data?.getFriendRequests || []
  },

  async acceptFriendRequest(senderId: string | number, receiverId: string | number) {
    const data = await apiClient.mutate<{ acceptFriendRequest: { success: boolean; message?: string } }>(
      ACCEPT_FRIEND_REQUEST_MUTATION,
      {
        senderId: String(senderId),
        receiverId: String(receiverId)
      }
    )
    return data?.acceptFriendRequest
  }
}
