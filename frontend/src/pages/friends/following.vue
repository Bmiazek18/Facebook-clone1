<template>
  <FriendsView :title="$t('profile.following')" />
</template>

<script setup lang="ts">
import FriendsView from '@/components/friends/FriendsView.vue'

definePageMeta({
  title: 'Obserwowani | Facebook',
})
</script>
