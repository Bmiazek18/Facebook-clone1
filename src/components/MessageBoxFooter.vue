<script setup lang="ts">
import { ref, computed, onUnmounted } from 'vue';

// --- FLOATING VUE ---
import { Dropdown as VDropdown } from 'floating-vue';
import 'floating-vue/dist/style.css';

// --- IKONY ---
import ImageOutlineIcon from 'vue-material-design-icons/ImageOutline.vue';
import StickerEmojiIcon from 'vue-material-design-icons/StickerEmoji.vue';
import EmoticonHappyOutlineIcon from 'vue-material-design-icons/EmoticonHappyOutline.vue';


// --- KOMPONENTY ---
import GifBox from './GifBox.vue';
import LazyEmojiPicker from './LazyEmojiPicker.vue';
import VoiceRecorder from './MessageBoxFooter/VoiceRecorder.vue';
import LikeButton from './MessageBoxFooter/LikeButton.vue';
import MediaPreview from './MessageBoxFooter/MediaPreview.vue';


import type { Message } from '@/types/Message';

const emit = defineEmits<{
  'add-message': [message: Message];
  'clearReply': []
}>()

const props = defineProps<{reply: Message | null; boxId?: string | number}>();

// translations available in templates via $t

// connect selected emoji/icon to the shared messenger theme store
import { useConversationsStore } from '@/stores/conversations'
const convStore = useConversationsStore();

const localSelectedEmoji = computed(() => {
  try {
    // if footer is used inside a chat (boxId), prefer the per-chat emoji stored in convStore.settings
    if (props.boxId) {
      const s = convStore.settings.find(x => x.chatId === Number(props.boxId));
      if (s?.emoji) return s.emoji;
    }
    return (convStore.selectedEmoji as string) || '👍'
  } catch {
    return '👍'
  }
})



const addMessage = (content: string, sizeState: 'default' | 'small' | 'medium' | 'large' = 'default', imageUrl?: string | null, gifUrl?: string | null, isAudio?: boolean, audioUrl?: string, duration?: number) => {
  const finalContent = content.trim();

  if (finalContent !== '' || imageUrl || gifUrl || isAudio) {
    const now = new Date();

    let newMsg: Message;

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
      };
    } else if (gifUrl && imageUrl) {
      newMsg = {
        id: Date.now(),
        sender: 'me',
        type: 'gif',
        content: finalContent,
        time: now.getTime(),
        iconSizeState: sizeState,
        imageUrl: imageUrl,
      };
    } else if (imageUrl) {
      newMsg = {
        id: Date.now(),
        sender: 'me',
        type: 'image',
        content: finalContent,
        time: now.getTime(),
        iconSizeState: sizeState,
        imageUrl: imageUrl,
      };
    } else {
      newMsg = {
        id: Date.now(),
        sender: 'me',
        type: 'text',
        content: finalContent,
        time: now.getTime(),
        iconSizeState: sizeState,
      };
    }

    emit('add-message', newMsg);
  }
};

const newMessage = ref('');

const fileInput = ref<HTMLInputElement | null>(null);
const selectedImageUrl = ref<string | null>(null);
const selectedGifUrl = ref<string | null>(null);


const isInputEmpty = computed(() => {
  return newMessage.value.trim().length === 0 && !selectedImageUrl.value && !selectedGifUrl.value;
});


// Media Selection
const clearMediaSelection = () => {
  if (selectedImageUrl.value) {
    URL.revokeObjectURL(selectedImageUrl.value);
  }
  selectedImageUrl.value = null;
  selectedGifUrl.value = null;
  newMessage.value = '';
};

const selectImage = () => {
  fileInput.value?.click();
};

const handleImageUpload = (event: Event) => {
  const target = event.target as HTMLInputElement;
  const file = target.files?.[0];

  if (file && file.type.startsWith('image/')) {
    selectedGifUrl.value = null;
    selectedImageUrl.value = URL.createObjectURL(file);
    newMessage.value = file.name;
  }
};

const handleGifSelect = (gifUrl: string) => {
  if (selectedImageUrl.value) URL.revokeObjectURL(selectedImageUrl.value);
  selectedImageUrl.value = null;
  selectedGifUrl.value = gifUrl;
  newMessage.value = 'Wysłano GIF';
};


const handleAudioRecorded = (payload: { audioUrl: string, duration: number }) => {
    addMessage(
        `Wiadomość głosowa (${payload.duration}s)`,
        'default',
        null,
        null,
        true,
        payload.audioUrl,
        payload.duration
    );
};


// Emoji & Picker
// togglePicker usunięte - obsługuje to Floating Vue

const showEmoji = (e: { native: string }) => {
  const emoji = e.native;
  // persist per-chat emoji if footer is inside a chat
  if (props.boxId) {
    try { convStore.setChatEmoji(Number(props.boxId), emoji); } catch {}
  }
  // also update global selection so UI that relies on it updates
  try { convStore.setSelectedEmoji(emoji); } catch {}
  newMessage.value = newMessage.value + emoji;
};

const handleSendLike = (sizeState: 'default' | 'small' | 'medium' | 'large') => {
    if (newMessage.value.trim().length === 0 && !selectedGifUrl.value && !selectedImageUrl.value) {
        addMessage(localSelectedEmoji.value, sizeState);
    }
}


const sendMessage = (content: string, sizeState: 'default' | 'small' | 'medium' | 'large' = 'default') => {
  const isSendingMedia = selectedImageUrl.value || selectedGifUrl.value;
  const finalContent = selectedGifUrl.value ? 'GIF' : content.trim();

  if (finalContent !== '' || isSendingMedia) {
    addMessage(finalContent, sizeState, selectedImageUrl.value, selectedGifUrl.value);
    newMessage.value = '';

    selectedImageUrl.value = null;
    selectedGifUrl.value = null;
  }
};

onUnmounted(() => {
  if (selectedImageUrl.value) URL.revokeObjectURL(selectedImageUrl.value);
  if (selectedGifUrl.value) URL.revokeObjectURL(selectedGifUrl.value);
});
</script>

<template>
  <footer class="p-2 border-t border-gray-200 bg-white shrink-0">
    <MediaPreview :imageUrl="selectedImageUrl" :gifUrl="selectedGifUrl" @clear-media="clearMediaSelection" />

    <transition name="reply">
      <div v-if="props.reply" class="reply-preview">
        <div class="flex justify-between items-center mb-1">
          <span class="reply-sender">{{ props.reply.sender === 'me' ? $t('ui.you') : props.reply.sender }}</span>
          <button @click="$emit('clearReply')" class="text-gray-400 hover:text-gray-600 text-xs">✕</button>
        </div>
        <span class="reply-content truncate">
          <template v-if="props.reply.type === 'text'">
            {{ props.reply.content }}
          </template>
          <template v-else-if="props.reply.type === 'image'">
            {{ $t('ui.image') }}
          </template>
          <template v-else-if="props.reply.type === 'gif'">
            {{ $t('ui.gif') }}
          </template>
          <template v-else-if="props.reply.type === 'audio'">
            {{ $t('ui.voiceMessage') }}
          </template>
        </span>
      </div>
    </transition>

    <div class="flex items-center space-x-1">
        <VoiceRecorder @audio-recorded="handleAudioRecorded" />
        <div class="flex space-x-1 text-gray-500 shrink-0">
        <ImageOutlineIcon
          :size="24"
          class="hover:text-purple-600 cursor-pointer"
          @click="selectImage"
        />
        <StickerEmojiIcon :size="24" class="hover:text-purple-600 cursor-pointer" />
        <GifBox v-if="false" @gif-selected="handleGifSelect" />
        <span class="font-bold text-xs border p-1 rounded cursor-pointer text-gray-500 hover:text-purple-600 self-center">GIF</span>
        <input
          type="file"
          ref="fileInput"
          @change="handleImageUpload"
          accept="image/*"
          class="hidden"
        />
      </div>

      <div class="relative grow">
        <input
          :value="newMessage"
          @input="newMessage = ($event.target as HTMLInputElement).value"
          @keyup.enter="sendMessage(newMessage, 'default')"
          type="text"
          placeholder="Aa"
          class="grow w-full p-2 pr-10 border border-gray-300 rounded-full focus:outline-none focus:ring-1 focus:ring-blue-500 text-sm transition duration-150 ease-in-out"
          style="padding-top: 8px; padding-bottom: 8px;"
        />

        <VDropdown
          placement="top-end"
          :distance="10"
          :skidding="0"
          :triggers="['click']"
          :autoHide="true"
          class="absolute right-2 top-1/2 transform -translate-y-1/2"
        >
          <EmoticonHappyOutlineIcon
            :size="24"
            class="text-gray-500 hover:text-purple-600 cursor-pointer"
          />

          <template #popper>
            <div class="emoji-popper-content">
              <LazyEmojiPicker @select="(e) => { showEmoji(e);  }" />
            </div>
          </template>
        </VDropdown>
      </div>

      <LikeButton :emoji="localSelectedEmoji" @send-like="handleSendLike" />
    </div>
  </footer>
</template>

<style scoped>
@keyframes wave {
  0%, 100% {
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

.delay-1 { animation-delay: 0.1s; }
.delay-2 { animation-delay: 0.2s; }
.delay-3 { animation-delay: 0.3s; }

/* Transition dla odpowiedzi */
.reply-enter-from {
  transform: translateY(20px);
  opacity: 0;
}
.reply-enter-to {
  transform: translateY(0);
  opacity: 1;
}
.reply-enter-active {
  transition: all 0.3s ease-out;
}

.reply-content {
  font-size: 0.875rem;
  color: #374151; /* gray-700 */
  display: -webkit-box;
  -webkit-line-clamp: 2; /* ograniczenie do 2 linii */
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: normal; /* pozwala na zawijanie w liniach */
  word-break: break-word; /* długie słowa też zawijają */
}

.reply-sender {
  font-weight: 600;
  font-size: 0.75rem;
  color: #6b7280; /* gray-500 */
}

/* Stylizacja kontenera popovera */
.emoji-popper-content {
  max-width: 320px;
  max-height: 400px;
  overflow: hidden;
}
</style>
