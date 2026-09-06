<script setup lang="ts">
import { ref, computed } from 'vue'
import VueApexCharts from 'vue3-apexcharts'
import DateInput from './DateInput.vue'

const monthsPL = ['sty', 'lut', 'mar', 'kwi', 'maj', 'cze', 'lip', 'sie', 'wrz', 'paź', 'lis', 'gru']

// 1. GŁÓWNY STAN ZARZĄDZAJĄCY ZAKRESEM DAT
const selectedRange = ref('last_28_days')

// 2. Mapowanie wybranego klucza na liczbę dni
const daysCount = computed(() => {
  if (selectedRange.value === 'today') return 1;
  if (selectedRange.value === 'this_year') {
    const today = new Date();
    const start = new Date(today.getFullYear(), 0, 1);
    return Math.ceil((today.getTime() - start.getTime()) / (1000 * 60 * 60 * 24)) - 1;
  }

  switch (selectedRange.value) {
    case 'last_7_days': return 7;
    case 'last_14_days': return 14;
    case 'last_28_days': return 28;
    case 'last_60_days': return 60;
    case 'last_90_days': return 90;
    default: return 28;
  }
})

// 3. Generowanie kategorii (osi X)
const categories = computed(() => {
  const dates = []
  const today = new Date()
  const days = daysCount.value || 28

  for (let i = days - 1; i >= 0; i--) {
    const d = new Date(today)
    d.setDate(today.getDate() - i - 1)
    dates.push(`${d.getDate()} ${monthsPL[d.getMonth()]}`)
  }
  return dates
})

// 4. Tekst nagłówka daty (np. "Ostatnie 14 dni: 28 lip - 10 sie")
const rangeText = computed(() => {
  const cats = categories.value
  if (cats.length === 0) return ''
  const start = cats[0]
  const end = cats[cats.length - 1]

  if (selectedRange.value === 'today') return `Dziś: ${start}`
  if (selectedRange.value === 'this_year') return `W tym roku: ${start} – ${end}`
  return `Ostatnie ${daysCount.value} dni: ${start} – ${end}`
})

// 5. Losowe generowanie danych (żeby widzieć zmianę wykresu na żywo)
const chartSeries = computed(() => {
  const totalDays = categories.value.length;
  // Tworzymy tablicę z losowymi wartościami od 0 do 200
  const randomData = Array.from({ length: totalDays }, () => Math.floor(Math.random() * 200));

  return [{
    name: 'Wyświetlenia',
    data: randomData
  }]
})

// 6. Opcje wykresu
const chartOptions = computed(() => ({
  chart: {
    type: 'line',
    toolbar: { show: false },
    zoom: { enabled: false },
    fontFamily: 'inherit',
    parentHeightOffset: 0,
    animations: { enabled: true }
  },
  colors: ['#1877F2'],
  stroke: {
    curve: 'straight',
    width: 2
  },
  dataLabels: { enabled: false },
  markers: {
    size: 0,
    colors: ['#1877F2'],
    strokeColors: '#fff',
    strokeWidth: 2,
    hover: { size: 5.5, sizeOffset: 0 }
  },
  xaxis: {
    categories: categories.value,
    tickPlacement: 'on',
    axisBorder: {
      show: true,
      color: '#1877F2',
      height: 2,
      offsetY: -0.5
    },
    axisTicks: { show: false },
    labels: {
      formatter: function (value: string) {
        const cats = categories.value
        const idx = cats.indexOf(value)
        const total = cats.length

        // Zabezpieczenie: Przy małej ilości danych pokazuj wszystko
        if (total <= 14) return value;

        const today = new Date()
        const targetDate = new Date(today)
        targetDate.setDate(today.getDate() - 3)
        const targetLabel = `${targetDate.getDate()} ${monthsPL[targetDate.getMonth()]}`
        const targetIdx = cats.indexOf(targetLabel)

        if (idx !== -1 && (targetIdx - idx) % 5 === 0 && idx <= targetIdx) {
          return value
        }
        return ''
      },
      style: { colors: '#65676B', fontSize: '13px', fontWeight: 500 },
      offsetY: 5
    },
    tooltip: { enabled: false },
    crosshairs: {
      show: true,
      position: 'back',
      stroke: { color: '#8A8D91', width: 1, dashArray: 4 }
    }
  },
  yaxis: { show: false }, // Wyłączyłem max: 10, by wykres sam się skalował do losowych liczb
  grid: {
    show: false,
    padding: { top: 0, right: 15, bottom: 0, left: 15 }
  },
  tooltip: {
    enabled: true,
    custom: function({ series, seriesIndex, dataPointIndex }: any) {
      const date = categories.value[dataPointIndex]
      const value = series[seriesIndex][dataPointIndex]
      return `
        <div class="bg-white rounded-lg shadow-[0_2px_8px_rgba(0,0,0,0.12)] p-3 min-w-[130px] text-left">
          <div class="text-[14px] text-[#050505] font-medium leading-tight mb-1">{{ $t('dashboard.wyswietlenia') }}</div>
          <div class="text-[17px] font-bold text-[#050505] leading-tight mb-1">${value}</div>
          <div class="text-[13px] text-[#65676B] leading-tight">${date}</div>
        </div>
      `
    }
  }
}))
</script>

<template>
  <div class="bg-white rounded-[8px] shadow-sm p-4 flex flex-col">
    <div class="flex justify-between items-start mb-4">
      <div>
        <h2 class="text-[20px] font-bold text-[#050505] leading-tight">{{ $t('createLive.stats') }}</h2>
        <!-- Wyświetlanie poprawnego przedziału dat nad wykresem -->
        <p class="text-[13px] text-[#65676B] mt-1">{{ rangeText }}</p>
      </div>
      <div class="flex items-center gap-3">
        <!-- Pewne dwukierunkowe przypisanie zamiast cukru syntaktycznego v-model -->
        <DateInput
          :modelValue="selectedRange"
          @update:modelValue="selectedRange = $event"
        />
        <a href="#" class="text-[#1877F2] text-[15px] font-semibold hover:underline">{{ $t('notifications_page.viewAll') }}</a>
      </div>
    </div>

    <!-- Kafelki ze statystykami (Bez zmian względem oryginału) -->
    <div class="grid grid-cols-3 gap-3 mb-6">
      <div class="border-[2px] border-[#1877F2] rounded-lg p-3 cursor-pointer bg-[#F7F9FD] flex flex-col gap-1">
        <svg viewBox="0 0 24 24" width="20" height="20" fill="#65676B" class="mb-1">
          <path d="M12 4.5C7 4.5 2.73 7.61 1 12c1.73 4.39 6 7.5 11 7.5s9.27-3.11 11-7.5c-1.73-4.39-6-7.5-11-7.5zM12 17c-2.76 0-5-2.24-5-5s2.24-5 5-5 5 2.24 5 5-2.24 5-5 5zm0-8c-1.66 0-3 1.34-3 3s1.34 3 3 3 3-1.34 3-3-1.34-3-3-3z"/>
        </svg>
        <div class="font-bold text-[20px] leading-tight text-[#050505]">0 <span class="text-[#65676B] text-[15px] font-normal">- 0%</span></div>
        <div class="text-[13px] text-[#65676B] flex items-center gap-1 font-medium">{{ $t('dashboard.wyswietlenia') }}</div>
      </div>

      <div class="rounded-lg p-3 cursor-pointer hover:bg-[#F0F2F5] transition-colors flex flex-col gap-1">
        <svg viewBox="0 0 24 24" width="20" height="20" fill="#65676B" class="mb-1">
          <path d="M21.99 4c0-1.1-.89-2-1.99-2H4c-1.1 0-2 .9-2 2v12c0 1.1.9 2 2 2h14l4 4-.01-18zM18 14H6v-2h12v2zm0-3H6V9h12v2zm0-3H6V6h12v2z"/>
        </svg>
        <div class="font-bold text-[20px] leading-tight text-[#050505]">0 <span class="text-[#65676B] text-[15px] font-normal">- 0%</span></div>
        <div class="text-[13px] text-[#65676B] flex items-center gap-1 font-medium">{{ $t('emojiPicker.categories.activity') }}</div>
      </div>

      <div class="rounded-lg p-3 cursor-pointer hover:bg-[#F0F2F5] transition-colors flex flex-col gap-1">
        <svg viewBox="0 0 24 24" width="20" height="20" fill="#65676B" class="mb-1">
          <path d="M16 11c1.66 0 2.99-1.34 2.99-3S17.66 5 16 5c-1.66 0-3 1.34-3 3s1.34 3 3 3zm-8 0c1.66 0 2.99-1.34 2.99-3S9.66 5 8 5C6.34 5 5 6.34 5 8s1.34 3 3 3zm0 2c-2.33 0-7 1.17-7 3.5V19h14v-2.5c0-2.33-4.67-3.5-7-3.5zm8 0c-.29 0-.62.02-.97.05 1.16.84 1.97 1.97 1.97 3.45V19h6v-2.5c0-2.33-4.67-3.5-7-3.5z"/>
        </svg>
        <div class="font-bold text-[20px] leading-tight text-[#050505]">0 <span class="text-[#65676B] text-[15px] font-normal">- 0%</span></div>
        <div class="text-[13px] text-[#65676B] flex items-center gap-1 font-medium">{{ $t('dashboard.liczbaObserwatorowNetto') }}</div>
      </div>
    </div>

    <!-- APEXCHARTS: Klucz key wymusza przeładowanie całkowite komponentu by uniknąć glitchy wizualnych -->
    <div class="w-full relative mt-[-10px] custom-chart">
      <VueApexCharts
        :key="selectedRange"
        type="line"
        height="150"
        :options="chartOptions"
        :series="chartSeries"
      />
    </div>
  </div>
</template>

<style scoped>
:deep(.custom-chart .apexcharts-tooltip) {
  background: transparent !important;
  border: none !important;
  box-shadow: none !important;
  overflow: visible !important;
}
:deep(.custom-chart .apexcharts-tooltip-title) {
  display: none !important;
}
:deep(.custom-chart .apexcharts-xaxistooltip) {
  display: none !important;
}
:deep(.custom-chart .apexcharts-active) {
  padding-bottom: 5px !important;
}
</style>
