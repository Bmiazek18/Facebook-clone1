<template>
  <div class="min-w-[360px] relative transform translate-x-0">
    <aside
      class="w-[360px] flex flex-col bg-theme-bg-secondary shadow-lg top-[56px] sticky h-[calc(100vh-56px)] z-20"
    >
      <HoverScrollbar class="flex-1">
        <div class="p-3">
          <div class="flex items-center justify-between mb-3 px-2">
            <div class="flex items-center gap-3">
              <NuxtLink
                v-if="backRoute"
                :to="backRoute"
                class="p-2 -ml-2 hover:bg-theme-hover rounded-full transition text-theme-text flex items-center justify-center cursor-pointer shrink-0"
              >
                <ArrowLeftIcon :size="20" />
              </NuxtLink>
              <h1 class="text-2xl font-bold text-theme-text">{{ title }}</h1>
            </div>

            <!-- Przycisk Ustawień z podświetleniem po otwarciu -->
            <VDropdown
              v-if="showSettings"
              placement="bottom-end"
              :distance="10"
              :triggers="['click']"
              :autoHide="true"
              @show="isSettingsOpen = true"
              @hide="isSettingsOpen = false"
            >
              <!-- Trigger / Ikona Cog -->
              <button
                class="p-2 rounded-full transition-colors cursor-pointer"
                :class="[
                  isSettingsOpen
                    ? 'bg-[#0866FF] text-white hover:bg-blue-600'
                    : 'bg-theme-bg-tertiary hover:bg-theme-hover text-theme-text'
                ]"
              >
                <CogIcon :size="20" />
              </button>

              <!-- Treść wstrzykiwana przez Slot -->
              <template #popper>
                <slot name="settings-dropdown">
                  <!-- Domyślna treść (opcjonalna), jeśli slot nie zostanie podany -->
                  <div class="p-4 text-sm text-theme-text">Brak dostępnych opcji settings.</div>
                </slot>
              </template>
            </VDropdown>
          </div>

          <div v-if="showSearch" class="relative mb-4 pr-2">
            <MagnifyIcon class="absolute left-3 top-2.5 text-theme-text-secondary" :size="22" />
            <input
              type="text"
              v-model="searchQuery"
              @input="emit('update:search', searchQuery)"
              :placeholder="searchPlaceholder"
              class="w-full bg-[#F1F2F5] dark:bg-[#333334] rounded-full py-2 pl-10 pr-4 placeholder-theme-text-secondary focus:outline-none text-[15px]"
            />
          </div>

          <nav class="space-y-1 mb-4">
            <NuxtLink
              v-for="item in items"
              :key="item.text"
              :to="item.route"
              class="group flex items-center space-x-3 px-2 py-2 rounded-lg hover:bg-theme-hover transition-colors"
              #default="linkProps"
            >
              <div
                v-if="item.avatar"
                class="w-8 h-8 shrink-0 rounded-full overflow-hidden border border-theme-border flex items-center justify-center bg-gray-100"
              >
                <img :src="item.avatar" alt="" class="w-full h-full object-cover" />
              </div>
              <div
                v-else
                class="p-1.5 shrink-0 rounded-full transition-colors"
                :class="[
                  linkProps?.isExactActive
                    ? 'bg-[#0866FF] text-white'
                    : 'bg-theme-bg-tertiary text-theme-text',
                ]"
              >
                <component :is="item.icon" :size="20" />
              </div>

              <span class="text-[15px] transition-colors text-theme-text">
                {{ item.text }}
              </span>

              <ChevronRightIcon
                v-if="item.hasArrow"
                class="ml-auto text-theme-text-secondary"
                :class="{ 'text-[#0866FF]': linkProps?.isExactActive }"
                :size="20"
              />
            </NuxtLink>
          </nav>

          <component
            :is="createButton?.route ? NuxtLink : 'button'"
            v-if="createButton"
            :to="createButton.route"
            @click="handleCreateButtonClick"
            class="mt-4 mb-2 w-full cursor-pointer bg-theme-primary-subtle hover:bg-theme-primary-subtle-hover text-theme-primary font-semibold py-2 rounded-lg flex items-center justify-center gap-2 transition text-[15px]"
          >
            <component :is="createButton.icon" :size="20" />
            <span>{{ createButton.text }}</span>
          </component>

          <slot name="pre-list"></slot>

          <div
            class="border-t border-theme-border my-4"
            v-if="$slots['list-header'] || $slots['list-items']"
          ></div>

          <div class="flex justify-between items-center">
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
import { defineProps, defineEmits, ref } from 'vue'
import { NuxtLink } from '#components'

import CogIcon from 'vue-material-design-icons/Cog.vue'
import MagnifyIcon from 'vue-material-design-icons/Magnify.vue'
import HoverScrollbar from '@/components/common/HoverScrollbar.vue'
import ChevronRightIcon from 'vue-material-design-icons/ChevronRight.vue'
import ArrowLeftIcon from 'vue-material-design-icons/ArrowLeft.vue'

// Stan otwarcia dropdownu do kontroli niebieskiego tła
const isSettingsOpen = ref(false)

const searchQuery = ref('')
const emit = defineEmits(['update:search'])

const props = defineProps({
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
    type: Array as () => {
      icon?: any
      text: string
      route: string
      active?: boolean
      hasArrow?: boolean
      avatar?: string
    }[],
    required: true,
    default: () => [],
  },
  createButton: {
    type: Object as () => { icon: any; text: string; route?: string; action?: () => void } | null,
    default: null,
  },
  backRoute: {
    type: String,
    default: '',
  },
})

const handleCreateButtonClick = () => {
  if (props.createButton && typeof props.createButton.action === 'function') {
    props.createButton.action()
  }
}
</script>
