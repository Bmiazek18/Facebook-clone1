<template>
  <div class="gc-wrapper">
    <!-- WŁASNY NAGŁÓWEK -->
    <header class="gc-header">
      <div class="gc-header-left">
        <h1 class="gc-title">
          {{ dateRangeText }}
          <svg class="dropdown-icon" viewBox="0 0 24 24"><path d="M7 10l5 5 5-5z"></path></svg>
        </h1>
        <div class="gc-subtitle">{{ $t('dashboard.czasSrodkowoeuropejskiLetniEurope') }}</div>
      </div>

      <div class="gc-header-right">
        <div class="gc-nav-group">
          <button class="gc-btn icon" @click="vuecal?.previous()">
            <svg viewBox="0 0 24 24"><path d="M15.41 16.59L10.83 12l4.58-4.59L14 6l-6 6 6 6 1.41-1.41z"></path></svg>
          </button>
          <button class="gc-btn text" @click="goToToday">{{ $t('dashboard.dzis') }}</button>
          <button class="gc-btn icon" @click="vuecal?.next()">
            <svg viewBox="0 0 24 24"><path d="M8.59 16.59L13.17 12 8.59 7.41 10 6l6 6-6 6-1.41-1.41z"></path></svg>
          </button>
        </div>

        <button class="gc-btn select">{{ $t('dashboard.tydzien') }}<svg class="dropdown-icon" viewBox="0 0 24 24"><path d="M7 10l5 5 5-5z"></path></svg>
        </button>

        <button class="gc-btn icon settings">
           <svg focusable="false" viewBox="0 0 24 24"><path d="M13.85 22.25h-3.7c-.74 0-1.36-.54-1.45-1.27l-.27-1.89c-.27-.14-.53-.29-.79-.46l-1.8.72c-.7.26-1.47-.03-1.81-.65L2.2 15.53c-.35-.66-.2-1.44.36-1.88l1.52-1.19c-.01-.15-.02-.3-.02-.46 0-.15.01-.3.02-.46l-1.52-1.19c-.56-.45-.71-1.23-.36-1.88l1.83-3.17c.34-.62 1.11-.9 1.81-.65l1.8.72c.26-.17.52-.32.79-.46l.27-1.89c.09-.73.71-1.27 1.45-1.27zm-1.85-5.25c2.48 0 4.5-2.02 4.5-4.5s-2.02-4.5-4.5-4.5-4.5 2.02-4.5 4.5 2.02 4.5 4.5 4.5z" fill="#5f6368"></path></svg>
        </button>
      </div>
    </header>

    <!-- KONTENER Z KALENDARZEM -->
    <div class="gc-card">
      <div class="gc-body">
        <vue-cal
          ref="vuecal"
          v-model:selected-date="selectedDate"
          active-view="week"
          :disable-views="['years', 'year', 'month', 'day']"
          :hide-title-bar="true"
          :time-from="0"
          :time-to="24 * 60"
          :time-step="60"
          :time-cell-height="60"
          @view-change="onViewChange"
        >
          <!-- USUNIĘCIE NAPISU "NO EVENTS" -->
          <template #no-event>
            <span></span>
          </template>

          <!-- NAGŁÓWEK DNI (DZISIAJ NA NIEBIESKO) -->
          <template #heading="{ heading }">
            <div
              class="gc-day-header"
              :class="{ 'is-today': checkIsToday(heading.date) }"
            >
              <span class="gc-day-name">{{ heading.day }}</span>
              <span class="gc-day-number">{{ heading.date.getDate() }}</span>
            </div>
          </template>

          <!-- GODZINY AM/PM -->
          <template #time-cell="{ hours }">
            <div class="gc-time-label">
              {{ formatHour(hours) }}
            </div>
          </template>

          <!-- POZYCJONOWANIE HOVER DLA GODZIN I LOGIKA MINIONYCH DAT -->
          <template #cell-content="{ cell }">
            <div class="gc-hourly-grid-overlay">
              <div
                v-for="hour in 24"
                :key="hour - 1"
                class="gc-hour-slot"
                :class="{ 'is-past': isPastSlot(cell.startDate, hour - 1) }"
              >
                <!-- AKTYWNA PRZYSZŁA/OBECNA GODZINA: Przycisk edycji -->
                <button
                  v-if="!isPastSlot(cell.startDate, hour - 1)"
                  class="gc-hover-edit-btn"
                  @click.stop="handleCellEdit(cell, hour - 1)"
                  :title="$t('dashboard.dodajWpis')"
                >
                  <svg viewBox="0 0 24 24" fill="none" stroke="#5f6368" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"></path>
                    <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"></path>
                  </svg>
                </button>

                <!-- MINIONA GODZINA: Tooltip informacyjny -->
                <div v-else class="gc-past-tooltip-wrapper">
                  <div class="gc-past-tooltip">{{ $t('dashboard.tenDzienGodzinaJuz') }}</div>
                </div>
              </div>
            </div>
          </template>
        </vue-cal>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import VueCal from 'vue-cal'
import 'vue-cal/dist/vuecal.css'

definePageMeta({
  layout: 'dashboard'
})

const vuecal = ref(null)

// Dzisiejsza data
const todayDate = new Date()
const selectedDate = ref(todayDate)
const dateRangeText = ref('')

const months = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec']

// --- NAWIGACJA ---
const goToToday = () => {
  selectedDate.value = new Date()
}

const onViewChange = (event) => {
  if (!event.startDate || !event.endDate) return
  const start = new Date(event.startDate)
  const end = new Date(event.endDate)
  dateRangeText.value = `${months[start.getMonth()]} ${start.getDate()}–${months[end.getMonth()]} ${end.getDate()}, ${end.getFullYear()}`
}

// Sprawdzanie czy podana data to dokładnie dzisiaj
const checkIsToday = (date) => {
  if (!date) return false
  const now = new Date()
  return date.getFullYear() === now.getFullYear() &&
         date.getMonth() === now.getMonth() &&
         date.getDate() === now.getDate()
}

// Sprawdzanie czy dany slot godzinowy już minął
const isPastSlot = (cellDate, hour) => {
  if (!cellDate) return false
  const slotDate = new Date(cellDate)
  slotDate.setHours(hour, 59, 59, 999) // Koniec danej godziny
  const now = new Date()
  return slotDate.getTime() < now.getTime()
}

const formatHour = (hour) => {
  if (hour === 0 || hour === 24) return '12 am'
  const ampm = hour >= 12 ? 'pm' : 'am'
  const h = hour % 12 || 12
  return `${h} ${ampm}`
}

const handleCellEdit = (cell, hour) => {
  console.log(`Dodawanie wpisu dla: ${cell.startDate.toDateString()}, godzina: ${hour}:00`)
}
</script>

<style scoped>
/* --- GŁÓWNY KONTENER --- */
.gc-wrapper {
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
  background: #ffffff;
  height: 100vh;
  padding: 16px;
  display: flex;
  flex-direction: column;
  box-sizing: border-box;
  overflow: hidden;
}

/* --- NAGŁÓWEK --- */
.gc-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 8px 16px 8px;
  flex-shrink: 0;
}

.gc-title {
  font-size: 20px;
  font-weight: 500;
  color: #1f1f1f;
  margin: 0 0 2px 0;
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
}

.dropdown-icon {
  width: 14px;
  height: 14px;
  fill: #5f6368;
}

.gc-subtitle {
  font-size: 12px;
  color: #70757a;
}

.gc-header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.gc-nav-group {
  display: flex;
  align-items: center;
  background: #e9eef6;
  border-radius: 8px;
  padding: 2px;
}

.gc-btn {
  background: transparent;
  border: none;
  color: #1f1f1f;
  cursor: pointer;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 500;
  border-radius: 6px;
  transition: background-color 0.2s;
}

.gc-btn:hover {
  background: rgba(0, 0, 0, 0.05);
}

.gc-btn.icon {
  width: 32px;
}

.gc-btn.icon svg {
  width: 18px;
  height: 18px;
  fill: #444746;
}

.gc-btn.text {
  padding: 0 12px;
}

.gc-btn.select {
  background: #e9eef6;
  padding: 0 12px 0 16px;
  gap: 8px;
  border-radius: 8px;
}

.gc-btn.settings {
  background: #e9eef6;
  border-radius: 8px;
}

/* --- KARTA Z KALENDARZEM --- */
.gc-card {
  background: #ffffff;
  border: 1px solid #dadce0;
  border-radius: 16px;
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.gc-body {
  flex: 1;
  height: 100%;
  position: relative;
  overflow: hidden;
}

:deep(.vuecal) {
  border: none !important;
  box-shadow: none !important;
  height: 100% !important;
  display: flex;
  flex-direction: column;
  background: #ffffff !important;
}

:deep(.vuecal__menu) {
  display: none !important;
}

/* UKRYCIE CZERWONEJ LINII AKTUALNEJ GODZINY */
:deep(.vuecal__now-line) {
  display: none !important;
}

:deep(.vuecal__body) {
  overflow-y: auto !important;
  scroll-behavior: smooth;
  -webkit-overflow-scrolling: touch;
  flex: 1;
  background: #ffffff !important;
}

:deep(.vuecal__cells), :deep(.vuecal__bg) {
  overflow: visible !important;
  background: #ffffff !important;
}
:deep(.vuecal__weekdays-headings) {
  border: none !important;
}
:deep(.vuecal__cell:before){
  border: none !important;
}
:deep(.vuecal__weekdays-headings) {
  padding-left: 59px !important;  }
:deep(.vuecal__header) {

  flex-shrink: 0;
  position: sticky;
  top: 0;
  z-index: 20;
  background: #ffffff !important;
}

:deep(.vuecal__heading) {
  height: 52px;
  padding: 0;
  font-weight: 400;
  color: #70757a;
  background: #ffffff !important;
}

/* NAGŁÓWEK DZISIEJSZEGO DNIA (WYRAŹNY NIEBIESKI) */
.gc-day-header {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 2px;
  font-size: 11px;
  font-weight: 500;
  color: #70757a;
  padding: 4px 0;
}

.gc-day-number {
  font-size: 14px;
  width: 26px;
  height: 26px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
}

/* Styl dla dzisiejszego dnia */
.gc-day-header.is-today {
  color: #1a73e8 !important;
  font-weight: 600;
}

.gc-day-header.is-today .gc-day-number {
  background-color: #1a73e8;
  color: #ffffff;
  font-weight: 600;
}

:deep(.vuecal__time-column) {
  width: 60px !important;
  border-right: 1px solid #e0e0e0 !important;
  background: #ffffff !important;
}

:deep(.vuecal__time-cell) {
  display: flex;
  align-items: flex-start;
  justify-content: flex-end;
  padding-right: 8px;
  overflow: visible !important;
}

.gc-time-label {
  font-size: 11px;
  color: #70757a;
  transform: translateY(-50%);
  background: #ffffff;
  padding: 0 2px;
  white-space: nowrap;
}

:deep(.vuecal__cell) {
  border-right: 1px solid #e0e0e0 !important;
  background-image: linear-gradient(to bottom, #e0e0e0 1px, transparent 1px) !important;
  background-size: 100% 60px !important;
  background-color: #ffffff !important;
  position: relative;
  padding: 0 !important;
}

:deep(.vuecal__cell.vuecal__cell--today),
:deep(.vuecal__cell.vuecal__cell--selected),
:deep(.vuecal__cell.vuecal__cell--current) {
  background-color: #ffffff !important;
}

:deep(.vuecal__cell:last-child) {
  border-right: none !important;
}

:deep(.vuecal__no-event) {
  display: none !important;
}

/* SIATKA HOVER Z LOGIKĄ DLA PRZESZŁOŚCI */
.gc-hourly-grid-overlay {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.gc-hour-slot {
  height: 60px;
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
}

/* Komórki, które minęły */
.gc-hour-slot.is-past {
  cursor: not-allowed;
}

.gc-hour-slot.is-past:hover {
  background-color: rgba(0, 0, 0, 0.015);
}

.gc-hour-slot:not(.is-past):hover .gc-hover-edit-btn {
  opacity: 1;
  visibility: visible;
  transform: scale(1);
}

/* PRZYCISK EDYCJI */
.gc-hover-edit-btn {
  width: 110px;
  height: 52px;
  background: #ffffff;
  border-radius: 12px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12), 0 1px 3px rgba(0, 0, 0, 0.08);
  border: 1px solid #dadce0;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  pointer-events: auto;

  opacity: 0;
  visibility: hidden;
  transform: scale(0.95);
  transition: opacity 0.15s ease, transform 0.15s ease, background-color 0.15s;
  z-index: 10;
}

.gc-hover-edit-btn:hover {
  background-color: #f8f9fa;
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.15);
}

.gc-hover-edit-btn svg {
  width: 22px;
  height: 22px;
}

/* TOOLTIP DLA PRZESZŁYCH DNI/GODZIN */
.gc-past-tooltip-wrapper {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  pointer-events: none;
}

.gc-hour-slot.is-past:hover .gc-past-tooltip {
  opacity: 1;
  visibility: visible;
  transform: translateY(0);
}

.gc-past-tooltip {
  opacity: 0;
  visibility: hidden;
  background: #323232;
  color: #ffffff;
  font-size: 11px;
  font-weight: 500;
  padding: 6px 12px;
  border-radius: 6px;
  white-space: nowrap;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
  transition: opacity 0.15s ease, transform 0.15s ease;
  transform: translateY(4px);
  z-index: 30;
}
:deep(.vuecal__time-column) {
  border: none !important;
}
:deep(.week-view) {
  border: none !important;
}
</style>
