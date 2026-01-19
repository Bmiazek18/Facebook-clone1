<script setup lang="ts">
import { markRaw, computed } from 'vue';
import { useI18n } from 'vue-i18n';
import type { Component } from 'vue';

// 1. Importowanie komponentów ikon z vue-material-design-icons
import EmailOutlineIcon from 'vue-material-design-icons/EmailOutline.vue';
import MessageTextOutlineIcon from 'vue-material-design-icons/MessageTextOutline.vue';
import BellOffOutlineIcon from 'vue-material-design-icons/BellOffOutline.vue';
import AlertOutlineIcon from 'vue-material-design-icons/AlertOutline.vue';
import ArchiveOutlineIcon from 'vue-material-design-icons/ArchiveOutline.vue';
import ExitToAppIcon from 'vue-material-design-icons/ExitToApp.vue';

const { t } = useI18n();

// 2. Definicja interfejsu MenuItem
interface MenuItemConfig {
  id: number;
  labelKey: string;
  icon: Component;
  action: string;
  isDestructive?: boolean;
}

interface MenuItemData extends MenuItemConfig {
  label: string;
}

// 3. Dane menu z przypisanymi ikonami
const menuItemsConfig: MenuItemConfig[] = [
  { id: 1, labelKey: 'chat.markAsRead', icon: markRaw(EmailOutlineIcon), action: 'mark-as-read' },
  { id: 2, labelKey: 'chat.openMacMessenger', icon: markRaw(MessageTextOutlineIcon), action: 'open-mac-messenger' },
  { id: 3, labelKey: 'chat.muteNotifications', icon: markRaw(BellOffOutlineIcon), action: 'mute-notifications' },
  { id: 4, labelKey: 'chat.report', icon: markRaw(AlertOutlineIcon), action: 'report' },
  { id: 5, labelKey: 'chat.archive', icon: markRaw(ArchiveOutlineIcon), action: 'archive' },
  { id: 6, labelKey: 'chat.leaveChannel', icon: markRaw(ExitToAppIcon), action: 'leave-channel', isDestructive: true },
];

// Computed property to get translated menu items
const menuItems = computed(() =>
  menuItemsConfig.map(item => ({
    ...item,
    label: t(item.labelKey)
  }))
);

// 4. Definicja Emisji Zdarzeń
const emit = defineEmits<{
  (e: 'select-action', action: string): void;
}>();

// 5. Funkcja obsługująca kliknięcie
const handleItemClick = (item: MenuItemData) => {
  emit('select-action', item.action);
  console.log(`Akcja: ${item.action}`);
};
</script>

<template>
  <div style="filter: drop-shadow(rgba(0, 0, 0, 0.2) 0px 0px 6px);" class="w-72 bg-white rounded-xl  z-30 p-2 font-[15px] font-sans">
    <ul role="menu" class="list-none m-0 p-0">
      <li
        v-for="item in menuItems"
        :key="item.id"
        role="menuitem"
        @click="handleItemClick(item)"
        class="flex items-center p-3 rounded-lg cursor-pointer transition-colors text-base font-medium hover:bg-gray-100"
        :class="{
          'text-red-600': item.isDestructive,
          'text-gray-900': !item.isDestructive,
        }"
      >
        <span class="mr-4" :class="{ 'text-red-600': item.isDestructive, 'text-gray-600': !item.isDestructive }">
          <component :is="item.icon" :size="24" />
        </span>

        <span class="grow">{{ item.label }}</span>
      </li>
    </ul>
  </div>
</template>
