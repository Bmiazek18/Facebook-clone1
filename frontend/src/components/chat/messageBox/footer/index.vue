<script setup lang="ts">
import { ref, computed, toRef } from 'vue'
import { useConversationsStore } from '@/stores/conversations'
import { Emoji, EmojiIndex } from 'emoji-mart-vue-fast/src'
import data from 'emoji-mart-vue-fast/data/all.json'

import 'floating-vue/dist/style.css'

import ImageOutlineIcon from 'vue-material-design-icons/ImageOutline.vue'
import StickerEmojiIcon from 'vue-material-design-icons/StickerEmoji.vue'
import EmoticonHappyOutlineIcon from 'vue-material-design-icons/EmoticonHappyOutline.vue'
import SendIcon from 'vue-material-design-icons/Send.vue'
import MicrophoneIcon from 'vue-material-design-icons/Microphone.vue'
import ImageMultipleIcon from 'vue-material-design-icons/ImageMultiple.vue'
import FileIcon from 'vue-material-design-icons/File.vue'
import PollIcon from 'vue-material-design-icons/Poll.vue'

import GifBox from '@/components/common/GifBox.vue'
import LazyEmojiPicker from '@/components/common/LazyEmojiPicker.vue'
import VoiceRecorder from './VoiceRecorder.vue'
import LikeButton from './LikeButton.vue'
import CreateChatPollModal from '../modals/CreateChatPollModal.vue'
import ChatAttachmentTray from './ChatAttachmentTray.vue'
import ChatMentionAutocomplete from './ChatMentionAutocomplete.vue'
import { useChatTypingStatus } from '@/composables/chat/useChatTypingStatus'
import { useChatMediaUpload } from '@/composables/chat/useChatMediaUpload'

import type { Message } from '@/types/Message'
import type { Theme } from '@/types/Theme'

// --- TYPY I STAŁE ---
const EMOJI_REGEX_SPLIT = /(\ud83c[\udf00-\udfff]|\ud83d[\udc00-\ude4f]|\ud83d[\ude80-\udeff]|\ud83e[\udd00-\uddff]|[\u2600-\u27bf])/g
const EMOJI_REGEX_TEST = /(\ud83c[\udf00-\udfff]|\ud83d[\udc00-\ude4f]|\ud83d[\ude80-\udeff]|\ud83e[\udd00-\uddff]|[\u2600-\u27bf])/

const emojiIndex = new EmojiIndex(data)
const convStore = useConversationsStore()

const props = defineProps<{
  reply: Message | null
  boxId?: string | number
  themes?: Theme
}>()

const emit = defineEmits<{
  'add-message': [message: Message]
  clearReply: []
}>()

// --- REFS / STANY ---
const visualLayer = ref<HTMLElement | null>(null)
const fileInput = ref<HTMLInputElement | null>(null)
const gifDropdown = ref<{ hide: () => void; show: () => void } | null>(null)
const plusDropdown = ref<{ hide: () => void } | null>(null)

const newMessage = ref('')

const documentFileInput = ref<HTMLInputElement | null>(null)
const selectedDocumentFiles = ref<File[]>([])

const selectDocumentFile = () => documentFileInput.value?.click()

const handleDocumentUpload = (event: Event) => {
  const target = event.target as HTMLInputElement
  const files = target.files
  if (!files) return

  selectedGifUrl.value = null
  Array.from(files).forEach((file) => {
    selectedDocumentFiles.value.push(file)
  })
}

const removeDocumentFile = (index: number) => {
  selectedDocumentFiles.value.splice(index, 1)
}

const isLink = (str: string) => {
  const pattern = /^(https?:\/\/)?(www\.)?[-a-zA-Z0-9@:%._\+~#=]{1,256}\.[a-zA-Z0-9()]{1,6}\b([-a-zA-Z0-9()@:%_\+.~#?&//=]*)$/i;
  return pattern.test(str.trim());
}

// --- TYPING STATUS COMPOSABLE ---
const { onFocus, onBlur } = useChatTypingStatus(toRef(props, 'boxId'), newMessage)

const selectedImageUrls = ref<string[]>([])
const selectedImageFiles = ref<File[]>([])
const selectedGifUrl = ref<string | null>(null)
const isVoiceRecording = ref(false)
const showVoiceRecorder = ref(false)

// --- COMPUTED ---

const parsedTokens = computed(() => {
  if (!newMessage.value) return []
  const text = newMessage.value.replace(/\[@([a-zA-Z0-9-]+)\]/g, (match, uId) => {
    const member = groupMembersList.value.find((m) => String(m.id) === String(uId))
    return member ? `@${member.name}` : `@${uId}`
  })
  return text
    .split(EMOJI_REGEX_SPLIT)
    .filter(Boolean)
    .map((part) => ({
      value: part,
      isEmoji: EMOJI_REGEX_TEST.test(part),
    }))
})

// Wyciągnięte wyszukiwanie nadawcy odpowiedzi
const replySenderName = computed(() => {
  if (!props.reply) return ''
  if (props.reply.sender === 'me') return 'Ty'

  const chat = convStore.chats.find((c) => String(c.id) === String(props.boxId))
  return chat?.name || 'Użytkownik'
})

const localSelectedEmoji = computed(() => {
  if (props.boxId) {
    const setting = convStore.settings.find((x) => String(x.chatId) === String(props.boxId))
    if (setting?.emoji) return setting.emoji
  }
  return (convStore.selectedEmoji as string) || '👍'
})

const hasMedia = computed(
  () => selectedImageUrls.value.length > 0 || selectedDocumentFiles.value.length > 0 || newMessage.value.trim().length > 0 || !!selectedGifUrl.value
)

// --- MEDIA UPLOAD COMPOSABLE ---
const { uploadFile, uploadGifFromUrl } = useChatMediaUpload()

// --- METODY ---

const handleScroll = (e: Event) => {
  if (visualLayer.value) {
    visualLayer.value.scrollLeft = (e.target as HTMLInputElement).scrollLeft
  }
}

const showPollModal = ref(false)

const openPollModal = () => {
  plusDropdown.value?.hide()
  showPollModal.value = true
}

const handlePollSubmit = (pollData: any) => {
  const pollId = `poll_${Date.now()}`
  const pollMessage: any = {
    id: pollId,
    chatId: String(props.boxId || convStore.activeChatId),
    type: 'poll',
    sender: 'me',
    content: pollData.question,
    time: Date.now(),
    timestamp: new Date().toISOString(),
    pollData: {
      question: pollData.question,
      options: pollData.options,
      allowMultiple: pollData.allowMultiple,
      allowAddOption: pollData.allowAddOption,
    },
  }

  emit('add-message', pollMessage)

  // Broadcast poll creation to participants via MQTT
  const chatId = props.boxId || convStore.activeChatId
  if (chatId) {
    const chat = convStore.chats.find((c) => String(c.id) === String(chatId))
    const cleanSenderId = String(convStore.currentUserUuid).replace(/^user_/, '')
    const participantIds: string[] = []
    if (chat && chat.groupMembers) {
      chat.groupMembers.forEach((m: any) => {
        const pId = String(m.userId || m.id).replace(/^user_/, '')
        if (pId) participantIds.push(pId)
      })
    } else {
      const cleanChatId = String(chatId).replace(/^user_/, '')
      participantIds.push(cleanChatId)
    }
    if (!participantIds.includes(cleanSenderId)) {
      participantIds.push(cleanSenderId)
    }

    const convId = convStore.getSymmetricConversationId(chatId)
    participantIds.forEach((pId) => {
      convStore.publishMqtt(`chat/messages/user/${pId}`, {
        type: 'poll',
        conversationId: convId,
        messageId: pollId,
        senderId: cleanSenderId,
        text: pollData.question,
        pollData: pollMessage.pollData,
        participantIds: participantIds,
      }, { qos: 1 })
    })
  }
}

const inputRef = ref<HTMLInputElement | null>(null)
const mentionQuery = ref<string | null>(null)
const mentionStartIndex = ref<number>(-1)
const selectedMentionIndex = ref<number>(0)

const currentChat = computed(() => {
  const cId = props.boxId || convStore.activeChatId
  return convStore.chats.find((c) => String(c.id) === String(cId))
})

const isGroupChat = computed(() => {
  return currentChat.value?.type === 'group' || (currentChat.value?.groupMembers && currentChat.value.groupMembers.length > 0)
})

const groupMembersList = computed(() => {
  const members = currentChat.value?.groupMembers || []
  return members.map((m: any) => ({
    id: String(m.id || m.userId).replace(/^user_/, ''),
    name: m.name || m.username || 'Użytkownik',
    avatar: m.avatar || m.avatarUrl || '/default-avatar.png',
  }))
})

const matchingMentionMembers = computed(() => {
  if (mentionQuery.value === null) return []
  const q = mentionQuery.value.trim().toLowerCase()
  const list = groupMembersList.value.filter((m) => {
    return m.name.toLowerCase().includes(q)
  })

  const res = [...list]
  if (!q || 'wszyscy'.includes(q) || 'everyone'.includes(q) || 'all'.includes(q)) {
    res.unshift({
      id: 'all',
      name: 'Wszyscy (@wszyscy)',
      avatar: '/default-avatar.png',
    })
  }
  return res
})

function handleInputKeydown(e: KeyboardEvent) {
  if (mentionQuery.value !== null && matchingMentionMembers.value.length > 0) {
    if (e.key === 'ArrowDown') {
      e.preventDefault()
      selectedMentionIndex.value = (selectedMentionIndex.value + 1) % matchingMentionMembers.value.length
      return
    }
    if (e.key === 'ArrowUp') {
      e.preventDefault()
      selectedMentionIndex.value = (selectedMentionIndex.value - 1 + matchingMentionMembers.value.length) % matchingMentionMembers.value.length
      return
    }
    if (e.key === 'Enter' || e.key === 'Tab') {
      e.preventDefault()
      selectMention(matchingMentionMembers.value[selectedMentionIndex.value])
      return
    }
    if (e.key === 'Escape') {
      mentionQuery.value = null
      return
    }
  }

  if (e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault()
    sendMessage(newMessage.value)
  }
}

function handleInputUpdate(e: Event) {
  const target = e.target as HTMLInputElement
  const val = target.value
  const cursor = target.selectionStart || val.length
  const textBefore = val.slice(0, cursor)
  const lastAt = textBefore.lastIndexOf('@')

  if (lastAt !== -1) {
    const textAfterAt = textBefore.slice(lastAt + 1)
    if (!textAfterAt.includes(' ') && !textAfterAt.includes('\n')) {
      mentionQuery.value = textAfterAt
      mentionStartIndex.value = lastAt
      selectedMentionIndex.value = 0
      return
    }
  }
  mentionQuery.value = null
}

function selectMention(member: { id: string; name: string }) {
  if (mentionStartIndex.value === -1) return
  const val = newMessage.value
  const before = val.slice(0, mentionStartIndex.value)
  const queryLen = mentionQuery.value ? mentionQuery.value.length : 0
  const after = val.slice(mentionStartIndex.value + 1 + queryLen)

  const token = member.id === 'all' ? '@wszyscy ' : `[@${member.id}] `
  newMessage.value = before + token + after
  mentionQuery.value = null

  setTimeout(() => {
    if (inputRef.value) {
      inputRef.value.focus()
      const newPos = before.length + token.length
      inputRef.value.setSelectionRange(newPos, newPos)
    }
  }, 20)
}

const openGifFromPlus = () => {
  plusDropdown.value?.hide()
  setTimeout(() => gifDropdown.value?.show(), 50)
}

const clearMediaSelection = () => {
  selectedImageUrls.value.forEach((url) => URL.revokeObjectURL(url))
  selectedImageUrls.value = []
  selectedImageFiles.value = []
  selectedGifUrl.value = null
  selectedDocumentFiles.value = []
}

const removeImage = (index: number) => {
  URL.revokeObjectURL(selectedImageUrls.value[index])
  selectedImageUrls.value.splice(index, 1)
  selectedImageFiles.value.splice(index, 1)
}

const selectImage = () => fileInput.value?.click()

const handleImageUpload = (event: Event) => {
  const target = event.target as HTMLInputElement
  const files = target.files
  if (!files) return

  selectedGifUrl.value = null
  Array.from(files).forEach((file) => {
    if (file.type.startsWith('image/')) {
      selectedImageUrls.value.push(URL.createObjectURL(file))
      selectedImageFiles.value.push(file)
    }
  })
}

const handleExposedFiles = (files: FileList | File[]) => {
  selectedGifUrl.value = null
  Array.from(files).forEach((file) => {
    if (file.type.startsWith('image/')) {
      selectedImageUrls.value.push(URL.createObjectURL(file))
      selectedImageFiles.value.push(file)
    } else {
      selectedDocumentFiles.value.push(file)
    }
  })
}

defineExpose({
  handleExposedFiles
})

// Fabryka wiadomości
const createAndEmitMessage = (payload: Partial<Message>) => {
  const baseMessage: Message = {
    id: Date.now(),
    sender: 'me',
    time: Date.now(),
    type: 'text',
    content: '',
    iconSizeState: 'default',
    ...payload,
  }
  emit('add-message', baseMessage)
}

const handleGifSelect = async (gifUrl: string) => {
  clearMediaSelection()
  gifDropdown.value?.hide()
  try {
    const serverUrl = await uploadGifFromUrl(gifUrl)
    createAndEmitMessage({
      type: 'gif',
      content: 'Wysłano GIF',
      imageUrl: serverUrl,
    })
  } catch (err) {
    console.error('Błąd podczas przesyłania GIF-a:', err)
  }
}

const handleAudioRecorded = async (payload: { audioUrl: string; duration: number; mimeType: string }) => {
  try {
    const response = await fetch(payload.audioUrl)
    const blob = await response.blob()
    const extension = payload.mimeType.includes('mp4') ? 'm4a' : 'webm'
    const file = new File(
      [blob],
      `voice-recording-${Date.now()}.${extension}`,
      { type: payload.mimeType },
    )

    const minioUrl = await uploadFile(file)

    createAndEmitMessage({
      type: 'audio',
      content: `Wiadomość głosowa (${Math.max(payload.duration, 1)}s)`,
      audioUrl: minioUrl,
      duration: Math.max(payload.duration, 1),
    })
  } catch (err) {
    console.error('Błąd podczas przesyłania wiadomości głosowej:', err)
  }
}

const handleSendLike = (sizeState: 'default' | 'small' | 'medium' | 'large') => {
  if (!hasMedia.value) {
    createAndEmitMessage({
      content: localSelectedEmoji.value,
      iconSizeState: sizeState,
      duration: sizeState === 'small' ? 2 : (sizeState === 'medium' ? 3 : (sizeState === 'large' ? 4 : 1))
    })
  }
}

const sendMessage = async (content: string) => {
  const finalContent = content.trim()

  if (!finalContent && !selectedImageFiles.value.length && !selectedGifUrl.value && !selectedDocumentFiles.value.length) return

  try {
    if (selectedImageFiles.value.length > 0) {
      const minioUrl = await uploadFile(selectedImageFiles.value[0])
      createAndEmitMessage({
        type: 'image',
        content: finalContent,
        imageUrl: minioUrl,
        mediaUrls: [minioUrl],
      })
    } else if (selectedDocumentFiles.value.length > 0) {
      const file = selectedDocumentFiles.value[0]
      const minioUrl = await uploadFile(file)
      createAndEmitMessage({
        type: 'file',
        content: finalContent || file.name,
        fileUrl: minioUrl,
        fileName: file.name,
        fileSize: file.size,
      })
    } else if (selectedGifUrl.value) {
      const serverUrl = await uploadGifFromUrl(selectedGifUrl.value)
      createAndEmitMessage({
        type: 'gif',
        content: finalContent || 'Wysłano GIF',
        imageUrl: serverUrl,
      })
    } else if (isLink(finalContent)) {
      const urlStr = finalContent.startsWith('http://') || finalContent.startsWith('https://')
        ? finalContent
        : 'https://' + finalContent;
      createAndEmitMessage({
        type: 'link',
        content: finalContent,
        linkUrl: urlStr,
        url: urlStr,
      })
    } else {
      let sizeState: 'default' | 'small' | 'medium' | 'large' = 'default'
      const emojiRegex = /[\u2700-\u27BF]|[\uE000-\uF8FF]|\uD83C[\uDC00-\uDFFF]|\uD83D[\uDC00-\uDFFF]|[\u2011-\u26FF]|\uD83E[\uDD10-\uDDFF]/g
      const cleanContent = finalContent.replace(/\s+/g, '')
      const nonEmoji = cleanContent.replace(emojiRegex, '')
      if (nonEmoji.length === 0 && cleanContent.length > 0) {
        const segmenter = new Intl.Segmenter(undefined, { granularity: 'grapheme' })
        const emojiCount = Array.from(segmenter.segment(cleanContent)).length
        if (emojiCount === 1) sizeState = 'large'
        else if (emojiCount === 2) sizeState = 'medium'
        else if (emojiCount === 3) sizeState = 'small'
      }

      createAndEmitMessage({
        type: 'text',
        content: finalContent,
        iconSizeState: sizeState,
        duration: sizeState === 'small' ? 2 : (sizeState === 'medium' ? 3 : (sizeState === 'large' ? 4 : 1))
      })
    }
  } catch (err) {
    console.error('Błąd wysyłania wiadomości:', err)
  } finally {
    newMessage.value = ''
    clearMediaSelection()
  }
}

onUnmounted(() => {
  clearMediaSelection()
})
</script>

<template>
  <footer
    class="p-2 shrink-0  shadow-md relative"
    :style="{ backgroundColor: props.themes?.footerColor }"
  >
    <Transition
      enter-active-class="transition-all duration-250 ease-out"
      enter-from-class="translate-y-[10px] opacity-0"
      enter-to-class="translate-y-0 opacity-100"
      leave-active-class="transition-all duration-250 ease-in"
      leave-from-class="translate-y-0 opacity-100"
      leave-to-class="translate-y-[10px] opacity-0"
    >
      <div
        v-if="props.reply"
        class="bg-inherit border-t border-[var(--color-theme-border,rgba(150,150,150,0.2))] py-[5px] px-2 shadow-[0_-4px_15px_rgba(0,0,0,0.03)] flex items-center justify-between gap-3 z-10"
      >
        <div class="flex flex-col flex-1 min-w-0">
          <div class="font-bold text-[14.5px] mb-[2px] truncate text-theme-text">{{ $t('chat.odpowiadaszUzytkownikowiReplysendername') }}</div>
          <div class="text-[13.5px] opacity-75 truncate text-theme-text-secondary">
            <template v-if="props.reply.type === 'text'">{{ props.reply.content }}</template>
            <template v-else-if="props.reply.type === 'image'">{{ $t('ui.image') }}</template>
            <template v-else-if="props.reply.type === 'gif'">{{ $t('ui.gif') }}</template>
            <template v-else-if="props.reply.type === 'audio'">{{ $t('ui.voiceMessage') }}</template>
            <template v-else-if="props.reply.type === 'video'">{{ $t('login.video') }}</template>
            <template v-else-if="props.reply.type === 'file'">{{ $t('chat.plik') }}</template>
          </div>
        </div>

        <button
          @click="$emit('clearReply')"
          class="flex items-center justify-center p-1 cursor-pointer bg-transparent border-0 shrink-0 opacity-80 hover:opacity-100 text-theme-text transition-opacity"
        >
          <svg
            xmlns="http://www.w3.org/2000/svg"
            class="w-5 h-5"
            fill="none"
            viewBox="0 0 24 24"
            stroke="currentColor"
            stroke-width="2"
          >
            <path stroke-linecap="round" stroke-linejoin="round" d="M6 18L18 6M6 6l12 12" />
          </svg>
        </button>
      </div>
    </Transition>

    <div class="flex items-end space-x-1">
      <div :class="{ 'w-full': isVoiceRecording }" v-if="showVoiceRecorder">
        <VoiceRecorder
          :theme-color="props.themes?.iconColor"
          @audio-recorded="handleAudioRecorded"
          @recording-start="isVoiceRecording = true"
          @recording-stop=";((isVoiceRecording = false), (showVoiceRecorder = false))"
        />
      </div>

      <template v-if="!isVoiceRecording">
        <!-- Przycisk Więcej akcji (+) - TYLKO DLA GRUP -->
        <VDropdown
          v-if="isGroupChat"
          ref="plusDropdown"
          placement="top-start"
          :distance="12"
          :triggers="['click']"
          :autoHide="true"
        >
          <button
            v-tooltip.top="'Więcej opcji (Ankieta, Pliki)'"
            type="button"
            class="h-[36px] w-[36px] mb-0.5 flex items-center justify-center rounded-full hover:bg-black/5 dark:hover:bg-white/10 cursor-pointer transition-colors shrink-0"
          >
            <svg
              width="22"
              height="22"
              viewBox="0 0 24 24"
              fill="none"
              xmlns="http://www.w3.org/2000/svg"
            >
              <circle cx="12" cy="12" r="11" :fill="props.themes?.iconColor || '#3b82f6'" />
              <path
                d="M12 7V17M7 12H17"
                stroke="white"
                stroke-width="2"
                stroke-linecap="round"
              />
            </svg>
          </button>

          <template #popper>
            <div
              class="flex flex-col py-2 min-w-[260px] rounded-2xl shadow-2xl border border-gray-100 dark:border-gray-700 overflow-hidden bg-white dark:bg-[#242526]"
            >
              <button
                @click="openPollModal"
                class="flex items-center gap-3.5 w-full px-4 py-2.5 hover:bg-gray-100 dark:hover:bg-gray-700 transition-colors group text-left cursor-pointer"
              >
                <div class="w-8 h-8 rounded-full flex items-center justify-center bg-blue-50 dark:bg-blue-900/30 text-blue-600 dark:text-blue-400">
                  <poll-icon :size="20" />
                </div>
                <div class="flex flex-col">
                  <span class="text-gray-900 dark:text-gray-100 font-semibold text-[14px]">{{ $t('createLive.createPoll') }}</span>
                  <span class="text-gray-500 dark:text-gray-400 text-[12px]">{{ $t('chat.zbierzOpinieCzlonkowGrupy') }}</span>
                </div>
              </button>

              <button
                @click="selectDocumentFile"
                class="flex items-center gap-3.5 w-full px-4 py-2.5 hover:bg-gray-100 dark:hover:bg-gray-700 transition-colors group text-left cursor-pointer"
              >
                <div class="w-8 h-8 rounded-full flex items-center justify-center bg-purple-50 dark:bg-purple-900/30 text-purple-600 dark:text-purple-400">
                  <file-icon :size="20" />
                </div>
                <div class="flex flex-col">
                  <span class="text-gray-900 dark:text-gray-100 font-semibold text-[14px]">{{ $t('chat.zalaczPlik') }}</span>
                  <span class="text-gray-500 dark:text-gray-400 text-[12px]">{{ $t('chat.do100Mb') }}</span>
                </div>
              </button>

              <button
                @click="showVoiceRecorder = true; plusDropdown?.hide()"
                class="flex items-center gap-3.5 w-full px-4 py-2.5 hover:bg-gray-100 dark:hover:bg-gray-700 transition-colors group text-left cursor-pointer"
              >
                <div class="w-8 h-8 rounded-full flex items-center justify-center bg-red-50 dark:bg-red-900/30 text-red-600 dark:text-red-400">
                  <microphone-icon :size="20" />
                </div>
                <div class="flex flex-col">
                  <span class="text-gray-900 dark:text-gray-100 font-semibold text-[14px]">{{ $t('ui.voiceMessage') }}</span>
                  <span class="text-gray-500 dark:text-gray-400 text-[12px]">{{ $t('chat.nagrajIWyslijAudio') }}</span>
                </div>
              </button>

              <button
                @click="selectImage(); plusDropdown?.hide()"
                class="flex items-center gap-3.5 w-full px-4 py-2.5 hover:bg-gray-100 dark:hover:bg-gray-700 transition-colors group text-left cursor-pointer"
              >
                <div class="w-8 h-8 rounded-full flex items-center justify-center bg-green-50 dark:bg-green-900/30 text-green-600 dark:text-green-400">
                  <image-outline-icon :size="20" />
                </div>
                <div class="flex flex-col">
                  <span class="text-gray-900 dark:text-gray-100 font-semibold text-[14px]">{{ $t('chat.zdjeciaIWideo') }}</span>
                  <span class="text-gray-500 dark:text-gray-400 text-[12px]">{{ $t('chat.udostepnijMultimedia') }}</span>
                </div>
              </button>
            </div>
          </template>
        </VDropdown>

        <!-- Nagrywanie głosu (w czatach prywatnych gdy brak wpisanego tekstu) -->
        <button
          v-if="!isGroupChat && !hasMedia"
          v-tooltip.top="'Wiadomość głosowa'"
          @click="showVoiceRecorder = true"
          class="p-1 rounded-full hover:bg-black/5 dark:hover:bg-white/10 mb-1 cursor-pointer"
          :style="{ color: props.themes?.iconColor }"
        >
          <MicrophoneIcon :size="22" />
        </button>

        <!-- Szybkie ikony akcji (widoczne gdy pole jest puste) -->
        <div v-show="!hasMedia" class="flex items-center gap-0.5 shrink-0 pb-1">
          <div
            v-tooltip.top="'Zdjęcia'"
            class="p-1 rounded-full hover:bg-black/5 dark:hover:bg-white/10 cursor-pointer"
          >
            <ImageOutlineIcon
              :size="22"
              :style="{ color: props.themes?.iconColor }"
              @click="selectImage"
            />
          </div>
          <div
            v-tooltip.top="'Naklejki'"
            class="p-1 rounded-full hover:bg-black/5 dark:hover:bg-white/10 cursor-pointer"
          >
            <StickerEmojiIcon :size="22" :fillColor="props.themes?.iconColor" />
          </div>

          <VDropdown
            ref="gifDropdown"
            placement="top"
            :distance="10"
            :skidding="0"
            :triggers="['click']"
            :autoHide="true"
            class="relative"
          >
            <div
              v-tooltip.top="'GIF'"
              :style="{ backgroundColor: props.themes?.iconColor || '#3b82f6' }"
              class="text-white text-[10px] font-black p-1 rounded flex ml-1 items-center justify-center h-[19px] w-[23px] leading-none hover:opacity-90 cursor-pointer"
            >{{ $t('ui.gif') }}</div>

            <template #popper>
              <div class="max-w-[320px] max-h-[420px] overflow-hidden">
                <GifBox
                  :theme-color="props.themes?.iconColor"
                  @handleGifSelection="handleGifSelect"
                />
              </div>
            </template>
          </VDropdown>
        </div>

          <input
            type="file"
            ref="fileInput"
            @change="handleImageUpload"
            accept="image/*"
            class="hidden"
            multiple
          />
          <input
            type="file"
            ref="documentFileInput"
            @change="handleDocumentUpload"
            class="hidden"
            multiple
          />

        <div
          class="relative flex flex-col grow rounded-[20px] overflow-hidden transition-all duration-200"
          :style="{ backgroundColor: props.themes?.textInputColor }"
        >
          <!-- Attachments tray (Images, Documents, GIFs) -->
          <ChatAttachmentTray
            :image-urls="selectedImageUrls"
            :document-files="selectedDocumentFiles"
            :gif-url="selectedGifUrl"
            @select-more-images="selectImage"
            @remove-image="removeImage"
            @remove-document="removeDocumentFile"
            @remove-gif="clearMediaSelection"
          />

          <div class="flex items-center relative min-h-[40px] grow overflow-visible">
            <!-- Autocomplete wzmianek w grupie (@) -->
            <ChatMentionAutocomplete
              v-if="mentionQuery !== null && matchingMentionMembers.length > 0"
              :members="matchingMentionMembers"
              :selected-index="selectedMentionIndex"
              @select="selectMention"
              @hover="selectedMentionIndex = $event"
            />

            <div
              ref="visualLayer"
              class="absolute inset-0 px-4 pr-10 py-2 text-[15px] whitespace-pre flex items-center overflow-hidden pointer-events-none [&_.emoji-mart-emoji]:!inline-flex [&_.emoji-mart-emoji]:items-center [&_.emoji-mart-emoji]:justify-center [&_.emoji-mart-emoji]:align-text-bottom [&_.emoji-mart-emoji]:leading-none"
              :style="{ color: props.themes?.timestampColor }"
            >
              <span v-if="!newMessage.length" class="opacity-60">{{ $t('chat.aa') }}</span>
              <template v-else v-for="(token, index) in parsedTokens" :key="index">
                <span
                  v-if="token.isEmoji"
                  class="inline-flex items-center justify-center align-middle"
                >
                  <Emoji
                    :data="emojiIndex"
                    :emoji="token.value"
                    :size="16"
                    :native="false"
                    set="facebook"
                  />
                </span>
                <span v-else class="align-middle">{{ token.value }}</span>
              </template>
            </div>

            <input
              ref="inputRef"
              v-model="newMessage"
              @keydown="handleInputKeydown"
              @input="handleInputUpdate"
              @scroll="handleScroll"
              @focus="onFocus"
              @blur="onBlur"
              type="text"
              class="absolute inset-0 w-full h-full px-4 pr-10 py-2 bg-transparent focus:outline-none text-[15px] z-10 !text-transparent selection:bg-blue-500/40 selection:!text-transparent placeholder:!text-transparent"
              :style="{ caretColor: props.themes?.timestampColor || '#000' }"
            />

            <VDropdown
              placement="top-end"
              :distance="10"
              :skidding="0"
              :triggers="['click']"
              :autoHide="true"
              class="absolute right-2 top-1/2 transform -translate-y-1/2 z-20"
            >
              <div v-tooltip.top="'Emoji'" class="p-1 rounded-full hover:bg-black/5">
                <EmoticonHappyOutlineIcon
                  :size="24"
                  class="cursor-pointer"
                  :fillColor="props.themes?.iconColor"
                />
              </div>

              <template #popper>
                <div class="max-w-[320px] max-h-[400px] overflow-hidden">
                  <LazyEmojiPicker
                    @select="
                      (e) => {
                        newMessage += e.native
                      }
                    "
                  />
                </div>
              </template>
            </VDropdown>
          </div>
        </div>

        <div class="flex items-center shrink-0">
          <LikeButton
            v-if="!isVoiceRecording && !newMessage.length && !hasMedia"
            :emoji="localSelectedEmoji"
            @send-like="handleSendLike"
          />
          <div
            v-if="!isVoiceRecording && (newMessage.length || hasMedia)"
            v-tooltip.top="'Wyślij'"
            class="w-[40px] h-[40px] flex items-center justify-center"
          >
            <SendIcon
              :size="22"
              class="cursor-pointer"
              :fillColor="props.themes?.iconColor || '#3b82f6'"
              @click="sendMessage(newMessage)"
            />
          </div>
        </div>
      </template>
    </div>
  </footer>

  <CreateChatPollModal
    :show="showPollModal"
    :theme-color="props.themes?.iconColor || '#1877F2'"
    @close="showPollModal = false"
    @submit="handlePollSubmit"
  />
</template>
