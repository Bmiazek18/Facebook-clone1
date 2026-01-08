<template>
  <div v-if="text" class="bg-gray-800 rounded-lg p-4 space-y-3">
    <h4 class="text-white font-semibold">Edycja Tekstu</h4>

    <div>
      <label class="text-gray-400 text-sm block mb-1">Treść</label>
      <input
        :model-value="text.content"
        @input="emit('update', { ...text, content: ($event.target as HTMLInputElement).value })"
        type="text"
        class="w-full bg-gray-700 text-white px-3 py-2 rounded border border-gray-600 focus:border-blue-500 outline-none"
        placeholder="Wpisz tekst..."
      />
    </div>

    <div>
      <label class="text-gray-400 text-sm block mb-1">Rozmiar: {{ text.fontSize }}px</label>
      <input
        :model-value="text.fontSize"
        @input="emit('update', { ...text, fontSize: Number(($event.target as HTMLInputElement).value) })"
        type="range"
        min="12"
        max="200"
        class="w-full"
      />
    </div>

    <div>
      <label class="text-gray-400 text-sm block mb-1">Kolor</label>
      <input
        :model-value="text.color"
        @input="emit('update', { ...text, color: ($event.target as HTMLInputElement).value })"
        type="color"
        class="w-full h-10 rounded border border-gray-600"
      />
    </div>

    <div class="grid grid-cols-2 gap-2">
      <div>
        <label class="text-gray-400 text-sm block mb-1">Start (s)</label>
        <input
          :model-value="text.startTime"
          @input="emit('update', { ...text, startTime: Number(($event.target as HTMLInputElement).value) })"
          type="number"
          step="0.1"
          class="w-full bg-gray-700 text-white px-3 py-2 rounded border border-gray-600 focus:border-blue-500 outline-none"
        />
      </div>
      <div>
        <label class="text-gray-400 text-sm block mb-1">Koniec (s)</label>
        <input
          :model-value="text.endTime"
          @input="emit('update', { ...text, endTime: Number(($event.target as HTMLInputElement).value) })"
          type="number"
          step="0.1"
          class="w-full bg-gray-700 text-white px-3 py-2 rounded border border-gray-600 focus:border-blue-500 outline-none"
        />
      </div>
    </div>

    <div>
      <label class="text-gray-400 text-sm block mb-1">Animacja Wejścia</label>
      <select
        :model-value="text.entryAnimation"
        @change="emit('update', { ...text, entryAnimation: ($event.target as HTMLSelectElement).value })"
        class="w-full bg-gray-700 text-white px-3 py-2 rounded border border-gray-600 focus:border-blue-500 outline-none"
      >
        <option value="none">Brak</option>
        <option value="fade-in">Fade In</option>
        <option value="slide-in-left">Slide In ←</option>
        <option value="slide-in-right">Slide In →</option>
        <option value="slide-in-top">Slide In ↑</option>
        <option value="slide-in-bottom">Slide In ↓</option>
        <option value="zoom-in">Zoom In</option>
        <option value="pop-in">Pop In</option>
        <option value="typewriter">Typewriter</option>
      </select>
    </div>

    <div v-if="text.entryAnimation && text.entryAnimation !== 'none'">
      <label class="text-gray-400 text-sm block mb-1">Czas wejścia: {{ text.entryDuration }}s</label>
      <input
        :model-value="text.entryDuration"
        @input="emit('update', { ...text, entryDuration: Number(($event.target as HTMLInputElement).value) })"
        type="range"
        min="0.1"
        max="3"
        step="0.1"
        class="w-full"
      />
    </div>

    <div>
      <label class="text-gray-400 text-sm block mb-1">Animacja Ciągła</label>
      <select
        :model-value="text.loopAnimation"
        @change="emit('update', { ...text, loopAnimation: ($event.target as HTMLSelectElement).value })"
        class="w-full bg-gray-700 text-white px-3 py-2 rounded border border-gray-600 focus:border-blue-500 outline-none"
      >
        <option value="none">Brak</option>
        <option value="pulse">Pulse (Pulsowanie)</option>
        <option value="float">Float (Unoszenie)</option>
        <option value="shake">Shake (Trzęsienie)</option>
      </select>
    </div>

    <div>
      <label class="text-gray-400 text-sm block mb-1">Animacja Wyjścia</label>
      <select
        :model-value="text.exitAnimation"
        @change="emit('update', { ...text, exitAnimation: ($event.target as HTMLSelectElement).value })"
        class="w-full bg-gray-700 text-white px-3 py-2 rounded border border-gray-600 focus:border-blue-500 outline-none"
      >
        <option value="none">Brak</option>
        <option value="fade-out">Fade Out</option>
        <option value="slide-out-left">Slide Out ←</option>
        <option value="slide-out-right">Slide Out →</option>
        <option value="slide-out-top">Slide Out ↑</option>
        <option value="slide-out-bottom">Slide Out ↓</option>
        <option value="zoom-out">Zoom Out</option>
        <option value="pop-out">Pop Out</option>
      </select>
    </div>

    <div v-if="text.exitAnimation && text.exitAnimation !== 'none'">
      <label class="text-gray-400 text-sm block mb-1">Czas wyjścia: {{ text.exitDuration }}s</label>
      <input
        :model-value="text.exitDuration"
        @input="emit('update', { ...text, exitDuration: Number(($event.target as HTMLInputElement).value) })"
        type="range"
        min="0.1"
        max="3"
        step="0.1"
        class="w-full"
      />
    </div>

    <button
      @click="emit('delete')"
      class="w-full bg-red-600 hover:bg-red-700 text-white py-2 rounded-lg transition-colors"
    >
      Usuń Tekst
    </button>
  </div>
</template>

<script setup lang="ts">
import type { TextOverlay } from '@/types/video-editor.types';

interface Props {
  text: TextOverlay | null;
}

defineProps<Props>();

const emit = defineEmits<{
  update: [text: TextOverlay];
  delete: [];
}>();
</script>
