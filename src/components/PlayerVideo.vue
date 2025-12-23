<template>
  <div :class="isLightbox ? '': 'rounded-2xl'" class="relative w-full  overflow-hidden bg-[#e4e6eb] cursor-pointer shadow-md">
    <video
      ref="video"
      :src="url"
      class="w-full block "
      @timeupdate="updateProgress"
      @loadedmetadata="setDuration"
      @play="onPlay"
      @pause="onPause"
      @ended="onEnded"
      @click="handleClick"
    ></video>


    <div
      class="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2
             text-white text-[52px] w-[70px] h-[70px] rounded-full
             bg-black/30 flex items-center justify-center pointer-events-none
             transition-opacity duration-200"
      :class="showOverlay ? 'opacity-100' : 'opacity-0'"
    >
      {{ overlayIcon }}
    </div>


    <div class="absolute bottom-2 left-2 right-2 flex items-center gap-2 w-full pr-12">

      <div class="flex items-center gap-2">
        <span
          class="text-white text-lg cursor-pointer select-none"
          @click.stop="handleClick"
        >
          ▶
        </span>

        <div v-if="isLightbox" class="text-white text-xs select-none">
          {{ formattedTime }}
        </div>
      </div>


      <div
        class="flex-1 h-2 bg-white/30 rounded-lg cursor-pointer relative"
        @click.stop="seek($event)"
      >
        <div
          class="h-full bg-blue-500 rounded-lg"
          :style="{ width: progress + '%' }"
        ></div>
      </div>

      <div v-if="settings" class="relative flex items-center justify-center ml-2">

         <div v-if="showSettings" class="absolute bottom-10 right-[-60px] md:right-0 bg-black/90 text-white rounded-lg py-2 min-w-[280px] text-sm z-20 shadow-xl select-none backdrop-blur-sm overflow-hidden" @click.stop>

             <div v-if="settingsView === 'main'" class="flex flex-col">
                 <div @click="settingsView = 'quality'" class="flex justify-between items-center px-4 py-3 hover:bg-white/10 cursor-pointer transition-colors">
                    <div class="flex items-center gap-2">
                        <Tune variant="outline" class="text-white" :size="18" />
                        <span>Jakość</span>
                    </div>
                    <div class="flex items-center gap-1 text-gray-300 text-xs font-medium">
                        <span>{{ currentQuality }}</span>
                        <ChevronRight :size="16" />
                    </div>
                 </div>

                 <div @click="settingsView = 'speed'" class="flex justify-between items-center px-4 py-3 hover:bg-white/10 cursor-pointer transition-colors">
                    <div class="flex items-center gap-2">
                         <Speedometer class="text-white" :size="18" />
                        <span>Szybkość odtwarzania</span>
                    </div>
                    <div class="flex items-center gap-1 text-gray-300 text-xs font-medium">
                        <span v-if="currentSpeed === 1">Normalna</span>
                        <span v-else>{{ currentSpeed }}</span>
                        <ChevronRight :size="16" />
                    </div>
                 </div>
             </div>

             <div v-else-if="settingsView === 'speed'" class="flex flex-col">
                 <div class="flex items-center px-2 py-2 border-b border-white/10 mb-1">
                     <div @click="settingsView = 'main'" class="cursor-pointer p-1 hover:bg-white/10 rounded-full mr-1">
                         <ChevronLeft :size="20" />
                     </div>
                     <span class="font-medium">Szybkość odtwarzania</span>
                 </div>

                 <div
                    v-for="rate in playbackRates"
                    :key="rate"
                    @click="setPlaybackSpeed(rate)"
                    class="flex items-center px-4 py-2 hover:bg-white/10 cursor-pointer gap-3"
                 >
                    <div class="w-4 h-4 rounded-full border border-gray-400 flex items-center justify-center">
                        <div v-if="currentSpeed === rate" class="w-2 h-2 bg-white rounded-full"></div>
                    </div>
                    <span class="text-sm" :class="currentSpeed === rate ? 'font-bold' : 'font-normal'">
                        {{ rate === 1 ? 'Normalna' : rate }}
                    </span>
                 </div>
             </div>

             <div v-else-if="settingsView === 'quality'" class="flex flex-col">
                 <div class="flex items-center px-2 py-2 border-b border-white/10 mb-1">
                     <div @click="settingsView = 'main'" class="cursor-pointer p-1 hover:bg-white/10 rounded-full mr-1">
                         <ChevronLeft :size="20" />
                     </div>
                     <span class="font-medium">Jakość</span>
                 </div>

                 <div
                    v-for="quality in qualityOptions"
                    :key="quality"
                    @click="setQuality(quality)"
                    class="flex items-center px-4 py-2 hover:bg-white/10 cursor-pointer gap-3"
                 >
                    <div class="w-4 h-4 rounded-full border border-gray-400 flex items-center justify-center">
                        <div v-if="currentQuality === quality" class="w-2 h-2 bg-white rounded-full"></div>
                    </div>

                    <span class="text-sm" :class="currentQuality === quality ? 'font-bold' : 'font-normal'">
                        {{ quality }}
                    </span>

                    <span v-if="quality.includes('1080') || quality.includes('720')" class="ml-auto text-[10px] text-blue-400 font-bold border border-blue-400 px-1 rounded">
                      HD
                    </span>
                 </div>
             </div>

         </div>

         <Cog
            class="text-white cursor-pointer drop-shadow-md hover:scale-110 transition-transform hover:rotate-45 duration-300"
            :size="20"
            @click.stop="toggleSettings"
         />
      </div>

      <div v-if="isLightbox"
           class="text-white cursor-pointer text-xl ml-2"
           @click.stop="toggleFullscreen">
        ⛶
      </div>
    </div>


    <div class="absolute bottom-2 right-2 flex flex-col items-center"  @mouseenter="hoverVolume = true"
        @mouseleave="hoverVolume = false">

      <div
        class="relative w-3 h-12 mb-2 flex justify-center items-center"
        v-show="hoverVolume"
      >
        <input
          type="range"
          min="0"
          max="1"
          step="0.01"
          v-model="volume"
          @input="updateVolume"
          class="absolute w-12 h-3 -rotate-90 origin-center bg-blue-500  pointer-events-auto"
        />
      </div>


      <component
        :is="volumeIcon"
        class="w-6 h-6 text-white cursor-pointer"
        @click.stop="toggleMute"
      />
    </div>
  </div>
</template>

<script lang="ts" setup>
import { ref, computed } from 'vue';
import type { Ref } from 'vue';
import VolumeHigh from 'vue-material-design-icons/VolumeHigh.vue';
import VolumeMedium from 'vue-material-design-icons/VolumeMedium.vue';
import VolumeLow from 'vue-material-design-icons/VolumeLow.vue';
import VolumeOff from 'vue-material-design-icons/VolumeOff.vue';
import Cog from 'vue-material-design-icons/Cog.vue';
import ChevronRight from 'vue-material-design-icons/ChevronRight.vue';
import ChevronLeft from 'vue-material-design-icons/ChevronLeft.vue';
import Speedometer from 'vue-material-design-icons/Speedometer.vue';
import Tune from 'vue-material-design-icons/Tune.vue';

interface Props {
  url: string;
  lightbox?: boolean;
  settings?: boolean;
}
const props = defineProps<Props>();
const isLightbox = props.lightbox ?? false;


const video: Ref<HTMLVideoElement | null> = ref(null);

const duration: Ref<number> = ref(0);
const currentTime: Ref<number> = ref(0);
const ended: Ref<boolean> = ref(false);
const paused: Ref<boolean> = ref(true);

// --- SETTINGS STATE ---
const showSettings: Ref<boolean> = ref(false);
// Dodany typ 'quality'
const settingsView: Ref<'main' | 'speed' | 'quality'> = ref('main');

// Speed State
const currentSpeed: Ref<number> = ref(1);
const playbackRates = [0.5, 0.75, 1, 1.25, 1.5, 1.75, 2];

// Quality State (NEW)
const currentQuality: Ref<string> = ref('Automatycznie');
const qualityOptions = ['1080p', '720p', '480p', '360p', 'Automatycznie'];


const volume: Ref<number> = ref(1);
const previousVolume: Ref<number> = ref(1);
const hoverVolume: Ref<boolean> = ref(false);


const volumeIcon = computed(() => {
  if (volume.value == 0) return VolumeOff;
  if (volume.value < 0.4) return VolumeLow;
  if (volume.value < 0.8) return VolumeMedium;
  return VolumeHigh;
});


const showOverlay = computed(() => paused.value || ended.value);
const overlayIcon = computed(() => {
  if (ended.value) return '↺';
  return paused.value ? '▶' : '⏸';
});


const progress = computed(() =>
  duration.value ? (currentTime.value / duration.value) * 100 : 0
);


const formattedTime = computed(() => {
  const elapsed = formatTime(currentTime.value);

  return `${elapsed} / ${formatTime(duration.value)}`;
});

function formatTime(seconds: number) {
  const m = Math.floor(seconds / 60);
  const s = Math.floor(seconds % 60);
  return `${m.toString().padStart(2,'0')}:${s.toString().padStart(2,'0')}`;
}


function handleClick(): void {
  if (showSettings.value) {
    showSettings.value = false;
    settingsView.value = 'main';
    return;
  }

  if (!video.value) return;

  if (ended.value) {
    ended.value = false;
    video.value.currentTime = 0;
    video.value.play();
    return;
  }

  if (video.value.paused) video.value.play();
  else video.value.pause();
}

function onPlay(): void { paused.value = false; }
function onPause(): void { paused.value = true; }
function updateProgress(): void { if(video.value) currentTime.value = video.value.currentTime; }
function setDuration(): void { if(video.value) duration.value = video.value.duration; }
function onEnded(): void { ended.value = true; paused.value = true; }
function seek(event: MouseEvent): void {
  if (!video.value) return;
  const rect = (event.target as HTMLElement).getBoundingClientRect();
  const pos = (event.clientX - rect.left) / rect.width;
  video.value.currentTime = pos * duration.value;
  ended.value = false;
  video.value.play();
}

// --- SETTINGS LOGIC ---
function toggleSettings(): void {
  showSettings.value = !showSettings.value;
  if (!showSettings.value) {
      setTimeout(() => settingsView.value = 'main', 200);
  }
}

function setPlaybackSpeed(rate: number): void {
    currentSpeed.value = rate;
    if (video.value) {
        video.value.playbackRate = rate;
    }
    // settingsView.value = 'main'; // Opcjonalnie powrót
}

// Funkcja ustawiania jakości (Mockup)
function setQuality(quality: string): void {
    currentQuality.value = quality;
    // Tutaj normalnie nastąpiłaby zmiana źródła wideo (src)
    // settingsView.value = 'main'; // Opcjonalnie powrót
}

function updateVolume(): void {
  if (!video.value) return;
  video.value.volume = volume.value;
  if (volume.value > 0) previousVolume.value = volume.value;
}

function toggleMute(): void {
  if (volume.value > 0) {
    previousVolume.value = volume.value;
    volume.value = 0;
  } else {
    volume.value = previousVolume.value || 1;
  }
  updateVolume();
}


function toggleFullscreen(): void {
  if (!video.value) return;
  if (!document.fullscreenElement) {
    video.value.parentElement?.requestFullscreen();
  } else {
    document.exitFullscreen();
  }
}
</script>
