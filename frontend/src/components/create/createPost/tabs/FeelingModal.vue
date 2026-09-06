<script setup lang="ts">
import { ref, computed } from 'vue'
import { useCreatePostStore } from '@/stores/createPost'

const createPostStore = useCreatePostStore()

// --- TYPY DANYCH ---
interface Feeling {
  emoji: string
  label: string
}

interface Activity {
  id: string
  label: string
  emoji: string
  colorBg: string
}

interface SubActivity {
  label: string
  emoji: string
}

// --- STAN ---
const activeTab = ref('feelings')
const searchQuery = ref('')
const selectedActivity = ref<Activity | null>(null)

const feelings: Feeling[] = [
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
]

const activities: Activity[] = [
  { id: 'celebrating', label: 'Świętuje...', emoji: '🎉', colorBg: 'bg-yellow-100' },
  { id: 'watching', label: 'Ogląda...', emoji: '📺', colorBg: 'bg-red-100' },
  { id: 'eating', label: 'Je...', emoji: '🍴', colorBg: 'bg-green-100' },
  { id: 'drinking', label: 'Pije...', emoji: '🥤', colorBg: 'bg-orange-100' },
  { id: 'attending', label: 'Bierze udział...', emoji: '📅', colorBg: 'bg-blue-100' },
  { id: 'traveling', label: 'Podróżuje do...', emoji: '✈️', colorBg: 'bg-purple-100' },
]

const subCategories: Record<string, SubActivity[]> = {
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
  ],
}

// --- LOGIKA ---

const handleActivityClick = (activity: Activity) => {
  selectedActivity.value = activity
  searchQuery.value = ''
}

const clearSelectedActivity = () => {
  selectedActivity.value = null
  searchQuery.value = ''
}

const filteredFeelings = computed(() => {
  return feelings.filter((f) => f.label.toLowerCase().includes(searchQuery.value.toLowerCase()))
})

const currentActivityList = computed(() => {
  const query = searchQuery.value.toLowerCase()

  // 1. Podkategorie
  if (selectedActivity.value) {
    const list = subCategories[selectedActivity.value.id] || []
    return list.filter((item) => item.label.toLowerCase().includes(query))
  }

  // 2. Główne kategorie
  return activities.filter((a) => a.label.toLowerCase().includes(query))
})

const selectFinalItem = (item: SubActivity) => {
  createPostStore.postData.activity = {
    parent: selectedActivity.value?.label,
    item: item,
  }
  createPostStore.navigateBack()
}

const selectFeeling = (feeling: Feeling) => {
  createPostStore.postData.feeling = feeling
  createPostStore.navigateBack()
}
</script>

<template>
  <div class="flex flex-col h-full bg-white   overflow-hidden text-[#1c1e21]">
    <div class="flex border-b border-gray-100 px-2 gap-6">
      <button
        @click="activeTab = 'feelings'"
        class="py-4 font-medium text-[15px] transition-all relative"
        :class="activeTab === 'feelings' ? 'text-blue-600' : 'text-gray-500 hover:text-gray-700'"
      >{{ $t('create.uczucia') }}<div
          v-if="activeTab === 'feelings'"
          class="absolute bottom-0 left-0 w-full h-[3px] bg-blue-600 rounded-t-full"
        ></div>
      </button>

      <button
        @click="activeTab = 'activities'"
        class="py-4 font-medium text-[15px] transition-all relative"
        :class="activeTab === 'activities' ? 'text-blue-600' : 'text-gray-500 hover:text-gray-700'"
      >{{ $t('create.zajecia') }}<div
          v-if="activeTab === 'activities'"
          class="absolute bottom-0 left-0 w-full h-[3px] bg-blue-600 rounded-t-full"
        ></div>
      </button>
    </div>

    <div class="px-4 pt-4 pb-2">
      <!-- Główny kontener flex trzymający tag i wyszukiwarkę obok siebie -->
      <div class="flex items-center gap-2">
        <!-- Poprawiony tag (jasnoniebieskie tło, niebieski tekst, pełne zaokrąglenie) -->
        <div
          v-if="selectedActivity"
          class="flex items-center bg-[#e7f3ff] text-[#1877f2] px-3 py-1.5 rounded-full text-[14px] font-semibold animate-fade-in shrink-0"
        >
          <span class="mr-1.5">{{ selectedActivity.label }}</span>
          <button
            @click="clearSelectedActivity"
            class="text-[#1877f2] hover:opacity-80 transition-opacity flex items-center"
          >
            <svg
              xmlns="http://www.w3.org/2000/svg"
              width="14"
              height="14"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="3"
              stroke-linecap="round"
            >
              <line x1="18" y1="6" x2="6" y2="18" />
              <line x1="6" y1="6" x2="18" y2="18" />
            </svg>
          </button>
        </div>

        <!-- Wyszukiwarka jako osobny "pill" (zawsze z lupą i zaokrąglona jak na zrzucie) -->
        <div
          class="relative bg-[#f0f2f5] rounded-full flex items-center flex-1 px-3 py-2 transition-all focus-within:ring-2 focus-within:ring-blue-500/20"
        >
          <svg
            class="text-gray-500 mr-2 shrink-0"
            xmlns="http://www.w3.org/2000/svg"
            width="18"
            height="18"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            stroke-width="2.5"
            stroke-linecap="round"
          >
            <circle cx="11" cy="11" r="8" />
            <line x1="21" y1="21" x2="16.65" y2="16.65" />
          </svg>
          <input
            v-model="searchQuery"
            type="text"
            :placeholder="$t('common.search')"
            class="bg-transparent w-full outline-none text-[15px] placeholder-gray-500"
          />
        </div>
      </div>
    </div>

    <div class="flex-1 overflow-y-auto px-2 pb-4 pt-2">
      <div v-if="activeTab === 'feelings'" class="grid grid-cols-2">
        <div
          v-for="(feeling, index) in filteredFeelings"
          :key="index"
          class="flex items-center p-2 rounded-lg hover:bg-[#d3d6da] cursor-pointer transition-colors"
          @click="selectFeeling(feeling)"
        >
          <div class="w-8 h-8 flex items-center justify-center text-xl mr-2 flex-shrink-0">
            {{ feeling.emoji }}
          </div>
          <span class="text-[15px] font-medium">{{ feeling.label }}</span>
        </div>
      </div>

      <div v-else class="flex flex-col">
        <template v-if="selectedActivity">
          <div
            v-for="(item, index) in currentActivityList"
            :key="'sub-' + index"
            class="flex items-center p-3 rounded-xl hover:bg-gray-50 cursor-pointer transition-colors group"
            @click="selectFinalItem(item)"
          >
            <div class="w-9 h-9 flex items-center justify-center mr-3 flex-shrink-0 text-2xl">
              {{ item.emoji }}
            </div>
            <span class="text-[15px] font-medium">{{ item.label }}</span>
          </div>
        </template>

        <template v-else>
          <div
            v-for="(activity, index) in currentActivityList"
            :key="'main-' + index"
            class="flex items-center justify-between p-3 rounded-xl hover:bg-gray-50 cursor-pointer transition-colors group"
            @click="handleActivityClick(activity)"
          >
            <div class="flex items-center">
              <div class="w-9 h-9 flex items-center justify-center mr-3 text-2xl">
                {{ activity.emoji }}
              </div>
              <span class="text-[15px] font-medium">{{ activity.label }}</span>
            </div>
            <svg
              class="text-gray-400"
              xmlns="http://www.w3.org/2000/svg"
              width="20"
              height="20"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="2"
              stroke-linecap="round"
            >
              <polyline points="9 18 15 12 9 6" />
            </svg>
          </div>
        </template>
      </div>
    </div>
  </div>
</template>

<style scoped>
@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateX(-5px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
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
