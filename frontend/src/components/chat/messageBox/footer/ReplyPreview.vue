<template>
  <Transition
    enter-active-class="transition-all duration-300 ease-out"
    enter-from-class="translate-y-full opacity-0"
    enter-to-class="translate-y-0 opacity-100"
    leave-active-class="transition-all duration-300 ease-in"
    leave-from-class="translate-y-0 opacity-100"
    leave-to-class="translate-y-full opacity-0"
  >
    <div
      v-if="reply"
      class="absolute bottom-full left-0 w-full flex flex-col py-2 px-4 rounded-t-[12px] border-t border-[var(--color-theme-border)] bg-[var(--color-theme-bg-secondary)] shadow-[0_-4px_12px_rgba(0,0,0,0.05)]"
    >
      <div class="flex justify-between items-center mb-1">
        <span class="font-semibold text-[15px] text-theme-text">
          {{ isMine ? $t('ui.you') : reply.sender }}
        </span>
        <button
          @click="$emit('clearReply')"
          class="text-theme-text-secondary hover:text-theme-text text-xs cursor-pointer"
        >
          ✕
        </button>
      </div>

      <span class="truncate text-[14px] text-theme-text">
        <template v-if="reply.type === 'text'">
          {{ reply.content }}
        </template>
        <template v-else-if="reply.type === 'image'">
          {{ $t('ui.image') }}
        </template>
        <template v-else-if="reply.type === 'gif'">
          {{ $t('ui.gif') }}
        </template>
        <template v-else-if="reply.type === 'audio'">
          {{ $t('ui.voiceMessage') }}
        </template>
      </span>
    </div>
  </Transition>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { Message } from '@/types/Message'

const props = defineProps<{
  reply: Message | null
}>()

defineEmits(['clearReply'])

const isMine = computed(() => props.reply?.sender === 'me')
</script>
