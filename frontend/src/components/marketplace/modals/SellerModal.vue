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

// Import ikon z vue-material-design-icons
import CloseIcon from 'vue-material-design-icons/Close.vue'
import AccountPlusIcon from 'vue-material-design-icons/AccountPlus.vue'
import FacebookMessengerIcon from 'vue-material-design-icons/FacebookMessenger.vue'
import AccountOutlineIcon from 'vue-material-design-icons/AccountOutline.vue'
import DotsHorizontalIcon from 'vue-material-design-icons/DotsHorizontal.vue'
import HomeIcon from 'vue-material-design-icons/Home.vue'
import FacebookIcon from 'vue-material-design-icons/Facebook.vue'
import MagnifyIcon from 'vue-material-design-icons/Magnify.vue'
import MenuDownIcon from 'vue-material-design-icons/MenuDown.vue'

const defaultProfile = {
  name: 'Igor Kucharski',
  joinedYear: '2017',
  activeListingsCount: 4, // Zaktualizowana liczba
  location: 'Radom',
  avatarUrl: 'https://placehold.co/400x400/111/fff?text=IK',
  coverUrl: 'https://placehold.co/800x300/333/111?text=BMW+Cover',
}

const userProfile = { ...defaultProfile, ...props.profile }

// Stan dla inputów
const searchQuery = ref('')

// Dodane 4 ogłoszenia (zgodnie z prośbą)
const listings = ref([
  {
    id: 1,
    price: 'PLN5,500',
    title: '2007 BMW seria 1',
    location: 'Radom',
    imageUrl: 'https://placehold.co/400x400/222/555?text=BMW+1'
  },
  {
    id: 2,
    price: 'PLN6,000',
    title: '2007 BMW seria 1',
    location: 'Radom',
    imageUrl: 'https://placehold.co/400x400/222/555?text=BMW+2'
  },
  {
    id: 3,
    price: 'PLN4,800',
    title: '2005 BMW seria 1',
    location: 'Radom',
    imageUrl: 'https://placehold.co/400x400/222/555?text=BMW+3'
  },
  {
    id: 4,
    price: 'PLN7,200',
    title: '2009 BMW seria 1',
    location: 'Radom',
    imageUrl: 'https://placehold.co/400x400/222/555?text=BMW+4'
  }
])
</script>

<template>
  <div class="fixed inset-0 z-50 flex items-center justify-center bg-black/60 p-4 font-sans">
    <div class="bg-white w-full max-w-[680px] rounded-xl shadow-2xl relative overflow-hidden flex flex-col max-h-[90vh]">

      <!-- Kontener przewijany (Scroll) -->
      <div class="overflow-y-auto custom-scrollbar flex-1">

        <!-- Okładka (Cover Photo) -->
        <div class="relative w-full h-[220px] bg-gray-200">
          <img
            :src="userProfile.coverUrl"
            :alt="$t('createEvent.coverPhoto')"
            class="w-full h-full object-cover"
          />
          <!-- Przycisk zamykania na zdjęciu -->
          <button
            @click="emit('close')"
            class="absolute top-3 right-3 bg-white/95 hover:bg-white rounded-full w-9 h-9 flex items-center justify-center text-gray-700 z-10 transition shadow-sm"
          >
            <CloseIcon :size="20" />
          </button>
        </div>

        <div class="px-4 pb-6">

          <!-- Avatar (nachodzący na zdjęcie w tle) -->
          <div class="relative -mt-20 mb-3">
            <div class="w-[152px] h-[152px] rounded-full bg-white p-1">
              <img
                :src="userProfile.avatarUrl"
                :alt="$t('chat.avatar')"
                class="w-full h-full rounded-full object-cover border border-gray-100"
              />
            </div>
          </div>

          <!-- Nagłówek profilu -->
          <h1 class="text-[28px] font-bold text-theme-text leading-tight">{{ userProfile.name }}</h1>
          <p class="text-[15px] text-theme-text-secondary mt-1">{{ $t('marketplace.naFacebookuOdUserprofile') }}</p>
          <p class="text-[15px] text-theme-text-secondary mb-5">
            <span class="font-bold text-theme-text">{{ userProfile.activeListingsCount }}</span>{{ $t('marketplace.aktywneOgloszenia') }}</p>

          <!-- Główny panel przycisków akcji -->
          <div class="flex gap-2 mb-2">
            <button class="flex-1 bg-[#0866FF] hover:bg-blue-600 text-white font-semibold py-1.5 px-4 rounded-lg flex items-center justify-center gap-2 transition-colors h-10 text-[15px]">
              <AccountPlusIcon :size="20" />{{ $t('feed.obserwuj2') }}</button>
            <button class="flex-1 bg-theme-bg-tertiary text-[#BCC0C4] cursor-not-allowed font-semibold py-1.5 px-4 rounded-lg flex items-center justify-center gap-2 h-10 text-[15px]">
              <FacebookMessengerIcon :size="18" />{{ $t('profile.sendMessage') }}</button>
          </div>

          <div class="flex gap-2 mb-4">
            <button class="flex-1 bg-theme-bg-tertiary hover:bg-theme-hover-strong text-theme-text font-semibold py-1.5 px-4 rounded-lg flex items-center justify-center gap-2 transition-colors h-10 text-[15px]">
              <AccountOutlineIcon :size="20" />{{ $t('profile.viewProfile') }}</button>
            <button class="bg-theme-bg-tertiary hover:bg-theme-hover-strong text-theme-text font-semibold px-4 rounded-lg flex items-center justify-center transition-colors h-10">
              <DotsHorizontalIcon :size="20" />
            </button>
          </div>

          <hr class="border-[#E4E6EB] my-4" />

          <!-- Sekcja: Informacje -->
          <div class="mb-4">
            <h2 class="text-[17px] font-bold text-theme-text mb-3">{{ $t('groups.information') }}</h2>
            <div class="space-y-3">
              <div class="flex items-center text-theme-text">
                <HomeIcon class="text-theme-text-secondary mr-3" :size="24" />
                <span class="text-[15px]">{{ $t('marketplace.mieszkaW') }}<span class="font-bold">{{ userProfile.location }}</span></span>
              </div>
              <div class="flex items-center text-theme-text">
                <FacebookIcon class="text-theme-text-secondary mr-3" :size="24" />
                <span class="text-[15px]">{{ $t('marketplace.dolaczenieDoFacebookaUserprofile') }}</span>
              </div>
            </div>
          </div>

          <hr class="border-[#E4E6EB] my-4" />

          <!-- Sekcja: Ogłoszenia -->
          <div>
            <h2 class="text-[17px] font-bold text-theme-text mb-4">{{ $t('marketplace.ogloszeniaUserprofileNameSplit') }}</h2>

            <!-- Pasek filtrów (Wyszukiwarka + Selecty) -->
            <div class="flex flex-col sm:flex-row gap-2 mb-4">
              <!-- Wyszukiwarka -->
              <div class="relative flex-1">
                <MagnifyIcon class="absolute left-3 top-1/2 -translate-y-1/2 text-theme-text-secondary" :size="20" />
                <input
                  type="text"
                  :placeholder="$t('marketplace.wyszukajOgloszenia')"
                  class="w-full bg-theme-bg text-[15px] text-theme-text rounded-full py-2 pl-9 pr-4 focus:outline-none placeholder-[#65676B]"
                  v-model="searchQuery"
                />
              </div>

              <!-- Filtry -->
              <button class="bg-theme-bg-tertiary hover:bg-theme-hover-strong text-theme-text font-semibold py-2 px-3.5 rounded-lg flex items-center justify-between gap-1 text-[15px] transition-colors shrink-0">{{ $t('marketplace.dostepneINaStanie') }}<MenuDownIcon class="text-theme-text-secondary" :size="20" />
              </button>
              <button class="bg-theme-bg-tertiary hover:bg-theme-hover-strong text-theme-text font-semibold py-2 px-3.5 rounded-lg flex items-center justify-between gap-1 text-[15px] transition-colors shrink-0">{{ $t('marketplace.sortujWedlug') }}<MenuDownIcon class="text-theme-text-secondary" :size="20" />
              </button>
            </div>

            <!-- Zaktualizowany Grid z 4 ogłoszeniami -->
            <div class="grid grid-cols-2 sm:grid-cols-2 gap-3 pb-4">
              <div
                v-for="item in listings"
                :key="item.id"
                class="flex flex-col cursor-pointer group"
              >
                <!-- Miniatura -->
                <div class="w-full aspect-square rounded-xl overflow-hidden bg-gray-100 mb-2 border border-theme-border">
                  <img
                    :src="item.imageUrl"
                    :alt="item.title"
                    class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-300"
                  />
                </div>
                <!-- Informacje -->
                <h3 class="font-bold text-[17px] text-theme-text leading-tight">{{ item.price }}</h3>
                <p class="text-[15px] text-theme-text truncate mt-0.5">{{ item.title }}</p>
                <p class="text-[13px] text-theme-text-secondary mt-0.5">{{ item.location }}</p>
              </div>
            </div>

          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.custom-scrollbar::-webkit-scrollbar {
  width: 8px;
}
.custom-scrollbar::-webkit-scrollbar-track {
  background: transparent;
}
.custom-scrollbar::-webkit-scrollbar-thumb {
  background-color: #CED0D4;
  border-radius: 20px;
}
</style>
