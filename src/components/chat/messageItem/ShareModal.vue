<script setup lang="ts">
import { ref, computed } from 'vue'

interface ChatItem {
  id: number
  name: string
  avatar: string
  type: 'recent' | 'group'
}

// Przykładowe dane na podstawie Twojego screena
const items = ref<ChatItem[]>([
  {
    id: 1,
    name: 'Milf Hunters',
    avatar: 'https://randomuser.me/api/portraits/men/1.jpg',
    type: 'recent',
  },
  {
    id: 2,
    name: 'Mateusz Bieniek',
    avatar: 'https://randomuser.me/api/portraits/men/2.jpg',
    type: 'recent',
  },
  {
    id: 3,
    name: 'Carbonara 😎',
    avatar: 'https://randomuser.me/api/portraits/women/3.jpg',
    type: 'recent',
  },
  { id: 4, name: 'Infa 2025', avatar: 'https://placekitten.com/100/100', type: 'recent' },
  {
    id: 5,
    name: 'Koalicja 2 Grudnia',
    avatar: 'https://randomuser.me/api/portraits/men/4.jpg',
    type: 'recent',
  },
  {
    id: 6,
    name: 'WC UPOSIE',
    avatar: 'https://randomuser.me/api/portraits/men/5.jpg',
    type: 'group',
  },
])

const searchQuery = ref('')

// Filtrowanie listy
const filteredRecent = computed(() =>
  items.value.filter(
    (i) => i.type === 'recent' && i.name.toLowerCase().includes(searchQuery.value.toLowerCase()),
  ),
)

const filteredGroups = computed(() =>
  items.value.filter(
    (i) => i.type === 'group' && i.name.toLowerCase().includes(searchQuery.value.toLowerCase()),
  ),
)

const handleSend = (name: string) => {
  console.log(`Wysłano do: ${name}`)
}
</script>

<template>
  <div class="w-[500px] mx-auto bg-white p-4   text-gray-900">
    <div class="relative mb-6">
      <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
        <svg class="h-5 w-5 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path
            stroke-linecap="round"
            stroke-linejoin="round"
            stroke-width="2"
            d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"
          />
        </svg>
      </div>
      <input
        v-model="searchQuery"
        type="text"
        placeholder="Szukaj osób i grup"
        class="block w-full pl-10 pr-3 py-2 bg-gray-100 border-none rounded-full text-sm focus:ring-2 focus:ring-blue-500 outline-none"
      />
    </div>

    <div class="mb-6">
      <h2 class="text-lg font-bold mb-4 px-1">Ostatnie</h2>
      <div class="space-y-4">
        <div
          v-for="item in filteredRecent"
          :key="item.id"
          class="flex items-center justify-between group"
        >
          <div class="flex items-center gap-3">
            <img
              :src="item.avatar"
              class="w-12 h-12 rounded-full object-cover shadow-sm"
              alt="Avatar"
            />
            <span class="font-semibold text-[15px]">{{ item.name }}</span>
          </div>
          <button
            @click="handleSend(item.name)"
            class="px-4 py-1.5 bg-blue-50 hover:bg-blue-100 text-blue-600 font-bold rounded-lg text-sm transition-colors"
          >
            Wyślij
          </button>
        </div>
      </div>
    </div>

    <div>
      <h2 class="text-lg font-bold mb-4 px-1">Grupy</h2>
      <div class="space-y-4">
        <div
          v-for="item in filteredGroups"
          :key="item.id"
          class="flex items-center justify-between"
        >
          <div class="flex items-center gap-3">
            <img
              :src="item.avatar"
              class="w-12 h-12 rounded-full object-cover shadow-sm"
              alt="Avatar"
            />
            <span class="font-semibold text-[15px]">{{ item.name }}</span>
          </div>
          <button
            @click="handleSend(item.name)"
            class="px-4 py-1.5 bg-blue-50 hover:bg-blue-100 text-blue-600 font-bold rounded-lg text-sm transition-colors"
          >
            Wyślij
          </button>
        </div>
      </div>
    </div>
  </div>
</template>


