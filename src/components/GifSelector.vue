<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { GiphyFetch } from '@giphy/js-fetch-api';
import { useI18n } from 'vue-i18n';

const emit = defineEmits<{
  (e: 'confirm', url: string): void;
}>();

const { t } = useI18n();
const searchTerm = ref('');
const gifs = ref<any[]>([]);
const loading = ref(false);
const gf = new GiphyFetch(import.meta.env.VITE_GIPHY_KEY as string);

const fetchGifs = async () => {
  loading.value = true;
  try {
    const res = searchTerm.value
      ? await gf.search(searchTerm.value, { limit: 20 })
      : await gf.trending({ limit: 20 });

    gifs.value = res.data;
  } finally {
    loading.value = false;
  }
};

const debounce = (func: Function, wait: number) => {
  let timeout: any;
  return (...args: any) => {
    clearTimeout(timeout);
    timeout = setTimeout(() => func(...args), wait);
  };
};

const handleGifSearch = debounce(() => fetchGifs(), 500);

const selectGif = (url: string) => {
  emit('confirm', url);
};

onMounted(() => fetchGifs());
</script>

<template>
  <div class="flex flex-col h-[600px] bg-white rounded-t-xl overflow-hidden">
    <div class="p-3 sticky top-0 bg-white z-10 border-b border-gray-50">
      <div class="relative flex items-center">
        <span class="absolute left-4 text-gray-400">
          <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
          </svg>
        </span>
        <input
          type="text"
          v-model="searchTerm"
          @input="handleGifSearch"
          class="w-full bg-[#F0F2F5] border-none rounded-full py-2 pl-11 pr-4 text-[16px] focus:ring-0 outline-none placeholder-gray-500"
          placeholder="Szukaj"
        />
      </div>
    </div>

    <div class="flex-1 overflow-y-auto custom-scrollbar">
      <div v-if="loading && gifs.length === 0" class="flex justify-center py-10">
        <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-gray-400"></div>
      </div>

      <div v-else class="flex flex-col">
        <div
          v-for="gif in gifs"
          :key="gif.id"
          class="w-full mb-[2px] cursor-pointer active:opacity-80 transition-opacity"
          @click="selectGif(gif.images.fixed_height.url)"
        >
          <img
            :src="gif.images.fixed_height.url"
            :alt="gif.title"
            class="w-full h-auto block"
            loading="lazy"
          />
        </div>
      </div>

      <div v-if="!loading && gifs.length === 0" class="text-center text-gray-400 py-10">
        Brak wyników
      </div>
    </div>
  </div>
</template>

<style scoped>
.custom-scrollbar::-webkit-scrollbar {
  width: 5px;
}
.custom-scrollbar::-webkit-scrollbar-thumb {
  background: #e0e0e0;
  border-radius: 10px;
}
</style>
