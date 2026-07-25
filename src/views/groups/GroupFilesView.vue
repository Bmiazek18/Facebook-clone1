<template>
  <div class="min-h-screen bg-theme-bg-secondary text-theme-text p-6  ">
    <div class="flex justify-between items-center mb-6">
      <h1 class="text-xl font-bold text-theme-text">Pliki</h1>
      <div class="flex items-center space-x-4">
        <div class="relative">
          <span class="absolute inset-y-0 left-3 flex items-center text-theme-text-secondary">
            <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path
                d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
              />
            </svg>
          </span>
          <input
            type="text"
            placeholder="Szukaj plików"
            class="bg-theme-bg-subtle border-none rounded-full py-1.5 pl-10 pr-4 text-sm w-72 focus:ring-1 focus:ring-blue-500 outline-none text-theme-text placeholder-theme-text-secondary"
          />
        </div>
        <button class="text-theme-primary font-medium hover:underline text-sm">
          Prześlij plik
        </button>
      </div>
    </div>

    <div
      class="grid grid-cols-12 gap-4 px-4 py-2 bg-theme-bg-subtle-opacity-50 rounded-md text-[11px] font-bold text-theme-text-secondary uppercase tracking-wider mb-2"
    >
      <div class="col-span-6">Nazwa pliku</div>
      <div class="col-span-2 text-center">Typ</div>
      <div class="col-span-3">Ostatnia modyfikacja</div>
      <div class="col-span-1"></div>
    </div>

    <div class="space-y-[2px]">
      <div
        v-for="(file, index) in files"
        :key="index"
        class="grid grid-cols-12 gap-4 px-4 py-3 hover:bg-theme-hover transition-colors rounded-md items-center group cursor-pointer"
      >
        <div class="col-span-6 flex items-center space-x-3">
          <div class="w-8 h-8 flex-shrink-0 flex items-center justify-center">
            <img
              v-if="file.type === 'PDF'"
              src="https://upload.wikimedia.org/wikipedia/commons/8/87/PDF_file_icon.svg"
              class="w-6 h-6"
            />
            <img
              v-else
              src="https://upload.wikimedia.org/wikipedia/commons/7/73/Microsoft_Excel_2013-2019_logo.svg"
              class="w-6 h-6"
            />
          </div>
          <span
            class="text-[14px] font-medium truncate group-hover:text-theme-primary transition-colors"
          >
            {{ file.name }}
          </span>
        </div>

        <div class="col-span-2 text-[13px] text-theme-text-secondary text-center">
          {{ file.type === 'PDF' ? 'PDF' : 'Arkusz kalkulacyjny' }}
        </div>

        <div class="col-span-3 text-[13px] text-theme-text-secondary">
          <div class="text-theme-text-opacity-90">{{ file.date }}</div>
          <div class="text-[11px] text-theme-text-secondary">
            przez: <span class="hover:underline text-theme-text-secondary">{{ file.author }}</span>
          </div>
        </div>

        <div class="col-span-1 text-right opacity-0 group-hover:opacity-100 transition-opacity">
          <button class="p-2 hover:bg-theme-hover-strong rounded-full text-theme-text-secondary">
            <svg class="w-5 h-5" fill="currentColor" viewBox="0 0 20 20">
              <path
                d="M6 10a2 2 0 11-4 0 2 2 0 014 0zM12 10a2 2 0 11-4 0 2 2 0 014 0zM16 12a2 2 0 100-4 2 2 0 000 4z"
              />
            </svg>
          </button>
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
