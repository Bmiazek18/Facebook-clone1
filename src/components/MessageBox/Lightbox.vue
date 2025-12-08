<script setup lang="ts">
import { ref, computed, watch, onUnmounted, onMounted } from 'vue';
import CloseIcon from 'vue-material-design-icons/Close.vue';
import ChevronLeftIcon from 'vue-material-design-icons/ChevronLeft.vue';
import ChevronRightIcon from 'vue-material-design-icons/ChevronRight.vue';
import DownloadIcon from 'vue-material-design-icons/Download.vue';
import ShareVariantOutlineIcon from 'vue-material-design-icons/ShareVariantOutline.vue';
import PlayerVideo from '../PlayerVideo.vue';
const props = defineProps<{
  media: Array<{
    id: number;
    type: 'image' | 'video'; // nowy typ
    imageUrl?: string;
    videoUrl?: string;
  }>,
  modelValue: boolean,
  startIndex: number
}>();

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
  document.body.style.overflow = shouldLock ? 'hidden' : '';
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
  <Teleport to="body">
    <div class="fixed inset-0 z-99999 flex flex-col items-center justify-between bg-black">

      <div class="absolute inset-0 z-0 blur-background"
           :style="{ backgroundImage: currentMedia && !isVideo(currentMedia) && currentMedia.imageUrl ? `url(${currentMedia.imageUrl})` : 'none' }">
      </div>

      <header class="w-full flex justify-between items-center p-3 text-white relative z-10">
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
        <div class="flex items-center justify-center w-full grow relative">
          <button @click="prev" class="absolute left-4 p-2 bg-black bg-opacity-50 rounded-full text-white hover:bg-opacity-70 z-20">
            <ChevronLeftIcon :size="30" />
          </button>

          <!-- render obraz lub video -->
          <template v-if="currentMedia">
            <img v-if="!isVideo(currentMedia)"
                 :src="currentMedia.imageUrl"
                 class="max-w-[80%] max-h-[80vh] object-contain"
                 alt="Powiększony obraz" />
            <PlayerVideo v-else
            :lightbox="true"
                   :url="currentMedia.videoUrl"
                   class="max-w-[80%] max-h-[80vh] object-contain"
                   />
          </template>

          <button @click="next" class="absolute right-4 p-2 bg-black bg-opacity-50 rounded-full text-white hover:bg-opacity-70 z-20">
            <ChevronRightIcon :size="30" />
          </button>
        </div>

        <div v-if="media.length > 0" class="flex overflow-x-auto space-x-2 p-4 w-full justify-center max-w-[80%]">
          <div v-for="(m, idx) in media" :key="m.id"
               @click="goTo(idx)"
               class="shrink-0 w-16 h-16 cursor-pointer border-2 transition-all duration-200"
               :class="{ 'border-white opacity-100 scale-105': idx === currentIndex, 'border-transparent opacity-60 hover:opacity-100': idx !== currentIndex }">

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
  </Teleport>
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
