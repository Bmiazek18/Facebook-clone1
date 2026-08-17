<template>
  <DetailsSection :profile-user="profileUser" />
</template>

<script setup lang="ts">
import DetailsSection from '@/components/profile/info-tab/DetailsSection.vue'

defineProps<{
  profileUser: any
}>()

definePageMeta({
  keepScroll: true,
})
</script>
