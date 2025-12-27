<script setup lang="ts">
import { ref, computed } from 'vue';

// Definicja zdarzeń
const emit = defineEmits(['feeling-selected', 'back']);

// --- STAN ---
const activeTab = ref('activities');
const searchQuery = ref('');
const selectedActivity = ref<any>(null);

// --- DANE: UCZUCIA (Już były emotkami) ---
const feelings = [
  { emoji: '🙂', label: 'szczęśliwy' },
  { emoji: '🥰', label: 'kochany' },
  { emoji: '🤩', label: 'rewelacyjnie' },
  { emoji: '😆', label: 'podekscytowany' },
  { emoji: '🤪', label: 'szalony' },
  { emoji: '😊', label: 'błogo' },
  { emoji: '😛', label: 'głupio' },
  { emoji: '🙂', label: 'cudownie' },
  { emoji: '😟', label: 'smutny' },
  { emoji: '😃', label: 'wdzięczny' },
  { emoji: '😍', label: 'zakochany' },
  { emoji: '😴', label: 'zrelaksowany' },
];

// --- DANE: KATEGORIE GŁÓWNE (Teraz z Emotkami) ---
const activities = [
  { id: 'celebrating', label: 'Świętuje...', emoji: '🎉', colorBg: 'bg-yellow-100' }, // Zmieniłem tła na jaśniejsze, żeby emotki były czytelne
  { id: 'watching', label: 'Ogląda...', emoji: '📺', colorBg: 'bg-red-100' },
  { id: 'eating', label: 'Je...', emoji: '🍴', colorBg: 'bg-green-100' },
  { id: 'drinking', label: 'Pije...', emoji: '🥤', colorBg: 'bg-orange-100' },
  { id: 'attending', label: 'Bierze udział...', emoji: '📅', colorBg: 'bg-blue-100' },
  { id: 'traveling', label: 'Podróżuje do...', emoji: '✈️', colorBg: 'bg-purple-100' },
];

// --- DANE: PODKATEGORIE (Emotki zamiast ikon) ---
// Klucze odpowiadają `id` z tablicy activities
const subCategories: Record<string, Array<{label: string, emoji: string}>> = {
  celebrating: [
    { label: 'urodziny', emoji: '🎂' },
    { label: 'przyjaźń', emoji: '🤝' },
    { label: 'Twój specjalny dzień', emoji: '✨' },
    { label: 'Boże Narodzenie', emoji: '🎄' },
    { label: 'Sylwestra', emoji: '🎆' },
    { label: 'rocznicę', emoji: '💍' },
  ],
  watching: [
    { label: 'film', emoji: '🎬' },
    { label: 'serial', emoji: '📺' },
    { label: 'YouTube', emoji: '▶️' },
    { label: 'Gwiezdne Wojny', emoji: '⚔️' },
    { label: 'Netflix', emoji: '🍿' },
    { label: 'wiadomości', emoji: '📰' },
  ],
  eating: [
    { label: 'obiad', emoji: '🍝' },
    { label: 'pizzę', emoji: '🍕' },
    { label: 'śniadanie', emoji: '🍳' },
    { label: 'kolację', emoji: '🍽️' },
    { label: 'burgera', emoji: '🍔' },
    { label: 'sushi', emoji: '🍣' },
  ],
  drinking: [
    { label: 'kawę', emoji: '☕' },
    { label: 'herbatę', emoji: '🍵' },
    { label: 'piwo', emoji: '🍺' },
    { label: 'wino', emoji: '🍷' },
    { label: 'wodę', emoji: '💧' },
    { label: 'drinka', emoji: '🍹' },
  ],
  attending: [
    { label: 'koncert', emoji: '🎵' },
    { label: 'wesele', emoji: '👰' },
    { label: 'spotkanie', emoji: '🤝' },
    { label: 'imprezę', emoji: '🎈' },
  ],
  traveling: [
    { label: 'do domu', emoji: '🏠' },
    { label: 'do pracy', emoji: '💼' },
    { label: 'do Warszawy', emoji: '🏙️' },
    { label: 'do Krakowa', emoji: '🐉' },
    { label: 'na wakacje', emoji: '🏖️' },
  ]
};

// --- LOGIKA ---

const handleActivityClick = (activity: any) => {
  selectedActivity.value = activity;
  searchQuery.value = '';
};

const clearSelectedActivity = () => {
  selectedActivity.value = null;
  searchQuery.value = '';
};

const filteredFeelings = computed(() => {
  return feelings.filter(f => f.label.toLowerCase().includes(searchQuery.value.toLowerCase()));
});

const currentActivityList = computed(() => {
  const query = searchQuery.value.toLowerCase();

  // 1. Podkategorie
  if (selectedActivity.value) {
    const list = subCategories[selectedActivity.value.id] || [];
    return list.filter(item => item.label.toLowerCase().includes(query));
  }

  // 2. Główne kategorie
  return activities.filter(a => a.label.toLowerCase().includes(query));
});

const selectFinalItem = (item: any) => {
    emit('feeling-selected', {
        type: 'activity',
        data: {
          parent: selectedActivity.value?.label,
          item: item
        }
    });
};

const selectFeeling = (feeling: any) => {
  emit('feeling-selected', {
    type: 'feeling',
    data: feeling
  });
};
</script>

<template>
  <div class="flex flex-col h-[600px] bg-white font-sans overflow-hidden">

      <div class="flex items-center p-3 border-b border-gray-100 relative">
        <button @click="emit('back')" class="p-2 rounded-full hover:bg-gray-100 absolute left-2 text-gray-600">
          <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M19 12H5"/><path d="M12 19l-7-7 7-7"/></svg>
        </button>
        <h2 class="text-lg font-bold text-center w-full text-gray-900">Co robisz?</h2>
      </div>

      <div class="border-b border-gray-200">
        <div class="flex">
          <button
            @click="activeTab = 'feelings'"
            class="flex-1 py-3 text-center font-semibold text-sm transition-colors relative"
            :class="activeTab === 'feelings' ? 'text-blue-600' : 'text-gray-500 hover:bg-gray-50'"
          >
            Uczucia
            <div v-if="activeTab === 'feelings'" class="absolute bottom-0 left-0 w-full h-[3px] rounded-t-full bg-blue-600"></div>
          </button>

          <button
            @click="activeTab = 'activities'"
            class="flex-1 py-3 text-center font-semibold text-sm transition-colors relative"
            :class="activeTab === 'activities' ? 'text-blue-600' : 'text-gray-500 hover:bg-gray-50'"
          >
            Zajęcia
            <div v-if="activeTab === 'activities'" class="absolute bottom-0 left-0 w-full h-[3px] rounded-t-full bg-blue-600"></div>
          </button>
        </div>
      </div>

      <div class="p-4">
        <div class="relative bg-gray-100 rounded-full flex items-center px-3 py-2 transition-all">

          <svg v-if="!selectedActivity" class="text-gray-500 mr-2" xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg>

          <div
            v-if="selectedActivity"
            class="flex items-center bg-blue-100 text-blue-600 px-2 py-1 rounded-md mr-2 text-sm font-semibold select-none animate-fade-in"
          >
            <span class="mr-1 whitespace-nowrap">{{ selectedActivity.label }}</span>
            <button @click="clearSelectedActivity" class="hover:text-blue-800 flex items-center">
                <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3" stroke-linecap="round" stroke-linejoin="round"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
            </button>
          </div>

          <input
            v-model="searchQuery"
            type="text"
            placeholder="Szukaj"
            class="bg-transparent w-full outline-none text-gray-700 placeholder-gray-500 text-sm h-6"
          />
        </div>
      </div>

      <div class="flex-1 overflow-y-auto px-2 pb-4">

        <div v-if="activeTab === 'feelings'" class="grid grid-cols-2 gap-2">
          <div
            v-for="(feeling, index) in filteredFeelings"
            :key="index"
            class="flex items-center p-2 rounded-lg hover:bg-gray-100 cursor-pointer transition-colors"
             @click="selectFeeling(feeling)"
          >
            <div class="w-10 h-10 rounded-full bg-gray-200 flex items-center justify-center text-xl mr-3 flex-shrink-0">
              {{ feeling.emoji }}
            </div>
            <span class="text-gray-800 text-sm font-medium">{{ feeling.label }}</span>
          </div>
        </div>

        <div v-else class="flex flex-col">

            <template v-if="selectedActivity">
                <div
                    v-for="(item, index) in currentActivityList"
                    :key="'sub-'+index"
                    class="flex items-center p-3 rounded-lg hover:bg-gray-100 cursor-pointer transition-colors group"
                    @click="selectFinalItem(item)"
                >
                    <div class="w-10 h-10 flex items-center justify-center mr-3 flex-shrink-0 text-3xl">
                         {{ item.emoji }}
                    </div>
                    <span class="text-gray-800 text-sm font-medium">{{ item.label }}</span>
                </div>

                <div v-if="currentActivityList.length === 0" class="text-center text-gray-500 mt-4 text-sm">
                    Brak wyników dla "{{ searchQuery }}"
                </div>
            </template>

            <template v-else>
                <div
                    v-for="(activity, index) in currentActivityList"
                    :key="'main-'+index"
                    class="flex items-center justify-between p-3 rounded-lg hover:bg-gray-100 cursor-pointer transition-colors group"
                    @click="handleActivityClick(activity)"
                >
                    <div class="flex items-center">
                      <div :class="`w-10 h-10 rounded-full ${activity.colorBg} flex items-center justify-center mr-3 text-lg`">
                          {{ activity.emoji }}
                      </div>
                      <span class="text-gray-800 text-sm font-medium">{{ activity.label }}</span>
                    </div>

                    <svg class="text-gray-400 group-hover:text-gray-600" xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="9 18 15 12 9 6"/></svg>
                </div>
            </template>

        </div>

      </div>
    </div>
</template>

<style scoped>
@keyframes fadeIn {
    from { opacity: 0; transform: translateX(-5px); }
    to { opacity: 1; transform: translateX(0); }
}
.animate-fade-in {
    animation: fadeIn 0.2s ease-out;
}

::-webkit-scrollbar {
  width: 6px;
}
::-webkit-scrollbar-thumb {
  background-color: #e5e7eb;
  border-radius: 3px;
}
</style>
