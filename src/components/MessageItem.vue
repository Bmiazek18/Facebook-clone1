<script setup lang="ts">
import { ref, computed } from 'vue';
import ThumbUpIcon from 'vue-material-design-icons/ThumbUp.vue';
import PlayIcon from 'vue-material-design-icons/Play.vue';
import PauseIcon from 'vue-material-design-icons/Pause.vue';

import type { Message, ImageMessage, GifMessage, AudioState } from '@/types/Message';

interface ImageMessageWithOptionalGroup extends ImageMessage {
  mediaUrls?: string[];
}

const props = defineProps<{
  message: Message;
  index: number;
  audioStates: Record<number, AudioState>;
}>();

const emit = defineEmits<{
  (e: 'open-lightbox', url: string): void;
  (e: 'toggle-audio-playback', message: Message): void;
  (e: 'add-reaction', payload: { messageId: number; emoji: string }): void;
}>();

const getMessageDuration = (message: Message): number => {
  return message.type === 'audio' ? (message as any).duration : 0;
};

const formatSeconds = (seconds: number): string => {
  const minutes = Math.floor(seconds / 60);
  const secs = Math.floor(seconds % 60);
  if (isNaN(seconds) || seconds < 0) return '0:00';
  return `${minutes}:${secs.toString().padStart(2, '0')}`;
};

const isEmojiOnly = (content: string): boolean => {
  if (!content.trim()) return false;
  const strippedContent = content
    .trim()
    .replace(
      /(\u00a9|\u00ae|[\u2000-\u3300]|\ud83c[\ud000-\udfff]|\ud83d[\ud000-\udfff]|\ud83e[\ud000-\udfff])/g,
      ''
    );
  return strippedContent.trim().length === 0 && content.trim().length > 0;
};

const audioProgressWidth = computed(() => {
  const state = props.audioStates[props.message.id];
  const duration = getMessageDuration(props.message);
  if (!state || duration === 0) return '0%';
  return `${((state.currentTime || 0) / duration) * 100}%`;
});

const shouldDisplayAvatar = computed(() => {
  return props.message.sender === 'other' && !isEmojiOnly(props.message.content);
});

const isGroupedImage = computed(() => {
  const imageMsg = props.message as ImageMessageWithOptionalGroup;
  return props.message.type === 'image' && imageMsg.mediaUrls && imageMsg.mediaUrls.length > 0;
});

const isSingleImageOrGif = computed(() => {
  return (props.message.type === 'image' || props.message.type === 'gif') && !isGroupedImage.value;
});

const isAudioMessage = computed(() => {
  return props.message.type === 'audio';
});

const isTextMessage = computed(() => {
  return props.message.type === 'text' && !isEmojiOnly(props.message.content);
});

// --- Reakcje ---
const showReactions = ref(false);
const reactionsList = ['👍', '❤️', '😂', '😮', '😢', '👏'];

const toggleReactions = () => {
  showReactions.value = !showReactions.value;
};

const selectReaction = (emoji: string) => {
  emit('add-reaction', { messageId: props.message.id, emoji });
  showReactions.value = false;
};
</script>

<template>
<div class="relative flex items-end group" :class="{ 'justify-start': message.sender === 'other', 'justify-end': message.sender === 'me' }">

  <!-- Avatar -->
  <div v-if="shouldDisplayAvatar"
       class="w-7 h-7 rounded-full bg-blue-100 flex items-center justify-center mr-2">
    <span>😊</span>
  </div>

  <div class="relative max-w-[70%]">

    <!-- Wiadomość -->
    <div v-if="isEmojiOnly(message.content)"
         :class="{
            'emoji-size-small': message.iconSizeState === 'small',
            'emoji-size-medium': message.iconSizeState === 'medium',
            'emoji-size-large': message.iconSizeState === 'large',
            'emoji-size-default': message.iconSizeState === 'default' || !message.iconSizeState
         }"
         class="p-0 wrap-break-word">
      <p>{{ message.content }}</p>
    </div>

    <div v-else-if="isGroupedImage" class="mb-2">
      <div class="grid gap-1 rounded-xl overflow-hidden"
           :class="{
              'grid-cols-2': (message as ImageMessageWithOptionalGroup).mediaUrls!.length >= 2,
              'grid-cols-1': (message as ImageMessageWithOptionalGroup).mediaUrls!.length === 1,
              'rounded-tl-none': message.sender === 'other',
              'rounded-br-none': message.sender === 'me'
           }">
        <img v-for="(url, mediaIndex) in (message as ImageMessageWithOptionalGroup).mediaUrls" :key="mediaIndex"
             :src="url"
             alt="Zdjęcie grupowe"
             class="w-full h-full object-cover cursor-pointer"
             :class="{ 'aspect-square': (message as ImageMessageWithOptionalGroup).mediaUrls!.length > 1 }"
             @click="emit('open-lightbox', url)" />
      </div>
    </div>

    <div v-else-if="isSingleImageOrGif" class="mb-2">
      <img
        :src="(message as ImageMessage).imageUrl"
        :alt="message.type === 'gif' ? 'Animated GIF' : 'Image'"
        class="max-w-full h-auto rounded-lg cursor-pointer"
        :class="{ 'border-2 border-purple-400': message.type === 'gif' }"
        @click="emit('open-lightbox', (message as ImageMessage).imageUrl)"
      />
    </div>

    <div
  v-else-if="isAudioMessage"
  class="flex items-center w-full min-w-[180px] space-x-2 p-2 rounded-full h-10"
  :class="{
    'bg-purple-600 text-white rounded-tl-none': message.sender === 'other',
    'bg-blue-500 text-white rounded-br-none': message.sender === 'me'
  }"
  style="padding: 6px 10px;"
>
  <audio
    :src="message.audioUrl"
    :id="`audio-${message.id}`"
    class="hidden"
    preload="metadata"
  ></audio>

  <button
    @click="emit('toggle-audio-playback', message)"
    class="w-7 h-7 rounded-full flex items-center justify-center shrink-0 transition"
    :class="{
      'bg-white text-purple-600': message.sender === 'other',
      'bg-white text-blue-500': message.sender === 'me'
    }"
  >
    <template v-if="audioStates[message.id]?.isPlaying">
      <PauseIcon :size="16" />
    </template>
    <template v-else>
      <PlayIcon :size="16" :fill="true" class="ml-0.5" />
    </template>
  </button>

  <div
    class="flex-grow h-6 relative overflow-hidden rounded-full cursor-pointer"
    :class="{ 'opacity-80': audioStates[message.id]?.isPlaying }"
  >


<div class="absolute inset-0 flex items-center justify-between space-x-0.5 h-full px-1">
  <div
    v-for="(height, index) in [10, 20, 14, 25, 20, 15, 20, 10]"
    :key="index"
    class="rounded-full shrink-0 z-10 transition-colors duration-300"
    :style="{
      height: `${height}px`,
      width: '3px',
      backgroundColor: audioStates[message.id]?.isPlaying
        ? ((audioStates[message.id]?.currentTime || 0) / (getMessageDuration(message) || 1) * 100 > ([0, 12, 25, 37, 50, 62, 75, 87][index] + 6)
            ? 'white' : 'gray')
        : 'white'
    }"
  ></div>
  <div
  v-if="audioStates[message.id]?.isPlaying"
    class="absolute top-0 bottom-0 w-[3px] bg-white z-20 transition-left duration-100 linear"
    :style="{
      left: `${(audioStates[message.id]?.currentTime || 0) / (getMessageDuration(message) || 1) * 100}%`
    }"
  ></div>
</div>



  </div>

  <span class="text-xs font-semibold shrink-0 text-white">
    <template v-if="audioStates[message.id]?.isPlaying">
      {{ formatSeconds(audioStates[message.id]?.currentTime || 0) }}
    </template>
    <template v-else>
      {{ formatSeconds(getMessageDuration(message)) }}
    </template>
  </span>
</div>


    <div v-else-if="isTextMessage"
         class="relative p-3 text-sm rounded-xl shadow-md break-words"
         :class="{
            'bg-purple-600 text-white rounded-tl-none': message.sender === 'other',
            'bg-theme-bg-secondary text-theme-text border border-theme-border rounded-br-none': message.sender === 'me'
         }"
    >
      <p>{{ message.content }}</p>
    </div>

    <!-- Ostatnia reakcja w prawym dolnym rogu -->
    <div v-if="message.reactions && message.reactions.length"
         class="absolute bottom-0 right-0 transform translate-x-1/2 translate-y-1/2 bg-white rounded-full w-6 h-6 flex items-center justify-center shadow-md text-sm">
      {{ message.reactions[message.reactions.length - 1] }}
    </div>

    <!-- Panel wyboru reakcji -->
    <div v-if="showReactions"
         class="absolute bottom-full mb-2 left-0 flex gap-1 p-1 bg-white rounded-full shadow-md z-10">
      <span v-for="emoji in reactionsList"
            :key="emoji"
            class="cursor-pointer text-lg hover:scale-110 transition"
            @click="selectReaction(emoji)">
        {{ emoji }}
      </span>
    </div>
  </div>

  <!-- Przycisk wyboru reakcji -->
  <button
          @click="toggleReactions"
          class=" absolute w-7 h-7 rounded-full group-hover:flex flex items-center justify-center bg-gray-200 hover:bg-gray-300 transition"
          :class="message.sender === 'me' ? 'left-[20px]' : 'right-[20px]' ">
    😊
  </button>

</div>
</template>

<style scoped>
.emoji-size-default { font-size: 1.75rem; }
.emoji-size-small { font-size: 45px; }
.emoji-size-medium { font-size: 60px; }
.emoji-size-large { font-size: 80px; }
.bg-purple-600 { background-color: #8B5CF6; }

/* Pokazuje przycisk tylko na hover nad wiadomością */
.group-hover\:flex {
  display: none;
}
.group:hover .group-hover\:flex {
  display: flex;
}
</style>
