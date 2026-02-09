<script setup lang="ts">
import type { ImageElement } from '@/types/StoryElement'

defineProps<{
  element: ImageElement
}>()
</script>

<template>
  <div class="flex flex-col items-center select-none w-full h-full">
    <div
      class="relative w-full h-full overflow-hidden"

      :style="element.styles"
    >
      <img
        :src="element.content"
        crossorigin="anonymous"
        class="absolute max-w-none pointer-events-none origin-center"
        :style="{
          transform: `translate(${element.cropX || 0}px, ${element.cropY || 0}px) scale(${element.cropZoom || 1})`,
          width: '100%',
          height: '100%',
          objectFit: 'cover'
        }"
      />

    </div>


  </div>
</template>
