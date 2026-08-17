<script setup lang="ts">
import CloseIcon from 'vue-material-design-icons/Close.vue'
import WebIcon from 'vue-material-design-icons/Web.vue'
import type { LinkPreviewData } from '@/composables/shared/useLinkPreview'

defineProps<{
  preview: LinkPreviewData
  loading: boolean
}>()

defineEmits<{
  (e: 'remove'): void
}>()
</script>

<template>
  <div v-if="loading" class="mb-3 bg-theme-bg-tertiary rounded-lg p-4 border border-theme-border">
    <div class="flex items-center gap-3">
      <div
        class="animate-spin rounded-full h-5 w-5 border-2 border-theme-border border-t-theme-primary"
      ></div>
      <span class="text-sm text-theme-text-secondary">Pobieranie podglądu...</span>
    </div>
  </div>

  <div v-else-if="preview" class="relative mb-3 group">
    <button
      @click="$emit('remove')"
      class="absolute top-2 right-2 z-20 bg-black/60 hover:bg-black/80 rounded-full p-1 text-white transition-all opacity-0 group-hover:opacity-100"
    >
      <CloseIcon :size="16" />
    </button>

    <a
      :href="preview.url"
      target="_blank"
      class="block bg-theme-bg-tertiary rounded-lg overflow-hidden border border-theme-border hover:bg-theme-bg-hover transition-colors cursor-pointer no-underline"
    >
      <div
        v-if="preview.image"
        class="w-full h-48 overflow-hidden bg-theme-bg-tertiary relative border-b border-theme-border"
      >
        <img :src="preview.image" class="w-full h-full object-cover" alt="Link preview" />
      </div>

      <div class="p-3">
        <div
          class="text-xs text-theme-text-secondary uppercase font-semibold mb-1 flex items-center truncate"
        >
          <WebIcon :size="12" class="mr-1" v-if="!preview.image" />
          {{ preview.domain }}
        </div>
        <div class="font-bold text-theme-text text-[15px] leading-snug mb-0.5 line-clamp-2">
          {{ preview.title }}
        </div>
        <div class="text-theme-text-secondary text-sm leading-snug line-clamp-1">
          {{ preview.description }}
        </div>
      </div>
    </a>
  </div>
</template>
