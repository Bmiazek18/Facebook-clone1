<script setup lang="ts">
import { computed } from 'vue'
import type { TextMessage } from '@/types/Message'

const props = defineProps<{
  message: TextMessage
}>()



const emojiSizeClass = computed(() => {
  if (!props.message.iconSizeState || props.message.iconSizeState === 'default') return 'text-[30px]'
  if (props.message.iconSizeState === 'small') return 'text-[45px]'
  if (props.message.iconSizeState === 'medium') return 'text-[60px]'
  if (props.message.iconSizeState === 'large') return 'text-[80px]'
  return 'text-[3rem]' // Default fallback
})
</script>

<template>
  <div

    class="leading-none select-none "
    :class="emojiSizeClass"
  >
    {{ message.content }}
  </div>
</template>
