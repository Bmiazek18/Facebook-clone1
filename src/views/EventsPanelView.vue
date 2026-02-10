<template>
  <div class="flex h-screen bg-theme-bg font-sans text-theme-text overflow-hidden">
    <EventsSidebar />

    <div class="flex-1 flex justify-center overflow-y-auto pt-[64px] pb-10 bg-theme-bg">

      <main class="w-full max-w-[1900px] h-fit bg-theme-bg-secondary my-4 mx-4 md:mx-10 rounded-2xl shadow-sm relative border border-theme-border">
        <div class="p-6 md:p-8">

          <header class="mb-8 relative">
            <h2 class="text-[24px] font-bold mb-6">Odkryj wydarzenia</h2>

            <div class="flex flex-wrap gap-2 items-center">
              <button class="flex items-center gap-2 px-3 py-2 bg-theme-bg-subtle hover:bg-theme-hover rounded-lg font-semibold text-[15px] transition-colors text-theme-text border border-transparent">
                <MapMarkerIcon :size="18" />
                <span>Moja lokalizacja</span>
                <ChevronDownIcon :size="18" />
              </button>

              <div class="relative">
                <button
                  @click="toggleMenu"
                  :class="[
                    'flex items-center gap-2 px-3 py-2 rounded-lg font-semibold text-[15px] transition-colors border border-transparent',
                    selectedDate !== 'Dowolna data'
                      ? 'bg-theme-primary text-white'
                      : 'bg-theme-bg-subtle hover:bg-theme-hover text-theme-text'
                  ]"
                >
                  <CalendarMonthIcon :size="18" />
                  <span>{{ selectedDate }}</span>
                  <CloseIcon v-if="selectedDate !== 'Dowolna data'" @click.stop="resetDate" :size="18" class="hover:bg-white/20 rounded-full" />
                  <ChevronDownIcon v-else :size="18" />
                </button>

                <div v-if="isDateMenuOpen" class="absolute top-full left-0 mt-2 w-[340px] bg-theme-bg-secondary rounded-xl shadow-2xl border border-theme-border overflow-hidden z-50">
                  <div v-if="currentMenuView === 'list'" class="py-2">
                    <div v-for="option in dateOptions" :key="option"
                         @click="selectQuickDate(option)"
                         class="flex items-center justify-between px-4 py-3 hover:bg-theme-bg-subtle cursor-pointer transition">
                      <span class="text-[15px] font-semibold">{{ option }}</span>
                      <div class="w-5 h-5 rounded-full border-2 flex items-center justify-center transition-colors"
                           :class="selectedDate === option ? 'border-theme-primary' : 'border-theme-border'">
                        <div v-if="selectedDate === option" class="w-2.5 h-2.5 rounded-full bg-theme-primary"></div>
                      </div>
                    </div>
                    <div class="border-t border-theme-border my-1"></div>
                    <div @click="currentMenuView = 'calendar'" class="flex items-center justify-between px-4 py-3 hover:bg-theme-bg-subtle cursor-pointer transition">
                      <span class="text-[15px] font-semibold">Niestandardowy zakres dat</span>
                      <ChevronRightIcon :size="20" class="text-theme-text-secondary" />
                    </div>
                  </div>

                  <div v-else class="flex flex-col bg-theme-bg-secondary">
                    <div class="flex items-center p-2 border-b border-theme-border">
                      <button @click="currentMenuView = 'list'" class="p-2 hover:bg-theme-hover rounded-full transition text-theme-text">
                        <ArrowLeftIcon :size="24" />
                      </button>
                      <span class="ml-2 font-bold text-[18px]">Wybierz datę</span>
                    </div>
                    <div class="p-4 custom-calendar">
                      <VDatePicker v-model="range" is-range color="blue" borderless transparent locale="pl" :disabled-dates="disabledDates"/>
                      <button
                        @click="applyCustomDate"
                        :disabled="!range"
                        class="w-full mt-4 font-bold py-2.5 rounded-lg text-[15px] transition"
                        :class="[range ? 'bg-theme-primary text-white' : 'bg-theme-bg-subtle text-theme-text-secondary cursor-not-allowed']"
                      >
                        Zastosuj
                      </button>
                    </div>
                  </div>
                </div>
              </div>

              <button class="px-4 py-2 bg-theme-bg-subtle hover:bg-theme-hover text-theme-text rounded-full font-semibold text-[15px] transition-colors">Najpopularniejsze</button>
              <button class="px-4 py-2 bg-theme-primary-subtle text-theme-primary rounded-full font-semibold text-[15px] transition-colors">Znajomi</button>
            </div>
          </header>

          <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4">
            <div
              v-for="eventItem in events"
              :key="eventItem.id"
              class="group bg-theme-bg-secondary rounded-[18px] border border-theme-border shadow-sm overflow-hidden flex flex-col transition duration-200 cursor-pointer"
              @click="navigateToEvent(eventItem.id)"
            >
              <div class="relative aspect-video w-full bg-theme-bg-subtle overflow-hidden">
                <img :src="eventItem.images[0]" class="w-full h-full object-cover transition-transform duration-500 group-hover:scale-105" v-if="eventItem.images?.length" />
                <div v-else class="w-full h-full flex items-center justify-center text-theme-text-secondary">Brak zdjęcia</div>
                <button class="absolute top-2 right-2 bg-black/50 hover:bg-black/70 text-white p-2 rounded-full transition">
                  <DotsHorizontalIcon :size="18" />
                </button>
              </div>

              <div class="p-4 flex flex-1 flex-col">
                <p class="text-theme-primary text-[13px] font-bold mb-1 uppercase tracking-wide">
                  {{ eventItem.startDate }} · {{ eventItem.startTime }}
                </p>
                <h3 class="text-[17px] font-bold leading-tight mb-1 text-theme-text line-clamp-2 group-hover:underline">
                  {{ eventItem.name }}
                </h3>
                <p class="text-theme-text-secondary text-[14px] mb-4">{{ eventItem.location }}</p>

                <div class="flex gap-2 mt-auto">
                  <button class="flex-[4] flex items-center justify-center gap-2 py-2 bg-theme-bg-subtle hover:bg-theme-hover text-theme-text font-semibold rounded-xl transition text-[15px]">
                    <StarOutlineIcon :size="20" /> Zainteresowany(a)
                  </button>
                  <button class="flex-1 flex items-center justify-center py-2 bg-theme-bg-subtle hover:bg-theme-hover text-theme-text rounded-xl transition">
                    <ReplyIcon :size="22" class="scale-x-[-1]" />
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </main>
    </div>
  </div>

  <BaseModal v-if="isOpen" @close="isOpen = false" title="Utwórz wydarzenie">
    <CreateEventModal @close="isOpen = false" />
  </BaseModal>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import { DatePicker as VDatePicker } from 'v-calendar';
import 'v-calendar/dist/style.css';
import { useEventsStore } from '@/stores/events';
import { useRouter } from 'vue-router';
import EventsSidebar from '@/components/events/EventsSidebar.vue';
import CreateEventModal from '@/components/events/CreateEventModal.vue';

/* Ikony */
import MapMarkerIcon from 'vue-material-design-icons/MapMarker.vue';
import CalendarMonthIcon from 'vue-material-design-icons/CalendarMonth.vue';
import ChevronDownIcon from 'vue-material-design-icons/ChevronDown.vue';
import ChevronRightIcon from 'vue-material-design-icons/ChevronRight.vue';
import ArrowLeftIcon from 'vue-material-design-icons/ArrowLeft.vue';
import CloseIcon from 'vue-material-design-icons/Close.vue';
import DotsHorizontalIcon from 'vue-material-design-icons/DotsHorizontal.vue';
import StarOutlineIcon from 'vue-material-design-icons/StarOutline.vue';
import ReplyIcon from 'vue-material-design-icons/Reply.vue';

const eventsStore = useEventsStore();
const router = useRouter();

const isDateMenuOpen = ref(false);
const currentMenuView = ref('list');
const selectedDate = ref('Dowolna data');
const range = ref(null);
const isOpen = ref(false);

const dateOptions = ['Dowolna data', 'Dzisiaj', 'Jutro', 'W ten weekend', 'W tym tygodniu', 'W przyszłym tygodniu', 'W tym miesiącu'];

const toggleMenu = () => {
  isDateMenuOpen.value = !isDateMenuOpen.value;
  if (!isDateMenuOpen.value) currentMenuView.value = 'list';
};

const selectQuickDate = (val: string) => {
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

const disabledDates = ref([{ start: null, end: new Date() }]);
const events = computed(() => eventsStore.events);

const navigateToEvent = (eventId: string) => {
  router.push({ name: 'event', params: { id: eventId } });
};
</script>

<style scoped>
/* Kalendarz VCalendar customizacja bez @apply */
.custom-calendar :deep(.vc-container) {
  border: none;
  background: transparent;
}

.custom-calendar :deep(.vc-title) {
  color: var(--color-text); /* Użycie zmiennej CSS zamiast JS */
  font-weight: 700;
}

.custom-calendar :deep(.vc-day-content) {
  color: var(--color-text);
}

/* Wymuszenie koloru dla wszystkich ikon w tym komponencie */
:deep(svg) {
  fill: currentColor !important;
}

/* Poprawka dla zachowania proporcji i animacji zdjęć */
.aspect-video {
  position: relative;
  overflow: hidden;
}

button:active {
  transform: scale(0.97);
}
</style>
