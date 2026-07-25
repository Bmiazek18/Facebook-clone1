<template>
  <svg
    class="spinner"
    :style="{ width: computedSize, height: computedSize }"
    viewBox="0 0 120 120"
    xmlns="http://www.w3.org/2000/svg"
  >
    <circle
      class="spinner-circle"
      cx="60"
      cy="60"
      r="46"
      :stroke="color"
      fill="none"
    />
  </svg>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = withDefaults(defineProps<{
  size?: string | number
  color?: string
}>(), {
  size: '40px', // Domyślny rozmiar, idealny dla małych komponentów
  color: '#fff' // Domyślny kolor ciemnoszary
})

// Jeśli podasz samą liczbę (np. :size="50"), Vue potraktuje to jako '50px'
const computedSize = computed(() => {
  return typeof props.size === 'number' ? `${props.size}px` : props.size
})
</script>

<style scoped>
.spinner {
  /* Usunięte position: absolute i sztywne wymiary */
  display: inline-block;
}

.spinner-circle {
  --r: 47px;
  --1deg: calc(2 * pi * var(--r) / 360);

  /* Zmniejszyłem lekko intensywność cienia (0.2), by wyglądał lepiej na mniejszej ikonie */
  filter: drop-shadow(0px 0px 5px rgba(0, 0, 0, 0.2));
  r: var(--r);
  stroke-width: 10px;
  transform-origin: 50% 50%;

  animation:
    dash-anim 1400ms ease-in-out infinite,
    full-rotation-anim 2000ms linear infinite;
}

@keyframes dash-anim {
  0% {
    stroke-dasharray: 0 0 calc(2 * var(--1deg)) calc(358 * var(--1deg));
  }
  50% {
    stroke-dasharray: 0 calc(35 * var(--1deg)) calc(290 * var(--1deg)) calc(35 * var(--1deg));
  }
  100% {
    stroke-dasharray: 0 calc(358 * var(--1deg)) calc(2 * var(--1deg));
  }
}

@keyframes full-rotation-anim {
  0% {
    transform: rotate(0);
  }
  100% {
    transform: rotate(360deg);
  }
}
</style>
