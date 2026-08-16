<template>
  <div class="min-h-screen bg-white flex justify-center py-10 font-sans">

    <!-- Główny kontener -->
    <div class="w-full max-w-2xl px-4">

      <!-- Nagłówek -->
      <div class="mb-8">
        <h1 class="text-[22px] font-bold text-gray-900 leading-tight mb-2">
          Jakiego rodzaju komentarze powinien ukrywać Asystent moderacji?
        </h1>
        <p class="text-[16px] text-gray-500">
          Rozpocznij od jednego kryterium, a potem dodaj ich więcej.
        </p>
      </div>

      <!-- KRYTERIA DOTYCZĄCE AUTORA -->
      <div class="mb-8">
        <h2 class="text-[14px] font-semibold text-gray-800 uppercase tracking-wide mb-3 pl-2">
          Kryteria dotyczące autora
        </h2>
        <div class="flex flex-col gap-1">
          <button
            v-for="item in authorCriteria"
            :key="item.id"
            class="w-full flex items-center justify-between p-3 rounded-xl hover:bg-gray-50 transition-colors text-left group"
          >
            <div class="flex items-center gap-4">
              <!-- Dynamiczna Ikona w okręgu -->
              <div
                class="w-[44px] h-[44px] rounded-full flex items-center justify-center shrink-0 transition-colors"
                :class="item.isActive ? 'bg-[#1877F2] text-white' : 'bg-[#E4E6EB] text-gray-800'"
              >
                <component :is="item.icon" :size="24" />
              </div>

              <!-- Teksty -->
              <div class="flex flex-col">
                <span class="text-[17px] font-semibold text-gray-900 leading-snug">
                  {{ item.title }}
                </span>
                <span v-if="item.subtitle" class="text-[14px] text-gray-500 mt-0.5">
                  {{ item.subtitle }}
                </span>
              </div>
            </div>

            <!-- Strzałka (Chevron) -->
            <ChevronRightIcon class="text-gray-400 group-hover:text-gray-600 transition-colors" :size="24" />
          </button>
        </div>
      </div>

      <!-- KRYTERIA DOTYCZĄCE KOMENTARZY -->
      <div>
        <h2 class="text-[14px] font-semibold text-gray-800 uppercase tracking-wide mb-3 pl-2">
          Kryteria dotyczące komentarzy
        </h2>
        <div class="flex flex-col gap-1">
          <button
            v-for="item in commentCriteria"
            :key="item.id"
            class="w-full flex items-center justify-between p-3 rounded-xl hover:bg-gray-50 transition-colors text-left group"
          >
            <div class="flex items-center gap-4">
              <!-- Dynamiczna Ikona w okręgu -->
              <div
                class="w-[44px] h-[44px] rounded-full flex items-center justify-center shrink-0 bg-[#E4E6EB] text-gray-800"
              >
                <component :is="item.icon" :size="24" />
              </div>

              <!-- Tytuł -->
              <span class="text-[17px] font-semibold text-gray-900 leading-snug">
                {{ item.title }}
              </span>
            </div>

            <!-- Strzałka (Chevron) -->
            <ChevronRightIcon class="text-gray-400 group-hover:text-gray-600 transition-colors" :size="24" />
          </button>
        </div>
      </div>

    </div>
  </div>
</template>

<script setup>
// Importy z biblioteki vue-material-design-icons
import 'vue-material-design-icons/styles.css';

import CheckCircleIcon from 'vue-material-design-icons/CheckCircle.vue';
import CakeVariantIcon from 'vue-material-design-icons/CakeVariant.vue';
import AccountOffIcon from 'vue-material-design-icons/AccountOff.vue';
import MessageAlertIcon from 'vue-material-design-icons/MessageAlert.vue';
import LinkIcon from 'vue-material-design-icons/Link.vue';
import ImageIcon from 'vue-material-design-icons/Image.vue';
import WebIcon from 'vue-material-design-icons/Web.vue';
import PlayCircleIcon from 'vue-material-design-icons/PlayCircle.vue';
import MessageTextIcon from 'vue-material-design-icons/MessageText.vue';
import MessageMinusIcon from 'vue-material-design-icons/MessageMinus.vue';
import ChevronRightIcon from 'vue-material-design-icons/ChevronRight.vue';

// Tablice danych z wstrzykniętymi komponentami ikon
const authorCriteria = [
  {
    id: 1,
    title: 'Bez znajomych i obserwatorów',
    subtitle: 'Włączono 9 sie 2026',
    isActive: true,
    icon: CheckCircleIcon
  },
  {
    id: 2,
    title: 'Nowy profil',
    isActive: false,
    icon: CakeVariantIcon
  },
  {
    id: 3,
    title: 'Brak zdjęcia profilowego',
    isActive: false,
    icon: AccountOffIcon
  },
  {
    id: 4,
    title: 'Osoba nagminnie łamiąca zasady',
    isActive: false,
    icon: MessageAlertIcon
  }
];

const commentCriteria = [
  {
    id: 5,
    title: 'Link w komentarzu',
    icon: LinkIcon
  },
  {
    id: 6,
    title: 'Obraz w komentarzu',
    icon: ImageIcon
  },
  {
    id: 7,
    title: 'Wulgaryzmy w komentarzach',
    icon: MessageAlertIcon
  },
  {
    id: 8,
    title: 'Linki do określonych witryn',
    icon: WebIcon
  },
  {
    id: 9,
    title: 'Film w komentarzu',
    icon: PlayCircleIcon
  },
  {
    id: 10,
    title: 'Słowa kluczowe w komentarzach',
    icon: MessageTextIcon
  },
  {
    id: 11,
    title: 'Komentarze ze spamem',
    icon: MessageMinusIcon
  }
];
</script>

<style scoped>
/* Jeśli chcesz usunąć domyślne style ikon nadawane przez bibliotekę (żeby korzystać tylko z Tailwind), użyj tego: */
:deep(.material-design-icon__svg) {
  fill: currentColor;
}
</style>
