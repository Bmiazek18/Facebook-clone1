import type { StoryElement } from './StoryElement';

export interface Story {
  id: string | number;
  backgroundImageUrl: string;
  profileImageUrl: string;
  title: string;
}

export interface StoryItem {
  id: string;
  userId: string;
  type: 'image' | 'text' | 'video';
  imageUrl?: string;
  backgroundColor?: string;
  backgroundGradient?: string;
  elements?: StoryElement[];
  createdAt: number;
  expiresAt: number;
  viewCount?: number;
  viewers?: string[];
}

export interface UserStories {
  userId: string;
  userName: string;
  userAvatar: string;
  stories: StoryItem[];
  hasUnviewedStories: boolean;
}
