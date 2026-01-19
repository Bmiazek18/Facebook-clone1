<template>
  <div class="bg-gray-100 dark:bg-gray-800">
    <div class="relative">
      <button
        v-if="!isStart"
        @click="scrollLeft"
        class="absolute top-1/2 z-30 left-0 transform -translate-y-1/2 p-2 bg-theme-bg-secondary rounded-full shadow-lg border dark:border-gray-600 hover:bg-gray-200 dark:hover:bg-gray-600 transition duration-150"
        style="margin-left: 64.5px;"
      >
        <ChevronLeftIcon :size="24" fillColor="#4B5563" />
      </button>

      <div
        ref="carouselRef"
        class="flex overflow-x-auto bg-theme-bg scrollbar-hide"
      >
        <AddStoryCard class="mr-4"/>

        <!-- User Stories from Store -->
        <StoryCard
          v-for="userStory in allUserStories"
          :key="userStory.userId"
          :user-story="userStory"
          @click="handleStoryClick(userStory)"
          class="mr-4 last-of-type:mr-0"
        />
      </div>

      <button
        v-if="!isEnd"
        @click="scrollRight"
        class="absolute top-1/2 z-30 right-0 transform -translate-y-1/2 p-2 bg-theme-bg-secondary rounded-full shadow-lg border dark:border-gray-600 hover:bg-gray-200 dark:hover:bg-gray-600 transition duration-150"
        style="margin-right: 64.5px;"
      >
        <ChevronRightIcon :size="24" fillColor="#4B5563" />
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { useRouter } from 'vue-router';
import AddStoryCard from '@/components/stories/AddStoryCard.vue';
import StoryCard from '@/components/stories/StoryCard.vue';
import { useCarousel } from '../composables/useCarousel';
import { useStoriesStore } from '@/stores/stories';
import type { UserStories } from '@/types/Story';

import ChevronRightIcon from 'vue-material-design-icons/ChevronRight.vue';
import ChevronLeftIcon from 'vue-material-design-icons/ChevronLeft.vue';

const router = useRouter();
const storiesStore = useStoriesStore();

const {
  carouselRef,
  isStart,
  isEnd,
  scrollLeft,
  scrollRight,
} = useCarousel(4);

const allUserStories = computed(() => storiesStore.allUserStories);

const handleStoryClick = (userStory: UserStories) => {
  // Navigate to story viewer
  router.push(`/stories/${userStory.userId}`);
};
</script>



<style scoped>
.scrollbar-hide {
  -ms-overflow-style: none;
  scrollbar-width: none;
}

.scrollbar-hide::-webkit-scrollbar {
  display: none;
}
</style>
