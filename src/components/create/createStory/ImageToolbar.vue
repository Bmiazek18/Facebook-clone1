<script setup lang="ts">
import { computed } from 'vue';
import RotateRight from 'vue-material-design-icons/RotateRight.vue';
import Plus from 'vue-material-design-icons/Plus.vue';
import Minus from 'vue-material-design-icons/Minus.vue';

const props = defineProps<{
  scale: number;
}>();

const emit = defineEmits<{
  (e: 'update:scale', value: number): void;
  (e: 'rotate'): void;
}>();

// Obliczanie tła suwaka (niebieski pasek postępu)
const sliderBackground = computed(() => {
  const min = 0.5;
  const max = 3;
  const percentage = ((props.scale - min) / (max - min)) * 100;
  // #2563eb to blue-600, #4b5563 to gray-600
  return {
    background: `linear-gradient(to right, #2563eb 0%, #2563eb ${percentage}%, #9ca3af ${percentage}%, #9ca3af 100%)`
  };
});

const updateScale = (e: Event) => {
    const target = e.target as HTMLInputElement;
    emit('update:scale', parseFloat(target.value));
};
</script>

<template>
  <div class="flex">
<div class=" flex items-center gap-4 z-[200] w-[650px]">

       <div class="flex items-center gap-3 px-4 py-3 flex-1 ">
           <Minus
              :size="24"
              class="text-white cursor-pointer hover:text-gray-300 transition shrink-0"
              @click="emit('update:scale', Math.max(0.5, scale - 0.1))"
           />

           <input
                type="range"
                min="0.5"
                max="3"
                step="0.05"
                :value="scale"
                @input="updateScale"
                :style="sliderBackground"
                class="w-full h-1.5 rounded-lg appearance-none cursor-pointer"
           />

           <Plus
              :size="24"
              class="text-white cursor-pointer hover:text-gray-300 transition shrink-0"
              @click="emit('update:scale', Math.min(3, scale + 0.1))"
           />
       </div>

       <button
            @click="emit('rotate')"
            class="flex items-center gap-2 bg-gray-200 text-black hover:bg-gray-300 px-5 py-3 rounded-xl transition font-semibold text-sm shadow-md shrink-0"
        >
           <RotateRight :size="20" />
           Obróć
       </button>
  </div>
  </div>

</template>

<style scoped>
/* Reset podstawowego wyglądu slidera */
input[type=range] {
  -webkit-appearance: none;
  /* Tło jest obsługiwane przez styl inline w template (linear-gradient) */
}

/* Stylizacja "kciuka" (kółka) dla Webkit (Chrome/Safari) */
input[type=range]::-webkit-slider-thumb {
  -webkit-appearance: none;
  height: 20px;
  width: 20px;
  border-radius: 50%;
  background: #2563eb; /* blue-600 */
  cursor: pointer;
  box-shadow: 0 0 0 2px rgba(0, 0, 0, 0.3); /* Delikatny cień/obrys */
  transition: transform 0.1s ease;
}

/* Hover na kciuk */
input[type=range]::-webkit-slider-thumb:hover {
  transform: scale(1.1);
}

/* Stylizacja "kciuka" dla Firefoxa */
input[type=range]::-moz-range-thumb {
  height: 20px;
  width: 20px;
  border: none;
  border-radius: 50%;
  background: #2563eb;
  cursor: pointer;
  box-shadow: 0 0 0 2px rgba(0, 0, 0, 0.3);
}

</style>
