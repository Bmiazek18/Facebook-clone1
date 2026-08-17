<script setup lang="ts">
import { ref, computed } from 'vue'
import Tune from 'vue-material-design-icons/Tune.vue'
import Lock from 'vue-material-design-icons/Lock.vue'

const emits = defineEmits(['close'])

// Limit zaznaczeń
const maxSelection = 50
const selectedPostIds = ref<number[]>([])

// Dane strukturalne odwzorowujące zrzut ekranu
const postGroups = ref([
  {
    month: 'lipca 2026',
    posts: [
      {
        id: 1,
        img: '/path-to-img1.jpg',
        author: 'g',
        date: '5 lipca o 11:45',
        authorImg: '/path-to-avatar1.jpg',
        isPrivate: true,
      },
      {
        id: 2,
        img: '/path-to-img2.jpg',
        author: 'g',
        date: '5 lipca o 11:44',
        authorImg: '/path-to-avatar2.jpg',
        isPrivate: true,
      },
    ],
  },
  {
    month: 'czerwca 2026',
    posts: [
      {
        id: 3,
        img: '/path-to-img3.jpg',
        author: 'CRISTINA DRAGOMIR-ANANIA',
        date: '29 czerwca o 12:00',
        authorImg: '/path-to-avatar3.jpg',
        isPrivate: false,
      },
      {
        id: 4,
        img: '/path-to-img4.jpg',
        author: 'Autor',
        date: '25 czerwca o 10:30',
        authorImg: '/path-to-avatar4.jpg',
        isPrivate: false,
      },
      {
        id: 5,
        img: '/path-to-img5.jpg',
        author: 'Autor',
        date: '20 czerwca o 18:15',
        authorImg: '/path-to-avatar4.jpg',
        isPrivate: false,
      },
    ],
  },
])

const isMaxSelected = computed(() => selectedPostIds.value.length >= maxSelection)
const isAnySelected = computed(() => selectedPostIds.value.length > 0)

const selectAllInGroup = (posts: any[]) => {
  posts.forEach((post) => {
    if (!selectedPostIds.value.includes(post.id)) {
      // Dodaj tylko, jeśli nie przekraczamy limitu
      if (selectedPostIds.value.length < maxSelection) {
        selectedPostIds.value.push(post.id)
      }
    }
  })
}

const clearSelection = () => {
  selectedPostIds.value = []
}
</script>

<template>
  <div class="flex flex-col h-[85vh] bg-white rounded-xl overflow-hidden relative">
    <!-- Nagłówek -->
    <div class="flex items-center justify-between px-6 py-4 border-b border-gray-200 shrink-0">
      <h2 class="text-[18px] font-medium text-gray-900">Wybierz posty, którymi chcesz zarządzać</h2>
      <button
        class="flex items-center px-4 py-2 bg-gray-100 hover:bg-gray-200 rounded-lg font-semibold text-[14px] text-gray-800 transition-colors"
      >
        <Tune :size="18" class="mr-2" />
        Filtry
      </button>
    </div>

    <!-- Obszar przewijany (Lista postów) -->
    <div class="flex-1 overflow-y-auto px-6 py-6 bg-white">
      <div v-for="(group, index) in postGroups" :key="index" class="mb-10 last:mb-4">
        <!-- Nagłówek miesiąca -->
        <div class="flex items-center justify-between mb-4">
          <h3 class="text-[18px] font-bold text-gray-900">{{ group.month }}</h3>
          <button
            @click="selectAllInGroup(group.posts)"
            class="text-blue-600 hover:text-blue-700 font-medium text-[15px] transition-colors"
          >
            Zaznacz wszystkie
          </button>
        </div>

        <!-- Siatka postów -->
        <div class="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-5">
          <label
            v-for="post in group.posts"
            :key="post.id"
            class="relative border border-gray-200 rounded-xl overflow-hidden cursor-pointer group bg-white shadow-sm hover:shadow-md transition-shadow flex flex-col h-[220px]"
            :class="{ 'ring-2 ring-blue-500': selectedPostIds.includes(post.id) }"
          >
            <!-- Ukryty natywny input, powiązany bezpośrednio z grupą -->
            <input
              type="checkbox"
              v-model="selectedPostIds"
              :value="post.id"
              class="absolute opacity-0"
            />

            <!-- Ręcznie narysowany checkbox (wierniejsze odwzorowanie systemowego wyglądu) -->
            <div class="absolute top-3 right-3 z-10">
              <div
                class="w-6 h-6 rounded flex items-center justify-center transition-colors border shadow-sm"
                :class="
                  selectedPostIds.includes(post.id)
                    ? 'border-blue-500 bg-blue-500'
                    : 'border-gray-300 bg-white group-hover:border-gray-400'
                "
              >
                <svg
                  v-if="selectedPostIds.includes(post.id)"
                  class="w-4 h-4 text-white"
                  fill="none"
                  viewBox="0 0 24 24"
                  stroke="currentColor"
                >
                  <path
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    stroke-width="3"
                    d="M5 13l4 4L19 7"
                  />
                </svg>
              </div>
            </div>

            <!-- Miniaturka posta -->
            <div class="h-[140px] w-full bg-gray-100 flex-shrink-0 relative">
              <!-- Zaślepka wizualna, docelowo: <img :src="post.img" class="object-cover w-full h-full" /> -->
              <div class="w-full h-full bg-gray-200"></div>
            </div>

            <!-- Stopka posta z informacjami -->
            <div class="p-3 flex items-start flex-1 bg-white">
              <!-- Awatar -->
              <div class="w-8 h-8 rounded-full bg-gray-300 mr-3 flex-shrink-0 mt-0.5">
                <!-- <img :src="post.authorImg" class="w-full h-full rounded-full object-cover" /> -->
              </div>

              <!-- Teksty -->
              <div class="flex flex-col min-w-0">
                <span class="text-[13px] font-semibold text-gray-900 truncate block">
                  {{ post.author }}
                </span>
                <div class="flex items-center text-[12px] text-gray-500 mt-0.5">
                  <span class="truncate">{{ post.date }}</span>
                  <Lock v-if="post.isPrivate" :size="12" class="ml-1 text-gray-500 flex-shrink-0" />
                </div>
              </div>
            </div>
          </label>
        </div>
      </div>
    </div>

    <!-- Dolny pasek zarządzania (Sticky Footer) -->
    <div
      class="border-t border-gray-200 bg-white px-6 py-4 flex items-center justify-between shrink-0"
    >
      <div class="text-[16px] font-bold text-blue-600">
        {{ selectedPostIds.length }}/{{ maxSelection }}
      </div>

      <div class="flex space-x-3">
        <button
          @click="clearSelection"
          :disabled="!isAnySelected"
          class="px-6 py-2 rounded-lg font-semibold text-[14px] transition-colors"
          :class="
            isAnySelected
              ? 'bg-gray-100 hover:bg-gray-200 text-gray-800'
              : 'bg-gray-100 text-gray-400 cursor-not-allowed opacity-70'
          "
        >
          Wyczyść
        </button>

        <button
          :disabled="!isAnySelected"
          class="px-8 py-2 rounded-lg font-semibold text-[14px] transition-colors"
          :class="
            isAnySelected
              ? 'bg-blue-600 hover:bg-blue-700 text-white'
              : 'bg-gray-200 text-gray-400 cursor-not-allowed opacity-70'
          "
        >
          Dalej
        </button>
      </div>
    </div>
  </div>
</template>
