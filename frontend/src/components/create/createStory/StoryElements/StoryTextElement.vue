<script setup lang="ts">
import { computed, ref, watch, nextTick } from 'vue'
import type { StoryElement } from '@/types/StoryElement'
import { getAllUsers } from '@/utils/users'
import MentionInput from '@/components/MentionInput.vue'

const props = defineProps<{
  element: StoryElement
  isEditing: boolean
  onEnableEdit: (id: string) => void
  onDisableEdit: () => void
}>()

const emit = defineEmits<{ 'update-content': [id: string, value: string] }>()

// Local draft text state to avoid triggering parent component re-renders while typing
const localText = ref(props.element.content)

const formattedContent = computed(() => {
  if (!props.element.content) return ''
  const allUsers = getAllUsers()
  // Safe HTML replacement: escape tags first, then wrap user tags [@id] in underlined span (no @ symbol)
  return props.element.content
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/\[@([a-zA-Z0-9-]+)\]/g, (match, userId) => {
      const user = allUsers.find((u) => String(u.id) === userId)
      return user
        ? `<span class="underline font-semibold cursor-pointer">${user.name}</span>`
        : match
    })
})

const mentionInputRef = ref<any>(null)

// Watch for isEditing state changes:
// - When editing starts: load content to local ref and focus.
// - When editing ends: emit the final sformatted content to the parent.
watch(
  () => props.isEditing,
  (editing) => {
    if (editing) {
      localText.value = props.element.content
      nextTick(() => {
        mentionInputRef.value?.focus()
        mentionInputRef.value?.moveCursorToEnd()
      })
    } else {
      emit('update-content', props.element.id, localText.value)
    }
  },
  { immediate: true },
)
</script>

<template>
  <!-- Display Mode -->
 <div
    v-if="!isEditing"
    @dblclick="onEnableEdit(element.id)"
    class="flex flex-col items-center justify-center text-center min-w-[50px] whitespace-pre-wrap leading-tight drop-shadow-lg p-2 rounded-lg w-full h-full"
    :style="element.styles"
  >
    <span v-if="element.content" v-html="formattedContent"></span>
    <span v-else class="opacity-50">{{ $t('create.zacznijPisac') }}</span>
  </div>

  <!-- Edit Mode -->
  <MentionInput
    v-else
    ref="mentionInputRef"
    v-model="localText"
    @keydown.enter.stop="onDisableEdit"
    @mousedown.stop
    :inputClass="'bg-transparent text-center resize-none outline-none border-none ring-0 focus:ring-0 focus:outline-none overflow-visible min-w-[200px] p-2 rounded-lg placeholder:text-white/60 w-full h-full'"
    placeholderClass="w-full text-center p-2 text-white/60"
    :style="element.styles"
    :placeholder="$t('create.zacznijPisac')"
  />
</template>
