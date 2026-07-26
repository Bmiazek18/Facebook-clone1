<script setup lang="ts">
import { computed } from 'vue'
import PlayIcon from 'vue-material-design-icons/Play.vue'
import PauseIcon from 'vue-material-design-icons/Pause.vue'
import type { Message, AudioState } from '@/types/Message'
import { useAudioPlayer } from '@/composables/media/useAudioPlayer' // Added

const props = defineProps<{
  message: Message
  boxId: string | number | undefined // Changed from injectedBoxId
  bubbleColor: string
  // removed injectedAudioStates
}>()

// Use useAudioPlayer directly
const { audioStates, toggleAudioPlayback, seekAudio } = useAudioPlayer(props.boxId)

// Update toggleAudio to use the local toggleAudioPlayback
const toggleAudio = () => toggleAudioPlayback(props.message)

const handleSeek = (event: MouseEvent) => {
  const progressBar = event.currentTarget as HTMLElement
  const clickX = event.clientX - progressBar.getBoundingClientRect().left
  const progressBarWidth = progressBar.offsetWidth
  const seekPercentage = clickX / progressBarWidth
  const newTime = (props.message.duration || 0) * seekPercentage
  seekAudio(props.message, newTime)
}

const visualizerBars = [10, 20, 14, 25, 20, 15, 20, 10]
const VISUALIZER_THRESHOLDS = [0, 12, 25, 37, 50, 62, 75, 87]

const formatSeconds = (seconds: number): string => {
  if (isNaN(seconds) || seconds < 0) return '0:00'
  const m = Math.floor(seconds / 60)
  const s = Math.floor(seconds % 60)
  return `${m}:${s.toString().padStart(2, '0')}`
}

const getAudioBarStyle = (msgId: number, index: number, duration: number) => {
  const state = audioStates && audioStates.value[msgId] // Add check for audioStates.value
  const isPlaying = state?.isPlaying || state?.currentTime > 0
  const progressPercent = state ? (state.currentTime / (duration || 1)) * 100 : 0
  const threshold = VISUALIZER_THRESHOLDS[index] ?? 0
  const isActive = isPlaying && progressPercent > threshold + 6

  return {
    height: `${visualizerBars[index]}px`,
    width: '3px',
    backgroundColor: isActive || !isPlaying ? 'white' : 'rgba(255,255,255,0.5)',
  }
}

const getPlaybackIndicatorStyle = (msgId: number, duration: number) => {
  const state = audioStates.value && audioStates.value[msgId] // Add check for audioStates.value
  const leftPos = state ? (state.currentTime / (duration || 1)) * 100 : 0
  return { left: `${leftPos}%` }
}
</script>

<template>
  <div
    class="flex items-center w-full min-w-[200px] space-x-3 p-2.5 rounded-full h-12 shadow-sm transition-colors"
    :style="{ backgroundColor: bubbleColor }"
  >
    <audio
      :src="message.audioUrl"
      class="hidden"
      :id="`audio-${props.boxId ?? '0'}-${message.id}`"
      preload="metadata"
    ></audio>

    <button
      @click="toggleAudio()"
      class="w-8 h-8 rounded-full bg-white flex items-center justify-center shrink-0 shadow-sm hover:scale-105 transition-transform"
      :style="{ color: bubbleColor }"
    >
      <PauseIcon v-if="audioStates[message.id]?.isPlaying" :size="18" />
      <PlayIcon v-else :size="18" class="ml-0.5" />
    </button>

    <div
      class="grow h-8 relative overflow-hidden flex items-center cursor-pointer"
      @click="handleSeek($event)"
    >
      <div class="flex items-center justify-between space-x-[2px] w-full px-1">
        <div
          v-for="(height, idx) in visualizerBars"
          :key="idx"
          class="rounded-full shrink-0 transition-colors duration-200"
          :style="getAudioBarStyle(message.id, idx, message.duration)"
        ></div>
      </div>
      <div
        v-if="audioStates[message.id]?.isPlaying || audioStates[message.id]?.currentTime > 0"
        class="absolute top-0 bottom-0 w-[2px] bg-white/80 shadow-[0_0_5px_rgba(255,255,255,0.8)] transition-all duration-100 ease-linear"
        :style="getPlaybackIndicatorStyle(message.id, message.duration)"
      ></div>
    </div>

    <span class="text-xs font-bold tabular-nums pr-2 select-none min-w-[35px] text-right">
      {{
        formatSeconds(
          audioStates &&
            (audioStates[message.id]?.isPlaying || audioStates[message.id]?.currentTime > 0)
            ? audioStates[message.id]?.currentTime || 0
            : message.duration,
        )
      }}
    </span>
  </div>
</template>
