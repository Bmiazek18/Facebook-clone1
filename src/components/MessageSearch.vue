
<template>
  <div class="w-full h-full max-w-md mx-auto overflow-hidden h-[800px] flex flex-col font-sans">

    <div class="p-3 border-b border-gray-100 flex items-center gap-3">
      <button @click="emit('close')" class="text-black hover:bg-gray-100 p-2 rounded-full transition duration-200">
        <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="2.5" stroke="currentColor" class="w-5 h-5">
          <path stroke-linecap="round" stroke-linejoin="round" d="M10.5 19.5L3 12m0 0l7.5-7.5M3 12h18" />
        </svg>
      </button>
      <h1 class="text-lg font-bold text-gray-900 tracking-tight">Szukaj</h1>
    </div>

    <div class="px-4 py-2">
      <div class="flex items-center bg-gray-100 rounded-full px-3 py-1.5 transition-colors focus-within:bg-gray-200/70">
        <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 text-gray-500 mr-2 shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
        </svg>

        <input
          v-model="searchQuery"
          type="text"
          placeholder="Szukaj"
          class="bg-transparent border-none outline-none text-[15px] text-gray-900 w-full placeholder-gray-500 h-8"
        />

        <div v-if="searchQuery" class="bg-gray-300/60 text-gray-600 text-[11px] font-semibold px-2 py-0.5 rounded-md whitespace-nowrap ml-2">
          Ponad {{ filteredMessages.length }} wyników
        </div>

        <button
          v-if="searchQuery"
          @click="clearSearch"
          class="ml-2 bg-gray-300 rounded-full p-1 hover:bg-gray-400 transition shrink-0"
        >
          <svg xmlns="http://www.w3.org/2000/svg" class="h-3 w-3 text-gray-600" viewBox="0 0 20 20" fill="currentColor">
            <path fill-rule="evenodd" d="M4.293 4.293a1 1 0 011.414 0L10 8.586l4.293-4.293a1 1 0 111.414 1.414L11.414 10l4.293 4.293a1 1 0 01-1.414 1.414L10 11.414l-4.293 4.293a1 1 0 01-1.414-1.414L8.586 10 4.293 5.707a1 1 0 010-1.414z" clip-rule="evenodd" />
          </svg>
        </button>
      </div>
    </div>

    <div class="flex-1 overflow-y-auto px-2 pb-4 scrollbar-hide">
      <div
        v-for="msg in filteredMessages"
        :key="msg.id"
        class="flex items-center gap-3 p-2.5 rounded-lg hover:bg-gray-100 cursor-pointer transition-colors group"
        @click="goToMessage(msg)"
      >
        <div class="relative shrink-0">
          <img :src="msg.avatar || 'https://i.pravatar.cc/150?img=12'" :alt="msg.name || msg.sender" class="w-12 h-12 rounded-full object-cover border border-gray-100 shadow-sm" />
        </div>

        <div class="flex-1 min-w-0 flex flex-col justify-center h-12">
          <h3 class="text-[15px] font-semibold text-gray-900 leading-none mb-1">
            {{ msg.name || msg.sender }}
          </h3>

          <div class="text-[13px] text-gray-500 truncate leading-snug">
            <span v-html="highlightText(msg.content)"></span>
            <span class="text-gray-400 font-normal"> · {{ formatTime(msg.time) }}</span>
          </div>
        </div>
      </div>

      <div v-if="filteredMessages.length === 0" class="flex flex-col items-center justify-center h-40 text-gray-500">
        <p class="text-sm">Brak wyników wyszukiwania.</p>
      </div>
    </div>

  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import { useConversationsStore } from '@/stores/conversations';

const emit = defineEmits<{
  (e: 'close'): void
  (e: 'go-to-message', payload: { id: number; chatId?: string | number }): void
}>();

const props = withDefaults(defineProps<{ boxId?: string | number }>(), { boxId: undefined });

const convStore = useConversationsStore();

const searchQuery = ref<string>('');

// Source messages: prefer store-backed messages for given chatId, otherwise fall back to sampleMessages
const sourceMessages = computed(() => {
  if (props.boxId !== undefined && props.boxId !== null) {
    const msgs = convStore.getMessagesByChatId(Number(props.boxId));
    if (msgs && msgs.length) return msgs;
  }
  return [];
});
const filteredMessages = computed(() => {
  if (!searchQuery.value) return [];
  const query = searchQuery.value.toLowerCase();
  return sourceMessages.value.filter((msg) => {
    const content = String(msg.content ?? '').toLowerCase();
    const textMatch = content.includes(query);

    return textMatch ;
  });
});

// 5. Funkcja podkreślająca (Highlighting)
const highlightText = (text: string): string => {
  if (!searchQuery.value) return text;
  const regex = new RegExp(`(${searchQuery.value})`, 'gi');
  return String(text).replace(regex, '<span class="font-bold text-gray-900">$1</span>');
};

const clearSearch = (): void => { searchQuery.value = ''; };

const formatTime = (t: number | string | undefined) => {
  if (!t) return '';
  const date = typeof t === 'number' ? new Date(t) : new Date(String(t));
  return date.toLocaleString();
};

function goToMessage(msg) {
  // emit message id and chatId (if present)
  emit('go-to-message', { id: msg.id, chatId: props.boxId });
}
</script>

<style scoped>
/* Opcjonalne ukrycie scrollbara dla czystszego wyglądu (Chrome/Safari/Opera) */
.scrollbar-hide::-webkit-scrollbar {
    display: none;
}
.scrollbar-hide {
    -ms-overflow-style: none;
    scrollbar-width: none;
}
</style>
