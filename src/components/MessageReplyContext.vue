<script setup lang="ts">
import ReplyIcon from 'vue-material-design-icons/Reply.vue';
import type { Message } from '@/types/Message';

const props = defineProps<{
    reply: Message;
}>();
</script>

<template>
  <div
    class="flex flex-col"
    :class="{
      'items-start': reply.sender === 'other',
      'items-end': reply.sender === 'me'
    }"
  >
    <span class="flex flex-row text-[12px] align-center">
      <ReplyIcon :size="12" />
      <p class="font-semibold">Odpowiadasz {{ reply.replyToSender }}</p>
    </span>

    <div
      class="pb-2 text-xs -mb-[15px] bg-gray-100/70 rounded-lg backdrop-blur-sm max-w-[80%] overflow-hidden"
      style="word-break: break-word;"
    >
      <p class="text-gray-700 p-3 text-[11px]">{{ reply.replyToContentSnippet }}</p>
    </div>
  </div>
</template>
