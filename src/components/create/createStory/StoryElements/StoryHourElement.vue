<script setup lang="ts">
import { ref } from 'vue'

// Możesz przekazać czas jako prop, domyślnie ustawi się na "8:50 PM"
const props = withDefaults(defineProps<{
  time?: string
}>(), {
  time: '8:50 PM'
})
</script>

<template>
  <!-- Główny kontener o kształcie pigułki (rounded-full) -->
  <div class="inline-flex items-center justify-center bg-white rounded-[32px] px-5 py-3 shadow-sm border border-gray-100">
    <div class="flex items-center gap-3">

      <!-- Pomarańczowa ikona zegara -->
      <svg
        class="w-[28px] h-[28px] text-[#DD7B22]"
        xmlns="http://www.w3.org/2000/svg"
        viewBox="0 0 24 24"
        fill="none"
        stroke="currentColor"
        stroke-width="2.2"
        stroke-linecap="round"
        stroke-linejoin="round"
      >
        <circle cx="12" cy="12" r="10"></circle>
     
        <polyline points="12 6 12 12 15 15"></polyline>
      </svg>

      <!-- Tekst godziny -->
      <span class="text-black text-[26px] font-normal tracking-wide leading-none mt-0.5">
        {{ time }}
      </span>

    </div>
  </div>
</template>
