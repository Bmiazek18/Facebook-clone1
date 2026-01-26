<script setup lang="ts">
import { ref } from 'vue';
import MarketplaceLeftSidebar from '@/components/MarketplaceLeftSidebar.vue';
import BaseModal from '@/components/common/BaseModal.vue';
import MapRadius from '@/components/MapRadius.vue';

// Modal state for location selection
const showLocationModal = ref(false);
const selectedRadius = ref(402); // Default radius

const openLocationModal = () => {
  showLocationModal.value = true;
};

const closeLocationModal = () => {
  showLocationModal.value = false;
};

const handleRadiusUpdate = (radius: number) => {
  selectedRadius.value = radius;
};

const handleApply = (radius: number) => {
  selectedRadius.value = radius;
  closeLocationModal();
};
</script>

<template>
  <div class="flex min-h-screen bg-theme-bg font-sans text-theme-text">
    <MarketplaceLeftSidebar 
      :selectedRadius="selectedRadius" 
      @open-location="openLocationModal" 
    />

    <main class="ml-90 w-full p-6">
      <router-view />
    </main>
  </div>

  <!-- Location Modal -->
  <BaseModal v-if="showLocationModal" @close="closeLocationModal" title="Wybierz lokalizację">
    <MapRadius @update:radius="handleRadiusUpdate" @apply="handleApply" />
  </BaseModal>
</template>

<style scoped>
/* layout-level styles if needed */
</style>