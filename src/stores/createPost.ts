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
  parent: string | undefined;
  item: {
    label: string;
    emoji: string;
  }
}

interface Poll {
  question: string;
  options: { text: string }[];
}

export const useCreatePostStore = defineStore('createPost', () => {
  // --- STATE (Stan) ---
  const taggedUsers = ref<User[]>([]);
  const selectedLocation = ref<LocationResult | null>(null);
  const selectedGif = ref<string | null>(null);
  const selectedPrivacy = ref<string>('friends');
  const imageToEdit = ref<SelectedImage | null>(null);
  const imageIndexToEdit = ref<number | null>(null);
  const videoToEdit = ref<string | null>(null);
  const postVideoUrl = ref<string | null>(null);
  const postContent = ref<string>('');
  const selectedImages = ref<SelectedImage[]>([]);
  const selectedCardBgId = ref<number>(0);
  const initialView = ref<string | null>(null);
  const selectedFeeling = ref<Feeling | null>(null);
  const selectedActivity = ref<Activity | null>(null);
  const targetId = ref<string | null>(null)
  const targetType = ref<'User' | 'Group' | null>(null)
  const isAnonymous = ref<boolean>(false); // Add isAnonymous state
  const poll = ref<Poll | null>(null);


  // --- ACTIONS (Akcje) ---
  function setTarget (id: string | null, type: 'User' | 'Group' | null) {
    targetId.value = id
    targetType.value = type
  }
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

  function setImageToEdit(image: SelectedImage | null, index: number | null = null) {
    imageToEdit.value = image;
    imageIndexToEdit.value = index;
  }

  function setVideoToEdit(url: string | null) {
    videoToEdit.value = url;
  }

  function setPostContent(content: string) {
    postContent.value = content;
  }

  function addSelectedImage(image: SelectedImage) {
    selectedImages.value.push(image);
  }

  function removeSelectedImage(index: number) {
    selectedImages.value.splice(index, 1);
  }

  function setSelectedCardBgId(id: number) {
    selectedCardBgId.value = id;
  }

  function updateImageAltText(index: number, altText: string) {
    if (selectedImages.value[index]) {
      selectedImages.value[index].altText = altText;
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
  function setPostVideoUrl(video){
postVideoUrl.value = video
  }

  function setIsAnonymous(value: boolean) { // Add setIsAnonymous action
    isAnonymous.value = value;
  }

  function setPoll(newPoll: Poll | null) {
    poll.value = newPoll;
  }

  // Funkcja resetująca stan (oprócz privacy)
  function reset() {
    taggedUsers.value = [];
    selectedLocation.value = null;
    selectedGif.value = null;
    imageToEdit.value = null;
    videoToEdit.value = null;
    postVideoUrl.value = null;
    postContent.value = '';
    selectedImages.value = [];
initialView.value = null;
    selectedCardBgId.value = 0; // Reset do wartości domyślnej
    selectedFeeling.value = null;
    selectedActivity.value = null;
    targetId.value = null
    targetType.value = null
    isAnonymous.value = false; // Reset isAnonymous
    poll.value = null;

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
        selectedImages.value.length > 0 ||
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
    imageIndexToEdit,
    videoToEdit,
    postVideoUrl,
    postContent,
    selectedImages,
    selectedCardBgId,
    initialView,
    selectedFeeling,
    selectedActivity,
    targetId,
    targetType,
    hasUnsavedChanges,
    isAnonymous, // Expose isAnonymous
    poll,
    setTarget,
    setTaggedUsers,
    addTaggedUser,
    setLocation,
    setGif,
    setPrivacy,
    setImageToEdit,
    setVideoToEdit,
    setPostContent,
    setPostVideoUrl,
    addSelectedImage,
    removeSelectedImage,
    updateImageAltText,
    setSelectedCardBgId,
    setInitialView,
    setSelectedFeeling,
    setSelectedActivity,
    setIsAnonymous, // Expose setIsAnonymous
    setPoll,
    reset,
  };
});
