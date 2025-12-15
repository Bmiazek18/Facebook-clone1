import { ref } from 'vue';
import { defineStore } from 'pinia';

export const useTempStoryStore = defineStore('tempStory', () => {
  // stores an object URL for the selected file (transient, not persisted)
  const selectedImage = ref<string | null>(null);

  function setFromFile(file: File) {
    // revoke previous if present
    if (selectedImage.value) {
      try { URL.revokeObjectURL(selectedImage.value); } catch { console.warn('Failed to revoke previous objectURL'); }
    }
    const url = URL.createObjectURL(file);
    selectedImage.value = url;
    // debug aid
    try { console.debug('[tempStory] created objectURL', url); } catch {}
    return selectedImage.value;
  }

  function clear() {
    if (selectedImage.value) {
      try { URL.revokeObjectURL(selectedImage.value); } catch { /* ignore */ }
    }
    selectedImage.value = null;
  }

  return { selectedImage, setFromFile, clear };
});
