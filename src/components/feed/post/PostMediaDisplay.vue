<template>
  <PostMediaGallery
    v-if="post.media && post.media.length > 0"
    :media="post.media"
    :post-id="post.id"
    @click="
      (post as any).marketplaceData && $emit('image-click', (post as any).marketplaceData.itemId)
    "
  />
</template>

<script setup lang="ts">
import PostMediaGallery from './PostMediaGallery.vue'
import type { Post } from '@/types/Post'

interface Props {
  post: Post
}

defineProps<Props>()

defineEmits<{
  'image-click': [itemId: string]
}>()
</script>
