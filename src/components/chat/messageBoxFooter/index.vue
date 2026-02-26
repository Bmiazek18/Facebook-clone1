<script setup lang="ts">
import { ref, computed, onUnmounted } from 'vue'
import { useConversationsStore } from '@/stores/conversations'

const convStore = useConversationsStore()

// --- FLOATING VUE ---
import 'floating-vue/dist/style.css'

// --- IKONY ---
import ImageOutlineIcon from 'vue-material-design-icons/ImageOutline.vue'
import StickerEmojiIcon from 'vue-material-design-icons/StickerEmoji.vue'
import EmoticonHappyOutlineIcon from 'vue-material-design-icons/EmoticonHappyOutline.vue'
import SendIcon from 'vue-material-design-icons/Send.vue'
import MicrophoneIcon from 'vue-material-design-icons/Microphone.vue'
import ImageMultipleIcon from 'vue-material-design-icons/ImageMultiple.vue'

// --- KOMPONENTY ---
import GifBox from '@/components/common/GifBox.vue'
import LazyEmojiPicker from '@/components/common/LazyEmojiPicker.vue'
import VoiceRecorder from './VoiceRecorder.vue'
import LikeButton from './LikeButton.vue'

import type { Message } from '@/types/Message'
import type { Theme } from '@/stores/chatTheme'

const emit = defineEmits<{
  'add-message': [message: Message]
  clearReply: []
}>()

const props = defineProps<{ reply: Message | null; boxId?: string | number; themes?: Theme }>()

const fileInput = ref<HTMLInputElement | null>(null)
const selectedImageUrls = ref<string[]>([])
const selectedGifUrl = ref<string | null>(null)
const gifDropdown = ref<any>(null) // Declare ref for VDropdown
const plusDropdown = ref<any>(null)

const openGifFromPlus = () => {
  plusDropdown.value?.hide() // Zamyka menu opcji
  setTimeout(() => {
    gifDropdown.value?.show() // Otwiera menu GIF
  }, 50)
}
const localSelectedEmoji = computed(() => {
  try {
    if (props.boxId) {
      const s = convStore.settings.find((x) => x.chatId === Number(props.boxId))
      if (s?.emoji) return s.emoji
    }
    return (convStore.selectedEmoji as string) || '👍'
  } catch {
    return '👍'
  }
})

// Pomocnicza zmienna: czy mamy wybrane jakieś multimedia?
const hasMedia = computed(
  () => selectedImageUrls.value.length > 0 || newMessage.value.trim().length > 0,
)

const addMessage = (
  content: string,
  sizeState: 'default' | 'small' | 'medium' | 'large' = 'default',
  imageUrls?: string[] | null,
  gifUrl?: string | null,
  isAudio?: boolean,
  audioUrl?: string,
  duration?: number,
) => {
  const finalContent = content.trim()

  if (finalContent !== '' || (imageUrls && imageUrls.length > 0) || gifUrl || isAudio) {
    const now = new Date()
    let newMsg: Message

    if (isAudio && audioUrl && duration !== undefined) {
      newMsg = {
        id: Date.now(),
        sender: 'me',
        type: 'audio',
        content: finalContent,
        time: now.getTime(),
        iconSizeState: sizeState,
        audioUrl: audioUrl,
        duration: duration,
      }
    } else if (gifUrl) {
      newMsg = {
        id: Date.now(),
        sender: 'me',
        type: 'gif',
        content: finalContent,
        time: now.getTime(),
        iconSizeState: sizeState,
        imageUrl: gifUrl,
      }
    } else if (imageUrls && imageUrls.length > 0) {
      newMsg = {
        id: Date.now(),
        sender: 'me',
        type: 'image',
        content: finalContent,
        time: now.getTime(),
        iconSizeState: sizeState,
        imageUrl: imageUrls[0],
        mediaUrls: imageUrls,
      }
    } else {
      newMsg = {
        id: Date.now(),
        sender: 'me',
        type: 'text',
        content: finalContent,
        time: now.getTime(),
        iconSizeState: sizeState,
      }
    }

    emit('add-message', newMsg)
  }
}

const newMessage = ref('')
const isVoiceRecording = ref(false)
const showVoiceRecorder = ref(false)

const removeImage = (index: number) => {
  URL.revokeObjectURL(selectedImageUrls.value[index])
  selectedImageUrls.value.splice(index, 1)
}

const clearGifSelection = () => {
  selectedGifUrl.value = null
}

const clearMediaSelection = () => {
  selectedImageUrls.value.forEach((url) => URL.revokeObjectURL(url))
  selectedImageUrls.value = []
  selectedGifUrl.value = null
  newMessage.value = ''
}

const selectImage = () => {
  fileInput.value?.click()
}

const handleImageUpload = (event: Event) => {
  const target = event.target as HTMLInputElement
  const files = target.files

  if (files) {
    selectedGifUrl.value = null
    for (let i = 0; i < files.length; i++) {
      const file = files[i]
      if (file.type.startsWith('image/')) {
        selectedImageUrls.value.push(URL.createObjectURL(file))
      }
    }
  }
}

const handleGifSelect = (gifUrl: string) => {
  selectedImageUrls.value.forEach((url) => URL.revokeObjectURL(url))
  selectedImageUrls.value = []
  addMessage('Wysłano GIF', 'default', null, gifUrl)
  if (gifDropdown.value) {
    gifDropdown.value.hide() // Close the dropdown
  }
}

const handleAudioRecorded = (payload: { audioUrl: string; duration: number }) => {
  addMessage(
    `Wiadomość głosowa (${payload.duration}s)`,
    'default',
    null,
    null,
    true,
    payload.audioUrl,
    payload.duration,
  )
}

const showEmoji = (e: { native: string }) => {
  const emoji = e.native
  if (props.boxId) {
    try {
      convStore.setChatEmoji(Number(props.boxId), emoji)
    } catch {}
  }
  try {
    convStore.setSelectedEmoji(emoji)
  } catch {}
  newMessage.value = newMessage.value + emoji
}

const handleSendLike = (sizeState: 'default' | 'small' | 'medium' | 'large') => {
  if (
    newMessage.value.trim().length === 0 &&
    !selectedGifUrl.value &&
    !selectedImageUrls.value.length
  ) {
    addMessage(localSelectedEmoji.value, sizeState)
  }
}

const sendMessage = (
  content: string,
  sizeState: 'default' | 'small' | 'medium' | 'large' = 'default',
) => {
  const finalContent = content.trim()

  if (finalContent !== '' || selectedImageUrls.value.length > 0 || selectedGifUrl.value) {
    if (selectedImageUrls.value.length > 0) {
      addMessage(finalContent, sizeState, selectedImageUrls.value)
    } else if (selectedGifUrl.value) {
      addMessage(finalContent, sizeState, null, selectedGifUrl.value)
    } else {
      addMessage(finalContent, sizeState)
    }

    newMessage.value = ''
    selectedImageUrls.value = []
    selectedGifUrl.value = null
  }
}

onUnmounted(() => {
  selectedImageUrls.value.forEach((url) => URL.revokeObjectURL(url))
  if (selectedGifUrl.value && selectedGifUrl.value.startsWith('blob:')) {
    URL.revokeObjectURL(selectedGifUrl.value)
  }
})
</script>

<template>
  <footer
    class="p-2 shrink-0 rounded-b-xl shadow-md relative"
    :style="{ backgroundColor: props.themes?.footerColor }"
  >
    <transition name="reply">
      <div v-if="props.reply" class="reply-preview">
        <div class="reply-text-container">
          <div class="reply-title text-theme-text">
            Odpowiadanie {{ reply.sender === 'me' ? 'Tobie' : reply.sender }}
          </div>
          <div class="reply-content text-theme-text-secondary">
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
          class="reply-close-btn text-theme-text hover:text-theme-text-secondary transition-colors"
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
    </transition>

    <div class="flex items-end space-x-1">
      <div :class="{ 'w-full': isVoiceRecording }" v-if="showVoiceRecorder">
        <VoiceRecorder
          :theme-color="props.themes.iconColor"
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
          :style="{ color: props.themes.iconColor }"
        >
          <MicrophoneIcon :size="22" />
        </button>
        <div class="flex items-center gap-1 shrink-0 pb-1 mr-1">
          <div
            class="flex items-center gap-1 mr-1 transition-width duration-300 shrink-0"
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
                  <div class="gif-popper-content">
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
                      <circle cx="12" cy="12" r="12" :fill="props.themes.iconColor || '#3b82f6'" />
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
                        @click="selectImage"
                        class="flex items-center gap-4 w-full px-4 py-2 hover:bg-gray-100 transition-colors group text-left"
                      >
                        <image-multiple-icon :size="24" :fillColor="props.themes?.iconColor" />
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
                  <div class="gif-popper-content">
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
        </div>

        <div
          class="relative flex flex-col grow rounded-[20px] overflow-hidden transition-all duration-200"
          :style="{ backgroundColor: props.themes?.textInputColor }"
        >
          <div
            v-if="selectedImageUrls.length > 0"
            class="flex items-center gap-2 pt-2 px-3 pb-0 overflow-x-auto"
          >
            <div
              @click="selectImage"
              class="w-12 h-12 shrink-0 rounded-[10px] flex items-center justify-center cursor-pointer hover:bg-black/10 transition-colors"
              style="background-color: rgba(0, 0, 0, 0.06)"
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

            <div v-if="selectedGifUrl" class="relative w-12 h-12 shrink-0 mt-2 mb-1">
              <img
                :src="selectedGifUrl"
                class="w-full h-full object-cover rounded-[10px] border border-black/5"
              />
              <button
                @click.stop="clearGifSelection"
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

          <div class="flex items-center relative min-h-[40px]">
            <input
              :value="newMessage"
              @input="newMessage = ($event.target as HTMLInputElement).value"
              @keyup.enter="sendMessage(newMessage, 'default')"
              type="text"
              placeholder="Aa"
              class="grow w-full px-4 py-2 bg-transparent tr focus:outline-none text-[15px]"
              :style="{ color: props.themes?.timestampColor }"
            />

            <VDropdown
              placement="top-end"
              :distance="10"
              :skidding="0"
              :triggers="['click']"
              :autoHide="true"
              class="absolute right-2 top-1/2 transform -translate-y-1/2"
            >
              <div v-tooltip.top="'Emoji'" class="p-1 rounded-full hover:bg-black/5">
                <EmoticonHappyOutlineIcon
                  :size="24"
                  class="cursor-pointer"
                  :fillColor="props.themes.iconColor"
                />
              </div>

              <template #popper>
                <div class="emoji-popper-content">
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
            class="w-[40px] h-[40px] flex align-center items-center justify-center"
          >
            <SendIcon
              :size="22"
              class="cursor-pointer"
              :fillColor="props.themes?.iconColor || '#3b82f6'"
              @click="sendMessage(newMessage, 'default')"
            />
          </div>
        </div>
      </template>
    </div>
  </footer>
</template>

<style scoped>
@keyframes wave {
  0%,
  100% {
    transform: scaleY(0.5);
  }
  50% {
    transform: scaleY(1.5);
  }
}

.animate-wave {
  animation: wave 1.2s ease-in-out infinite;
}

.icon-state-0 {
  width: 24px;
  height: 24px;
  font-size: 24px;
  transition: all 0.3s ease-out;
}
.icon-state-1 {
  width: 30px;
  height: 30px;
  font-size: 30px;
  transition: all 0.3s ease-out;
}
.icon-state-2 {
  width: 36px;
  height: 36px;
  font-size: 36px;
  transition: all 0.3s ease-out;
}
.icon-state-3 {
  width: 48px;
  height: 48px;
  font-size: 48px;
  transition: all 0.3s ease-out;
}

.delay-1 {
  animation-delay: 0.1s;
}
.delay-2 {
  animation-delay: 0.2s;
}
.delay-3 {
  animation-delay: 0.3s;
}

/* Stylizacja kontenera popovera */
.emoji-popper-content {
  max-width: 320px;
  max-height: 400px;
  overflow: hidden;
}

.reply-preview {
  background-color: inherit;

  border-top: 1px solid var(--color-theme-border, rgba(150, 150, 150, 0.2));
  padding: 5px 8px;
  box-shadow: 0 -4px 15px rgba(0, 0, 0, 0.03);
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  z-index: 10;
}

.reply-text-container {
  display: flex;
  flex-direction: column;
  flex: 1;
  min-width: 0;
}

.reply-title {
  font-weight: 700;
  font-size: 14.5px;
  margin-bottom: 2px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.reply-content {
  font-size: 13.5px;
  opacity: 0.75;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.reply-close-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 4px;
  cursor: pointer;
  background: transparent;
  border: none;
  flex-shrink: 0;
  opacity: 0.8;
}

.reply-close-btn:hover {
  opacity: 1;
}

.reply-enter-active,
.reply-leave-active {
  transition: all 0.25s cubic-bezier(0.25, 0.8, 0.25, 1);
}

.reply-enter-from,
.reply-leave-to {
  transform: translateY(10px);
  opacity: 0;
}

.gif-popper-content {
  max-width: 320px;
  max-height: 420px;
  overflow: hidden;
}
</style>
