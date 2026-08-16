<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import VueApexCharts from 'vue3-apexcharts'
import DateInput from './DateInput.vue'

import EyeOutlineIcon from 'vue-material-design-icons/EyeOutline.vue'
import CommentProcessingOutlineIcon from 'vue-material-design-icons/CommentProcessingOutline.vue'
import AccountMultipleOutlineIcon from 'vue-material-design-icons/AccountMultipleOutline.vue'
import InformationIcon from 'vue-material-design-icons/Information.vue'
import 'vue-material-design-icons/styles.css'

const monthsPL = ['sty', 'lut', 'mar', 'kwi', 'maj', 'cze', 'lip', 'sie', 'wrz', 'paź', 'lis', 'gru']

const selectedRange = ref('last_28_days')
const selectedCard = ref('activity')

// --- STAN DLA AKTYWNEGO TOOLTIPA (KLIKNIĘCIE) ---
const activeTooltipId = ref<string | null>(null)

const toggleTooltip = (id: string) => {
  if (activeTooltipId.value === id) {
    activeTooltipId.value = null
  } else {
    activeTooltipId.value = id
  }
}

const handleClickOutside = (event: MouseEvent) => {
  const target = event.target as HTMLElement
  if (!target.closest('.tooltip-container')) {
    activeTooltipId.value = null
  }
}

onMounted(() => document.addEventListener('click', handleClickOutside))
onUnmounted(() => document.removeEventListener('click', handleClickOutside))

import { usePageAnalytics } from '@/composables/analytics/usePageAnalytics'

const { insights } = usePageAnalytics()

// --- STRUKTURA KAFELKÓW ORAZ TEKSTÓW TOOLTIPÓW ---
const statsCards = computed(() => [
  {
    id: 'views',
    title: 'Wyświetlenia',
    value: String(insights.value?.totalViews ?? 0),
    change: `${insights.value?.viewsGrowthPercent ?? 0}%`,
    trend: (insights.value?.viewsGrowthPercent ?? 0) >= 0 ? 'up' : 'down',
    icon: EyeOutlineIcon,
    tooltipText: 'Liczba odtworzeń lub wyświetleń Twoich materiałów, takich jak rolki, posty, relacje i reklamy. ',
    tooltipLink: 'Dowiedz się więcej'
  },
  {
    id: 'activity',
    title: 'Aktywność',
    value: String((insights.value?.totalReactions ?? 0) + (insights.value?.totalComments ?? 0) + (insights.value?.totalShares ?? 0)),
    change: '100%',
    trend: 'up',
    icon: CommentProcessingOutlineIcon,
    tooltipText: 'Liczba reakcji, kliknięć, komentarzy, udostępnień i zapisań dotyczących Twoich postów.',
    tooltipLink: null
  },
  {
    id: 'followers',
    title: 'Liczba obserwatorów netto',
    value: String(insights.value?.netFollowers ?? 0),
    change: `${insights.value?.followersGrowthPercent ?? 0}%`,
    trend: (insights.value?.followersGrowthPercent ?? 0) >= 0 ? 'up' : 'neutral',
    icon: AccountMultipleOutlineIcon,
    tooltipText: 'Liczba nowych obserwatorów pomniejszona o liczbę osób, które zrezygnowały z obserwowania w wybranym okresie.',
    tooltipLink: null
  }
])

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

const categories = computed(() => {
  const timeline = insights.value?.timeline
  if (timeline && timeline.length > 0) {
    return timeline.map(t => {
      const parts = t.date.split('-')
      if (parts.length === 3) {
        const mIdx = parseInt(parts[1], 10) - 1
        return `${parseInt(parts[2], 10)} ${monthsPL[mIdx] || ''}`
      }
      return t.date
    })
  }

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

const rangeText = computed(() => {
  const cats = categories.value
  if (cats.length === 0) return ''
  const start = cats[0]
  const end = cats[cats.length - 1]

  if (selectedRange.value === 'today') return `Dziś: ${start}`
  if (selectedRange.value === 'this_year') return `W tym roku: ${start} – ${end}`
  return `Ostatnie ${daysCount.value} dni: ${start} – ${end}`
})

const chartSeries = computed(() => {
  const activeCard = statsCards.value.find(c => c.id === selectedCard.value)
  const activeCardName = activeCard?.title || 'Wartość'
  const timeline = insights.value?.timeline

  if (timeline && timeline.length > 0) {
    const dataPoints = timeline.map(t => {
      if (selectedCard.value === 'views') return t.views || 0
      if (selectedCard.value === 'activity') return (t.reactions || 0) + (t.comments || 0)
      return t.followers || 0
    })
    return [{ name: activeCardName, data: dataPoints }]
  }

  const totalDays = categories.value.length;
  const maxVal = selectedCard.value === 'views' ? 20 : selectedCard.value === 'activity' ? 5 : 2;
  const randomData = Array.from({ length: totalDays }, () => Math.floor(Math.random() * maxVal));

  return [{
    name: activeCardName,
    data: randomData
  }]
})

const chartOptions = computed(() => ({
  chart: {
    type: 'area',
    toolbar: { show: false },
    zoom: { enabled: false },
    fontFamily: 'inherit',
    parentHeightOffset: 0,
    animations: { enabled: true }
  },
  colors: ['#1877F2'],
  fill: { type: 'solid', opacity: 0.1 },
  stroke: { curve: 'straight', width: 2 },
  dataLabels: { enabled: false },
  markers: { size: 0, colors: ['#1877F2'], strokeColors: '#fff', strokeWidth: 2, hover: { size: 5.5, sizeOffset: 0 } },
  xaxis: {
    categories: categories.value,
    tickPlacement: 'on',
    axisBorder: { show: true, color: '#E5E7EB', height: 1, offsetY: 0 },
    axisTicks: { show: false },
    labels: {
      formatter: function (value: string) {
        const cats = categories.value
        const total = cats.length
        if (total <= 14) return value;
        const idx = cats.indexOf(value)
        const today = new Date()
        const targetDate = new Date(today)
        targetDate.setDate(today.getDate() - 3)
        const targetLabel = `${targetDate.getDate()} ${monthsPL[targetDate.getMonth()]}`
        const targetIdx = cats.indexOf(targetLabel)
        if (idx !== -1 && (targetIdx - idx) % 5 === 0 && idx <= targetIdx) return value;
        return ''
      },
      style: { colors: '#65676B', fontSize: '13px', fontWeight: 500 },
      offsetY: 5
    },
    tooltip: { enabled: false },
    crosshairs: { show: true, position: 'back', stroke: { color: '#8A8D91', width: 1, dashArray: 4 } }
  },
  yaxis: {
    show: true,
    labels: { style: { colors: '#65676B', fontSize: '13px' }, formatter: (val: number) => Math.round(val).toString(), offsetX: -10 }
  },
  grid: {
    show: true, borderColor: '#E5E7EB', xaxis: { lines: { show: false } }, yaxis: { lines: { show: true } }, padding: { top: 0, right: 0, bottom: 0, left: 10 }
  },
  tooltip: {
    enabled: true,
    custom: function({ series, seriesIndex, dataPointIndex }: any) {
      const date = categories.value[dataPointIndex]
      const value = series[seriesIndex][dataPointIndex]
      const name = statsCards.find(c => c.id === selectedCard.value)?.title
      return `
        <div class="bg-white rounded-lg shadow-[0_2px_8px_rgba(0,0,0,0.12)] p-3 min-w-[130px] text-left">
          <div class="text-[14px] text-[#050505] font-medium leading-tight mb-1">${name}</div>
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
        <h2 class="text-[20px] font-bold text-[#050505] leading-tight">Statystyki</h2>
        <p class="text-[13px] text-[#65676B] mt-1">{{ rangeText }}</p>
      </div>
      <div class="flex items-center gap-3">
        <DateInput v-model="selectedRange" />
        <a href="#" class="text-[#1877F2] text-[15px] font-semibold hover:underline">Wyświetl wszystko</a>
      </div>
    </div>

    <!-- KAFELKI -->
    <div class="grid grid-cols-3 gap-3 mb-3">
      <div
        v-for="card in statsCards"
        :key="card.id"
        @click="selectedCard = card.id"
        :class="[
          'rounded-xl py-2.5 px-3.5 cursor-pointer flex flex-col transition-all border-[2px] bg-white relative items-start',
          selectedCard === card.id
            ? 'border-[#1877F2] shadow-[0_0_0_1px_rgba(24,119,242,0.1)]'
            : 'border-[#E4E6EB] hover:bg-[#F0F2F5]'
        ]"
      >
        <!-- Ikona wyrównana do lewej -->
        <div class="mb-1.5 flex justify-start w-full">
          <component :is="card.icon" :size="22" class="text-[#050505]" />
        </div>

        <!-- Wartość i Zmiana -->
        <div class="flex items-end gap-1.5 mb-1">
          <span class="font-bold text-[17px] leading-none text-[#050505]">{{ card.value }}</span>

          <span v-if="card.trend === 'up'" class="flex items-center text-[#31A24C] font-semibold text-[15px] leading-none pb-[1px]">
            <span class="mr-0.5 text-[16px]">↑</span> {{ card.change }}
          </span>
          <span v-else-if="card.trend === 'neutral'" class="text-[#65676B] text-[15px] font-normal leading-none pb-[1px]">
            – {{ card.change }}
          </span>
        </div>

        <!-- Tytuł i ikona info -->
        <div class="text-[13px] text-[#050505] flex items-center gap-1 mt-auto">
          {{ card.title }}

          <div class="relative flex items-center tooltip-container">
            <InformationIcon
              :size="16"
              @click.stop="toggleTooltip(card.id)"
              class="text-[#65676B] cursor-pointer hover:text-[#050505] transition-colors"
            />

            <div
              v-if="activeTooltipId === card.id"
              class="absolute top-full left-1/2 -translate-x-1/2 mt-2 w-[260px] bg-white rounded-xl shadow-[0_4px_20px_rgba(0,0,0,0.15)] p-4 z-50 text-left border border-[#CED0D4] cursor-default"
              @click.stop
            >
              <div class="absolute -top-[6px] left-1/2 -translate-x-1/2 w-3 h-3 bg-white border-t border-l border-[#CED0D4] rotate-45"></div>

              <div class="relative z-10 bg-white">
                <h4 class="font-bold text-[15px] text-[#050505] mb-1">{{ card.title }}</h4>
                <p class="text-[13px] text-[#050505] leading-snug">
                  {{ card.tooltipText }}
                  <a v-if="card.tooltipLink" href="#" class="text-[#1877F2] font-semibold hover:underline">
                    {{ card.tooltipLink }}
                  </a>
                </p>
              </div>

            </div>
          </div>

        </div>
      </div>
    </div>

    <!-- APEXCHARTS -->
    <ClientOnly>
      <div class="w-full relative mt-[-10px] custom-chart">
        <VueApexCharts
          :key="`${selectedRange}-${selectedCard}`"
          type="area"
          height="150"
          :options="chartOptions"
          :series="chartSeries"
        />
      </div>
    </ClientOnly>
  </div>
</template>

<style scoped>
:deep(.custom-chart .apexcharts-tooltip) { background: transparent !important; border: none !important; box-shadow: none !important; overflow: visible !important; }
:deep(.custom-chart .apexcharts-tooltip-title) { display: none !important; }
:deep(.custom-chart .apexcharts-xaxistooltip) { display: none !important; }
:deep(.custom-chart .apexcharts-active) { padding-bottom: 5px !important; }


</style>
