<script setup lang="ts">
import { computed } from 'vue'
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
  () => isImageMessage(props.message) && (props.message.mediaUrls?.length ?? 0) > 1,
)
const isSingleImageOrGif = computed(
  () => (isImageMessage(props.message) || isGifMessage(props.message)) && !isGroupedImage.value,
)
</script>

<template>
  <div class="overflow-visible">
    <div v-if="!isGroupedImage" class="mb-1 color">
      <img
        :src="(message as ImageMessage).imageUrl"
        class="max-w-full h-auto rounded-xl shadow-sm cursor-pointer hover:opacity-95 transition-opacity"
        @click="emit('open-lightbox', (message as ImageMessage).imageUrl)"
        :alt="$t('chat.attachment')"
        loading="lazy"
      />
    </div>

    <div
      v-else-if="
        isGroupedImage &&
        (message.mediaUrls?.length ?? 0) >= 2 &&
        (message.mediaUrls?.length ?? 0) <= 3
      "
      class="relative w-[180px] cursor-pointer"
      :class="(message.mediaUrls?.length ?? 0) === 3 ? 'h-[260px]' : 'h-[200px]'"
    >
      <img
        :src="message.mediaUrls?.[0]"
        @click="emit('open-lightbox', message.mediaUrls?.[0] ?? '')"
        class="absolute top-0 right-0 w-[120px] h-[120px] rounded-2xl object-cover bg-theme-bg shadow-lg z-30"
        :alt="$t('chat.zdjecie1')"
      />

      <img
        :src="message.mediaUrls?.[1]"
        @click="emit('open-lightbox', message.mediaUrls?.[1] ?? '')"
        class="absolute top-[70px] left-0 w-[120px] h-[120px] rounded-2xl object-cover bg-theme-bg shadow-lg z-20"
        :alt="$t('chat.zdjecie2')"
      />

      <template v-if="(message.mediaUrls?.length ?? 0) === 3">
        <img
          @click="emit('open-lightbox', message.mediaUrls?.[2] ?? '')"
          :src="message.mediaUrls?.[2]"
          class="absolute bottom-0 right-0 w-[120px] h-[120px] rounded-2xl object-cover bg-theme-bg shadow-lg z-10"
          :alt="$t('chat.zdjecie3')"
        />
      </template>
    </div>

    <div
      v-else-if="isGroupedImage && (message.mediaUrls?.length ?? 0) > 3"
      class="relative w-[140px] h-[100px] cursor-pointer"
    >
      <img
        @click="emit('open-lightbox', message.mediaUrls?.[0] ?? '')"
        :src="message.mediaUrls?.[0]"
        class="absolute top-0 left-0 w-[100px] h-20 object-cover rounded-lg shadow-md ring-2 z-10"
        :style="{ transform: 'rotate(-6deg)' }"
        :alt="$t('chat.zdjecie1')"
      />
      <img
        @click="emit('open-lightbox', message.mediaUrls?.[1] ?? '')"
        :src="message.mediaUrls?.[1]"
        class="absolute top-2 left-8 w-[100px] h-20 object-cover rounded-lg shadow-md ring-2 z-20"
        :style="{ transform: 'rotate(4deg)' }"
        :alt="$t('chat.zdjecie2')"
      />
      <img
        @click="emit('open-lightbox', message.mediaUrls?.[2] ?? '')"
        v-if="(message.mediaUrls?.length ?? 0) >= 3"
        :src="message.mediaUrls?.[2]"
        class="absolute -top-2 left-4 w-[100px] h-20 object-cover rounded-lg shadow-md ring-2 z-0"
        :style="{ transform: 'rotate(-2deg)' }"
        :alt="$t('chat.zdjecie3')"
      />
      <span
        class="absolute bottom-0 right-0 bg-black/60 text-white text-xs px-2 py-0.5 rounded-full z-30"
      >
        {{ message.mediaUrls?.length }}
      </span>
    </div>
  </div>
</template>
