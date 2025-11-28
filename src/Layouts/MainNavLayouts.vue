<script setup lang="ts">
import { ref, useTemplateRef } from 'vue'

import Magnify from 'vue-material-design-icons/Magnify.vue'
import Home from 'vue-material-design-icons/Home.vue'

import TelevisionPlay from 'vue-material-design-icons/TelevisionPlay.vue'
import StorefrontOutline from 'vue-material-design-icons/StorefrontOutline.vue'
import AccountGroup from 'vue-material-design-icons/AccountGroup.vue'
import ControllerClassicOutline from 'vue-material-design-icons/ControllerClassicOutline.vue'
import DotsGrid from 'vue-material-design-icons/DotsGrid.vue'
import FacebookMessenger from 'vue-material-design-icons/FacebookMessenger.vue'
import Bell from 'vue-material-design-icons/Bell.vue'

import { onClickOutside } from '@vueuse/core'
// Zakładamy, że te komponenty są dostępne:
import ProfileMenu from '@/components/ProfileMenu.vue'
import NotificationMenu from '@/components/NotificationMenu.vue'
import MessageMenu from '@/components/MessageMenu.vue'
import ThemeSwitcher from '@/components/ThemeSwitcher.vue'

// Definicja typu dla aktywnego menu
type ActiveMenuType = 'profile' | 'notifications' | 'message' | null;

const target = useTemplateRef<HTMLElement>('target')

// Zmieniamy flagę boolean na zmienną przechowującą stan (nazwę aktywnego menu lub null)
const activeMenu = ref<ActiveMenuType>(null)

// Funkcja przełączająca menu
const toggleMenu = (menuName: ActiveMenuType) => {
  // Jeśli kliknięto to samo menu, zamknij je (ustaw na null).
  if (activeMenu.value === menuName) {
    activeMenu.value = null
  } else {
    // W przeciwnym razie, ustaw nowe menu jako aktywne.
    activeMenu.value = menuName
  }
}

// onClickOutside zamyka aktywne menu
onClickOutside(target, () => {
  activeMenu.value = null
})
</script>

<template>
  <div
    id="MainNav"
    class="fixed z-1 w-full flex items-center justify-between top-0 h-14  bg-theme-bg-secondary shadow-md"
  >
    <div id="NavLeft" class="flex items-center justify-start w-[260px]">
      <a class="pl-3 min-w-[55px]">
        <img class="w-10" src="../assets/images/FacebookLogoCircle.png" />
      </a>
      <div class="flex items-center justify-center bg-[#EFF2F5] dark:bg-gray-800 p-1 rounded-full h-10 ml-2">
        <Magnify class="p-1" :size="22" fillColor="#64676B" />
        <input
            class="lg:block hidden border-none p-0 bg-[#EFF2F5] bg-theme-bg text-theme-text placeholder-[#64676B] ring-0 focus:ring-0"
          placeholder="Search Facebook"
          type="text"
        />
      </div>
    </div>

    <div
      id="NavCenter"
      class="hidden lg:flex items-center justify-center w-8/12 max-w-[700px]"
    >
    <div class="border-b-4 border-b-blue-400 flex items-center justify-center h-12 p-1  w-full mx-1 cursor-pointer dark:bg-gray-800 dark:bg-opacity-50 rounded-lg">
      <button
        class=" rounded-lg "
      >
        <Home :size="27" fillColor="#1A73E3" class="mx-auto" />

      </button>
</div>
      <button
        class="flex items-center justify-center h-12 p-1 hover:bg-theme-hover w-full rounded-lg mx-1 cursor-pointer"
      >
        <TelevisionPlay class="mx-auto" :size="27" fillColor="#64676B" />
      </button>
      <button
          class="flex items-center justify-center h-12 p-1 hover:bg-theme-hover w-full rounded-lg mx-1 cursor-pointer"
      >
        <StorefrontOutline class="mx-auto" :size="27" fillColor="#64676B" />
      </button>
      <button
        class="flex items-center justify-center h-12 p-1 hover:bg-[#F2F2F2] dark:hover:bg-gray-700 w-full rounded-lg mx-1 cursor-pointer"
      >
        <span class="rounded-full border-2 dark:border-gray-400 border-[#64676B] p-1">
          <AccountGroup class="mx-auto" :size="22" fillColor="#64676B" />
        </span>
      </button>
      <button
        class="flex items-center justify-center h-12 p-1 hover:bg-[#F2F2F2] dark:hover:bg-gray-700 w-full rounded-lg mx-1 cursor-pointer"
      >
        <ControllerClassicOutline class="mx-auto" :size="32" fillColor="#64676B" />
      </button>
    </div>

    <div class="flex items-center justify-end w-[260px] mr-4 relative">
      <ThemeSwitcher />
      <button class="rounded-full bg-[#E3E6EA] dark:bg-gray-700 p-2 hover:bg-gray-300 dark:hover:bg-gray-600 mx-1 cursor-pointer">
        <DotsGrid :size="23" fillColor="#050505" />
      </button>
      <button @click="toggleMenu('message')" class="rounded-full bg-[#E3E6EA] dark:bg-gray-700 p-2 hover:bg-gray-300 dark:hover:bg-gray-600 mx-1 cursor-pointer">
        <FacebookMessenger :size="23" fillColor="#050505" />
      </button>

      <button @click="toggleMenu('notifications')" class="rounded-full bg-[#E3E6EA] dark:bg-gray-700 p-2 hover:bg-gray-300 dark:hover:bg-gray-600 mx-1 cursor-pointer">
        <Bell :size="23" fillColor="#050505" />
      </button>

      <div class="flex items-center justify-center relative">
        <button @click="toggleMenu('profile')">
          <img
            class="rounded-full ml-1 min-w-10 max-h-10 cursor-pointer"
            src="https://scontent-waw2-1.xx.fbcdn.net/v/t39.30808-1/295055057_582985040112298_215415809791370036_n.jpg?stp=cp0_dst-jpg_s40x40_tt6&_nc_cat=104&ccb=1-7&_nc_sid=e99d92&_nc_ohc=-o822DQWa_kQ7kNvwEBBrQN&_nc_oc=Adk7CLzzn6vvAFCclTDzM32DkA0bnwHJCU8V-LZ-6Rgt046578D_zYBPKIpVqrH_jqSITUodiSom9HftYGfou-YR&_nc_zt=24&_nc_ht=scontent-waw2-1.xx&_nc_gid=hWinwIkg4qpusDkFaBv_tg&oh=00_AfhegpWXzJqTqkSqYk4lk-AflwjwvP0sVVYiWvBV-lyexg&oe=6917A7AC"
          />
        </button>
      </div>

      <div
        v-if="activeMenu"
        ref="target"
        style="box-shadow: rgba(0, 0, 0, 0.2) 0px 12px 28px 0px, rgba(0, 0, 0, 0.1) 0px 2px 4px 0px;"
        class="absolute  top-10 right-0 w-auto rounded-3xl mt-1"
      >
        <ProfileMenu v-if="activeMenu === 'profile'" />
        <MessageMenu v-if="activeMenu === 'message'" />
        <NotificationMenu v-if="activeMenu === 'notifications'" />
      </div>
    </div>
  </div>


</template>
