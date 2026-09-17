<template>
  <FriendsView :title="$t('friends.highSchool')" filterType="HIGH_SCHOOL" />
</template>

<script setup lang="ts">
import FriendsView from '@/components/friends/FriendsView.vue'

definePageMeta({
  title: 'Szkoła średnia | Facebook',
})
</script>
