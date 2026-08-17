<script setup>
import { ref } from 'vue'

// Importy ikon
import ChevronDownIcon from 'vue-material-design-icons/ChevronDown.vue'
import PencilIcon from 'vue-material-design-icons/Pencil.vue'

// Struktura logów aktywności pogrupowana po dacie
const activityLog = ref([
  {
    date: 'dziś',
    activities: [
      {
        id: 0,
        text: 'Test Testowy utworzył nową <b>reguła</b>.',
        time: '13 sie 2026, 13:23',
        note: 'ddd',
        actions: ['Edytuj notatkę']
      }
    ]
  },
  {
    date: '12 sie 2026',
    activities: [
      {
        id: 1,
        text: 'Test Testowy utworzył grupę.',
        time: '12 sie 2026, 21:15',
        actions: ['Dodaj notatkę']
      },
      {
        id: 2,
        text: 'Test Testowy zmienił ustawienia, aby włączyć anonimowy udział w grupie.',
        time: '12 sie 2026, 21:15',
        actions: ['Dodaj notatkę', 'Cofnij']
      },
      {
        id: 3,
        text: 'Test Testowy dodał(a) użytkownika Wkład do grupy.',
        time: '12 sie 2026, 21:15',
        actions: ['Dodaj notatkę']
      },
      {
        id: 4,
        text: 'Test Testowy enabled members to invite people to the group with link.',
        time: '12 sie 2026, 21:15',
        actions: ['Dodaj notatkę']
      }
    ]
  }
])
</script>

<template>
  <div class="min-h-screen  text-[#e4e6eb] font-sans flex flex-col relative selection:bg-blue-600 pb-20">

    <!-- Pasek nagłówka z filtrami -->
    <div class="bg-theme-bg-secondary  px-4 py-4 sm:px-8 shadow-sm z-10 sticky top-0">
      <div class="max-w-5xl mx-auto">
        <h1 class="text-[20px] font-bold leading-tight mb-4">Dziennik aktywności</h1>

        <!-- Rząd przycisków filtrów -->
        <div class="flex flex-wrap items-center gap-2">
          <!-- Wyczyść filtry (wyłączony) -->
          <button disabled class="bg-theme-bg/50 px-3 py-1.5 rounded-md text-[15px] font-semibold text-theme-text/50 cursor-not-allowed">
            Wyczyść filtry
          </button>

          <!-- Filtry z dropdownem -->
          <button class="bg-theme-bg hover:bg-theme-bg-hover transition-colors rounded-md px-3 py-1.5 flex items-center gap-1.5">
            <span class="text-[15px] font-semibold">Wybierz daty</span>
            <ChevronDownIcon :size="18" class="text-[#b0b3b8]" />
          </button>

          <button class="bg-theme-bg hover:bg-theme-bg-hover transition-colors rounded-md px-3 py-1.5 flex items-center gap-1.5">
            <span class="text-[15px] font-semibold">Administratorzy i moderatorzy</span>
            <ChevronDownIcon :size="18" class="text-[#b0b3b8]" />
          </button>

          <button class="bg-[#3a3b3c] hover:bg-[#4e4f50] transition-colors rounded-md px-3 py-1.5 flex items-center gap-1.5">
            <span class="text-[15px] font-semibold">Członkowie</span>
            <ChevronDownIcon :size="18" class="text-[#b0b3b8]" />
          </button>

          <!-- Więcej filtrów (bez strzałki) -->
          <button class="bg-[#3a3b3c] hover:bg-[#4e4f50] transition-colors rounded-md px-3 py-1.5 flex items-center gap-1.5">
            <span class="text-[15px] font-semibold">Więcej filtrów</span>
          </button>
        </div>
      </div>
    </div>

    <!-- Główna zawartość - Lista Aktywności -->
    <div class="flex-1 w-full max-w-5xl mx-auto p-4 sm:p-6">

      <div
        v-for="(group, groupIndex) in activityLog"
        :key="groupIndex"
        class="bg-theme-bg-secondary rounded-xl p-4 sm:p-5 shadow-sm mb-4 last:mb-0"
      >
        <!-- Nagłówek Daty -->
        <h2 class="text-[16px] font-bold text-[#e4e6eb] mb-4">{{ group.date }}</h2>

        <!-- Elementy listy -->
        <div class="space-y-4">
          <div
            v-for="activity in group.activities"
            :key="activity.id"
            class="flex flex-col sm:flex-row sm:items-start justify-between gap-4"
          >
            <!-- Lewa strona (Avatar + Tekst + Notatka) -->
            <div class="flex items-start gap-3">
              <!-- Generyczny Avatar -->
              <div class="w-10 h-10 rounded-full bg-theme-bg placeholder:text-theme-text/50  flex items-center justify-center shrink-0 overflow-hidden">
                <svg viewBox="0 0 36 36" fill="none" xmlns="http://www.w3.org/2000/svg" class="w-full h-full mt-1.5">
                  <circle cx="18" cy="11" r="6" fill="#65676B"/>
                  <path d="M7 31C7 24 12 21 18 21C24 21 29 24 29 31L7 31Z" fill="#65676B"/>
                </svg>
              </div>

              <!-- Informacje o aktywności -->
              <div class="flex flex-col pt-0.5">
                <span class="text-[15px] text-[#e4e6eb] leading-snug" v-html="activity.text"></span>
                <span class="text-[13px] text-[#b0b3b8] mt-0.5">
                  {{ activity.time }}
                </span>

                <!-- Notatka (jeśli istnieje) -->
                <div v-if="activity.note" class="mt-1.5 border-l-[2px] border-[#3e4042] pl-2.5 py-0.5">
                  <span class="text-[14px] text-[#b0b3b8] italic">{{ activity.note }}</span>
                </div>
              </div>
            </div>

            <!-- Prawa strona (Przyciski akcji) -->
            <div class="flex flex-wrap items-center gap-2 sm:shrink-0 pt-0.5">
              <button
                v-for="(action, index) in activity.actions"
                :key="index"
                class="bg-[#3a3b3c] hover:bg-[#4e4f50] transition-colors px-4 py-1.5 rounded-md text-[15px] font-semibold text-[#e4e6eb]"
              >
                {{ action }}
              </button>
            </div>
          </div>
        </div>
      </div>

    </div>

  </div>
</template>
