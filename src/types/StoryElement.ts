export type ElementType = 'text' | 'image'

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
}

export interface BackgroundSettings {
  type: 'gradient' | 'image';
  value: string;
}
