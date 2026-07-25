<template>
  <StoryView :user-id="route.params.userId as string" />
</template>

<script setup lang="ts">
import { useRoute } from '#imports'
import StoryView from '@/views/StoryView.vue'

const route = useRoute()

definePageMeta({
  showMainLayout: false,
  key: 'stories-viewer',
})
</script>
