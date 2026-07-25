<script setup lang="ts">
import { ref, computed, onUnmounted, watch } from 'vue'
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
import { useFileSize } from '@/composables/shared/useFileSize'

import GifBox from '@/components/common/GifBox.vue'
import LazyEmojiPicker from '@/components/common/LazyEmojiPicker.vue'
import VoiceRecorder from './VoiceRecorder.vue'
import LikeButton from './LikeButton.vue'

import type { Message } from '@/types/Message'
import type { Theme } from '@/types/Theme'

// --- TYPY I STAŁE ---
const API_UPLOAD_URL = 'http://localhost:8080/api/users/upload'

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

let typingTimeout: any = null
let isCurrentlyTyping = false

const sendTypingStatus = (isTyping: boolean) => {
  if (!props.boxId) return
  const conversationId = convStore.getSymmetricConversationId(props.boxId)
  convStore.publishMqtt('chat/messages/inbound', {
    type: 'typing',
    conversationId,
    senderId: convStore.currentUserUuid,
    isTyping,
  }, { qos: 0 })
}

watch(newMessage, (newVal) => {
  if (newVal.trim().length > 0) {
    if (!isCurrentlyTyping) {
      isCurrentlyTyping = true
      sendTypingStatus(true)
    }
    if (typingTimeout) clearTimeout(typingTimeout)
    typingTimeout = setTimeout(() => {
      isCurrentlyTyping = false
      sendTypingStatus(false)
    }, 2500)
  } else {
    if (isCurrentlyTyping) {
      isCurrentlyTyping = false
      if (typingTimeout) clearTimeout(typingTimeout)
      sendTypingStatus(false)
    }
  }
})

onUnmounted(() => {
  if (typingTimeout) clearTimeout(typingTimeout)
  if (isCurrentlyTyping) {
    sendTypingStatus(false)
  }
})
const selectedImageUrls = ref<string[]>([])
const selectedImageFiles = ref<File[]>([])
const selectedGifUrl = ref<string | null>(null)
const isVoiceRecording = ref(false)
const showVoiceRecorder = ref(false)

// --- COMPUTED ---

// Naprawiony błąd z testowaniem Regexa (bez flagi /g)
const parsedTokens = computed(() => {
  if (!newMessage.value) return []
  return newMessage.value
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

// --- POMOCNICZE FUNKCJE SIECIOWE (DRY) ---

const uploadFile = async (file: File): Promise<string> => {
  const formData = new FormData()
  formData.append('file', file)

  const headers: Record<string, string> = {}
  const token = localStorage.getItem('keycloak-token')
  if (token) headers['Authorization'] = `Bearer ${token}`

  const res = await fetch(API_UPLOAD_URL, {
    method: 'POST',
    headers,
    body: formData,
  })

  if (!res.ok) throw new Error('Błąd wgrywania pliku na serwer')
  const data = await res.json()
  return data.url
}

// --- METODY ---

const handleScroll = (e: Event) => {
  if (visualLayer.value) {
    visualLayer.value.scrollLeft = (e.target as HTMLInputElement).scrollLeft
  }
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

const handleGifSelect = (gifUrl: string) => {
  clearMediaSelection()
  createAndEmitMessage({
    type: 'gif',
    content: 'Wysłano GIF',
    imageUrl: gifUrl,
  })
  gifDropdown.value?.hide()
}

const handleAudioRecorded = async (payload: { audioUrl: string; duration: number }) => {
  try {
    const response = await fetch(payload.audioUrl)
    const blob = await response.blob()
    const file = new File([blob], `voice-recording-${Date.now()}.wav`, { type: 'audio/wav' })

    const minioUrl = await uploadFile(file)

    createAndEmitMessage({
      type: 'audio',
      content: `Wiadomość głosowa (${payload.duration}s)`,
      audioUrl: minioUrl,
      duration: payload.duration,
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
      createAndEmitMessage({
        type: 'gif',
        content: finalContent,
        imageUrl: selectedGifUrl.value,
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
      createAndEmitMessage({
        type: 'text',
        content: finalContent,
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
    class="p-2 shrink-0 rounded-b-xl shadow-md relative"
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
          <div class="font-bold text-[14.5px] mb-[2px] truncate text-theme-text">
            Odpowiadasz użytkownikowi: {{ replySenderName }}
          </div>
          <div class="text-[13.5px] opacity-75 truncate text-theme-text-secondary">
            <template v-if="props.reply.type === 'text'">{{ props.reply.content }}</template>
            <template v-else-if="props.reply.type === 'image'">Obraz</template>
            <template v-else-if="props.reply.type === 'gif'">GIF</template>
            <template v-else-if="props.reply.type === 'audio'">Wiadomość głosowa</template>
            <template v-else-if="props.reply.type === 'video'">Wideo</template>
            <template v-else-if="props.reply.type === 'file'">Plik</template>
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
        <button
          v-if="!hasMedia"
          v-tooltip.top="'Wiadomość głosowa'"
          @click="showVoiceRecorder = true"
          class="p-1 rounded-full hover:bg-black/5 mb-1 cursor-pointer"
          :style="{ color: props.themes?.iconColor }"
        >
          <MicrophoneIcon :size="22" />
        </button>
        <div class="flex items-center gap-1 shrink-0 pb-1 mr-1">
          <div
            class="flex items-center gap-1 mr-1 transition-all duration-300 shrink-0"
            :class="hasMedia ? 'w-[32px]' : 'w-[104px]'"
          >
            <div v-show="!hasMedia" class="flex items-center gap-1">
              <div
                v-tooltip.top="'Zdjęcia'"
                class="p-1 rounded-full hover:bg-black/5 cursor-pointer"
              >
                <ImageOutlineIcon
                  :size="24"
                  :style="{ color: props.themes?.iconColor }"
                  @click="selectImage"
                />
              </div>
              <div
                v-tooltip.top="'Naklejki'"
                class="p-1 rounded-full hover:bg-black/5 cursor-pointer"
              >
                <StickerEmojiIcon :size="24" :fillColor="props.themes?.iconColor" />
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
                  class="text-white text-[10px] font-black p-1 rounded flex ml-1 items-center justify-center h-[20px] w-[24px] leading-none hover:bg-black/5 cursor-pointer"
                >
                  GIF
                </div>

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

            <div v-show="hasMedia" class="flex items-center">
              <VDropdown
                ref="gifDropdown"
                placement="top-start"
                :distance="12"
                :triggers="[]"
                :autoHide="true"
                class="relative"
              >
                <VDropdown
                  ref="plusDropdown"
                  placement="top-start"
                  :distance="12"
                  :triggers="['click']"
                  :autoHide="true"
                >
                  <div
                    v-tooltip.top="'Więcej opcji'"
                    class="h-[40px] w-[40px] flex items-center justify-center cursor-pointer hover:opacity-85 transition-opacity pt-[5px]"
                  >
                    <svg
                      width="22"
                      height="22"
                      viewBox="0 0 24 24"
                      fill="none"
                      xmlns="http://www.w3.org/2000/svg"
                    >
                      <circle cx="12" cy="12" r="12" :fill="props.themes?.iconColor || '#3b82f6'" />
                      <path
                        d="M12 7V17M7 12H17"
                        stroke="white"
                        stroke-width="2"
                        stroke-linecap="round"
                      />
                    </svg>
                  </div>

                  <template #popper>
                    <div
                      class="flex flex-col py-2 min-w-[280px] rounded-xl shadow-2xl border border-gray-100 overflow-hidden bg-white"
                    >
                      <button
                        @click="showVoiceRecorder = true"
                        class="flex items-center gap-4 w-full px-4 py-2 hover:bg-gray-100 transition-colors group text-left"
                      >
                        <microphone-icon :size="24" :fillColor="props.themes?.iconColor" />
                        <span class="text-gray-800 font-medium text-[15px]"
                          >Prześlij nagranie głosowe</span
                        >
                      </button>

                      <button
                        @click="selectDocumentFile"
                        class="flex items-center gap-4 w-full px-4 py-2 hover:bg-gray-100 transition-colors group text-left"
                      >
                        <file-icon :size="24" :fillColor="props.themes?.iconColor" />
                        <span class="text-gray-800 font-medium text-[15px]"
                          >Załącz plik o wielkości do 100 MB</span
                        >
                      </button>

                      <button
                        class="flex items-center gap-4 w-full px-4 py-2 hover:bg-gray-100 transition-colors group text-left"
                      >
                        <sticker-emoji-icon :size="24" :fillColor="props.themes?.iconColor" />
                        <span class="text-gray-800 font-medium text-[15px]">Wybierz naklejkę</span>
                      </button>

                      <button
                        @click="openGifFromPlus"
                        class="flex items-center gap-4 w-full px-4 py-2 hover:bg-gray-100 transition-colors group text-left"
                      >
                        <div
                          :style="{ backgroundColor: props.themes?.iconColor || '#3b82f6' }"
                          class="text-white text-[10px] font-black px-1 rounded flex items-center justify-center h-[20px] w-[24px] leading-none"
                        >
                          GIF
                        </div>
                        <span class="text-gray-800 font-medium text-[15px]">Wybierz GIF</span>
                      </button>
                    </div>
                  </template>
                </VDropdown>

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
        </div>

        <div
          class="relative flex flex-col grow rounded-[20px] overflow-hidden transition-all duration-200"
          :style="{ backgroundColor: props.themes?.textInputColor }"
        >
          <div
            v-if="selectedImageUrls.length > 0 || selectedDocumentFiles.length > 0"
            class="flex items-center gap-2 pt-2 px-3 pb-0 overflow-x-auto"
          >
            <div
              @click="selectImage"
              class="w-12 h-12 shrink-0 rounded-[10px] flex items-center justify-center cursor-pointer hover:bg-black/10 transition-colors bg-black/5"
            >
              <svg
                width="24"
                height="24"
                viewBox="0 0 24 24"
                fill="none"
                xmlns="http://www.w3.org/2000/svg"
              >
                <rect
                  x="3"
                  y="5"
                  width="14"
                  height="14"
                  rx="3"
                  stroke="currentColor"
                  stroke-width="2"
                />
                <path
                  d="M7 12H13M10 9V15"
                  stroke="currentColor"
                  stroke-width="2"
                  stroke-linecap="round"
                />
                <path
                  d="M21 7V17C21 18.1046 20.1046 19 19 19H9"
                  stroke="currentColor"
                  stroke-width="2"
                  stroke-linecap="round"
                />
              </svg>
            </div>

            <div
              v-for="(img, index) in selectedImageUrls"
              :key="index"
              class="relative w-12 h-12 shrink-0 mt-2 mb-1"
            >
              <img
                :src="img"
                class="w-full h-full object-cover rounded-[10px] border border-black/5"
              />
              <button
                @click.stop="removeImage(index)"
                class="absolute -top-2 -right-2 w-[22px] h-[22px] bg-white rounded-full flex items-center justify-center shadow-[0_1px_4px_rgba(0,0,0,0.25)] hover:bg-gray-100 z-10"
              >
                <svg
                  width="10"
                  height="10"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="black"
                  stroke-width="3"
                  stroke-linecap="round"
                >
                  <path d="M18 6L6 18M6 6l12 12" />
                </svg>
              </button>
            </div>

            <div
              v-for="(file, index) in selectedDocumentFiles"
              :key="'doc-' + index"
              class="relative flex items-center gap-1.5 p-1.5 bg-black/5 rounded-[10px] border border-black/5 h-12 mt-2 mb-1 text-gray-800 dark:text-gray-200"
            >
              <FileIcon :size="18" class="shrink-0" />
              <div class="min-w-0 flex flex-col justify-center leading-tight">
                <p class="text-[10px] font-semibold truncate max-w-[80px]">{{ file.name }}</p>
                <p class="text-[8px] opacity-70">{{ useFileSize(file.size) }}</p>
              </div>
              <button
                @click.stop="removeDocumentFile(index)"
                class="w-[18px] h-[18px] bg-white dark:bg-zinc-800 rounded-full flex items-center justify-center shadow-[0_1px_4px_rgba(0,0,0,0.25)] hover:bg-gray-100 dark:hover:bg-zinc-700 ml-1 shrink-0"
              >
                <svg
                  width="8"
                  height="8"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  stroke-width="3"
                  stroke-linecap="round"
                >
                  <path d="M18 6L6 18M6 6l12 12" />
                </svg>
              </button>
            </div>

            <div v-if="selectedGifUrl" class="relative w-12 h-12 shrink-0 mt-2 mb-1">
              <img
                :src="selectedGifUrl"
                class="w-full h-full object-cover rounded-[10px] border border-black/5"
              />
              <button
                @click.stop="clearMediaSelection"
                class="absolute -top-2 -right-2 w-[22px] h-[22px] bg-white rounded-full flex items-center justify-center shadow-[0_1px_4px_rgba(0,0,0,0.25)] hover:bg-gray-100 z-10"
              >
                <svg
                  width="10"
                  height="10"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="black"
                  stroke-width="3"
                  stroke-linecap="round"
                >
                  <path d="M18 6L6 18M6 6l12 12" />
                </svg>
              </button>
            </div>
          </div>

          <div class="flex items-center relative min-h-[40px] grow overflow-hidden">
            <div
              ref="visualLayer"
              class="absolute inset-0 px-4 pr-10 py-2 text-[15px] whitespace-pre flex items-center overflow-hidden pointer-events-none [&_.emoji-mart-emoji]:!inline-flex [&_.emoji-mart-emoji]:items-center [&_.emoji-mart-emoji]:justify-center [&_.emoji-mart-emoji]:align-text-bottom [&_.emoji-mart-emoji]:leading-none"
              :style="{ color: props.themes?.timestampColor }"
            >
              <span v-if="!newMessage.length" class="opacity-60">Aa</span>
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
              v-model="newMessage"
              @keyup.enter="sendMessage(newMessage)"
              @scroll="handleScroll"
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
</template>
