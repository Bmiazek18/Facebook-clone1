<script setup lang="ts">
import { ref, computed } from 'vue';
import ImageWithGradient from '@/components/media/ImageWithGradient.vue';
import EventsSidebar from '@/components/events/EventsSidebar.vue';
import CreatePost from '@/components/create/createPost/CreateModal.vue';

// Import Icons (Material Design)
import StarIcon from 'vue-material-design-icons/Star.vue';
import CheckCircleIcon from 'vue-material-design-icons/CheckCircle.vue';
import EmailIcon from 'vue-material-design-icons/Email.vue';
import ShareVariantIcon from 'vue-material-design-icons/ShareVariant.vue';
import DotsHorizontalIcon from 'vue-material-design-icons/DotsHorizontal.vue';
import ChevronLeftIcon from 'vue-material-design-icons/ChevronLeft.vue';
import ChevronRightIcon from 'vue-material-design-icons/ChevronRight.vue';
import { useRoute, RouterLink } from 'vue-router';
import { useEventsStore } from '@/stores/events';
import type { Event as EventType } from '@/data/events';

const route = useRoute();
const eventsStore = useEventsStore();
const eventDetails = computed<EventType | undefined>(() => {
  const id = route.params.id as string;
  return eventsStore.getEventById(id);
});

// Computed properties for date formatting
const eventMonth = computed(() => {
  if (!eventDetails.value?.startDate) return '';
  return new Date(eventDetails.value.startDate).toLocaleDateString('pl-PL', { month: 'short' }).toUpperCase();
});

const eventDay = computed(() => {
  if (!eventDetails.value?.startDate) return '';
  return new Date(eventDetails.value.startDate).getDate();
});

const eventDateDisplay = computed(() => {
  if (!eventDetails.value?.startDate) return 'Brak daty';
  const date = new Date(eventDetails.value.startDate);
  const dayNames = ['niedziela', 'poniedziałek', 'wtorek', 'środa', 'czwartek', 'piątek', 'sobota'];
  const monthNames = ['stycznia', 'lutego', 'marca', 'kwietnia', 'maja', 'czerwca',
                      'lipca', 'sierpnia', 'września', 'października', 'listopada', 'grudnia'];
  const dayOfWeek = dayNames[date.getDay()];
  const day = date.getDate();
  const month = monthNames[date.getMonth()];
  const year = date.getFullYear();
  const timeStr = eventDetails.value?.startTime || '';
  const dateFormatted = `${dayOfWeek}, ${day} ${month} ${year}`;
  return `${dateFormatted} ${timeStr ? ' o ' + timeStr : ''}`;
});

// Carousel logic
const currentImageIndex = ref(0);
const currentImage = computed(() => {
  const images = eventDetails.value?.images || [];
  if (images.length === 0) return '';
  return images[currentImageIndex.value];
});
const hasMultipleImages = computed(() => (eventDetails.value?.images?.length || 0) > 1);
const previousImage = () => {
  const imagesCount = eventDetails.value?.images?.length || 0;
  currentImageIndex.value = (currentImageIndex.value - 1 + imagesCount) % imagesCount;
};
const nextImage = () => {
  const imagesCount = eventDetails.value?.images?.length || 0;
  currentImageIndex.value = (currentImageIndex.value + 1) % imagesCount;
};

// Share event logic
const showShareModal = ref(false);
const shareEvent = () => {
  showShareModal.value = true;
};
const closeShareModal = () => {
  showShareModal.value = false;
};
const handlePublishPost = (content: string) => {
  console.log('Published post with event:', { content, eventId: eventDetails.value?.id });
  closeShareModal();
};
</script>

<template>
  <div class="flex min-h-screen bg-theme-bg font-sans text-theme-text pb-10">
    <EventsSidebar />
    <div class="flex-1" v-if="eventDetails">
      <div class="h-[350px] relative flex justify-center items-center overflow-visible shadow-sm group">
        <!-- Carousel -->
        <ImageWithGradient
          :image-url="currentImage || ''"
          :initial-width="700"
          :initial-height="350"
          class="w-full h-full object-cover bg-theme-bg-secondary"
        />
        <button
          v-if="hasMultipleImages"
          @click="previousImage"
          class="absolute left-4 top-1/2 -translate-y-1/2 bg-black/50 hover:bg-black/70 text-white p-2 rounded-full opacity-0 group-hover:opacity-100 transition z-10"
        >
          <ChevronLeftIcon :size="24" />
        </button>
        <button
          v-if="hasMultipleImages"
          @click="nextImage"
          class="absolute right-4 top-1/2 -translate-y-1/2 bg-black/50 hover:bg-black/70 text-white p-2 rounded-full opacity-0 group-hover:opacity-100 transition z-10"
        >
          <ChevronRightIcon :size="24" />
        </button>
        <!-- Date Box -->
        <div class="absolute bottom-12 left-15 transform translate-y-1/2 bg-theme-bg-secondary border border-theme-border rounded-lg shadow-lg w-20 h-20 flex flex-col items-center justify-center shrink-0 overflow-hidden text-center z-20">
          <div class="h-5 w-full bg-red-600 text-white text-[10px] font-bold flex items-center justify-center uppercase tracking-wide">
            {{ eventMonth }}
          </div>
          <div class="text-2xl font-bold text-theme-text-primary leading-none pt-1">
            {{ eventDay }}
          </div>
        </div>
      </div>

      <div class="w-full mx-auto px-4 sm:px-0 relative">
        <div class="bg-theme-bg-secondary pb-4 pt-0 shadow-sm border-b border-theme-border -mt-4 relative z-10 px-15">
          <div class="flex flex-col md:flex-row gap-4 pt-6">
            <div class="grow">
              <div class="text-red-600 text-sm font-semibold uppercase">{{ eventDateDisplay }}</div>
              <h1 class="text-3xl font-bold text-theme-text mt-1">{{ eventDetails?.title || eventDetails?.name }}</h1>
              <div class="text-theme-text-secondary text-sm mt-1 font-medium">
                {{ eventDetails?.locationName || eventDetails?.location }}<span v-if="eventDetails?.address">, {{ eventDetails.address }}</span>
              </div>
            </div>
          </div>

          <div class="flex flex-col md:flex-row justify-between items-center mt-6 border-t border-theme-border pt-4 gap-4">
            <div class="flex gap-6 text-sm font-semibold text-theme-text-secondary">
              <RouterLink :to="{ name: 'event-about', params: { id: eventDetails.id } }"  class="pb-4 -mb-4 px-1 hover:bg-theme-hover rounded-t-sm transition-colors">
                Informacje
              </RouterLink>
              <RouterLink :to="{ name: 'event-discussion', params: { id: eventDetails.id } }"  class="pb-4 -mb-4 px-1 hover:bg-theme-hover rounded-t-sm transition-colors">
                Dyskusja
              </RouterLink>
            </div>
            <div class="flex gap-2 w-full md:w-auto">
              <button class="flex-1 md:flex-none flex items-center justify-center gap-2 bg-theme-bg-subtle hover:bg-theme-hover text-theme-text px-4 py-2 rounded-md font-semibold text-sm transition">
                <StarIcon :size="20" /> Zainteresowany(a)
              </button>
              <button class="flex-1 md:flex-none flex items-center justify-center gap-2 bg-theme-bg-subtle hover:bg-theme-hover text-theme-text px-4 py-2 rounded-md font-semibold text-sm transition">
                <CheckCircleIcon :size="20" /> Wezmę udział
              </button>
               <button class="flex-1 md:flex-none flex items-center justify-center gap-2 bg-theme-bg-subtle hover:bg-theme-hover text-theme-text px-4 py-2 rounded-md font-semibold text-sm transition">
                <EmailIcon :size="20" /> Zaproś
              </button>
              <button @click="shareEvent" class="bg-theme-bg-subtle hover:bg-theme-hover text-theme-text px-3 py-2 rounded-md transition">
                <ShareVariantIcon :size="20" />
              </button>
              <button class="bg-theme-bg-subtle hover:bg-theme-hover text-theme-text px-3 py-2 rounded-md transition">
                <DotsHorizontalIcon :size="20" />
              </button>
            </div>
          </div>
        </div>

        <!-- Router View for nested routes -->
        <router-view :event-details="eventDetails" />
      </div>
    </div>
    <div v-else class="flex-1 flex items-center justify-center">
      <p>Nie znaleziono wydarzenia.</p>
    </div>

    <!-- Share Modal -->
    <Teleport to="body">
      <div v-if="showShareModal" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4" @click.self="closeShareModal">
        <div class="bg-theme-bg-secondary rounded-lg shadow-xl max-w-[500px] w-full max-h-[90vh] overflow-auto">
          <CreatePost
            :shared-event-id="eventDetails?.id"
            @close="closeShareModal"
            @publish="handlePublishPost"
          />
        </div>
      </div>
    </Teleport>
  </div>
</template>

<style scoped>
.router-link-exact-active{
  color: #1877F2 !important;
  border-bottom: 2px;
  border-bottom-color: #1877F2 !important; /* Niebieska linia na dole */
}
</style>
