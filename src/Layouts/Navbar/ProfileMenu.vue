<template>
  <div class="w-full md:w-[360px] bg-theme-bg-secondary p-2 rounded-xl shadow-2xl overflow-hidden">
    <div
      class="relative transition-[height] duration-300 ease-in-out"
      ref="wrapperRef"
      style="min-height: 50px"
    >
      <Transition :name="transitionName" @enter="onEnter" @after-enter="onAfterEnter">
        <component
          :is="currentViewComponent"
          :key="currentView"
          class="view-container bg-theme-bg-secondary p-0"
          @navigate="handleNavigation"
          @back="handleNavigationBack"
          @view-all-profiles="handleViewAllProfiles"
          @switch-account="handleSwitchAccount"
          @selected="handleProfileSelected"
        />
      </Transition>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, type Component } from 'vue'
import { useSlideTransition } from '@/composables/ui/useSlideTransition'
import { useAuthStore } from '@/stores/auth'

import MainMenu from '@/layouts/Navbar/menu/MainMenu.vue'
import SubMenuDisplay from '@/layouts/Navbar/menu/SubMenu.vue'
import LanguageSelector from '@/layouts/Navbar/menu/LanguageSelector.vue'
import SettingsMenu from '@/layouts/Navbar/menu/SettingsMenu.vue'
import ProfilesSelector from '@/layouts/Navbar/menu/ProfilesSelector.vue'

const authStore = useAuthStore()

const viewComponents: Record<string, Component> = {
  main: MainMenu,
  display: SubMenuDisplay,
  language: LanguageSelector,
  settings: SettingsMenu,
  profiles: ProfilesSelector,
}

const { wrapperRef, currentView, transitionName, navigateTo, navigateBack, onEnter, onAfterEnter } =
  useSlideTransition('main')

const currentViewComponent = computed(() => {
  return viewComponents[currentView.value] || MainMenu
})

const handleNavigation = (viewName: string) => navigateTo(viewName)
const handleNavigationBack = () => navigateBack()

const handleViewAllProfiles = () => {
  navigateTo('profiles')
}

const handleSwitchAccount = () => {
  authStore.switchAccount()
}

const handleProfileSelected = () => {
  navigateBack()
}
</script>

<style scoped>
/* Styl podstawowy kontenera widoku.
  Musi mieć top:0 left:0, żeby przy position:absolute nie uciekał.
*/
.view-container {
  top: 0;
  left: 0;
  /* Ważne: brak position: absolute w stanie spoczynku,
     dzięki temu browser poprawnie liczy offsetHeight */
}

/* --- ANIMACJE (Facebook Style) --- */

.slide-left-enter-active,
.slide-left-leave-active,
.slide-right-enter-active,
.slide-right-leave-active {
  transition:
    transform 0.3s cubic-bezier(0.25, 1, 0.5, 1),
    opacity 0.3s ease;
  /* KLUCZOWE: Position absolute TYLKO podczas ruchu,
     aby elementy mogły być na sobie (jeden nad drugim) */
  position: absolute;
  width: 100%;
}

/* WCHODZENIE W GŁĄB (Next) */
.slide-left-enter-from {
  transform: translateX(100%);
  opacity: 0;
}
.slide-left-leave-to {
  transform: translateX(-100%);
  opacity: 0; /* Opcjonalne: lekkie zanikanie starego */
}

/* POWRÓT (Back) */
.slide-right-enter-from {
  transform: translateX(-100%);
  opacity: 0;
}
.slide-right-leave-to {
  transform: translateX(100%);
  opacity: 0;
}
</style>
