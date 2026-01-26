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
import NotificationMenu from '@/Layouts/Navbar/NotificationMenu.vue'
import MessageMenu from './MessageMenu.vue'

import { useTheme } from '@/composables/useTheme'
import { useAuthStore } from '@/stores/auth'


type ActiveMenuType = 'profile' | 'notifications' | 'message' | 'main' | null;

const { isDark } = useTheme()
const route = useRoute()
const auth = useAuthStore()

const hideMessageIcon = computed(() => route.meta?.hideMessageIcon === true)


const activeMenu = ref<ActiveMenuType>(null)
const menuTarget = ref(null)

const toggleMenu = (menuName: ActiveMenuType) => {
  activeMenu.value = activeMenu.value === menuName ? null : menuName
}

onClickOutside(menuTarget, () => {
  activeMenu.value = null
})
</script>

<template>
   <div class="flex items-center justify-end w-[260px] relative">
      <button
        @click="toggleMenu('main')"
        v-tooltip.bottom.no-arrow="'Menu'"
        :class="activeMenu === 'main' ? 'bg-blue-100 dark:bg-blue-900' : 'bg-[#E3E6EA] dark:bg-[#3b3d3f]'"
        class="rounded-full p-2 hover:bg-gray-300 dark:hover:bg-gray-600 mx-1"
      >
        <DotsGrid :size="23" :fillColor="activeMenu === 'main' ? '#1877F2' : (isDark ? '#fff' : '#050505')" />
      </button>

      <button
        v-if="!hideMessageIcon"
        @click="toggleMenu('message')"
        v-tooltip.bottom.no-arrow="'Wiadomości'"
        :class="activeMenu === 'message' ? 'bg-blue-100 dark:bg-blue-900' : 'bg-[#E3E6EA] dark:bg-[#3b3d3f]'"
        class="rounded-full p-2 hover:bg-gray-300 dark:hover:bg-gray-600 mx-1"
      >
        <FacebookMessenger :size="23" :fillColor="activeMenu === 'message' ? '#1877F2' : (isDark ? '#fff' : '#050505')" />
      </button>

      <button
        @click="toggleMenu('notifications')"
        v-tooltip.bottom.no-arrow="'Powiadomienia'"
        :class="activeMenu === 'notifications' ? 'bg-blue-100 dark:bg-blue-900' : 'bg-[#E3E6EA] dark:bg-[#3b3d3f]'"
        class="relative rounded-full p-2 hover:bg-gray-300 dark:hover:bg-gray-600 mx-1"
      >
        <Bell :size="23" :fillColor="activeMenu === 'notifications' ? '#1877F2' : (isDark ? '#fff' : '#050505')"/>
        <div class="absolute -top-1 -right-1 bg-red-500 text-white text-[11px] font-bold w-[18px] h-[18px] rounded-full flex items-center justify-center border-2 border-white dark:border-[#242526]">1</div>
      </button>

      <div class="flex items-center relative">
        <button @click="toggleMenu('profile')" v-tooltip.bottom.no-arrow="'Konto'" class="relative">
          <img
            :class="activeMenu === 'profile' ? 'ring-2 ring-blue-500' : ''"
            class="rounded-full ml-1 w-10 h-10 object-cover cursor-pointer"
            :src="auth.currentUser?.avatar"
          />
          <div
            :class="activeMenu === 'profile' ? 'bg-blue-100 dark:bg-blue-900' : 'bg-[#E3E6EA] dark:bg-[#3b3d3f]'"
            class="absolute bottom-0 right-0 rounded-full p-px border-2 border-white dark:border-[#242526]"
          >
            <ChevronDown :size="12" :fillColor="activeMenu === 'profile' ? '#1877F2' : (isDark ? '#fff' : '#050505')" />
          </div>
        </button>
      </div>

      <div v-if="activeMenu" ref="menuTarget" class="fixed lg:absolute top-14 lg:top-12 left-[2vw] lg:left-auto right-[4vw] lg:right-0 w-[94vw] lg:w-auto z-50">
        <MainMenu v-if="activeMenu === 'main'" />
        <ProfileMenu v-if="activeMenu === 'profile'" />
        <MessageMenu v-if="activeMenu === 'message'" />
        <NotificationMenu v-if="activeMenu === 'notifications'" />
      </div>
    </div>
</template>
