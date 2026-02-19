<script setup lang="ts">
import type { TextMessage } from '@/types/Message'
import { computed } from 'vue'

const props = defineProps<{
  message: TextMessage // Changed from Message to TextMessage for more specific type
  bubbleRadiusClass?: string // Optional for emoji messages
  bubbleColor?: string // Optional for emoji messages
  textColor?: string // Optional for emoji messages
}>()

const style = computed(() => ({
  backgroundColor: props.bubbleColor,
  color: props.textColor,
}))

const isEmojiOnly = (content: string): boolean => {
  if (!content?.trim()) return false
  const nonEmojiChars = content
    .replace(
      /(\u00a9|\u00ae|[\u2000-\u3300]|\ud83c[\ud000-\udfff]|\ud83d[\ud000-\udfff]|\ud83e[\ud000-\udfff])/g,
      '',
    )
    .trim()
  return nonEmojiChars.length === 0
}

const emojiSizeClass = computed(() => {
  if (!props.message.iconSizeState || props.message.iconSizeState === 'default') return 'text-[3rem]'
  if (props.message.iconSizeState === 'small') return 'text-[45px]'
  if (props.message.iconSizeState === 'medium') return 'text-[60px]'
  if (props.message.iconSizeState === 'large') return 'text-[80px]'
  return 'text-[3rem]' // Default fallback
})
</script>

<template>
  <div v-if="isEmojiOnly(message.content)"
    class="leading-none select-none transition-transform hover:scale-110"
    :class="emojiSizeClass"
  >
    {{ message.content }}
  </div>
  <div v-else
    class="relative px-4 py-2 text-[15px] leading-relaxed shadow-sm break-words max-w-full"
    :class="[bubbleRadiusClass]"
    :style="style"
  >
    <p>{{ message.content }}</p>
  </div>
</template>
