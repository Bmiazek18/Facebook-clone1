import { defineStore } from 'pinia';
import { ref } from 'vue';
import reelsData from '@/data/reels.json';

// Updated interface: removed nested user object, added authorId and isFollowing
export interface Reel {
  id: string;
  videoSrc: string;
  poster: string;
  likes: string;
  comments: string;
  shares: string;
  caption: string;
  hashtags: string;
  music: string;
  authorId: number;
  isLiked: boolean;
  isFollowing?: boolean; // Interaction state
}

interface ReelFromJson {
  id: number;
  author: string;
  authorId: number;
  videoSrc: string;
  poster: string;
  caption: string;
  likes: number;
  commentsCount: number;
  sharesCount: number;
  hashtags: string;
  music: string;
}

export const useReelsStore = defineStore('reels', () => {
  // Initialize with isFollowing default to false if not present, though we might want to fetch this
  const reels = ref<Reel[]>(reelsData.map((r: ReelFromJson) => ({
    ...r,
    id: String(r.id),
    likes: String(r.likes),
    comments: String(r.commentsCount),
    shares: String(r.sharesCount),
    isFollowing: false,
    isLiked: false,
  })));

  // Get reel by ID
  const getReelById = (id: string) => {
    return reels.value.find(reel => reel.id === id);
  };

  // Get reel and next (for smooth animation)
  const getReelWithNext = (id: string) => {
    const currentIndex = reels.value.findIndex(reel => reel.id === id);
    if (currentIndex === -1) return { current: null, next: null };

    const current = reels.value[currentIndex];
    const next = currentIndex < reels.value.length - 1 ? reels.value[currentIndex + 1] : null;

    return { current, next };
  };

  // Get index of reel
  const getReelIndex = (id: string) => {
    return reels.value.findIndex(reel => reel.id === id);
  };

  // Toggle like
  const toggleLike = (id: string) => {
    const reel = reels.value.find(r => r.id === id);
    if (reel) {
      reel.isLiked = !reel.isLiked;
      // Handle 'k' notation roughly or just parse simple numbers
      let likesNum = parseInt(reel.likes.replace(/[^\d]/g, ''));
      if (isNaN(likesNum)) likesNum = 0;
      
      reel.likes = reel.isLiked
        ? (likesNum + 1).toString()
        : (likesNum > 0 ? (likesNum - 1).toString() : '0');
    }
  };

  // Toggle follow (by authorId)
  const toggleFollow = (authorId: number) => {
    // Update all reels by this author
    reels.value.filter(r => r.authorId === authorId).forEach(reel => {
        reel.isFollowing = !reel.isFollowing;
    });
  };

  return {
    reels,
    getReelById,
    getReelWithNext,
    getReelIndex,
    toggleLike,
    toggleFollow,
  };
});