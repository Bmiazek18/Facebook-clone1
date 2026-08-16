<script setup lang="ts">
import { ref } from 'vue'

// Import ikon
import CloseIcon from 'vue-material-design-icons/Close.vue'
import LockIcon from 'vue-material-design-icons/Lock.vue'
import StarOutlineIcon from 'vue-material-design-icons/StarOutline.vue'
import HomeIcon from 'vue-material-design-icons/Home.vue'
import MagnifyIcon from 'vue-material-design-icons/Magnify.vue'
import MenuDownIcon from 'vue-material-design-icons/MenuDown.vue'

const searchQuery = ref('')
const emit = defineEmits(['close'])
</script>

<template>


    <!-- Kontener Modala -->
    <div class="bg-white w-full max-w-[680px] max-h-[90vh] rounded-xl shadow-2xl flex flex-col relative overflow-hidden">

      <!-- Przycisk zamykania (Pływający nad tłem) -->
      <button
        @click="emit('close')"
        class="absolute top-3 right-3 w-9 h-9 bg-black/10 hover:bg-black/20 rounded-full flex items-center justify-center text-gray-800 transition-colors z-20"
      >
        <CloseIcon :size="20" />
      </button>

      <!-- Scrollowalna zawartość -->
      <div class="overflow-y-auto flex-1 custom-scrollbar">

        <!-- Okładka (Cover Photo) - w razie potrzeby podmień na <img> -->
        <div class="w-full h-[150px] bg-gradient-to-b from-[#E4E6EB] to-[#F0F2F5]"></div>

        <!-- Główna sekcja profilowa -->
        <div class="px-4 relative">

          <!-- Avatar (Wystaje na okładkę) -->
          <div class="relative -mt-16 mb-2">
            <img
              src="https://placehold.co/400x400/333/fff?text=BM"
              alt="Zdjęcie profilowe"
              class="w-[132px] h-[132px] rounded-full border-4 border-white object-cover bg-gray-200 shadow-sm"
            />
          </div>

          <!-- Nagłówek (Imię i nazwisko) -->
          <h1 class="text-[28px] font-bold text-[#050505] leading-tight">Bartosz Miazek</h1>
          <p class="text-[15px] text-[#65676B] mt-1 mb-4 font-medium">Na Facebooku od 2020</p>

          <!-- Informacja o prywatności -->
          <div class="bg-[#F0F2F5] p-3 rounded-lg flex items-start gap-3 mb-5">
            <div class="pt-0.5">
              <LockIcon class="text-[#65676B]" :size="18" />
            </div>
            <p class="text-[14px] text-[#050505] leading-snug">
              Twoje ustawienia prywatności na Facebooku określają, co osoby, łącznie z użytkownikami Marketplace, mogą zobaczyć w Twoim profilu na Facebooku.
              <a href="#" class="text-[#0064D1] hover:underline font-semibold">Przejdź do ustawień</a>
            </p>
          </div>
        </div>

        <hr class="border-[#E4E6EB] mx-4" />

        <!-- Sekcja: Ocena i zalety -->
        <div class="px-4 py-4">
          <h2 class="text-[17px] font-bold text-[#050505] mb-2">Ocena i zalety</h2>
          <div class="flex items-center gap-1.5 text-[15px]">
            <div class="flex text-[#8A8D91]">
              <StarOutlineIcon v-for="i in 5" :key="i" :size="22" />
            </div>
            <span class="font-bold text-[#050505] ml-1">0.0</span>
            <span class="text-[#65676B]">(0)</span>
          </div>
        </div>

        <hr class="border-[#E4E6EB] mx-4" />

        <!-- Sekcja: Informacje -->
        <div class="px-4 py-4">
          <h2 class="text-[17px] font-bold text-[#050505] mb-3">Informacje</h2>
          <div class="flex items-center gap-3 text-[15px] text-[#050505]">
            <HomeIcon class="text-[#8A8D91]" :size="24" />
            <span>Mieszka w: <span class="font-semibold">Łuków, Siedlce, Poland</span></span>
          </div>
        </div>

        <hr class="border-[#E4E6EB] mx-4" />

        <!-- Sekcja: Ogłoszenia -->
        <div class="px-4 pt-4 pb-12">
          <h2 class="text-[17px] font-bold text-[#050505]">Ogłoszenia Bartosz</h2>
          <p class="text-[14px] text-[#65676B] mt-1 mb-4 leading-snug">
            Ogłoszeniami możesz zarządzać na stronie <a href="#" class="text-[#0064D1] hover:underline font-semibold">Twoje ogłoszenia</a>. Ogłoszenia w grupach prywatnych mogą nie ...
          </p>

          <!-- Pasek narzędzi ogłoszeń -->
          <div class="flex flex-col sm:flex-row gap-2 mb-10">
            <!-- Wyszukiwarka -->
            <div class="relative flex-1">
              <MagnifyIcon class="absolute left-3 top-1/2 -translate-y-1/2 text-[#65676B]" :size="20" />
              <input
                v-model="searchQuery"
                type="text"
                placeholder="Wyszukaj ogłoszenia"
                class="w-full bg-[#F0F2F5] text-[15px] text-[#050505] rounded-full py-2 pl-9 pr-4 focus:outline-none placeholder-[#65676B] font-medium"
              />
            </div>
            <!-- Filtry -->
            <button class="bg-[#E4E6EB] hover:bg-[#D8DADF] transition-colors text-[#050505] font-semibold text-[15px] px-3.5 py-2 rounded-lg flex items-center justify-between gap-1 shrink-0">
              Dostępne i na stanie <MenuDownIcon :size="20" class="text-[#65676B]" />
            </button>
            <button class="bg-[#E4E6EB] hover:bg-[#D8DADF] transition-colors text-[#050505] font-semibold text-[15px] px-3.5 py-2 rounded-lg flex items-center justify-between gap-1 shrink-0">
              Sortuj według <MenuDownIcon :size="20" class="text-[#65676B]" />
            </button>
          </div>

          <!-- Empty State -->
          <div class="flex flex-col items-center justify-center pt-6 pb-8">
            <svg width="120" height="100" viewBox="0 0 140 110" fill="none" xmlns="http://www.w3.org/2000/svg" class="mb-4 opacity-90">
              <!-- Statyw -->
              <path d="M48 60 L38 95 M48 60 L58 95 M48 60 L48 95" stroke="#4B4F56" stroke-width="4" stroke-linecap="round"/>
              <!-- Luneta -->
              <path d="M30 70 L65 45" stroke="#8A8D91" stroke-width="12" stroke-linecap="round"/>
              <path d="M25 75 L35 68" stroke="#1C1E21" stroke-width="16" stroke-linecap="round"/>
              <!-- Postać -->
              <path d="M60 48 L75 35 L90 35 L85 55 L75 55" fill="#A0C3E8"/>
              <path d="M75 55 L85 90 L100 90 L95 55" fill="#1877F2"/>
              <!-- Buty -->
              <rect x="80" y="86" width="12" height="6" fill="#4B4F56" rx="3" />
              <rect x="95" y="86" width="12" height="6" fill="#4B4F56" rx="3" />
              <!-- Ręce i Głowa -->
              <path d="M75 35 L65 45 L70 50" fill="#4B4F56" stroke="#4B4F56" stroke-width="6" stroke-linecap="round"/>
              <circle cx="82" cy="25" r="8" fill="#1C1E21" />
            </svg>
            <p class="text-[16px] text-[#65676B] font-semibold">Nie znaleziono ogłoszeń</p>
          </div>

        </div>
      </div>
    </div>

</template>

<style scoped>
/* Stylizacja ukrywająca domyślny scrollbar, pozostawiając funkcjonalność */
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
