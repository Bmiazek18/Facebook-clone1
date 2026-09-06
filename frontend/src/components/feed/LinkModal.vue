<template>
  <div
    class="w-full max-w-[480px] rounded-xl bg-theme-bg-secondary p-6 text-theme-text shadow-2xl   relative"
  >
    <div v-if="loading" class="flex flex-col items-center py-10">
      <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-theme-primary"></div>
      <p class="mt-4 text-theme-text-secondary text-sm">{{ $t('create.pobieranieDanych') }}</p>
    </div>

    <template v-else>
      <header class="flex items-start mb-5">
        <div
          class="mr-4 flex h-12 w-12 flex-shrink-0 items-center justify-center rounded-full bg-white overflow-hidden shadow-sm"
        >
          <img
            v-if="displayData.image_url"
            :src="displayData.image_url"
            :alt="$t('feed.logo')"
            class="h-full w-full object-contain p-1"
          />
          <span v-else class="text-[11px] font-bold text-gray-700 uppercase tracking-tighter">{{
            displayData.logoText
          }}</span>
        </div>

        <div>
          <div class="flex items-center">
            <h2 class="text-xl font-normal leading-tight">{{ displayData.title }}</h2>
            <svg class="ml-2 h-5 w-5 text-theme-primary" viewBox="0 0 24 24" fill="currentColor">
              <path
                d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z"
              />
            </svg>
          </div>
          <p class="mt-0.5 text-base text-theme-text-secondary">{{ displayData.source === 'Platforma' ? 'Strona na platformie' : subtitle }}</p>
        </div>
      </header>

      <section v-if="displayData.source !== 'Brak danych'" class="mb-6">
        <p class="text-[14px] leading-relaxed text-theme-text">
          {{ displayData.description }}
        </p>
        <p class="mt-2 text-[13px] text-theme-text-secondary">{{ $t('feed.zrodlo') }}<a
            :href="displayData.wiki_url"
            target="_blank"
            class="text-theme-primary hover:underline cursor-pointer font-medium"
          >
            {{ displayData.source }}
          </a>
        </p>
      </section>

      <footer v-if="displayData.source === 'Platforma'" class="flex items-start border-t border-theme-border pt-4">
        <div class="mr-3 flex h-9 w-9 items-center justify-center rounded-full bg-theme-bg-tertiary text-theme-text-secondary shrink-0">
          <svg class="h-5 w-5" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
        </div>
        <div class="text-[13px] flex-1">
          <h3 class="font-medium text-[15px] text-theme-text mb-1">{{ $t('feed.biogramStrony') }}</h3>
          <p class="text-theme-text-secondary italic leading-relaxed">{{ $t('feed.displaydataDescriptionTaStrona') }}</p>
        </div>
      </footer>

      <footer v-else class="flex items-center border-t border-theme-border pt-4">
        <div
          class="mr-3 flex h-9 w-9 items-center justify-center rounded-full bg-theme-bg-tertiary text-theme-text-secondary"
        >
          <svg
            class="h-5 w-5"
            fill="none"
            stroke="currentColor"
            stroke-width="2"
            viewBox="0 0 24 24"
          >
            <circle cx="12" cy="12" r="10"></circle>
            <polyline points="12 6 12 12 16 14"></polyline>
          </svg>
        </div>
        <div class="text-[13px]">
          <h3 class="font-medium text-[15px] text-theme-text">{{ $t('feed.rejestracjaDomenyDisplaydataDomain') }}</h3>
          <p class="text-theme-text-secondary">{{ $t('feed.displaydataRegistrationDateZ') }}<span class="font-medium text-theme-primary uppercase">{{ $t('feed.whois') }}</span>
          </p>
        </div>
      </footer>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'

const props = defineProps({
  targetUrl: { type: String, default: '' },
  subtitle: { type: String, default: 'Witryna informacyjna' },
})

const loading = ref(true)
const displayData = ref({
  title: '',
  description: '',
  registration_date: '',
  domain: '',
  logoText: '',
  source: '',
  image_url: '', // Dodane
  wiki_url: '', // Dodane
})

const fetchSiteInfo = async () => {
  loading.value = true
  try {
    const response = await fetch(
      `${import.meta.env.VITE_API_URL || 'http://localhost:8080'}/linkguard/graphql`,
      {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          query: `query SiteInfo($url: String!) { getSiteInfo(url: $url) { title description registrationDate domain source imageUrl wikiUrl } }`,
          variables: { url: props.targetUrl },
        }),
      },
    )

    const result = await response.json()
    if (!response.ok || result.errors?.length)
      throw new Error(result.errors?.[0]?.message || 'LinkGuard unavailable')
    const data = result.data.getSiteInfo

    displayData.value = {
      title: data.title,
      description: data.description,
      registration_date: data.registrationDate,
      domain: data.domain,
      logoText: data.title.substring(0, 10),
      source: data.source,
      image_url: data.imageUrl, // Mapowanie zdjęcia
      wiki_url: data.wikiUrl, // Mapowanie linku
    }
  } catch (error) {
    console.error('Błąd API:', error)
    displayData.value.description = 'Nie udało się wczytać danych.'
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchSiteInfo()
})
</script>
