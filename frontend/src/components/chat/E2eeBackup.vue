<script setup lang="ts">
import { ref } from 'vue'

const pin = ref('')

const emit = defineEmits(['close', 'use-one-time-code', 'pin-complete'])

function handleClose() {
  emit('close')
}

function handleAlternativeMethod() {
  emit('use-one-time-code')
}
</script>

<template>
  <div class="max-w-[380px] w-full bg-[#F9F9FB] rounded-[16px] p-4 relative font-sans shadow-sm border border-gray-100">

    <!-- Przycisk zamknięcia (X) -->
    <button
      @click="handleClose"
      class="absolute top-3 right-3 w-7 h-7 bg-[#E5E5EA] hover:bg-[#D1D1D6] rounded-full flex items-center justify-center text-black transition-colors"
      aria-label="Zamknij"
    >
      <svg xmlns="http://www.w3.org/2000/svg" class="h-3.5 w-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2.5">
        <path stroke-linecap="round" stroke-linejoin="round" d="M6 18L18 6M6 6l12 12" />
      </svg>
    </button>

    <!-- Nagłówek i opis z ikoną -->
    <div class="flex items-start gap-3 mb-4 pr-8">
      <!-- Ikona (niebieski okrąg z wykrzyknikiem) -->
      <div class="flex-shrink-0 w-7 h-7 bg-[#1A73E8] rounded-full flex items-center justify-center text-white font-bold text-base mt-0.5">
        !
      </div>

      <!-- Tekst -->
      <div>
        <h2 class="text-base font-bold text-black leading-tight mb-1">
          Brak historii czatu
        </h2>
        <p class="text-[13px] text-[#1F1F1F] leading-snug">
          Wprowadź kod PIN, aby przywrócić historię czatu.
        </p>
      </div>
    </div>

    <!-- Sześciosegmentowe wprowadzanie PIN-u -->
    <div class="relative w-max mx-auto mb-4">
      <!-- Niewidoczny input przechwytujący znaki z klawiatury -->
      <input
        v-model="pin"
        type="tel"
        maxlength="6"
        class="absolute inset-0 w-full h-full opacity-0 z-10 cursor-text"
      />

      <!-- Wizualne kafelki -->
      <div class="flex gap-1.5">
        <div
          v-for="i in 6"
          :key="i"
          class="w-[38px] h-[46px] rounded-lg flex items-center justify-center text-xl font-bold transition-all"
          :class="[
            pin.length === i - 1
              ? 'border-[2px] border-[#1A73E8] bg-[#EAEBEF]'
              : 'border-[2px] border-transparent bg-[#EAEBEF]'
          ]"
        >
          <span v-if="pin[i - 1]" class="text-black">•</span>
          <!-- Kreseczka minusa ze zrzutu ekranu, lekko pogrubiona -->
          <span v-else class="text-[#5F6368] text-lg font-black mb-1">-</span>
        </div>
      </div>
    </div>

    <!-- Dolny przycisk z linkiem -->
    <div class="text-center pb-1">
      <button
        @click="handleAlternativeMethod"
        class="text-[#1A73E8] font-semibold text-[13px] hover:underline"
      >
        Zamiast tego użyj kodu jednorazowego
      </button>
    </div>

  </div>
</template>
