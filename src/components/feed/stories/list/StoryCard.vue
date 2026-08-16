<template>
  <div
    class="w-[113px] h-[200px] mt-3 rounded-xl shadow-sm overflow-hidden flex flex-col relative group transition duration-300 ease-in-out cursor-pointer shrink-0"
    :style="storyBackground"
  >
    <!-- Gradient overlay -->
    <div
      class="absolute inset-x-0 bottom-0 h-1/2 bg-linear-to-t from-black via-black/50 to-transparent z-10"
    ></div>

    <!-- Avatar with ring (blue for unviewed, gray for viewed) -->
    <div class="absolute inset-x-0 top-0 h-[70%] z-20 p-2">
      <div
        :class="[
          'w-9 h-9 rounded-full border-[3px] overflow-hidden bg-white flex items-center justify-center shadow-md',
          userStory.hasUnviewedStories ? 'border-blue-500' : 'border-gray-400',
        ]"
      >
        <img
          v-if="userStory.userAvatar"
          :src="userStory.userAvatar"
          :alt="userStory.userName"
          class="w-full h-full object-cover"
        />
      </div>
    </div>

    <!-- User name -->
    <div class="absolute bottom-3 left-3 right-3 text-white z-30">
      <p class="text-xs font-semibold leading-tight line-clamp-2 drop-shadow-lg">
        {{ isOwner ? t('story.myStory') : userStory.userName }}
      </p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { UserStories } from '../types/Story'
import { useStoriesStore } from '@/composables/feed/useAppState'
import { useI18n } from 'vue-i18n'

const props = defineProps<{
  userStory: UserStories
}>()

const { t } = useI18n()
const storiesStore = useStoriesStore()
const isOwner = computed(() => props.userStory.userId === storiesStore.currentUserId)

// Get the first story for preview
const firstStory = computed(() => props.userStory.stories[0])

const storyBackground = computed(() => {
  const bgUrl = firstStory.value?.thumbnailUrl || firstStory.value?.imageUrl
  if (bgUrl) {
    return {
      backgroundImage: `url(${bgUrl})`,
      backgroundSize: 'cover',
      backgroundPosition: 'center',
    }
  } else if (firstStory.value?.backgroundGradient) {
    return {
      background: firstStory.value.backgroundGradient,
    }
  } else if (firstStory.value?.backgroundColor) {
    return {
      backgroundColor: firstStory.value.backgroundColor,
    }
  } else {
    return {
      background: 'linear-gradient(to bottom, #3b82f6, #86efac)',
    }
  }
})
</script>
