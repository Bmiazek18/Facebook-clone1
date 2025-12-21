<script setup lang="ts">
import { ref, watch, computed } from 'vue';
import Cog from 'vue-material-design-icons/Cog.vue';
import ArrowLeft from 'vue-material-design-icons/ArrowLeft.vue';
import TextModeOptions from './StorySidebar/TextModeOptions.vue';
import ImageModeOptions from './StorySidebar/ImageModeOptions.vue';

// --- TYPY ---
interface FontStyle {
  id: string;
  label: string;
  class: string;
}

const props = defineProps<{
  isMusicModalOpen: boolean;
  currentAltText?: string;
  isImageSelected: boolean;
  mode?: 'image' | 'text';
  // Text mode props
  selectedBackgroundId?: number;
  selectedFontId?: string;
}>();

const emit = defineEmits<{
  (e: 'add-text'): void;
  (e: 'add-image'): void;
  (e: 'toggle-music'): void;
  (e: 'add-link'): void;
  (e: 'share-post'): void;
  (e: 'back'): void;
  (e: 'save-alt-text', text: string): void;
  // Text mode emits
  (e: 'select-background', id: number): void;
  (e: 'select-font', font: FontStyle): void;
}>();
</script>

<template>
  <aside  class="w-[360px] shrink-0 bg-white shadow-xl z-20 flex flex-col justify-between h-full border-r border-gray-300 overflow-y-auto custom-scrollbar">
      <div>
        <div class="flex items-center justify-between p-4 border-b border-gray-200 bg-white sticky top-0 z-10">
          <div class="flex items-center gap-2">
            <button @click="emit('back')" class="p-2 rounded-full hover:bg-gray-100 transition">
              <ArrowLeft :size="24" class="text-gray-700" />
            </button>
            <h1 class="text-2xl font-bold text-black">{{ $t('ui.yourStory') }}</h1>
          </div>
          <div class="bg-gray-100 p-2 rounded-full cursor-pointer hover:bg-gray-200 transition">
            <Cog :size="24" class="text-black"/>
          </div>
        </div>

        <div class="flex items-center gap-3 p-4">
           <div class="w-12 h-12 bg-blue-600 rounded-full flex items-center justify-center text-white font-bold text-xl overflow-hidden">
               <img src="https://i.pravatar.cc/150?img=12" class="w-full h-full object-cover" />
           </div>
           <span class="font-semibold text-lg text-gray-900">Bartosz Mązek</span>
        </div>

        <TextModeOptions
            v-if="mode === 'text'"
            :is-music-modal-open="isMusicModalOpen"
            :selected-background-id="selectedBackgroundId"
            :selected-font-id="selectedFontId"
            @select-background="(id) => emit('select-background', id)"
            @select-font="(font) => emit('select-font', font)"
            @toggle-music="emit('toggle-music')"
        />
        <ImageModeOptions
            v-else
            :is-music-modal-open="isMusicModalOpen"
            :current-alt-text="currentAltText"
            :is-image-selected="isImageSelected"
            @add-text="emit('add-text')"
            @toggle-music="emit('toggle-music')"
            @add-link="emit('add-link')"
            @share-post="emit('share-post')"
            @save-alt-text="(text) => emit('save-alt-text', text)"
        />
      </div>


      <div class="p-4 flex gap-3 shadow-[0_-4px_10px_rgba(0,0,0,0.05)] bg-white z-30 sticky bottom-0">
         <button @click="emit('back')" class="flex-1 py-2.5 rounded-lg bg-gray-200 text-gray-800 font-semibold hover:bg-gray-300 transition text-sm">Odrzuć</button>
         <button class="flex-1 py-2.5 rounded-lg bg-blue-600 text-white font-semibold hover:bg-blue-700 transition shadow-md text-sm">Udostępnij</button>
      </div>
    </aside>
</template>

<style scoped>
.custom-scrollbar::-webkit-scrollbar { width: 5px; }
.custom-scrollbar::-webkit-scrollbar-track { background: transparent; }
.custom-scrollbar::-webkit-scrollbar-thumb { background-color: #d1d5db; border-radius: 20px; }
</style>
