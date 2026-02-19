<script setup lang="ts">
import { defineEmits, defineProps, ref, computed } from 'vue';
import Sidebar from '@/components/common/Sidebar.vue';
import Plus from 'vue-material-design-icons/Plus.vue';
import Storefront from 'vue-material-design-icons/Storefront.vue';
import Bell from 'vue-material-design-icons/Bell.vue';
import TagOutline from 'vue-material-design-icons/TagOutline.vue';
import MapMarker from 'vue-material-design-icons/MapMarker.vue';
import Shopping from 'vue-material-design-icons/Shopping.vue';

// Icon imports for categories
import Car from 'vue-material-design-icons/Car.vue';
import HomeCity from 'vue-material-design-icons/HomeCity.vue';
import Paw from 'vue-material-design-icons/Paw.vue';
import HomeVariant from 'vue-material-design-icons/HomeVariant.vue';
import HammerWrench from 'vue-material-design-icons/HammerWrench.vue';
import Run from 'vue-material-design-icons/Run.vue';
import Heart from 'vue-material-design-icons/Heart.vue';
import Home from 'vue-material-design-icons/Home.vue';
import Cellphone from 'vue-material-design-icons/Cellphone.vue';
import Brush from 'vue-material-design-icons/Brush.vue';
import TshirtCrew from 'vue-material-design-icons/TshirtCrew.vue';
import Shovel from 'vue-material-design-icons/Shovel.vue';
import TagTextOutline from 'vue-material-design-icons/TagTextOutline.vue';
import MovieOpen from 'vue-material-design-icons/MovieOpen.vue';
import GamepadVariant from 'vue-material-design-icons/GamepadVariant.vue';

const props = defineProps({
  selectedRadius: { type: Number, default: 402 }
});
const emit = defineEmits(['open-location']);

const openLocation = () => emit('open-location');

// Full list of categories matching the screenshot, defined as a ref
const categories = ref([
  { name: 'Pojazdy', icon: Car },
  { name: 'Wynajem nieruchomości', icon: HomeCity },
  { name: 'Artykuły biurowe', icon: TagOutline },
  { name: 'Artykuły dla zwierząt domowych', icon: Paw },
  { name: 'Artykuły domowe', icon: HomeVariant },
  { name: 'Artykuły remontowe', icon: HammerWrench },
  { name: 'Artykuły sportowe', icon: Run },
  { name: 'Darmowe przedmioty', icon: TagOutline },
  { name: 'Dla rodziny', icon: Heart },
  { name: 'Domy na sprzedaż', icon: Home },
  { name: 'Elektronika', icon: Cellphone },
  { name: 'Hobby', icon: Brush },
  { name: 'Odzież', icon: TshirtCrew },
  { name: 'Ogród i otoczenie', icon: Shovel },
  { name: 'Ogłoszenia drobne', icon: TagTextOutline },
  { name: 'Rozrywka', icon: MovieOpen },
  { name: 'Zabawki i gry', icon: GamepadVariant },
]);

// Sidebar items based on existing router-link elements
const sidebarItems = computed(() => [
  { icon: Storefront, text: 'Przeglądaj wszystkie', route: '/marketplace' },
  { icon: Bell, text: 'Powiadomienia', route: '/marketplace/notifications' },
  { icon: Shopping, text: 'Kupno', route: '/marketplace' },
  { icon: TagOutline, text: 'Sprzedaż', route: '/marketplace/you/dashboard' },
]);
</script>

<template>
  <Sidebar
    title="Marketplace"
    searchPlaceholder="Wyszukaj w Marketplace"
    :showSettings="true"
    :showSearch="true"
    :items="sidebarItems"
    :createButton="{ icon: Plus, text: 'Utwórz nowe ogłoszenie', route: '/marketplace/create/item' }"
  >
    <template #pre-list>
      <div class="px-2 pt-3 border-t border-theme-border">
        <h3 class="font-semibold text-[17px] mb-2 text-theme-text">Lokalizacja</h3>
        <button @click="openLocation" class="text-blue-600 text-sm hover:underline flex items-center">
          <MapMarker :size="16" class="mr-1" />
          Łęczyca gmina - W promieniu {{ props.selectedRadius }} km
        </button>
      </div>
    </template>

    <template #list-items>
      <div>
        <h3 class="font-semibold text-[17px] px-2 mb-2 text-theme-text">Kategorie</h3>
        <ul class="space-y-1">
          <li v-for="(cat, index) in categories" :key="index">
            <a href="#" class="flex items-center px-2 py-2 hover:bg-theme-hover rounded-lg cursor-pointer">
              <div class="mr-3">
                <component :is="cat.icon" :size="20" class="text-theme-text" />
              </div>
              <span class="font-medium text-[15px] text-theme-text">{{ cat.name }}</span>
            </a>
          </li>
        </ul>
      </div>
    </template>
  </Sidebar>
</template>

<style scoped>
/* keep sidebar styles local if needed */
</style>
