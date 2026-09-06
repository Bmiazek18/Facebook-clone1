<script setup lang="ts">
import { ref, computed } from 'vue'

// Flaga określająca, czy są dostępne materiały.
// Zmień na `true`, aby zobaczyć kafelki ze statystykami.
const hasMaterials = ref(false)

const scheduledDays = computed(() => {
  const days = []
  const today = new Date()
  for (let i = 0; i < 4; i++) {
    const d = new Date(today)
    d.setDate(today.getDate() + i)
    days.push(d.getDate())
  }
  return days
})
</script>

<template>
  <div class="bg-white rounded-[8px] shadow-sm p-4 flex flex-col">

    <!-- Nagłówek -->
    <div class="flex justify-between items-start mb-10">
      <div>
        <h2 class="text-[20px] font-bold text-[#050505] leading-tight">{{ $t('dashboard.materialy') }}</h2>
        <p class="text-[13px] text-[#65676B] mt-1">{{ $t('dashboard.uzyskujDostepDoOpublikowanych') }}</p>
      </div>
      <a href="#" class="text-[#1877F2] text-[15px] font-semibold hover:underline shrink-0">{{ $t('notifications_page.viewAll') }}</a>
    </div>

    <!-- WIDOK GDY NIE MA MATERIAŁÓW -->
    <div v-if="!hasMaterials" class="flex flex-col items-center justify-center mb-8">
      <div class="w-[72px] h-[72px] mb-4 relative">
        <svg viewBox="0 0 48 48" fill="none" xmlns="http://www.w3.org/2000/svg" class="w-full h-full">
          <path d="M12 4C9.79 4 8 5.79 8 8V40C8 42.21 9.79 44 12 44H36C38.21 44 40 42.21 40 40V16L28 4H12Z" fill="#A4A7AB"/>
          <path d="M28 4V16H40" fill="#1877F2"/>
          <rect x="16" y="22" width="16" height="2" fill="white"/>
          <rect x="16" y="28" width="16" height="2" fill="white"/>
          <rect x="16" y="34" width="10" height="2" fill="white"/>
        </svg>
      </div>
      <h3 class="text-[17px] font-bold text-[#050505] mb-1">{{ $t('dashboard.nieMaszJeszczeZadnych') }}</h3>
      <p class="text-[15px] text-[#65676B] mb-4 text-center">{{ $t('dashboard.zacznijTworzycAbyDodac') }}</p>
      <button class="bg-[#1877F2] text-white px-4 py-2 rounded-md font-semibold text-[15px] flex items-center gap-1.5 hover:bg-[#166FE5] transition-colors">
        <svg viewBox="0 0 24 24" width="16" height="16" fill="currentColor">
          <path d="M19 3H5c-1.11 0-2 .89-2 2v14c0 1.11.89 2 2 2h14c1.11 0 2-.89 2-2V5c0-1.11-.89-2-2-2zm-2 10h-4v4h-2v-4H7v-2h4V7h2v4h4v2z"/>
        </svg>{{ $t('feed.utworz') }}<svg viewBox="0 0 24 24" width="18" height="18" fill="currentColor">
          <path d="M7 10l5 5 5-5z"/>
        </svg>
      </button>
    </div>

    <!-- WIDOK GDY SĄ MATERIAŁY (Statystyki) -->
    <div v-else class="flex flex-col md:flex-row gap-4 mb-10">

      <!-- Miniaturka posta (lewa strona) -->
      <div class="shrink-0 p-1 bg-white border border-gray-100 shadow-[0_2px_8px_rgba(0,0,0,0.08)] rounded-2xl w-[140px] h-[140px]">
        <div class="w-full h-full rounded-xl bg-gradient-to-b from-[#8E9399] to-[#55595E] p-3 flex flex-col">
          <span class="text-white font-bold text-[18px] leading-none">{{ $t('chat.aa') }}</span>
        </div>
      </div>

      <!-- Kafelki statystyk (prawa strona) -->
      <div class="flex-1 flex flex-col gap-3">

        <!-- Wyświetlenia (Górny kafelek) -->
        <div class="bg-[#F7F8FA] rounded-xl p-4 flex flex-col justify-center">
          <svg class="w-[22px] h-[22px] mb-2 text-gray-800" fill="none" stroke="currentColor" stroke-width="1.8" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
            <path stroke-linecap="round" stroke-linejoin="round" d="M2.036 12.322a1.012 1.012 0 010-.639C3.423 7.51 7.36 4.5 12 4.5c4.638 0 8.573 3.007 9.963 7.178.07.207.07.431 0 .639C20.577 16.49 16.64 19.5 12 19.5c-4.638 0-8.573-3.007-9.963-7.178z"></path>
            <path stroke-linecap="round" stroke-linejoin="round" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"></path>
          </svg>
          <div class="text-[18px] font-bold text-gray-900 leading-none mb-1">0</div>
          <div class="flex items-center gap-1.5 text-[14px] text-gray-700">{{ $t('dashboard.wyswietlenia') }}<svg class="w-4 h-4 text-gray-500" viewBox="0 0 20 20" fill="currentColor">
              <path fill-rule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7-4a1 1 0 11-2 0 1 1 0 012 0zM9 9a1 1 0 000 2v3a1 1 0 001 1h1a1 1 0 100-2v-3a1 1 0 00-1-1H9z" clip-rule="evenodd" />
            </svg>
          </div>
        </div>

        <!-- Dolny rząd (Aktywność i Obserwacje) -->
        <div class="flex gap-3">

          <!-- Aktywność -->
          <div class="flex-1 bg-[#F7F8FA] rounded-xl p-4 flex flex-col justify-center">
            <svg class="w-[22px] h-[22px] mb-2 text-gray-800" fill="none" stroke="currentColor" stroke-width="1.8" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
              <path stroke-linecap="round" stroke-linejoin="round" d="M8.625 12a.375.375 0 11-.75 0 .375.375 0 01.75 0zm0 0H8.25m4.125 0a.375.375 0 11-.75 0 .375.375 0 01.75 0zm0 0H12m4.125 0a.375.375 0 11-.75 0 .375.375 0 01.75 0zm0 0h-.375M21 12c0 4.556-4.03 8.25-9 8.25a9.764 9.764 0 01-2.555-.337A5.972 5.972 0 015.41 20.97a5.969 5.969 0 01-.474-.065 4.48 4.48 0 00.978-2.025c.09-.457-.133-.901-.467-1.226C3.93 16.178 3 14.189 3 12c0-4.556 4.03-8.25 9-8.25s9 3.694 9 8.25z"></path>
            </svg>
            <div class="text-[18px] font-bold text-gray-900 leading-none mb-1">0</div>
            <div class="flex items-center gap-1.5 text-[14px] text-gray-700">{{ $t('emojiPicker.categories.activity') }}<svg class="w-4 h-4 text-gray-500" viewBox="0 0 20 20" fill="currentColor">
                <path fill-rule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7-4a1 1 0 11-2 0 1 1 0 012 0zM9 9a1 1 0 000 2v3a1 1 0 001 1h1a1 1 0 100-2v-3a1 1 0 00-1-1H9z" clip-rule="evenodd" />
              </svg>
            </div>
          </div>

          <!-- Obserwacje -->
          <div class="flex-1 bg-[#F7F8FA] rounded-xl p-4 flex flex-col justify-center">
            <svg class="w-[22px] h-[22px] mb-2 text-gray-800" fill="none" stroke="currentColor" stroke-width="1.8" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
              <path stroke-linecap="round" stroke-linejoin="round" d="M15 19.128a9.38 9.38 0 002.625.372 9.337 9.337 0 004.121-.952 4.125 4.125 0 00-7.533-2.493M15 19.128v-.003c0-1.113-.285-2.16-.786-3.07M15 19.128v.106A12.318 12.318 0 018.624 21c-2.331 0-4.512-.645-6.374-1.766l-.001-.109a6.375 6.375 0 0111.964-3.07M12 6.375a3.375 3.375 0 11-6.75 0 3.375 3.375 0 016.75 0zm8.25 2.25a2.625 2.625 0 11-5.25 0 2.625 2.625 0 015.25 0z"></path>
            </svg>
            <div class="text-[18px] font-bold text-gray-900 leading-none mb-1">0</div>
            <div class="flex items-center gap-1.5 text-[14px] text-gray-700">{{ $t('dashboard.obserwacje') }}<svg class="w-4 h-4 text-gray-500" viewBox="0 0 20 20" fill="currentColor">
                <path fill-rule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7-4a1 1 0 11-2 0 1 1 0 012 0zM9 9a1 1 0 000 2v3a1 1 0 001 1h1a1 1 0 100-2v-3a1 1 0 00-1-1H9z" clip-rule="evenodd" />
              </svg>
            </div>
          </div>

        </div>
      </div>
    </div>

    <!-- Zaplanowana zawartość -->
    <div class="flex justify-between items-center mb-4">
      <h2 class="text-[20px] font-bold text-[#050505]">{{ $t('dashboard.zaplanowanaZawartosc') }}</h2>
      <a href="#" class="text-[#1877F2] text-[15px] font-semibold hover:underline">{{ $t('notifications_page.viewAll') }}</a>
    </div>

    <!-- Kalendarz -->
    <div class="grid grid-cols-4 rounded-lg overflow-hidden min-h-[180px]">
      <!-- Day 1 (Today) -->
      <div class="border-r border-[#CED0D4] p-2 flex flex-col items-center justify-center bg-[#F0F2F5] text-center relative pb-4 pt-8">
        <div class="absolute top-2 left-2 w-6 h-6 rounded-full bg-[#1877F2] text-white flex items-center justify-center text-[13px] font-semibold">{{ scheduledDays[0] }}</div>
        <svg viewBox="0 0 24 24" width="22" height="22" fill="#050505" class="mb-1">
          <path d="M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zM20.71 7.04c.39-.39.39-1.02 0-1.41l-2.34-2.34c-.39-.39-1.02-.39-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z"/>
        </svg>
        <div class="font-bold text-[14px] text-[#050505] leading-tight mb-1">{{ $t('dashboard.brakPostowDzisiaj') }}</div>
        <div class="text-[12px] text-[#65676B] leading-tight px-1">{{ $t('dashboard.utrzymujZainteresowanieOdbiorcowPoprzez') }}</div>
      </div>
      <!-- Day 2 -->
      <div class="border-r border-[#CED0D4] p-2 relative">
         <div class="text-[13px] text-[#65676B] font-medium">{{ scheduledDays[1] }}</div>
      </div>
      <!-- Day 3 -->
      <div class="border-r border-[#CED0D4] p-2 relative">
         <div class="text-[13px] text-[#65676B] font-medium">{{ scheduledDays[2] }}</div>
      </div>
      <!-- Day 4 -->
      <div class="p-2 relative">
         <div class="text-[13px] text-[#65676B] font-medium">{{ scheduledDays[3] }}</div>
      </div>
    </div>
  </div>
</template>
