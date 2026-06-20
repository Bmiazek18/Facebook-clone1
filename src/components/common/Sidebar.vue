<template>
  <div class="min-w-[360px] relative transform translate-x-0">
  <aside class="w-[360px] flex flex-col bg-theme-bg-secondary shadow-lg top-[56px] sticky h-[calc(100vh-56px)] z-20">
    <HoverScrollbar class="flex-1">
      <div class="p-3">
        <div class="flex justify-between items-center mb-3 px-2">
          <h1 class="text-2xl font-bold text-theme-text">{{ title }}</h1>
          <button v-if="showSettings" class="p-2 bg-theme-bg-tertiary hover:bg-theme-hover rounded-full transition-colors">
            <CogIcon :size="20" class="text-theme-text" />
          </button>
        </div>

        <div v-if="showSearch" class="relative mb-4 pr-2">
          <MagnifyIcon class="absolute left-3 top-2.5 text-theme-text-secondary" :size="22" />
          <input
            type="text"
            :placeholder="searchPlaceholder"
            class="w-full bg-[#F1F2F5] dark:bg-[#333334] rounded-full  py-2 pl-10 pr-4 placeholder-theme-text-secondary focus:outline-none text-[15px]"
          />
        </div>

<nav class="space-y-1 mb-4">
    <RouterLink
      v-for="item in items"
      :key="item.text"
      :to="item.route"
      class="group flex items-center space-x-3 px-2 py-2 rounded-lg hover:bg-theme-hover transition-colors"
      #default="{ isExactActive }"
    >
      <div
        class="p-1.5 shrink-0 rounded-full transition-colors"
        :class="[
          isExactActive ? 'bg-[#0866FF] text-white' : 'bg-theme-bg-tertiary text-theme-text'
        ]"
      >
        <component :is="item.icon" :size="20" />
      </div>

      <span
        class="text-[15px] transition-colors text-theme-text"

      >
        {{ item.text }}
      </span>

      <ChevronRightIcon
        v-if="item.hasArrow"
        class="ml-auto text-theme-text-secondary"
        :class="{ 'text-[#0866FF]': isExactActive }"
        :size="20"
      />
    </RouterLink>
  </nav>

        <component
          :is="createButton?.route ? 'router-link' : 'button'"
          v-if="createButton"
          :to="createButton.route"
          @click="createButton.action ? createButton.action() : null"
          class="mt-4 mb-2 w-full bg-theme-primary-subtle hover:bg-theme-primary-subtle-hover text-theme-primary font-semibold py-2 rounded-lg flex items-center justify-center gap-2 transition text-[15px]"
        >
          <component :is="createButton.icon" :size="20" />
          <span>{{ createButton.text }}</span>
        </component>

        <slot name="pre-list"></slot>

        <div class="border-t border-theme-border my-4 " v-if="$slots['list-header'] || $slots['list-items']"></div>

        <div class="flex justify-between items-center ">
          <slot name="list-header"></slot>
        </div>

        <div class="space-y-1">
          <slot name="list-items"></slot>
        </div>
      </div>
    </HoverScrollbar>
  </aside>
  </div>

</template>

<script setup lang="ts">
import { defineProps } from 'vue';
import CogIcon from 'vue-material-design-icons/Cog.vue';
import MagnifyIcon from 'vue-material-design-icons/Magnify.vue';
import HoverScrollbar from '@/components/common/HoverScrollbar.vue';
import ChevronRightIcon from 'vue-material-design-icons/ChevronRight.vue';

defineProps({
  title: {
    type: String,
    required: true,
  },
  searchPlaceholder: {
    type: String,
    default: 'Search',
  },
  showSettings: {
    type: Boolean,
    default: false,
  },
  showSearch: {
    type: Boolean,
    default: true,
  },
  items: {
    type: Array as () => { icon: any; text: string; route: string; active: boolean; hasArrow?: boolean }[],
    required: true,
    default: () => [],
  },
  createButton: {
    type: Object as () => { icon: any; text: string; route?: string; action?: () => void } | null,
    default: null,
  },
});
</script>
