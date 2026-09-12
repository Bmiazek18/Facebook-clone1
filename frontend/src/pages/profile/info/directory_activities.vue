<template>
  <EventsSection :profile-user="profileUser" />
</template>

<script setup lang="ts">
import EventsSection from '@/components/profile/info-tab/EventsSection.vue'

defineProps<{
  profileUser: any
}>()

definePageMeta({
  keepScroll: true,
})
</script>
