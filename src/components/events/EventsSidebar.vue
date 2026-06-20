<template>
  <Sidebar
    title="Wydarzenia"
    search-p laceholder="Wyszukaj wydarzenia"
    :show-search="true"
    :items="sidebarItems"
    :create-button="createEventButton"
  >
  <template v-if="!not" #pre-list>
      <div class="flex justify-between items-center px-2">
        <h3 class="text-[17px] font-medium">Twoje nadchodzące wydarzenia</h3>
        <button class="text-[#0866FF] text-[14px] font-medium hover:bg-blue-50 px-2 py-1 rounded">Pokaż wszystkie</button>
      </div>

      <div class="space-y-2">
        <div v-for="event in upcoming" :key="event.id" class="flex gap-3 p-2 hover:bg-theme-hover rounded-xl cursor-pointer transition group">
          <img :src="event.img" class="w-[60px] h-[60px] rounded-xl object-cover" />
          <div class="flex flex-col min-w-0">
            <span class="text-[12px] text-[#F02849] font-medium truncate">{{ event.date }}</span>
            <h4 class="text-[15px] font-medium leading-tight line-clamp-2">{{ event.title }}</h4>
            <div v-if="event.author" class="flex items-center gap-1.5 mt-1">
              <img :src="event.authorImg" class="w-4 h-4 rounded-full" />
              <span class="text-[12px] text-theme-text-secondary">{{ event.author }}</span>
            </div>
          </div>
        </div>
      </div>
<div class="flex justify-between items-center  px-2">
        <h3 class="text-[17px] font-medium">Polecane wydarzenia</h3>
        <button class="text-[#0866FF] text-[14px] font-medium hover:bg-blue-50 px-2 py-1 rounded">Pokaż wszystkie</button>
      </div>

      <div class="space-y-2">
        <div v-for="event in recommended" :key="event.id" class="flex gap-3 p-2 hover:bg-theme-hover rounded-xl cursor-pointer transition">
          <img :src="event.img" class="w-[60px] h-[60px] rounded-xl object-cover" />
          <div class="flex flex-col min-w-0">
            <span class="text-[11px] text-[#F02849] ">{{ event.date }}</span>
            <h4 class="text-[15px] font-medium leading-tight line-clamp-2">{{ event.title }}</h4>
          </div>
        </div>
      </div>


    </template>
    <template #list-header>
      <h3 class="px-2 text-[17px] font-bold mb-4">Kategorie</h3>
    </template>
    <template #list-items>
      <div class="space-y-1">
        <div v-for="cat in categories" :key="cat.name" class="flex items-center gap-3 hover:bg-theme-hover px-3 py-2 rounded-lg cursor-pointer transition group">
          <div class="bg-theme-bg-subtle group-hover:bg-theme-hover-strong p-2 rounded-full">
            <component :is="cat.icon" :size="20" />
          </div>
          <span class="text-[15px] font-medium">{{ cat.name }}</span>
        </div>
      </div>
    </template>
  </Sidebar>
  <BaseModal v-if="isOpen" @close="isOpen = false" :title="'Utwórz wydarzenie'">
    <CreateEventModal @close="isOpen = false" />
  </BaseModal>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import Sidebar from '@/components/common/Sidebar.vue';
import HomeIcon from 'vue-material-design-icons/Home.vue';
import AccountGroupIcon from 'vue-material-design-icons/AccountGroup.vue';
import BellIcon from 'vue-material-design-icons/Bell.vue';
import PlusIcon from 'vue-material-design-icons/Plus.vue';
import HomeOutlineIcon from 'vue-material-design-icons/HomeOutline.vue';
import DumbbellIcon from 'vue-material-design-icons/Dumbbell.vue';
import GamepadVariantIcon from 'vue-material-design-icons/GamepadVariant.vue';
import TheaterIcon from 'vue-material-design-icons/Theater.vue';
import CreateEventModal from '@/components/events/CreateEventModal.vue';
import BaseModal from '@/components/common/BaseModal.vue';
 const props = defineProps({
  not: {
    type: Boolean,
    default: false
  }
});
const isOpen = ref(false);
const handleOpenModal = () => {
  isOpen.value = true;
};
const upcoming = ref([
  {
    id: 1,
    date: 'Sobota, 16 maja 2026 o 15:00',
    title: 'Technikalia.26',
    img: 'path_to_image',
    author: 'Bartek',
    authorImg: 'path_to_avatar'
  },
  {
    id: 2,
    date: '29 maj o 17:00 – 31 maj o 04:00',
    title: 'JUWENALIA TRÓJMIASTA 2026 ☆ FESTIWAL ☆ 29-31 MAJ ☆',
    img: 'path_to_image'
  }
]);
const recommended = ref([
  {
    id: 3,
    date: 'dziś o 17:00',
    title: 'Wieczór Planszówek',
    img: 'path_to_image'
  },
  {
    id: 4,
    date: 'czwartek o 22:00 – 03:30',
    title: 'DZIEŃ KOBIET w Kwadratowej ✨🌸 - czwartkowa impreza',
    img: 'path_to_image'
  }
]);
const sidebarItems = ref([
  {
    icon: HomeIcon,
    text: 'Strona główna',
    route: '/event',
  },
  {
    icon: AccountGroupIcon,
    text: 'Twoje wydarzenia',
    route: '/events/your-events',
  },
  {
    icon: BellIcon,
    text: 'Powiadomienia',
    route: '#',
  },
]);

const createEventButton = ref({
  icon: PlusIcon,
  text: 'Utwórz nowe wydarzenie',
  action: handleOpenModal,
});

const categories = [
  { name: 'Dom i ogród', icon: HomeOutlineIcon },
  { name: 'Fitness i trening', icon: DumbbellIcon },
  { name: 'Gry', icon: GamepadVariantIcon },
  { name: 'Imprezy', icon: TheaterIcon },
];
</script>


