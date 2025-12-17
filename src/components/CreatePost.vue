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
import { ref, type Ref, type DefineComponent, computed, onMounted,  onBeforeUnmount } from 'vue';

// --- Import Komponentów Widoków ---
import PostCreator from './PostCreator.vue';
import PrivacySelector from './PrivacySelector.vue';

import type { PostData } from '@/types/StoryElement';

// Props dla udostępnianego posta
defineProps<{
  sharedPost?: PostData | null;
}>();

const emit = defineEmits<{
  (e: 'publish', content: string): void;
}>();

// --- Stan Widoków ---
const currentView: Ref<string> = ref('creator'); // Początkowy widok to 'creator'
const previousView: Ref<string | null> = ref(null);

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
};

// --- Logika Animacji Wysokości (TWOJA LOGIKA Z WATCH) ---

const wrapperRef = ref<HTMLElement | null>(null);

const getActiveViewElement = (): HTMLElement | null => {
  if (!wrapperRef.value) return null;
  // Szukamy elementu z atrybutem data-view wewnątrz Transition
  return wrapperRef.value.querySelector<HTMLElement>('[data-view]') ?? null;
};

const updateHeight = async () => {
  const active = getActiveViewElement();
  if (!wrapperRef.value) return;

  if (active) {

    wrapperRef.value.style.height = Math.ceil(active.clientHeight) + 'px';
  } else {
    wrapperRef.value.style.height = '0px';
  }
};

let resizeObserver: ResizeObserver | null = null;

onMounted(() => {
  updateHeight();

  if (wrapperRef.value && 'ResizeObserver' in window) {
    resizeObserver = new ResizeObserver(updateHeight);
    resizeObserver.observe(wrapperRef.value);
  }

  window.addEventListener('resize', updateHeight);
});

onBeforeUnmount(() => {
  window.removeEventListener('resize', updateHeight);
  if (resizeObserver) {
    resizeObserver.disconnect();
    resizeObserver = null;
  }
});




// Obliczenie nazwy przejścia (animacja slajdu)
const transitionName = computed(() => {
  const isGoingForward = currentView.value === 'privacy' && previousView.value === 'creator';
  const isGoingBackward = currentView.value === 'creator' && previousView.value === 'privacy';

  if (isGoingForward) {
    return 'slide-left'; // creator -> privacy
  }
  if (isGoingBackward) {
    return 'slide-right'; // privacy -> creator
  }
  return 'slide-left'; // Domyślnie
});
</script>

<style scoped>
/* Stylizacja CSS dla animacji przejścia i dostosowania wysokości */
.transition-wrapper {
  position: relative;
  width: 100%;
  overflow: hidden;
  transition: height 0.2s ease;
  min-height: 48px;
}

/* KLUCZOWA ZMIANA: position: absolute tylko dla aktywnej tranzycji */
.view-container {
  /* Zapewniamy, że komponenty wpływają na wysokość wrapper'a poza tranzycją */
  position: relative;
  width: 100%;
}

.slide-left-enter-active,
.slide-left-leave-active,
.slide-right-enter-active,
.slide-right-leave-active {
  transition: transform 0.28s ease, opacity 0.28s ease;
  will-change: transform, opacity;

  /* position: absolute jest konieczne dla animacji slajdu */
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
}
.slide-left-leave-active,
.slide-right-leave-active {
    z-index: 1;
}
.slide-left-enter-from { transform: translateX(100%); opacity: 0; }
.slide-left-enter-to { transform: translateX(0%); opacity: 1; }
.slide-left-leave-from { transform: translateX(0%); opacity: 1; }
.slide-left-leave-to { transform: translateX(-100%); opacity: 0; }

.slide-right-enter-from { transform: translateX(-100%); opacity: 0; }
.slide-right-enter-to { transform: translateX(0%); opacity: 1; }
.slide-right-leave-from { transform: translateX(0%); opacity: 1; }
.slide-right-leave-to { transform: translateX(100%); opacity: 0; }
</style>
