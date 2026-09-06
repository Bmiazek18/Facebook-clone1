<script setup lang="ts">
import StoryBirthdaySlide from '@/components/feed/stories/StoryBirthdaySlide.vue'
import type { Story } from '@/types/Story'

const props = defineProps({
  currentItem: {
    type: Object as () => Story & {
      type: string
      src: string
      user: { name: string; avatar: string }
    },
    required: true,
  },
  isVideo: {
    type: Boolean,
    required: true,
  },
  setVideoRef: {
    type: Function,
    required: true,
  },
  setImageRef: {
    type: Function,
    required: true,
  },
  updateProgress: {
    type: Function,
    required: true,
  },
  nextStory: {
    type: Function,
    required: true,
  },
})
</script>

<template>
  <div class="relative w-full h-full overflow-hidden">
    <video
      v-if="isVideo"
      :ref="props.setVideoRef"
      :src="currentItem.src"
      class="absolute inset-0 w-full h-full object-cover z-0"
      playsinline
      @timeupdate="props.updateProgress"
      @ended="props.nextStory"
    ></video>
    <StoryBirthdaySlide v-else-if="currentItem.type === 'birthday'" :current-item="currentItem" />

    <img
      v-else
      :ref="props.setImageRef"
      :src="currentItem.src"
      class="absolute inset-0 w-full h-full object-cover z-0"
      :alt="$t('feed.story')"
    />
  </div>
</template>
