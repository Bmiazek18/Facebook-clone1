<template>
  <FriendsView :title="$t('profile.info.highSchool')" />
</template>

<script setup lang="ts">
import FriendsView from '@/components/friends/FriendsView.vue'

definePageMeta({
  title: 'Szkoła średnia | Facebook',
})
</script>
