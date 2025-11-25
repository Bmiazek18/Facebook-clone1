<script setup lang="ts">
import { defineEmits, ref, markRaw } from 'vue';
import type { Component } from 'vue';

// 1. Importowanie komponentów ikon z vue-material-design-icons
import EmailOutlineIcon from 'vue-material-design-icons/EmailOutline.vue';
import MessageTextOutlineIcon from 'vue-material-design-icons/MessageTextOutline.vue';
import BellOffOutlineIcon from 'vue-material-design-icons/BellOffOutline.vue';
import AlertOutlineIcon from 'vue-material-design-icons/AlertOutline.vue';
import ArchiveOutlineIcon from 'vue-material-design-icons/ArchiveOutline.vue';
import ExitToAppIcon from 'vue-material-design-icons/ExitToApp.vue';

// 2. Definicja interfejsu MenuItem
interface MenuItem {
  id: number;
  label: string;
  icon: Component;
  action: string;
  isDestructive?: boolean;
}

// 3. Dane menu z przypisanymi ikonami
const menuItems = ref<MenuItem[]>([
  { id: 1, label: 'Oznacz jako przeczytane', icon: markRaw(EmailOutlineIcon), action: 'mark-as-read' },
  { id: 2, label: 'Otwórz Messengera na komputery Mac', icon: markRaw(MessageTextOutlineIcon), action: 'open-mac-messenger' },
  { id: 3, label: 'Wycisz powiadomienia', icon: markRaw(BellOffOutlineIcon), action: 'mute-notifications' },
  { id: 4, label: 'Zgłoś', icon: markRaw(AlertOutlineIcon), action: 'report' },
  { id: 5, label: 'Archiwizuj', icon: markRaw(ArchiveOutlineIcon), action: 'archive' },
  { id: 6, label: 'Opuść kanał', icon: markRaw(ExitToAppIcon), action: 'leave-channel', isDestructive: true },
]);

// 4. Definicja Emisji Zdarzeń
const emit = defineEmits<{
  (e: 'select-action', action: string): void;
}>();

// 5. Funkcja obsługująca kliknięcie
const handleItemClick = (item: MenuItem) => {
  emit('select-action', item.action);
  console.log(`Akcja: ${item.action}`);
};

// 6. Funkcja do symulacji użycia
const simulateAppUse = (action: string) => {
    alert(`Wybrano akcję: ${action}`);
}
</script>

<template>
  <div style="filter: drop-shadow(rgba(0, 0, 0, 0.2) 0px 0px 6px);" class="w-72 bg-white absolute top-[40px] mx-[100%] right-[-10px] rounded-xl  z-30 p-2 font-[15px] font-sans">
    <ul role="menu" class="list-none m-0 p-0">
      <li
        v-for="item in menuItems"
        :key="item.id"
        role="menuitem"
        @click="handleItemClick(item); simulateAppUse(item.action)"

        class="
          flex items-center p-3 rounded-lg cursor-pointer transition-colors text-base font-medium
          hover:bg-gray-100

          "
          :class="{
            'text-red-600': item.isDestructive,
            'text-gray-900': !item.isDestructive,
          }"
      >
        <span class="mr-4"
              :class="{ 'text-red-600': item.isDestructive, 'text-gray-600': !item.isDestructive }"
        >
          <component :is="item.icon" :size="24" />
        </span>

        <span class="flex-grow">{{ item.label }}</span>
      </li>
    </ul>
  </div>
</template>
