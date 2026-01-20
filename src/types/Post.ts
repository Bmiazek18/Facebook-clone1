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

export interface Comment {
  id: number;
  authorId: number;
  content: string;
  date: string;
  likesCount: number;
  // Nested replies structure remains specific to comments
  replies?: Comment[];
  image?: string;
  gif?: string;
  userReaction?: string;
  reactions?: Partial<Record<ReactionType, number[]>>;
}

export interface PostStats {
  comments: number;
  shares: number;
}

// Grouping all media assets
export interface PostMedia {
  images?: {
    src: string;
    altText?: string;
    tags?: ImageTagType[];
  }[];
  videoUrl?: string;
  gif?: string;
}


export interface PostContext {
  taggedUsersIds?: number[]; // Changed to IDs
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
}

export type SharedContentType = 'post' | 'reel' | 'event';

export interface SharedContent {
  type: SharedContentType;
  originalId: string;

}

export type ReactionType = 'like' | 'love' | 'haha' | 'wow' | 'sad' | 'angry';

export interface Post {
  id: string;
  authorId: number;
  content: string;
  date: string;
  timestamp: number;
  media: PostMedia;
  context: PostContext;
  reactions: Partial<Record<ReactionType, number[]>>;
  stats: PostStats;
  sharedContent?: SharedContent;
  comments?: Comment[];
  selectedCardBgId?: number;
  detectedLanguage?: string;
}
