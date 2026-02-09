import type { Component } from 'vue';
import type { ImageTagType } from './ImageTag';

export interface PostLocation {
  title: string;
  subtitle: string;
  type: 'city' | 'district' | 'attraction' | 'park' | 'current'| 'place';
  lat: string | null;
  lon: string | null;
  searchbox_id?: string;
}

export interface LinkPreview {
  url: string;
  title: string;
  description: string;
  image?: string;
  domain: string;
}

export interface Comment {
  id: number;
  authorId: number;
  content: string;
  date?: string;
  timestamp?: number;
  likesCount: number;
  // Nested replies structure remains specific to comments
  replies?: Comment[];
  image?: string;
  gif?: string;
  userReaction?: string;
  reactions?: Partial<Record<ReactionType, number[]>>;
  linkPreview?: LinkPreview;
}

export interface PostStats {
  reactions: number
  comments: number;
  shares: number;
}

export interface PostMedia {
  src: string
  altText?: string
  tags?: ImageTagType[]
  audioUrl?: string
}

export interface Poll {
  question: string;
  options: {
    id: string; // Unique ID for each option
    text: string;
    votes: string[]; // Array of user IDs who voted for this option
  }[];
}

export interface PostContext {
  taggedUsersIds?: number[];
  location?: PostLocation;
  privacy: string;
  feeling?: {
    emoji: string;
    label: string;
  } | null;
  activity?: {
    parent: string;
    item: {
      label: string;
      icon: Component;
      color: string;
    }
  } | null;
  createdEvent?: boolean;
  detectedLanguage?: string;
}

export type SharedContentType = 'post' | 'reel' | 'event'|'marketplace';

export interface SharedContent {
  type: SharedContentType;
  originalId: string;

}

export type ReactionType = 'like' | 'love' | 'haha' | 'wow' | 'sad' | 'angry';

export interface Post {
  id: string;
  authorId: number;
  groupId?: string;
  targetId?: string;
  targetType?: 'User' | 'Group' | 'Event';
  content: string;
  date: string;
  timestamp: number;
  media: PostMedia[];
  context: PostContext;
  reactions: Partial<Record<ReactionType, number[]>>;
  stats: PostStats;
  sharedContent?: SharedContent;
  comments?: Comment[];
  linkPreview?: LinkPreview;
  isAnonymous?: boolean;
  poll?: Poll;
}
