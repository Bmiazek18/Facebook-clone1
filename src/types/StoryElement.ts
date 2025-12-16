export type ElementType = 'text' | 'image' | 'link' | 'post'

export interface PostData {
  id: string;
  authorName: string;
  authorAvatar: string;
  content: string;
  imageUrl?: string;
  timestamp: number;
}

export interface StoryElement {
  id: string;
  type: ElementType;
  content: string;
  x: number;
  y: number;
  width?: number;
  height?: number;
  rotation: number;
  scale: number;
  cropX?: number;
  cropY?: number;
  cropZoom?: number;
  styles?: Record<string, string>;
  altText?: string;
  musicTitle?: string;
  musicArtist?: string;
  musicStyle?: 'large' | 'small' | 'text' | 'icon';
  // Link sticker properties
  linkUrl?: string;
  linkTitle?: string;
  linkStyle?: 'default' | 'minimal' | 'button';
  // Shared post properties
  postData?: PostData;
}

export interface BackgroundSettings {
  type: 'gradient' | 'image';
  value: string;
}
