<script setup lang="ts">
import { ref, reactive, onUnmounted, computed,onMounted } from 'vue';

// --- IKONY ---
import Cog from 'vue-material-design-icons/Cog.vue';
import Close from 'vue-material-design-icons/Close.vue';
import FormatFont from 'vue-material-design-icons/FormatFont.vue';
import MusicNote from 'vue-material-design-icons/MusicNote.vue';
import StickerEmoji from 'vue-material-design-icons/StickerEmoji.vue';
import ImageIcon from 'vue-material-design-icons/Image.vue';
import Magnify from 'vue-material-design-icons/Magnify.vue';
import Play from 'vue-material-design-icons/Play.vue';
import Pause from 'vue-material-design-icons/Pause.vue';
import ResizeBottomRight from 'vue-material-design-icons/ResizeBottomRight.vue';
import VolumeHigh from 'vue-material-design-icons/VolumeHigh.vue';
import VolumeOff from 'vue-material-design-icons/VolumeOff.vue';
import RotateLeft from 'vue-material-design-icons/RotateLeft.vue';
import BookmarkOutline from 'vue-material-design-icons/BookmarkOutline.vue';
import Bookmark from 'vue-material-design-icons/Bookmark.vue';
import FormatListBulleted from 'vue-material-design-icons/FormatListBulleted.vue';
import Crop from 'vue-material-design-icons/Crop.vue';
import Check from 'vue-material-design-icons/Check.vue';

// --- STAŁE WYMIARY ---
const CANVAS_WIDTH = 558;
const CANVAS_HEIGHT = 1000;

// --- TYPY DANYCH ---
type ElementType = 'text' | 'image';

interface StoryElement {
  id: string;
  type: ElementType;
  content: string; // URL obrazka lub tekst
  x: number;
  y: number;
  width?: number; // Szerokość kontenera
  height?: number; // Wysokość kontenera (dla zdjęć)
  rotation: number;
  // Pola dla kadrowania (tylko obrazy)
  cropX?: number; // Przesunięcie obrazka wewnątrz ramki X
  cropY?: number; // Przesunięcie obrazka wewnątrz ramki Y
  cropZoom?: number; // Skala obrazka wewnątrz ramki
  styles?: Record<string, string>;
  musicTitle?: string;
  musicArtist?: string;
}

interface BackgroundSettings {
  type: 'gradient' | 'image';
  value: string;
}

interface MusicTrack {
  id: number;
  title: string;
  artist: string;
  coverUrl: string;
  coverUrlLarge: string;
  previewUrl: string;
}

// --- DYREKTYWA V-FOCUS ---
const vFocus = {
  mounted: (el: HTMLElement) => el.focus()
};

// --- STAN APLIKACJI ---
const background = reactive<BackgroundSettings>({
  type: 'gradient',
  value: 'linear-gradient(to bottom, #3b82f6, #86efac)'
});

const storyElements = ref<StoryElement[]>([
  {
    id: 'el_init_text',
    type: 'text',
    content: 'Przesuwaj mnie!',
    x: 80, y: 100, rotation: 0,
    styles: { color: '#ffffff', fontSize: '24px', fontWeight: 'bold' }
  }
]);

// --- AUDIO (Tło) ---
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
const bgDimensions = reactive({ width: CANVAS_WIDTH, height: CANVAS_HEIGHT }); // Domyślnie startujemy ze stałych
let resizeObserver: ResizeObserver | null = null;

onMounted(() => {
  if (backgroundRef.value) {
    resizeObserver = new ResizeObserver((entries) => {
      for (const entry of entries) {
        bgDimensions.width = entry.contentRect.width;
        bgDimensions.height = entry.contentRect.height;
      }
    });
    resizeObserver.observe(backgroundRef.value);
  }
});
onUnmounted(() => {
  audioPlayer.pause();
});

// --- MUZYKA: STAN I LOGIKA ---
const isMusicModalOpen = ref(false);
const musicQuery = ref('');
const musicResults = ref<MusicTrack[]>([]);
const savedMusic = ref<MusicTrack[]>([]);
const isSearching = ref(false);
const currentTab = ref<'saved' | 'browse'>('browse');

const displayedMusic = computed(() => {
    return currentTab.value === 'saved' ? savedMusic.value : musicResults.value;
});

// API iTunes
const searchMusic = async () => {
  if (musicQuery.value.trim().length < 2) return;
  isSearching.value = true;
  musicResults.value = [];
  currentTab.value = 'browse';

  try {
    const term = musicQuery.value.replace(/\s+/g, '+');
    const url = `https://itunes.apple.com/search?term=${term}&media=music&entity=song&limit=10&country=PL`;
    const response = await fetch(url);
    const data = await response.json();

    musicResults.value = data.results.map((item: any) => ({
      id: item.trackId,
      title: item.trackName,
      artist: item.artistName,
      coverUrl: item.artworkUrl100,
      coverUrlLarge: item.artworkUrl100.replace('100x100', '600x600'),
      previewUrl: item.previewUrl
    }));
  } catch (error) {
    console.error("Błąd iTunes:", error);
  } finally {
    isSearching.value = false;
  }
};

const loadInitialMusic = () => {
    musicQuery.value = 'Top Hits';
    searchMusic().then(() => musicQuery.value = '');
}

const toggleMusicModal = () => {
    isMusicModalOpen.value = !isMusicModalOpen.value;
    if(isMusicModalOpen.value && musicResults.value.length === 0) {
        loadInitialMusic();
    }
}

const toggleSaveMusic = (track: MusicTrack) => {
    const index = savedMusic.value.findIndex(t => t.id === track.id);
    if (index === -1) {
        savedMusic.value.push(track);
    } else {
        savedMusic.value.splice(index, 1);
    }
};

const isSaved = (trackId: number) => {
    return savedMusic.value.some(t => t.id === trackId);
}

const currentPreviewUrl = ref<string | null>(null);
const currentPreviewTrackId = ref<number | null>(null);

const togglePreviewTrack = (track: MusicTrack) => {
    if (currentPreviewTrackId.value === track.id && !audioPlayer.paused) {
        audioPlayer.pause();
        currentPreviewUrl.value = null;
        currentPreviewTrackId.value = null;
        isPlaying.value = false;
    } else {
        audioPlayer.src = track.previewUrl;
        audioPlayer.play()
            .then(() => {
                currentPreviewUrl.value = track.previewUrl;
                currentPreviewTrackId.value = track.id;
                isPlaying.value = true;
            })
            .catch(e => console.error("Autoplay blocked:", e));
    }
}

const addMusicPoster = (track: MusicTrack) => {
  const newId = `el_${Date.now()}`;
  storyElements.value.push({
    id: newId,
    type: 'image',
    content: track.coverUrlLarge,
    x: 50, y: 150, width: 200, height: 200,
    rotation: 0,
    cropX: 0, cropY: 0, cropZoom: 1,
    styles: { borderRadius: '12px', boxShadow: '0 8px 20px rgba(0,0,0,0.4)' },
    musicTitle: track.title, musicArtist: track.artist
  });

  if (track.previewUrl && audioPlayer.src !== track.previewUrl) {
    audioPlayer.src = track.previewUrl;
    audioPlayer.play().then(() => { isPlaying.value = true; });
  } else if (audioPlayer.paused) {
     audioPlayer.play().then(() => { isPlaying.value = true; });
  }

  isMusicModalOpen.value = false;
};

// --- LOGIKA INTERAKCJI ---
const activeDragId = ref<string | null>(null);
const activeResizeId = ref<string | null>(null);
const activeRotateId = ref<string | null>(null);
const editingId = ref<string | null>(null);
const croppingId = ref<string | null>(null);

const dragStart = reactive({ x: 0, y: 0 });
const elementStart = reactive({ x: 0, y: 0, w: 0, h: 0, rotation: 0, cropX: 0, cropY: 0 });

// 1. Przesuwanie (Drag)
const startDrag = (event: MouseEvent, element: StoryElement) => {
  if (editingId.value === element.id || activeResizeId.value || activeRotateId.value) return;

  if (croppingId.value === element.id) {
     activeDragId.value = 'CROP_MOVE';
     dragStart.x = event.clientX;
     dragStart.y = event.clientY;
     elementStart.cropX = element.cropX || 0;
     elementStart.cropY = element.cropY || 0;
  } else {
     if (croppingId.value) return;
     activeDragId.value = element.id;
     dragStart.x = event.clientX;
     dragStart.y = event.clientY;
     elementStart.x = element.x;
     elementStart.y = element.y;

     const target = event.currentTarget as HTMLElement;
     elementStart.w = target.offsetWidth;
     elementStart.h = target.offsetHeight;
  }

  window.addEventListener('mousemove', onMouseMove);
  window.addEventListener('mouseup', stopInteraction);
};

// 2. Skalowanie (Resize)
const startResize = (event: MouseEvent, element: StoryElement) => {
  event.stopPropagation();
  event.preventDefault();
  activeResizeId.value = element.id;
  dragStart.x = event.clientX;
  elementStart.w = element.width || 200;
  elementStart.h = element.height || 200;
  window.addEventListener('mousemove', onMouseMove);
  window.addEventListener('mouseup', stopInteraction);
};

// 3. Obracanie (Rotate)
const startRotate = (event: MouseEvent, element: StoryElement) => {
  event.stopPropagation();
  event.preventDefault();
  activeRotateId.value = element.id;
  elementStart.rotation = element.rotation;

  const elRect = (event.currentTarget as HTMLElement).closest('.group')?.getBoundingClientRect();
  if (elRect) {
    dragStart.x = elRect.left + elRect.width / 2;
    dragStart.y = elRect.top + elRect.height / 2;
  } else {
     dragStart.x = event.clientX;
     dragStart.y = event.clientY;
  }

  window.addEventListener('mousemove', onMouseMove);
  window.addEventListener('mouseup', stopInteraction);
};

// Główna pętla ruchu
const onMouseMove = (event: MouseEvent) => {
  // A. KADROWANIE
  if (activeDragId.value === 'CROP_MOVE' && croppingId.value) {
      const element = storyElements.value.find(el => el.id === croppingId.value);
      if (element) {
          const deltaX = event.clientX - dragStart.x;
          const deltaY = event.clientY - dragStart.y;
          element.cropX = elementStart.cropX + deltaX;
          element.cropY = elementStart.cropY + deltaY;
      }
      return;
  }

  // B. PRZESUWANIE ELEMENTU
  if (activeDragId.value && activeDragId.value !== 'CROP_MOVE') {
    const element = storyElements.value.find(el => el.id === activeDragId.value);
    if (element) {
      const deltaX = event.clientX - dragStart.x;
      const deltaY = event.clientY - dragStart.y;

      let newX = elementStart.x + deltaX;
      let newY = elementStart.y + deltaY;

      // ZMIANA: Blokujemy TYLKO jeśli to TEKST lub element z muzyką (tekstowy)
      // Używamy dynamicznych wymiarów bgDimensions zamiast stałych CANVAS_
      if (element.type === 'text' || element.musicArtist ) {

          // Blokada lewa
          if (newX < 0) newX = 0;
          // Blokada prawa (dynamiczna szerokość)
          else if (newX + elementStart.w > bgDimensions.width) {
              newX = bgDimensions.width - elementStart.w;
          }

          // Blokada góra
          if (newY < 0) newY = 0;
          // Blokada dół (dynamiczna wysokość)
          else if (newY + elementStart.h > bgDimensions.height) {
              newY = bgDimensions.height - elementStart.h;
          }
      }

      element.x = newX;
      element.y = newY;
    }
  }

  // C. SKALOWANIE (bez zmian)
  if (activeResizeId.value) {
    const element = storyElements.value.find(el => el.id === activeResizeId.value);
    if (element) {
      const deltaX = event.clientX - dragStart.x;
      const newSize = Math.max(100, elementStart.w + deltaX);
      element.width = newSize;
      if (element.height) element.height = newSize;
    }
  }

  // D. OBRACANIE (bez zmian)
  if (activeRotateId.value) {
    const element = storyElements.value.find(el => el.id === activeRotateId.value);
    if (element) {
      const angleRad = Math.atan2(event.clientY - dragStart.y, event.clientX - dragStart.x);
      const angleDeg = (angleRad * 180 / Math.PI) + 90;
      element.rotation = angleDeg;
    }
  }
};

const stopInteraction = () => {
  activeDragId.value = null;
  activeResizeId.value = null;
  activeRotateId.value = null;
  window.removeEventListener('mousemove', onMouseMove);
  window.removeEventListener('mouseup', stopInteraction);
};

// --- TRYB EDYCJI ---
const enableEdit = (id: string) => {
  editingId.value = id;
  activeDragId.value = null;
};
const disableEdit = () => {
  editingId.value = null;
};

// --- TRYB KADROWANIA ---
const toggleCrop = (id: string) => {
    if (croppingId.value === id) {
        croppingId.value = null;
    } else {
        croppingId.value = id;
        editingId.value = null;
    }
}

// --- DODAWANIE ELEMENTÓW ---
const addTextElement = () => {
  storyElements.value.push({
    id: `el_${Date.now()}`,
    type: 'text',
    content: 'Nowy Tekst',
    x: 100, y: 300, rotation: 0,
    styles: { color: '#ffffff', fontSize: '24px', fontWeight: 'bold' }
  });
};

const addImageElement = () => {
    const sampleImages = [
        'https://images.unsplash.com/photo-1517849845537-4d257902454a?ixlib=rb-1.2.1&auto=format&fit=crop&w=800&q=80',
        'https://images.unsplash.com/photo-1554080353-a576cf803bda?ixlib=rb-1.2.1&auto=format&fit=crop&w=800&q=80',
        'https://images.unsplash.com/photo-1506744038136-46273834b3fb?ixlib=rb-1.2.1&auto=format&fit=crop&w=800&q=80'
    ];
    const randomImg = sampleImages[Math.floor(Math.random() * sampleImages.length)];

    storyElements.value.push({
        id: `el_img_${Date.now()}`,
        type: 'image',
        content: randomImg,
        x: 60, y: 200, width: 240, height: 320,
        rotation: 0,
        cropX: 0, cropY: 0, cropZoom: 1
    });
}

const removeElement = (id: string) => {
  storyElements.value = storyElements.value.filter(el => el.id !== id);
};

const elementTransform = (element: StoryElement) => {
   return `rotate(${element.rotation}deg)`;
}
</script>

<template>
  <div class="flex h-screen w-full bg-[#F0F2F5] font-sans overflow-hidden select-none relative">

    <aside class="w-[360px] flex-shrink-0 bg-white shadow-xl z-20 flex flex-col justify-between h-full border-r border-gray-300">
      <div>
        <div class="flex items-center justify-between p-4 border-b border-gray-200">
          <h1 class="text-2xl font-bold text-black">Twoja relacja</h1>
          <div class="bg-gray-100 p-2 rounded-full cursor-pointer hover:bg-gray-200 transition"><Cog :size="24" class="text-black"/></div>
        </div>
        <div class="flex items-center gap-3 p-4">
           <div class="w-12 h-12 bg-blue-600 rounded-full flex items-center justify-center text-white font-bold text-xl overflow-hidden">
               <img src="https://i.pravatar.cc/150?img=12" class="w-full h-full object-cover" />
           </div>
           <span class="font-semibold text-lg text-gray-900">Bartosz Mązek</span>
        </div>
        <div class="flex flex-col mt-2">
           <div @click="addTextElement" class="flex items-center gap-4 px-4 py-3 hover:bg-gray-100 cursor-pointer transition active:scale-95">
              <div class="bg-gray-200 p-2.5 rounded-full border border-gray-300"><FormatFont :size="24" class="text-gray-700"/></div>
              <span class="font-medium text-gray-700 text-sm">Dodaj tekst</span>
           </div>
           <div @click="addImageElement" class="flex items-center gap-4 px-4 py-3 hover:bg-gray-100 cursor-pointer transition active:scale-95">
              <div class="bg-gray-200 p-2.5 rounded-full border border-gray-300"><ImageIcon :size="24" class="text-green-600"/></div>
              <span class="font-medium text-gray-700 text-sm">Dodaj zdjęcie</span>
           </div>
           <div @click="toggleMusicModal" class="flex items-center gap-4 px-4 py-3 hover:bg-gray-100 cursor-pointer transition" :class="{'bg-blue-50 border-l-4 border-blue-500': isMusicModalOpen}">
              <div class="bg-gray-200 p-2.5 rounded-full border border-gray-300"><MusicNote :size="24" class="text-gray-700"/></div>
              <span class="font-medium text-gray-700 text-sm">Dodaj muzykę</span>
           </div>
           <div class="flex items-center gap-4 px-4 py-3 hover:bg-gray-100 cursor-pointer transition">
              <div class="bg-gray-200 p-2.5 rounded-full border border-gray-300"><StickerEmoji :size="24" class="text-gray-700"/></div>
              <span class="font-medium text-gray-700 text-sm">Naklejki</span>
           </div>
        </div>
      </div>
      <div class="p-4 flex gap-3 shadow-[0_-4px_10px_rgba(0,0,0,0.05)] bg-white z-30">
         <button class="flex-1 py-2.5 rounded-lg bg-gray-200 text-gray-800 font-semibold hover:bg-gray-300 transition">Odrzuć</button>
         <button class="flex-1 py-2.5 rounded-lg bg-blue-600 text-white font-semibold hover:bg-blue-700 transition shadow-md">Udostępnij w relacji</button>
      </div>
    </aside>

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

           <div class="flex-1 bg-[#18191A] rounded-lg flex items-center justify-center overflow-hidden relative shadow-inner border border-gray-800" @mousedown.self="disableEdit(); croppingId = null;">

               <div class="relative aspect-[9/16] h-[calc(100%-68px)] bg-black overflow-visible shadow-2xl rounded-md border border-gray-700">

                    <div class="absolute inset-0 w-full h-full pointer-events-none rounded-md overflow-hidden" :style="{ background: background.value }" ref="backgroundRef"></div>

                    <div
                      v-for="element in storyElements"
                      :key="element.id"
                      class="absolute cursor-move group transition-transform duration-75"
                      :class="{ 'z-50': activeDragId === element.id || activeResizeId === element.id || activeRotateId === element.id || croppingId === element.id }"
                      :style="{ top: element.y + 'px', left: element.x + 'px' }"
                      @mousedown="(e) => startDrag(e, element)"
                    >
                      <div class="relative transition-transform duration-75" :style="{ width: element.width ? element.width + 'px' : 'auto', height: element.height ? element.height + 'px' : 'auto', transform: elementTransform(element) }">

                          <button v-if="editingId !== element.id && croppingId !== element.id" @click.stop="removeElement(element.id)" class="absolute -top-3 -right-3 w-6 h-6 bg-red-500 text-white rounded-full flex items-center justify-center opacity-0 group-hover:opacity-100 hover:bg-red-600 z-50 transition-opacity shadow-md"><Close :size="14" /></button>

                          <div v-if="element.type === 'image'" class="flex flex-col items-center select-none w-full h-full">
                             <div
                                class="relative w-full h-full overflow-hidden bg-gray-900 border-2"
                                :class="croppingId === element.id ? 'border-blue-500 shadow-[0_0_0_1000px_rgba(0,0,0,0.5)] z-50' : 'border-transparent'"
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
                                <div @mousedown.stop="(e) => startRotate(e, element)" class="absolute top-1 left-1 bg-white text-black p-1 rounded-full opacity-0 group-hover:opacity-100 cursor-pointer shadow-md hover:scale-110 transition z-40" title="Obróć"><RotateLeft :size="16" /></div>
                                <button @click.stop="toggleCrop(element.id)" class="absolute top-1 right-1 bg-white text-black p-1 rounded-full opacity-0 group-hover:opacity-100 cursor-pointer shadow-md hover:scale-110 transition z-40" title="Przytnij / Kadruj"><Crop :size="16" /></button>
                             </template>

                             <div v-if="croppingId === element.id" class="absolute -bottom-14 left-1/2 -translate-x-1/2 bg-white px-4 py-2 rounded-full shadow-xl flex items-center gap-3 z-[60]">
                                <span class="text-xs font-bold text-gray-500">Zoom</span>
                                <input type="range" min="1" max="3" step="0.1" v-model.number="element.cropZoom" class="w-24 accent-blue-600 cursor-pointer" @mousedown.stop />
                                <button @click.stop="toggleCrop(element.id)" class="bg-blue-600 text-white rounded-full p-1 hover:bg-blue-700"><Check :size="16"/></button>
                             </div>

                             <div v-if="element.musicTitle" class="mt-2 text-center w-full px-1 pointer-events-none">
                                <p class="text-white font-bold text-sm leading-tight drop-shadow-md break-words">{{ element.musicTitle }}</p>
                                <p class="text-white/80 text-xs font-medium drop-shadow-md break-words">{{ element.musicArtist }}</p>
                             </div>
                          </div>

                          <template v-if="element.type === 'text'">
                             <div v-if="editingId !== element.id" @dblclick="enableEdit(element.id)" class="text-center min-w-[50px] whitespace-pre-wrap leading-tight drop-shadow-lg p-2 border-2 border-transparent group-hover:border-white/40 rounded-lg" :style="element.styles">{{ element.content }}</div>
                             <textarea v-else v-focus v-model="element.content" @blur="disableEdit" @keydown.enter.stop="disableEdit" @mousedown.stop class="bg-transparent text-center resize-none outline-none overflow-hidden min-w-[200px] p-2 border-2 border-blue-500 rounded-lg" :style="element.styles" rows="2"></textarea>
                          </template>
                      </div>
                    </div>

                    <div class="pointer-events-none absolute inset-0 z-[100] rounded-md shadow-[0_0_0_9999px_rgba(24,25,26,0.75)]"></div>

               </div>
           </div>

           <Transition name="pop">
                   <div v-if="isMusicModalOpen" class="absolute -right-[115px] top-15 w-[320px] bg-white rounded-xl shadow-2xl z-50 flex flex-col h-[500px] overflow-hidden border border-gray-200">
                       <div class="p-3 border-b border-gray-100">
                           <div class="relative">
                               <Magnify class="absolute left-3 top-2.5 text-gray-400" :size="20"/>
                               <input v-model="musicQuery" @keydown.enter="searchMusic" type="text" placeholder="Szukaj muzyki" class="w-full pl-10 pr-8 py-2 bg-gray-100 rounded-full text-sm placeholder-gray-500 focus:outline-none focus:ring-1 focus:ring-blue-500 transition"/>
                               <button v-if="musicQuery" @click="musicQuery = ''; musicResults = []" class="absolute right-3 top-2.5 text-gray-400 hover:text-gray-600"><Close :size="16"/></button>
                           </div>
                           <div class="flex mt-3 gap-2">
                               <button @click="currentTab = 'saved'" class="flex items-center gap-1.5 px-3 py-1.5 rounded-full text-xs font-medium transition" :class="currentTab === 'saved' ? 'bg-gray-900 text-white' : 'hover:bg-gray-100 text-gray-600'"><Bookmark :size="14" />Zapisane</button>
                               <button @click="currentTab = 'browse'" class="flex items-center gap-1.5 px-3 py-1.5 rounded-full text-xs font-medium transition" :class="currentTab === 'browse' ? 'bg-gray-900 text-white' : 'hover:bg-gray-100 text-gray-600'"><FormatListBulleted :size="14" />Przeglądaj</button>
                           </div>
                       </div>

                       <div class="flex-1 overflow-y-auto custom-scrollbar">
                           <div class="px-2 py-2">
                               <h3 class="text-xs font-bold text-gray-500 mb-2 px-2 uppercase tracking-wide">{{ currentTab === 'saved' ? 'Twoje ulubione' : 'Dla Ciebie' }}</h3>
                               <div v-if="isSearching" class="text-center py-4 text-gray-400 text-sm">Szukam...</div>

                               <div v-for="track in displayedMusic" :key="track.id" class="group flex items-center justify-between py-2 px-2 hover:bg-gray-100 rounded-lg transition cursor-pointer" @click="addMusicPoster(track)">
                                   <div class="flex items-center gap-3 overflow-hidden">
                                       <div class="relative w-10 h-10 flex-shrink-0 rounded overflow-hidden bg-gray-200">
                                           <img :src="track.coverUrl" class="w-full h-full object-cover"/>
                                           <div class="absolute inset-0 bg-black/20 flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity">
                                               <button @click.stop="togglePreviewTrack(track)" class="text-white hover:scale-110 transition"><Pause v-if="currentPreviewTrackId === track.id && isPlaying" :size="18"/><Play v-else :size="18"/></button>
                                           </div>
                                       </div>
                                       <div class="flex flex-col min-w-0">
                                           <span class="text-sm font-semibold text-gray-900 truncate">{{ track.title }}</span>
                                           <span class="text-xs text-gray-500 truncate">{{ track.artist }}</span>
                                       </div>
                                   </div>
                                   <div class="flex items-center gap-1">
                                       <button @click.stop="toggleSaveMusic(track)" class="p-1 text-gray-400 hover:text-blue-600 rounded-full hover:bg-blue-50 transition" :title="isSaved(track.id) ? 'Usuń z ulubionych' : 'Zapisz'">
                                           <Bookmark v-if="isSaved(track.id)" :size="18" class="text-blue-600"/>
                                           <BookmarkOutline v-else :size="18" />
                                       </button>
                                   </div>
                               </div>

                               <div v-if="!displayedMusic.length && !isSearching" class="text-center py-8">
                                   <MusicNote class="mx-auto text-gray-300 mb-2" :size="32"/>
                                   <p class="text-gray-500 text-xs">{{ currentTab === 'saved' ? 'Brak zapisanych utworów.' : 'Wpisz nazwę utworu powyżej.' }}</p>
                               </div>
                           </div>
                       </div>
                   </div>
               </Transition>

           <div class="text-center py-3">
             <div class="text-gray-500 text-xs font-medium">Kliknij dwukrotnie zdjęcie, aby je przyciąć. Przeciągnij róg, aby skalować.</div>
           </div>

       </div>
    </main>
  </div>
</template>

<style scoped>
.select-none { user-select: none; }
.pointer-events-none .cursor-move { cursor: default !important; }

/* Custom Scrollbar */
.custom-scrollbar::-webkit-scrollbar { width: 5px; }
.custom-scrollbar::-webkit-scrollbar-track { background: transparent; }
.custom-scrollbar::-webkit-scrollbar-thumb { background-color: #d1d5db; border-radius: 20px; }

/* Animacja modala */
.pop-enter-active, .pop-leave-active { transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1); }
.pop-enter-from, .pop-leave-to { opacity: 0; transform: scale(0.8) translateY(20px); }
</style>
