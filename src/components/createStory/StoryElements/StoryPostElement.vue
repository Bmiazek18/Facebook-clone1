<script setup lang="ts">
import Earth from 'vue-material-design-icons/Earth.vue'
import type { PostElement } from '@/types/StoryElement'

defineProps<{
  element: PostElement
}>()
</script>

<template>
  <div
    v-if="element.postData"
    class="bg-white rounded-xl overflow-hidden shadow-xl pointer-events-none select-none"
    style="width: 240px;"
  >
    <!-- Post Header -->
    <div class="p-3 flex items-center gap-2">
      <img :src="element.postData.authorAvatar" class="w-8 h-8 rounded-full object-cover" />
      <div class="flex-1 min-w-0">
        <p class="text-sm font-semibold text-gray-900 truncate">{{ element.postData.authorName }}</p>
        <div class="flex items-center gap-1 text-xs text-gray-500">
          <Earth :size="10" />
          <span>Publiczny</span>
        </div>
      </div>
    </div>

    <!-- Post Content -->
    <p class="px-3 pb-2 text-sm text-gray-800 line-clamp-3">{{ element.postData.content }}</p>

    <!-- Post Image -->
    <img
      v-if="element.postData.imageUrl"
      :src="element.postData.imageUrl"
      class="w-full h-32 object-cover"
    />

    <!-- Tap to view -->
    <div class="p-2 bg-gray-50 text-center">
      <span class="text-xs text-blue-600 font-medium">Dotknij, aby wyświetlić post</span>
    </div>
  </div>
</template>

<style scoped>
.line-clamp-3 {
  display: -webkit-box;
  -webkit-line-clamp: 3;
  line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>
