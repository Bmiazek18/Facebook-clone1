<script setup lang="ts">
import { useAuthStore } from '@/stores/auth'
import type { PageForm } from '@/types/pageCreation'

defineProps<{
  form: PageForm
}>()

defineEmits<{
  'prev-step': []
  'next-step': []
}>()

const authStore = useAuthStore()
</script>

<template>
  <div class="flex-1 overflow-y-auto px-4 py-2 custom-scrollbar flex flex-col">
    <div class="text-[13px] text-[#65676B] mb-1 font-semibold">
      Krok 4 z 5
    </div>

    <h1 class="text-[24px] font-bold leading-tight mb-2 text-[#050505]">
      Poszerz grupę odbiorców strony
    </h1>

    <p class="text-[15px] text-[#65676B] leading-snug mb-4">
      Rozwijaj stronę <span class="font-bold text-[#050505]">{{ form.pageName || 'Nazwa strony' }}</span>, zapraszając znajomych do nawiązania kontaktu z nią.
    </p>

    <div class="inline-flex items-center gap-1.5 bg-[#25823B] text-white px-2 py-1 rounded-[4px] text-[13px] font-semibold mb-10 w-fit">
      <svg viewBox="0 0 24 24" width="14" height="14" fill="currentColor">
        <path d="M12 2a10 10 0 1 0 10 10A10 10 0 0 0 12 2zm1 14.5h-2v-6h2zm0-8h-2v-2h2z"></path>
      </svg>
      Pomaga polepszyć wyniki reklam
    </div>

    <div class="flex flex-col items-center text-center mt-2 px-2">
      <!-- Ikona Grupy Ludzi -->
      <svg width="110" height="90" viewBox="0 0 100 90" fill="none" xmlns="http://www.w3.org/2000/svg" class="mb-6 mx-auto">
        <rect x="20" y="35" width="40" height="45" rx="8" fill="#A4A7AB" />
        <circle cx="40" cy="20" r="12" fill="#75A6FF" />
        <path d="M28 20 C 28 8, 52 8, 52 20 L 52 14 C 52 5, 28 5, 28 14 Z" fill="#050505" />
        <path d="M45 80 C 45 60, 95 60, 95 80 Z" fill="#5E6165" />
        <circle cx="70" cy="40" r="11" fill="#1877F2" />
        <circle cx="60" cy="32" r="6" fill="#A4A7AB" />
      </svg>

      <h3 class="text-[19px] font-bold text-[#050505] mb-2 leading-tight">
        Dodaj znajomych później, aby rozwijać swoją stronę.
      </h3>
      <p class="text-[15px] text-[#65676B] leading-snug">
        Aby zaprosić znajomych do połączenia ze stroną <span class="font-bold">{{ form.pageName || 'Nazwa strony' }}</span>, najpierw dodaj ich do swojego profilu <span class="font-bold">{{ authStore.originalUser?.name || 'Profil osobisty' }}</span>. Wypróbuj tę funkcję po skonfigurowaniu swojej strony.
        <a href="#" class="text-[#1877F2] font-semibold hover:underline">Dowiedz się więcej</a>
      </p>
    </div>
  </div>

  <div class="px-4 py-3 bg-white shrink-0 mt-auto border-t border-[#E5E5E5] shadow-[0_-2px_4px_rgba(0,0,0,0.05)]">
    <div class="text-[15px] text-[#050505] mb-1">
      Kondycja strony: <span class="font-bold">Przeciętna</span>
    </div>
    <div class="text-[15px] text-[#050505] leading-snug mb-3">
      W porównaniu z podobnymi stronami o dużej aktywności.
    </div>
    <div class="h-1.5 w-full bg-[#E5E5E5] rounded-full mb-4 flex overflow-hidden">
      <div class="bg-[#B58A14] w-[45%] h-full rounded-full"></div>
    </div>

    <div class="flex gap-2">
      <button
        @click="$emit('prev-step')"
        class="flex-1 py-2 rounded-md font-semibold text-[15px] bg-[#E4E6EB] text-[#050505] hover:bg-[#D8DADF] transition-colors"
      >
        Wstecz
      </button>
      <button
        @click="$emit('next-step')"
        class="flex-1 py-2 rounded-md font-semibold text-[15px] bg-[#E7F3FF] text-[#1877F2] hover:bg-[#DBE7F2] transition-colors"
      >
        Dalej
      </button>
    </div>
  </div>
</template>
