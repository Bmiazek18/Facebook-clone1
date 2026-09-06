<template>
  <div v-if="image" class="bg-gray-800 rounded-lg p-4 space-y-3">
    <h4 class="text-white font-semibold">{{ $t('create.edycjaObrazka') }}</h4>

    <div>
      <label class="text-gray-400 text-sm block mb-1">{{ $t('create.rotacjaImageRotation') }}</label>
      <input
        :model-value="image.rotation"
        @input="
          emit('update', { ...image, rotation: Number(($event.target as HTMLInputElement).value) })
        "
        type="range"
        min="0"
        max="360"
        class="w-full"
      />
    </div>

    <div>
      <label class="text-gray-400 text-sm block mb-1"
        >{{ $t('create.przezroczystoscImageOpacity100') }}</label
      >
      <input
        :model-value="image.opacity"
        @input="
          emit('update', { ...image, opacity: Number(($event.target as HTMLInputElement).value) })
        "
        type="range"
        min="0"
        max="1"
        step="0.1"
        class="w-full"
      />
    </div>

    <div class="grid grid-cols-2 gap-2">
      <div>
        <label class="text-gray-400 text-sm block mb-1">{{ $t('create.startS') }}</label>
        <input
          :model-value="image.startTime"
          @input="
            emit('update', {
              ...image,
              startTime: Number(($event.target as HTMLInputElement).value),
            })
          "
          type="number"
          step="0.1"
          class="w-full bg-gray-700 text-white px-3 py-2 rounded border border-gray-600 focus:border-blue-500 outline-none"
        />
      </div>
      <div>
        <label class="text-gray-400 text-sm block mb-1">{{ $t('create.koniecS') }}</label>
        <input
          :model-value="image.endTime"
          @input="
            emit('update', { ...image, endTime: Number(($event.target as HTMLInputElement).value) })
          "
          type="number"
          step="0.1"
          class="w-full bg-gray-700 text-white px-3 py-2 rounded border border-gray-600 focus:border-blue-500 outline-none"
        />
      </div>
    </div>

    <div>
      <label class="text-gray-400 text-sm block mb-1">{{ $t('create.animacjaWejscia') }}</label>
      <select
        :model-value="image.entryAnimation"
        @change="
          emit('update', { ...image, entryAnimation: ($event.target as HTMLSelectElement).value })
        "
        class="w-full bg-gray-700 text-white px-3 py-2 rounded border border-gray-600 focus:border-blue-500 outline-none"
      >
        <option value="none">{{ $t('create.brak') }}</option>
        <option value="fade-in">{{ $t('create.fadeIn') }}</option>
        <option value="zoom-in">{{ $t('create.zoomIn') }}</option>
        <option value="slide-in-left">{{ $t('create.slideIn') }}</option>
        <option value="slide-in-right">{{ $t('create.slideIn2') }}</option>
      </select>
    </div>

    <div>
      <label class="text-gray-400 text-sm block mb-1">{{ $t('create.animacjaWyjscia') }}</label>
      <select
        :model-value="image.exitAnimation"
        @change="
          emit('update', { ...image, exitAnimation: ($event.target as HTMLSelectElement).value })
        "
        class="w-full bg-gray-700 text-white px-3 py-2 rounded border border-gray-600 focus:border-blue-500 outline-none"
      >
        <option value="none">{{ $t('create.brak') }}</option>
        <option value="fade-out">{{ $t('create.fadeOut') }}</option>
        <option value="zoom-out">{{ $t('create.zoomOut') }}</option>
        <option value="slide-out-left">{{ $t('create.slideOut') }}</option>
        <option value="slide-out-right">{{ $t('create.slideOut2') }}</option>
      </select>
    </div>

    <button
      @click="emit('delete')"
      class="w-full bg-red-600 hover:bg-red-700 text-white py-2 rounded-lg transition-colors"
    >{{ $t('create.usunObrazek') }}</button>
  </div>
</template>

<script setup lang="ts">
import type { ImageOverlay } from '@/types/video-editor.types'

interface Props {
  image: ImageOverlay | null
}

defineProps<Props>()

const emit = defineEmits<{
  update: [image: ImageOverlay]
  delete: []
}>()
</script>
