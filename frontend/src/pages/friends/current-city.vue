<template>
  <FriendsView :title="$t('friends.aktualneMiejsceZamieszkania')" />
</template>

<script setup lang="ts">
import FriendsView from '@/views/FriendsView.vue'

definePageMeta({
  title: 'Aktualne miejsce zamieszkania | Facebook',
})
</script>
