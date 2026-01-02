<template>
  <div class=""
       @mouseup="stopDrag"
       @mouseleave="stopDrag"
       @mousemove="onDrag">

    <div class="bg-white rounded-2xl shadow-2xl w-full overflow-hidden flex flex-col max-h-[90vh]">

      <div class="p-6 overflow-y-auto">
        <div class="mb-6">
          <textarea
            v-model="description"
            placeholder="Opis"
            class="w-full h-24 p-3 border border-gray-200 rounded-xl outline-none resize-none text-gray-800 placeholder-gray-400 text-base focus:border-blue-500 transition-colors"
          ></textarea>
        </div>

        <div class="flex flex-col items-center">

          <div
            ref="containerRef"
            class="relative w-full sm:h-[380px] h-[300px] bg-gray-50 rounded-lg overflow-hidden mb-8 cursor-grab active:cursor-grabbing select-none ring-1 ring-gray-100 touch-none"
            @mousedown="startDrag"
            @touchstart="startDrag"
            @touchmove="onDrag"
            @touchend="stopDrag"
          >
             <div class="absolute inset-0 flex items-center justify-center pointer-events-none overflow-hidden">

               <img
                ref="imageRef"
                src="https://images.unsplash.com/photo-1542831371-29b0f74f9713?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80"
                alt="Podgląd"
                class="max-w-none pointer-events-auto"
                @load="onImageLoad"
                :style="{
                  // Jeśli obrazek szerszy niż kontener -> dopasuj wysokość (height: 100%), szerokość auto
                  // Jeśli obrazek wyższy niż kontener -> dopasuj szerokość (width: 100%), wysokość auto
                  width: imageAspectRatio > containerRatio ? 'auto' : '100%',
                  height: imageAspectRatio > containerRatio ? '100%' : 'auto',

                  transform: `translate(${position.x}px, ${position.y}px) scale(${zoom / 100})`,
                  transition: isDragging ? 'none' : 'transform 0.1s ease-out'
                }"
               />
             </div>

          <div
               ref="cropRef"
               class="absolute left-1/2 top-1/2 -translate-x-1/2 -translate-y-1/2 w-[240px] h-[240px] sm:w-[280px] sm:h-[280px] pointer-events-none z-10 transition-all duration-300 ease-in-out"
               :style="{

                 boxShadow: isCropMode
                    ? '0 0 0 9999px rgba(255, 255, 255, 1)'
                    : '0 0 0 9999px rgba(255, 255, 255, 0.85)',


                 borderRadius: isCropMode ? '0%' : '50%',

               }"
             >
                <div
                  v-if="isCropMode"
                  class="absolute inset-0 "
                  :style="{
                    background: 'radial-gradient(circle closest-side, transparent 98%, rgba(255, 255, 255, 0.85) 100%)'
                             }"
                ></div>


             </div>
          </div>

          <div class="flex items-center w-full max-w-[380px] gap-6 mb-8 px-2">
            <button @click="changeZoom(-10)" class="text-gray-400 hover:text-gray-800 transition-colors">
              <minus-icon :size="20" />
            </button>

            <div class="relative w-full h-6 flex items-center">
               <input
                type="range"
                :min="minZoom"
                max="250"
                step="1"
                v-model="zoom"
                @input="checkBounds"
                class="custom-slider w-full h-1 bg-gray-200 rounded-lg appearance-none cursor-pointer"
              />
            </div>

            <button @click="changeZoom(10)" class="text-gray-400 hover:text-gray-800 transition-colors">
              <plus-icon :size="20" />
            </button>
          </div>

          <div class="flex gap-3 w-full max-w-[380px] mb-6">
            <button
              @click="isCropMode = !isCropMode"
              class="flex-1 flex items-center justify-center gap-2 px-4 py-2.5 rounded-lg text-sm font-semibold transition-all duration-200 border"
              :class="isCropMode ? 'bg-blue-600 text-white border-blue-600 shadow-md shadow-blue-200' : 'bg-white text-gray-700 border-gray-200 hover:bg-gray-50'"
            >
              <crop-icon :size="18" />
              {{ isCropMode ? 'Zatwierdź' : 'Przytnij zdjęcie' }}
            </button>

            <button class="flex-1 flex items-center justify-center gap-2 px-4 py-2.5 bg-white border border-gray-200 hover:bg-gray-50 rounded-lg text-sm font-semibold text-gray-700 transition-colors">
              <clock-outline-icon :size="18" />
              Tymczasowe
            </button>
          </div>
        </div>

        <div class="text-xs text-gray-500 leading-relaxed text-center sm:text-left">
          Twoje zdjęcie profilowe dla profilu <span class="font-bold text-gray-900">Bartosz Miazek</span> zostanie także zaktualizowane na platformach Instagram.
        </div>
      </div>

      <div class="border-t border-gray-100 p-4 flex justify-end gap-3 bg-white">
        <button class="px-6 py-2 text-blue-600 font-semibold text-sm hover:bg-blue-50 rounded-lg transition-colors">
          Anuluj
        </button>
        <button class="px-8 py-2 bg-blue-600 text-white font-semibold text-sm rounded-lg hover:bg-blue-700 transition-colors shadow-md shadow-blue-200">
          Zapisz
        </button>
      </div>

    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, nextTick } from 'vue';

import MinusIcon from 'vue-material-design-icons/Minus.vue';
import PlusIcon from 'vue-material-design-icons/Plus.vue';
import CropIcon from 'vue-material-design-icons/Crop.vue';
import ClockOutlineIcon from 'vue-material-design-icons/ClockOutline.vue';

// --- STAN ---
const description = ref<string>('');
const zoom = ref<number>(100);
const minZoom = ref<number>(50);
const isCropMode = ref<boolean>(false);

const containerRef = ref<HTMLElement | null>(null);
const imageRef = ref<HTMLImageElement | null>(null);
const cropRef = ref<HTMLElement | null>(null);

const position = reactive<{ x: number; y: number }>({ x: 0, y: 0 });
const imageAspectRatio = ref<number>(1);
const containerRatio = ref<number>(1); // Dodano zmienną reaktywną dla kontenera

let isDragging: boolean = false;
let startMouseX: number = 0;
let startMouseY: number = 0;
let startPosX: number = 0;
let startPosY: number = 0;

// --- LOGIKA ---

const onImageLoad = (): void => {
  // Używamy nextTick, aby upewnić się, że width/height kontenera są przeliczone
  nextTick(() => {
    if (!imageRef.value || !containerRef.value || !cropRef.value) return;

    const { naturalWidth, naturalHeight } = imageRef.value;
    const containerW = containerRef.value.clientWidth;
    const containerH = containerRef.value.clientHeight;

    // Obliczamy proporcje
    imageAspectRatio.value = naturalWidth / naturalHeight;
    containerRatio.value = containerW / containerH;

    position.x = 0;
    position.y = 0;

    // --- OBLICZANIE STARTOWEGO ZOOMU ---
    const cropW = cropRef.value.clientWidth;
    const cropH = cropRef.value.clientHeight;

    let baseRenderedW: number, baseRenderedH: number;

    // Logika zgodna z nowym stylem width/height w template:
    if (imageAspectRatio.value > containerRatio.value) {
      // Obrazek "poziomy" względem kontenera -> height: 100%
      baseRenderedH = containerH;
      baseRenderedW = containerH * imageAspectRatio.value;
    } else {
      // Obrazek "pionowy" względem kontenera -> width: 100%
      baseRenderedW = containerW;
      baseRenderedH = containerW / imageAspectRatio.value;
    }

    // Szukamy skali, by zakryć koło
    const scaleToFitCrop = Math.max(cropW / baseRenderedW, cropH / baseRenderedH);
    const newMinZoom = Math.ceil(scaleToFitCrop * 100);

    minZoom.value = newMinZoom;
    zoom.value = newMinZoom;

    // Na wszelki wypadek sprawdzamy granice od razu
    checkBounds();
  });
};

const calculateBounds = (): { maxX: number; maxY: number } => {
  if (!containerRef.value || !cropRef.value) return { maxX: 0, maxY: 0 };

  const containerW = containerRef.value.clientWidth;
  const containerH = containerRef.value.clientHeight;
  const cropW = cropRef.value.clientWidth;
  const cropH = cropRef.value.clientHeight;

  const currentScale = zoom.value / 100;

  let renderedW: number, renderedH: number;

  // Ta sama logika co w onImageLoad i template
  if (imageAspectRatio.value > containerRatio.value) {
    renderedH = containerH * currentScale;
    renderedW = (containerH * imageAspectRatio.value) * currentScale;
  } else {
    renderedW = containerW * currentScale;
    renderedH = (containerW / imageAspectRatio.value) * currentScale;
  }

  const maxX = Math.max(0, (renderedW - cropW) / 2);
  const maxY = Math.max(0, (renderedH - cropH) / 2);

  return { maxX, maxY };
};

const checkBounds = (): void => {
  const bounds = calculateBounds();
  position.x = Math.max(-bounds.maxX, Math.min(position.x, bounds.maxX));
  position.y = Math.max(-bounds.maxY, Math.min(position.y, bounds.maxY));
};

const changeZoom = (amount: number): void => {
  const newZoom = zoom.value + amount;
  if (newZoom >= minZoom.value && newZoom <= 250) {
    zoom.value = newZoom;
    checkBounds();
  }
};

const getClientCoords = (e: MouseEvent | TouchEvent): { x: number; y: number } => {
  if (e instanceof TouchEvent && e.touches && e.touches.length > 0) {
    return { x: e.touches[0].clientX, y: e.touches[0].clientY };
  } else if (e instanceof MouseEvent) {
    return { x: e.clientX, y: e.clientY };
  }
  return { x: 0, y: 0 }; // Fallback
};

const startDrag = (e: MouseEvent | TouchEvent): void => {
  isDragging = true;
  const coords = getClientCoords(e);
  startMouseX = coords.x;
  startMouseY = coords.y;
  startPosX = position.x;
  startPosY = position.y;
};

const onDrag = (e: MouseEvent | TouchEvent): void => {
  if (!isDragging) return;
  if (e.cancelable) e.preventDefault();

  const coords = getClientCoords(e);
  const deltaX = coords.x - startMouseX;
  const deltaY = coords.y - startMouseY;

  const bounds = calculateBounds();

  position.x = Math.max(-bounds.maxX, Math.min(startPosX + deltaX, bounds.maxX));
  position.y = Math.max(-bounds.maxY, Math.min(startPosY + deltaY, bounds.maxY));
};

const stopDrag = (): void => { isDragging = false; };

onMounted(() => {
  // Sprawdzamy, czy obrazek nie załadował się już wcześniej (z cache)
  nextTick(() => {
    if (imageRef.value && imageRef.value.complete) {
      onImageLoad();
    }
  });
});
</script>

<style scoped>
.custom-slider {
  -webkit-appearance: none;
  background: transparent;
}
.custom-slider::-webkit-slider-thumb {
  -webkit-appearance: none;
  height: 20px;
  width: 20px;
  border-radius: 50%;
  background: #2563EB;
  cursor: pointer;
  margin-top: -8px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.3);
}
.custom-slider::-webkit-slider-runnable-track {
  width: 100%;
  height: 4px;
  cursor: pointer;
  background: #E5E7EB;
  border-radius: 2px;
}
</style>
