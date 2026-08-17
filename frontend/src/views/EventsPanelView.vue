<template>
  <div class="flex h-screen bg-theme-bg   text-theme-text overflow-hidden">
    <EventsSidebar not />

    <div class="flex-1 flex flex-col items-center overflow-y-auto pt-[64px] pb-10 bg-theme-bg">
      <main class="w-[calc(100%-2rem)] md:w-[calc(100%-5rem)] max-w-[1900px] h-fit">
        <NewForYou />
        <EventsList />
        <div
          class="p-6 bg-theme-bg-secondary my-4 mx-auto rounded-2xl shadow-sm relative border border-theme-border"
        >
          <header class="mb-8 relative">
            <h2 class="text-[24px] font-bold mb-6">Odkryj wydarzenia</h2>

            <div class="flex flex-wrap gap-2 items-center">
              <button
                class="flex items-center gap-2 px-3 py-2 bg-theme-bg-subtle hover:bg-theme-hover rounded-lg font-semibold text-[15px] transition-colors text-theme-text border border-transparent"
              >
                <MapMarkerIcon :size="18" />
                <span>Moja lokalizacja</span>
                <ChevronDownIcon :size="18" />
              </button>

              <div class="relative">
                <button
                  @click="toggleMenu"
                  :class="[
                    'flex items-center gap-2 px-4 py-2 rounded-full font-semibold text-[15px] transition-colors border',
                    selectedDate !== 'Dowolna data'
                      ? 'bg-theme-primary text-white border-transparent'
                      : 'bg-white hover:bg-gray-100 text-black border-gray-300',
                  ]"
                >
                  <CalendarMonthIcon :size="18" />
                  <span>{{ selectedDate }}</span>
                  <CloseIcon
                    v-if="selectedDate !== 'Dowolna data'"
                    @click.stop="resetDate"
                    :size="18"
                    class="hover:bg-white/20 rounded-full"
                  />
                  <ChevronDownIcon v-else :size="18" />
                </button>

                <div
                  v-if="isDateMenuOpen"
                  class="absolute top-full left-0 mt-2 w-[340px] bg-white rounded-2xl shadow-[0_8px_30px_rgb(0,0,0,0.12)] border border-gray-100 overflow-hidden z-50"
                >
                  <div v-if="currentMenuView === 'list'" class="py-1">
                    <div v-for="(option, index) in dateOptions" :key="option">
                      <div
                        @click="selectQuickDate(option)"
                        class="flex items-center justify-between px-5 py-4 hover:bg-gray-50 cursor-pointer transition-colors"
                      >
                        <span class="text-[17px] font-medium text-gray-900">{{ option }}</span>

                        <div
                          class="w-6 h-6 rounded-full border-2 flex items-center justify-center transition-all"
                          :class="selectedDate === option ? 'border-black' : 'border-gray-400'"
                        >
                          <div
                            v-if="selectedDate === option"
                            class="w-3.5 h-3.5 rounded-full bg-black"
                          ></div>
                        </div>
                      </div>
                      <div v-if="index === 0" class="mx-5 border-b border-gray-200 mb-1"></div>
                    </div>

                    <div
                      @click="currentMenuView = 'calendar'"
                      class="flex items-center justify-between px-5 py-4 hover:bg-gray-50 cursor-pointer transition-colors mt-1"
                    >
                      <span class="text-[17px] font-medium text-gray-900"
                        >Niestandardowy zakres dat</span
                      >
                      <ChevronRightIcon :size="24" class="text-gray-500" />
                    </div>
                  </div>

                  <div v-else class="flex flex-col bg-white min-h-[420px]">
                    <div class="flex items-center px-2 py-3 border-b border-gray-200">
                      <button
                        @click="currentMenuView = 'list'"
                        class="p-2 hover:bg-gray-100 rounded-full transition text-black"
                      >
                        <ArrowLeftIcon :size="24" />
                      </button>
                      <span class="ml-2 font-semibold text-[19px] text-gray-900"
                        >Niestandardowy zakres dat</span
                      >
                    </div>

                    <div class="p-4 custom-calendar">
                      <VDatePicker
                        v-model="range"
                        is-range
                        borderless
                        transparent
                        locale="pl"
                        :disabled-dates="disabledDates"
                        :trim-weeks="false"
                        :first-day-of-week="2"
                        title-position="center"
                      />

                      <div class="mt-4 px-2">
                        <button
                          @click="applyCustomDate"
                          :disabled="!range"
                          class="w-full font-bold py-3.5 rounded-xl text-[16px] transition-all"
                          :class="[
                            range
                              ? 'bg-gray-200 text-gray-800 hover:bg-gray-300'
                              : 'bg-[#E8EAED] text-[#9AA0A6] cursor-not-allowed',
                          ]"
                        >
                          Zastosuj
                        </button>
                      </div>
                    </div>
                  </div>
                </div>
              </div>

              <button
                class="px-4 py-2 bg-theme-bg-subtle hover:bg-theme-hover text-theme-text rounded-full font-semibold text-[15px] transition-colors"
              >
                Najpopularniejsze
              </button>
              <button
                class="px-4 py-2 bg-theme-primary-subtle text-theme-primary rounded-full font-semibold text-[15px] transition-colors"
              >
                Znajomi
              </button>
            </div>
          </header>
          <div
            class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 2xl:grid-cols-5 3xl:grid-cols-6 gap-4"
          >
            <YourEventItem v-for="eventItem in events" :key="eventItem.id" :event="eventItem" />
          </div>
        </div>
      </main>
    </div>
  </div>

  <BaseModal v-if="isOpen" @close="isOpen = false" title="Utwórz wydarzenie">
    <CreateEventModal @close="isOpen = false" class="w-[500px]" />
  </BaseModal>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { DatePicker as VDatePicker } from 'v-calendar'
import 'v-calendar/dist/style.css'
import { useEventsStore } from '@/stores/events'
import EventsSidebar from '@/components/events/EventsSidebar.vue'
import CreateEventModal from '@/components/events/CreateEventModal.vue'

/* Ikony */
import MapMarkerIcon from 'vue-material-design-icons/MapMarker.vue'
import CalendarMonthIcon from 'vue-material-design-icons/CalendarMonth.vue'
import ChevronDownIcon from 'vue-material-design-icons/ChevronDown.vue'
import ChevronRightIcon from 'vue-material-design-icons/ChevronRight.vue'
import ArrowLeftIcon from 'vue-material-design-icons/ArrowLeft.vue'
import CloseIcon from 'vue-material-design-icons/Close.vue'

import EventsList from '@/components/events/EventsList.vue'
import NewForYou from '@/components/events/NewForYou.vue'

const isDateMenuOpen = ref(false)
const currentMenuView = ref('list')
const selectedDate = ref('Dowolna data')
const range = ref<{ start: Date; end: Date } | null>(null)
const isOpen = ref(false)

const dateOptions = [
  'Dowolna data',
  'Dzisiaj',
  'Jutro',
  'W ten weekend',
  'W tym tygodniu',
  'W przyszłym tygodniu',
  'W tym miesiącu',
]

const toggleMenu = () => {
  isDateMenuOpen.value = !isDateMenuOpen.value
  if (!isDateMenuOpen.value) currentMenuView.value = 'list'
}

const selectQuickDate = (val: string) => {
  selectedDate.value = val
  isDateMenuOpen.value = false
}

const applyCustomDate = () => {
  if (range.value) {
    const start = range.value.start.toLocaleDateString('pl-PL', { day: 'numeric', month: 'short' })
    const end = range.value.end.toLocaleDateString('pl-PL', { day: 'numeric', month: 'short' })
    selectedDate.value = `${start} – ${end}`
    isDateMenuOpen.value = false
    currentMenuView.value = 'list'
  }
}

const resetDate = () => {
  selectedDate.value = 'Dowolna data'
  range.value = null
}

const disabledDates = ref([
  { start: null, end: new Date(new Date().setDate(new Date().getDate() - 1)) },
])
import YourEventItem from '@/components/events/YourEventItem.vue'

const eventsStore = useEventsStore()
const router = useRouter()
const events = computed(() => eventsStore.events)

const navigateToEvent = (eventId: string) => {
  router.push(`/event/${eventId}`)
}
</script>

<style scoped>
/* Kalendarz VCalendar customizacja bez @apply */
.custom-calendar :deep(.vc-container) {
  border: none;
  background: transparent;
}

.custom-calendar :deep(.vc-title) {
  color: var(--color-text); /* Użycie zmiennej CSS zamiast JS */
  font-weight: 700;
}

.custom-calendar :deep(.vc-day-content) {
  color: var(--color-text);
}

/* Wymuszenie koloru dla wszystkich ikon w tym komponencie */
:deep(svg) {
  fill: currentColor !important;
}

/* Poprawka dla zachowania proporcji i animacji zdjęć */
.aspect-video {
  position: relative;
  overflow: hidden;
}

button:active {
  transform: scale(0.97);
}

.custom-calendar :deep(.vc-day-content[aria-disabled='false'] .vc-day-layer) {
  border-radius: 6px !important;
}

.custom-calendar :deep(.vc-day-content[aria-disabled='true']) {
  color: #000000 !important;
  cursor: not-allowed !important;
  background-color: transparent !important;
  /* Ekstremalne opacity dla efektu "wyłączenia" */
  opacity: 0.1 !important;
  pointer-events: none !important;
  user-select: none !important;
}
/* --- DNI SPOZA MIESIĄCA (Szare kafelki z widocznymi datami) --- */
.custom-calendar :deep(.vc-day.is-not-in-month) {
  background-color: #f1f3f4 !important; /* Szare tło */

  display: flex !important;
  align-items: center;
  justify-content: center;
  opacity: 1 !important;
  visibility: visible !important;
}

/* WYMUSZENIE WIDOCZNOŚCI CYFR W SZARYCH POLACH */
.custom-calendar :deep(.vc-day.is-not-in-month .vc-day-content) {
  color: #9aa0a6 !important; /* Jasnoszary kolor cyfry */
  display: flex !important;
  opacity: 1 !important;
  font-weight: 400;
}
.custom-calendar :deep(.vc-highlight) {
  background-color: #0866ff !important; /* Delikatny efekt hover */
  border: 1px solid white !important;
}

.custom-calendar :deep(.is-today .vc-day-content) {
  font-weight: 800;
}
/* --- DNI ZABLOKOWANE (Disabled) --- */
.custom-calendar :deep(.vc-day.is-disabled) {
  cursor: not-allowed !important;
}

.custom-calendar :deep(.vc-day.is-disabled .vc-day-content) {
  color: #dadce0 !important; /* Bardzo jasny kolor dla zablokowanych */
  opacity: 0.5 !important;
  pointer-events: none !important;
}

/* --- DNI TYGODNIA --- */
.custom-calendar :deep(.vc-weekdays) {
  padding: 12px 0;
  color: #70757a;
  font-weight: 500;
  font-size: 13px;
}
.custom-calendar :deep(.vc-week) {
  margin-bottom: 3px;
}
.custom-calendar :deep(.vc-highlight-bg-outline) {
  border: none !important;
}
.custom-calendar :deep(.vc-highlight) {
  background-color: #e7f3ff !important; /* Bardzo jasny niebieski */
  width: 100% !important;
  height: 34px !important;
  border-radius: 6px !important;
}
.custom-calendar :deep(.vc-day-content:focus) {
  outline: none !important;
  box-shadow: none !important;
}

/* Dodatkowe zabezpieczenie dla przycisków i interaktywnych elementów biblioteki */
.custom-calendar :deep(.vc-focusable) {
  outline: none !important;
  box-shadow: none !important;
}

/* Jeśli obwódka pojawia się na kontenerze zaznaczenia */
.custom-calendar :deep(.vc-highlight:focus) {
  outline: none !important;
}
.custom-calendar :deep(.vc-day-content.vc-highlight-content-solid) {
  background-color: #0866ff !important; /* Facebook Blue */
  color: #ffffff !important;
  border-radius: 6px !important; /* Zaokrąglony kwadrat jak na zdjęciu */
  z-index: 2;
  width: 100% !important;
  height: 100% !important;
  font-weight: 700 !important;
}

/* --- NAGŁÓWEK --- */
.custom-calendar :deep(.vc-title) {
  font-size: 18px;
  font-weight: 700;
  color: #202124;
  text-transform: lowercase;
}
</style>
