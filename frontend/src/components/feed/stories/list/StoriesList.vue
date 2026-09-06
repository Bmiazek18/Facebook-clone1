<template>
  <div class="bg-gray-100 dark:bg-gray-800 mb-3 mt-3">
    <!-- Widok karuzeli (jeśli są dostępne stories) -->
    <div v-if="allUserStories.length > 0" class="relative">
      <button
        v-if="!isStart"
        @click="scrollLeft"
        class="absolute top-1/2 z-30 left-0 transform -translate-y-1/2 p-2 bg-theme-bg-secondary rounded-full shadow-lg hover:bg-theme-bg-hover transition duration-150"
        style="margin-left: 64.5px"
      >
        <ChevronLeftIcon :size="24" :fillColor="chevronFillColor" />
      </button>

      <div ref="carouselRef" class="flex overflow-x-auto bg-theme-bg scrollbar-hide">
        <AddStoryCard class="mr-4" />

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
        class="absolute top-1/2 z-30 p-3 right-0 transform -translate-y-1/2 bg-theme-bg-secondary rounded-full shadow-lg hover:bg-theme-bg-hover transition duration-150"
        style="margin-right: 14.5px"
      >
        <ChevronRightIcon :size="25" :fillColor="chevronFillColor" />
      </button>
    </div>

    <!-- Widok pusty (brak relacji, bazujący na Zrzut ekranu 2026-08-10 o 12.15.23.png) -->
    <div
      v-else
      @click="handleCreateStoryClick"
      class="flex items-center p-4 bg-theme-bg-secondary shadow-sm  rounded-xl  dark:border-gray-700 cursor-pointer hover:bg-gray-50 dark:hover:bg-theme-bg-hover transition duration-150  mt-2"
    >
      <!-- Niebieski przycisk z plusem -->
      <div class="flex-shrink-0 flex items-center justify-center w-12 h-12 bg-[#eaf3ff] dark:bg-blue-900/30 rounded-full text-[#1b74e4] dark:text-blue-400">
        <PlusIcon :size="26" />
      </div>

      <!-- Tekst -->
      <div class="ml-4 flex flex-col justify-center">
        <span class="text-base font-semibold text-black dark:text-white leading-tight">{{ $t('story.createStory') }}</span>
        <span class="text-sm text-gray-500 dark:text-gray-400 mt-0.5">{{ $t('story.sharePhotoOrWriteSomething') }}</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import AddStoryCard from '@/components/feed/stories/list/AddStoryCard.vue'
import StoryCard from '@/components/feed/stories/list/StoryCard.vue'
import { useCarousel } from '@/composables/media/useCarousel'
import { useTheme } from '@/composables/shared/useTheme'
import ChevronRightIcon from 'vue-material-design-icons/ChevronRight.vue'
import ChevronLeftIcon from 'vue-material-design-icons/ChevronLeft.vue'
import PlusIcon from 'vue-material-design-icons/Plus.vue' // Dodany import ikony plusa
import type { UserStories } from '@/types/Story'

const props = defineProps<{
  stories?: any[]
}>()

const router = useRouter()
const { isDark } = useTheme()

const chevronFillColor = computed(() => (isDark.value ? '#B0B3B8' : '#4B5563'))

const { carouselRef, isStart, isEnd, scrollLeft, scrollRight } = useCarousel(4)

const allUserStories = computed(() => props.stories ?? [])

const handleStoryClick = (userStory: UserStories) => {
  // Navigate to story viewer
  router.push(`/stories/${userStory.userId}`)
}

const handleCreateStoryClick = () => {
  router.push('/stories/create')
}
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
