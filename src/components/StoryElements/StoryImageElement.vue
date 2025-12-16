<script setup lang="ts">
import ResizeBottomRight from 'vue-material-design-icons/ResizeBottomRight.vue'
import type { StoryElement } from '@/types/StoryElement'

defineProps<{
  element: StoryElement
  isCropping: boolean
  isSelected: boolean
  onStartResize: (e: MouseEvent, element: StoryElement) => void
  onToggleCrop: (id: string) => void
}>()
</script>

<template>
  <div class="flex flex-col items-center select-none w-full h-full">
    <div
      class="relative w-full h-full overflow-hidden bg-gray-900 border-2"
      :class="isCropping || isSelected ? 'border-blue-500 shadow-md' : 'border-transparent'"
      :style="element.styles"
      @dblclick.stop="onToggleCrop(element.id)"
    >
      <img
        :src="element.content"
        class="absolute max-w-none pointer-events-none origin-center"
        :style="{
          transform: `translate(${element.cropX || 0}px, ${element.cropY || 0}px) scale(${element.cropZoom || 1})`,
          width: '100%',
          height: '100%',
          objectFit: 'cover'
        }"
      />

      <!-- Crop Grid -->
      <div v-if="isCropping" class="absolute inset-0 grid grid-cols-3 grid-rows-3 pointer-events-none opacity-50">
        <div class="border-r border-b border-white/30"></div>
        <div class="border-r border-b border-white/30"></div>
        <div class="border-b border-white/30"></div>
        <div class="border-r border-b border-white/30"></div>
        <div class="border-r border-b border-white/30"></div>
        <div class="border-b border-white/30"></div>
        <div class="border-r border-white/30"></div>
        <div class="border-r border-white/30"></div>
        <div></div>
      </div>
    </div>

    <!-- Resize Handle -->
    <template v-if="!isCropping">
      <div
        @mousedown.stop="(e: MouseEvent) => onStartResize(e, element)"
        class="absolute bottom-1 right-1 bg-white text-black p-1 rounded-full opacity-0 group-hover:opacity-100 cursor-nwse-resize shadow-md hover:scale-110 transition z-40"
      >
        <ResizeBottomRight :size="16" />
      </div>
    </template>
  </div>
</template>
