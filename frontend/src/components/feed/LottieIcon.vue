<template>
  <div class="">
    <div ref="container" class="w-[35px] h-[40px]"></div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import lottie from 'lottie-web'

const props = defineProps({
  animationData: { type: Object, required: true },
  size: { type: Number, default: 40 },
  loop: { type: Boolean, default: false },
})

const container = ref(null)
let anim: any = null

onMounted(() => {
  anim = lottie.loadAnimation({
    container: container.value!,
    renderer: 'svg',
    autoplay: true,
    animationData: props.animationData,
    rendererSettings: {
      progressiveLoad: true,
      hideOnTransparent: true,
      preserveAspectRatio: 'xMidYMid meet', // Zapobiega rozciąganiu
    },
  })
})

// Restart animacji przy każdej zmianie (np. gdy użytkownik ponownie kliknie)
watch(
  () => props.animationData,
  () => {
    if (anim) {
      anim.destroy()
      anim = lottie.loadAnimation({
        container: container.value!,
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
