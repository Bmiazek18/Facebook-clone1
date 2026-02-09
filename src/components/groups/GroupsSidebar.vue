<template>
  <Sidebar
    title="Grupy"
    search-placeholder="Szukaj grup"
    :show-settings="true"
    :show-search="true"
    :items="sidebarItems"
    :create-button="createGroupButton"
  >
    <template #list-header>
      <h2 class="text-[17px] font-semibold text-theme-text leading-snug">Grupy, do których należysz</h2>
      <a href="#" class="text-[13px] text-blue-600 hover:underline shrink-0">Wyświetl wszystkie</a>
    </template>

    <template #list-items>
      <a
        v-for="(group, index) in groups"
        :key="index"
        href="#"
        class="flex items-center space-x-3 p-2 rounded-lg hover:bg-theme-hover transition-colors group"
      >
        <img
          :src="group.image"
          alt="Group Icon"
          class="w-12 h-12 rounded-xl object-cover border border-theme-border shrink-0"
        >
        <div class="flex-1 min-w-0">
          <h3 class="text-[15px] font-semibold text-theme-text truncate group-hover:text-theme-text">
            {{ group.name }}
          </h3>
          <p class="text-[13px] text-gray-500 truncate">
            Ostatnia aktywność {{ group.lastActive }}
          </p>
        </div>
      </a>
    </template>
  </Sidebar>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import Sidebar from '@/components/common/Sidebar.vue';
import PlusIcon from 'vue-material-design-icons/Plus.vue';
import NewspaperVariantOutlineIcon from 'vue-material-design-icons/NewspaperVariantOutline.vue';
import InformationOutlineIcon from 'vue-material-design-icons/InformationOutline.vue';
import AccountGroupOutlineIcon from 'vue-material-design-icons/AccountGroupOutline.vue';

const sidebarItems = ref([
  {
    icon: NewspaperVariantOutlineIcon,
    text: 'Twoje Aktualności',
    route: '#',
  },
  {
    icon: InformationOutlineIcon,
    text: 'Odkryj',
    route: '#',
  },
  {
    icon: AccountGroupOutlineIcon,
    text: 'Twoje grupy',
    route: '#',
  },
]);

const createGroupButton = ref({
  icon: PlusIcon,
  text: 'Utwórz nową grupę',
  route: '/add-group',
});

const groups = ref([
  {
    name: 'Kolegium Sędziów BOZPN',
    lastActive: '2 dni temu',
    image: 'https://ui-avatars.com/api/?name=KS&background=3F6212&color=fff&size=128&font-size=0.4'
  },
  {
    name: 'Absurdalnie Tanie Loty',
    lastActive: '37 min temu',
    image: 'https://ui-avatars.com/api/?name=TL&background=3B82F6&color=fff&size=128&font-size=0.4'
  },
  {
    name: 'Reprezentacja Polski Diabetyków w Futsalu',
    lastActive: 'tydzień temu',
    image: 'https://ui-avatars.com/api/?name=PL&background=DC2626&color=fff&size=128&font-size=0.4'
  },
  {
    name: 'Praca dla początkujących programistów: Staż, Junior',
    lastActive: '6 dni temu',
    image: 'https://ui-avatars.com/api/?name=IT&background=F3F4F6&color=111&size=128&font-size=0.4'
  },
  {
    name: 'Piłkarski Świat',
    lastActive: '46 min temu',
    image: 'https://ui-avatars.com/api/?name=PS&background=E5E7EB&color=111&size=128&font-size=0.4'
  },
  {
    name: 'Grupa Piłkarzyki',
    lastActive: '22 godz. temu',
    image: 'https://ui-avatars.com/api/?name=GP&background=1F2937&color=fff&size=128&font-size=0.4'
  },
  {
    name: 'BILETY // POLSKA SIATKÓWKA // INFORMACJE',
    lastActive: '5 dni temu',
    image: 'https://ui-avatars.com/api/?name=VP&background=1E3A8A&color=fff&size=128&font-size=0.4'
  },
  {
    name: 'EA SPORTS FC 26 - POLSKA',
    lastActive: '5 min temu',
    image: 'https://ui-avatars.com/api/?name=EA&background=450a0a&color=fff&size=128&font-size=0.4'
  },
]);
</script>
