<script setup lang="ts">
import { ref, computed, onUnmounted } from 'vue';
import CloseIcon from 'vue-material-design-icons/Close.vue';
import ImageOutlineIcon from 'vue-material-design-icons/ImageOutline.vue';
import StickerEmojiIcon from 'vue-material-design-icons/StickerEmoji.vue';
import EmoticonHappyOutlineIcon from 'vue-material-design-icons/EmoticonHappyOutline.vue';
import MicrophoneIcon from 'vue-material-design-icons/Microphone.vue';
import StopIcon from 'vue-material-design-icons/Stop.vue';
import SendIcon from 'vue-material-design-icons/Send.vue';
import PlayIcon from 'vue-material-design-icons/Play.vue';
import ThumbUpIcon from 'vue-material-design-icons/ThumbUp.vue';
import GifBox from './GifBox.vue';
import { Picker, EmojiIndex } from "emoji-mart-vue-fast/src";
import "emoji-mart-vue-fast/css/emoji-mart.css";
import data from "emoji-mart-vue-fast/data/all.json";

import type { Message } from '@/types/Message';

const emojiIndex = new EmojiIndex(data);

const emit = defineEmits<{
  'add-message': [message: Message];
  'clearReply': void
}>()

const props = defineProps<{reply: Message | null}>();
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
const showPicker = ref(false);
const isRecording = ref(false);
const isPaused = ref(false);
const recordingDuration = ref(0);

// Media Recorder
const mediaRecorder = ref<MediaRecorder | null>(null);
const audioChunks = ref<Blob[]>([]);
const recordingTimer = ref<ReturnType<typeof setInterval> | null>(null);
const isRecordingInitialized = ref(false);
let mediaStream: MediaStream | null = null;
const recordedBlob = ref<Blob | null>(null);
const audioUrlPreview = ref<string | null>(null);

// Press animation
const animationTimer = ref<ReturnType<typeof setInterval> | null>(null);
const pressStartTime = ref<number | null>(null);
const currentPressDurationVisual = ref(0);
const ICON_CLASSES = ['icon-state-0', 'icon-state-1', 'icon-state-2', 'icon-state-3'];
const currentIconState = ref(ICON_CLASSES[0]);
const MAX_PRESS_TIME_MS = 5000;
const DURATION_STEP_MS = 1000;

const isInputEmpty = computed(() => {
  return newMessage.value.trim().length === 0 && !selectedImageUrl.value && !selectedGifUrl.value;
});

const formatDuration = (totalSeconds: number): string => {
  const minutes = Math.floor(totalSeconds / 60);
  const seconds = totalSeconds % 60;
  return `${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`;
};

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

// Voice Recording
const resetRecordingState = () => {
  if (recordingTimer.value) clearInterval(recordingTimer.value);
  isRecording.value = false;
  isPaused.value = false;
  recordingDuration.value = 0;
  audioChunks.value = [];
  recordedBlob.value = null;
  audioUrlPreview.value = null;
};

const startVoiceRecording = async () => {
  if (isPaused.value) {
    mediaRecorder.value?.resume();
    isPaused.value = false;
    isRecording.value = true;
    recordingTimer.value = setInterval(() => {
      recordingDuration.value++;
    }, 1000);
    return;
  }

  if (!isInputEmpty.value || isRecording.value) return;

  try {
    if (!isRecordingInitialized.value) {
      mediaStream = await navigator.mediaDevices.getUserMedia({ audio: true });
      mediaRecorder.value = new MediaRecorder(mediaStream, { mimeType: 'audio/webm' });

      mediaRecorder.value.ondataavailable = event => {
        if (event.data.size > 0) {
          audioChunks.value.push(event.data);
        }
      };
      isRecordingInitialized.value = true;
    }

    audioChunks.value = [];
    recordedBlob.value = null;
    if (audioUrlPreview.value) URL.revokeObjectURL(audioUrlPreview.value);
    audioUrlPreview.value = null;

    mediaRecorder.value?.start();
    isRecording.value = true;
    recordingDuration.value = 0;

    recordingTimer.value = setInterval(() => {
      recordingDuration.value++;
    }, 1000);
  } catch (error) {
    console.error('Błąd dostępu do mikrofonu:', error);
    alert('Nie można uzyskać dostępu do mikrofonu. Upewnij się, że zezwoliłeś na użycie mikrofonu.');
  }
};

const pauseRecording = () => {
  if (!isRecording.value || !mediaRecorder.value) return;

  mediaRecorder.value.stop();

  if (recordingTimer.value) {
    clearInterval(recordingTimer.value);
    recordingTimer.value = null;
  }

  isRecording.value = false;
  isPaused.value = true;

  mediaRecorder.value.onstop = () => {
    if (audioChunks.value.length === 0) {
      resetRecordingState();
      return;
    }

    const blob = new Blob(audioChunks.value, { type: 'audio/webm' });
    recordedBlob.value = blob;
    audioUrlPreview.value = URL.createObjectURL(blob);

    audioChunks.value = [];
  };
};

const sendRecordedAudio = () => {
  if (!recordedBlob.value || !audioUrlPreview.value) return;

  const durationSeconds = Math.floor(recordingDuration.value);
  addMessage(
    `Wiadomość głosowa (${formatDuration(recordingDuration.value)})`,
    'default',
    null,
    null,
    true,
    audioUrlPreview.value,
    durationSeconds
  );

  isRecording.value = false;
  isPaused.value = false;
  recordingDuration.value = 0;
  audioChunks.value = [];
  recordedBlob.value = null;
  audioUrlPreview.value = null;

  if (recordingTimer.value) clearInterval(recordingTimer.value);
};

const cancelVoiceRecording = () => {
  if (mediaRecorder.value) {
    mediaRecorder.value.stop();
    mediaRecorder.value.onstop = () => {};
  }

  if (audioUrlPreview.value) {
    URL.revokeObjectURL(audioUrlPreview.value);
  }

  resetRecordingState();
};

// Emoji & Picker
const togglePicker = () => {
  showPicker.value = !showPicker.value;
};

const showEmoji = (e: { native: string }) => {
  newMessage.value = newMessage.value + e.native;
};

// Press Animation
const clearTimers = () => {
  if (animationTimer.value) {
    clearInterval(animationTimer.value);
    animationTimer.value = null;
  }
  pressStartTime.value = null;
  currentPressDurationVisual.value = 0;
  currentIconState.value = ICON_CLASSES[0];
};

const handlePressStart = (event: MouseEvent | TouchEvent) => {
  if (event instanceof MouseEvent && event.button !== 0) return;
  if (pressStartTime.value !== null) return;

  pressStartTime.value = Date.now();
  currentIconState.value = ICON_CLASSES[0];
  currentPressDurationVisual.value = 0;

  const increaseIconState = () => {
    currentPressDurationVisual.value += 1;

    if (currentPressDurationVisual.value < ICON_CLASSES.length) {
      currentIconState.value = ICON_CLASSES[currentPressDurationVisual.value];
    } else {
      clearTimers();
    }
  };

  animationTimer.value = setInterval(increaseIconState, DURATION_STEP_MS);
};

const handlePressEnd = (content: string) => {
  if (pressStartTime.value === null) return;

  const durationMs = Date.now() - pressStartTime.value;
  clearTimers();

  const durationSeconds = durationMs / 1000;

  if (durationMs > MAX_PRESS_TIME_MS) {
    return;
  }

  let sizeState: 'default' | 'small' | 'medium' | 'large' = 'default';

  if (durationSeconds >= 3) {
    sizeState = 'large';
  } else if (durationSeconds >= 2) {
    sizeState = 'medium';
  } else if (durationSeconds >= 1) {
    sizeState = 'small';
  }

  if (content.trim().length === 0 && !selectedGifUrl.value && !selectedImageUrl.value) {
    addMessage('👍', sizeState);
  }
};

const sendMessage = (content: string, sizeState: 'default' | 'small' | 'medium' | 'large' = 'default') => {
  const isSendingMedia = selectedImageUrl.value || selectedGifUrl.value;
  const finalContent = selectedGifUrl.value ? 'GIF' : content.trim();

  if (finalContent !== '' || isSendingMedia) {
    if (isRecording.value || isPaused.value) {
      cancelVoiceRecording();
    }

    addMessage(finalContent, sizeState, selectedImageUrl.value, selectedGifUrl.value);
    newMessage.value = '';

    selectedImageUrl.value = null;
    selectedGifUrl.value = null;
  }
};

onUnmounted(() => {
  clearTimers();

  if (selectedImageUrl.value) URL.revokeObjectURL(selectedImageUrl.value);
  if (selectedGifUrl.value) URL.revokeObjectURL(selectedGifUrl.value);
  if (audioUrlPreview.value) URL.revokeObjectURL(audioUrlPreview.value);

  resetRecordingState();
  if (mediaStream) {
    mediaStream.getTracks().forEach(track => track.stop());
  }
});
</script>
<template>
  <footer class="p-2 border-t border-gray-200 bg-white shrink-0">
    <!-- Media Preview -->
    <div v-if="selectedImageUrl || selectedGifUrl" class="p-2 mb-2 bg-gray-100 rounded-lg flex items-center justify-between">
        <div class="flex items-center space-x-2">
        <img :src="selectedImageUrl || selectedGifUrl || ''" :alt="selectedGifUrl ? $t('post.selectedGif') : $t('post.selectedImage')" class="w-10 h-10 object-cover rounded" />
        <span class="text-xs text-gray-600 truncate">{{ selectedGifUrl ? $t('post.selectedGif') : $t('post.readyToSend') }}</span>
      </div>
      <button @click="clearMediaSelection" class="text-red-500 hover:text-red-700 text-sm font-semibold">
        {{ $t('common.delete') }}
      </button>
    </div>

<transition name="reply">
  <div v-if="props.reply" class="reply-preview">
    <div class="flex justify-between items-center mb-1">
      <span class="reply-sender">{{ props.reply.sender === 'me' ? 'Ty' : props.reply.sender }}</span>
      <button @click="$emit('clearReply')" class="text-gray-400 hover:text-gray-600 text-xs">✕</button>
    </div>
    <span class="reply-content truncate">
      <template v-if="props.reply.type === 'text'">
        {{ props.reply.content }}
      </template>
      <template v-else-if="props.reply.type === 'image'">
        Obraz
      </template>
      <template v-else-if="props.reply.type === 'gif'">
        GIF
      </template>
      <template v-else-if="props.reply.type === 'audio'">
        Wiadomość głosowa
      </template>
    </span>
  </div>
</transition>


    <!-- Recording State -->
    <div v-if="isRecording" class="flex items-center space-x-2 w-full">
      <button @click="cancelVoiceRecording" class="p-2 rounded-full bg-purple-600 text-white shrink-0 hover:bg-purple-700 transition">
        <CloseIcon :size="24" />
      </button>

      <div class="grow h-10 flex items-center bg-purple-600 rounded-full px-2 py-1 space-x-2 relative shadow-lg">
        <button @click="pauseRecording" class="w-8 h-8 rounded-full bg-white text-purple-600 flex items-center justify-center shrink-0">
          <StopIcon :size="20" class="text-purple-600" />
        </button>
        <div class="grow h-full flex items-center justify-center">
          <div class="w-full h-1 bg-purple-700 opacity-60 rounded-full relative">
            <div class="absolute inset-y-0 flex items-center space-x-1 left-2">
              <div class="w-1 h-2 bg-white rounded-full opacity-50 animate-wave delay-1"></div>
              <div class="w-1 h-3 bg-white rounded-full opacity-80 animate-wave delay-2"></div>
              <div class="w-1 h-4 bg-white rounded-full opacity-100 animate-wave delay-3"></div>
            </div>
          </div>
        </div>
        <span class="text-white text-sm font-semibold w-10 text-right shrink-0">{{ formatDuration(recordingDuration) }}</span>
      </div>

      <button disabled class="w-12 h-12 rounded-full bg-gray-300 text-gray-500 border-2 border-gray-400 flex items-center justify-center shrink-0">
        <SendIcon :size="24" />
      </button>
    </div>

    <!-- Paused Recording State -->
    <div v-else-if="isPaused" class="flex items-center space-x-2 w-full">
      <button @click="cancelVoiceRecording" class="p-2 rounded-full bg-purple-600 text-white shrink-0 hover:bg-purple-700 transition">
        <CloseIcon :size="24" />
      </button>

      <div class="grow h-10 flex items-center bg-purple-600 rounded-full px-2 py-1 space-x-2 relative shadow-lg">
        <button @click="startVoiceRecording" class="w-8 h-8 rounded-full bg-white text-purple-600 flex items-center justify-center shrink-0">
          <PlayIcon :size="20" class="text-purple-600" />
        </button>
        <div class="grow flex items-center"></div>
        <span class="text-white text-sm font-semibold w-10 text-right shrink-0">{{ formatDuration(recordingDuration) }}</span>
      </div>

      <button @click="sendRecordedAudio" class="w-12 h-12 rounded-full bg-white text-blue-500 border-2 border-blue-500 flex items-center justify-center shrink-0 hover:bg-gray-50 transition">
        <SendIcon :size="24" />
      </button>
    </div>

    <!-- Normal Input State -->
    <div v-else class="flex items-center space-x-1">
      <div class="flex space-x-1 text-gray-500 shrink-0">
        <MicrophoneIcon @click="startVoiceRecording" :size="24" class="text-blue-500 hover:text-blue-700 cursor-pointer" />
        <ImageOutlineIcon
          :size="24"
          class="hover:text-purple-600 cursor-pointer"
          @click="selectImage"
        />
        <StickerEmojiIcon :size="24" class="hover:text-purple-600 cursor-pointer" />
        <GifBox @gif-selected="handleGifSelect" />
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
        <EmoticonHappyOutlineIcon
          @click="togglePicker"
          :size="24"
          class="absolute right-2 top-1/2 transform -translate-y-1/2 text-gray-500 hover:text-purple-600 cursor-pointer"
        />
      </div>
      <div
        class="flex space-x-1 text-gray-500 shrink-0 cursor-pointer transition-transform duration-100"
        @mousedown="handlePressStart"
        @mouseup="handlePressEnd(newMessage)"
        @mouseleave="handlePressEnd(newMessage)"
        @touchstart.prevent="handlePressStart"
        @touchend.prevent="handlePressEnd(newMessage)"
      >
        <ThumbUpIcon
          :class="[currentIconState, 'text-blue-500 hover:text-blue-700']"
        />
      </div>
    </div>

    <!-- Emoji Picker -->
    <Picker :data="emojiIndex" v-if="showPicker" class="max-h-[313px] shadow-2xl absolute right-[50px]" set="facebook" perLine="8" color="oklch(48.8% 0.243 264.376)" :showPreview="false" @select="showEmoji" />
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
}.icon-state-0 {
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
.reply-enter-from{
  transform: translateY(20px);
  opacity: 0;
}
.reply-enter-to
 {
  transform: translateY(0);
  opacity: 1;
}
.reply-enter-active
 {
  transition: all 0.3s ease-out;
}

.reply-content {
  font-size: 0.875rem;
  color: #374151; /* gray-700 */
  display: -webkit-box;
  -webkit-line-clamp: 2; /* ograniczenie do 2 linii */
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

</style>
