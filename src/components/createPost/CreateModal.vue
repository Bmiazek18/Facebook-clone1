<script setup lang="ts">
import { computed, watch, onMounted, onBeforeUnmount, type Component, ref } from 'vue';
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

// --- Typy ---
import type { PostData } from '@/types/StoryElement';


type ViewKey =
  | 'creator' | 'privacy' | 'tagUsers' | 'location'
  | 'gifSelector' | 'imageEditor' | 'videoEditor' | 'feeling';

const props = defineProps<{
  sharedPost?: PostData | null;
  sharedEventId?: string;
  targetId?: string;
  targetType?: 'User' | 'Group' | 'Event';
}>();

const emit = defineEmits<{
  (e: 'close'): void;
  (e: 'publish', content: string): void;
  (e: 'update:showBack', value: boolean): void;
  (e: 'update:title', value: string): void;
}>();

const { t } = useI18n();
const createPostStore = useCreatePostStore();

// --- Konfiguracja Widoków ---
const VIEW_CONFIG: Record<ViewKey, { component: Component; titleKey: string; widthClass?: string }> = {
  creator: { component: PostCreator, titleKey: 'post.createPost', widthClass: 'w-full sm:w-125' },
  privacy: { component: PrivacySelector, titleKey: 'post.selectPrivacy', widthClass: 'w-full sm:w-125' },
  tagUsers: { component: TagUsers, titleKey: 'post.tagUsers', widthClass: 'w-full sm:w-125' },
  location: { component: LocationSelector, titleKey: 'post.addLocation', widthClass: 'w-full sm:w-125' },
  gifSelector: { component: GifSelector, titleKey: 'post.selectGif', widthClass: 'w-full sm:w-125' },
  imageEditor: { component: ImageEditor, titleKey: 'post.editImage', widthClass: 'w-full lg:w-300' },
  videoEditor: { component: VideoEditor, titleKey: 'post.editVideo', widthClass: 'w-full lg:w-300' },
  feeling: { component: FeelingModal, titleKey: 'post.feelingActivity', widthClass: 'w-full sm:w-125' },
};

// --- Composables ---
const {
  wrapperRef,
  currentView,
  transitionName,
  navigateTo,
  navigateBack,
  onEnter: baseOnEnter, // Przemianowujemy oryginalny onEnter
  onAfterEnter
} = useSlideTransition('creator');

// --- RESIZE OBSERVER (FIX WYSOKOŚCI) ---
const resizeObserver = ref<ResizeObserver | null>(null);

const onEnterWithObserver = (el: Element) => {
  // 1. Najpierw ustawiamy wysokość startową (tak jak wcześniej)
  baseOnEnter(el);

  // 2. Czyścimy stary observer
  if (resizeObserver.value) resizeObserver.value.disconnect();

  // 3. Tworzymy nowy observer dla aktywnego widoku
  resizeObserver.value = new ResizeObserver((entries) => {
    for (const entry of entries) {
      if (wrapperRef.value) {
        // Aktualizujemy wysokość wrappera, gdy zmieni się wysokość dziecka (np. załadowanie obrazka)
        wrapperRef.value.style.height = `${entry.contentRect.height}px`;
      }
    }
  });

  // 4. Zaczynamy obserwować element, który właśnie wchodzi
  resizeObserver.value.observe(el);
};

// Sprzątanie observera przy odmontowaniu komponentu
onBeforeUnmount(() => {
  if (resizeObserver.value) resizeObserver.value.disconnect();
});


// --- Computed & Watchers ---
const activeViewConfig = computed(() => VIEW_CONFIG[currentView.value as ViewKey] || VIEW_CONFIG.creator);
const activeComponent = computed(() => activeViewConfig.value.component);

const dynamicProps = computed(() => {
  if (currentView.value === 'creator') {
    return {
      sharedPost: props.sharedPost,
      sharedEventId: props.sharedEventId
    };
  }
  return {};
});

watch(() => currentView.value, (newView) => {
  emit('update:showBack', newView !== 'creator');
  const config = VIEW_CONFIG[newView as ViewKey];
  if (config) emit('update:title', t(config.titleKey));
});

// --- Nawigacja ---
const handleNavigation = (viewName: ViewKey, data: { url: string, index: number } | null = null) => {
  if (data) {
    if (viewName === 'imageEditor') {
       const existingAlt = createPostStore.selectedImages[data.index]?.altText || '';
       createPostStore.setImageToEdit({ url: data.url, altText: existingAlt }, data.index);
    } else if (viewName === 'videoEditor') {
       createPostStore.setVideoToEdit(data.url);
    }
  }

  if (VIEW_CONFIG[viewName]) navigateTo(viewName);
};

const goBack = () => {
  if (currentView.value === 'imageEditor') createPostStore.setImageToEdit(null);
  if (currentView.value === 'videoEditor') createPostStore.setVideoToEdit(null);
  navigateBack();
};

defineExpose({ goBack });

// --- Handlery ---
const handleClose = () => {
  createPostStore.reset();
  emit('close');
};

const handleMediaEdited = (url: string) => {
  if (currentView.value === 'imageEditor' && createPostStore.imageIndexToEdit !== null) {
    createPostStore.selectedImages[createPostStore.imageIndexToEdit].url = url;
    createPostStore.setImageToEdit(null, null);
  } else if (currentView.value === 'videoEditor') {
    createPostStore.postVideoUrl = url;
    createPostStore.setVideoToEdit(null);
  }
  navigateBack();
};

// --- Init ---
onMounted(() => {
  if (props.targetId && props.targetType) {
    createPostStore.setTarget(props.targetId, props.targetType);
  }
  const savedPrivacy = localStorage.getItem('fc_default_privacy');
  if (savedPrivacy) createPostStore.setPrivacy(savedPrivacy);

  if (createPostStore.initialView && VIEW_CONFIG[createPostStore.initialView as ViewKey]) {
    navigateTo(createPostStore.initialView);
  }
});
</script>

<template>
  <div
    :class="[
      activeViewConfig.widthClass,
      'p-2 sm:p-4 mx-auto rounded-xl relative overflow-hidden transition-all duration-300 ease-in-out'
    ]"
  >
    <div
      class="relative transition-[height] duration-300 ease-in-out"
      ref="wrapperRef"
      style="min-height: 100px"
    >
      <Transition
        :name="transitionName"
        @enter="onEnterWithObserver"
        @after-enter="onAfterEnter"
      >
        <component
          :is="activeComponent"
          :key="currentView"
          class="view-container bg-theme-bg-secondary"
          v-bind="dynamicProps"

          @navigate="handleNavigation"
          @publish="(content) => { emit('publish', content); createPostStore.reset(); }"
          @close="handleClose"
          @back="goBack"

          @confirm="(payload) => {
             createPostStore.setPrivacy(payload.id);
             if(payload.setDefault) localStorage.setItem('fc_default_privacy', payload.id);
             navigateBack();
          }"

          @done="handleMediaEdited"

          @select="(url) => { createPostStore.setGif(url); navigateBack(); }"
          @removeGif="createPostStore.setGif(null)"

          @feeling-selected="(payload) => {
             payload.type === 'feeling'
               ? createPostStore.setSelectedFeeling(payload.data)
               : createPostStore.setSelectedActivity(payload.data);
             navigateBack();
          }"

          @openTagUsers="() => navigateTo('tagUsers')"
          @openLocation="() => navigateTo('location')"
          @openGifSelector="() => navigateTo('gifSelector')"
          @open-feeling-selector="() => navigateTo('feeling')"
        />
      </Transition>
    </div>
  </div>
</template>

<style scoped>
.view-container {
  width: 100%;
  top: 0;
  left: 0;
}

/* Animacje Slide */
.slide-left-enter-active, .slide-left-leave-active,
.slide-right-enter-active, .slide-right-leave-active {
  transition: transform 0.3s cubic-bezier(0.25, 1, 0.5, 1), opacity 0.3s ease;
  position: absolute;
  width: 100%;
}
.slide-left-enter-from { transform: translateX(100%); }
.slide-left-leave-to { transform: translateX(-100%); opacity: 0; }
.slide-right-enter-from { transform: translateX(-100%); }
.slide-right-leave-to { transform: translateX(100%); opacity: 0; }
</style>
