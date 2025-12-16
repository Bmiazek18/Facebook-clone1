import { ref, computed } from 'vue';
import { defineStore } from 'pinia';

type ChatId = string | number;

export const useChatStore = defineStore('chat', () => {
  const activeBoxIds = ref<ChatId[]>(['chat-1', 'chat-2']);
  const minimizedBoxCache = ref<ChatId[]>([]);

  const getBoxIds = computed(() => activeBoxIds.value);

  const isMinimized = computed(() => (id: ChatId) => {
    return minimizedBoxCache.value.includes(id);
  });

  function addMessageBox(id: ChatId) {
    if (!activeBoxIds.value.includes(id)) {
      if (activeBoxIds.value.length >= 5) {
        activeBoxIds.value.shift();
      }
      activeBoxIds.value.push(id);
    }

    const cacheIndex = minimizedBoxCache.value.indexOf(id);
    if (cacheIndex > -1) {
      minimizedBoxCache.value.splice(cacheIndex, 1);
    }
  }

  function removeMessageBox(id: ChatId) {
    activeBoxIds.value = activeBoxIds.value.filter(boxId => boxId !== id);
    minimizedBoxCache.value = minimizedBoxCache.value.filter(boxId => boxId !== id);
  }

  function toggleMinimize(id: ChatId) {
    const index = activeBoxIds.value.indexOf(id);

    if (index > -1) {
      activeBoxIds.value.splice(index, 1);
      if (!minimizedBoxCache.value.includes(id)) {
        minimizedBoxCache.value.push(id);
      }
    } else {
      activeBoxIds.value.push(id);
      const cacheIndex = minimizedBoxCache.value.indexOf(id);
      if (cacheIndex > -1) {
        minimizedBoxCache.value.splice(cacheIndex, 1);
      }
    }
  }

  return {
    activeBoxIds,
    minimizedBoxCache,
    getBoxIds,
    isMinimized,
    addMessageBox,
    removeMessageBox,
    toggleMinimize
  };
}, {
  persist: {
    storage: localStorage,
    key: 'chat-boxes-state',
    pick: ['activeBoxIds', 'minimizedBoxCache']
  }
});
