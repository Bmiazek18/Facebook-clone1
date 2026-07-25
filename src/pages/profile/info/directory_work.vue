<template>
  <PlacesSection :profile-user="profileUser" />
</template>

<script setup lang="ts">
import PlacesSection from '@/components/profile/info-tab/PlacesSection.vue'

defineProps<{
  profileUser: any
}>()

definePageMeta({
  keepScroll: true,
})
</script>
