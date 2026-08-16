<template>
  <div>
    <!-- Galeria zdjęć/filmów -->
    <PostMediaGallery
      v-if="galleryMedia.length > 0"
      :media="galleryMedia"
      :post-id="post.id"
      @click="
        (post as any).marketplaceData && $emit('image-click', (post as any).marketplaceData.itemId)
      "
    />

    <!-- Lista załączników plików -->
    <div v-if="fileAttachments.length > 0" class=" space-y-2 mt-2">
      <div
        v-for="(file, idx) in fileAttachments"
        :key="idx"
        @click="downloadFile(file)"
        class="p-3.5 bg-[#F0F2F5]  flex items-center justify-between gap-3.5 transition cursor-pointer border border-transparent "
      >
        <div class="flex items-center gap-3.5 min-w-0">
          <!-- Duża ikonka karty PDF/Pliku stylizowana na profil Facebooka -->
          <div class="w-12 h-14 bg-white border-2 border-[#8A8D91] rounded-lg flex items-center justify-center shrink-0 shadow-sm relative overflow-hidden">
            <!-- Górny zagięty róg dokumentu -->
            <div class="absolute top-0 right-0 w-3 h-3 bg-[#8A8D91] rounded-bl-sm"></div>
            <!-- Czerwone logo symbolizujące dokument PDF -->
            <svg class="w-7 h-7 text-[#E41E3F]" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path stroke-linecap="round" stroke-linejoin="round" d="M7 21h10a2 2 0 002-2V9.414a1 1 0 00-.293-.707l-5.414-5.414A1 1 0 0012.586 3H7a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2z" />
              <circle cx="12" cy="13" r="3" stroke="currentColor" stroke-width="2"/>
            </svg>
          </div>

          <!-- Dane pliku: Format + Nazwa -->
          <div class="flex flex-col min-w-0 justify-center">
            <span class="text-[12px] font-semibold text-[#65676B] uppercase tracking-wider mb-0.5">
              {{ getFileExtension(parseFileMetadata(file.altText).name) }}
            </span>
            <span class="font-bold text-[15px] text-[#050505] truncate leading-tight">
              {{ parseFileMetadata(file.altText).name }}
            </span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import PostMediaGallery from './PostMediaGallery.vue'
import type { Post } from '@/types/Post'
import { useFileSize } from '@/composables/shared/useFileSize'

interface Props {
  post?: Post
}

const props = withDefaults(defineProps<Props>(), {
  post: () => ({} as any),
})

defineEmits<{
  'image-click': [itemId: string]
}>()

const galleryMedia = computed(() => {
  if (!props.post?.media) return []
  return props.post.media.filter(m => !m.altText?.startsWith('file:'))
})

const fileAttachments = computed(() => {
  if (!props.post?.media) return []
  return props.post.media.filter(m => m.altText?.startsWith('file:'))
})

const parseFileMetadata = (altText: string) => {
  if (!altText || !altText.startsWith('file:')) {
    return { name: 'Plik', size: 0 }
  }
  const parts = altText.split('|')
  const namePart = parts[0]?.replace('file:', '') || 'Plik'
  const sizePart = parts[1]?.replace('size:', '') || '0'
  return {
    name: namePart,
    size: parseInt(sizePart) || 0
  }
}

// Wyciąganie rozszerzenia z nazwy pliku
const getFileExtension = (filename: string) => {
  if (!filename || !filename.includes('.')) return 'PLIK'
  const ext = filename.split('.').pop()
  return ext ? ext.toUpperCase() : 'PLIK'
}

const getMediaUrl = (src: string) => {
  if (!src) return ''
  if (src.startsWith('http://localhost/files/') || src.startsWith('http://localhost/videos/') || src.startsWith('http://localhost/media/')) {
    src = src.replace('http://localhost/', 'http://localhost:8080/')
  }
  if (
    src.startsWith('http://') ||
    src.startsWith('https://') ||
    src.startsWith('blob:') ||
    src.startsWith('data:')
  ) {
    return src
  }
  const baseUrl = import.meta.env.VITE_API_URL || 'http://localhost:8080'
  if (src.startsWith('/')) {
    return `${baseUrl}${src}`
  }
  return `${baseUrl}/${src}`
}

const downloadFile = (file: any) => {
  const meta = parseFileMetadata(file.altText)
  const link = document.createElement('a')
  link.href = getMediaUrl(file.src)
  link.download = meta.name
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
}
</script>
