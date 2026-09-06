<script setup lang="ts">
import { computed, inject, ref, type Ref } from 'vue'
// Import wymaganych ikon
import Camera from 'vue-material-design-icons/Camera.vue'
import ImageOutline from 'vue-material-design-icons/ImageOutline.vue'
import Upload from 'vue-material-design-icons/Upload.vue'
import { Dropdown as VDropdown } from 'floating-vue'

// Props
interface Props {
  imageUrl?: string | null
  initialWidth?: number
  initialHeight?: number
}

const props = withDefaults(defineProps<Props>(), {
  imageUrl: null,
  initialWidth: 1250,
  initialHeight: 450,
})

const isOwner = inject('isOwner', false)

// Corrected logic: Has cover image when imageUrl is truthy
const hasCoverImage = computed(() => !!props.imageUrl)

const imageLoaded: Ref<boolean> = ref(false)

const emit = defineEmits<{
  'upload-cover': []
  'view-cover': []
}>()
</script>

<template>
  <!-- Kontener Główny (Pełna szerokość) -->
  <div
    class="w-full overflow-hidden mb-8 relative transition-all duration-300 flex justify-center"
    :style="{ height: `${initialHeight}px` }"
  >
    <!-- Rozmyte tło na pełną szerokość (Tylko gdy posiada zdjęcie w tle) -->
    <template v-if="hasCoverImage">
      <img
        :src="props.imageUrl!"
        :alt="$t('media.rozmyteTlo')"
        class="absolute top-0 left-0 w-full h-full object-cover blur-md scale-105 z-10 opacity-60"
        crossOrigin="anonymous"
      />
      <!-- Nakładka gradientowa (maska) wygładzająca brzegi -->
      <div
        class="absolute inset-0 bg-gradient-to-t from-white via-white/70 to-transparent z-20 pointer-events-none"
      />
    </template>

    <!-- Wycentrowana zawartość o stałej szerokości -->
    <div class="relative h-full z-30" :style="{ width: `${initialWidth}px` }">
      <!-- STAN 1: Użytkownik POSIADA zdjęcie w tle -->
      <template v-if="hasCoverImage">
        <div
          class="relative h-full flex items-center hover:brightness-95 cursor-pointer justify-center overflow-hidden rounded-b-xl shadow-xl w-full"
          @click="emit('view-cover')"
        >
          <img
            :src="props.imageUrl!"
            :alt="$t('media.zrodlowyObraz')"
            class="object-cover w-full h-full"
            @load="imageLoaded = true"
            crossOrigin="anonymous"
          />
        </div>
      </template>

      <!-- STAN 2: BRAK zdjęcia w tle -->
      <template v-else>
        <!-- Szary blok -->
        <div class="relative h-full bg-[#e4e6eb] rounded-b-xl overflow-hidden shadow-sm w-full">
          <!-- Przyciemnienie dolnej krawędzi -->
          <div
            v-if="isOwner"
            class="absolute bottom-0 left-0 right-0 h-28 bg-gradient-to-t from-black/40 to-transparent z-10 pointer-events-none"
          />
        </div>
      </template>

      <!-- Wspólny przycisk dodawania/edycji zdjęcia (Widoczny dla właściciela w obu stanach) -->
      <VDropdown
        v-if="isOwner"
        placement="bottom-end"
        :distance="8"
        class="absolute bottom-4 right-8 z-40"
      >
        <!-- Trigger: Przycisk główny -->
        <button
          class="bg-white hover:bg-gray-100 text-[#050505] font-semibold text-[15px] px-3.5 py-1.5 rounded-md shadow-sm flex items-center gap-2 transition-all active:scale-95 cursor-pointer"
        >
          <Camera :size="18" class="text-black" />
          <span>{{ hasCoverImage ? 'Edytuj zdjęcie w tle' : 'Dodaj zdjęcie w tle' }}</span>
        </button>

        <!-- Popper: Menu rozwijane -->
        <template #popper>
          <div class="bg-white p-1.5 min-w-[240px] flex flex-col gap-0.5 rounded-xl">
            <!-- Opcja 1: Wybierz zdjęcie -->
            <button
              class="w-full flex items-center gap-3 px-3 py-2.5 text-[15px] font-medium text-black hover:bg-gray-100 rounded-lg transition-colors text-left cursor-pointer"
            >
              <ImageOutline :size="20" class="text-black" />
              <span>{{ $t('media.wybierzZdjecieWTle') }}</span>
            </button>

            <!-- Opcja 2: Prześlij zdjęcie -->
            <button
              @click="emit('upload-cover')"
              class="w-full flex items-center gap-3 px-3 py-2.5 text-[15px] font-medium text-black hover:bg-gray-100 rounded-lg transition-colors text-left cursor-pointer"
            >
              <Upload :size="20" class="text-black" />
              <span>{{ $t('media.przeslijZdjecie') }}</span>
            </button>
          </div>
        </template>
      </VDropdown>
    </div>
  </div>
</template>

<style>
.v-popper__inner {
  border-radius: 0.75rem !important; /* rounded-xl */
  border: none !important;
  box-shadow:
    0 10px 15px -3px rgba(0, 0, 0, 0.1),
    0 4px 6px -2px rgba(0, 0, 0, 0.05) !important;
}
</style>
