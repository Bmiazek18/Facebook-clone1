<script setup lang="ts">
import { computed } from 'vue';
import PencilIcon from 'vue-material-design-icons/Pencil.vue';
import CloseIcon from 'vue-material-design-icons/Close.vue';
import PlayerVideo from '@/components/media/PlayerVideo.vue';

interface SelectedImage {
  url: string;
  altText: string;
}

const props = defineProps<{
  selectedImages: SelectedImage[];
  selectedGif?: string | null;
  postVideoUrl?: string | null;
}>();

const emit = defineEmits<{
  (e: 'remove-image', index: number): void;
  (e: 'remove-gif'): void;
  (e: 'remove-video'): void;
  (e: 'loaded'): void;
  (e: 'edit-image', index: number): void;
  (e: 'edit-video', index: number): void;
}>();

const onImageLoad = () => emit('loaded');

const isVideoType = (url: string) =>
  url.startsWith('data:video/mp4') ||
  url.startsWith('blob:') ||
  url.endsWith('.mp4') ||
  url.endsWith('.webm') ||
  url.endsWith('.ogg');

// Usunęliśmy gridClass, bo nie jest już potrzebny do układu pionowego
</script>

<template>
  <div class="space-y-4 mb-4">
    <div
      v-if="postVideoUrl"
      class="relative bg-gray-100 rounded-lg overflow-hidden border border-theme-border w-full"
    >
      <div class="aspect-video bg-black flex items-center">
        <PlayerVideo :url="postVideoUrl" @loadeddata="onImageLoad" />
      </div>
      <div
        @click="emit('edit-video', 0)"
        class="absolute top-2 left-2 bg-white/90 hover:bg-white flex items-center gap-1 px-2 py-1 rounded shadow text-blue-600 text-sm font-medium cursor-pointer z-10"
      >
        <PencilIcon :size="16" />
        <span>Edytuj wideo</span>
      </div>
      <button
        @click="emit('remove-video')"
        class="absolute top-2 right-2 bg-black/50 text-white p-1.5 rounded-full shadow hover:bg-black/70 transition z-10"
      >
        <CloseIcon :size="20" />
      </button>
    </div>

    <div
      v-for="(image, index) in selectedImages"
      :key="index"
      class="relative bg-gray-100 rounded-lg overflow-hidden border border-theme-border w-full"
    >
      <template v-if="isVideoType(image.url)">
        <div class="aspect-video bg-black flex items-center">
          <PlayerVideo :url="image.url" @loadeddata="onImageLoad" />
        </div>
        <div
          @click="emit('edit-video', index)"
          class="absolute top-2 left-2 bg-white/90 hover:bg-white flex items-center gap-1 px-2 py-1 rounded shadow text-blue-600 text-sm font-medium cursor-pointer z-10"
        >
          <PencilIcon :size="16" />
          <span>Edytuj wideo</span>
        </div>
      </template>

      <template v-else>
        <img
          :src="image.url"
          class="w-full h-auto block"
          @load="onImageLoad"
        />
        <div
          @click="emit('edit-image', index)"
          class="absolute top-2 left-2 bg-white/90 hover:bg-white flex items-center gap-1 px-2 py-1 rounded shadow text-blue-600 text-sm font-medium cursor-pointer z-10"
        >
          <PencilIcon :size="16" />
          <span>Edytuj</span>
        </div>
      </template>

      <button
        @click="emit('remove-image', index)"
        class="absolute top-2 right-2 bg-black/50 text-white p-1.5 rounded-full shadow hover:bg-black/70 transition z-10"
      >
        <CloseIcon :size="20" />
      </button>
    </div>

    <div v-if="props.selectedGif" class="relative bg-gray-100 rounded-lg overflow-hidden border border-theme-border">
      <img :src="props.selectedGif" class="w-full h-auto block" @load="onImageLoad" />
      <button
        @click="$emit('remove-gif')"
        class="absolute top-2 right-2 bg-black/50 text-white p-1.5 rounded-full shadow hover:bg-black/70 transition"
      >
        <CloseIcon :size="20" />
      </button>
    </div>
  </div>
</template>
