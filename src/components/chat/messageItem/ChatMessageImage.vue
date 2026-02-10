<script setup lang="ts">
import { computed } from 'vue'
import MessageMediaGallery from '@/components/chat/MessageMediaGallery.vue'
import type { Message, ImageMessage, GifMessage } from '@/types/Message'

interface ImageMessageWithGroup extends ImageMessage {
  mediaUrls?: string[]
}

const props = defineProps<{
  message: Message
  isMe: boolean
}>()

const emit = defineEmits<{
  (e: 'open-lightbox', url: string): void
}>()

const isImageMessage = (msg: Message): msg is ImageMessageWithGroup => msg.type === 'image'
const isGifMessage = (msg: Message): msg is GifMessage => msg.type === 'gif'

const isGroupedImage = computed(
  () => isImageMessage(props.message) && (props.message.mediaUrls?.length ?? 0) > 0,
)
const isSingleImageOrGif = computed(
  () => (isImageMessage(props.message) || isGifMessage(props.message)) && !isGroupedImage.value,
)
</script>

<template>
  <MessageMediaGallery
    v-if="isGroupedImage"
    :media-urls="message.mediaUrls || []"
    :is-me="isMe"
    @open-lightbox="emit('open-lightbox', $event)"
  />

  <div v-else-if="isSingleImageOrGif" class="mb-1">
    <img
      :src="(message as ImageMessage).imageUrl"
      class="max-w-full h-auto rounded-xl shadow-sm cursor-pointer hover:opacity-95 transition-opacity"
      :class="{ 'border-2 border-purple-400': message.type === 'gif' }"
      @click="emit('open-lightbox', (message as ImageMessage).imageUrl)"
      alt="Attachment"
      loading="lazy"
    />
  </div>
</template>
