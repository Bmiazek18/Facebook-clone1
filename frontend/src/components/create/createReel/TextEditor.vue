<template>
  <div v-if="text" class="bg-gray-800 rounded-lg p-4 space-y-3">
    <h4 class="text-white font-semibold">{{ $t('create.edycjaTekstu') }}</h4>

    <div>
      <label class="text-gray-400 text-sm block mb-1">{{ $t('create.tresc') }}</label>
      <input
        :model-value="text.content"
        @input="emit('update', { ...text, content: ($event.target as HTMLInputElement).value })"
        type="text"
        class="w-full bg-gray-700 text-white px-3 py-2 rounded border border-gray-600 focus:border-blue-500 outline-none"
        :placeholder="$t('create.wpiszTekst')"
      />
    </div>

    <div>
      <label class="text-gray-400 text-sm block mb-1">{{ $t('create.rozmiarTextFontsizePx') }}</label>
      <input
        :model-value="text.fontSize"
        @input="
          emit('update', { ...text, fontSize: Number(($event.target as HTMLInputElement).value) })
        "
        type="range"
        min="12"
        max="200"
        class="w-full"
      />
    </div>

    <div>
      <label class="text-gray-400 text-sm block mb-1">{{ $t('create.kolor') }}</label>
      <input
        :model-value="text.color"
        @input="emit('update', { ...text, color: ($event.target as HTMLInputElement).value })"
        type="color"
        class="w-full h-10 rounded border border-gray-600"
      />
    </div>

    <div class="grid grid-cols-2 gap-2">
      <div>
        <label class="text-gray-400 text-sm block mb-1">{{ $t('create.startS') }}</label>
        <input
          :model-value="text.startTime"
          @input="
            emit('update', {
              ...text,
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
          :model-value="text.endTime"
          @input="
            emit('update', { ...text, endTime: Number(($event.target as HTMLInputElement).value) })
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
        :model-value="text.entryAnimation"
        @change="
          emit('update', { ...text, entryAnimation: ($event.target as HTMLSelectElement).value })
        "
        class="w-full bg-gray-700 text-white px-3 py-2 rounded border border-gray-600 focus:border-blue-500 outline-none"
      >
        <option value="none">{{ $t('create.brak') }}</option>
        <option value="fade-in">{{ $t('create.fadeIn') }}</option>
        <option value="slide-in-left">{{ $t('create.slideIn') }}</option>
        <option value="slide-in-right">{{ $t('create.slideIn2') }}</option>
        <option value="slide-in-top">{{ $t('create.slideIn3') }}</option>
        <option value="slide-in-bottom">{{ $t('create.slideIn4') }}</option>
        <option value="zoom-in">{{ $t('create.zoomIn') }}</option>
        <option value="pop-in">{{ $t('create.popIn') }}</option>
        <option value="typewriter">{{ $t('create.typewriter') }}</option>
      </select>
    </div>

    <div v-if="text.entryAnimation && text.entryAnimation !== 'none'">
      <label class="text-gray-400 text-sm block mb-1"
        >{{ $t('create.czasWejsciaTextEntryduration') }}</label
      >
      <input
        :model-value="text.entryDuration"
        @input="
          emit('update', {
            ...text,
            entryDuration: Number(($event.target as HTMLInputElement).value),
          })
        "
        type="range"
        min="0.1"
        max="3"
        step="0.1"
        class="w-full"
      />
    </div>

    <div>
      <label class="text-gray-400 text-sm block mb-1">{{ $t('create.animacjaCiagla') }}</label>
      <select
        :model-value="text.loopAnimation"
        @change="
          emit('update', { ...text, loopAnimation: ($event.target as HTMLSelectElement).value })
        "
        class="w-full bg-gray-700 text-white px-3 py-2 rounded border border-gray-600 focus:border-blue-500 outline-none"
      >
        <option value="none">{{ $t('create.brak') }}</option>
        <option value="pulse">{{ $t('create.pulsePulsowanie') }}</option>
        <option value="float">{{ $t('create.floatUnoszenie') }}</option>
        <option value="shake">{{ $t('create.shakeTrzesienie') }}</option>
      </select>
    </div>

    <div>
      <label class="text-gray-400 text-sm block mb-1">{{ $t('create.animacjaWyjscia') }}</label>
      <select
        :model-value="text.exitAnimation"
        @change="
          emit('update', { ...text, exitAnimation: ($event.target as HTMLSelectElement).value })
        "
        class="w-full bg-gray-700 text-white px-3 py-2 rounded border border-gray-600 focus:border-blue-500 outline-none"
      >
        <option value="none">{{ $t('create.brak') }}</option>
        <option value="fade-out">{{ $t('create.fadeOut') }}</option>
        <option value="slide-out-left">{{ $t('create.slideOut') }}</option>
        <option value="slide-out-right">{{ $t('create.slideOut2') }}</option>
        <option value="slide-out-top">{{ $t('create.slideOut3') }}</option>
        <option value="slide-out-bottom">{{ $t('create.slideOut4') }}</option>
        <option value="zoom-out">{{ $t('create.zoomOut') }}</option>
        <option value="pop-out">{{ $t('create.popOut') }}</option>
      </select>
    </div>

    <div v-if="text.exitAnimation && text.exitAnimation !== 'none'">
      <label class="text-gray-400 text-sm block mb-1">{{ $t('create.czasWyjsciaTextExitduration') }}</label>
      <input
        :model-value="text.exitDuration"
        @input="
          emit('update', {
            ...text,
            exitDuration: Number(($event.target as HTMLInputElement).value),
          })
        "
        type="range"
        min="0.1"
        max="3"
        step="0.1"
        class="w-full"
      />
    </div>

    <button
      @click="emit('delete')"
      class="w-full bg-red-600 hover:bg-red-700 text-white py-2 rounded-lg transition-colors"
    >{{ $t('create.usunTekst') }}</button>
  </div>
</template>

<script setup lang="ts">
import type { TextOverlay } from '@/types/video-editor.types'

interface Props {
  text: TextOverlay | null
}

defineProps<Props>()

const emit = defineEmits<{
  update: [text: TextOverlay]
  delete: []
}>()
</script>
