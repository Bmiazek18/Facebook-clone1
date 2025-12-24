<script setup lang="ts">
import { ref, reactive, computed, onUnmounted } from 'vue';
import { FFmpeg } from '@ffmpeg/ffmpeg';
import { fetchFile, toBlobURL } from '@ffmpeg/util';
import type { LogEvent } from '@ffmpeg/ffmpeg/dist/esm/types';

interface VideoEditorProps {
  video: string;
}

const props = defineProps<VideoEditorProps>();

const emit = defineEmits<{
  (e: 'done', url: string): void;
  (e: 'back'): void;
}>();

// Ikony
import Play from 'vue-material-design-icons/Play.vue';
import Pause from 'vue-material-design-icons/Pause.vue';
import ContentCut from 'vue-material-design-icons/ContentCut.vue';
import Check from 'vue-material-design-icons/Check.vue';
import ClosedCaption from 'vue-material-design-icons/ClosedCaption.vue';
import Headphones from 'vue-material-design-icons/Headphones.vue';

// --- KONFIGURACJA FFMPEG (Z Twojego Snippetu) ---
const baseURL = 'https://cdn.jsdelivr.net/npm/@ffmpeg/core@0.12.10/dist/esm';


// --- STAN ---
const videoPlayer = ref<HTMLVideoElement | null>(null);
const timelineRef = ref<HTMLDivElement | null>(null);
const isPlaying = ref(false);
const duration = ref(0);
const frames = ref<string[]>([]);
const isProcessing = ref(false);
const message = ref(''); // Dodano do wyświetlania logów

const ffmpeg = new FFmpeg();

const range = reactive({ start: 0, end: 10 });
const dragging = ref<'start' | 'end' | null>(null);

const leftPercent = computed(() => duration.value ? (range.start / duration.value) * 100 : 0);
const widthPercent = computed(() => duration.value ? ((range.end - range.start) / duration.value) * 100 : 0);

// --- DRAG & DROP ---
const startDrag = (type: 'start' | 'end') => {
  dragging.value = type;
  document.body.style.cursor = 'ew-resize';
  window.addEventListener('mousemove', handleDrag);
  window.addEventListener('mouseup', stopDrag);
};

const handleDrag = (e: MouseEvent) => {
  if (!dragging.value || !timelineRef.value || duration.value === 0) return;
  const rect = timelineRef.value.getBoundingClientRect();
  const offsetX = e.clientX - rect.left;
  let newTime = (offsetX / rect.width) * duration.value;
  newTime = Math.max(0, Math.min(duration.value, newTime));
  const minGap = 0.5;

  if (dragging.value === 'start') {
    range.start = Math.min(newTime, range.end - minGap);
    if (videoPlayer.value) videoPlayer.value.currentTime = range.start;
  } else {
    range.end = Math.max(newTime, range.start + minGap);
    if (videoPlayer.value) videoPlayer.value.currentTime = range.end;
  }
};

const stopDrag = () => {
  dragging.value = null;
  document.body.style.cursor = '';
  window.removeEventListener('mousemove', handleDrag);
  window.removeEventListener('mouseup', stopDrag);
};

// --- KLATKI PODGLĄDU ---
const generateFrames = async () => {
  if (!duration.value) return;

  const tempVideo = document.createElement('video');
  tempVideo.crossOrigin = 'anonymous';
  tempVideo.src = props.video;
  tempVideo.muted = true;

  await new Promise(resolve => {
    tempVideo.onloadedmetadata = () => resolve(true);
    setTimeout(() => resolve(false), 5000);
  });

  const canvas = document.createElement('canvas');
  const ctx = canvas.getContext('2d');
  const frameCount = 20;
  const interval = duration.value / frameCount;

  frames.value = [];
  canvas.width = 100;
  canvas.height = 56;

  for (let i = 0; i < frameCount; i++) {
    tempVideo.currentTime = i * interval;
    await new Promise(r => tempVideo.onseeked = r);
    try {
      if (ctx) {
        ctx.drawImage(tempVideo, 0, 0, canvas.width, canvas.height);
        frames.value.push(canvas.toDataURL('image/jpeg', 0.5));
      }
    } catch (e) {}
  }
};

const onMetadataLoaded = () => {
  if (videoPlayer.value) {
    duration.value = videoPlayer.value.duration;
    range.end = videoPlayer.value.duration;
    generateFrames();
  }
};

const togglePlay = () => {
  if (!videoPlayer.value) return;
  if (isPlaying.value) videoPlayer.value.pause();
  else videoPlayer.value.play();
  isPlaying.value = !isPlaying.value;
};

const onTimeUpdate = () => {
  if (videoPlayer.value && isPlaying.value && !dragging.value) {
    if (videoPlayer.value.currentTime >= range.end) {
      videoPlayer.value.currentTime = range.start;
    }
  }
};

const transcode = async () => {
  isProcessing.value = true;
  message.value = 'Ładowanie silnika FFmpeg...';

  try {
    // Logowanie zdarzeń (z Twojego kodu)
    ffmpeg.on('log', ({ message: msg }: LogEvent) => {
      console.log(msg); // Logi w konsoli
      message.value = msg;
    });

    if (!ffmpeg.loaded) {
      // Używamy Twojego mechanizmu toBlobURL z wersją core-mt
      await ffmpeg.load({
        coreURL: await toBlobURL(`${baseURL}/ffmpeg-core.js`, 'text/javascript'),
        wasmURL: await toBlobURL(`${baseURL}/ffmpeg-core.wasm`, 'application/wasm'),
        workerURL: await toBlobURL(`${baseURL}/ffmpeg-core.worker.js`, 'text/javascript')
      });
    }

    message.value = 'Pobieranie pliku wideo...';
    // Zapis pliku do pamięci WASM
    await ffmpeg.writeFile('input.mp4', await fetchFile(props.video));

    message.value = 'Przycinanie wideo...';
    // Wykonanie komendy cięcia (zachowujemy logikę edytora: -ss, -t, -c copy)
    await ffmpeg.exec([
      '-i', 'input.mp4',
      '-ss', range.start.toString(),
      '-t', (range.end - range.start).toString(),
      '-c', 'copy', // Szybkie kopiowanie bez rekompresji
      'output.mp4'
    ]);

    message.value = 'Generowanie pliku wynikowego...';
    const data = await ffmpeg.readFile('output.mp4');

    // Tworzenie linku do pobrania
    const url = URL.createObjectURL(
      new Blob([(data as Uint8Array).buffer], { type: 'video/mp4' })
    );
    emit('done', url);

    message.value = 'Gotowe!';

  } catch (error: any) {
    console.error("FFmpeg Error:", error);
    message.value = `Błąd: ${error.message}`;
    alert(`Wystąpił błąd: ${error.message}`);
  } finally {
    isProcessing.value = false;
  }
};
</script>

<template>
  <div class="flex h-[90vh] w-full bg-[#2c2c2c] overflow-hidden font-sans select-none">

    <aside class="w-80 bg-white flex flex-col shadow-lg z-20 flex-shrink-0">
      <div>
        <div class="bg-blue-50 border-l-4 border-blue-600 p-4 flex items-center justify-between cursor-pointer">
          <div class="flex items-center gap-3 text-blue-700 font-semibold">
            <div class="bg-blue-600 text-white p-1.5 rounded-full"><ContentCut :size="18" /></div>
            Skróć film
          </div>
        </div>
        <div class="p-4 flex items-center justify-between text-gray-700 border-b border-gray-100 opacity-60">
          <div class="flex items-center gap-3"><div class="bg-gray-200 p-1.5 rounded-full"><ClosedCaption :size="18"/></div>Napisy</div>
        </div>
        <div class="p-4 flex items-center justify-between text-gray-700 border-b border-gray-100 opacity-60">
          <div class="flex items-center gap-3"><div class="bg-gray-200 p-1.5 rounded-full"><Headphones :size="18"/></div>Audiodeskrypcje</div>
        </div>
      </div>

      <div v-if="message" class="p-4 bg-gray-100 text-xs text-gray-600 font-mono border-t break-all">
        {{ message }}
      </div>
    </aside>

    <main class="flex-1 flex flex-col bg-[#333333] relative min-w-0">

      <div class="flex-1 flex justify-center items-center p-8 bg-[#2a2a2a] overflow-hidden">
        <div class="relative shadow-2xl rounded-lg overflow-hidden w-full max-w-5xl bg-black border border-gray-800" style="aspect-ratio: 16/9;">
          <video
            ref="videoPlayer"
            :src="props.video"
            crossorigin="anonymous"
            @loadedmetadata="onMetadataLoaded"
            @timeupdate="onTimeUpdate"
            @click="togglePlay"
            class="w-full h-full object-contain"
          ></video>
        </div>
      </div>

      <div class="h-32 bg-[#1f1f1f] border-t border-gray-700 flex items-center px-8 gap-6 z-10">
        <button @click="togglePlay" class="flex-shrink-0 w-12 h-12 flex items-center justify-center bg-black hover:bg-gray-800 text-white rounded-full transition border border-gray-600">
          <Pause v-if="isPlaying" :size="24" />
          <Play v-else :size="24" />
        </button>

        <div ref="timelineRef" class="relative flex-1 h-20 bg-gray-900 rounded-lg cursor-pointer group border border-gray-700 overflow-hidden">
          <div class="absolute inset-0 flex opacity-40 grayscale pointer-events-none">
            <template v-if="frames.length > 0">
              <div v-for="(frame, i) in frames" :key="i" class="flex-1 h-full relative bg-gray-800 border-r border-gray-800/30">
                <img v-if="frame" :src="frame" class="w-full h-full object-cover" />
                <div v-else class="w-full h-full bg-gray-800"></div>
              </div>
            </template>
            <div v-else class="w-full h-full flex items-center justify-center text-gray-500 text-sm">
              Ładowanie...
            </div>
          </div>

          <div
            class="absolute top-0 bottom-0 z-10 border-t-4 border-b-4 border-blue-500 shadow-[0_0_15px_rgba(59,130,246,0.4)]"
            :style="{ left: `${leftPercent}%`, width: `${widthPercent}%` }"
          >
            <div class="absolute inset-0 bg-blue-500 opacity-10 pointer-events-none"></div>
            <div @mousedown.stop.prevent="startDrag('start')" class="absolute left-0 top-0 bottom-0 w-5 -translate-x-1/2 bg-blue-600 cursor-ew-resize flex items-center justify-center rounded-l-md hover:bg-blue-500 z-20 pointer-events-auto">
              <div class="h-8 w-1 bg-white rounded-full opacity-70"></div>
            </div>
            <div @mousedown.stop.prevent="startDrag('end')" class="absolute right-0 top-0 bottom-0 w-5 translate-x-1/2 bg-blue-600 cursor-ew-resize flex items-center justify-center rounded-r-md hover:bg-blue-500 z-20 pointer-events-auto">
              <div class="h-8 w-1 bg-white rounded-full opacity-70"></div>
            </div>
          </div>
        </div>

        <div class="flex flex-col items-end min-w-[60px]">
             <div class="text-xs text-gray-400">Długość</div>
             <div class="font-mono text-white text-lg font-bold">{{ (range.end - range.start).toFixed(1) }}s</div>
        </div>

        <button @click="transcode" :disabled="isProcessing" class="flex-shrink-0 w-14 h-14 flex items-center justify-center bg-blue-600 hover:bg-blue-500 text-white rounded-xl transition shadow-xl disabled:opacity-50">
          <span v-if="isProcessing" class="animate-spin w-6 h-6 border-2 border-white rounded-full border-t-transparent"></span>
          <Check v-else :size="28"/>
        </button>

      </div>
    </main>
  </div>
</template>

<style scoped>
.select-none { user-select: none; -webkit-user-select: none; }
</style>
