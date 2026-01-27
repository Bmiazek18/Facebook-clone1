<template>
  <div class="rounded-lg ">


    <div v-if="allUserStories.length === 0" class="text-gray-500 dark:text-gray-400 text-sm text-center py-4">
      Brak aktywnych relacji
    </div>

    <div v-else class="space-y-3">
      <div
        v-for="userStory in allUserStories"
        :key="userStory.userId"
        @click="handleStoryClick(userStory)"
        class="flex items-center gap-3 p-2 hover:bg-gray-100 dark:hover:bg-gray-700 rounded-lg cursor-pointer transition-colors"
      >
        <!-- Avatar with ring -->
        <div class="relative shrink-0">
          <div
            :class="[
              'p-0.5 rounded-full',
              userStory.hasUnviewedStories
                ? 'bg-linear-to-tr from-blue-500 to-blue-600'
                : 'bg-gray-300 dark:bg-gray-600'
            ]"
          >
            <div class="p-0.5 bg-white dark:bg-gray-900 rounded-full">
              <img
                :src="userStory.userAvatar"
                class="w-12 h-12 rounded-full object-cover"
                :alt="userStory.userName"
              />
            </div>
          </div>

          <!-- Story count badge -->
          <div
            v-if="userStory.stories.length > 1"
            class="absolute -bottom-1 -right-1 bg-blue-600 text-white text-xs font-bold rounded-full w-5 h-5 flex items-center justify-center border-2 border-white dark:border-gray-800"
          >
            {{ userStory.stories.length }}
          </div>
        </div>

        <!-- User info -->
        <div class="flex-1 min-w-0">
          <p class="font-semibold text-sm text-gray-900 dark:text-white truncate">
            {{ userStory.userName }}
          </p>
          <p v-if="userStory.userId === 'birthdays'" class="text-xs text-gray-500 dark:text-gray-400">
            {{ todaysDate }}
          </p>
          <p v-else class="text-xs text-gray-500 dark:text-gray-400">
            {{ getTimeAgo(userStory.stories[0]?.createdAt) }}
          </p>
        </div>


      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { useRouter } from 'vue-router';
import { useStoriesStore } from '@/stores/stories';
import type { UserStories } from '@/types/Story';

const router = useRouter();
const storiesStore = useStoriesStore();

const allUserStories = computed(() => storiesStore.allUserStories);

const handleStoryClick = (userStory: UserStories) => {
  router.push(`/stories/${userStory.userId}`);
};

const todaysDate = computed(() => {
  return new Date().toLocaleDateString(undefined, { month: 'long', day: 'numeric' });
});

const getTimeAgo = (timestamp?: number) => {
  if (!timestamp) return '';

  const now = Date.now();
  const diff = now - timestamp;

  const minutes = Math.floor(diff / 60000);
  const hours = Math.floor(diff / 3600000);

  if (minutes < 1) return 'Teraz';
  if (minutes < 60) return `${minutes} min`;
  if (hours < 24) return `${hours} godz.`;
  return 'Wczoraj';
};
</script>
