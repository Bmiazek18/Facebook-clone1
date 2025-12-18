export interface User {
  id: number
  name: string
  avatar: string
  bio: string
  location: string
  website: string
  joinDate: string
  followersCount: number
  followingCount: number
  friendsCount: number
  postsCount: number
  cover: string
  status: 'online' | 'offline'
  job?: string
  company?: string
  education?: string
  school?: string
  mutualFriendsCount?: number
  phone?: string
  email?: string
  gender?: string
  birthDate?: string
  languages?: string
  relationshipStatus?: string
  relationshipSince?: string
  partnerName?: string
  partnerAvatar?: string
  hometown?: string
  familyMembers?: { name: string; relationship: string }[]
  bioDetails?: string
  namePronounciation?: string
  otherNames?: string[]
  favoriteQuotes?: string[]
  lifeEvents?: { date: string; event: string }[]
}

import usersData from './users.json'

export const users: User[] = (usersData as { users: User[] }).users

export const getUserById = (id: number): User | undefined => {
  return users.find(user => user.id === id)
}

export const getUserByName = (name: string): User | undefined => {
  return users.find(user => user.name === name)
}

export const getAllUsers = (): User[] => {
  return [...users]
}
