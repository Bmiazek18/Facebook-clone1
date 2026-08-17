<script setup lang="ts">
import { ref, computed } from 'vue'
import Sidebar from '@/components/common/Sidebar.vue'
import Plus from 'vue-material-design-icons/Plus.vue'
import Storefront from 'vue-material-design-icons/Storefront.vue'
import Bell from 'vue-material-design-icons/Bell.vue'
import TagOutline from 'vue-material-design-icons/TagOutline.vue'
import MapMarker from 'vue-material-design-icons/MapMarker.vue'
import Shopping from 'vue-material-design-icons/Shopping.vue'

// Icon imports for categories
import Car from 'vue-material-design-icons/Car.vue'
import HomeCity from 'vue-material-design-icons/HomeCity.vue'
import Paw from 'vue-material-design-icons/Paw.vue'
import HomeVariant from 'vue-material-design-icons/HomeVariant.vue'
import HammerWrench from 'vue-material-design-icons/HammerWrench.vue'
import Run from 'vue-material-design-icons/Run.vue'
import Heart from 'vue-material-design-icons/Heart.vue'
import Home from 'vue-material-design-icons/Home.vue'
import Cellphone from 'vue-material-design-icons/Cellphone.vue'
import Brush from 'vue-material-design-icons/Brush.vue'
import TshirtCrew from 'vue-material-design-icons/TshirtCrew.vue'
import Shovel from 'vue-material-design-icons/Shovel.vue'
import TagTextOutline from 'vue-material-design-icons/TagTextOutline.vue'
import MovieOpen from 'vue-material-design-icons/MovieOpen.vue'
import GamepadVariant from 'vue-material-design-icons/GamepadVariant.vue'

const props = defineProps({
  selectedRadius: { type: Number, default: 402 },
  selectedCityName: { type: String, default: 'Łęczyca' }
})
const emit = defineEmits(['open-location', 'update:search'])

const openLocation = () => emit('open-location')
const showNotificationDots = ref(true);
const allowFollowing = ref(true);
const sellerMode = ref(false);
// Full list of categories matching the screenshot, defined as a ref
const categories = ref([
  { name: 'Pojazdy', icon: Car },
  { name: 'Wynajem nieruchomości', icon: HomeCity },
  { name: 'Artykuły biurowe', icon: TagOutline },
  { name: 'Artykuły dla zwierząt domowych', icon: Paw },
  { name: 'Artykuły domowe', icon: HomeVariant },
  { name: 'Artykuły remontowe', icon: HammerWrench },
  { name: 'Artykuły sportowe', icon: Run },
  { name: 'Darmowe przedmioty', icon: TagOutline },
  { name: 'Dla rodziny', icon: Heart },
  { name: 'Domy na sprzedaż', icon: Home },
  { name: 'Elektronika', icon: Cellphone },
  { name: 'Hobby', icon: Brush },
  { name: 'Odzież', icon: TshirtCrew },
  { name: 'Ogród i otoczenie', icon: Shovel },
  { name: 'Ogłoszenia drobne', icon: TagTextOutline },
  { name: 'Rozrywka', icon: MovieOpen },
  { name: 'Zabawki i gry', icon: GamepadVariant },
])

// Sidebar items based on existing router-link elements
const sidebarItems = computed(() => [
  { icon: Storefront, text: 'Przeglądaj wszystkie', route: '/marketplace' },
  { icon: Bell, text: 'Powiadomienia', route: '/marketplace/notifications' },
  { icon: Shopping, text: 'Kupno', route: '/marketplace' },
  { icon: TagOutline, text: 'Sprzedaż', route: '/marketplace/you/dashboard' },
])
</script>

<template>
  <Sidebar
    title="Marketplace"
    searchPlaceholder="Wyszukaj w Marketplace"
    :showSettings="true"
    :showSearch="true"
    :items="sidebarItems"
    @update:search="(q) => emit('update:search', q)"
    :createButton="{
      icon: Plus,
      text: 'Utwórz nowe ogłoszenie',
      route: '/marketplace/create/item',
    }"
  >
<template #settings-dropdown>
<div class="w-[360px] max-h-[80vh] overflow-y-auto bg-theme-surface rounded-2xl p-3 text-theme-text shadow-2xl border border-theme-border">

    <!-- Sekcja: Ustawienia powiadomień -->
    <div class="mb-3">
      <!-- Zmniejszono nagłówek z 19px na 16px -->
      <h2 class="text-[16px] font-bold mb-1">Ustawienia powiadomień</h2>
      <!-- Zmniejszono opis z 14px na 12px -->
      <p class="text-[12px] text-gray-500 leading-tight mb-3">
        Możesz zarządzać sposobem powiadamiania o aktualizacjach w Marketplace.
      </p>

      <!-- Wyświetl kropki powiadomień -->
      <!-- Zmniejszono padding pionowy (py-1.5 zamiast py-2) -->
      <div
        @click="showNotificationDots = !showNotificationDots"
        class="flex items-center justify-between py-1.5 mb-1 cursor-pointer select-none"
      >
        <div class="flex items-center gap-2.5">
          <!-- Zmniejszono tło ikony z 40px (w-10) na 32px (w-8) i samą ikonę do w-5 h-5 -->
          <div class="w-8 h-8 bg-gray-200 rounded-full flex items-center justify-center shrink-0 text-gray-800">
            <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <rect x="4" y="4" width="16" height="16" rx="4" stroke-width="2"></rect>
              <circle cx="16" cy="8" r="2" fill="currentColor"></circle>
            </svg>
          </div>
          <!-- Zmniejszono nazwę z 16px na 14px -->
          <span class="text-[14px] font-medium">Wyświetl kropki powiadomień</span>
        </div>
        <!-- Zmniejszono toggle: szerokość w-10 (było 12), wysokość h-6 (było 7) -->
        <div
          :class="showNotificationDots ? 'bg-blue-600' : 'bg-gray-400'"
          class="w-10 h-6 rounded-full flex items-center px-1 cursor-pointer shrink-0 transition-colors duration-200"
        >
          <!-- Zmniejszono kropkę do w-4 h-4 (było 5) oraz przesunięcie na translate-x-4 (było 5) -->
          <div
            :class="showNotificationDots ? 'translate-x-4' : 'translate-x-0'"
            class="w-4 h-4 bg-white rounded-full shadow-sm transform transition-transform duration-200"
          ></div>
        </div>
      </div>

      <!-- Dostosuj powiadomienia -->
      <div class="flex items-center justify-between py-1.5 cursor-pointer select-none hover:opacity-80 transition-opacity">
        <div class="flex items-center gap-2.5">
          <div class="w-8 h-8 bg-gray-200 rounded-full flex items-center justify-center shrink-0 text-gray-800">
            <svg class="w-4 h-4" fill="currentColor" viewBox="0 0 24 24">
              <path d="M12 22c1.1 0 2-.9 2-2h-4c0 1.1.9 2 2 2zm6-6v-5c0-3.07-1.63-5.64-4.5-6.32V4c0-.83-.67-1.5-1.5-1.5s-1.5.67-1.5 1.5v.68C7.64 5.36 6 7.92 6 11v5l-2 2v1h16v-1l-2-2z"></path>
            </svg>
          </div>
          <span class="text-[14px] font-medium">Dostosuj powiadomienia</span>
        </div>
      </div>
    </div>

    <hr class="border-gray-300 my-2" />

    <!-- Sekcja: Ustawienia Marketplace -->
    <div class="mt-3">
      <h2 class="text-[16px] font-bold mb-3">Ustawienia Marketplace</h2>

      <!-- Zarządzaj trybem wakacyjnym -->
      <div class="flex items-start gap-2.5 py-1.5 cursor-pointer select-none hover:opacity-80 transition-opacity">
        <div class="w-8 h-8 bg-gray-200 rounded-full flex items-center justify-center shrink-0 text-gray-800">
          <svg class="w-4 h-4" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
            <path d="M12 22v-9m0 0a9 9 0 100-18 9 9 0 000 18zm0 0h9"></path>
          </svg>
        </div>
        <div class="flex flex-col justify-center min-h-[32px]">
          <span class="text-[14px] font-medium">Zarządzaj trybem wakacyjnym</span>
        </div>
      </div>

      <!-- Udostępnij opcję obserwowania Cię -->
      <div
        @click="allowFollowing = !allowFollowing"
        class="flex items-start justify-between py-2 cursor-pointer select-none"
      >
        <div class="flex items-start gap-2.5 pr-3">
          <div class="w-8 h-8 bg-gray-200 rounded-full flex items-center justify-center shrink-0 text-gray-800">
            <svg class="w-4 h-4" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
              <path d="M4 11a9 9 0 019 9M4 4a16 16 0 0116 16M4 20h.01"></path>
            </svg>
          </div>
          <div>
            <span class="text-[14px] font-medium block mb-0.5">Udostępnij opcję obserwowania Cię</span>
            <span class="text-[12px] text-gray-500 leading-tight block">Obserwujący w Marketplace oraz Twoich grupach kupna i sprzedaży będą widzieć, kiedy wystawisz ogłoszenie.</span>
          </div>
        </div>
        <div
          :class="allowFollowing ? 'bg-blue-600' : 'bg-gray-400'"
          class="w-10 h-6 rounded-full flex items-center px-1 shrink-0 mt-0.5 transition-colors duration-200"
        >
          <div
            :class="allowFollowing ? 'translate-x-4' : 'translate-x-0'"
            class="w-4 h-4 bg-white rounded-full shadow-sm transform transition-transform duration-200"
          ></div>
        </div>
      </div>

      <!-- Ustaw wiadomość niestandardową do nabywcy -->
      <div class="flex items-start gap-2.5 py-2 cursor-pointer select-none hover:opacity-80 transition-opacity">
        <div class="w-8 h-8 bg-gray-200 rounded-full flex items-center justify-center shrink-0 text-gray-800">
          <svg class="w-4 h-4" fill="currentColor" viewBox="0 0 24 24">
            <path d="M20 2H4c-1.1 0-2 .9-2 2v18l4-4h14c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2zM9 11H7V9h2v2zm4 0h-2V9h2v2zm4 0h-2V9h2v2z"></path>
          </svg>
        </div>
        <div>
          <span class="text-[14px] font-medium block mb-0.5">Ustaw wiadomość niestandardową do nabywcy</span>
          <span class="text-[12px] text-gray-500 leading-tight block">Wiadomość, którą obecnie wysyłasz do sprzedawców: „Czy jest nadal dostępny?”</span>
        </div>
      </div>

      <!-- Tryb sprzedawcy -->
      <div
        @click="sellerMode = !sellerMode"
        class="flex items-start justify-between py-2 cursor-pointer select-none"
      >
        <div class="flex items-start gap-2.5 pr-3">
          <div class="w-8 h-8 bg-gray-200 rounded-full flex items-center justify-center shrink-0 text-gray-800">
            <svg class="w-4 h-4" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
              <path d="M12 4v4m0 0l-3-3m3 3l3-3M12 20v-4m0 0l3 3m-3-3l-3 3M4 12h4m0 0l-3 3m3-3l-3-3M20 12h-4m0 0l3-3m-3 3l3 3"></path>
              <circle cx="12" cy="12" r="3"></circle>
            </svg>
          </div>
          <div>
            <span class="text-[14px] font-medium block mb-0.5">Tryb sprzedawcy</span>
            <span class="text-[12px] text-gray-500 leading-tight block">
              Włącz tryb sprzedawcy, aby domyślnie był wyświetlany pulpit sprzedawcy podczas odwiedzania Marketplace.
              <a href="#" class="text-blue-600 hover:underline" @click.stop>Dowiedz się więcej</a>
            </span>
          </div>
        </div>
        <div
          :class="sellerMode ? 'bg-blue-600' : 'bg-gray-400'"
          class="w-10 h-6 rounded-full flex items-center px-1 shrink-0 mt-0.5 transition-colors duration-200"
        >
          <div
            :class="sellerMode ? 'translate-x-4' : 'translate-x-0'"
            class="w-4 h-4 bg-white rounded-full shadow-sm transform transition-transform duration-200"
          ></div>
        </div>
      </div>

      <!-- Ogłoszenia partnerów -->
      <div class="flex items-start gap-2.5 py-2 cursor-pointer select-none hover:opacity-80 transition-opacity">
        <div class="w-8 h-8 bg-gray-200 rounded-full flex items-center justify-center shrink-0 text-gray-800">
          <svg class="w-4 h-4" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
            <path d="M3 3h18v4H3zM4 7v14h16V7"></path>
            <path d="M9 11h6M9 15h6"></path>
          </svg>
        </div>
        <div>
          <span class="text-[14px] font-medium block mb-0.5">Ogłoszenia partnerów</span>
          <span class="text-[12px] text-gray-500 leading-tight block">Wybierz partnerów, od których chcesz kupować</span>
        </div>
      </div>

      <!-- Ustawienia konta reklamowego -->
      <div class="flex items-start gap-2.5 py-2 cursor-pointer select-none hover:opacity-80 transition-opacity">
        <div class="w-8 h-8 bg-gray-200 rounded-full flex items-center justify-center shrink-0 text-gray-800">
          <svg class="w-4 h-4" fill="currentColor" viewBox="0 0 24 24">
            <rect x="2" y="4" width="20" height="16" rx="2"></rect>
            <circle cx="8" cy="12" r="3" fill="white"></circle>
            <path d="M14 10h5v2h-5zM14 14h3v2h-3z" fill="white"></path>
          </svg>
        </div>
        <div>
          <span class="text-[14px] font-medium block mb-0.5">Ustawienia konta reklamowego</span>
          <span class="text-[12px] text-gray-500 leading-tight block">Dane kontaktowe konta reklamowego, role na koncie reklamowym i płatności firmowe.</span>
        </div>
      </div>

    </div>
  </div>
</template>
    <template #pre-list>
      <div class="px-2 pt-3 border-t border-theme-border">
        <h3 class="font-semibold text-[17px] mb-2 text-theme-text">Lokalizacja</h3>
        <button
          @click="openLocation"
          class="text-blue-600 text-sm hover:underline flex items-center"
        >
          <MapMarker :size="16" class="mr-1" />
          {{ props.selectedCityName }} - W promieniu {{ props.selectedRadius }} km
        </button>
      </div>
    </template>

    <template #list-items>
 <div>
     <h3 class="font-semibold text-[17px] px-2 mb-2 text-theme-text">Kategorie</h3>

  <ul class="space-y-1">
    <li v-for="(cat, index) in categories" :key="index">
      <a
        href="#"
        class="flex items-center gap-3 px-2 py-2 hover:bg-theme-hover rounded-lg cursor-pointer transition-colors"
      >
        <!-- Dodano kontener dla ikony: okrągłe, szare tło (bg-gray-200), sztywne wymiary (w-9 h-9) i wyśrodkowanie zawartości -->
        <div class="w-9 h-9 bg-gray-200 rounded-full flex items-center justify-center shrink-0 text-gray-900">
          <component :is="cat.icon" :size="20" />
        </div>

        <!-- Tekst kategorii -->
        <span class="font-medium text-[15px] text-theme-text">{{ cat.name }}</span>
      </a>
    </li>
  </ul>
</div>
    </template>
  </Sidebar>
</template>



