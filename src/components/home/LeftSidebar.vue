<script setup lang="ts">
import { ref, computed } from 'vue';
import { useAuthStore } from '@/stores/auth';

// Import ikon
import AccountMultiple from 'vue-material-design-icons/AccountMultiple.vue'; // Znajomi
import CalendarStar from 'vue-material-design-icons/CalendarStar.vue'; // Wydarzenia
import Bookmark from 'vue-material-design-icons/Bookmark.vue'; // Zapisane
import StorefrontOutline from 'vue-material-design-icons/StorefrontOutline.vue'; // Marketplace
import ClockTimeTwoOutline from 'vue-material-design-icons/ClockTimeTwoOutline.vue'; // Wspomnienia
import AccountGroup from 'vue-material-design-icons/AccountGroup.vue'; // Grupy
import ChevronDown from 'vue-material-design-icons/ChevronDown.vue'; // Strzałka w dół
import ChevronUp from 'vue-material-design-icons/ChevronUp.vue'; // Strzałka w górę
import Robot from 'vue-material-design-icons/Robot.vue'; // Meta AI (zamiennik)

const auth = useAuthStore();
const showMoreMenu = ref(false);
const showMoreShortcuts = ref(false);

// Główne Menu (na podstawie Twojego zrzutu)
const menuItems = [
  { icon: Robot, label: 'Meta AI', color: '#2ABBA7', isGradient: true, path: '/' }, // Gradient dla AI
  { icon: AccountMultiple, label: 'Znajomi', color: '#1B74E4', path: '/friends' },
  { icon: CalendarStar, label: 'Wydarzenia', color: '#F3425F', path: '/event' }, // Czerwony
  { icon: Bookmark, label: 'Zapisane', color: '#A033FF', path: '/' }, // Fioletowy
  { icon: StorefrontOutline, label: 'Marketplace', color: '#1B74E4', path: '/marketplace' },
  { icon: ClockTimeTwoOutline, label: 'Wspomnienia', color: '#2ABBA7', path: '/' },
  { icon: AccountGroup, label: 'Grupy', color: '#1B74E4', path: '/groups' },
];

// Lista Skrótów (Przykładowe dane z obrazka)
const shortcuts = [
  { img: 'https://picsum.photos/40/40?random=1', label: 'Basketball FRVR' },
  { img: 'https://picsum.photos/40/40?random=2', label: 'Kolegium Sędziów BOZPN' },
  { img: 'https://picsum.photos/40/40?random=3', label: 'Praca dla początkujących...' },
  { img: 'https://picsum.photos/40/40?random=4', label: 'Reprezentacja Polski...' },
  { img: 'https://picsum.photos/40/40?random=5', label: 'Piłkarski Świat' },
];

// Logika wyświetlania (pokaż 5 pierwszych lub wszystkie)
const visibleMenuItems = computed(() => showMoreMenu.value ? menuItems : menuItems.slice(0, 5));
const visibleShortcuts = computed(() => showMoreShortcuts.value ? shortcuts : shortcuts.slice(0, 5));
</script>

<template>
  <div class="fixed flex flex-col h-[calc(100vh-56px)] p-2 max-w-[360px] pr-2 sticky top-[56px] bg-theme-bg">

    <div class="flex-1 overflow-y-auto custom-scrollbar">

      <a class="flex items-center justify-start w-full cursor-pointer hover:bg-theme-hover p-2 rounded-lg transition-colors mb-2">
        <img
          class="rounded-full w-[36px] h-[36px] object-cover border border-theme-border"
          :src="auth.currentUser?.avatar || 'https://placehold.co/40'"
          alt="Avatar"
        />
        <div class="text-[15px] text-theme-text font-medium pl-3">
          {{ auth.currentUser?.name || 'Bartosz Miazek' }}
        </div>
      </a>

      <div class="space-y-1">
        <RouterLink
          v-for="item in visibleMenuItems"
          :key="item.label"
          :to="item.path"
          class="flex items-center w-full hover:bg-theme-hover rounded-lg p-2 transition-colors group"
        >
          <div v-if="item.isGradient" class="flex items-center justify-center w-9 h-9 rounded-full bg-gradient-to-tr from-blue-400 to-purple-500">
             <component :is="item.icon" :size="24" class="text-theme-text" />
          </div>
          <div v-else class="flex items-center justify-center w-9 h-9">
            <component :is="item.icon" :size="28" :fillColor="item.color" />
          </div>
          <span class="text-[15px] text-theme-text font-medium pl-3 truncate">
            {{ item.label }}
          </span>
        </RouterLink>

        <button
          @click="showMoreMenu = !showMoreMenu"
          class="flex items-center w-full hover:bg-theme-hover rounded-lg p-2 transition-colors"
        >
          <div class="flex items-center justify-center w-9 h-9 rounded-full bg-theme-bg-tertiary">
            <component :is="showMoreMenu ? ChevronUp : ChevronDown" :size="24" class="text-theme-text" />
          </div>
          <span class="text-[15px] text-theme-text font-medium pl-3">
            {{ showMoreMenu ? 'Zobacz mniej' : 'Zobacz więcej' }}
          </span>
        </button>
      </div>

      <div class="border-b border-theme-border my-4 mx-2"></div>

      <div class="px-2 pb-4"> <div class="flex justify-between items-center mb-2 group">
          <h3 class="text-[17px] font-semibold text-theme-text-secondary group-hover:text-theme-text transition-colors">
            Twoje skróty
          </h3>
          <button class="text-theme-primary text-[15px] opacity-0 group-hover:opacity-100 hover:underline transition-opacity px-2 py-1 rounded">
            Edytuj
          </button>
        </div>

        <div class="space-y-1">
          <a
            v-for="shortcut in visibleShortcuts"
            :key="shortcut.label"
            href="#"
            class="flex items-center w-full hover:bg-theme-hover rounded-lg p-2 transition-colors"
          >
            <img :src="shortcut.img" class="w-9 h-9 rounded-lg object-cover" alt="Shortcut" />
            <span class="text-[15px] text-theme-text font-medium pl-3 truncate line-clamp-2 leading-tight">
              {{ shortcut.label }}
            </span>
          </a>

          <button
            v-if="shortcuts.length > 5"
            @click="showMoreShortcuts = !showMoreShortcuts"
            class="flex items-center w-full hover:bg-theme-hover rounded-lg p-2 transition-colors"
          >
            <div class="flex items-center justify-center w-9 h-9 rounded-full bg-theme-bg-tertiary">
              <component :is="showMoreShortcuts ? ChevronUp : ChevronDown" :size="24" class="text-theme-text" />
            </div>
            <span class="text-[15px] text-theme-text font-medium pl-3">
              {{ showMoreShortcuts ? 'Zobacz mniej' : 'Zobacz więcej' }}
            </span>
          </button>
        </div>
      </div>
    </div>
    <div class="mt-auto py-4 text-[13px] text-theme-text-secondary ">
      <p>Prywatność · Regulamin · Reklama · Wybór reklam · Pliki cookie · Meta © 2026</p>
    </div>

  </div>
</template>

<style scoped>
.custom-scrollbar::-webkit-scrollbar {
  width: 8px;
}
.custom-scrollbar::-webkit-scrollbar-track {
  background: transparent;
}
.custom-scrollbar:hover::-webkit-scrollbar-thumb {
  background: var(--color-hover-strong); /* Ciemniejszy kolor paska dla lepszej widoczności */
  border-radius: 4px;
}
.custom-scrollbar::-webkit-scrollbar-thumb {
  background: transparent;
}
</style>
