<!-- components/BaseSidebar.vue -->
<template>
  <div class="min-w-[360px] relative transform translate-x-0">
    <aside
      class="w-[360px] flex flex-col bg-theme-bg-secondary shadow-lg top-[56px] sticky h-[calc(100vh-56px)] z-20"
    >
      <HoverScrollbar class="flex-1">
        <div class="p-3">
          <!-- Subtitle (np. "Marketplace", "Wydarzenia") -->
          <div v-if="subtitle" class="px-2 text-[13px] text-theme-text-secondary font-semibold mb-1">
            {{ subtitle }}
          </div>

          <!-- Nagłówek i powrót -->
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

            <!-- Ustawienia -->
            <VDropdown
              v-if="showSettings"
              placement="bottom-end"
              :distance="10"
              :triggers="['click']"
              :autoHide="true"
              @show="isSettingsOpen = true"
              @hide="isSettingsOpen = false"
            >
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
              <template #popper>
                <slot name="settings-dropdown">
                  <div class="p-4 text-sm text-theme-text">{{ $t('common.brakDostepnychOpcjiSettings') }}</div>
                </slot>
              </template>
            </VDropdown>
          </div>

          <!-- Wyszukiwarka z panelem na całą szerokość -->
          <div v-if="showSearch" class="relative mb-4 z-40">
            <!-- Tło zakrywające, by dropdown wychodził zza wyszukiwarki -->
            <div class="relative pr-2 bg-theme-bg-secondary z-20">
              <MagnifyIcon class="absolute left-3 top-2.5 text-theme-text-secondary" :size="22" />

              <input
                type="text"
                v-model="searchQuery"
                @input="emit('update:search', searchQuery)"
                @focus="isSearchFocused = true"
                @blur="handleSearchBlur"
                :placeholder="searchPlaceholder"
                class="w-full bg-[#F1F2F5] dark:bg-[#333334] rounded-full py-2 pl-10 pr-4 placeholder-theme-text-secondary focus:outline-none text-[15px]"
              />
            </div>


              <div
                v-if="isSearchFocused"
                class="absolute left-[-12px] w-[360px] top-[100%] bg-theme-bg-secondary shadow-[10px_20px_20px_-5px_rgba(0,0,0,0.15)] dark:shadow-[0_15px_15px_-5px_rgba(0,0,0,0.4)] rounded-b-xl pt-5 pb-6 flex items-center justify-center z-10"
              >
                <slot name="search-dropdown">
                  <span class="text-[15px] text-gray-500 dark:text-gray-400">{{ $t('search.noRecent') }}</span>
                </slot>
              </div>

          </div>

          <!-- Menu nawigacyjne -->
          <nav v-if="items && items.length > 0" class="space-y-1 mb-4 pr-2">
            <template v-for="item in items" :key="item.text">
              <!-- Linki (NuxtLink) -->
             <!-- Linki (NuxtLink) -->
<NuxtLink
  v-if="item.route"
  :to="item.route"
  class="group flex items-center space-x-3 px-2 py-2 rounded-lg hover:bg-theme-hover transition-colors w-full text-left"
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
      item.active
        ? 'bg-[#0866FF] text-white'
        : 'bg-theme-bg-tertiary text-theme-text group-hover:bg-theme-hover-strong'
    ]"
  >
    <component :is="item.icon" :size="20" />
  </div>

  <div class="flex flex-col">
    <span
      class="text-[15px] transition-colors text-theme-text leading-tight"
      :class="{ 'font-semibold': item.active, 'font-medium': !item.active }"
    >
      {{ item.text }}
    </span>
    <!-- Sekcja dla dodatkowego tekstu (np. "21 nowych") -->
    <div v-if="item.secondaryText" class="flex items-center text-[13px] text-theme-text-secondary mt-0.5">
      <div v-if="item.showNotificationDot" class="w-2 h-2 rounded-full bg-[#0866FF] mr-1.5 shrink-0"></div>
      <span>{{ item.secondaryText }}</span>
    </div>
  </div>

  <ChevronRightIcon
    v-if="item.hasArrow"
    class="ml-auto text-theme-text-secondary"
    :class="{ 'text-[#0866FF]': item.active }"
    :size="20"
  />
</NuxtLink>

              <!-- Akcje (Button) -->
              <button
                v-else
                @click="item.action && item.action()"
                class="group flex items-center space-x-3 px-2 py-2 rounded-lg hover:bg-theme-hover transition-colors w-full text-left"
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
                    item.active
                      ? 'bg-[#0866FF] text-white'
                      : 'bg-theme-bg-tertiary text-theme-text group-hover:bg-theme-hover-strong'
                  ]"
                >
                  <component :is="item.icon" :size="20" />
                </div>

                <span
                  class="text-[15px] transition-colors text-theme-text"
                  :class="{ 'font-semibold': item.active, 'font-medium': !item.active }"
                >
                  {{ item.text }}
                </span>

                <ChevronRightIcon
                  v-if="item.hasArrow"
                  class="ml-auto text-theme-text-secondary"
                  :class="{ 'text-[#0866FF]': item.active }"
                  :size="20"
                />
              </button>
            </template>
          </nav>

          <!-- Slot na customowe przyciski akcji -->
          <div v-if="$slots.actions" class="space-y-2 mt-4 mb-4 pr-2">
            <slot name="actions"></slot>
          </div>

          <!-- Przycisk createButton -->
          <component
            :is="createButton?.route ? NuxtLink : 'button'"
            v-if="createButton && !$slots.actions"
            :to="createButton.route"
            @click="handleCreateButtonClick"
            class="mt-4 mb-2 w-[calc(100%-8px)] mx-auto cursor-pointer bg-theme-primary-subtle hover:bg-theme-primary-subtle-hover text-theme-primary font-semibold py-2 px-4 rounded-lg flex items-center justify-center gap-2 transition text-[15px]"
          >
            <component :is="createButton.icon" :size="20" />
            <span>{{ createButton.text }}</span>
          </component>

          <!-- Dodatkowe sloty na sekcje z zawartością -->
          <slot name="pre-list"></slot>

          <div
            class="border-t border-theme-border my-4 pr-2"
            v-if="$slots['list-header'] || $slots['list-items']"
          ></div>

          <div class="flex justify-between items-center pr-2 mb-2">
            <slot name="list-header"></slot>
          </div>

          <div class="space-y-1 pr-2">
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

const isSettingsOpen = ref(false)
const searchQuery = ref('')
const isSearchFocused = ref(false)

const emit = defineEmits(['update:search'])

const props = defineProps({
  title: {
    type: String,
    required: true,
  },
  subtitle: {
    type: String,
    default: '',
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
      route?: string
      action?: () => void
      active?: boolean
      hasArrow?: boolean
      avatar?: string
    }[],
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

const handleSearchBlur = () => {
  setTimeout(() => {
    isSearchFocused.value = false
  }, 150)
}

const handleCreateButtonClick = () => {
  if (props.createButton && typeof props.createButton.action === 'function') {
    props.createButton.action()
  }
}
</script>
