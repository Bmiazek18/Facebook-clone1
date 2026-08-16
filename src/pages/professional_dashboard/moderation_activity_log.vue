<template>
  <div class="min-h-screen bg-[#F0F2F5] flex flex-col font-sans">

    <!-- Sekcja Nagłówka -->
    <div class="bg-white px-6 py-5 border-b border-gray-200">
      <div class="max-w-[1200px] mx-auto">
        <h1 class="text-[24px] font-bold text-black mb-1">Dziennik aktywności</h1>
        <p class="text-[15px] text-[#65676B] mb-4">
          Zobacz działania moderacyjne podjęte przez Asystenta moderacji i administratorów strony.
        </p>

        <!-- Pasek narzędzi / Filtry -->
        <div class="flex flex-wrap items-center gap-3">

          <!-- Przycisk: Cały zakres -->
          <button class="flex items-center gap-2 bg-[#E4E6EB] hover:bg-[#D8DADF] transition-colors px-3 py-2 rounded-md text-[14px] font-semibold text-black">
            <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
              <path stroke-linecap="round" stroke-linejoin="round" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
            </svg>
            Cały zakres
            <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 text-gray-600 ml-1" viewBox="0 0 20 20" fill="currentColor">
              <path fill-rule="evenodd" d="M5.293 7.293a1 1 0 011.414 0L10 10.586l3.293-3.293a1 1 0 111.414 1.414l-4 4a1 1 0 01-1.414 0l-4-4a1 1 0 010-1.414z" clip-rule="evenodd" />
            </svg>
          </button>

          <!-- Przycisk: Cała aktywność -->
          <button class="flex items-center gap-2 bg-[#E4E6EB] hover:bg-[#D8DADF] transition-colors px-3 py-2 rounded-md text-[14px] font-semibold text-black">
            <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4" viewBox="0 0 24 24" fill="currentColor">
              <path d="M12 1L3 5v6c0 5.55 3.84 10.74 9 12 5.16-1.26 9-6.45 9-12V5l-9-4zm0 10.99h7c-.53 4.12-3.28 7.79-7 8.94V12H5V6.3l7-3.11v8.8z" opacity="0.3"/>
              <path d="M12 1L3 5v6c0 5.55 3.84 10.74 9 12 5.16-1.26 9-6.45 9-12V5l-9-4zm-1 15.5l-3-3 1.41-1.41L11 13.67l4.59-4.58L17 10.5l-6 6z"/>
            </svg>
            Cała aktywność
            <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 text-gray-600 ml-1" viewBox="0 0 20 20" fill="currentColor">
              <path fill-rule="evenodd" d="M5.293 7.293a1 1 0 011.414 0L10 10.586l3.293-3.293a1 1 0 111.414 1.414l-4 4a1 1 0 01-1.414 0l-4-4a1 1 0 010-1.414z" clip-rule="evenodd" />
            </svg>
          </button>

          <!-- Przycisk: Wszystkie kryteria -->
          <button class="flex items-center gap-2 bg-[#E4E6EB] hover:bg-[#D8DADF] transition-colors px-3 py-2 rounded-md text-[14px] font-semibold text-black">
            <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4" viewBox="0 0 20 20" fill="currentColor">
              <path fill-rule="evenodd" d="M3 6a3 3 0 013-3h10a1 1 0 01.8 1.6L14.25 8l2.55 3.4A1 1 0 0116 13H6a1 1 0 00-1 1v3a1 1 0 11-2 0V6z" clip-rule="evenodd" />
            </svg>
            Wszystkie kryteria
            <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 text-gray-600 ml-1" viewBox="0 0 20 20" fill="currentColor">
              <path fill-rule="evenodd" d="M5.293 7.293a1 1 0 011.414 0L10 10.586l3.293-3.293a1 1 0 111.414 1.414l-4 4a1 1 0 01-1.414 0l-4-4a1 1 0 010-1.414z" clip-rule="evenodd" />
            </svg>
          </button>

          <!-- Pole wyszukiwania -->
          <div class="flex items-center gap-2 bg-[#F0F2F5] px-4 py-2 rounded-full min-w-[240px]">
            <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 text-gray-500" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
              <path stroke-linecap="round" stroke-linejoin="round" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
            </svg>
            <input
              type="text"
              placeholder="Szukaj komentatora"
              class="bg-transparent border-none outline-none text-[15px] text-black placeholder-[#65676B] w-full"
            />
          </div>

        </div>
      </div>
    </div>

    <!-- Pusty stan / Zawartość -->
    <div class="flex-1 flex flex-col items-center justify-center p-8 text-center">

      <!-- Ilustracja wektorowa (Człowiek z teleskopem) -->
      <svg class="w-40 h-40 mb-2" viewBox="0 0 120 120" fill="none" xmlns="http://www.w3.org/2000/svg">
        <!-- Statyw -->
        <path d="M55 70 L45 95" stroke="#374151" stroke-width="4" stroke-linecap="round"/>
        <path d="M55 70 L65 95" stroke="#374151" stroke-width="4" stroke-linecap="round"/>
        <!-- Teleskop -->
        <polygon points="35,65 65,35 75,45 45,75" fill="#9CA3AF" />
        <polygon points="30,60 35,65 45,55 40,50" fill="#111827" />
        <circle cx="35" cy="65" r="4" fill="#111827" />
        <!-- Człowiek (Ciało) -->
        <path d="M68 35 L80 40 L85 60 L70 55 Z" fill="#9CA3AF" />
        <!-- Koszula -->
        <path d="M65 30 L80 40 L80 50 L60 45 Z" fill="#3B82F6" />
        <!-- Nogi -->
        <path d="M75 55 L70 75 L78 75 L82 55 Z" fill="#0055FF" />
        <!-- Twarz / Włosy -->
        <circle cx="68" cy="28" r="6" fill="#111827" />
        <path d="M65 25 Q70 20 73 25" stroke="#111827" stroke-width="3" stroke-linecap="round"/>
        <!-- Ręce -->
        <path d="M70 40 L55 50" stroke="#3B82F6" stroke-width="4" stroke-linecap="round"/>
      </svg>

      <!-- Komunikat -->
      <h2 class="text-[20px] font-bold text-[#65676B] mb-1">
        Brak aktywności administracyjnej
      </h2>
      <p class="text-[16px] text-[#65676B] max-w-[460px] leading-snug">
        W zakresie objętym wybranymi filtrami nie ma żadnej aktywności. Zmień filtry wyszukiwania i spróbuj ponownie.
      </p>
    </div>

  </div>
</template>

<script setup>
definePageMeta({
  layout: 'dashboard'
})
</script>


