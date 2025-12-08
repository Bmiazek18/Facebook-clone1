<script setup lang="ts">
import { ref, onUnmounted, computed, nextTick, onMounted } from 'vue';
import MultiMediaLightbox from './MessageBox/Lightbox.vue';
import MessageBoxHeader from './MessageBoxHeader.vue';
import MessageBoxFooter from './MessageBoxFooter.vue';
import MessageItem from './MessageItem.vue';

import type { Message, ImageMessage, GifMessage, AudioState } from '@/types/Message';

interface ImageMessageWithOptionalGroup extends ImageMessage {
  mediaUrls?: string[];
}

const createTimestamp = (timeStr: string, daysAgo: number = 0): number => {
  const [hours, minutes] = timeStr.split(':').map(Number);
  const now = new Date();
  const date = new Date(now.getFullYear(), now.getMonth(), now.getDate() - daysAgo, hours, minutes, 0);
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
  } as ImageMessageWithOptionalGroup,
  {
    id: 15,
    sender: 'other',
    type: 'image',
    content: 'Pojedynczy obrazek:',
    time: createTimestamp('15:01', 1),
    imageUrl: 'https://primefaces.org/cdn/primevue/images/galleria/galleria12.jpg',
  } as ImageMessageWithOptionalGroup,
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
]);

const chatContainer = ref(null);
function scrollToBottom() {
  nextTick(() => {
    if (chatContainer.value) {
      (chatContainer.value as HTMLElement).scrollTop = (chatContainer.value as HTMLElement).scrollHeight;
    }
  });
}

const MAX_TIME_DIFF_MS = 5 * 60 * 1000;

const isLightboxOpen = ref(false);
const currentMediaIndex = ref(0);

const filteredMedia = computed(() => {
  const media: (ImageMessage | GifMessage | { id: number; sender: string; type: 'video'; content: string; time: number; videoUrl: string; })[] = [];

  messages.value.forEach(msg => {
    if (msg.type === 'image' || msg.type === 'gif' || msg.type === 'video') {
      if (msg.type === 'video') {
        media.push({
          id: msg.id,
          sender: msg.sender,
          type: 'video',
          content: msg.content,
          time: msg.time,
          videoUrl: (msg as any).videoUrl,
        });
      } else {
        const imageMsg = msg as ImageMessageWithOptionalGroup;
        if (imageMsg.mediaUrls && imageMsg.mediaUrls.length > 0) {
          imageMsg.mediaUrls.forEach(url => {
            media.push({
              id: msg.id,
              sender: msg.sender,
              type: msg.type,
              content: msg.content,
              time: msg.time,
              imageUrl: url
            } as ImageMessage);
          });
        } else if (imageMsg.imageUrl) {
          media.push(msg as (ImageMessage | GifMessage));
        }
      }
    }
  });

  return media;
});


const openLightbox = (url: string) => {
  const idx = filteredMedia.value.findIndex(msg => {
    if (msg.type === 'video') return (msg as any).videoUrl === url;
    return (msg as any).imageUrl === url;
  });
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
        audioStates.value[id] = {
            isPlaying: false,
            duration: duration,
            currentTime: 0,
        };
    } else {
        if (!isNaN(audio.duration) && audio.duration > 0) {
            audioStates.value[id].duration = audio.duration;
        }
    }
};

const toggleAudioPlayback = (message: Message) => {
    const audioId = message.id;
    const audioElement = document.getElementById(`audio-${audioId}`) as HTMLAudioElement;

    if (!audioElement) return;

    const messageDuration = message.type === 'audio' ? (message as any).duration : undefined;

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

    Object.keys(audioStates.value).forEach(id => {
        const otherId = parseInt(id);
        if (otherId !== audioId && audioStates.value[otherId].isPlaying) {
            const otherAudio = document.getElementById(`audio-${otherId}`) as HTMLAudioElement;
            otherAudio?.pause();
            audioStates.value[otherId].isPlaying = false;
        }
    });

    if (currentState.isPlaying) {
        audioElement.pause();
        currentState.isPlaying = false;
    } else {
        if (audioElement.currentTime === audioElement.duration) {
            audioElement.currentTime = 0;
            currentState.currentTime = 0;
        }

        audioElement.play();
        currentState.isPlaying = true;

        audioElement.ontimeupdate = () => {
            currentState.currentTime = audioElement.currentTime;
        };

        audioElement.onended = () => {
            currentState.isPlaying = false;
            currentState.currentTime = 0;
        };
    }
};

const isSameDay = (t1: number, t2: number): boolean => {
    const d1 = new Date(t1);
    const d2 = new Date(t2);
    return d1.getFullYear() === d2.getFullYear() && d1.getMonth() === d2.getMonth() && d1.getDate() === d2.getDate();
};
const capitalizeWord = (wyraz: string): string => wyraz ? wyraz.charAt(0).toUpperCase() + wyraz.slice(1).toLowerCase() : wyraz;
const getDayAbbreviation = (timestamp: number): string => new Date(timestamp).toLocaleDateString('pl-PL', { weekday: 'short' }).replace('.', '');
const formatTime = (timestamp: number): string => new Date(timestamp).toLocaleTimeString('pl-PL', { hour: '2-digit', minute: '2-digit' });

const getDisplayTime = (index: number): string | null => {
  const currentTimestamp = messages.value[index].time;

  if (index === 0) return `${capitalizeWord(getDayAbbreviation(currentTimestamp))}, ${formatTime(currentTimestamp)}`;

  const prevTimestamp = messages.value[index - 1].time;
  const timeDifference = currentTimestamp - prevTimestamp;

  const isTimeGapBig = timeDifference >= MAX_TIME_DIFF_MS;
  const isNewDay = !isSameDay(currentTimestamp, prevTimestamp);

  if (isNewDay) return `${capitalizeWord(getDayAbbreviation(currentTimestamp))}, ${formatTime(currentTimestamp)}`;
  if (isTimeGapBig) return formatTime(currentTimestamp);

  return null;
};
const getMessagePositionInGroup = (index: number): 'single' | 'first' | 'middle' | 'last' => {
  const current = messages.value[index];
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
        if (msg.type === 'audio' && (msg as any).audioUrl) {
            URL.revokeObjectURL((msg as any).audioUrl);
        }
    });
});

// ============= DRAG & DROP + OVERLAY =============
const isDragging = ref(false);

const onDragOver = () => { isDragging.value = true; };
const onDragLeave = () => { isDragging.value = false; };
const onDrop = (event: DragEvent) => {
  isDragging.value = false;
  const files = event.dataTransfer?.files;
  if (!files || files.length === 0) return;

  for (const file of Array.from(files)) {
    const url = URL.createObjectURL(file);

    if (file.type.startsWith('image/')) {
      addDroppedImage(url);
    }else if (file.type.startsWith('video/')) {
      addDroppedVideo(url,file);
    } else {
      addDroppedFile(url, file);
    }
  }

  scrollToBottom();
};


const addDroppedImage = (url: string) => {
  messages.value.push({
    id: Date.now(),
    sender: 'me',
    type: 'image',
    content: 'Wysłano obraz:',
    time: Date.now(),
    imageUrl: url
  });
};
const addDroppedVideo = async (url: string, file: File) => {


  messages.value.push({
    id: Date.now(),
    sender: 'me',
    type: 'video',
    content: file.name,
    time: Date.now(),
    videoUrl: url,
    fileName: file.name,
    fileSize: file.size,
  });
};


const addDroppedFile = (url: string, file: File) => {
  messages.value.push({
    id: Date.now(),
    sender: 'me',
    type: 'file',
    content: `Wysłano plik (${file.name})`,
    time: Date.now(),
    fileUrl: url,
    fileName: file.name,
    fileType: file.type,
    fileSize: file.size
  });
};

const replyTarget = ref<Message | null>(null);

const setReplyTo = (message: Message) => {
  replyTarget.value = message;
};

const clearReply = () => {
  replyTarget.value = null;
};

</script>

<template>
  <div class="flex items-center relative justify-center py-4 px-2">

    <div class="w-full relative max-w-[328px] h-[455px] bg-theme-bg-secondary rounded-xl shadow-2xl flex flex-col overflow-hidden">

      <MessageBoxHeader title="Alan, Jacek" :users="['Alan', 'Jacek']" />
<div
      v-if="isDragging"
      class="absolute inset-0 h-[400px] top-[55px] z-50 flex items-center justify-center bg-black/50 pointer-events-none"
    >
      <div class="text-white text-lg font-semibold p-4 rounded-lg border-2 border-dashed border-white">
        Przeciągnij plik aby wysłać
      </div>
    </div>
      <main
        ref="chatContainer"
        class=" relative flex flex-col grow p-4 space-y-4 overflow-y-auto custom-scrollbar bg-theme-bg-secondary transition-all duration-150"
        @dragover.prevent="onDragOver"
        @dragleave="onDragLeave"
        @drop.prevent="onDrop"
      >

        <div v-for="(message, index) in messages" :key="message.id" class="my-2">

          <div
            v-if="getDisplayTime(index)"
            class="text-xs text-theme-text-secondary text-center my-2 select-none"
          >
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
.emoji-size-default { font-size: 1.75rem; }
.emoji-size-small { font-size: 45px; }
.emoji-size-medium { font-size: 60px; }
.emoji-size-large { font-size: 80px; }
.custom-scrollbar::-webkit-scrollbar { width: 6px; background-color: transparent; }
.custom-scrollbar::-webkit-scrollbar-thumb { background-color: #d1d5db; border-radius: 3px; }
.custom-scrollbar:hover::-webkit-scrollbar-thumb { background-color: #9ca3af; }
.bg-purple-600 { background-color: #8B5CF6; }
.v-popper__arrow-container { display: none !important; }
.lightbox-bg-gradient { background: radial-gradient(circle, rgba(0,0,0,0.95) 0%, rgba(0,0,0,0.85) 100%); }
@keyframes wave { 0%, 100% { transform: scaleY(0.5); } 50% { transform: scaleY(1.5); } }
.animate-wave { animation: wave 1.2s ease-in-out infinite; }
.delay-1 { animation-delay: 0.1s; }
.delay-2 { animation-delay: 0.2s; }
.delay-3 { animation-delay: 0.3s; }
</style>
