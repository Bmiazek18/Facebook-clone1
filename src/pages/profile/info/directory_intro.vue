<template>
  <OverviewSection :profile-user="profileUser" />
</template>

<script setup lang="ts">
import OverviewSection from '@/components/profile/info-tab/OverviewSection.vue'

defineProps<{
  profileUser: any
}>()

definePageMeta({
  keepScroll: true,
})
</script>
