<template>
  <div>
    <h1 class="text-[24px] font-bold text-gray-900 mb-1 leading-tight">{{ $t('pages.odkrywanieStron') }}</h1>
    <h2 class="text-[20px] font-bold text-gray-900 mb-5 leading-tight">{{ $t('pages.propozycjeDlaCiebie') }}</h2>

    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
      <div
        v-for="page in suggestedPages"
        :key="page.id"
        class="bg-white rounded-[12px] shadow-sm border border-gray-200 overflow-hidden flex flex-col relative"
      >
        <!-- Przycisk Zamknij (X) -->
        <button class="absolute top-2 right-2 w-8 h-8 bg-black/40 hover:bg-black/60 rounded-full flex items-center justify-center text-white z-10 transition-colors">
          <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" viewBox="0 0 20 20" fill="currentColor">
            <path fill-rule="evenodd" d="M4.293 4.293a1 1 0 011.414 0L10 8.586l4.293-4.293a1 1 0 111.414 1.414L11.414 10l4.293 4.293a1 1 0 01-1.414 1.414L10 11.414l-4.293 4.293a1 1 0 01-1.414-1.414L8.586 10 4.293 5.707a1 1 0 010-1.414z" clip-rule="evenodd" />
          </svg>
        </button>

        <!-- Tło (Cover) -->
        <div class="h-[140px] w-full bg-gray-200 shrink-0">
          <img :src="page.coverUrl" :alt="page.name" class="w-full h-full object-cover" />
        </div>

        <!-- Treść karty -->
        <div class="p-4 flex flex-col flex-1">
          <div class="flex gap-3 mb-4">
            <img :src="page.avatarUrl" :alt="page.name" class="w-12 h-12 rounded-full border border-gray-200 object-cover shrink-0 bg-white" />
            <div class="flex flex-col overflow-hidden">
              <span class="font-bold text-[16px] text-gray-900 truncate">{{ page.name }}</span>
              <span class="text-[13px] text-gray-500 truncate mt-0.5">{{ page.category }}</span>
              <span class="text-[13px] text-gray-500 leading-snug mt-1 line-clamp-2">{{ page.mutualInfo }}</span>
            </div>
          </div>

          <!-- Przycisk Obserwuj -->
          <div class="mt-auto">
            <button class="w-full bg-[#0866FF] hover:bg-blue-700 text-white font-semibold py-1.5 px-4 rounded-md flex items-center justify-center gap-2 transition-colors text-[15px]">
              <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4" viewBox="0 0 20 20" fill="currentColor">
                <path fill-rule="evenodd" d="M10 5a1 1 0 011 1v3h3a1 1 0 110 2h-3v3a1 1 0 11-2 0v-3H6a1 1 0 110-2h3V6a1 1 0 011-1z" clip-rule="evenodd" />
              </svg>{{ $t('feed.obserwuj2') }}</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
const suggestedPages = [
  { id: 1, name: 'AS Roma', category: 'Profesjonalna drużyna sportowa', mutualInfo: 'Michał Nowosz i 12 innych obserwują tę stronę', coverUrl: 'https://picsum.photos/seed/roma_cover/400/200', avatarUrl: 'https://picsum.photos/seed/roma_avatar/100/100' },
  { id: 2, name: 'OficjalneZero', category: 'Kanał', mutualInfo: 'Paweł Sitkowski i 14 innych obserwują tę stronę', coverUrl: 'https://picsum.photos/seed/zero_cover/400/200', avatarUrl: 'https://picsum.photos/seed/zero_avatar/100/100' },
  { id: 3, name: 'TVP Sport', category: 'Kanał telewizyjny', mutualInfo: 'Paweł Sitkowski i 70 innych obserwują tę stronę', coverUrl: 'https://picsum.photos/seed/tvp_cover/400/200', avatarUrl: 'https://picsum.photos/seed/tvp_avatar/100/100' },
  { id: 4, name: 'Krosno112.pl', category: 'Lokalna firma', mutualInfo: '119,367 użytkowników obserwuje tę stronę', coverUrl: 'https://picsum.photos/seed/krosno_cover/400/200', avatarUrl: 'https://picsum.photos/seed/krosno_avatar/100/100' },
  { id: 5, name: 'Polonia Warszawa S.A.', category: 'Klub sportowy', mutualInfo: 'Dariusz Blacha i 6 innych obserwują tę stronę', coverUrl: 'https://picsum.photos/seed/polonia_cover/400/200', avatarUrl: 'https://picsum.photos/seed/polonia_avatar/100/100' },
  { id: 6, name: 'KSZO 1929 Ostrowiec Świętokrzyski', category: 'Klub sportowy', mutualInfo: 'Michał Nowosz i 7 innych obserwują tę stronę', coverUrl: 'https://picsum.photos/seed/kszo_cover/400/200', avatarUrl: 'https://picsum.photos/seed/kszo_avatar/100/100' }
]
</script>
