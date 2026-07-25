<script setup lang="ts">
import { ref, computed, nextTick, onMounted, watch, provide } from 'vue'
import { storeToRefs } from 'pinia'
import { useVirtualizer } from '@tanstack/vue-virtual'
import { useQuery } from '@vue/apollo-composable'
import gql from 'graphql-tag'

import MultiMediaLightbox from './MediaLightbox.vue'
import MessageBoxHeader from '@/components/chat/messageBox/MessageBoxHeader.vue'
import MessageBoxFooter from '@/components/chat/messageBox/footer/index.vue'
import MessageItem from '@/components/chat/messageItem/MessageItem.vue'
import ChatStartHeader from '@/components/chat/messageBox/ChatStartHeader.vue'
import TypingIndicator from './TypingIndicator.vue'

import { useConversationsStore } from '@/stores/conversations'
import { useMessageGrouper } from '@/composables/chat/useMessageGrouper'
import { useLightbox } from '@/composables/ui/useLightbox'
import { useFlipAnimation } from '@/composables/ui/useFlipAnimation'
import { useChatDrop } from '@/composables/chat/useChatDrop'

import type { Theme } from '@/types/Theme'
import type { Message, ImageMessage, VideoMessage } from '@/types/Message'

const emit = defineEmits(['open-modal', 'back-to-list', 'show-info', 'load-older'])
const props = withDefaults(
  defineProps<{
    boxId?: string | number
    mode?: 'card' | 'full'
    messages?: Message[]
    hideHeaderIcons?: boolean
  }>(),
  {
    mode: 'card',
    hideHeaderIcons: false,
  },
)

const convStore = useConversationsStore()
const { themes, selectedTheme, currentUserUuid } = storeToRefs(convStore)

const localMessages = ref<Message[]>([])

const messagesList = computed((): Message[] => {
  if (props.boxId) {
    return convStore.getMessagesByChatId(props.boxId) as Message[]
  }
  return props.messages?.length ? props.messages : localMessages.value
})

const isLoadingOlder = ref(false)
const showStartHeader = computed(() => messagesList.value.length > 0)
const msgIndex = (virtualIndex: number) => showStartHeader.value ? virtualIndex - 1 : virtualIndex

// ==========================================
// DRAG & DROP COMPOSABLE (GLOBAL + TARGET)
// ==========================================
const { isDragging, reset: resetDrag, handleDrop } = useChatDrop((files) => {
  Array.from(files).forEach((file) => {
    const url = URL.createObjectURL(file)
    const baseMsg = { sender: 'me', time: Date.now() }

    const pushMsg = (msg: Message) => {
      if (props.boxId) convStore.addMessage(props.boxId, msg)
      else localMessages.value.push({ id: Date.now() + Math.random(), ...msg })
    }

    if (file.type.startsWith('image/')) {
      pushMsg({ ...baseMsg, type: 'image', content: 'Wysłano obraz', imageUrl: url } as ImageMessage)
    } else if (file.type.startsWith('video/')) {
      pushMsg({ ...baseMsg, type: 'video', content: file.name, videoUrl: url } as VideoMessage)
    } else {
      pushMsg({ ...baseMsg, type: 'file', content: `Plik: ${file.name}`, fileUrl: url, fileName: file.name, fileSize: file.size } as Message)
    }
  })

  scrollToBottom('smooth')
})

// ==========================================
// WIRTUALIZATOR TANSTACK
// ==========================================
const chatContainer = ref<HTMLElement | null>(null)
const showScrollToBottomBtn = ref(false)

const virtualizerOptions = computed(() => ({
  count: messagesList.value.length + (showStartHeader.value ? 1 : 0),
  getScrollElement: () => chatContainer.value,
  estimateSize: (index: number) => (showStartHeader.value && index === 0 ? 240 : 80),
  overscan: 30,
  getItemKey: (index: number) => {
    if (showStartHeader.value) {
      if (index === 0) return 'chat-start-header'
      return messagesList.value[index - 1]?.id ?? index
    }
    return messagesList.value[index]?.id ?? index
  },
  shouldAdjustScrollPositionOnItemSizeChange: (item) => {
    if (!chatContainer.value) return false
    const vItems = virtualizer.value.getVirtualItems()
    if (!vItems.length) return false

    const firstVisible = vItems.find((i) => i.start + i.size > chatContainer.value!.scrollTop)
    if (!firstVisible) return false

    return item.index <= firstVisible.index
  },
}))

const virtualizer = useVirtualizer(virtualizerOptions)

const handleScroll = (e: Event) => {
  const target = e.target as HTMLElement
  if (target.scrollTop <= 10 && !isLoadingOlder.value) {
    emit('load-older', props.boxId)
  }
  const distanceFromBottom = target.scrollHeight - target.scrollTop - target.clientHeight
  showScrollToBottomBtn.value = distanceFromBottom > 300
}

const lastRead = computed(() => {
  return convStore.lastReadMaps[props.boxId] || {}
})

const { capturePositions, onAvatarEnter, onAvatarLeave } = useFlipAnimation(chatContainer)
provide('flip-animation', { onAvatarEnter, onAvatarLeave })

watch(
  () => convStore.lastReadMaps[props.boxId],
  () => capturePositions(),
  { deep: true, flush: 'sync' }
)

onMounted(() => {
  scrollToBottom('auto')
  setTimeout(() => scrollToBottom('auto'), 100)
  window.addEventListener('keydown', (e) => {
    if (e.key === 'Escape' && isLightboxOpen.value) isLightboxOpen.value = false
  })
})

watch(
  () => props.messages,
  (newVal, oldVal) => {
    if (newVal) {
      const isAppended = oldVal && newVal.length > oldVal.length
      localMessages.value = [...newVal]
      if (isAppended || !oldVal) {
        scrollToBottom('auto')
      }
    }
  },
  { immediate: true, deep: true },
)

watch(
  () => messagesList.value,
  (newVal, oldVal) => {
    if (newVal) {
      const isAppended = oldVal && newVal.length > oldVal.length
      if (isAppended || !oldVal || newVal.length !== oldVal.length) {
        scrollToBottom('auto')
      }
    }
  },
  { immediate: true, deep: true },
)

const boxTheme = computed(() => {
  if (!props.boxId) return selectedTheme.value
  const chatId = props.boxId
  const settings = convStore.settings.find((x) => String(x.chatId) === String(chatId))
  if (!settings || settings.themeId === undefined) return selectedTheme.value
  if (typeof settings.themeId === 'number') {
    return themes.value[settings.themeId] || selectedTheme.value
  }
  return themes.value.find((t: Theme) => t.id === settings.themeId) || selectedTheme.value
})

function scrollToBottom(behavior: 'auto' | 'smooth' = 'auto') {
  const performScroll = () => {
    const total = messagesList.value.length
    if (total > 0) {
      const targetIndex = showStartHeader.value ? total : total - 1
      virtualizer.value.scrollToIndex(targetIndex, { align: 'end', behavior })
    }
  }
  nextTick(performScroll)
  setTimeout(performScroll, 50)
}

function scrollToMessage(messageId: number) {
  const index = messagesList.value.findIndex((m) => m.id === messageId)
  if (index !== -1) {
    const targetIndex = showStartHeader.value ? index + 1 : index
    virtualizer.value.scrollToIndex(targetIndex, { align: 'center', behavior: 'smooth' })

    setTimeout(() => {
      highlightedMessageId.value = messageId
      setTimeout(() => {
        highlightedMessageId.value = null
      }, 2000)
    }, 500)
  }
}

const highlightedMessageId = ref<number | null>(null)
const { getDisplayTime, getMessagePositionInGroup } = useMessageGrouper(messagesList)
const { isLightboxOpen, currentMediaIndex, filteredMedia, openLightbox } = useLightbox(messagesList)

const handlePin = (messageId: number | string) => {
  if (props.boxId) {
    convStore.togglePinMessage(props.boxId, messageId)
  }
}

const replyTarget = ref<Message | null>(null)

const isRecipientTyping = computed(() => {
  if (!props.boxId) return false
  return !!convStore.typingUsers[String(props.boxId)]
})

watch(isRecipientTyping, (typing) => {
  if (typing) {
    nextTick(() => scrollToBottom('smooth'))
  }
})

const handleAddMessage = (msg: Message) => {
  if (props.boxId) {
    const newMessage = {
      ...msg,
      isReply: !!replyTarget.value,
      replyToId: replyTarget.value?.id,
      replyToSender: replyTarget.value?.sender === 'me' ? 'Ty' : (convStore.chats.find((c) => String(c.id) === String(props.boxId))?.name || 'Użytkownik'),
      replyToSenderId: replyTarget.value?.sender === 'me' ? currentUserUuid.value : String(props.boxId),
      replyToContentSnippet: replyTarget.value?.content?.slice(0, 100),
    }
    convStore.addMessage(props.boxId, newMessage)
    replyTarget.value = null
  } else {
    localMessages.value.push(msg)
  }
  nextTick(() => scrollToBottom('smooth'))
}

const handleAddReaction = (messageId: number, emoji: string) => {
  if (props.boxId) {
    convStore.addReaction(props.boxId, messageId, emoji)
  } else {
    const target = localMessages.value.find((m) => m.id === messageId)
    if (target) {
      if (!target.reactions) target.reactions = []
      target.reactions.push(emoji)
    }
  }
}

provide('chatActions', {
  openLightbox,
  reply: (message: Message) => {
    replyTarget.value = message
  },
  addReaction: (payload: { messageId: number; emoji: string }) => {
    handleAddReaction(payload.messageId, payload.emoji)
  },
  openModal: (type: 'CHANGE_E' | 'CHANGE_NICKNAME' | 'CHANGE_THEME') => {
    emit('open-modal', type)
  },
  scrollToMessage,
  pin: handlePin,
})

// ==========================================
// APOLLO GRAPHQL CACHE
// ==========================================
const GET_USER_HEADER_INFO = gql`
  query GetUserById($userId: ID!) {
    getUserById(userId: $userId) {
      id
      firstName
      lastName
      avatarId
    }
  }
`

const { result: apolloUserResult } = useQuery(
  GET_USER_HEADER_INFO,
  () => ({ userId: String(props.boxId) }),
  () => ({
    enabled: !!props.boxId,
    fetchPolicy: 'cache-first'
  })
)

const fetchedUser = computed(() => apolloUserResult.value?.getUserById)

const headerTitle = computed(() => {
  if (!props.boxId) return 'Czat'
  const boxIdStr = String(props.boxId)

  const chat = convStore.chats.find((c) => String(c.id) === boxIdStr)
  if (chat) return chat.name

  if (fetchedUser.value) {
    return `${fetchedUser.value.firstName} ${fetchedUser.value.lastName}`.trim()
  }

  return `Czat ${props.boxId}`
})

const headerAvatar = computed(() => {
  if (!props.boxId) return ''
  const boxIdStr = String(props.boxId)

  const chat = convStore.chats.find((c) => String(c.id) === boxIdStr)
  if (chat && chat.avatarUrl) return chat.avatarUrl

  if (fetchedUser.value && fetchedUser.value.avatarId) {
    return fetchedUser.value.avatarId
  }

  const baseUrl = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'
  return `${baseUrl}/api/users/avatar/default-avatar.svg`
})

const isFull = computed(() => props.mode === 'full')
const lastMessageId = computed(() => {
  const allMsgs = messagesList.value
  return allMsgs.length > 0 ? allMsgs[allMsgs.length - 1].id : null
})

defineExpose({ scrollToMessage })
</script>

<template>
  <div
    :class="[
      isFull
        ? 'relative flex-1 flex flex-col h-full w-full'
        : 'flex items-center w-[328px] box-content relative justify-center ',
    ]"
  >
    <div
      :class="[
        isFull
          ? 'w-full h-full rounded-t-xl shadow-none'
          : 'w-full max-w-[328px] h-[455px] rounded-xl shadow-2xl',
        'bg-theme-bg-secondary flex flex-col relative transition-all duration-300 overflow-hidden',
      ]"
    >
      <MessageBoxHeader
        :title="headerTitle"
        :avatar-url="headerAvatar"
        :users="[headerTitle]"
        :boxId="boxId ?? 1"
        :hideIcons="hideHeaderIcons"
        :themes="boxTheme"
        @back="emit('back-to-list')"
        @show-info="emit('show-info')"
      />

      <!-- Kontener główny obejmujący listę wiadomości ORAZ stopkę -->
      <div class="relative flex-1 flex flex-col min-h-0">
        <main
          ref="chatContainer"
          @scroll="handleScroll"
          class="relative flex-1 flex flex-col overflow-y-auto custom-scrollbar bg-theme-bg min-h-0"
          :style="[
            boxTheme?.backgroundImage
              ? {
                  backgroundImage: `url(${boxTheme.backgroundImage})`,
                  backgroundSize: 'cover',
                  backgroundPosition: 'center',
                }
              : {},
            { overflowAnchor: 'none' },
          ]"
        >
          <div
            v-if="isLoadingOlder"
            class="absolute top-0 left-0 w-full px-4 py-3 text-center z-10 transition-opacity duration-300"
          >
            <div
              class="inline-flex items-center justify-center space-x-2 text-theme-text-secondary bg-theme-bg/90 backdrop-blur-md shadow-sm px-4 py-1.5 rounded-full border border-theme-border/50"
            >
              <div
                class="w-4 h-4 border-2 border-theme-border border-t-theme-primary rounded-full animate-spin"
              ></div>
              <span class="text-xs font-semibold uppercase tracking-wide"
                >Ładowanie historii...</span
              >
            </div>
          </div>

          <div
            class="w-full relative shrink-0 mt-auto pb-4"
            :style="{ height: `${virtualizer.getTotalSize()}px` }"
          >
            <div
              v-for="virtualItem in virtualizer.getVirtualItems()"
              :key="virtualItem.key"
              :ref="virtualizer.measureElement"
              :data-index="virtualItem.index"
              class="absolute top-0 left-0 w-full"
              :style="{ transform: `translateY(${virtualItem.start}px)` }"
            >
              <div v-if="showStartHeader && virtualItem.index === 0" class="px-2">
                <ChatStartHeader
                  :name="headerTitle"
                  :avatar-url="headerAvatar"
                  :subtitle="`Rozpoczęto konwersację z użytkownikiem ${headerTitle}`"
                />
              </div>

              <div v-else class="px-2">
                <div
                  v-if="getDisplayTime(msgIndex(virtualItem.index))"
                  class="text-[11px] font-medium text-center my-3 select-none uppercase tracking-wide opacity-80"
                  :style="{ color: boxTheme.timestampColor }"
                >
                  {{ getDisplayTime(msgIndex(virtualItem.index)) }}
                </div>

                <MessageItem
                  :message="messagesList[msgIndex(virtualItem.index)]"
                  :theme="boxTheme"
                  :metadata="{
                    position: getMessagePositionInGroup(msgIndex(virtualItem.index)),
                    isLatest: messagesList[msgIndex(virtualItem.index)].id === lastMessageId,
                  }"
                  :id="`msg-${boxId ?? '0'}-${messagesList[msgIndex(virtualItem.index)].id}`"
                  :isHighlighted="messagesList[msgIndex(virtualItem.index)].id === highlightedMessageId"
                  :last-read-map="lastRead"
                />
              </div>
            </div>
          </div>

          <TypingIndicator v-if="isRecipientTyping" />
        </main>

        <MessageBoxFooter
          :reply="replyTarget"
          :boxId="boxId"
          :themes="boxTheme"
          @clearReply="replyTarget = null"
          @add-message="handleAddMessage"
        />

        <!-- Przycisk przewijania w dół -->
        <transition name="fade-slide">
          <button
            v-if="showScrollToBottomBtn"
            @click="scrollToBottom()"
            class="absolute bottom-16 left-1/2 -translate-x-1/2 p-3 z-40 rounded-full shadow-lg flex items-center justify-center text-blue-500 bg-theme-bg hover:bg-blue-600 transition-colors backdrop-blur-md"
            aria-label="Przewiń na dół"
          >
            <svg
              class="w-5 h-5"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
              xmlns="http://www.w3.org/2000/svg"
            >
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2.5"
                d="M19 14l-7 7m0 0l-7-7m7 7V3"
              ></path>
            </svg>
          </button>
        </transition>

        <!-- ========================================== -->
        <!-- WYSINOWANY TEMPLATE DRAG OVERLAY          -->
        <!-- ========================================== -->
        <Transition
          enter-active-class="transition duration-200 ease-out"
          enter-from-class="opacity-0 scale-95"
          enter-to-class="opacity-100 scale-100"
          leave-active-class="transition duration-150 ease-in"
          leave-from-class="opacity-100 scale-100"
          leave-to-class="opacity-0 scale-95"
        >
          <div
            v-if="isDragging"
            @drop.prevent="handleDrop"
            @dragover.prevent
            class="absolute inset-0 z-50 flex flex-col items-center justify-between p-6 bg-white/90 dark:bg-slate-900/95 backdrop-blur-md text-center pointer-events-auto transition-all shadow-2xl"
          >
            <!-- Środek z nagłówkami i ikoną -->
            <div class="flex-1 flex flex-col items-center justify-center pointer-events-none select-none">


              <h3 class="text-base font-extrabold text-slate-800 dark:text-slate-100 max-w-[220px] leading-snug">
                Upuść pliki tutaj
              </h3>

              <p class="text-xs text-slate-500 dark:text-slate-400 mt-2 font-medium">
                Maksymalnie 100 MB
              </p>
            </div>

          </div>
        </Transition>
      </div>

      <MultiMediaLightbox
        v-if="isLightboxOpen"
        v-model="isLightboxOpen"
        :media="filteredMedia"
        :startIndex="currentMediaIndex"
      />
    </div>
  </div>
</template>
