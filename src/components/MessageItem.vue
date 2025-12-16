<script setup lang="ts">
import { ref, computed } from 'vue';
import PlayIcon from 'vue-material-design-icons/Play.vue';
import PauseIcon from 'vue-material-design-icons/Pause.vue';
import type { Message, ImageMessage, GifMessage, AudioMessage, FileMessage, VideoMessage, AudioState } from '@/types/Message';
import MessegePool from './MessegePool.vue';
import Modal from './Modal.vue';
import PlayerVideo from './PlayerVideo.vue';
import ReactionPanel from './ReactionPanel.vue';
import MessageReplyContext from './MessageReplyContext.vue';
import MessageMediaGallery from './MessageMediaGallery.vue';
import MessageFileAttachment from './MessageFileAttachment.vue';
import MessageReactions from './MessageReactions.vue';
// ----------------------------------------

interface ImageMessageWithGroup extends ImageMessage { mediaUrls?: string[]; }

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


// --- TYPE GUARDS i COMPUTED PROPERTIES ---

const isAudioMessage = (msg: Message): msg is AudioMessage => msg.type === 'audio';
const isImageMessage = (msg: Message): msg is ImageMessageWithGroup => msg.type === 'image';
const isFileMessage = (msg: Message): msg is FileMessage => msg.type === 'file';
const isVideoMessage = (msg: Message): msg is VideoMessage => msg.type === 'video';
const isGifMessage = (msg: Message): msg is GifMessage => msg.type === 'gif';

const isMe = computed(() => props.message.sender === 'me');

const isEmojiOnly = (content: string): boolean => {
  if (!content?.trim()) return false;
  const strippedContent = content.trim().replace(
    /(\u00a9|\u00ae|[\u2000-\u3300]|\ud83c[\ud000-\udfff]|\ud83d[\ud000-\udfff]|\ud83e[\ud000-\udfff])/g,
    ''
  );
  return strippedContent.trim().length === 0 && content.trim().length > 0;
};

const positionClasses = computed(() => {
  const mappings: Record<string, string> = {
    single: 'rounded-xl',
    first: isMe.value
      ? 'rounded-tl-xl rounded-tr-xl rounded-bl-xl rounded-br-[4px]'
      : 'rounded-tl-xl rounded-tr-xl rounded-br-xl rounded-bl-[4px]',
    middle: isMe.value
      ? 'rounded-tl-xl rounded-bl-xl rounded-tr-[4px] rounded-br-[4px]'
      : 'rounded-tr-xl rounded-br-xl rounded-tl-[4px] rounded-bl-[4px]',
    last: isMe.value
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

const isGroupedImage = computed(() => {
  return isImageMessage(props.message) && (props.message.mediaUrls?.length ?? 0) > 0;
});

const isSingleImageOrGif = computed(() => {
  return (isImageMessage(props.message) || isGifMessage(props.message)) && !isGroupedImage.value;
});

const isTextMessage = computed(
  () => props.message.type === 'text' && !isEmojiOnly(props.message.content)
);



const formatSeconds = (seconds: number): string => {
  if (isNaN(seconds) || seconds < 0) return '0:00';
  const minutes = Math.floor(seconds / 60);
  const secs = Math.floor(seconds % 60);
  return `${minutes}:${secs.toString().padStart(2, '0')}`;
};

// Paski wizualizera
const visualizerBars = [10, 20, 14, 25, 20, 15, 20, 10];
// Punkty procentowe, po których dany pasek powinien 'zaświecić się'
const visualizerThresholds = [0, 12, 25, 37, 50, 62, 75, 87];

// Właściwości obliczane na podstawie stanu audio (MessageAudioPlayer logic, adapted)
const audioState = computed(() => props.audioStates[props.message.id]);

const audioIsPlaying = computed(() => audioState.value?.isPlaying ?? false);
const audioCurrentTime = computed(() => audioState.value?.currentTime ?? 0);
const audioDuration = computed(() => (props.message as AudioMessage).duration || 1);

// Właściwość obliczana: Procent odtwarzania (dla białej linii postępu)
const progressPercent = computed(() => {
    return audioDuration.value > 0 ? (audioCurrentTime.value / audioDuration.value) * 100 : 0;
});

// Właściwość obliczana: Czas pozostały (countdown)
const remainingTime = computed(() => {
  const totalDuration = audioDuration.value || 0;
  const current = audioCurrentTime.value;
  // Zaokrąglenie w górę, aby uniknąć wyświetlania '0:-1' i uprościć wyświetlanie
  return Math.max(0, Math.ceil(totalDuration - current));
});

const isOtherSender = computed(() => props.message.sender === 'other');


// --- Lokalne Stany i Handlery ---
const showReactionsPanel = ref(false);
const openReactionsPanel = () => (showReactionsPanel.value = true);

const handleReply = () => emit('reply', props.message);
const handleToggleAudio = () => emit('toggle-audio-playback', props.message);
const handleAddReaction = (payload: { messageId: number; emoji: string }) => emit('add-reaction', payload);


</script>

<template>
  <MessageReplyContext v-if="message.isReply" :reply="message" />

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
      'justify-start': !isMe,
      'justify-end': isMe,
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
    <div v-else-if="!isMe" class="w-9"></div>

    <div
      class="flex items-center w-full"
      :class="{ 'flex-row': !isMe, 'flex-row-reverse': isMe }"
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

        <MessageMediaGallery
          v-else-if="isGroupedImage && isImageMessage(message)"
          :media-urls="(message as ImageMessageWithGroup).mediaUrls || []"
          :is-me="isMe"
          @open-lightbox="emit('open-lightbox', $event)"
        />

        <div v-else-if="isSingleImageOrGif" class="mb-2">
          <img
            :src="(message as ImageMessage).imageUrl"
            class="max-w-full h-auto rounded-lg cursor-pointer"
            :class="{ 'border-2 border-purple-400': message.type === 'gif' }"
            @click="emit('open-lightbox', (message as ImageMessage).imageUrl)"
            alt="Obraz lub GIF"
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
                        (([0, 12, 25, 37, 50, 62, 75, 87][index] ?? 0) + 6)
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

        <MessageFileAttachment
          v-else-if="isFileMessage(message)"
          :message="message as FileMessage"
        />

        <div v-else-if="isVideoMessage(message)">
          <PlayerVideo :url="(message as VideoMessage).videoUrl" />
        </div>

        <div
          v-else-if="isTextMessage"
          class="relative p-3 text-sm shadow-md break-words"
          :class="[
            positionClasses,
            !isMe ? 'bg-purple-600 text-white' : 'bg-theme-bg-secondary text-theme-text border border-theme-border'
          ]"
        >
          <p>{{ message.content }}</p>
        </div>

        <div
          @click.stop="openReactionsPanel"
          v-if="message.reactions?.length"
          class="absolute bottom-0 right-0 cursor-pointer translate-x-1/2 translate-y-1/2 bg-white rounded-full w-6 h-6 flex items-center justify-center shadow-md text-sm border border-gray-200 z-10"
        >
          {{ message.reactions[message.reactions.length - 1] }}
        </div>
      </div>

      <MessageReactions
        :message-id="message.id"
        :reactions="message.reactions"
        @add-reaction="handleAddReaction"
        @open-panel="openReactionsPanel"
        @reply="handleReply"
      />
    </div>
  </div>

  <Modal v-if="showReactionsPanel" @close="showReactionsPanel = false" title="Reakcje na wiadomości">
    <ReactionPanel />
  </Modal>
</template>

<style scoped>
/* Zachowanie stylów dla emoji i tła */
.emoji-size-default { font-size: 1.75rem; }
.emoji-size-small { font-size: 45px; }
.emoji-size-medium { font-size: 60px; }
.emoji-size-large { font-size: 80px; }
.bg-purple-600 { background-color: #8B5CF6; }
.group-hover\:flex { display: none; }
.group:hover .group-hover\:flex { display: flex; }
</style>
