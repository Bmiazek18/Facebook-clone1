<template>
  <div class="flex min-h-screen bg-[#f0f2f5] font-sans text-[#1c1e21]">
    <EventsSidebar />
    <main class="flex-1 overflow-y-auto bg-white my-2 mr-2 rounded-2xl shadow-sm relative m-5 mx-8">
      <div class="p-8 max-w-[1600px] mx-auto ">

        <header class="mb-8 relative">
          <h2 class="text-[24px] font-bold mb-6">Odkryj wydarzenia</h2>

          <div class="flex flex-wrap gap-2 items-center">
            <button class="bg-[#f0f2f5] hover:bg-[#e4e6eb] px-3 py-2 rounded-lg flex items-center gap-1 font-semibold text-[15px] transition">
              <map-marker-icon :size="18" /> Moja lokalizacja <chevron-down-icon :size="18" />
            </button>

            <div class="relative">
              <button
                @click="toggleMenu"
                :class="[selectedDate !== 'Dowolna data' ? 'bg-[#1877f2] text-white hover:bg-[#166fe5]' : 'bg-[#f0f2f5] hover:bg-[#e4e6eb] text-[#1c1e21]']"
                class="px-3 py-2 rounded-lg flex items-center gap-2 font-semibold text-[15px] transition"
              >
                <div class="flex items-center gap-1">
                  <calendar-month-icon :size="18" />
                  {{ selectedDate }}
                </div>
                <close-icon v-if="selectedDate !== 'Dowolna data'" @click.stop="resetDate" :size="18" class="hover:bg-white/20 rounded-full p-0.5" />
                <chevron-down-icon v-else :size="18" />
              </button>

              <div v-if="isDateMenuOpen" class="absolute top-full left-0 mt-2 w-[340px] bg-white rounded-xl shadow-[0_12px_28px_rgba(0,0,0,0.15)] border border-gray-200 overflow-hidden z-50">

                <div v-if="currentMenuView === 'list'" class="py-2">
                  <div v-for="option in dateOptions" :key="option"
                       @click="selectQuickDate(option)"
                       class="flex items-center justify-between px-4 py-3 hover:bg-[#f0f2f5] cursor-pointer transition">
                    <span class="text-[15px] font-semibold">{{ option }}</span>
                    <div class="w-5 h-5 rounded-full border-2 flex items-center justify-center"
                         :class="selectedDate === option ? 'border-[#1877f2]' : 'border-gray-400'">
                      <div v-if="selectedDate === option" class="w-2.5 h-2.5 rounded-full bg-[#1877f2]"></div>
                    </div>
                  </div>
                  <div class="border-t border-gray-200 my-1"></div>
                  <div @click="currentMenuView = 'calendar'" class="flex items-center justify-between px-4 py-3 hover:bg-[#f0f2f5] cursor-pointer transition">
                    <span class="text-[15px] font-semibold">Niestandardowy zakres dat</span>
                    <chevron-right-icon :size="20" class="text-[#65676b]" />
                  </div>
                </div>

                <div v-else class="flex flex-col bg-white">
                  <div class="flex items-center p-2 border-b border-gray-200">
                    <button @click="currentMenuView = 'list'" class="p-2 hover:bg-gray-100 rounded-full transition">
                      <arrow-left-icon :size="24" class="text-[#1c1e21]" />
                    </button>
                    <span class="ml-2 font-bold text-[20px]">Niestandardowy zakres dat</span>
                  </div>
                  <div class="p-4 custom-calendar">
                    <VDatePicker v-model="range" is-range color="blue" borderless transparent title-position="center" locale="pl" :disabled-dates="disabledDates"/>
                    <button
                      @click="applyCustomDate"
                      :disabled="!range"
                      :class="[range ? 'bg-[#1877f2] text-white hover:bg-[#166fe5]' : 'bg-[#e4e6eb] text-[#bcc0c4] cursor-not-allowed']"
                      class="w-full mt-6 font-bold py-2 rounded-lg text-[15px] transition"
                    >
                      Zastosuj
                    </button>
                  </div>
                </div>
              </div>
            </div>

            <button class="bg-[#f0f2f5] hover:bg-[#e4e6eb] px-4 py-2 rounded-full font-semibold text-[15px]">Najpopularniejsze</button>
            <button class="bg-[#ebf5ff] text-[#1877f2] px-4 py-2 rounded-full font-semibold text-[15px]">Znajomi</button>
          </div>
        </header>

        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4">
          <div v-for="event in events" :key="event.id" class="bg-white rounded-[18px] border border-gray-200 shadow-sm overflow-hidden flex flex-col hover:shadow-md transition duration-200">
            <div class="relative aspect-video w-full bg-gray-100">
              <img :src="event.image" class="w-full h-full object-cover" />
              <button class="absolute top-2 right-2 bg-[#1c1e21]/70 hover:bg-[#1c1e21] text-white p-1.5 rounded-full">
                <dots-horizontal-icon :size="18" />
              </button>
            </div>

            <div class="p-4 pt-3 flex flex-col flex-1">
              <p :class="[event.isLive ? 'text-[#d22d2d]' : 'text-[#1c1e21]', 'text-[14px] font-semibold mb-1']">{{ event.dateLabel }}</p>
              <h3 class="text-[17px] font-bold leading-tight mb-1 text-[#1c1e21] line-clamp-2 hover:underline cursor-pointer">{{ event.title }}</h3>
              <p class="text-[#65676b] text-[15px] font-medium mb-1">{{ event.location }}</p>

              <div v-if="event.friend" class="flex items-center gap-2 mt-2 mb-4">
                <img :src="event.friend.avatar" class="w-7 h-7 rounded-full object-cover border border-gray-200" />
                <span class="text-[#65676b] text-[14px] font-normal truncate">{{ event.friend.name }} jest zainteresowany(a)</span>
              </div>
              <p v-else class="text-[#65676b] text-[14px] mb-4">{{ event.stats }}</p>

              <div class="flex gap-2 mt-auto">
                <button class="flex-[4] bg-[#e4e6eb] hover:bg-[#d8dadf] text-[#1c1e21] font-semibold py-2 rounded-xl flex items-center justify-center gap-2 transition text-[15px]">
                  <star-outline-icon :size="20" /> Zainteresowany(a)
                </button>
                <button class="flex-1 bg-[#e4e6eb] hover:bg-[#d8dadf] text-[#1c1e21] flex items-center justify-center rounded-xl transition">
                  <reply-icon :size="22" class="transform scale-x-[-1]" />
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </main>
  </div>
  <BaseModal v-if="isOpen" @close="isOpen = false" :title="'Utwórz wydarzenie'">
    <CreateEventModal @close="isOpen = false" />
  </BaseModal>
</template>


<script setup>
import { ref } from 'vue';
import { DatePicker as VDatePicker } from 'v-calendar';
import 'v-calendar/dist/style.css';

/* Importy ikon */
import MapMarkerIcon from 'vue-material-design-icons/MapMarker.vue';
import CalendarMonthIcon from 'vue-material-design-icons/CalendarMonth.vue';
import ChevronDownIcon from 'vue-material-design-icons/ChevronDown.vue';
import ChevronRightIcon from 'vue-material-design-icons/ChevronRight.vue';
import ArrowLeftIcon from 'vue-material-design-icons/ArrowLeft.vue';
import CloseIcon from 'vue-material-design-icons/Close.vue';
import DotsHorizontalIcon from 'vue-material-design-icons/DotsHorizontal.vue';
import StarOutlineIcon from 'vue-material-design-icons/StarOutline.vue';
import ReplyIcon from 'vue-material-design-icons/Reply.vue';
import EventsSidebar from '@/components/events/EventsSidebar.vue';

// Logika filtrów
const isDateMenuOpen = ref(false);
const currentMenuView = ref('list');
const selectedDate = ref('Dowolna data');
const range = ref(null);
const dateOptions = ['Dowolna data', 'Dzisiaj', 'Jutro', 'W ten weekend', 'W tym tygodniu', 'W przyszłym tygodniu', 'W tym miesiącu'];

const toggleMenu = () => {
  isDateMenuOpen.value = !isDateMenuOpen.value;
  if (!isDateMenuOpen.value) currentMenuView.value = 'list';
};

const selectQuickDate = (val) => {
  selectedDate.value = val;
  isDateMenuOpen.value = false;
};

const applyCustomDate = () => {
  if (range.value) {
    const start = range.value.start.toLocaleDateString('pl-PL', { day: 'numeric', month: 'short' });
    const end = range.value.end.toLocaleDateString('pl-PL', { day: 'numeric', month: 'short' });
    selectedDate.value = `${start} – ${end}`;
    isDateMenuOpen.value = false;
    currentMenuView.value = 'list';
  }
};

const resetDate = () => {
  selectedDate.value = 'Dowolna data';
  range.value = null;
};

const disabledDates = ref([
  {
    start: null, // Blokada zaczyna się od teraz
    end: new Date()          // null oznacza brak końca blokady (wszystkie przyszłe daty)
  }
]);
const events = [
  { id: 1, dateLabel: 'W trakcie', isLive: true, title: 'Bal Swobodnych Dusz', location: 'Pałac Zamoyskich', stats: '580 osób zainteresowanych', image: 'https://picsum.photos/seed/ev1/600/350' },
  { id: 2, dateLabel: 'Nie, 28 gru – 2 sty', title: 'Sylwester z Rodziną Addamsów MCHTR x MT 2025', location: 'Piwniczna-Zdrój', friend: { name: 'Bartosz', avatar: 'https://i.pravatar.cc/100?u=1' }, image: 'https://picsum.photos/seed/ev2/600/350' },
  { id: 3, dateLabel: 'Sob, 27 gru o 21:00', title: '27.12 | WHITE 2115 | SIEDLCE', location: 'Siedlce', friend: { name: 'Maja', avatar: 'https://i.pravatar.cc/100?u=2' }, image: 'https://picsum.photos/seed/ev3/600/350' },
  { id: 4, dateLabel: 'Czw, 1 sty o 17:00', title: 'Likwidacja Lecha Poznań - Spotkanie', location: 'Poznań', stats: 'Dawid i 2 znajomych bierze udział', image: 'https://picsum.photos/seed/ev4/600/350' },
  { id: 5, dateLabel: 'Sob, 24 sty o 19:00', title: 'Wielki Studencki Bal Karnawałowy KARNAVAULI', location: 'Gmach Główny PW', friend: { name: 'Tomek', avatar: 'https://i.pravatar.cc/100?u=3' }, image: 'https://picsum.photos/seed/ev5/600/350' },
  { id: 6, dateLabel: 'Śr, 31 gru o 20:00', title: 'Sylwester na Placu Zamkowym', location: 'Warszawa', stats: '12,5 tys. osób zainteresowanych', image: 'https://picsum.photos/seed/ev6/600/350' },
  { id: 7, dateLabel: 'Pt, 2 sty o 10:00', title: 'Warsztaty: Fotografia Analogowa', location: 'Łódź, Off Piotrkowska', stats: 'Zostały 3 miejsca', image: 'https://picsum.photos/seed/ev7/600/350' },
  { id: 8, dateLabel: 'Nie, 4 sty o 16:00', title: 'Koncert Muzyki Filmowej - Hans Zimmer', location: 'Atlas Arena, Łódź', stats: '6 znajomych jest zainteresowanych', image: 'https://picsum.photos/seed/ev8/600/350' },
];
</script>

<style>
.custom-calendar .vc-container { border: none; font-family: inherit; }
.custom-calendar .vc-title { font-size: 17px; font-weight: 700; }
.custom-calendar .vc-weekday { color: #65676b; font-size: 13px; font-weight: 400; }
.custom-calendar .vc-day-content { font-size: 15px; width: 38px; height: 38px; }
.custom-calendar .vc-highlight { background-color: #f0f2f5 !important; }
.custom-calendar .vc-highlight-content-outline { background-color: #1877f2 !important; color: white !important; }

</style>
