<template>
  <FriendsView :title="$t('friends.niedawnoDodani')" />
</template>

<script setup lang="ts">
import FriendsView from '@/components/friends/FriendsView.vue'

definePageMeta({
  title: 'Niedawno dodani | Facebook',
})
</script>
