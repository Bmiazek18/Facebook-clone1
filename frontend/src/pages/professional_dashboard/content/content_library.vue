<template>
  <div class="bg-[#f0f2f5] min-h-screen p-6 font-sans text-[#050505]">

    <div class="bg-white border border-[#dadde1] rounded-lg shadow-sm">

      <!-- GÓRNA SEKCJA -->
      <div class="p-5">

        <h1 class="text-[20px] font-bold mb-4 tracking-tight">{{ $t('dashboard.bibliotekaMaterialow') }}</h1>

        <!-- Zakładki -->
        <div class="flex space-x-1 mb-5">
          <button class="bg-[#ebf5ff] text-[#1877f2] font-semibold text-[15px] px-4 py-1.5 rounded-full">{{ $t('dashboard.opublikowane') }}</button>
          <button class="text-[#050505] font-semibold text-[15px] px-4 py-1.5 rounded-full hover:bg-[#f0f2f5] transition-colors">{{ $t('metaAi.zaplanowane') }}</button>
          <button class="text-[#050505] font-semibold text-[15px] px-4 py-1.5 rounded-full hover:bg-[#f0f2f5] transition-colors">{{ $t('dashboard.wersjeRobocze') }}</button>
        </div>

        <!-- PASEK NARZĘDZI (Wyszukiwarka + Przyciski) -->
        <div class="flex items-start gap-4 mb-2">

          <!-- Lewa strona: Wyszukiwarka i licznik pod spodem -->
          <div class="flex flex-col gap-2 shrink-0">
            <div class="relative">
              <svg class="w-4 h-4 text-gray-500 absolute left-3 top-1/2 transform -translate-y-1/2" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2.5" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"></path></svg>
              <input
                type="text"
                :placeholder="$t('dashboard.szukajPostow')"
                class="bg-[#f0f2f5] text-[15px] text-[#050505] rounded-full pl-9 pr-4 py-2 w-[240px] focus:outline-none placeholder-gray-500"
              >
            </div>
            <div class="text-[13px] text-[#65676b] ml-1">
              <span class="font-bold text-[#050505]">0/50</span>{{ $t('dashboard.wybranychPostow') }}</div>
          </div>

          <!-- Prawa strona: Przyciski akcji -->
          <div class="flex flex-wrap items-center gap-2 pt-0.5">

            <!-- Przycisk Utwórz -->
            <button class="bg-[#1877f2] hover:bg-[#166fe5] text-white text-[15px] font-semibold px-4 py-2 rounded-lg flex items-center gap-1.5 transition-colors">
              <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2.5" d="M12 4v16m8-8H4"></path></svg>{{ $t('feed.utworz') }}<svg class="w-[10px] h-[10px] ml-0.5 mt-0.5" fill="currentColor" viewBox="0 0 16 16"><path d="M4 6h8l-4 5z"></path></svg>
            </button>

            <!-- Przycisk Filtry -->
            <button class="bg-[#e4e6eb] hover:bg-[#d8dadf] text-[#050505] text-[15px] font-semibold px-3 py-2 rounded-lg flex items-center gap-1.5 transition-colors">
              <svg class="w-[18px] h-[18px]" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2.5" d="M12 6V4m0 2a2 2 0 100 4m0-4a2 2 0 110 4m-6 8a2 2 0 100-4m0 4a2 2 0 110-4m0 4v2m0-6V4m6 6v10m6-2a2 2 0 100-4m0 4a2 2 0 110-4m0 4v2m0-6V4"></path></svg>{{ $t('search.filters') }}<svg class="w-[10px] h-[10px] text-[#050505] ml-0.5 mt-0.5" fill="currentColor" viewBox="0 0 16 16"><path d="M4 6h8l-4 5z"></path></svg>
            </button>

            <!-- Przycisk Kolumny -->
            <button class="bg-[#e4e6eb] hover:bg-[#d8dadf] text-[#050505] text-[15px] font-semibold px-3 py-2 rounded-lg flex items-center gap-1.5 transition-colors">
              <svg class="w-[18px] h-[18px]" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2.5" d="M9 17V7m0 10a2 2 0 01-2 2H5a2 2 0 01-2-2V7a2 2 0 012-2h2a2 2 0 012 2m0 10a2 2 0 002 2h2a2 2 0 002-2M9 7a2 2 0 012-2h2a2 2 0 012 2m0 10V7m0 10a2 2 0 002 2h2a2 2 0 002-2V7a2 2 0 00-2-2h-2a2 2 0 00-2 2"></path></svg>{{ $t('dashboard.kolumny') }}<svg class="w-[10px] h-[10px] text-[#050505] ml-0.5 mt-0.5" fill="currentColor" viewBox="0 0 16 16"><path d="M4 6h8l-4 5z"></path></svg>
            </button>

            <!-- Przycisk Data -->
            <button class="bg-[#e4e6eb] hover:bg-[#d8dadf] text-[#050505] text-[15px] font-semibold px-3 py-2 rounded-lg flex items-center gap-1.5 transition-colors">{{ $t('dashboard.ostatnie28Dni13') }}<svg class="w-[10px] h-[10px] text-[#050505] ml-0.5 mt-0.5" fill="currentColor" viewBox="0 0 16 16"><path d="M4 6h8l-4 5z"></path></svg>
            </button>

            <!-- Przycisk Eksportuj -->
            <button class="bg-[#e4e6eb] hover:bg-[#d8dadf] text-[#050505] text-[15px] font-semibold px-3 py-2 rounded-lg flex items-center gap-1.5 transition-colors">
              <svg class="w-[18px] h-[18px]" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2.5" d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-4l-4 4m0 0l-4-4m4 4V4"></path></svg>{{ $t('dashboard.eksportujDane') }}<svg class="w-[10px] h-[10px] text-[#050505] ml-0.5 mt-0.5" fill="currentColor" viewBox="0 0 16 16"><path d="M4 6h8l-4 5z"></path></svg>
            </button>

            <!-- 3 kropki (proste, bez tła) -->
            <button class="text-[#65676b] hover:bg-[#f0f2f5] p-1 rounded-md transition-colors ml-1">
              <svg class="w-5 h-5" fill="currentColor" viewBox="0 0 24 24">
                <circle cx="5" cy="12" r="1.5"></circle>
                <circle cx="12" cy="12" r="1.5"></circle>
                <circle cx="19" cy="12" r="1.5"></circle>
              </svg>
            </button>

          </div>
        </div>
      </div>

      <!-- NAGŁÓWKI TABELI -->
      <div class="border-y border-[#dadde1] bg-white">
        <div class="flex items-start px-2 py-3 text-[13px] text-[#050505]">

          <div class="flex items-center gap-4 w-[280px] pl-2 shrink-0 pt-1">
            <input type="checkbox" class="rounded-sm border-gray-400 w-[18px] h-[18px] cursor-pointer">
            <div class="flex items-center gap-1 cursor-pointer">{{ $t('chat.podglad') }}<svg class="w-3 h-3 text-[#65676b] shrink-0" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M7 15l5 5 5-5"></path><path d="M7 9l5-5 5 5"></path></svg>
            </div>
          </div>

          <div class="flex flex-1 justify-between items-start pr-4">
            <div class="flex items-start gap-1 w-[85px] cursor-pointer">
              <span class="leading-tight">{{ $t('dashboard.wyswietleni') }}<br>{{ $t('dashboard.a') }}</span>
              <svg class="w-[14px] h-[14px] text-[#65676b] shrink-0 mt-0.5" fill="currentColor" viewBox="0 0 20 20"><path fill-rule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7-4a1 1 0 11-2 0 1 1 0 012 0zM9 9a1 1 0 000 2v3a1 1 0 001 1h1a1 1 0 100-2v-3a1 1 0 00-1-1H9z" clip-rule="evenodd"></path></svg>
              <!-- Aktywna strzałka w dół -->
              <svg class="w-3 h-3 text-blue-600 shrink-0 mt-0.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3" stroke-linecap="round" stroke-linejoin="round"><path d="M7 10l5 5 5-5"></path></svg>
            </div>

            <div class="flex items-start gap-1 w-[80px] cursor-pointer">
              <span>{{ $t('createLive.viewers') }}</span>
              <svg class="w-[14px] h-[14px] text-[#65676b] shrink-0 mt-0.5" fill="currentColor" viewBox="0 0 20 20"><path fill-rule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7-4a1 1 0 11-2 0 1 1 0 012 0zM9 9a1 1 0 000 2v3a1 1 0 001 1h1a1 1 0 100-2v-3a1 1 0 00-1-1H9z" clip-rule="evenodd"></path></svg>
              <svg class="w-3 h-3 text-[#65676b] shrink-0 mt-0.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M7 15l5 5 5-5"></path><path d="M7 9l5-5 5 5"></path></svg>
            </div>

            <div class="flex items-start gap-1 w-[90px] cursor-pointer">
              <span>{{ $t('emojiPicker.categories.activity') }}</span>
              <svg class="w-[14px] h-[14px] text-[#65676b] shrink-0 mt-0.5" fill="currentColor" viewBox="0 0 20 20"><path fill-rule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7-4a1 1 0 11-2 0 1 1 0 012 0zM9 9a1 1 0 000 2v3a1 1 0 001 1h1a1 1 0 100-2v-3a1 1 0 00-1-1H9z" clip-rule="evenodd"></path></svg>
              <svg class="w-3 h-3 text-[#65676b] shrink-0 mt-0.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M7 15l5 5 5-5"></path><path d="M7 9l5-5 5 5"></path></svg>
            </div>

            <div class="flex items-start gap-1 w-[110px] cursor-pointer">
              <span class="leading-tight">{{ $t('dashboard.liczba') }}<br>{{ $t('dashboard.obserwatoro') }}<br>{{ $t('dashboard.wNetto') }}</span>
              <svg class="w-[14px] h-[14px] text-[#65676b] shrink-0 mt-0.5" fill="currentColor" viewBox="0 0 20 20"><path fill-rule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7-4a1 1 0 11-2 0 1 1 0 012 0zM9 9a1 1 0 000 2v3a1 1 0 001 1h1a1 1 0 100-2v-3a1 1 0 00-1-1H9z" clip-rule="evenodd"></path></svg>
              <svg class="w-3 h-3 text-[#65676b] shrink-0 mt-0.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M7 15l5 5 5-5"></path><path d="M7 9l5-5 5 5"></path></svg>
            </div>

            <div class="flex items-start gap-1 w-[85px] cursor-pointer">
              <span class="leading-tight">{{ $t('dashboard.wyswietleni') }}<br>{{ $t('dashboard.a') }}</span>
              <svg class="w-[14px] h-[14px] text-[#65676b] shrink-0 mt-0.5" fill="currentColor" viewBox="0 0 20 20"><path fill-rule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7-4a1 1 0 11-2 0 1 1 0 012 0zM9 9a1 1 0 000 2v3a1 1 0 001 1h1a1 1 0 100-2v-3a1 1 0 00-1-1H9z" clip-rule="evenodd"></path></svg>
              <svg class="w-3 h-3 text-[#65676b] shrink-0 mt-0.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M7 15l5 5 5-5"></path><path d="M7 9l5-5 5 5"></path></svg>
            </div>

            <div class="flex items-start gap-1 w-[100px] cursor-pointer">
              <span>{{ $t('createLive.comments') }}</span>
              <svg class="w-[14px] h-[14px] text-[#65676b] shrink-0 mt-0.5" fill="currentColor" viewBox="0 0 20 20"><path fill-rule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7-4a1 1 0 11-2 0 1 1 0 012 0zM9 9a1 1 0 000 2v3a1 1 0 001 1h1a1 1 0 100-2v-3a1 1 0 00-1-1H9z" clip-rule="evenodd"></path></svg>
              <svg class="w-3 h-3 text-[#65676b] shrink-0 mt-0.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M7 15l5 5 5-5"></path><path d="M7 9l5-5 5 5"></path></svg>
            </div>

            <div class="flex items-start gap-1 cursor-pointer">
              <span>{{ $t('dashboard.dystrybucja') }}</span>
              <svg class="w-[14px] h-[14px] text-[#65676b] shrink-0 mt-0.5" fill="currentColor" viewBox="0 0 20 20"><path fill-rule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7-4a1 1 0 11-2 0 1 1 0 012 0zM9 9a1 1 0 000 2v3a1 1 0 001 1h1a1 1 0 100-2v-3a1 1 0 00-1-1H9z" clip-rule="evenodd"></path></svg>
            </div>
          </div>

        </div>
      </div>

      <!-- Miejsce na dane -->
      <div class="h-40 bg-white rounded-b-lg"></div>

    </div>
  </div>
</template>

<script setup>
definePageMeta({
  layout: 'dashboard' // Nazwa pliku layoutu bez rozszerzenia .vue
})
</script>
