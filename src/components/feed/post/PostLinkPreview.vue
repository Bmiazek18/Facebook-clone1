<script setup lang="ts">
import type { LinkPreviewData } from '@/types/Post'
import WebIcon from 'vue-material-design-icons/Web.vue'
// Dodajemy import ikony informacji
import InformationIcon from 'vue-material-design-icons/Information.vue'

defineProps<{
  linkPreview: LinkPreviewData
}>()
</script>

<template>
  <div class="relative mb-3 group">
    <a
      :href="linkPreview.url"
      target="_blank"
      class="block bg-theme-bg-tertiary overflow-hidden hover:bg-theme-bg-hover transition-colors cursor-pointer no-underline shadow-sm"
    >
      <div
        v-if="linkPreview.image"
        class="w-full aspect-[1.91/1] overflow-hidden bg-theme-bg-tertiary relative"
      >
        <img :src="linkPreview.image" class="w-full h-full object-cover" alt="Link preview" />

        <div
          class="absolute bottom-2 right-2 bg-black/60 text-white/90 rounded-full w-6 h-6 flex items-center justify-center backdrop-blur-[2px]"
        >
          <InformationIcon :size="14" />
        </div>
      </div>

      <div class="p-4 flex flex-col justify-center">
        <div
          class="text-[13px] text-theme-text-secondary uppercase font-medium tracking-wide mb-1.5 flex items-center truncate opacity-80"
        >
          <WebIcon :size="14" class="mr-1.5" v-if="!linkPreview.image" />
          {{ linkPreview.domain }}
        </div>

        <div class="font-bold text-theme-text text-[17px] leading-6 mb-1 line-clamp-2">
          {{ linkPreview.title }}
        </div>

        <div class="text-theme-text-secondary text-sm leading-snug line-clamp-1 opacity-90">
          {{ linkPreview.description }}
        </div>
      </div>
    </a>
  </div>
</template>
