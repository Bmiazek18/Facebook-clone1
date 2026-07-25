<script setup lang="ts">
import { computed } from 'vue'
import LazyEmojiPicker from '@/components/common/LazyEmojiPicker.vue'
import EmoticonHappyIcon from 'vue-material-design-icons/EmoticonHappy.vue'

// --- FLOATING VUE ---
import { Dropdown as VDropdown } from 'floating-vue'
import 'floating-vue/dist/style.css'

interface CardBackground {
  id: number
  class: string
  textClass?: string
}

const props = defineProps<{
  modelValue: string
  bgId: number
  backgrounds: CardBackground[]
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', v: string): void
  (e: 'update:bgId', v: number): void
  (e: 'close'): void
}>()

const localText = computed({
  get: () => props.modelValue,
  set: (v: string) => emit('update:modelValue', v),
})

const addEmoji = (e: { native: string }) => {
  emit('update:modelValue', props.modelValue + e.native)
}

const selectBg = (id: number) => {
  emit('update:bgId', id)
  if (id === 0) {
    emit('close')
  }
}

const currentClass = computed(() => {
  const found = props.backgrounds.find((b) => b.id === props.bgId)
  return found ? found.class : ''
})

const currentTextClass = computed(() => {
  const found = props.backgrounds.find((b) => b.id === props.bgId)
  return found && found.textClass ? found.textClass : 'text-white'
})
</script>

<template>
  <div
    class="relative mb-4 bg-theme-bg-tertiary rounded-lg overflow-hidden border border-theme-border"
  >
    <div
      :class="['w-full h-60 rounded-lg flex items-center justify-center relative', currentClass]"
    >
      <div class="z-10 w-full h-full px-4 flex items-center justify-center">
        <textarea
          v-model="localText"
          class="w-full resize-none overflow-hidden bg-transparent text-center outline-none px-2 py-6 placeholder-theme-text-placeholder"
          :class="[currentTextClass, 'text-2xl leading-normal']"
          :placeholder="$t('post.writeText')"
          spellcheck="false"
        ></textarea>
      </div>

      <div class="absolute bottom-3 right-3 z-40">
        <VDropdown
          placement="top-end"
          :distance="10"
          :skidding="0"
          :triggers="['click']"
          :autoHide="true"
        >
          <button
            class="bg-theme-bg-secondary p-2 rounded-full shadow-theme-shadow-sm text-theme-text-secondary hover:bg-theme-bg-hover transition"
          >
            <EmoticonHappyIcon :size="20" />
          </button>

          <template #popper>
            <div class="emoji-popper-content">
              <LazyEmojiPicker @select="addEmoji" />
            </div>
          </template>
        </VDropdown>
      </div>

      <div class="absolute bottom-3 left-0 right-0 flex items-center justify-center gap-2 z-30">
        <template v-for="bg in backgrounds" :key="bg.id">
          <button
            @click="selectBg(bg.id)"
            :class="[
              'w-8 h-8 rounded-md overflow-hidden',
              bg.id === bgId ? 'ring-2 ring-theme-border-highlight' : 'ring-0',
            ]"
          >
            <div :class="bg.class + ' w-full h-full'" />
          </button>
        </template>
      </div>
    </div>
  </div>
</template>

<style scoped>
textarea::placeholder {
  opacity: 0.75;
}

/* Stylizacja kontenera popovera */
.emoji-popper-content {
  max-width: 320px;
  max-height: 400px;
  overflow: hidden;
}
</style>
