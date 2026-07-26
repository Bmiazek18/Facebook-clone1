<script setup lang="ts">
import { inject } from 'vue'
import type { Theme } from '@/types/Theme'
import type { Message } from '@/types/Message'

import ChatMessagePool from '@/components/chat/messageItem/bubbles/ChatMessagePool.vue'
import ChatMessageAction from '@/components/chat/messageItem/bubbles/ChatMessageAction.vue'
import MessageBubbleWrapper from '@/components/chat/messageItem/MessageBubbleWrapper.vue'

const props = defineProps<{
  message: Message
  theme: Theme
  metadata: {
    position: 'single' | 'first' | 'middle' | 'last'
    isLatest: boolean
  }
  lastReadMap: Record<string, number>
  isHighlighted?: boolean
}>()

const chatActions = inject<{
  openModal: (type: 'CHANGE_E' | 'CHANGE_NICKNAME' | 'CHANGE_THEME') => void
}>('chatActions')
</script>

<template>
  <!-- 1. ANKIETY - na środku -->
  <div v-if="message.type === 'poll'" class="flex justify-center mb-4">
    <ChatMessagePool
      :question="message.pollData.question"
      :initial-options="message.pollData.options"
      :allow-multiple="message.pollData.allowMultiple"
      :allow-add-option="message.pollData.allowAddOption"
    />
  </div>

  <!-- 2. AKCJE SYSTEMOWE (zmiana motywu, nicku) - na środku -->
  <div v-else-if="message.type === 'action'" class="flex justify-center w-full my-3">
    <ChatMessageAction
      :message="message"
      @open-modal="chatActions?.openModal($event)"
    />
  </div>

  <!-- 3. STANDARDOWE WIADOMOŚCI (Tekst, Media) - lewo/prawo -->
  <MessageBubbleWrapper
    v-else
    :message="message"
    :theme="theme"
    :metadata="metadata"
    :last-read-map="lastReadMap"
    :is-highlighted="isHighlighted"
  />
</template>
