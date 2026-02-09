<script setup lang="ts">
import FileIcon from 'vue-material-design-icons/File.vue';
import type { FileMessage } from '@/types/Message';
import { useFileSize } from '@/composables/useFileSize';

 defineProps<{
  message: FileMessage;
}>();

const downloadFile = (message: FileMessage) => {
  const link = document.createElement('a');
  link.href = message.fileUrl;
  link.download = message.fileName;
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
};
</script>

<template>
  <div
    class="p-3 bg-gray-200 rounded-xl flex items-center gap-2 cursor-pointer hover:bg-gray-300"
    @click="downloadFile(message)"
  >
    <FileIcon :size="22" />
    <div>
      <p class="text-sm font-semibold">{{ message.fileName }}</p>
      <p class="text-xs text-gray-600">{{ useFileSize(message.fileSize) }}</p>
    </div>
  </div>
</template>
