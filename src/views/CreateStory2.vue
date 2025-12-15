<template>
  <div class="flex h-screen w-full bg-[#F0F2F5] font-sans">

  <Sidebar :is-music-modal-open="false" :current-alt-text="''" :is-image-selected="false" />

    <main class="flex-1 flex items-center justify-center p-8 relative">

      <div class="flex gap-6">

        <div
          class="group relative w-[220px] h-[330px] rounded-xl overflow-hidden cursor-pointer shadow-sm hover:shadow-md transition-all duration-300 transform hover:scale-[1.02]"
          @click="openFilePicker"
        >
          <div class="absolute inset-0 bg-linear-to-b from-[#56C3F8] via-[#488AD4] to-[#2F5C96]"></div>

          <div class="relative z-10 flex flex-col items-center justify-center h-full gap-4">
            <div class="bg-white p-3 rounded-full shadow-md">
              <ImageMultiple class="text-gray-800" :size="28" />
            </div>
            <span class="text-white font-semibold text-center px-4">
              Utwórz relację ze zdjęciem
            </span>
          </div>
          <input type="file" accept="image/*" ref="fileInput" @change="onFileChange" class="hidden" />
        </div>

        <div
          class="group relative w-[220px] h-[330px] rounded-xl overflow-hidden cursor-pointer shadow-sm hover:shadow-md transition-all duration-300 transform hover:scale-[1.02]"
        >
          <div class="absolute inset-0 bg-linear-to-b from-[#AA50E1] via-[#B9449C] to-[#E94E5A]"></div>

          <div class="relative z-10 flex flex-col items-center justify-center h-full gap-4">
            <div class="bg-white p-3 rounded-full shadow-md">
              <FormatFont class="text-gray-800" :size="28" />
            </div>
            <span class="text-white font-semibold text-center px-4">
              Utwórz relację tekstową
            </span>
          </div>
        </div>

      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
// Import ikon z vue-material-design-icons
import ImageMultiple from 'vue-material-design-icons/ImageMultiple.vue';
import FormatFont from 'vue-material-design-icons/FormatFont.vue';
import Sidebar from '@/components/Sidebar.vue';
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { useTempStoryStore } from '@/stores/tempStory';

const fileInput = ref<HTMLInputElement | null>(null);
const router = useRouter();

const openFilePicker = () => {
  fileInput.value?.click();
};

  const onFileChange = (e: Event) => {
  const input = e.target as HTMLInputElement;
  const file = input.files && input.files[0];
  if (!file) return;

  // Preferred: store transiently in Pinia as an object URL to avoid sessionStorage quota issues
  try {
    const tempStore = useTempStoryStore();
    const url = tempStore.setFromFile(file);
    try { console.debug('[CreateStory2] setFromFile ->', url); } catch {}
    router.push({ name: 'createReel' });
    return;
  } catch {
    // fall through to FileReader fallback
  }

};

// Logika komponentu (tutaj prosta, bo to głównie UI)
</script>

<style scoped>
/* Dodatkowe style nie są konieczne dzięki Tailwind,
   ale upewnij się, że masz zdefiniowane fonty sans-serif */
</style>
