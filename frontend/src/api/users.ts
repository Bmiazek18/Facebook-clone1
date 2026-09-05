import { apiClient } from './client'
import gql from 'graphql-tag'
import { GET_FRIENDS, GET_FRIEND_SUGGESTIONS } from '@/graphql/friends'
import { GET_SEARCH_HISTORY, SEARCH_USERS, RECORD_SEARCH, DELETE_SEARCH_HISTORY_ITEM } from '@/graphql/search'

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

export const usersApi = {
  async searchUsers(query: string, currentUserId: string | number) {
    const data = await apiClient.query<{ searchUsers: any[] }>(
      SEARCH_USERS,
      { query, currentUserId: String(currentUserId) },
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

  async getFriendSuggestions(currentUserId: string | number) {
    const data = await apiClient.query<{ getFriendSuggestions: any[] }>(
      GET_FRIEND_SUGGESTIONS,
      { currentUserId: String(currentUserId) },
      { fetchPolicy: 'network-only' }
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
        albumName: albumName || null,
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

  async createProfilePhotoPost(input: any) {
    const data = await apiClient.mutate<{ createPost: any }>(
      CREATE_PROFILE_PHOTO_POST_MUTATION,
      { input }
    )
    return data?.createPost || null
  },

  async getUserById(userId: string | number) {
    const cleanId = String(userId).replace('user_', '')
    const data = await apiClient.query<{ getUserById: any }>(
      gql`
        query GetUserById($userId: ID!) {
          getUserById(userId: $userId) {
            id
            firstName
            lastName
            avatar
            note
          }
        }
      `,
      { userId: cleanId },
      { errorPolicy: 'ignore' }
    )
    return data?.getUserById || null
  },

  async getUserProfile(userId: string | number) {
    const cleanId = String(userId).replace('user_', '')
    const data = await apiClient.query<{ getUserById: any }>(
      gql`
        query GetUserProfile($userId: ID!) {
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
      `,
      { userId: cleanId },
      { fetchPolicy: 'cache-first' }
    )
    return data?.getUserById || null
  },

  async getProfileFriends(userId: string | number, currentUserId?: string | number) {
    const data = await apiClient.query<{ getFriends: any[] }>(
      gql`
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
      `,
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
      gql`
        mutation SendFriendRequest($senderId: ID!, $receiverId: ID!) {
          sendFriendRequest(senderId: $senderId, receiverId: $receiverId) {
            success
            message
          }
        }
      `,
      {
        senderId: String(senderId),
        receiverId: String(receiverId)
      }
    )
    return data?.sendFriendRequest
  },

  async getActiveStatuses(userIds: (string | number)[]) {
    const data = await apiClient.query<{ getActiveStatuses: any[] }>(
      gql`
        query GetActiveStatuses($userIds: [ID!]!) {
          getActiveStatuses(userIds: $userIds) {
            userId
            active
            lastActiveText
          }
        }
      `,
      { userIds: userIds.map((id) => String(id)) },
      { fetchPolicy: 'network-only' }
    )
    return data?.getActiveStatuses || []
  },

  async updateProfile(userId: string | number, input: any) {
    const data = await apiClient.mutate<{ updateProfile: any }>(
      gql`
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
      `,
      {
        userId: String(userId),
        input
      }
    )
    return data?.updateProfile || null
  },

  async getFriendRequests(currentUserId: string | number) {
    const data = await apiClient.query<{ getFriendRequests: any[] }>(
      gql`
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
      `,
      { currentUserId: String(currentUserId) },
      { fetchPolicy: 'network-only' }
    )
    return data?.getFriendRequests || []
  },

  async acceptFriendRequest(senderId: string | number, receiverId: string | number) {
    const data = await apiClient.mutate<{ acceptFriendRequest: { success: boolean; message?: string } }>(
      gql`
        mutation AcceptFriendRequest($senderId: ID!, $receiverId: ID!) {
          acceptFriendRequest(senderId: $senderId, receiverId: $receiverId) {
            success
            message
          }
        }
      `,
      {
        senderId: String(senderId),
        receiverId: String(receiverId)
      }
    )
    return data?.acceptFriendRequest
  }
}
