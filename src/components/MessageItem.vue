<script setup lang="ts">
import { ref, computed } from 'vue';
import ThumbUpIcon from 'vue-material-design-icons/ThumbUp.vue';
import PlayIcon from 'vue-material-design-icons/Play.vue';
import PauseIcon from 'vue-material-design-icons/Pause.vue';

import type { Message, ImageMessage, GifMessage, AudioState } from '@/types/Message';
import Modal from './Modal.vue';
import ReactionButton from './ReactionButton.vue';
import ReactionPanel from './ReactionPanel.vue';

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

const getMessageDuration = (message: Message): number =>
  message.type === 'audio' ? (message as any).duration : 0;

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
  return props.message.type === 'image' && imageMsg.mediaUrls?.length;
});

const isSingleImageOrGif = computed(() => {
  return (props.message.type === 'image' || props.message.type === 'gif') && !isGroupedImage.value;
});

const isAudioMessage = computed(() => props.message.type === 'audio');
const isTextMessage = computed(
  () => props.message.type === 'text' && !isEmojiOnly(props.message.content)
);

// Reactions
const showReactions = ref(false);
const reactionsList = ['👍', '❤️', '😂', '😮', '😢', '👏'];
const showReactionsPanel = ref(false);
const closeReactionsPanel = () => {
    showReactionsPanel.value = !showReactionsPanel.value
}
const openReactionsPanel = () => (showReactionsPanel.value = true);
const toggleReactions = () => (showReactions.value = !showReactions.value);

const selectReaction = (emoji: string) => {
  emit('add-reaction', { messageId: props.message.id, emoji });
  showReactions.value = false;
};
</script>

<template>
<div
  class="relative flex items-end group"
  :class="{
    'justify-start': message.sender === 'other',
    'justify-end': message.sender === 'me'
  }"
>

  <!-- Avatar -->
  <div
    v-if="shouldDisplayAvatar"
    class="w-7 h-7 rounded-full bg-blue-100 flex items-center justify-center mr-2"
  >
    <span>😊</span>
  </div>

  <!-- BUBBLE + REACTION BUTTON WRAPPER -->
  <div
    class="flex items-end"
    :class="{
      'flex-row': message.sender === 'other',
      'flex-row-reverse': message.sender === 'me'
    }"
  >
    <!-- Treść wiadomości -->
    <div class="relative  max-w-[60%]">

      <!-- Emoji -->
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

      <!-- Grupa obrazów -->
      <div v-else-if="isGroupedImage" class="mb-2">
        <div
          class="grid gap-1 rounded-xl overflow-hidden"
          :class="{
            'grid-cols-2': (message as any).mediaUrls.length >= 2,
            'grid-cols-1': (message as any).mediaUrls.length === 1,
            'rounded-tl-none': message.sender === 'other',
            'rounded-br-none': message.sender === 'me'
          }"
        >
          <img
            v-for="(url, i) in (message as any).mediaUrls"
            :key="i"
            :src="url"
            class="object-cover w-full h-full cursor-pointer"
            :class="{ 'aspect-square': (message as any).mediaUrls.length > 1 }"
            @click="emit('open-lightbox', url)"
          />
        </div>
      </div>

      <!-- Pojedynczy obraz / GIF -->
      <div v-else-if="isSingleImageOrGif" class="mb-2">
        <img
          :src="(message as ImageMessage).imageUrl"
          class="max-w-full h-auto rounded-lg cursor-pointer"
          :class="{ 'border-2 border-purple-400': message.type === 'gif' }"
          @click="emit('open-lightbox', (message as ImageMessage).imageUrl)"
        />
      </div>

      <!-- Audio -->
      <div
        v-else-if="isAudioMessage"
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
              v-for="(height, index) in [10, 20, 14, 25, 20, 15, 20, 10]"
              :key="index"
              class="rounded-full shrink-0 z-10 transition-colors duration-300"
              :style="{
                height: height + 'px',
                width: '3px',
                backgroundColor: audioStates[message.id]?.isPlaying
                  ? ((audioStates[message.id]?.currentTime || 0) /
                      (getMessageDuration(message) || 1) *
                      100 >
                      ([0, 12, 25, 37, 50, 62, 75, 87][index] + 6)
                      ? 'white'
                      : 'gray')
                  : 'white'
              }"
            ></div>

            <div
              v-if="audioStates[message.id]?.isPlaying"
              class="absolute top-0 bottom-0 w-[3px] bg-white z-20 transition-left duration-100"
              :style="{
                left:
                  (audioStates[message.id]?.currentTime || 0) /
                    (getMessageDuration(message) || 1) *
                    100 + '%'
              }"
            ></div>
          </div>
        </div>

        <span class="text-xs font-semibold">
          <template v-if="audioStates[message.id]?.isPlaying">
            {{ formatSeconds(audioStates[message.id]?.currentTime || 0) }}
          </template>
          <template v-else>
            {{ formatSeconds(getMessageDuration(message)) }}
          </template>
        </span>
      </div>

      <!-- Tekst -->
      <div
        v-else-if="isTextMessage"
        class="relative p-3 text-sm rounded-xl shadow-md break-words"
        :class="{
          'bg-purple-600 text-white rounded-tl-none': message.sender === 'other',
          'bg-theme-bg-secondary text-theme-text border border-theme-border rounded-br-none':
            message.sender === 'me'
        }"
      >
        <p>{{ message.content }}</p>
      </div>

      <!-- Ostatnia reakcja -->
      <div
        @click="openReactionsPanel"
        v-if="message.reactions?.length"
        class="absolute bottom-0 right-0 cursor-pointer translate-x-1/2 translate-y-1/2 bg-white rounded-full w-6 h-6 flex items-center justify-center shadow-md text-sm"
      >
        {{ message.reactions.at(-1) }}
      </div>

      <!-- Panel reakcji -->
      <div
        v-if="showReactions"

        class="absolute bottom-full mb-2 left-0 flex gap-1 p-1 bg-white rounded-full shadow-md z-10"
      >
        <span
          v-for="emoji in reactionsList"
          :key="emoji"
          class="cursor-pointer text-lg hover:scale-110 transition"
          @click="selectReaction(emoji)"
        >
          {{ emoji }}
        </span>
      </div>
    </div>

    <!-- Przycisk reakcji -->
     <button
      @click="toggleReactions"
      class="w-7 h-7 mx-2 rounded-full bg-gray-200 hover:bg-gray-300 hidden  group-hover:flex items-center justify-center transition"
    >
      😊
    </button>
  </div>

</div>
<Modal v-if="showReactionsPanel" @close="()=>showReactionsPanel = false" title="Reackcje na wiadomości">
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
