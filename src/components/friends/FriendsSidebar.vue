<template>
  <div class="w-[360px] bg-theme-bg-secondary shadow-lg h-screen fixed left-0 top-[50px] pt-4 px-2 flex flex-col overflow-y-auto z-10">
    <div class="flex justify-between items-center px-2 mb-4">
      <h1 class="text-[24px] font-bold text-theme-text">{{ t('friends.friends') }}</h1>
      <div class="bg-theme-bg-tertiary w-9 h-9 rounded-full flex items-center justify-center cursor-pointer hover:bg-theme-bg-hover transition-colors">
        <CogIcon :size="20" />
      </div>
    </div>

    <div class="flex flex-col gap-1">
      <router-link
        v-for="(item, index) in menuItems"
        :key="index"
        :to="item.path"
        v-slot="{ isActive }"
      >
        <div
          class="flex items-center justify-between px-2 py-2 hover:bg-theme-bg-tertiary rounded-lg cursor-pointer group transition-colors"
          :class="{ 'bg-theme-bg-tertiary': isActive }"
        >
          <div class="flex items-center gap-3">
            <div
              class="rounded-full p-1.5"
              :class="[isActive ? 'bg-[#1877f2] text-white' : 'bg-theme-bg-tertiary text-theme-text group-hover:bg-theme-bg-secondary']"
            >
              <component :is="item.icon" :size="20" />
            </div>
            <span class="text-[17px] font-medium text-theme-text">{{ t(item.label) }}</span>
          </div>
          <ChevronRightIcon v-if="!isActive" :size="24" class="text-theme-text-secondary" />
        </div>
      </router-link>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useI18n } from 'vue-i18n';

// Icons
import CogIcon from 'vue-material-design-icons/Cog.vue';
import AccountGroupIcon from 'vue-material-design-icons/AccountGroup.vue';
import AccountPlusIcon from 'vue-material-design-icons/AccountPlus.vue';
import AccountMultipleIcon from 'vue-material-design-icons/AccountMultiple.vue';
import GiftIcon from 'vue-material-design-icons/Gift.vue';
import ChevronRightIcon from 'vue-material-design-icons/ChevronRight.vue';

const { t } = useI18n();

const menuItems = [
  { label: 'friends.home', icon: AccountGroupIcon, path: '/friends' },
  { label: 'friends.friendRequests', icon: AccountPlusIcon, path: '/friends/requests' },
  { label: 'friends.suggestions', icon: AccountPlusIcon, path: '/friends/suggestions' },
  { label: 'friends.allFriends', icon: AccountMultipleIcon, path: '/friends/list' },
  { label: 'friends.birthdays', icon: GiftIcon, path: '/friends/birthday' },
];
</script>

<style scoped>
/* Optional: Hide scrollbar for aesthetics */
::-webkit-scrollbar {
  width: 8px;
}
::-webkit-scrollbar-track {
  background: transparent;
}
::-webkit-scrollbar-thumb {
  background-color: #bcc0c4;
  border-radius: 4px;
}
</style>
