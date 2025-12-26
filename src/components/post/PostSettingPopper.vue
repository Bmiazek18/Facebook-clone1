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
    description: 'Przestań wyświetlać posty z tej strony.',
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
    description: 'Nie będziecie mogli się widzieć ani skontaktować.',
    icon: AccountCancelIcon,
  },
];

const handleClick = (item: MenuItem) => {
  console.log(`Kliknięto: ${item.label}`);
};
</script>

<template>
  <div class="bg-white w-full max-w-[340px] rounded-lg shadow-[0_2px_8px_rgba(0,0,0,0.15)] py-1 text-[#050505]">

    <div v-for="item in menuItems" :key="item.id">
      <button
        @click="handleClick(item)"
        class="group flex items-start w-full px-3 py-1.5 hover:bg-gray-100 transition-colors duration-200 text-left"
      >
        <div class="mr-2 mt-0.5 text-[#1c1e21] shrink-0">
          <component :is="item.icon" :size="20" />
        </div>

        <div class="flex flex-col">
          <span class="text-sm font-medium leading-tight text-[#050505]">
            {{ item.label }}
          </span>
          <span v-if="item.description" class="text-xs text-[#65676b] leading-tight mt-0.5">
            {{ item.description }}
          </span>
        </div>
      </button>

      <div v-if="item.hasSeparator" class="h-px bg-gray-200 my-1 mx-3"></div>
    </div>

  </div>
</template>
