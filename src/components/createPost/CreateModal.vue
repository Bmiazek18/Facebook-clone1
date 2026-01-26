<script setup lang="ts">
import { computed,watch, onMounted, type Component } from 'vue';
import { useCreatePostStore } from '@/stores/createPost';
import PostCreator from './tabs/PostCreator.vue';
import PrivacySelector from '@/components/common/PrivacySelector.vue';
import TagUsers from './tabs/TagUsers.vue';
import LocationSelector from './tabs/LocationSelector.vue';
import GifSelector from '@/components/common/GifSelector.vue';
import ImageEditor from './tabs/ImageEditor.vue';
import VideoEditor from './tabs/VideoEditor.vue';
import FeelingModal from './tabs/FeelingModal.vue';
import '@/assets/animations/slideTransition.css';
import { useSlideTransition } from '@/composables/useSlideTransition';
import { useI18n } from 'vue-i18n';
const { t } = useI18n();
import type { PostData } from '@/types/StoryElement';
import type { User } from '@/data/users';

defineProps<{
  sharedPost?: PostData | null;
  sharedEventId?: string;
}>();


const emit = defineEmits<{
  (e: 'close'): void;
  (e: 'publish', content: string): void;
  (e: 'update:showBack', value: boolean): void;
  (e: 'update:title', value: string): void;
}>();

onMounted(() => {
  if (createPostStore.initialView === 'feeling') {
    openFeelingView();
    createPostStore.setInitialView(null);
  }
});
const { wrapperRef, currentView, previousView, updateHeight, transitionName } = useSlideTransition();
const createPostStore = useCreatePostStore();

const showBack = computed(() => currentView.value !== 'creator');
watch(showBack, (newValue) => {
  emit('update:showBack', newValue);
});

const viewTitles: Record<string, string> = {
  creator: t('post.createPost'),
  privacy: t('post.selectPrivacy'),
  tagUsers: t('post.tagUsers'),
  location: t('post.addLocation'),
  gifSelector: t('post.selectGif'),
  imageEditor: t('post.editImage'),
  videoEditor: t('post.editVideo'),
  feeling: t('post.feelingActivity'),
};

const currentTitle = computed(() => viewTitles[currentView.value] || '');
watch(currentTitle, (newTitle) => {
  emit('update:title', newTitle);
});

const openFeelingView = () => {
  previousView.value = currentView.value;
  currentView.value = 'feeling';
};



const viewComponents: Record<string, Component> = {
  creator: PostCreator,
  privacy: PrivacySelector,
  imageEditor: ImageEditor,
  videoEditor: VideoEditor,
  feeling: FeelingModal,
};

const goBack = () => {
  if (previousView.value) {
    currentView.value = previousView.value;
    previousView.value = null; // Clear previous view after going back
  }
};
defineExpose({ goBack });

const handleNavigation = (viewName: string, data: string | null = null) => {
  if (viewName === 'imageEditor' && data) {
    const existingAltText = createPostStore.selectedImage?.altText || '';
    createPostStore.setImageToEdit({ url: data, altText: existingAltText });
  }
  if (viewName === 'videoEditor' && data) {
    createPostStore.setVideoToEdit(data);
  }
  if (viewComponents[viewName]) {
    previousView.value = currentView.value;
    currentView.value = viewName;
  } else {
    console.log(`${t('post.actionOutsideViewNavigation')}: ${viewName}`);
  }
};

const handleClose = () => {
  emit('close');
  createPostStore.reset();
};

const handleImageEditorBack = () => {
  createPostStore.setImageToEdit(null);
  currentView.value = 'creator';
};

const handleImageEdited = (editedImageUrl: string) => {
  if (createPostStore.selectedImage) {
    createPostStore.selectedImage.url = editedImageUrl;
  }
  createPostStore.setImageToEdit(null);
  currentView.value = 'creator';
};

const handleVideoEditorBack = () => {
  createPostStore.setVideoToEdit(null);
  currentView.value = 'creator';
};

const handleVideoEdited = (editedVideoUrl: string) => {
  createPostStore.setSelectedImage(editedVideoUrl);
  createPostStore.setVideoToEdit(null);
  currentView.value = 'creator';
};

// --- Tagowanie użytkowników ---
const openTagUsers = () => {
  previousView.value = currentView.value;
  currentView.value = 'tagUsers';
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

const handleGifSelect = (url: string) => {
  createPostStore.setGif(url);
  currentView.value = 'creator';
};


try {
  const saved = localStorage.getItem('fc_default_privacy');
  if (saved) createPostStore.setPrivacy(saved);
} catch { /* ignore on SSR or if localStorage not available */ }

const handlePrivacyConfirm = (payload: { id: string; setDefault: boolean }) => {

  createPostStore.setPrivacy(payload.id);
  console.log('Store privacy after setting:', createPostStore.selectedPrivacy);
  if (payload.setDefault) {
    try { localStorage.setItem('fc_default_privacy', payload.id); } catch {}
  }
  // return to creator view
  currentView.value = 'creator';
};

interface Feeling {
  emoji: string;
  label: string;
}

interface SubActivity {
  label: string;
  emoji: string;
}

type FeelingPayload = { type: 'feeling'; data: Feeling };
type ActivityPayload = { type: 'activity'; data: { parent: string | undefined; item: SubActivity } };

const handleFeelingOrActivitySelected = (payload: FeelingPayload | ActivityPayload) => {
  if (payload.type === 'feeling') {
    createPostStore.setSelectedFeeling(payload.data);
    createPostStore.setSelectedActivity(null);
  } else if (payload.type === 'activity') {
    createPostStore.setSelectedActivity(payload.data);
    createPostStore.setSelectedFeeling(null);
  }
  currentView.value = 'creator';
};

const handleInternalPublish = (content: string) => {
  emit('publish', content);
};

</script>

<template>
  <div :class="{'w-full lg:w-300': currentView === 'imageEditor' || currentView === 'videoEditor',}"  class='p-2 sm:p-4 w-full sm:w-125 mx-auto rounded-xl relative overflow-hidden' >
    <div class="transition-wrapper" ref="wrapperRef">
      <Transition :name="transitionName" mode="out-in" @before-enter="updateHeight()">
        <PostCreator
          v-if="currentView === 'creator'"
          key="creator"
          class="view-container bg-theme-bg-secondary"
          data-view="creator"
          :shared-post="sharedPost"
          :shared-event-id="sharedEventId"

          @navigate="handleNavigation"
          @publish="handleInternalPublish"

          @close="handleClose"
          @updateHeight="updateHeight"
          @openTagUsers="openTagUsers"
          @openLocation="openLocation"
          @openGifSelector="openGifSelector"
          @removeGif="handleRemoveGif"
          @open-feeling-selector="openFeelingView"

        />
        <PrivacySelector
          v-else-if="currentView === 'privacy'"
          key="privacy"
          class="view-container bg-theme-bg-secondary"
          data-view="privacy"
          @navigate="handleNavigation"
          @confirm="handlePrivacyConfirm"
        />
        <TagUsers
          v-else-if="currentView === 'tagUsers'"
          key="tagUsers"
          class="view-container bg-theme-bg-secondary"
          data-view="tagUsers"
          @confirm="handleTagUsersConfirm"
        />
        <LocationSelector
          v-else-if="currentView === 'location'"
          key="location"
          class="view-container bg-theme-bg-secondary"
          data-view="location"
        />
        <GifSelector
          v-else-if="currentView === 'gifSelector'"
          key="gifSelector"
          class="view-container bg-theme-bg-secondary"
          data-view="gifSelector"
          @select="handleGifSelect"
        />
        <ImageEditor
          v-else-if="currentView === 'imageEditor'"
          key="imageEditor"
          class="view-container bg-theme-bg-secondary"
          data-view="imageEditor"
          @back="handleImageEditorBack"
          @done="handleImageEdited"
          @updateHeight="updateHeight"
        />
        <VideoEditor
          v-else-if="currentView === 'videoEditor' && createPostStore.videoToEdit"
          key="videoEditor"
          class="view-container bg-theme-bg-secondary"
          data-view="videoEditor"
          @back="handleVideoEditorBack"
          @done="handleVideoEdited"
          @updateHeight="updateHeight"
        />
        <FeelingModal
          v-else-if="currentView === 'feeling'"
          key="feeling"
          class="view-container bg-theme-bg-secondary"
          data-view="feeling"
          @feeling-selected="handleFeelingOrActivitySelected"
        />
      </Transition>
    </div>
  </div>
</template>
