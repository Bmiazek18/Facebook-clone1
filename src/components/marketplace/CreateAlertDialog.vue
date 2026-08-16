<!-- components/CreateAlertDialog.vue -->
<template>
  <div class="w-full max-w-[500px] bg-theme-bg-secondary">
    <!-- Główna zawartość -->
    <div class="p-4 space-y-5">
      <!-- Informacja -->
      <p class="text-[15px] text-theme-text">
        Otrzymuj powiadomienia, gdy nowe ogłoszenia pasują do Twoich filtrów i haseł wyszukania.
      </p>

      <!-- Wyszukiwana fraza -->
      <div>
        <h3 class="font-semibold text-[15px] text-theme-text mb-0.5">
          Wyszukiwana fraza
        </h3>
        <p class="text-[15px] text-theme-text-secondary">
          {{ searchPhrase || 'Brak wpisanej frazy' }}
        </p>
      </div>

      <!-- Cena -->
      <div>
        <h3 class="font-semibold text-[15px] text-theme-text mb-2">
          Cena
        </h3>
        <div class="flex items-center gap-3">
          <input
            type="number"
            v-model="priceMin"
            placeholder="Min."
            class="w-full bg-[#F1F2F5] dark:bg-[#333334] border border-transparent rounded-lg p-2.5 focus:outline-none focus:ring-1 focus:ring-[#0866FF] text-theme-text text-[15px] placeholder-theme-text-secondary transition-shadow"
          />
          <span class="text-[15px] text-theme-text">do</span>
          <input
            type="number"
            v-model="priceMax"
            placeholder="Maks."
            class="w-full bg-[#F1F2F5] dark:bg-[#333334] border border-transparent rounded-lg p-2.5 focus:outline-none focus:ring-1 focus:ring-[#0866FF] text-theme-text text-[15px] placeholder-theme-text-secondary transition-shadow"
          />
        </div>
      </div>

      <!-- Lokalizacja -->
      <div>
        <h3 class="font-semibold text-[15px] text-theme-text mb-1">
          Lokalizacja
        </h3>
        <!-- Przycisk z chevronem (udaje select lub otwiera kolejny modal) -->
        <button
          @click="emit('change-location')"
          class="w-full flex items-center justify-between text-[15px] text-theme-text hover:bg-theme-hover p-2 -mx-2 rounded-lg transition-colors cursor-pointer group"
        >
          <span>{{ location }}</span>
          <ChevronRightIcon :size="24" class="text-theme-text-secondary group-hover:text-theme-text transition-colors" />
        </button>
      </div>
    </div>

    <!-- Stopka z przyciskiem -->
    <div class="border-t border-theme-border p-4 flex justify-end">
      <button
        @click="handleCreateAlert"
        class="bg-[#0866FF] hover:bg-[#075ce6] text-white font-semibold py-2 px-5 rounded-lg text-[15px] transition-colors"
      >
        Utwórz alert
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, defineProps, defineEmits } from 'vue'
import ChevronRightIcon from 'vue-material-design-icons/ChevronRight.vue'

// Props, by móc wstrzyknąć aktualnie wyszukiwaną frazę i lokalizację z komponentu rodzica
const props = defineProps({
  searchPhrase: {
    type: String,
    default: 'Artykuły biurowe'
  },
  location: {
    type: String,
    default: 'Łęczyca (Gmina) · 500 km'
  }
})

const emit = defineEmits(['submit', 'change-location'])

// Lokalne stany dla ceny
const priceMin = ref<number | null>(null)
const priceMax = ref<number | null>(null)

// Obsługa zapisu
const handleCreateAlert = () => {
  emit('submit', {
    searchPhrase: props.searchPhrase,
    priceMin: priceMin.value,
    priceMax: priceMax.value,
    location: props.location
  })
}
</script>
