<script setup lang="ts">
import { ref } from 'vue'

const emit = defineEmits(['close', 'save'])

const days = [
  'poniedziałek',
  'wtorek',
  'środa',
  'czwartek',
  'piątek',
  'sobota',
  'niedziela'
]

const handleClose = () => {
  emit('close')
}

const handleSave = () => {
  emit('save')
}
</script>

<template>
  <!-- Overlay (tło modala) -->
  <div class="fixed inset-0 bg-white/50 z-50 flex items-center justify-center p-4">
    <!-- Kontener modala -->
    <div class="bg-white rounded-xl shadow-[0_2px_12px_rgba(0,0,0,0.2)] w-full max-w-[540px] flex flex-col font-sans">

      <!-- Nagłówek -->
      <div class="relative h-[60px] flex items-center justify-center border-b border-[#E5E5E5] shrink-0">
        <h2 class="text-[20px] font-bold text-[#050505]">{{ $t('pages.wybraneGodziny') }}</h2>
        <button
          @click="handleClose"
          class="absolute right-4 w-9 h-9 bg-[#E4E6EB] hover:bg-[#D8DADF] rounded-full flex items-center justify-center text-[#65676B] transition-colors"
        >
          <svg viewBox="0 0 24 24" width="20" height="20" fill="currentColor">
            <path d="M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z"></path>
          </svg>
        </button>
      </div>

      <!-- Lista dni (scrollowana zawartość) -->
      <div class="p-4 flex flex-col gap-3 overflow-y-auto max-h-[60vh] custom-scrollbar">
        <div
          v-for="day in days"
          :key="day"
          class="flex items-center gap-2"
        >
          <!-- Nazwa dnia -->
          <div class="w-[110px] text-[15px] font-bold text-[#050505] shrink-0">
            {{ day }}
          </div>

          <!-- Przycisk Otwarcie -->
          <button class="flex-1 h-[44px] border border-[#CED0D4] rounded-lg flex items-center px-3 hover:bg-[#F0F2F5] transition-colors group">
            <svg class="w-5 h-5 text-[#65676B] mr-2" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
              <circle cx="12" cy="12" r="10"></circle>
              <path stroke-linecap="round" stroke-linejoin="round" d="M12 6v6l4 2"></path>
            </svg>
            <span class="text-[15px] text-[#65676B]">{{ $t('pages.otwarcie') }}</span>
          </button>

          <!-- Separator -->
          <span class="text-[#050505] font-medium px-1">-</span>

          <!-- Przycisk Zamknięcie -->
          <button class="flex-1 h-[44px] border border-[#CED0D4] rounded-lg flex items-center px-3 hover:bg-[#F0F2F5] transition-colors group">
            <svg class="w-5 h-5 text-[#65676B] mr-2" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
              <circle cx="12" cy="12" r="10"></circle>
              <path stroke-linecap="round" stroke-linejoin="round" d="M12 6v6l4 2"></path>
            </svg>
            <span class="text-[15px] text-[#65676B]">{{ $t('pages.zamkniecie') }}</span>
          </button>

          <!-- Przycisk Plus -->
          <button class="w-[44px] h-[44px] bg-[#E7F3FF] hover:bg-[#DBE7F2] rounded-lg flex items-center justify-center shrink-0 transition-colors">
            <svg viewBox="0 0 24 24" width="20" height="20" fill="#1877F2">
              <path d="M19 11h-6V5h-2v6H5v2h6v6h2v-6h6z"></path>
            </svg>
          </button>
        </div>
      </div>

      <!-- Stopka -->
      <div class="p-4 border-t border-[#E5E5E5] flex justify-end items-center gap-4 shrink-0">
        <button
          @click="handleClose"
          class="text-[#1877F2] font-semibold text-[15px] hover:underline"
        >{{ $t('common.cancel') }}</button>
        <button
          @click="handleSave"
          class="bg-[#0866FF] hover:bg-[#0753D8] text-white font-semibold text-[15px] px-8 py-2 rounded-md transition-colors"
        >{{ $t('createLive.save') }}</button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.custom-scrollbar::-webkit-scrollbar {
  width: 8px;
}
.custom-scrollbar::-webkit-scrollbar-track {
  background: transparent;
}
.custom-scrollbar::-webkit-scrollbar-thumb {
  background: #bcc0c4;
  border-radius: 4px;
}
.custom-scrollbar::-webkit-scrollbar-thumb:hover {
  background: #8d949e;
}
</style>
