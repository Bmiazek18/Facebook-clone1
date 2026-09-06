<template>
  <FriendsView :title="$t('friends.niedawnoDodani')" />
</template>

<script setup lang="ts">
import FriendsView from '@/views/FriendsView.vue'

definePageMeta({
  title: 'Niedawno dodani | Facebook',
})
</script>
