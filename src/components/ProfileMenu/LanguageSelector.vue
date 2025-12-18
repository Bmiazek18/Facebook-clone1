<template>
  <div class="language-selector max-w-xl mx-auto bg-white dark:bg-gray-900 text-gray-900 dark:text-white shadow-lg rounded-xl">
    <!-- Header with Back Button -->
    <div class="w-full flex leading-7 pb-2">
      <button
        @click="handleBackClick"
        class="rounded-lg transition duration-150 mb-3 -mx-2 px-2"
        aria-label="Powrót"
      >
        <span
          class="h-9 w-9 hover:bg-gray-100 dark:hover:bg-gray-800 border border-gray-300 dark:border-gray-600 rounded-full flex items-center justify-center mr-3 shrink-0"
        >
          <ArrowLeftIcon class="text-xl text-gray-700 dark:text-gray-300" />
        </span>
      </button>
      <span class="text-gray-900 w-full dark:text-white font-bold text-[24px]">
        {{ $t('profile_menu.language') }}
      </span>
    </div>

    <!-- Language Options -->
    <section class="setting-group mb-4">
      <div class="options-list space-y-2">
        <!-- Polish -->
        <label
          class="option-item flex items-center justify-between py-3 px-3 cursor-pointer hover:bg-gray-50 dark:hover:bg-gray-800 rounded-lg transition duration-100"
        >
          <div class="flex items-center">
            <span class="h-10 w-10 border border-gray-300 dark:border-gray-600 rounded-full flex items-center justify-center mr-3 shrink-0 text-lg">
              🇵🇱
            </span>
            <div>
              <span class="text-base font-medium block">Polski (PL)</span>
              <p class="text-sm text-gray-500 dark:text-gray-400">Polish</p>
            </div>
          </div>
          <input type="radio" v-model="selectedLanguage" @click="selectLanguage('pl')" value="pl" class="hidden" />
          <div
            :class="['w-5 h-5 rounded-full border-2 shrink-0', selectedLanguage === 'pl' ? 'border-blue-600 bg-blue-600' : 'border-gray-400 dark:border-gray-600']"
          >
            <div v-if="selectedLanguage === 'pl'" class="w-3 h-3 bg-white rounded-full mx-auto my-auto mt-0.5"></div>
          </div>
        </label>

        <!-- English -->
        <label
          class="option-item flex items-center justify-between py-3 px-3 cursor-pointer hover:bg-gray-50 dark:hover:bg-gray-800 rounded-lg transition duration-100"
        >
          <div class="flex items-center">
            <span class="h-10 w-10 border border-gray-300 dark:border-gray-600 rounded-full flex items-center justify-center mr-3 shrink-0 text-lg">
              🇬🇧
            </span>
            <div>
              <span class="text-base font-medium block">English (EN)</span>
              <p class="text-sm text-gray-500 dark:text-gray-400">Angielski</p>
            </div>
          </div>
          <input type="radio" v-model="selectedLanguage" @click="selectLanguage('en')" value="en" class="hidden" />
          <div
            :class="['w-5 h-5 rounded-full border-2 shrink-0', selectedLanguage === 'en' ? 'border-blue-600 bg-blue-600' : 'border-gray-400 dark:border-gray-600']"
          >
            <div v-if="selectedLanguage === 'en'" class="w-3 h-3 bg-white rounded-full mx-auto my-auto mt-0.5"></div>
          </div>
        </label>
      </div>
    </section>

    <!-- Info Message -->
    <div class="px-3 py-3 bg-blue-50 dark:bg-blue-900/20 rounded-lg border border-blue-200 dark:border-blue-800">
      <p class="text-sm text-blue-700 dark:text-blue-300">
        {{ languageChangeMessage }}
      </p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import { useI18n } from 'vue-i18n';
import ArrowLeftIcon from 'vue-material-design-icons/ArrowLeft.vue';

const { locale } = useI18n();

// Get current language
const selectedLanguage = ref<string>(locale.value);

// Computed message based on selected language
const languageChangeMessage = computed(() => {
  if (selectedLanguage.value === 'pl') {
    return 'Zmiana języka na polski. Strona zostanie odświeżona.';
  } else {
    return 'Language changed to English. The page will be refreshed.';
  }
});

const emit = defineEmits(['back']);

// Handle back button
const handleBackClick = (): void => {
  emit('back');
};

// Handle language selection
const selectLanguage = (lang: string): void => {
  locale.value = lang;
  selectedLanguage.value = lang;
  // Optional: Add a small delay before going back to see the message
  setTimeout(() => {
    handleBackClick();
  }, 300);
};
</script>

<style scoped>
.language-selector {
  animation: fadeIn 0.2s ease;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

.option-item {
  transition: all 0.2s ease;
}

.option-item:active {
  transform: scale(0.98);
}
</style>
