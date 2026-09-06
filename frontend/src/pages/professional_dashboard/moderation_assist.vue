<template>
  <div class="min-h-screen bg-[#F3F4F6] p-6 flex justify-center items-start font-sans">

    <!-- Główny kontener ograniczający szerokość -->
    <div class="w-full max-w-3xl flex flex-col gap-8">

      <!-- Karta 1: Asystent moderacji -->
      <div class="bg-white rounded-2xl shadow-sm border border-gray-200 px-8 py-10 flex flex-col items-center text-center">
        <!-- Złożona ikona Asystenta moderacji (gwiazdka/kwiat) -->
        <div class="relative w-14 h-14 mb-5">
          <svg viewBox="0 0 48 48" fill="none" xmlns="http://www.w3.org/2000/svg" class="w-full h-full drop-shadow-sm">
            <!-- Płatki w różnych kolorach -->
            <path d="M24 4 L30 16 L44 18 L32 28 L36 42 L24 34 L12 42 L16 28 L4 18 L18 16 Z" fill="#2563EB" />
            <circle cx="24" cy="10" r="6" fill="#0EA5E9" />
            <circle cx="36" cy="18" r="6" fill="#EF4444" />
            <circle cx="32" cy="32" r="6" fill="#8B5CF6" />
            <circle cx="16" cy="32" r="6" fill="#F97316" />
            <circle cx="12" cy="18" r="6" fill="#0EA5E9" />
            <!-- Centralna biała gwiazdka -->
            <path d="M24 16L25.8 21.6H31.7L26.9 25.1L28.8 30.7L24 27.2L19.2 30.7L21.1 25.1L16.3 21.6H22.2L24 16Z" fill="white" />
          </svg>
        </div>

        <h2 class="text-[22px] font-bold text-gray-900 mb-3 tracking-tight">{{ $t('dashboard.asystentModeracji') }}</h2>

        <p class="text-[15px] text-gray-500 leading-relaxed max-w-[600px]">{{ $t('dashboard.przypiszDzialaniaDoAsystenta') }}<span class="font-semibold text-gray-900">{{ $t('dashboard.pulpicieProfesjonalnym') }}</span>.
        </p>
      </div>

      <!-- Sekcja 2: Kryteria i lista -->
      <div>
        <!-- Nagłówek sekcji -->
        <div class="mb-5 px-1">
          <h3 class="text-xl font-bold text-gray-900 flex items-center">{{ $t('dashboard.dodajLubEdytujKryteria') }}<span class="text-gray-400 font-normal ml-2 text-lg">· 1</span>
          </h3>
          <p class="text-[15px] text-gray-500 mt-1">{{ $t('dashboard.zachowujeszKontroleDostosujKryteria') }}</p>
        </div>

        <!-- Karta 2: Zarządzanie komentarzami (Akordion) -->
        <div class="bg-white rounded-2xl shadow-sm border border-gray-200 p-5">

          <!-- Nagłówek akordionu -->
          <div class="flex items-center justify-between mb-4 cursor-pointer">
            <div class="flex items-center gap-3">
              <!-- Ikona dymku z gwiazdką -->
              <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" class="w-6 h-6 text-blue-600 fill-current">
                <path d="M12 2C6.477 2 2 6.03 2 11c0 2.84 1.48 5.38 3.79 7.04L5 22l3.85-2.05c1.02.29 2.1.45 3.15.45 5.523 0 10-4.03 10-9s-4.477-9-10-9zm0 13.5l-2.6 1.5 1-3-2.4-2h3.1l1-3 1 3h3.1l-2.4 2 1 3-2.6-1.5z" />
              </svg>
              <h4 class="text-[16px] font-semibold text-blue-600">{{ $t('dashboard.zarzadzajKomentarzami1') }}</h4>
            </div>
            <!-- Strzałka (zwiń) -->
            <button class="p-1 hover:bg-gray-100 rounded-full transition-colors">
              <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 text-gray-500" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                <path stroke-linecap="round" stroke-linejoin="round" d="M5 15l7-7 7 7" />
              </svg>
            </button>
          </div>

          <!-- Zawartość akordionu -->
          <div class="pl-1">
            <div class="flex flex-col sm:flex-row sm:items-start justify-between gap-4 mb-4">
              <p class="text-[16px] font-medium text-gray-900 leading-snug">{{ $t('dashboard.wPrzypadkuSpelnieniaDowolnego') }}</p>
              <button class="shrink-0 px-5 py-2 bg-blue-50 hover:bg-blue-100 text-blue-600 font-semibold rounded-lg text-sm transition-colors">{{ $t('call.dodaj') }}</button>
            </div>

            <!-- Pojedyncze kryterium -->
            <div class="bg-gray-50 rounded-xl p-4 flex items-center justify-between group hover:bg-gray-100 transition-colors cursor-pointer">
              <span class="text-[15px] text-gray-900">{{ $t('dashboard.autorNieMaZadnych') }}</span>
              <!-- Ikona edycji (ołówek) -->
              <button class="text-gray-400 group-hover:text-gray-600 transition-colors">
                <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" viewBox="0 0 20 20" fill="currentColor">
                  <path d="M13.586 3.586a2 2 0 112.828 2.828l-.793.793-2.828-2.828.793-.793zM11.379 5.793L3 14.172V17h2.828l8.38-8.379-2.83-2.828z" />
                </svg>
              </button>
            </div>
          </div>

        </div>
      </div>

    </div>
  </div>
</template>

<script setup>
definePageMeta({
  layout: 'dashboard'
})
</script>
