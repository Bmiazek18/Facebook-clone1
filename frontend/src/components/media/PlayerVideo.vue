<template>
  <div
    :class="isLightbox ? '' : 'rounded-2xl'"
    class="relative w-full overflow-hidden flex justify-center bg-black cursor-pointer shadow-md select-none group"
  >
    <video
      ref="video"
      :src="url"
      :class="props.isSingleVideo ? 'max-w-[max(412.5px,calc(-243.75px+75vh))]' : 'h-full'"
      class="w-full h-full block object-cover"
      @timeupdate="updateProgress"
      @loadedmetadata="setDuration"
      @play="onPlay"
      @pause="onPause"
      @ended="onEnded"
      @click="handleClick"
    ></video>

    <!-- Overlay Icon -->
    <div
      class="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 text-white text-[52px] w-[70px] h-[70px] rounded-full bg-black/40 backdrop-blur-sm flex items-center justify-center pointer-events-none transition-opacity duration-200"
      :class="showOverlay ? 'opacity-100' : 'opacity-0'"
    >
      {{ overlayIcon }}
    </div>

    <!-- Pasek kontrolny na dole -->
    <div class="absolute bottom-2 left-3 right-3 flex items-center gap-3">
      <!-- Przycisk Play / Czas -->
      <div class="flex items-center gap-2">
        <span class="text-white text-lg cursor-pointer hover:scale-110 transition-transform" @click.stop="handleClick">
          {{ paused || ended ? '▶' : '⏸' }}
        </span>

        <div v-if="isLightbox" class="text-white text-xs font-medium tracking-wide">
          {{ formattedTime }}
        </div>
      </div>

      <!-- Pasek postępu -->
      <div
        class="flex-1 h-1.5 hover:h-2 bg-white/30 rounded-full cursor-pointer relative transition-all duration-150"
        @click.stop="seek($event)"
      >
        <div class="h-full bg-blue-500 rounded-full relative" :style="{ width: progress + '%' }">
          <div class="absolute right-0 top-1/2 -translate-y-1/2 w-3 h-3 bg-white rounded-full shadow opacity-0 group-hover:opacity-100 transition-opacity"></div>
        </div>
      </div>

      <!-- Ustawienia -->
      <div v-if="settings" class="relative flex items-center justify-center">
        <div
          v-if="showSettings"
          class="absolute bottom-10 right-0 bg-black/90 text-white rounded-xl py-2 min-w-[240px] text-sm z-20 shadow-2xl border border-white/10 backdrop-blur-md overflow-hidden"
          @click.stop
        >
          <div v-if="settingsView === 'main'" class="flex flex-col">
            <div
              @click="settingsView = 'quality'"
              class="flex justify-between items-center px-4 py-2.5 hover:bg-white/10 cursor-pointer transition-colors"
            >
              <div class="flex items-center gap-2">
                <Tune variant="outline" class="text-white" :size="18" />
                <span>{{ $t('media.jakosc') }}</span>
              </div>
              <div class="flex items-center gap-1 text-gray-300 text-xs font-medium">
                <span>{{ currentQuality }}</span>
                <ChevronRight :size="16" />
              </div>
            </div>

            <div
              @click="settingsView = 'speed'"
              class="flex justify-between items-center px-4 py-2.5 hover:bg-white/10 cursor-pointer transition-colors"
            >
              <div class="flex items-center gap-2">
                <Speedometer class="text-white" :size="18" />
                <span>{{ $t('media.szybkosc') }}</span>
              </div>
              <div class="flex items-center gap-1 text-gray-300 text-xs font-medium">
                <span v-if="currentSpeed === 1">{{ $t('media.normalna') }}</span>
                <span v-else>{{ $t('media.currentspeedX') }}</span>
                <ChevronRight :size="16" />
              </div>
            </div>
          </div>

          <div v-else-if="settingsView === 'speed'" class="flex flex-col">
            <div class="flex items-center px-2 py-2 border-b border-white/10 mb-1">
              <div
                @click="settingsView = 'main'"
                class="cursor-pointer p-1 hover:bg-white/10 rounded-full mr-1"
              >
                <ChevronLeft :size="20" />
              </div>
              <span class="font-medium text-xs uppercase tracking-wider text-gray-400">{{ $t('media.szybkoscOdtwarzania') }}</span>
            </div>

            <div
              v-for="rate in playbackRates"
              :key="rate"
              @click="setPlaybackSpeed(rate)"
              class="flex items-center px-4 py-2 hover:bg-white/10 cursor-pointer gap-3"
            >
              <div class="w-3.5 h-3.5 rounded-full border border-gray-400 flex items-center justify-center">
                <div v-if="currentSpeed === rate" class="w-2 h-2 bg-blue-500 rounded-full"></div>
              </div>
              <span class="text-sm" :class="currentSpeed === rate ? 'font-bold text-white' : 'text-gray-300'">
                {{ rate === 1 ? 'Normalna' : rate + 'x' }}
              </span>
            </div>
          </div>

          <div v-else-if="settingsView === 'quality'" class="flex flex-col">
            <div class="flex items-center px-2 py-2 border-b border-white/10 mb-1">
              <div
                @click="settingsView = 'main'"
                class="cursor-pointer p-1 hover:bg-white/10 rounded-full mr-1"
              >
                <ChevronLeft :size="20" />
              </div>
              <span class="font-medium text-xs uppercase tracking-wider text-gray-400">{{ $t('media.jakoscWideo') }}</span>
            </div>

            <div
              v-for="quality in qualityOptions"
              :key="quality"
              @click="setQuality(quality)"
              class="flex items-center px-4 py-2 hover:bg-white/10 cursor-pointer gap-3"
            >
              <div class="w-3.5 h-3.5 rounded-full border border-gray-400 flex items-center justify-center">
                <div v-if="currentQuality === quality" class="w-2 h-2 bg-blue-500 rounded-full"></div>
              </div>

              <span class="text-sm" :class="currentQuality === quality ? 'font-bold text-white' : 'text-gray-300'">
                {{ quality }}
              </span>

              <span
                v-if="quality.includes('1080') || quality.includes('720')"
                class="ml-auto text-[10px] text-blue-400 font-bold border border-blue-400/40 bg-blue-500/10 px-1.5 py-0.5 rounded"
              >{{ $t('media.hd') }}</span>
            </div>
          </div>
        </div>

        <Cog
          class="text-white cursor-pointer drop-shadow hover:scale-110 transition-transform hover:rotate-45 duration-300"
          :size="20"
          @click.stop="toggleSettings"
        />
      </div>

      <!-- Pełny ekran -->
      <div
        v-if="isLightbox"
        class="text-white cursor-pointer text-lg hover:scale-110 transition-transform flex items-center justify-center"
        @click.stop="toggleFullscreen"
      >
        ⛶
      </div>

    <!-- Sekcja głośności z pionowym suwakiem -->
<div
  class="relative flex items-center justify-center"
  @mouseenter="hoverVolume = true"
  @mouseleave="hoverVolume = false"
>
  <!-- Niewidzialny kontener z paddingiem tworzący "most" dla kursora -->
  <div
    v-show="hoverVolume"
    class="absolute bottom-full left-1/2 -translate-x-1/2 pb-3"
  >
    <!-- Właściwe, widoczne tło suwaka -->
    <div class="w-8 h-24 bg-black/80 backdrop-blur-md rounded-xl flex items-center justify-center pb-1 shadow-lg border border-white/10 transition-all">
      <div class="w-24 h-6 flex items-center justify-center -rotate-90">
        <input
          type="range"
          min="0"
          max="1"
          step="0.01"
          v-model="volume"
          @input="updateVolume"
          class="volume-slider w-20 h-1.5 rounded-lg appearance-none cursor-pointer"
          :style="{ background: `linear-gradient(to right, #3b82f6 ${volume * 100}%, rgba(255, 255, 255, 0.3) ${volume * 100}%)` }"
        />
      </div>
    </div>
  </div>

  <component
    :is="volumeIcon"
    class="w-5 h-5 text-white cursor-pointer hover:scale-110 transition-transform"
    @click.stop="toggleMute"
  />
</div>
    </div>
  </div>
</template>

<script lang="ts" setup>
// Twój dotychczasowy kod <script lang="ts" setup> pozostaje bez zmian!
import { ref, computed, watch, onMounted, onUnmounted } from 'vue'
import type { Ref } from 'vue'
import VolumeHigh from 'vue-material-design-icons/VolumeHigh.vue'
import VolumeMedium from 'vue-material-design-icons/VolumeMedium.vue'
import VolumeLow from 'vue-material-design-icons/VolumeLow.vue'
import VolumeOff from 'vue-material-design-icons/VolumeOff.vue'
import Cog from 'vue-material-design-icons/Cog.vue'
import ChevronRight from 'vue-material-design-icons/ChevronRight.vue'
import ChevronLeft from 'vue-material-design-icons/ChevronLeft.vue'
import Speedometer from 'vue-material-design-icons/Speedometer.vue'
import { useIntersectionObserver } from '@vueuse/core'
import { useImpressionTracker } from '@/composables/analytics/useImpressionTracker'

const config = useRuntimeConfig()

interface Props {
  url?: string
  lightbox?: boolean
  settings?: boolean
  isSingleVideo?: boolean
  postId?: string | number
  pageId?: string
}
const props = defineProps<Props>()
const isLightbox = props.lightbox ?? false

const { trackVideoProgress, trackVideoLoop, trackAudioToggle } = useImpressionTracker()
const trackedMilestones = new Set<number>()

const video: Ref<HTMLVideoElement | null> = ref(null)
let hlsInstance: any = null

const duration: Ref<number> = ref(0)
const currentTime: Ref<number> = ref(0)
const ended: Ref<boolean> = ref(false)
const paused: Ref<boolean> = ref(true)

const showSettings: Ref<boolean> = ref(false)
const settingsView: Ref<'main' | 'speed' | 'quality'> = ref('main')

const currentSpeed: Ref<number> = ref(1)
const playbackRates = [0.5, 0.75, 1, 1.25, 1.5, 1.75, 2]

const currentQuality: Ref<string> = ref('Automatycznie')
const qualityOptions: Ref<string[]> = ref(['1080p', '720p', '480p', '360p', 'Automatycznie'])

const volume: Ref<number> = ref(1)
const previousVolume: Ref<number> = ref(1)
const hoverVolume: Ref<boolean> = ref(false)

const volumeIcon = computed(() => {
  if (volume.value == 0) return VolumeOff
  if (volume.value < 0.4) return VolumeLow
  if (volume.value < 0.8) return VolumeMedium
  return VolumeHigh
})

const showOverlay = computed(() => paused.value || ended.value)
const overlayIcon = computed(() => {
  if (ended.value) return '↺'
  return paused.value ? '▶' : '⏸'
})

const progress = computed(() => (duration.value ? (currentTime.value / duration.value) * 100 : 0))

const formattedTime = computed(() => {
  const elapsed = formatTime(currentTime.value)
  return `${elapsed} / ${formatTime(duration.value)}`
})

function formatTime(seconds: number) {
  const m = Math.floor(seconds / 60)
  const s = Math.floor(seconds % 60)
  return `${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}`
}

function handleClick(): void {
  if (showSettings.value) {
    showSettings.value = false
    settingsView.value = 'main'
    return
  }

  if (!video.value) return

  if (ended.value) {
    ended.value = false
    video.value.currentTime = 0
    video.value.play()
    if (props.postId) {
      trackVideoLoop(String(props.postId), props.pageId, 1)
    }
    return
  }

  if (video.value.paused) video.value.play()
  else video.value.pause()
}

function onPlay(): void {
  paused.value = false
}
function onPause(): void {
  paused.value = true
}
function updateProgress(): void {
  if (video.value) {
    currentTime.value = video.value.currentTime
    if (props.postId && duration.value > 0) {
      const pct = (currentTime.value / duration.value) * 100
      const milestones = [25, 50, 75]
      for (const m of milestones) {
        if (pct >= m && !trackedMilestones.has(m)) {
          trackedMilestones.add(m)
          trackVideoProgress(String(props.postId), props.pageId, m)
        }
      }
    }
  }
}
function setDuration(): void {
  if (video.value) duration.value = video.value.duration
}
function onEnded(): void {
  ended.value = true
  paused.value = true
  if (props.postId) {
    trackVideoProgress(String(props.postId), props.pageId, 100)
    trackVideoLoop(String(props.postId), props.pageId, 1)
  }
}
function seek(event: MouseEvent): void {
  if (!video.value) return
  const rect = (event.target as HTMLElement).getBoundingClientRect()
  const pos = (event.clientX - rect.left) / rect.width
  video.value.currentTime = pos * duration.value
  ended.value = false
  video.value.play()
}

function toggleSettings(): void {
  showSettings.value = !showSettings.value
  if (!showSettings.value) {
    setTimeout(() => (settingsView.value = 'main'), 200)
  }
}

function setPlaybackSpeed(rate: number): void {
  currentSpeed.value = rate
  if (video.value) {
    video.value.playbackRate = rate
  }
}

function loadHlsScript(callback: () => void) {
  if ((window as any).Hls) {
    callback()
    return
  }
  const script = document.createElement('script')
  script.src = 'https://cdn.jsdelivr.net/npm/hls.js@latest'
  script.onload = callback
  document.head.appendChild(script)
}

async function initVideoPlayer() {
  if (hlsInstance) {
    hlsInstance.destroy()
    hlsInstance = null
  }

  const originalUrl = props.url
  if (!originalUrl) return

  const baseUrl = config.public.apiUrl
  const getMediaUrl = (src: string) => {
    if (!src) return ''
    if (src.startsWith('http://localhost/files/') || src.startsWith('http://localhost/videos/') || src.startsWith('http://localhost/media/')) {
      src = src.replace('http://localhost/', config.public.apiUrl + '/')
    }
    if (
      src.startsWith('http://') ||
      src.startsWith('https://') ||
      src.startsWith('blob:') ||
      src.startsWith('data:')
    ) {
      return src
    }
    if (src.startsWith('/')) {
      return `${baseUrl}${src}`
    }
    return `${baseUrl}/${src}`
  }

  if (originalUrl.includes('/files/') || originalUrl.includes('/media/')) {
    const marker = originalUrl.includes('/media/') ? '/media/' : '/files/'
    let fileId = originalUrl.substring(originalUrl.lastIndexOf(marker) + marker.length)
    const qIdx = fileId.indexOf('?')
    const queryParams = qIdx !== -1 ? fileId.substring(qIdx) : ''
    if (qIdx !== -1) {
      fileId = fileId.substring(0, qIdx)
    }
    const plusIdx = fileId.indexOf('+')
    if (plusIdx !== -1) {
      fileId = fileId.substring(0, plusIdx)
    }
    const hlsUrl = `${baseUrl}/videos/${fileId}/master.m3u8${queryParams}`

    try {
      const resp = await fetch(hlsUrl, { method: 'HEAD' })
      const contentType = resp.headers.get('content-type') || ''
      if (resp.ok && !contentType.includes('text/html')) {
        setupHls(hlsUrl)
        return
      }
    } catch (e) {
      console.warn('ABR HLS is not available, playing original raw file: ', e)
    }
  }

  if (video.value) {
    video.value.src = getMediaUrl(originalUrl)
  }
}

function setupHls(hlsUrl: string) {
  if (!video.value) return

  loadHlsScript(() => {
    if (!video.value) return
    const HlsClass = (window as any).Hls
    if (HlsClass && HlsClass.isSupported()) {
      hlsInstance = new HlsClass()
      hlsInstance.loadSource(hlsUrl)
      hlsInstance.attachMedia(video.value)

      hlsInstance.on(HlsClass.Events.MANIFEST_PARSED, () => {
        const levels = hlsInstance.levels
        const options = levels.map((l: any) => l.height + 'p')
        options.push('Automatycznie')
        qualityOptions.value = options
      })
    } else if (video.value.canPlayType('application/vnd.apple.mpegurl')) {
      video.value.src = hlsUrl
    } else {
      video.value.src = props.url || ''
    }
  })
}

function setQuality(quality: string): void {
  currentQuality.value = quality
  if (hlsInstance) {
    if (quality === 'Automatycznie') {
      hlsInstance.nextLevel = -1
    } else {
      const height = parseInt(quality)
      const levelIndex = hlsInstance.levels.findIndex((l: any) => l.height === height)
      if (levelIndex !== -1) {
        hlsInstance.nextLevel = levelIndex
      }
    }
  }
}

function updateVolume(): void {
  if (!video.value) return
  video.value.volume = volume.value
  if (volume.value > 0) previousVolume.value = volume.value
}

function toggleMute(): void {
  if (volume.value > 0) {
    previousVolume.value = volume.value
    volume.value = 0
  } else {
    volume.value = previousVolume.value || 1
    if (props.postId) {
      trackAudioToggle(String(props.postId), props.pageId, false)
    }
  }
  updateVolume()
}

function toggleFullscreen(): void {
  if (!video.value) return
  if (!document.fullscreenElement) {
    video.value.parentElement?.requestFullscreen()
  } else {
    document.exitFullscreen()
  }
}

let stopObserver: (() => void) | null = null

onMounted(() => {
  initVideoPlayer()

  if (video.value && !isLightbox) {
    const { stop } = useIntersectionObserver(
      video,
      ([{ isIntersecting }]) => {
        if (!isIntersecting && video.value && !video.value.paused) {
          video.value.pause()
        }
      },
      { threshold: 0.25 }
    )
    stopObserver = stop
  }
})

onUnmounted(() => {
  if (stopObserver) {
    stopObserver()
    stopObserver = null
  }
  if (hlsInstance) {
    hlsInstance.destroy()
  }
})

watch(
  () => props.url,
  () => {
    initVideoPlayer()
  },
)
</script>

<style scoped>
/* Niestandardowy wygląd suwaka głośności w przeglądarkach Webkit/Blink i Firefox */
.volume-slider::-webkit-slider-thumb {
  -webkit-appearance: none;
  appearance: none;
  width: 12px;
  height: 12px;
  background: #ffffff;
  border-radius: 50%;
  cursor: pointer;
  box-shadow: 0 0 4px rgba(0, 0, 0, 0.5);
  transition: transform 0.1s ease;
}

.volume-slider::-webkit-slider-thumb:hover {
  transform: scale(1.2);
}

.volume-slider::-moz-range-thumb {
  width: 12px;
  height: 12px;
  background: #ffffff;
  border: none;
  border-radius: 50%;
  cursor: pointer;
  box-shadow: 0 0 4px rgba(0, 0, 0, 0.5);
}
</style>
