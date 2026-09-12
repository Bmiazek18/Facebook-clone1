<!-- src/components/FacebookSplash.vue -->
<template>
  <div
    id="static-facebook-splash"
    class="fixed inset-0 z-[999999] w-screen h-screen bg-white flex flex-col justify-between items-center py-8 select-none font-sans box-border"
  >
    <!-- Pusta przestrzeń do wyśrodkowania w flexboxie -->
    <div></div>

    <!-- Centrowane logo Facebooka -->
    <div class="flex items-center justify-center">
      <svg
        class="w-20 h-20 text-[#0866FF]"
        fill="currentColor"
        viewBox="0 0 24 24"
        aria-hidden="true"
      >
        <path
          d="M24 12.073c0-6.627-5.373-12-12-12s-12 5.373-12 12c0 5.99 4.388 10.954 10.125 11.854v-8.385H7.078v-3.47h3.047V9.43c0-3.007 1.792-4.669 4.533-4.669 1.312 0 2.686.235 2.686.235v2.953H15.83c-1.491 0-1.956.925-1.956 1.874v2.25h3.328l-.532 3.47h-2.796v8.385C19.612 23.027 24 18.062 24 12.073z"
        />
      </svg>
    </div>

    <!-- Stopka "from Meta" na dole -->
    <div class="flex flex-col items-center gap-1">
      <span class="text-xs text-[#65676B] tracking-tight">{{ $t('common.from') }}</span>
      <svg
        class="h-4 text-[#0866FF]"
        viewBox="0 0 100 20"
        fill="currentColor"
        aria-hidden="true"
      >
        <path
          d="M19.1 2.3C16.2 2.3 13.8 4.2 12.1 6.8 10.4 4.2 8 2.3 5.1 2.3 2.1 2.3 0 4.7 0 8.3v9.4h3.6V9c0-2 1.2-3.3 2.7-3.3 1.6 0 2.6 1.1 2.6 3v8.7h3.6V9c0-2 1.2-3.3 2.7-3.3 1.6 0 2.6 1.1 2.6 3v8.7h3.6V8.3c0-3.6-2.1-6-5.1-6z"
        />
      </svg>
    </div>
  </div>
</template>


