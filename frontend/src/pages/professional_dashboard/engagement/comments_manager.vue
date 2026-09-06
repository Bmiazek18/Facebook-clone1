<template>
  <div class="min-h-screen bg-gray-50 p-6 flex justify-center items-start">
    <!-- Główny kontener -->
    <div class="w-full max-w-6xl bg-white rounded-xl shadow-sm border border-gray-200 overflow-hidden">

      <!-- Nagłówek -->
      <div class="p-6 pb-0">
        <h1 class="text-2xl font-bold text-gray-900 mb-6">{{ $t('dashboard.menedzerKomentarzy') }}</h1>

        <div class="flex flex-col sm:flex-row sm:items-center justify-between pb-4 border-b border-gray-200 gap-4">
          <!-- Zakładki (lewa strona) -->
          <div class="flex items-center space-x-1">
            <button class="px-5 py-2 bg-blue-50 text-blue-600 font-semibold rounded-full text-sm transition-colors">{{ $t('createLive.comments') }}</button>
            <button class="px-5 py-2 text-gray-700 font-semibold hover:bg-gray-100 rounded-full text-sm transition-colors">{{ $t('dashboard.wzmianki') }}</button>
          </div>

          <!-- Filtry i opcje (prawa strona) -->
          <div class="flex items-center space-x-3">
            <button class="flex items-center space-x-2 px-4 py-2 bg-gray-100 hover:bg-gray-200 text-gray-800 rounded-lg text-sm font-semibold transition-colors">
              <!-- Ikona suwaków (Filtry) -->
              <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                <path stroke-linecap="round" stroke-linejoin="round" d="M12 6V4m0 2a2 2 0 100 4m0-4a2 2 0 110 4m-6 8a2 2 0 100-4m0 4a2 2 0 110-4m0 4v2m0-6V4m6 6v10m6-2a2 2 0 100-4m0 4a2 2 0 110-4m0 4v2m0-6V4" />
              </svg>
              <span>{{ $t('search.filters') }}</span>
              <!-- Strzałka w dół -->
              <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 text-gray-600" viewBox="0 0 20 20" fill="currentColor">
                <path fill-rule="evenodd" d="M5.293 7.293a1 1 0 011.414 0L10 10.586l3.293-3.293a1 1 0 111.414 1.414l-4 4a1 1 0 01-1.414 0l-4-4a1 1 0 010-1.414z" clip-rule="evenodd" />
              </svg>
            </button>

            <button class="flex items-center space-x-2 px-4 py-2 bg-blue-50 hover:bg-blue-100 text-blue-600 rounded-lg text-sm font-semibold transition-colors">
              <span>{{ $t('dashboard.nieOdpowiedziano') }}</span>
              <!-- Strzałka w dół -->
              <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4" viewBox="0 0 20 20" fill="currentColor">
                <path fill-rule="evenodd" d="M5.293 7.293a1 1 0 011.414 0L10 10.586l3.293-3.293a1 1 0 111.414 1.414l-4 4a1 1 0 01-1.414 0l-4-4a1 1 0 010-1.414z" clip-rule="evenodd" />
              </svg>
            </button>
          </div>
        </div>
      </div>

      <!-- Pusty stan (Zawartość) -->
      <div class="flex flex-col items-center justify-center py-32 px-4 text-center">
        <!-- Niestandardowa ikona wiadomości z sercem -->
        <svg viewBox="0 0 64 64" class="w-32 h-32 mb-4 drop-shadow-md">
          <defs>
            <linearGradient id="bubble-gradient" x1="0%" y1="0%" x2="0%" y2="100%">
              <stop offset="0%" stop-color="#5599FF" />
              <stop offset="100%" stop-color="#1A66FF" />
            </linearGradient>
          </defs>
          <!-- Dymek czatu -->
          <path d="M8 14 C8 8.5 12.5 4 18 4 L46 4 C51.5 4 56 8.5 56 14 L56 36 C56 41.5 51.5 46 46 46 L34 46 L23.5 55.5 C21 57.5 17 56 17 52 L17 46 L18 46 C12.5 46 8 41.5 8 36 Z" fill="url(#bubble-gradient)" />
          <!-- Serce w środku -->
          <path d="M32 37 C32 37 18 26 18 18 C18 13.5 21.5 10 26 10 C28.5 10 30.5 11.5 32 13.5 C33.5 11.5 35.5 10 38 10 C42.5 10 46 13.5 46 18 C46 26 32 37 32 37 Z" fill="white" />
        </svg>

        <!-- Teksty -->
        <p class="text-[17px] text-gray-500 mb-1">{{ $t('dashboard.jestesNaBiezaco') }}</p>
        <p class="text-[17px] text-gray-500">{{ $t('dashboard.rozpoczynajNoweKonwersacjePoprzez') }}<a href="#" class="text-blue-600 font-medium hover:underline">{{ $t('dashboard.tworzeniePosta') }}</a>.
        </p>
      </div>

    </div>
  </div>
</template>

<script setup>
definePageMeta({
  layout: 'dashboard'
})
</script>

<style scoped>
/* Dodatkowe drobne style (większość obsługuje Tailwind) */
</style>
