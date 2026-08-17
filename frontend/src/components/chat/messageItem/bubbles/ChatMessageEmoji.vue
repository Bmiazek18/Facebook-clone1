<script setup lang="ts">
import { computed } from 'vue'
import type { TextMessage } from '@/types/Message'
import { Emoji, EmojiIndex } from 'emoji-mart-vue-fast/src'
import data from 'emoji-mart-vue-fast/data/all.json'

const emojiIndex = new EmojiIndex(data)

const props = defineProps<{
  message: TextMessage
}>()

// Mapujemy stany na konkretne liczby (piksele) dla propsa :size
const emojiSize = computed(() => {
  switch (props.message.iconSizeState) {
    case 'small':
      return 40
    case 'medium':
      return 50
    case 'large':
      return 65
    case 'default':
    default:
      return 30
  }
})
</script>

<template>
  <div class="flex items-center justify-center">
    <Emoji
      :key="message.content + emojiSize"
      :data="emojiIndex"
      :emoji="message.content"
      set="facebook"
      :size="emojiSize"
    />
  </div>
</template>
