<script setup lang="ts">
import { ref } from 'vue'
import VueApexCharts from 'vue3-apexcharts'

// Importy ikon
import ChevronDownIcon from 'vue-material-design-icons/ChevronDown.vue'
import DownloadIcon from 'vue-material-design-icons/Download.vue'
import InformationIcon from 'vue-material-design-icons/Information.vue'
import MenuUpIcon from 'vue-material-design-icons/MenuUp.vue'

// --- Konfiguracja Wykresu 1 (Łączna liczba członków) ---
const chart1Series = ref([
  {
    name: 'Członkowie',
    data: [0, 0, 0, 0, 1] // Płaska linia na 0, skok do 1 na końcu
  }
])

const chart1Options = ref({
  chart: {
    type: 'line',
    toolbar: { show: false },
    zoom: { enabled: false },
    fontFamily: 'inherit',
    parentHeightOffset: 0
  },
  colors: ['#1877f2'], // Niebieski kolor Facebooka
  dataLabels: { enabled: false },
  stroke: {
    curve: 'straight',
    width: 2.5
  },
  grid: {
    borderColor: '#e4e6eb',
    strokeDashArray: 0,
    xaxis: { lines: { show: false } },
    yaxis: { lines: { show: true } },
    padding: { top: 10, right: 0, bottom: 0, left: 10 }
  },
  xaxis: {
    categories: ['19 lip', '26 lip', '2 sie', '9 sie', '12 sie'],
    axisBorder: { show: true, color: '#e4e6eb', height: 1 },
    axisTicks: { show: true, color: '#e4e6eb', height: 4 },
    labels: {
      style: { colors: '#65676b', fontSize: '12px' },
      offsetY: 0
    },
    tooltip: { enabled: false }
  },
  yaxis: {
    opposite: true, // Oś Y po prawej stronie (jak na zrzucie ekranu)
    min: 0,
    max: 1,
    tickAmount: 1, // Wymusza tylko dwie etykiety: 0 i 1
    labels: {
      style: { colors: '#65676b', fontSize: '12px' },
      offsetX: -10
    }
  },
  markers: {
    size: 0,
    hover: { size: 4 }
  },
  tooltip: {
    theme: 'light'
  }
})

// --- Konfiguracja Wykresu 2 (Prośby o dołączenie) ---
const chart2Series = ref([
  {
    name: 'Prośby o dołączenie',
    data: [0, 0, 0, 0, 0] // Płaska linia
  }
])

const chart2Options = ref({
  ...chart1Options.value,
  yaxis: {
    ...chart1Options.value.yaxis,
    show: false // Ukryta oś Y dla płaskiego wykresu na 0
  },
  grid: {
    ...chart1Options.value.grid,
    yaxis: { lines: { show: false } },
    padding: { top: 0, right: 0, bottom: 0, left: 0 }
  }
})
</script>

<template>
  <div class="min-h-screen bg-theme-bg font-sans text-theme-text selection:bg-blue-600 pb-20">

    <!-- Pasek nagłówka (Sticky) -->
    <header class="bg-white border-b border-theme-border px-4 py-3 flex items-center justify-between sticky top-0 z-50">
      <!-- Opcje po lewej (Wybór zakresu) -->
      <button class="bg-theme-bg-tertiary hover:bg-theme-hover-strong transition-colors rounded-lg px-3.5 py-2 flex items-center gap-1.5 cursor-pointer">
        <span class="text-[15px] font-semibold text-theme-text">{{ $t('groups.ostatnie28Dni') }}</span>
        <ChevronDownIcon :size="20" class="text-theme-text" />
      </button>

      <!-- Opcje po prawej (Pobieranie) -->
      <button class="bg-theme-bg-tertiary hover:bg-theme-hover-strong transition-colors rounded-lg px-3.5 py-2 flex items-center gap-2 cursor-pointer">
        <DownloadIcon :size="18" class="text-theme-text" />
        <span class="text-[15px] font-semibold text-theme-text">{{ $t('groups.pobierz') }}</span>
      </button>
    </header>

    <!-- Główna zawartość (Karty z wykresami) -->
    <main class="w-full max-w-4xl mx-auto p-4 sm:p-6 space-y-4">

      <!-- Karta 1: Łącznie członków -->
      <div class="bg-white rounded-xl shadow-sm border border-theme-border p-5">
        <div class="mb-4">
          <div class="flex items-center gap-1.5">
            <h2 class="text-[17px] font-bold leading-tight">{{ $t('groups.lacznie1Czlonkow') }}</h2>
            <InformationIcon :size="16" class="text-theme-text-secondary cursor-pointer hover:text-theme-text transition-colors" />
          </div>
          <p class="text-[13px] text-theme-text-secondary mt-0.5">{{ $t('groups.12Sie2026') }}</p>
        </div>

        <!-- Wykres 1 -->
        <div class="h-[250px] w-full">
          <VueApexCharts
            type="line"
            height="100%"
            :options="chart1Options"
            :series="chart1Series"
          />
        </div>
      </div>

      <!-- Karta 2: Prośby o dołączenie -->
      <div class="bg-white rounded-xl shadow-sm border border-theme-border p-5">
        <div class="mb-4">
          <div class="flex items-center gap-1.5">
            <h2 class="text-[17px] font-bold leading-tight">{{ $t('groups.0ProsbODolaczenie') }}</h2>
            <InformationIcon :size="16" class="text-theme-text-secondary cursor-pointer hover:text-theme-text transition-colors" />
          </div>
          <p class="text-[13px] text-theme-text-secondary mt-0.5">{{ $t('groups.16Lip202612') }}</p>
        </div>

        <!-- Wykres 2 -->
        <div class="h-[200px] w-full mb-2">
          <VueApexCharts
            type="line"
            height="100%"
            :options="chart2Options"
            :series="chart2Series"
          />
        </div>

        <!-- Stopka karty 2 -->
        <div class="mt-4">
          <p class="text-[13px] text-theme-text-secondary text-center mb-3">{{ $t('groups.liczbaNowychProsbO') }}</p>
          <button class="w-full bg-theme-bg-tertiary hover:bg-theme-hover-strong transition-colors text-theme-text text-[15px] font-semibold py-2 rounded-lg cursor-pointer">{{ $t('groups.zobaczWszystkieProsbyO') }}</button>
        </div>
      </div>

      <!-- Karta 3: Status rozpatrzonych próśb -->
      <div class="bg-white rounded-xl shadow-sm border border-theme-border p-5">
        <div>
          <div class="flex items-center gap-2">
            <h2 class="text-[17px] font-bold leading-tight">{{ $t('groups.rozpatrzono0Prosb') }}</h2>
            <!-- Zielony wskaźnik trendu -->
            <div class="flex items-center text-[#31a24c] font-semibold text-[13px]">
              <MenuUpIcon :size="20" class="-mr-1" />
              <span>0%</span>
            </div>
            <InformationIcon :size="16" class="text-theme-text-secondary cursor-pointer hover:text-theme-text transition-colors ml-1" />
          </div>
          <p class="text-[13px] text-theme-text-secondary mt-0.5">{{ $t('groups.16Lip202612') }}</p>
        </div>

        <!-- Siatka z 3 kolumnami -->
        <div class="grid grid-cols-1 sm:grid-cols-3 gap-3 mt-5">
          <!-- Zatwierdzone -->
          <div class="bg-theme-bg rounded-xl p-4 flex flex-col justify-between h-[120px]">
            <h3 class="text-[16px] font-semibold text-theme-text">{{ $t('groups.zatwierdzone') }}</h3>
            <div>
              <div class="text-[36px] font-light text-theme-text leading-none mb-1">0</div>
              <div class="flex items-center text-theme-text-secondary text-[13px] font-semibold">
                <MenuUpIcon :size="18" class="-ml-1" />
                <span>0%</span>
              </div>
            </div>
          </div>

          <!-- Odrzucone -->
          <div class="bg-theme-bg rounded-xl p-4 flex flex-col justify-start h-[120px]">
            <h3 class="text-[16px] font-semibold text-theme-text">{{ $t('groups.odrzucone') }}</h3>
            <div class="text-[36px] font-light text-theme-text leading-none mt-auto pb-1">0</div>
          </div>

          <!-- Zablokowane -->
          <div class="bg-theme-bg rounded-xl p-4 flex flex-col justify-start h-[120px]">
            <h3 class="text-[16px] font-semibold text-theme-text">{{ $t('groups.zablokowane') }}</h3>
            <div class="text-[36px] font-light text-theme-text leading-none mt-auto pb-1">0</div>
          </div>
        </div>
      </div>

    </main>
  </div>
</template>

<style>
/* Nadpisywanie domyślnych stylów apexcharts pod layout Facebooka */
.apexcharts-tooltip {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15) !important;
  border-radius: 8px !important;
  border: 1px solid #e4e6eb !important;
}
.apexcharts-tooltip-title {
  background: #f0f2f5 !important;
  border-bottom: 1px solid #e4e6eb !important;
  font-family: inherit !important;
  font-weight: 600 !important;
  padding: 6px 10px !important;
}
.apexcharts-text tspan {
  font-family: inherit;
}
</style>
