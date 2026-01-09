<template>
  <div class="language-settings max-w-xl mx-auto bg-white dark:bg-gray-900 text-gray-900 dark:text-white rounded-xl font-sans">

    <div class="w-full flex items-center pb-4 border-b border-gray-200 dark:border-gray-700 mb-4 px-4 pt-4">
      <button
        @click="handleBackClick"
        class="rounded-full p-2 -ml-2 hover:bg-gray-100 dark:hover:bg-gray-800 transition duration-150 mr-2"
        aria-label="Powrót"
      >
        <ArrowLeftIcon class="text-2xl text-gray-700 dark:text-gray-300" />
      </button>
      <h1 class="text-xl font-bold">{{ $t('language_settings.title') }}</h1>
    </div>

    <div class="px-4 pb-6 space-y-6">

      <section>
        <h2 class="text-lg font-bold mb-2">{{ $t('language_settings.facebook_language') }}</h2>
        <div class="py-2">
          <p class="text-[15px] text-gray-600 dark:text-gray-300 leading-snug mb-4">
            {{ $t('language_settings.language_description') }}
          </p>

          <div class="flex items-center justify-between">
            <span class="text-[15px] font-bold text-gray-900 dark:text-white">{{ currentLanguageName }}</span>

            <!-- Switch Toggle -->
            <div class="flex items-center gap-3 bg-gray-200 dark:bg-gray-700 rounded-full p-1">
              <button
                @click="changeLanguage('pl')"
                :class="[
                  'px-4 py-2 rounded-full transition-all duration-200 text-lg font-semibold',
                  currentLocale === 'pl'
                    ? 'bg-blue-600 text-white shadow-md'
                    : 'text-gray-700 dark:text-gray-300 hover:text-gray-900 dark:hover:text-white'
                ]"
              >
                🇵🇱
              </button>
              <button
                @click="changeLanguage('en')"
                :class="[
                  'px-4 py-2 rounded-full transition-all duration-200 text-lg font-semibold',
                  currentLocale === 'en'
                    ? 'bg-blue-600 text-white shadow-md'
                    : 'text-gray-700 dark:text-gray-300 hover:text-gray-900 dark:hover:text-white'
                ]"
              >
                🇬🇧
              </button>
            </div>
          </div>
        </div>
      </section>

      <hr class="border-gray-200 dark:border-gray-700" />



      <section>
        <h2 class="text-lg font-bold mb-2">Posty od znajomych i stron</h2>
        <div class="py-2">
          <p class="text-[15px] text-gray-600 dark:text-gray-300 leading-snug mb-4">
            Język, na który mają być tłumaczone posty
          </p>

          <div class="flex items-center justify-between">
            <span class="text-[15px] font-bold text-gray-900 dark:text-white">{{ currentLanguageName }}</span>

            <!-- Switch Toggle -->
            <div class="flex items-center gap-3 bg-gray-200 dark:bg-gray-700 rounded-full p-1">
              <button
                @click="changeLanguage('pl')"
                :class="[
                  'px-4 py-2 rounded-full transition-all duration-200 text-lg font-semibold',
                  currentLocale === 'pl'
                    ? 'bg-blue-600 text-white shadow-md'
                    : 'text-gray-700 dark:text-gray-300 hover:text-gray-900 dark:hover:text-white'
                ]"
              >
                🇵🇱
              </button>
              <button
                @click="changeLanguage('en')"
                :class="[
                  'px-4 py-2 rounded-full transition-all duration-200 text-lg font-semibold',
                  currentLocale === 'en'
                    ? 'bg-blue-600 text-white shadow-md'
                    : 'text-gray-700 dark:text-gray-300 hover:text-gray-900 dark:hover:text-white'
                ]"
              >
                🇬🇧
              </button>
            </div>
          </div>
        </div>
      </section>

    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { useI18n } from 'vue-i18n';
import ArrowLeftIcon from 'vue-material-design-icons/ArrowLeft.vue';

const { locale } = useI18n();
const emit = defineEmits(['back']);

interface Language {
  code: string;
  name: string;
  nativeName: string;
}

const availableLanguages: Language[] = [
  { code: 'pl', name: 'Polski', nativeName: 'Polski' },
  { code: 'en', name: 'English', nativeName: 'English (US)' }
];

const currentLocale = computed(() => locale.value);

const currentLanguageName = computed(() => {
  const lang = availableLanguages.find(l => l.code === locale.value);
  return lang ? lang.name : 'Polski';
});

const changeLanguage = (langCode: string) => {
  locale.value = langCode;
  localStorage.setItem('locale', langCode);
};

const handleBackClick = () => {
  emit('back');
};
</script>

<style scoped>
/* Opcjonalne: drobne korekty dla idealnego dopasowania */
</style>
