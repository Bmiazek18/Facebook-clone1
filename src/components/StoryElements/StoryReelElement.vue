<script setup lang="ts">
import Play from 'vue-material-design-icons/Play.vue'
import type { StoryElement } from '@/types/StoryElement'

defineProps<{
  element: StoryElement
}>()
</script>

<template>
  <div
    v-if="element.type === 'reel' && element.reelData"
    class="bg-black rounded-xl overflow-hidden shadow-xl pointer-events-none select-none relative"
    style="width: 200px;"
  >
    <!-- Video Thumbnail (poster) -->
    <div class="relative aspect-9/16">

      <video
        :src="element.reelData.videoSrc"
        class="w-full h-full object-cover"
        muted
        autoplay
        loop
        playsinline
        preload="auto"
      />

      <!-- Play Icon Overlay -->
      <div class="absolute inset-0 flex items-center justify-center bg-black/20">
        <div class="w-12 h-12 bg-white/30 rounded-full flex items-center justify-center backdrop-blur-sm">
          <Play :size="28" class="text-white ml-1" />
        </div>
      </div>

      <!-- Reel Badge -->
      <div class="absolute top-2 right-2 bg-black/60 px-2 py-0.5 rounded text-white text-xs font-semibold">
        Reel
      </div>
    </div>

    <!-- Reel Info -->
    <div class="p-2 bg-linear-to-t from-black/80 to-transparent absolute bottom-0 left-0 right-0">
      <div class="flex items-center gap-2">
        <img :src="element.reelData.authorAvatar" class="w-6 h-6 rounded-full object-cover border border-white/30" />
        <span class="text-white text-xs font-medium truncate">{{ element.reelData.authorName }}</span>
      </div>
      <p v-if="element.reelData.caption" class="text-white/80 text-xs mt-1 line-clamp-2">
        {{ element.reelData.caption }}
      </p>
    </div>


  </div>
</template>

<style scoped>
.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>
