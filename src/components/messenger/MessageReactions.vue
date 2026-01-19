<script setup lang="ts">
import { ref } from 'vue';
import ReplyIcon from 'vue-material-design-icons/Reply.vue';

const props = defineProps<{
  messageId: number;
  reactions: string[] | undefined;
}>();

const emit = defineEmits<{
  (e: 'addReaction', payload: { messageId: number; emoji: string }): void;
  (e: 'openPanel'): void;
  (e: 'reply'): void;
}>();

const showReactions = ref(false);
const reactionsList = ['👍', '❤️', '😂', '😮', '😢', '👏'];

const selectReaction = (emoji: string) => {
  emit('addReaction', { messageId: props.messageId, emoji });
  showReactions.value = false;
};

const toggleReactions = () => (showReactions.value = !showReactions.value);
</script>

<template>
  <div class="flex flex-row items-center gap-2 mx-2 opacity-0 group-hover:opacity-100 transition-opacity">
    <button
      @click.stop="toggleReactions"
      class="w-7 h-7 rounded-full bg-gray-200 hover:bg-gray-300 flex items-center justify-center"
      aria-label="Reakcje"
    >
      <span>😊</span>
    </button>
    <button
      @click="emit('reply')"
      class="w-7 h-7 rounded-full bg-gray-200 hover:bg-gray-300 flex items-center justify-center"
      aria-label="Odpowiedz"
    >
      <ReplyIcon :size="18" />
    </button>
  </div>

  <div
    v-if="showReactions"
    class="absolute bottom-full mb-2 left-0 flex gap-1 p-2 bg-white rounded-full shadow-lg z-20 border border-gray-100"
  >
    <span
      v-for="emoji in reactionsList"
      :key="emoji"
      class="cursor-pointer text-lg hover:scale-125 transition-transform"
      @click.stop="selectReaction(emoji)"
    >
      {{ emoji }}
    </span>
  </div>
</template>
