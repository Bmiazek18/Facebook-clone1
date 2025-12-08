<template>
  <div class="relative w-full rounded-2xl overflow-hidden bg-[#e4e6eb] cursor-pointer shadow-md">
    <video
      ref="video"
      :src="url"
      class="w-full block rounded-2xl"
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

interface Props {
  url: string;
  lightbox?: boolean;
}
const props = defineProps<Props>();
const isLightbox = props.lightbox ?? false;


const video: Ref<HTMLVideoElement | null> = ref(null);

const duration: Ref<number> = ref(0);
const currentTime: Ref<number> = ref(0);
const ended: Ref<boolean> = ref(false);
const paused: Ref<boolean> = ref(true);


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
  const remaining = formatTime(duration.value - currentTime.value);
  return `${elapsed} / ${remaining}`;
});

function formatTime(seconds: number) {
  const m = Math.floor(seconds / 60);
  const s = Math.floor(seconds % 60);
  return `${m.toString().padStart(2,'0')}:${s.toString().padStart(2,'0')}`;
}


function handleClick(): void {
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
