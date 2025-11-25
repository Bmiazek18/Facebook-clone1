<template>
  <div class="bg-gray-100">


    <div class="relative">

      <button
        v-if="!isStart"
        @click="scrollLeft"
        class="absolute top-1/2 z-30 left-0 transform -translate-y-1/2 p-2 bg-white rounded-full shadow-lg border hover:bg-gray-200 transition duration-150 z-10"
        style="margin-left: 64.5px;"
      >
        <ChevronLeftIcon :size="24" fillColor="#4B5563" />
      </button>

      <div
        ref="carouselRef"
        class="flex overflow-x-auto pb-4 scrollbar-hide "

      >
        <AddStoryCard class="mr-4"/>
        <StoryCard v-for="story in stories" :key="story.id" :story="story" class="mr-4 last-of-type:mr-0" />


      </div>

      <button
        v-if="!isEnd"
        @click="scrollRight"
        class="absolute top-1/2 z-30 right-0 transform -translate-y-1/2 p-2 bg-white rounded-full shadow-lg border hover:bg-gray-200 transition duration-150 z-10"
        style="margin-right: 64.5px;"
      >
        <ChevronRightIcon :size="24" fillColor="#4B5563" />
      </button>

    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import StoryCard from './StoryCard.vue';
import AddStoryCard from './AddStoryCard.vue';
import { Story } from '../types/Story';
import {useCarousel} from '../composables/useCarousel';

// --- IMPORT IKON ---
import ChevronRightIcon from 'vue-material-design-icons/ChevronRight.vue';
import ChevronLeftIcon from 'vue-material-design-icons/ChevronLeft.vue';

// --- UŻYCIE COMPOSABLE ---
const {
  carouselRef,
  isStart,
  isEnd,
  scrollLeft,
  scrollRight,
} = useCarousel(4);

// --- DANE ---
const stories = ref<Story[]>([
  {
    id: 1,
    backgroundImageUrl: 'https://images.unsplash.com/photo-1517048676732-d65bc937f952?q=80&w=2070&auto=format&fit=crop&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D',
    profileImageUrl: 'https://upload.wikimedia.org/wikipedia/commons/thumb/1/1d/Logo_Politechniki_Gda%C5%84skiej.svg/1200px-Logo_Politechniki_Gda%C5%84skiej.svg.png',
    title: 'Politechnika Gdańska',
  },
  {
    id: 2,
    backgroundImageUrl: 'https://images.unsplash.com/photo-1548687788-b21a8d011c77?q=80&w=2070&auto=format&fit=crop&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D',
    profileImageUrl: 'https://upload.wikimedia.org/wikipedia/commons/thumb/4/41/LZPN_logo.png/640px-LZPN_logo.png',
    title: 'Lubelski Związek Pił...',
  },
  {
    id: 3,
    backgroundImageUrl: 'https://images.unsplash.com/photo-1549472304-4c8e96df231f?q=80&w=2070&auto=format&fit=crop&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D',
    profileImageUrl: 'https://i.imgur.com/K1x56gJ.png',
    title: 'Nowa Aktualizacja!',
  },
  {
    id: 4,
    backgroundImageUrl: 'https://images.unsplash.com/photo-1504198458643-ad41740d6945?q=80&w=2070&auto=format&fit=crop&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D',
    profileImageUrl: 'https://i.imgur.com/b9iYF5u.png',
    title: 'Eventy w Mieście',
  },
  {
    id: 6,
    backgroundImageUrl: 'https://images.unsplash.com/photo-1504198458643-ad41740d6945?q=80&w=2070&auto=format&fit=crop&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D',
    profileImageUrl: 'https://i.imgur.com/b9iYF5u.png',
    title: 'Eventy w Mieście 1',
  },
  {
    id: 7,
    backgroundImageUrl: 'https://images.unsplash.com/photo-1504198458643-ad41740d6945?q=80&w=2070&auto=format&fit=crop&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D',
    profileImageUrl: 'https://i.imgur.com/b9iYF5u.png',
    title: 'Eventy w Mieście',
  },
  {
    id: 8,
    backgroundImageUrl: 'https://images.unsplash.com/photo-1517048676732-d65bc937f952?q=80&w=2070&auto=format&fit=crop&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D',
    profileImageUrl: 'https://upload.wikimedia.org/wikipedia/commons/thumb/1/1d/Logo_Politechniki_Gda%C5%84skiej.svg/1200px-Logo_Politechniki_Gda%C5%84skiej.svg.png',
    title: 'Politechnika Gdańska',
  },
  {
    id: 9,
    backgroundImageUrl: 'https://images.unsplash.com/photo-1548687788-b21a8d011c77?q=80&w=2070&auto=format&fit=crop&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D',
    profileImageUrl: 'https://upload.wikimedia.org/wikipedia/commons/thumb/4/41/LZPN_logo.png/640px-LZPN_logo.png',
    title: 'Lubelski Związek Pił...',
  },
  {
    id: 10,
    backgroundImageUrl: 'https://images.unsplash.com/photo-1549472304-4c8e96df231f?q=80&w=2070&auto=format&fit=crop&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D',
    profileImageUrl: 'https://i.imgur.com/K1x56gJ.png',
    title: 'Nowa Aktualizacja!',
  },
  {
    id: 11,
    backgroundImageUrl: 'https://images.unsplash.com/photo-1504198458643-ad41740d6945?q=80&w=2070&auto=format&fit=crop&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D',
    profileImageUrl: 'https://i.imgur.com/b9iYF5u.png',
    title: 'Eventy w Mieście',
  },
  {
    id: 12,
    backgroundImageUrl: 'https://images.unsplash.com/photo-1504198458643-ad41740d6945?q=80&w=2070&auto=format&fit=crop&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D',
    profileImageUrl: 'https://i.imgur.com/b9iYF5u.png',
    title: 'Eventy w Mieście',
  },
  {
    id:13,
    backgroundImageUrl: 'https://images.unsplash.com/photo-1504198458643-ad41740d6945?q=80&w=2070&auto=format&fit=crop&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D',
    profileImageUrl: 'https://i.imgur.com/b9iYF5u.png',
    title: 'Eventy w Mieście',
  },
    {
    id: 14,
    backgroundImageUrl: 'https://images.unsplash.com/photo-1504198458643-ad41740d6945?q=80&w=2070&auto=format&fit=crop&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D',
    profileImageUrl: 'https://i.imgur.com/b9iYF5u.png',
    title: 'Eventy w Mieście',
  }, {
    id: 15,
    backgroundImageUrl: 'https://images.unsplash.com/photo-1504198458643-ad41740d6945?q=80&w=2070&auto=format&fit=crop&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D',
    profileImageUrl: 'https://i.imgur.com/b9iYF5u.png',
    title: 'Eventy w Mieście',
  }
  , {
    id: 16,
    backgroundImageUrl: 'https://images.unsplash.com/photo-1504198458643-ad41740d6945?q=80&w=2070&auto=format&fit=crop&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D',
    profileImageUrl: 'https://i.imgur.com/b9iYF5u.png',
    title: 'Eventy w Mieście',
  },
]);
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
