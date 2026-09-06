<script setup lang="ts">
import type { PageForm } from '@/types/pageCreation'

defineProps<{
  form: PageForm
  isSubmitting: boolean
}>()

defineEmits<{
  'prev-step': []
  'finish': []
}>()
</script>

<template>
  <div class="flex-1 overflow-y-auto px-4 py-2 custom-scrollbar flex flex-col">
    <div class="text-[13px] text-[#65676B] mb-1 font-semibold">{{ $t('pages.krok5Z5') }}</div>

    <h1 class="text-[24px] font-bold leading-tight mb-2 text-[#050505]">{{ $t('pages.badzNaBiezacoZe') }}</h1>

    <p class="text-[15px] text-[#65676B] leading-snug mb-6">{{ $t('pages.wlaczTeFunkcjeAby') }}<span class="font-bold text-[#050505]">{{ form.pageName || 'Nazwa strony' }}</span>{{ $t('pages.mozeszJeZmienicW') }}</p>

    <!-- Powiadomienia dotyczące strony -->
    <div class="flex items-start justify-between gap-3 mb-6">
      <div class="w-9 h-9 rounded-full bg-[#E4E6EB] flex items-center justify-center shrink-0 mt-0.5 text-[#050505]">
        <svg viewBox="0 0 24 24" width="20" height="20" fill="currentColor">
          <path d="M12 22c1.1 0 2-.9 2-2h-4c0 1.1.9 2 2 2zm6-6v-5c0-3.07-1.63-5.64-4.5-6.32V4c0-.83-.67-1.5-1.5-1.5s-1.5.67-1.5 1.5v.68C7.64 5.36 6 7.92 6 11v5l-2 2v1h16v-1l-2-2z"/>
        </svg>
      </div>
      <div class="flex-1 pr-2">
        <h3 class="text-[15px] font-bold text-[#050505] leading-snug mb-1">{{ $t('pages.powiadomieniaDotyczaceStronyW') }}</h3>
        <p class="text-[13px] text-[#65676B] leading-snug">{{ $t('pages.badzNaBiezacoZe2') }}<span class="font-bold text-[#050505]">{{ form.pageName || 'Nazwa strony' }}</span>{{ $t('pages.gdyPrzelaczyszSieNa') }}</p>
      </div>
      <div class="shrink-0 pt-1">
        <label class="relative inline-flex items-center cursor-pointer">
          <input type="checkbox" v-model="form.pageNotifications" class="sr-only peer">
          <div class="w-11 h-6 bg-[#8A8D91] peer-focus:outline-none rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-[#1877F2]"></div>
        </label>
      </div>
    </div>

    <!-- Marketingowe i promocyjne e-maile -->
    <div class="flex items-start justify-between gap-3 mb-6">
      <div class="w-9 h-9 rounded-full bg-[#E4E6EB] flex items-center justify-center shrink-0 mt-0.5 text-[#050505]">
        <svg viewBox="0 0 24 24" width="20" height="20" fill="currentColor">
          <path d="M20 4H4c-1.1 0-1.99.9-1.99 2L2 18c0 1.1.9 2 2 2h16c1.1 0 2-.9 2-2V6c0-1.1-.9-2-2-2zm0 4l-8 5-8-5V6l8 5 8-5v2z"/>
        </svg>
      </div>
      <div class="flex-1 pr-2">
        <h3 class="text-[15px] font-bold text-[#050505] leading-snug mb-1">{{ $t('pages.marketingoweIPromocyjneE') }}</h3>
        <p class="text-[13px] text-[#65676B] leading-snug">{{ $t('pages.dowiedzSieWiecejO2') }}<span class="font-bold text-[#050505]">{{ form.pageName || 'Nazwa strony' }}</span>{{ $t('pages.mozeLatwiejOdniescSukces') }}</p>
      </div>
      <div class="shrink-0 pt-1">
        <label class="relative inline-flex items-center cursor-pointer">
          <input type="checkbox" v-model="form.promotionalEmails" class="sr-only peer">
          <div class="w-11 h-6 bg-[#8A8D91] peer-focus:outline-none rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-[#1877F2]"></div>
        </label>
      </div>
    </div>
  </div>

  <div class="px-4 py-3 bg-white shrink-0 mt-auto border-t border-[#E5E5E5] shadow-[0_-2px_4px_rgba(0,0,0,0.05)]">
    <div class="text-[15px] text-[#050505] mb-1">{{ $t('pages.kondycjaStrony') }}<span class="font-bold">{{ $t('pages.przecietna') }}</span>
    </div>
    <div class="text-[15px] text-[#050505] leading-snug mb-3">{{ $t('pages.wPorownaniuZPodobnymi') }}</div>
    <div class="h-1.5 w-full bg-[#E5E5E5] rounded-full mb-4 flex overflow-hidden">
      <div class="bg-[#B58A14] w-[55%] h-full rounded-full"></div>
    </div>

    <div class="flex gap-2">
      <button
        @click="$emit('prev-step')"
        class="flex-1 py-2 rounded-md font-semibold text-[15px] bg-[#E4E6EB] text-[#050505] hover:bg-[#D8DADF] transition-colors"
      >{{ $t('createLive.back') }}</button>
      <button
        @click="$emit('finish')"
        :disabled="isSubmitting"
        class="flex-1 py-2 rounded-md font-semibold text-[15px] bg-[#1877F2] text-white hover:bg-[#166FE5] transition-colors flex items-center justify-center gap-2"
        :class="{ 'opacity-70 cursor-not-allowed': isSubmitting }"
      >
        <svg v-if="isSubmitting" class="animate-spin h-4 w-4 text-white" fill="none" viewBox="0 0 24 24">
          <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
          <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8v8H4z"></path>
        </svg>
        <span>{{ isSubmitting ? 'Tworzenie...' : 'Gotowe' }}</span>
      </button>
    </div>
  </div>
</template>
