<script setup lang="ts">
import Close from 'vue-material-design-icons/Close.vue'


const emit = defineEmits(['close'])

const closeModal = () => {
    emit('close')
}
defineProps<{
    title?: string
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
      <div class="p-4 border-b border-theme-border flex items-center justify-center relative">
        <h2 class="text-xl text-theme-text font-bold">{{ title }}</h2>
        <button
          @click="closeModal"
          class="absolute right-4 text-theme-text hover:bg-theme-hover rounded-full p-2"
        >
          <Close :size="24" fillColor="#65686C" />
        </button>
      </div>


      <slot/>
    </div>
  </div>
  </Teleport>
</template>
