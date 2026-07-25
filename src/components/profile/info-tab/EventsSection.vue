<script setup lang="ts">
import { ref, computed } from 'vue'
import AccountMultiple from 'vue-material-design-icons/AccountMultiple.vue'
import Controller from 'vue-material-design-icons/Controller.vue' // Ikona zastępcza dla konkretnych hobby
import Shapes from 'vue-material-design-icons/ShapeOutline.vue' // Ikona czterech figur geometrycznych

// --- STAN TRYBU WIDOKU / EDYCJI ---
const isEditing = ref(false)

// --- STAN FORMULARZA ---
const searchQuery = ref('')
const isFocused = ref(false)
const selectedHobbies = ref<string[]>([])

// --- MOCK POPULARNYCH HOBBY ---
const popularHobbies = ref([
  { id: 1, name: 'Słuchanie muzyki', count: '10.0K innych' },
  { id: 2, name: 'Podróżowanie', count: '10.0K innych' },
  { id: 3, name: 'Gry wideo', count: '10.0K innych' },
  { id: 4, name: 'Czytanie', count: '10.0K innych' },
  { id: 5, name: 'Fotografia', count: '10.0K innych' },
])

const filteredHobbies = computed(() => {
  if (!searchQuery.value) return popularHobbies.value
  return popularHobbies.value.filter((h) =>
    h.name.toLowerCase().includes(searchQuery.value.toLowerCase()),
  )
})

const selectHobby = (name: string) => {
  if (selectedHobbies.value.includes(name)) {
    selectedHobbies.value = selectedHobbies.value.filter((h) => h !== name)
  } else {
    if (selectedHobbies.value.length < 10) {
      selectedHobbies.value.push(name)
    }
  }
}

const handleCancel = () => {
  searchQuery.value = ''
  isFocused.value = false
  isEditing.value = false // Powrót do widoku czystego
}

const handleSave = () => {
  // Tutaj logika wysyłki danych do bazy/store
  isFocused.value = false
  isEditing.value = false // Powrót do widoku czystego
}
</script>

<template>
  <div
    class="max-w-[850px] bg-white rounded-xl p-4 text-[#050505] antialiased   transition-all"
  >
    <!-- ======================================================= -->
    <!-- 1. WIDOK PODGLĄDU (Gdy użytkownik nie chce edytować)    -->
    <!-- ======================================================= -->
    <div v-if="!isEditing" @click="isEditing = true" class="cursor-pointer group select-none">
      <!-- Tytuł Sekcji -->
      <div class="flex justify-between items-center mb-4">
        <h2 class="text-[20px] font-bold text-[#050505]">Hobby</h2>
        <!-- Opcjonalny przycisk podświetlany jak na FB przy hoverze -->
        <span class="text-[14px] text-[#1877F2] font-medium hidden group-hover:block">Edytuj</span>
      </div>

      <!-- Wiersz z ikoną figur i tekstem (Zrzut ekranu 2026-06-25 o 10.37.50.png) -->
      <div class="flex items-center gap-3 py-1">
        <div class="text-[#050505] flex items-center justify-center">
          <Shapes :size="24" class="stroke-[1.5]" />
        </div>
        <span class="text-[15px] text-[#65676B] font-normal"> Hobby </span>
      </div>
    </div>

    <!-- ======================================================= -->
    <!-- 2. WIDOK EDYCJI (Po kliknięciu / aktywacji)             -->
    <!-- ======================================================= -->
    <div v-else class="space-y-4">
      <!-- Nagłówek sekcji -->
      <div class="space-y-3">
        <h2 class="text-[20px] font-bold text-[#050505]">Hobby</h2>

        <!-- Badge prywatności -->
        <div
          class="inline-flex items-center gap-1.5 bg-[#E4E6EB] hover:bg-[#D8DADF] transition-colors px-3 py-1.5 rounded-md font-semibold text-[15px] text-[#050505] cursor-pointer select-none"
        >
          <AccountMultiple :size="16" class="text-[#050505]" />
          Znajomi znajomych
        </div>
      </div>

      <!-- Główny Kontener Pola Wyszukiwania wraz z Listą -->
      <div class="mt-4 relative">
        <!-- Input Szukaj (Zrzut ekranu 2026-06-25 o 10.35.59.png) -->
        <div
          class="relative border rounded-xl transition-all duration-200 bg-white"
          :class="[
            isFocused
              ? 'border-[#1877F2] ring-2 ring-[#1877F2]/20 rounded-b-none border-b-transparent'
              : 'border-gray-300 hover:border-gray-400 pb-2',
          ]"
        >
          <!-- Label unoszący się / aktywacyjny -->
          <label
            class="absolute left-4 transition-all duration-200 pointer-events-none"
            :class="[
              isFocused || searchQuery
                ? 'top-1.5 text-[12px] text-[#1877F2]'
                : 'top-1/2 -translate-y-1/2 text-[15px] text-[#65676B]',
            ]"
          >
            Szukaj
          </label>

          <input
            type="text"
            v-model="searchQuery"
            @focus="isFocused = true"
            class="w-full bg-transparent px-4 pt-6 pb-1 text-[15px] text-[#050505] outline-none"
          />
        </div>

        <!-- Rozwijana lista propozycji po kliknięciu (Zrzut ekranu 2026-06-25 o 10.35.59.png) -->
        <div
          v-if="isFocused"
          class="border border-t-0 border-[#1877F2] rounded-b-xl bg-white shadow-lg overflow-hidden z-50 p-4 pt-2 space-y-3"
        >
          <h3 class="text-[17px] font-bold text-[#050505] px-1">Popularne</h3>

          <div class="space-y-1">
            <div
              v-for="hobby in filteredHobbies"
              :key="hobby.id"
              @mousedown="selectHobby(hobby.name)"
              class="flex items-center gap-3 p-2 rounded-lg hover:bg-gray-100 transition-colors cursor-pointer select-none"
              :class="{ 'bg-blue-50/60': selectedHobbies.includes(hobby.name) }"
            >
              <!-- Ikona Hobby z tłem kołowym -->
              <div
                class="w-10 h-10 bg-[#E4E6EB] rounded-full flex items-center justify-center text-[#050505] shrink-0"
              >
                <Controller :size="20" />
              </div>

              <!-- Teksty -->
              <div class="flex flex-col min-w-0">
                <span class="text-[15px] font-semibold text-[#050505] leading-tight">{{
                  hobby.name
                }}</span>
                <span class="text-[13px] text-[#65676B] mt-0.5"
                  >Dodane przez {{ hobby.count }}</span
                >
              </div>
            </div>
          </div>
        </div>

        <!-- Podpis pod polem (widoczny, gdy lista jest zamknięta) -->
        <p v-if="!isFocused" class="text-[13px] text-[#65676B] px-1 mt-1.5">Dodaj do 10 hobby.</p>
      </div>

      <!-- Linia separatora i przyciski akcji (Zrzut ekranu 2026-06-25 o 10.35.46.png) -->
      <div class="mt-5 pt-4 border-t border-gray-200 flex justify-end gap-2">
        <button
          @click="handleCancel"
          class="px-5 py-2 bg-[#E4E6EB] hover:bg-[#D8DADF] text-[#050505] font-semibold text-[15px] rounded-lg transition-colors"
        >
          Anuluj
        </button>
        <button
          @click="handleSave"
          :disabled="selectedHobbies.length === 0"
          class="px-5 py-2 font-semibold text-[15px] rounded-lg transition-colors"
          :class="[
            selectedHobbies.length > 0
              ? 'bg-[#E4E6EB] hover:bg-[#D8DADF] text-[#050505]'
              : 'bg-[#E4E6EB] text-[#BCC0C4] cursor-not-allowed',
          ]"
        >
          Zapisz
        </button>
      </div>
    </div>
  </div>
</template>
