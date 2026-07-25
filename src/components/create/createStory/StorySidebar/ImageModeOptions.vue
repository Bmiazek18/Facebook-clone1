<template>
  <div class="flex flex-col mt-2 pb-4">
    <div
      @click="emit('add-text')"
      class="flex items-center gap-3 px-4 py-2 hover:bg-gray-100 cursor-pointer transition-colors active:scale-95"
    >
      <div class="w-10 h-10 bg-gray-200 rounded-full flex items-center justify-center shrink-0">
        <FormatFont :size="24" class="text-black" />
      </div>
      <span class="font-semibold text-gray-900 text-[15px]">Dodaj tekst</span>
    </div>

    <div
      @click="!hasMusic && emit('toggle-music')"
      :class="[
        'flex items-center gap-3 px-4 py-2 transition-colors',
        hasMusic
          ? 'opacity-40 cursor-not-allowed pointer-events-none select-none'
          : 'hover:bg-gray-100 cursor-pointer active:scale-95',
      ]"
    >
      <div class="w-10 h-10 bg-gray-200 rounded-full flex items-center justify-center shrink-0">
        <MusicNote :size="24" class="text-black" />
      </div>
      <span class="font-semibold text-gray-900 text-[15px]">Dodaj muzykę</span>
    </div>

    <div
      @click="emit('add-link')"
      class="flex items-center gap-3 px-4 py-2 hover:bg-gray-100 cursor-pointer transition-colors active:scale-95"
    >
      <div class="w-10 h-10 bg-gray-200 rounded-full flex items-center justify-center shrink-0">
        <Link :size="24" class="text-black" />
      </div>
      <span class="font-semibold text-gray-900 text-[15px]">Dodaj link</span>
    </div>

    <div class="flex flex-col">
      <div
        @click="toggleAltTextSection"
        class="flex items-center justify-between px-4 py-2 hover:bg-gray-100 cursor-pointer transition-colors active:scale-95"
      >
        <div class="flex items-center gap-3">
          <div class="w-10 h-10 bg-gray-200 rounded-full flex items-center justify-center shrink-0">
            <AlphaABox :size="24" class="text-black" />
          </div>
          <span class="font-semibold text-gray-900 text-[15px]">Tekst alternatywny</span>
        </div>
      </div>

      <div v-if="isAltTextExpanded" class="px-4 py-2 animate-fade-in-down">
        <AltTextEditor
          :alt-text="currentAltText"
          @update:altText="(text: string) => (currentAltText = text)"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import FormatFont from 'vue-material-design-icons/FormatFont.vue'
import MusicNote from 'vue-material-design-icons/MusicNote.vue'
import Link from 'vue-material-design-icons/Link.vue'
import AlphaABox from 'vue-material-design-icons/AlphaABox.vue'
import AltTextEditor from '@/components/media/AltTextEditor.vue'

const props = defineProps<{
  isMusicModalOpen: boolean
  isImageSelected: boolean
  hasMusic?: boolean
}>()

const emit = defineEmits<{
  (e: 'add-text'): void
  (e: 'toggle-music'): void
  (e: 'add-link'): void
  (e: 'save-alt-text', text: string): void
}>()

const isAltTextExpanded = ref(false)
const currentAltText = ref('')

const toggleAltTextSection = () => {
  // Logika otwierania
  if (!props.isImageSelected) {
    // Możesz tutaj dodać toast zamiast alertu dla lepszego UX
    alert('Wybierz zdjęcie, aby edytować tekst alternatywny.')
    return
  }
  isAltTextExpanded.value = !isAltTextExpanded.value
}
</script>

<style scoped>
/* Opcjonalna animacja dla rozwijania alt textu */
.animate-fade-in-down {
  animation: fadeInDown 0.2s ease-out;
}
@keyframes fadeInDown {
  from {
    opacity: 0;
    transform: translateY(-5px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
