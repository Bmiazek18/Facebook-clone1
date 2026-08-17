export interface User {
  id: string | number
  name: string
  avatar: string
  bio?: string
  location?: string
  website?: string
  joinDate?: string
  followersCount?: number
  followingCount?: number
  friendsCount?: number
  postsCount?: number
  cover?: string
  status?: 'online' | 'offline'
  job?: string
  company?: string
  education?: string
  school?: string
  mutualFriendsCount?: number
  photos?: string[]
}
