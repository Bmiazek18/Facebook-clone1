<script setup lang="ts">
import { reactive, ref } from 'vue';

// Import ikon
import CloseIcon from 'vue-material-design-icons/Close.vue';
import PlusIcon from 'vue-material-design-icons/Plus.vue';
import ChevronDownIcon from 'vue-material-design-icons/ChevronDown.vue';
import CellphoneIcon from 'vue-material-design-icons/Cellphone.vue';
import RocketLaunchIcon from 'vue-material-design-icons/RocketLaunch.vue';
import LockIcon from 'vue-material-design-icons/Lock.vue';
import EarthIcon from 'vue-material-design-icons/Earth.vue';
import BaseModal from '@/components/common/BaseModal.vue';
import SellerModal from '@/components/marketplace/SellerModal.vue';

// --- TYPY ---

interface FormState {
  title: string;
  price: string;
  category: string;
  condition: string;
  description: string;
}

// Typ pól, które mogą być "focused"
type FocusedFieldType = 'title' | 'price' | 'category' | 'condition' | 'description' | 'photo' | null;

// --- STAN ---
const form = reactive<FormState>({
  title: '',
  price: '',
  category: '',
  condition: '',
  description: '',
});

const focusedField = ref<FocusedFieldType>(null);

// Funkcja Spotlight
const getHighlightClass = (fieldName: string): string => {
  const isAnyFocused = focusedField.value !== null;
  const isThisFocused = focusedField.value === fieldName;

  // Logika dla sekcji szczegółów (podświetla się, gdy edytujemy kategorię lub stan)
  if (fieldName === 'details') {
     if (focusedField.value === 'category' || focusedField.value === 'condition') {
       return 'bg-[#EAF3FF] ring-2 ring-transparent opacity-100 -mx-2 px-2';
     }
  }

  if (isThisFocused) {
    return 'bg-[#EAF3FF] ring-2 ring-transparent opacity-100 -mx-2 px-2';
  } else if (isAnyFocused) {
    return 'bg-transparent opacity-30 blur-[0.5px]';
  } else {
    return 'bg-transparent opacity-100';
  }
};

const categories: string[] = ['Narzędzia', 'Meble', 'Ogród', 'Elektronika', 'Motoryzacja'];
const conditions: string[] = ['Nowy', 'Używany - jak nowy', 'Używany - dobry', 'Używany - akceptowalny'];

// Obsługa zdjęć
const fileInput = ref<HTMLInputElement | null>(null);
const uploadedImages = ref<string[]>([]);

const triggerUpload = () => {
  fileInput.value?.click();
};

// Seller modal
const isSellerModalOpen = ref<boolean>(false);
const openSellerModal = () => { isSellerModalOpen.value = true; };
const closeSellerModal = () => { isSellerModalOpen.value = false; };

const handleFileChange = (event: Event) => {
  const target = event.target as HTMLInputElement;

  if (target.files) {
    const files = Array.from(target.files);
    files.forEach(file => {
      if (file && uploadedImages.value.length < 10) {
        uploadedImages.value.push(URL.createObjectURL(file));
      }
    });
  }

  // Wyczyść input żeby można było dodać ten sam plik ponownie
  target.value = '';
};

const removeImage = (index: number) => {
  uploadedImages.value.splice(index, 1);
};
</script>

<template>
  <div class="flex h-screen w-full bg-[#F0F2F5] font-sans text-[#1C1E21] overflow-hidden">

    <aside class="w-[360px] flex-shrink-0 bg-white shadow-sm flex flex-col border-r border-gray-200 z-20 h-full">

      <div class="px-4 pt-4 pb-2 flex-shrink-0">
        <div class="flex justify-between items-start mb-1">
           <span class="text-[13px] text-gray-500 font-normal">Marketplace</span>
           <button class="text-[15px] text-blue-600 font-semibold hover:underline">Zapisz wersję roboczą</button>
        </div>
        <h1 class="text-2xl font-bold leading-tight mb-4">Przedmiot na sprzedaż</h1>

        <div class="flex items-center gap-3 mb-4">
          <img src="https://i.pravatar.cc/150?img=12" alt="Avatar" class="w-10 h-10 rounded-full border border-gray-100" />
          <div>
            <p class="font-bold text-[15px] text-[#1C1E21]">Bartosz Miazek</p>
            <div class="flex items-center gap-1 text-[13px] text-gray-500">
               <span>Ogłaszanie w Marketplace</span>
               <span>·</span>
               <EarthIcon :size="12" />
               <span>Publiczne</span>
            </div>
          </div>
        </div>
      </div>

      <div class="flex-1 overflow-y-auto px-4 pb-4 space-y-6 custom-scrollbar">

        <div
           @mouseenter="focusedField = 'photo'"
           @mouseleave="focusedField = null"
           class="transition duration-200"
        >
           <div class="flex justify-between items-center mb-2">
             <span class="font-semibold text-[15px]">Zdjęcia · {{ uploadedImages.length }} / 10</span>
             <span class="text-[13px] text-gray-500 font-normal hidden">Możesz dodać maksymalnie 10 zdjęć</span>
           </div>
           <p class="text-[12px] text-gray-500 mb-2">Możesz dodać maksymalnie 10 zdjęć</p>

           <div class="flex gap-2 overflow-x-auto pb-2" :class="{'ring-2 ring-blue-500 rounded-lg p-1 -m-1': focusedField === 'photo'}">

             <div v-for="(img, index) in uploadedImages" :key="index" class="relative w-[100px] h-[100px] flex-shrink-0 bg-gray-100 rounded-lg border border-gray-200 overflow-hidden">
                <img :src="img" class="w-full h-full object-cover" />
                <button @click.stop="removeImage(index)" class="absolute top-1 right-1 bg-white rounded-full p-1 shadow hover:bg-gray-100">
                  <CloseIcon :size="14" />
                </button>
             </div>

             <div
               @click="triggerUpload"
               class="w-[100px] h-[100px] flex-shrink-0 bg-[#E4E6EB] hover:bg-[#d8dadf] rounded-lg flex flex-col items-center justify-center cursor-pointer transition"
             >
               <div class="w-8 h-8 bg-gray-600 rounded-full flex items-center justify-center mb-1">
                 <PlusIcon :size="20" class="text-white" />
               </div>
               <span class="text-[13px] font-semibold text-[#1C1E21] text-center px-1 leading-tight">Dodaj zdjęcie</span>
               <input type="file" ref="fileInput" @change="handleFileChange" class="hidden" accept="image/*" multiple />
             </div>

           </div>
        </div>

        <div class="bg-[#F0F2F5] rounded-lg p-3 flex items-center justify-between">
           <div class="flex items-center gap-3">
              <CellphoneIcon :size="24" class="text-gray-800" />
              <div>
                 <p class="text-[13px] font-semibold leading-tight mb-0.5">Dodaj zdjęcia bezpośrednio z telefonu.</p>
                 <a href="#" class="text-[13px] text-blue-600 hover:underline">Dowiedz się więcej</a>
              </div>
           </div>
           <button class="bg-[#D8DADF] hover:bg-[#cdd0d5] text-[#1C1E21] text-[13px] font-semibold px-3 py-1.5 rounded-md transition">
             Wypróbuj
           </button>
        </div>

        <div class="space-y-3">
          <div class="mb-1">
            <h3 class="font-bold text-[17px]">Wymagane</h3>
            <p class="text-[13px] text-gray-500">Opis powinien być jak najbardziej szczegółowy.</p>
          </div>

          <input
            v-model="form.title"
            @focus="focusedField = 'title'"
            @blur="focusedField = null"
            type="text"
            placeholder="Tytuł"
            class="w-full border border-[#CED0D4] rounded-md py-3 px-3 placeholder-gray-500 focus:border-blue-500 focus:ring-1 focus:ring-blue-500 outline-none transition-shadow text-[15px]"
          />

          <input
            v-model="form.price"
            @focus="focusedField = 'price'"
            @blur="focusedField = null"
            type="text"
            placeholder="Cena"
            class="w-full border border-[#CED0D4] rounded-md py-3 px-3 placeholder-gray-500 focus:border-blue-500 focus:ring-1 focus:ring-blue-500 outline-none transition-shadow text-[15px]"
          />

          <div class="relative">
             <select
               v-model="form.category"
               @focus="focusedField = 'category'"
               @blur="focusedField = null"
               class="w-full border border-[#CED0D4] rounded-md py-3 px-3 appearance-none bg-white focus:border-blue-500 focus:ring-1 focus:ring-blue-500 outline-none text-[15px] cursor-pointer"
               :class="form.category ? 'text-[#1C1E21]' : 'text-gray-500'"
             >
               <option value="" disabled selected>Kategoria</option>
               <option v-for="cat in categories" :key="cat" :value="cat">{{ cat }}</option>
             </select>
             <ChevronDownIcon :size="24" class="absolute right-3 top-1/2 -translate-y-1/2 text-gray-500 pointer-events-none" />
          </div>

          <div class="relative">
             <select
               v-model="form.condition"
               @focus="focusedField = 'condition'"
               @blur="focusedField = null"
               class="w-full border border-[#CED0D4] rounded-md py-3 px-3 appearance-none bg-white focus:border-blue-500 focus:ring-1 focus:ring-blue-500 outline-none text-[15px] cursor-pointer"
               :class="form.condition ? 'text-[#1C1E21]' : 'text-gray-500'"
             >
               <option value="" disabled selected>Stan</option>
               <option v-for="cond in conditions" :key="cond" :value="cond">{{ cond }}</option>
             </select>
             <ChevronDownIcon :size="24" class="absolute right-3 top-1/2 -translate-y-1/2 text-gray-500 pointer-events-none" />
          </div>

          <div class="border-t border-[#CED0D4] pt-4 mt-2">
             <div class="flex justify-between items-center cursor-pointer">
                <div>
                   <h3 class="font-bold text-[17px]">Więcej informacji</h3>
                   <p class="text-[13px] text-gray-500 mt-0.5 max-w-[280px]">Wzbudź większe zainteresowanie, podając więcej szczegółów informacji.</p>
                </div>
                <div class="p-2 bg-[#E4E6EB] rounded-full">
                  <ChevronDownIcon :size="20" />
                </div>
             </div>

             <textarea
               v-show="false"
               v-model="form.description"
               @focus="focusedField = 'description'"
               @blur="focusedField = null"
               class="w-full mt-2 border border-[#CED0D4] rounded-md p-3"
             ></textarea>
          </div>

          <hr class="border-[#CED0D4]" />

          <div class="space-y-5 pt-1">
             <div class="flex items-center justify-between">
                <div class="flex gap-3">
                   <div class="mt-0.5"><RocketLaunchIcon :size="24" class="text-gray-800" /></div>
                   <div class="max-w-[220px]">
                      <h4 class="font-semibold text-[15px]">Promuj ogłoszenie po opublikowaniu</h4>
                      <p class="text-[12px] text-gray-500 mt-0.5 leading-snug">
                        Dodaj krok w celu promowania ogłoszenia po jego opublikowaniu, aby dotrzeć do większej liczby...
                      </p>
                   </div>
                </div>
                <div class="w-12 h-7 bg-[#CED0D4] rounded-full relative cursor-pointer">
                   <div class="w-5 h-5 bg-white rounded-full shadow absolute top-1 left-1"></div>
                </div>
             </div>

             <div class="flex items-center justify-between pb-4">
                <div class="flex gap-3 items-center">
                   <div><LockIcon :size="24" class="text-gray-800" /></div>
                   <div>
                      <h4 class="font-semibold text-[15px]">Ukryj przed znajomymi</h4>
                      <p class="text-[12px] text-gray-500 mt-0.5">To ogłoszenie jest nadal publiczne.</p>
                   </div>
                </div>
                <div class="w-12 h-7 bg-[#CED0D4] rounded-full relative cursor-pointer">
                   <div class="w-5 h-5 bg-white rounded-full shadow absolute top-1 left-1"></div>
                </div>
             </div>
          </div>

        </div>
      </div>

      <div class="px-4 pb-4 pt-2 border-t border-gray-200 bg-white shadow-[0_-2px_5px_rgba(0,0,0,0.05)] z-30">
        <div class="flex gap-1 mb-3">
           <div class="h-1.5 flex-1 bg-blue-600 rounded-full"></div>
           <div class="h-1.5 flex-1 bg-[#E4E6EB] rounded-full"></div>
        </div>

        <button
          class="w-full py-2 rounded-md font-semibold text-[15px] transition select-none"
          :class="form.title ? 'bg-blue-600 text-white hover:bg-blue-700' : 'bg-[#E4E6EB] text-[#BCC0C4] cursor-not-allowed'"
          :disabled="!form.title"
        >
          Dalej
        </button>
      </div>
    </aside>

    <main class="flex-1 flex flex-col items-center justify-center p-8 relative overflow-hidden bg-[#F0F2F5]">

      <div class="w-full h-[90vh] max-w-[980px] z-10 flex flex-col items-center">
        <h2 class="text-[17px] font-semibold mb-3 text-[#1C1E21] w-full text-left">Podgląd</h2>

        <div class="bg-white rounded-lg shadow-sm border border-[#dbdbdb] w-full h-full flex overflow-hidden">

          <div
            class="w-[60%] bg-[#F7F8FA] flex flex-col items-center justify-center text-center p-6 border-r border-[#dbdbdb] relative transition-all duration-300 ease-in-out"
            :class="focusedField === 'photo' ? 'opacity-100' : (focusedField ? 'opacity-40 blur-[1px]' : 'opacity-100')"
          >
             <div v-if="uploadedImages.length > 0" class="absolute inset-0 bg-black flex items-center justify-center">
                 <img :src="uploadedImages[0]" class="max-w-full max-h-full object-contain" />
             </div>

             <div v-else class="max-w-[260px] select-none">
                <h3 class="text-[17px] font-bold text-[#65676B] mb-2">Podgląd Twojego ogłoszenia</h3>
                <p class="text-[13px] text-[#65676B] leading-[1.3]">
                  Tworząc ogłoszenie, możesz wyświetlić jego podgląd i sprawdzić, jak będzie wyglądać dla innych użytkowników w Marketplace.
                </p>
             </div>
          </div>

          <div class="w-[40%] flex flex-col h-full bg-white">

            <div class="p-5 flex-1 overflow-y-auto space-y-4 custom-scrollbar">

              <div class="space-y-1">
                <div class="transition-all duration-300 rounded-lg py-1" :class="getHighlightClass('title')">
                   <h1 class="text-2xl font-bold leading-tight break-words text-[#1C1E21]">
                    {{ form.title || 'Tytuł' }}
                   </h1>
                </div>

                <div class="transition-all duration-300 rounded-lg py-1" :class="getHighlightClass('price')">
                   <p class="text-[17px] text-[#1C1E21]">
                     {{ form.price ? form.price + ' zł' : 'Cena' }}
                   </p>
                   <p class="text-xs text-[#65676B] mt-1">
                     Opublikowano kilka sekund temu w: <span class="font-semibold">Łęczyca (Gmina)</span>
                   </p>
                </div>
              </div>


              <div class="transition-all duration-300 rounded-lg py-2" :class="getHighlightClass('details')">
                 <h3 class="text-[17px] font-bold mb-3 text-[#1C1E21]">Szczegóły</h3>
                 <div class="flex justify-between text-[15px] py-1">
                    <span class="text-[#65676B]">Stan</span>
                    <span class="text-[#1C1E21]">{{ form.condition || '–' }}</span>
                 </div>
                 <div class="flex justify-between text-[15px] py-1">
                    <span class="text-[#65676B]">Kategoria</span>
                    <span class="text-[#1C1E21]">{{ form.category || '–' }}</span>
                 </div>
              </div>

              <div class="transition-all duration-300 rounded-lg py-2" :class="getHighlightClass('description')">
                 <p class="text-[15px] text-[#1C1E21] leading-relaxed break-words whitespace-pre-line">
                   {{ form.description || 'W tym miejscu pojawi się opis.' }}
                 </p>
              </div>

              <hr class="border-[#dbdbdb] my-2" />

              <div class="transition-all duration-300 py-1 rounded-lg" :class="focusedField ? 'opacity-30 blur-[1px]' : 'opacity-100'">
                <div class="flex justify-between items-center mb-4">
                  <h3 class="text-[17px] font-bold text-[#1C1E21]">Informacje o sprzedawcy</h3>
                  <a href="#" class="text-[15px] text-blue-600 font-semibold hover:underline">Szczegóły</a>
                </div>
                <div @click="openSellerModal" class="flex items-center gap-3 cursor-pointer">
                  <img src="https://i.pravatar.cc/150?img=12" alt="Avatar" class="w-10 h-10 rounded-full border border-gray-200" />
                  <div>
                    <p class="font-semibold text-[15px] text-[#1C1E21]">Bartosz Miazek</p>
                    <p class="text-[13px] text-[#65676B]">Dołączył(a) do Facebooka w 2015</p>
                  </div>
                </div>
              </div>

            </div>

            <div class="p-4 border-t border-[#dbdbdb] bg-white z-20">
              <button disabled class="w-full bg-[#E4E6EB] text-[#BCC0C4] font-bold py-2 rounded-md mb-3 cursor-not-allowed select-none transition-colors">
                Wyślij wiadomość
              </button>
              <p class="text-[11px] text-[#65676B] text-center leading-[1.2] px-1">
                <a href="#" class="text-blue-600 hover:underline">Dowiedz się więcej</a> about purchasing from consumers.
              </p>
            </div>

          </div>
        </div>
      </div>

    </main>
  </div>
  <BaseModal v-if="isSellerModalOpen" @close="closeSellerModal" :title="'Profil sprzedawcy'">
    <SellerModal :profile="{ name: 'Bartosz Miazek', joinedText: 'Na Facebooku od 2015', location: '', avatarUrl: 'https://i.pravatar.cc/150?img=12' }" @close="closeSellerModal" />
  </BaseModal>
</template>

<style scoped>
/* Scrollbar */
.custom-scrollbar::-webkit-scrollbar {
  width: 8px;
  height: 8px; /* Dla poziomego scrolla przy zdjęciach */
}
.custom-scrollbar::-webkit-scrollbar-track {
  background: transparent;
}
.custom-scrollbar::-webkit-scrollbar-thumb {
  background-color: #bcc0c4;
  border-radius: 4px;
}
.custom-scrollbar::-webkit-scrollbar-thumb:hover {
  background-color: #a0a4a8;
}
</style>
