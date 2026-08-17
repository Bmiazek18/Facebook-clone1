<template>
  <ProfilePostsTab v-bind="$attrs" />
</template>

<script setup lang="ts">
import ProfilePostsTab from '@/components/profile/ProfilePostsTab.vue'

definePageMeta({
  keepScroll: true,
})
</script>
