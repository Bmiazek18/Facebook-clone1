<template>
  <div class="bg-theme-bg-secondary rounded-xl shadow-sm p-4 md:p-5">
    <h3 class="text-[20px] font-bold text-theme-text mb-4">
      {{ t('events.suggestedEvents.title') }}
    </h3>

    <div class="relative group">
      <div
        ref="carouselRef"
        class="flex gap-4 overflow-x-auto snap-x snap-mandatory scroll-smooth scrollbar-hide"
      >
        <div
          v-for="event in suggestedEvents"
          :key="event.id"
          class="snap-start flex-shrink-0 w-full md:w-[calc(50%-0.5rem)] border border-theme-border rounded-xl flex flex-col overflow-hidden bg-theme-bg-secondary"
        >
          <div class="aspect-[4/3] w-full relative overflow-hidden bg-theme-bg-subtle">
            <img
              :src="event.image"
              :alt="event.title"
              class="w-full h-full object-cover hover:scale-105 transition-transform duration-300 cursor-pointer"
            />
          </div>

          <div class="p-3.5 flex flex-col flex-1">
            <div class="text-[#E41E3F] text-[13px] font-medium mb-1 truncate">
              {{ event.date }}
            </div>

            <h4
              class="text-[17px] font-bold leading-tight mb-1 line-clamp-2 text-theme-text hover:underline cursor-pointer"
            >
              {{ event.title }}
            </h4>

            <div class="text-[14px] text-theme-text-secondary mb-1 truncate">
              {{ event.location }}
            </div>

            <div class="text-[13px] text-theme-text-secondary mt-1 mb-3">
              {{ event.interested }}
            </div>

            <div class="flex gap-2 mt-auto pt-1">
              <button
                class="flex-1 bg-theme-bg-tertiary dark:bg-white/10 hover:bg-theme-hover-strong dark:hover:bg-white/20 text-theme-text py-2 rounded-lg font-semibold text-[15px] flex items-center justify-center gap-2 transition-colors"
              >
                <StarOutlineIcon :size="20" /> {{ t('events.suggestedEvents.interestedButton') }}
              </button>

              <button
                class="w-11 flex items-center justify-center bg-theme-bg-tertiary dark:bg-white/10 hover:bg-theme-hover-strong dark:hover:bg-white/20 text-theme-text rounded-lg transition-colors"
              >
                <ReplyIcon :size="20" class="rotate-0 scale-x-[-1]" />
              </button>
            </div>
          </div>
        </div>
      </div>
      <button
        v-if="!isStart"
        @click="scrollLeft"
        class="absolute top-1/2 -translate-y-1/2 left-2 bg-white/80 dark:bg-black/80 p-2 rounded-full shadow-md hover:bg-white dark:hover:bg-black transition-opacity opacity-0 group-hover:opacity-100"
      >
        <ChevronLeftIcon :size="24" />
      </button>
      <button
        v-if="!isEnd"
        @click="scrollRight"
        class="absolute top-1/2 -translate-y-1/2 right-2 bg-white/80 dark:bg-black/80 p-2 rounded-full shadow-md hover:bg-white dark:hover:bg-black transition-opacity opacity-0 group-hover:opacity-100"
      >
        <ChevronRightIcon :size="24" />
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import StarOutlineIcon from 'vue-material-design-icons/StarOutline.vue'
import ReplyIcon from 'vue-material-design-icons/Reply.vue' // Służy jako ikona "Udostępnij" po odwróceniu
import ChevronLeftIcon from 'vue-material-design-icons/ChevronLeft.vue'
import ChevronRightIcon from 'vue-material-design-icons/ChevronRight.vue'
import { useCarousel } from '@/composables/media/useCarousel'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()
const { carouselRef, isStart, isEnd, scrollLeft, scrollRight } = useCarousel(1)

// Przykładowe dane ze zrzutu ekranu
const suggestedEvents = [
  {
    id: 1,
    image: 'https://placehold.co/400x300/333/FFF?text=LINKIN+PARK',
    date: 'Sob, 14 mar o 21:00',
    title: 'LINKIN PARK night // Gdańsk 2026',
    location: 'AK PG Kwadratowa',
    interested: '718 zainteresowanych użytkowników',
  },
  {
    id: 2,
    image: 'https://placehold.co/400x300/4B0082/FFF?text=FIESTA+NIGHT',
    date: 'Czw, 19 mar o 22:00',
    title: 'FIESTA Night - czwartkowa impreza - studenci z listy fb do 23:00 wchodz...',
    location: 'AK PG Kwadratowa',
    interested: '70 zainteresowanych użytkowników',
  },
  // Add more events to make the carousel scrollable
  {
    id: 3,
    image: 'https://placehold.co/400x300/008080/FFF?text=TECH+CONF',
    date: 'Pon, 2 mar o 09:00',
    title: 'Tech Conference 2026',
    location: 'Gdańsk Science and Technology Park',
    interested: '1200 zainteresowanych użytkowników',
  },
  {
    id: 4,
    image: 'https://placehold.co/400x300/FF4500/FFF?text=FOOD+FEST',
    date: 'Pią, 20 mar o 12:00',
    title: 'Gdańsk Food Fest',
    location: 'Forum Gdańsk',
    interested: '2500 zainteresowanych użytkowników',
  },
]
</script>

<style scoped>
.scrollbar-hide::-webkit-scrollbar {
  display: none;
}
.scrollbar-hide {
  -ms-overflow-style: none;
  scrollbar-width: none;
}
</style>
