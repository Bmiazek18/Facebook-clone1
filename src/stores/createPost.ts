import { defineStore } from 'pinia';
import { ref,computed } from 'vue';
import type { User } from '@/data/users';
import type { LocationResult } from '@/components/LocationSelector.vue';

export const useCreatePostStore = defineStore('createPost', () => {
  // --- STATE (Stan) ---
  const taggedUsers = ref<User[]>([]);
  const selectedLocation = ref<LocationResult | null>(null);
  const selectedGif = ref<string | null>(null);
  const selectedPrivacy = ref<string>('friends');
  const imageToEdit = ref<string | null>(null);
  const postContent = ref<string>('');
  const selectedImage = ref<string | null>(null);
  const selectedCardBgId = ref<number>(0);

  // --- ACTIONS (Akcje) ---
  function setTaggedUsers(users: User[]) {
    taggedUsers.value = users;
  }

  function setLocation(location: LocationResult | null) {
    selectedLocation.value = location;
  }

  function setGif(url: string | null) {
    selectedGif.value = url;
  }

  function setPrivacy(privacy: string) {
    selectedPrivacy.value = privacy;
  }

  function setImageToEdit(url: string | null) {
    imageToEdit.value = url;
  }

  function setPostContent(content: string) {
    postContent.value = content;
  }

  function setSelectedImage(url: string | null) {
    selectedImage.value = url;
  }

  function setSelectedCardBgId(id: number) {
    selectedCardBgId.value = id;
  }

  // Funkcja resetująca stan (oprócz privacy)
  function reset() {
    taggedUsers.value = [];
    selectedLocation.value = null;
    selectedGif.value = null;
    imageToEdit.value = null;
    postContent.value = '';
    selectedImage.value = null;
    selectedCardBgId.value = 1; // Reset do wartości domyślnej
    // Nie resetujemy privacy, ponieważ jest ładowane z localStorage
  }
  const hasUnsavedChanges = computed(() => {
      return (
        taggedUsers.value.length > 0 ||
        selectedLocation.value !== null ||
        selectedGif.value !== null ||
        imageToEdit.value !== null ||
        postContent.value !== '' ||
        selectedImage.value !== null
      );
    });
  // --- RETURN (Udostępnienie publiczne) ---
  return {
    taggedUsers,
    selectedLocation,
    selectedGif,
    selectedPrivacy,
    imageToEdit,
    postContent,
    selectedImage,
    selectedCardBgId,
  hasUnsavedChanges,
    setTaggedUsers,
    setLocation,
    setGif,
    setPrivacy,
    setImageToEdit,
    setPostContent,
    setSelectedImage,
    setSelectedCardBgId,
    reset,
  };
});
