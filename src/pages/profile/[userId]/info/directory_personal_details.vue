<template>
  <PersonalDetailsSection :profile-user="profileUser" />
</template>

<script setup lang="ts">
import PersonalDetailsSection from '@/components/profile/info-tab/PersonalDetailsSection.vue'

defineProps<{
  profileUser: any
}>()

definePageMeta({
  keepScroll: true,
})
</script>
