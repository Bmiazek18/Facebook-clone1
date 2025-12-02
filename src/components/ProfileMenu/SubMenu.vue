<template>
  <div class="shadow-xl p-3 rounded-lg bg-theme-bg-secondary">
    <button
        @click="handleBackClick"
        class="w-full flex items-center p-2 rounded-lg hover:bg-theme-hover transition duration-150 mb-2"
    >
        <span class="h-9 w-9 bg-theme-bg bg-theme-hover rounded-full flex items-center justify-center mr-3 shrink-0">
            <ChevronLeftIcon class="text-xl text-theme-text" />
        </span>
        <span class="text-theme-text font-[15px] ">{{ $t('common.back') }}</span>
    </button>

    <div class="my-3 border-theme-border"></div>

    <ul role="menu" class="space-y-1">
      <li v-for="item in subMenuItems" :key="item.name">
        <button
          @click="handleSubMenuClick(item)"
          class="w-full flex items-center p-2 rounded-lg hover:bg-theme-hover transition duration-150"
        >
          <span class="h-9 w-9 bg-theme-bg bg-theme-hover rounded-full flex items-center justify-center mr-3 shrink-0">
            <component :is="item.iconComponent" class="text-xl text-theme-text" />
          </span>

          <span class="text-theme-text font-[15px] ">{{ $t(item.labelKey) }}</span>
        </button>
      </li>
    </ul>
  </div>
</template>

<script setup lang="ts">
import { ref, type Ref, type DefineComponent } from 'vue';
import { useI18n } from 'vue-i18n';

import WeatherNightIcon from 'vue-material-design-icons/WeatherNight.vue';
import SunIcon from 'vue-material-design-icons/WhiteBalanceSunny.vue';
import ChevronLeftIcon from 'vue-material-design-icons/ChevronLeft.vue';

useI18n();

interface MenuItem {
  name: string;
  labelKey: string;
  iconComponent: DefineComponent;
}

const subMenuItems: Ref<MenuItem[]> = ref([
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
]);

const emit = defineEmits(['back']);

const handleBackClick = (): void => {
  emit('back');
};

const handleSubMenuClick = (item: MenuItem): void => {
  console.log(`Wybrano opcję wyświetlania: ${item.name}`);
};
</script>

<style scoped>
/* Empty */
</style>
