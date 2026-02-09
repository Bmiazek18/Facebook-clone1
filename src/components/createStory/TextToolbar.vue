<script setup lang="ts">
import { ref } from 'vue';
import Close from 'vue-material-design-icons/Close.vue';

const props = defineProps<{
  currentColor: string;
  elementId: string;
}>();

const emit = defineEmits<{
  (e: 'update:color', payload: { id: string, color: string }): void;
  (e: 'close'): void; // Add close event for consistency with MusicToolbar
}>();

// Paleta kolorów odwzorowana ze zrzutu ekranu
const colors = ref([
  '#000000', '#2563EB', '#581c0c', '#ff5722', '#22d3ee', '#eab308', '#6b7280', // Rząd 1
  '#84cc16', '#bae6fd', '#cbd5e1', '#e9d5ff', '#ec4899', '#86efac', '#1e3a8a', // Rząd 2
  '#f97316', '#fbcfe8', '#9333ea', '#ef4444', '#4c1d95', '#FFFFFF', '#fde047'  // Rząd 3
]);

const selectColor = (newColor: string) => {
  props.currentColor =  newColor
};
</script>

<template>
  <div class="absolute -right-[115px] top-15 mt-20 bg-white rounded-2xl shadow-2xl p-4 w-[320px] z-900 animate-pop">
    <div class="border border-gray-200 rounded-xl p-3">

      <div class="grid grid-cols-7 gap-3">
        <button
          v-for="c in colors"
          :key="c"
          type="button"
          @click="selectColor(c)"
          class="w-8 h-8 rounded-full cursor-pointer transition-transform hover:scale-110 focus:outline-none relative"
          :class="[
            // Jeśli kolor jest wybrany: niebieski pierścień z odstępem (offset)
            currentColor=== c ? 'ring-2 ring-blue-600 ring-offset-2' : 'border border-black/10 hover:border-black/20',
          ]"
          :style="{ backgroundColor: c }"
          :aria-label="`Wybierz kolor ${c}`"
        >
        </button>
      </div>

    </div>
  </div>
</template>


