<template>
  <div ref="containerRef" class="relative w-full h-full min-h-0 overflow-hidden">
    <div
      ref="contentRef"
      class="w-full h-full overflow-y-auto scroll-smooth custom-scrollbar-hide"
      :style="{ maxHeight: maxHeight || 'none' }"
      @scroll="updateThumb"
    >
      <div ref="slotRef">
        <slot />
      </div>
    </div>

    <div
      v-show="scrollNeeded"
      class="absolute top-0 right-0 w-1.5 rounded-full bg-gray-400/60 transition-opacity duration-300 cursor-pointer z-50 hover:bg-gray-500"
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
import { ref, onMounted, toRefs, watchEffect } from 'vue'
import { useElementHover, useResizeObserver } from '@vueuse/core'

const props = defineProps<{
  maxHeight?: string
}>();
const { maxHeight } = toRefs(props);

const containerRef = ref<HTMLElement | null>(null)
const contentRef = ref<HTMLElement | null>(null)
const slotRef = ref<HTMLElement | null>(null)

const thumbHeight = ref(20)
const thumbTop = ref(0)
const scrollNeeded = ref(false)
const visible = ref(false)

const hovered = useElementHover(containerRef)
let fadeTimeout: number | null = null

function updateThumb() {
  const el = contentRef.value
  if (!el) return

  const { clientHeight, scrollHeight, scrollTop } = el

  // Scroll potrzebny tylko gdy treść wystaje poza clientHeight
  scrollNeeded.value = scrollHeight > clientHeight

  if (scrollNeeded.value) {
    // Proporcjonalna wysokość paska
    thumbHeight.value = Math.max((clientHeight / scrollHeight) * clientHeight, 30)
    // Proporcjonalna pozycja paska
    thumbTop.value = (scrollTop / scrollHeight) * clientHeight
  }
}

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

// Obserwujemy ZARÓWNO kontener, jak i treść w środku (slotRef)
useResizeObserver(contentRef, updateThumb)
useResizeObserver(slotRef, updateThumb)

// --- LOGIKA DRAG ---
let startY = 0
let startScrollTop = 0

function startDrag(event: MouseEvent) {
  if (!contentRef.value) return
  startY = event.clientY
  startScrollTop = contentRef.value.scrollTop
  document.addEventListener('mousemove', onDrag)
  document.addEventListener('mouseup', stopDrag)
  document.body.style.userSelect = 'none'
}

function onDrag(event: MouseEvent) {
  if (!contentRef.value) return
  const { clientHeight, scrollHeight } = contentRef.value
  const deltaY = event.clientY - startY

  // Przelicznik: o ile px przesunąć scroll względem ruchu myszki
  const scrollRatio = scrollHeight / clientHeight
  contentRef.value.scrollTop = startScrollTop + deltaY * scrollRatio
}

function stopDrag() {
  document.removeEventListener('mousemove', onDrag)
  document.removeEventListener('mouseup', stopDrag)
  document.body.style.userSelect = ''
}

onMounted(() => {
  updateThumb()

  const wheelHandler = (event: WheelEvent) => {
    const el = contentRef.value
    if (!el) return
    const { scrollTop, scrollHeight, clientHeight } = el
    const atTop = scrollTop === 0
    const atBottom = Math.abs(scrollHeight - clientHeight - scrollTop) < 1

    if ((atTop && event.deltaY < 0) || (atBottom && event.deltaY > 0)) {
      // Pozwól na scroll strony tylko jeśli jesteśmy na krawędziach
    } else {
       event.stopPropagation()
    }
  }
  contentRef.value?.addEventListener('wheel', wheelHandler, { passive: true })
})
</script>

<style scoped>
/* Ukrycie natywnego scrollbara */
.custom-scrollbar-hide {
  -ms-overflow-style: none;
  scrollbar-width: none;
}
.custom-scrollbar-hide::-webkit-scrollbar {
  display: none;
}
</style>
