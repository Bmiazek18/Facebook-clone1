<script setup lang="ts">
import { ref, computed, inject, onMounted, onUnmounted } from 'vue'

import { useAuthStore } from '@/stores/auth' // Re-added

import ChatMessagePool from '@/components/chat/messageItem/ChatMessagePool.vue'
import BaseModal from '@/components/common/BaseModal.vue'
import ReactionPanel from '@/components/feed/ReactionPanel.vue'
import MessageReplyContext from '@/components/chat/MessageReplyContext.vue'
import MessageReactions from '@/components/chat/MessageReactions.vue'

import ChatMessageEmoji from '@/components/chat/messageItem/ChatMessageEmoji.vue'
import ChatMessageAction from '@/components/chat/messageItem/ChatMessageAction.vue'
import ChatMessageCall from '@/components/chat/messageItem/ChatMessageCall.vue'
import ChatMessageImage from '@/components/chat/messageItem/ChatMessageImage.vue'
import ChatMessageAudio from '@/components/chat/messageItem/ChatMessageAudio.vue'
import ChatMessageFile from '@/components/chat/messageItem/ChatMessageFile.vue'
import ChatMessageVideo from '@/components/chat/messageItem/ChatMessageVideo.vue'
import ChatMessageLink from '@/components/chat/messageItem/ChatMessageLink.vue'
import ChatMessagePostLink from '@/components/chat/messageItem/ChatMessagePostLink.vue'
import ChatMessageText from '@/components/chat/messageItem/ChatMessageText.vue'
import { formatTimeAgo } from '@/utils/timeFormatter'

import type {
  Message,
  ImageMessage,
  GifMessage,
  AudioMessage,
  FileMessage,
  VideoMessage,
  AudioState,
  LinkMessage,
} from '@/types/Message'

interface Theme {
  id?: string
  sentBubbleColor?: string
}

interface ImageMessageWithGroup extends ImageMessage {
  mediaUrls?: string[]
}
const isEmojiOnly = (content: string): boolean => {
  if (!content?.trim()) return false
  const nonEmojiChars = content
    .replace(
      /(\u00a9|\u00ae|[\u2000-\u3300]|\ud83c[\ud000-\udfff]|\ud83d[\ud000-\udfff]|\ud83e[\ud000-\udfff])/g,
      '',
    )
    .trim()
  return nonEmojiChars.length === 0
}
const props = defineProps<{
  message: Message
  metadata: {
    position: 'single' | 'first' | 'middle' | 'last'
    isLatest: boolean
  }
  lastReadMap: Record<string, number> // Added back
}>()

const emit = defineEmits<{
  (e: 'open-lightbox', url: string): void
  (e: 'reply', message: Message): void
  (e: 'add-reaction', payload: { messageId: number; emoji: string }): void
  (e: 'open-modal', type: 'CHANGE_E' | 'CHANGE_NICKNAME' | 'CHANGE_THEME'): void
}>()

// --- INJECTED CONTEXT ---
// Removed ChatContext interface and related injections/computed properties
const myUserId = computed(() => `user_${useAuthStore().currentUserId}`);


// --- FLIP ANIMATION INJECTION ---
// Wstrzykujemy funkcje od rodzica (MessageBox), co naprawia błędy przy wielu oknach
const flipContext = inject<{
  onAvatarEnter: (el: Element, done: () => void) => void
  onAvatarLeave: (el: Element, done: () => void) => void
}>('flip-animation')

// Fallback (zabezpieczenie)
const onAvatarEnter = flipContext?.onAvatarEnter || ((el, done) => done())
const onAvatarLeave = flipContext?.onAvatarLeave || ((el, done) => done())

// --- LOGIKA "SEEN BY" ---
const MAX_VISIBLE_AVATARS = 3
const getUserAvatar = (id: string) =>
  `https://ui-avatars.com/api/?name=${id}&background=random&color=fff&size=64`

const displayReadBy = computed(() => {
  if (!props.lastReadMap) return { visible: [], hiddenCount: 0 }

  let readersHere = Object.entries(props.lastReadMap)
    .filter(([, msgId]) => msgId == props.message.id)
    .map(([userId]) => userId)

  if (isMe.value) {
    readersHere = readersHere.filter((userId) => userId !== myUserId.value) // Changed
  }

  if (readersHere.length === 0) return { visible: [], hiddenCount: 0 }

  const total = readersHere.length
  const shouldTruncate = total > MAX_VISIBLE_AVATARS

  const visible = shouldTruncate ? readersHere.slice(0, MAX_VISIBLE_AVATARS) : readersHere
  const hiddenCount = shouldTruncate ? total - MAX_VISIBLE_AVATARS : 0

  return { visible, hiddenCount }
})

const currentTime = ref(Date.now())
let intervalId: ReturnType<typeof setInterval> | null = null

onMounted(() => {
  intervalId = setInterval(() => {
    currentTime.value = Date.now()
  }, 60 * 1000) // Update every minute
})

onUnmounted(() => {
  if (intervalId) {
    clearInterval(intervalId)
  }
})

const displayTimeAgo = computed(() => {
  if (
    props.metadata.isLatest &&
    isMe.value &&
    (props.metadata.position === 'single' || props.metadata.position === 'last')
  ) {
    return formatTimeAgo(props.message.time)
  }
  return null
})

// --- HELPERY TYPÓW ---
const isImageMessage = (msg: Message): msg is ImageMessageWithGroup => msg.type === 'image'
const isFileMessage = (msg: Message): msg is FileMessage => msg.type === 'file'
const isVideoMessage = (msg: Message): msg is VideoMessage => msg.type === 'video'
const isGifMessage = (msg: Message): msg is GifMessage => msg.type === 'gif'
const isAudioMessage = (msg: Message): msg is AudioMessage => msg.type === 'audio'
const isCallMessage = (msg: Message): boolean => msg.type === 'call'
const isCallRejectedMessage = (msg: Message): boolean => msg.type === 'call_rejected'
const isLinkMessage = (msg: Message): msg is LinkMessage => msg.type === 'link'
const isAnyCallType = (msg: Message): boolean => isCallMessage(msg) || isCallRejectedMessage(msg)
const isTextMessage = (msg: Message): boolean => msg.type === 'text' && !isEmojiOnly(msg.content)
const isPostLinkMessage = (msg: Message): boolean => msg.type === 'post_link'
const isMe = computed(() => props.message.sender === 'me')

const isVideoCall = (msg: Message) => {
  return (
    msg.content?.toLowerCase().includes('wideo') || msg.content?.toLowerCase().includes('video')
  )
}

const bubbleRadiusClass = computed(() => {
  const map = {
    single: 'rounded-xl',
    first: isMe.value
      ? 'rounded-l-xl rounded-tr-xl rounded-br-[4px]'
      : 'rounded-r-xl rounded-tl-xl rounded-bl-[4px]',
    middle: isMe.value ? 'rounded-l-xl rounded-r-[4px]' : 'rounded-r-xl rounded-l-[4px]',
    last: isMe.value
      ? 'rounded-l-xl rounded-tr-[4px] rounded-br-xl'
      : 'rounded-r-xl rounded-tl-[4px] rounded-bl-xl',
  }
  return map[props.metadata.position] || 'rounded-xl'
})

const bubbleColorClass = computed(() => {
  if (!isMe.value) return 'bg-gray-200 text-black'
  // Remove injectedTheme usage, assume default theme for now, or get it from props if necessary
  return ('bg-blue-500' || 'bg-blue-500') + ' text-white border border-white/10' // Simplified
})

const shouldDisplayAvatar = computed(() => {
  return (
    props.message.sender === 'other' &&
    ['single', 'last'].includes(props.metadata.position) &&
    !isEmojiOnly(props.message.content || '')
  )
})



const showReactionsPanel = ref(false)
const openReactionsPanel = () => (showReactionsPanel.value = true)
const handleReply = () => emit('reply', props.message)
const handleAddReaction = (payload: { messageId: number; emoji: string }) =>
  emit('add-reaction', payload)
</script>

<template>
  <MessageReplyContext v-if="message.isReply" :reply="message" />

  <div v-if="message.type === 'poll'" class="flex justify-center mb-4">
    <ChatMessagePool
      :question="message.pollData.question"
      :initial-options="message.pollData.options"
      :allow-multiple="message.pollData.allowMultiple"
      :allow-add-option="message.pollData.allowAddOption"
    />
  </div>

  <div
    v-else
    class="relative flex flex-col group duration-200"
    :class="{
      'items-start': !isMe,
      'items-end': isMe,
      'mb-1': props.metadata.position !== 'last' && props.metadata.position !== 'single',
      'mb-3': props.metadata.position === 'last' || props.metadata.position === 'single',
    }"
  >
    <div class="flex items-end w-full" :class="{ 'flex-row': !isMe, 'flex-row-reverse': isMe }">
      <div
        v-if="shouldDisplayAvatar"
        class="w-8 h-8 rounded-full bg-blue-100 flex items-center justify-center mr-2 shrink-0 overflow-hidden shadow-sm"
      >
        <img
          src="https://ui-avatars.com/api/?name=User&background=random"
          alt="Avatar"
          class="w-full h-full object-cover"
        />
      </div>
      <div v-else-if="!isMe" class="w-10"></div>

      <div
        class="flex items-center max-w-[75%]"
        :class="{ 'flex-row': !isMe, 'flex-row-reverse': isMe }"
      >
        <div class="relative flex flex-col overflow-visible">
          <ChatMessageEmoji
            v-if="isEmojiOnly(message.content)"
            :message="message"
          />

          <ChatMessageAction
            v-else-if="message.type === 'action'"
            :message="message"
            @open-modal="emit('open-modal', $event)"
          />

          <ChatMessageCall
            v-else-if="isAnyCallType(message)"
            :message="message"
            :is-me="isMe"
          />

          <ChatMessageImage
            v-else-if="isImageMessage(message) || isGifMessage(message)"
            :message="message"
            :is-me="isMe"
            @open-lightbox="emit('open-lightbox', $event)"
          />

          <ChatMessageAudio
            v-else-if="isAudioMessage(message)"
            :message="message"
            :box-id="message.chatId"
            :bubble-color-class="bubbleColorClass"
          />

          <ChatMessageFile
            v-else-if="isFileMessage(message)"
            :message="message"
            :is-me="isMe"
            :box-id="message.chatId"
          />

          <ChatMessageVideo
            v-else-if="isVideoMessage(message)"
            :message="message"
            :box-id="message.chatId"
          />

          <ChatMessageLink
            v-else-if="isLinkMessage(message)"
            :message="message"
          />

          <ChatMessagePostLink
            v-else-if="isPostLinkMessage(message)"
            :message="message"
          />

          <ChatMessageText
            v-else-if="isTextMessage(message)"
            :message="message"
            :bubble-radius-class="bubbleRadiusClass"
            :bubble-color-class="bubbleColorClass"
          />

          <div
            v-if="message.reactions?.length && !isAnyCallType(message)"
            @click.stop="openReactionsPanel"
            class="absolute -bottom-2 cursor-pointer bg-white rounded-full px-1.5 py-0.5 min-w-[24px] h-6 flex items-center justify-center shadow-md border border-gray-100 z-10 transition-transform hover:scale-110"
            :class="isMe ? 'left-0 translate-y-0' : 'right-0 translate-y-0'"
          >
            <span class="text-xs leading-none">{{
              message.reactions[message.reactions.length - 1]
            }}</span>
            <span
              v-if="message.reactions.length > 1"
              class="text-[9px] font-bold text-gray-500 ml-0.5"
              >{{ message.reactions.length }}</span
            >
          </div>
        </div>

        <MessageReactions
          v-if="!isAnyCallType(message) && message.type !== 'action'"
          :message-id="message.id"
          :reactions="message.reactions"
          :is-me="isMe"
          @add-reaction="handleAddReaction"
          @open-panel="openReactionsPanel"
          @reply="handleReply"
        />
      </div>
    </div>
    <div
      v-show="
        displayReadBy.visible.length > 0 ||
        displayReadBy.hiddenCount > 0 ||
        props.metadata.position == 'single' ||
        props.metadata.position == 'last'
      "
      class="h-4 mt-1 mr-1 flex justify-end w-full relative min-h-[16px]"
    >
      <div v-if="!(displayReadBy.visible.length > 0 || displayReadBy.hiddenCount > 0)">
        {{ displayTimeAgo }}
      </div>
      <TransitionGroup
        tag="div"
        class="flex justify-end items-center overflow-visible p-1"
        @enter="onAvatarEnter"
        @leave="onAvatarLeave"
        :css="false"
      >
        <div
          v-if="displayReadBy.hiddenCount > 0"
          key="counter"
          class="w-3.5 h-3.5 rounded-full ring-2 ring-white bg-gray-200 text-[8px] flex items-center justify-center text-gray-500 font-bold relative z-0"
        >
          +{{ displayReadBy.hiddenCount }}
        </div>

        <img
          v-for="(userId, i) in displayReadBy.visible"
          :key="userId"
          :data-avatar-userid="userId"
          :src="getUserAvatar(userId)"
          class="w-3.5 h-3.5 rounded-full ring-2 ring-white relative object-cover shadow-sm select-none border-white bg-gray-200"
          :style="{ zIndex: i + 1 }"
          alt="seen"
        />
      </TransitionGroup>
    </div>
  </div>

  <BaseModal v-if="showReactionsPanel" @close="showReactionsPanel = false" title="Reakcje">
    <ReactionPanel />
  </BaseModal>
</template>

<style scoped>
.emoji-size-default {
  font-size: 1.75rem;
}
.emoji-size-small {
  font-size: 45px;
}
.emoji-size-medium {
  font-size: 60px;
}
.emoji-size-large {
  font-size: 80px;
}
.bg-purple-600 {
  background-color: #8b5cf6;
}
.group-hover\:flex {
  display: none;
}
.group:hover .group-hover\:flex {
  display: flex;
}
</style>
