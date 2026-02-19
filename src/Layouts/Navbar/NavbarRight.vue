<script setup lang="ts">
import { ref, computed } from 'vue'
import { onClickOutside } from '@vueuse/core'
import { useRoute } from 'vue-router'

// Ikony
import DotsGrid from 'vue-material-design-icons/DotsGrid.vue'
import FacebookMessenger from 'vue-material-design-icons/FacebookMessenger.vue'
import Bell from 'vue-material-design-icons/Bell.vue'
import ChevronDown from 'vue-material-design-icons/ChevronDown.vue'

import MainMenu from './MainMenu.vue'
import ProfileMenu from './ProfileMenu.vue'
import NotificationMenu from '@/layouts/Navbar/NotificationMenu.vue'
import MessageMenu from './MessageMenu.vue'

import { useAuthStore } from '@/stores/auth'

type ActiveMenuType = 'profile' | 'notifications' | 'message' | 'main' | null;

const route = useRoute()
const auth = useAuthStore()
const hideMessageIcon = computed(() => route.meta?.hideMessageIcon === true)
const isChatActive = computed(() => route.name === 'chat' || route.name === 'chatMessages')

const activeMenu = ref<ActiveMenuType>(null)
const navTarget = ref(null)

const toggleMenu = (menuName: ActiveMenuType) => {
  activeMenu.value = activeMenu.value === menuName ? null : menuName
}

onClickOutside(navTarget, () => {
  activeMenu.value = null
})

// Stałe klasy dla przycisków, aby kod był czystszy
const btnClass = "rounded-full p-2 mx-1 transition-colors flex items-center justify-center bg-[#E3E6EA] dark:bg-[#3b3d3f] hover:bg-gray-300 dark:hover:bg-gray-600 text-[#050505] dark:text-white"
const activeBtnClass = "bg-[#E7F3FF] dark:bg-[#263951] text-[#1877F2] dark:text-[#1877F2]"
</script>

<template>
   <div ref="navTarget" class="flex items-center justify-end w-[260px] relative">

      <button
        @click="toggleMenu('main')"
        v-tooltip.bottom.no-arrow="{ content: 'Menu', triggers: ['hover'] }"
        :class="[btnClass, activeMenu === 'main' ? activeBtnClass : '']"
      >
        <DotsGrid :size="23" />
      </button>

      <button
        v-if="!hideMessageIcon"
        @click="toggleMenu('message')"
        v-tooltip.bottom.no-arrow="{ content: 'Wiadomości', triggers: ['hover'] }"
        :class="[btnClass, activeMenu === 'message' || isChatActive ? activeBtnClass : '']"
      >
        <FacebookMessenger :size="23" />
      </button>

      <button
        @click="toggleMenu('notifications')"
        v-tooltip.bottom.no-arrow="{ content: 'Powiadomienia', triggers: ['hover'] }"
        :class="[btnClass, activeMenu === 'notifications' ? activeBtnClass : '']"
        class="relative"
      >
        <Bell :size="23" />
        <div class="absolute -top-1 -right-1 bg-red-600 text-white text-[12px] font-semibold w-[18px] h-[18px] rounded-full flex items-center justify-center ">
          1
        </div>
      </button>

      <div class="flex items-center relative ml-1">
        <button @click="toggleMenu('profile')" v-tooltip.bottom.no-arrow="{ content: 'Konto', triggers: ['hover'] }" class="relative">
          <img
            :class="[
              'rounded-full w-10 h-10 object-cover cursor-pointer transition-all',
              activeMenu === 'profile' ? 'ring-2 ring-[#1877F2]' : ''
            ]"
            :src="auth.currentUser?.avatar"
          />
          <div
            :class="[
              'absolute bottom-0 -right-1 rounded-full p-px border-[2px] border-white dark:border-[#242526] flex items-center justify-center transition-colors',
              btnClass,
              activeMenu === 'profile' ? activeBtnClass : 'p-0 w-3 h-3'
            ]"
          >
            <ChevronDown :size="12" />
          </div>
        </button>
      </div>

      <div v-if="activeMenu" class="fixed sm:absolute top-14 sm:top-12 left-[2vw] sm:left-auto right-[4vw] sm:right-0 w-[94vw] sm:w-auto z-50">
        <MainMenu v-if="activeMenu === 'main'" />
        <ProfileMenu v-if="activeMenu === 'profile'" />
        <MessageMenu v-if="activeMenu === 'message'" />
        <NotificationMenu v-if="activeMenu === 'notifications'" />
      </div>
    </div>
</template>

