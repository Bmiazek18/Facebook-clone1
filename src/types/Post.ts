export interface Comment {
  id: number;
  authorName: string;
  authorAvatar: string;
  content: string;
  date: string; // np. 41 tyg.
}

export interface Post {
  id: number;
  authorName: string;
  authorAvatar: string;
  date: string; // np. 24 lutego
  group?: string;
  content: string; // Treść życzeń
  isLiked: boolean; // Czy ja to polubiłem
  reactionCount: number; // Liczba reakcji (np. 'Super')
  commentCount: number; // Liczba komentarzy
  comments: Comment[];
}
