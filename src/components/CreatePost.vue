<script setup lang="ts">
import { type DefineComponent, ref, onUnmounted } from 'vue';
import { storeToRefs } from 'pinia';
import PostCreator from './PostCreator.vue';
import PrivacySelector from './PrivacySelector.vue';
import TagUsers from './TagUsers.vue';
import LocationSelector, { type LocationResult } from './LocationSelector.vue';
import GifSelector from './GifSelector.vue';
import ImageEditor from './ImageEditor.vue';
import '@/assets/animations/slideTransition.css';
import { useSlideTransition } from '@/composables/useSlideTransition';
import { useCreatePostStore } from '@/stores/createPost';
import type { PostData } from '@/types/StoryElement';
import type { User } from '@/data/users';

defineProps<{
  sharedPost?: PostData | null;
}>();

const emit = defineEmits<{
  (e: 'publish', content: string): void;
  (e: 'close'): void;
}>();

const { wrapperRef, currentView, previousView, updateHeight, transitionName } = useSlideTransition();
const createPostStore = useCreatePostStore();
const {
  taggedUsers,
  selectedLocation,
  selectedGif,
  selectedPrivacy,
  imageToEdit,
} = storeToRefs(createPostStore);

const viewComponents: Record<string, DefineComponent> = {
  creator: PostCreator as DefineComponent,
  privacy: PrivacySelector as DefineComponent,
  imageEditor: ImageEditor as DefineComponent,
};

const handleNavigation = (viewName: string, data: string | null = null) => {
  if (viewName === 'imageEditor' && data) {
    createPostStore.setImageToEdit(data);
  }
  if (viewComponents[viewName]) {
    previousView.value = currentView.value;
    currentView.value = viewName;
  } else {
    console.log(`Akcja poza nawigacją widoku: ${viewName}`);
  }
};

const handlePublish = (content: string) => {
  emit('publish', content);
  createPostStore.reset();
};

const handleClose = () => {
  emit('close');
  createPostStore.reset();
};

const handleNavigationBack = () => {
    previousView.value = currentView.value;
    currentView.value = 'creator';
    handleClose();
};

const handleImageEditorBack = () => {
  createPostStore.setImageToEdit(null);
  currentView.value = 'creator';
};

const handleImageEdited = (editedImageUrl: string) => {
  createPostStore.setSelectedImage(editedImageUrl);
  createPostStore.setImageToEdit(null);
  currentView.value = 'creator';
};

// --- Tagowanie użytkowników ---
const openTagUsers = () => {
  previousView.value = currentView.value;
  currentView.value = 'tagUsers';
};
const backToCreator = () => {
  currentView.value = 'creator';
};
const handleTagUsersConfirm = (users: User[]) => {
  createPostStore.setTaggedUsers(users);
  currentView.value = 'creator';
};

// --- Wybór lokalizacji ---
const openLocation = () => {
  previousView.value = currentView.value;
  currentView.value = 'location';
};

// --- Wybór GIF-a ---
const openGifSelector = () => {
  previousView.value = currentView.value;
  currentView.value = 'gifSelector';
};
const handleRemoveGif = () => {
  createPostStore.setGif(null);
};

// --- Privacy selection ---
// load saved default privacy (if any)
try {
  const saved = localStorage.getItem('fc_default_privacy');
  if (saved) createPostStore.setPrivacy(saved);
} catch { /* ignore on SSR or if localStorage not available */ }

const handlePrivacyConfirm = (payload: { id: string; setDefault: boolean }) => {
  console.log('handlePrivacyConfirm received payload:', payload);
  console.log('Store privacy before setting:', createPostStore.selectedPrivacy);
  createPostStore.setPrivacy(payload.id);
  console.log('Store privacy after setting:', createPostStore.selectedPrivacy);
  if (payload.setDefault) {
    try { localStorage.setItem('fc_default_privacy', payload.id); } catch {}
  }
  // return to creator view
  currentView.value = 'creator';
};


</script>

<template>
  <div :class="{'w-[1200px]': currentView === 'imageEditor',}"  class='p-4 w-[500px] mx-auto rounded-xl relative overflow-hidden' >
    <div class="transition-wrapper" ref="wrapperRef">
      <Transition :name="transitionName" mode="out-in" @before-enter="updateHeight()">
        <PostCreator
          v-if="currentView === 'creator'"
          key="creator"
          class="view-container bg-white"
          data-view="creator"
          :shared-post="sharedPost"
          @navigate="handleNavigation"
          @back="handleNavigationBack"
          @publish="handlePublish"
          @close="handleClose"
          @updateHeight="updateHeight"
          @openTagUsers="openTagUsers"
          @openLocation="openLocation"
          @openGifSelector="openGifSelector"
          @removeGif="handleRemoveGif"
        />
        <PrivacySelector
          v-else-if="currentView === 'privacy'"
          key="privacy"
          class="view-container bg-white"
          data-view="privacy"
          @navigate="handleNavigation"
          @back="handleNavigationBack"
          @confirm="handlePrivacyConfirm"
        />
        <TagUsers
          v-else-if="currentView === 'tagUsers'"
          key="tagUsers"
          class="view-container bg-white"
          data-view="tagUsers"
          @back="backToCreator"
          @confirm="handleTagUsersConfirm"
        />
        <LocationSelector
          v-else-if="currentView === 'location'"
          key="location"
          class="view-container bg-white"
          data-view="location"
          @back="backToCreator"
        />
        <GifSelector
          v-else-if="currentView === 'gifSelector'"
          key="gifSelector"
          class="view-container bg-white"
          data-view="gifSelector"
          @back="backToCreator"
        />
        <ImageEditor
          v-else-if="currentView === 'imageEditor'"
          key="imageEditor"
          class="view-container bg-white"
          data-view="imageEditor"
          :initial-image="imageToEdit"
          @back="handleImageEditorBack"
          @done="handleImageEdited"
          @updateHeight="updateHeight"
        />
      </Transition>
    </div>
  </div>
</template>
