<template>
  <div
    v-tooltip.top="'Naciśnij i przytrzymaj, by powiększyć'"
    class="flex items-center justify-center shrink-0 w-[40px] h-[40px] cursor-pointer"
    @mousedown="handlePressStart"
    @mouseup="handlePressEnd"
    @mouseleave="handlePressEnd"
    @touchstart.prevent="handlePressStart"
    @touchend.prevent="handlePressEnd"
  >
    <div
      class="transition-transform duration-200 ease-out"
      :style="{ transform: `scale(${currentScale})` }"
    >
      <Emoji :data="emojiIndex" :emoji="emoji" set="facebook" :size="28" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onUnmounted } from 'vue'
import { Emoji, EmojiIndex } from 'emoji-mart-vue-fast/src'
import data from 'emoji-mart-vue-fast/data/all.json'
import { el } from 'date-fns/locale'

const props = defineProps<{
  emoji: string
}>()

const emit = defineEmits<{
  'send-like': [sizeState: 'default' | 'small' | 'medium' | 'large']
}>()


const emojiIndex = new EmojiIndex(data)


const currentScale = ref(1)
const pressStartTime = ref<number | null>(null)
const animationTimer = ref<ReturnType<typeof setInterval> | null>(null)

const handlePressStart = (event: MouseEvent | TouchEvent) => {
  if (event instanceof MouseEvent && event.button !== 0) return
  if (pressStartTime.value !== null) return

  pressStartTime.value = Date.now()

  // Płynne zwiększanie skali co 100ms
  animationTimer.value = setInterval(() => {
    const elapsed = Date.now() - (pressStartTime.value || 0)
    if (elapsed < 1000)
      currentScale.value = 1.0 // default
    else if (elapsed < 2000)
      currentScale.value = 1.2 // small
    else if (elapsed < 3000)
      currentScale.value = 1.4 // medium
    else if (elapsed < 4000)
      currentScale.value = 1.8 // large
    else {
      // Maksymalny rozmiar osiągnięty
      if (animationTimer.value) {
        clearInterval(animationTimer.value)
        clearInterval(animationTimer.value)
        currentScale.value = 1.0
      }
    }
  }, 100)
}

const handlePressEnd = () => {
  if (pressStartTime.value === null) return

  const durationMs = Date.now() - pressStartTime.value

  // Zatrzymujemy timer
  if (animationTimer.value) clearInterval(animationTimer.value)

  let sizeState: 'default' | 'small' | 'medium' | 'large' = 'default'

  if (durationMs < 1000) sizeState = 'default'
  else if (durationMs < 2000) sizeState = 'small'
  else if (durationMs < 3000) sizeState = 'medium'
  else if (durationMs < 4000) sizeState = 'large'

  emit('send-like', sizeState)

  // Resetujemy stan wizualny
  pressStartTime.value = null
  currentScale.value = 1
}

onUnmounted(() => {
  if (animationTimer.value) clearInterval(animationTimer.value)
})
</script>

<style scoped>

.reaction-btn {
  user-select: none;
  -webkit-tap-highlight-color: transparent;
}
</style>
