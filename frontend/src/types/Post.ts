import type { Component } from 'vue'
import type { Person } from './Person'

export type ImageTagType = {
  id: string
  x: number
  y: number
  /** Tagged user id (UUID). Prefer this over user.id when persisting. */
  userId?: string
  name?: string
  user?: Person
  isTemp?: boolean
}

export interface PostLocation {
  title: string
  subtitle: string
  type: 'city' | 'district' | 'attraction' | 'park' | 'current' | 'place'
  lat: string | null
  lon: string | null
  searchbox_id?: string
}

export interface LinkPreview {
  url: string
  title: string
  description: string
  image?: string
  domain: string
}

export interface Comment {
  id: number
  authorId: string | number
  author?: PostAuthor | null
  content: string
  date?: string
  timestamp?: number
  likesCount: number
  // Nested replies structure remains specific to comments
  replies?: Comment[]
  image?: string
  gif?: string
  userReaction?: string
  reactions?: Partial<Record<ReactionType, number[]>>
  linkPreview?: LinkPreview
}

export interface PostStats {
  reactions: number
  comments: number
  shares: number
}

export interface PostMedia {
  src: string
  altText?: string
  tags?: ImageTagType[]
  audioUrl?: string
  backgroundColor?: string
}

/** Dane autora zwracane razem z postem przez API. */
export interface PostAuthor {
  id: string | number
  firstName?: string | null
  lastName?: string | null
  avatarId?: string | null
}

export interface Poll {
  question: string
  options: {
    id: string // Unique ID for each option
    text: string
    votes: string[] // Array of user IDs who voted for this option
  }[]
}

export interface PostContext {
  taggedUsersIds?: number[]
  location?: PostLocation
  privacy: string
  feeling?: {
    emoji: string
    label: string
  } | null
  activity?: {
    parent: string
    item: {
      label: string
      icon: Component
      color: string
    }
  } | null
  createdEvent?: boolean
  detectedLanguage?: string
}

export type SharedContentType = 'post' | 'reel' | 'event' | 'marketplace'

export interface SharedContent {
  type: SharedContentType
  originalId: string
  media?: PostMedia[]
}

export type ReactionType = 'like' | 'love' | 'haha' | 'wow' | 'sad' | 'angry'

export interface Post {
  id: string
  authorId: string | number
  author?: PostAuthor | null
  groupId?: string
  targetId?: string
  targetType?: 'User' | 'Group' | 'Event' | 'event'
  content: string
  date: string
  timestamp: number
  media: PostMedia[]
  context?: PostContext
  reactions: Partial<Record<ReactionType, number[]>>
  stats: PostStats
  sharedContent?: SharedContent
  comments?: Comment[]
  linkPreview?: LinkPreview
  isAnonymous?: boolean
  visibility?: string
  commentCount?: number
  shareCount?: number
  status?: string
  scheduledPublishTime?: number
  authorGroupRole?: string
  allowedUserIds?: Array<string | number>
  poll?: Poll
  isBirthday?: boolean
  isLifeEvent?: boolean
  lifeEventCategory?: string
}
