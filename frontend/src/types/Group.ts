export interface Group {
  id: string
  name: string
  image: string
  description?: string
  members?: number
  privacy?: 'public' | 'private'
  images?: string[]
  lastActive?: string
  newPostsToday?: number
  newPostsMonth?: number
  newMembersWeek?: string
  createdAge?: string
}

export enum GroupRole {
  ADMIN = 'ADMIN',
  MODERATOR = 'MODERATOR',
  MEMBER = 'MEMBER',
  PENDING = 'PENDING'
}

export interface GroupMember {
  userId: string
  role: GroupRole
  joinedAt: string
  isFriend?: boolean
}
