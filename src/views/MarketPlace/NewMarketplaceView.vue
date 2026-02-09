<script setup lang="ts">
import { shallowRef, ref } from 'vue';
import { useRouter } from 'vue-router';

// Ikony
import TagIcon from 'vue-material-design-icons/Tag.vue';
import FormatListBulletedIcon from 'vue-material-design-icons/FormatListBulleted.vue';
import HelpCircleIcon from 'vue-material-design-icons/HelpCircle.vue';
import KettleIcon from 'vue-material-design-icons/Kettle.vue';
import CarSideIcon from 'vue-material-design-icons/CarSide.vue';
import HomeIcon from 'vue-material-design-icons/Home.vue';
import PencilIcon from 'vue-material-design-icons/Pencil.vue';

// Komponenty
import NavbarRight from '@/layouts/Navbar/NavbarRight.vue';
import AppCloseHeader from '@/layouts/AppCloseHeader.vue';

const router = useRouter();

// --- DANE MENU ---
// Rozdzielamy menu na "Główne" (aktywne) i "Pozostałe", aby łatwiej wstawić separator
const activeMenuItem = shallowRef({
  id: 1,
  label: 'Wybierz typ ogłoszenia',
  icon: TagIcon,
  active: true
});

const secondaryMenuItems = shallowRef([
  {
    id: 2,
    label: 'Twoje ogłoszenia',
    icon: FormatListBulletedIcon,
    route: '/marketplace/you/dashboard'
  },
  {
    id: 3,
    label: 'Pomoc dla sprzedawcy',
    icon: HelpCircleIcon,
    route: '#'
  },
]);

const listingTypes = shallowRef([
  {
    id: 1,
    title: 'Przedmiot na sprzedaż',
    description: 'Utwórz jedno ogłoszenie dla jednego lub wielu przedmiotów na...',
    iconBg: 'bg-pink-100',
    icon: KettleIcon
  },
  {
    id: 2,
    title: 'Pojazd na sprzedaż',
    description: 'Sprzedaj auto, ciężarówkę lub pojazd innego typu.',
    iconBg: 'bg-blue-100',
    icon: CarSideIcon
  },
  {
    id: 3,
    title: 'Dom na sprzedaż lub wynajem',
    description: 'Wystaw dom lub mieszkanie na sprzedaż lub wynajem.',
    iconBg: 'bg-orange-100',
    icon: HomeIcon
  }
]);

const showNewCreating = ref(false);
const selectedListingType = ref(null);

const handleListingTypeClick = (listingType: any) => {
  selectedListingType.value = listingType;
  router.push('/marketplace/create/item');
};

const closeNewCreating = () => {
  showNewCreating.value = false;
  selectedListingType.value = null;
};
</script>

<template>
  <div class="flex h-screen w-full bg-theme-bg font-sans text-theme-text overflow-hidden">

    <div class="absolute top-0 right-0 z-20 py-2 px-4">
      <NavbarRight />
    </div>

    <div class="flex h-full w-full">

      <aside class="w-[360px] flex-shrink-0 bg-theme-bg-secondary flex flex-col border-r border-theme-border h-full">
<AppCloseHeader class="shadow-sm pb-2"/>
        <div class="px-4 py-2">
          <h1 class="text-[24px] font-semibold text-theme-text">Utwórz nowe ogłoszenie</h1>
        </div>

        <nav class="px-2 space-y-1">

          <div class=" pb-2">
            <div class="flex items-center gap-3 px-3 py-2 rounded-lg cursor-default bg-gray-100 dark:bg-blue-900/20 transition-colors">
              <div class="w-10 h-10 rounded-full flex items-center justify-center bg-[#1877F2] text-white shadow-sm">
                <component :is="activeMenuItem.icon" :size="20" />
              </div>
              <span class="font-semibold text-[15px] text-theme-text">{{ activeMenuItem.label }}</span>
            </div>
          </div>

          <div class="mx-2 border-b border-theme-border my-2"></div>

          <div class="pt-1 space-y-1">
            <div
              v-for="item in secondaryMenuItems"
              :key="item.id"
              class="flex items-center gap-3 px-3 py-2 rounded-lg cursor-pointer hover:bg-theme-hover transition-colors group"
            >
              <div class="w-10 h-10 rounded-full flex items-center justify-center bg-theme-bg-tertiary group-hover:bg-gray-300 dark:group-hover:bg-zinc-600 transition-colors text-theme-text">
                <component :is="item.icon" :size="22" />
              </div>
              <span class="font-semibold text-[15px] text-theme-text">{{ item.label }}</span>
            </div>
          </div>
        </nav>

        <div class="flex-1 bg-theme-bg-secondary"></div>
      </aside>

      <main class="flex-1 flex flex-col relative bg-theme-bg">
        <div class="flex-1 flex flex-col items-center justify-center p-8">
          <h2 class="text-[20px] font-bold mb-8 text-theme-text">Wybierz typ ogłoszenia</h2>

          <div class="flex flex-wrap justify-center gap-4">
            <div
              v-for="item in listingTypes"
              :key="item.id"
              @click="handleListingTypeClick(item)"
              class="bg-theme-bg-secondary w-[220px] h-[260px] rounded-lg border border-theme-border hover:bg-theme-hover hover:border-gray-300 dark:hover:border-zinc-600 cursor-pointer flex flex-col items-center p-5 text-center transition-all duration-200"
            >
              <div class="mb-5 mt-2">
                 <div :class="`w-16 h-16 rounded-full ${item.iconBg} flex items-center justify-center text-gray-900`">
                   <component :is="item.icon" :size="32" />
                 </div>
              </div>

              <h3 class="font-bold text-[17px] mb-2 text-theme-text leading-tight">{{ item.title }}</h3>
              <p class="text-[13px] text-theme-text-secondary leading-snug px-1">{{ item.description }}</p>
            </div>
          </div>
        </div>

        <button class="absolute bottom-8 right-8 w-12 h-12 bg-theme-bg-tertiary rounded-full flex items-center justify-center hover:bg-theme-hover shadow-lg border border-theme-border text-theme-text transition-transform hover:scale-105">
          <PencilIcon :size="24" />
        </button>
      </main>
    </div>
  </div>
</template>

<style scoped>
/* Ukrycie paska przewijania w sidebarze, ale zachowanie funkcjonalności */
aside {
  scrollbar-width: none; /* Firefox */
  -ms-overflow-style: none;  /* IE 10+ */
}
aside::-webkit-scrollbar {
  display: none; /* Chrome/Safari */
}
</style>
