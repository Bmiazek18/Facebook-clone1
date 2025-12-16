<script setup lang="ts">
import Link from 'vue-material-design-icons/Link.vue'
import OpenInNew from 'vue-material-design-icons/OpenInNew.vue'
import type { StoryElement } from '@/types/StoryElement'

defineProps<{
  element: StoryElement
}>()
</script>

<template>
  <div class="select-none pointer-events-none">
    <!-- Default Style -->
    <div 
      v-if="element.linkStyle === 'default' || !element.linkStyle" 
      class="inline-flex items-center gap-2 bg-linear-to-r from-blue-500 to-purple-500 rounded-xl px-4 py-2.5 text-white font-medium shadow-lg"
    >
      <Link :size="18" />
      <span>{{ element.linkTitle || element.content }}</span>
      <OpenInNew :size="16" class="opacity-70" />
    </div>

    <!-- Minimal Style -->
    <div 
      v-else-if="element.linkStyle === 'minimal'" 
      class="inline-flex items-center gap-2 bg-white/95 backdrop-blur border border-gray-200 rounded-xl px-4 py-2.5 text-gray-800 font-medium shadow-md"
    >
      <Link :size="18" class="text-gray-500" />
      <span>{{ element.linkTitle || element.content }}</span>
      <OpenInNew :size="16" class="text-gray-400" />
    </div>

    <!-- Button Style -->
    <div 
      v-else-if="element.linkStyle === 'button'" 
      class="inline-block bg-black rounded-full px-6 py-3 text-white font-bold shadow-lg"
    >
      {{ element.linkTitle || element.content }}
    </div>
  </div>
</template>
