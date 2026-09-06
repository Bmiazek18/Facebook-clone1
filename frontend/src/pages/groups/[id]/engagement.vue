<script setup lang="ts">
import { ref } from 'vue'
import VueApexCharts from 'vue3-apexcharts'

// Import ikony z vue-material-design-icons
import InformationIcon from 'vue-material-design-icons/Information.vue'

// --------------------------------------------------------
// 1. KARTA: 0 postów
// --------------------------------------------------------
const postsFilter = ref('Posty')
const postsFilterOptions = ['Posty', 'Komentarze', 'Reakcje', 'Wszystkie']

const chartPostsSeries = ref([
  {
    name: 'Posty',
    data: [0, 0, 0, 0, 0]
  }
])

const chartPostsOptions = ref({
  chart: {
    type: 'line',
    toolbar: { show: false },
    zoom: { enabled: false },
    fontFamily: 'inherit',
    parentHeightOffset: 0
  },
  colors: ['#1877f2'], // Facebook Blue
  dataLabels: { enabled: false },
  stroke: { curve: 'straight', width: 2.5 },
  grid: {
    borderColor: '#e4e6eb',
    yaxis: { lines: { show: false } },
    padding: { top: 10, right: 10, bottom: 0, left: 10 }
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
  yaxis: { show: false }, // Ukryta oś Y
  markers: { size: 0, hover: { size: 4 } },
  tooltip: { theme: 'light' }
})

// --------------------------------------------------------
// 2. KARTA: 1 aktywny członek
// --------------------------------------------------------
const chartActiveMembersSeries = ref([
  {
    name: 'Aktywni członkowie',
    data: [0, 0, 0, 0, 1]
  }
])

const chartActiveMembersOptions = ref({
  ...chartPostsOptions.value,
  grid: {
    borderColor: '#e4e6eb',
    yaxis: { lines: { show: true } }, // Pokazujemy poziome linie siatki
    padding: { top: 10, right: 0, bottom: 0, left: 10 }
  },
  yaxis: {
    show: true,
    opposite: true, // Oś po prawej stronie
    min: 0,
    max: 1,
    tickAmount: 1, // Wymusza tylko '0' i '1'
    labels: {
      style: { colors: '#65676b', fontSize: '12px' },
      offsetX: -10
    }
  }
})

// --------------------------------------------------------
// 3. KARTA: Popularne dni
// --------------------------------------------------------
const chartPopularDaysSeries = ref([
  {
    name: 'Aktywność',
    data: [0, 0, 0, 0, 0, 0, 0]
  }
])

const chartPopularDaysOptions = ref({
  ...chartPostsOptions.value,
  xaxis: {
    categories: ['Pn.', 'Wt.', 'Śr.', 'Czw.', 'Pt.', 'Sob.', 'Nd.'],
    axisBorder: { show: false },
    axisTicks: { show: false },
    labels: {
      style: { colors: '#65676b', fontSize: '12px' },
      offsetY: 0
    },
    tooltip: { enabled: false }
  },
  yaxis: {
    show: true,
    min: 0,
    max: 1,
    tickAmount: 1,
    labels: {
      formatter: (val: number) => (val === 0 ? '0' : ''),
      style: { colors: '#65676b', fontSize: '12px' },
      offsetX: 10
    }
  },
  grid: {
    borderColor: '#e4e6eb',
    yaxis: { lines: { show: true } },
    padding: { top: 30, right: 10, bottom: 0, left: 0 }
  },
  stroke: { curve: 'straight', width: 0 } // Brak niebieskiej linii dla stanu pustego (tylko linia osi)
})

// --------------------------------------------------------
// 4. KARTA: Popularne godziny
// --------------------------------------------------------
const activeDayHourFilter = ref('Pn.')
const daysHourOptions = ['Pn.', 'W..', 'Śr.', 'C..', 'Pt.', 'S..', 'N..'] // Zgodnie z uciętymi etykietami na zrzucie ekranu
</script>

<template>
  <div class="min-h-screen bg-[#f0f2f5] font-sans text-[#050505] selection:bg-blue-600 p-4 sm:p-6 pb-20">

    <div class="w-full max-w-[900px] mx-auto space-y-4">

      <!-- Karta 1: Posty -->
      <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-5">
        <div class="flex flex-col md:flex-row md:items-start justify-between gap-4 mb-4">
          <div>
            <div class="flex items-center gap-1.5">
              <h2 class="text-[17px] font-bold leading-tight">{{ $t('groups.0Postow') }}</h2>
              <InformationIcon :size="16" class="text-[#65676b] cursor-pointer hover:text-[#050505] transition-colors" />
            </div>
            <p class="text-[13px] text-[#65676b] mt-0.5">{{ $t('groups.16Lip202612') }}</p>
          </div>

          <!-- Pigułki filtrów (Posty, Komentarze...) -->
          <div class="flex flex-wrap items-center gap-2">
            <button
              v-for="filter in postsFilterOptions"
              :key="filter"
              @click="postsFilter = filter"
              :class="[
                'px-3.5 py-1.5 rounded-full text-[14px] font-semibold transition-colors cursor-pointer',
                postsFilter === filter
                  ? 'bg-[#e7f3ff] text-[#1877f2]'
                  : 'bg-[#e4e6eb] text-[#050505] hover:bg-[#d8dadf]'
              ]"
            >
              {{ filter }}
            </button>
          </div>
        </div>

        <div class="h-[250px] w-full mt-8">
          <VueApexCharts
            type="line"
            height="100%"
            :options="chartPostsOptions"
            :series="chartPostsSeries"
          />
        </div>
      </div>

      <!-- Karta 2: Aktywni członkowie -->
      <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-5">
        <div class="mb-4">
          <div class="flex items-center gap-1.5">
            <h2 class="text-[17px] font-bold leading-tight">{{ $t('groups.1AktywnyCzlonek') }}</h2>
            <InformationIcon :size="16" class="text-[#65676b] cursor-pointer hover:text-[#050505] transition-colors" />
          </div>
          <p class="text-[13px] text-[#65676b] mt-0.5">{{ $t('groups.16Lip202612') }}</p>
        </div>

        <div class="h-[250px] w-full mt-4">
          <VueApexCharts
            type="line"
            height="100%"
            :options="chartActiveMembersOptions"
            :series="chartActiveMembersSeries"
          />
        </div>

        <p class="text-center text-[13px] text-[#65676b] mt-4">{{ $t('groups.aktywniCzlonkowieDziennie16') }}</p>
      </div>

      <!-- Grid 2 Kolumny: Popularne dni / Popularne godziny -->
      <div class="grid grid-cols-1 md:grid-cols-2 gap-4">

        <!-- Karta 3: Popularne dni -->
        <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-5 flex flex-col">
          <div class="mb-4">
            <div class="flex items-center gap-1.5">
              <h2 class="text-[17px] font-bold leading-tight">{{ $t('groups.popularneDni') }}</h2>
              <InformationIcon :size="16" class="text-[#65676b] cursor-pointer hover:text-[#050505] transition-colors" />
            </div>
            <p class="text-[13px] text-[#65676b] mt-1 leading-snug">{{ $t('groups.sredniaLiczbaPostowKomentarzy') }}</p>
          </div>

          <div class="h-[200px] w-full mt-auto">
            <VueApexCharts
              type="line"
              height="100%"
              :options="chartPopularDaysOptions"
              :series="chartPopularDaysSeries"
            />
          </div>
        </div>

        <!-- Karta 4: Popularne godziny -->
        <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-5 flex flex-col">
          <div class="mb-4">
            <div class="flex items-center gap-1.5">
              <h2 class="text-[17px] font-bold leading-tight">{{ $t('groups.popularneGodziny') }}</h2>
              <InformationIcon :size="16" class="text-[#65676b] cursor-pointer hover:text-[#050505] transition-colors" />
            </div>
            <p class="text-[13px] text-[#65676b] mt-1 leading-snug">{{ $t('groups.sredniaLiczbaPostowKomentarzy2') }}</p>
          </div>

          <!-- Pigułki Dni -->
          <div class="flex flex-wrap items-center gap-1.5 mb-8">
            <button
              v-for="day in daysHourOptions"
              :key="day"
              @click="activeDayHourFilter = day"
              :class="[
                'w-10 h-10 rounded-full text-[14px] font-semibold transition-colors flex items-center justify-center cursor-pointer shrink-0',
                activeDayHourFilter === day
                  ? 'bg-[#e7f3ff] text-[#1877f2]'
                  : 'bg-[#e4e6eb] text-[#050505] hover:bg-[#d8dadf]'
              ]"
            >
              {{ day }}
            </button>
          </div>

          <!-- Stan pusty -->
          <div class="flex-1 flex items-center justify-center min-h-[100px]">
            <span class="text-[15px] font-semibold text-[#65676b]">{{ $t('groups.brakDanychDoWyswietlenia') }}</span>
          </div>
        </div>

      </div>
    </div>
  </div>
</template>
