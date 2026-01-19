<script setup lang="ts">
import { computed } from 'vue';
import PlayIcon from 'vue-material-design-icons/Play.vue';
import PauseIcon from 'vue-material-design-icons/Pause.vue';
import type { AudioMessage, AudioState } from '@/types/Message';

const props = defineProps<{
  message: AudioMessage;
  audioState: AudioState;
}>();

const emit = defineEmits<{
  (e: 'togglePlayback'): void;
}>();

const formatSeconds = (seconds: number): string => {
  if (isNaN(seconds) || seconds < 0) return '0:00';
  const minutes = Math.floor(seconds / 60);
  const secs = Math.floor(seconds % 60);
  return `${minutes}:${secs.toString().padStart(2, '0')}`;
};

// Paski wizualizera
const visualizerBars = [10, 20, 14, 25, 20, 15, 20, 10];
// Punkty procentowe, po których dany pasek powinien 'zaświecić się'
const visualizerThresholds = [0, 12, 25, 37, 50, 62, 75, 87];

// Właściwości obliczane na podstawie stanu audio
const isPlaying = computed(() => props.audioState?.isPlaying ?? false);
const currentTime = computed(() => props.audioState?.currentTime ?? 0);
const duration = computed(() => props.message.duration || 1);

// Właściwość obliczana: Procent odtwarzania (dla białej linii postępu)
const progressPercent = computed(() => {
    return duration.value > 0 ? (currentTime.value / duration.value) * 100 : 0;
});

</script>

<template>
  <div

          class="flex items-center w-full min-w-[180px] space-x-2 p-2 rounded-full h-10"
          :class="{
            'bg-purple-600 text-white rounded-tl-none': message.sender === 'other',
            'bg-blue-500 text-white rounded-br-none': message.sender === 'me'
          }"
        >
          <audio
            :src="message.audioUrl"
            class="hidden"
            :id="`audio-${message.id}`"
            preload="metadata"
          ></audio>

          <button
            @click="emit('togglePlayback')"
            class="w-7 h-7 rounded-full bg-white flex items-center justify-center shrink-0"
            :class="{
              'text-purple-600': message.sender === 'other',
              'text-blue-500': message.sender === 'me'
            }"
          >
            <template v-if="isPlaying">
              <PauseIcon :size="16" />
            </template>
            <template v-else>
              <PlayIcon :size="16" />
            </template>
          </button>

          <div class="grow h-6 relative overflow-hidden rounded-full cursor-pointer">
            <div class="absolute inset-0 flex items-center justify-between space-x-0.5 h-full px-1">
              <div
                v-for="(height, index) in visualizerBars"
                :key="index"
                class="rounded-full shrink-0 z-10 transition-colors duration-300"
                :style="{
                  height: height + 'px',
                  width: '3px',
                  backgroundColor: isPlaying
                    ? (progressPercent > (visualizerThresholds[index] ?? 0) + 6 ? 'white' : 'rgba(255,255,255,0.5)')
                    : 'white'
                }"
              ></div>

              <div
                v-if="isPlaying"
                class="absolute top-0 bottom-0 w-[3px] bg-white z-20 transition-left duration-100"
                :style="{
                  left: progressPercent + '%'
                }"
              ></div>
            </div>
          </div>

          <span class="text-xs font-semibold">
            <template v-if="isPlaying">
              {{ formatSeconds(currentTime) }}
            </template>
            <template v-else>
              {{ formatSeconds(message.duration) }}
            </template>
          </span>
        </div>
</template>
