<script setup lang="ts">
import { ref, onUnmounted, computed, nextTick, onMounted } from 'vue';
import MultiMediaLightbox from './MessageBox/MediaLightbox.vue';
import MessageBoxHeader from './MessageBoxHeader.vue';
import MessageBoxFooter from './MessageBoxFooter.vue';
import MessageItem from './MessageItem.vue';
import type { Message, ImageMessage, AudioState, VideoMessage, AudioMessage as AudioMsg, FileMessage } from '@/types/Message';

interface MediaItem {
  id: number;
  sender: string;
  type: 'image' | 'gif' | 'video';
  content: string;
  time: number;
  imageUrl?: string;
  videoUrl?: string;
}

const createTimestamp = (timeStr: string, daysAgo: number = 0): number => {
  const parts = timeStr.split(':').map(Number);
  const hours = parts[0] ?? 0;
  const minutes = parts[1] ?? 0;
  const date = new Date();
  date.setDate(date.getDate() - daysAgo);
  date.setHours(hours, minutes, 0, 0);
  return date.getTime();
};

const messages = ref<Message[]>([
  {
    id: 1,
    sender: 'other',
    type: 'text',
    content: 'Ja bym zrobił ze w poniedziałek zdalnie i najwyżej we 2 będziemy albo w środę zdalnie zrobic tylko troche późno',
    time: createTimestamp('13:55', 2),
  },
   {
    id: 12,
    sender: 'other',
    type: 'text',
    content: 'Ja222 bym zrobił ze w poniedziałek zdalnie i najwyżej we 2 będziemy albo w środę zdalnie zrobic tylko troche późno',
    time: createTimestamp('13:56', 2),
  },
  {
    id: 2,
    sender: 'me',
    type: 'text',
    content: 'Okej, mi pasuje, ale musimy to dobrze zaplanować, bo inaczej się nie wyrobimy ze wszystkim na czas. Zgadzam się z planem na poniedziałek po południu.',
    time: createTimestamp('14:25', 1),
    isReply: true,
    replyToSender: 'Użytkownik Alan',
    replyToContentSnippet: 'Ja bym zrobił ze w poniedziałek zdalnie i najwyżej we 2 będziemy albo w ...',
  },
  {
    id: 10,
    sender: 'other',
    type: 'text',
    content: 'Spoko! Dzięki za info 😉',
    time: createTimestamp('14:50', 1),
  },
  {
    id: 11,
    sender: 'me',
    type: 'image',
    content: 'Wysłane zdjęcia:',
    time: createTimestamp('15:00', 1),
    imageUrl: 'https://primefaces.org/cdn/primevue/images/galleria/galleria10.jpg',
    mediaUrls: [
      'https://primefaces.org/cdn/primevue/images/galleria/galleria10.jpg',
      'https://primefaces.org/cdn/primevue/images/galleria/galleria11.jpg',
      'https://primefaces.org/cdn/primevue/images/galleria/galleria14.jpg',
    ],
  },
  {
    id: 15,
    sender: 'other',
    type: 'image',
    content: 'Pojedynczy obrazek:',
    time: createTimestamp('15:01', 1),
    imageUrl: 'https://primefaces.org/cdn/primevue/images/galleria/galleria12.jpg',
  },
  {
    id: 12,
    sender: 'other',
    type: 'text',
    content: 'Właśnie wysłałem Ci link do tego nowego frameworka. Zobacz, czy to może nam pomóc przy optymalizacji.',
    time: createTimestamp('15:05', 1),
  },
  {
    id: 13,
    sender: 'other',
    type: 'audio',
    content: 'Wiadomość głosowa (0:15)',
    time: createTimestamp('15:10', 1),
    audioUrl: 'http://example.com/audio/other_voice_message.mp3',
    duration: 15,
  },
  {
    id: 14,
    sender: 'me',
    type: 'text',
    content: 'Oki, zaraz obczaję ten link i odsłucham wiadomość. Wielkie dzięki!',
    time: createTimestamp('15:12', 1),
  },
  {
    id: 16,
    sender: 'me',
    type: 'poll',
    content: 'Utworzono ankietę',
    time: createTimestamp('15:15', 1),
    pollData: {
      question: 'Kiedy robimy calla podsumowującego? 📅',
      allowMultiple: true,
      allowAddOption: true,
      options: [
        { id: 1, text: 'Wtorek 10:00', votes: 2, votedByMe: true },
        { id: 2, text: 'Środa 14:00', votes: 0, votedByMe: false },
        { id: 3, text: 'Piątek rano', votes: 1, votedByMe: false }
      ]
    }
  }
]);

const chatContainer = ref<HTMLElement | null>(null);

function scrollToBottom() {
  nextTick(() => {
    if (chatContainer.value) {
      chatContainer.value.scrollTop = chatContainer.value.scrollHeight;
    }
  });
}

const MAX_TIME_DIFF_MS = 5 * 60 * 1000;
const isLightboxOpen = ref(false);
const currentMediaIndex = ref(0);

const filteredMedia = computed<MediaItem[]>(() => {
  const result: MediaItem[] = [];
  for (const msg of messages.value) {
    if (msg.type === 'video') {
      const videoMsg = msg as VideoMessage;
      result.push({
        id: videoMsg.id,
        sender: videoMsg.sender,
        type: 'video',
        content: videoMsg.content,
        time: videoMsg.time,
        videoUrl: videoMsg.videoUrl,
      });
    } else if (msg.type === 'image' || msg.type === 'gif') {
      const imgMsg = msg as ImageMessage & { mediaUrls?: string[] };
      if (imgMsg.mediaUrls?.length) {
        for (const url of imgMsg.mediaUrls) {
          result.push({
            id: imgMsg.id,
            sender: imgMsg.sender,
            type: imgMsg.type,
            content: imgMsg.content,
            time: imgMsg.time,
            imageUrl: url
          });
        }
      } else if (imgMsg.imageUrl) {
        result.push({
          id: imgMsg.id,
          sender: imgMsg.sender,
          type: imgMsg.type,
          content: imgMsg.content,
          time: imgMsg.time,
          imageUrl: imgMsg.imageUrl
        });
      }
    }
  }
  return result;
});

const openLightbox = (url: string) => {
  const idx = filteredMedia.value.findIndex(msg =>
    msg.videoUrl === url || msg.imageUrl === url
  );
  if (idx !== -1) {
    currentMediaIndex.value = idx;
    isLightboxOpen.value = true;
  }
};

const onKeyDown = (e: KeyboardEvent) => {
  if ((e.key === 'Escape' || e.key === 'Esc') && isLightboxOpen.value) {
    isLightboxOpen.value = false;
  }
};

const audioStates = ref<Record<number, AudioState>>({});

const initializeAudioState = (id: number, audio: HTMLAudioElement, messageDuration?: number) => {
  if (!audioStates.value[id]) {
    const duration = messageDuration ?? (isNaN(audio.duration) ? 0 : audio.duration);
    audioStates.value[id] = { isPlaying: false, duration, currentTime: 0 };
  } else if (audio.duration > 0 && audio.duration !== Infinity) {
    audioStates.value[id].duration = audio.duration;
  }
};

const toggleAudioPlayback = (message: Message) => {
  const audioId = message.id;
  const audioElement = document.getElementById(`audio-${audioId}`) as HTMLAudioElement;
  if (!audioElement) return;

  const messageDuration = message.type === 'audio' ? (message as AudioMsg).duration : undefined;

  if (audioElement.readyState < 1) {
    const onLoaded = () => {
      initializeAudioState(audioId, audioElement, messageDuration);
      toggleAudioPlayback(message);
      audioElement.removeEventListener('loadedmetadata', onLoaded);
    };
    audioElement.addEventListener('loadedmetadata', onLoaded);
    audioElement.load();
    return;
  }

  initializeAudioState(audioId, audioElement, messageDuration);
  const currentState = audioStates.value[audioId];
  if (!currentState) return;

  Object.keys(audioStates.value).forEach(id => {
    const otherId = parseInt(id);
    if (otherId !== audioId && audioStates.value[otherId]?.isPlaying) {
      const otherAudio = document.getElementById(`audio-${otherId}`) as HTMLAudioElement;
      otherAudio?.pause();
      audioStates.value[otherId].isPlaying = false;
    }
  });

  if (currentState.isPlaying) {
    audioElement.pause();
    currentState.isPlaying = false;
  } else {
    if (Math.abs(audioElement.currentTime - audioElement.duration) < 0.5 || audioElement.ended) {
      audioElement.currentTime = 0;
      currentState.currentTime = 0;
    }
    audioElement.play().catch(e => console.error(e));
    currentState.isPlaying = true;

    audioElement.ontimeupdate = () => { currentState.currentTime = audioElement.currentTime; };
    audioElement.onended = () => { currentState.isPlaying = false; currentState.currentTime = 0; };
  }
};

const isSameDay = (t1: number, t2: number): boolean => new Date(t1).toDateString() === new Date(t2).toDateString();
const capitalizeWord = (wyraz: string): string => wyraz ? wyraz.charAt(0).toUpperCase() + wyraz.slice(1).toLowerCase() : '';
const getDayAbbreviation = (timestamp: number): string => new Date(timestamp).toLocaleDateString('pl-PL', { weekday: 'short' }).replace('.', '');
const formatTime = (timestamp: number): string => new Date(timestamp).toLocaleTimeString('pl-PL', { hour: '2-digit', minute: '2-digit' });

const getDisplayTime = (index: number): string | null => {
  const currentMsg = messages.value[index];
  if (!currentMsg) return null;
  const currentTimestamp = currentMsg.time;
  if (index === 0) return `${capitalizeWord(getDayAbbreviation(currentTimestamp))}, ${formatTime(currentTimestamp)}`;

  const prevMsg = messages.value[index - 1];
  if (!prevMsg) return `${capitalizeWord(getDayAbbreviation(currentTimestamp))}, ${formatTime(currentTimestamp)}`;
  const prevTimestamp = prevMsg.time;
  const timeDifference = currentTimestamp - prevTimestamp;
  const isTimeGapBig = timeDifference >= MAX_TIME_DIFF_MS;
  const isNewDay = !isSameDay(currentTimestamp, prevTimestamp);

  if (isNewDay) return `${capitalizeWord(getDayAbbreviation(currentTimestamp))}, ${formatTime(currentTimestamp)}`;
  if (isTimeGapBig) return formatTime(currentTimestamp);
  return null;
};

const getMessagePositionInGroup = (index: number): 'single' | 'first' | 'middle' | 'last' => {
  const current = messages.value[index];
  if (!current) return 'single';
  const prev = messages.value[index - 1];
  const next = messages.value[index + 1];

  const isSameSenderAsPrev = prev && prev.sender === current.sender && (current.time - prev.time) < MAX_TIME_DIFF_MS;
  const isSameSenderAsNext = next && next.sender === current.sender && (next.time - current.time) < MAX_TIME_DIFF_MS;

  if (!isSameSenderAsPrev && !isSameSenderAsNext) return 'single';
  if (!isSameSenderAsPrev && isSameSenderAsNext) return 'first';
  if (isSameSenderAsPrev && isSameSenderAsNext) return 'middle';
  if (isSameSenderAsPrev && !isSameSenderAsNext) return 'last';
  return 'single';
};

onMounted(() => {
  scrollToBottom();
  window.addEventListener('keydown', onKeyDown);
});

onUnmounted(() => {
  window.removeEventListener('keydown', onKeyDown);
  messages.value.forEach(msg => {
    if (msg.type === 'audio') {
      const audioMsg = msg as AudioMsg;
      if (audioMsg.audioUrl?.startsWith('blob:')) {
        URL.revokeObjectURL(audioMsg.audioUrl);
      }
    }
  });
});

// ============= DRAG & DROP =============
const isDragging = ref(false);
const onDragOver = () => { isDragging.value = true; };
const onDragLeave = (e: DragEvent) => {
    if (e.currentTarget && (e.currentTarget as Element).contains(e.relatedTarget as Node)) {
        return;
    }
    isDragging.value = false;
};
const onDrop = (event: DragEvent) => {
  isDragging.value = false;
  const files = event.dataTransfer?.files;
  if (!files?.length) return;

  Array.from(files).forEach(file => {
    const url = URL.createObjectURL(file);
    if (file.type.startsWith('image/')) addDroppedImage(url);
    else if (file.type.startsWith('video/')) addDroppedVideo(url, file);
    else addDroppedFile(url, file);
  });
  scrollToBottom();
};

const addDroppedImage = (url: string) => {
  messages.value.push({ id: Date.now() + Math.random(), sender: 'me', type: 'image', content: 'Wysłano obraz:', time: Date.now(), imageUrl: url } as Message);
};
const addDroppedVideo = (url: string, file: File) => {
  messages.value.push({ id: Date.now() + Math.random(), sender: 'me', type: 'video', content: file.name, time: Date.now(), videoUrl: url } as VideoMessage);
};
const addDroppedFile = (url: string, file: File) => {
  messages.value.push({ id: Date.now() + Math.random(), sender: 'me', type: 'file', content: `Wysłano plik (${file.name})`, time: Date.now(), fileUrl: url, fileName: file.name, fileSize: file.size } as FileMessage);
};

const replyTarget = ref<Message | null>(null);
const setReplyTo = (message: Message) => { replyTarget.value = message; };
const clearReply = () => { replyTarget.value = null; };

const props = withDefaults(defineProps<{ boxId?: string | number; mode?: 'card' | 'full' }>(), { mode: 'card' });

const isFull = computed(() => props.mode === 'full');
const outerClass = computed(() => isFull.value ? 'relative flex-1 flex flex-col h-full w-full' : 'flex items-center relative justify-center py-4 px-2');
const innerClass = computed(() => isFull.value ? 'w-full h-full bg-theme-bg-secondary rounded-none shadow-none flex flex-col overflow-hidden' : 'w-full relative max-w-[328px] h-[455px] bg-theme-bg-secondary rounded-xl shadow-2xl flex flex-col overflow-hidden');
const dragOverlayClass = computed(() => isFull.value ? 'absolute inset-0 z-50 flex items-center justify-center bg-black/50 pointer-events-none' : 'absolute inset-0 h-[400px] top-[55px] z-50 flex items-center justify-center bg-black/50 pointer-events-none');
</script>

<template>
  <div
    :class="outerClass"
    @dragover.prevent="onDragOver"
    @dragleave.prevent="onDragLeave"
    @drop.prevent="onDrop"
  >
    <div :class="innerClass">

      <MessageBoxHeader title="Alan, Jacek" :users="['Alan', 'Jacek']" :boxId="props.boxId ?? 1"/>

      <div
        v-if="isDragging"
        :class="dragOverlayClass"
      >
        <div class="text-white text-lg font-semibold p-4 rounded-lg border-2 border-dashed border-white">
          {{ $t('ui.dragFileToSend') }}
        </div>
      </div>

      <main
        ref="chatContainer"
        class="relative flex flex-col grow px-4 pt-4 space-y-4 overflow-y-auto custom-scrollbar bg-theme-bg-secondary transition-all duration-150"
      >
        <div v-for="(message, index) in messages" :key="message.id" class="mb-1">
          <div v-if="getDisplayTime(index)" class="text-xs text-theme-text-secondary text-center my-2 select-none opacity-70">
            {{ getDisplayTime(index) }}
          </div>

          <MessageItem
            :message="message"
            :index="index"
            :audioStates="audioStates"
            @open-lightbox="openLightbox"
            @reply="setReplyTo"
            :positionInGroup="getMessagePositionInGroup(index)"
            @toggle-audio-playback="toggleAudioPlayback"
            @add-reaction="({ messageId, emoji }) => {
              const msg = messages.find(m => m.id === messageId);
              if (msg) {
                if (!msg.reactions) msg.reactions = [];
                msg.reactions.push(emoji);
              }
            }"
          />
        </div>
      </main>

      <MessageBoxFooter
        :reply="replyTarget"
        @clearReply="clearReply"
        @add-message="(msg) => { messages.push(msg); scrollToBottom(); }"
      />
    </div>

    <MultiMediaLightbox
      v-if="isLightboxOpen"
      :modelValue="isLightboxOpen"
      @update:modelValue="isLightboxOpen = $event"
      :media="filteredMedia"
      :startIndex="currentMediaIndex"
    />
  </div>
</template>

<style scoped>
.custom-scrollbar::-webkit-scrollbar { width: 6px; background-color: transparent; }
.custom-scrollbar::-webkit-scrollbar-thumb { background-color: #d1d5db; border-radius: 3px; }
.custom-scrollbar:hover::-webkit-scrollbar-thumb { background-color: #9ca3af; }
</style>
