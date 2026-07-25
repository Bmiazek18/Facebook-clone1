<template>
  <div
    :class="[
      'flex items-center gap-3 shadow-sm pb-2 transition pl-4 pt-2',
      { 'slide-in': !hasAnimatedOnce },
    ]"
  >
    <button
      @click="goBack"
      @keydown.enter="goBack"
      v-tooltip="'Wcisnij esc, aby zamknąć'"
      class="w-11 h-11 bg-gray-400 hover:bg-gray-500 rounded-full flex items-center justify-center text-white"
    >
      <Close :size="24" />
    </button>
    <div class="w-11 h-11 bg-[#1877F2] rounded-full flex items-center justify-center text-white">
      <Facebook :size="28" />
    </div>
  </div>
</template>

<script setup lang="ts">
import Close from 'vue-material-design-icons/Close.vue'
import Facebook from 'vue-material-design-icons/Facebook.vue'
import { onMounted, onUnmounted, ref } from 'vue'

const router = useRouter()
const hasAnimatedOnce = ref(false) // Removed

const goBack = () => {
  router.back()
}

const handleEscape = (e: KeyboardEvent) => {
  if (e.key === 'Escape') {
    goBack()
  }
}

onMounted(() => {
  document.addEventListener('keydown', handleEscape)
  setTimeout(() => {
    hasAnimatedOnce.value = true
  }, 300) // Match this duration to your CSS animation duration (0.3s)
})

onUnmounted(() => {
  document.removeEventListener('keydown', handleEscape)
})
</script>
