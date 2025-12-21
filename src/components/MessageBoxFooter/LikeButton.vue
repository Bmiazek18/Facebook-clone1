<template>
    <div
        class="flex space-x-1 text-gray-500 shrink-0 cursor-pointer transition-transform duration-100"
        @mousedown="handlePressStart"
        @mouseup="handlePressEnd"
        @mouseleave="handlePressEnd"
        @touchstart.prevent="handlePressStart"
        @touchend.prevent="handlePressEnd"
      >
       <span :class="[currentIconState, 'text-blue-500 hover:text-blue-700 text-2xl']">{{ emoji }}</span>
      </div>
</template>

<script setup lang="ts">
import { ref, onUnmounted } from 'vue';

const props = defineProps<{
    emoji: string;
}>();

const emit = defineEmits<{
    'send-like': [sizeState: 'default' | 'small' | 'medium' | 'large'];
}>();

const animationTimer = ref<ReturnType<typeof setInterval> | null>(null);
const pressStartTime = ref<number | null>(null);
const currentPressDurationVisual = ref(0);
const ICON_CLASSES = ['icon-state-0', 'icon-state-1', 'icon-state-2', 'icon-state-3'];
const currentIconState = ref(ICON_CLASSES[0]);
const MAX_PRESS_TIME_MS = 5000;
const DURATION_STEP_MS = 1000;


const clearTimers = () => {
  if (animationTimer.value) {
    clearInterval(animationTimer.value);
    animationTimer.value = null;
  }
  pressStartTime.value = null;
  currentPressDurationVisual.value = 0;
  currentIconState.value = ICON_CLASSES[0];
};

const handlePressStart = (event: MouseEvent | TouchEvent) => {
  if (event instanceof MouseEvent && event.button !== 0) return;
  if (pressStartTime.value !== null) return;

  pressStartTime.value = Date.now();
  currentIconState.value = ICON_CLASSES[0];
  currentPressDurationVisual.value = 0;

  const increaseIconState = () => {
    currentPressDurationVisual.value += 1;

    if (currentPressDurationVisual.value < ICON_CLASSES.length) {
      currentIconState.value = ICON_CLASSES[currentPressDurationVisual.value];
    } else {
      clearTimers();
    }
  };

  animationTimer.value = setInterval(increaseIconState, DURATION_STEP_MS);
};

const handlePressEnd = () => {
  if (pressStartTime.value === null) return;

  const durationMs = Date.now() - pressStartTime.value;
  clearTimers();

  const durationSeconds = durationMs / 1000;

  if (durationMs > MAX_PRESS_TIME_MS) {
    return;
  }

  let sizeState: 'default' | 'small' | 'medium' | 'large' = 'default';

  if (durationSeconds >= 3) {
    sizeState = 'large';
  } else if (durationSeconds >= 2) {
    sizeState = 'medium';
  } else if (durationSeconds >= 1) {
    sizeState = 'small';
  }
  emit('send-like', sizeState);
};


onUnmounted(() => {
  clearTimers();
});
</script>

<style scoped>
.icon-state-0 {
    width: 24px;
    height: 24px;
    font-size: 24px;
    transition: all 0.3s ease-out;
}
.icon-state-1 {
    width: 30px;
    height: 30px;
    font-size: 30px;
    transition: all 0.3s ease-out;
}
.icon-state-2 {
    width: 36px;
    height: 36px;
    font-size: 36px;
    transition: all 0.3s ease-out;
}
.icon-state-3 {
    width: 48px;
    height: 48px;
    font-size: 48px;
    transition: all 0.3s ease-out;
}
</style>
