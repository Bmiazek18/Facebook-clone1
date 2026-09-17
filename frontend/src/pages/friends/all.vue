<template>
  <FriendsView :title="$t('friends.allFriends')" filterType="ALL" />
</template>

<script setup lang="ts">
import FriendsView from '@/components/friends/FriendsView.vue'

definePageMeta({
  title: 'Wszyscy znajomi | Facebook',
})
</script>
