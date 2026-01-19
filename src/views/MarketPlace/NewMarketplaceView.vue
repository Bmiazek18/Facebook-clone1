<script setup>
import { shallowRef, ref } from 'vue';
import NewListingForm from '@/components/marketplace/NewListingForm.vue';


// --- IMPORT IKON ---
// Sidebar
import CloseIcon from 'vue-material-design-icons/Close.vue';
import TagIcon from 'vue-material-design-icons/Tag.vue';
import FormatListBulletedIcon from 'vue-material-design-icons/FormatListBulleted.vue';
import HelpCircleIcon from 'vue-material-design-icons/HelpCircle.vue';
// Top Nav
import AppsIcon from 'vue-material-design-icons/Apps.vue'; // Siatka menu
import FacebookMessengerIcon from 'vue-material-design-icons/FacebookMessenger.vue';
import BellIcon from 'vue-material-design-icons/Bell.vue';
// Karty
import KettleIcon from 'vue-material-design-icons/Kettle.vue'; // Przedmiot
import CarSideIcon from 'vue-material-design-icons/CarSide.vue'; // Pojazd
import HomeIcon from 'vue-material-design-icons/Home.vue'; // Dom
// FAB
import PencilIcon from 'vue-material-design-icons/Pencil.vue';
import NavbarRight from '@/layouts/Navbar/NavbarRight.vue';

// --- DANE ---

// Używamy shallowRef dla komponentów, aby Vue nie próbowało ich głęboko reaktywować (wydajność)
const menuItems = shallowRef([
  {
    id: 1,
    label: 'Wybierz typ ogłoszenia',
    icon: TagIcon,
    active: true
  },
  {
    id: 2,
    label: 'Twoje ogłoszenia',
    icon: FormatListBulletedIcon,
    active: false
  },
  {
    id: 3,
    label: 'Pomoc dla sprzedawcy',
    icon: HelpCircleIcon,
    active: false
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

// Stan do kontrolowania widoczności komponentu NewListingForm
const showNewCreating = ref(false);
const selectedListingType = ref(null);

// Funkcja do obsługi kliknięcia na typ ogłoszenia
const handleListingTypeClick = (listingType) => {
  selectedListingType.value = listingType;
  showNewCreating.value = true;
};

// Funkcja do zamknięcia komponentu NewListingForm
const closeNewCreating = () => {
  showNewCreating.value = false;
  selectedListingType.value = null;
};
</script>

<template>
  <div class="flex h-screen w-full bg-[#F0F2F5] font-sans flex-col text-gray-900">

      <div class="flex absolute z-20 right-0 w-full justify-end py-2 px-4">
<NavbarRight />
</div>
  <NewListingForm
     v-if="showNewCreating"
      @close="closeNewCreating"
      :listing-type="selectedListingType"
    />
    <template v-else>
    <aside class="w-[360px] flex-shrink-0 bg-white shadow-sm flex flex-col border-r border-gray-200 overflow-y-auto">




      <div class="px-4 py-2">
        <h1 class="text-2xl font-bold mb-4">Utwórz nowe ogłoszenie</h1>
      </div>

      <nav class="px-2 flex-1 space-y-1">
        <div
          v-for="item in menuItems"
          :key="item.id"
          class="flex items-center gap-3 px-2 py-3 rounded-lg cursor-pointer transition-colors"
          :class="item.active ? 'bg-blue-50' : 'hover:bg-gray-100'"
        >
          <div
            class="w-9 h-9 rounded-full flex items-center justify-center"
            :class="item.active ? 'bg-blue-600 text-white' : 'bg-gray-200 text-gray-800'"
          >
            <component :is="item.icon" :size="20" />
          </div>

          <span class="font-medium text-[15px]">{{ item.label }}</span>
        </div>
      </nav>
    </aside>

    <main class="flex-1 flex flex-col relative">



      <div class="flex-1 flex flex-col items-center justify-center p-8">

        <h2 class="text-xl font-bold mb-8 text-black">Wybierz typ ogłoszenia</h2>

        <div class="flex flex-wrap justify-center gap-4">

          <div
            v-for="item in listingTypes"
            :key="item.id"
            @click="handleListingTypeClick(item)"
            class="bg-white w-[230px] h-[280px] rounded-lg shadow-sm border border-transparent hover:bg-gray-50 hover:shadow-md cursor-pointer flex flex-col items-center p-4 text-center transition-all duration-200 group"
          >
            <div class="mb-4">
               <div :class="`w-20 h-20 rounded-full ${item.iconBg} flex items-center justify-center text-gray-800`">
                 <component :is="item.icon" :size="36" />
               </div>
            </div>

            <h3 class="font-bold text-[16px] mb-2 text-gray-900 leading-tight">{{ item.title }}</h3>
            <p class="text-[13px] text-gray-500 leading-snug">{{ item.description }}</p>

          </div>

        </div>

      </div>

      <button class="absolute bottom-6 right-6 w-12 h-12 bg-gray-200 rounded-full flex items-center justify-center hover:bg-gray-300 shadow-md text-black">
        <PencilIcon :size="24" />
      </button>

    </main>
    </template>
  </div>


</template>

<style scoped>
/* Dostosowanie scrollbara */
aside::-webkit-scrollbar {
  width: 8px;
}
aside::-webkit-scrollbar-track {
  background: transparent;
}
aside::-webkit-scrollbar-thumb {
  background-color: rgba(0,0,0,0.1);
  border-radius: 4px;
}
aside::-webkit-scrollbar-thumb:hover {
  background-color: rgba(0,0,0,0.2);
}


</style>
