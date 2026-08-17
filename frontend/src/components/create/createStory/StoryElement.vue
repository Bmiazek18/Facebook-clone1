<script setup lang="ts">
import { computed } from 'vue'
import Close from 'vue-material-design-icons/Close.vue'
import type { StoryElement } from '@/types/StoryElement'

// Import sub-components
import StoryImageElement from './StoryElements/StoryImageElement.vue'
import StoryMusicElement from './StoryElements/StoryMusicElement.vue'
import StoryLinkElement from './StoryElements/StoryLinkElement.vue'
import StoryPostElement from './StoryElements/StoryPostElement.vue'
import StoryTextElement from './StoryElements/StoryTextElement.vue'
import StoryReelElement from './StoryElements/StoryReelElement.vue'

const props = defineProps<{
  element: StoryElement
  state: {
    active: boolean
    cropping: boolean
    editing: boolean
    selected: boolean
  }
  isViewing?: boolean
  onStartDrag: (e: MouseEvent, element: StoryElement) => void
  onStartRotate: (e: MouseEvent, element: StoryElement) => void
  onStartScale: (e: MouseEvent, element: StoryElement) => void
  onToggleCrop: (id: string) => void
  onEnableEdit: (id: string) => void
  onDisableEdit: () => void
  onRemove: (id: string) => void
}>()

const emit = defineEmits<{
  'update-content': [id: string, value: string]
  'post-clicked': [postId: string]
}>()

const elementTransform = computed(
  () => `rotate(${props.element.rotation}deg) scale(${props.element.scale ?? 1})`,
)

// Obsługa przesuwania (środek)
const handleStartDrag = (e: MouseEvent) => {
  if (!props.isViewing) {
    props.onStartDrag?.(e, props.element)
  }
}

// Obsługa obracania (strzałka)
const handleStartRotate = (e: MouseEvent) => {
  if (!props.isViewing) {
    props.onStartRotate?.(e, props.element)
  }
}

// Obsługa skalowania (rogi)
const handleStartScale = (e: MouseEvent) => {
  if (!props.isViewing) {
    props.onStartScale?.(e, props.element)
  }
}

const handleRemove = () => props.onRemove?.(props.element.id)

const handleUpdateContent = (id: string, value: string) => {
  emit('update-content', id, value)
}
</script>

<template>
  <div
    class="absolute group transition-transform duration-75"
    :class="{
      'z-50': state.active,
      'cursor-move': !isViewing,
      'pointer-events-none': isViewing && element.type !== 'post',
    }"
    :style="{ top: `${element.y}px`, left: `${element.x}px` }"
    @mousedown.stop="handleStartDrag"
  >
    <div
      class="relative transition-transform duration-75 origin-center p-1.5 border border-transparent"
      :class="{
        'border-white': element.type !== 'image' && !state.editing && state.selected && !isViewing,
      }"
      :style="{
        width: element.type === 'post' ? 'auto' : element.width ? element.width + 'px' : 'auto',
        height: element.type === 'post' ? 'auto' : element.height ? element.height + 'px' : 'auto',
        transform: elementTransform,
        ...element.styles,
      }"
    >
      <template v-if="state.selected && !state.editing && !isViewing && element.type !== 'image' ">

        <!-- Przycisk usuwania -->
        <button
          data-story-control
          v-if="!state.cropping"
          @click.stop="handleRemove"
          class="absolute -top-4 -left-4 w-7 h-7 bg-white text-black rounded-full flex items-center justify-center z-50 transition-transform hover:scale-105 shadow-lg border border-gray-200"
        >
          <Close :size="16" />
        </button>

        <!-- Prawy górny róg - Kropka -->
        <div
          v-if="element.type !== 'post' "
          class="absolute -top-1.5 -right-1.5 w-3 h-3 bg-white rounded-full border border-gray-400 cursor-nesw-resize z-50 shadow-sm hover:scale-125 transition-transform"
          @mousedown.stop="handleStartScale"
        ></div>
        <div
          v-else
          class="absolute -top-1.5 -right-1.5 w-3 h-3 bg-white rounded-full border border-gray-300 pointer-events-none"
        ></div>

        <!-- Ikona obracania (odsunięta za prawy górny róg) -->
        <div
          v-if="element.type !== 'post'"
          class="absolute -top-8 -right-8 w-8 h-8 flex items-center justify-center cursor-pointer z-50 drop-shadow-md hover:scale-110 transition-transform"
          @mousedown.stop="handleStartRotate"
        >
          <!-- Gruba, biała strzałka symbolizująca obrót -->
          <svg
            xmlns="http://www.w3.org/2000/svg"
            width="18"
            height="18"
            viewBox="0 0 24 24"
            fill="none"
            stroke="white"
            stroke-width="3"
            stroke-linecap="round"
            stroke-linejoin="round"
          >
            <polyline points="23 4 23 10 17 10"></polyline>
            <path d="M20.49 15a9 9 0 1 1-2.12-9.36L23 10"></path>
          </svg>
        </div>

        <!-- Lewy dolny róg -->
        <div
          v-if="element.type !== 'post' "
          class="absolute -bottom-1.5 -left-1.5 w-3 h-3 bg-white rounded-full border border-gray-400 cursor-nesw-resize z-50 shadow-sm hover:scale-125 transition-transform"
          @mousedown.stop="handleStartScale"
        ></div>
        <div
          v-else
          class="absolute -bottom-1.5 -left-1.5 w-3 h-3 bg-white rounded-full border border-gray-300 pointer-events-none"
        ></div>

        <!-- Prawy dolny róg -->
        <div
          v-if="element.type !== 'post' "
          class="absolute -bottom-1.5 -right-1.5 w-3 h-3 bg-white rounded-full border border-gray-400 cursor-nwse-resize z-50 shadow-sm hover:scale-125 transition-transform"
          @mousedown.stop="handleStartScale"
        ></div>
        <div
          v-else
          class="absolute -bottom-1.5 -right-1.5 w-3 h-3 bg-white rounded-full border border-gray-300 pointer-events-none"
        ></div>
      </template>

      <StoryMusicElement v-if="element.type === 'image' && element.musicTitle" :element="element" />
      <StoryImageElement v-else-if="element.type === 'image'" :element="element" />
      <StoryLinkElement v-else-if="element.type === 'link'" :element="element" />
      <StoryPostElement v-else-if="element.type === 'post'" :element="element" />
      <StoryReelElement v-else-if="element.type === 'reel'" :element="element" />
      <StoryTextElement
        v-else-if="element.type === 'text'"
        :element="element"
        :is-editing="state.editing"
        :on-enable-edit="onEnableEdit"
        :on-disable-edit="onDisableEdit"
        @update-content="handleUpdateContent"
      />
    </div>
  </div>
</template>
