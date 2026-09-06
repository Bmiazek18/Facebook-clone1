<template>
  <div>
    <!-- Zmiany dotyczące stron -->
    <div class="bg-white rounded-[12px] shadow-sm border border-gray-200 p-5 mb-5">
      <h2 class="text-[20px] font-bold text-gray-900 mb-1">{{ $t('pages.zmianyDotyczaceStron') }}</h2>
      <p class="text-[15px] text-gray-500 mb-5 leading-snug">{{ $t('pages.nazwyTychStronZmienily') }}</p>

      <div class="grid grid-cols-1 md:grid-cols-2 gap-4 mb-4">
        <div v-for="page in changedPages" :key="page.id" class="border border-gray-200 rounded-[12px] p-4 flex gap-3 shadow-sm">
          <img :src="page.avatar" :alt="page.name" class="w-16 h-16 rounded-full border border-gray-100 shrink-0" />
          <div class="flex flex-col">
            <span class="font-bold text-[15px] text-gray-900 leading-tight">{{ page.name }}</span>
            <span class="text-[13px] text-gray-500 mt-1 mb-2">{{ page.category }}</span>
            <span class="text-[13px] text-gray-900">{{ $t('pages.poprzedniaNazwa') }}<b>{{ page.oldName }}</b>
            </span>
          </div>
        </div>
      </div>

      <button class="w-full bg-[#E4E6EB] hover:bg-[#D8DADF] text-gray-900 font-semibold text-[15px] py-2 rounded-lg transition-colors flex justify-center items-center gap-1">{{ $t('pages.zobaczWszystkie34') }}<svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4" viewBox="0 0 20 20" fill="currentColor">
          <path fill-rule="evenodd" d="M5.293 7.293a1 1 0 011.414 0L10 10.586l3.293-3.293a1 1 0 111.414 1.414l-4 4a1 1 0 01-1.414 0l-4-4a1 1 0 010-1.414z" clip-rule="evenodd" />
        </svg>
      </button>
    </div>

    <!-- Wszystkie obserwowane strony -->
    <div class="bg-white rounded-[12px] shadow-sm border border-gray-200 p-5">
      <div class="flex justify-between items-center mb-5">
        <h2 class="text-[20px] font-bold text-gray-900">{{ $t('pages.allPagesYouFollow') }}</h2>
        <button class="text-[#0866FF] hover:bg-gray-100 px-3 py-1.5 rounded-md text-[15px] font-medium transition-colors">{{ $t('pages.sortuj') }}</button>
      </div>

      <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
        <div v-for="page in followedPages" :key="page.id" class="border border-gray-200 rounded-[12px] p-4 flex flex-col shadow-sm">
          <div class="flex gap-3 mb-4 items-center">
            <img :src="page.avatar" :alt="page.name" class="w-[60px] h-[60px] rounded-full border border-gray-100 shrink-0" />
            <div class="flex flex-col">
              <span class="font-bold text-[15px] text-gray-900">{{ page.name }}</span>
              <span class="text-[13px] text-gray-500">{{ page.category }}</span>
            </div>
          </div>
          <button class="w-full bg-[#E7F3FF] hover:bg-[#DBE7F2] text-[#0866FF] font-semibold text-[15px] py-1.5 rounded-lg flex justify-center items-center gap-2 transition-colors">
            <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4" viewBox="0 0 20 20" fill="currentColor">
              <path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd" />
            </svg>{{ $t('pages.obserwujesz') }}</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
const changedPages = [
  { id: 1, name: 'DAF ŁUKÓW Dziecięca Akademia Futbolu', category: 'Sport', oldName: 'Dziecięca Akademia Futbolu DAF Łuków + 2 innych', avatar: 'https://picsum.photos/seed/daf/100/100' },
  { id: 2, name: 'Amplus Łuków - ŁSR Akademia Młodego Piłkarza', category: 'Organizacja non profit', oldName: 'Amplus Łuków - ŁSR Akademia Młodego Piłkarza + 3 innych', avatar: 'https://picsum.photos/seed/amplus/100/100' },
]

const followedPages = [
  { id: 1, name: 'Pasut', category: 'Twórca rolki', avatar: 'https://picsum.photos/seed/pasut/100/100' },
  { id: 2, name: 'Wisłoka Dębica', category: 'Zainteresowanie', avatar: 'https://picsum.photos/seed/wisloka/100/100' },
  { id: 3, name: 'Gradient PG', category: 'Nauka, technologia i inżynieria', avatar: 'https://picsum.photos/seed/gradient/100/100' },
  { id: 4, name: 'Liga Przyszłych Mistrzów', category: 'Liga sportowa', avatar: 'https://picsum.photos/seed/liga/100/100' },
]
</script>
