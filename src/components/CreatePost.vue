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
          @navigate="handleNavigation"
          @back="handleNavigationBack"
          @publish="(content) => emit('publish', content)"
          @close="() => emit('close')"
        />
        <PrivacySelector
          v-else-if="currentView === 'privacy'"
          key="privacy"
          class="view-container bg-white"
          data-view="privacy"
          @navigate="handleNavigation"
          @back="handleNavigationBack"
        />
      </Transition>
    </div>
  </div>
</template>

<script setup lang="ts">
import { type DefineComponent } from 'vue';

// --- Import Komponentów Widoków ---
import PostCreator from './PostCreator.vue';
import PrivacySelector from './PrivacySelector.vue';

// --- Import Animacji ---
import '@/assets/animations/slideTransition.css';

// --- Import Composables ---
import { useSlideTransition } from '@/composables/useSlideTransition';

import type { PostData } from '@/types/StoryElement';

// Props dla udostępnianego posta
defineProps<{
  sharedPost?: PostData | null;
}>();

const emit = defineEmits<{
  (e: 'publish', content: string): void;
  (e: 'close'): void;
}>();

// --- Use Composables ---
const { wrapperRef, currentView, previousView, updateHeight, transitionName } = useSlideTransition();

// Mapowanie komponentów do nazw (kept for reference if needed)
const viewComponents: Record<string, DefineComponent> = {
  creator: PostCreator as DefineComponent,
  privacy: PrivacySelector as DefineComponent,
};

// --- Logika Nawigacji ---

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
    currentView.value = 'creator'; // Zawsze wracamy do widoku tworzenia posta
    emit('close'); // Emituj event zamknięcia
};
</script>
