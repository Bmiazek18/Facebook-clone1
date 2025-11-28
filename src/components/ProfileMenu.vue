<template>
  <div class="w-[360px] bg-theme-bg-secondary p-4 rounded-lg relative overflow-hidden">
    <!-- wrapper ma ref i animowaną wysokość -->
    <div class="transition-wrapper" ref="wrapperRef">
      <Transition :name="transitionName" mode="out-in">
        <!-- Każdy widok ma data-view z kluczem; dzięki temu możemy go wybrać z DOM -->
        <div
          :key="currentView"
          class="view-container bg-theme-bg-secondary p-0"
          :data-view="currentView"
        >
          <div v-if="currentView === 'main'" class="shadow-xl p-3 rounded-lg bg-theme-bg-secondary">
            <RouterLink
              to="/profile"
              class="flex pt-2 items-center space-x-3 pb-3 border-b border-theme-border hover:bg-gray-100 cursor-pointer"
            >
              <img
                src="https://scontent-waw2-1.xx.fbcdn.net/v/t39.30808-1/295055057_582985040112298_215415809791370036_n.jpg?stp=cp0_dst-jpg_s40x40_tt6&_nc_cat=104&ccb=1-7&_nc_sid=e99d92&_nc_ohc=-o822DQWa_kQ7kNvwEBBrQN&_nc_oc=Adk7CLzzn6vvAFCclTDzM32DkA0bnwHJCU8V-LZ-6Rgt046578D_zYBPKIpVqrH_jqSITUodiSom9HftYGfou-YR&_nc_zt=24&_nc_ht=scontent-waw2-1.xx&_nc_gid=hWinwIkg4qpusDkFaBv_tg&oh=00_AfhegpWXzJqTqkSqYk4lk-AflwjwvP0sVVYiWvBV-lyexg&oe=6917A7AC"
                class="h-10 w-10 bg-gray-300 rounded-full shrink-0"
                alt="avatar"
              />
              <span class="font-semibold text-theme-text font-[15px] truncate">
                Bartosz Miazek
              </span>
            </RouterLink>

            <button
              class="w-full mt-3 py-2 px-3 flex items-center justify-center bg-gray-100 hover:bg-theme-hover rounded-lg text-sm font-medium text-gray-800 text-theme-text transition duration-150"
            >
              <AccountSearchIcon class="mr-2 h-5 w-5 text-gray-600 text-theme-text-secondary" />
              {{ $t('profile_menu.viewAllProfiles') }}
            </button>
          </div>

          <div class="my-3 border-theme-border"></div>

          <ul role="menu" class="space-y-1">
            <li v-for="item in currentMenu" :key="item.name">
              <button
                @click="handleMenuClick(item)"
                class="w-full flex items-center p-2 rounded-lg hover:bg-theme-hover transition duration-150"
              >
                <span class="h-9 w-9 bg-theme-bg bg-theme-hover rounded-full flex items-center justify-center mr-3 shrink-0">
                  <component :is="item.iconComponent" class="text-xl text-theme-text" />
                </span>

                <span class="text-theme-text font-[15px] ">{{ $t(item.labelKey) }}</span>

                <span class="ml-auto flex items-center">
                  <ChevronRightIcon v-if="item.arrowIcon" size="30" />
                  <ChevronLeftIcon v-if="item.backAction" size="30" />
                </span>
              </button>
            </li>
          </ul>
        </div>
      </Transition>
    </div>



    <div v-if="currentView === 'main'" class="px-2 text-xs font-bold text-theme-text-secondary leading-tight">
      <a href="#" class="hover:underline">{{ $t('common.privacy') }}</a> ·
      <a href="#" class="hover:underline">{{ $t('common.terms') }}</a> ·
      <a href="#" class="hover:underline">{{ $t('common.advertising') }}</a> ·
      <a href="#" class="hover:underline">{{ $t('common.adOptions') }}</a>
      <p class="mt-0.5">
        · <a href="#" class="hover:underline">{{ $t('common.cookies') }}</a>
        · <a href="#" class="hover:underline">{{ $t('common.more') }}</a>
      </p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, type Ref, type DefineComponent, computed, nextTick, onMounted, watch, onBeforeUnmount } from 'vue';
import { useI18n } from 'vue-i18n';

import ChevronRightIcon from 'vue-material-design-icons/ChevronRight.vue';
import ChevronLeftIcon from 'vue-material-design-icons/ChevronLeft.vue';
import CogIcon from 'vue-material-design-icons/Cog.vue';
import HelpCircleOutlineIcon from 'vue-material-design-icons/HelpCircleOutline.vue';
import WeatherNightIcon from 'vue-material-design-icons/WeatherNight.vue';
import CommentProcessingOutlineIcon from 'vue-material-design-icons/CommentProcessingOutline.vue';
import LogoutIcon from 'vue-material-design-icons/Logout.vue';
import AccountSearchIcon from 'vue-material-design-icons/AccountSearch.vue';
import SunIcon from 'vue-material-design-icons/WhiteBalanceSunny.vue';

useI18n();

interface MenuItem {
  name: string;
  labelKey: string;
  iconComponent: DefineComponent;
  arrowIcon?: boolean;
  subMenu?: MenuItem[];
  backAction?: boolean;
}

const currentView: Ref<string> = ref('main');
const previousView: Ref<string | null> = ref(null);

// refs
const wrapperRef = ref<HTMLElement | null>(null);

// Helper: znajdź aktualny DOM widoku wewnątrz wrappera
const getActiveViewElement = (): HTMLElement | null => {
  if (!wrapperRef.value) return null;
  // selektor matches data-view
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
  // initial measurement
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

// menu data & logic (unchanged)
const menuItems: Ref<MenuItem[]> = ref([
  {
    name: 'settings',
    labelKey: 'profile_menu.settings',
    iconComponent: CogIcon,
    arrowIcon: true,
  },
  {
    name: 'help',
    labelKey: 'profile_menu.help',
    iconComponent: HelpCircleOutlineIcon,
    arrowIcon: true,
  },
  {
    name: 'display',
    labelKey: 'profile_menu.display',
    iconComponent: WeatherNightIcon,
    arrowIcon: true,
    subMenu: [
      {
        name: 'back_to_main',
        labelKey: 'common.back',
        iconComponent: ChevronLeftIcon,
        backAction: true,
      },
      {
        name: 'mode_dark',
        labelKey: 'profile_menu.modeDark',
        iconComponent: WeatherNightIcon,
      },
      {
        name: 'mode_light',
        labelKey: 'profile_menu.modeLight',
        iconComponent: SunIcon,
      },
    ],
  },
  {
    name: 'feedback',
    labelKey: 'profile_menu.feedback',
    iconComponent: CommentProcessingOutlineIcon,
  },
  {
    name: 'logout',
    labelKey: 'profile_menu.logout',
    iconComponent: LogoutIcon,
  },
]);

const currentMenu = computed(() => {
  if (currentView.value === 'main') return menuItems.value;
  const parentItem = menuItems.value.find((item) => item.name === currentView.value);
  return parentItem?.subMenu ?? [];
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

const handleMenuClick = (item: MenuItem): void => {
  if (item.subMenu) {
    previousView.value = currentView.value;
    currentView.value = item.name;
  } else if (item.backAction) {
    previousView.value = currentView.value;
    currentView.value = 'main';
  } else {
    console.log(`Kliknięto: ${item.name}`);
    if (item.name === 'logout') {
      alert('Wylogowanie...');
    }
  }
};
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

/* Slide transitions: ZMIANA! Dodajemy position: absolute oraz top/left/width */
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

/* Slide in from right (wchodzimy w głąb) */
.slide-left-enter-from {
  transform: translateX(100%);
  opacity: 0;
}
.slide-left-enter-to {
  transform: translateX(0%);
  opacity: 1;
}
.slide-left-leave-from {
  transform: translateX(0%);
  opacity: 1;
}
.slide-left-leave-to {
  transform: translateX(-100%);
  opacity: 0;
}

/* Slide in from left (wracamy) */
.slide-right-enter-from {
  transform: translateX(-100%);
  opacity: 0;
}
.slide-right-enter-to {
  transform: translateX(0%);
  opacity: 1;
}
.slide-right-leave-from {
  transform: translateX(0%);
  opacity: 1;
}
.slide-right-leave-to {
  transform: translateX(100%);
  opacity: 0;
}
</style>
