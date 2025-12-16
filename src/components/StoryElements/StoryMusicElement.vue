<script setup lang="ts">
import MusicNote from 'vue-material-design-icons/MusicNote.vue'
import type { StoryElement } from '@/types/StoryElement'

defineProps<{
  element: StoryElement
}>()
</script>

<template>
  <div class="w-full h-full relative select-none">
    <!-- Large Style (default) -->
    <template v-if="element.musicStyle === 'large' || !element.musicStyle">
      <div class="w-full h-full bg-white/95 backdrop-blur-sm rounded-xl p-3 flex flex-col gap-3 shadow-lg pointer-events-none border border-gray-100">
        <img :src="element.content" class="w-full aspect-square object-cover rounded-lg shadow-sm" />
        <div class="text-center overflow-hidden pb-1">
          <p class="text-gray-900 font-bold text-lg leading-tight truncate">{{ element.musicTitle }}</p>
          <p class="text-gray-500 text-sm truncate">{{ element.musicArtist }}</p>
        </div>
      </div>
    </template>

    <!-- Small Style -->
    <template v-else-if="element.musicStyle === 'small'">
      <div class="w-full h-full bg-white/90 backdrop-blur-md rounded-lg p-2 flex items-center gap-3 shadow-md pointer-events-none border border-white/20">
        <img :src="element.content" class="w-12 h-12 rounded-md object-cover shrink-0" />
        <div class="flex flex-col min-w-0 flex-1">
          <p class="text-gray-900 font-bold text-sm truncate">{{ element.musicTitle }}</p>
          <p class="text-gray-500 text-xs truncate">{{ element.musicArtist }}</p>
        </div>
        <MusicNote :size="20" class="text-gray-400 mr-2" />
      </div>
    </template>

    <!-- Text Style -->
    <template v-else-if="element.musicStyle === 'text'">
      <div class="w-full h-full flex flex-col items-center justify-center pointer-events-none text-center">
        <p class="text-white font-bold text-2xl drop-shadow-[0_2px_8px_rgba(0,0,0,0.6)]">{{ element.musicTitle }}</p>
        <div class="flex items-center gap-1 opacity-90 drop-shadow-[0_1px_4px_rgba(0,0,0,0.6)] mt-1">
          <MusicNote :size="14" class="text-white" />
          <p class="text-white text-sm font-medium">{{ element.musicArtist }}</p>
        </div>
      </div>
    </template>

    <!-- Icon Style -->
    <template v-else-if="element.musicStyle === 'icon'">
      <div class="w-full h-full rounded-full shadow-lg border-2 border-white pointer-events-none relative overflow-hidden bg-black">
        <img
          :src="element.content"
          class="w-full h-full object-cover rounded-full animate-spin-record"
          alt="Album cover"
        />
        <div class="absolute inset-0 bg-black/30 flex items-center justify-center z-10">
          <MusicNote :size="24" class="text-white drop-shadow-md" />
        </div>
      </div>
    </template>
  </div>
</template>

<style scoped>
@keyframes spin-record {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.animate-spin-record {
  animation: spin-record 8s linear infinite;
}
</style>
