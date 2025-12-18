<template>
  <div class="w-[360px] bg-theme-bg-secondary p-4 rounded-lg relative overflow-hidden">
    <div class="profile-menu-wrapper" ref="wrapperRef">
      <Transition :name="transitionName" mode="out-in" @before-enter="updateHeight()">
        <component
          :is="currentViewComponent"
          :key="currentView"
          class="view-container bg-theme-bg-secondary p-0"
          :data-view="currentView"
          @navigate="handleNavigation"
          @back="handleNavigationBack"
        />
      </Transition>
    </div>
  </div>
</template>

<script setup lang="ts">
import { type Component, computed } from 'vue';
import { useI18n } from 'vue-i18n';

import MainMenu from './ProfileMenu/MainMenu.vue';
import SubMenuDisplay from './ProfileMenu/SubMenu.vue';
import LanguageSelector from './ProfileMenu/LanguageSelector.vue';

// --- Import Animacji ---
import '@/assets/animations/slideTransition.css';

// --- Import Composables ---
import { useSlideTransition } from '@/composables/useSlideTransition';

useI18n();

// --- Use Composables ---
const { wrapperRef, currentView, previousView, updateHeight, transitionName } = useSlideTransition();

const viewComponents: Record<string, Component> = {
  main: MainMenu,
  display: SubMenuDisplay,
  language: LanguageSelector,
};

const currentViewComponent = computed(() => {
  return viewComponents[currentView.value] || MainMenu;
});

const handleNavigation = (viewName: string) => {
  if (viewComponents[viewName]) {
    previousView.value = currentView.value;
    currentView.value = viewName;
  } else {
    console.log(`Kliknięto akcję: ${viewName}`);
    if (viewName === 'logout') {
        alert('Wylogowanie...');
    }
  }
};

const handleNavigationBack = () => {
    previousView.value = currentView.value;
    currentView.value = 'main';
};
</script>

<style scoped>
.profile-menu-wrapper {
  position: relative;
  width: 100%;
  overflow: hidden;
  transition: height 0.08s ease;
  min-height: 48px;
}

.view-container {
  position: relative;
  width: 100%;
}
</style>
