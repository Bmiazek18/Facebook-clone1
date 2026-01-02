<template>
  <aside class="w-[360px] flex flex-col bg-white border-r border-gray-200 sticky top-14 h-[calc(100vh-56px)] overflow-y-auto z-20">
    <div class="p-4">
      <h1 class="text-[24px] font-bold mb-4 px-2">Wydarzenia</h1>

      <div class="relative mb-6 px-2">
        <magnify-icon class="absolute left-5 top-2.5 text-[#65676b]" :size="20" />
        <input
          type="text"
          placeholder="Wyszukaj wydarzenia"
          class="w-full bg-[#f0f2f5] rounded-full py-2.5 pl-11 pr-4 placeholder-[#65676b] focus:outline-none text-[15px]"
        />
      </div>

      <nav class="space-y-1">
        <a href="#" class="flex items-center gap-3 bg-[#f0f2f5] px-3 py-2 rounded-lg transition group">
          <div class="bg-[#1877f2] p-1.5 rounded-full text-white">
            <home-icon :size="20" />
          </div>
          <span class="font-semibold text-[15px]">Strona główna</span>
        </a>
        <a href="#" class="flex items-center gap-3 hover:bg-gray-100 px-3 py-2 rounded-lg transition group">
          <div class="bg-[#e4e6eb] group-hover:bg-[#d8dadf] p-1.5 rounded-full">
            <account-group-icon :size="20" />
          </div>
          <span class="font-medium text-[15px]">Twoje wydarzenia</span>
          <chevron-down-icon class="ml-auto text-[#65676b]" :size="20" />
        </a>
        <a href="#" class="flex items-center gap-3 hover:bg-gray-100 px-3 py-2 rounded-lg transition group">
          <div class="bg-[#e4e6eb] group-hover:bg-[#d8dadf] p-1.5 rounded-full">
            <bell-icon :size="20" />
          </div>
          <span class="font-medium text-[15px]">Powiadomienia</span>
        </a>
      </nav>

      <button @click="handleOpenModal" class="mt-4 w-full bg-[#ebf5ff] hover:bg-[#dbeafe] text-[#1877f2] font-semibold py-2 rounded-lg flex items-center justify-center gap-2 transition text-[15px]">
        <plus-icon :size="20" /> Utwórz nowe wydarzenie
      </button>

      <div class="mt-8 border-t pt-4">
        <h3 class="px-2 text-[17px] font-bold mb-4">Kategorie</h3>
        <div class="space-y-1">
          <div v-for="cat in categories" :key="cat.name" class="flex items-center gap-3 hover:bg-gray-100 px-3 py-2 rounded-lg cursor-pointer transition group">
            <div class="bg-[#e4e6eb] group-hover:bg-[#d8dadf] p-2 rounded-full">
              <component :is="cat.icon" :size="20" />
            </div>
            <span class="text-[15px] font-medium">{{ cat.name }}</span>
          </div>
        </div>
      </div>
    </div>
  </aside>
  <BaseModal v-if="isOpen" @close="isOpen = false" :title="'Utwórz wydarzenie'">
    <CreateEventModal @close="isOpen = false" />
  </BaseModal>
</template>

<script setup>
import { ref } from 'vue';
import MagnifyIcon from 'vue-material-design-icons/Magnify.vue';
import HomeIcon from 'vue-material-design-icons/Home.vue';
import AccountGroupIcon from 'vue-material-design-icons/AccountGroup.vue';
import BellIcon from 'vue-material-design-icons/Bell.vue';
import PlusIcon from 'vue-material-design-icons/Plus.vue';
import ChevronDownIcon from 'vue-material-design-icons/ChevronDown.vue';
import HomeOutlineIcon from 'vue-material-design-icons/HomeOutline.vue';
import DumbbellIcon from 'vue-material-design-icons/Dumbbell.vue';
import GamepadVariantIcon from 'vue-material-design-icons/GamepadVariant.vue';
import TheaterIcon from 'vue-material-design-icons/Theater.vue';
import CreateEventModal from '@/components/createEventModal.vue';
import BaseModal from '@/components/BaseModal.vue';

const isOpen = ref(false);
const handleOpenModal = () => {
  isOpen.value = true;
};

const categories = [
  { name: 'Dom i ogród', icon: HomeOutlineIcon },
  { name: 'Fitness i trening', icon: DumbbellIcon },
  { name: 'Gry', icon: GamepadVariantIcon },
  { name: 'Imprezy', icon: TheaterIcon },
];
</script>

<style>
aside::-webkit-scrollbar { width: 4px; }
aside::-webkit-scrollbar-thumb { background: #bcc0c4; border-radius: 10px; }
</style>
