<template>
  <div class="settings-view max-w-xl mx-auto bg-white dark:bg-theme-bg-secondary text-theme-text rounded-xl p-4">
    <div class="w-full flex items-center pb-4">
      <button
        @click="handleBackClick"
        class="rounded-full p-2 -ml-2 hover:bg-gray-100 dark:hover:bg-theme-hover transition duration-150 mr-2"
        :aria-label="$t('common.back')"
      >
        <ArrowLeftIcon class="text-2xl text-theme-text" />
      </button>
      <span class="text-theme-text font-bold text-xl">Ustawienia i prywatność</span>
    </div>

    <section class="flex flex-col gap-2">

      <a href="#" class="setting-link group flex items-center py-2 px-2 hover:bg-gray-100 dark:hover:bg-theme-hover rounded-lg transition duration-150">
        <span class="h-10 w-10 bg-gray-200 dark:bg-gray-700 rounded-full flex items-center justify-center mr-4 shrink-0 text-theme-text">
          <CogIcon :size="24" />
        </span>
        <div class="grow text-lg font-medium text-theme-text">
          Ustawienia
        </div>
      </a>

      <button @click="handleLanguageClick" class="setting-link group flex items-center py-2 px-2 hover:bg-gray-100 dark:hover:bg-theme-hover rounded-lg transition duration-150 w-full text-left">
        <span class="h-10 w-10 bg-gray-200 dark:bg-gray-700 rounded-full flex items-center justify-center mr-4 shrink-0 text-theme-text">
          <WebIcon :size="24" />
        </span>
        <div class="grow text-lg font-medium text-theme-text">
          Język
        </div>
        <ChevronRightIcon class="text-gray-400" :size="24" />
      </button>

      <a href="#" class="setting-link group flex items-center py-2 px-2 hover:bg-gray-100 dark:hover:bg-theme-hover rounded-lg transition duration-150">
        <span class="h-10 w-10 bg-gray-200 dark:bg-gray-700 rounded-full flex items-center justify-center mr-4 shrink-0 text-theme-text">
          <LockCheckIcon :size="24" /> </span>
        <div class="grow text-lg font-medium text-theme-text">
          Kontrola prywatności
        </div>
      </a>

      <a href="#" class="setting-link group flex items-center py-2 px-2 hover:bg-gray-100 dark:hover:bg-theme-hover rounded-lg transition duration-150">
        <span class="h-10 w-10 bg-gray-200 dark:bg-gray-700 rounded-full flex items-center justify-center mr-4 shrink-0 text-theme-text">
          <LockIcon :size="24" />
        </span>
        <div class="grow text-lg font-medium text-theme-text">
          Centrum ochrony prywatności
        </div>
      </a>

      <a href="#" class="setting-link group flex items-center py-2 px-2 hover:bg-gray-100 dark:hover:bg-theme-hover rounded-lg transition duration-150">
        <span class="h-10 w-10 bg-gray-200 dark:bg-gray-700 rounded-full flex items-center justify-center mr-4 shrink-0 text-theme-text">
          <FormatListBulletedIcon :size="24" />
        </span>
        <div class="grow text-lg font-medium text-theme-text">
          Dziennik aktywności
        </div>
      </a>

      <a href="#" class="setting-link group flex items-center py-2 px-2 hover:bg-gray-100 dark:hover:bg-theme-hover rounded-lg transition duration-150">
        <span class="h-10 w-10 bg-gray-200 dark:bg-gray-700 rounded-full flex items-center justify-center mr-4 shrink-0 text-theme-text">
          <CardTextIcon :size="24" />
        </span>
        <div class="grow text-lg font-medium text-theme-text">
          Preferencje dotyczące Aktualności
        </div>
      </a>

    </section>
  </div>
</template>

<script setup lang="ts">
import { useI18n } from 'vue-i18n';
// Ikony
import ArrowLeftIcon from 'vue-material-design-icons/ArrowLeft.vue';
import CogIcon from 'vue-material-design-icons/Cog.vue';
import WebIcon from 'vue-material-design-icons/Web.vue'; // Dla ikony globusa/języka
import LockCheckIcon from 'vue-material-design-icons/LockCheck.vue'; // Dla Kontroli prywatności
import LockIcon from 'vue-material-design-icons/Lock.vue';
import FormatListBulletedIcon from 'vue-material-design-icons/FormatListBulleted.vue'; // Dla Dziennika
import CardTextIcon from 'vue-material-design-icons/CardText.vue'; // Dla Aktualności (feed)
import ChevronRightIcon from 'vue-material-design-icons/ChevronRight.vue';

useI18n();

const emit = defineEmits(['back', 'navigate']);

const handleBackClick = () => {
  emit('back');
};

const handleLanguageClick = () => {
  emit('navigate', 'language');
};
</script>

<style scoped>
.setting-link {
  text-decoration: none;
  color: inherit;
}
</style>
