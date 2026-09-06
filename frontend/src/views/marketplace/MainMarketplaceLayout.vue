<script setup lang="ts">
import { ref } from 'vue'
import MarketplaceLeftSidebar from '@/components/marketplace/MarketplaceLeftSidebar.vue'
import BaseModal from '@/components/common/BaseModal.vue'
import MapRadius from '@/components/marketplace/MapRadius.vue'

// Modal state for location selection
const showLocationModal = ref(false)
const selectedRadius = ref(402) // Default radius

const openLocationModal = () => {
  showLocationModal.value = true
}

const closeLocationModal = () => {
  showLocationModal.value = false
}

const handleRadiusUpdate = (radius: number) => {
  selectedRadius.value = radius
}

const handleApply = (radius: number) => {
  selectedRadius.value = radius
  closeLocationModal()
}
</script>

<template>
  <div class="flex min-h-screen bg-theme-bg   text-theme-text">
    <MarketplaceLeftSidebar :selectedRadius="selectedRadius" @open-location="openLocationModal" />

    <main class="w-full p-6">
      <NuxtPage />
    </main>
  </div>

  <!-- Location Modal -->
  <BaseModal v-if="showLocationModal" @close="closeLocationModal" :title="$t('common.wybierzLokalizacje')">
    <MapRadius @update:radius="handleRadiusUpdate" @apply="handleApply" />
  </BaseModal>
</template>

<style scoped>
/* layout-level styles if needed */
</style>
