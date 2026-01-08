// Base interfaces with common properties

/**
 * Base interface for all timeline items
 */
export interface BaseTimelineItem {
  id: string;
  startTime: number;
  endTime: number;
}

/**
 * Base interface for items with position
 */
export interface BasePositionedItem extends BaseTimelineItem {
  position: { x: number; y: number };
}

/**
 * Base interface for items with animations
 */
export interface BaseAnimatedItem extends BasePositionedItem {
  entryAnimation?: string;
  entryDuration?: number;
  exitAnimation?: string;
  exitDuration?: number;
}

/**
 * Base interface for visual overlays (images, videos)
 */
export interface BaseVisualOverlay extends BaseAnimatedItem {
  width: number;
  height: number;
  rotation: number;
  opacity: number;
}

/**
 * Base interface for media items with URL
 */
export interface BaseMediaItem {
  id: string;
  url: string;
}

// Specific interfaces

/**
 * Video clip in the main timeline
 */
export interface VideoClip extends BaseMediaItem {
  duration: number;
  startTime: number;
  thumbnails: string[];
}

/**
 * Text overlay with styling and animations
 */
export interface TextOverlay extends BaseAnimatedItem {
  content: string;
  fontSize: number;
  color: string;
  fontWeight: string;
  loopAnimation?: string;
}

/**
 * Image overlay with transformations
 */
export interface ImageOverlay extends BaseVisualOverlay, BaseMediaItem {}

/**
 * Picture-in-Picture video overlay
 */
export interface PipVideoOverlay extends BaseVisualOverlay, BaseMediaItem {
  volume: number;
}

/**
 * Animation state for rendering
 */
export interface AnimationState {
  opacity: number;
  scale: number;
  translateX: number;
  translateY: number;
}

/**
 * Animation state for images (no translateY)
 */
export interface ImageAnimationState {
  opacity: number;
  scale: number;
  translateX: number;
}

/**
 * Animation types
 */
export type EntryAnimationType =
  | 'none'
  | 'fade-in'
  | 'zoom-in'
  | 'pop-in'
  | 'slide-in-left'
  | 'slide-in-right'
  | 'slide-in-top'
  | 'slide-in-bottom'
  | 'typewriter';

export type ExitAnimationType =
  | 'none'
  | 'fade-out'
  | 'zoom-out'
  | 'pop-out'
  | 'slide-out-left'
  | 'slide-out-right'
  | 'slide-out-top'
  | 'slide-out-bottom';

export type LoopAnimationType =
  | 'none'
  | 'pulse'
  | 'float'
  | 'shake';
