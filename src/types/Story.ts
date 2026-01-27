import type { StoryElement } from './StoryElement';

export type ReactionType = 'like' | 'love' | 'haha' | 'wow' | 'sad' | 'angry';


export interface Story {
  id: string | number;
  backgroundImageUrl: string;
  profileImageUrl: string;
  title: string;
}

export interface StoryInteraction {
  userId: string;
  reaction: ReactionType | null;
}

export interface StoryItem {
  id: string;
  userId: string;
  type: 'image' | 'text' | 'video' | 'birthday';
  imageUrl?: string;
  backgroundColor?: string;
  backgroundGradient?: string;
  musicUrl?: string;
  elements?: StoryElement[];
  sharedPostInfo?: {
    postId: string;
    x: number;
    y: number;
    width: number;
    height: number;
  };
  sharedLinkInfo?: {
    url: string;
    x: number;
    y: number;
    width: number;
    height: number;
  };
  createdAt: number;
  expiresAt: number;
  viewCount?: number;
  interactions?: StoryInteraction[];
  originalUserName?: string;
  originalUserAvatar?: string;
}

export interface UserStories {
  userId: string;
  userName: string;
  userAvatar: string;
  stories: StoryItem[];
  hasUnviewedStories: boolean;
}
