import type { Comment } from './Post';

export interface Reel {

  id: string;

  authorId: number;

  videoSrc: string;

  poster: string;

  caption: string;

  likes: string;

  isLiked: boolean;

  commentsCount: number;

  sharesCount: number;

  music: string;

  comments?: Comment[];

}
