<script setup lang="ts">
import AccountMultiple from 'vue-material-design-icons/AccountMultiple.vue';
import Flag from 'vue-material-design-icons/Flag.vue';
import ClockTimeTwoOutline from 'vue-material-design-icons/ClockTimeTwoOutline.vue';
import AccountGroup from 'vue-material-design-icons/AccountGroup.vue';
import StorefrontOutline from 'vue-material-design-icons/StorefrontOutline.vue';
import TelevisionPlay from 'vue-material-design-icons/TelevisionPlay.vue';
import Restore from 'vue-material-design-icons/Restore.vue';

defineProps<{
  userName: string;
  userAvatar: string;
}>();

const menuItems = [
  { icon: AccountMultiple, label: 'home.friends', bgColor: '#1B74E4' },
  { icon: Flag, label: 'home.pages', bgColor: '#EAB308' },
  { icon: ClockTimeTwoOutline, label: 'home.mostRecent', bgColor: '#10B981' },
  { icon: AccountGroup, label: 'home.groups', bgColor: '#9333EA' },
  { icon: StorefrontOutline, label: 'home.marketplace', bgColor: '#F97316' },
  { icon: TelevisionPlay, label: 'home.watch', bgColor: '#EF4444' },
  { icon: Restore, label: 'home.memories', bgColor: '#3B82F6' }
];
</script>

<template>
  <div class="max-w-[320px] pr-4 sticky top-[72px]">
    <a
      class="flex items-center justify-start w-full cursor-pointer hover:bg-theme-hover p-2 rounded-md"
    >
      <img
        class="rounded-full ml-1 min-w-[38px] max-h-[38px]"
        :src="userAvatar"
      />
      <div class="text-[15px] text-theme-text font-medium pl-3">
        {{ userName }}
      </div>
    </a>

    <div class="mt-2">
      <button
        v-for="item in menuItems"
        :key="item.label"
        class="flex items-center w-full hover:bg-theme-hover rounded-md p-3 transition"
      >
        <div
          class="flex items-center justify-center w-9 h-9 rounded-full"
          :style="{ backgroundColor: item.bgColor }"
        >
          <component :is="item.icon" :size="22" fillColor="white" />
        </div>
        <span class="text-[15px] text-theme-text font-medium pl-3">
          {{ $t(item.label) }}
        </span>
      </button>
    </div>
  </div>
</template>
