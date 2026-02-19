<template>
  <div class="py-4 pl-4 bg-theme-bg-secondary rounded-none lg:rounded-lg shadow-md max-w-4xl mx-0 lg:mx-auto">

    <div class="flex justify-between items-center mb-4">
      <div class="flex items-center text-theme-text">
        <AccountGroupIcon :size="24" class="mr-2" fillColor="var(--color-text-secondary)" />
        <h2 class="text-[15px] font-semibold">{{ $t('home.peopleYouMayKnow') }}</h2>
      </div>
      <button class="text-theme-text-secondary hover:text-theme-text">
        <DotsHorizontalIcon :size="24" />
      </button>
    </div>

    <div class="relative">

      <button
        v-if="!isStart"
        @click="scrollLeft"
        class="absolute top-1/2 left-0 transform -translate-y-1/2 p-2 bg-theme-bg-secondary rounded-full shadow-lg border border-theme-border hover:bg-theme-hover transition duration-150 z-10"
        style="margin-left: 24px;" >
        <ChevronLeftIcon :size="24" fillColor="var(--color-text-secondary)" />
      </button>

      <div
        ref="carouselRef"
        class="flex overflow-x-scroll pb-2 h-72 scrollbar-hide overflow-y-hidden"

      >
        <Card
          v-for="person in people"
          :key="person.id"
          :person="person"
          type
          class="max-w-[180px]  shrink-0 mr-3"
          @remove="removeCard"
        />

        <div
          class="flex flex-col items-center justify-center p-4 cursor-pointer w-49 h-72 border border-theme-border rounded-lg shadow-sm hover:shadow-md transition duration-200 bg-theme-bg-subtle shrink-0 mr-3"
        >
          <PlusCircleIcon :size="32" class="text-theme-primary mb-2" fillColor="var(--color-primary)" />
          <span class="text-theme-primary font-semibold text-sm">{{ $t('home.showAll') }}</span>
        </div>
        <div class="shrink-0" style="width: 0.75rem;"></div>
      </div>

      <button
        v-if="!isEnd"
        @click="scrollRight"
        class="absolute top-1/2 right-0 transform -translate-y-1/2 p-2 bg-theme-bg-secondary rounded-full shadow-lg border border-theme-border hover:bg-theme-hover transition duration-150 z-10"
        style="margin-right: 24px;" >
        <ChevronRightIcon :size="24" fillColor="var(--color-text-secondary)" />
      </button>

      <div class="text-center mt-2">
        <button class="text-theme-primary font-medium py-1 px-4 rounded hover:bg-theme-primary-subtle transition duration-150">
          {{ $t('home.showAll') }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, nextTick } from 'vue';
import { useI18n } from 'vue-i18n';
import Card from '@/components/friends/PeopleYouMayKnowCard.vue';
import type { Person } from '@/types/Person';
import {useCarousel} from '@/composables/useCarousel';

// i18n
useI18n()

// --- IMPORT IKON (bez zmian) ---
import AccountGroupIcon from 'vue-material-design-icons/AccountGroup.vue';
import DotsHorizontalIcon from 'vue-material-design-icons/DotsHorizontal.vue';
import ChevronRightIcon from 'vue-material-design-icons/ChevronRight.vue';
import ChevronLeftIcon from 'vue-material-design-icons/ChevronLeft.vue';
import PlusCircleIcon from 'vue-material-design-icons/PlusCircle.vue';

// --- UŻYCIE COMPOSABLE ---
const {
  carouselRef,
  isStart,
  isEnd,
  scrollLeft,
  scrollRight,
  checkScrollState
} = useCarousel(2);

// --- DANE (bez zmian) ---
const initialPeople: Person[] = [
  { id: 1, name: 'Maria Tumi', imageUrl: 'https://randomuser.me/api/portraits/women/68.jpg', commonFriends: 1, isFriend: false },
  { id: 2, name: 'Maja Misiura', imageUrl: 'https://randomuser.me/api/portraits/women/69.jpg', commonFriends: 1, isFriend: false },
  { id: 3, name: 'Marcin Lem', imageUrl: 'https://randomuser.me/api/portraits/men/68.jpg', commonFriends: 8, isFriend: false },
  { id: 4, name: 'Dariusz Z.', imageUrl: 'https://randomuser.me/api/portraits/men/69.jpg', commonFriends: 25, isFriend: false },
  { id: 5, name: 'Katarzyna N.', imageUrl: 'https://randomuser.me/api/portraits/women/70.jpg', commonFriends: 3, isFriend: false },
  { id: 6, name: 'Wojciech S.', imageUrl: 'https://randomuser.me/api/portraits/men/70.jpg', commonFriends: 5, isFriend: false },
  { id: 7, name: 'Anna K.', imageUrl: 'https://randomuser.me/api/portraits/women/71.jpg', commonFriends: 12, isFriend: false },
  { id: 1, name: 'Maria Tumi', imageUrl: 'https://randomuser.me/api/portraits/women/72.jpg', commonFriends: 1, isFriend: false },
  { id: 2, name: 'Maja Misiura', imageUrl: 'https://randomuser.me/api/portraits/women/73.jpg', commonFriends: 1, isFriend: false },
  { id: 3, name: 'Marcin Lem', imageUrl: 'https://randomuser.me/api/portraits/men/71.jpg', commonFriends: 8, isFriend: false },
  { id: 4, name: 'Dariusz Z.', imageUrl: 'https://randomuser.me/api/portraits/men/72.jpg', commonFriends: 25, isFriend: false },
  { id: 5, name: 'Katarzyna N.', imageUrl: 'https://randomuser.me/api/portraits/women/74.jpg', commonFriends: 3, isFriend: false },
  { id: 6, name: 'Wojciech S.', imageUrl: 'https://randomuser.me/api/portraits/men/73.jpg', commonFriends: 5, isFriend: false },
  { id: 7, name: 'Anna K.', imageUrl: 'https://randomuser.me/api/portraits/women/75.jpg', commonFriends: 12, isFriend: false },
   { id: 7, name: 'Anna K.', imageUrl: 'https://randomuser.me/api/portraits/women/76.jpg', commonFriends: 12, isFriend: false },
];

const people = ref<Person[]>(initialPeople);

// --- METODY KOMPONENTU ---

const removeCard = (id: number) => {
    people.value = people.value.filter((p: Person) => p.id !== id);
    nextTick(() => {
        checkScrollState();
    });
}
</script>

<style scoped>
.scrollbar-hide {
  -ms-overflow-style: none;
  scrollbar-width: none;
}

.scrollbar-hide::-webkit-scrollbar {
  display: none;
}
</style>
