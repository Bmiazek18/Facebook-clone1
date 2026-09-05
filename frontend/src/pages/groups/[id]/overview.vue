<script setup lang="ts">
import { ref, markRaw, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useGroupsStore } from '@/stores/groups'
import VueApexCharts from 'vue3-apexcharts'

// Importy ikon dla sekcji "Do sprawdzenia"
import MessageAlertOutlineIcon from 'vue-material-design-icons/MessageAlertOutline.vue'
import BellAlertOutlineIcon from 'vue-material-design-icons/BellAlertOutline.vue'
import TextBoxOutlineIcon from 'vue-material-design-icons/TextBoxOutline.vue'
import AccountPlusOutlineIcon from 'vue-material-design-icons/AccountPlusOutline.vue'
import ClipboardCheckOutlineIcon from 'vue-material-design-icons/ClipboardCheckOutline.vue'
import ChevronRightIcon from 'vue-material-design-icons/ChevronRight.vue'

// Importy ikon dla "Podsumowanie statystyk"
import MessageTextOutlineIcon from 'vue-material-design-icons/MessageTextOutline.vue'
import ThumbUpOutlineIcon from 'vue-material-design-icons/ThumbUpOutline.vue'
import ArrowTopRightIcon from 'vue-material-design-icons/ArrowTopRight.vue'
import InformationIcon from 'vue-material-design-icons/Information.vue'

const route = useRoute()
const groupsStore = useGroupsStore()

const groupId = route.params.id as string
const isLoading = ref(true)
const hasAccess = ref(true)

// Dane dla sekcji "Do sprawdzenia"
const reviewItems = ref([
  {
    id: 1,
    title: 'Materiały zgłoszone przez członków',
    subtitle: '0 nowych wpisów dziś',
    count: 0,
    icon: markRaw(MessageAlertOutlineIcon)
  },
  {
    id: 2,
    title: 'Alerty moderacji',
    subtitle: '0 nowych wpisów dziś',
    count: 0,
    icon: markRaw(BellAlertOutlineIcon)
  },
  {
    id: 3,
    title: 'Oczekujące posty',
    subtitle: '0 nowych wpisów dziś',
    count: 0,
    icon: markRaw(TextBoxOutlineIcon)
  },
  {
    id: 4,
    title: 'Prośby o dołączenie',
    subtitle: '0 nowych wpisów dziś',
    count: 0,
    icon: markRaw(AccountPlusOutlineIcon)
  },
  {
    id: 5,
    title: 'Status grupy',
    subtitle: '0 naruszeń administratorów',
    count: 0,
    icon: markRaw(ClipboardCheckOutlineIcon),
    hasDot: true // Wskaźnik (niebieska kropka)
  }
])

// Dane dla sekcji "Podsumowanie statystyk"
const statsItems = ref([
  { id: 1, label: 'Posty', value: 0, trend: '0%', icon: markRaw(TextBoxOutlineIcon) },
  { id: 2, label: 'Komentarze', value: 0, trend: '0%', icon: markRaw(MessageTextOutlineIcon) },
  { id: 3, label: 'Reakcje', value: 0, trend: '0%', icon: markRaw(ThumbUpOutlineIcon) },
])

// --- Konfiguracja Wykresu (ApexCharts) ---
const chartSeries = ref([
  {
    name: 'Aktywni członkowie',
    data: [0, 0, 0, 0, 0, 0, 1] // Zgodnie z wykresem: płasko na 0, skok na koniec
  }
])

const chartOptions = ref({
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
    borderColor: 'rgba(101, 103, 107, 0.2)', // Półprzezroczysty kolor siatki pasujący do jasnego/ciemnego trybu
    strokeDashArray: 0,
    xaxis: { lines: { show: false } },
    yaxis: { lines: { show: true } },
    padding: { top: 10, right: 0, bottom: 0, left: 10 }
  },
  xaxis: {
    categories: ['6 sie', '7 sie', '8 sie', '9 sie', '10 sie', '11 sie', '12 sie'],
    axisBorder: { show: true, color: 'rgba(101, 103, 107, 0.2)', height: 1 },
    axisTicks: { show: true, color: 'rgba(101, 103, 107, 0.2)', height: 4 },
    labels: {
      style: { colors: '#8c939d', fontSize: '12px' },
      offsetY: 0
    },
    tooltip: { enabled: false }
  },
  yaxis: {
    opposite: true, // Oś Y po prawej stronie
    min: 0,
    max: 2,
    tickAmount: 1, // Wymusza tylko dwie etykiety: 0 i 2
    labels: {
      style: { colors: '#8c939d', fontSize: '12px' },
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

const loadOverview = async () => {
  isLoading.value = true
  if (groupId) {
    const overview = await groupsStore.getGroupOverview(groupId)
    if (!overview) {
      hasAccess.value = false
    } else {
      hasAccess.value = true
      // Dane "Do sprawdzenia"
      reviewItems.value[0].count = overview.reportedItemsCount
      reviewItems.value[0].subtitle = `${overview.reportedItemsCount} nowych wpisów dziś`

      reviewItems.value[1].count = overview.moderationAlertsCount
      reviewItems.value[1].subtitle = `${overview.moderationAlertsCount} nowych wpisów dziś`

      reviewItems.value[2].count = overview.pendingPostsCount
      reviewItems.value[2].subtitle = `${overview.pendingPostsCount} nowych wpisów dziś`

      reviewItems.value[3].count = overview.pendingRequestsCount
      reviewItems.value[3].subtitle = `${overview.pendingRequestsCount} nowych wpisów dziś`

      reviewItems.value[4].count = overview.groupStatusViolationCount
      reviewItems.value[4].subtitle = `${overview.groupStatusViolationCount} naruszeń administratorów`

      // Dane "Podsumowanie statystyk"
      statsItems.value[0].value = overview.postsCount7Days
      statsItems.value[0].trend = overview.postsTrend

      statsItems.value[1].value = overview.commentsCount7Days
      statsItems.value[1].trend = overview.commentsTrend

      statsItems.value[2].value = overview.reactionsCount7Days
      statsItems.value[2].trend = overview.reactionsTrend

      // Wykres
      chartSeries.value[0].data = overview.activeMembersChart
      // Aktualizacja yaxis max dla dopasowania skali do danych wykresu
      const maxValue = Math.max(...overview.activeMembersChart)
      chartOptions.value.yaxis.max = maxValue > 0 ? maxValue * 2 : 2
      chartOptions.value.xaxis.categories = overview.chartCategories
    }
  }
  isLoading.value = false
}

onMounted(() => {
  loadOverview()
})
</script>

<template>
  <!-- Stan ładowania -->
  <div v-if="isLoading" class="min-h-screen bg-theme-bg dark:bg-[#18191a] text-theme-text font-sans flex flex-col items-center justify-center py-12">
    <div class="w-8 h-8 border-4 border-[#2d88ff] border-t-transparent rounded-full animate-spin"></div>
    <p class="mt-4 text-[#b0b3b8] text-sm font-semibold">Ładowanie statystyk...</p>
  </div>

  <!-- Brak dostępu (Nie-Admin) -->
  <div v-else-if="!hasAccess" class="min-h-screen bg-theme-bg dark:bg-[#18191a] text-theme-text font-sans flex flex-col items-center justify-center p-8 text-center selection:bg-blue-600">
    <div class="w-20 h-20 rounded-full bg-red-500/10 flex items-center justify-center text-red-500 mb-6 border border-red-500/25">
      <svg xmlns="http://www.w3.org/2000/svg" class="h-10 w-10" fill="none" viewBox="0 0 24 24" stroke="currentColor">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
      </svg>
    </div>
    <h2 class="text-2xl font-bold mb-2">Brak dostępu</h2>
    <p class="text-sm text-theme-text-secondary max-w-md mb-6 leading-relaxed">
      Tylko administratorzy tej grupy mają uprawnienia do przeglądania podsumowania statystyk oraz panelu zarządzania.
    </p>
    <NuxtLink :to="`/groups/${groupId}`" class="bg-[#2d88ff] hover:bg-[#1a73e8] text-white px-6 py-2.5 rounded-lg text-sm font-semibold transition-colors shadow">
      Wróć do grupy
    </NuxtLink>
  </div>

  <!-- Główny Widok Overview -->
  <div v-else class="min-h-screen bg-theme-bg dark:bg-[#18191a] text-theme-text font-sans p-4 sm:p-6 lg:p-8 flex justify-center selection:bg-blue-600 pb-24">
    <div class="w-full max-w-[950px] flex flex-col gap-4">

      <!-- Sekcja 1: Do sprawdzenia -->
      <div class="bg-theme-bg-secondary rounded-xl p-5 shadow-sm border border-theme-border">
        <div class="mb-4">
          <h2 class="text-[20px] font-bold text-theme-text">Do sprawdzenia</h2>
          <p class="text-[15px] text-theme-text-secondary mt-0.5">
            {{ reviewItems.reduce((acc, item) => acc + item.count, 0) }} aktualizacji do sprawdzenia
          </p>
        </div>

        <div class="grid grid-cols-1 md:grid-cols-2 gap-x-8 gap-y-1">
          <button
            v-for="item in reviewItems"
            :key="item.id"
            class="flex items-center justify-between py-2 hover:bg-theme-hover rounded-xl transition-colors px-2 -mx-2 group cursor-pointer text-left"
          >
            <!-- Lewa strona: Ikona i Teksty -->
            <div class="flex items-center gap-3.5">
              <div class="w-10 h-10 rounded-full bg-theme-bg-tertiary flex items-center justify-center shrink-0 text-theme-text">
                <component :is="item.icon" :size="20" />
              </div>
              <div class="flex flex-col">
                <span class="text-[15px] font-semibold text-theme-text leading-snug">
                  {{ item.title }}
                </span>
                <span class="text-[13px] text-theme-text-secondary mt-0.5">
                  {{ item.subtitle }}
                </span>
              </div>
            </div>

            <!-- Prawa strona: Licznik, Kropka i Strzałka -->
            <div class="flex items-center gap-3 shrink-0 pl-4">
              <div v-if="item.hasDot" class="w-2 h-2 rounded-full bg-[#1877f2]"></div>
              <span class="text-[17px] font-bold text-theme-text">{{ item.count }}</span>
              <ChevronRightIcon :size="24" class="text-theme-text-secondary group-hover:text-theme-text dark:group-hover:text-[#e4e6eb] transition-colors" />
            </div>
          </button>
        </div>
      </div>

      <!-- Dolny układ (2 kolumny) -->
      <div class="grid grid-cols-1 md:grid-cols-2 gap-4">

        <!-- Sekcja 2A: Podsumowanie statystyk -->
        <div class="bg-theme-bg-secondary rounded-xl p-5 shadow-sm border border-theme-border flex flex-col h-full">
          <div class="mb-4">
            <h2 class="text-[17px] font-bold text-theme-text">Podsumowanie statystyk</h2>
            <p class="text-[14px] text-theme-text-secondary mt-0.5">W ciągu ostatnich 7 dni</p>
          </div>

          <div class="flex flex-col gap-2.5 flex-1">
            <div
              v-for="stat in statsItems"
              :key="stat.id"
              class="bg-theme-bg dark:bg-[#303031] rounded-xl p-4 flex items-center justify-between"
            >
              <div class="flex items-center gap-3">
                <component :is="stat.icon" :size="20" class="text-theme-text" />
                <span class="text-[15px] font-semibold text-theme-text">{{ stat.label }}</span>
              </div>
              <div class="flex items-center gap-2">
                <span class="text-[17px] font-bold text-theme-text">{{ stat.value }}</span>
                <div class="flex items-center text-[#8c939d] dark:text-[#b0b3b8]">
                  <ArrowTopRightIcon :size="16" />
                  <span class="text-[14px] ml-0.5 font-semibold">{{ stat.trend }}</span>
                </div>
              </div>
            </div>
          </div>

          <button class="w-full mt-4 bg-theme-bg-tertiary hover:bg-theme-hover-strong dark:bg-[#3a3b3c] dark:hover:bg-[#4e4f50] transition-colors text-theme-text font-semibold text-[15px] py-2.5 rounded-lg cursor-pointer">
            Zobacz statystyki aktywności
          </button>
        </div>

        <!-- Sekcja 2B: Członkowie aktywni co tydzień (Wykres ApexCharts) -->
        <div class="bg-theme-bg-secondary rounded-xl p-5 shadow-sm border border-theme-border flex flex-col h-full">
          <div class="mb-2">
            <div class="flex items-center gap-1.5">
              <h2 class="text-[17px] font-bold text-theme-text">Członkowie aktywni co tydzień</h2>
              <InformationIcon :size="16" class="text-theme-text-secondary cursor-pointer hover:text-theme-text transition-colors" />
            </div>
            <div class="flex items-center gap-1.5 mt-0.5 text-theme-text-secondary">
              <span class="text-[14px]">
                {{ chartSeries[0].data.reduce((acc, v) => Math.max(acc, v), 0) }} w ciągu ostatnich 7 dni
              </span>
              <div class="flex items-center text-[#8c939d]">
                <ArrowTopRightIcon :size="14" />
                <span class="text-[13px] ml-0.5 font-semibold">0%</span>
              </div>
            </div>
          </div>

          <!-- Integracja wykresu ApexCharts -->
          <div class="flex-1 w-full min-h-[180px] -ml-2">
            <VueApexCharts
              type="line"
              height="100%"
              :options="chartOptions"
              :series="chartSeries"
            />
          </div>

          <button class="w-full mt-2 bg-theme-bg-tertiary hover:bg-theme-hover-strong dark:bg-[#3a3b3c] dark:hover:bg-[#4e4f50] transition-colors text-theme-text font-semibold text-[15px] py-2.5 rounded-lg cursor-pointer">
            Zobacz statystyki aktywności
          </button>
        </div>

      </div>

    </div>
  </div>
</template>

<style>
/* Nadpisywanie domyślnych stylów apexcharts pod layout */
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
  color: #050505 !important;
}
.apexcharts-text tspan {
  font-family: inherit;
}
</style>
