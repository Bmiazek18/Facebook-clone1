<script setup>
import { ref } from 'vue';
import MarketplaceSidebar from '@/components/MarketplaceSidebar.vue';
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

const handleRadiusUpdate = (radius) => {
  selectedRadius.value = radius;
};

const handleApply = (radius) => {
  selectedRadius.value = radius;
  closeLocationModal();
};
</script>

<template>
  <div class="flex min-h-screen bg-[#F0F2F5] font-sans text-gray-900">
    <MarketplaceSidebar
      :selectedRadius="selectedRadius"
      @open-location="openLocationModal"
    />

    <main class="flex-1  p-8">
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
