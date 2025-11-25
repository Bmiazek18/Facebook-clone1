import { ref, computed } from 'vue';
import { defineStore } from 'pinia';

type ChatId = string | number;

export const useChatStore = defineStore('chat', () => {

  // STATE

  // 1. Lista ID czatów aktualnie WIDOCZNYCH (otwartych/zmaksymalizowanych) w dolnym pasku.
  const activeBoxIds = ref<ChatId[]>(['chat-1', 'chat-2']);

  // 2. Cache ID czatów, które zostały ZMINIMALIZOWANE/UKRYTE, ale są gotowe do szybkiego przywrócenia.
  const minimizedBoxCache = ref<ChatId[]>([]);


  // GETTERS
  const getBoxIds = computed(() => activeBoxIds.value);

  // Getter do sprawdzenia, czy dany box jest w cache (może być przydatny np. dla ikonki w bocznym pasku)
  const isMinimized = computed(() => (id: ChatId) => {
      return minimizedBoxCache.value.includes(id);
  });


  // ACTIONS

  // AKCJA: Dodawanie/Otwieranie MessageBox
  function addMessageBox(id: ChatId) {
    if (!activeBoxIds.value.includes(id)) {
      if (activeBoxIds.value.length >= 5) {
          activeBoxIds.value.shift();
      }
      activeBoxIds.value.push(id);
    }

    // Jeśli otwieramy, usuwamy z cache ukrytych
    const cacheIndex = minimizedBoxCache.value.indexOf(id);
    if (cacheIndex > -1) {
        minimizedBoxCache.value.splice(cacheIndex, 1);
    }
  }

  // AKCJA: Usuwanie/Zamykanie (Przycisk X)
  function removeMessageBox(id: ChatId) {
    activeBoxIds.value = activeBoxIds.value.filter(boxId => boxId !== id);
    // Upewniamy się, że usunęliśmy również z cache ukrytych
    minimizedBoxCache.value = minimizedBoxCache.value.filter(boxId => boxId !== id);
  }

  // AKCJA: Minimalizacja / Maksymalizacja (Przełączanie stanu widoczności)
  function toggleMinimize(id: ChatId) {
    const index = activeBoxIds.value.indexOf(id);

    if (index > -1) {
      // 1. Box jest WIDOCZNY -> MINIMALIZUJ (USUŃ Z OTWARTYCH)
      activeBoxIds.value.splice(index, 1);

      // Dodaj do cache ukrytych (jeśli tam jeszcze nie ma)
      if (!minimizedBoxCache.value.includes(id)) {
        minimizedBoxCache.value.push(id);
      }

    } else {
      // 2. Box jest UKRYTY -> MAKSYMALIZUJ (DODAJ DO OTWARTYCH)

      // Wracamy do widoczności
      activeBoxIds.value.push(id);

      // Usuń z cache ukrytych
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
  // === KONFIGURACJA PERSISTENCJI ===
  persist: {
    storage: localStorage,
    key: 'chat-boxes-state',
    // Upewniamy się, że oba stany widoczności są zapisywane
    paths: ['activeBoxIds', 'minimizedBoxCache']
  }
});
