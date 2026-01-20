<template>
  <div v-if="!post.sharedContent">
    <!-- Video -->
    <div v-if="post.media && post.media.videoUrl" ref="videoContainerRef" class="w-full">
      <PlayerVideo
        :settings="true"
        :lightbox="true"
        ref="videoRef"
        :url="post.media.videoUrl"
      />
    </div>

    <!-- Images -->
    <PostImageGallery
      v-else-if="post.media && post.media.images && post.media.images.length > 0"
      :images="post.media.images"
      :post-id="Number(post.id) ?? 0"
      @click="$emit('image-click', (post as any).marketplaceData?.itemId)"
    />
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import PlayerVideo from '@/components/media/PlayerVideo.vue'
import PostImageGallery from '@/components/feed/PostImageGallery.vue'
import { useVideoAutoplay } from '@/composables/useVideoAutoplay'
import type { Post } from '@/types/Post'

interface Props {
  post: Post
}

defineProps<Props>()

defineEmits<{
  'image-click': [itemId: string]
}>()

const videoContainerRef = ref<HTMLElement | null>(null)
const videoRef = ref<InstanceType<typeof PlayerVideo> | null>(null)
useVideoAutoplay(videoContainerRef)
</script>