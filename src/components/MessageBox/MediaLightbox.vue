<script setup lang="ts" name="MultiMediaLightbox">
import { ref, computed, watch, onUnmounted, onMounted } from 'vue';
import CloseIcon from 'vue-material-design-icons/Close.vue';
import ChevronLeftIcon from 'vue-material-design-icons/ChevronLeft.vue';
import ChevronRightIcon from 'vue-material-design-icons/ChevronRight.vue';
import DownloadIcon from 'vue-material-design-icons/Download.vue';
import ShareVariantOutlineIcon from 'vue-material-design-icons/ShareVariantOutline.vue';
import PlayerVideo from '@/components/media/PlayerVideo.vue';
const props = withDefaults(defineProps<{
  media: Array<{
    id: number;
    type: 'image' | 'video' | 'gif';
    imageUrl?: string;
    videoUrl?: string;
  }>,
  modelValue: boolean,
  startIndex: number,
  fullscreen?: boolean
}>(), {
  fullscreen: true
});

const emit = defineEmits(['update:modelValue']);

const currentIndex = ref(props.startIndex);

const currentMedia = computed(() => props.media[currentIndex.value] || null);

const close = () => emit('update:modelValue', false);
const goTo = (idx: number) => {
  if (idx >= 0 && idx < props.media.length) currentIndex.value = idx;
};
const next = () => {
  currentIndex.value = (currentIndex.value + 1) % props.media.length;
};
const prev = () => {
  currentIndex.value = (currentIndex.value - 1 + props.media.length) % props.media.length;
};

const isVideo = (media: typeof currentMedia.value) => media?.type === 'video';

const download = () => {
  if (!currentMedia.value) return;

  const url = isVideo(currentMedia.value) ? currentMedia.value.videoUrl : currentMedia.value.imageUrl;
  if (!url) return;

  const link = document.createElement('a');
  link.href = url;
  link.download = `media_${Date.now()}.${url.split('.').pop()}`;
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
};

const share = () => {
  alert('Funkcja udostępniania nie jest dostępna w Twojej przeglądarce lub dla tego typu pliku.');
};

// --- LOGIKA BLOKOWANIA SCROLLA ---
const toggleBodyScroll = (shouldLock: boolean) => {
  // Tylko blokuj scroll gdy fullscreen jest włączony
  if (props.fullscreen) {
    document.body.style.overflow = shouldLock ? 'hidden' : '';
  }
};

watch(() => props.modelValue, (isOpened) => {
  toggleBodyScroll(isOpened);
}, { immediate: true });

const handleKeydown = (event: KeyboardEvent) => {
  switch (event.key) {
    case 'Escape':
      close();
      break;
    case 'ArrowRight':
      next();
      break;
    case 'ArrowLeft':
      prev();
      break;
  }
};

onMounted(() => window.addEventListener('keydown', handleKeydown));
onUnmounted(() => {
  window.removeEventListener('keydown', handleKeydown);
  toggleBodyScroll(false);
});

watch(() => props.startIndex, (newIndex) => {
  currentIndex.value = newIndex;
});
</script>

<template>
  <component :is="fullscreen ? 'Teleport' : 'div'" :to="fullscreen ? 'body' : undefined">
    <div
      class="flex flex-col bg-black"
      :class="[
        fullscreen ? 'fixed inset-0 z-99999 items-center justify-between' : 'relative w-full h-full items-center justify-center'
      ]"
    >

      <div
        v-if="!isVideo(currentMedia)"
        class="absolute inset-0 z-0 blur-background"
        :style="{ backgroundImage: currentMedia && currentMedia.imageUrl ? `url(${currentMedia.imageUrl})` : 'none' }"
      >
      </div>

      <header
        v-if="fullscreen"
        class="w-full flex justify-between items-center p-3 text-white relative z-10"
      >
        <button @click="close" class="flex items-center space-x-1 text-lg hover:text-gray-300 transition">
          <CloseIcon :size="24" />
          <span>Zamknij</span>
        </button>
        <div class="flex space-x-3">
          <DownloadIcon :size="24" class="cursor-pointer hover:text-gray-300" @click="download" />
          <ShareVariantOutlineIcon :size="24" class="cursor-pointer hover:text-gray-300" @click="share" />
        </div>
      </header>

      <div class="flex flex-col items-center justify-center grow w-full relative z-10">
        <div class="flex items-center justify-center w-full grow relative group">
          <button
            @click="prev"
            class="absolute left-4 p-2 bg-black bg-opacity-50 rounded-full text-white hover:bg-opacity-70 z-20 transition-opacity"
            :class="fullscreen ? '' : 'opacity-0 group-hover:opacity-100'"
          >
            <ChevronLeftIcon :size="30" />
          </button>

          <!-- render obraz lub video -->
          <template v-if="currentMedia">
            <img v-if="!isVideo(currentMedia)"
                 :src="currentMedia.imageUrl"
                 :class="fullscreen ? 'max-w-[80%] max-h-[80vh]' : 'max-w-full max-h-[70vh]'"
                 class="object-contain"
                 alt="Powiększony obraz" />
            <PlayerVideo v-else
                 :lightbox="true"
                 :url="currentMedia.videoUrl ?? ''"
                 :class="fullscreen ? 'max-w-[80%] max-h-[80vh]' : 'max-w-full max-h-[70vh]'"
                 class="object-contain"
                 />
          </template>

          <button
            @click="next"
            class="absolute right-4 p-2 bg-black bg-opacity-50 rounded-full text-white hover:bg-opacity-70 z-20 transition-opacity"
            :class="fullscreen ? '' : 'opacity-0 group-hover:opacity-100'"
          >
            <ChevronRightIcon :size="30" />
          </button>

          <!-- Image Counter -->
          <div
            v-if="!fullscreen && media.length > 1"
            class="absolute bottom-4 right-4 bg-gray-800/60 backdrop-blur-sm rounded-full px-4 py-2 text-white text-sm font-medium z-20"
          >
            {{ currentIndex + 1 }} / {{ media.length }}
          </div>
        </div>

        <div
          v-if="media.length > 1"
          class="flex overflow-x-auto overflow-y-hidden space-x-2 w-full justify-center"
          :class="[
            fullscreen ? 'p-4 max-w-[80%]' : 'mt-4 px-4 max-w-full'
          ]"
        >
          <div v-for="(m, idx) in media" :key="m.id"
               @click="goTo(idx)"
               class="shrink-0 w-16 h-16 cursor-pointer border-2 transition-all duration-200 rounded-lg overflow-hidden"
               :class="{
                 'border-white opacity-100 scale-105': idx === currentIndex,
                 'border-transparent opacity-60 hover:opacity-100': idx !== currentIndex
               }">

            <img v-if="m.type === 'image'" :src="m.imageUrl" class="w-full h-full object-cover" :alt="`Thumbnail ${idx + 1}`" />
            <div v-else class="relative w-full h-full">
              <video :src="m.videoUrl" class="w-full h-full object-cover" muted></video>
              <div class="absolute inset-0 flex items-center justify-center pointer-events-none">
                <svg class="w-6 h-6 text-white opacity-75" fill="currentColor" viewBox="0 0 24 24">
                  <path d="M8 5v14l11-7z"/>
                </svg>
              </div>
            </div>

          </div>
        </div>
      </div>
    </div>
  </component>
</template>

<style scoped>
.blur-background {
  background-size: 125%;
  background-position: center;
  background-repeat: no-repeat;
  filter: blur(20px) saturate(50%);
  -webkit-filter: blur(20px) saturate(50%);
}
</style>
