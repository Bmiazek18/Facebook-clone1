<template>
  <div class="h-full w-full flex items-center justify-center snap-start snap-always py-4">
    <div class="flex items-end gap-2 md:gap-4 h-full md:max-h-[90vh] max-h-[850px]">

      <div
        class="relative h-full aspect-9/16 bg-[#222] rounded-lg shadow-2xl overflow-hidden shrink"
        @mouseenter="isVideoHovered = true"
        @mouseleave="isVideoHovered = false"
      >
        <video
          ref="videoRef"
          class="w-full h-full object-cover cursor-pointer"
          :poster="reel.poster"
          loop
          playsinline
          :muted="isMuted"
          @click="togglePlay"
          @timeupdate="onTimeUpdate"
        >
          <source :src="reel.videoSrc" type="video/mp4" />
        </video>

        <Transition name="fade">
          <div v-if="isVideoHovered" class="absolute top-4 left-4 flex gap-2 z-20 items-center">
            <button @click.stop="togglePlay" class="p-2 bg-black/50 hover:bg-black/70 rounded-full transition-colors backdrop-blur-sm">
              <PlayIcon v-if="isPaused" :size="24" fillColor="#FFFFFF" />
              <PauseIcon v-else :size="24" fillColor="#FFFFFF" />
            </button>

            <div
              class="relative flex items-center bg-black/50 backdrop-blur-md rounded-full px-2 py-1.5 transition-colors hover:bg-black/60"
              @mouseenter="showVolumeSlider = true"
              @mouseleave="showVolumeSlider = false"
            >
              <button @click.stop="$emit('update-mute', !isMuted)" class="p-1 hover:scale-110 transition-transform outline-none">
                <VolumeMuteIcon v-if="isMuted || globalVolume === 0" :size="20" fillColor="#FFFFFF" />
                <VolumeHighIcon v-else :size="20" fillColor="#FFFFFF" />
              </button>

              <Transition name="slide-width">
                <div v-if="showVolumeSlider" class="flex items-center overflow-hidden h-[20px]" @click.stop>
                  <input
                    type="range" min="0" max="1" step="0.01"
                    :value="globalVolume"
                    @input="handleVolumeChange"
                    class="custom-slider w-24 h-1 mx-2 appearance-none rounded-full cursor-pointer outline-none"
                    :style="{
                      background: `linear-gradient(to right, #ffffff ${globalVolume * 100}%, rgba(255, 255, 255, 0.3) ${globalVolume * 100}%)`
                    }"
                  />
                </div>
              </Transition>
            </div>
          </div>
        </Transition>

        <div v-if="isVideoHovered" class="absolute bottom-0 left-0 right-0 h-1 bg-white/30 z-20">
          <div class="h-full bg-white transition-all duration-100 ease-linear" :style="{ width: `${progress}%` }"></div>
        </div>

        <div class="absolute inset-0 pointer-events-none bg-linear-to-b from-transparent via-transparent to-black/60"></div>

        <div class="absolute bottom-4 left-4 right-4 text-white z-10 pointer-events-none">
          <div class="flex items-center gap-2 mb-2 pointer-events-auto">
            <img :src="reel.avatar" class="w-8 h-8 rounded-full border border-white/20 cursor-pointer" />
            <span class="font-bold text-[14px] hover:underline cursor-pointer">{{ reel.authorName }}</span>
          </div>
          <p class="text-[13px] md:text-[15px] leading-snug line-clamp-2 pointer-events-auto">
            <template v-for="(part, index) in processedCaption" :key="index">
              <span v-if="part.type === 'text'">{{ part.value }}</span>
              <RouterLink
                v-else-if="part.type === 'hashtag'"
                :to="{ name: 'hashtag', params: { hashtag: part.hashtag } }"
                class="text-blue-500 hover:underline"
              >
                {{ part.value }}
              </RouterLink>
              <RouterLink
                v-else-if="part.type === 'mention'"
                :to="{ name: 'userProfile', params: { userId: part.userId } }"
                class="text-blue-500 hover:underline"
              >
                {{ part.value }}
              </RouterLink>
              <a
                v-else-if="part.type === 'link'"
                :href="part.url"
                target="_blank"
                rel="noopener noreferrer"
                class="text-blue-500 hover:underline"
              >
                {{ part.value }}
              </a>
            </template>
          </p>
        </div>
      </div>

      <div class="flex flex-col items-center gap-3 md:gap-4 text-white pb-2">
                    <ReactionButton
                      :post-id="reel.id"
                      :is-liked="reel.isLiked"
                      :likes-count="reel.likes"
                      :hide-text="true"
                      :has-dark-background="true"
                      post-type="reel"
                    />
        <div @click="$emit('toggle-comments')" class="flex flex-col items-center gap-1 cursor-pointer group" v-tooltip.top="{ content: 'Komentarze', theme: 'dark' }" >
          <div class="p-2 md:p-3 bg-[#3a3b3c] group-hover:bg-[#4e4f50] rounded-full transition-colors"
               :class="{'bg-white': isCommentsOpen}">
            <CommentIcon :size="20" :fillColor="isCommentsOpen ? 'black' : 'white'" />
          </div>
          <span class="text-[10px] md:text-xs font-bold text-gray-300">{{ reel.commentsCount }}</span>
        </div>

        <div @click="$emit('open-share', reel)" class="flex flex-col items-center gap-1 cursor-pointer group" v-tooltip.top="{ content: 'Udostępnij', theme: 'dark' }">
          <div class="p-2 md:p-3 bg-[#3a3b3c] group-hover:bg-[#4e4f50] rounded-full transition-colors">
            <ShareIcon :size="20" />
          </div>
          <span class="text-[10px] md:text-xs font-bold text-gray-300">{{ reel.shares }}</span>
        </div>

        <div class="p-2 md:p-3 bg-[#3a3b3c] hover:bg-[#4e4f50] rounded-full cursor-pointer transition-colors mt-1" v-tooltip.top="{ content: 'Wiecej opcji', theme: 'dark' }">
          <DotsHorizontalIcon :size="20" />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, onUnmounted, computed } from 'vue';
import { RouterLink } from 'vue-router';
import { processContent, } from '@/utils/contentProcessor';
import ReactionButton from '@/components/feed/ReactionButton.vue';
import CommentIcon from 'vue-material-design-icons/Comment.vue';
import ShareIcon from 'vue-material-design-icons/Share.vue';
import PlayIcon from 'vue-material-design-icons/Play.vue';
import PauseIcon from 'vue-material-design-icons/Pause.vue';
import VolumeMuteIcon from 'vue-material-design-icons/VolumeMute.vue';
import VolumeHighIcon from 'vue-material-design-icons/VolumeHigh.vue';
import DotsHorizontalIcon from 'vue-material-design-icons/DotsHorizontal.vue';

const props = defineProps<{
  reel: any;
  isActive: boolean;
  isMuted: boolean;
  isCommentsOpen: boolean;
  globalVolume: number;
}>();

const emit = defineEmits(['toggle-comments', 'update-mute', 'update-volume', 'open-share']);

const videoRef = ref<HTMLVideoElement | null>(null);
const isPaused = ref(true);
const isVideoHovered = ref(false);
const showVolumeSlider = ref(false);
const progress = ref(0);

const processedCaption = computed(() => {
  return processContent(props.reel.caption);
});

watch(() => props.isActive, async (active) => {
  if (!videoRef.value) return;
  if (active) {
    videoRef.value.volume = props.globalVolume;
    videoRef.value.play().catch(() => {});
    isPaused.value = false;
  } else {
    videoRef.value.pause();
    isPaused.value = true;
  }
});

watch(() => props.globalVolume, (newVol) => {
  if (videoRef.value) videoRef.value.volume = newVol;
});

const handleVolumeChange = (e: Event) => {
  const val = parseFloat((e.target as HTMLInputElement).value);
  emit('update-volume', val);
  if (val > 0) emit('update-mute', false);
};

const togglePlay = () => {
  if (!videoRef.value) return;
  videoRef.value.paused ? videoRef.value.play() : videoRef.value.pause();
  isPaused.value = videoRef.value.paused;
};

const onTimeUpdate = () => {
  if (videoRef.value) {
    progress.value = (videoRef.value.currentTime / videoRef.value.duration) * 100;
  }
};

onUnmounted(() => {
  if (videoRef.value) {
    videoRef.value.src = "";
    videoRef.value.load();
  }
});
</script>

<style scoped>
.custom-slider::-webkit-slider-thumb {
  -webkit-appearance: none; appearance: none;
  height: 14px; width: 14px;
  background-color: white; border-radius: 50%; cursor: pointer;
}
.slide-width-enter-active { transition: all 0.3s ease-out; }
.slide-width-enter-from { max-width: 0; opacity: 0; }
.slide-width-enter-to { max-width: 160px; opacity: 1; }

.fade-enter-active, .fade-leave-active { transition: opacity 0.2s; }
.fade-enter-from, .fade-leave-to { opacity: 0; }
.v-popper--theme-dark .v-popper__inner {
  background-color: red !important;


  /* Cień i zaokrąglenia ze screena */
  box-shadow: 0 12px 28px rgba(0, 0, 0, 0.3) !important;
  padding: 8px 14px !important;
  border-radius: 12px !important; /* Na nowym SS dymek jest bardziej "obły" */

  /* Typografia */
  font-size: 13px !important;
  font-weight: 400;
  line-height: 1.4;
  white-space: nowrap;
}
</style>
