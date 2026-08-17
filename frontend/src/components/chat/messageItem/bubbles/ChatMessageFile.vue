<script setup lang="ts">
import { computed } from 'vue'
import FileIcon from 'vue-material-design-icons/FileDocument.vue'
import type { FileMessage } from '@/types/Message'
import { useFileSize } from '@/composables/shared/useFileSize'

const props = defineProps<{
  message: FileMessage
  isMe: boolean
}>()

const downloadFile = (message: FileMessage) => {
  const link = document.createElement('a')
  link.href = message.fileUrl
  link.download = message.fileName
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
}

// Logika stylów (tło) w zależności od nadawcy, tak jak w audio/połączeniach
const bubbleStyle = computed(() => {
  return {
    bgClass: props.isMe ? 'bg-[#F0F2F5]' : 'bg-white shadow-sm border border-gray-100/30',
    iconBgClass: 'bg-[#E4E6EB]' // Szare kółko pod ikoną niezależnie od nadawcy
  }
})
</script>

<template>
  <div
    class="p-3 rounded-[22px] flex items-start gap-3 cursor-pointer max-w-[240px]"
    :class="bubbleStyle.bgClass"
    @click="downloadFile(props.message)"
  >
    <!-- Ikona w okrągłym tle -->
    <div
      class="w-10 h-10 rounded-full flex items-center justify-center shrink-0 text-black"
      :class="bubbleStyle.iconBgClass"
    >
      <FileIcon :size="22" />
    </div>

    <!-- Tekst pliku -->
    <div class="flex flex-col text-black min-w-0 mt-0.5">
      <span class="font-semibold text-[16px] leading-[1.2] break-all pr-1">
        {{ props.message.fileName }}
      </span>
      <span class="text-[13px] text-gray-500 mt-1">
        {{ useFileSize(props.message.fileSize) }}
      </span>
    </div>
  </div>
</template>
