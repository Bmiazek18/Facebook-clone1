<template>
  <div>
    <div class="shadow-xl p-3 rounded-lg bg-theme-bg-secondary">
      <RouterLink
        to="/profile"
        class="flex pt-2 items-center space-x-3 pb-3 border-b border-theme-border hover:bg-gray-100 cursor-pointer"
      >
        <img
          src="https://scontent-waw2-1.xx.fbcdn.net/v/t39.30808-1/295055057_582985040112298_215415809791370036_n.jpg?stp=cp0_dst-jpg_s40x40_tt6&_nc_cat=104&ccb=1-7&_nc_sid=e99d92&_nc_ohc=-o822DQWa_kQ7kNvwEBBrQN&_nc_oc=Adk7CLzzn6vvAFCclTDzM32DkA0bnwHJCU8V-LZ-6Rgt046578D_zYBPKIpVqrH_jqSITUodiSom9HftYGfou-YR&_nc_zt=24&_nc_ht=scontent-waw2-1.xx&_nc_gid=hWinwIkg4qpusDkFaBv_tg&oh=00_AfhegpWXzJqTqkSqYk4lk-AflwjwvP0sVVYiWvBV-lyexg&oe=6917A7AC"
          class="h-10 w-10 bg-gray-300 rounded-full shrink-0"
          alt="avatar"
        />
        <span class="font-semibold text-theme-text font-[15px] truncate">
          Bartosz Miazek
        </span>
      </RouterLink>

      <button
        class="w-full mt-3 py-2 px-3 flex items-center justify-center bg-gray-100 hover:bg-theme-hover rounded-lg text-sm font-medium text-gray-800 text-theme-text transition duration-150"
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
          class="w-full flex items-center p-2 rounded-lg hover:bg-theme-hover transition duration-150"
        >
          <span class="h-9 w-9 bg-theme-bg bg-theme-hover rounded-full flex items-center justify-center mr-3 shrink-0">
            <component :is="item.iconComponent" class="text-xl text-theme-text" />
          </span>

          <span class="text-theme-text font-[15px] ">{{ $t(item.labelKey) }}</span>

          <span class="ml-auto flex items-center">
            <ChevronRightIcon v-if="item.hasSubMenu" size="30" />
          </span>
        </button>
      </li>
    </ul>
     <div  class="px-2 text-xs font-bold text-theme-text-secondary leading-tight mt-4">
      <a href="#" class="hover:underline">{{ $t('common.privacy') }}</a> ·
      <a href="#" class="hover:underline">{{ $t('common.terms') }}</a> ·
      <a href="#" class="hover:underline">{{ $t('common.advertising') }}</a> ·
      <a href="#" class="hover:underline">{{ $t('common.adOptions') }}</a>
      <p class="mt-0.5">
        · <a href="#" class="hover:underline">{{ $t('common.cookies') }}</a>
        · <a href="#" class="hover:underline">{{ $t('common.more') }}</a>
      </p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, type Ref, type DefineComponent } from 'vue';
import { useI18n } from 'vue-i18n';

import ChevronRightIcon from 'vue-material-design-icons/ChevronRight.vue';
import CogIcon from 'vue-material-design-icons/Cog.vue';
import HelpCircleOutlineIcon from 'vue-material-design-icons/HelpCircleOutline.vue';
import WeatherNightIcon from 'vue-material-design-icons/WeatherNight.vue';
import CommentProcessingOutlineIcon from 'vue-material-design-icons/CommentProcessingOutline.vue';
import LogoutIcon from 'vue-material-design-icons/Logout.vue';
import AccountSearchIcon from 'vue-material-design-icons/AccountSearch.vue';

useI18n();

interface MenuItem {
  name: string;
  labelKey: string;
  iconComponent: DefineComponent;
  hasSubMenu?: boolean;
}

const menuItems: Ref<MenuItem[]> = ref([
  {
    name: 'settings',
    labelKey: 'profile_menu.settings',
    iconComponent: CogIcon,
    hasSubMenu: true,
  },
  {
    name: 'help',
    labelKey: 'profile_menu.help',
    iconComponent: HelpCircleOutlineIcon,
    hasSubMenu: true,
  },
  {
    name: 'display',
    labelKey: 'profile_menu.display',
    iconComponent: WeatherNightIcon,
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
]);

const emit = defineEmits(['navigate']);

const handleMenuClick = (item: MenuItem): void => {
  emit('navigate', item.name);
};
</script>


