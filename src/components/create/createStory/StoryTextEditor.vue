<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue';
import { useStoryExport } from '@/composables/useStoryExport';
import type { StoryElement as StoryElementType, TextElement } from '@/types/StoryElement';
import { Dropdown as VDropdown } from 'floating-vue';
import { useContentEditable } from '@/composables/useContentEditable';


// --- IMPORT KOMPONENTÓW ---
import StorySidebar from './StorySidebar/StorySidebar.vue';
import EmoticonOutlineIcon from 'vue-material-design-icons/EmoticonOutline.vue';
import { useAuthStore } from '@/stores/auth';
import { useRouter } from 'vue-router';
import { useStoriesStore } from '@/stores/stories';

// --- EMIT ---
const emit = defineEmits<{
  (e: 'back'): void;
  (e: 'export-story'): void;
  (e: 'export', dataUrl: string): void;
}>();

// --- TYPY DANYCH ---

interface FontStyle {
  id: string;
  label: string;
  class: string;
}


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

const storyContainerRef = ref<HTMLElement | null>(null);
const contentEditableDivRef = ref<HTMLDivElement | null>(null);

const textContent = ref('Pisz tutaj...');
const selectedBackgroundId = ref<number>(1);
const selectedFont = ref<FontStyle>(fontStyles[0]!);
const isMusicModalOpen = ref(false);

const {
    onContentInput: useContentEditableOnContentInput,
    matchingUsers,
    showUserDropdown,
    selectUser,
    renderContentEditable,
    addEmoji,
    moveCursorToEnd
} = useContentEditable(contentEditableDivRef, textContent);


const storyElements = computed((): StoryElementType[] => [
    {
        id: 'el_text_main',
        type: 'text',
        content: textContent.value,
        x: 0, y: 0, // Pozycja jest względna do kontenera, więc wyśrodkowanie załatwia CSS
        width: 100, height: 100, // Szerokość/wysokość 100% kontenera
        rotation: 0, scale: 1,
        styles: {
            fontClass: selectedFont.value.class,
            color: 'white',
        }
    } as TextElement
]);

onMounted(() => {
    // Initial render of contenteditable div
    renderContentEditable();
});


// --- STORY EXPORT ---
const { isRendering, renderProgress, renderStoryToImage } = useStoryExport();
const storiesStore = useStoriesStore();
const authStore = useAuthStore();
const router = useRouter();

const exportStory = async () => {
    if (!storyContainerRef.value) return;

    try {
       const currentUser = authStore.currentUser;
    if (!currentUser) {
      alert('Musisz być zalogowany aby dodać story');
      return;
    }
        const dataUrl = await renderStoryToImage(storyContainerRef.value, storyElements.value, false);
         storiesStore.addStory(
      currentUser.id.toString(),
      {
        type: 'text',
        imageUrl: dataUrl, // Używamy wyrenderowanego obrazu

        elements: storyElements.value,

      }
    );
    router.push('/')
    } catch (error) {
        console.error('Failed to export story:', error);
        alert('Błąd podczas eksportowania story');
    }
};

// --- COMPUTED ---

const currentBackground = computed(() => {
  return allBackgrounds.find(b => b.id === selectedBackgroundId.value);
});

// --- FUNKCJE ---

const onContentInput = () => {
    // Use the onContentInput from useContentEditable
    useContentEditableOnContentInput();
};

const onKeydown = (e: KeyboardEvent) => {
  // If useContentEditable had a keydown handler to expose, we'd call it here.
  // For now, we can leave it empty or emit if the parent needed to know.
};

const onFocus = (e: FocusEvent) => {
  // If useContentEditable had a focus handler to expose, we'd call it here.
};

const onBlur = (e: FocusEvent) => {
  // If useContentEditable had a blur handler to expose, we'd call it here.
};

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

defineExpose({
  exportStory,
  isRendering,
  renderProgress,
  // expose previous data for compatibility if needed, though the new flow should use the exported image
  textContent,
  currentBackground,
  selectedFont,
  addEmoji, // Expose addEmoji
  moveCursorToEnd, // Expose moveCursorToEnd
});

</script>

<template>
  <div class="flex h-screen w-full bg-theme-bg font-sans overflow-hidden select-none relative">
    <!-- Rendering Modal -->
    <div v-if="isRendering" class="absolute inset-0 bg-black/90 flex items-center justify-center z-50">
      <div class="bg-theme-bg-secondary rounded-lg p-8 max-w-md w-full mx-4 text-center">
        <h3 class="text-theme-text text-xl font-bold mb-4">Renderowanie...</h3>
        <div class="mb-4">
          <div class="bg-gray-700 rounded-full h-4 overflow-hidden">
            <div
              class="bg-blue-600 h-full transition-all duration-300"
              :style="{ width: renderProgress + '%' }"
            ></div>
          </div>
          <div class="text-theme-text text-center mt-2 font-bold">{{ renderProgress }}%</div>
        </div>
        <p class="text-theme-text-secondary text-sm">Tworzenie obrazu story...</p>
      </div>
    </div>

    <StorySidebar
      mode="text"
      :is-music-modal-open="isMusicModalOpen"
      :is-image-selected="false"
      :selected-background-id="selectedBackgroundId"
      :selected-font-id="selectedFont.id"
      @back="handleBack"
      @toggle-music="toggleMusicModal"
      @select-background="handleSelectBackground"
      @select-font="handleSelectFont"
      @export-story="exportStory"
    />

    <main class="flex-1 flex flex-col h-full relative z-0">
      <div class="m-4 bg-theme-bg-secondary rounded-lg shadow-sm p-4 h-full flex flex-col">
        <h2 class="text-sm font-semibold text-theme-text mb-4">Podgląd</h2>

        <div class="bg-theme-bg rounded-lg flex-1 flex items-center justify-center overflow-hidden relative border border-theme-border">
          <div
            ref="storyContainerRef"
            class="relative aspect-9/16 h-[calc(100%-68px)] shadow-2xl rounded-md border border-theme-border flex flex-col items-center justify-center transition-all duration-500 overflow-hidden"
            :class="currentBackground?.class"
          >
            <div class="z-10 w-full px-4 grid place-items-center">
              <VDropdown
                :shown="showUserDropdown"
                placement="bottom-start"
                :triggers="[]"
                :auto-hide="true"
                class="w-full"
                popper-class="v-popper--theme-menu"
              >
                <div
                  ref="contentEditableDivRef"
                  contenteditable="true"
                  class="w-full bg-transparent text-white text-center outline-none px-2 py-0 placeholder-white/50"
                  :class="[selectedFont.class, 'text-4xl leading-normal']"
                  placeholder="Pisz tutaj..."
                  spellcheck="false"
                  @input="onContentInput"
                  @keydown="onKeydown"
                  @focus="$emit('focus', $event)"
                  @blur="$emit('blur', $event)"
                ></div>
                <template #popper>
                  <div class="user-dropdown-content w-64 max-h-60 overflow-y-auto pointer-events-auto bg-white dark:bg-gray-800 shadow-lg rounded-lg">
                    <ul>
                      <li
                        v-for="user in matchingUsers"
                        :key="user.id"
                        class="px-4 py-2 cursor-pointer hover:bg-gray-100 dark:hover:bg-gray-700 flex items-center gap-2"
                        @mousedown.prevent="selectUser(user)"
                      >
                        <img :src="user.avatar" class="w-8 h-8 rounded-full object-cover">
                        <span class="font-medium text-sm text-gray-900 dark:text-gray-100">{{ user.name }}</span>
                      </li>
                    </ul>
                  </div>
                </template>
              </VDropdown>
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

@import url('https://fonts.googleapis.com/css2?family=Caveat:wght@700&family=Playfair+Display:ital,wght@1,700&family=Poppins:ital,wght@0,900;1,900&family=VT323&display=swap');
</style>
