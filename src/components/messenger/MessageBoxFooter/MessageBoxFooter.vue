<script setup lang="ts">
import { ref, computed, onUnmounted } from 'vue';
import { useConversationsStore } from '@/stores/conversations'; // Added
import { storeToRefs } from 'pinia';
import { useMessengerThemeStore } from '@/stores/messengerTheme';

const convStore = useConversationsStore();
const { selectedTheme: currentTheme } = storeToRefs(useMessengerThemeStore());

// --- FLOATING VUE ---
import 'floating-vue/dist/style.css';

// --- IKONY ---
import ImageOutlineIcon from 'vue-material-design-icons/ImageOutline.vue';
import StickerEmojiIcon from 'vue-material-design-icons/StickerEmoji.vue';
import EmoticonHappyOutlineIcon from 'vue-material-design-icons/EmoticonHappyOutline.vue';
import SendIcon from 'vue-material-design-icons/Send.vue';


// --- KOMPONENTY ---
import GifBox from '@/components/common/GifBox.vue';
import LazyEmojiPicker from '@/components/common/LazyEmojiPicker.vue';
import VoiceRecorder from './VoiceRecorder.vue';
import LikeButton from './LikeButton.vue';
import MediaPreview from './MediaPreview.vue';


import type { Message } from '@/types/Message';

const emit = defineEmits<{
  'add-message': [message: Message];
  'clearReply': []
}>()

const props = defineProps<{reply: Message | null; boxId?: string | number;}>();

const fileInput = ref<HTMLInputElement | null>(null);
const selectedImageUrls = ref<string[]>([]);
const selectedGifUrl = ref<string | null>(null);



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



const addMessage = (content: string, sizeState: 'default' | 'small' | 'medium' | 'large' = 'default', imageUrls?: string[] | null, gifUrl?: string | null, isAudio?: boolean, audioUrl?: string, duration?: number) => {
  const finalContent = content.trim();

  if (finalContent !== '' || (imageUrls && imageUrls.length > 0) || gifUrl || isAudio) {
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
    } else if (gifUrl) {
      newMsg = {
        id: Date.now(),
        sender: 'me',
        type: 'gif',
        content: finalContent,
        time: now.getTime(),
        iconSizeState: sizeState,
        imageUrl: gifUrl,
      };
    } else if (imageUrls && imageUrls.length > 0) {
      newMsg = {
        id: Date.now(),
        sender: 'me',
        type: 'image',
        content: finalContent,
        time: now.getTime(),
        iconSizeState: sizeState,
        imageUrl: imageUrls[0], // for preview
        mediaUrls: imageUrls,
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
const isVoiceRecording = ref(false);


const removeImage = (index: number) => {
  URL.revokeObjectURL(selectedImageUrls.value[index]);
  selectedImageUrls.value.splice(index, 1);
};

const clearGifSelection = () => {
  selectedGifUrl.value = null;
};

// Media Selection
const clearMediaSelection = () => {
  selectedImageUrls.value.forEach(url => URL.revokeObjectURL(url));
  selectedImageUrls.value = [];
  selectedGifUrl.value = null;
  newMessage.value = '';
};

const selectImage = () => {
  fileInput.value?.click();
};

const handleImageUpload = (event: Event) => {
  const target = event.target as HTMLInputElement;
  const files = target.files;

  if (files) {
    selectedGifUrl.value = null;
    for (let i = 0; i < files.length; i++) {
      const file = files[i];
      if (file.type.startsWith('image/')) {
        selectedImageUrls.value.push(URL.createObjectURL(file));
      }
    }
  }
};

const handleGifSelect = (gifUrl: string) => {
  selectedImageUrls.value.forEach(url => URL.revokeObjectURL(url));
  selectedImageUrls.value = [];
  addMessage('Wysłano GIF', 'default', null, gifUrl);
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
    if (newMessage.value.trim().length === 0 && !selectedGifUrl.value && !selectedImageUrls.value.length) {
        addMessage(localSelectedEmoji.value, sizeState);
    }
}


const sendMessage = (content: string, sizeState: 'default' | 'small' | 'medium' | 'large' = 'default') => {
  const finalContent = content.trim();

  if (finalContent !== '' || selectedImageUrls.value.length > 0 || selectedGifUrl.value) {
    if (selectedImageUrls.value.length > 0) {
      addMessage(finalContent, sizeState, selectedImageUrls.value);
    } else if (selectedGifUrl.value) {
      addMessage(finalContent, sizeState, null, selectedGifUrl.value);
    } else {
      addMessage(finalContent, sizeState);
    }

    newMessage.value = '';

     selectedImageUrls.value = [];
    selectedImageUrls.value = [];
    selectedGifUrl.value = null;
  }
};

onUnmounted(() => {
  selectedImageUrls.value.forEach(url => URL.revokeObjectURL(url));
  if (selectedGifUrl.value) {
    // Sprawdzenie, czy to nie jest zwykły URL
    if (selectedGifUrl.value.startsWith('blob:')) {
      URL.revokeObjectURL(selectedGifUrl.value);
    }
  }
});
</script>

<template>
  <footer class="p-2 border-t border-theme-border bg-theme-bg-secondary shrink-0">
    <MediaPreview :imageUrls="selectedImageUrls" :gifUrl="selectedGifUrl" @clear-media="removeImage" @clear-gif="clearGifSelection" />

    <transition name="reply">
      <div v-if="props.reply" class="reply-preview">
        <div class="flex justify-between items-center mb-1">
          <span class="reply-sender text-theme-text">{{ props.reply.sender === 'me' ? $t('ui.you') : props.reply.sender }}</span>
          <button @click="$emit('clearReply')" class="text-theme-text-secondary hover:text-theme-text text-xs">✕</button>
        </div>
        <span class="reply-content truncate text-theme-text">
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
      <div :class="{ 'w-full': isVoiceRecording }">
        <VoiceRecorder
          :theme-color="currentTheme.sentBubbleColor"
          @audio-recorded="handleAudioRecorded"
          @recording-start="isVoiceRecording = true"
          @recording-stop="isVoiceRecording = false"
        />
      </div>

      <template v-if="!isVoiceRecording">
        <div class="flex space-x-1 shrink-0">
        <ImageOutlineIcon
          :size="24"
          class="cursor-pointer text-blue-500"

          :style="{ color: currentTheme?.sentBubbleColor }"
          @click="selectImage"
        />
        <StickerEmojiIcon :size="24" class="cursor-pointer" :style="{ fill: currentTheme?.sentBubbleColor }" :fillColor="currentTheme.sentBubbleColor" />
        <GifBox :size="24" :theme-color="currentTheme.sentBubbleColor" @gif-selected="handleGifSelect" />
        <input
          type="file"
          ref="fileInput"
          @change="handleImageUpload"
          accept="image/*"
          class="hidden"
          multiple
        />
      </div>

      <div class="relative grow">
        <input
          :value="newMessage"
          @input="newMessage = ($event.target as HTMLInputElement).value"
          @keyup.enter="sendMessage(newMessage, 'default')"
          type="text"
          placeholder="Aa"
          class="grow w-full p-2 pr-10 border border-theme-border rounded-full focus:outline-none focus:ring-1 focus:ring-theme-primary text-sm transition duration-150 ease-in-out bg-theme-bg text-theme-text"
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
            class="cursor-pointer"
            :fillColor="currentTheme.sentBubbleColor"
          />

          <template #popper>
            <div class="emoji-popper-content">
              <LazyEmojiPicker @select="(e) => { showEmoji(e);  }" />
            </div>
          </template>
        </VDropdown>
      </div>

      <LikeButton v-if="!isVoiceRecording && !newMessage.length && !selectedImageUrls.length && !selectedGifUrl" :emoji="localSelectedEmoji" @send-like="handleSendLike" />
      <SendIcon v-if="!isVoiceRecording && (newMessage.length || selectedImageUrls.length || selectedGifUrl)" :size="24" class="cursor-pointer" :fillColor="currentTheme.sentBubbleColor" @click="sendMessage(newMessage, 'default')" />
    </template>
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
  color: var(--theme-text);
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
  color: var(--theme-text-secondary);
}

/* Stylizacja kontenera popovera */
.emoji-popper-content {
  max-width: 320px;
  max-height: 400px;
  overflow: hidden;
}
</style>
