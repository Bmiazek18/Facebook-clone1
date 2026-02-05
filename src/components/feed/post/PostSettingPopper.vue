<script setup lang="ts">
import { type Component } from 'vue';

// Import ikon
import PlusCircleIcon from 'vue-material-design-icons/PlusCircle.vue';
import MinusCircleIcon from 'vue-material-design-icons/MinusCircle.vue';
import BookmarkIcon from 'vue-material-design-icons/Bookmark.vue';
import LinkVariantIcon from 'vue-material-design-icons/LinkVariant.vue';
import BellIcon from 'vue-material-design-icons/Bell.vue';
import InformationIcon from 'vue-material-design-icons/Information.vue';
import XmlIcon from 'vue-material-design-icons/Xml.vue';
import CloseBoxIcon from 'vue-material-design-icons/CloseBox.vue';
import ClockTimeFourIcon from 'vue-material-design-icons/ClockTimeFour.vue';
import TextBoxRemoveIcon from 'vue-material-design-icons/TextBoxRemove.vue';
import MessageAlertIcon from 'vue-material-design-icons/MessageAlert.vue';
import AccountCancelIcon from 'vue-material-design-icons/AccountCancel.vue';

interface MenuItem {
  id: number;
  label: string;
  description?: string;
  icon: Component;
  hasSeparator?: boolean;
  action?: () => void;
}

const menuItems: MenuItem[] = [
  {
    id: 1,
    label: 'Wyświetl więcej',
    description: 'Będziesz widzieć więcej takich postów.',
    icon: PlusCircleIcon,
  },
  {
    id: 2,
    label: 'Wyświetl mniej',
    description: 'Będziesz widzieć mniej takich postów.',
    icon: MinusCircleIcon,
    hasSeparator: true,
  },
  {
    id: 3,
    label: 'Zapisz film',
    description: 'Dodaj do zapisanych elementów.',
    icon: BookmarkIcon,
    hasSeparator: true,
  },
  {
    id: 4,
    label: 'Kopiuj link',
    icon: LinkVariantIcon,
  },
  {
    id: 5,
    label: 'Włącz powiadomienia',
    icon: BellIcon,
  },
  {
    id: 6,
    label: 'Dlaczego widzę ten post?',
    icon: InformationIcon,
  },
  {
    id: 7,
    label: 'Osadź',
    icon: XmlIcon,
    hasSeparator: true,
  },
  {
    id: 8,
    label: 'Ukryj post',
    description: 'Wyświetlaj mniej postów podobnych do tego.',
    icon: CloseBoxIcon,
  },
  {
    id: 9,
    label: 'Wstrzymaj relacje ŁKS Łazy na 30 dni',
    description: 'Tymczasowo zatrzymaj wyświetlanie postów.',
    icon: ClockTimeFourIcon,
  },
  {
    id: 10,
    label: 'Przestań obserwować użytkownika ŁKS Łazy',
    description: 'Przestań wyświetlać posty z tej strony. Strona nie otrzyma powiadomienia o Twojej rezygnacji z obserwowania.',
    icon: TextBoxRemoveIcon,
  },
  {
    id: 11,
    label: 'Zgłoś post',
    description: 'Nie poinformujemy ŁKS Łazy, kto to zgłosił.',
    icon: MessageAlertIcon,
  },
  {
    id: 12,
    label: 'Zablokuj profil ŁKS Łazy',
    description: 'Nie będziecie mogli się widzieć ani skontaktować się ze sobą.',
    icon: AccountCancelIcon,
  },
];

const handleClick = (item: MenuItem) => {
  console.log(`Kliknięto: ${item.label}`);
};
</script>

<template>
  <div class="bg-theme-bg-secondary w-full max-w-sm rounded-lg shadow-[0_4px_12px_rgba(0,0,0,0.15)] py-1 text-theme-text">

    <div v-for="item in menuItems" :key="item.id">
      <button
        @click="handleClick(item)"
        class="group flex items-start w-full px-3 py-1.5 hover:bg-theme-hover transition-colors duration-200 text-left"
      >
        <div class="mr-3 mt-0.5 text-theme-text shrink-0">
          <component :is="item.icon" :size="20" />
        </div>

        <div class="flex flex-col">
          <span class="text-[14px] font-medium leading-5 text-theme-text">
            {{ item.label }}
          </span>
          <span v-if="item.description" class="text-[13px] text-theme-text-secondary leading-4 mt-0.5">
            {{ item.description }}
          </span>
        </div>
      </button>

      <div v-if="item.hasSeparator" class="h-px bg-theme-border my-1 mx-3"></div>
    </div>

  </div>
</template>
