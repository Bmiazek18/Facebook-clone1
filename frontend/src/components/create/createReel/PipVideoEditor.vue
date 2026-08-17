<template>
  <div v-if="video" class="bg-gray-800 rounded-lg p-4 space-y-3">
    <h4 class="text-white font-semibold">Edycja Video PiP</h4>

    <div>
      <label class="text-gray-400 text-sm block mb-1">Rotacja: {{ video.rotation }}°</label>
      <input
        :model-value="video.rotation"
        @input="
          emit('update', { ...video, rotation: Number(($event.target as HTMLInputElement).value) })
        "
        type="range"
        min="0"
        max="360"
        class="w-full"
      />
    </div>

    <div>
      <label class="text-gray-400 text-sm block mb-1"
        >Przezroczystość: {{ (video.opacity * 100).toFixed(0) }}%</label
      >
      <input
        :model-value="video.opacity"
        @input="
          emit('update', { ...video, opacity: Number(($event.target as HTMLInputElement).value) })
        "
        type="range"
        min="0"
        max="1"
        step="0.1"
        class="w-full"
      />
    </div>

    <div>
      <label class="text-gray-400 text-sm block mb-1"
        >Głośność: {{ (video.volume * 100).toFixed(0) }}%</label
      >
      <input
        :model-value="video.volume"
        @input="
          emit('update', { ...video, volume: Number(($event.target as HTMLInputElement).value) })
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
          :model-value="video.startTime"
          @input="
            emit('update', {
              ...video,
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
          :model-value="video.endTime"
          @input="
            emit('update', { ...video, endTime: Number(($event.target as HTMLInputElement).value) })
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
        :model-value="video.entryAnimation"
        @change="
          emit('update', { ...video, entryAnimation: ($event.target as HTMLSelectElement).value })
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
        :model-value="video.exitAnimation"
        @change="
          emit('update', { ...video, exitAnimation: ($event.target as HTMLSelectElement).value })
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
      Usuń Video PiP
    </button>
  </div>
</template>

<script setup lang="ts">
import type { PipVideoOverlay } from '@/types/video-editor.types'

interface Props {
  video: PipVideoOverlay | null
}

defineProps<Props>()

const emit = defineEmits<{
  update: [video: PipVideoOverlay]
  delete: []
}>()
</script>
