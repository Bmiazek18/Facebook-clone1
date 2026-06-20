<template>
    <div class="bg-theme-bg-secondary w-[800px] rounded-xl shadow-[0_12px_28px_rgba(0,0,0,0.2)] flex flex-col overflow-hidden relative border border-theme-border z-10 h-[850px]">
<BaseModal
      :title="locationModalTitle"
      v-if="showLocationModal">
         <LocationModal
        :show="showLocationModal"
        :initial-location="form.location"
        @close="showLocationModal = false"
        @confirm="handleLocationConfirm"
      />
    </BaseModal>


      <div class="overflow-y-auto flex-1 custom-scrollbar relative" @click="activeDatePicker = null">

        <div class="relative w-full h-[200px] bg-theme-bg group shrink-0">
          <input type="file" ref="fileInput" accept="image/*" class="hidden" @change="handleFileChange" multiple />

          <div ref="carouselRef" class="flex overflow-hidden w-full h-full">
            <div
              v-for="(image, index) in previewImages"
              :key="index"
              class="flex-shrink-0 w-full h-full transition-transform duration-300 ease-in-out"
              :style="{ transform: `translateX(-${currentImageIndex * 100}%)` }"
            >
              <img :src="image" class="w-full h-full object-cover" />
            </div>
          </div>

          <button @click.stop="triggerFileInput" class="absolute bottom-4 right-4 bg-black/40 hover:bg-black/50 text-white px-4 py-2 rounded-lg font-semibold text-[15px] flex items-center gap-2 transition backdrop-blur-sm z-10">
             <camera-plus-icon v-if="previewImages.length === 0" :size="20" />
             <camera-icon v-else :size="20" />
             <span>{{ previewImages.length > 0 ? 'Edytuj' : 'Dodaj' }}</span>
          </button>

          <button v-if="previewImages.length > 0" @click.stop="removeImage" class="absolute top-4 right-4 bg-black/40 hover:bg-black/50 text-white p-2 rounded-full backdrop-blur-sm transition z-10" title="Usuń zdjęcie">
             <close-icon :size="16" />
          </button>

          <button v-if="previewImages.length > 1 && currentImageIndex > 0" @click.stop="prevImage" class="absolute top-1/2 left-2 -translate-y-1/2 bg-black/40 hover:bg-black/50 text-white p-2 rounded-full backdrop-blur-sm transition z-10">
            <chevron-left-icon :size="20" />
          </button>

          <button v-if="previewImages.length > 1 && currentImageIndex < previewImages.length - 1" @click.stop="nextImage" class="absolute top-1/2 right-2 -translate-y-1/2 bg-black/40 hover:bg-black/50 text-white p-2 rounded-full backdrop-blur-sm transition z-10">
            <chevron-right-icon :size="20" />
          </button>
        </div>

        <div class="p-4">
          <div class="flex items-start gap-3 mb-6 mt-2">
            <img :src="auth.currentUser?.avatar" class="w-10 h-10 rounded-full border border-theme-border" />
            <div class="flex flex-col">
              <span class="font-semibold text-[15px] text-theme-text">{{ auth.currentUser?.name }}</span>
              <span class="text-[13px] text-theme-text-secondary">Organizator — Twój profil</span>
            </div>
          </div>

          <div class="mb-4 relative group">
              <input v-model="form.name" @focus="isNameFocused = true" @blur="isNameFocused = false" type="text" id="eventName" maxlength="100" class="block rounded-lg pl-4 pr-16 pt-6 pb-2 w-full text-[17px] text-theme-text bg-transparent border border-theme-border appearance-none focus:outline-none focus:ring-0 focus:border-theme-primary peer" placeholder=" " />
              <label for="eventName" class="absolute text-[15px] text-theme-text-secondary duration-150 transform -translate-y-3 scale-75 top-4 z-10 origin-[0] left-4 peer-placeholder-shown:scale-100 peer-placeholder-shown:translate-y-0 peer-focus:scale-75 peer-focus:-translate-y-3 peer-focus:text-theme-primary">Nazwa wydarzenia</label>
              <span v-if="isNameFocused || form.name.length > 0" class="absolute top-3 right-3 text-[12px] text-theme-text-secondary pointer-events-none transition-opacity duration-200">{{ form.name.length }}/100</span>
          </div>

          <div class="space-y-4 mb-4">
            <div class="grid grid-cols-2 gap-4">
              <div class="relative">
                 <div @click.stop="toggleDatePicker('start')" :class="['border rounded-lg p-2 cursor-pointer flex items-center gap-2 hover:bg-theme-hover transition', activeDatePicker === 'start' ? 'border-theme-primary' : 'border-theme-border']">
                   <calendar-month-icon class="text-theme-text-secondary" />
                   <div class="flex flex-col"><span class="text-[12px] text-theme-text-secondary leading-none mb-0.5">Data rozpoczęcia</span><span class="text-[15px] text-theme-text">{{ formatDate(form.startDate) }}</span></div>
                 </div>
                 <div v-if="activeDatePicker === 'start'" class="absolute top-full left-0 z-50 mt-1 shadow-xl rounded-xl" @click.stop><VDatePicker v-model="form.startDate" mode="date" locale="pl" @dayclick="activeDatePicker = null" /></div>
              </div>
              <div class="border border-theme-border rounded-lg p-2 hover:bg-theme-hover cursor-pointer flex items-center gap-2 relative overflow-hidden">
                 <clock-outline-icon class="text-theme-text-secondary" />
                 <div class="flex flex-col w-full"><span class="text-[12px] text-theme-text-secondary leading-none mb-0.5">Czas rozpoczęcia</span><input v-model="form.startTime" type="time" class="text-[15px] text-theme-text bg-transparent border-none p-0 focus:ring-0 w-full font-sans" /></div>
              </div>
            </div>

            <div v-if="showEndDate" class="grid grid-cols-2 gap-4 animate-in fade-in slide-in-from-top-2 duration-200">
               <div class="relative">
                 <div @click.stop="toggleDatePicker('end')" :class="['border rounded-lg p-2 cursor-pointer flex items-center gap-2 hover:bg-theme-hover transition', activeDatePicker === 'end' ? 'border-theme-primary' : 'border-theme-border']">
                   <calendar-month-icon class="text-theme-text-secondary" />
                   <div class="flex flex-col"><span class="text-[12px] text-theme-text-secondary leading-none mb-0.5">Data zakończenia</span><span class="text-[15px] text-theme-text">{{ formatDate(form.endDate) }}</span></div>
                 </div>
                 <div v-if="activeDatePicker === 'end'" class="absolute top-full left-0 z-50 mt-1 shadow-xl rounded-xl" @click.stop><VDatePicker v-model="form.endDate" :min-date="form.startDate" mode="date" locale="pl" @dayclick="activeDatePicker = null" /></div>
              </div>
               <div class="border border-theme-border rounded-lg p-2 hover:bg-theme-hover cursor-pointer flex items-center gap-2 relative overflow-hidden">
                 <clock-outline-icon class="text-theme-text-secondary" />
                 <div class="flex flex-col w-full"><span class="text-[12px] text-theme-text-secondary leading-none mb-0.5">Godzina zakończenia</span><input v-model="form.endTime" type="time" class="text-[15px] text-theme-text bg-transparent border-none p-0 focus:ring-0 w-full font-sans" /></div>
              </div>
              <div class="col-span-2 text-[13px] text-theme-text-secondary">Strefa czasowa: CET</div>
            </div>

            <div class="flex items-center">
              <button
                @click="showEndDate = !showEndDate"
                class="text-theme-primary font-semibold text-[15px] hover:underline flex items-center gap-1 transition"
              >
                <plus-icon v-if="!showEndDate" :size="20"/>
                <minus-icon v-else :size="20"/>
                {{ !showEndDate ? 'Data i godzina zakończenia' : 'Data i godzina zakończenia' }}
              </button>
            </div>

          </div>

          <CustomDropdown
            v-model="form.type"
            :options="eventTypeOptions"
            label="Czy wydarzenie jest offline czy online?"
            class="mb-4"
            :icon-class="{ 'transform -rotate-45': form.type === 'online' }"
          />

           <div v-if="form.type === 'offline'" class="mb-4 relative">
             <div
               @click="showLocationModal = true"
               class="w-full p-3.5 pl-4 pr-12 border border-theme-border rounded-lg text-[15px] cursor-pointer hover:bg-theme-hover transition min-h-[52px] flex items-center truncate"
               :class="{'text-theme-text-secondary': !form.location, 'text-theme-text': form.location}"
             >
               {{ form.location || 'Dodaj lokalizację' }}
             </div>
             <div class="absolute right-1.5 top-1.5 bottom-1.5 aspect-square bg-theme-bg-subtle hover:bg-theme-hover-strong rounded-md flex items-center justify-center cursor-pointer transition pointer-events-none">
                <map-marker-icon class="text-theme-text" :size="20"/>
             </div>
          </div>

          <div class="mb-6">
             <label class="text-[15px] font-semibold mb-2 block">Kto może to zobaczyć?</label>
             <Dropdown class="w-full" :distance="6">
                <div class="w-full p-3 border border-theme-border rounded-lg flex items-center justify-between bg-theme-bg-secondary text-[15px] cursor-pointer hover:bg-theme-hover transition">
                    <div class="flex items-center gap-2">
                       <component :is="selectedPrivacyOption.icon" :size="20" class="text-theme-text-secondary"/>
                       <span class="text-theme-text">{{ selectedPrivacyOption.title }}</span>
                    </div>
                    <chevron-down-icon class="text-theme-text-secondary" :size="20"/>
                </div>

                <template #popper>
                  <div class="w-[340px] p-2 bg-theme-bg-secondary rounded-xl shadow-xl">
                     <div
                        v-for="option in privacyOptions"
                        :key="option.id"
                        @click="form.privacy = option.id"
                        class="flex items-center gap-3 p-3 rounded-lg cursor-pointer hover:bg-theme-bg transition relative"
                     >
                        <div class="bg-theme-bg-subtle p-2.5 rounded-full text-theme-text flex items-center justify-center">
                           <component :is="option.icon" :size="24" />
                        </div>
                        <div class="flex flex-col flex-1">
                           <span class="text-[15px] font-bold text-theme-text">{{ option.title }}</span>
                           <span class="text-[13px] text-theme-text-secondary leading-tight">{{ option.desc }}</span>
                        </div>
                        <div v-if="form.privacy === option.id" class="w-5 h-5 rounded-full border-[6px] border-theme-primary bg-theme-bg-secondary"></div>
                        <div v-else class="w-5 h-5 rounded-full border border-theme-text-secondary"></div>
                        <chevron-right-icon v-if="option.hasArrow" class="absolute right-2 text-theme-text-secondary" :size="24" />
                     </div>
                  </div>
                </template>
             </Dropdown>
          </div>

          <CustomTextarea
               v-model="form.description"
               id="desc"
               label="Jakie są szczegółowe informacje?"
               placeholder=" "
               class="min-h-[120px]"
             />

          <div class="space-y-4">

            <div class="border-t border-theme-border">
              <button @click.stop="toggleAccordion('cohosts')" class="flex items-center justify-between w-full py-4 hover:bg-theme-hover -mx-4 px-4 transition">
                <div class="flex items-center gap-3">
                   <plus-circle-outline-icon :size="24" class="text-theme-text-secondary" />
                   <span class="text-[15px] font-semibold text-theme-text">Dodaj współorganizatorów</span>
                </div>
                <chevron-down-icon :class="['text-theme-text-secondary transition-transform', accordions.cohosts ? 'rotate-180' : '']" :size="20" />
              </button>

              <div v-if="accordions.cohosts" class="pb-4 bg-theme-bg rounded-lg p-3">
                 <input type="text" placeholder="Dodaj współorganizatorów" class="w-full bg-theme-bg-secondary border border-theme-border rounded-lg px-3 py-2 text-[15px] mb-2 focus:border-theme-primary focus:outline-none" />
                 <p class="text-[12px] text-theme-text-secondary leading-tight">Współorganizatorzy mogą zaakceptować lub odrzucić zaproszenie dopiero po opublikowaniu wydarzenia.</p>
              </div>
            </div>

            <div class="border-t border-theme-border">
              <button @click.stop="toggleAccordion('recurring')" class="flex items-center justify-between w-full py-4 hover:bg-theme-hover -mx-4 px-4 transition">
                <div class="flex items-center gap-3">
                   <refresh-icon :size="24" class="text-theme-text-secondary" />
                   <span class="text-[15px] font-semibold text-theme-text">Powtarzane wydarzenie</span>
                </div>
                <chevron-down-icon :class="['text-theme-text-secondary transition-transform', accordions.recurring ? 'rotate-180' : '']" :size="20" />
              </button>

              <div v-if="accordions.recurring" class="pb-4 bg-theme-bg rounded-lg p-3">
                 <div class="mb-3">
                    <label class="text-[13px] text-theme-text-secondary mb-1 block">Częstotliwość</label>
                    <select v-model="form.frequency" class="w-full bg-theme-bg-secondary border border-theme-border rounded-lg px-3 py-2 text-[15px] focus:border-theme-primary focus:outline-none">
                       <option>Nigdy</option>
                       <option>Codziennie</option>
                       <option>Co tydzień</option>
                    </select>
                 </div>
                 <div class="flex gap-2">
                    <div class="flex-1 bg-theme-bg-subtle border border-theme-border rounded-lg p-2 flex items-center justify-between text-theme-text-muted">
                       <span>Data zakończenia</span>
                       <alert-circle-icon :size="18" class="text-[#d22d2d]" />
                    </div>
                     <div class="flex-1 bg-theme-bg-subtle border border-theme-border rounded-lg p-2 flex items-center justify-between text-theme-text-muted">
                       <span>Godzina zakończ...</span>
                       <alert-circle-icon :size="18" class="text-[#d22d2d]" />
                    </div>
                 </div>
                 <p class="text-[12px] text-theme-text-secondary mt-2">Wskaż datę zakończenia, która przypada po dacie rozpoczęcia.</p>
              </div>
            </div>

            <div class="border-t border-theme-border">
              <button @click.stop="toggleAccordion('settings')" class="flex items-center justify-between w-full py-4 hover:bg-theme-hover -mx-4 px-4 transition">
                <div class="flex items-center gap-3">
                   <format-list-bulleted-icon :size="24" class="text-theme-text-secondary" />
                   <span class="text-[15px] font-semibold text-theme-text">Dodatkowe ustawienia</span>
                </div>
                <chevron-down-icon :class="['text-theme-text-secondary transition-transform', accordions.settings ? 'rotate-180' : '']" :size="20" />
              </button>

              <div v-if="accordions.settings" class="pb-4 px-1">
                 <div class="flex items-center justify-between py-2">
                    <span class="text-[15px] text-theme-text">Pokaż listę gości</span>
                    <label class="relative inline-flex items-center cursor-pointer">
                      <input type="checkbox" v-model="form.showGuestList" class="sr-only peer">
                      <div class="w-11 h-6 bg-theme-bg-subtle peer-focus:outline-none rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-theme-border after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-theme-primary"></div>
                    </label>
                 </div>
              </div>
            </div>

          </div>

        </div>
      </div>

      <div class="p-4 border-t border-theme-border bg-theme-bg-secondary shadow-[0_-4px_12px_rgba(0,0,0,0.05)] shrink-0 z-20">
        <button @click="submit" :disabled="!isFormValid" :class="[isFormValid ? 'bg-theme-primary text-white hover:bg-theme-primary-hover' : 'bg-theme-hover text-theme-text-muted cursor-not-allowed']" class="w-full font-bold text-[15px] py-2 rounded-lg transition">Utwórz wydarzenie</button>
      </div>

    </div>

</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue';
import { DatePicker as VDatePicker } from 'v-calendar';
import { Dropdown } from 'floating-vue';
import 'v-calendar/dist/style.css';
import 'floating-vue/dist/style.css';
import CustomDropdown from '@/components/common/CustomDropdown.vue';
import CustomTextarea from '@/components/common/CustomTextarea.vue';
import { v4 as uuidv4 } from 'uuid'; // Import uuid for unique IDs
import { useEventsStore } from '@/stores/events'; // Import the events store
const auth = useAuthStore()

// Import nowego komponentu mapy
import LocationModal from '../LocationModal.vue';

// Import ikon Material Design
import CloseIcon from 'vue-material-design-icons/Close.vue';
import ChevronLeftIcon from 'vue-material-design-icons/ChevronLeft.vue';
import ChevronRightIcon from 'vue-material-design-icons/ChevronRight.vue';
import CalendarMonthIcon from 'vue-material-design-icons/CalendarMonth.vue';
import ClockOutlineIcon from 'vue-material-design-icons/ClockOutline.vue';
import ChevronDownIcon from 'vue-material-design-icons/ChevronDown.vue';

import MapMarkerIcon from 'vue-material-design-icons/MapMarker.vue';
import SendOutlineIcon from 'vue-material-design-icons/SendOutline.vue';
import VideoOutlineIcon from 'vue-material-design-icons/VideoOutline.vue';
import CameraIcon from 'vue-material-design-icons/Camera.vue';
import CameraPlusIcon from 'vue-material-design-icons/CameraPlus.vue';
import PlusCircleOutlineIcon from 'vue-material-design-icons/PlusCircleOutline.vue';
import PlusIcon from 'vue-material-design-icons/Plus.vue';
import MinusIcon from 'vue-material-design-icons/Minus.vue';
import LockIcon from 'vue-material-design-icons/Lock.vue';
import EarthIcon from 'vue-material-design-icons/Earth.vue';
import AccountGroupIcon from 'vue-material-design-icons/AccountGroup.vue';
import RefreshIcon from 'vue-material-design-icons/Refresh.vue';
import FormatListBulletedIcon from 'vue-material-design-icons/FormatListBulleted.vue';
import AlertCircleIcon from 'vue-material-design-icons/AlertCircle.vue';
import BaseModal from '@/components/common/BaseModal.vue';
import { useAuthStore } from '@/stores/auth';


const emit = defineEmits(['close', 'create']);

const eventsStore = useEventsStore(); // Initialize the events store

const fileInput = ref(null);
const previewImages = ref([]);
const currentImageIndex = ref(0);

const isNameFocused = ref(false);
const activeDatePicker = ref(null);
const showEndDate = ref(false);
const showLocationModal = ref(false);

const accordions = ref({ cohosts: false, recurring: false, settings: false });
const toggleAccordion = (key) => { accordions.value[key] = !accordions.value[key]; };

const form = ref({
  name: '',
  startDate: new Date(),
  startTime: '10:00',
  endDate: new Date(),
  endTime: '13:00',
  type: 'offline',
  privacy: 'private',
  location: '',
  description: '',
  imageFiles: [],
  frequency: 'Nigdy',
  showGuestList: true
});

const locationModalTitle = computed(() => {
  if (form.value.type === 'online') {
    return 'Wydarzenie online';
  }
  return 'Wybierz lokalizację';
});

const eventTypeOptions = [
  { id: 'offline', title: 'Offline', description: 'Spotkajcie się w określonej lokalizacji.', icon: SendOutlineIcon },
  { id: 'online', title: 'Wirtualne', description: 'Odbądźcie spotkanie wideo lub transmisję na żywo.', icon: VideoOutlineIcon }
];

const privacyOptions = [
  { id: 'private', title: 'Prywatne', desc: 'Tylko zaproszone osoby', icon: LockIcon },
  { id: 'public', title: 'Publiczne', desc: 'Każdy na Facebooku i poza nim', icon: EarthIcon },
  { id: 'group', title: 'Grupa', desc: 'Członkowie grupy', icon: AccountGroupIcon, hasArrow: true }
];
const selectedPrivacyOption = computed(() => privacyOptions.find(opt => opt.id === form.value.privacy));

// --- IMAGE HANDLING ---
const triggerFileInput = () => { fileInput.value.click(); };
const handleFileChange = (event) => {
  const files = event.target.files;
  if (!files) return;
  for (const file of files) {
    if (form.value.imageFiles.length < 10) {
      form.value.imageFiles.push(file);
      previewImages.value.push(URL.createObjectURL(file));
    }
  }
};
const removeImage = () => {
  if (previewImages.value.length === 0) return;
  const indexToRemove = currentImageIndex.value;
  URL.revokeObjectURL(previewImages.value[indexToRemove]);
  previewImages.value.splice(indexToRemove, 1);
  form.value.imageFiles.splice(indexToRemove, 1);
  if (currentImageIndex.value >= previewImages.value.length) {
    currentImageIndex.value = Math.max(0, previewImages.value.length - 1);
  }
  if (fileInput.value) fileInput.value.value = '';
};
const nextImage = () => { if (currentImageIndex.value < previewImages.value.length - 1) currentImageIndex.value++; };
const prevImage = () => { if (currentImageIndex.value > 0) currentImageIndex.value--; };

// --- LOCATION HANDLING ---
const handleLocationConfirm = (newLocation) => {
    form.value.location = newLocation;
};

// --- FORM UTILS ---
const toggleDatePicker = (type) => { activeDatePicker.value = activeDatePicker.value === type ? null : type; };
const formatDate = (date) => { if (!date) return ''; return date.toLocaleDateString('pl-PL', { day: 'numeric', month: 'short', year: 'numeric' }); };
watch(() => form.value.startDate, (newStart) => { if (newStart > form.value.endDate) { form.value.endDate = newStart; } });
const isFormValid = computed(() => { return form.value.name.length > 0; });
const submit = () => {
  const newEvent = {
    id: uuidv4(),
    name: form.value.name,
    startDate: form.value.startDate.toISOString().split('T')[0], // Format to YYYY-MM-DD
    startTime: form.value.startTime,
    endDate: form.value.endDate ? form.value.endDate.toISOString().split('T')[0] : undefined,
    endTime: form.value.endTime,
    type: form.value.type,
    privacy: form.value.privacy,
    location: form.value.location,
    description: form.value.description,
    images: previewImages.value, // These are already URLs
    frequency: form.value.frequency,
    showGuestList: form.value.showGuestList,
  };
  eventsStore.addEvent(newEvent);
  emit('create', newEvent); // Emit the created event
  emit('close'); // Close the modal after creation
};
</script>

<style scoped>

.is-not-in-month{
  display: none !important;
}
</style>
