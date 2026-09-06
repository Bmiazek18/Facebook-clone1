<template>
  <div class="bg-white rounded-[8px] shadow-sm p-4">
    <h2 class="text-[17px] font-bold text-[#050505] mb-1">{{ $t('dashboard.opinia') }}</h2>
    <div class="flex gap-2 items-start mt-2">
      <svg viewBox="0 0 24 24" width="16" height="16" fill="#65676B" class="shrink-0 mt-0.5">
        <path d="M21.99 4c0-1.1-.89-2-1.99-2H4c-1.1 0-2 .9-2 2v12c0 1.1.9 2 2 2h14l4 4-.01-18z"/>
      </svg>
      <div>
        <div class="font-semibold text-[15px] text-[#050505] leading-snug mb-1">{{ $t('dashboard.chceszPomocWUlepszaniu') }}</div>
        <div class="text-[13px] text-[#65676B] leading-snug mb-4">{{ $t('dashboard.podajSzczegoloweInformacjeO') }}</div>
      </div>
    </div>
    <button class="w-full bg-[#E4E6EB] hover:bg-[#D8DADF] text-[#050505] font-semibold text-[15px] py-2 rounded-md transition-colors">{{ $t('groups.rozpocznij') }}</button>
  </div>
</template>
