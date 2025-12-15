<script setup lang="ts">
import { computed } from 'vue'
import Close from 'vue-material-design-icons/Close.vue'
import ResizeBottomRight from 'vue-material-design-icons/ResizeBottomRight.vue'
import MusicNote from 'vue-material-design-icons/MusicNote.vue'

import type { StoryElement } from '@/types/StoryElement'

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
const handleStartResize = (e: MouseEvent) => props.onStartResize?.(e, props.element)
const handleToggleCrop = () => props.onToggleCrop?.(props.element.id)
const handleEnableEdit = () => props.onEnableEdit?.(props.element.id)
const handleDisableEdit = () => props.onDisableEdit?.()
const handleRemove = () => props.onRemove?.(props.element.id)

const textValue = computed({
  get: () => props.element.content,
  set: (val: string) => emit('update-content', props.element.id, val)
})
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
      <button
  v-if="!state.editing && !state.cropping && !element.musicTitle"
        @click.stop="handleRemove"
        class="absolute -top-3 -right-3 w-6 h-6 bg-red-500 text-white rounded-full flex items-center justify-center opacity-0 group-hover:opacity-100 hover:bg-red-600 z-50 transition-opacity shadow-md"
      >
        <Close :size="14" />
      </button>

      <div v-if="element.type === 'image' && element.musicTitle" class="w-full h-full relative select-none">
        <template v-if="element.musicStyle === 'large' || !element.musicStyle">
          <div class="w-full h-full bg-white/95 backdrop-blur-sm rounded-xl p-3 flex flex-col gap-3 shadow-lg pointer-events-none border border-gray-100">
            <img :src="element.content" class="w-full aspect-square object-cover rounded-lg shadow-sm" />
            <div class="text-center overflow-hidden pb-1">
              <p class="text-gray-900 font-bold text-lg leading-tight truncate">{{ element.musicTitle }}</p>
              <p class="text-gray-500 text-sm truncate">{{ element.musicArtist }}</p>
            </div>
          </div>
        </template>

        <template v-else-if="element.musicStyle === 'small'">
          <div class="w-full h-full bg-white/90 backdrop-blur-md rounded-lg p-2 flex items-center gap-3 shadow-md pointer-events-none border border-white/20">
            <img :src="element.content" class="w-12 h-12 rounded-md object-cover shrink-0" />
            <div class="flex flex-col min-w-0 flex-1">
              <p class="text-gray-900 font-bold text-sm truncate">{{ element.musicTitle }}</p>
              <p class="text-gray-500 text-xs truncate">{{ element.musicArtist }}</p>
            </div>
            <MusicNote :size="20" class="text-gray-400 mr-2" />
          </div>
        </template>

        <template v-else-if="element.musicStyle === 'text'">
          <div class="w-full h-full flex flex-col items-center justify-center pointer-events-none text-center">
            <p class="text-white font-bold text-2xl drop-shadow-[0_2px_8px_rgba(0,0,0,0.6)]">{{ element.musicTitle }}</p>
            <div class="flex items-center gap-1 opacity-90 drop-shadow-[0_1px_4px_rgba(0,0,0,0.6)] mt-1">
              <MusicNote :size="14" class="text-white" />
              <p class="text-white text-sm font-medium">{{ element.musicArtist }}</p>
            </div>
          </div>
        </template>

        <template v-else-if="element.musicStyle === 'icon'">
          <div class="w-full h-full rounded-full shadow-lg border-2 border-white pointer-events-none relative overflow-hidden bg-black">
            <img
              :src="element.content"
              class="w-full h-full object-cover rounded-full animate-spin-record"
              alt="Album cover"
            />
            <div class="absolute inset-0 bg-black/30 flex items-center justify-center z-10">
              <MusicNote :size="24" class="text-white drop-shadow-md" />
            </div>
          </div>
        </template>
      </div>

      <div v-else-if="element.type === 'image'" class="flex flex-col items-center select-none w-full h-full">
        <div
          class="relative w-full h-full overflow-hidden bg-gray-900 border-2"
          :class="state.cropping || state.selected ? 'border-blue-500 shadow-md' : 'border-transparent'"
          :style="element.styles"
          @dblclick.stop="handleToggleCrop"
        >
          <img
            :src="element.content"
            class="absolute max-w-none pointer-events-none origin-center"
            :style="{
              transform: `translate(${element.cropX || 0}px, ${element.cropY || 0}px) scale(${element.cropZoom || 1})`,
              width: '100%', height: '100%', objectFit: 'cover'
            }"
          />
          <div v-if="state.cropping" class="absolute inset-0 grid grid-cols-3 grid-rows-3 pointer-events-none opacity-50">
            <div class="border-r border-b border-white/30"></div><div class="border-r border-b border-white/30"></div><div class="border-b border-white/30"></div>
            <div class="border-r border-b border-white/30"></div><div class="border-r border-b border-white/30"></div><div class="border-b border-white/30"></div>
            <div class="border-r border-white/30"></div><div class="border-r border-white/30"></div><div></div>
          </div>
        </div>
  <template v-if="!state.cropping">
          <div
            @mousedown.stop="handleStartResize"
            class="absolute bottom-1 right-1 bg-white text-black p-1 rounded-full opacity-0 group-hover:opacity-100 cursor-nwse-resize shadow-md hover:scale-110 transition z-40"
          >
            <ResizeBottomRight :size="16" />
          </div>
        </template>
      </div>

      <template v-else-if="element.type === 'text'">
        <div
          v-if="!state.editing"
          @dblclick="handleEnableEdit"
          class="text-center min-w-[50px] whitespace-pre-wrap leading-tight drop-shadow-lg p-2 border-2 border-transparent group-hover:border-white/40 rounded-lg"
          :style="element.styles"
        >
          {{ element.content }}
        </div>
        <textarea
          v-else
          v-focus
          v-model="textValue"
          @blur="handleDisableEdit"
          @keydown.enter.stop="handleDisableEdit"
          @mousedown.stop
          class="bg-transparent text-center resize-none outline-none overflow-hidden min-w-[200px] p-2 border-2 border-blue-500 rounded-lg"
          :style="element.styles"
          rows="2"
        ></textarea>
      </template>
    </div>
  </div>
</template>

<style scoped>
@keyframes spin-record {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.animate-spin-record {
  animation: spin-record 8s linear infinite;
}
</style>
