<script setup lang="ts">
import RotateRight from 'vue-material-design-icons/RotateRight.vue';
import Plus from 'vue-material-design-icons/Plus.vue';
import Minus from 'vue-material-design-icons/Minus.vue';

defineProps<{
  scale: number;
}>();

const emit = defineEmits<{
  (e: 'update:scale', value: number): void;
  (e: 'rotate'): void;
}>();
</script>

<template>
  <div class="absolute bottom-8 left-1/2 -translate-x-1/2 bg-black/80 backdrop-blur-md px-6 py-3 rounded-full shadow-2xl flex items-center gap-6 z-[200] border border-white/10 animate-fade-in">
       <div class="flex items-center gap-3">
           <Minus
              :size="20"
              class="text-gray-300 cursor-pointer hover:text-white transition"
              @click="emit('update:scale', Math.max(0.5, scale - 0.1))"
           />

           <input
                type="range"
                min="0.5"
                max="3"
                step="0.05"
                :value="scale"
                @input="emit('update:scale', +($event.target as HTMLInputElement).value)"
                class="w-48 h-1.5 bg-gray-600/50 rounded-lg appearance-none cursor-pointer accent-blue-500"
           />

           <Plus
              :size="20"
              class="text-gray-300 cursor-pointer hover:text-white transition"
              @click="emit('update:scale', Math.min(3, scale + 0.1))"
           />
       </div>

       <div class="w-px h-6 bg-gray-600/50"></div>

       <button @click="emit('rotate')" class="flex items-center gap-2 text-white bg-white/10 hover:bg-white/20 px-4 py-2 rounded-full transition font-medium text-sm">
           <RotateRight :size="18" />
           Obróć
       </button>
  </div>
</template>

<style scoped>
/* Style dla input range przeniesione tutaj */
input[type=range] { -webkit-appearance: none; background: transparent; }
input[type=range]::-webkit-slider-thumb { -webkit-appearance: none; height: 18px; width: 18px; border-radius: 50%; background: #3b82f6; margin-top: -6px; cursor: pointer; box-shadow: 0 0 0 3px rgba(255, 255, 255, 0.2); transition: transform 0.1s; }
input[type=range]::-webkit-slider-thumb:hover { transform: scale(1.1); }
input[type=range]::-webkit-slider-runnable-track { width: 100%; height: 6px; cursor: pointer; background: rgba(255, 255, 255, 0.2); border-radius: 3px; }

/* Animacja Fade In */
@keyframes fadeIn { from { opacity: 0; transform: translate(-50%, 20px); } to { opacity: 1; transform: translate(-50%, 0); } }
.animate-fade-in { animation: fadeIn 0.3s cubic-bezier(0.16, 1, 0.3, 1) forwards; }
</style>
