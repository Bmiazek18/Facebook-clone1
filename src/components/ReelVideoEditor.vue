<template>
  <div class="fixed inset-0 bg-black z-50 flex flex-col">
    <div class="bg-gray-900 text-white px-4 py-3 flex items-center justify-between border-b border-gray-700">
      <button @click="$emit('back')" class="flex items-center gap-2 hover:text-blue-400 transition-colors">
        <ChevronLeftIcon :size="24" />
        <span class="font-medium">Wstecz</span>
      </button>
      <h2 class="text-lg font-bold">Edytor Video</h2>
      <button
        @click="handleSave"
        :disabled="clips.length === 0 || isRendering"
        class="bg-blue-600 hover:bg-blue-700 disabled:bg-gray-700 disabled:cursor-not-allowed px-4 py-2 rounded-lg font-medium transition-colors flex items-center gap-2"
      >
        <span v-if="isRendering" class="animate-spin w-4 h-4 border-2 border-white rounded-full border-t-transparent"></span>
        <span>{{ isRendering ? `${renderProgress}%` : 'Eksportuj' }}</span>
      </button>
    </div>

    <div v-if="isRendering" class="absolute inset-0 bg-black/90 flex items-center justify-center z-50">
      <div class="bg-gray-900 rounded-lg p-8 max-w-md w-full mx-4 text-center">
        <h3 class="text-white text-xl font-bold mb-4">Renderowanie...</h3>
        <div class="mb-4">
          <div class="bg-gray-700 rounded-full h-4 overflow-hidden">
            <div
              class="bg-blue-600 h-full transition-all duration-300"
              :style="{ width: renderProgress + '%' }"
            ></div>
          </div>
          <div class="text-white text-center mt-2 font-bold">{{ renderProgress }}%</div>
        </div>
        <p class="text-gray-400 text-sm">Nie zamykaj tej karty przeglądarki.</p>
      </div>
    </div>

    <canvas ref="renderCanvasRef" width="1080" height="1920" class="hidden"></canvas>

    <div class="flex-1 flex overflow-hidden">
      <div class="flex-1 flex items-center justify-center bg-gray-950 p-4">
        <div class="relative max-w-[360px] w-full aspect-9/16 bg-black rounded-lg overflow-hidden shadow-2xl">
          <video
            ref="previewVideoRef"
            :src="currentPreviewVideo"
            class="w-full h-full object-contain"
            @loadedmetadata="handleVideoLoaded"
            @ended="handleVideoEnded"
            playsinline
            muted
            crossorigin="anonymous"
          ></video>

          <div
            v-for="image in visibleImages"
            :key="image.id"
            class="absolute pointer-events-none"
            :style="getImageStyle(image)"
          >
            <img :src="image.url" class="w-full h-full object-contain" crossorigin="anonymous" />
          </div>

          <div
            v-for="pipVideo in visiblePipVideos"
            :key="pipVideo.id"
            class="absolute pointer-events-none overflow-hidden"
            :style="getPipVideoStyle(pipVideo)"
          >
            <video
              :ref="el => setPipVideoRef(el as HTMLVideoElement, pipVideo.id)"
              :src="pipVideo.url"
              class="w-full h-full object-cover"
              :muted="pipVideo.volume === 0"
              :volume="pipVideo.volume"
              playsinline
              crossorigin="anonymous"
            ></video>
          </div>

          <div
            v-for="text in visibleTexts"
            :key="text.id"
            class="absolute pointer-events-none"
            :style="getTextStyle(text)"
          >
            {{ getTextContent(text) }}
          </div>

          <div class="absolute bottom-4 left-1/2 -translate-x-1/2 flex gap-2">
            <button
              @click="togglePlayback"
              class="bg-white/90 hover:bg-white p-3 rounded-full transition-colors shadow-lg"
            >
              <PlayIcon v-if="!isPlaying" :size="24" fillColor="#000" />
              <PauseIcon v-else :size="24" fillColor="#000" />
            </button>
          </div>
        </div>
      </div>

      <div class="w-80 bg-gray-900 border-l border-gray-700 overflow-y-auto">
        <div class="p-4 space-y-4">
          <div>
            <h3 class="text-white font-bold mb-3 flex items-center gap-2">
              <VideoIcon :size="20" />
              Dodaj Video
            </h3>
            <label class="block">
              <input
                type="file"
                accept="video/*"
                @change="handleAddVideo"
                class="hidden"
              />
              <div class="border-2 border-dashed border-gray-600 hover:border-blue-500 rounded-lg p-4 text-center cursor-pointer transition-colors">
                <PlusIcon :size="32" class="mx-auto text-gray-400" />
                <p class="text-gray-400 text-sm mt-2">Kliknij aby dodać video</p>
              </div>
            </label>
          </div>

          <div>
            <h3 class="text-white font-bold mb-3 flex items-center gap-2">
              <TextIcon :size="20" />
              Dodaj Tekst
            </h3>
            <button
              @click="addTextOverlay"
              class="w-full bg-blue-600 hover:bg-blue-700 text-white py-2 rounded-lg transition-colors"
            >
              + Nowy Tekst
            </button>
          </div>

          <div>
            <h3 class="text-white font-bold mb-3 flex items-center gap-2">
              <ImageIcon :size="20" />
              Dodaj Obrazek
            </h3>
            <label class="block">
              <input
                type="file"
                accept="image/*"
                @change="handleAddImage"
                class="hidden"
              />
              <div class="w-full border-2 border-dashed border-gray-600 hover:border-blue-500 rounded-lg p-4 text-center cursor-pointer transition-colors">
                <ImageIcon :size="32" class="mx-auto text-gray-400" />
                <p class="text-gray-400 text-sm mt-2">Kliknij aby dodać obrazek</p>
              </div>
            </label>
          </div>

          <div>
            <h3 class="text-white font-bold mb-3 flex items-center gap-2">
              <VideoIcon :size="20" />
              Dodaj Video PiP
            </h3>
            <label class="block">
              <input
                type="file"
                accept="video/*"
                @change="handleAddPipVideo"
                class="hidden"
              />
              <div class="w-full border-2 border-dashed border-gray-600 hover:border-blue-500 rounded-lg p-4 text-center cursor-pointer transition-colors">
                <VideoIcon :size="32" class="mx-auto text-gray-400" />
                <p class="text-gray-400 text-sm mt-2">Kliknij aby dodać video</p>
              </div>
            </label>
          </div>

          <!-- Editor Components -->
          <TextEditor :text="selectedText" @update="updateSelectedText" @delete="deleteText" />
          <ImageEditor :image="selectedImage" @update="updateSelectedImage" @delete="deleteImage" />
          <PipVideoEditor :video="selectedPipVideo" @update="updateSelectedPipVideo" @delete="deletePipVideo" />
        </div>
      </div>
    </div>

    <div class="bg-gray-900 border-t border-gray-700 p-4">
      <div class="max-w-full">
        <div class="flex items-center justify-between mb-3">
          <h3 class="text-white font-bold">Timeline</h3>
          <div class="text-gray-400 text-sm">
            {{ formatTime(currentTime) }} / {{ formatTime(totalDuration) }}
          </div>
        </div>

        <div ref="timelineRef" class="relative bg-gray-800 rounded-lg p-3 overflow-x-auto">
          <div @click="handleTimelineClick" class="flex items-center gap-2 mb-2 text-gray-400 text-xs">
            <div v-for="i in Math.floor(totalDuration)" :key="i" class="flex-1 text-right">
              {{ i }}s
            </div>
          </div>

          <div class="mb-2">
            <div class="text-white text-xs mb-1 flex items-center gap-1">
              <VideoIcon :size="14" />
              Video
            </div>
            <div class="relative h-16 bg-gray-700 rounded flex">
              <template v-for="(clip, index) in clips" :key="clip.id">
                <div
                  :style="{
                    width: (clip.duration / totalDuration * 100) + '%',
                  }"
                  class="relative h-full cursor-pointer group overflow-hidden"
                  @click="selectClip(clip)"
                >
                  <div class="absolute inset-0 flex">
                    <div
                      v-for="(thumb, thumbIndex) in clip.thumbnails"
                      :key="thumbIndex"
                      class="flex-1 h-full"
                      :style="{
                        backgroundImage: `url(${thumb})`,
                        backgroundSize: 'cover',
                        backgroundPosition: 'center',
                      }"
                    ></div>
                  </div>
                  <div class="absolute inset-0 bg-blue-500/20 opacity-0 group-hover:opacity-100 transition-opacity"></div>

                  <button
                    @click.stop="removeClip(index)"
                    class="absolute top-1 right-1 bg-red-500 hover:bg-red-600 text-white rounded-full p-1 opacity-0 group-hover:opacity-100 transition-opacity z-10"
                  >
                    <CloseIcon :size="12" />
                  </button>

                  <div class="absolute bottom-1 left-1 text-white text-[10px] bg-black/70 px-1 rounded">
                    {{ formatTime(clip.duration) }}
                  </div>
                </div>

                <div v-if="index < clips.length - 1" class="relative w-1 flex-shrink-0">
                  <div class="absolute inset-y-0 left-1/2 -translate-x-1/2 w-0.5 bg-white"></div>
                </div>
              </template>
            </div>
          </div>

          <TimelineTrack
            type="text"
            :items="textOverlays"
            :total-duration="totalDuration"
            @select="selectText"
          />

          <TimelineTrack
            type="image"
            :items="imageOverlays"
            :total-duration="totalDuration"
            @select="selectImage"
          />

          <TimelineTrack
            type="pipVideo"
            :items="pipVideoOverlays"
            :total-duration="totalDuration"
            @select="selectPipVideo"
         />

          <div
            v-if="totalDuration > 0"
            :style="{ left: (currentTime / totalDuration * 100) + '%' }"
            class="absolute top-0 bottom-0 w-0.5 bg-red-500 cursor-ew-resize z-10"
            @mousedown="startPlayheadDrag"
          >
            <div class="absolute top-0 left-1/2 -translate-x-1/2 w-3 h-3 bg-red-500 rounded-full"></div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onUnmounted } from 'vue';

import ChevronLeftIcon from 'vue-material-design-icons/ChevronLeft.vue';
import PlayIcon from 'vue-material-design-icons/Play.vue';
import PauseIcon from 'vue-material-design-icons/Pause.vue';
import VideoIcon from 'vue-material-design-icons/Video.vue';
import TextIcon from 'vue-material-design-icons/FormatText.vue';
import PlusIcon from 'vue-material-design-icons/Plus.vue';
import CloseIcon from 'vue-material-design-icons/Close.vue';
import ImageIcon from 'vue-material-design-icons/Image.vue';

// --- Sub-components ---
import TextEditor from './ReelVideoEditor/TextEditor.vue';
import ImageEditor from './ReelVideoEditor/ImageEditor.vue';
import PipVideoEditor from './ReelVideoEditor/PipVideoEditor.vue';
import TimelineTrack from './ReelVideoEditor/TimelineTrack.vue';

// --- Composables ---
import { useVideoExport } from '@/composables/useVideoExport';
import { usePreviewStyles } from '@/composables/usePreviewStyles';
import { useMediaUpload } from '@/composables/useMediaUpload';

// --- Types ---
import type {
  VideoClip,
  TextOverlay,
  ImageOverlay,
  PipVideoOverlay
} from '@/types/video-editor.types';

const emit = defineEmits<{
  back: [];
  done: [url: string];
}>();

// --- State ---
const previewVideoRef = ref<HTMLVideoElement | null>(null);
const renderCanvasRef = ref<HTMLCanvasElement | null>(null);
const timelineRef = ref<HTMLDivElement | null>(null);
const pipVideoRefs = new Map<string, HTMLVideoElement>();
const isPlaying = ref(false);
const currentTime = ref(0);
const currentClipIndex = ref(0);
const clips = ref<VideoClip[]>([]);
const textOverlays = ref<TextOverlay[]>([]);
const selectedText = ref<TextOverlay | null>(null);
const imageOverlays = ref<ImageOverlay[]>([]);
const selectedImage = ref<ImageOverlay | null>(null);
const pipVideoOverlays = ref<PipVideoOverlay[]>([]);
const selectedPipVideo = ref<PipVideoOverlay | null>(null);
let animationFrameId: number | null = null;

// --- Computed ---
const totalDuration = computed(() => {
  const dur = clips.value.reduce((sum, clip) => sum + clip.duration, 0);
  return dur > 0 ? dur : 1;
});

const currentPreviewVideo = computed(() => {
  if (clips.value.length === 0) return '';
  return clips.value[currentClipIndex.value]?.url || '';
});

const visibleTexts = computed(() => {
  return textOverlays.value.filter(text =>
    currentTime.value >= text.startTime && currentTime.value <= text.endTime
  );
});
const handleTimelineClick = (event: MouseEvent) => {
  if (!timelineRef.value) return;
  const rect = timelineRef.value.getBoundingClientRect();
  const clickX = event.clientX - rect.left + timelineRef.value.scrollLeft;
  const timelineWidth = timelineRef.value.scrollWidth;
  const clickTime = (clickX / timelineWidth) * totalDuration.value;
  currentTime.value = Math.min(Math.max(0, clickTime), totalDuration.value);

  // Sync video to new time
  syncVideoToCurrentTime();
};

const visibleImages = computed(() => {
  return imageOverlays.value.filter(image =>
    currentTime.value >= image.startTime && currentTime.value <= image.endTime
  );
});

const visiblePipVideos = computed(() => {
  return pipVideoOverlays.value.filter(video =>
    currentTime.value >= video.startTime && currentTime.value <= video.endTime
  );
});

// --- PiP Video Management ---
const setPipVideoRef = (el: HTMLVideoElement | null, id: string) => {
  if (el) {
    pipVideoRefs.set(id, el);
  } else {
    pipVideoRefs.delete(id);
  }
};

const syncPipVideos = () => {
  pipVideoOverlays.value.forEach(pipVideo => {
    const videoEl = pipVideoRefs.get(pipVideo.id);
    if (!videoEl) return;

    if (currentTime.value >= pipVideo.startTime && currentTime.value <= pipVideo.endTime) {
      const localTime = currentTime.value - pipVideo.startTime;
      const targetTime = Math.min(localTime, videoEl.duration);

      // Synchronizuj tylko jeśli różnica jest większa niż 0.1s
      if (Math.abs(videoEl.currentTime - targetTime) > 0.1) {
        videoEl.currentTime = targetTime;
      }

      if (isPlaying.value && videoEl.paused) {
        videoEl.play().catch(() => {});
      } else if (!isPlaying.value && !videoEl.paused) {
        videoEl.pause();
      }
    } else {
      // Jeśli nie powinno być widoczne, zapauzuj
      if (!videoEl.paused) {
        videoEl.pause();
      }
    }
  });
};

// --- Preview Styles (using composable) ---
const { getTextStyle, getTextContent, getImageStyle, getPipVideoStyle } = usePreviewStyles(currentTime);

const { isRendering, renderProgress, exportVideo } = useVideoExport(
  renderCanvasRef,
  clips,
  textOverlays,
  imageOverlays,
  pipVideoOverlays,
  totalDuration
);

const handleSave = async () => {
  isPlaying.value = false;
  await exportVideo((url) => emit('done', url));
};

// --- Media Upload (using composable) ---
const { handleAddVideo, handleAddImage, handleAddPipVideo } = useMediaUpload(
  clips,
  imageOverlays,
  pipVideoOverlays,
  currentTime,
  totalDuration,
  selectedImage,
  selectedPipVideo,
  selectedText
);

// --- Helper Functions ---
const clearAllSelections = () => {
  selectedText.value = null;
  selectedImage.value = null;
  selectedPipVideo.value = null;
};

const selectOverlay = <T extends { startTime: number }>(
  item: T,
  targetRef: { value: T | null }
) => {
  clearAllSelections();
  targetRef.value = item;
  currentTime.value = item.startTime;
  syncVideoToCurrentTime();
};

const updateOverlay = <T extends { id: string }>(
  updated: T,
  items: { value: T[] },
  selectedRef: { value: T | null }
) => {
  const index = items.value.findIndex((item: T) => item.id === updated.id);
  if (index !== -1) {
    items.value[index] = updated;
    selectedRef.value = updated;
  }
};

// --- Update/Delete Handlers ---
const updateSelectedText = (updatedText: TextOverlay) => {
  updateOverlay(updatedText, textOverlays, selectedText);
};

const updateSelectedImage = (updatedImage: ImageOverlay) => {
  updateOverlay(updatedImage, imageOverlays, selectedImage);
};

const updateSelectedPipVideo = (updatedVideo: PipVideoOverlay) => {
  updateOverlay(updatedVideo, pipVideoOverlays, selectedPipVideo);
};

const deleteImage = () => {
  if (selectedImage.value) {
    imageOverlays.value = imageOverlays.value.filter(img => img.id !== selectedImage.value!.id);
    selectedImage.value = null;
  }
};

const deletePipVideo = () => {
  if (selectedPipVideo.value) {
    pipVideoOverlays.value = pipVideoOverlays.value.filter(v => v.id !== selectedPipVideo.value!.id);
    selectedPipVideo.value = null;
  }
};

const deleteText = () => {
  if (selectedText.value) {
    textOverlays.value = textOverlays.value.filter(t => t.id !== selectedText.value!.id);
    selectedText.value = null;
  }
};

const addTextOverlay = () => {
  const newText: TextOverlay = {
    id: `text_${Date.now()}`,
    content: 'Nowy tekst',
    startTime: currentTime.value,
    endTime: Math.min(currentTime.value + 3, totalDuration.value),
    position: { x: 50, y: 50 },
    fontSize: 60,
    color: '#ffffff',
    fontWeight: 'bold',
    entryAnimation: 'fade-in',
    entryDuration: 0.5,
    exitAnimation: 'fade-out',
    exitDuration: 0.5,
  };
  textOverlays.value.push(newText);
  selectedText.value = newText;
};

// --- Timeline & Playback ---
const removeClip = (index: number) => {
  clips.value.splice(index, 1);
  if (currentClipIndex.value >= clips.value.length) currentClipIndex.value = Math.max(0, clips.value.length - 1);
};

// Funkcja synchronizująca główne video z currentTime
const syncVideoToCurrentTime = () => {
  if (!previewVideoRef.value || clips.value.length === 0) return;

  // Znajdź który clip powinien być aktywny
  let accumulated = 0;
  let targetClipIndex = 0;
  let localTime = 0;

  for (let i = 0; i < clips.value.length; i++) {
    const clip = clips.value[i];
    if (!clip) continue;

    if (currentTime.value < accumulated + clip.duration) {
      targetClipIndex = i;
      localTime = currentTime.value - accumulated;
      break;
    }
    accumulated += clip.duration;
  }

  // Zmień clip jeśli potrzeba
  if (currentClipIndex.value !== targetClipIndex) {
    currentClipIndex.value = targetClipIndex;
  }

  // Ustaw czas w video
  if (previewVideoRef.value) {
    previewVideoRef.value.currentTime = Math.max(0, localTime);
  }

  // Synchronizuj PiP videos
  syncPipVideos();
};

const selectClip = (clip: VideoClip) => {
  const index = clips.value.findIndex(c => c.id === clip.id);
  if (index !== -1) {
    currentClipIndex.value = index;
    // Jump to start of this clip (simplified)
    let acc = 0;
    for(let i=0; i<index; i++) {
      const currentClip = clips.value[i];
      if (currentClip) acc += currentClip.duration;
    }
    currentTime.value = acc;
    syncVideoToCurrentTime();
  }
};

// --- Selection Functions (using helper) ---
const selectText = (text: TextOverlay) => {
  selectOverlay(text, selectedText);
};

const selectImage = (image: ImageOverlay) => {
  selectOverlay(image, selectedImage);
};

const selectPipVideo = (video: PipVideoOverlay) => {
  selectOverlay(video, selectedPipVideo);
};

// --- Playhead Drag ---
const startPlayheadDrag = (event: MouseEvent) => {
  event.preventDefault();
  const wasPlaying = isPlaying.value;
  if (isPlaying.value) {
      previewVideoRef.value?.pause();
      isPlaying.value = false;
  }

  const handleDrag = (e: MouseEvent) => {
    if (!timelineRef.value || totalDuration.value === 0) return;
    const rect = timelineRef.value.getBoundingClientRect();
    const offsetX = e.clientX - rect.left;
    const newTime = (offsetX / rect.width) * totalDuration.value;
    currentTime.value = Math.max(0, Math.min(totalDuration.value, newTime));

    // Sync video to new time while dragging
    syncVideoToCurrentTime();
  };

  const stopDrag = () => {
    document.removeEventListener('mousemove', handleDrag);
    document.removeEventListener('mouseup', stopDrag);
    if (wasPlaying && previewVideoRef.value) {
        previewVideoRef.value.play();
        isPlaying.value = true;
        startAnimationLoop();
    }
  };
  document.addEventListener('mousemove', handleDrag);
  document.addEventListener('mouseup', stopDrag);
};

const togglePlayback = () => {
  if (!previewVideoRef.value) return;
  if (isPlaying.value) {
    previewVideoRef.value.pause();
    isPlaying.value = false;
    // Zapauzuj wszystkie PiP videos
    pipVideoRefs.forEach(videoEl => videoEl.pause());
    if (animationFrameId !== null) { cancelAnimationFrame(animationFrameId); animationFrameId = null; }
  } else {
    previewVideoRef.value.play();
    isPlaying.value = true;

    syncPipVideos();
    startAnimationLoop();
  }
};

const startAnimationLoop = () => {
  const updateFrame = () => {
    if (!isPlaying.value || !previewVideoRef.value) { animationFrameId = null; return; }

    let accumulated = 0;
    for(let i=0; i<currentClipIndex.value; i++) {
      const currentClip = clips.value[i];
      if (currentClip) accumulated += currentClip.duration;
    }

    currentTime.value = accumulated + previewVideoRef.value.currentTime;

    syncPipVideos();

    animationFrameId = requestAnimationFrame(updateFrame);
  };
  animationFrameId = requestAnimationFrame(updateFrame);
};

const handleVideoLoaded = () => {
  if (isPlaying.value) {
      previewVideoRef.value?.play();
      startAnimationLoop();
  }
};

const handleVideoEnded = () => {
  if (currentClipIndex.value < clips.value.length - 1) {
    currentClipIndex.value++;
    // Video element src changes via computed, onloadedmetadata fires, loop continues
  } else {
    isPlaying.value = false;
    currentClipIndex.value = 0;
    currentTime.value = 0;
    if (animationFrameId) cancelAnimationFrame(animationFrameId);
  }
};

const formatTime = (seconds: number) => {
  const mins = Math.floor(seconds / 60);
  const secs = Math.floor(seconds % 60);
  return `${mins}:${secs.toString().padStart(2, '0')}`;
};

onUnmounted(() => {
  if (animationFrameId !== null) cancelAnimationFrame(animationFrameId);
  clips.value.forEach(clip => URL.revokeObjectURL(clip.url));
});
</script>

<style scoped>
::-webkit-scrollbar { width: 8px; height: 8px; }
::-webkit-scrollbar-track { background: #1f2937; }
::-webkit-scrollbar-thumb { background: #4b5563; border-radius: 4px; }
</style>
