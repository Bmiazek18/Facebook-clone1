<script setup lang="ts">
import { ref } from 'vue'

// Ikony z vue-material-design-icons
import Magnify from 'vue-material-design-icons/Magnify.vue'
import FormatListBulleted from 'vue-material-design-icons/FormatListBulleted.vue'
import ViewGrid from 'vue-material-design-icons/ViewGrid.vue'
import ImageOutline from 'vue-material-design-icons/ImageOutline.vue'
import Pencil from 'vue-material-design-icons/Pencil.vue'
import Delete from 'vue-material-design-icons/Delete.vue'
import DotsHorizontal from 'vue-material-design-icons/DotsHorizontal.vue'

// Zmienna sterująca widokiem (list | grid)
const viewMode = ref<'list' | 'grid'>('list')
const searchQuery = ref('')
</script>

<template>
  <!-- Główne tło strony (jasnoszare, by ładnie kontrastowało z białymi kartami) -->
  <div class="min-h-screen bg-[#F0F2F5] p-4 font-sans">

    <!-- Górny panel (Header) -->
    <div class="bg-white rounded-xl p-3 px-4 shadow-sm flex flex-col md:flex-row md:items-center justify-between gap-4 mb-4 max-w-5xl mx-auto">

      <!-- Tytuł -->
      <h1 class="text-[24px] font-bold text-black">{{ $t('marketplace.twojeOgloszenia') }}</h1>

      <!-- Wyszukiwarka i przełączniki -->
      <div class="flex items-center gap-2">

        <!-- Pole wyszukiwania -->
        <div class="relative flex items-center">
          <Magnify :size="20" class="absolute left-3 text-gray-500" />
          <input
            v-model="searchQuery"
            type="text"
            :placeholder="$t('marketplace.wyszukajSwojeOgloszenia')"
            class="bg-[#F0F2F5] text-[15px] text-gray-800 rounded-full pl-10 pr-4 py-2 w-[280px] md:w-[320px] focus:outline-none placeholder-gray-500"
          />
        </div>

        <!-- Przełączniki widoku -->
        <div class="flex gap-1">
          <!-- Przycisk Lista -->
          <button
            @click="viewMode = 'list'"
            :class="viewMode === 'list' ? 'bg-[#0866FF] text-white' : 'bg-[#F0F2F5] text-gray-600 hover:bg-gray-200'"
            class="p-2 rounded-lg transition-colors flex items-center justify-center w-10 h-10"
          >
            <FormatListBulleted :size="22" />
          </button>

          <!-- Przycisk Siatka -->
          <button
            @click="viewMode = 'grid'"
            :class="viewMode === 'grid' ? 'bg-[#0866FF] text-white' : 'bg-[#F0F2F5] text-gray-600 hover:bg-gray-200'"
            class="p-2 rounded-lg transition-colors flex items-center justify-center w-10 h-10"
          >
            <ViewGrid :size="22" />
          </button>
        </div>
      </div>
    </div>

    <!-- Kontener na ogłoszenia -->
    <div class="max-w-5xl mx-auto">

      <!-- WIDOK: LISTA (Odpowiada pierwszemu zdjęciu) -->
      <div v-if="viewMode === 'list'" class="bg-white rounded-xl p-4 shadow-sm flex flex-col md:flex-row gap-4">
        <!-- Obrazek -->
        <div class="w-full md:w-[140px] h-[140px] bg-[#E4E6EB] rounded-lg flex items-center justify-center text-gray-600 shrink-0">
          <ImageOutline :size="40" />
        </div>

        <!-- Zawartość po prawej -->
        <div class="flex flex-col justify-between flex-1">
          <!-- Teksty -->
          <div>
            <h3 class="text-[17px] font-bold text-black leading-tight">{{ $t('marketplace.ff') }}</h3>
            <p class="text-[15px] text-black font-normal mt-0.5">{{ $t('marketplace.pln0') }}</p>
            <p class="text-[14px] text-gray-500 mt-0.5">{{ $t('marketplace.wersjaRobocza') }}</p>
          </div>

          <!-- Przyciski akcji -->
          <div class="flex flex-wrap md:flex-nowrap gap-2 mt-4 md:mt-0">
            <button class="flex-1 md:flex-none md:w-auto px-4 py-2 bg-[#EBF5FF] text-[#0064D1] font-semibold text-[15px] rounded-lg flex items-center justify-center gap-1.5 hover:bg-blue-100 transition-colors">
              <Pencil :size="18" />{{ $t('marketplace.kontynuuj') }}</button>
            <button class="flex-1 md:flex-none md:w-auto px-4 py-2 bg-[#E4E6EB] text-gray-900 font-semibold text-[15px] rounded-lg flex items-center justify-center gap-1.5 hover:bg-gray-300 transition-colors">
              <Delete :size="18" />{{ $t('common.usunWersjeRobocza') }}</button>
            <button class="px-3 py-2 bg-[#E4E6EB] text-gray-900 rounded-lg flex items-center justify-center hover:bg-gray-300 transition-colors">
              <DotsHorizontal :size="20" />
            </button>
          </div>
        </div>
      </div>

      <!-- WIDOK: SIATKA (Odpowiada drugiemu zdjęciu) -->
      <div v-if="viewMode === 'grid'" class="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 xl:grid-cols-5 gap-4">
        <!-- Karta siatki -->
        <div class="flex flex-col cursor-pointer group">
          <!-- Obrazek -->
          <div class="w-full aspect-square bg-[#E4E6EB] rounded-xl flex items-center justify-center text-gray-600 mb-2 border border-gray-200">
            <ImageOutline :size="40" />
          </div>
          <!-- Teksty -->
          <div>
            <h3 class="text-[16px] font-bold text-black leading-tight group-hover:underline">{{ $t('marketplace.ff') }}</h3>
            <p class="text-[14px] text-black font-normal mt-0.5">{{ $t('marketplace.pln0') }}</p>
            <p class="text-[13px] text-gray-500 mt-0.5">{{ $t('marketplace.wersjaRobocza') }}</p>
          </div>
        </div>
      </div>

    </div>
  </div>
</template>
