<script setup lang="ts">
import { computed } from 'vue'
import type { PageForm, ViewMode } from '@/types/pageCreation'

const props = defineProps<{
  form: PageForm
  viewMode: ViewMode
}>()

const emit = defineEmits<{
  'update:viewMode': [mode: ViewMode]
}>()

const previewTitle = computed(() =>
  props.viewMode === 'mobile' ? 'Podgląd na urządzeniu mobilnym' : 'Podgląd na komputerze',
)
</script>

<template>
  <main class="flex-1 flex flex-col bg-[#F0F2F5] items-center overflow-hidden p-4 mt-[70px]">
    <div
      :class="[
        'bg-white rounded-lg shadow-[0_1px_2px_rgba(0,0,0,0.1)] flex flex-col overflow-hidden transition-all duration-300 ease-in-out w-full h-full border border-[#CED0D4]',
        viewMode === 'mobile' ? 'max-w-[460px]' : 'max-w-[965px]',
      ]"
    >
      <!-- Nagłówek podglądu -->
      <div class="flex items-center justify-between px-5 py-3 bg-white shrink-0">
        <span class="font-bold text-[#050505] text-[15px]">{{ previewTitle }}</span>

        <div class="flex gap-2 text-[#65676B]">
          <button
            @click="emit('update:viewMode', 'desktop')"
            :class="[
              'w-8 h-8 rounded-full flex items-center justify-center transition-colors',
              viewMode === 'desktop' ? 'text-[#1877F2] bg-[#E7F3FF]' : 'hover:bg-[#F0F2F5]',
            ]"
          >
            <svg class="w-[18px] h-[18px]" fill="none" stroke="currentColor" stroke-width="2.5" viewBox="0 0 24 24" stroke-linecap="round" stroke-linejoin="round">
              <rect x="2" y="3" width="20" height="14" rx="2" ry="2"></rect>
              <line x1="8" y1="21" x2="16" y2="21"></line>
              <line x1="12" y1="17" x2="12" y2="21"></line>
            </svg>
          </button>

          <button
            @click="emit('update:viewMode', 'mobile')"
            :class="[
              'w-8 h-8 rounded-full flex items-center justify-center transition-colors',
              viewMode === 'mobile' ? 'text-[#1877F2] bg-[#E7F3FF]' : 'hover:bg-[#F0F2F5]',
            ]"
          >
            <svg class="w-[18px] h-[18px]" fill="none" stroke="currentColor" stroke-width="2.5" viewBox="0 0 24 24" stroke-linecap="round" stroke-linejoin="round">
              <rect x="5" y="2" width="14" height="20" rx="2" ry="2"></rect>
              <line x1="12" y1="18" x2="12.01" y2="18"></line>
            </svg>
          </button>
        </div>
      </div>

      <!-- Obszar przewijany podglądu -->
      <div class="flex-1 overflow-y-auto custom-scrollbar p-4 flex justify-center">
        <!-- PODGLĄD DESKTOP -->
        <template v-if="viewMode === 'desktop'">
          <div class="border border-[#CED0D4] rounded-[8px] w-full flex flex-col overflow-hidden bg-white shadow-sm h-fit">
            <div class="relative w-full rounded-t-[8px]">
              <div class="h-[350px] bg-gradient-to-b from-[#F5F6F8] to-[#EBEDF0] w-full rounded-t-[8px] overflow-hidden">
                <img v-if="form.coverImage" :src="form.coverImage" class="w-full h-full object-cover" />
              </div>
              <div class="absolute left-1/2 -bottom-[14px] transform -translate-x-1/2 z-10">
                <div class="w-[168px] h-[168px] rounded-full border-[4px] border-white bg-[#D8DADF] flex items-end justify-center overflow-hidden">
                  <img v-if="form.profileImage" :src="form.profileImage" class="w-full h-full object-cover" />
                  <svg v-else viewBox="0 0 24 24" class="w-[140px] h-[140px] fill-white translate-y-3">
                    <path d="M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z"/>
                  </svg>
                </div>
              </div>
            </div>

            <!-- Tytuł strony i Biogram -->
            <div class="pt-[16px] px-4 relative bg-white flex flex-col items-center text-center">
              <h1
                class="text-[32px] font-bold"
                :class="form.pageName ? 'text-[#050505]' : 'text-[#BCC0C4]'"
              >
                {{ form.pageName || 'Nazwa strony' }}
              </h1>
              <p v-if="form.bio" class="text-[17px] pb-2 text-[#050505] max-w-[500px] break-words">
                {{ form.bio }}
              </p>
            </div>

            <div class="px-8 bg-white">
              <div class="h-px bg-[#CED0D4] w-full mb-1"></div>
            </div>

            <!-- Menu nawigacyjne -->
            <div class="px-4 flex items-center justify-between py-1 bg-white mb-2">
              <div class="flex items-center text-[15px] font-semibold text-[#65676B]">
                <div class="py-3 px-4 cursor-pointer rounded-md">Posty</div>
                <div class="py-3 px-4 cursor-pointer rounded-md">Informacje</div>
                <div class="py-3 px-4 cursor-pointer rounded-md">Obserwujący</div>
                <div class="py-3 px-4 flex items-center gap-1.5 cursor-pointer rounded-md">
                  Więcej
                  <svg viewBox="0 0 20 20" width="16" height="16" fill="currentColor">
                    <path d="M10 14a1 1 0 01-.755-.34l-5-5.5a1 1 0 011.51-1.32L10 11.528l4.245-4.668a1 1 0 011.51 1.32l-5 5.5A1 1 0 0110 14z"></path>
                  </svg>
                </div>
              </div>
              <div class="flex gap-2">
                <button class="bg-[#E4E6EB] text-[#BCC0C4] px-3.5 py-[7px] rounded-md font-semibold text-[15px] flex items-center gap-1.5 cursor-not-allowed">
                  <svg viewBox="0 0 24 24" width="16" height="16" fill="currentColor"><path d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"/></svg>
                  Obserwuj
                </button>
                <button class="bg-[#E4E6EB] text-[#BCC0C4] px-3.5 py-[7px] rounded-md font-semibold text-[15px] flex items-center gap-1.5 cursor-not-allowed">
                  <svg viewBox="0 0 24 24" width="16" height="16" fill="currentColor"><path d="M20 2H4c-1.1 0-2 .9-2 2v18l4-4h14c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2z"/></svg>
                  Wyślij wiadomość
                </button>
                <button class="bg-[#E4E6EB] text-[#BCC0C4] w-[44px] py-[7px] rounded-md font-semibold text-[15px] flex items-center justify-center cursor-not-allowed tracking-widest">
                  •••
                </button>
              </div>
            </div>

            <!-- Sekcja dolna -->
            <div class="bg-[#F0F2F5] p-4 flex gap-4 w-full border-t border-[#CED0D4]">
              <div class="w-[40%] bg-white rounded-[8px] shadow-sm border border-[#CED0D4] p-4 h-max">
                <h2 class="font-bold text-[20px] text-[#050505] mb-4">Prezentacja</h2>
                <div class="flex items-center gap-3 mb-4">
                  <div class="w-[20px] h-[20px] flex justify-center items-center shrink-0">
                    <svg viewBox="0 0 24 24" width="20" height="20" fill="#65676B">
                      <path d="M19 6h-3V5c0-1.65-1.35-3-3-3h-2C9.35 2 8 3.35 8 5v1H5c-1.1 0-2 .9-2 2v10c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V8c0-1.1-.9-2-2-2zm-9-1c0-.55.45-1 1-1h2c.55 0 1 .45 1 1v1h-4V5zm11 13H3V8h18v10z"></path>
                    </svg>
                  </div>
                  <span class="text-[#050505] font-semibold text-[15px] leading-tight">0 obserwujący</span>
                </div>
                <div class="flex items-start gap-3">
                  <div class="w-[20px] h-[20px] flex justify-center items-center shrink-0 mt-0.5">
                    <svg viewBox="0 0 24 24" width="20" height="20" fill="#65676B">
                      <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-6h2v6zm0-8h-2V7h2v2z"></path>
                    </svg>
                  </div>
                  <span class="text-[#050505] font-semibold text-[15px] leading-tight">
                    <span class="font-bold">Strona</span> · <span class="font-normal">{{ form.category || 'Kategoria' }}</span>
                  </span>
                </div>
              </div>

              <div class="w-[60%] flex gap-4 flex-col">
                <div class="bg-white rounded-[8px] shadow-sm border border-[#CED0D4] p-4 flex items-center justify-between">
                  <h2 class="font-bold text-[20px] text-[#050505]">Posty</h2>
                  <button class="bg-[#E4E6EB] text-[#050505] px-3.5 py-1.5 rounded-md font-semibold text-[15px] flex items-center gap-2 hover:bg-[#D8DADF] transition-colors">
                    <svg viewBox="0 0 24 24" width="16" height="16" fill="currentColor">
                      <path d="M10 18h4v-2h-4v2zM3 6v2h18V6H3zm3 7h12v-2H6v2z"></path>
                    </svg>
                    Filtry
                  </button>
                </div>
              </div>
            </div>
          </div>
        </template>

        <!-- PODGLĄD MOBILE -->
        <template v-else>
          <div class="border border-[#CED0D4] rounded-[8px] w-full max-w-[420px] flex flex-col overflow-hidden bg-white shadow-sm h-fit">
            <div class="relative w-full">
              <div class="h-[200px] bg-gradient-to-b from-[#F5F6F8] to-[#EBEDF0] w-full overflow-hidden">
                <img v-if="form.coverImage" :src="form.coverImage" class="w-full h-full object-cover" />
              </div>
              <div class="absolute left-1/2 -bottom-[66px] transform -translate-x-1/2 z-10">
                <div class="w-[132px] h-[132px] rounded-full border-[4px] border-white bg-[#D8DADF] flex items-end justify-center overflow-hidden shadow-sm">
                  <img v-if="form.profileImage" :src="form.profileImage" class="w-full h-full object-cover" />
                  <svg v-else viewBox="0 0 24 24" class="w-[110px] h-[110px] fill-white translate-y-2">
                    <path d="M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z"/>
                  </svg>
                </div>
              </div>
            </div>

            <!-- Tytuł strony i Biogram -->
            <div class="pt-[80px] pb-4 px-4 bg-white text-center flex flex-col items-center">
              <h1
                class="text-[28px] font-bold leading-tight"
                :class="form.pageName ? 'text-[#050505]' : 'text-[#BCC0C4]'"
              >
                {{ form.pageName || 'Nazwa strony' }}
              </h1>
              <p v-if="form.bio" class="text-[15px] text-[#050505] mt-1 text-center break-words w-full">
                {{ form.bio }}
              </p>
            </div>

            <div class="px-4 bg-white">
              <div class="h-px bg-[#CED0D4] w-full mb-3"></div>
            </div>

            <!-- Przyciski akcji (Mobile) -->
            <div class="px-4 flex items-center justify-between pb-4 bg-white gap-2">
              <div class="text-[15px] font-semibold text-[#65676B] flex items-center gap-1 cursor-pointer hover:bg-[#F0F2F5] px-2 py-1.5 rounded-md">
                Więcej
                <svg viewBox="0 0 20 20" width="16" height="16" fill="currentColor">
                  <path d="M10 14a1 1 0 01-.755-.34l-5-5.5a1 1 0 011.51-1.32L10 11.528l4.245-4.668a1 1 0 011.51 1.32l-5 5.5A1 1 0 0110 14z"></path>
                </svg>
              </div>
              <div class="flex gap-1.5">
                <button class="bg-[#E4E6EB] text-[#BCC0C4] px-3 py-1.5 rounded-md font-semibold text-[14px] flex items-center gap-1.5 cursor-not-allowed">
                  <svg viewBox="0 0 24 24" width="16" height="16" fill="currentColor"><path d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"/></svg>
                  Obserwuj
                </button>
                <button class="bg-[#E4E6EB] text-[#BCC0C4] px-3 py-1.5 rounded-md font-semibold text-[14px] flex items-center gap-1.5 cursor-not-allowed">
                  <svg viewBox="0 0 24 24" width="16" height="16" fill="currentColor"><path d="M20 2H4c-1.1 0-2 .9-2 2v18l4-4h14c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2z"/></svg>
                  Wyślij wiadomość
                </button>
                <button class="bg-[#E4E6EB] text-[#BCC0C4] px-3 py-1.5 rounded-md font-semibold text-[14px] flex items-center justify-center cursor-not-allowed">
                  •••
                </button>
              </div>
            </div>

            <!-- Sekcja zawartości -->
            <div class="bg-[#F0F2F5] p-4 flex flex-col gap-4 border-t border-[#CED0D4]">
              <div class="bg-white rounded-[8px] p-4 border border-[#CED0D4] shadow-sm">
                <h3 class="font-bold text-[20px] mb-4 text-[#050505]">Prezentacja</h3>
                <div class="flex flex-col gap-4">
                  <div class="flex items-center gap-3">
                    <div class="w-5 h-5 flex justify-center items-center shrink-0 text-[#65676B]">
                      <svg viewBox="0 0 24 24" width="20" height="20" fill="currentColor"><path d="M19 6h-3V5c0-1.65-1.35-3-3-3h-2C9.35 2 8 3.35 8 5v1H5c-1.1 0-2 .9-2 2v10c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V8c0-1.1-.9-2-2-2zm-9-1c0-.55.45-1 1-1h2c.55 0 1 .45 1 1v1h-4V5zm11 13H3V8h18v10z"></path></svg>
                    </div>
                    <span class="text-[#050505] font-semibold text-[15px]">0 obserwujący</span>
                  </div>
                  <div class="flex items-start gap-3">
                    <div class="w-5 h-5 flex justify-center items-center shrink-0 text-[#65676B] mt-0.5">
                      <svg viewBox="0 0 24 24" width="20" height="20" fill="currentColor"><path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-6h2v6zm0-8h-2V7h2v2z"></path></svg>
                    </div>
                    <span class="text-[#050505] font-semibold text-[15px]">
                      <span class="font-bold">Strona</span> <span class="font-normal text-[#65676B]">· {{ form.category || 'Kategoria' }}</span>
                    </span>
                  </div>
                </div>
              </div>

              <div class="bg-white rounded-[8px] p-4 border border-[#CED0D4] shadow-sm flex items-center justify-between">
                <h3 class="font-bold text-[20px] text-[#050505]">Posty</h3>
                <button class="bg-[#E4E6EB] text-[#050505] px-3.5 py-1.5 rounded-md font-semibold text-[15px] flex items-center gap-2 hover:bg-[#D8DADF] transition-colors">
                  <svg viewBox="0 0 24 24" width="16" height="16" fill="currentColor"><path d="M10 18h4v-2h-4v2zM3 6v2h18V6H3zm3 7h12v-2H6v2z"></path></svg>
                  Filtry
                </button>
              </div>
            </div>
          </div>
        </template>
      </div>
    </div>
  </main>
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
