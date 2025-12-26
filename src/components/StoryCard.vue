<template>
  <RouterLink
    to="/story"
    class="
      w-[113px]
      h-[200px]
      mt-3
      rounded-xl
      shadow-sm
      overflow-hidden
      flex
      flex-col
      relative
      group
      transition duration-300 ease-in-out
      cursor-pointer
      shrink-0
    "
    :style="{ backgroundImage: `url(${story.backgroundImageUrl})`, backgroundSize: 'cover', backgroundPosition: 'center' }"
  >

    <div class="absolute inset-x-0 bottom-0 h-1/2 bg-linear-to-t from-black via-black/50 to-transparent z-10"></div>

    <div class="absolute inset-x-0 top-0 h-[70%] z-20 p-2">
      <div
        class="w-9 h-9 rounded-full border-2 border-blue-500 overflow-hidden bg-white flex items-center justify-center shadow-md"
      >
        <img v-if="story.profileImageUrl" :src="story.profileImageUrl" :alt="story.title" class="w-full h-full object-cover" />
      </div>
    </div>

    <div class="absolute bottom-3 left-3 right-3 text-white z-30">
      <p class="text-xs font-semibold leading-tight line-clamp-2">
        {{ story.id }}
      </p>
    </div>
  </RouterLink>
</template>

<script setup lang="ts">
import type { Story } from '../types/Story';
defineProps<{
  story: Story;
}>();
</script>
