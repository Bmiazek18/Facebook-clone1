<script setup lang="ts">
import { ref, computed } from 'vue';

// --- IMPORT KOMPONENTÓW ---
import Sidebar from '@/components/Sidebar.vue';
import EmoticonOutlineIcon from 'vue-material-design-icons/EmoticonOutline.vue';

// --- EMIT ---
const emit = defineEmits<{
  (e: 'back'): void;
}>();

// --- TYPY DANYCH ---

interface FontStyle {
  id: string;
  label: string;
  class: string;
}

// --- KONFIGURACJA DANYCH ---

// Lista wszystkich teł (gradienty + solid)
const allBackgrounds = [
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

// --- STAN APLIKACJI ---

const textContent = ref('😎 DDD');
const selectedBackgroundId = ref<number>(1);
const selectedFont = ref<FontStyle>(fontStyles[0]!);
const isMusicModalOpen = ref(false);

// --- COMPUTED ---

const currentBackground = computed(() => {
  return allBackgrounds.find(b => b.id === selectedBackgroundId.value);
});

// --- FUNKCJE ---

const handleBack = () => {
  emit('back');
};

const handleSelectBackground = (id: number) => {
  selectedBackgroundId.value = id;
};

const handleSelectFont = (font: FontStyle) => {
  selectedFont.value = font;
};

const toggleMusicModal = () => {
  isMusicModalOpen.value = !isMusicModalOpen.value;
};
</script>

<template>
  <div class="flex h-screen w-full bg-[#F0F2F5] font-sans overflow-hidden select-none">

    <Sidebar
      mode="text"
      :is-music-modal-open="isMusicModalOpen"
      :is-image-selected="false"
      :selected-background-id="selectedBackgroundId"
      :selected-font-id="selectedFont.id"
      @back="handleBack"
      @toggle-music="toggleMusicModal"
      @select-background="handleSelectBackground"
      @select-font="handleSelectFont"
    />

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
</style>
