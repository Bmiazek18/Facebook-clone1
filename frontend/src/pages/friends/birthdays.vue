<template>
  <FriendsView :title="$t('friends.birthdays')" />
</template>

<script setup lang="ts">
import FriendsView from '@/components/friends/FriendsView.vue'

definePageMeta({
  title: 'Urodziny | Facebook',
})
</script>
