<script setup lang="ts">
import { computed } from 'vue';
import PencilIcon from 'vue-material-design-icons/Pencil.vue';
import CloseIcon from 'vue-material-design-icons/Close.vue';
import PlayerVideo from './PlayerVideo.vue';

const props = defineProps<{
  selectedImage?: { url: string; altText: string } | null;
  selectedGif?: string | null;
}>();

const emit = defineEmits<{
  (e: 'remove-image'): void;
  (e: 'remove-gif'): void;
  (e: 'loaded'): void;
  (e: 'edit-image'): void;
  (e: 'edit-video'): void;
}>();

const onImageLoad = () => emit('loaded');

const isVideo = computed(() => {
  return props.selectedImage?.url.startsWith('data:video/mp4');
});

const isImage = computed(() => {
  return props.selectedImage && !isVideo.value;
});
</script>

<template>
  <div>
    <div v-if="isImage" class="relative mb-4 bg-gray-100 rounded-lg overflow-hidden border border-gray-200">
      <img :src="props.selectedImage.url" class="w-full object-cover" @load="onImageLoad" />
      <div @click="emit('edit-image')" class="absolute top-2 left-2 bg-white flex items-center gap-1 px-2 py-1 rounded shadow text-blue-600 text-sm font-medium cursor-pointer hover:bg-gray-50">
        <PencilIcon :size="16" class="mr-1" />
        Edytuj
      </div>
      <button @click="$emit('remove-image')" class="absolute top-2 right-2 bg-white text-gray-700 p-1.5 rounded-full shadow hover:bg-gray-100 transition">
        <CloseIcon :size="20" />
      </button>
    </div>

    <div v-else-if="isVideo" class="relative mb-4 bg-gray-100 rounded-lg overflow-hidden border border-gray-200">
      <PlayerVideo :url="props.selectedImage.url" />
      <div @click="emit('edit-video')" class="absolute top-2 left-2 bg-white flex items-center gap-1 px-2 py-1 rounded shadow text-blue-600 text-sm font-medium cursor-pointer hover:bg-gray-50">
        <PencilIcon :size="16" class="mr-1" />
        Edytuj video
      </div>
      <button @click="$emit('remove-image')" class="absolute top-2 right-2 bg-white text-gray-700 p-1.5 rounded-full shadow hover:bg-gray-100 transition">
        <CloseIcon :size="20" />
      </button>
    </div>

    <div v-else-if="props.selectedGif" class="relative mb-4 bg-gray-100 rounded-lg overflow-hidden border border-gray-200">
      <img :src="props.selectedGif" class="w-full object-contain" @load="onImageLoad" />
      <button @click="$emit('remove-gif')" class="absolute top-2 right-2 bg-white text-gray-700 p-1.5 rounded-full shadow hover:bg-gray-100 transition">
        <CloseIcon :size="20" />
      </button>
    </div>
  </div>
</template>
