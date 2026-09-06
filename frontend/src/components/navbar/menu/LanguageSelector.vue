<template>
  <div
    class="language-settings max-w-xl mx-auto bg-white dark:bg-theme-bg-secondary text-theme-text rounded-xl p-4"
  >
    <!-- Nagłówek -->
    <div class="w-full flex items-center pb-4 border-b border-gray-200 dark:border-gray-800 mb-2">
      <button
        @click="handleBackClick"
        class="rounded-full p-2 -ml-2 hover:bg-gray-100 dark:hover:bg-theme-hover transition duration-150 mr-2"
        :aria-label="$t('chat.powrot')"
      >
        <ArrowLeftIcon class="text-2xl text-theme-text" />
      </button>
      <h1 class="text-xl font-bold text-theme-text">{{ $t('language_settings.title') }}</h1>
    </div>

    <div class="space-y-6 pt-2">
      <!-- Sekcja 1: Język aplikacji -->
      <section>
        <h2 class="text-base font-bold text-theme-text px-2 mb-1">
          {{ $t('language_settings.facebook_language') }}
        </h2>
        <p class="text-sm text-gray-500 dark:text-gray-400 px-2 mb-3 leading-snug">
          {{ $t('language_settings.language_description') }}
        </p>

        <!-- Wyborca języka (Facebook Style) -->
        <div class="flex flex-col gap-1">
          <button
            v-for="lang in availableLanguages"
            :key="lang.code"
            @click="changeLanguage(lang.code)"
            class="w-full flex items-center justify-between py-2.5 px-3 rounded-lg hover:bg-gray-100 dark:hover:bg-theme-hover transition duration-150 text-left cursor-pointer"
          >
            <div class="flex flex-col">
              <span class="text-[15px] font-semibold text-theme-text leading-tight">
                {{ lang.nativeName }}
              </span>
              <span v-if="lang.name !== lang.nativeName" class="text-xs text-gray-500 dark:text-gray-400">
                {{ lang.name }}
              </span>
            </div>

            <!-- Radio Indicator -->
            <div
              :class="[
                'w-5 h-5 rounded-full border-2 flex items-center justify-center transition-all shrink-0',
                currentLocale === lang.code
                  ? 'border-gray-900 dark:border-white'
                  : 'border-gray-400 dark:border-gray-500',
              ]"
            >
              <div
                v-if="currentLocale === lang.code"
                class="w-2.5 h-2.5 bg-gray-900 dark:bg-white rounded-full"
              ></div>
            </div>
          </button>
        </div>
      </section>

      <hr class="border-gray-200 dark:border-gray-800 -mx-4" />

      <!-- Sekcja 2: Tłumaczenia postów -->
      <section>
        <h2 class="text-base font-bold text-theme-text px-2 mb-1">{{ $t('header.postyOdZnajomychI') }}</h2>
        <p class="text-sm text-gray-500 dark:text-gray-400 px-2 mb-3 leading-snug">{{ $t('header.jezykNaKtoryMaja') }}</p>

        <button
          class="w-full flex items-center justify-between py-3 px-3 rounded-lg hover:bg-gray-100 dark:hover:bg-theme-hover transition duration-150 text-left cursor-pointer"
        >
          <div class="flex flex-col">
            <span class="text-xs text-gray-500 dark:text-gray-400">{{ $t('header.jezykDocelowy') }}</span>
            <span class="text-[15px] font-semibold text-theme-text">
              {{ currentLanguageName }}
            </span>
          </div>
          <ChevronRightIcon :size="24" class="text-gray-400" />
        </button>
      </section>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import ArrowLeftIcon from 'vue-material-design-icons/ArrowLeft.vue'
import ChevronRightIcon from 'vue-material-design-icons/ChevronRight.vue'

const { locale } = useI18n()
const emit = defineEmits(['back'])

interface Language {
  code: string
  name: string
  nativeName: string
}

const availableLanguages: Language[] = [
  { code: 'pl', name: 'Polski', nativeName: 'Polski' },
  { code: 'en', name: 'Angielski', nativeName: 'English (US)' },
]

const currentLocale = computed(() => locale.value)

const currentLanguageName = computed(() => {
  const lang = availableLanguages.find((l) => l.code === locale.value)
  return lang ? lang.nativeName : 'Polski'
})

const changeLanguage = (langCode: string) => {
  locale.value = langCode
  localStorage.setItem('locale', langCode)
}

const handleBackClick = () => {
  emit('back')
}
</script>
