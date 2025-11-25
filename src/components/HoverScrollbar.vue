<template>
  <div ref="containerRef" class="relative w-full h-[calc(100vh-85px)]">
    <div ref="contentRef" class="h-full w-full overflow-y-auto pr-2" @scroll="updateThumb">
      <slot />
    </div>

    <div
      v-show="scrollNeeded" class="absolute top-0 right-0 w-2 rounded-full bg-gray-400 transition-opacity duration-300 cursor-pointer"
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
import { ref, onMounted, watch } from 'vue'
import { useElementHover } from '@vueuse/core'

const containerRef = ref<HTMLElement | null>(null)
const contentRef = ref<HTMLElement | null>(null)

const thumbHeight = ref(20)
const thumbTop = ref(0)
const scrollNeeded = ref(false) // NOWOŚĆ: Flaga, czy zawartość jest większa niż kontener

const hovered = useElementHover(containerRef)
const visible = ref(false)
let fadeTimeout: number | null = null

watch(hovered, (h) => {
  if (fadeTimeout) clearTimeout(fadeTimeout)
  // Zaktualizowana logika: Widoczne, gdy najechane I przewijanie jest potrzebne
  if (h && scrollNeeded.value) {
    visible.value = true
  } else {
    fadeTimeout = window.setTimeout(() => {
      visible.value = false
    }, 500)
  }
})

let startY = 0
let startScrollTop = 0

function updateThumb() {
  if (!contentRef.value) return
  const containerHeight = contentRef.value.clientHeight
  const scrollHeight = contentRef.value.scrollHeight
  const scrollTop = contentRef.value.scrollTop

  // Ustawienie flagi scrollNeeded
  scrollNeeded.value = scrollHeight > containerHeight

  // Ustawia wysokość i pozycję tylko, jeśli przewijanie jest potrzebne,
  // aby uniknąć błędów dzielenia przez zero, chociaż nowoczesne przeglądarki
  // zazwyczaj poprawnie obsługują 0 dla scrollHeight == containerHeight.
  if (scrollNeeded.value) {
    thumbHeight.value = Math.max((containerHeight / scrollHeight) * containerHeight, 20)
    thumbTop.value = (scrollTop / scrollHeight) * containerHeight
  }
}

function startDrag(event: MouseEvent) {
  if (!contentRef.value) return

  startY = event.clientY
  startScrollTop = contentRef.value.scrollTop

  document.addEventListener('mousemove', onDrag)
  document.addEventListener('mouseup', stopDrag)
}

function onDrag(event: MouseEvent) {
  if (!contentRef.value) return
  const containerHeight = contentRef.value.clientHeight
  const scrollHeight = contentRef.value.scrollHeight
  const deltaY = event.clientY - startY

  contentRef.value.scrollTop = startScrollTop + (deltaY * scrollHeight) / containerHeight
  updateThumb()
}

function stopDrag() {
  document.removeEventListener('mousemove', onDrag)
  document.removeEventListener('mouseup', stopDrag)
}

onMounted(() => {
  updateThumb()

  // Prevent scroll from propagating to parent when at edges
  if (contentRef.value) {
    contentRef.value.addEventListener('wheel', (event) => {
      const el = contentRef.value
      if (!el) return

      const { scrollTop, scrollHeight, clientHeight } = el
      const atTop = scrollTop === 0
      const atBottom = scrollTop + clientHeight >= scrollHeight

      // If at top and scrolling up OR at bottom and scrolling down
      if ((atTop && event.deltaY < 0) || (atBottom && event.deltaY > 0)) {
        event.preventDefault()
        event.stopPropagation()
      }
    }, { passive: false })
  }
})

window.addEventListener('resize', updateThumb)

</script>

<style scoped>
/* Hide native scrollbar */
div::-webkit-scrollbar {
  width: 0;
  background: transparent;
}
</style>
