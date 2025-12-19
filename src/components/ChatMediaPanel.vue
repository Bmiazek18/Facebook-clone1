<template>
  <div class="h-full flex flex-col bg-white">

    <div class="flex items-center px-4 py-4">
      <button
        @click="$emit('close')"
        class="mr-4 hover:bg-gray-100 rounded-full p-1 transition"
        aria-label="Powrót"
      >
        <ArrowLeftIcon :size="24" class="text-black" />
      </button>
      <h2 class="text-[17px] font-bold text-gray-900">Multimedia i pliki</h2>
    </div>

    <div class="flex border-b border-gray-200 px-4">
      <button class="px-4 py-3 font-semibold text-[14px] text-blue-600 border-b-[3px] border-blue-600">
         Multimedia
      </button>
      <button class="px-4 py-3 font-semibold text-[14px] text-gray-500 hover:bg-gray-50 rounded-t-md transition ml-2">
         Pliki
      </button>
    </div>

    <div class="flex-1 overflow-y-auto custom-scrollbar p-1">
       <div class="p-2 pt-3">
          <h3 class="font-bold text-[15px] text-gray-900 mb-2">grudnia</h3>
       </div>
       <div class="grid grid-cols-3 gap-1 px-1 pb-4">
          <div v-for="n in 15" :key="n" class="relative aspect-square bg-gray-100 cursor-pointer overflow-hidden group">
             <img :src="`https://picsum.photos/300?random=${n}`" class="w-full h-full object-cover">
             <div class="absolute inset-0 bg-black/10 opacity-0 group-hover:opacity-100 transition"></div>
          </div>
       </div>
    </div>

  </div>
</template>

<script setup lang="ts">
import ArrowLeftIcon from 'vue-material-design-icons/ArrowLeft.vue';
// This component is a presentational extraction of the media panel from ChatView.
</script>

<style scoped>
/* keep styles from ChatView for the media panel */
</style>
