<template>
  <div class="w-[500px]  p-4 mx-auto rounded-xl relative overflow-hidden ">
    <div class="transition-wrapper" ref="wrapperRef">
      <Transition :name="transitionName" mode="out-in" @before-enter="updateHeight()">
        <PostCreator
          v-if="currentView === 'creator'"
          key="creator"
          class="view-container bg-white"
          data-view="creator"
          :shared-post="sharedPost"
          :tagged-users="taggedUsers"
          :selected-location="selectedLocation"
          @navigate="handleNavigation"
          @back="handleNavigationBack"
          @publish="(content) => emit('publish', content)"
          @close="() => emit('close')"
          @updateHeight="updateHeight"
          @openTagUsers="openTagUsers"
          @openLocation="openLocation"
        />
        <PrivacySelector
          v-else-if="currentView === 'privacy'"
          key="privacy"
          class="view-container bg-white"
          data-view="privacy"
          @navigate="handleNavigation"
          @back="handleNavigationBack"
        />
        <TagUsers
          v-else-if="currentView === 'tagUsers'"
          key="tagUsers"
          class="view-container bg-white"
          data-view="tagUsers"
          :initial-selected="taggedUsers"
          @back="backToCreator"
          @confirm="handleTagUsersConfirm"
        />
        <LocationSelector
          v-else-if="currentView === 'location'"
          key="location"
          class="view-container bg-white"
          data-view="location"
          @back="backToCreator"
          @confirm="handleLocationConfirm"
        />
      </Transition>
    </div>
  </div>
</template>

<script setup lang="ts">
import { type DefineComponent, ref } from 'vue';
import PostCreator from './PostCreator.vue';
import PrivacySelector from './PrivacySelector.vue';
import TagUsers from './TagUsers.vue';
import LocationSelector from './LocationSelector.vue';
import '@/assets/animations/slideTransition.css';
import { useSlideTransition } from '@/composables/useSlideTransition';
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

const viewComponents: Record<string, DefineComponent> = {
  creator: PostCreator as DefineComponent,
  privacy: PrivacySelector as DefineComponent,
};

const handleNavigation = (viewName: string) => {
  if (viewComponents[viewName]) {
    previousView.value = currentView.value;
    currentView.value = viewName;
  } else {
    console.log(`Akcja poza nawigacją widoku: ${viewName}`);
  }
};

const handleNavigationBack = () => {
    previousView.value = currentView.value;
    currentView.value = 'creator';
    emit('close');
};

// --- Tagowanie użytkowników ---
const taggedUsers = ref<User[]>([]);
const openTagUsers = () => {
  previousView.value = currentView.value;
  currentView.value = 'tagUsers';
};
const backToCreator = () => {
  currentView.value = 'creator';
};
const handleTagUsersConfirm = (users: User[]) => {
  taggedUsers.value = users;
  currentView.value = 'creator';
};

// --- Wybór lokalizacji ---
const selectedLocation = ref(null);
const openLocation = () => {
  previousView.value = currentView.value;
  currentView.value = 'location';
};
const handleLocationConfirm = (location) => {
  selectedLocation.value = location;
  currentView.value = 'creator';
};
</script>
