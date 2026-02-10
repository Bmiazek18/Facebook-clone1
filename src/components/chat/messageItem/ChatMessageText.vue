<script setup lang="ts">
import type { Message } from '@/types/Message'

const props = defineProps<{
  message: Message
  bubbleRadiusClass: string
  bubbleColorClass: string
}>()
</script>

<template>
  <div
    class="relative px-4 py-2 text-[15px] leading-relaxed shadow-sm break-words max-w-full"
    :class="[bubbleRadiusClass, bubbleColorClass]"
  >
    <p>{{ message.content }}</p>
  </div>
</template>
