<script setup lang="ts">
import PlayerVideo from '@/components/media/PlayerVideo.vue'
import type { Message } from '@/types/Message'

const props = defineProps<{
  message: Message
}>()
</script>

<template>
  <div class="rounded-xl overflow-hidden shadow-sm">
    <PlayerVideo :url="message.videoUrl" />
  </div>
</template>
