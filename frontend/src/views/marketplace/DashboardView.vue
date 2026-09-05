<script setup lang="ts">
import { ref } from 'vue'

// Import ikon z vue-material-design-icons
import StarOutline from 'vue-material-design-icons/StarOutline.vue'
import RocketLaunchOutline from 'vue-material-design-icons/RocketLaunchOutline.vue'
import SquareEditOutline from 'vue-material-design-icons/SquareEditOutline.vue'
import AlertCircleOutline from 'vue-material-design-icons/AlertCircleOutline.vue'
import TagOutline from 'vue-material-design-icons/TagOutline.vue'
import TagOffOutline from 'vue-material-design-icons/TagOffOutline.vue'
import FileDocumentOutline from 'vue-material-design-icons/FileDocumentOutline.vue'
import Refresh from 'vue-material-design-icons/Refresh.vue'
import SwapHorizontalCircleOutline from 'vue-material-design-icons/SwapHorizontalCircleOutline.vue'
import EyeOutline from 'vue-material-design-icons/EyeOutline.vue'
import BookmarkOutline from 'vue-material-design-icons/BookmarkOutline.vue'
import ShareOutline from 'vue-material-design-icons/ShareOutline.vue'
import AccountGroupOutline from 'vue-material-design-icons/AccountGroupOutline.vue'
import ImageOutline from 'vue-material-design-icons/ImageOutline.vue'
import Pencil from 'vue-material-design-icons/Pencil.vue'
import Delete from 'vue-material-design-icons/Delete.vue'
import DotsHorizontal from 'vue-material-design-icons/DotsHorizontal.vue'

import CustomDropdown from '@/components/common/CustomDropdown.vue'

const timeOptions = [
  { id: 'last_7_days', title: 'Ostatnie 7 dni', description: 'Statystyki z ostatnich 7 dni' },
  { id: 'last_30_days', title: 'Ostatnie 30 dni', description: 'Statystyki z ostatnich 30 dni' },
  { id: 'all_time', title: 'Cały czas', description: 'Statystyki z całego okresu' },
]

const selectedTimeOption = ref('last_7_days')

// Dane do menu "Twoje ogłoszenia" z przypisanymi ikonami
const listingStats = [
  { label: 'Wymagane działanie', value: 0, icon: AlertCircleOutline },
  { label: 'Aktywne i oczekujące', value: 0, icon: TagOutline },
  { label: 'Sprzedane lub brak na stanie', value: 0, icon: TagOffOutline },
  { label: 'Wersje robocze', value: 1, icon: FileDocumentOutline },
  { label: 'Do odnowienia', value: 0, icon: Refresh },
  { label: 'Do usunięcia i ponownego zamieszczenia', value: 0, icon: SwapHorizontalCircleOutline },
]

// Dane do sekcji "Statystyki dotyczące Marketplace"
const marketStats = [
  { label: 'Kliknięcia w ogłoszeniach', value: 0, icon: EyeOutline },
  { label: 'Zdarzenia zapisania ogłoszenia', value: 0, icon: BookmarkOutline },
  { label: 'Udostępnienia ogłoszenia', value: 0, icon: ShareOutline },
  { label: 'Obserwujący w Marketplace', value: 0, icon: AccountGroupOutline },
]
</script>

<template>
  <div class="max-w-5xl mx-auto mt-10 space-y-4">
    <!-- SEKCJA: Podsumowanie -->
    <section class="bg-white rounded-xl shadow-sm border border-theme-border p-5">
      <h2 class="text-[20px] font-bold text-gray-900 mb-4">Podsumowanie</h2>
      <div class="border border-theme-border rounded-xl p-4 w-full md:w-[48%] flex flex-col justify-between">
        <div class="mb-2 text-gray-800">
          <StarOutline :size="28" />
        </div>
        <div class="text-[15px] font-medium text-gray-900">Ocena sprzedawcy</div>
        <div class="text-[13px] text-gray-600 mt-0.5">0</div>
      </div>
    </section>

    <!-- SEKCJA: Twoje ogłoszenia -->
    <section class="bg-white rounded-xl shadow-sm border border-theme-border p-5">
      <div class="flex flex-col md:flex-row justify-between items-start md:items-center mb-5 gap-3">
        <h2 class="text-[20px] font-bold text-gray-900">Twoje ogłoszenia</h2>
        <div class="flex flex-wrap gap-2">
          <button
            class="flex items-center gap-1.5 px-3 py-1.5 text-[#0064D1] font-semibold bg-[#EBF5FF] rounded-lg hover:bg-blue-100 transition-colors text-[14px]"
          >
            <RocketLaunchOutline :size="18" /> Promuj ogłoszenia
          </button>
          <button
            class="flex items-center gap-1.5 px-3 py-1.5 text-[#0064D1] font-semibold bg-[#EBF5FF] rounded-lg hover:bg-blue-100 transition-colors text-[14px]"
          >
            <SquareEditOutline :size="18" /> Utwórz nowe ogłoszenie
          </button>
        </div>
      </div>

      <!-- Kafelki statystyk ogłoszeń -->
      <div class="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-4 gap-3 mb-5">
        <div
          v-for="(stat, index) in listingStats"
          :key="index"
          class="border border-theme-border rounded-xl p-3.5 flex flex-col justify-between min-h-[110px]"
        >
          <div class="mb-2 text-gray-800">
            <component :is="stat.icon" :size="24" />
          </div>
          <div>
            <div class="text-[15px] font-medium text-gray-900 leading-tight mb-1">
              {{ stat.label }}
            </div>
            <div class="text-[13px] text-gray-600 font-medium">
              {{ stat.value }}
            </div>
          </div>
        </div>
      </div>

      <!-- Pojedyncze ogłoszenie (Wersja robocza) -->
      <div class="border border-theme-border rounded-xl p-4 mb-4">
        <div class="flex items-center gap-4 mb-4">
          <div class="w-[100px] h-[100px] bg-theme-bg-tertiary rounded-lg flex items-center justify-center text-gray-600">
            <ImageOutline :size="32" />
          </div>
          <div class="flex flex-col justify-center">
            <div class="font-bold text-gray-900 text-[16px]">ff</div>
            <div class="text-gray-900 text-[14px]">PLN0</div>
            <div class="text-gray-500 text-[13px]">Wersja robocza</div>
          </div>
        </div>
        <div class="flex gap-2">
          <button class="flex-1 flex items-center justify-center gap-2 py-2 bg-[#EBF5FF] text-[#0064D1] font-semibold rounded-lg hover:bg-blue-100 transition-colors text-[15px]">
            <Pencil :size="18" /> Kontynuuj
          </button>
          <button class="flex-1 flex items-center justify-center gap-2 py-2 bg-theme-bg-tertiary text-gray-800 font-semibold rounded-lg hover:bg-gray-300 transition-colors text-[15px]">
            <Delete :size="18" /> Usuń wersję roboczą
          </button>
          <button class="px-4 py-2 bg-theme-bg-tertiary text-gray-800 font-semibold rounded-lg hover:bg-gray-300 transition-colors">
            <DotsHorizontal :size="18" />
          </button>
        </div>
      </div>

      <div class="mt-2 text-center border-t border-theme-border pt-4">
        <a href="#" class="text-[#0064D1] font-semibold text-[15px] hover:underline">
          Zobacz wszystkie ogłoszenia
        </a>
      </div>
    </section>

    <!-- SEKCJA: Statystyki dotyczące Marketplace -->
    <section class="bg-white rounded-xl shadow-sm border border-theme-border p-5">
      <div class="flex justify-between items-center mb-5">
        <h2 class="text-[20px] font-bold text-gray-900">Statystyki dotyczące Marketplace</h2>
        <div class="bg-gray-100 rounded-lg">
          <!-- Twój komponent dropdown -->
          <CustomDropdown v-model="selectedTimeOption" :options="timeOptions" />
        </div>
      </div>

      <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-3">
        <div
          v-for="(stat, index) in marketStats"
          :key="index"
          class="border border-theme-border rounded-xl p-4 flex flex-col justify-center min-h-[100px]"
        >
          <div class="flex items-center gap-2.5 mb-1.5">
            <component :is="stat.icon" :size="24" class="text-gray-800" />
            <span class="text-[24px] font-bold text-gray-900 leading-none">{{ stat.value }}</span>
          </div>
          <span class="text-[15px] text-gray-900 leading-tight font-medium">{{ stat.label }}</span>
        </div>
      </div>

      <div class="mt-5 text-center">
        <a href="#" class="text-[#0064D1] font-semibold text-[15px] hover:underline">
          Zobacz więcej statystyk
        </a>
      </div>
    </section>
  </div>
</template>
