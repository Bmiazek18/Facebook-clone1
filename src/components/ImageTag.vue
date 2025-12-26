<script setup lang="ts">
import type { ImageTagType } from '@/types/ImageTag'
import { ref } from 'vue';

defineProps<{
  tag: ImageTagType
}>()

const isHovered = ref(false);
</script>

<template>
  <div
    class="absolute w-10 h-10 border-2 transform -translate-x-1/2 -translate-y-1/2 transition-colors duration-200"
    :class="{ 'border-white': isHovered, 'border-transparent': !isHovered }"
    :style="{ left: `${tag.x}%`, top: `${tag.y}%` }"
    @mouseenter="isHovered = true"
    @mouseleave="isHovered = false"
  >
    <div
      v-if="isHovered"
      class="absolute left-1/2 top-[calc(100%+12px)] -translate-x-1/2 flex flex-col items-center filter drop-shadow-md whitespace-nowrap"
    >
      <div
        class="bg-white text-black px-3 py-1.5 rounded text-xs font-semibold"
      >
        {{ tag.user?.name || tag.name }}
      </div>
      <div
        class="w-0 h-0 border-l-[6px] border-l-transparent border-r-[6px] border-r-transparent border-b-[6px] border-b-white translate-y-[-6px]"
      ></div>
    </div>
  </div>
</template>
