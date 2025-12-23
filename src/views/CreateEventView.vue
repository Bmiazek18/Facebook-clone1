<script setup lang="ts">
import { reactive, computed, ref } from 'vue';

// --- TYPY I STAN ---
type ViewMode = 'desktop' | 'mobile';

interface EventForm {
  eventName: string;
  startDate: string;
  startTime: string;
  privacy: string;
  description: string;
}

const viewMode = ref<ViewMode>('mobile');
const usePastEvent = ref(false); // Stan przełącznika "Wypełnij przy użyciu danych..."

const form = reactive<EventForm>({
  eventName: '',
  startDate: '2025-12-23', // Data ze screena
  startTime: '00:47',     // Godzina ze screena
  privacy: 'Prywatne',
  description: '',
});

// --- COMPUTED PROPERTIES ---

// Format daty dla podglądu: "wtorek, 23 grudnia 2025"
const formattedDateFull = computed(() => {
  if (!form.startDate) return '';
  const date = new Date(form.startDate);
  return date.toLocaleDateString('pl-PL', {
    weekday: 'long',
    day: 'numeric',
    month: 'long',
    year: 'numeric'
  });
});

// Format daty dla Inputa (symulacja formatu "23 gru 2025")
const formattedDateShort = computed(() => {
    if (!form.startDate) return '';
    const date = new Date(form.startDate);
    const day = date.getDate();
    const year = date.getFullYear();
    // Pobranie skróconego miesiąca
    const month = date.toLocaleDateString('pl-PL', { month: 'short' });
    return `${day} ${month} ${year}`;
});

const dayNumber = computed(() => {
  if (!form.startDate) return '23';
  return new Date(form.startDate).getDate();
});

const previewTitle = computed(() =>
  viewMode.value === 'mobile' ? 'Podgląd na urządzeniu mobilnym' : 'Podgląd na komputerze'
);

const containerClass = computed(() =>
  viewMode.value === 'mobile'
    ? 'max-w-[375px] min-h-[600px] border-x border-gray-200 shadow-sm'
    : 'max-w-[850px] shadow-[0_1px_2px_rgba(0,0,0,0.1)] border border-[#dbdbdb]'
);
</script>

<template>
  <div class="flex h-screen w-full bg-[#F0F2F5] font-sans text-[#050505] overflow-hidden antialiased">

    <div class="w-[360px] flex-shrink-0 flex flex-col bg-white shadow-[2px_0_5px_rgba(0,0,0,0.05)] h-full z-20 relative border-r border-[#E5E5E5]">

      <div class="h-[60px] flex items-center px-4 border-b border-[#E5E5E5] flex-shrink-0">
        <h1 class="text-[20px] font-bold tracking-tight">Utwórz wydarzenie</h1>
      </div>

      <div class="flex-1 overflow-y-auto px-4 py-4 custom-scrollbar">

        <div class="flex items-center gap-3 mb-4">
           <div class="w-9 h-9 rounded-full bg-gray-300 overflow-hidden">
             <img src="https://i.pravatar.cc/150?u=bartosz" class="w-full h-full object-cover"/>
           </div>
           <div class="leading-tight">
             <div class="font-semibold text-[#050505] text-[15px]">Bartosz Miazek</div>
             <div class="text-[13px] text-[#65676B]">Organizator — Twój profil</div>
           </div>
        </div>

        <div class="mb-5">
            <label class="flex items-start justify-between cursor-pointer group">
                <div class="w-[80%]">
                    <div class="font-semibold text-[15px] leading-tight mb-1">Wypełnij przy użyciu danych minionego wydarzenia</div>
                    <div class="text-[13px] text-[#65676B] leading-snug">Wybierz dowolne minione wydarzenie, aby oszczędzić czas podczas konfigurowania tego nowego wydarzenia.</div>
                </div>
                <div class="relative inline-flex items-center cursor-pointer mt-1">
                    <input type="checkbox" v-model="usePastEvent" class="sr-only peer">
                    <div class="w-[44px] h-[24px] bg-gray-300 peer-focus:outline-none rounded-full peer peer-checked:after:translate-x-5 peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-[#1877F2]"></div>
                </div>
            </label>
        </div>

        <div class="mb-3">
            <input
                v-model="form.eventName"
                type="text"
                class="w-full h-[56px] border border-[#CED0D4] rounded-md px-4 bg-white hover:border-[#8D949E] focus:border-[#1877F2] outline-none placeholder-gray-500 text-[15px] transition-colors"
                placeholder="Nazwa wydarzenia"
            />
        </div>

        <div class="flex gap-3 mb-2">
            <div class="flex-1 relative h-[56px] border border-[#dba60d] rounded-md bg-white hover:bg-gray-50 cursor-pointer flex flex-col justify-center px-3 group overflow-hidden">
                <label class="text-[12px] text-[#65676B] absolute top-1.5 left-3">Data rozpoczęcia</label>
                <div class="text-[15px] text-[#050505] pt-3">{{ formattedDateShort }}</div>

                <input type="date" v-model="form.startDate" class="absolute inset-0 opacity-0 cursor-pointer" />

                <div class="absolute right-2 top-1/2 -translate-y-1/2 text-[#dba60d]">
                    <svg class="w-6 h-6" viewBox="0 0 24 24" fill="currentColor"><path d="M1 21h22L12 2 1 21zm12-3h-2v-2h2v2zm0-4h-2v-4h2v4z"/></svg>
                </div>
            </div>

            <div class="w-[110px] relative h-[56px] border border-[#CED0D4] rounded-md bg-white hover:border-[#8D949E] cursor-pointer flex flex-col justify-center px-3 group">
                <label class="text-[12px] text-[#65676B] absolute top-1.5 left-3 group-focus-within:text-[#1877F2]">Czas rozpoczęcia</label>
                <input
                    v-model="form.startTime"
                    type="time"
                    class="w-full bg-transparent border-none outline-none text-[15px] text-[#050505] pt-3 p-0 m-0 font-sans"
                />
            </div>
        </div>

        <div class="text-[13px] text-[#B08D55] mb-3 leading-snug">
            Planując wydarzenie z przynajmniej 7-dniowym wyprzedzeniem, pozwalasz je odkryć większemu gronu osób.
        </div>

        <div class="mb-4">
            <button class="text-[#1877F2] font-semibold text-[15px] hover:underline flex items-center gap-1">
                <span>+</span> Data i godzina zakończenia
            </button>
        </div>

        <div class="mb-4">
            <button class="w-full h-[36px] bg-[#E4E6EB] hover:bg-[#D8DADF] rounded-md flex items-center justify-center gap-2 font-semibold text-[15px] text-[#050505] transition-colors">
                <svg class="w-5 h-5" fill="currentColor" viewBox="0 0 24 24"><path d="M12 4V1L8 5l4 4V6c3.31 0 6 2.69 6 6 0 1.01-.25 1.97-.7 2.8l1.46 1.46C19.54 15.03 20 13.57 20 12c0-4.42-3.58-8-8-8zm0 14c-3.31 0-6-2.69-6-6 0-1.01.25-1.97.7-2.8L5.24 7.74C4.46 8.97 4 10.43 4 12c0 4.42 3.58 8 8 8v3l4-4-4-4v3z"/></svg>
                Wydarzenie cykliczne
            </button>
        </div>

        <div class="mb-4 relative">
             <div class="w-full h-[56px] border border-[#CED0D4] rounded-md px-3 bg-white hover:border-[#8D949E] cursor-pointer flex items-center justify-between group">
                <div class="flex items-center gap-3">
                    <svg class="w-5 h-5 text-[#65676B]" fill="currentColor" viewBox="0 0 24 24"><path d="M18 8h-1V6c0-2.76-2.24-5-5-5S7 3.24 7 6v2H6c-1.1 0-2 .9-2 2v10c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V10c0-1.1-.9-2-2-2zm-9-2c0-1.66 1.34-3 3-3s3 1.34 3 3v2H9V6zm9 14H6V10h12v10zm-6-3c1.1 0 2-.9 2-2s-.9-2-2-2-2 .9-2 2 .9 2 2 2z"/></svg>
                    <div class="flex flex-col">
                        <span class="text-[12px] text-[#65676B]">Prywatność</span>
                        <span class="text-[15px] font-semibold text-[#050505]">Prywatne</span>
                    </div>
                </div>
                <svg class="w-5 h-5 text-[#65676B]" fill="currentColor" viewBox="0 0 24 24"><path d="M7 10l5 5 5-5z"/></svg>
             </div>
        </div>

        <div class="mb-10">
            <textarea
                v-model="form.description"
                class="w-full min-h-[140px] border border-[#CED0D4] rounded-md p-3 bg-white hover:border-[#8D949E] focus:border-[#1877F2] outline-none text-[15px] resize-none placeholder-gray-500"
                placeholder="Opis"
            ></textarea>
        </div>

      </div>

      <div class="p-4 border-t border-[#E5E5E5] flex items-center bg-white shadow-[0_-2px_4px_rgba(0,0,0,0.05)] mt-auto z-30">
         <button class="px-5 py-2 rounded-md bg-[#E4E6EB] text-[#050505] font-semibold hover:bg-[#D8DADF] transition text-[15px]">Wstecz</button>
         <div class="flex-1 mx-4">
             <div class="h-2 bg-[#F0F2F5] rounded-full overflow-hidden">
                 <div class="w-[35%] h-full bg-[#1877F2]"></div>
             </div>
         </div>
         <button class="px-8 py-2 rounded-md bg-[#E4E6EB] text-[#BCC0C4] font-semibold cursor-not-allowed text-[15px]">Dalej</button>
      </div>
    </div>

    <div class="flex-1 flex flex-col h-full bg-[#F0F2F5] overflow-hidden">

      <div class="bg-white px-5 py-3 shadow-sm flex items-center justify-between z-10 border-b border-[#E5E5E5] h-[60px] flex-shrink-0">
        <span class="font-bold text-[#65676B] text-[15px]">{{ previewTitle }}</span>
        <div class="flex gap-2 text-[#65676B]">
           <div
             @click="viewMode = 'desktop'"
             :class="['flex items-center justify-center w-10 h-9 rounded cursor-pointer transition', viewMode === 'desktop' ? 'text-[#1877F2] bg-[#E7F3FF]' : 'hover:bg-gray-100']"
           >
              <svg class="w-6 h-6" fill="currentColor" viewBox="0 0 24 24"><path d="M20 18c1.1 0 1.99-.9 1.99-2L22 6c0-1.1-.9-2-2-2H4c-1.1 0-2 .9-2 2v10c0 1.1.9 2 2 2H0v2h24v-2h-4zM4 6h16v10H4V6z"/></svg>
           </div>
           <div
             @click="viewMode = 'mobile'"
             :class="['flex items-center justify-center w-10 h-9 rounded cursor-pointer transition', viewMode === 'mobile' ? 'text-[#1877F2] bg-[#E7F3FF]' : 'hover:bg-gray-100']"
           >
              <svg class="w-6 h-6" fill="currentColor" viewBox="0 0 24 24"><path d="M17 1.01L7 1c-1.1 0-2 .9-2 2v18c0 1.1.9 2 2 2h10c1.1 0 2-.9 2-2V3c0-1.1-.9-1.99-2-1.99zM17 19H7V5h10v14z"/></svg>
           </div>
        </div>
      </div>

      <div class="flex-1 overflow-y-auto p-4 md:p-8 flex justify-center items-start custom-scrollbar">

        <div
          :class="['bg-white rounded-xl overflow-hidden transition-all duration-300 ease-in-out', containerClass]"
        >

          <div :class="['bg-gray-100 relative overflow-hidden group cursor-pointer border-b border-[#E5E5E5]', viewMode === 'mobile' ? 'h-[180px]' : 'h-[280px]']">
             <div class="absolute inset-0 flex items-center justify-center">
               <svg class="w-12 h-12 text-[#BCC0C4] opacity-50" fill="currentColor" viewBox="0 0 24 24"><path d="M21 19V5c0-1.1-.9-2-2-2H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2zM8.5 13.5l2.5 3.01L14.5 12l4.5 6H5l3.5-4.5z"/></svg>
             </div>

             <div class="absolute top-4 left-4 bg-white rounded-lg shadow-sm border border-[#dbdbdb] text-center overflow-hidden w-[60px] z-10">
               <div class="bg-[#D93025] h-4 w-full"></div> <div class="py-1 flex items-center justify-center h-[40px]">
                 <span class="block font-bold text-[24px] text-[#050505] leading-none">{{ dayNumber }}</span>
               </div>
             </div>
          </div>

          <div class="px-4 py-4">

             <div class="text-[#D93025] font-bold text-[13px] uppercase tracking-wide mb-1">
               {{ formattedDateFull }} {{ form.startTime }} CET
             </div>

             <div class="flex justify-between items-start gap-3 mb-2">
                <h2 :class="['font-bold text-[#050505] leading-tight break-words flex-1', form.eventName ? '' : 'text-[#BCC0C4]', viewMode === 'mobile' ? 'text-[22px]' : 'text-[28px]']">
                  {{ form.eventName || 'Nazwa wydarzenia' }}
                </h2>
                <button class="bg-[#1877F2] text-white font-semibold text-[14px] px-3 py-2 rounded-md hover:bg-[#166FE5] transition flex-shrink-0">
                  Dołącz do transmisji na żywo
                </button>
             </div>

             <div class="text-[#65676B] font-semibold text-[15px] mb-4">Facebook Live</div>

             <template v-if="viewMode === 'mobile'">
                <div class="bg-[#F0F2F5] rounded-xl p-3 mb-4">
                  <div class="flex items-center gap-3 mb-4">
                    <div class="w-10 h-10 rounded-full bg-gray-300 overflow-hidden border border-white">
                      <img src="https://i.pravatar.cc/150?u=bartosz" class="w-full h-full object-cover"/>
                    </div>
                    <div class="text-[15px] leading-snug">
                      <span class="font-bold text-[#050505]">Bartosz Miazek</span>
                      <span class="text-[#65676B]"> zaprasza Cię</span>
                    </div>
                  </div>

                  <div class="flex flex-col gap-2">
                     <div class="flex gap-2">
                       <button class="flex-1 flex items-center justify-center gap-2 h-9 bg-white hover:bg-gray-50 rounded-md text-[14px] font-semibold text-[#050505] border border-transparent shadow-sm">
                         <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24" stroke-width="2"><path stroke-linecap="round" stroke-linejoin="round" d="M5 13l4 4L19 7"></path></svg>
                         Wezmę udział
                       </button>
                       <button class="flex-1 flex items-center justify-center gap-2 h-9 bg-white hover:bg-gray-50 rounded-md text-[14px] font-semibold text-[#050505] border border-transparent shadow-sm">
                         <svg class="w-4 h-4 text-[#050505]" fill="none" stroke="currentColor" viewBox="0 0 24 24" stroke-width="2"><path stroke-linecap="round" stroke-linejoin="round" d="M8.228 9c.549-1.165 2.03-2 3.772-2 2.21 0 4 1.343 4 3 0 1.4-1.278 2.575-3.006 2.907-.542.104-.994.54-.994 1.093m0 3h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg>
                         Może
                       </button>
                     </div>
                     <div class="flex gap-2">
                       <button class="flex-1 flex items-center justify-center gap-2 h-9 bg-white hover:bg-gray-50 rounded-md text-[14px] font-semibold text-[#050505] border border-transparent shadow-sm">
                         <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24" stroke-width="2"><path stroke-linecap="round" stroke-linejoin="round" d="M6 18L18 6M6 6l12 12"></path></svg>
                         Nie mogę wziąć udziału
                       </button>
                     </div>
                     <button class="w-full flex items-center justify-center gap-2 h-9 bg-[#E4E6EB] hover:bg-[#D8DADF] rounded-md text-[14px] font-semibold text-[#050505] mt-1">
                       <svg class="w-4 h-4" fill="currentColor" viewBox="0 0 24 24"><path d="M2.01 21L23 12 2.01 3 2 10l15 2-15 2z"/></svg>
                       Zaproś
                     </button>
                  </div>
                </div>
             </template>

             <template v-else>
               <div class="flex items-center gap-3 mb-5">
                 <div class="w-10 h-10 rounded-full bg-gray-300 overflow-hidden">
                   <img src="https://i.pravatar.cc/150?u=bartosz" class="w-full h-full object-cover"/>
                 </div>
                 <div class="text-[15px]">
                   <span class="font-bold text-[#050505]">Bartosz Miazek</span>
                   <span class="text-[#65676B]"> zaprasza Cię</span>
                 </div>
               </div>

               <div class="flex flex-wrap gap-2 mb-6">
                  <button class="flex items-center gap-2 px-3 h-9 bg-[#E4E6EB] hover:bg-[#D8DADF] rounded-md text-[15px] font-semibold text-[#050505]">Wezmę udział</button>
                  <button class="flex items-center gap-2 px-3 h-9 bg-[#E4E6EB] hover:bg-[#D8DADF] rounded-md text-[15px] font-semibold text-[#050505]">Może</button>
                  <button class="flex items-center gap-2 px-3 h-9 bg-[#E4E6EB] hover:bg-[#D8DADF] rounded-md text-[15px] font-semibold text-[#050505]">Nie mogę...</button>
                  <button class="flex items-center gap-2 px-3 h-9 bg-[#E4E6EB] hover:bg-[#D8DADF] rounded-md text-[15px] font-semibold text-[#050505]">Zaproś</button>
               </div>
               <hr class="border-[#CED0D4] mb-4" />
             </template>

             <div :class="viewMode === 'mobile' ? 'mt-6' : ''">
               <h3 class="text-[17px] font-bold text-[#050505] mb-2">Szczegółowe informacje</h3>
               <div class="text-[#65676B] text-[15px] mb-4 whitespace-pre-line">
                 {{ form.description || 'Nie ma jeszcze opisu wydarzenia' }}
               </div>

               <div class="flex flex-wrap gap-2">
                 <span class="px-3 py-1.5 bg-[#E4E6EB] rounded-md text-[15px] font-semibold text-[#050505]">Online</span>
                 <span class="px-3 py-1.5 bg-[#E4E6EB] rounded-md text-[15px] font-semibold text-[#050505]">Transmisja wideo na żywo</span>
               </div>
             </div>

          </div>
        </div>

      </div>
    </div>
  </div>
</template>

<style scoped>
/* Pasek przewijania */
.custom-scrollbar::-webkit-scrollbar {
  width: 8px;
}
.custom-scrollbar::-webkit-scrollbar-track {
  background: transparent;
}
.custom-scrollbar::-webkit-scrollbar-thumb {
  background: #BCC0C4;
  border-radius: 4px;
}
.custom-scrollbar::-webkit-scrollbar-thumb:hover {
  background: #8D949E;
}
</style>
