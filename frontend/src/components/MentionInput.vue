<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { Dropdown as VDropdown } from 'floating-vue'
import { useContentEditable } from '@/composables/ui/useContentEditable'

const props = defineProps<{
  modelValue: string
  placeholder?: string
  inputClass?: string
  placeholderClass?: string
}>()
onMounted(() => {
  renderContentEditable()
  // To sprawia, że Enter wstawia <br> zamiast tworzyć nowe <div>
  document.execCommand('defaultParagraphSeparator', false, 'br')
})
const emit = defineEmits<{
  (e: 'update:modelValue', value: string): void
  (e: 'input', event: Event): void
  (e: 'keydown', event: KeyboardEvent): void
  (e: 'focus', event: FocusEvent): void
  (e: 'blur', event: FocusEvent): void
}>()

const contentEditableDiv = ref<HTMLDivElement | null>(null)
const localContent = ref(props.modelValue)

const {
  onContentInput: originalOnContentInput,
  matchingUsers,
  showUserDropdown,
  selectUser,
  renderContentEditable,
  addEmoji,
  moveCursorToEnd,
} = useContentEditable(contentEditableDiv, localContent)

watch(
  () => props.modelValue,
  (val) => {
    if (val !== localContent.value) {
      localContent.value = val
      nextTick(() => {
        renderContentEditable()
      })
    }
  },
)

// Watch localContent changes from the composable and emit update
watch(localContent, (val) => {
  emit('update:modelValue', val)
})

const onInput = (e: Event) => {
  originalOnContentInput()
  emit('input', e)
}

const onKeydown = (e: KeyboardEvent) => {
  emit('keydown', e)
}

onMounted(() => {
  renderContentEditable()
})

defineExpose({
  addEmoji,
  focus: () => contentEditableDiv.value?.focus(),
  moveCursorToEnd,
})
</script>

<template>
  <div class="relative w-full">
    <VDropdown
      :shown="showUserDropdown"
      placement="bottom-start"
      :triggers="[]"
      :auto-hide="true"
      :no-auto-focus="true"
      class="w-full"
      popper-class="v-popper--theme-menu"
    >
      <div
        ref="contentEditableDiv"
        contenteditable="true"
        @input="onInput"
        @keydown="onKeydown"
        @focus="$emit('focus', $event)"
        @blur="$emit('blur', $event)"
        :class="
          inputClass ||
          'w-full bg-transparent border-none outline-none focus:ring-0 p-0 text-[15px] text-[#050505] resize-none overflow-hidden min-h-[22px] leading-relaxed whitespace-pre-wrap'
        "
      ></div>
      <template #popper>
        <div
          class="user-dropdown-content w-64 max-h-60 overflow-y-auto pointer-events-auto bg-white dark:bg-gray-800 shadow-lg rounded-lg"
        >
          <ul>
            <li
              v-for="user in matchingUsers"
              :key="user.id"
              class="px-4 py-2 cursor-pointer hover:bg-gray-100 dark:hover:bg-gray-700 flex items-center gap-2"
              @mousedown.prevent="selectUser(user)"
            >
              <img :src="user.avatar" class="w-8 h-8 rounded-full object-cover" />
              <span class="font-medium text-sm text-gray-900 dark:text-gray-100">{{
                user.name
              }}</span>
            </li>
          </ul>
        </div>
      </template>
    </VDropdown>
    <div
      v-if="!modelValue && placeholder"
      class="absolute top-0 left-0 text-gray-500 pointer-events-none text-[15px]"
      :class="placeholderClass"
    >
      {{ placeholder }}
    </div>
  </div>
</template>
