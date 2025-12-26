<template>
  <div class="py-4 pl-4 bg-theme-bg-secondary rounded-lg shadow-md max-w-4xl mx-auto">

    <div class="flex justify-between items-center mb-4">
      <div class="flex items-center text-theme-text">
        <AccountGroupIcon :size="24" class="mr-2" fillColor="#4B5563" />
        <h2 class="text-xl font-bold">{{ $t('home.peopleYouMayKnow') }}</h2>
      </div>
      <button class="text-gray-500 hover:text-gray-700">
        <DotsVerticalIcon :size="24" />
      </button>
    </div>

    <div class="relative">

      <button
        v-if="!isStart"
        @click="scrollLeft"
        class="absolute top-1/2 left-0 transform -translate-y-1/2 p-2 bg-white rounded-full shadow-lg border hover:bg-gray-100 transition duration-150 z-10"
        style="margin-left: 104px;" >
        <ChevronLeftIcon :size="24" fillColor="#4B5563" />
      </button>

      <div
        ref="carouselRef"
        class="flex overflow-x-scroll pb-2 h-72 scrollbar-hide"

      >
        <Card
          v-for="person in people"
          :key="person.id"
          :person="person"
          class="max-w-[180px]  shrink-0 mr-3"
          @remove="removeCard"
        />

        <div
          class="flex flex-col items-center justify-center p-4 cursor-pointer w-49 h-72 border border-gray-200 rounded-lg shadow-sm hover:shadow-md transition duration-200 bg-gray-50 shrink-0 mr-3"
        >
          <PlusCircleIcon :size="32" class="text-blue-600 mb-2" fillColor="#2563EB" />
          <span class="text-blue-600 font-semibold text-sm">{{ $t('home.showAll') }}</span>
        </div>
        <div class="shrink-0" style="width: 0.75rem;"></div>
      </div>

      <button
        v-if="!isEnd"
        @click="scrollRight"
        class="absolute top-1/2 right-0 transform -translate-y-1/2 p-2 bg-white rounded-full shadow-lg border hover:bg-gray-100 transition duration-150 z-10"
        style="margin-right: 104px;" >
        <ChevronRightIcon :size="24" fillColor="#4B5563" />
      </button>

      <div class="text-center mt-2">
        <button class="text-blue-600 font-medium py-1 px-4 rounded hover:bg-blue-50 transition duration-150">
          {{ $t('home.showAll') }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, nextTick } from 'vue';
import { useI18n } from 'vue-i18n';
import Card from './PeopleYouMayKnowCard.vue';
import type { Person } from '../types/Person';
import {useCarousel} from '../composables/useCarousel';

// i18n
useI18n()

// --- IMPORT IKON (bez zmian) ---
import AccountGroupIcon from 'vue-material-design-icons/AccountGroup.vue';
import DotsVerticalIcon from 'vue-material-design-icons/DotsVertical.vue';
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
