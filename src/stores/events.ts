import { defineStore } from 'pinia';
import { ref } from 'vue';
import { events as eventsData, type Event } from '@/data/events';
import { usePostsStore } from './posts';
import type { Post } from '@/types/Post';

export const useEventsStore = defineStore('events', () => {
  const events = ref<Event[]>(JSON.parse(JSON.stringify(eventsData)));

  const addEvent = (event: Event) => {
    events.value.push(event);
    
    // Automatycznie twórz post z wydarzeniem
    const postsStore = usePostsStore();
    const currentUser = postsStore.currentUser;
    
    const newPost: Post = {
      id: `${Date.now()}-event`,
      content: '', // Pusty content, bo całe info jest w evencie
      images: [],
      videoUrl: undefined,
      authorName: currentUser.name,
      authorAvatar: currentUser.avatar,
      authorId: currentUser.id,
      date: new Date().toLocaleDateString('pl-PL', { day: 'numeric', month: 'long' }),
      likesCount: 0,
      commentsCount: 0,
      sharesCount: 0,
      taggedUsers: [],
      location: undefined,
      gif: undefined,
      isLiked: false,
      likedType: null,
      reactionCount: 0,
      commentCount: 0,
      comments: [],
      selectedCardBgId: 0,
      privacy: event.privacy || 'public',
      timestamp: Date.now(),
      sharedEventId: event.id,
      createdEvent: true, // Flaga oznaczająca że to post o utworzeniu eventu
    };
    
    postsStore.addPost(newPost);
  };

  const getEventById = (id: string) => {
    return events.value.find((event) => event.id === id);
  };

  return {
    events,
    addEvent,
    getEventById,
  };
});
