<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch } from 'vue';
import Plus from 'vue-material-design-icons/Plus.vue';
import Minus from 'vue-material-design-icons/Minus.vue';
import ArrowExpand from 'vue-material-design-icons/ArrowExpand.vue';
import ArrowCollapse from 'vue-material-design-icons/ArrowCollapse.vue';
import ChevronLeft from 'vue-material-design-icons/ChevronLeft.vue';
import ChevronRight from 'vue-material-design-icons/ChevronRight.vue';
import ImageTag from '../ImageTag.vue';
import type { ImageTagType } from '@/types/ImageTag';


// --- PROPS & EMITS ---
const props = defineProps<{
  image: {
    src: string;
    altText?: string;
    tags?: ImageTagType[];
  };
  hasPrev?: boolean;
  hasNext?: boolean;
}>();

const emit = defineEmits<{
  prev: [];
  next: [];
}>();

const isFullScreen = defineModel<boolean>('isFullScreen', { default: false });

// --- ZOOM STATE ---
const currentZoom = ref(1.0);
const zoomStep = 0.2;
const maxZoom = 3.0;
const minZoom = 1.0;

// --- DRAG STATE ---
const isDragging = ref(false);
const startDragX = ref(0);
const startDragY = ref(0);
const imageOffsetX = ref(0);
const imageOffsetY = ref(0);

const imageContainer = ref<HTMLElement | null>(null);
const imageElement = ref<HTMLElement | null>(null);

// --- ZOOM FUNCTIONS ---
const zoomIn = () => {
  if (currentZoom.value < maxZoom) {
    currentZoom.value = Math.min(maxZoom, currentZoom.value + zoomStep);
  }
};

const zoomOut = () => {
  if (currentZoom.value > minZoom) {
    currentZoom.value = Math.max(minZoom, currentZoom.value - zoomStep);
  }
};

const toggleFullScreen = () => {
  isFullScreen.value = !isFullScreen.value;
  // Reset zoom and position when toggling
  currentZoom.value = 1.0;
  imageOffsetX.value = 0;
  imageOffsetY.value = 0;
};

// --- BOUNDS ---
const applyBounds = () => {
  if (!imageContainer.value || !imageElement.value || currentZoom.value <= 1.0) return;

  const containerWidth = imageContainer.value.clientWidth;
  const containerHeight = imageContainer.value.clientHeight;

  const imageDisplayedWidth = imageElement.value.clientWidth;
  const imageDisplayedHeight = imageElement.value.clientHeight;

  const zoomedWidth = imageDisplayedWidth * currentZoom.value;
  const zoomedHeight = imageDisplayedHeight * currentZoom.value;

  const maxShiftX = Math.max(0, (zoomedWidth - containerWidth) / 2 / currentZoom.value);
  const maxShiftY = Math.max(0, (zoomedHeight - containerHeight) / 2 / currentZoom.value);

  if (imageOffsetX.value > maxShiftX) {
    imageOffsetX.value = maxShiftX;
  } else if (imageOffsetX.value < -maxShiftX) {
    imageOffsetX.value = -maxShiftX;
  }

  if (imageOffsetY.value > maxShiftY) {
    imageOffsetY.value = maxShiftY;
  } else if (imageOffsetY.value < -maxShiftY) {
    imageOffsetY.value = -maxShiftY;
  }
};

watch(currentZoom, () => {
  if (currentZoom.value <= 1.0) {
    imageOffsetX.value = 0;
    imageOffsetY.value = 0;
  } else {
    applyBounds();
  }
});

// --- DRAG FUNCTIONS ---
type PointerEvent = MouseEvent | TouchEvent;

const startDrag = (event: PointerEvent) => {
  if (currentZoom.value > 1.0) {
    isDragging.value = true;
    const clientX = 'touches' in event ? event.touches[0]?.clientX ?? 0 : event.clientX;
    const clientY = 'touches' in event ? event.touches[0]?.clientY ?? 0 : event.clientY;
    startDragX.value = clientX;
    startDragY.value = clientY;

    if ('preventDefault' in event) event.preventDefault();
  }
};

const drag = (event: PointerEvent) => {
  if (!isDragging.value) return;

  const clientX = 'touches' in event ? event.touches[0]?.clientX ?? 0 : event.clientX;
  const clientY = 'touches' in event ? event.touches[0]?.clientY ?? 0 : event.clientY;

  const deltaX = clientX - startDragX.value;
  const deltaY = clientY - startDragY.value;

  imageOffsetX.value += deltaX;
  imageOffsetY.value += deltaY;

  startDragX.value = clientX;
  startDragY.value = clientY;

  applyBounds();
};

const endDrag = () => {
  isDragging.value = false;
};

// --- LIFECYCLE ---
onMounted(() => {
  document.addEventListener('mousemove', drag as EventListener);
  document.addEventListener('mouseup', endDrag);
  document.addEventListener('touchmove', drag as EventListener);
  document.addEventListener('touchend', endDrag);
  document.addEventListener('touchcancel', endDrag);
});

onUnmounted(() => {
  document.removeEventListener('mousemove', drag as EventListener);
  document.removeEventListener('mouseup', endDrag);
  document.removeEventListener('touchmove', drag as EventListener);
  document.removeEventListener('touchend', endDrag);
  document.removeEventListener('touchcancel', endDrag);
});
</script>

<template>
  <div
    class="bg-black flex flex-col items-center justify-center relative"
    :class="{
      'grow w-full min-w-[50%] rounded-l-lg': !isFullScreen,
      'w-full rounded-r-none rounded-l-lg': isFullScreen
    }"
  >
    <!-- Zoom controls -->
    <div class="absolute top-4 right-4 flex space-x-2 z-40">
      <button
        @click="zoomIn"
        :disabled="currentZoom >= maxZoom"
        class="p-2 bg-gray-700 bg-opacity-80 text-white rounded-full hover:bg-opacity-100 disabled:opacity-50 disabled:cursor-not-allowed transition"
        aria-label="Powiększ"
      >
        <Plus :size="20" fillColor="#FFFFFF" />
      </button>
      <button
        @click="zoomOut"
        :disabled="currentZoom <= minZoom"
        class="p-2 bg-gray-700 bg-opacity-80 text-white rounded-full hover:bg-opacity-100 disabled:opacity-50 disabled:cursor-not-allowed transition"
        aria-label="Pomniejsz"
      >
        <Minus :size="20" fillColor="#FFFFFF" />
      </button>
      <button
        @click="toggleFullScreen"
        class="p-1 text-white rounded-full hover:bg-white/10 transition self-center"
        aria-label="Pełny ekran"
      >
        <component :is="isFullScreen ? ArrowCollapse : ArrowExpand" :size="28" fillColor="#FFFFFF" />
      </button>
    </div>

    <!-- Left arrow -->
    <button
      v-if="hasPrev"
      @click="emit('prev')"
      class="absolute left-4 top-1/2 -translate-y-1/2 p-2 bg-white/90 hover:bg-white text-gray-800 rounded-full shadow-lg transition z-40"
      aria-label="Poprzednie zdjęcie"
    >
      <ChevronLeft :size="28" />
    </button>

    <!-- Right arrow -->
    <button
      v-if="hasNext"
      @click="emit('next')"
      class="absolute right-4 top-1/2 -translate-y-1/2 p-2 bg-white/90 hover:bg-white text-gray-800 rounded-full shadow-lg transition z-40"
      aria-label="Następne zdjęcie"
    >
      <ChevronRight :size="28" />
    </button>

    <!-- Image container -->
    <div
      ref="imageContainer"
      class="flex items-center justify-center overflow-hidden w-full h-full relative"
    >
      <img
        ref="imageElement"
        class="max-w-full max-h-full object-contain"
        :class="{
          'cursor-grab': currentZoom > 1.0 && !isDragging,
          'cursor-grabbing': isDragging
        }"
        :style="{
          transform: `scale(${currentZoom}) translate(${imageOffsetX}px, ${imageOffsetY}px)`,
          transition: isDragging ? 'none' : 'transform 100ms ease-in-out'
        }"
        :src="props.image.src"
        :alt="props.image.altText || 'Image'"
        @mousedown="startDrag"
        @touchstart="startDrag"
      />
      <template v-if="props.image.tags && props.image.tags.length > 0">
        <ImageTag
          v-for="tag in props.image.tags"
          :key="tag.id"
          :tag="tag"
          class="absolute"
        />
      </template>
    </div>
  </div>
</template>
