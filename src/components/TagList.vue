<script setup lang="ts">
import CloseIcon from 'vue-material-design-icons/Close.vue';
import type { ImageTagType } from '@/types/ImageTag';

defineProps<{
  tags: ImageTagType[];
}>();

const emit = defineEmits<{
  (e: 'remove-tag', id: string): void;
}>();

const handleRemove = (id: string) => {
  emit('remove-tag', id);
};
</script>

<template>
  <div class="mt-4 p-2">
    <h3 class="text-xs font-bold text-gray-500 tracking-wide mb-3 uppercase">Oznaczeni</h3>
    <div class="border border-gray-300 rounded-md p-2 min-h-10 flex flex-nowrap gap-2 overflow-x-auto">
      <div
        v-for="tag in tags"
        :key="tag.id"
        class="flex items-center bg-blue-50 text-blue-600 px-2 py-1.5 rounded-sm cursor-default"
      >
        <span class="font-semibold text-sm mr-1">{{ tag.name }}</span>
        <button @click="handleRemove(tag.id)" class="hover:text-blue-800 flex items-center">
          <CloseIcon :size="16" />
        </button>
      </div>
    </div>
  </div>
</template>
