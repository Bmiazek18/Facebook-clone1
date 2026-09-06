<script setup lang="ts">
import FileIcon from 'vue-material-design-icons/File.vue'
import { useFileSize } from '@/composables/shared/useFileSize'

defineProps<{
  imageUrls: string[]
  documentFiles: File[]
  gifUrl: string | null
}>()

const emit = defineEmits<{
  'select-more-images': []
  'remove-image': [index: number]
  'remove-document': [index: number]
  'remove-gif': []
}>()
</script>

<template>
  <div
    v-if="imageUrls.length > 0 || documentFiles.length > 0 || gifUrl"
    class="flex items-center gap-2 pt-2 px-3 pb-0 overflow-x-auto"
  >
    <!-- Add more images button -->
    <div
      v-if="!gifUrl"
      @click="emit('select-more-images')"
      class="w-12 h-12 shrink-0 rounded-[10px] flex items-center justify-center cursor-pointer hover:bg-black/10 transition-colors bg-black/5"
    >
      <svg
        width="24"
        height="24"
        viewBox="0 0 24 24"
        fill="none"
        xmlns="http://www.w3.org/2000/svg"
      >
        <rect x="3" y="5" width="14" height="14" rx="3" stroke="currentColor" stroke-width="2" />
        <path d="M7 12H13M10 9V15" stroke="currentColor" stroke-width="2" stroke-linecap="round" />
        <path d="M21 7V17C21 18.1046 20.1046 19 19 19H9" stroke="currentColor" stroke-width="2" stroke-linecap="round" />
      </svg>
    </div>

    <!-- Image previews -->
    <div
      v-for="(img, index) in imageUrls"
      :key="'img-' + index"
      class="relative w-12 h-12 shrink-0 mt-2 mb-1"
    >
      <img
        :src="img"
        class="w-full h-full object-cover rounded-[10px] border border-black/5"
      />
      <button
        @click.stop="emit('remove-image', index)"
        class="absolute -top-2 -right-2 w-[22px] h-[22px] bg-white rounded-full flex items-center justify-center shadow-[0_1px_4px_rgba(0,0,0,0.25)] hover:bg-gray-100 z-10"
      >
        <svg width="10" height="10" viewBox="0 0 24 24" fill="none" stroke="black" stroke-width="3" stroke-linecap="round">
          <path d="M18 6L6 18M6 6l12 12" />
        </svg>
      </button>
    </div>

    <!-- Document previews -->
    <div
      v-for="(file, index) in documentFiles"
      :key="'doc-' + index"
      class="relative flex items-center gap-1.5 p-1.5 bg-black/5 rounded-[10px] border border-black/5 h-12 mt-2 mb-1 text-gray-800 dark:text-gray-200"
    >
      <FileIcon :size="18" class="shrink-0" />
      <div class="min-w-0 flex flex-col justify-center leading-tight">
        <p class="text-[10px] font-semibold truncate max-w-[80px]">{{ file.name }}</p>
        <p class="text-[8px] opacity-70">{{ useFileSize(file.size) }}</p>
      </div>
      <button
        @click.stop="emit('remove-document', index)"
        class="w-[18px] h-[18px] bg-white dark:bg-zinc-800 rounded-full flex items-center justify-center shadow-[0_1px_4px_rgba(0,0,0,0.25)] hover:bg-gray-100 dark:hover:bg-zinc-700 ml-1 shrink-0"
      >
        <svg width="8" height="8" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3" stroke-linecap="round">
          <path d="M18 6L6 18M6 6l12 12" />
        </svg>
      </button>
    </div>

    <!-- GIF preview -->
    <div
      v-if="gifUrl"
      class="relative w-16 h-12 shrink-0 mt-2 mb-1"
    >
      <img
        :src="gifUrl"
        class="w-full h-full object-cover rounded-[10px] border border-black/5"
      />
      <button
        @click.stop="emit('remove-gif')"
        class="absolute -top-2 -right-2 w-[22px] h-[22px] bg-white rounded-full flex items-center justify-center shadow-[0_1px_4px_rgba(0,0,0,0.25)] hover:bg-gray-100 z-10"
      >
        <svg width="10" height="10" viewBox="0 0 24 24" fill="none" stroke="black" stroke-width="3" stroke-linecap="round">
          <path d="M18 6L6 18M6 6l12 12" />
        </svg>
      </button>
    </div>
  </div>
</template>
