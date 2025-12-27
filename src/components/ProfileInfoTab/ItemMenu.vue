<script setup lang="ts">
import { Dropdown as VDropdown } from 'floating-vue'
import 'floating-vue/dist/style.css'

// Ikony
import DotsHorizontal from 'vue-material-design-icons/DotsHorizontal.vue'
import PencilOutline from 'vue-material-design-icons/PencilOutline.vue'
import TrashCanOutline from 'vue-material-design-icons/TrashCanOutline.vue'

// Definiujemy co ten komponent przyjmuje
defineProps<{
  editText: string
  removeText: string
}>()

// Definiujemy jakie zdarzenia wysyła w górę
const emit = defineEmits(['edit', 'remove'])
</script>

<template>
  <VDropdown placement="bottom-end" :distance="10">
    <button class="hover:bg-gray-200 p-2 rounded-full bg-gray-100 transition-colors outline-none">
      <DotsHorizontal class="text-xl text-gray-700" />
    </button>

    <template #popper="{ hide }">
      <div class="flex flex-col min-w-[240px] py-2 bg-white rounded-lg shadow-xl text-gray-700 font-medium text-sm">

        <button
          @click="emit('edit'); hide()"
          class="flex items-center px-4 py-2 hover:bg-gray-100 transition-colors w-full text-left"
        >
          <PencilOutline class="mr-3 text-xl" />
          {{ editText }}
        </button>

        <button
          @click="emit('remove'); hide()"
          class="flex items-center px-4 py-2 hover:bg-gray-100 transition-colors w-full text-left"
        >
          <TrashCanOutline class="mr-3 text-xl" />
          {{ removeText }}
        </button>

      </div>
    </template>
  </VDropdown>
</template>

<style>
/* Ukrycie strzałki dymka (Facebook jej nie ma w menu kontekstowym) */
.v-popper__arrow-container {
  display: none;
}
</style>
