<template>
  <PlayerVideo
    v-if="isVideo(media.src)"
    :settings="true"
    :lightbox="true"
    :url="media.src"
    :post-id="postId"
    :index="index"
  />
  <router-link
    v-else
    :to="`/photo/${postId}/${index}`"
    class="block bg-black/5 relative"
    :class="{
      'aspect-square': media.tags === undefined,
    }"
  >

    <img
      class="w-full h-full object-cover cursor-pointer"
      :src="media.src"
      :alt="media.altText || 'Post content'"
    />
    <template v-if="media.tags">
      <ImageTag
        v-for="tag in media.tags"
        :key="tag.id"
        :tag="tag"
        :style="{ left: `${tag.x}%`, top: `${tag.y}%` }"
        class="absolute"
      />
    </template>
  </router-link>
</template>

<script setup lang="ts">
import ImageTag from '@/components/media/ImageTag.vue'
import type { ImageTagType } from '@/types/ImageTag'
import PlayerVideo from '../media/PlayerVideo.vue';

const props = defineProps<{
  media: {
    src: string
    altText?: string
    tags?: ImageTagType[]
  }
  postId: number
  index: number
}>()


const isVideo = (src: string) => {
  return src.toLowerCase().endsWith('.mp4') || src.toLowerCase().endsWith('.webm')
}
</script>
