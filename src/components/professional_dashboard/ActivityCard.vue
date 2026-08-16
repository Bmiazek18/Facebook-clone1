<template>
  <div class="bg-white rounded-[8px] shadow-sm p-4">
    <div class="flex justify-between items-start mb-4">
      <div>
        <h2 class="text-[20px] font-bold text-[#050505] leading-tight">Aktywność</h2>
        <p class="text-[14px] text-[#65676B] mt-1 max-w-[90%] leading-snug">Zarządzaj komentarzami, wzmiankami i znacznikami oraz odkrywaj nowe sposoby na kontaktowanie się z fanami.</p>
      </div>
      <a href="#" class="text-[#1877F2] text-[15px] font-semibold hover:underline shrink-0 mt-1">Wyświetl wszystko</a>
    </div>

    <div class="flex gap-4">
      <!-- Box 1 -->
      <div class="flex-1 bg-[#F0F2F5] rounded-lg p-3 flex flex-col justify-between">
        <svg viewBox="0 0 24 24" width="20" height="20" fill="#050505" class="mb-2">
          <path d="M1 21h4V9H1v12zm22-11c0-1.1-.9-2-2-2h-6.31l.95-4.57.03-.32c0-.41-.17-.79-.44-1.06L14.17 1 7.59 7.59C7.22 7.95 7 8.45 7 9v10c0 1.1.9 2 2 2h9c.83 0 1.54-.5 1.84-1.22l3.02-7.05c.09-.23.14-.47.14-.73v-2z"/>
        </svg>
        <div>
          <div class="font-bold text-[17px] text-[#050505]">0</div>
          <div class="text-[13px] text-[#65676B] flex items-center gap-1 font-medium">
            Aktywność
            <svg viewBox="0 0 24 24" width="14" height="14" fill="currentColor"><path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-6h2v6zm0-8h-2V7h2v2z"/></svg>
          </div>
        </div>
      </div>
      <!-- Box 2 -->
      <div class="flex-1 bg-[#F0F2F5] rounded-lg p-3 flex flex-col justify-between">
        <svg viewBox="0 0 24 24" width="20" height="20" fill="#050505" class="mb-2">
           <path d="M21.99 4c0-1.1-.89-2-1.99-2H4c-1.1 0-2 .9-2 2v12c0 1.1.9 2 2 2h14l4 4-.01-18zM18 14H6v-2h12v2zm0-3H6V9h12v2zm0-3H6V6h12v2z"/>
        </svg>
        <div>
          <div class="font-bold text-[17px] text-[#050505]">0</div>
          <div class="text-[13px] text-[#65676B]">Ostatnie komentarze</div>
        </div>
      </div>
    </div>
  </div>
</template>
