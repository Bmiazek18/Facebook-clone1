<template>
  <!-- Komponent otrzymuje kontekst z adresu URL -->
  <FriendsSection :is-full-view="true" :page-type="pageType" />
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from '#app'
import FriendsSection from '@/components/friends/FriendsSection.vue'

const route = useRoute()

// Do celów logiki wewnątrz komponentu (opcjonalnie)
const pageType = computed(() => route.params.pageType as string)

definePageMeta({
  keepScroll: true,
  // ZABEZPIECZENIE (Walidacja): Nuxt wpuści użytkownika TYLKO, gdy URL pasuje do tablicy poniżej
  validate: (route) => {
    const validPages = [
      'friends_all',
      'friends_recent',
      'friends_birthdays',
      'friends_high_school',
      'friends_current_city',
      'following',
    ]

    // Jeśli zwrócimy false, Nuxt natychmiast przerwie renderowanie i pokaże 404
    return validPages.includes(route.params.pageType as string)
  },
})
</script>
