<script setup lang="ts">
import { ref, computed } from 'vue';

// --- IMPORT IKON ---
import CloseIcon from 'vue-material-design-icons/Close.vue';
import FacebookIcon from 'vue-material-design-icons/Facebook.vue';
import CogIcon from 'vue-material-design-icons/Cog.vue';
import MusicNoteIcon from 'vue-material-design-icons/MusicNote.vue';
import ChevronDownIcon from 'vue-material-design-icons/ChevronDown.vue';
import EmoticonOutlineIcon from 'vue-material-design-icons/EmoticonOutline.vue';
import FormatSizeIcon from 'vue-material-design-icons/FormatSize.vue';

// --- EMIT ---
const emit = defineEmits<{
  (e: 'back'): void;
}>();

// --- TYPY DANYCH ---

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

// --- KONFIGURACJA DANYCH ---

// Lista wszystkich teł (gradienty + solid)
const allBackgrounds: BackgroundOption[] = [
  // Podstawowe
  { id: 1, name: 'Błękitny Ocean', class: 'bg-gradient-to-b from-blue-500 to-blue-700' },
  { id: 2, name: 'Zachód Słońca', class: 'bg-gradient-to-tr from-pink-500 via-red-500 to-yellow-500' },
  { id: 3, name: 'Nocne Niebo', class: 'bg-gradient-to-br from-purple-900 via-indigo-800 to-blue-900' },
  { id: 4, name: 'Czysta Czerwień', class: 'bg-red-500' },
  { id: 5, name: 'Neonowa Zieleń', class: 'bg-gradient-to-r from-green-400 to-teal-500' },
  { id: 6, name: 'Węgiel', class: 'bg-gray-800' },
  // Nowe gradienty (widoczne po rozwinięciu lub w pierwszej partii)
  { id: 7, name: 'Cotton Candy', class: 'bg-gradient-to-r from-indigo-300 via-purple-300 to-pink-300' },
  { id: 8, name: 'Magma', class: 'bg-gradient-to-t from-gray-900 via-red-900 to-orange-800' },
  { id: 9, name: 'Cyberpunk', class: 'bg-gradient-to-r from-pink-500 via-red-500 to-yellow-500' },
  { id: 10, name: 'Limonka', class: 'bg-gradient-to-bl from-lime-400 to-green-600' },
  { id: 11, name: 'Zorza Polarna', class: 'bg-gradient-to-t from-teal-200 to-lime-200' },
  // Ukryte domyślnie (Extra)
  { id: 12, name: 'Disco', class: 'bg-[conic-gradient(at_top,_var(--tw-gradient-stops))] from-yellow-200 via-emerald-200 to-yellow-200' },
  { id: 13, name: 'Głębia', class: 'bg-gradient-to-b from-gray-900 via-purple-900 to-violet-600' },
  { id: 14, name: 'Brzoskwinia', class: 'bg-gradient-to-r from-red-200 via-red-300 to-yellow-200' },
  { id: 15, name: 'Stalowy', class: 'bg-gradient-to-r from-slate-500 to-slate-800' },
  { id: 16, name: 'Lawenda', class: 'bg-gradient-to-tr from-violet-500 to-orange-300' },
  { id: 17, name: 'Instagram', class: 'bg-gradient-to-bl from-indigo-900 via-indigo-400 to-indigo-900' },
  { id: 18, name: 'Horyzont', class: 'bg-gradient-to-t from-orange-400 to-sky-400' },
];

const fontStyles: FontStyle[] = [
  {
    id: 'modern',
    label: 'Nowoczesny',
    class: "font-['Poppins'] font-black uppercase tracking-widest drop-shadow-lg italic"
  },
  {
    id: 'hand',
    label: 'Odręczny',
    class: "font-['Caveat'] font-bold tracking-wide drop-shadow-md"
  },
  {
    id: 'typewriter',
    label: 'Maszyna',
    class: "font-['VT323'] tracking-widest drop-shadow-sm"
  },
  {
    id: 'serif',
    label: 'Elegant',
    class: "font-['Playfair_Display'] font-bold italic tracking-normal drop-shadow-md"
  },
  {
    id: 'simple',
    label: 'Prosty',
    class: "font-sans font-semibold drop-shadow-sm"
  },
];

// --- STAN APLIKACJI ---

const textContent = ref('😎 DDD');
const selectedBackgroundId = ref<number>(1);
const selectedFont = ref<FontStyle>(fontStyles[0]!);

const isFontMenuOpen = ref(false);
const isBackgroundsExpanded = ref(false);

// --- COMPUTED ---

const currentBackground = computed(() => {
  return allBackgrounds.find(b => b.id === selectedBackgroundId.value);
});

const visibleBackgrounds = computed(() => {
  if (isBackgroundsExpanded.value) {
    return allBackgrounds;
  }
  return allBackgrounds.slice(0, 11);
});

// --- FUNKCJE ---

const setBackground = (id: number) => {
  selectedBackgroundId.value = id;
};

const toggleFontMenu = () => {
  isFontMenuOpen.value = !isFontMenuOpen.value;
};

const selectFont = (font: FontStyle) => {
  selectedFont.value = font;
  isFontMenuOpen.value = false;
};

const toggleBackgrounds = () => {
  isBackgroundsExpanded.value = !isBackgroundsExpanded.value;
};

const handleBack = () => {
  emit('back');
};
</script>

<template>
  <div class="flex h-screen w-full bg-[#F0F2F5] font-sans overflow-hidden select-none">

    <aside class="w-[360px] bg-white h-full flex flex-col border-r border-gray-300 shadow-sm z-20">

      <div class="p-4 flex items-center gap-3 border-b border-gray-200 shadow-sm">
        <button 
          @click="handleBack"
          class="p-2 bg-gray-200 rounded-full hover:bg-gray-300 transition text-gray-600"
        >
          <CloseIcon :size="20" />
        </button>
        <div class="bg-blue-600 rounded-full p-1 h-8 w-8 flex items-center justify-center">
            <FacebookIcon :size="24" class="text-white" />
        </div>
      </div>

      <div class="flex-1 overflow-y-auto p-4 custom-scrollbar">
        <div class="flex justify-between items-center mb-6">
          <h1 class="text-2xl font-bold text-gray-900">Twoja relacja</h1>
          <button class="p-2 bg-gray-100 rounded-full hover:bg-gray-200 text-gray-700">
            <CogIcon :size="20" />
          </button>
        </div>

        <div class="flex items-center gap-3 mb-6">
          <img
            src="https://i.pravatar.cc/150?img=11"
            alt="Avatar"
            class="w-12 h-12 rounded-full border border-gray-200"
          />
          <span class="font-semibold text-gray-900">Bartosz Miazek</span>
        </div>

        <div class="relative mb-6">
          <div
            @click="toggleFontMenu"
            class="border border-gray-300 rounded-md p-3 flex justify-between items-center cursor-pointer hover:bg-gray-50 transition"
          >
            <div class="flex items-center gap-2 text-gray-700">
               <FormatSizeIcon :size="20" />
               <div class="flex flex-col items-start leading-tight">
                 <span class="text-xs text-gray-400 font-semibold uppercase">Styl tekstu</span>
                 <span class="text-sm font-medium">{{ selectedFont.label }}</span>
               </div>
            </div>
            <ChevronDownIcon :size="20" class="text-gray-500 transition-transform" :class="{'rotate-180': isFontMenuOpen}" />
          </div>

          <div v-if="isFontMenuOpen" class="absolute top-full left-0 w-full mt-1 bg-white border border-gray-200 rounded-md shadow-lg z-30 overflow-hidden">
            <ul>
              <li
                v-for="font in fontStyles"
                :key="font.id"
                @click="selectFont(font)"
                class="px-4 py-3 hover:bg-blue-50 cursor-pointer flex items-center justify-between group border-b border-gray-100 last:border-0"
              >
                <span :class="font.class" class="text-xl text-gray-800 scale-90 origin-left">{{ font.label }}</span>
                <span v-if="selectedFont.id === font.id" class="text-blue-600 font-bold">✓</span>
              </li>
            </ul>
          </div>
        </div>

        <div class="border border-gray-200 rounded-lg p-4 mb-4 shadow-sm bg-white transition-all duration-300">
          <div class="flex justify-between items-end mb-3">
             <p class="text-xs text-gray-500 font-bold uppercase tracking-wider">Tła</p>
             <span class="text-xs font-medium text-blue-600 animate-fade-in truncate max-w-[150px] text-right">
               {{ currentBackground?.name }}
             </span>
          </div>

          <div class="flex flex-wrap gap-2.5">
            <div
                v-for="bg in visibleBackgrounds" :key="bg.id"
                class="rounded-full p-0.5 border-2 transition-all duration-200"
                :class="selectedBackgroundId === bg.id ? 'border-blue-500 scale-110' : 'border-transparent'"
            >
                 <button
                  @click="setBackground(bg.id)"
                  :class="[
                    'w-9 h-9 rounded-full cursor-pointer shadow-sm border border-black/10 hover:opacity-80 transition',
                    bg.class
                  ]"
                  :title="bg.name"
                ></button>
            </div>

            <button
                @click="toggleBackgrounds"
                class="w-10 h-10 flex items-center justify-center bg-gray-100 rounded-full hover:bg-gray-200 transition text-gray-600 ml-1"
                :title="isBackgroundsExpanded ? 'Zwiń' : 'Pokaż więcej'"
            >
                <ChevronDownIcon
                    :size="20"
                    class="transition-transform duration-300"
                    :class="{'rotate-180': isBackgroundsExpanded}"
                />
            </button>
          </div>
        </div>

        <button class="w-full flex items-center gap-3 p-3 font-semibold text-gray-700 hover:bg-gray-100 rounded-md transition">
          <MusicNoteIcon :size="24" class="text-gray-500" />
          Dodaj muzykę
        </button>
      </div>

      <div class="p-4 border-t border-gray-200 flex gap-3 bg-white">
        <button 
          @click="handleBack"
          class="flex-1 py-2 px-4 bg-gray-200 hover:bg-gray-300 text-gray-700 font-semibold rounded-md transition"
        >
          Odrzuć
        </button>
        <button class="flex-1 py-2 px-4 bg-blue-600 hover:bg-blue-700 text-white font-semibold rounded-md transition shadow-md">
          Udostępnij
        </button>
      </div>
    </aside>

    <main class="flex-1 flex flex-col h-full relative z-0">
      <div class="m-4 bg-white rounded-lg shadow-sm p-4 h-full flex flex-col">
          <h2 class="text-sm font-semibold text-gray-900 mb-4">Podgląd</h2>

          <div class="bg-[#18191A] flex-1 rounded-lg flex items-center justify-center overflow-hidden relative border border-gray-800">

            <div
              class="relative w-[340px] h-[600px] rounded-xl shadow-2xl overflow-hidden flex flex-col items-center justify-center transition-all duration-500 ring-8 ring-black"
              :class="currentBackground?.class"
            >

              <div class="z-10 w-full px-4 grid place-items-center">

                 <div
                    class="col-start-1 row-start-1 invisible whitespace-pre-wrap wrap-break-word text-center pointer-events-none px-2 py-0 border border-transparent"
                    :class="[selectedFont.class, 'text-4xl leading-normal']"
                    aria-hidden="true"
                 >
                    {{ textContent + ' ' }}
                 </div>

                 <textarea
                    v-model="textContent"
                    class="col-start-1 row-start-1 w-full h-full resize-none overflow-hidden bg-transparent text-white text-center outline-none px-2 py-0 placeholder-white/50"
                    :class="[selectedFont.class, 'text-4xl leading-normal']"
                    placeholder="Pisz tutaj..."
                    spellcheck="false"
                 ></textarea>

              </div>

               <div class="absolute bottom-6 right-4 text-white opacity-80 cursor-pointer hover:opacity-100 hover:scale-110 transition drop-shadow-md">
                  <EmoticonOutlineIcon :size="32" />
               </div>

            </div>

          </div>
      </div>
    </main>

  </div>
</template>

<style>
/* Import czcionek z Google Fonts */
@import url('https://fonts.googleapis.com/css2?family=Caveat:wght@700&family=Playfair+Display:ital,wght@1,700&family=Poppins:ital,wght@0,900;1,900&family=VT323&display=swap');

.custom-scrollbar::-webkit-scrollbar {
  width: 6px;
}
.custom-scrollbar::-webkit-scrollbar-track {
  background: transparent;
}
.custom-scrollbar::-webkit-scrollbar-thumb {
  background-color: #d1d5db;
  border-radius: 20px;
}
.custom-scrollbar::-webkit-scrollbar-thumb:hover {
  background-color: #9ca3af;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(2px); }
  to { opacity: 1; transform: translateY(0); }
}
.animate-fade-in {
  animation: fadeIn 0.3s ease-out forwards;
}
</style>
