<template>
  <div class="">
    <div ref="container" class="w-[35px] h-[40px]"></div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch } from 'vue'

const props = defineProps({
  animationData: { type: Object, required: true },
  size: { type: Number, default: 40 },
  loop: { type: Boolean, default: false },
})

const container = ref<HTMLElement | null>(null)
let anim: any = null

onMounted(async () => {
  if (!container.value) return
  const lottieModule = await import('lottie-web')
  const lottie = lottieModule.default || lottieModule
  if (!container.value) return

  anim = lottie.loadAnimation({
    container: container.value,
    renderer: 'svg',
    autoplay: true,
    animationData: props.animationData,
    rendererSettings: {
      progressiveLoad: true,
      hideOnTransparent: true,
      preserveAspectRatio: 'xMidYMid meet',
    },
  })
})

onUnmounted(() => {
  if (anim) {
    anim.destroy()
    anim = null
  }
})

// Restart animacji przy każdej zmianie
watch(
  () => props.animationData,
  async () => {
    if (!container.value) return
    const lottieModule = await import('lottie-web')
    const lottie = lottieModule.default || lottieModule
    if (anim) {
      anim.destroy()
    }
    if (container.value) {
      anim = lottie.loadAnimation({
        container: container.value,
        renderer: 'svg',
        loop: props.loop,
        autoplay: true,
        animationData: props.animationData,
      })
    }
  },
)
</script>
<style scoped>
.lottie-box svg {
  shape-rendering: geometricPrecision;
  image-rendering: -webkit-optimize-contrast; /* Dla lepszej ostrości */
}
</style>
