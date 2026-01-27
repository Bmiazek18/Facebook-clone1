import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import type { UserStories, StoryItem } from '@/types/Story';
import type { StoryElement } from '@/types/StoryElement';
import storiesData from '@/data/stories.json';

export const useStoriesStore = defineStore('stories', () => {
  // State
  const userStories = ref<UserStories[]>([]);
  const currentUserId = ref<string>('1'); // Default current user ID

  // Load stories from JSON file (always fresh data)
  const loadStories = () => {
    loadDefaultStories();
  };

  // Load default stories from JSON
  const loadDefaultStories = () => {
    // Update timestamps to be current (for demo purposes)
    const now = Date.now();
    const updatedStories = (storiesData as UserStories[]).map(userStory => ({
      ...userStory,
      stories: userStory.stories.map(story => ({
        ...story,
        createdAt: now - Math.random() * 3600000, // Random time within last hour
        expiresAt: now + 24 * 60 * 60 * 1000 - Math.random() * 3600000 // Expires within next 23-24h
      }))
    }));
    userStories.value = updatedStories as UserStories[];
  };

  // Save stories to localStorage (disabled - not used anymore)
  const saveStories = () => {
    // localStorage.setItem('user-stories', JSON.stringify(userStories.value));
  };

  // Remove expired stories (older than 24h)
  const cleanupExpiredStories = () => {
    const now = Date.now();
    let hasChanges = false;

    userStories.value = userStories.value
      .map(userStory => {
        const validStories = userStory.stories.filter(story => story.expiresAt > now);
        if (validStories.length !== userStory.stories.length) {
          hasChanges = true;
        }
        return {
          ...userStory,
          stories: validStories
        };
      })
      .filter(userStory => userStory.stories.length > 0);

    if (hasChanges) {
      saveStories();
    }
  };

  // Get stories for specific user
  const getUserStories = (userId: string): UserStories | undefined => {
    return userStories.value.find(us => us.userId === userId);
  };

  // Get all stories sorted by has unviewed and creation time
  const allUserStories = computed(() => {
    const birthdayStories: StoryItem[] = [];
    // Deep clone to avoid mutating the original state inside a computed property
    const processedUserStories: UserStories[] = JSON.parse(JSON.stringify(userStories.value));

    // Extract birthday stories and remove them from original users
    processedUserStories.forEach(userStory => {
      const storiesBefore = userStory.stories.length;
      const nonBirthdayStories = userStory.stories.filter(story => {
        if (story.type === 'birthday') {
          // Also pass user info to birthday story for display
          const storyWithUserInfo = { ...story, originalUserName: userStory.userName, originalUserAvatar: userStory.userAvatar };
          birthdayStories.push(storyWithUserInfo);
          return false;
        }
        return true;
      });

      if (nonBirthdayStories.length < storiesBefore) {
        userStory.hasUnviewedStories = nonBirthdayStories.some(s => 
          !s.interactions?.some(i => i.userId === currentUserId.value) && s.userId !== currentUserId.value
        );
      }
      userStory.stories = nonBirthdayStories;
    });

    const finalUserStories = processedUserStories.filter(userStory => userStory.stories.length > 0);

    if (birthdayStories.length > 0) {
      const hasUnviewedBirthdayStories = birthdayStories.some(story =>
        !story.interactions?.some(i => i.userId === currentUserId.value) && story.userId !== currentUserId.value
      );

      const birthdayUser: UserStories = {
        userId: 'birthdays',
        userName: 'Urodziny',
        userAvatar: 'https://emojicdn.elk.sh/🎂?style=twitter',
        stories: birthdayStories.sort((a, b) => b.createdAt - a.createdAt),
        hasUnviewedStories: hasUnviewedBirthdayStories,
      };
      finalUserStories.push(birthdayUser);
    }

    return finalUserStories.sort((a, b) => {
      // Current user first
      if (a.userId === currentUserId.value) return -1;
      if (b.userId === currentUserId.value) return 1;

      // Then Birthdays user
      if (a.userId === 'birthdays') return -1;
      if (b.userId === 'birthdays') return 1;
      
      // Then by unviewed status
      if (a.hasUnviewedStories && !b.hasUnviewedStories) return -1;
      if (!a.hasUnviewedStories && b.hasUnviewedStories) return 1;

      // Then by most recent story
      const aLatest = Math.max(...a.stories.map(s => s.createdAt));
      const bLatest = Math.max(...b.stories.map(s => s.createdAt));
      return bLatest - aLatest;
    });
  });

  // Add new story
  const addStory = (
    userId: string,
    userName: string,
    userAvatar: string,
    storyData: {
      type: 'image' | 'text' | 'video';
      imageUrl?: string;
      musicUrl?: string;
      backgroundColor?: string;
      backgroundGradient?: string;
      elements?: StoryElement[];
      sharedPostInfo?: StoryItem['sharedPostInfo'];
      sharedLinkInfo?: StoryItem['sharedLinkInfo'];
    }
  ) => {
    const now = Date.now();
    const expiresAt = now + 24 * 60 * 60 * 1000; // 24 hours

    const newStory: StoryItem = {
      id: `story_${userId}_${now}`,
      userId,
      type: storyData.type,
      imageUrl: storyData.imageUrl,
      musicUrl: storyData.musicUrl,
      backgroundColor: storyData.backgroundColor,
      backgroundGradient: storyData.backgroundGradient,
      elements: storyData.elements,
      sharedPostInfo: storyData.sharedPostInfo,
      sharedLinkInfo: storyData.sharedLinkInfo,
      createdAt: now,
      expiresAt,
      viewCount: 0,
      interactions: []
    };

    // Find existing user stories
    const existingUserStories = userStories.value.find(us => us.userId === userId);

    if (existingUserStories) {
      // Add to existing user
      existingUserStories.stories.push(newStory);
      existingUserStories.hasUnviewedStories = true;
    } else {
      // Create new user stories entry
      userStories.value.push({
        userId,
        userName,
        userAvatar,
        stories: [newStory],
        hasUnviewedStories: true
      });
    }

    saveStories();
    return newStory;
  };

  // Mark story as viewed
  const markStoryAsViewed = (storyId: string, viewerId: string) => {
    for (const userStory of userStories.value) {
      const story = userStory.stories.find(s => s.id === storyId);
      if (story) {
        if (!story.interactions) {
          story.interactions = [];
        }
        const existingInteraction = story.interactions.find(i => i.userId === viewerId);
        if (!existingInteraction) {
          story.interactions.push({ userId: viewerId, reaction: null });
          story.viewCount = (story.viewCount || 0) + 1;
        }

        // Check if all stories are viewed
        const allViewed = userStory.stories.every(s =>
          s.interactions?.some(i => i.userId === viewerId) || s.userId === viewerId
        );
        userStory.hasUnviewedStories = !allViewed;

        saveStories();
        break;
      }
    }
  };

  // Delete story
  const deleteStory = (storyId: string) => {
    for (const userStory of userStories.value) {
      const index = userStory.stories.findIndex(s => s.id === storyId);
      if (index !== -1) {
        userStory.stories.splice(index, 1);

        // Remove user stories if no stories left
        if (userStory.stories.length === 0) {
          const userIndex = userStories.value.findIndex(us => us.userId === userStory.userId);
          if (userIndex !== -1) {
            userStories.value.splice(userIndex, 1);
          }
        }

        saveStories();
        break;
      }
    }
  };

  // Initialize
  loadStories();

  // Cleanup expired stories every minute
  setInterval(cleanupExpiredStories, 60 * 1000);

  return {
    userStories,
    currentUserId,
    allUserStories,
    getUserStories,
    addStory,
    markStoryAsViewed,
    deleteStory,
    loadStories,
    cleanupExpiredStories
  };
});
