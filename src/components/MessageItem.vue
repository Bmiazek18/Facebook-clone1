<script setup lang="ts">
import { ref, computed } from 'vue';
import PlayIcon from 'vue-material-design-icons/Play.vue';
import PauseIcon from 'vue-material-design-icons/Pause.vue';
import PhoneMissedIcon from 'vue-material-design-icons/PhoneMissed.vue';
import PhoneIcon from 'vue-material-design-icons/Phone.vue';
import VideoIcon from 'vue-material-design-icons/Video.vue';

import MessegePool from './MessegePool.vue';
import BaseModal from './BaseModal.vue';
import PlayerVideo from './PlayerVideo.vue';
import ReactionPanel from './ReactionPanel.vue';
import MessageReplyContext from './MessageReplyContext.vue';
import MessageMediaGallery from './MessageMediaGallery.vue';
import MessageFileAttachment from './MessageFileAttachment.vue';
import MessageReactions from './MessageReactions.vue';

// Types
import type { Message, ImageMessage, GifMessage, AudioMessage, FileMessage, VideoMessage, AudioState, LinkMessage } from '@/types/Message';

interface Theme {
  id?: string;
  sentBubbleColor?: string;
}

interface ImageMessageWithGroup extends ImageMessage { mediaUrls?: string[]; }

const props = defineProps<{
  message: Message;
  index: number;
  positionInGroup: 'single' | 'first' | 'middle' | 'last';
  audioStates: Record<number, AudioState>;
  currentTheme?: Theme;
}>();

const emit = defineEmits<{
  (e: 'open-lightbox', url: string): void;
  (e: 'toggle-audio-playback', message: Message): void;
  (e: 'reply', message: Message): void;
  (e: 'add-reaction', payload: { messageId: number; emoji: string }): void;
}>();

// --- TYPE GUARDS ---
const isAudioMessage = (msg: Message): msg is AudioMessage => msg.type === 'audio';
const isImageMessage = (msg: Message): msg is ImageMessageWithGroup => msg.type === 'image';
const isFileMessage = (msg: Message): msg is FileMessage => msg.type === 'file';
const isVideoMessage = (msg: Message): msg is VideoMessage => msg.type === 'video';
const isGifMessage = (msg: Message): msg is GifMessage => msg.type === 'gif';
const isCallMessage = (msg: Message): boolean => msg.type === 'call';
const isCallRejectedMessage = (msg: Message): boolean => msg.type === 'call_rejected';
const isLinkMessage = (msg: Message): msg is LinkMessage => msg.type === 'link';
const isAnyCallType = (msg: Message): boolean => isCallMessage(msg) || isCallRejectedMessage(msg);
const isTextMessage = (msg: Message): boolean => msg.type === 'text' && !isEmojiOnly(msg.content);

const isMe = computed(() => props.message.sender === 'me');

const isEmojiOnly = (content: string): boolean => {
  if (!content?.trim()) return false;
  const nonEmojiChars = content.replace(/(\u00a9|\u00ae|[\u2000-\u3300]|\ud83c[\ud000-\udfff]|\ud83d[\ud000-\udfff]|\ud83e[\ud000-\udfff])/g, '').trim();
  return nonEmojiChars.length === 0;
};

// Helper do wykrywania wideo wewnątrz template
const isVideoCall = (msg: Message) => {
  return msg.content?.toLowerCase().includes('wideo') || msg.content?.toLowerCase().includes('video');
};

// --- STYLING LOGIC ---
const bubbleRadiusClass = computed(() => {
  const map = {
    single: 'rounded-xl',
    first: isMe.value ? 'rounded-l-xl rounded-tr-xl rounded-br-[4px]' : 'rounded-r-xl rounded-tl-xl rounded-bl-[4px]',
    middle: isMe.value ? 'rounded-l-xl rounded-r-[4px]' : 'rounded-r-xl rounded-l-[4px]',
    last: isMe.value ? 'rounded-l-xl rounded-tr-[4px] rounded-br-xl' : 'rounded-r-xl rounded-tl-[4px] rounded-bl-xl',
  };
  return map[props.positionInGroup] || 'rounded-xl';
});

const bubbleColorClass = computed(() => {
  if (!isMe.value) return 'bg-gray-200 text-black';
  return (props.currentTheme?.sentBubbleColor || 'bg-blue-500') + ' text-white border border-white/10';
});

const shouldDisplayAvatar = computed(() => {
  return props.message.sender === 'other' &&
         ['single', 'last'].includes(props.positionInGroup) &&
         !isEmojiOnly(props.message.content || '');
});

// --- MEDIA LOGIC ---
const isGroupedImage = computed(() => isImageMessage(props.message) && (props.message.mediaUrls?.length ?? 0) > 0);
const isSingleImageOrGif = computed(() => (isImageMessage(props.message) || isGifMessage(props.message)) && !isGroupedImage.value);

// --- CALL LOGIC HELPERS ---

// Helper do formatowania czasu trwania
const formatCallDuration = (duration: string | number | undefined) => {
  if (!duration) return 'Zakończono';
  if (typeof duration === 'string') return duration;

  const minutes = Math.floor(duration / 60);
  const seconds = duration % 60;

  if (minutes > 0) return `${minutes} min`;
  return `${seconds} s`;
};

// Tylko stylizacja ikony (kolor i komponent) - napisy są w template
const callStyle = computed(() => {
  if (isCallRejectedMessage(props.message)) {
    return {
      icon: PhoneMissedIcon,
      // Czerwone tło tylko jeśli to "nieodebrane" od kogoś innego
      iconBgClass: !isMe.value ? 'bg-red-500 text-white' : 'bg-gray-200 text-gray-500'
    };
  }

  // Zwykłe połączenie (Call)
  const isVideo = isVideoCall(props.message);
  return {
    icon: isVideo ? VideoIcon : PhoneIcon,
    iconBgClass: 'bg-gray-200 text-gray-800'
  };
});


// --- AUDIO VISUALIZER LOGIC ---
const visualizerBars = [10, 20, 14, 25, 20, 15, 20, 10];
const VISUALIZER_THRESHOLDS = [0, 12, 25, 37, 50, 62, 75, 87];

const formatSeconds = (seconds: number): string => {
  if (isNaN(seconds) || seconds < 0) return '0:00';
  const m = Math.floor(seconds / 60);
  const s = Math.floor(seconds % 60);
  return `${m}:${s.toString().padStart(2, '0')}`;
};
 function extractDomain(url: string): string {
    try {
      const hostname = new URL(url).hostname;
      return hostname.startsWith('www.') ? hostname.substring(4) : hostname;
    } catch (error) {
      console.error('Error extracting domain from URL:', url, error);
      return url;
    }
  }

const getAudioBarStyle = (msgId: number, index: number, duration: number) => {
  const state = props.audioStates[msgId];
  const isPlaying = state?.isPlaying;
  const progressPercent = state ? (state.currentTime / (duration || 1)) * 100 : 0;
  const threshold = VISUALIZER_THRESHOLDS[index] ?? 0;
  const isActive = isPlaying && progressPercent > (threshold + 6);

  return {
    height: `${visualizerBars[index]}px`,
    width: '3px',
    backgroundColor: isActive || !isPlaying ? 'white' : 'rgba(255,255,255,0.5)'
  };
};

const getPlaybackIndicatorStyle = (msgId: number, duration: number) => {
  const state = props.audioStates[msgId];
  const leftPos = state ? (state.currentTime / (duration || 1)) * 100 : 0;
  return { left: `${leftPos}%` };
};

// --- HANDLERS ---
const showReactionsPanel = ref(false);
const openReactionsPanel = () => (showReactionsPanel.value = true);
const handleReply = () => emit('reply', props.message);
const handleAddReaction = (payload: { messageId: number; emoji: string }) => emit('add-reaction', payload);
const toggleAudio = (msg: Message) => emit('toggle-audio-playback', msg);

</script>

<template>
  <MessageReplyContext v-if="message.isReply" :reply="message" />

  <div v-if="message.type === 'poll'" class="flex justify-center mb-4">
    <MessegePool
      :question="message.pollData.question"
      :initial-options="message.pollData.options"
      :allow-multiple="message.pollData.allowMultiple"
      :allow-add-option="message.pollData.allowAddOption"
    />
  </div>

  <div
    v-else
    class="relative flex items-end group transition-all duration-200"
    :class="{
      'justify-start': !isMe,
      'justify-end': isMe,
      'mb-1': positionInGroup !== 'last' && positionInGroup !== 'single',
      'mb-3': positionInGroup === 'last' || positionInGroup === 'single'
    }"
  >
    <div
      v-if="shouldDisplayAvatar"
      class="w-8 h-8 rounded-full bg-blue-100 flex items-center justify-center mr-2 shrink-0 overflow-hidden shadow-sm"
    >
      <img src="https://ui-avatars.com/api/?name=User&background=random" alt="Avatar" class="w-full h-full object-cover"/>
    </div>
    <div v-else-if="!isMe" class="w-10"></div>

    <div
      class="flex items-center max-w-[75%]"
      :class="{ 'flex-row': !isMe, 'flex-row-reverse': isMe }"
    >
      <div class="relative flex flex-col overflow-visible">

        <div
          v-if="isEmojiOnly(message.content)"
          class="leading-none select-none transition-transform hover:scale-110"
          :class="{
            'text-[3rem]': !message.iconSizeState || message.iconSizeState === 'default',
            'text-[45px]': message.iconSizeState === 'small',
            'text-[60px]': message.iconSizeState === 'medium',
            'text-[80px]': message.iconSizeState === 'large',
          }"
        >
          {{ message.content }}
        </div>

        <div
          v-else-if="isAnyCallType(message)"
          class="flex items-center p-3 bg-white rounded-2xl shadow-sm min-w-[240px] border border-gray-100"
        >
          <div
            class="w-10 h-10 rounded-full flex items-center justify-center shrink-0 mr-3"
            :class="callStyle.iconBgClass"
          >
            <component :is="callStyle.icon" :size="20" />
          </div>

          <div class="flex flex-col text-gray-800">
            <span class="font-bold text-[15px] leading-tight">
              <span v-if="isCallRejectedMessage(message)">Nieodebrane połączenie głosowe</span>
              <span v-else-if="isVideoCall(message)">Rozmowa wideo</span>
              <span v-else>Połączenie głosowe</span>
            </span>

            <span class="text-xs text-gray-500 mt-1">
              <span v-if="isCallRejectedMessage(message)">
                {{ message.timestamp ? message.timestamp.slice(0, 5) : '16:13' }}
              </span>
              <span v-else>
                {{ formatSeconds(message.duration) }}
              </span>
            </span>
          </div>
        </div>

        <MessageMediaGallery
          v-else-if="isGroupedImage && isImageMessage(message)"
          :media-urls="message.mediaUrls || []"
          :is-me="isMe"
          @open-lightbox="emit('open-lightbox', $event)"
        />

        <div v-else-if="isSingleImageOrGif" class="mb-1">
          <img
            :src="(message as ImageMessage).imageUrl"
            class="max-w-full h-auto rounded-xl shadow-sm cursor-pointer hover:opacity-95 transition-opacity"
            :class="{ 'border-2 border-purple-400': message.type === 'gif' }"
            @click="emit('open-lightbox', (message as ImageMessage).imageUrl)"
            alt="Attachment"
            loading="lazy"
          />
        </div>

        <div
          v-else-if="isAudioMessage(message)"
          class="flex items-center w-full min-w-[200px] space-x-3 p-2.5 rounded-full h-12 shadow-sm transition-colors"
          :class="bubbleColorClass"
        >
          <audio
            :src="message.audioUrl"
            class="hidden"
            :id="`audio-${message.id}`"
            preload="metadata"
          ></audio>

          <button
            @click="toggleAudio(message)"
            class="w-8 h-8 rounded-full bg-white text-blue-600 flex items-center justify-center shrink-0 shadow-sm hover:scale-105 transition-transform"
          >
            <PauseIcon v-if="audioStates[message.id]?.isPlaying" :size="18" />
            <PlayIcon v-else :size="18" class="ml-0.5" />
          </button>

          <div class="flex-grow h-8 relative overflow-hidden flex items-center cursor-pointer" @click="toggleAudio(message)">
            <div class="flex items-center justify-between space-x-[2px] w-full px-1">
              <div
                v-for="(height, idx) in visualizerBars"
                :key="idx"
                class="rounded-full shrink-0 transition-colors duration-200"
                :style="getAudioBarStyle(message.id, idx, message.duration)"
              ></div>
            </div>
            <div
              v-if="audioStates[message.id]?.isPlaying"
              class="absolute top-0 bottom-0 w-[2px] bg-white/80 shadow-[0_0_5px_rgba(255,255,255,0.8)] transition-all duration-100 ease-linear"
              :style="getPlaybackIndicatorStyle(message.id, message.duration)"
            ></div>
          </div>

          <span class="text-xs font-bold tabular-nums pr-2 select-none min-w-[35px] text-right">
            {{ formatSeconds(audioStates[message.id]?.isPlaying ? (audioStates[message.id]?.currentTime || 0) : message.duration) }}
          </span>
        </div>

        <MessageFileAttachment
          v-else-if="isFileMessage(message)"
          :message="message"
          :class="isMe ? 'text-white' : 'text-gray-900'"
        />

        <div v-else-if="isVideoMessage(message)" class="rounded-xl overflow-hidden shadow-sm">
          <PlayerVideo :url="message.videoUrl" />
        </div>

       <div
          v-else-if="isLinkMessage(message)"
          class="flex flex-col overflow-hidden rounded-xl shadow-sm min-w-[250px] max-w-full border border-gray-200 "
        >
          <a
            :href="message.url"
            target="_blank"
            rel="noopener noreferrer"
            :class="[(props.currentTheme?.sentBubbleColor || 'bg-blue-500') ]"

            class="block  p-3 text-white no-underline hover:underline break-all text-sm font-medium"
          >
            {{ message.url }}
          </a>

          <div class="bg-gray-100 px-3 py-2 flex items-center justify-between">
            <span class="text-black font-bold text-[15px]">
              {{  extractDomain(message.url) }}
            </span>
            <LinkVariantIcon :size="16" class="text-gray-400 opacity-50"/>
          </div>
        </div>

        <div
          v-else-if="isTextMessage(message)"
          class="relative px-4 py-2 text-[15px] leading-relaxed shadow-sm break-words max-w-full"
          :class="[bubbleRadiusClass, bubbleColorClass]"
        >
          <p>{{ message.content }}</p>
        </div>

        <div
          v-if="message.reactions?.length && !isAnyCallType(message)"
          @click.stop="openReactionsPanel"
          class="absolute -bottom-2 cursor-pointer bg-white rounded-full px-1.5 py-0.5 min-w-[24px] h-6 flex items-center justify-center shadow-md border border-gray-100 z-10 transition-transform hover:scale-110"
          :class="isMe ? 'right-0 translate-y-0' : 'left-0 translate-y-0'"
        >
          <span class="text-xs leading-none">{{ message.reactions[message.reactions.length - 1] }}</span>
          <span v-if="message.reactions.length > 1" class="text-[9px] font-bold text-gray-500 ml-0.5">{{ message.reactions.length }}</span>
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
      />
    </div>
  </div>

  <BaseModal v-if="showReactionsPanel" @close="showReactionsPanel = false" title="Reakcje">
    <ReactionPanel />
  </BaseModal>
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
