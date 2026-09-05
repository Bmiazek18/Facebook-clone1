export interface User {
  id: string | number
  name: string
  firstName?: string
  lastName?: string
  avatar: string
  avatarId?: string | null
  cover?: string
  coverId?: string | null
  email?: string
  bio?: string
  location?: string
  city?: string
  hometown?: string
  website?: string
  phone?: string
  joinDate?: string
  createdAt?: string
  updatedAt?: string
  followersCount?: number
  followingCount?: number
  friendsCount?: number
  postsCount?: number
  status?: 'online' | 'offline'
  job?: string
  company?: string
  work?: string
  education?: string
  school?: string
  highSchool?: string
  gender?: string
  birthDate?: string
  languages?: string
  pronouns?: string
  relationshipStatus?: string
  relationshipSince?: string
  partnerName?: string
  partnerAvatar?: string
  bioDetails?: string
  namePronunciation?: string
  otherNames?: string
  favoriteQuotes?: string
  note?: string
  mutualFriendsCount?: number
  photos?: string[]
}

export interface UserSummary {
  id: string | number
  name: string
  firstName?: string
  lastName?: string
  avatar: string
  avatarId?: string | null
  mutualFriendsCount?: number
  inHistory?: boolean
  newPostsCount?: number
}

export interface UserSearchHistoryItem {
  id: string
  name: string
  avatar: string
  newPostsCount?: number
}

