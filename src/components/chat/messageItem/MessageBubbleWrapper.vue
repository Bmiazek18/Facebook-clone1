<script setup lang="ts">
import { ref, computed, inject, onMounted, onUnmounted } from 'vue'
import type { Theme } from '@/types/Theme'
import { useAuthStore } from '@/stores/auth'
import { useConversationsStore } from '@/stores/conversations'
import PinIcon from 'vue-material-design-icons/Pin.vue'

import BaseModal from '@/components/common/BaseModal.vue'
import MessageReplyContext from '@/components/chat/messageItem/MessageReplyContext.vue'
import MessageReactions from '@/components/chat/messageItem/MessageReactions.vue'
import ChatMessageEmoji from '@/components/chat/messageItem/bubbles/ChatMessageEmoji.vue'
import ChatMessageCall from '@/components/chat/messageItem/bubbles/ChatMessageCall.vue'
import ChatMessageImage from '@/components/chat/messageItem/bubbles/ChatMessageImage.vue'
import ChatMessageAudio from '@/components/chat/messageItem/bubbles/ChatMessageAudio.vue'
import ChatMessageFile from '@/components/chat/messageItem/bubbles/ChatMessageFile.vue'
import ChatMessageVideo from '@/components/chat/messageItem/bubbles/ChatMessageVideo.vue'
import ChatMessageLink from '@/components/chat/messageItem/bubbles/ChatMessageLink.vue'
import ChatMessagePostLink from '@/components/chat/messageItem/bubbles/ChatMessagePostLink.vue'
import ChatMessageContent from '@/components/chat/messageItem/bubbles/ChatMessageContent.vue'
import { formatTimeAgo } from '@/utils/timeFormatter'

import type {
  Message,
  ImageMessageWithGroup,
  GifMessage,
  AudioMessage,
  FileMessage,
  VideoMessage,
  LinkMessage,
} from '@/types/Message'

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
  openLightbox: (url: string) => void
  reply: (message: Message) => void
  addReaction: (payload: { messageId: number; emoji: string }) => void
  scrollToMessage: (messageId: number) => void
  pin: (messageId: number) => void
}>('chatActions')

const myUserId = computed(() => `user_${useAuthStore().currentUserId}`)

const flipContext = inject<{
  onAvatarEnter: (el: Element, done: () => void) => void
  onAvatarLeave: (el: Element, done: () => void) => void
}>('flip-animation')

const onAvatarEnter = flipContext?.onAvatarEnter || ((el, done) => done())
const onAvatarLeave = flipContext?.onAvatarLeave || ((el, done) => done())

const MAX_VISIBLE_AVATARS = 3

function isSameDay(t1: number, t2: number) {
  return new Date(t1).toDateString() === new Date(t2).toDateString()
}

const getDisplayTime = (timestamp: number) => {
  const dateStr = new Date(timestamp)
  const timeStr = dateStr.toLocaleTimeString('pl-PL', { hour: '2-digit', minute: '2-digit' })

  if (props.metadata.position === 'first' || !isSameDay(timestamp, new Date().getTime())) {
    const dayName = dateStr.toLocaleDateString('pl-PL', { weekday: 'short' }).replace('.', '')
    return `${dayName.charAt(0).toUpperCase() + dayName.slice(1)}, ${timeStr}`
  }
  return timeStr
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

const getUserAvatar = (userId: string) => {
  const cleanId = userId.replace('user_', '')
  const convStore = useConversationsStore()
  const user = convStore.usersCache[cleanId]
  if (user?.avatar) return user.avatar
  convStore.getOrFetchUser(cleanId)
  const apiUrl = import.meta.env.VITE_API_URL || 'http://localhost:8080'
  return `${apiUrl}/api/users/avatar/default-avatar.svg`
}

const getUserName = (userId: string) => {
  const cleanId = userId.replace('user_', '')
  const convStore = useConversationsStore()
  const user = convStore.usersCache[cleanId]
  if (user) return user.name
  convStore.getOrFetchUser(cleanId)
  return 'Użytkownik'
}

const getTooltipText = (userId: string) => {
  const name = getUserName(userId)
  const timeStr = getDisplayTime(props.message.time)
  return `Wyświetlona przez ${name} o ${timeStr}`
}

const displayReadBy = computed(() => {
  if (!props.lastReadMap) return { visible: [], hiddenCount: 0 }

  let readersHere = Object.entries(props.lastReadMap)
    .filter(([, msgId]) => msgId == props.message.id)
    .map(([userId]) => userId)

  readersHere = readersHere.filter((userId) => userId !== myUserId.value)

  if (readersHere.length === 0) return { visible: [], hiddenCount: 0 }

  const total = readersHere.length
  const shouldTruncate = total > MAX_VISIBLE_AVATARS

  const visible = shouldTruncate ? readersHere.slice(0, MAX_VISIBLE_AVATARS) : readersHere
  const hiddenCount = shouldTruncate ? total - MAX_VISIBLE_AVATARS : 0

  return { visible, hiddenCount }
})

const currentTime = ref(Date.now())
let intervalId: ReturnType<typeof setInterval> | null = null

const messageWrapperRef = ref<HTMLElement | null>(null)
const isVisible = ref(false)
let observer: IntersectionObserver | null = null

onMounted(() => {
  intervalId = setInterval(() => {
    currentTime.value = Date.now()
  }, 60 * 1000)

  observer = new IntersectionObserver(
    ([entry]) => {
      isVisible.value = entry.isIntersecting
    },
    { threshold: 1 },
  )

  if (messageWrapperRef.value) {
    observer.observe(messageWrapperRef.value)
  }
})

onUnmounted(() => {
  if (intervalId) clearInterval(intervalId)
  if (observer) observer.disconnect()
})

const displayTimeAgo = computed(() => {
  if (
    props.metadata.isLatest &&
    isMe.value &&
    (props.metadata.position === 'single' || props.metadata.position === 'last')
  ) {
    return `Wysłano ${formatTimeAgo(props.message.time)}`
  }
  return null
})

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

const bubbleColor = computed(() => {
  if (!isMe.value) return '#fff'
  return props.theme.sentBubbleColor
})

const textColor = computed(() => {
  if (!isMe.value) return 'black'
  return 'white'
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
const handleReply = () => chatActions?.reply(props.message)
const handleAddReaction = (payload: { messageId: number | string; emoji: string }) =>
  chatActions?.addReaction({ messageId: Number(payload.messageId), emoji: payload.emoji })
const handlePin = (messageId: number) => chatActions?.pin(messageId)

const reactionEmojis = computed(() => {
  if (!props.message.reactions) return []
  return Object.keys(props.message.reactions)
})

const totalReactions = computed(() => {
  if (!props.message.reactions) return 0
  return Object.values(props.message.reactions).reduce((acc, val) => {
    if (Array.isArray(val)) return acc + val.length
    if (typeof val === 'number') return acc + val
    return acc
  }, 0)
})
</script>

<template>
  <MessageReplyContext
    v-if="message.isReply"
    :reply="message"
    @scrollToReplied="chatActions?.scrollToMessage(message.replyToId!)"
  />

  <div
    ref="messageWrapperRef"
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
        <img src="https://ui-avatars.com/api/?name=User&background=random" alt="Avatar" class="w-full h-full object-cover" />
      </div>
      <div v-else-if="!isMe" class="w-10"></div>

      <div class="flex items-center max-w-[475px]" :class="{ 'flex-row': !isMe, 'flex-row-reverse': isMe }">
        <div
          v-tooltip.left="getDisplayTime(message.time)"
          class="relative flex flex-col overflow-visible"
          :class="{ 'highlighted-message': props.isHighlighted && isVisible }"
        >
          <span v-if="message.isPinned" class="text-[10px] absolute -top-5 right-0" :style="{ color: theme.timestampColor }">Przypięta</span>
          <PinIcon v-if="message.isPinned" :size="20" class="absolute z-99 rotate-45 top-0 right-0 text-red-500 transform translate-x-1/4 -translate-y-1/4" />

          <ChatMessageEmoji v-if="isEmojiOnly(message.content)" :message="message" />
          <ChatMessageCall v-else-if="isAnyCallType(message)" :message="message" :is-me="isMe" />
          <ChatMessageImage v-else-if="isImageMessage(message) || isGifMessage(message)" :message="message" :is-me="isMe" @open-lightbox="chatActions?.openLightbox($event)" />
          <ChatMessageAudio v-else-if="isAudioMessage(message)" :message="message" :box-id="message.chatId" :bubble-color="bubbleColor" />
          <ChatMessageFile v-else-if="isFileMessage(message)" :message="message" :is-me="isMe" :box-id="message.chatId" />
          <ChatMessageVideo v-else-if="isVideoMessage(message)" :message="message" :box-id="message.chatId" />
          <ChatMessageLink v-else-if="isLinkMessage(message)" :message="message" />
          <ChatMessagePostLink v-else-if="isPostLinkMessage(message)" :message="message" />
          <ChatMessageContent v-else-if="isTextMessage(message)" :message="message" :bubble-radius-class="bubbleRadiusClass" :bubble-color="bubbleColor" :text-color="textColor" />

          <div
            v-if="reactionEmojis.length > 0 && !isAnyCallType(message)"
            @click.stop="openReactionsPanel"
            class="absolute -bottom-2 right-0 translate-y-0 cursor-pointer bg-white rounded-full px-1.5 py-0.5 flex items-center justify-center w-[20px] h-[18px] shadow-md border border-gray-100 z-10"
          >
            <span class="text-xs leading-none">{{ reactionEmojis[reactionEmojis.length - 1] }}</span>
            <span v-if="totalReactions > 1" class="text-[9px] font-bold text-gray-500 ml-0.5">{{ totalReactions }}</span>
          </div>
        </div>

        <MessageReactions
          v-if="!isAnyCallType(message)"
          :message-id="message.id"
          :reactions="message.reactions"
          :is-me="isMe"
          @add-reaction="handleAddReaction"
          @open-panel="openReactionsPanel"
          @reply="handleReply"
          @pin="handlePin"
        />
      </div>
    </div>

    <!-- Pasek statusów pod wiadomością -->
    <div
      :class="{ ' h-4 mt-1 ': displayReadBy.visible.length > 0 || displayReadBy.hiddenCount > 0 || displayTimeAgo }"
      class="h-0 transition-all duration-300 flex justify-end w-full relative"
    >
      <div v-if="!(displayReadBy.visible.length > 0 || displayReadBy.hiddenCount > 0)" :style="{ color: props.theme.timestampColor }" class="text-[12px]">
        {{ displayTimeAgo }}
      </div>
      <TransitionGroup tag="div" class="flex justify-end items-center overflow-visible p-1" @enter="onAvatarEnter" @leave="onAvatarLeave">
        <div v-if="displayReadBy.hiddenCount > 0" key="counter" class="w-3.5 h-3.5 rounded-full ring-2 ring-white bg-gray-200 text-[8px] flex items-center justify-center text-gray-500 font-bold relative z-0">
          +{{ displayReadBy.hiddenCount }}
        </div>
        <img v-for="(userId, i) in displayReadBy.visible" :key="userId" :data-avatar-userid="userId" :src="getUserAvatar(userId)" v-tooltip="getTooltipText(userId)" class="w-3.5 h-3.5 rounded-full ring-2 ring-white relative object-cover shadow-sm select-none border-white bg-gray-200" :style="{ zIndex: i + 1 }" alt="seen" />
      </TransitionGroup>
    </div>
  </div>

  <!-- Reakcje Modal -->
  <BaseModal v-if="showReactionsPanel" @close="showReactionsPanel = false" title="Reakcje">
    <div class="p-4 max-h-[400px] overflow-y-auto min-w-[280px]">
      <div v-if="!message.reactions || Object.keys(message.reactions).length === 0" class="text-center text-gray-500 py-4">Brak reakcji</div>
      <div v-else class="space-y-4">
        <div v-for="(userIds, emoji) in message.reactions" :key="emoji" class="border-b border-gray-100 pb-3 last:border-0 last:pb-0">
          <div class="flex items-center space-x-2 mb-2">
            <span class="text-2xl">{{ emoji }}</span>
            <span class="text-xs font-semibold text-gray-500 bg-gray-100 px-2 py-0.5 rounded-full">{{ Array.isArray(userIds) ? userIds.length : 1 }}</span>
          </div>
          <div class="space-y-2 pl-2">
            <template v-if="Array.isArray(userIds)">
              <div v-for="userId in userIds" :key="userId" class="flex items-center space-x-3">
                <img :src="getUserAvatar(userId)" class="w-8 h-8 rounded-full object-cover shadow-sm bg-gray-100 border border-gray-200" alt="avatar" />
                <span class="text-sm font-medium text-gray-700">{{ getUserName(userId) }}</span>
              </div>
            </template>
            <template v-else>
              <div class="flex items-center space-x-3">
                <img :src="getUserAvatar(String(userIds))" class="w-8 h-8 rounded-full object-cover shadow-sm bg-gray-100 border border-gray-200" alt="avatar" />
                <span class="text-sm font-medium text-gray-700">{{ getUserName(String(userIds)) }}</span>
              </div>
            </template>
          </div>
        </div>
      </div>
    </div>
  </BaseModal>
</template>

<style scoped>
.group-hover\:flex { display: none; }
.group:hover .group-hover\:flex { display: flex; }
@keyframes pulsowanie {
  0% { transform: scale(1); }
  50% { transform: scale(1.1); }
  100% { transform: scale(1); }
}
.highlighted-message {
  animation: pulsowanie 0.5s ease-in-out;
  border-radius: 16px;
}
.v-move { transition: transform 0.5s ease; }
</style>
