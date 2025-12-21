<template>
    <div class="flex flex-col mt-2 pb-4 px-4">
        <!-- Font Style Selector -->
        <div class="relative mb-4">
            <div @click="toggleFontMenu" class="border border-gray-300 rounded-md p-3 flex justify-between items-center cursor-pointer hover:bg-gray-50 transition">
                <div class="flex items-center gap-2 text-gray-700">
                    <FormatSize :size="20" />
                    <div class="flex flex-col items-start leading-tight">
                        <span class="text-xs text-gray-400 font-semibold uppercase">Styl tekstu</span>
                        <span class="text-sm font-medium">{{ currentFont?.label }}</span>
                    </div>
                </div>
                <ChevronDown :size="20" class="text-gray-500 transition-transform" :class="{'rotate-180': isFontMenuOpen}" />
            </div>

            <div v-if="isFontMenuOpen" class="absolute top-full left-0 w-full mt-1 bg-white border border-gray-200 rounded-md shadow-lg z-30 overflow-hidden">
                <ul>
                    <li v-for="font in fontStyles" :key="font.id" @click="selectFont(font)" class="px-4 py-3 hover:bg-blue-50 cursor-pointer flex items-center justify-between group border-b border-gray-100 last:border-0">
                        <span :class="font.class" class="text-xl text-gray-800 scale-90 origin-left">{{ font.label }}</span>
                        <span v-if="currentFont?.id === font.id" class="text-blue-600 font-bold">✓</span>
                    </li>
                </ul>
            </div>
        </div>

        <!-- Background Selector -->
        <div class="border border-gray-200 rounded-lg p-4 mb-4 shadow-sm bg-white">
            <div class="flex justify-between items-end mb-3">
                <p class="text-xs text-gray-500 font-bold uppercase tracking-wider">Tła</p>
                <span class="text-xs font-medium text-blue-600 truncate max-w-[150px] text-right">
                {{ currentBackground?.name }}
                </span>
            </div>

            <div class="flex flex-wrap gap-2.5">
                <div v-for="bg in visibleBackgrounds" :key="bg.id" class="rounded-full p-0.5 border-2 transition-all duration-200" :class="selectedBackgroundId === bg.id ? 'border-blue-500 scale-110' : 'border-transparent'">
                    <button @click="selectBackground(bg.id)" :class="['w-9 h-9 rounded-full cursor-pointer shadow-sm border border-black/10 hover:opacity-80 transition', bg.class]" :title="bg.name"></button>
                </div>

                <button @click="toggleBackgrounds" class="w-10 h-10 flex items-center justify-center bg-gray-100 rounded-full hover:bg-gray-200 transition text-gray-600 ml-1" :title="isBackgroundsExpanded ? 'Zwiń' : 'Pokaż więcej'">
                    <ChevronDown :size="20" class="transition-transform duration-300" :class="{'rotate-180': isBackgroundsExpanded}" />
                </button>
            </div>
        </div>

        <!-- Music button for text mode -->
        <div @click="emit('toggle-music')" class="flex items-center gap-4 py-3 hover:bg-gray-100 cursor-pointer transition rounded-md" :class="{'bg-blue-50 border-l-4 border-blue-500': isMusicModalOpen}">
            <div class="bg-gray-200 p-2.5 rounded-full border border-gray-300">
                <MusicNote :size="24" class="text-gray-700" />
            </div>
            <span class="font-medium text-gray-700 text-sm">Dodaj muzykę</span>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import MusicNote from 'vue-material-design-icons/MusicNote.vue';
import ChevronDown from 'vue-material-design-icons/ChevronDown.vue';
import FormatSize from 'vue-material-design-icons/FormatSize.vue';

interface BackgroundOption {
  id: number;
  name: string;
  class: string;
}

interface FontStyle {
  id: string;
  label: string;
  class: string;
}

const props = defineProps<{
  isMusicModalOpen: boolean;
  selectedBackgroundId?: number;
  selectedFontId?: string;
}>();

const emit = defineEmits<{
  (e: 'toggle-music'): void;
  (e: 'select-background', id: number): void;
  (e: 'select-font', font: FontStyle): void;
}>();

const allBackgrounds: BackgroundOption[] = [
  { id: 1, name: 'Błękitny Ocean', class: 'bg-gradient-to-b from-blue-500 to-blue-700' },
  { id: 2, name: 'Zachód Słońca', class: 'bg-gradient-to-tr from-pink-500 via-red-500 to-yellow-500' },
  { id: 3, name: 'Nocne Niebo', class: 'bg-gradient-to-br from-purple-900 via-indigo-800 to-blue-900' },
  { id: 4, name: 'Czysta Czerwień', class: 'bg-red-500' },
  { id: 5, name: 'Neonowa Zieleń', class: 'bg-gradient-to-r from-green-400 to-teal-500' },
  { id: 6, name: 'Węgiel', class: 'bg-gray-800' },
  { id: 7, name: 'Cotton Candy', class: 'bg-gradient-to-r from-indigo-300 via-purple-300 to-pink-300' },
  { id: 8, name: 'Magma', class: 'bg-gradient-to-t from-gray-900 via-red-900 to-orange-800' },
  { id: 9, name: 'Cyberpunk', class: 'bg-gradient-to-r from-pink-500 via-red-500 to-yellow-500' },
  { id: 10, name: 'Limonka', class: 'bg-gradient-to-bl from-lime-400 to-green-600' },
  { id: 11, name: 'Zorza Polarna', class: 'bg-gradient-to-t from-teal-200 to-lime-200' },
  { id: 12, name: 'Disco', class: 'bg-[conic-gradient(at_top,_var(--tw-gradient-stops))] from-yellow-200 via-emerald-200 to-yellow-200' },
  { id: 13, name: 'Głębia', class: 'bg-gradient-to-b from-gray-900 via-purple-900 to-violet-600' },
  { id: 14, name: 'Brzoskwinia', class: 'bg-gradient-to-r from-red-200 via-red-300 to-yellow-200' },
  { id: 15, name: 'Stalowy', class: 'bg-gradient-to-r from-slate-500 to-slate-800' },
  { id: 16, name: 'Lawenda', class: 'bg-gradient-to-tr from-violet-500 to-orange-300' },
];

const fontStyles: FontStyle[] = [
  { id: 'modern', label: 'Nowoczesny', class: "font-['Poppins'] font-black uppercase tracking-widest drop-shadow-lg italic" },
  { id: 'hand', label: 'Odręczny', class: "font-['Caveat'] font-bold tracking-wide drop-shadow-md" },
  { id: 'typewriter', label: 'Maszyna', class: "font-['VT323'] tracking-widest drop-shadow-sm" },
  { id: 'serif', label: 'Elegant', class: "font-['Playfair_Display'] font-bold italic tracking-normal drop-shadow-md" },
  { id: 'simple', label: 'Prosty', class: "font-sans font-semibold drop-shadow-sm" },
];

const isBackgroundsExpanded = ref(false);
const isFontMenuOpen = ref(false);

const visibleBackgrounds = computed(() => {
  return isBackgroundsExpanded.value ? allBackgrounds : allBackgrounds.slice(0, 11);
});

const currentBackground = computed(() => {
  return allBackgrounds.find(b => b.id === props.selectedBackgroundId) || allBackgrounds[0];
});

const currentFont = computed(() => {
  return fontStyles.find(f => f.id === props.selectedFontId) || fontStyles[0];
});

const toggleBackgrounds = () => {
  isBackgroundsExpanded.value = !isBackgroundsExpanded.value;
};

const toggleFontMenu = () => {
  isFontMenuOpen.value = !isFontMenuOpen.value;
};

const selectFont = (font: FontStyle) => {
  emit('select-font', font);
  isFontMenuOpen.value = false;
};

const selectBackground = (id: number) => {
  emit('select-background', id);
};
</script>
