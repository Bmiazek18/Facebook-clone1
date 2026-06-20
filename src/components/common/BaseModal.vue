<script setup lang="ts">
import { onMounted, onUnmounted } from 'vue'
import Close from 'vue-material-design-icons/Close.vue'
import ArrowLeft from 'vue-material-design-icons/ArrowLeft.vue'

const emit = defineEmits(['close','back'])

const closeModal = () => {
    emit('close')
}
const handleBack = () => {
    emit('back')
}
defineProps<{
    title?: string
    back?: boolean
}>()


onMounted(() => {
  document.body.style.overflow = 'hidden'
})

onUnmounted(() => {
  document.body.style.overflow = ''
})
</script>

<template>
  <Teleport to="body">
    <div

      @click.self="closeModal"
      class="fixed inset-0 z-55 flex items-center justify-center bg-gray-200/80 dark:bg-black/80 px-2"
    >
      <div class="bg-theme-bg-secondary rounded-lg shadow-2xl relative flex flex-col max-h-[98vh] md:w-fit w-full min-w-[320px] max-w-full">

        <div class="p-3 border-b border-theme-border grid grid-cols-[auto_1fr_auto] items-center gap-4">

          <div class="flex justify-start min-w-[40px]">
              <button
                v-if="back"
                @click="handleBack"
                class="text-theme-text hover:bg-theme-hover rounded-full p-2 transition-colors"
              >
                <ArrowLeft :size="24" fillColor="currentColor" />
              </button>
          </div>

          <h2 class="text-xl text-theme-text font-semibold text-center truncate px-2">
              {{ title }}
          </h2>

          <div class="flex justify-end min-w-[40px]">
              <button
                @click="closeModal"
                class="text-theme-text bg-gray-200 hover:bg-theme-hover rounded-full p-1.5 transition-colors"
              >
                <Close :size="24" class="text-gray-500" />
              </button>
          </div>
        </div>

        <div class=" overflow-visible">
          <slot/>
        </div>
      </div>
    </div>
  </Teleport>
</template>
