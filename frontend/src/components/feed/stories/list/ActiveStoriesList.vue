<template>
  <div class="rounded-lg">
    <div
      v-if="allUserStories.length === 0"
      class="text-gray-500 dark:text-gray-400 text-sm text-center py-4"
    >{{ $t('feed.brakAktywnychRelacji') }}</div>

    <div v-else class="space-y-1">
      <div
        v-for="userStory in allUserStories"
        :key="userStory.userId"
        @click="handleStoryClick(userStory)"
        class="flex items-center gap-3 p-2 hover:bg-gray-100 dark:hover:bg-zinc-800 rounded-xl cursor-pointer transition-colors"
      >
        <!-- Avatar z niebieską ramką dla nieobejrzanych (zgodnie ze screenem z FB szeroki, pełny border) -->
        <div class="relative shrink-0">
          <div
            :class="[
              'rounded-full w-[60px] h-[60px] transition-all',
              userStory.hasUnviewedStories
                ? 'ring-3 ring-blue-500   p-0.75'
                : 'ring-1 ring-gray-500  p-0.5',
            ]"
          >
            <img
              :src="userStory.userAvatar"
              class="w-full h-full rounded-full object-cover"
              :alt="userStory.userName"
            />
          </div>
        </div>

        <!-- Informacje o użytkowniku -->
        <div class="flex-1 min-w-0">
          <p class="font-semibold text-[15px] text-theme-text truncate mb-0.5">
            {{ userStory.userName }}
          </p>
          <div class="flex items-center text-[15px] text-theme-text-secondary font-normal">
            <!-- Liczba nowych relacji wyświetlana obok czasu tekstowo (np. "1 nowa · 16 godz.") -->
            <template v-if="userStory.hasUnviewedStories && userStory.stories.length > 0">
              <span class="text-blue-600 dark:text-blue-400 font-medium">
                {{ userStory.stories.length }} {{ getStoryLabel(userStory.stories.length) }}
              </span>
              <span class="mx-1 text-gray-400 dark:text-gray-500 select-none">·</span>
            </template>

            <span v-if="userStory.userId === 'birthdays'">
              {{ todaysDate }}
            </span>
            <span v-else>
              {{ getTimeAgo(userStory.stories[0]?.createdAt) }}
            </span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useStoriesStore } from '@/composables/feed/useAppState'
import type { UserStories } from '@/types/Story'

const router = useRouter()
const storiesStore = useStoriesStore()

const allUserStories = computed(() => storiesStore.allUserStories)

const handleStoryClick = (userStory: UserStories) => {
  router.push(`/stories/${userStory.userId}`)
}

const todaysDate = computed(() => {
  return new Date().toLocaleDateString(undefined, { month: 'long', day: 'numeric' })
})

// Pomocnicza funkcja do odmiany słowa "nowa/nowe/nowych"
const getStoryLabel = (count: number) => {
  if (count === 1) return 'nowa'
  if (count % 10 >= 2 && count % 10 <= 4 && (count % 100 < 10 || count % 100 >= 20)) return 'nowe'
  return 'nowych'
}

const getTimeAgo = (timestamp?: number) => {
  if (!timestamp) return ''

  const now = Date.now()
  const diff = now - timestamp

  const minutes = Math.floor(diff / 60000)
  const hours = Math.floor(diff / 3600000)

  if (minutes < 1) return 'Teraz'
  if (minutes < 60) return `${minutes} min`
  if (hours < 24) return `${hours} godz.`
  return 'Wczoraj'
}
</script>
