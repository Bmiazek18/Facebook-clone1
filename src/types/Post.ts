import type { User } from '@/data/users';
import type { ImageTagType } from './ImageTag';
export interface PostLocation {
  id: string;
  name: string;
  description: string;
}

export interface Comment {
  id: number;
  authorName: string;
  authorAvatar: string;
  content: string;
  date: string;
  likesCount: number;
  isLiked?: boolean;
  userReaction?: string;
  image?: string;
  gif?: string;
  replies?: Comment[];
}

export interface Post {
  id: string
  content: string
  images: {
    src: string
    altText?: string
    tags?: ImageTagType[]
  }[]
  imageUrl?: string;
  videoUrl?: string
  authorName: string
  authorAvatar: string
  authorId: number
  date: string
  likesCount: number
  commentsCount: number
  sharesCount: number
  taggedUsers?: User[]
  location?: PostLocation
  gif?: string
  isLiked?: boolean
  likedType?: 'super' | 'like' | null
  reactionCount?: number
  commentCount?: number
  comments?: Comment[]
  selectedCardBgId?: number;
  privacy?: string;
  timestamp: number;
}
