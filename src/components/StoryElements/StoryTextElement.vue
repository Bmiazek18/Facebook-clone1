<script setup lang="ts">
import { computed } from 'vue'
import type { StoryElement } from '@/types/StoryElement'

const props = defineProps<{
  element: StoryElement
  isEditing: boolean
  onEnableEdit: (id: string) => void
  onDisableEdit: () => void
}>()

const emit = defineEmits<{ 'update-content': [id: string, value: string] }>()

const textValue = computed({
  get: () => props.element.content,
  set: (val: string) => emit('update-content', props.element.id, val)
})

// Auto-focus directive
const vFocus = {
  mounted: (el: HTMLElement) => el.focus()
}
</script>

<template>
  <!-- Display Mode -->
  <div
    v-if="!isEditing"
    @dblclick="onEnableEdit(element.id)"
    class="text-center min-w-[50px] whitespace-pre-wrap leading-tight drop-shadow-lg p-2 border-2 border-transparent group-hover:border-white/40 rounded-lg"
    :style="element.styles"
  >
    {{ element.content }}
  </div>
  
  <!-- Edit Mode -->
  <textarea
    v-else
    v-focus
    v-model="textValue"
    @blur="onDisableEdit"
    @keydown.enter.stop="onDisableEdit"
    @mousedown.stop
    class="bg-transparent text-center resize-none outline-none overflow-hidden min-w-[200px] p-2 border-2 border-blue-500 rounded-lg"
    :style="element.styles"
    rows="2"
  ></textarea>
</template>
