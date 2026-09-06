<script setup lang="ts">
import { onMounted, onUnmounted, watch } from 'vue'

const props = defineProps<{
  visible: boolean
  imgs: string[]
  index: number
  title?: string
}>()

const emit = defineEmits<{
  (e: 'update:visible', value: boolean): void
  (e: 'update:index', value: number): void
}>()

const close = () => {
  emit('update:visible', false)
}

const next = (e?: Event) => {
  if (e) e.stopPropagation()
  if (props.index < props.imgs.length - 1) {
    emit('update:index', props.index + 1)
  } else {
    emit('update:index', 0)
  }
}

const prev = (e?: Event) => {
  if (e) e.stopPropagation()
  if (props.index > 0) {
    emit('update:index', props.index - 1)
  } else {
    emit('update:index', props.imgs.length - 1)
  }
}

const handleKeyDown = (e: KeyboardEvent) => {
  if (!props.visible) return
  if (e.key === 'Escape') close()
  if (e.key === 'ArrowRight' && props.imgs.length > 1) next()
  if (e.key === 'ArrowLeft' && props.imgs.length > 1) prev()
}

watch(() => props.visible, (newVal) => {
  if (newVal) {
    document.body.style.overflow = 'hidden'
  } else {
    document.body.style.overflow = ''
  }
})

onMounted(() => window.addEventListener('keydown', handleKeyDown))
onUnmounted(() => {
  window.removeEventListener('keydown', handleKeyDown)
  document.body.style.overflow = ''
})
</script>

<template>
  <Transition name="fade">
    <div 
      v-if="visible" 
      class="fixed inset-0 z-[9999] flex items-center justify-center bg-black/80 select-none overflow-hidden"
      @click="close"
    >
      <div class="absolute inset-0 z-0 pointer-events-none transform scale-125">
        <img 
          :src="imgs[index]" 
          class="w-full h-full object-cover blur-[80px] brightness-[0.3]" 
          :alt="$t('metaAi.tlo')"
        />
      </div>

      <div class="absolute top-6 left-6 z-[10000] flex items-center gap-3 text-white/90">
        <button 
          @click="close" 
          class="p-1.5 hover:bg-white/10 rounded-full transition duration-200 cursor-pointer text-white"
          :aria-label="$t('ui.back')"
        >
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
            <line x1="19" y1="12" x2="5" y2="12"></line>
            <polyline points="12 19 5 12 12 5"></polyline>
          </svg>
        </button>

        <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="text-white/80">
          <rect x="3" y="3" width="18" height="18" rx="2" ry="2"></rect>
          <circle cx="8.5" cy="8.5" r="1.5"></circle>
          <polyline points="21 15 16 10 5 21"></polyline>
        </svg>

        <span class="text-[17px] font-normal tracking-wide drop-shadow-md">
          {{ title || 'zrzut_ekranu_podgląd.png' }}
        </span>
      </div>

      <button 
        v-if="imgs.length > 1"
        @click="prev" 
        class="absolute left-6 z-[10000] p-4 text-white/60 hover:text-white bg-black/20 hover:bg-white/10 rounded-full transition duration-200 cursor-pointer backdrop-blur-md"
      >
        <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
          <polyline points="15 18 9 12 15 6"></polyline>
        </svg>
      </button>

      <div class="relative max-w-[85vw] max-h-[82vh] flex flex-col items-center z-10" @click.stop>
        <img 
          :src="imgs[index]" 
          :alt="$t('chat.podglad')" 
          class="max-w-full max-h-[82vh] object-contain rounded border border-white/10 shadow-2xl animate-zoom"
        />
        
        <div v-if="imgs.length > 1" class="absolute -bottom-12 left-1/2 transform -translate-x-1/2 text-sm text-white/60 font-medium bg-black/40 px-3 py-1 rounded-full backdrop-blur-sm">
          {{ index + 1 }} / {{ imgs.length }}
        </div>
      </div>

      <button 
        v-if="imgs.length > 1"
        @click="next" 
        class="absolute right-6 z-[10000] p-4 text-white/60 hover:text-white bg-black/20 hover:bg-white/10 rounded-full transition duration-200 cursor-pointer backdrop-blur-md"
      >
        <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
          <polyline points="9 18 15 12 9 6"></polyline>
        </svg>
      </button>
    </div>
  </Transition>
</template>

<style scoped>
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.animate-zoom {
  animation: zoomIn 0.22s cubic-bezier(0.2, 0, 0.2, 1);
}

@keyframes zoomIn {
  from {
    transform: scale(0.97);
    opacity: 0;
  }
  to {
    transform: scale(1);
    opacity: 1;
  }
}
</style>
