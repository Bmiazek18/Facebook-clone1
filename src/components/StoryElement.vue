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

const props = defineProps<{
  element: StoryElement
  state: {
    active: boolean
    cropping: boolean
    editing: boolean
    selected: boolean
  }
  onStartDrag: (e: MouseEvent, element: StoryElement) => void
  onStartResize: (e: MouseEvent, element: StoryElement) => void
  onToggleCrop: (id: string) => void
  onEnableEdit: (id: string) => void
  onDisableEdit: () => void
  onRemove: (id: string) => void
}>()

const emit = defineEmits<{ 'update-content': [id: string, value: string] }>()

const elementTransform = computed(() => `rotate(${props.element.rotation}deg) scale(${props.element.scale ?? 1})`)

const handleStartDrag = (e: MouseEvent) => props.onStartDrag?.(e, props.element)
const handleRemove = () => props.onRemove?.(props.element.id)

// Check if element is a music element (image with music data)
const isMusicElement = computed(() => props.element.type === 'image' && props.element.musicTitle)

const handleUpdateContent = (id: string, value: string) => {
  emit('update-content', id, value)
}
</script>

<template>
  <div
    class="absolute cursor-move group transition-transform duration-75"
    :class="{ 'z-50': state.active }"
    :style="{ top: `${element.y}px`, left: `${element.x}px` }"
    @mousedown="handleStartDrag"
  >
    <div
      class="relative transition-transform duration-75 origin-center"
      :style="{
        width: element.width ? element.width + 'px' : 'auto',
        height: element.height ? element.height + 'px' : 'auto',
        transform: elementTransform,
        ...element.styles
      }"
    >
      <!-- Remove Button -->
      <button
        v-if="!state.editing && !state.cropping && !element.musicTitle"
        @click.stop="handleRemove"
        class="absolute -top-3 -right-3 w-6 h-6 bg-red-500 text-white rounded-full flex items-center justify-center opacity-0 group-hover:opacity-100 hover:bg-red-600 z-50 transition-opacity shadow-md"
      >
        <Close :size="14" />
      </button>

      <!-- Music Element -->
      <StoryMusicElement 
        v-if="isMusicElement" 
        :element="element" 
      />

      <!-- Image Element -->
      <StoryImageElement
        v-else-if="element.type === 'image'"
        :element="element"
        :is-cropping="state.cropping"
        :is-selected="state.selected"
        :on-start-resize="onStartResize"
        :on-toggle-crop="onToggleCrop"
      />

      <!-- Link Element -->
      <StoryLinkElement
        v-else-if="element.type === 'link'"
        :element="element"
      />

      <!-- Post Element -->
      <StoryPostElement
        v-else-if="element.type === 'post'"
        :element="element"
      />

      <!-- Text Element -->
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
