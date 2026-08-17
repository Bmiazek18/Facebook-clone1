<!-- components/MarketplaceSidebar.vue -->
<template>
  <Sidebar
    title="Wyniki wyszukiwania"
    subtitle="Marketplace"
    searchPlaceholder="Pojazdy"
    :showSearch="true"
    v-model:search="filters.searchQuery"
    :items="[]"
  >
    <!-- SLOT: Historia wyszukiwania (Dropdown) -->
    <template #search-dropdown>
      <div class="w-full px-4 flex flex-col">
        <h3 class="font-semibold text-[17px] text-theme-text mb-3">
          Ostatnie wyszukiwania
        </h3>

        <!-- Lista ostatnich wyszukiwań -->
        <div class="space-y-1" v-if="recentSearches.length > 0">
          <button
            v-for="search in recentSearches"
            :key="search"
            @click="filters.searchQuery = search"
            class="w-full flex items-center gap-3 p-2 hover:bg-theme-hover rounded-lg transition-colors cursor-pointer"
          >
            <div class="w-9 h-9 rounded-full bg-[#F1F2F5] dark:bg-[#333334] flex items-center justify-center shrink-0 text-theme-text-secondary">
              <ClockOutlineIcon :size="20" />
            </div>
            <span class="text-[15px] text-theme-text">{{ search }}</span>
          </button>
        </div>

        <div v-else class="text-sm text-theme-text-secondary mb-2">
          Brak ostatnich wyszukiwań
        </div>

        <!-- Przycisk usuwania -->
        <button
          v-if="recentSearches.length > 0"
          @click="recentSearches = []"
          class="mt-3 w-full bg-[#F1F2F5] dark:bg-[#333334] hover:bg-[#E4E6EB] dark:hover:bg-[#4E4F50] text-theme-text font-semibold py-2 rounded-lg transition-colors text-[15px]"
        >
          Usuń ostatnie wyszukiwania
        </button>
      </div>
    </template>

    <!-- Przyciski akcji -->
    <template #actions>
      <button @click="isAlertOpen = true" class="w-full bg-[#E7F3FF] hover:bg-[#DBE7F2] text-[#0866FF] font-semibold py-2 rounded-lg flex items-center justify-center gap-2 transition text-[15px]">
        <BellOutlineIcon :size="20" />
        <span>Powiadom mnie</span>
      </button>
      <button @click="createListing" class="w-full bg-[#E7F3FF] hover:bg-[#DBE7F2] text-[#0866FF] font-semibold py-2 rounded-lg flex items-center justify-center gap-2 transition text-[15px]">
        <PlusIcon :size="20" />
        <span>Utwórz nowe ogłoszenie</span>
      </button>
    </template>

    <!-- Nagłówek sekcji filtrów -->
    <template #list-header>
      <div class="w-full mb-1">
        <h2 class="text-lg font-bold text-theme-text mb-2">Filtry</h2>
        <button class="text-[#0866FF] font-semibold text-[15px] hover:underline text-left">
          Łęczyca gmina · W promieniu 500 km
        </button>
      </div>
    </template>

    <!-- Lista filtrów -->
    <template #list-items>

      <!-- 1. Sortuj według -->
      <div class="border-b border-theme-border py-3">
        <button @click="toggleSection('sort')" class="w-full flex justify-between items-center font-semibold text-[15px] text-theme-text">
          <span>Sortuj według</span>
          <ChevronUpIcon v-if="openSections.sort" :size="20" />
          <ChevronDownIcon v-else :size="20" />
        </button>
        <div v-if="openSections.sort" class="mt-3 space-y-3">
          <label v-for="option in sortOptions" :key="option" class="flex items-center justify-between cursor-pointer group">
            <span class="text-[15px] text-theme-text group-hover:text-theme-text-secondary transition-colors">{{ option }}</span>
            <input
              type="radio"
              v-model="filters.sort"
              :value="option"
              class="hidden"
            />
            <!-- TWÓJ CUSTOMOWY INDICATOR -->
            <div
              :class="[
                'w-5 h-5 rounded-full border-2 flex items-center justify-center transition-all shrink-0',
                filters.sort === option
                  ? 'border-[#0866FF]'
                  : 'border-gray-400 dark:border-gray-500',
              ]"
            >
              <div
                v-if="filters.sort === option"
                class="w-2.5 h-2.5 bg-[#0866FF] rounded-full"
              ></div>
            </div>
          </label>
        </div>
      </div>

      <!-- 2. Cena -->
      <div class="border-b border-theme-border py-3">
        <div class="font-semibold text-[15px] text-theme-text mb-3">Cena</div>
        <div class="flex items-center gap-3">
          <input
            type="number"
            v-model="filters.priceMin"
            placeholder="Min."
            class="w-full bg-[#F1F2F5] dark:bg-[#333334] border-transparent rounded-lg p-2.5 focus:outline-none focus:ring-1 focus:ring-[#0866FF] text-theme-text text-[15px] placeholder-theme-text-secondary"
          />
          <span class="text-[15px] text-theme-text">do</span>
          <input
            type="number"
            v-model="filters.priceMax"
            placeholder="Maks."
            class="w-full bg-[#F1F2F5] dark:bg-[#333334] border-transparent rounded-lg p-2.5 focus:outline-none focus:ring-1 focus:ring-[#0866FF] text-theme-text text-[15px] placeholder-theme-text-secondary"
          />
        </div>
      </div>

      <!-- 3. Data zamieszczenia -->
      <div class="border-b border-theme-border py-3">
        <button @click="toggleSection('date')" class="w-full flex justify-between items-center font-semibold text-[15px] text-theme-text">
          <span>Data zamieszczenia</span>
          <ChevronUpIcon v-if="openSections.date" :size="20" />
          <ChevronDownIcon v-else :size="20" />
        </button>
        <div v-if="openSections.date" class="mt-3 space-y-3">
          <label v-for="option in dateOptions" :key="option" class="flex items-center justify-between cursor-pointer group">
            <span class="text-[15px] text-theme-text group-hover:text-theme-text-secondary transition-colors">{{ option }}</span>
            <input
              type="radio"
              v-model="filters.date"
              :value="option"
              class="hidden"
            />
            <!-- TWÓJ CUSTOMOWY INDICATOR -->
            <div
              :class="[
                'w-5 h-5 rounded-full border-2 flex items-center justify-center transition-all shrink-0',
                filters.date === option
                  ? 'border-[#0866FF]'
                  : 'border-gray-400 dark:border-gray-500',
              ]"
            >
              <div
                v-if="filters.date === option"
                class="w-2.5 h-2.5 bg-[#0866FF] rounded-full"
              ></div>
            </div>
          </label>
        </div>
      </div>

      <!-- 4. Typ pojazdu -->
      <div class="py-3">
        <button @click="toggleSection('type')" class="w-full flex justify-between items-center font-semibold text-[15px] text-theme-text">
          <span>Typ pojazdu</span>
          <ChevronUpIcon v-if="openSections.type" :size="20" />
          <ChevronDownIcon v-else :size="20" />
        </button>
        <div v-if="openSections.type" class="mt-3 space-y-3">
          <label v-for="option in typeOptions" :key="option" class="flex items-center justify-between cursor-pointer group">
            <span class="text-[15px] text-theme-text group-hover:text-theme-text-secondary transition-colors">{{ option }}</span>
            <input
              type="radio"
              v-model="filters.type"
              :value="option"
              class="hidden"
            />
            <!-- TWÓJ CUSTOMOWY INDICATOR -->
            <div
              :class="[
                'w-5 h-5 rounded-full border-2 flex items-center justify-center transition-all shrink-0',
                filters.type === option
                  ? 'border-[#0866FF]'
                  : 'border-gray-400 dark:border-gray-500',
              ]"
            >
              <div
                v-if="filters.type === option"
                class="w-2.5 h-2.5 bg-[#0866FF] rounded-full"
              ></div>
            </div>
          </label>
        </div>
      </div>

    </template>
  </Sidebar>

  <BaseModal v-if="isAlertOpen" @close="isAlertOpen = false" title="Utwórz alert">
    <CreateAlertDialog
      :search-phrase="filters.searchQuery"
      :price-min="filters.priceMin"
      :price-max="filters.priceMax"
      :location="'Łęczyca gmina · W promieniu 500 km'"
    />
  </BaseModal>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import Sidebar from '@/components/common/Sidebar.vue'

// Importy ikon
import BellOutlineIcon from 'vue-material-design-icons/BellOutline.vue'
import PlusIcon from 'vue-material-design-icons/Plus.vue'
import ChevronDownIcon from 'vue-material-design-icons/ChevronDown.vue'
import ChevronUpIcon from 'vue-material-design-icons/ChevronUp.vue'
import ClockOutlineIcon from 'vue-material-design-icons/ClockOutline.vue'

import CreateAlertDialog from '~/components/marketplace/CreateAlertDialog.vue'
import BaseModal from '~/components/common/BaseModal.vue'

const isAlertOpen = ref(false)

// Przykładowe dane ostatnich wyszukiwań
const recentSearches = ref(['grzecznosciowa', 'bwm'])

// Akcja (przykład) dla przycisku "Utwórz ogłoszenie"
const createListing = () => {
  console.log('Kliknięto utworzenie ogłoszenia')
}

// Główny obiekt trzymający wartości filtrów
const filters = reactive({
  searchQuery: '',
  sort: 'Proponowane',
  priceMin: null as number | null,
  priceMax: null as number | null,
  date: 'Wszystko',
  type: 'Wszystko'
})

// Stan określający, które zakładki są rozwinięte
const openSections = reactive({
  sort: false,
  date: true,
  type: true
})

// Funkcja pomocnicza do klikania w nagłówki akordeonów
const toggleSection = (section: keyof typeof openSections) => {
  openSections[section] = !openSections[section]
}

// Opcje dla poszczególnych list
const sortOptions = [
  'Proponowane',
  'Odległość: od najbliższych',
  'Data ogłoszenia: od najnowszych',
  'Cena: od najniższych',
  'Cena: od najwyższych'
]

const dateOptions = [
  'Wszystko',
  'Ostatnie 24 godzin',
  'Ostatnie 7 dni',
  'Ostatnie 30 dni'
]

const typeOptions = [
  'Wszystko',
  'Samochody i ciężarówki',
  'Motocykle',
  'Sporty motorowe',
  'Samochody turystyczne i campery',
  'Łodzie',
  'Komercyjne i przemysłowe',
  'Przyczepy',
  'Inne'
]
</script>
