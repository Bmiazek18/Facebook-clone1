<script setup lang="ts">
import FileIcon from 'vue-material-design-icons/File.vue';
import type { FileMessage } from '@/types/Message';
import { useFileSize } from '@/composables/useFileSize';

const props = defineProps<{
  message: FileMessage; // Changed from Message to FileMessage
  isMe: boolean;
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
    :class="props.isMe ? 'text-white' : 'text-gray-900'"
    @click="downloadFile(props.message)"
  >
    <FileIcon :size="22" />
    <div>
      <p class="text-sm font-semibold">{{ props.message.fileName }}</p>
      <p class="text-xs text-gray-600">{{ useFileSize(props.message.fileSize) }}</p>
    </div>
  </div>
</template>