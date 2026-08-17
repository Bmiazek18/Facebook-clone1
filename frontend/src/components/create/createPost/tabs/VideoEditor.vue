<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import { FFmpeg } from '@ffmpeg/ffmpeg'
import { fetchFile, toBlobURL } from '@ffmpeg/util'
import type { LogEvent } from '@ffmpeg/ffmpeg/types/events'
import { useCreatePostStore } from '@/stores/createPost'
import { storeToRefs } from 'pinia'



// Ikony z paczki
import Play from 'vue-material-design-icons/Play.vue'
import Pause from 'vue-material-design-icons/Pause.vue'
import ContentCut from 'vue-material-design-icons/ContentCut.vue'
import ClosedCaption from 'vue-material-design-icons/ClosedCaption.vue'

// --- STAN AKTYWNEJ ZAKŁADKI ---
type TabType = 'trim' | 'subtitles' | 'audio' | 'transcription'
const activeTab = ref<TabType>('trim')

const menuItems = [
  { id: 'trim' as TabType, label: 'Skróć film', icon: ContentCut, hasArrow: false },
  { id: 'subtitles' as TabType, label: 'Napisy', icon: ClosedCaption, hasArrow: true },
]

// --- KONFIGURACJA FFMPEG ---
const baseURL = 'https://cdn.jsdelivr.net/npm/@ffmpeg/core@0.12.10/dist/esm'
const createPostStore = useCreatePostStore()
const videoToEdit = computed(() => createPostStore.uiState.videoToEdit)

const videoPlayer = ref<HTMLVideoElement | null>(null)
const timelineRef = ref<HTMLDivElement | null>(null)
const isPlaying = ref(false)
const duration = ref(0)
const currentTime = ref(0)
const frames = ref<string[]>([])
const isProcessing = ref(false)
const message = ref('')

const ffmpeg = new FFmpeg()
const range = reactive({ start: 0, end: 10 })
const dragging = ref<'start' | 'end' | null>(null)

const leftPercent = computed(() => (duration.value ? (range.start / duration.value) * 100 : 0))
const widthPercent = computed(() =>
  duration.value ? ((range.end - range.start) / duration.value) * 100 : 0,
)

// Obliczanie pozycji pionowej białej kreski
const currentPlayPercent = computed(() => {
  if (!duration.value) return 0
  const boundedTime = Math.max(range.start, Math.min(range.end, currentTime.value))
  return (boundedTime / duration.value) * 100
})

// --- OBSŁUGA PLIKU SRT ---
const handleSrtUpload = (event: Event) => {
  const target = event.target as HTMLInputElement
  if (target.files && target.files[0]) {
    const file = target.files[0]
    console.log('Wybrany plik napisów:', file.name)
  }
}

// --- DRAG & DROP SZEWÓW OSI ---
const startDrag = (type: 'start' | 'end') => {
  dragging.value = type
  document.body.style.cursor = 'ew-resize'
  window.addEventListener('mousemove', handleDrag)
  window.addEventListener('mouseup', stopDrag)
}

const handleDrag = (e: MouseEvent) => {
  if (!dragging.value || !timelineRef.value || duration.value === 0) return
  const rect = timelineRef.value.getBoundingClientRect()
  const offsetX = e.clientX - rect.left
  let newTime = (offsetX / rect.width) * duration.value
  newTime = Math.max(0, Math.min(duration.value, newTime))
  const minGap = 0.5

  if (dragging.value === 'start') {
    range.start = Math.min(newTime, range.end - minGap)
    if (videoPlayer.value) videoPlayer.value.currentTime = range.start
  } else {
    range.end = Math.max(newTime, range.start + minGap)
    if (videoPlayer.value) videoPlayer.value.currentTime = range.end
  }
}

const stopDrag = () => {
  dragging.value = null
  document.body.style.cursor = ''
  window.removeEventListener('mousemove', handleDrag)
  window.removeEventListener('mouseup', stopDrag)

  // Po zakończeniu przesuwania ustaw odtwarzacz i kreskę na początku lewego wskaźnika
  if (videoPlayer.value) {
    videoPlayer.value.currentTime = range.start
    currentTime.value = range.start
  }
}

// Kliknięcie wewnątrz osi czasu
const setTimelinePos = (e: MouseEvent) => {
  if (dragging.value || !timelineRef.value || duration.value === 0) return
  const rect = timelineRef.value.getBoundingClientRect()
  const offsetX = e.clientX - rect.left
  let newTime = (offsetX / rect.width) * duration.value

  newTime = Math.max(range.start, Math.min(range.end, newTime))
  if (videoPlayer.value) {
    videoPlayer.value.currentTime = newTime
  }
}

// --- KLATKI PODGLĄDU ---
const generateFrames = async () => {
  if (!duration.value || !videoToEdit.value) return

  const tempVideo = document.createElement('video')
  tempVideo.crossOrigin = 'anonymous'
  tempVideo.src = videoToEdit.value
  tempVideo.muted = true

  await new Promise((resolve) => {
    tempVideo.onloadedmetadata = () => resolve(true)
    setTimeout(() => resolve(false), 5000)
  })

  const canvas = document.createElement('canvas')
  const ctx = canvas.getContext('2d')
  const frameCount = 50
  const interval = duration.value / frameCount

  frames.value = []
  canvas.width = 100
  canvas.height = 56

  for (let i = 0; i < frameCount; i++) {
    tempVideo.currentTime = i * interval
    await new Promise((r) => (tempVideo.onseeked = r))
    try {
      if (ctx) {
        ctx.drawImage(tempVideo, 0, 0, canvas.width, canvas.height)
        frames.value.push(canvas.toDataURL('image/jpeg', 0.5))
      }
    } catch {
      // ignore
    }
  }
}

const onMetadataLoaded = () => {
  if (videoPlayer.value) {
    duration.value = videoPlayer.value.duration
    range.end = videoPlayer.value.duration
    currentTime.value = videoPlayer.value.currentTime
    generateFrames()
  }
}

const togglePlay = () => {
  if (!videoPlayer.value) return
  if (isPlaying.value) videoPlayer.value.pause()
  else videoPlayer.value.play()
  isPlaying.value = !isPlaying.value
}

const onTimeUpdate = () => {
  if (videoPlayer.value) {
    currentTime.value = videoPlayer.value.currentTime

    if (isPlaying.value && !dragging.value) {
      if (
        videoPlayer.value.currentTime >= range.end ||
        videoPlayer.value.currentTime < range.start
      ) {
        videoPlayer.value.currentTime = range.start
      }
    }
  }
}

const transcode = async () => {
  isProcessing.value = true
  message.value = 'Ładowanie silnika FFmpeg...'

  try {
    ffmpeg.on('log', ({ message: msg }: LogEvent) => {
      console.log(msg)
      message.value = msg
    })

    if (!ffmpeg.loaded) {
      await ffmpeg.load({
        coreURL: await toBlobURL(`${baseURL}/ffmpeg-core.js`, 'text/javascript'),
        wasmURL: await toBlobURL(`${baseURL}/ffmpeg-core.wasm`, 'application/wasm'),
        workerURL: await toBlobURL(`${baseURL}/ffmpeg-core.worker.js`, 'text/javascript'),
      })
    }

    message.value = 'Pobieranie pliku wideo...'

    if (!videoToEdit.value) {
      throw new Error('No video to transcode.')
    }
    await ffmpeg.writeFile('input.mp4', await fetchFile(videoToEdit.value))

    message.value = 'Przycinanie wideo...'
    await ffmpeg.exec([
      '-ss',
      range.start.toString(),
      '-i',
      'input.mp4',
      '-t',
      (range.end - range.start).toString(),
      '-c',
      'copy',
      'output.mp4',
    ])

    const data = await ffmpeg.readFile('output.mp4')
    const url = URL.createObjectURL(new Blob([(data as Uint8Array).buffer], { type: 'video/mp4' }))
    createPostStore.saveEditedMedia(url)
    message.value = 'Gotowe!'
  } catch (error: unknown) {
    console.error('FFmpeg Error:', error)
    if (error instanceof Error) message.value = `Błąd: ${error.message}`
  } finally {
    isProcessing.value = false
  }
}
</script>

<template>
  <div class="flex h-[90vh] w-full bg-[#2c2c2c] overflow-hidden   select-none">
    <aside
      class="w-85 bg-white flex flex-col justify-between shadow-xl z-20 shrink-0 overflow-y-auto"
    >
      <div class="flex flex-col">
        <template v-for="item in menuItems" :key="item.id">
          <div
            @click="activeTab = item.id"
            :class="[
              'flex items-center justify-between cursor-pointer py-2.5 px-3 rounded-2xl transition-all duration-200',
              activeTab === item.id && item.id === 'subtitles'
                ? 'bg-[#eef4ff]'
                : 'hover:bg-gray-50',
            ]"
          >
            <div class="flex items-center gap-4">
              <div
                :class="[
                  'p-3 rounded-full flex items-center justify-center transition-colors w-[48px] h-[48px]',
                  activeTab === item.id && item.id === 'subtitles'
                    ? 'bg-[#1a5cff] text-white'
                    : 'bg-[#eef0f4] text-[#1c1e21]',
                ]"
              >
                <component v-if="!item.isCustomIcon" :is="item.icon" :size="22" />
                <svg
                  v-else
                  class="w-5 h-5"
                  fill="none"
                  stroke="currentColor"
                  stroke-width="2.5"
                  viewBox="0 0 24 24"
                >
                  <path
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    d="M3.75 6.75h16.5M3.75 12h12M3.75 17.25h16.5"
                  />
                </svg>
              </div>
              <span class="font-bold text-[#1c1e21] text-[16px] tracking-wide">{{
                item.label
              }}</span>
            </div>

            <svg
              v-if="item.hasArrow"
              :class="[
                'w-5 h-5 text-[#52575c] transition-transform duration-200 stroke-[3]',
                activeTab === item.id ? 'rotate-180 text-black' : '',
              ]"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                d="M19.5 8.25l-7.5 7.5-7.5-7.5"
              />
            </svg>
          </div>

          <div
            v-if="item.id === 'subtitles' && activeTab === 'subtitles'"
            class="pl-3 pr-1 py-2 flex items-center justify-between transition-all"
          >
            <div class="flex flex-col">
              <span class="font-bold text-black text-[16px]">Dodaj napisy ręcznie</span>
              <span class="text-gray-500 text-xs">Użyj plików Subrip (.srt)</span>
            </div>

            <label
              class="bg-[#e2e5ed] hover:bg-gray-300 active:bg-gray-400 text-black font-bold px-4 py-2 rounded-xl cursor-pointer transition-colors flex items-center gap-2 text-[14px]"
            >
              <svg
                class="w-4 h-4 stroke-[2.5]"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  d="M3 16.5v2.25A2.25 2.25 0 005.25 21h13.5A2.25 2.25 0 0021 18.75V16.5m-13.5-9L12 3m0 0l4.5 4.5M12 3v13.5"
                />
              </svg>
              Prześlij
              <input type="file" accept=".srt" class="hidden" @change="handleSrtUpload" />
            </label>
          </div>
        </template>
      </div>

      <div class="flex flex-col gap-3 mt-auto">
        <div
          v-if="message"
          class="p-2.5 bg-gray-50 rounded-lg text-[11px] text-gray-500 font-mono break-all max-h-16 overflow-y-auto border border-gray-100"
        >
          {{ message }}
        </div>
        <button
          @click="transcode"
          :disabled="isProcessing"
          class="w-full bg-[#1a5cff] hover:bg-[#0044ff] active:bg-[#003be5] text-white font-bold py-3.5 rounded-xl text-center transition-all tracking-wide text-sm shadow-sm disabled:opacity-50 flex items-center justify-center gap-2"
        >
          <span
            v-if="isProcessing"
            class="animate-spin inline-block w-4 h-4 border-2 border-white rounded-full border-t-transparent"
          ></span>
          Gotowe
        </button>
      </div>
    </aside>

    <main class="flex-1 flex flex-col bg-[#2a2a2a] relative min-w-0 py-[32px] px-[80px]">
      <div class="flex-1 flex justify-center items-center p-4 overflow-hidden">
        <div class="relative w-full h-full flex items-center justify-center overflow-hidden">
          <video
            ref="videoPlayer"
            :src="videoToEdit"
            crossorigin="anonymous"
            @loadedmetadata="onMetadataLoaded"
            @timeupdate="onTimeUpdate"
            @click="togglePlay"
            class="w-full h-full object-contain"
          ></video>
        </div>
      </div>

      <div
        v-if="activeTab === 'trim'"
        class="h-12 bg-black flex items-center px-8 rounded-lg gap-5 z-10"
      >
        <button
          @click="togglePlay"
          class="shrink-0 w-11 h-11 flex items-center justify-center cursor-pointer text-white"
        >
          <Pause v-if="isPlaying" :size="25" />
          <Play v-else :size="25" />
        </button>

        <div
          ref="timelineRef"
          @mousedown="setTimelinePos"
          class="relative flex-1 h-14 bg-black rounded-xl cursor-pointer border border-gray-800 overflow-hidden"
        >
          <div class="absolute inset-y-0 left-0 right-0 flex opacity-60 pointer-events-none">
            <template v-if="frames.length > 0">
              <div v-for="(frame, i) in frames" :key="i" class="flex-1 h-full relative bg-black">
                <img v-if="frame" :src="frame" class="w-full h-full object-cover" />
              </div>
            </template>
            <div
              v-else
              class="w-full h-full flex items-center justify-center text-gray-500 text-xs"
            >
              Generowanie podglądu...
            </div>
          </div>

          <div
            class="absolute top-0 bottom-0 z-10 border-t-[3px] border-b-[3px] border-[#1a5cff]"
            :style="{ left: `${leftPercent}%`, width: `${widthPercent}%` }"
          >
            <div class="absolute inset-0 bg-[#1a5cff] opacity-5 pointer-events-none"></div>

            <div
              @mousedown.stop.prevent="startDrag('start')"
              class="absolute left-0 top-0 bottom-0 w-3.5 -translate-x-1/2 bg-[#1a5cff] cursor-ew-resize flex items-center justify-center rounded-l-md hover:brightness-110 z-30 pointer-events-auto"
            >
              <div class="h-4 w-0.5 bg-white rounded-full opacity-80"></div>
            </div>

            <div
              @mousedown.stop.prevent="startDrag('end')"
              class="absolute right-0 top-0 bottom-0 w-3.5 translate-x-1/2 bg-[#1a5cff] cursor-ew-resize flex items-center justify-center rounded-r-md hover:brightness-110 z-30 pointer-events-auto"
            >
              <div class="h-4 w-0.5 bg-white rounded-full opacity-80"></div>
            </div>
          </div>

          <div
            v-if="!dragging"
            class="absolute top-0 bottom-0 w-[3px] bg-white z-20 pointer-events-none shadow-[0_0_8px_rgba(255,255,255,0.8)] transition-all ease-linear duration-75"
            :style="{ left: `${currentPlayPercent}%` }"
          ></div>
        </div>

        <div
          class="flex items-center justify-center min-w-[55px] text-gray-300 text-sm font-medium"
        >
          {{ (range.end - range.start).toFixed(1) }}s
        </div>
      </div>
    </main>
  </div>
</template>

<style scoped>
.select-none {
  user-select: none;
  -webkit-user-select: none;
}
</style>
