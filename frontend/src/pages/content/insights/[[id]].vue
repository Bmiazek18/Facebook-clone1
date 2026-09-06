<template>
  <div class="min-h-screen bg-[#F0F2F5] mt-[56px] flex font-sans">

    <aside class="w-[400px] shrink-0 bg-white border-r border-gray-200 flex flex-col h-screen sticky top-0 overflow-y-auto z-10 hidden md:flex">

      <!-- Nagłówek panelu -->
      <div class="flex items-start gap-3 px-4 pt-6 pb-2">
        <button class="mt-1 p-2 hover:bg-gray-100 rounded-full text-gray-700 transition-colors">
          <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
            <path stroke-linecap="round" stroke-linejoin="round" d="M10 19l-7-7m0 0l7-7m-7 7h18" />
          </svg>
        </button>
        <div class="flex flex-col">
          <span class="text-[14px] text-gray-500">{{ $t('dashboard.najpopularniejszaZawartosc') }}</span>
          <h1 class="text-[24px] font-bold text-gray-900 leading-tight mt-0.5">{{ $t('dashboard.statystykiPosta') }}</h1>
        </div>
      </div>

      <!-- Karta posta (podgląd) -->
      <div class="p-4">
        <div class="bg-white border border-gray-200 rounded-2xl p-4 shadow-sm">
          <!-- Nagłówek posta -->
          <div class="flex items-center justify-between">
            <div class="flex items-center gap-3">
              <div class="w-10 h-10 bg-gray-300 rounded-full overflow-hidden flex items-center justify-center">
                <svg class="w-8 h-8 text-gray-500 mt-2" fill="currentColor" viewBox="0 0 24 24">
                  <path d="M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z" />
                </svg>
              </div>
              <div class="flex flex-col">
                <span class="text-[15px] font-bold text-gray-900 leading-tight">{{ $t('dashboard.kont') }}</span>
                <div class="flex items-center text-[13px] text-gray-500 gap-1 mt-0.5">
                  <span>{{ $t('dashboard.5Min') }}</span><span>·</span>
                  <svg xmlns="http://www.w3.org/2000/svg" class="h-[14px] w-[14px]" viewBox="0 0 20 20" fill="currentColor">
                    <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM4.332 8.027a6.012 6.012 0 011.912-2.706C6.512 5.73 6.974 6 7.5 6A1.5 1.5 0 019 7.5V8a2 2 0 004 0 2 2 0 011.523-1.943A5.977 5.977 0 0116 10c0 .34-.028.675-.083 1H15a2 2 0 00-2 2v2.197A5.973 5.973 0 0110 16v-2a2 2 0 00-2-2 2 2 0 01-2-2 2 2 0 00-1.668-1.973z" clip-rule="evenodd" />
                  </svg>
                </div>
              </div>
            </div>
            <button class="text-gray-500 hover:bg-gray-100 p-2 rounded-full transition-colors">
              <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" viewBox="0 0 20 20" fill="currentColor">
                <path d="M6 10a2 2 0 11-4 0 2 2 0 014 0zM12 10a2 2 0 11-4 0 2 2 0 014 0zM16 12a2 2 0 100-4 2 2 0 000 4z" />
              </svg>
            </button>
          </div>

          <!-- Treść posta -->
          <div class="mt-3 text-[20px] text-black">{{ $t('dashboard.test') }}</div>

          <!-- Pasek akcji -->
          <div class="flex items-center gap-5 mt-4 text-gray-600">
            <button class="flex items-center gap-1.5 hover:bg-gray-100 p-1 rounded-md transition-colors">
              <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="1.5"><path stroke-linecap="round" stroke-linejoin="round" d="M14 10h4.764a2 2 0 011.789 2.894l-3.5 7A2 2 0 0115.263 21h-4.017c-.163 0-.326-.02-.485-.06L7 20m7-10V5a2 2 0 00-2-2h-.095c-.5 0-.905.405-.905.905 0 .714-.211 1.412-.608 2.006L7 11v9m7-10h-2M7 20H5a2 2 0 01-2-2v-6a2 2 0 012-2h2.5" /></svg>
              <span class="text-[14px] font-medium">1</span>
            </button>
            <button class="hover:bg-gray-100 p-1 rounded-md transition-colors">
              <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="1.5"><path stroke-linecap="round" stroke-linejoin="round" d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z" /></svg>
            </button>
            <button class="hover:bg-gray-100 p-1 rounded-md transition-colors">
              <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="1.5"><path stroke-linecap="round" stroke-linejoin="round" d="M8.684 13.342C8.886 12.938 9 12.482 9 12c0-.482-.114-.938-.316-1.342m0 2.684a3 3 0 110-2.684m0 2.684l6.632 3.316m-6.632-6l6.632-3.316m0 0a3 3 0 105.367-2.684 3 3 0 00-5.367 2.684zm0 9.316a3 3 0 105.368 2.684 3 3 0 00-5.368-2.684z" /></svg>
            </button>
            <div class="ml-auto">
              <div class="bg-red-500 text-white rounded-full p-[2px] shadow-sm flex items-center justify-center">
                <svg xmlns="http://www.w3.org/2000/svg" class="h-3 w-3" viewBox="0 0 20 20" fill="currentColor"><path fill-rule="evenodd" d="M3.172 5.172a4 4 0 015.656 0L10 6.343l1.172-1.171a4 4 0 115.656 5.656L10 17.657l-6.828-6.829a4 4 0 010-5.656z" clip-rule="evenodd" /></svg>
              </div>
            </div>
          </div>

          <!-- Pole komentarza -->
          <div class="mt-4 pt-1 flex items-center gap-2">
            <div class="relative cursor-pointer">
              <div class="w-8 h-8 bg-gray-300 rounded-full overflow-hidden flex items-center justify-center">
                <svg class="w-6 h-6 text-gray-500 mt-1.5" fill="currentColor" viewBox="0 0 24 24"><path d="M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z" /></svg>
              </div>
              <div class="absolute -bottom-1 -right-1 bg-gray-200 rounded-full p-[1px] border border-white">
                <svg xmlns="http://www.w3.org/2000/svg" class="h-2.5 w-2.5 text-gray-700" viewBox="0 0 20 20" fill="currentColor"><path fill-rule="evenodd" d="M5.293 7.293a1 1 0 011.414 0L10 10.586l3.293-3.293a1 1 0 111.414 1.414l-4 4a1 1 0 01-1.414 0l-4-4a1 1 0 010-1.414z" clip-rule="evenodd" /></svg>
              </div>
            </div>
            <div class="flex-1 bg-[#F0F2F5] rounded-full px-4 py-2 cursor-pointer hover:bg-[#E4E6E9] transition-colors">
              <span class="text-[14px] text-gray-500">{{ $t('dashboard.skomentujJakoKont') }}</span>
            </div>
          </div>
        </div>
      </div>
    </aside>

    <!-- ========================================== -->
    <!-- PRAWY PANEL (Główna siatka ze statystykami)-->
    <!-- ========================================== -->
    <main class="flex-1 p-6">
      <div class="max-w-6xl mx-auto flex flex-col gap-4">

        <!-- SIATKA GŁÓWNA (2 kolumny) -->
        <div class="grid grid-cols-1 xl:grid-cols-2 gap-4">

          <!-- Karta 1: Wyświetlenia -->

            <Stats/>


          <!-- Karta 2: Aktywność -->
          <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-5 flex flex-col min-h-[240px]">
            <h2 class="text-[16px] font-bold text-gray-900 flex items-center gap-1.5 mb-4">{{ $t('dashboard.1Aktywnosc') }}<InfoIcon />
            </h2>
            <!-- Bloki statystyk -->
            <div class="grid grid-cols-2 gap-2">
              <div class="bg-[#F7F8FA] rounded-lg p-3 flex justify-between items-start h-[72px]">
                <div class="flex flex-col justify-between h-full">
                  <span class="font-bold text-gray-900 text-[15px]">1</span>
                  <span class="text-[12px] text-gray-600">{{ $t('createLive.reactions') }}</span>
                </div>
                <svg class="h-5 w-5 text-gray-600" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="1.5"><path stroke-linecap="round" stroke-linejoin="round" d="M14 10h4.764a2 2 0 011.789 2.894l-3.5 7A2 2 0 0115.263 21h-4.017c-.163 0-.326-.02-.485-.06L7 20m7-10V5a2 2 0 00-2-2h-.095c-.5 0-.905.405-.905.905 0 .714-.211 1.412-.608 2.006L7 11v9m7-10h-2M7 20H5a2 2 0 01-2-2v-6a2 2 0 012-2h2.5" /></svg>
              </div>
              <div class="bg-[#F7F8FA] rounded-lg p-3 flex justify-between items-start h-[72px]">
                <div class="flex flex-col justify-between h-full">
                  <span class="font-bold text-gray-900 text-[15px]">0</span>
                  <span class="text-[12px] text-gray-600">{{ $t('dashboard.klikniecia') }}</span>
                </div>
                <svg class="h-5 w-5 text-gray-600" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="1.5"><path stroke-linecap="round" stroke-linejoin="round" d="M15 15l-2 5L9 9l11 4-5 2zm0 0l5 5M7.188 2.239l.777 2.897M5.136 7.965l-2.898-.777M13.95 4.05l-2.122 2.122m-5.657 5.656l-2.12 2.122" /></svg>
              </div>
              <div class="bg-[#F7F8FA] rounded-lg p-3 flex justify-between items-start h-[72px]">
                <div class="flex flex-col justify-between h-full">
                  <span class="font-bold text-gray-900 text-[15px]">0</span>
                  <span class="text-[12px] text-gray-600">{{ $t('createLive.comments') }}</span>
                </div>
                <svg class="h-5 w-5 text-gray-600" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="1.5"><path stroke-linecap="round" stroke-linejoin="round" d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z" /></svg>
              </div>
              <div class="bg-[#F7F8FA] rounded-lg p-3 flex justify-between items-start h-[72px]">
                <div class="flex flex-col justify-between h-full">
                  <span class="font-bold text-gray-900 text-[15px]">0</span>
                  <span class="text-[12px] text-gray-600">{{ $t('createLive.shares') }}</span>
                </div>
                <svg class="h-5 w-5 text-gray-600" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="1.5"><path stroke-linecap="round" stroke-linejoin="round" d="M8.684 13.342C8.886 12.938 9 12.482 9 12c0-.482-.114-.938-.316-1.342m0 2.684a3 3 0 110-2.684m0 2.684l6.632 3.316m-6.632-6l6.632-3.316m0 0a3 3 0 105.367-2.684 3 3 0 00-5.367 2.684zm0 9.316a3 3 0 105.368 2.684 3 3 0 00-5.368-2.684z" /></svg>
              </div>
            </div>
            <!-- Reakcje według typu -->
            <div class="mt-4">
              <h3 class="text-[14px] font-semibold text-gray-900 mb-2">{{ $t('dashboard.reakcjaWedlugTypu') }}</h3>
              <div class="flex justify-between items-center px-1">
                <div class="flex flex-col items-center gap-1"><span class="text-[20px]">👍</span><span class="text-[12px] text-gray-600">0</span></div>
                <div class="flex flex-col items-center gap-1"><span class="text-[20px]">❤️</span><span class="text-[12px] text-gray-600">1</span></div>
                <div class="flex flex-col items-center gap-1"><span class="text-[20px]">🥰</span><span class="text-[12px] text-gray-600">0</span></div>
                <div class="flex flex-col items-center gap-1"><span class="text-[20px]">😂</span><span class="text-[12px] text-gray-600">0</span></div>
                <div class="flex flex-col items-center gap-1"><span class="text-[20px]">😮</span><span class="text-[12px] text-gray-600">0</span></div>
                <div class="flex flex-col items-center gap-1"><span class="text-[20px]">😢</span><span class="text-[12px] text-gray-600">0</span></div>
                <div class="flex flex-col items-center gap-1"><span class="text-[20px]">😡</span><span class="text-[12px] text-gray-600">0</span></div>
              </div>
            </div>
          </div>

          <!-- Karta 3: Osoby obserwujące -->
          <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-5 flex flex-col min-h-[220px]">
            <h2 class="text-[16px] font-bold text-gray-900 flex items-center gap-1.5">{{ $t('dashboard.osobyObserwujaceANieobserwujace') }}<InfoIcon />
            </h2>
            <div class="flex-1 flex justify-center items-center gap-12">
              <div class="flex flex-col items-center text-center">
                <span class="font-bold text-[16px] text-gray-900">0%</span>
                <span class="text-[12px] text-gray-500 mb-1">0</span>
                <div class="flex items-center gap-1.5 text-[12px] text-gray-600">
                  <span class="w-2 h-2 rounded-full bg-blue-500"></span>{{ $t('dashboard.obserwatorzy') }}</div>
              </div>
              <div class="flex flex-col items-center text-center">
                <span class="font-bold text-[16px] text-gray-900">0%</span>
                <span class="text-[12px] text-gray-500 mb-1">0</span>
                <div class="flex items-start gap-1.5 text-[12px] text-gray-600 text-left">
                  <span class="w-2 h-2 rounded-full bg-gray-400 mt-1 shrink-0"></span>{{ $t('search.people') }}<br>{{ $t('dashboard.niebedace') }}<br>{{ $t('dashboard.obserwatorami') }}</div>
              </div>
            </div>
          </div>

          <!-- Karta 4: Obserwacje -->
          <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-5 flex flex-col min-h-[220px]">
            <h2 class="text-[16px] font-bold text-gray-900 flex items-center gap-1.5">{{ $t('dashboard.obserwacje') }}<InfoIcon />
            </h2>
            <div class="flex-1 flex flex-col items-center justify-center">
              <span class="text-[32px] font-bold text-gray-900 leading-none">0</span>
              <span class="text-[14px] text-gray-600 mt-1">{{ $t('dashboard.obserwacje') }}</span>
            </div>
          </div>

          <!-- Karta 5: Jak znajdują -->
          <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-5 flex flex-col min-h-[200px]">
            <h2 class="text-[16px] font-bold text-gray-900 flex items-center gap-1.5 mb-3">{{ $t('dashboard.jakUzytkownicyZnajdujaTwoje') }}<InfoIcon />
            </h2>
            <div class="flex items-center gap-2 mb-6">
              <button class="px-3 py-1.5 bg-blue-50 text-blue-700 font-semibold rounded-full text-[14px]">{{ $t('dashboard.ruch') }}</button>
              <button class="px-3 py-1.5 text-gray-900 font-semibold hover:bg-gray-50 rounded-full text-[14px]">{{ $t('dashboard.zrodlo') }}</button>
            </div>
            <div class="flex-1 flex items-center justify-center">
              <p class="text-[14px] text-gray-500">{{ $t('dashboard.zaMaloDanychDo') }}</p>
            </div>
          </div>

          <!-- Karta 6: Kto wyświetlił -->
          <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-5 flex flex-col min-h-[200px]">
            <h2 class="text-[16px] font-bold text-gray-900 flex items-center gap-1.5 mb-3">{{ $t('dashboard.ktoWyswietlilTwojeMaterialy') }}<InfoIcon />
            </h2>
            <div class="flex items-center gap-2 mb-6">
              <button class="px-3 py-1.5 bg-blue-50 text-blue-700 font-semibold rounded-full text-[14px]">{{ $t('dashboard.wiekIPlec') }}</button>
              <button class="px-3 py-1.5 text-gray-900 font-semibold hover:bg-gray-50 rounded-full text-[14px]">{{ $t('dashboard.czoloweKraje') }}</button>
            </div>
            <div class="flex-1 flex items-center justify-center">
              <p class="text-[14px] text-gray-500">{{ $t('dashboard.zaMaloDanychDo') }}</p>
            </div>
          </div>
        </div>

        <!-- DOLNY PANEL: Kliknięcia linku -->
        <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-5 w-full min-h-[220px] flex flex-col">
          <h2 class="text-[18px] font-bold text-gray-900 flex items-center gap-2">{{ $t('dashboard.kliknieciaLinku') }}<InfoIcon class="w-[18px] h-[18px]" />
          </h2>
          <div class="flex-1 flex flex-col items-center justify-center text-center mt-4">
            <h3 class="text-[18px] font-bold text-gray-600 mb-2">{{ $t('dashboard.statystykiNiedostepne') }}</h3>
            <p class="text-[15px] text-gray-500 max-w-[800px]">{{ $t('dashboard.prawdopodobnieNieMaJeszcze') }}</p>
          </div>
        </div>

      </div>
    </main>

  </div>
</template>

<script setup>
import { h } from 'vue'
import Stats from '~/components/professional_dashboard/Stats.vue';

const InfoIcon = () => h('svg', {
  xmlns: 'http://www.w3.org/2000/svg',
  viewBox: '0 0 20 20',
  fill: 'currentColor',
  class: 'w-[16px] h-[16px] text-gray-500'
}, [
  h('path', {
    'fill-rule': 'evenodd',
    d: 'M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7-4a1 1 0 11-2 0 1 1 0 012 0zM9 9a1 1 0 000 2v3a1 1 0 001 1h1a1 1 0 100-2v-3a1 1 0 00-1-1H9z',
    'clip-rule': 'evenodd'
  })
])
</script>
