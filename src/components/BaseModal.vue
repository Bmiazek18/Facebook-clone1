<script setup lang="ts">
import Close from 'vue-material-design-icons/Close.vue'
import ArrowLeft from 'vue-material-design-icons/ArrowLeft.vue' // Import ArrowLeft



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
</script>

<template>
  <Teleport to="body">
  <div
    @click.self="closeModal"
    class="fixed inset-0 z-55 flex items-center justify-center bg-gray-200/80 dark:bg-black/80"
  >
    <div class="bg-theme-bg-secondary rounded-lg shadow-2xl  mx-4 my-8 relative flex flex-col max-h-[98vh]">

      <!-- HEADER -->
     <div class="p-4 border-b border-theme-border grid grid-cols-3 items-center">
  <div class="flex justify-start">
    <button
      v-if="back"
      @click="handleBack"
      class="text-theme-text hover:bg-theme-hover rounded-full p-2 transition-colors"
    >
      <ArrowLeft :size="24" fillColor="#65686C" />
    </button>
  </div>

  <h2 class="text-xl text-theme-text font-bold text-center truncate">
    {{ title }}
  </h2>

  <div class="flex justify-end">
    <button
      @click="closeModal"
      class="text-theme-text hover:bg-theme-hover rounded-full p-2 transition-colors"
    >
      <Close :size="24" fillColor="#65686C" />
    </button>
  </div>
</div>


      <slot/>
    </div>
  </div>
  </Teleport>
</template>
