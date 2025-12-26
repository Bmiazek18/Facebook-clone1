// --- Element Types ---
export type ElementType = 'text' | 'image' | 'link' | 'post' | 'reel'

import type { ImageTagType } from './ImageTag';

// --- Shared Data Types ---
export interface PostData {
  id: string;
  authorName: string;
  authorAvatar: string;
  content: string;
  imageUrl?: string;
  images?: {
    src: string;
    altText?: string;
    tags?: ImageTagType[];
  }[];
  videoUrl?: string;
  timestamp: number;
}

export interface ReelData {
  id: string;
  authorName: string;
  authorAvatar: string;
  videoSrc: string;
  poster?: string;
  caption: string;
}

// --- Base Element Interface ---
export interface BaseStoryElement {
  id: string;
  type: ElementType;
  content: string;
  x: number;
  y: number;
  width?: number;
  height?: number;
  rotation: number;
  scale: number;
  styles?: Record<string, string>;
}

// --- Text Element ---
export interface TextElement extends BaseStoryElement {
  type: 'text';
}

// --- Image Element ---
export interface ImageElement extends BaseStoryElement {
  type: 'image';
  cropX?: number;
  cropY?: number;
  cropZoom?: number;
  altText?: string;
  // Music overlay (optional)
  musicTitle?: string;
  musicArtist?: string;
  musicStyle?: 'large' | 'small' | 'text' | 'icon';
}

// --- Link Element ---
export type LinkStyle = 'default' | 'minimal' | 'button';

export interface LinkElement extends BaseStoryElement {
  type: 'link';
  linkUrl?: string;
  linkTitle?: string;
  linkStyle?: LinkStyle;
}

// --- Post Element ---
export interface PostElement extends BaseStoryElement {
  type: 'post';
  postData?: PostData;
}

// --- Reel Element ---
export interface ReelElement extends BaseStoryElement {
  type: 'reel';
  reelData?: ReelData;
}

// --- Union Type for all elements ---
export type StoryElement = TextElement | ImageElement | LinkElement | PostElement | ReelElement;

// --- Background Settings ---
export interface BackgroundSettings {
  type: 'gradient' | 'image';
  value: string;
}
