<script setup lang="ts">
import { ref } from 'vue'

// Props & emits
const props = defineProps({
  profile: {
    type: Object,
    default: () => ({}),
  },
})
const emit = defineEmits(['close'])

// Import ikon
import Close from 'vue-material-design-icons/Close.vue'
import FacebookMessenger from 'vue-material-design-icons/FacebookMessenger.vue'
import StarOutline from 'vue-material-design-icons/StarOutline.vue'
import HomeVariant from 'vue-material-design-icons/HomeVariant.vue'
import Magnify from 'vue-material-design-icons/Magnify.vue'
import ChevronDown from 'vue-material-design-icons/ChevronDown.vue'
import Telescope from 'vue-material-design-icons/Telescope.vue' // Ikona zastępcza dla ilustracji braku wyników

// Dane profilowe (można przekazać przez props.profile)
const defaultProfile = {
  name: 'Bartosz Miazek',
  joinedText: 'Na Facebooku od 2020',
  location: 'Mieszka w: Łukow, Siedlce, Poland',
  avatarUrl: 'https://via.placeholder.com/150',
}

const profile = { ...defaultProfile, ...props.profile }

// Stan dla inputów (opcjonalny)
const searchQuery = ref('')
</script>

<template>
  <div class="bg-white w-full max-w-[600px] rounded-lg shadow-lg relative overflow-hidden">
    <button
      @click="emit('close')"
      class="absolute top-4 right-4 bg-gray-200 hover:bg-gray-300 rounded-full p-2 z-10 transition"
    >
      <Close class="text-gray-600" :size="20" />
    </button>

    <div class="p-4 pt-10">
      <div class="flex flex-col items-center text-center mb-6">
        <div class="relative mb-4">
          <div
            class="w-32 h-32 rounded-full overflow-hidden border-4 border-white shadow-sm bg-gray-200"
          >
            <img :src="profile.avatarUrl" alt="Avatar" class="w-full h-full object-cover" />
          </div>
        </div>

        <h1 class="text-3xl font-bold text-gray-900 mb-1">{{ profile.name }}</h1>
        <p class="text-gray-500 text-sm mb-4">{{ profile.joinedText }}</p>

        <button
          class="w-full bg-[#E4E6EB] hover:bg-[#D8DADF] text-gray-400 font-semibold py-2 px-4 rounded-md flex items-center justify-center transition cursor-not-allowed"
        >
          <FacebookMessenger class="mr-2" :size="18" />
          Wyślij wiadomość
        </button>
      </div>

      <hr class="border-gray-200 my-4" />

      <div class="mb-4">
        <h2 class="text-lg font-bold text-gray-900 mb-2">Oceny sprzedawcy</h2>
        <div class="flex space-x-1 mb-1 text-blue-500">
          <StarOutline v-for="i in 5" :key="i" :size="24" />
        </div>
        <p class="text-sm text-gray-900 font-medium">Brak ocen</p>
        <p class="text-xs text-gray-500 mt-1">
          (Widoczne publicznie po 5 ocenach.
          <span class="font-semibold cursor-pointer hover:underline">Dowiedz się więcej</span>)
        </p>
      </div>

      <hr class="border-gray-200 my-4" />

      <div class="mb-4">
        <h2 class="text-lg font-bold text-gray-900 mb-3">Informacje</h2>
        <div class="flex items-center text-gray-900">
          <HomeVariant class="text-gray-500 mr-3" :size="24" />
          <span class="text-[15px] font-medium">{{ profile.location }}</span>
        </div>
      </div>

      <hr class="border-gray-200 my-4" />

      <div>
        <h2 class="text-lg font-bold text-gray-900 mb-1">Ogłoszenia Bartosz</h2>
        <p class="text-xs text-gray-500 mb-4 leading-snug">
          Ogłoszeniami możesz zarządzać na stronie
          <a href="#" class="text-blue-500 hover:underline">Twoje ogłoszenia</a>. Ogłoszenia w
          grupach prywatnych mogą nie...
        </p>

        <div class="flex flex-col sm:flex-row gap-2 mb-8">
          <div class="relative flex-grow">
            <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
              <Magnify class="text-gray-500" />
            </div>
            <input
              type="text"
              placeholder="Wyszukaj ogłoszenia"
              class="w-full bg-[#F0F2F5] text-gray-900 rounded-full py-2 pl-10 pr-4 focus:outline-none focus:ring-1 focus:ring-gray-300 placeholder-gray-500"
              v-model="searchQuery"
            />
          </div>

          <button
            class="bg-[#E4E6EB] hover:bg-[#D8DADF] text-black font-semibold py-2 px-3 rounded-md text-sm flex items-center justify-between whitespace-nowrap"
          >
            Dostępne i na stanie
            <ChevronDown class="ml-1" :size="20" />
          </button>

          <button
            class="bg-[#E4E6EB] hover:bg-[#D8DADF] text-black font-semibold py-2 px-3 rounded-md text-sm flex items-center justify-between whitespace-nowrap"
          >
            Sortuj według
            <ChevronDown class="ml-1" :size="20" />
          </button>
        </div>

        <div class="flex flex-col items-center justify-center pb-10">
          <div class="mb-4 text-gray-400">
            <Telescope :size="120" class="opacity-80 text-blue-400" />
          </div>
          <p class="text-gray-500 text-lg">Nie znaleziono ogłoszeń</p>
        </div>
      </div>
    </div>
  </div>
</template>
