<script setup lang="ts">
import { ref } from 'vue'
import { useRoute } from 'vue-router'
import { onClickOutside } from '@vueuse/core'

// Ikony
import Magnify from 'vue-material-design-icons/Magnify.vue'
import Home from 'vue-material-design-icons/Home.vue'
import TelevisionPlay from 'vue-material-design-icons/TelevisionPlay.vue'
import StorefrontOutline from 'vue-material-design-icons/StorefrontOutline.vue'
import AccountGroup from 'vue-material-design-icons/AccountGroup.vue'
import ControllerClassicOutline from 'vue-material-design-icons/ControllerClassicOutline.vue'

import ArrowLeft from 'vue-material-design-icons/ArrowLeft.vue'


import ContactList from '@/components/ContactList.vue'

import NavbarRight from '@/components/NavbarRight.vue'

type ActiveMenuType = 'profile' | 'notifications' | 'message' | null;

const route = useRoute()

// Referencje
const activeMenu = ref<ActiveMenuType>(null)
const isSearchFocused = ref(false)
const navLeft = ref(null)
const menuTarget = ref(null)

// Zamykanie wyszukiwarki po kliknięciu poza nią
onClickOutside(navLeft, () => {
  isSearchFocused.value = false
})

// Zamykanie menu profilu/powiadomień
onClickOutside(menuTarget, () => {
  activeMenu.value = null
})
</script>

<template>
  <div
    id="MainNav"
    class="fixed z-50 w-full flex items-center justify-between top-0 h-14 bg-theme-bg-secondary shadow-md px-4"
  >
    <div
        id="NavLeft"
        ref="navLeft"
        class="flex items-center justify-start w-[260px] relative h-full"
    >
      <div
        v-if="isSearchFocused"
        class="absolute -top-2 -left-4 w-[360px] bg-white dark:bg-[#242526] rounded-b-xl shadow-[0_12px_28px_0_rgba(0,0,0,0.2),0_2px_4px_0_rgba(0,0,0,0.1)] z-10 pt-[65px] min-h-[400px] border-t-0"
      >
          <div class="flex justify-between items-center px-4 py-2 mb-1 mx-2">
              <span class="text-[17px] font-semibold text-[#050505] dark:text-gray-200">Ostatnie</span>
              <button class="text-[15px] text-blue-500 hover:bg-gray-100 dark:hover:bg-gray-700 px-2 py-1 rounded transition">
                Edytuj
              </button>
          </div>
          <ContactList />
      </div>

      <div class="z-20 flex items-center w-full">
        <Transition name="slide-fade" mode="out-in">
          <RouterLink v-if="!isSearchFocused" to="/" class="mr-2 min-w-10">
            <img class="w-10" src="../assets/images/FacebookLogoCircle.png" />
          </RouterLink>

          <div
            v-else
            class="mr-2 p-2 rounded-full hover:bg-gray-200 dark:hover:bg-gray-700 cursor-pointer text-[#64676B] dark:text-gray-200"
            @click="isSearchFocused = false"
          >
            <ArrowLeft :size="24" />
          </div>
        </Transition>

        <div class="flex relative w-full">
          <div
            class="flex items-center justify-center p-1 rounded-full h-10 w-full transition-all bg-[#EFF2F5] dark:bg-gray-800"
          >
            <Magnify
              class="p-1 ml-1"
              :size="22"
              fillColor="#64676B"
              v-if="!isSearchFocused"
            />
            <input
              class="lg:block hidden border-none p-0 bg-transparent text-theme-text placeholder-[#64676B] ring-0 focus:ring-0 w-full px-3"
              placeholder="Szukaj na Facebooku"
              type="text"
              @focus="isSearchFocused = true"
              @keyup.esc="isSearchFocused = false"
            />
          </div>
        </div>
      </div>
    </div>

    <div id="NavCenter" class="hidden lg:flex items-center justify-center w-8/12 max-w-[700px]">
      <RouterLink to="/" class="flex items-center justify-center h-12 w-full mx-1" :class="route.path === '/' ? 'border-b-[3px] border-b-blue-500' : 'hover:bg-theme-hover rounded-lg'">
        <Home :size="27" :fillColor="route.path === '/' ? '#1A73E3' : '#64676B'" />
      </RouterLink>

      <RouterLink to="/reel" class="flex items-center justify-center h-12 w-full mx-1" :class="route.path === '/reel' ? 'border-b-[3px] border-b-blue-500' : 'hover:bg-theme-hover rounded-lg'">
        <TelevisionPlay :size="27" :fillColor="route.path === '/reel' ? '#1A73E3' : '#64676B'" />
      </RouterLink>

      <RouterLink to="/marketplace" class="flex items-center justify-center h-12 w-full mx-1" :class="route.path === '/marketplace' ? 'border-b-[3px] border-b-blue-500' : 'hover:bg-theme-hover rounded-lg'">
        <StorefrontOutline :size="27" :fillColor="route.path === '/marketplace' ? '#1A73E3' : '#64676B'" />
      </RouterLink>

      <RouterLink to="/friends" class="flex items-center justify-center h-12 w-full mx-1" :class="route.path === '/friends' ? 'border-b-[3px] border-b-blue-500' : 'hover:bg-theme-hover rounded-lg'">
        <span class="rounded-full border-2 p-1" :class="route.path === '/friends' ? 'border-blue-500' : 'dark:border-gray-400 border-[#64676B]'">
          <AccountGroup :size="22" :fillColor="route.path === '/friends' ? '#1A73E3' : '#64676B'" />
        </span>
      </RouterLink>

      <RouterLink to="/gaming" class="flex items-center justify-center h-12 w-full mx-1" :class="route.path === '/gaming' ? 'border-b-[3px] border-b-blue-500' : 'hover:bg-theme-hover rounded-lg'">
        <ControllerClassicOutline :size="32" :fillColor="route.path === '/gaming' ? '#1A73E3' : '#64676B'" />
      </RouterLink>
    </div>

<NavbarRight/>
  </div>
</template>

<style scoped>
.slide-fade-enter-active { transition: all 0.2s ease-out; }
.slide-fade-leave-active { transition: all 0.2s cubic-bezier(1, 0.5, 0.8, 1); }
.slide-fade-enter-from, .slide-fade-leave-to { transform: translateX(-5px); opacity: 0; }
</style>
