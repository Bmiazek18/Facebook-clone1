<template>
  <FriendsView :title="$t('friends.following')" filterType="FOLLOWING" />
</template>

<script setup lang="ts">
import FriendsView from '@/components/friends/FriendsView.vue'

definePageMeta({
  title: 'Obserwowani | Facebook',
})
</script>
