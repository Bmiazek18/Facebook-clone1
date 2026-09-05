<template>
  <div class="bg-theme-bg w-full max-w-[360px] select-none antialiased">
    <!-- Nagłówek sekcji -->
    <div class="pb-3">
      <span class="text-[17px] font-semibold text-theme-text-secondary"> Sponsorowane </span>
    </div>

    <!-- Lista reklam -->
    <div class="space-y-4">
      <a
        v-for="ad in sponsoredAds"
        :key="ad.id"
        :href="ad.url"
        target="_blank"
        rel="noopener noreferrer"
        class="flex items-center gap-3 p-2 hover:bg-theme-bg-hover rounded-xl transition duration-150 ease-in-out cursor-pointer group functionality-link"
      >
        <!-- Lewa strona: Kontener na zdjęcie reklamy -->
        <div
          class="w-[115px] h-[115px] rounded-xl overflow-hidden border border-theme-border shrink-0 bg-theme-bg-tertiary flex items-center justify-center"
        >
          <img
            :src="ad.imageUrl"
            :alt="ad.title"
            class="w-full h-full object-cover group-hover:opacity-95 transition-opacity"
            loading="lazy"
          />
        </div>

        <!-- Prawa strona: Teksty i link -->
        <div class="flex flex-col justify-center min-w-0 flex-1 h-[115px]">
          <!-- Tytuł reklamy (maksymalnie 2-3 linijki, potem ucinany) -->
          <h4
            class="text-[15px] font-medium text-theme-text leading-[1.3] line-clamp-3 group-hover:underline"
          >
            {{ ad.title }}
          </h4>

          <!-- Domena/Link (szary, mniejszy tekst na dole) -->
          <span class="text-[13px] text-theme-text-secondary truncate mt-1">
            {{ ad.displayUrl }}
          </span>
        </div>
      </a>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

interface SponsoredAd {
  id: number
  title: string
  displayUrl: string
  url: string
  imageUrl: string
}

const sponsoredAds = ref<SponsoredAd[]>([
  {
    id: 1,
    title: 'Join the webinar: Accelerating IaC with AI: Smarter...',
    displayUrl: 'https://promisegroup.com/webinar',
    url: 'https://promisegroup.com/webinar',
    imageUrl: 'https://picsum.photos/seed/promise/200/200',
  },
  {
    id: 2,
    title: 'Twój kolejny projekt może być z USA.',
    displayUrl: 'app.terminal.io/onboarding',
    url: 'https://app.terminal.io/onboarding',
    imageUrl: 'https://picsum.photos/seed/terminal/200/200',
  },
])
</script>

<style scoped>
/* Bezpieczny fallback dla ucinania tekstu w starszych przeglądarkach (Tailwind line-clamp) */
.line-clamp-3 {
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>
