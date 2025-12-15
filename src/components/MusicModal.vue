<script setup lang="ts">
import { ref, computed, watch } from 'vue';

// Ikony
import Magnify from 'vue-material-design-icons/Magnify.vue';
import Close from 'vue-material-design-icons/Close.vue';
import Bookmark from 'vue-material-design-icons/Bookmark.vue';
import BookmarkOutline from 'vue-material-design-icons/BookmarkOutline.vue';
import FormatListBulleted from 'vue-material-design-icons/FormatListBulleted.vue';
import Play from 'vue-material-design-icons/Play.vue';
import Pause from 'vue-material-design-icons/Pause.vue';
import MusicNote from 'vue-material-design-icons/MusicNote.vue';

// Typy
export interface MusicTrack {
  id: number;
  title: string;
  artist: string;
  coverUrl: string;
  coverUrlLarge: string;
  previewUrl: string;
}

const props = defineProps<{
  isOpen: boolean;
}>();

const emit = defineEmits<{
  (e: 'close'): void;
  (e: 'add-track', track: MusicTrack): void;
}>();

// Stan
const musicQuery = ref('');
const musicResults = ref<MusicTrack[]>([]);
const savedMusic = ref<MusicTrack[]>([]);
const isSearching = ref(false);
const currentTab = ref<'saved' | 'browse'>('browse');
const previewAudio = new Audio(); // Osobny obiekt audio dla podglądu w modalu
const currentPreviewTrackId = ref<number | null>(null);
const isPreviewPlaying = ref(false);

const displayedMusic = computed(() => {
    return currentTab.value === 'saved' ? savedMusic.value : musicResults.value;
});

// Logika API
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

// Obserwuj otwarcie modala, aby załadować "Top Hits"
watch(() => props.isOpen, (newVal) => {
    if (newVal && musicResults.value.length === 0) {
        loadInitialMusic();
    }
    // Zatrzymaj muzykę podglądu, gdy modal się zamyka
    if (!newVal) {
        previewAudio.pause();
        currentPreviewTrackId.value = null;
        isPreviewPlaying.value = false;
    }
});

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

const togglePreviewTrack = (track: MusicTrack) => {
    if (currentPreviewTrackId.value === track.id && !previewAudio.paused) {
        previewAudio.pause();
        currentPreviewTrackId.value = null;
        isPreviewPlaying.value = false;
    } else {
        previewAudio.src = track.previewUrl;
        previewAudio.play()
            .then(() => {
                currentPreviewTrackId.value = track.id;
                isPreviewPlaying.value = true;
            })
            .catch(e => console.error("Autoplay blocked:", e));
    }
}

// Zdarzenie dodania do płótna
const selectTrack = (track: MusicTrack) => {
    // Zatrzymujemy podgląd przed dodaniem
    previewAudio.pause();
    currentPreviewTrackId.value = null;
    emit('add-track', track);
}
</script>

<template>
  <Transition name="pop">
     <div v-if="isOpen" class="absolute -right-[115px] top-15 w-[320px] bg-white rounded-xl shadow-2xl z-300 flex flex-col h-[500px] overflow-hidden border border-gray-200">
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

                 <div v-for="track in displayedMusic" :key="track.id" class="group flex items-center justify-between py-2 px-2 hover:bg-gray-100 rounded-lg transition cursor-pointer" @click="selectTrack(track)">
                     <div class="flex items-center gap-3 overflow-hidden">
                         <div class="relative w-10 h-10 flex-shrink-0 rounded overflow-hidden bg-gray-200">
                             <img :src="track.coverUrl" class="w-full h-full object-cover"/>
                             <div class="absolute inset-0 bg-black/20 flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity">
                                 <button @click.stop="togglePreviewTrack(track)" class="text-white hover:scale-110 transition"><Pause v-if="currentPreviewTrackId === track.id && isPreviewPlaying" :size="18"/><Play v-else :size="18"/></button>
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
</template>

<style scoped>
/* Custom Scrollbar */
.custom-scrollbar::-webkit-scrollbar { width: 5px; }
.custom-scrollbar::-webkit-scrollbar-track { background: transparent; }
.custom-scrollbar::-webkit-scrollbar-thumb { background-color: #d1d5db; border-radius: 20px; }

/* Animacja modala */
.pop-enter-active, .pop-leave-active { transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1); }
.pop-enter-from, .pop-leave-to { opacity: 0; transform: scale(0.8) translateY(20px); }
</style>
