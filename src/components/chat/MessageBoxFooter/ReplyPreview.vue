<template>
  <transition name="reply">
    <div v-if="reply" class="reply-preview">
      <div class="flex justify-between items-center mb-1">
        <span class="reply-sender text-theme-text">{{ reply.sender === 'me' ? $t('ui.you') : reply.sender }}</span>
        <button @click="$emit('clearReply')" class="text-theme-text-secondary hover:text-theme-text text-xs">✕</button>
      </div>
      <span class="reply-content truncate text-theme-text">
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
  </transition>
</template>

<script setup lang="ts">
import { defineProps, defineEmits } from 'vue';

const props = defineProps({
  reply: {
    type: Object,
    default: null,
  },
});

const emit = defineEmits(['clearReply']);
</script>

<style scoped>
.reply-preview {
  position: absolute;
  bottom: 100%;
  left: 0;
  width: 100%;
  background-color: var(--color-theme-bg-secondary);
  border-top-left-radius: 12px;
  border-top-right-radius: 12px;
  padding: 8px 16px;
  box-shadow: 0 -4px 12px rgba(0, 0, 0, 0.05);
  border-top: 1px solid var(--color-theme-border);
  display: flex;
  flex-direction: column;
}

.reply-sender {
  font-weight: 600;
  font-size: 15px;
}

.reply-content {
  font-size: 14px;
}

.reply-enter-active,
.reply-leave-active {
  transition: all 0.3s ease;
}

.reply-enter-from,
.reply-leave-to {
  transform: translateY(100%);
  opacity: 0;
}
</style>
