<template>
  <div>
    <div class="shadow-theme p-3 mt-1 mx-2 rounded-lg bg-theme-bg-secondary">
      <NuxtLink
        :to="profileLink"
        class="flex pt-2 px-1 rounded-lg items-center space-x-3 pb-2 hover:bg-theme-hover cursor-pointer"
      >
        <img
          :src="currentUser?.avatar || 'https://i.pravatar.cc/150?img=1'"
          class="h-10 w-10 bg-theme-bg-tertiary rounded-full shrink-0"
          :alt="currentUser?.name || 'avatar'"
        />
        <span class="font-semibold text-theme-text font-[15px] truncate">
          {{ currentUser?.name }}
        </span>
      </NuxtLink>
      <div class="border-b border-theme-border my-1"></div>
      <button
        class="w-full mt-3 py-2 px-3 flex items-center justify-center bg-theme-bg-tertiary hover:bg-theme-hover rounded-lg text-sm font-medium text-theme-text transition duration-150"
      >
        <AccountSearchIcon class="mr-2 h-5 w-5 text-gray-600 text-theme-text-secondary" />
        {{ $t('profile_menu.viewAllProfiles') }}
      </button>
    </div>

    <div class="my-3 border-theme-border"></div>

    <ul role="menu" class="space-y-1">
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

          <span class="text-theme-text font-[15px] font-medium" style="font-size: 15px;">{{ $t(item.labelKey) }}</span>

          <span class="ml-auto flex items-center">
            <ChevronRightIcon v-if="item.hasSubMenu" size="30" class="text-theme-text" />
          </span>
        </button>
      </li>
    </ul>
    <div class="px-2 pb-3 text-xs font-bold text-theme-text-secondary leading-tight mt-4">
      <a href="#" class="hover:underline">{{ $t('common.privacy') }}</a> ·
      <a href="#" class="hover:underline">{{ $t('common.terms') }}</a> ·
      <a href="#" class="hover:underline">{{ $t('common.advertising') }}</a> ·
      <a href="#" class="hover:underline">{{ $t('common.adOptions') }}</a>
      <p class="mt-0.5">
        · <a href="#" class="hover:underline">{{ $t('common.cookies') }}</a> ·
        <a href="#" class="hover:underline">{{ $t('common.more') }}</a>
      </p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, type Ref, type DefineComponent } from 'vue'
import { useI18n } from 'vue-i18n'
import { useAuthStore } from '@/stores/auth'

import ChevronRightIcon from 'vue-material-design-icons/ChevronRight.vue'
import HelpCircleOutlineIcon from 'vue-material-design-icons/HelpCircleOutline.vue'
import WeatherNightIcon from 'vue-material-design-icons/WeatherNight.vue'
import CommentProcessingOutlineIcon from 'vue-material-design-icons/CommentProcessingOutline.vue'
import LogoutIcon from 'vue-material-design-icons/Logout.vue'
import AccountSearchIcon from 'vue-material-design-icons/AccountSearch.vue'
import TranslateIcon from 'vue-material-design-icons/Translate.vue'
import CogIcon from 'vue-material-design-icons/Cog.vue'

useI18n()

const authStore = useAuthStore()

// Get current user data
const currentUser = computed(() => authStore.currentUser)

// Create profile link based on current user
const profileLink = computed(() => {
  return currentUser.value ? `/profile/${currentUser.value.id}` : '/profile'
})

interface MenuItem {
  name: string
  labelKey: string
  iconComponent: DefineComponent
  hasSubMenu?: boolean
}

const menuItems: Ref<MenuItem[]> = ref([
  {
    name: 'settings',
    labelKey: 'profile_menu.settings',
    iconComponent: CogIcon,
    hasSubMenu: true,
  },

  {
    name: 'display',
    labelKey: 'profile_menu.display',
    iconComponent: WeatherNightIcon,
    hasSubMenu: true,
  },
  {
    name: 'language',
    labelKey: 'profile_menu.language',
    iconComponent: TranslateIcon,
    hasSubMenu: true,
  },
  {
    name: 'feedback',
    labelKey: 'profile_menu.feedback',
    iconComponent: CommentProcessingOutlineIcon,
  },
  {
    name: 'logout',
    labelKey: 'profile_menu.logout',
    iconComponent: LogoutIcon,
  },
])

const emit = defineEmits(['navigate'])

const handleMenuClick = (item: MenuItem): void => {
  emit('navigate', item.name)
}
</script>
