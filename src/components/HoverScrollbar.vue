<template>
  <div ref="containerRef" class="relative w-full">
    <div
      ref="contentRef"
      class="w-full overflow-y-auto pr-2"
      :style="{ maxHeight: maxHeight || 'none' }"
      @scroll="updateThumb"
    >
      <slot />
    </div>

    <div
      v-show="scrollNeeded"
      class="absolute top-0 right-0 w-2 rounded-full bg-gray-400 transition-opacity duration-300 cursor-pointer"
      :style="{
        height: thumbHeight + 'px',
        transform: `translateY(${thumbTop}px)`,
        opacity: visible ? 1 : 0,
      }"
      @mousedown.prevent="startDrag"
    ></div>
  </div>
</template>

<script lang="ts" setup>
import { ref, onMounted, onUnmounted, toRefs } from 'vue'
import { useElementHover, useResizeObserver } from '@vueuse/core'

const props = defineProps<{
  maxHeight?: string
}>();
const { maxHeight } = toRefs(props);

const containerRef = ref<HTMLElement | null>(null)
const contentRef = ref<HTMLElement | null>(null)

const thumbHeight = ref(20)
const thumbTop = ref(0)
const scrollNeeded = ref(false)
const visible = ref(false)

const hovered = useElementHover(containerRef)
let fadeTimeout: number | null = null

// Główna funkcja aktualizująca wymiary
function updateThumb() {
  const el = contentRef.value
  if (!el) return

  const { clientHeight, scrollHeight, scrollTop } = el

  scrollNeeded.value = scrollHeight > clientHeight

  if (scrollNeeded.value) {
    // Proporcjonalna wysokość paska (min. 20px)
    thumbHeight.value = Math.max((clientHeight / scrollHeight) * clientHeight, 20)
    // Proporcjonalna pozycja paska
    thumbTop.value = (scrollTop / scrollHeight) * clientHeight
  }

  // Automatyczne ukrywanie, jeśli scroll przestał być potrzebny
  if (!scrollNeeded.value) visible.value = false
}

// Reakcja na hover z uwzględnieniem scrollNeeded
import { watchEffect } from 'vue'
watchEffect(() => {
  if (fadeTimeout) clearTimeout(fadeTimeout)

  if (hovered.value && scrollNeeded.value) {
    visible.value = true
  } else {
    fadeTimeout = window.setTimeout(() => {
      visible.value = false
    }, 500)
  }
})

// --- REAKTYWNOŚĆ ROZMIARU ---
// Obserwujemy contentRef: zadziała przy zmianie okna,
// zmianie zawartości slotu i zmianie maxHeight
useResizeObserver(contentRef, () => {
  updateThumb()
})

// Logika drag (bez zmian)
let startY = 0
let startScrollTop = 0

function startDrag(event: MouseEvent) {
  if (!contentRef.value) return
  startY = event.clientY
  startScrollTop = contentRef.value.scrollTop
  document.addEventListener('mousemove', onDrag)
  document.addEventListener('mouseup', stopDrag)
}

function onDrag(event: MouseEvent) {
  if (!contentRef.value) return
  const { clientHeight, scrollHeight } = contentRef.value
  const deltaY = event.clientY - startY
  contentRef.value.scrollTop = startScrollTop + (deltaY * scrollHeight) / clientHeight
  updateThumb()
}

function stopDrag() {
  document.removeEventListener('mousemove', onDrag)
  document.removeEventListener('mouseup', stopDrag)
}

onMounted(() => {
  // Blokowanie propagacji scrolla (overscroll-behavior: contain w CSS mógłby to uprościć)
  const wheelHandler = (event: WheelEvent) => {
    const el = contentRef.value
    if (!el) return
    const { scrollTop, scrollHeight, clientHeight } = el
    const atTop = scrollTop === 0
    const atBottom = Math.abs(scrollHeight - clientHeight - scrollTop) < 1

    if ((atTop && event.deltaY < 0) || (atBottom && event.deltaY > 0)) {
      event.preventDefault()
    }
  }
  contentRef.value?.addEventListener('wheel', wheelHandler, { passive: false })

  onUnmounted(() => {
    contentRef.value?.removeEventListener('wheel', wheelHandler)
  })
})

</script>

<style scoped>
/* Hide native scrollbar */
div::-webkit-scrollbar {
  width: 0;
  background: transparent;
}
</style>
