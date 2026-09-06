<template>
  <div
    class="settings-view max-w-xl mx-auto bg-white dark:bg-theme-bg-secondary text-theme-text rounded-xl p-4"
  >
    <!-- Nagłówek -->
    <div class="w-full flex items-center pb-4">
      <button
        @click="handleBackClick"
        class="rounded-full p-2 -ml-2 hover:bg-gray-100 dark:hover:bg-theme-hover transition duration-150 mr-2"
        :aria-label="$t('common.back')"
      >
        <ArrowLeftIcon class="text-2xl text-theme-text" />
      </button>
      <span class="text-theme-text font-bold text-xl">{{ $t('profile_menu.settings') }}</span>
    </div>

    <!-- Lista opcji -->
    <nav>
      <ul class="flex flex-col gap-1 p-0 m-0 list-none">
        <li v-for="item in menuItems" :key="item.name">
          <button
            @click="handleMenuClick(item)"
            class="w-full flex items-center p-2 rounded-lg hover:bg-theme-hover transition duration-150 cursor-pointer"
          >
            <span
              class="h-9 w-9 bg-theme-bg-tertiary rounded-full flex items-center justify-center mr-3 shrink-0"
            >
              <component :is="item.iconComponent" class="text-xl text-theme-text" />
            </span>

            <span class="text-theme-text font-[15px] font-medium" style="font-size: 15px;">
              {{ $t(item.labelKey) }}
            </span>

            <span class="ml-auto flex items-center">
              <ChevronRightIcon v-if="item.hasSubMenu" size="30" class="text-theme-text" />
            </span>
          </button>
        </li>
      </ul>
    </nav>
  </div>
</template>

<script setup lang="ts">
import { useI18n } from 'vue-i18n'

// Ikony
import ArrowLeftIcon from 'vue-material-design-icons/ArrowLeft.vue'
import CogIcon from 'vue-material-design-icons/Cog.vue'
import WebIcon from 'vue-material-design-icons/Web.vue'
import LockCheckIcon from 'vue-material-design-icons/LockCheck.vue'
import LockIcon from 'vue-material-design-icons/Lock.vue'
import FormatListBulletedIcon from 'vue-material-design-icons/FormatListBulleted.vue'
import CardTextIcon from 'vue-material-design-icons/CardText.vue'
import ChevronRightIcon from 'vue-material-design-icons/ChevronRight.vue'

useI18n()

const emit = defineEmits(['back', 'navigate'])

const handleBackClick = () => {
  emit('back')
}

// Obsługa kliknięcia elementu z menuItems
const handleMenuClick = (item: any) => {
  if (item.action) {
    item.action()
  } else if (item.href) {
    // Nawigacja lub zmiana trasy
    window.location.href = item.href
  }
}

const handleLanguageClick = () => {
  emit('navigate', 'language')
}

// Tablica menuItems dostosowana do Twojego szablonu HTML
const menuItems = [
  {
    name: 'settings',
    labelKey: 'settings.title', // Wpisz tu właściwe klucze tlumaczeń i18n
    iconComponent: CogIcon,
    href: '#',
  },
  {
    name: 'language',
    labelKey: 'settings.language',
    iconComponent: WebIcon,
    hasSubMenu: true,
    action: handleLanguageClick,
  },
  {
    name: 'privacyCheckup',
    labelKey: 'settings.privacyCheckup',
    iconComponent: LockCheckIcon,
    href: '#',
  },
  {
    name: 'privacyCenter',
    labelKey: 'settings.privacyCenter',
    iconComponent: LockIcon,
    href: '#',
  },
  {
    name: 'activityLog',
    labelKey: 'settings.activityLog',
    iconComponent: FormatListBulletedIcon,
    href: '#',
  },
  {
    name: 'feedPreferences',
    labelKey: 'settings.feedPreferences',
    iconComponent: CardTextIcon,
    href: '#',
  },
]
</script>

<style scoped>
/* Jeśli posiadasz dodatkowe style scoped, dodaj je tutaj */
</style>
