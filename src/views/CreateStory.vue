<script setup lang="ts">
import { ref, reactive, onUnmounted, computed, onMounted } from 'vue';

// --- IMPORT KOMPONENTÓW ---
import MusicModal, { type MusicTrack } from '../components/MusicModal.vue';
import Sidebar from '../components/Sidebar.vue';
import ImageToolbar from '../components/ImageToolbar.vue';
import MusicToolbar from '../components/MusicToolbar.vue';

// --- IKONY ---
import Close from 'vue-material-design-icons/Close.vue';
import ResizeBottomRight from 'vue-material-design-icons/ResizeBottomRight.vue';
import VolumeHigh from 'vue-material-design-icons/VolumeHigh.vue';
import VolumeOff from 'vue-material-design-icons/VolumeOff.vue';
import MusicNote from 'vue-material-design-icons/MusicNote.vue';

// --- STAŁE ---
const CANVAS_WIDTH = 558;
const CANVAS_HEIGHT = 1000;

// --- TYPY ---
type ElementType = 'text' | 'image';

interface StoryElement {
  id: string;
  type: ElementType;
  content: string;
  x: number;
  y: number;
  width?: number;
  height?: number;
  rotation: number;
  scale: number;
  cropX?: number;
  cropY?: number;
  cropZoom?: number;
  styles?: Record<string, string>;
  altText?: string
  // Pola muzyczne
  musicTitle?: string;
  musicArtist?: string;
  musicStyle?: 'large' | 'small' | 'text' | 'icon';
}

interface BackgroundSettings {
  type: 'gradient' | 'image';
  value: string;
}

// --- STAN ---
const background = reactive<BackgroundSettings>({
  type: 'gradient',
  value: 'linear-gradient(to bottom, #3b82f6, #86efac)'
});

const storyElements = ref<StoryElement[]>([
  {
    id: 'el_init_text',
    type: 'text',
    content: 'Przesuwaj mnie!',
    x: 80, y: 100, rotation: 0, scale: 1,
    styles: { color: '#ffffff', fontSize: '24px', fontWeight: 'bold' }
  }
]);

const selectedElementId = ref<string | null>(null);

const selectedElement = computed(() => {
    return storyElements.value.find(el => el.id === selectedElementId.value);
});

// --- AUDIO PLAYER ---
const audioPlayer = new Audio();
audioPlayer.loop = true;
const isPlaying = ref(false);

const toggleBackgroundMusic = () => {
  if (audioPlayer.paused && audioPlayer.src) {
    audioPlayer.play();
    isPlaying.value = true;
  } else {
    audioPlayer.pause();
    isPlaying.value = false;
  }
};

const backgroundRef = ref<HTMLElement | null>(null);
const bgDimensions = reactive({ width: CANVAS_WIDTH, height: CANVAS_HEIGHT });

onMounted(() => {
  if (backgroundRef.value) {
    const observer = new ResizeObserver((entries) => {
      for (const entry of entries) {
        bgDimensions.width = entry.contentRect.width;
        bgDimensions.height = entry.contentRect.height;
      }
    });
    observer.observe(backgroundRef.value);
  }
});

onUnmounted(() => {
  audioPlayer.pause();
});

// --- MODAL MUZYKI ---
const isMusicModalOpen = ref(false);
const toggleMusicModal = () => isMusicModalOpen.value = !isMusicModalOpen.value;

// Dodanie muzyki
const addMusicPoster = (track: MusicTrack) => {
  const newId = `el_${Date.now()}`;
  storyElements.value.push({
    id: newId,
    type: 'image',
    content: track.coverUrlLarge,
    x: 50, y: 150,
    width: 220, height: 300, // Startowe wymiary dla 'large'
    rotation: 0, scale: 1, cropX: 0, cropY: 0, cropZoom: 1,
    styles: {},
    musicTitle: track.title,
    musicArtist: track.artist,
    musicStyle: 'large'
  });
  selectedElementId.value = newId;

  if (track.previewUrl && audioPlayer.src !== track.previewUrl) {
    audioPlayer.src = track.previewUrl;
    audioPlayer.play().then(() => { isPlaying.value = true; });
  } else if (audioPlayer.paused) {
     audioPlayer.play().then(() => { isPlaying.value = true; });
  }
  isMusicModalOpen.value = false;
};

// --- LOGIKA ZMIANY STYLU MUZYKI ---
const updateMusicStyle = (style: 'large' | 'small' | 'text' | 'icon') => {
    if (!selectedElement.value) return;
    const el = selectedElement.value;
    el.musicStyle = style;
    el.scale = 1; // Reset skali przy zmianie stylu

    // Ustawiamy domyślne wymiary kontenera dla danego stylu
    if (style === 'large') {
        el.width = 220; el.height = 300;
    } else if (style === 'small') {
        el.width = 260; el.height = 70;
    } else if (style === 'text') {
        el.width = 300; el.height = 100;
    } else if (style === 'icon') {
        el.width = 60; el.height = 60;
    }
};

// --- LOGIKA INTERAKCJI (DRAG/RESIZE/ROTATE) ---
const activeDragId = ref<string | null>(null);
const activeResizeId = ref<string | null>(null);
const activeRotateId = ref<string | null>(null);
const editingId = ref<string | null>(null);
const croppingId = ref<string | null>(null);
const dragStart = reactive({ x: 0, y: 0 });
const elementStart = reactive({ x: 0, y: 0, w: 0, h: 0, rotation: 0, cropX: 0, cropY: 0 });

const startDrag = (event: MouseEvent, element: StoryElement) => {
  selectedElementId.value = element.id;
  if (editingId.value === element.id || activeResizeId.value || activeRotateId.value) return;

  if (croppingId.value === element.id) {
     activeDragId.value = 'CROP_MOVE';
     dragStart.x = event.clientX; dragStart.y = event.clientY;
     elementStart.cropX = element.cropX || 0; elementStart.cropY = element.cropY || 0;
  } else {
     if (croppingId.value) return;
     activeDragId.value = element.id;
     dragStart.x = event.clientX; dragStart.y = event.clientY;
     elementStart.x = element.x; elementStart.y = element.y;
     const target = event.currentTarget as HTMLElement;
     elementStart.w = target.offsetWidth; elementStart.h = target.offsetHeight;
  }
  window.addEventListener('mousemove', onMouseMove); window.addEventListener('mouseup', stopInteraction);
};

const startResize = (event: MouseEvent, element: StoryElement) => {
  event.stopPropagation(); event.preventDefault();
  activeResizeId.value = element.id; selectedElementId.value = element.id;
  dragStart.x = event.clientX; elementStart.w = element.width || 200; elementStart.h = element.height || 200;
  window.addEventListener('mousemove', onMouseMove); window.addEventListener('mouseup', stopInteraction);
};

const startRotate = (event: MouseEvent, element: StoryElement) => {
  event.stopPropagation(); event.preventDefault();
  activeRotateId.value = element.id; selectedElementId.value = element.id; elementStart.rotation = element.rotation;
  const elRect = (event.currentTarget as HTMLElement).closest('.group')?.getBoundingClientRect();
  if (elRect) { dragStart.x = elRect.left + elRect.width / 2; dragStart.y = elRect.top + elRect.height / 2; }
  else { dragStart.x = event.clientX; dragStart.y = event.clientY; }
  window.addEventListener('mousemove', onMouseMove); window.addEventListener('mouseup', stopInteraction);
};

const rotateElement90 = () => { if (selectedElement.value) selectedElement.value.rotation = (selectedElement.value.rotation + 90) % 360; };

const onMouseMove = (event: MouseEvent) => {
  if (activeDragId.value === 'CROP_MOVE' && croppingId.value) {
      const element = storyElements.value.find(el => el.id === croppingId.value);
      if (element) { element.cropX = elementStart.cropX + (event.clientX - dragStart.x); element.cropY = elementStart.cropY + (event.clientY - dragStart.y); }
      return;
  }
  if (activeDragId.value && activeDragId.value !== 'CROP_MOVE') {
    const element = storyElements.value.find(el => el.id === activeDragId.value);
    if (element) {
      let newX = elementStart.x + (event.clientX - dragStart.x);
      let newY = elementStart.y + (event.clientY - dragStart.y);
      if (element.type === 'text' || element.musicArtist ) {
          if (newX < 0) newX = 0; else if (newX + elementStart.w > bgDimensions.width) newX = bgDimensions.width - elementStart.w;
          if (newY < 0) newY = 0; else if (newY + elementStart.h > bgDimensions.height) newY = bgDimensions.height - elementStart.h;
      }
      element.x = newX; element.y = newY;
    }
  }
  if (activeResizeId.value) {
    const element = storyElements.value.find(el => el.id === activeResizeId.value);
    if (element) { const newSize = Math.max(50, elementStart.w + (event.clientX - dragStart.x)); element.width = newSize; if (element.height) element.height = newSize; element.scale = 1; }
  }
  if (activeRotateId.value) {
     const element = storyElements.value.find(el => el.id === activeRotateId.value);
     if(element) { const angleRad = Math.atan2(event.clientY - dragStart.y, event.clientX - dragStart.x); element.rotation = (angleRad * 180 / Math.PI) + 90; }
  }
};

const stopInteraction = () => { activeDragId.value = null; activeResizeId.value = null; activeRotateId.value = null; window.removeEventListener('mousemove', onMouseMove); window.removeEventListener('mouseup', stopInteraction); };
const enableEdit = (id: string) => { editingId.value = id; activeDragId.value = null; };
const disableEdit = () => { editingId.value = null; };
const onBackgroundClick = () => { disableEdit(); croppingId.value = null; selectedElementId.value = null; }
const toggleCrop = (id: string) => { if (croppingId.value === id) croppingId.value = null; else { croppingId.value = id; editingId.value = null; selectedElementId.value = id; } }
const addTextElement = () => { const newId = `el_${Date.now()}`; storyElements.value.push({ id: newId, type: 'text', content: 'Nowy Tekst', x: 100, y: 300, rotation: 0, scale: 1, styles: { color: '#ffffff', fontSize: '24px', fontWeight: 'bold' } }); selectedElementId.value = newId; };
const addImageElement = () => {
    const sampleImages = ['https://images.unsplash.com/photo-1517849845537-4d257902454a?ixlib=rb-1.2.1&auto=format&fit=crop&w=800&q=80', 'https://images.unsplash.com/photo-1554080353-a576cf803bda?ixlib=rb-1.2.1&auto=format&fit=crop&w=800&q=80'];
    const randomImg = sampleImages[Math.floor(Math.random() * sampleImages.length)];
    const newId = `el_img_${Date.now()}`; storyElements.value.push({ id: newId, type: 'image', content: randomImg, x: 60, y: 200, width: 240, height: 320, rotation: 0, scale: 1, cropX: 0, cropY: 0, cropZoom: 1 }); selectedElementId.value = newId;
}
const removeElement = (id: string) => { storyElements.value = storyElements.value.filter(el => el.id !== id); if (selectedElementId.value === id) selectedElementId.value = null; };
const elementTransform = (element: StoryElement) => { return `rotate(${element.rotation}deg) scale(${element.scale || 1})`; }
// Funkcja usuwająca muzykę i otwierająca ponownie listę utworów
const removeMusicAndOpenModal = () => {
    if (selectedElementId.value) {
        removeElement(selectedElementId.value); // Usuń element
        isMusicModalOpen.value = true;          // Otwórz modal
    }
};
</script>

<template>
  <div class="flex h-screen w-full bg-[#F0F2F5] font-sans overflow-hidden select-none relative">

    <Sidebar
      :is-music-modal-open="isMusicModalOpen"
      :current-alt-text="selectedElement?.altText"
      :is-image-selected="selectedElement?.type === 'image'"
      @add-text="addTextElement"
      @add-image="addImageElement"
      @toggle-music="toggleMusicModal"
    />

    <main class="flex-1 flex flex-col items-center justify-center p-6 bg-[#F0F2F5] overflow-hidden relative">
       <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-4 w-full h-full max-w-[1000px] flex flex-col relative">

           <div class="flex justify-between items-center mb-2 px-1">
               <span class="text-sm font-semibold text-gray-700">Podgląd</span>
               <div v-if="audioPlayer.src">
                   <button @click="toggleBackgroundMusic" class="p-2 rounded-full hover:bg-gray-100 transition text-gray-800" title="Wycisz podgląd">
                       <VolumeHigh v-if="isPlaying" :size="20" class="text-blue-600"/>
                       <VolumeOff v-else :size="20" class="text-gray-400"/>
                   </button>
               </div>
           </div>

           <div class="flex-1 bg-[#18191A] rounded-lg flex items-center justify-center overflow-hidden relative shadow-inner border border-gray-800" @mousedown.self="onBackgroundClick">
               <div class="relative aspect-[9/16] h-[calc(100%-68px)] bg-black overflow-visible shadow-2xl rounded-md border border-gray-700">

                    <div class="absolute inset-0 w-full h-full pointer-events-none rounded-md overflow-hidden" :style="{ background: background.value }" ref="backgroundRef"></div>

                    <div
                      v-for="element in storyElements"
                      :key="element.id"
                      class="absolute cursor-move group transition-transform duration-75"
                      :class="{ 'z-50': activeDragId === element.id || activeResizeId === element.id || selectedElementId === element.id }"
                      :style="{ top: element.y + 'px', left: element.x + 'px' }"
                      @mousedown="(e) => startDrag(e, element)"
                    >
                      <div class="relative transition-transform duration-75 origin-center"
                           :style="{
                               width: element.width ? element.width + 'px' : 'auto',
                               height: element.height ? element.height + 'px' : 'auto',
                               transform: elementTransform(element),
                               ...element.styles
                           }">

                          <button v-if="editingId !== element.id && croppingId !== element.id && !element.musicTitle" @click.stop="removeElement(element.id)" class="absolute -top-3 -right-3 w-6 h-6 bg-red-500 text-white rounded-full flex items-center justify-center opacity-0 group-hover:opacity-100 hover:bg-red-600 z-50 transition-opacity shadow-md"><Close :size="14" /></button>

                          <div v-if="element.type === 'image' && element.musicTitle" class="w-full h-full relative select-none">

                              <template v-if="element.musicStyle === 'large' || !element.musicStyle">
                                  <div class="w-full h-full bg-white/95 backdrop-blur-sm rounded-xl p-3 flex flex-col gap-3 shadow-lg pointer-events-none border border-gray-100">
                                      <img :src="element.content" class="w-full aspect-square object-cover rounded-lg shadow-sm" />
                                      <div class="text-center overflow-hidden pb-1">
                                          <p class="text-gray-900 font-bold text-lg leading-tight truncate">{{ element.musicTitle }}</p>
                                          <p class="text-gray-500 text-sm truncate">{{ element.musicArtist }}</p>
                                      </div>
                                  </div>
                              </template>

                              <template v-if="element.musicStyle === 'small'">
                                  <div class="w-full h-full bg-white/90 backdrop-blur-md rounded-lg p-2 flex items-center gap-3 shadow-md pointer-events-none border border-white/20">
                                      <img :src="element.content" class="w-12 h-12 rounded-md object-cover flex-shrink-0" />
                                      <div class="flex flex-col min-w-0 flex-1">
                                          <p class="text-gray-900 font-bold text-sm truncate">{{ element.musicTitle }}</p>
                                          <p class="text-gray-500 text-xs truncate">{{ element.musicArtist }}</p>
                                      </div>
                                      <MusicNote :size="20" class="text-gray-400 mr-2"/>
                                  </div>
                              </template>

                              <template v-if="element.musicStyle === 'text'">
                                  <div class="w-full h-full flex flex-col items-center justify-center pointer-events-none text-center">
                                      <p class="text-white font-bold text-2xl drop-shadow-[0_2px_8px_rgba(0,0,0,0.6)]">{{ element.musicTitle }}</p>
                                      <div class="flex items-center gap-1 opacity-90 drop-shadow-[0_1px_4px_rgba(0,0,0,0.6)] mt-1">
                                          <MusicNote :size="14" class="text-white"/>
                                          <p class="text-white text-sm font-medium">{{ element.musicArtist }}</p>
                                      </div>
                                  </div>
                              </template>

                              <template v-if="element.musicStyle === 'icon'">
                                  <div class="w-full h-full rounded-full shadow-lg border-2 border-white pointer-events-none relative overflow-hidden bg-black">

                                       <img
                                          :src="element.content"
                                          class="w-full h-full object-cover rounded-full animate-spin-record"
                                          alt="Album cover"
                                       />

                                       <div class="absolute inset-0 bg-black/30 flex items-center justify-center z-10">
                                            <MusicNote :size="24" class="text-white drop-shadow-md"/>
                                       </div>

                                  </div>
                              </template>

                          </div>

                          <div v-else-if="element.type === 'image'" class="flex flex-col items-center select-none w-full h-full">
                             <div
                                class="relative w-full h-full overflow-hidden bg-gray-900 border-2"
                                :class="(croppingId === element.id || selectedElementId === element.id) ? 'border-blue-500 shadow-md' : 'border-transparent'"
                                :style="element.styles"
                                @dblclick.stop="toggleCrop(element.id)"
                             >
                                <img
                                    :src="element.content"
                                    class="absolute max-w-none pointer-events-none origin-center"
                                    :style="{
                                        transform: `translate(${element.cropX || 0}px, ${element.cropY || 0}px) scale(${element.cropZoom || 1})`,
                                        width: '100%', height: '100%', objectFit: 'cover'
                                    }"
                                />
                                <div v-if="croppingId === element.id" class="absolute inset-0 grid grid-cols-3 grid-rows-3 pointer-events-none opacity-50">
                                    <div class="border-r border-b border-white/30"></div><div class="border-r border-b border-white/30"></div><div class="border-b border-white/30"></div>
                                    <div class="border-r border-b border-white/30"></div><div class="border-r border-b border-white/30"></div><div class="border-b border-white/30"></div>
                                    <div class="border-r border-white/30"></div><div class="border-r border-white/30"></div><div></div>
                                </div>
                             </div>
                             <template v-if="croppingId !== element.id">
                                <div @mousedown.stop="(e) => startResize(e, element)" class="absolute bottom-1 right-1 bg-white text-black p-1 rounded-full opacity-0 group-hover:opacity-100 cursor-nwse-resize shadow-md hover:scale-110 transition z-40"><ResizeBottomRight :size="16" /></div>
                             </template>
                          </div>

                          <template v-else-if="element.type === 'text'">
                             <div v-if="editingId !== element.id" @dblclick="enableEdit(element.id)" class="text-center min-w-[50px] whitespace-pre-wrap leading-tight drop-shadow-lg p-2 border-2 border-transparent group-hover:border-white/40 rounded-lg" :style="element.styles">{{ element.content }}</div>
                             <textarea v-else v-focus v-model="element.content" @blur="disableEdit" @keydown.enter.stop="disableEdit" @mousedown.stop class="bg-transparent text-center resize-none outline-none overflow-hidden min-w-[200px] p-2 border-2 border-blue-500 rounded-lg" :style="element.styles" rows="2"></textarea>
                          </template>
                      </div>
                    </div>
                    <div class="pointer-events-none absolute inset-0 z-[100] rounded-md shadow-[0_0_0_9999px_rgba(24,25,26,0.75)]"></div>
               </div>

               <ImageToolbar
                  v-if="selectedElement && selectedElement.type === 'image' && !selectedElement.musicTitle && croppingId !== selectedElement.id"
                  v-model:scale="selectedElement.scale"
                  @rotate="rotateElement90"
               />



           </div>
 <MusicToolbar
                  v-if="selectedElement && selectedElement.musicTitle"
                  :current-style="selectedElement.musicStyle || 'large'"
                  :track-title="selectedElement.musicTitle"
                  :track-artist="selectedElement.musicArtist"
                  :cover-url="selectedElement.content"
                  @update:style="updateMusicStyle"
                  @remove="removeMusicAndOpenModal()"
               />
           <MusicModal
              :is-open="isMusicModalOpen"
              @close="isMusicModalOpen = false"
              @add-track="addMusicPoster"
           />
       </div>
    </main>
  </div>
</template>

<style scoped>
.select-none { user-select: none; }
.pointer-events-none .cursor-move { cursor: default !important; }
@keyframes spin-record {
    from {
        transform: rotate(0deg);
    }
    to {
        transform: rotate(360deg);
    }
}

/* Klasa stosująca animację */
.animate-spin-record {
    /* 8s to czas jednego pełnego obrotu - dostosuj według uznania */
    animation: spin-record 8s linear infinite;
}
</style>
