<script setup lang="ts">
import FileDocument from 'vue-material-design-icons/FileDocument.vue'
</script>

<template>
  <div class="flex flex-col items-center justify-center p-6 text-center">
    <div class="mb-6">
      <FileDocument :size="90" class="text-theme-text-secondary opacity-50" />
    </div>

    <h3 class="text-xl md:text-2xl font-bold text-theme-text-secondary mb-2">{{ $t('feed.nieMaJeszczeKomentarzy') }}</h3>

    <p class="text-theme-text-secondary text-sm md:text-base max-w-xs">{{ $t('feed.badzPierwszaOsobaKtora') }}</p>
  </div>
</template>
