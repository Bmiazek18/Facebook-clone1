<script setup lang="ts" name="MultiMediaLightbox">
import { ref, computed, watch, onUnmounted, onMounted } from 'vue'
import CloseIcon from 'vue-material-design-icons/Close.vue'
import ChevronLeftIcon from 'vue-material-design-icons/ChevronLeft.vue'
import ChevronRightIcon from 'vue-material-design-icons/ChevronRight.vue'
import DownloadIcon from 'vue-material-design-icons/Download.vue'
import ShareVariantOutlineIcon from 'vue-material-design-icons/ShareVariantOutline.vue'
import PlayerVideo from '@/components/media/PlayerVideo.vue'

const props = withDefaults(
  defineProps<{
    media: Array<{
      id: number
      type: 'image' | 'video' | 'gif'
      imageUrl?: string
      videoUrl?: string
    }>
    modelValue: boolean
    startIndex: number
    fullscreen?: boolean
  }>(),
  {
    fullscreen: true,
  },
)

const emit = defineEmits(['update:modelValue'])

const currentIndex = ref(props.startIndex)

const currentMedia = computed(() => props.media[currentIndex.value] || null)

// Poprawione sprawdzanie GIF-ów (uwzględnia parametry w URL, np. '?cid=123')
const isStaticImage = computed(() => {
  const media = currentMedia.value
  if (!media) return false
  if (media.type === 'video' || media.type === 'gif') return false
  if (media.imageUrl?.toLowerCase().includes('.gif')) return false
  return media.type === 'image'
})

const close = () => emit('update:modelValue', false)
const goTo = (idx: number) => {
  if (idx >= 0 && idx < props.media.length) currentIndex.value = idx
}
const next = () => {
  currentIndex.value = (currentIndex.value + 1) % props.media.length
}
const prev = () => {
  currentIndex.value = (currentIndex.value - 1 + props.media.length) % props.media.length
}

const isVideo = (media: typeof currentMedia.value) => media?.type === 'video'

const download = async () => {
  if (!currentMedia.value) return

  const url = isVideo(currentMedia.value)
    ? currentMedia.value.videoUrl
    : currentMedia.value.imageUrl

  if (!url) return

  try {
    // 1. Pobieramy plik w tle (wymaga, aby serwer obsługiwał nagłówki CORS)
    const response = await fetch(url)
    if (!response.ok) throw new Error('Network response was not ok')

    // 2. Konwertujemy odpowiedź na Blob (surowe dane binarne)
    const blob = await response.blob()

    // 3. Tworzymy lokalny, tymczasowy URL dla tego Bloba
    const blobUrl = window.URL.createObjectURL(blob)

    // 4. Wyciągamy bezpieczne rozszerzenie (pomijając ew. parametry typu ?token=123)
    const extension = url.split('.').pop()?.split('?')[0] || 'file'

    // 5. Tworzymy link i sztucznie go klikamy
    const link = document.createElement('a')
    link.href = blobUrl
    link.download = `media_${Date.now()}.${extension}`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)

    // 6. Czyścimy pamięć
    setTimeout(() => window.URL.revokeObjectURL(blobUrl), 100)

  } catch (error) {
    console.error('Błąd podczas pobierania pliku:', error)
    // Fallback: Jeśli fetch zawiedzie przez rygorystyczny CORS na serwerze obrazków,
    // otwieramy plik w nowej karcie jako ostateczność.
    window.open(url, '_blank')
  }
}

const share = () => {
  alert('Funkcja udostępniania nie jest dostępna w Twojej przeglądarce lub dla tego typu pliku.')
}

// --- LOGIKA BLOKOWANIA SCROLLA ---
const toggleBodyScroll = (shouldLock: boolean) => {
  if (props.fullscreen) {
    document.body.style.overflow = shouldLock ? 'hidden' : ''
  }
}

watch(
  () => props.modelValue,
  (isOpened) => {
    toggleBodyScroll(isOpened)
  },
  { immediate: true },
)

const handleKeydown = (event: KeyboardEvent) => {
  switch (event.key) {
    case 'Escape':
      close()
      break
    case 'ArrowRight':
      next()
      break
    case 'ArrowLeft':
      prev()
      break
  }
}

onMounted(() => window.addEventListener('keydown', handleKeydown))
onUnmounted(() => {
  window.removeEventListener('keydown', handleKeydown)
  toggleBodyScroll(false)
})

watch(
  () => props.startIndex,
  (newIndex) => {
    currentIndex.value = newIndex
  },
)
</script>

<template>
  <component :is="fullscreen ? 'Teleport' : 'div'" :to="fullscreen ? 'body' : undefined">
    <div
      class="flex flex-col overflow-hidden bg-[#000000] z-99999"
      :class="[
        fullscreen
          ? 'fixed inset-0 items-center justify-between'
          : 'relative w-full h-full items-center justify-center',
      ]"
    >
      <!-- Rozmyte tło tylko dla statycznych obrazów -->
      <div
        v-if="isStaticImage"
        class="absolute inset-0 z-0 blur-background"
        :style="{
          backgroundImage: `url(${currentMedia?.imageUrl})`
        }"
      ></div>

      <!-- Nagłówek i ikony (zgodne z Twoim najnowszym zrzutem: ciemne okrągłe tła) -->
      <header
        v-if="fullscreen"
        class="absolute top-0 right-0 w-full flex justify-end items-center p-4 text-white z-50 pointer-events-none"
      >
        <div class="flex items-center space-x-3 pointer-events-auto mr-2">
          <button
            @click="download"
            class="w-10 h-10 rounded-full bg-black/60 hover:bg-black/80 flex items-center justify-center text-gray-200 hover:text-white transition-colors cursor-pointer backdrop-blur-sm"
            v-tooltip="'Pobierz'"
          >
            <DownloadIcon :size="22" />
          </button>
          <button
            @click="share"
            class="w-10 h-10 rounded-full bg-black/60 hover:bg-black/80 flex items-center justify-center text-gray-200 hover:text-white transition-colors cursor-pointer backdrop-blur-sm"
            v-tooltip="'Udostępnij'"
          >
            <ShareVariantOutlineIcon :size="22" />
          </button>
          <button
            @click="close"
            class="w-10 h-10 rounded-full bg-black/60 hover:bg-black/80 flex items-center justify-center text-gray-200 hover:text-white transition-colors cursor-pointer backdrop-blur-sm"
            v-tooltip="'Zamknij'"
          >
            <CloseIcon :size="24" />
          </button>
        </div>
      </header>

      <div class="flex flex-col items-center justify-center grow w-full relative z-10 overflow-hidden">
        <div class="flex items-center justify-center w-full grow relative h-full">

          <!-- Strefa klikalna LEWA z ZAWSZE WIDOCZNYM pionowym odciętym cieniem (jak na screenie) -->
          <div
            v-if="media.length > 1"
            @click="prev"
            class="absolute left-0 top-0 bottom-0 w-20 flex items-center justify-center z-20 cursor-pointer group bg-black/10 hover:bg-black/20 transition-colors duration-200"
          >
            <div class="w-10 h-10 rounded-full bg-gray-200/90 group-hover:bg-white text-gray-900 flex items-center justify-center shadow-lg transition-colors">
              <ChevronLeftIcon :size="28" />
            </div>
          </div>

          <!-- render obraz, wideo lub GIF -->
          <template v-if="currentMedia">
            <img
              v-if="!isVideo(currentMedia)"
              :src="currentMedia.imageUrl"
              :class="fullscreen ? 'max-w-[80%] max-h-[85vh]' : 'max-w-full max-h-[70vh]'"
              class="object-contain drop-shadow-2xl"
              alt="Podgląd"
            />
            <PlayerVideo
              v-else
              :lightbox="true"
              :url="currentMedia.videoUrl ?? ''"
              :class="fullscreen ? 'max-w-[80%] max-h-[85vh]' : 'max-w-full max-h-[70vh]'"
              class="object-contain drop-shadow-2xl"
            />
          </template>

          <!-- Strefa klikalna PRAWA z ZAWSZE WIDOCZNYM pionowym odciętym cieniem (jak na screenie) -->
          <div
            v-if="media.length > 1"
            @click="next"
            class="absolute right-0 top-0 bottom-0 w-20  flex items-center justify-center z-20 cursor-pointer group bg-black/10 hover:bg-black/20 hover:trans transition-colors duration-200"
          >
            <div class="w-10 h-10 rounded-full bg-gray-200/90 group-hover:bg-white text-gray-900 flex items-center justify-center shadow-lg transition-colors">
              <ChevronRightIcon :size="28" />
            </div>
          </div>

          <!-- Image Counter -->
          <div
            v-if="!fullscreen && media.length > 1"
            class="absolute bottom-4 right-4 bg-gray-900/70 backdrop-blur-sm rounded-full px-4 py-1.5 text-white text-sm font-medium z-20"
          >
            {{ currentIndex + 1 }} / {{ media.length }}
          </div>
        </div>

        <!-- Lista miniatur -->
        <div
          v-if="media.length > 1"
          class="flex overflow-x-auto overflow-y-hidden space-x-2 w-full hide-scrollbar z-10 justify-center pb-4"
          :class="[fullscreen ? 'pt-2 px-4 max-w-[90%] mx-auto' : 'mt-4 px-4 max-w-full']"
        >
          <div
            v-for="(m, idx) in media"
            :key="m.id"
            @click="goTo(idx)"
            class="shrink-0 w-12 h-12 cursor-pointer border-2 transition-all duration-200 rounded-lg overflow-hidden"
            :class="{
              'border-white opacity-100 scale-105': idx === currentIndex,
              'border-transparent opacity-50 hover:opacity-100': idx !== currentIndex,
            }"
          >
            <img
              v-if="m.type === 'image' || m.type === 'gif'"
              :src="m.imageUrl"
              class="w-full h-full object-cover"
              :alt="`Thumbnail ${idx + 1}`"
            />
            <div v-else class="relative w-full h-full bg-gray-900">
              <video :src="m.videoUrl" class="w-full h-full object-cover" muted></video>
              <div class="absolute inset-0 flex items-center justify-center pointer-events-none bg-black/30">
                <svg class="w-6 h-6 text-white opacity-90" fill="currentColor" viewBox="0 0 24 24">
                  <path d="M8 5v14l11-7z" />
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
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
  filter: blur(40px) brightness(0.4);
  -webkit-filter: blur(40px) brightness(0.4);
  transform: scale(1.1); /* Skalowanie zapobiega "pustym" rogom przy mocnym blurze */
}

/* Ukrycie paska przewijania dla estetyki miniatur */
.hide-scrollbar {
  -ms-overflow-style: none;  /* IE and Edge */
  scrollbar-width: none;  /* Firefox */
}
.hide-scrollbar::-webkit-scrollbar {
  display: none; /* Chrome, Safari and Opera */
}
</style>
