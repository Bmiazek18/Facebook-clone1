<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import ImageWithGradient from '@/components/media/ImageWithGradient.vue'
import EventsSidebar from '@/components/events/EventsSidebar.vue'
import CreatePost from '@/components/create/createPost/CreateModal.vue'

// Import Icons (Material Design)
import StarIcon from 'vue-material-design-icons/Star.vue'
import CheckCircleIcon from 'vue-material-design-icons/CheckCircle.vue'
import EmailIcon from 'vue-material-design-icons/Email.vue'
import ShareVariantIcon from 'vue-material-design-icons/ShareVariant.vue'
import DotsHorizontalIcon from 'vue-material-design-icons/DotsHorizontal.vue'
import ChevronLeftIcon from 'vue-material-design-icons/ChevronLeft.vue'
import ChevronRightIcon from 'vue-material-design-icons/ChevronRight.vue'
import { useEventsStore } from '@/stores/events'
import type { Event as EventType } from '@/types/Event'
import BaseModal from '@/components/common/BaseModal.vue'
import InviteModal from '@/components/events/InviteModal.vue'
import EventShareDropdown from '@/components/events/EventShareDropdown.vue'

const route = useRoute()
const eventsStore = useEventsStore()
const eventDetails = computed<EventType | undefined>(() => {
  const id = route.params.id as string
  return eventsStore.getEventById(id)
})

const fetchCurrentEvent = async () => {
  const id = route.params.id as string
  if (id) {
    await eventsStore.fetchEventById(id)
  }
}

onMounted(() => {
  fetchCurrentEvent()
})

watch(
  () => route.params.id,
  () => {
    fetchCurrentEvent()
  }
)

// Computed properties for date formatting
const eventMonth = computed(() => {
  if (!eventDetails.value?.startDate) return ''
  return new Date(eventDetails.value.startDate)
    .toLocaleDateString('pl-PL', { month: 'short' })
    .toUpperCase()
})

const eventDay = computed(() => {
  if (!eventDetails.value?.startDate) return ''
  return new Date(eventDetails.value.startDate).getDate()
})

const eventDateDisplay = computed(() => {
  if (!eventDetails.value?.startDate) return 'Brak daty'
  const date = new Date(eventDetails.value.startDate)
  const dayNames = ['niedziela', 'poniedziałek', 'wtorek', 'środa', 'czwartek', 'piątek', 'sobota']
  const monthNames = [
    'stycznia',
    'lutego',
    'marca',
    'kwietnia',
    'maja',
    'czerwca',
    'lipca',
    'sierpnia',
    'września',
    'października',
    'listopada',
    'grudnia',
  ]
  const dayOfWeek = dayNames[date.getDay()]
  const day = date.getDate()
  const month = monthNames[date.getMonth()]
  const year = date.getFullYear()
  const timeStr = eventDetails.value?.startTime || ''
  const dateFormatted = `${dayOfWeek}, ${day} ${month} ${year}`
  return `${dateFormatted} ${timeStr ? ' o ' + timeStr : ''}`
})
const isInviteModalOpen = ref(false)

// Carousel logic
const currentImageIndex = ref(0)
const currentImage = computed(() => {
  const images = eventDetails.value?.images || []
  if (images.length === 0) return ''
  return images[currentImageIndex.value]
})
const hasMultipleImages = computed(() => (eventDetails.value?.images?.length || 0) > 1)
const previousImage = () => {
  const imagesCount = eventDetails.value?.images?.length || 0
  currentImageIndex.value = (currentImageIndex.value - 1 + imagesCount) % imagesCount
}
const nextImage = () => {
  const imagesCount = eventDetails.value?.images?.length || 0
  currentImageIndex.value = (currentImageIndex.value + 1) % imagesCount
}


</script>

<template>
  <div class="flex min-h-screen bg-theme-bg   text-theme-text pb-10">
    <EventsSidebar />
    <div class="flex-1 flex flex-col">
      <div v-if="eventDetails">
        <div
          class="h-[350px] mt-[74px] relative flex justify-center items-center overflow-visible shadow-sm group"
        >
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
        </div>

        <div class="w-full mx-auto px-4 sm:px-0 relative">
          <div
            class="bg-theme-bg-secondary pb-4 pt-0 shadow-sm border-b border-theme-border -mt-4 relative z-10 px-15"
          >
            <div class="w-[1200px] relative mx-auto">
              <div
                class="absolute -top-28 left-0 transform translate-y-1/2 bg-theme-bg-secondary rounded-lg shadow-lg w-20 h-20 flex flex-col shrink-0 overflow-hidden text-center z-20"
              >
                <div
                  class="h-5 w-full bg-red-600 text-white text-[10px] font-bold flex items-center justify-center uppercase tracking-wide"
                ></div>
                <div
                  class="text-[40px] flex font-semibold text-theme-text-primary h-full leading-none justify-center items-center"
                >
                  {{ eventDay }}
                </div>
              </div>

              <div class="flex flex-col md:flex-row gap-4 pt-6">
                <div class="grow">
                  <div class="text-red-600 text-sm font-semibold uppercase">
                    {{ eventDateDisplay }}
                  </div>
                  <h1 class="text-3xl font-bold text-theme-text mt-1">
                    {{ eventDetails?.title || eventDetails?.name }}
                  </h1>
                  <div class="text-theme-text-secondary text-sm mt-1 font-medium">
                    {{ eventDetails?.locationName || eventDetails?.location
                    }}<span v-if="eventDetails?.address">{{ $t('events.eventdetailsAddress') }}</span>
                  </div>
                </div>
              </div>

              <div
                class="flex flex-col md:flex-row justify-between items-center mt-6 border-t border-theme-border pt-4 gap-4"
              >
                <div class="flex gap-6 text-sm font-semibold text-theme-text-secondary">
                  <NuxtLink
                    :to="`/event/${eventDetails.id}`"
                    class="pb-4 -mb-4 px-1 hover:bg-theme-hover rounded-t-sm transition-colors"
                  >{{ $t('groups.information') }}</NuxtLink>
                  <NuxtLink
                    :to="`/event/${eventDetails.id}/discussion`"
                    class="pb-4 -mb-4 px-1 hover:bg-theme-hover rounded-t-sm transition-colors"
                  >{{ $t('groups.discussion') }}</NuxtLink>
                </div>
                <div class="flex gap-2 w-full md:w-auto">
                  <button
                    class="flex-1 md:flex-none flex items-center justify-center gap-2 bg-theme-bg-subtle hover:bg-theme-hover text-theme-text px-4 py-2 rounded-md font-semibold text-sm transition"
                  >
                    <StarIcon :size="20" />{{ $t('events.zainteresowanyA') }}</button>
                  <button
                    class="flex-1 md:flex-none flex items-center justify-center gap-2 bg-theme-bg-subtle hover:bg-theme-hover text-theme-text px-4 py-2 rounded-md font-semibold text-sm transition"
                  >
                    <CheckCircleIcon :size="20" />{{ $t('events.suggestedEvents.interestedButton') }}</button>
                  <button
                    @click="isInviteModalOpen = true"
                    class="flex-1 md:flex-none flex items-center justify-center gap-2 bg-theme-bg-subtle hover:bg-theme-hover text-theme-text px-4 py-2 rounded-md font-semibold text-sm transition"
                  >
                    <EmailIcon :size="20" />{{ $t('groups.invite') }}</button>
                  <EventShareDropdown :event="eventDetails" not-title />
                  <button
                    class="bg-theme-bg-subtle hover:bg-theme-hover text-theme-text px-3 py-2 rounded-md transition"
                  >
                    <DotsHorizontalIcon :size="20" />
                  </button>
                </div>
              </div>
            </div>
          </div>

          <!-- Router View for nested routes -->
          <NuxtPage :event-details="eventDetails" />
        </div>
      </div>
      <div v-else class="flex-1 flex items-center justify-center">
        <p>{{ $t('events.nieZnalezionoWydarzenia') }}</p>
      </div>
      <BaseModal
        v-if="isInviteModalOpen"
        @close="isInviteModalOpen = false"
        :title="'Zaproś znajomych'"
      >
        <InviteModal />
      </BaseModal>

    </div>
  </div>
</template>

<style scoped>
.router-link-exact-active {
  color: #1877f2 !important;
  border-bottom: 3px solid #1877f2 !important; /* Dodane 'solid' i zmieniono na 3px dla lepszej widoczności */
}
</style>
