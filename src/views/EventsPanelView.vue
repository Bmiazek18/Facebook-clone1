<template>
  <div class="flex min-h-screen bg-theme-bg font-sans text-theme-text">
    <EventsSidebar />
    <main class="flex-1 overflow-y-auto bg-theme-bg-secondary my-2 mr-2 rounded-2xl shadow-sm relative m-5 mx-8">
      <div class="p-8 max-w-[1600px] mx-auto ">

        <header class="mb-8 relative">
          <h2 class="text-[24px] font-bold mb-6">Odkryj wydarzenia</h2>

          <div class="flex flex-wrap gap-2 items-center">
            <button class="bg-theme-bg-subtle hover:bg-theme-hover px-3 py-2 rounded-lg flex items-center gap-1 font-semibold text-[15px] transition">
              <map-marker-icon :size="18" /> Moja lokalizacja <chevron-down-icon :size="18" />
            </button>

            <div class="relative">
              <button
                @click="toggleMenu"
                :class="[selectedDate !== 'Dowolna data' ? 'bg-theme-primary text-white hover:bg-theme-primary-hover' : 'bg-theme-bg-subtle hover:bg-theme-hover text-theme-text']"
                class="px-3 py-2 rounded-lg flex items-center gap-2 font-semibold text-[15px] transition"
              >
                <div class="flex items-center gap-1">
                  <calendar-month-icon :size="18" />
                  {{ selectedDate }}
                </div>
                <close-icon v-if="selectedDate !== 'Dowolna data'" @click.stop="resetDate" :size="18" class="hover:bg-white/20 rounded-full p-0.5" />
                <chevron-down-icon v-else :size="18" />
              </button>

              <div v-if="isDateMenuOpen" class="absolute top-full left-0 mt-2 w-[340px] bg-theme-bg-secondary rounded-xl shadow-[0_12px_28px_rgba(0,0,0,0.15)] border border-theme-border overflow-hidden z-50">

                <div v-if="currentMenuView === 'list'" class="py-2">
                  <div v-for="option in dateOptions" :key="option"
                       @click="selectQuickDate(option)"
                       class="flex items-center justify-between px-4 py-3 hover:bg-theme-bg-subtle cursor-pointer transition">
                    <span class="text-[15px] font-semibold">{{ option }}</span>
                    <div class="w-5 h-5 rounded-full border-2 flex items-center justify-center"
                         :class="selectedDate === option ? 'border-theme-primary' : 'border-theme-border'">
                      <div v-if="selectedDate === option" class="w-2.5 h-2.5 rounded-full bg-theme-primary"></div>
                    </div>
                  </div>
                  <div class="border-t border-theme-border my-1"></div>
                  <div @click="currentMenuView = 'calendar'" class="flex items-center justify-between px-4 py-3 hover:bg-theme-bg-subtle cursor-pointer transition">
                    <span class="text-[15px] font-semibold">Niestandardowy zakres dat</span>
                    <chevron-right-icon :size="20" class="text-theme-text-secondary" />
                  </div>
                </div>

                <div v-else class="flex flex-col bg-theme-bg-secondary">
                  <div class="flex items-center p-2 border-b border-theme-border">
                    <button @click="currentMenuView = 'list'" class="p-2 hover:bg-theme-hover rounded-full transition">
                      <arrow-left-icon :size="24" class="text-theme-text" />
                    </button>
                    <span class="ml-2 font-bold text-[20px]">Niestandardowy zakres dat</span>
                  </div>
                  <div class="p-4 custom-calendar">
                    <VDatePicker v-model="range" is-range color="blue" borderless transparent title-position="center" locale="pl" :disabled-dates="disabledDates"/>
                    <button
                      @click="applyCustomDate"
                      :disabled="!range"
                      :class="[range ? 'bg-theme-primary text-white hover:bg-theme-primary-hover' : 'bg-theme-hover text-theme-text-muted cursor-not-allowed']"
                      class="w-full mt-6 font-bold py-2 rounded-lg text-[15px] transition"
                    >
                      Zastosuj
                    </button>
                  </div>
                </div>
              </div>
            </div>

            <button class="bg-theme-bg-subtle hover:bg-theme-hover px-4 py-2 rounded-full font-semibold text-[15px]">Najpopularniejsze</button>
            <button class="bg-theme-primary-subtle text-theme-primary px-4 py-2 rounded-full font-semibold text-[15px]">Znajomi</button>
          </div>
        </header>

        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4">
          <div
            v-for="eventItem in events"
            :key="eventItem.id"
            class="bg-theme-bg-secondary rounded-[18px] border border-theme-border shadow-sm overflow-hidden flex flex-col hover:shadow-md transition duration-200 cursor-pointer"
            @click="navigateToEvent(eventItem.id)"
          >
            <div class="relative aspect-video w-full bg-theme-bg-subtle">
              <img :src="eventItem.images[0]" class="w-full h-full object-cover" v-if="eventItem.images && eventItem.images.length > 0"/>
              <div v-else class="w-full h-full object-cover flex items-center justify-center text-theme-text-secondary">Brak zdjęcia</div>
              <button class="absolute top-2 right-2 bg-theme-text/70 hover:bg-theme-text text-white p-1.5 rounded-full">
                <dots-horizontal-icon :size="18" />
              </button>
            </div>

            <div class="p-4 pt-3 flex flex-col flex-1">
              <p :class="['text-theme-text', 'text-[14px] font-semibold mb-1']">{{ eventItem.startDate }} {{ eventItem.startTime }}</p>
              <h3 class="text-[17px] font-bold leading-tight mb-1 text-theme-text line-clamp-2 hover:underline cursor-pointer">{{ eventItem.name }}</h3>
              <p class="text-theme-text-secondary text-[15px] font-medium mb-1">{{ eventItem.location }}</p>

              <div class="flex gap-2 mt-auto">
                <button class="flex-[4] bg-theme-hover-strong hover:bg-theme-hover-strong text-theme-text font-semibold py-2 rounded-xl flex items-center justify-center gap-2 transition text-[15px]">
                  <star-outline-icon :size="20" /> Zainteresowany(a)
                </button>
                <button class="flex-1 bg-theme-hover-strong hover:bg-theme-hover-strong text-theme-text flex items-center justify-center rounded-xl transition">
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


<script setup lang="ts">
import { ref, computed } from 'vue'; // Added computed for events from store
import { DatePicker as VDatePicker } from 'v-calendar';
import 'v-calendar/dist/style.css';
import { useEventsStore } from '@/stores/events'; // Import the events store
import { useRouter } from 'vue-router'; // Import useRouter
import EventsSidebar from '@/components/events/EventsSidebar.vue';
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


const eventsStore = useEventsStore(); // Initialize the events store
const router = useRouter(); // Initialize router

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

const events = computed(() => eventsStore.events); // Use events from the store

const navigateToEvent = (eventId) => {
  router.push({ name: 'event', params: { id: eventId } });
};

</script>

<style>
.custom-calendar .vc-container { border: none; font-family: inherit; }
.custom-calendar .vc-title { font-size: 17px; font-weight: 700; }
.custom-calendar .vc-weekday { color: var(--color-text-secondary); font-size: 13px; font-weight: 400; }
.custom-calendar .vc-day-content { font-size: 15px; width: 38px; height: 38px; }
.custom-calendar .vc-highlight { background-color: var(--color-bg) !important; }
.custom-calendar .vc-highlight-content-outline { background-color: var(--color-primary) !important; color: white !important; }

</style>
