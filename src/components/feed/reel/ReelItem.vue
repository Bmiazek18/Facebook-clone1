<template>
  <div class="h-screen w-full flex items-center justify-center snap-start snap-always py-4">
    <div class="flex items-end gap-2 md:gap-4 h-full md:max-h-[90vh] max-h-[850px]">
      <div
        class="relative h-full aspect-9/16 bg-[#222] rounded-lg shadow-2xl overflow-hidden shrink"
        @mouseenter="isVideoHovered = true"
        @mouseleave="isVideoHovered = false"
      >
        <video
          ref="videoRef"
          class="w-full h-full object-cover cursor-pointer"
          :poster="reel?.poster"
          loop
          playsinline
          :muted="isMuted"
          @click="togglePlay"
          @timeupdate="onTimeUpdate"
        >
          <source v-if="reel?.videoSrc" :src="reel.videoSrc" type="video/mp4" />
        </video>

        <!-- KONTROLKA GŁOŚNOŚCI -->
        <div
          class="absolute top-4 left-4 flex items-center bg-black/40 backdrop-blur-md rounded-full p-2 z-20 transition-colors hover:bg-black/60 select-none"
          @mouseenter="showVolumeSlider = true"
          @mouseleave="showVolumeSlider = false"
        >
          <button
            @click.stop="$emit('update-mute', !isMuted)"
            class="hover:scale-110 transition-transform outline-none text-white flex items-center justify-center"
          >
            <VolumeMuteIcon
              v-if="isMuted || globalVolume === 0"
              :size="22"
              fillColor="#FFFFFF"
            />
            <VolumeHighIcon v-else :size="22" fillColor="#FFFFFF" />
          </button>

          <Transition name="slide-width">
            <div
              v-if="showVolumeSlider"
              class="flex items-center overflow-hidden h-[20px]"
              @click.stop
            >
              <input
                type="range"
                min="0"
                max="1"
                step="0.01"
                :value="globalVolume"
                @input="handleVolumeChange"
                class="custom-slider w-20 h-1 mx-2 appearance-none rounded-full cursor-pointer outline-none"
                :style="{
                  background: `linear-gradient(to right, #ffffff ${globalVolume * 100}%, rgba(255, 255, 255, 0.3) ${globalVolume * 100}%)`,
                }"
              />
            </div>
          </Transition>
        </div>

        <!-- WYSZUKIWANIE / LUPA -->
        <button
          class="absolute top-4 right-4 w-9 h-9 bg-black/40 backdrop-blur-md hover:bg-black/60 rounded-full flex items-center justify-center text-white z-20 transition-all duration-200 hover:scale-105"
          :class="isVideoHovered ? 'opacity-100' : 'opacity-0 pointer-events-none'"
        >
          <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24" stroke-width="2.5">
            <path stroke-linecap="round" stroke-linejoin="round" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
          </svg>
        </button>

        <!-- DOLNY PASEK POSTĘPU -->
        <div class="absolute bottom-0 left-0 right-0 h-[3px] bg-white/20 z-20">
          <div
            class="h-full bg-white transition-all ease-linear"
            :style="{
              width: `${progress}%`,
              transitionDuration: `${100 / playbackSpeed}ms`
            }"
          ></div>
        </div>

        <div class="absolute inset-0 pointer-events-none bg-linear-to-b from-transparent via-transparent to-black/60"></div>

        <!-- Opis i Autor -->
        <div class="absolute bottom-4 left-4 right-4 text-white z-10 pointer-events-none mb-2">
          <div class="flex items-center gap-2 mb-2 pointer-events-auto">
            <img :src="reel?.avatar" class="w-8 h-8 rounded-full border border-white/20 cursor-pointer" />
            <span class="font-bold text-[14px] hover:underline cursor-pointer flex items-center gap-1">
              {{ reel?.authorName }}
              <span class="text-gray-400 text-xs font-normal">· Obserwuj</span>
            </span>
          </div>
          <p class="text-[13px] md:text-[14px] leading-snug line-clamp-2 pointer-events-auto text-gray-100 drop-shadow">
            <template v-for="(part, index) in processedCaption" :key="index">
              <span v-if="part.type === 'text'">{{ part.value }}</span>
              <NuxtLink v-else-if="part.type === 'hashtag'" :to="`/hashtag/${part.hashtag}`" class="text-blue-400 hover:underline font-semibold">{{ part.value }}</NuxtLink>
              <NuxtLink v-else-if="part.type === 'mention'" :to="`/profile/${part.userId}`" class="text-blue-400 hover:underline font-semibold">{{ part.value }}</NuxtLink>
              <a v-else-if="part.type === 'link'" :href="part.url" target="_blank" rel="noopener noreferrer" class="text-blue-400 hover:underline">{{ part.value }}</a>
            </template>
          </p>
        </div>
      </div>

      <!-- SEKCJA PRAWA (IKONY AKCJI) -->
      <div class="flex flex-col items-center gap-4 text-white pb-3 select-none min-w-[45px]">
        <!-- Polubienia -->
        <div class="flex flex-col items-center cursor-pointer group">
          <ReactionButton
            v-if="reel"
            :post-id="reel.id"
            :is-liked="reel.isLiked"
            :likes-count="reel.likes"
            :hide-text="true"
            :has-dark-background="false"
            post-type="reel"
          />
          <span class="text-[12px] font-semibold text-gray-200 mt-1 drop-shadow-md">{{ reel?.likes || 0 }}</span>
        </div>

        <!-- Komentarze -->
        <div
          @click="$emit('toggle-comments')"
          class="flex flex-col items-center cursor-pointer group"
          v-tooltip.top="{ content: 'Komentarze', theme: 'dark' }"
        >
          <div class="text-white">
            <svg class="w-7 h-7 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24" stroke-width="2">
              <path stroke-linecap="round" stroke-linejoin="round" d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z" />
            </svg>
          </div>
          <span class="text-[12px] font-semibold text-gray-200 mt-1 drop-shadow-md">{{ reel?.commentsCount || 0 }}</span>
        </div>

        <!-- Udostępnienia -->
        <div
          @click="reel && $emit('open-share', reel)"
          class="flex flex-col items-center cursor-pointer group"
          v-tooltip.top="{ content: 'Udostępnij', theme: 'dark' }"
        >
          <div class="text-white">
            <ShareIcon :size="28" />
          </div>
          <span class="text-[12px] font-semibold text-gray-200 mt-1 drop-shadow-md">{{ reel?.shares || 0 }}</span>
        </div>

        <!-- Więcej opcji (FLOATING VUE DROPDOWN) -->
        <VDropdown
          v-if="reel"
          placement="left-end"
          :distance="12"
          theme="menu-dark"
          :triggers="['click']"
          @apply-hide="isSpeedSubmenuOpen = false"
        >
          <div class="flex flex-col items-center cursor-pointer group mt-1">
            <DotsHorizontalIcon :size="26" />
          </div>

          <template #popper="{ hide }">
            <div class="w-[310px] bg-[#262626] text-white rounded-xl py-2 text-[14px] overflow-hidden">

              <!-- WIDOK GŁÓWNY MENU -->
              <div v-if="!isSpeedSubmenuOpen">
                <!-- 1. Interesuje mnie -->
                <button @click="hide()" class="w-full px-4 py-2.5 flex items-start gap-3.5 hover:bg-white/10 text-left transition-colors">
                  <svg class="w-[22px] h-[22px] shrink-0 mt-0.5 text-gray-200" fill="none" stroke="currentColor" viewBox="0 0 24 24" stroke-width="1.5">
                    <circle cx="12" cy="12" r="9"/>
                    <path stroke-linecap="round" stroke-linejoin="round" d="M12 8v8M8 12h8"/>
                  </svg>
                  <div>
                    <p class="font-medium text-[15px]">Interesuje mnie</p>
                    <p class="text-[13px] text-gray-400">Będziesz widzieć więcej takich rolek.</p>
                  </div>
                </button>

                <!-- 2. Nie interesuje mnie -->
                <button @click="hide()" class="w-full px-4 py-2.5 flex items-start gap-3.5 hover:bg-white/10 text-left transition-colors">
                  <svg class="w-[22px] h-[22px] shrink-0 mt-0.5 text-gray-200" fill="none" stroke="currentColor" viewBox="0 0 24 24" stroke-width="1.5">
                    <circle cx="12" cy="12" r="9"/>
                    <path stroke-linecap="round" stroke-linejoin="round" d="M8 12h8"/>
                  </svg>
                  <div>
                    <p class="font-medium text-[15px]">Nie interesuje mnie</p>
                    <p class="text-[13px] text-gray-400">Będziesz widzieć mniej takich rolek.</p>
                  </div>
                </button>

                <!-- 3. Zapisz rolkę -->
                <button @click="hide()" class="w-full px-4 py-3 flex items-center gap-3.5 hover:bg-white/10 text-left transition-colors">
                  <svg class="w-[22px] h-[22px] shrink-0 text-gray-200" fill="none" stroke="currentColor" viewBox="0 0 24 24" stroke-width="1.5">
                    <path stroke-linecap="round" stroke-linejoin="round" d="M5 5a2 2 0 012-2h10a2 2 0 012 2v16l-7-3.5L5 21V5z"/>
                  </svg>
                  <p class="font-medium text-[15px]">Zapisz rolkę</p>
                </button>

                <!-- 4. Skopiuj link -->
                <button @click="hide()" class="w-full px-4 py-3 flex items-center gap-3.5 hover:bg-white/10 text-left transition-colors">
                  <svg class="w-[22px] h-[22px] shrink-0 text-gray-200" fill="none" stroke="currentColor" viewBox="0 0 24 24" stroke-width="1.5">
                    <path stroke-linecap="round" stroke-linejoin="round" d="M13.19 8.688a4.5 4.5 0 011.242 7.244l-4.5 4.5a4.5 4.5 0 01-6.364-6.364l1.757-1.757m13.35-.622l1.757-1.757a4.5 4.5 0 00-6.364-6.364l-4.5 4.5a4.5 4.5 0 001.242 7.244"/>
                  </svg>
                  <p class="font-medium text-[15px]">Skopiuj link</p>
                </button>

                <!-- 5. Zgłoś rolkę -->
                <button @click="hide()" class="w-full px-4 py-3 flex items-center gap-3.5 hover:bg-white/10 text-left transition-colors">
                  <svg class="w-[22px] h-[22px] shrink-0 text-gray-200" fill="none" stroke="currentColor" viewBox="0 0 24 24" stroke-width="1.5">
                    <rect x="3" y="5" width="18" height="14" rx="3" ry="3" stroke-linecap="round" stroke-linejoin="round"/>
                    <path stroke-linecap="round" stroke-linejoin="round" d="M12 9v4m0 3h.01"/>
                  </svg>
                  <p class="font-medium text-[15px]">Zgłoś rolkę</p>
                </button>

                <!-- 6. Prędkość odtwarzania -->
                <button @click.stop="isSpeedSubmenuOpen = true" class="w-full px-4 py-2.5 flex items-center justify-between hover:bg-white/10 text-left transition-colors">
                  <div class="flex items-center gap-3.5">
                    <svg class="w-[22px] h-[22px] shrink-0 text-gray-200" fill="none" stroke="currentColor" viewBox="0 0 24 24" stroke-width="1.5">
                       <path stroke-linecap="round" stroke-linejoin="round" d="M12.75 6.75a1.5 1.5 0 1 1-3 0 1.5 1.5 0 0 1 3 0ZM4.5 19.5l3.75-6.75 3 3-1.5 3.75" />
                       <path stroke-linecap="round" stroke-linejoin="round" d="M10.5 12l2.25-3.75 4.5 1.5M17.25 12l-2.25 6" />
                    </svg>
                    <div>
                      <p class="font-medium text-[15px]">Prędkość odtwarzania</p>
                      <p class="text-[13px] text-gray-400">{{ playbackSpeed === 1 ? '1 (normalna)' : `${playbackSpeed}x` }}</p>
                    </div>
                  </div>
                  <svg class="w-5 h-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24" stroke-width="2.5">
                    <path stroke-linecap="round" stroke-linejoin="round" d="M9 5l7 7-7 7" />
                  </svg>
                </button>

                <!-- 7. Coś nie działa -->
                <button @click="hide()" class="w-full px-4 py-3 flex items-center gap-3.5 hover:bg-white/10 text-left transition-colors">
                  <svg class="w-[22px] h-[22px] shrink-0 text-gray-200" fill="none" stroke="currentColor" viewBox="0 0 24 24" stroke-width="1.5">
                    <path stroke-linecap="round" stroke-linejoin="round" d="M12 2v2m0 16v2m-6-10H4m16 0h-2m-2.5 5.5l1.5 1.5M6 7.5L4.5 6m13.5 1.5L19.5 6M6 16.5L4.5 18M12 6c-3.314 0-6 2.686-6 6 0 3.314 2.686 6 6 6s6-2.686 6-6c0-3.314-2.686-6-6-6z"/>
                    <path stroke-linecap="round" stroke-linejoin="round" d="M12 6v12M9 9h6m-6 4h6"/>
                  </svg>
                  <p class="font-medium text-[15px]">Coś nie działa</p>
                </button>

                <!-- 8. Osadź -->
                <button @click="hide()" class="w-full px-4 py-3 flex items-center gap-3.5 hover:bg-white/10 text-left transition-colors">
                  <svg class="w-[22px] h-[22px] shrink-0 text-gray-200" fill="none" stroke="currentColor" viewBox="0 0 24 24" stroke-width="1.5">
                    <path stroke-linecap="round" stroke-linejoin="round" d="M10 20l4-16m4 4l4 4-4 4M6 16l-4-4 4-4" />
                  </svg>
                  <p class="font-medium text-[15px]">Osadź</p>
                </button>

                <!-- 9. Audio i język -->
                <button @click="hide()" class="w-full px-4 py-3 flex items-center justify-between hover:bg-white/10 text-left transition-colors">
                  <div class="flex items-center gap-3.5">
                    <svg class="w-[22px] h-[22px] shrink-0 text-gray-200" fill="none" stroke="currentColor" viewBox="0 0 24 24" stroke-width="1.5">
                      <path stroke-linecap="round" stroke-linejoin="round" d="M10.5 21l5.25-11.25L21 21m-9-3h7.5M3 5.621a48.474 48.474 0 016-.371m0 0c1.12 0 2.226.05 3.32.148m-3.32-.148V9m0-3.379c.323.01.644.025.965.044m-2.451 1.761a48.4 48.4 0 013.238 12.33M7.5 12.5a48.332 48.332 0 00-3.14-8.8m2.915 9.068a48.16 48.16 0 002.585 2.128M15 15.625l-2.073-4.257" />
                    </svg>
                    <p class="font-medium text-[15px]">Audio i język</p>
                  </div>
                  <svg class="w-5 h-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24" stroke-width="2.5">
                    <path stroke-linecap="round" stroke-linejoin="round" d="M9 5l7 7-7 7" />
                  </svg>
                </button>
              </div>

              <!-- WIDOK PODMENU PRĘDKOŚCI -->
              <div v-else class="animate-fade-in">
                <button @click.stop="isSpeedSubmenuOpen = false" class="w-full px-4 py-3 flex items-center gap-2 hover:bg-white/10 text-left font-semibold border-b border-white/10 text-gray-300 transition-colors">
                  <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24" stroke-width="2.5">
                    <path stroke-linecap="round" stroke-linejoin="round" d="M15 19l-7-7 7-7" />
                  </svg>
                  Wróć
                </button>
                <div class="py-1">
                  <button
                    v-for="speed in [0.5, 1, 1.25, 1.5, 2]"
                    :key="speed"
                    @click="changePlaybackSpeed(speed); hide()"
                    class="w-full px-4 py-2.5 hover:bg-white/10 text-left flex items-center justify-between transition-colors"
                    :class="{ 'text-blue-400 font-bold': playbackSpeed === speed }"
                  >
                    <span>{{ speed === 1 ? 'Normalna (1x)' : `${speed}x` }}</span>
                    <span v-if="playbackSpeed === speed" class="text-blue-400">✓</span>
                  </button>
                </div>
              </div>

            </div>
          </template>
        </VDropdown>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, onUnmounted, computed, nextTick } from 'vue'
import { processContent } from '@/utils/contentProcessor'
import ReactionButton from '@/components/feed/ReactionButton.vue'
import DotsHorizontalIcon from 'vue-material-design-icons/DotsHorizontal.vue'
import ShareIcon from 'vue-material-design-icons/Share.vue'
import VolumeMuteIcon from 'vue-material-design-icons/VolumeMute.vue'
import VolumeHighIcon from 'vue-material-design-icons/VolumeHigh.vue'
import { Dropdown as VDropdown } from 'floating-vue'

const props = defineProps<{
  reel: any
  isActive: boolean
  isMuted: boolean
  isCommentsOpen: boolean
  globalVolume: number
}>()

const emit = defineEmits(['toggle-comments', 'update-mute', 'update-volume', 'open-share'])

const videoRef = ref<HTMLVideoElement | null>(null)
const isPaused = ref(true)
const isVideoHovered = ref(false)
const showVolumeSlider = ref(false)
const progress = ref(0)

const playbackSpeed = ref(1)
const isSpeedSubmenuOpen = ref(false)

const processedCaption = computed(() => {
  return props.reel?.caption ? processContent(props.reel.caption) : []
})

watch(
  () => props.isActive,
  async (active) => {
    await nextTick()
    if (!videoRef.value) return
    if (active) {
      videoRef.value.volume = props.globalVolume
      videoRef.value.playbackRate = playbackSpeed.value
      videoRef.value.play().catch(() => {})
      isPaused.value = false
    } else {
      videoRef.value.pause()
      isPaused.value = true
    }
  },
  { immediate: true }
)

watch(
  () => props.globalVolume,
  (newVol) => {
    if (videoRef.value) videoRef.value.volume = newVol
  },
)

const handleVolumeChange = (e: Event) => {
  const val = parseFloat((e.target as HTMLInputElement).value)
  emit('update-volume', val)
  if (val > 0) emit('update-mute', false)
}

const togglePlay = () => {
  if (!videoRef.value) return
  videoRef.value.paused ? videoRef.value.play() : videoRef.value.pause()
  isPaused.value = videoRef.value.paused
}

const onTimeUpdate = () => {
  if (videoRef.value && videoRef.value.duration) {
    progress.value = (videoRef.value.currentTime / videoRef.value.duration) * 100
  }
}

const changePlaybackSpeed = (speed: number) => {
  playbackSpeed.value = speed
  if (videoRef.value) {
    videoRef.value.playbackRate = speed
  }
}

onUnmounted(() => {
  if (videoRef.value) {
    videoRef.value.src = ''
    videoRef.value.load()
  }
})
</script>

<style scoped>
.custom-slider::-webkit-slider-thumb {
  -webkit-appearance: none;
  appearance: none;
  height: 12px;
  width: 12px;
  background-color: white;
  border-radius: 50%;
  cursor: pointer;
}
.slide-width-enter-active {
  transition: all 0.25s ease-out;
}
.slide-width-enter-from {
  max-width: 0;
  opacity: 0;
}
.slide-width-enter-to {
  max-width: 120px;
  opacity: 1;
}

:deep(.v-popper--theme-menu-dark .v-popper__inner) {
  background: #262626 !important;
  border: 1px solid rgba(255, 255, 255, 0.08) !important;
  border-radius: 12px !important;
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.5) !important;
  padding: 0 !important;
}

:deep(.v-popper--theme-menu-dark .v-popper__arrow-inner),
:deep(.v-popper--theme-menu-dark .v-popper__arrow-outer) {
  display: none !important;
}

.animate-fade-in {
  animation: fadeIn 0.15s ease-out;
}
@keyframes fadeIn {
  from { opacity: 0; transform: scale(0.98); }
  to { opacity: 1; transform: scale(1); }
}
</style>
