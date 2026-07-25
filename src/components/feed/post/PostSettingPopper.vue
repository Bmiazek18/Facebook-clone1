<script setup lang="ts">
import { type Component } from 'vue'

// Najbardziej zbliżone odpowiedniki z vue-material-design-icons
import PlusCircleOutline from 'vue-material-design-icons/PlusCircleOutline.vue'
import MinusCircleOutline from 'vue-material-design-icons/MinusCircleOutline.vue'
import BellOutline from 'vue-material-design-icons/BellOutline.vue'
import InformationOutline from 'vue-material-design-icons/InformationOutline.vue'
import CodeTags from 'vue-material-design-icons/CodeTags.vue'
import CloseBoxOutline from 'vue-material-design-icons/CloseBoxOutline.vue'
import ClockOutline from 'vue-material-design-icons/ClockOutline.vue'
import AccountMinusOutline from 'vue-material-design-icons/AccountMinusOutline.vue'
import AlertCircleOutline from 'vue-material-design-icons/AlertCircleOutline.vue'
import AccountCancelOutline from 'vue-material-design-icons/AccountCancelOutline.vue'

interface MenuItem {
  id: number
  label: string
  description?: string
  icon: Component
  hasSeparator?: boolean
}

const menuItems: MenuItem[] = [
  {
    id: 1,
    label: 'Wyświetl więcej',
    description: 'Będziesz widzieć więcej takich postów.',
    icon: PlusCircleOutline,
  },
  {
    id: 2,
    label: 'Wyświetl mniej',
    description: 'Będziesz widzieć mniej takich postów.',
    icon: MinusCircleOutline,
    hasSeparator: true,
  },
  {
    id: 3,
    label: 'Włącz powiadomienia',
    icon: BellOutline,
  },
  {
    id: 4,
    label: 'Dlaczego widzę ten post?',
    icon: InformationOutline,
  },
  {
    id: 5,
    label: 'Osadź',
    icon: CodeTags,
    hasSeparator: true,
  },
  {
    id: 6,
    label: 'Ukryj post',
    description: 'Wyświetlaj mniej postów podobnych do tego.',
    icon: CloseBoxOutline,
  },
  {
    id: 7,
    label: 'Wstrzymaj relacje MKP Pogoń Siedlce na 30 dni',
    description: 'Tymczasowo zatrzymaj wyświetlanie postów.',
    icon: ClockOutline,
  },
  {
    id: 8,
    label: 'Przestań obserwować użytkownika MKP Pogoń Siedlce',
    description:
      'Przestań wyświetlać posty z tej strony. Strona nie otrzyma powiadomienia o Twojej rezygnacji z obserwowania.',
    icon: AccountMinusOutline,
  },
  {
    id: 9,
    label: 'Zgłoś post',
    description: 'Nie poinformujemy MKP Pogoń Siedlce, kto to zgłosił.',
    icon: AlertCircleOutline,
  },
  {
    id: 10,
    label: 'Zablokuj profil MKP Pogoń Siedlce',
    description: 'Nie będziecie mogli się widzieć ani skontaktować się ze sobą.',
    icon: AccountCancelOutline,
  },
]

const handleClick = (item: MenuItem) => {
  console.log(`Kliknięto: ${item.label}`)
}
</script>

<template>
  <div
    class="bg-theme-bg-secondary w-full max-w-[350px] rounded-2xl shadow-xl py-1.5 text-theme-text border border-gray-100 dark:border-gray-800"
  >
    <ul class="p-0 m-0 list-none">
      <li v-for="item in menuItems" :key="item.id">
        <button
          @click="handleClick(item)"
          class="group flex items-start w-full px-2.5 py-1.25 hover:bg-theme-hover transition-colors duration-150 text-left rounded-lg cursor-pointer"
        >
          <!-- Ikona -->
          <div class="mr-2.5 mt-0.5 text-gray-900 dark:text-gray-100 shrink-0">
            <component :is="item.icon" :size="20" />
          </div>

          <!-- Treść -->
          <div class="flex flex-col min-w-0 pr-1">
            <span class="text-[14px] font-semibold text-gray-900 dark:text-gray-100 leading-tight">
              {{ item.label }}
            </span>
            <span
              v-if="item.description"
              class="text-[12px] text-gray-500 dark:text-gray-400 font-normal leading-tight mt-0.5"
            >
              {{ item.description }}
            </span>
          </div>
        </button>

        <!-- Separator -->
        <div v-if="item.hasSeparator" class="h-px bg-gray-200 dark:bg-gray-700 my-1 mx-2.5"></div>
      </li>
    </ul>
  </div>
</template>

<style scoped>
:deep(svg) {
  fill: currentColor;
}
</style>
