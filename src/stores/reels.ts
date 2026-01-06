import { defineStore } from 'pinia';
import { ref } from 'vue';
import reelsData from '@/data/reels.json';

export interface ReelUser {
  id: string;
  name: string;
  avatar: string;
  isFollowing: boolean;
}

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
  user: ReelUser;
  isLiked: boolean;
}

export const useReelsStore = defineStore('reels', () => {
  const reels = ref<Reel[]>(reelsData);

  // Pobierz reel po ID
  const getReelById = (id: string) => {
    return reels.value.find(reel => reel.id === id);
  };

  // Pobierz reel i następny (dla płynnej animacji)
  const getReelWithNext = (id: string) => {
    const currentIndex = reels.value.findIndex(reel => reel.id === id);
    if (currentIndex === -1) return { current: null, next: null };

    const current = reels.value[currentIndex];
    const next = currentIndex < reels.value.length - 1 ? reels.value[currentIndex + 1] : null;

    return { current, next };
  };

  // Pobierz index reela
  const getReelIndex = (id: string) => {
    return reels.value.findIndex(reel => reel.id === id);
  };

  // Toggle like
  const toggleLike = (id: string) => {
    const reel = reels.value.find(r => r.id === id);
    if (reel) {
      reel.isLiked = !reel.isLiked;
      const likesNum = parseInt(reel.likes.replace(/[^\d]/g, ''));
      reel.likes = reel.isLiked
        ? (likesNum + 1).toString()
        : (likesNum - 1).toString();
    }
  };

  // Toggle follow
  const toggleFollow = (userId: string) => {
    const reel = reels.value.find(r => r.user.id === userId);
    if (reel) {
      reel.user.isFollowing = !reel.user.isFollowing;
    }
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
