<script setup lang="ts">
import { ref } from 'vue'

// Importy ikon
import InformationIcon from 'vue-material-design-icons/Information.vue'
import CogIcon from 'vue-material-design-icons/Cog.vue'
import BellIcon from 'vue-material-design-icons/Bell.vue'

// Stan zakładek (Tabs)
const activeTab = ref('all')

const tabs = [
  { id: 'all', label: 'Wszystkie' },
  { id: 'activity', label: 'Aktywność', count: 0 },
  { id: 'keyword', label: 'Słowo kluczowe', count: 0 }
]
</script>

<template>
  <!-- Główny kontener o minimalnej wysokości ekranu i szarym tle -->
  <div class="min-h-screen bg-[#f0f2f5] font-sans text-[#050505] selection:bg-blue-600 flex flex-col">

    <!-- Pasek nagłówka z tytułem, zakładkami i ustawieniami -->
    <header class="bg-white border-b border-gray-300 flex flex-col pt-4">
      <div class="max-w-[900px] w-full mx-auto px-4 sm:px-6">

        <!-- Górna sekcja: Tytuł + Ustawienia -->
        <div class="flex items-center justify-between mb-4">
          <div class="flex items-center gap-2">
            <h1 class="text-[24px] font-bold leading-tight">Alerty moderacji</h1>
            <InformationIcon :size="18" class="text-[#8c939d] cursor-pointer hover:text-[#65676b] transition-colors mt-1" />
          </div>

          <!-- Przycisk Ustawienia (Zębatka) -->
          <button class="w-10 h-10 rounded-full bg-[#e4e6eb] hover:bg-[#d8dadf] flex items-center justify-center transition-colors cursor-pointer">
            <CogIcon :size="20" class="text-[#050505]" />
          </button>
        </div>

        <!-- Dolna sekcja: Zakładki (Tabs) -->
        <div class="flex items-center gap-6">
          <button
            v-for="tab in tabs"
            :key="tab.id"
            @click="activeTab = tab.id"
            class="relative pb-3 text-[15px] font-semibold transition-colors cursor-pointer"
            :class="[
              activeTab === tab.id
                ? 'text-[#1877f2]'
                : 'text-[#65676b] hover:bg-gray-100 rounded-t-md px-1'
            ]"
          >
            {{ tab.label }}
            <span v-if="tab.count !== undefined" class="font-normal text-[#65676b]"> · {{ tab.count }}</span>

            <!-- Niebieska linia podkreślająca aktywną zakładkę -->
            <div
              v-if="activeTab === tab.id"
              class="absolute bottom-0 left-0 right-0 h-[3px] bg-[#1877f2] rounded-t-sm"
            ></div>
          </button>
        </div>

      </div>
    </header>

    <!-- Główna zawartość - Stan pusty (Brak alertów) -->
    <main class="flex-1 flex flex-col items-center justify-center p-6 text-center max-w-[600px] mx-auto w-full mt-10">

      <!-- Grafika Dzwonka -->
      <div class="relative w-[100px] h-[100px] mb-4">
        <!-- Szary dzwonek -->
        <svg viewBox="0 0 24 24" fill="#a0a3a7" class="w-full h-full">
          <path d="M12 22c1.1 0 2-.9 2-2h-4c0 1.1.9 2 2 2zm6-6v-5c0-3.07-1.63-5.64-4.5-6.32V4c0-.83-.67-1.5-1.5-1.5s-1.5.67-1.5 1.5v.68C7.64 5.36 6 7.92 6 11v5l-2 2v1h16v-1l-2-2z"/>
        </svg>
        <!-- Mały niebieski kółko u dołu (ozdobnik ze zrzutu ekranu) -->
        <div class="absolute bottom-2 left-1/2 -translate-x-1/2 w-3.5 h-3.5 bg-[#1877f2] rounded-full border-2 border-[#f0f2f5]"></div>
      </div>

      <!-- Teksty -->
      <h2 class="text-[20px] font-bold text-[#050505] mb-2">Brak alertów moderacji</h2>
      <p class="text-[15px] text-[#65676b] mb-6">
        Żaden post ani komentarz nie aktywował alertu moderacji.
      </p>

      <!-- Niebieski przycisk -->
      <button class="bg-[#1877f2] hover:bg-[#166fe5] text-white text-[15px] font-semibold px-10 py-2.5 rounded-lg transition-colors cursor-pointer shadow-sm">
        Edytuj alerty
      </button>

    </main>

  </div>
</template>
