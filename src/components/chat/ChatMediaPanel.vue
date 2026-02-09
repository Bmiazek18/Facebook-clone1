<template>
  <div class="h-full flex flex-col bg-white">

    <div class="flex items-center px-4 py-4">
      <button
        @click="$emit('close')"
        class="mr-4 hover:bg-gray-100 rounded-full p-1 transition"
        aria-label="Powrót"
      >
        <ArrowLeftIcon :size="24" class="text-black" />
      </button>
      <h2 class="text-[17px] font-bold text-gray-900">Multimedia i pliki</h2>
    </div>

    <div class="flex border-b border-gray-200 px-4">
      <button class="px-4 py-3 font-semibold text-[14px] text-blue-600 border-b-[3px] border-blue-600">
         Multimedia
      </button>
      <button class="px-4 py-3 font-semibold text-[14px] text-gray-500 hover:bg-gray-50 rounded-t-md transition ml-2">
         Pliki
      </button>
    </div>

    <div class="flex-1 overflow-y-auto custom-scrollbar p-1">

       <div v-for="(group, index) in groupedPhotos" :key="index" class="mb-4">

          <div class="p-2 pt-3">
             <h3 class="font-bold text-[15px] text-gray-900 capitalize">{{ group.title }}</h3>
          </div>

          <div class="grid grid-cols-3 gap-1 px-1">
             <div
                v-for="photo in group.items"
                :key="photo.id"
                class="relative aspect-square bg-gray-100 cursor-pointer overflow-hidden group"
             >
                <img :src="photo.url" loading="lazy" class="w-full h-full object-cover">
                <div class="absolute inset-0 bg-black/10 opacity-0 group-hover:opacity-100 transition"></div>
             </div>
          </div>

       </div>

       <div v-if="groupedPhotos.length === 0" class="text-center py-10 text-gray-500 text-sm">
          Brak multimediów
       </div>

    </div>

  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';
import ArrowLeftIcon from 'vue-material-design-icons/ArrowLeft.vue';

interface Photo {
  id: number;
  url: string;
  uploadDate: string;
}

// Przyjmijmy, że "dziś" to rok 2025 dla celów demonstracyjnych
// Dane testowe zawierają rok bieżący (2025) i poprzedni (2024)
const mockDatabase = ref<Photo[]>([
  // --- BIEŻĄCY ROK (pokaże tylko miesiąc) ---
  { id: 1, url: 'https://picsum.photos/300?random=1', uploadDate: '2025-12-20T10:00:00' },
  { id: 2, url: 'https://picsum.photos/300?random=2', uploadDate: '2025-12-15T14:30:00' },
  { id: 3, url: 'https://picsum.photos/300?random=3', uploadDate: '2025-11-28T11:20:00' },

  // --- POPRZEDNI ROK (pokaże miesiąc + rok) ---
  { id: 4, url: 'https://picsum.photos/300?random=4', uploadDate: '2024-12-10T16:45:00' },
  { id: 5, url: 'https://picsum.photos/300?random=5', uploadDate: '2024-05-30T12:00:00' },

  // --- JESZCZE STARSZE ---
  { id: 6, url: 'https://picsum.photos/300?random=6', uploadDate: '2023-01-05T18:00:00' },
]);

const groupedPhotos = computed(() => {
  // 1. Sortowanie od najnowszych
  const sorted = [...mockDatabase.value].sort((a, b) => {
    return new Date(b.uploadDate).getTime() - new Date(a.uploadDate).getTime();
  });

  const groups: { title: string; items: Photo[] }[] = [];

  // Pobieramy aktualny rok systemowy
  const currentYear = new Date().getFullYear();

  sorted.forEach((photo) => {
    const date = new Date(photo.uploadDate);
    const photoYear = date.getFullYear();

    let monthName = '';

    // 2. Warunek formatowania nazwy grupy
    if (photoYear === currentYear) {
      // Jeśli ten sam rok -> Tylko miesiąc (np. "grudzień")
      monthName = date.toLocaleString('pl-PL', { month: 'long' });
    } else {
      // Jeśli inny rok -> Miesiąc i rok (np. "grudzień 2024")
      monthName = date.toLocaleString('pl-PL', { month: 'long', year: 'numeric' });
    }

    let lastGroup = groups[groups.length - 1];

    // Sprawdzamy czy ostatnia grupa ma ten sam tytuł
    if (!lastGroup || lastGroup.title !== monthName) {
      lastGroup = { title: monthName, items: [] };
      groups.push(lastGroup);
    }

    lastGroup.items.push(photo);
  });

  return groups;
});
</script>

<style scoped>
.custom-scrollbar::-webkit-scrollbar {
  width: 6px;
}
.custom-scrollbar::-webkit-scrollbar-track {
  background: transparent;
}
.custom-scrollbar::-webkit-scrollbar-thumb {
  background-color: #e5e7eb;
  border-radius: 20px;
}
</style>
