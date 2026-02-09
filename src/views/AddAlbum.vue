<script setup>
import { ref } from 'vue';
import CustomInput from '@/components/common/CustomInput.vue';
import CustomDropdown from '@/components/common/CustomDropdown.vue';

const sortByOptions = [
  { id: 'drag', title: 'Przeciągnij i upuść', description: 'Ułóż zdjęcia w dowolnej kolejności' },
  { id: 'date_asc', title: 'Data: od najstarszych', description: 'Najstarsze zdjęcia na początku' },
  { id: 'date_desc', title: 'Data: od najnowszych', description: 'Najnowsze zdjęcia na początku' },
];

const albumName = ref('');
const sortBy = ref('drag');
const files = ref([]);
const draggedItem = ref(null); // Przechowuje obiekt przeciąganego elementu

const handleFileUpload = (event) => {
  const uploadedFiles = Array.from(event.target.files);

  const newFiles = uploadedFiles.map(file => ({
    id: Math.random().toString(36).substr(2, 9),
    file: file,
    url: URL.createObjectURL(file),
    description: ''
  }));

  files.value = [...files.value, ...newFiles];
  event.target.value = '';
};

const removeFile = (id) => {
  files.value = files.value.filter(f => f.id !== id);
};

// --- LOGIKA LIVE DRAG & DROP ---

const onDragStart = (event, item) => {
  if (sortBy.value !== 'drag') {
    event.preventDefault();
    return;
  }
  draggedItem.value = item;

  // Ustawienie efektu kursora
  event.dataTransfer.effectAllowed = 'move';
  event.dataTransfer.dropEffect = 'move';
  // Opcjonalnie: ukryj "ducha" systemowego lub zostaw domyślny
  // event.dataTransfer.setDragImage(event.target, 0, 0);
};

// Funkcja wywoływana, gdy najeżdżamy na inny element
const onDragEnter = (targetItem) => {
  // Jeśli nie ciągniemy niczego lub najeżdżamy na ten sam element - stop
  if (sortBy.value !== 'drag' || !draggedItem.value || draggedItem.value.id === targetItem.id) {
    return;
  }

  // Znajdź indeksy
  const oldIndex = files.value.findIndex(f => f.id === draggedItem.value.id);
  const newIndex = files.value.findIndex(f => f.id === targetItem.id);

  if (oldIndex !== -1 && newIndex !== -1) {
    // Wyciągnij element i wstaw go w nowe miejsce (mutacja tablicy wywoła animację Vue)
    const itemToMove = files.value.splice(oldIndex, 1)[0];
    files.value.splice(newIndex, 0, itemToMove);
  }
};

const onDragEnd = () => {
  draggedItem.value = null;
};

</script>

<template>
  <div class="flex h-screen w-full bg-theme text-theme-text font-sans antialiased">

    <aside class="w-[360px] bg-theme-bg-secondary border-r border-theme-border flex flex-col shadow-2xl z-10 shrink-0">
      <div class="p-4 border-b border-theme-border">
        <h1 class="text-xl font-bold">Utwórz album</h1>
      </div>

      <div class="p-4 flex-1 overflow-y-auto space-y-4">
        <div class="bg-theme-bg-tertiary p-1.5 px-3 rounded-md inline-flex items-center gap-2 text-[13px] font-semibold">
          <span class="text-theme-text-secondary">👤</span> Grupa prywatna
        </div>

        <CustomInput
            id="album-input"
            label="Nazwa albumu"
            v-model="albumName"
        />

        <label class="flex items-center justify-center gap-2 w-full bg-theme-bg-tertiary hover:bg-theme-hover-strong transition-colors cursor-pointer rounded-lg p-2.5 font-semibold text-theme-primary">
          <span class="text-xl">+</span> Prześlij zdjęcia lub filmy
          <input type="file" multiple class="hidden" @change="handleFileUpload" accept="image/*,video/*" />
        </label>

        <CustomDropdown
          label="Sortuj według"
          v-model="sortBy"
          :options="sortByOptions"
        />

        <button class="flex items-center gap-2 w-full bg-theme-bg-tertiary opacity-50 cursor-not-allowed rounded-lg p-2.5 font-semibold text-sm">
          🕒 Użyj daty ze zdjęć
        </button>
      </div>

      <div class="p-4 border-t border-theme-border">
        <button :disabled="!albumName || files.length === 0"
          class="w-full py-2 rounded-lg font-semibold transition-all bg-theme-primary text-white hover:bg-theme-primary-hover disabled:bg-theme-bg-tertiary disabled:text-theme-text-secondary disabled:cursor-not-allowed">
          Opublikuj
        </button>
      </div>
    </aside>

    <main class="flex-1 overflow-y-auto p-6 scrollbar-hide">

      <div v-if="files.length === 0" class="h-full flex flex-col items-center justify-center text-center">
        <div class="w-20 h-20 mb-4 bg-theme-bg-secondary rounded-xl flex items-center justify-center border border-theme-border relative">
          <div class="w-10 h-10 border-2 border-theme-border rounded bg-theme-bg-tertiary"></div>
          <div class="absolute -top-1 -right-1 w-5 h-5 bg-theme-primary rounded-full border-4 border-theme-bg"></div>
        </div>
        <h2 class="text-xl font-bold text-theme-text-secondary">Może coś dodasz?</h2>
<p class="text-theme-text-secondary">Przeciągnij zdjęcia i filmy tutaj, aby rozpocząć.</p>
      </div>

      <div v-else>
        <TransitionGroup
          name="list"
          tag="div"
          class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4 pb-20 relative"
        >
          <div
            v-for="item in files"
            :key="item.id"
            :draggable="sortBy === 'drag'"
            @dragstart="onDragStart($event, item)"
            @dragenter.prevent="onDragEnter(item)"
            @dragover.prevent
            @dragend="onDragEnd"
            class="bg-theme-bg-secondary rounded-lg border border-theme-border flex flex-col relative overflow-hidden shadow-md group transition-all duration-300"
            :class="{
              'cursor-move hover:shadow-xl': sortBy === 'drag',
              // STYL DLA PRZECIĄGANEGO ELEMENTU (Niebieska ramka, półprzezroczystość)
              'opacity-40 border-2 border-dashed border-theme-primary scale-95 bg-theme-bg': draggedItem && draggedItem.id === item.id
            }"
          >

            <button @click="removeFile(item.id)"
                    class="absolute top-2 right-2 z-20 bg-theme-bg/60 hover:bg-theme-bg/90 text-theme-text p-1.5 rounded-full transition-colors backdrop-blur-sm">
              <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/></svg>
            </button>

            <div class="w-full aspect-square bg-black flex items-center justify-center overflow-hidden relative select-none">
              <img :src="item.url" class="w-full h-full object-cover transition-transform duration-500 group-hover:scale-105" draggable="false" />
            </div>

            <div class="p-3 flex flex-col gap-2">
              <div class="relative">
                <textarea
                  v-model="item.description"
                  placeholder="Opis (opcjonalnie)"
                  class="w-full bg-theme-bg-tertiary rounded-lg p-2.5 text-sm text-theme-text placeholder:text-theme-text-secondary resize-none border border-transparent focus:border-theme-primary/50 focus:bg-theme-bg-tertiary/80 outline-none transition-all h-[60px]"></textarea>
              </div>

              <div class="flex items-center justify-between pt-1">
                 <div class="flex gap-3">
                    <button title="Oznacz osoby" class="text-theme-text-secondary hover:text-theme-text transition-colors">🏷️</button>
                    <button title="Dodaj lokalizację" class="text-theme-text-secondary hover:text-theme-text transition-colors">📍</button>
                    <button title="Czas" class="text-theme-text-secondary hover:text-theme-text transition-colors">🕒</button>
                 </div>
              </div>
            </div>

          </div>
        </TransitionGroup>
      </div>

    </main>
  </div>
</template>

<style scoped>
/* Ukrycie domyślnego scrollbara */
.scrollbar-hide::-webkit-scrollbar {
  display: none;
}
.scrollbar-hide {
  -ms-overflow-style: none;
  scrollbar-width: none;
}

/* ANIMACJE DRAG & DROP (Vue FLIP) */
/* 1. Element, który zmienia pozycję w liście (inne ustępują miejsca) */
.list-move {
  transition: transform 0.3s ease;
}

/* 2. Styl podczas upuszczania (opcjonalny fade) */
.list-enter-active,
.list-leave-active {
  transition: all 0.3s ease;
}
.list-enter-from,
.list-leave-to {
  opacity: 0;
  transform: scale(0.9);
}

/* Ważne: zapewnia płynność animacji elementów, które nie są ciągnięte */
.list-leave-active {
  position: absolute;
}
</style>
