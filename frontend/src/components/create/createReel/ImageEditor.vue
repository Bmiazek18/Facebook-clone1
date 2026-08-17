<template>
  <div v-if="image" class="bg-gray-800 rounded-lg p-4 space-y-3">
    <h4 class="text-white font-semibold">Edycja Obrazka</h4>

    <div>
      <label class="text-gray-400 text-sm block mb-1">Rotacja: {{ image.rotation }}°</label>
      <input
        :model-value="image.rotation"
        @input="
          emit('update', { ...image, rotation: Number(($event.target as HTMLInputElement).value) })
        "
        type="range"
        min="0"
        max="360"
        class="w-full"
      />
    </div>

    <div>
      <label class="text-gray-400 text-sm block mb-1"
        >Przezroczystość: {{ (image.opacity * 100).toFixed(0) }}%</label
      >
      <input
        :model-value="image.opacity"
        @input="
          emit('update', { ...image, opacity: Number(($event.target as HTMLInputElement).value) })
        "
        type="range"
        min="0"
        max="1"
        step="0.1"
        class="w-full"
      />
    </div>

    <div class="grid grid-cols-2 gap-2">
      <div>
        <label class="text-gray-400 text-sm block mb-1">Start (s)</label>
        <input
          :model-value="image.startTime"
          @input="
            emit('update', {
              ...image,
              startTime: Number(($event.target as HTMLInputElement).value),
            })
          "
          type="number"
          step="0.1"
          class="w-full bg-gray-700 text-white px-3 py-2 rounded border border-gray-600 focus:border-blue-500 outline-none"
        />
      </div>
      <div>
        <label class="text-gray-400 text-sm block mb-1">Koniec (s)</label>
        <input
          :model-value="image.endTime"
          @input="
            emit('update', { ...image, endTime: Number(($event.target as HTMLInputElement).value) })
          "
          type="number"
          step="0.1"
          class="w-full bg-gray-700 text-white px-3 py-2 rounded border border-gray-600 focus:border-blue-500 outline-none"
        />
      </div>
    </div>

    <div>
      <label class="text-gray-400 text-sm block mb-1">Animacja Wejścia</label>
      <select
        :model-value="image.entryAnimation"
        @change="
          emit('update', { ...image, entryAnimation: ($event.target as HTMLSelectElement).value })
        "
        class="w-full bg-gray-700 text-white px-3 py-2 rounded border border-gray-600 focus:border-blue-500 outline-none"
      >
        <option value="none">Brak</option>
        <option value="fade-in">Fade In</option>
        <option value="zoom-in">Zoom In</option>
        <option value="slide-in-left">Slide In ←</option>
        <option value="slide-in-right">Slide In →</option>
      </select>
    </div>

    <div>
      <label class="text-gray-400 text-sm block mb-1">Animacja Wyjścia</label>
      <select
        :model-value="image.exitAnimation"
        @change="
          emit('update', { ...image, exitAnimation: ($event.target as HTMLSelectElement).value })
        "
        class="w-full bg-gray-700 text-white px-3 py-2 rounded border border-gray-600 focus:border-blue-500 outline-none"
      >
        <option value="none">Brak</option>
        <option value="fade-out">Fade Out</option>
        <option value="zoom-out">Zoom Out</option>
        <option value="slide-out-left">Slide Out ←</option>
        <option value="slide-out-right">Slide Out →</option>
      </select>
    </div>

    <button
      @click="emit('delete')"
      class="w-full bg-red-600 hover:bg-red-700 text-white py-2 rounded-lg transition-colors"
    >
      Usuń Obrazek
    </button>
  </div>
</template>

<script setup lang="ts">
import type { ImageOverlay } from '@/types/video-editor.types'

interface Props {
  image: ImageOverlay | null
}

defineProps<Props>()

const emit = defineEmits<{
  update: [image: ImageOverlay]
  delete: []
}>()
</script>
