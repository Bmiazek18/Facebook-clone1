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

const handleStartDrag = (e: MouseEvent) => {
  if (!props.isViewing) {
    props.onStartDrag?.(e, props.element)
  }
}
const handleRemove = () => props.onRemove?.(props.element.id)

const handleUpdateContent = (id: string, value: string) => {
  emit('update-content', id, value)
}

const handleStartRotate = (e: MouseEvent) => {
  props.onStartRotate(e, props.element)
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
        'border-white': state.selected && !isViewing,
        'group-hover:border-white/50': !state.selected && !isViewing,
      }"
      :style="{
        width: element.type === 'post' ? 'auto' : element.width ? element.width + 'px' : 'auto',
        height: element.type === 'post' ? 'auto' : element.height ? element.height + 'px' : 'auto',
        transform: elementTransform,
        ...element.styles,
      }"
    >
      <button
        data-story-control
        v-if="state.selected && !state.editing && !state.cropping && !isViewing"
        @click.stop="handleRemove"
        class="absolute -top-4 -left-4 w-7 h-7 bg-white text-black rounded-full flex items-center justify-center z-50 transition-transform hover:scale-105 shadow-lg border border-gray-200"
      >
        <Close :size="16" />
      </button>

      <template v-if="state.selected && !state.editing && !isViewing">
        <div
          v-if="element.type !== 'post' && !(element.type === 'image' && !element.musicTitle)"
          class="absolute -top-1.5 -right-1.5 w-3 h-3 bg-white rounded-full border border-gray-400 cursor-nesw-resize z-50 shadow-sm hover:scale-125 transition-transform"
          @mousedown.stop="handleStartRotate"
        ></div>
        <div
          v-else
          class="absolute -top-1.5 -right-1.5 w-3 h-3 bg-white rounded-full border border-gray-300 pointer-events-none"
        ></div>

        <div
          v-if="element.type !== 'post' && !(element.type === 'image' && !element.musicTitle)"
          class="absolute -bottom-1.5 -left-1.5 w-3 h-3 bg-white rounded-full border border-gray-400 cursor-nesw-resize z-50 shadow-sm hover:scale-125 transition-transform"
          @mousedown.stop="handleStartRotate"
        ></div>
        <div
          v-else
          class="absolute -bottom-1.5 -left-1.5 w-3 h-3 bg-white rounded-full border border-gray-300 pointer-events-none"
        ></div>

        <div
          v-if="element.type !== 'post' && !(element.type === 'image' && !element.musicTitle)"
          class="absolute -bottom-1.5 -right-1.5 w-3 h-3 bg-white rounded-full border border-gray-400 cursor-se-resize z-50 shadow-sm hover:scale-125 transition-transform"
          @mousedown.stop="handleStartRotate"
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
