import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import type { User } from '@/data/users';
import type { LocationResult } from '@/types/Location';
import type { ImageTagType } from '@/types/ImageTag';

interface SelectedImage {
  url: string;
  altText: string;
  tags?: ImageTagType[];
}

interface Feeling {
  emoji: string;
  label: string;
}
interface Activity {
  parent: string;
  item: {
    label: string;
    icon: any;
    color: string;
  }
}

export const useCreatePostStore = defineStore('createPost', () => {
  // --- STATE (Stan) ---
  const taggedUsers = ref<User[]>([]);
  const selectedLocation = ref<LocationResult | null>(null);
  const selectedGif = ref<string | null>(null);
  const selectedPrivacy = ref<string>('friends');
  const imageToEdit = ref<string | null>(null);
  const videoToEdit = ref<string | null>(null);
  const postContent = ref<string>('');
  const selectedImage = ref<SelectedImage | null>(null);
  const selectedCardBgId = ref<number>(0);
  const initialView = ref<string | null>(null);
  const selectedFeeling = ref<Feeling | null>(null);
  const selectedActivity = ref<Activity | null>(null);

  // --- ACTIONS (Akcje) ---
  function setTaggedUsers(users: User[]) {
    taggedUsers.value = users;
  }

  function addTaggedUser(user: User) {
    taggedUsers.value.push(user);
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

  function setVideoToEdit(url: string | null) {
    videoToEdit.value = url;
  }

  function setPostContent(content: string) {
    postContent.value = content;
  }

  function setSelectedImage(image: string | SelectedImage | null) {
    if (typeof image === 'string') {
      selectedImage.value = { url: image, altText: '', tags: [] };
    } else if (image === null) {
      selectedImage.value = null;
    } else {
      selectedImage.value = image;
    }
  }

  function setSelectedCardBgId(id: number) {
    selectedCardBgId.value = id;
  }

  function setAltText(text: string) {
    if (selectedImage.value) {
      selectedImage.value.altText = text;
    }
  }

  function setInitialView(view: string | null) {
    initialView.value = view;
  }

  function setSelectedFeeling(feeling: Feeling | null) {
    selectedFeeling.value = feeling;
  }

  function setSelectedActivity(activity: Activity | null) {
    selectedActivity.value = activity;
  }

  // Funkcja resetująca stan (oprócz privacy)
  function reset() {
    taggedUsers.value = [];
    selectedLocation.value = null;
    selectedGif.value = null;
    imageToEdit.value = null;
    videoToEdit.value = null;
    postContent.value = '';
    selectedImage.value = null;
    selectedCardBgId.value = 0; // Reset do wartości domyślnej
    selectedFeeling.value = null;
    selectedActivity.value = null;
    // Nie resetujemy privacy, ponieważ jest ładowane z localStorage
  }
  const hasUnsavedChanges = computed(() => {
      return (
        taggedUsers.value.length > 0 ||
        selectedLocation.value !== null ||
        selectedGif.value !== null ||
        imageToEdit.value !== null ||
        videoToEdit.value !== null ||
        postContent.value !== '' ||
        (selectedImage.value !== null && (selectedImage.value.url !== '' || selectedImage.value.altText !== '' || (selectedImage.value.tags && selectedImage.value.tags.length > 0))) ||
        selectedFeeling.value !== null ||
        selectedActivity.value !== null
      );
    });
  // --- RETURN (Udostępnienie publiczne) ---
  return {
    taggedUsers,
    selectedLocation,
    selectedGif,
    selectedPrivacy,
    imageToEdit,
    videoToEdit,
    postContent,
    selectedImage,
    selectedCardBgId,
    initialView,
    selectedFeeling,
    selectedActivity,
    hasUnsavedChanges,
    setTaggedUsers,
    addTaggedUser,
    setLocation,
    setGif,
    setPrivacy,
    setImageToEdit,
    setVideoToEdit,
    setPostContent,
    setSelectedImage,
    setSelectedCardBgId,
    setAltText,
    setInitialView,
    setSelectedFeeling,
    setSelectedActivity,
    reset,
  };
});
