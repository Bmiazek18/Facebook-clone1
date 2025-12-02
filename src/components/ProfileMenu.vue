<template>
  <div class="w-[360px] bg-theme-bg-secondary p-4 rounded-lg relative overflow-hidden">
    <div class="transition-wrapper" ref="wrapperRef">
      <Transition :name="transitionName" mode="out-in">
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
import { ref, type Ref, type DefineComponent, computed, nextTick, onMounted, watch, onBeforeUnmount } from 'vue';
import { useI18n } from 'vue-i18n';

import MainMenu from './ProfileMenu/MainMenu.vue';
import SubMenuDisplay from './ProfileMenu/SubMenu.vue';


useI18n();

const currentView: Ref<string> = ref('main');
const previousView: Ref<string | null> = ref(null);

const viewComponents: Record<string, DefineComponent> = {
  main: MainMenu,
  display: SubMenuDisplay,
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

const wrapperRef = ref<HTMLElement | null>(null);

const getActiveViewElement = (): HTMLElement | null => {
  if (!wrapperRef.value) return null;
  return wrapperRef.value.querySelector<HTMLElement>('[data-view]') ?? null;
};

const updateHeight = async () => {
  await nextTick();
  const active = getActiveViewElement();
  if (!wrapperRef.value) return;

  if (active) {
    const rect = active.getBoundingClientRect();
    wrapperRef.value.style.height = Math.ceil(rect.height) + 'px';
  } else {
    wrapperRef.value.style.height = '0px';
  }
};

let resizeObserver: ResizeObserver | null = null;

onMounted(() => {
  updateHeight();

  const active = getActiveViewElement();
  if (active && 'ResizeObserver' in window) {
    resizeObserver = new ResizeObserver(() => {
      updateHeight();
    });
    resizeObserver.observe(active);
  }

  window.addEventListener('resize', updateHeight);
});

onBeforeUnmount(() => {
  window.removeEventListener('resize', updateHeight);
  if (resizeObserver) {
    resizeObserver.disconnect();
    resizeObserver = null;
  }
});

watch(currentView,  () => {
   updateHeight();

  if (resizeObserver) {
    resizeObserver.disconnect();
    const active = getActiveViewElement();
    if (active) resizeObserver.observe(active);
  }
});


const transitionName = computed(() => {
  if (currentView.value !== 'main' && (previousView.value === 'main' || previousView.value === null)) {
    return 'slide-left';
  }
  if (currentView.value === 'main' && previousView.value !== 'main') {
    return 'slide-right';
  }
  return 'slide-left';
});

</script>

<style scoped>
.transition-wrapper {
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
.slide-left-enter-active,
.slide-left-leave-active,
.slide-right-enter-active,
.slide-right-leave-active {
  transition: transform 0.28s ease, opacity 0.28s ease;
  will-change: transform, opacity;
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
}
.slide-left-enter-from { transform: translateX(100%); opacity: 0; }
.slide-left-enter-to { transform: translateX(0%); opacity: 1; }
.slide-left-leave-from { transform: translateX(0%); opacity: 1; }
.slide-left-leave-to { transform: translateX(-100%); opacity: 0; }
.slide-right-enter-from { transform: translateX(-100%); opacity: 0; }
.slide-right-enter-to { transform: translateX(0%); opacity: 1; }
.slide-right-leave-from { transform: translateX(0%); opacity: 1; }
.slide-right-leave-to { transform: translateX(100%); opacity: 0; }
</style>
