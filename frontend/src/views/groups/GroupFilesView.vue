<template>
  <div class="min-h-screen bg-transparent p-4 flex justify-center">
    <!-- Główny kontener (karta) -->
    <div class="w-full max-w-[1000px] bg-white dark:bg-[#242526] rounded-lg shadow-sm p-4">

      <!-- Nagłówek (Tytuł, Wyszukiwarka, Przycisk) -->
      <div class="flex justify-between items-center mb-4">
        <h1 class="text-[20px] font-bold text-[#050505] dark:text-[#E4E6EB]">{{ $t('groups.files') }}</h1>

        <div class="flex items-center gap-4">
          <!-- Wyszukiwarka -->
          <div class="relative flex items-center bg-[#F0F2F5] dark:bg-[#3A3B3C] rounded-full px-3 py-1.5 h-9 w-[240px]">
            <svg class="w-4 h-4 text-[#65676B] dark:text-[#B0B3B8]" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
            </svg>
            <input
              type="text"
              :placeholder="$t('groups.szukajPlikow')"
              class="bg-transparent border-none outline-none w-full ml-2 text-[15px] text-[#050505] dark:text-[#E4E6EB] placeholder-[#65676B] dark:placeholder-[#B0B3B8]"
            />
          </div>

          <!-- Przycisk przesyłania -->
          <button class="text-[#0866FF] dark:text-[#2D88FF] font-semibold text-[15px] hover:underline px-2 py-1 rounded-md transition-colors hover:bg-blue-50 dark:hover:bg-blue-900/20">{{ $t('groups.przeslijPlik') }}</button>
        </div>
      </div>

      <!-- Delikatna linia oddzielająca -->
      <hr class="mb-4 border-[#CED0D4] dark:border-[#3E4042]" />

      <!-- Nagłówki tabeli (z szarymi tłami) -->
      <!-- Używamy Grid, by zachować idealne proporcje z wierszami -->
      <div class="grid grid-cols-[minmax(0,1fr)_150px_200px_40px] gap-3 mb-2 px-2">
        <div class="bg-[#F0F2F5] dark:bg-[#3A3B3C] rounded-md py-2 flex items-center justify-center text-[13px] font-bold text-black dark:text-[#B0B3B8] tracking-wide">{{ $t('groups.nazwaPliku') }}</div>
        <div class="bg-[#F0F2F5] dark:bg-[#3A3B3C] rounded-md py-2 flex items-center justify-center text-[13px] font-bold text-black dark:text-[#B0B3B8] tracking-wide">{{ $t('groups.typ') }}</div>
        <div class="bg-[#F0F2F5] dark:bg-[#3A3B3C] rounded-md py-2 flex items-center justify-center gap-1 text-[13px] font-bold text-[#050505] dark:text-[#E4E6EB] tracking-wide cursor-pointer">{{ $t('groups.ostatniaModyfikac') }}<svg class="w-4 h-4 fill-currentColor" viewBox="0 0 24 24">
            <path d="M16.59 8.59L12 13.17 7.41 8.59 6 10l6 6 6-6z" />
          </svg>
        </div>
        <div><!-- Puste miejsce nad przyciskami opcji --></div>
      </div>

      <!-- Lista plików -->
      <div class="flex flex-col">
        <div
          v-for="(file, index) in files"
          :key="index"
          class="grid grid-cols-[minmax(0,1fr)_150px_200px_40px] gap-3 items-center px-2 py-2 hover:bg-[#F2F2F2] dark:hover:bg-[#3A3B3C] transition-colors rounded-lg group cursor-pointer"
        >
          <!-- Kolumna: Ikona + Nazwa -->
          <div class="flex items-center space-x-3 overflow-hidden">
            <div class="w-8 h-8 flex-shrink-0 flex items-center justify-center rounded-md bg-white border border-gray-200 shadow-sm p-1">
              <img
                v-if="file.type === 'PDF'"
                src="https://upload.wikimedia.org/wikipedia/commons/8/87/PDF_file_icon.svg"
                class="w-full h-full object-contain"
                :alt="$t('metaAi.pdf')"
              />
              <img
                v-else
                src="https://upload.wikimedia.org/wikipedia/commons/7/73/Microsoft_Excel_2013-2019_logo.svg"
                class="w-full h-full object-contain"
                :alt="$t('groups.excel')"
              />
            </div>
            <span class="text-[15px] font-bold text-[#050505] dark:text-[#E4E6EB] truncate hover:underline">
              {{ file.name }}
            </span>
          </div>

          <!-- Kolumna: Typ -->
          <div class="text-[13px] text-[#050505] dark:text-[#E4E6EB] pl-4">
            {{ file.type === 'PDF' ? 'PDF' : 'Arkusz kalkulacyjny' }}
          </div>

          <!-- Kolumna: Ostatnia modyfikacja -->
          <div class="flex flex-col pl-4">
            <div class="text-[13px] text-[#050505] dark:text-[#E4E6EB] leading-tight">
              {{ file.date }}
            </div>
            <div class="text-[13px] text-[#65676B] dark:text-[#B0B3B8] leading-tight mt-0.5">{{ $t('groups.przez') }}<span class="hover:underline">{{ file.author }}</span>
            </div>
          </div>

          <!-- Kolumna: Opcje (...) -->
          <div class="flex justify-end">
            <button class="w-9 h-9 bg-[#E4E6EB] dark:bg-[#3A3B3C] hover:bg-[#D8DADF] dark:hover:bg-[#4E4F50] rounded-md flex items-center justify-center text-[#050505] dark:text-[#E4E6EB] transition-colors">
              <svg class="w-5 h-5 fill-currentColor" viewBox="0 0 24 24">
                <path d="M6 10c-1.1 0-2 .9-2 2s.9 2 2 2 2-.9 2-2-.9-2-2-2zm12 0c-1.1 0-2 .9-2 2s.9 2 2 2 2-.9 2-2-.9-2-2-2zm-6 0c-1.1 0-2 .9-2 2s.9 2 2 2 2-.9 2-2-.9-2-2-2z" />
              </svg>
            </button>
          </div>
        </div>
      </div>

    </div>
  </div>
</template>

<script setup>
const files = [
  {
    name: 'OBSADA_18_15-22.11.2025_2.pdf',
    type: 'PDF',
    date: '13 listopada 2025 20:55',
    author: 'Paweł Sitkowski',
  },
  {
    name: 'Klasyfikacja sędziów na sezon 2025_2026.xlsx',
    type: 'XLSX',
    date: '21 czerwca 2025 23:13',
    author: 'Maciej Łasocha',
  },
  {
    name: 'OBSADA NR 15 - 15.06.-16.06.2024.xlsx',
    type: 'XLSX',
    date: '14 czerwca 2024 19:37',
    author: 'Artur Krasowski',
  },
  {
    name: 'OBSADA NR 15 - 15.06.-16.06.2024-w.2.xlsx',
    type: 'XLSX',
    date: '13 czerwca 2024 16:58',
    author: 'Artur Krasowski',
  },
  {
    name: 'OBSADA NR 15 - 15.06.-16.06.2024-w.1.xlsx',
    type: 'XLSX',
    date: '12 czerwca 2024 18:17',
    author: 'Artur Krasowski',
  },
  {
    name: 'OBSADA NR 14 - 08-09.06.2024.xlsx',
    type: 'XLSX',
    date: '8 czerwca 2024 10:08',
    author: 'Artur Krasowski',
  },
  {
    name: 'OBSADA NR 14 - 08-09.06.2024.xlsx',
    type: 'XLSX',
    date: '7 czerwca 2024 19:42',
    author: 'Artur Krasowski',
  },
  {
    name: 'OBSADA NR 14 - 08-09.06.2024 - w.2.xlsx',
    type: 'XLSX',
    date: '6 czerwca 2024 20:31',
    author: 'Artur Krasowski',
  },
  {
    name: 'OBSADA NR 14 - 08-09.06.2024 - w.1.xlsx',
    type: 'XLSX',
    date: '5 czerwca 2024 17:24',
    author: 'Artur Krasowski',
  },
  {
    name: 'Regulamin_rozgrywek_2025.pdf',
    type: 'PDF',
    date: '1 stycznia 2025 09:00',
    author: 'Zarząd',
  },
]
</script>
