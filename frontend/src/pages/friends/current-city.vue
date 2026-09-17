<template>
  <FriendsView :title="$t('friends.aktualneMiejsceZamieszkania')" filterType="CURRENT_CITY" />
</template>

<script setup lang="ts">
import FriendsView from '@/components/friends/FriendsView.vue'

definePageMeta({
  title: 'Aktualne miejsce zamieszkania | Facebook',
})
</script>
