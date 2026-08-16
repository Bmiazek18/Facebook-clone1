<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch, computed } from 'vue'
import MagnifyPlus from 'vue-material-design-icons/MagnifyPlus.vue'
import MagnifyMinus from 'vue-material-design-icons/MagnifyMinus.vue'
import ArrowExpand from 'vue-material-design-icons/ArrowExpand.vue'
import ArrowCollapse from 'vue-material-design-icons/ArrowCollapse.vue'
import ChevronLeft from 'vue-material-design-icons/ChevronLeft.vue'
import ChevronRight from 'vue-material-design-icons/ChevronRight.vue'
import ImageTag from '@/components/media/ImageTag.vue'
import type { ImageTagType } from '@/types/Post'
import PlayerVideo from '../media/PlayerVideo.vue'

// --- PROPS & EMITS ---
const props = defineProps<{
  media: {
    src: string
    altText?: string
    tags?: ImageTagType[]
  }
  hasPrev?: boolean
  hasNext?: boolean
}>()

const emit = defineEmits<{
  prev: []
  next: []
}>()

const isVideo = computed(() => {
  return (
    props.media.src.toLowerCase().endsWith('.mp4') ||
    props.media.src.toLowerCase().endsWith('.webm')
  )
})

const isFullScreen = defineModel<boolean>('isFullScreen', { default: false })

// --- ZOOM STATE ---
const currentZoom = ref(1.0)
const zoomStep = 0.2
const maxZoom = 3.0
const minZoom = 1.0
const showTags = ref(false)

// --- DRAG STATE ---
const isDragging = ref(false)
const startDragX = ref(0)
const startDragY = ref(0)
const imageOffsetX = ref(0)
const imageOffsetY = ref(0)

const imageContainer = ref<HTMLElement | null>(null)
const imageElement = ref<HTMLElement | null>(null)

// --- CONTROLS VISIBILITY (TIMER 5s) - Dotyczy TYLKO strzałek bocznych ---
const showArrows = ref(true)
let arrowsTimer: ReturnType<typeof setTimeout> | null = null

const resetArrowsTimer = () => {
  showArrows.value = true
  if (arrowsTimer) clearTimeout(arrowsTimer)

  // Ukryj po 5 sekundach (5000 ms)
  arrowsTimer = setTimeout(() => {
    showArrows.value = false
  }, 5000)
}

// --- ZOOM FUNCTIONS ---
const zoomIn = () => {
  if (currentZoom.value < maxZoom) {
    currentZoom.value = Math.min(maxZoom, currentZoom.value + zoomStep)
  }
}

const zoomOut = () => {
  if (currentZoom.value > minZoom) {
    currentZoom.value = Math.max(minZoom, currentZoom.value - zoomStep)
  }
}

const toggleFullScreen = () => {
  isFullScreen.value = !isFullScreen.value
  currentZoom.value = 1.0
  imageOffsetX.value = 0
  imageOffsetY.value = 0
}

// --- BOUNDS (Ograniczenia przeciągania) ---
const applyBounds = () => {
  if (!imageContainer.value || !imageElement.value || currentZoom.value <= 1.0) return

  const containerWidth = imageContainer.value.clientWidth
  const containerHeight = imageContainer.value.clientHeight

  const imageDisplayedWidth = imageElement.value.clientWidth
  const imageDisplayedHeight = imageElement.value.clientHeight

  const zoomedWidth = imageDisplayedWidth * currentZoom.value
  const zoomedHeight = imageDisplayedHeight * currentZoom.value

  const maxShiftX = Math.max(0, (zoomedWidth - containerWidth) / 2 / currentZoom.value)
  const maxShiftY = Math.max(0, (zoomedHeight - containerHeight) / 2 / currentZoom.value)

  if (imageOffsetX.value > maxShiftX) {
    imageOffsetX.value = maxShiftX
  } else if (imageOffsetX.value < -maxShiftX) {
    imageOffsetX.value = -maxShiftX
  }

  if (imageOffsetY.value > maxShiftY) {
    imageOffsetY.value = maxShiftY
  } else if (imageOffsetY.value < -maxShiftY) {
    imageOffsetY.value = -maxShiftY
  }
}

watch(currentZoom, () => {
  if (currentZoom.value <= 1.0) {
    imageOffsetX.value = 0
    imageOffsetY.value = 0
  } else {
    applyBounds()
  }
})

// --- DRAG FUNCTIONS ---
type PointerEvent = MouseEvent | TouchEvent

const startDrag = (event: PointerEvent) => {
  if (currentZoom.value > 1.0) {
    isDragging.value = true
    const clientX = 'touches' in event ? (event.touches[0]?.clientX ?? 0) : event.clientX
    const clientY = 'touches' in event ? (event.touches[0]?.clientY ?? 0) : event.clientY
    startDragX.value = clientX
    startDragY.value = clientY

    if ('preventDefault' in event) event.preventDefault()
  }
}

const drag = (event: PointerEvent) => {
  if (!isDragging.value) return

  const clientX = 'touches' in event ? (event.touches[0]?.clientX ?? 0) : event.clientX
  const clientY = 'touches' in event ? (event.touches[0]?.clientY ?? 0) : event.clientY

  const deltaX = clientX - startDragX.value
  const deltaY = clientY - startDragY.value

  imageOffsetX.value += deltaX
  imageOffsetY.value += deltaY

  startDragX.value = clientX
  startDragY.value = clientY

  applyBounds()
}

const endDrag = () => {
  isDragging.value = false
}

// --- LIFECYCLE ---
onMounted(() => {
  resetArrowsTimer() // Inicjalne odpalenie timera
  document.addEventListener('mousemove', drag as EventListener)
  document.addEventListener('mouseup', endDrag)
  document.addEventListener('touchmove', drag as EventListener)
  document.addEventListener('touchend', endDrag)
  document.addEventListener('touchcancel', endDrag)
})

onUnmounted(() => {
  if (arrowsTimer) clearTimeout(arrowsTimer)
  document.removeEventListener('mousemove', drag as EventListener)
  document.removeEventListener('mouseup', endDrag)
  document.removeEventListener('touchmove', drag as EventListener)
  document.removeEventListener('touchend', endDrag)
  document.removeEventListener('touchcancel', endDrag)
})
</script>

<template>
  <!-- Główne nasłuchiwanie ruchu myszy resetuje tylko timer strzałek -->
  <div
    @mousemove="resetArrowsTimer"
    @touchstart="resetArrowsTimer"
    class="bg-black flex flex-col items-center justify-center relative select-none"
    :class="{
      'grow w-full min-w-[50%] rounded-l-lg': !isFullScreen,
      'w-full rounded-r-none rounded-l-lg': isFullScreen,
    }"
  >
    <!-- Pasek sterowania (Zoom / Fullscreen) - ZAWSZE WIDOCZNY -->
    <div v-if="!isVideo" class="absolute top-4 right-4 flex items-center space-x-2 z-50">
      <button
        @click="zoomIn"
        :disabled="currentZoom >= maxZoom"
        class="p-2 bg-[#e2e5e9] text-black rounded-full hover:bg-white disabled:opacity-30 transition"
        aria-label="Powiększ"
      >
        <MagnifyPlus :size="22" fillColor="#000" />
      </button>
      <button
        @click="zoomOut"
        :disabled="currentZoom <= minZoom"
        class="p-2 bg-[#e2e5e9] text-black rounded-full hover:bg-white disabled:opacity-30 transition"
        aria-label="Pomniejsz"
      >
        <MagnifyMinus :size="22" fillColor="#000" />
      </button>
      <button
        @click="toggleFullScreen"
        class="p-2 bg-[#e2e5e9] text-black rounded-full hover:bg-white transition"
        aria-label="Pełny ekran"
      >
        <component
          :is="isFullScreen ? ArrowCollapse : ArrowExpand"
          :size="22"
          fillColor="#000"
        />
      </button>
    </div>

    <!-- Strzałka w lewo (Szerokie, wysokie ciemne tło + białe kółko) - ZNIKA -->
    <button
      v-if="hasPrev"
      @click="emit('prev')"
      class="absolute left-0 top-0 h-full w-20 md:w-28 flex items-center justify-center bg-black/40 hover:bg-black/60 transition-opacity duration-300 z-40"
      :class="{
        'opacity-100': showArrows,
        'opacity-0 pointer-events-none': !showArrows
      }"
      aria-label="Poprzednie"
    >
      <div class="p-2.5 bg-white rounded-full shadow-lg hover:scale-105 transition-transform flex items-center justify-center">
        <ChevronLeft :size="32" fillColor="#111827" />
      </div>
    </button>

    <!-- Strzałka w prawo (Szerokie, wysokie ciemne tło + białe kółko) - ZNIKA -->
    <button
      v-if="hasNext"
      @click="emit('next')"
      class="absolute right-0 top-0 h-full w-20 md:w-28 flex items-center justify-center bg-black/40 hover:bg-black/60 transition-opacity duration-300 z-40"
      :class="{
        'opacity-100': showArrows,
        'opacity-0 pointer-events-none': !showArrows
      }"
      aria-label="Następne"
    >
      <div class="p-2.5 bg-white rounded-full shadow-lg hover:scale-105 transition-transform flex items-center justify-center">
        <ChevronRight :size="32" fillColor="#111827" />
      </div>
    </button>

    <!-- Główny kontener kadru (bez zmian) -->
    <div
      ref="imageContainer"
      class="flex items-center justify-center overflow-hidden w-full h-full relative"
    >
      <PlayerVideo
        v-if="isVideo"
        :url="props.media.src"
        class="max-w-full max-h-full object-contain"
        :lightbox="true"
        :settings="true"
      />

      <div
        v-else
        ref="imageElement"
        class="relative max-w-full max-h-full"
        :class="{
          'cursor-grab': currentZoom > 1.0 && !isDragging,
          'cursor-grabbing': isDragging,
        }"
        :style="{
          transform: `scale(${currentZoom}) translate(${imageOffsetX}px, ${imageOffsetY}px)`,
          transition: isDragging ? 'none' : 'transform 100ms ease-in-out',
        }"
        @mousedown="startDrag"
        @touchstart="startDrag"
        @mouseenter="showTags = true"
        @mouseleave="showTags = false"
      >
        <img
          class="max-w-full max-h-full object-contain pointer-events-none block"
          :src="props.media.src"
          :alt="props.media.altText || 'Image'"
        />

        <template v-if="props.media.tags && props.media.tags.length > 0">
          <ImageTag
            v-for="tag in props.media.tags"
            :key="tag.id"
            :tag="tag"
            :force-show="showTags"
            class="absolute"
            :style="{ left: `${tag.x}%`, top: `${tag.y}%` }"
          />
        </template>
      </div>
    </div>
  </div>
</template>
