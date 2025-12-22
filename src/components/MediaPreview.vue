<script setup lang="ts">
import PencilIcon from 'vue-material-design-icons/Pencil.vue';
import CloseIcon from 'vue-material-design-icons/Close.vue';

const props = defineProps<{
  selectedImage?: string | null;
  selectedGif?: string | null;
}>();

const emit = defineEmits<{
  (e: 'remove-image'): void;
  (e: 'remove-gif'): void;
  (e: 'loaded'): void;
  (e: 'edit-image'): void; // Added edit-image event
}>();

const onImageLoad = () => emit('loaded');
</script>

<template>
  <div>
    <div v-if="props.selectedImage" class="relative mb-4 bg-gray-100 rounded-lg overflow-hidden border border-gray-200">
      <img :src="props.selectedImage" class="w-full  object-cover" @load="onImageLoad" />
      <div @click="emit('edit-image')" class="absolute top-2 left-2 bg-white flex items-center gap-1 px-2 py-1 rounded shadow text-blue-600 text-sm font-medium cursor-pointer hover:bg-gray-50">
        <PencilIcon :size="16" class="mr-1" />
        Edytuj
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
