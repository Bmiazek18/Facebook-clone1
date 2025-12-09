<script setup lang="ts">
import { ref, computed } from 'vue';
import PlayIcon from 'vue-material-design-icons/Play.vue';
import PauseIcon from 'vue-material-design-icons/Pause.vue';
import ReplyIcon from 'vue-material-design-icons/Reply.vue';
import FileIcon from 'vue-material-design-icons/File.vue';
import type { Message, ImageMessage, GifMessage, AudioMessage, FileMessage, VideoMessage, AudioState } from '@/types/Message';
import MessegePool from './MessegePool.vue';
import Modal from './Modal.vue';
import PlayerVideo from './PlayerVideo.vue';
import ReactionPanel from './ReactionPanel.vue';
import { useFileSize } from '@/composables/useFileSize';

// --- DEFINICJA TYPÓW DLA TYPE GUARDS ---
interface ImageMessageWithGroup extends ImageMessage {
  mediaUrls?: string[];
}

const props = defineProps<{
  message: Message;
  index: number;
  positionInGroup: 'single' | 'first' | 'middle' | 'last';
  audioStates: Record<number, AudioState>;
}>();

const emit = defineEmits<{
  (e: 'open-lightbox', url: string): void;
  (e: 'toggle-audio-playback', message: Message): void;
  (e: 'reply', message: Message): void;
  (e: 'add-reaction', payload: { messageId: number; emoji: string }): void;
}>();


const isAudioMessage = (msg: Message): msg is AudioMessage => msg.type === 'audio';
const isImageMessage = (msg: Message): msg is ImageMessageWithGroup => msg.type === 'image';
const isFileMessage = (msg: Message): msg is FileMessage => msg.type === 'file';
const isVideoMessage = (msg: Message): msg is VideoMessage => msg.type === 'video';
const isGifMessage = (msg: Message): msg is GifMessage => msg.type === 'gif';


const formatSeconds = (seconds: number): string => {
  if (isNaN(seconds) || seconds < 0) return '0:00';
  const minutes = Math.floor(seconds / 60);
  const secs = Math.floor(seconds % 60);
  return `${minutes}:${secs.toString().padStart(2, '0')}`;
};

const isEmojiOnly = (content: string): boolean => {
  if (!content?.trim()) return false;
  const strippedContent = content.trim().replace(
    /(\u00a9|\u00ae|[\u2000-\u3300]|\ud83c[\ud000-\udfff]|\ud83d[\ud000-\udfff]|\ud83e[\ud000-\udfff])/g,
    ''
  );
  return strippedContent.trim().length === 0 && content.trim().length > 0;
};

const positionClasses = computed(() => {
  const isMe = props.message.sender === 'me';

  const mappings: Record<string, string> = {
    single: 'rounded-xl',
    first: isMe
      ? 'rounded-tl-xl rounded-tr-xl rounded-bl-xl rounded-br-[4px]'
      : 'rounded-tl-xl rounded-tr-xl rounded-br-xl rounded-bl-[4px]',
    middle: isMe
      ? 'rounded-tl-xl rounded-bl-xl rounded-tr-[4px] rounded-br-[4px]'
      : 'rounded-tr-xl rounded-br-xl rounded-tl-[4px] rounded-bl-[4px]',
    last: isMe
      ? 'rounded-tl-xl rounded-bl-xl rounded-tr-[4px] rounded-br-xl'
      : 'rounded-tr-xl rounded-br-xl rounded-tl-[4px] rounded-bl-xl',
  };

  return mappings[props.positionInGroup] || 'rounded-xl';
});


const shouldDisplayAvatar = computed(() => {
  const isOther = props.message.sender === 'other';

  const isLastOrSingle = ['single', 'last'].includes(props.positionInGroup);

  return isOther && isLastOrSingle && !isEmojiOnly(props.message.content || '');
});

// --- LOGIKA TREŚCI ---
const isGroupedImage = computed(() => {
  return isImageMessage(props.message) && (props.message.mediaUrls?.length ?? 0) > 0;
});

const isSingleImageOrGif = computed(() => {
  return (isImageMessage(props.message) || isGifMessage(props.message)) && !isGroupedImage.value;
});

const isTextMessage = computed(
  () => props.message.type === 'text' && !isEmojiOnly(props.message.content)
);

// Audio Helper Variables
const visualizerBars = [10, 20, 14, 25, 20, 15, 20, 10];

const getMessageDuration = (message: Message): number => {
  return isAudioMessage(message) ? message.duration : 0;
};


const showReactions = ref(false);
const showReactionsPanel = ref(false);
const reactionsList = ['👍', '❤️', '😂', '😮', '😢', '👏'];

const openReactionsPanel = () => (showReactionsPanel.value = true);
const toggleReactions = () => (showReactions.value = !showReactions.value);

const selectReaction = (emoji: string) => {
  emit('add-reaction', { messageId: props.message.id, emoji });
  showReactions.value = false;
};

const downloadFile = (message: FileMessage) => {
  const link = document.createElement('a');
  link.href = message.fileUrl;
  link.download = message.fileName;
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
};
</script>

<template>
  <div v-if="message.isReply"
    class="flex flex-col"
    :class="{
      'items-start': message.sender === 'other',
      'items-end': message.sender === 'me'
    }"
  >
    <span class="flex flex-row text-[12px] align-center">
      <ReplyIcon :size="12"/>
      <p class="font-semibold">Odpowiadasz {{ message.replyToSender }}</p>
    </span>

    <div
      class="pb-2 text-xs -mb-[15px] bg-gray-100/70 rounded-lg backdrop-blur-sm max-w-[80%] overflow-hidden"
      style="word-break: break-word;"
    >
      <p class="text-gray-700 p-3 text-[11px]">{{ message.replyToContentSnippet }}</p>
    </div>
  </div>

  <div v-if="message.type === 'poll'" class="flex justify-center">
    <MessegePool
      :question="message.pollData.question"
      :initial-options="message.pollData.options"
      :allow-multiple="message.pollData.allowMultiple"
      :allow-add-option="message.pollData.allowAddOption"
    />
  </div>

  <div
    v-else
    class="relative flex items-end group"
    :class="{
      'justify-start': message.sender === 'other',
      'justify-end': message.sender === 'me',
      'mb-1': props.positionInGroup !== 'last' && props.positionInGroup !== 'single',
      'mb-3': props.positionInGroup === 'last' || props.positionInGroup === 'single'
    }"
  >
    <div
      v-if="shouldDisplayAvatar"
      class="w-7 h-7 rounded-full bg-blue-100 flex items-center justify-center mr-2 shrink-0"
    >
      <span>😊</span>
    </div>
    <div v-else-if="message.sender === 'other'" class="w-9"></div>

    <div
      class="flex items-center w-full"
      :class="{
        'flex-row': message.sender === 'other',
        'flex-row-reverse': message.sender === 'me'
      }"
    >
      <div class="relative flex flex-col flex-items max-w-[60%]">

        <div
          v-if="isEmojiOnly(message.content)"
          :class="{
            'emoji-size-small': message.iconSizeState === 'small',
            'emoji-size-medium': message.iconSizeState === 'medium',
            'emoji-size-large': message.iconSizeState === 'large',
            'emoji-size-default': !message.iconSizeState || message.iconSizeState === 'default'
          }"
        >
          <p>{{ message.content }}</p>
        </div>

        <div v-else-if="isGroupedImage && isImageMessage(message)" class="mb-2">
          <div
            class="grid gap-1 rounded-xl overflow-hidden"
            :class="{
              'grid-cols-2': (message.mediaUrls?.length ?? 0) >= 2,
              'grid-cols-1': (message.mediaUrls?.length ?? 0) === 1,
              'rounded-tl-none': message.sender === 'other',
              'rounded-br-none': message.sender === 'me'
            }"
          >
            <img
              v-for="(url, i) in message.mediaUrls"
              :key="i"
              :src="url"
              class="object-cover w-full h-full cursor-pointer"
              :class="{ 'aspect-square': (message.mediaUrls?.length ?? 0) > 1 }"
              @click="emit('open-lightbox', url)"
            />
          </div>
        </div>

        <div v-else-if="isSingleImageOrGif" class="mb-2">
           <img
            :src="(message as ImageMessage).imageUrl"
            class="max-w-full h-auto rounded-lg cursor-pointer"
            :class="{ 'border-2 border-purple-400': message.type === 'gif' }"
            @click="emit('open-lightbox', (message as ImageMessage).imageUrl)"
          />
        </div>

        <div
          v-else-if="isAudioMessage(message)"
          class="flex items-center w-full min-w-[180px] space-x-2 p-2 rounded-full h-10"
          :class="{
            'bg-purple-600 text-white rounded-tl-none': message.sender === 'other',
            'bg-blue-500 text-white rounded-br-none': message.sender === 'me'
          }"
        >
          <audio
            :src="message.audioUrl"
            class="hidden"
            :id="`audio-${message.id}`"
            preload="metadata"
          ></audio>

          <button
            @click="emit('toggle-audio-playback', message)"
            class="w-7 h-7 rounded-full bg-white flex items-center justify-center shrink-0"
            :class="{
              'text-purple-600': message.sender === 'other',
              'text-blue-500': message.sender === 'me'
            }"
          >
            <template v-if="audioStates[message.id]?.isPlaying">
              <PauseIcon :size="16" />
            </template>
            <template v-else>
              <PlayIcon :size="16" />
            </template>
          </button>

          <div class="flex-grow h-6 relative overflow-hidden rounded-full cursor-pointer">
            <div class="absolute inset-0 flex items-center justify-between space-x-0.5 h-full px-1">
              <div
                v-for="(height, index) in visualizerBars"
                :key="index"
                class="rounded-full shrink-0 z-10 transition-colors duration-300"
                :style="{
                  height: height + 'px',
                  width: '3px',
                  backgroundColor: audioStates[message.id]?.isPlaying
                    ? ((audioStates[message.id]?.currentTime || 0) /
                        (message.duration || 1) * 100 >
                        ([0, 12, 25, 37, 50, 62, 75, 87][index] + 6)
                        ? 'white'
                        : 'rgba(255,255,255,0.5)')
                    : 'white'
                }"
              ></div>

              <div
                v-if="audioStates[message.id]?.isPlaying"
                class="absolute top-0 bottom-0 w-[3px] bg-white z-20 transition-left duration-100"
                :style="{
                  left: ((audioStates[message.id]?.currentTime || 0) / (message.duration || 1) * 100) + '%'
                }"
              ></div>
            </div>
          </div>

          <span class="text-xs font-semibold">
            <template v-if="audioStates[message.id]?.isPlaying">
              {{ formatSeconds(audioStates[message.id]?.currentTime || 0) }}
            </template>
            <template v-else>
              {{ formatSeconds(message.duration) }}
            </template>
          </span>
        </div>

        <div
          v-else-if="isFileMessage(message)"
          class="p-3 bg-gray-200 rounded-xl flex items-center gap-2 cursor-pointer hover:bg-gray-300"
          @click="downloadFile(message)"
        >
          <FileIcon :size="22" />
          <div>
            <p class="text-sm font-semibold">{{ message.fileName }}</p>
            <p class="text-xs text-gray-600">{{ useFileSize(message.fileSize) }}</p>
          </div>
        </div>

        <div v-else-if="isVideoMessage(message)">
          <PlayerVideo :url="message.videoUrl" />
        </div>

        <div
          v-else-if="isTextMessage"
          class="relative p-3 text-sm shadow-md break-words"
          :class="[
            positionClasses,
            message.sender === 'other'
              ? 'bg-purple-600 text-white'
              : 'bg-theme-bg-secondary text-theme-text border border-theme-border'
          ]"
        >
          <p>{{ message.content }}</p>
        </div>

        <div
          @click.stop="openReactionsPanel"
          v-if="message.reactions?.length"
          class="absolute bottom-0 right-0 cursor-pointer translate-x-1/2 translate-y-1/2 bg-white rounded-full w-6 h-6 flex items-center justify-center shadow-md text-sm border border-gray-200 z-10"
        >
          {{ message.reactions.at(-1) }}
        </div>

        <div
          v-if="showReactions"
          class="absolute bottom-full mb-2 left-0 flex gap-1 p-2 bg-white rounded-full shadow-lg z-20 border border-gray-100"
        >
          <span
            v-for="emoji in reactionsList"
            :key="emoji"
            class="cursor-pointer text-lg hover:scale-125 transition-transform"
            @click="selectReaction(emoji)"
          >
            {{ emoji }}
          </span>
        </div>
      </div>

      <div class="flex flex-row items-center gap-2 mx-2 opacity-0 group-hover:opacity-100 transition-opacity">
        <button
          @click="toggleReactions"
          class="w-7 h-7 rounded-full bg-gray-200 hover:bg-gray-300 flex items-center justify-center"
        >
          <span>😊</span>
        </button>
        <button
          @click="emit('reply', message)"
          class="w-7 h-7 rounded-full bg-gray-200 hover:bg-gray-300 flex items-center justify-center"
        >
          <ReplyIcon :size="18" />
        </button>
      </div>
    </div>
  </div>

  <Modal v-if="showReactionsPanel" @close="showReactionsPanel = false" title="Reakcje na wiadomości">
    <ReactionPanel />
  </Modal>
</template>

<style scoped>
.emoji-size-default { font-size: 1.75rem; }
.emoji-size-small { font-size: 45px; }
.emoji-size-medium { font-size: 60px; }
.emoji-size-large { font-size: 80px; }
.bg-purple-600 { background-color: #8B5CF6; }
.group-hover\:flex { display: none; }
.group:hover .group-hover\:flex { display: flex; }
</style>
