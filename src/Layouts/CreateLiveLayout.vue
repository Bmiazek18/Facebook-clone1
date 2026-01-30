<script setup lang="ts">
import { ref, computed } from 'vue';
import { useI18n } from 'vue-i18n';
import { useRoute, useRouter } from 'vue-router';

// ... (IMPORTY IKON POZOSTAJĄ BEZ ZMIAN) ...
import VideoIcon from 'vue-material-design-icons/Video.vue';
import EarthIcon from 'vue-material-design-icons/Earth.vue';
import ViewDashboardIcon from 'vue-material-design-icons/ViewDashboard.vue';
import CogIcon from 'vue-material-design-icons/Cog.vue';
import StarIcon from 'vue-material-design-icons/Star.vue';
import MessageAlertOutlineIcon from 'vue-material-design-icons/MessageAlertOutline.vue';
import TabletDashboardIcon from 'vue-material-design-icons/TabletDashboard.vue';
import HelpCircleOutlineIcon from 'vue-material-design-icons/HelpCircleOutline.vue';
import CheckCircleIcon from 'vue-material-design-icons/CheckCircle.vue';
import ChevronDownIcon from 'vue-material-design-icons/ChevronDown.vue';
import LockIcon from 'vue-material-design-icons/Lock.vue';

const { t } = useI18n();
const route = useRoute();
const router = useRouter();

const isCreateLiveActive = computed(() => route.name === 'createLive');
const isDashboardActive = computed(() => route.name === 'liveDashboard');
</script>
<template>
    <div class="flex h-screen bg-[#F0F2F5] mt-[56px] text-[#050505] font-sans overflow-hidden antialiased leading-normal">

      <aside class="w-[360px] bg-white shadow-[1px_0_0_0_#CED0D4] flex flex-col h-full overflow-y-auto shrink-0 z-20 custom-scrollbar">

        <div class="px-4 pt-5 pb-2">
          <div class="flex justify-between items-start mb-4">
            <h1 class="text-[24px] font-bold leading-tight tracking-tight text-[#050505]">
              Utwórz transmisję<br>wideo na żywo
            </h1>
            <div class="flex items-center gap-2">
              <div class="w-9 h-9 hover:bg-[#F2F2F2] rounded-full flex items-center justify-center cursor-pointer transition-colors text-[#606770]">
                 <HelpCircleOutlineIcon :size="24" />
              </div>
              <div class="w-9 h-9 hover:bg-[#F2F2F2] rounded-full flex items-center justify-center cursor-pointer transition-colors text-[#606770]">
                 <TabletDashboardIcon :size="20" /> </div>
            </div>
          </div>

          <div class="flex items-center gap-3 mb-6">
              <div class="h-2 flex-1 bg-[#F0F2F5] rounded-full overflow-hidden">
                  <div class="h-full bg-[#1877F2] w-[33%] rounded-full"></div>
              </div>
              <span class="text-[#1877F2] font-semibold text-[15px]">1/3</span>
          </div>

          <div class="space-y-5 mb-6">
              <div class="flex items-start gap-3 cursor-pointer group">
                   <div class="w-6 h-6 rounded-full border-[2px] border-[#65676B] shrink-0 mt-0.5 group-hover:border-[#050505]"></div>
                   <span class="text-[15px] font-medium text-[#050505] leading-snug">Połącz źródło wideo</span>
              </div>

              <div class="flex items-start gap-3 cursor-pointer">
                   <CheckCircleIcon class="text-[#31A24C] shrink-0 mt-0.5" :size="24" />
                   <span class="text-[15px] font-medium text-[#050505] leading-snug">Uzupełnij szczegółowe informacje dotyczące posta</span>
              </div>

              <div class="flex items-start gap-3 cursor-pointer group">
                   <div class="w-6 h-6 rounded-full border-[2px] border-[#65676B] shrink-0 mt-0.5 group-hover:border-[#050505]"></div>
                   <span class="text-[15px] font-medium text-[#050505] leading-snug">Rozpocznij transmisję na żywo</span>
              </div>
          </div>

          <hr class="border-[#CED0D4] mb-5">

          <div class="flex items-center gap-3 mb-5">
              <img src="https://i.pravatar.cc/150?img=11" alt="Avatar" class="w-10 h-10 rounded-full border border-[#CED0D4]">
              <div class="leading-snug">
                <div class="font-semibold text-[15px] text-[#050505]">Bartosz Miazek</div>
                <div class="text-[13px] text-[#65676B]">Organizator — Twój profil</div>
              </div>
          </div>

          <div class="space-y-3 mb-3">
              <div class="border border-[#CED0D4] rounded-lg px-3 py-2 cursor-pointer hover:bg-[#F2F2F2] transition-colors flex justify-between items-center bg-white relative">
                  <div>
                      <div class="text-[11px] text-[#65676B] mb-0.5">Wskaż docelową lokalizację posta</div>
                      <div class="font-medium text-[15px] text-[#050505]">Opublikuj w profilu</div>
                  </div>
                  <ChevronDownIcon :size="20" class="text-[#050505]" />
              </div>

              <div class="border border-[#CED0D4] rounded-lg px-3 py-2 cursor-pointer hover:bg-[#F2F2F2] transition-colors flex justify-between items-center bg-white relative">
                  <div>
                      <div class="text-[11px] text-[#65676B] mb-0.5">Kiedy rozpoczniesz transmisję na żywo?</div>
                      <div class="font-medium text-[15px] text-[#050505]">Teraz</div>
                  </div>
                  <ChevronDownIcon :size="20" class="text-[#050505]" />
              </div>
          </div>

          <div class="inline-flex items-center px-3 py-1.5 bg-[#E4E6EB] hover:bg-[#D8DADF] rounded-md text-[15px] font-semibold text-[#050505] cursor-pointer transition-colors mb-4 w-max">
              <LockIcon :size="14" class="mr-2 text-[#65676B]" />
              Tylko ja
          </div>
        </div>

                  <nav class="flex-1 overflow-y-auto px-2 custom-scrollbar">
                  <ul class="flex flex-col space-y-1">
                      <li
                        class="flex items-center px-2 py-2 rounded-lg cursor-pointer transition-colors"
                        :class="{ 'bg-[#E7F3FF]': isCreateLiveActive, 'hover:bg-[#F2F2F2]': !isCreateLiveActive }"
                        @click="router.push({ name: 'createLive' })"
                      >
                          <div
                            class="w-10 h-10 flex items-center justify-center rounded-full mr-3 shrink-0"
                            :class="isCreateLiveActive ? 'bg-[#1877F2] text-white' : 'bg-[#E4E6EB] text-[#050505] group-hover:bg-[#D8DADF]'"
                          >
                              <VideoIcon :size="20" />
                          </div>
                          <span
                            class="font-semibold text-[15px]"
                            :class="isCreateLiveActive ? 'text-[#1877F2]' : 'text-[#050505]'"
                          >Konfiguracja transmisji</span>
                      </li>
        
                      <li
                        class="flex items-center px-2 py-2 rounded-lg cursor-pointer transition-colors"
                        :class="{ 'bg-[#E7F3FF]': isDashboardActive, 'hover:bg-[#F2F2F2]': !isDashboardActive }"
                        @click="router.push({ name: 'liveDashboard' })"
                      >
                          <div
                            class="w-10 h-10 flex items-center justify-center rounded-full mr-3 shrink-0"
                            :class="isDashboardActive ? 'bg-[#1877F2] text-white' : 'bg-[#E4E6EB] text-[#050505] group-hover:bg-[#D8DADF]'"
                          >
                              <ViewDashboardIcon :size="20" /> </div>
                          <span
                            class="font-semibold text-[15px]"
                            :class="isDashboardActive ? 'text-[#1877F2]' : 'text-[#050505]'"
                          >Pulpit</span>
                      </li>
        
                      <li class="flex items-center justify-between px-2 py-2 rounded-lg cursor-pointer hover:bg-[#F2F2F2] transition-colors group">
                          <div class="flex items-center">
                              <div class="w-10 h-10 flex items-center justify-center rounded-full bg-[#E4E6EB] text-[#050505] mr-3 shrink-0 group-hover:bg-[#D8DADF]">
                                  <CogIcon :size="20" />
                              </div>
                              <span class="font-semibold text-[15px] text-[#050505]">Ustawienia</span>
                          </div>
                          <ChevronDownIcon :size="20" class="text-[#65676B]" />
                      </li>
        
                      <li class="flex items-center justify-between px-2 py-2 rounded-lg cursor-pointer hover:bg-[#F2F2F2] transition-colors group">
                          <div class="flex items-center">
                              <div class="w-10 h-10 flex items-center justify-center rounded-full bg-[#E4E6EB] text-[#050505] mr-3 shrink-0 group-hover:bg-[#D8DADF]">
                                  <StarIcon :size="20" />
                              </div>
                              <span class="font-semibold text-[15px] text-[#050505]">Interaktywność</span>
                          </div>
                          <ChevronDownIcon :size="20" class="text-[#65676B]" />
                      </li>
        
                      <li class="flex items-center px-2 py-2 rounded-lg cursor-pointer hover:bg-[#F2F2F2] transition-colors group">
                          <div class="w-10 h-10 flex items-center justify-center rounded-full bg-[#E4E6EB] text-[#050505] mr-3 shrink-0 group-hover:bg-[#D8DADF]">
                              <MessageAlertOutlineIcon :size="20" />
                          </div>
                          <span class="font-semibold text-[15px] text-[#050505]">Przekaż opinię</span>
                      </li>
                  </ul>
                </nav>
        <div class="p-4 border-t border-[#CED0D4] flex items-center gap-3 bg-white z-30 mt-auto">
           <button class="px-5 py-2.5 bg-[#E4E6EB] hover:bg-[#D8DADF] text-[#050505] rounded-md font-semibold text-[15px] transition-colors">Wstecz</button>
           <button class="flex-1 px-4 py-2.5 bg-[#E4E6EB] text-[#BCC0C4] rounded-md font-semibold text-[15px] cursor-not-allowed text-center truncate">Rozpocznij transmisję na żywo</button>
        </div>
      </aside>

      <main class="flex-1 overflow-hidden relative">
        <router-view />
      </main>
    </div>
  </template>


  <style scoped>
  /* Stylizacja scrollbarów, aby pasowały do designu */
  .custom-scrollbar::-webkit-scrollbar { width: 8px; height: 8px; }
  .custom-scrollbar::-webkit-scrollbar-thumb { background: #BCC0C4; border-radius: 4px; border: 2px solid transparent; background-clip: content-box; }
  .custom-scrollbar::-webkit-scrollbar-thumb:hover { background-color: #A8ABAF; }
  .custom-scrollbar::-webkit-scrollbar-track { background: transparent; }
  </style>
