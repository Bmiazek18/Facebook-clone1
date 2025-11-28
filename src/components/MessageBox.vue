<script setup lang="ts">
import { ref, onUnmounted, computed, nextTick, onMounted } from 'vue';
import MultiMediaLightbox from './MessageBox/Lightbox.vue';
import MessageBoxHeader from './MessageBoxHeader.vue';
import MessageBoxFooter from './MessageBoxFooter.vue';

import ThumbUpIcon from 'vue-material-design-icons/ThumbUp.vue';
import PlayIcon from 'vue-material-design-icons/Play.vue';
import PauseIcon from 'vue-material-design-icons/Pause.vue';

import type { Message, ImageMessage, GifMessage, AudioState } from '@/types/Message';


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
    time: createTimestamp('14:50', 0),
  },
  {
    id: 11,
    sender: 'me',
    type: 'image',
    content: 'Wybrane zdjęcie:',
    time: createTimestamp('15:00', 0),
    imageUrl: 'https://primefaces.org/cdn/primevue/images/galleria/galleria10.jpg'
  }, {
    id: 11,
    sender: 'me',
    type: 'image',
    content: 'Wybrane zdjęcie:',
    time: createTimestamp('15:00', 0),
    imageUrl: 'https://primefaces.org/cdn/primevue/images/galleria/galleria11.jpg'
  },
  {
    id: 13,
    sender: 'me',
    type: 'image',
    content: 'Wybrane zdjęcie:',
    time: createTimestamp('15:00', 0),
    imageUrl: 'https://primefaces.org/cdn/primevue/images/galleria/galleria14.jpg'
  },
  {
    id: 12,
    sender: 'other',
    type: 'text',
    content: 'Właśnie wysłałem Ci link do tego nowego frameworka. Zobacz, czy to może nam pomóc przy optymalizacji.',
    time: createTimestamp('15:05', 0),
  },
  {
    id: 13,
    sender: 'other',
    type: 'audio',
    content: 'Wiadomość głosowa (0:15)',
    time: createTimestamp('15:10', 0),
    audioUrl: 'http://example.com/audio/other_voice_message.mp3',
    duration: 15,
  },
  {
    id: 14,
    sender: 'me',
    type: 'text',
    content: 'Oki, zaraz obczaję ten link i odsłucham wiadomość. Wielkie dzięki!',
    time: createTimestamp('15:12', 0),
  },
]);
const chatContainer = ref(null);

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
const filteredMedia = computed(() =>
  messages.value
    .filter(msg => msg.type === 'image' || msg.type === 'gif')
    .map(msg => msg as (ImageMessage | GifMessage))
);
const openLightbox = (url: string) => {
  const idx = filteredMedia.value.findIndex(msg => msg.imageUrl === url);
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



const formatSeconds = (seconds: number): string => {
    const minutes = Math.floor(seconds / 60);
    const secs = Math.floor(seconds % 60);

    if (isNaN(seconds) || seconds < 0) {
        return '0:00';
    }

    return `${minutes}:${secs.toString().padStart(2, '0')}`;
};

const initializeAudioState = (id: number, audio: HTMLAudioElement, messageDuration?: number) => {
    if (!audioStates.value[id]) {
        // Użyj messageDuration jeśli jest dostępny, inaczej audio.duration
        const duration = messageDuration ?? (isNaN(audio.duration) ? 0 : audio.duration);
        audioStates.value[id] = {
            isPlaying: false,
            duration: duration,
            currentTime: 0,
        };
    } else {
        // Aktualizuj duration z audio.duration jeśli jest dostępna, w innym wypadku zostaw
        if (!isNaN(audio.duration) && audio.duration > 0) {
            audioStates.value[id].duration = audio.duration;
        }
    }
};

const getMessageDuration = (message: Message): number => {
  return message.type === 'audio' ? message.duration : 0;
};

const toggleAudioPlayback = (message: Message) => {
    const audioId = message.id;
    const audioElement = document.getElementById(`audio-${audioId}`) as HTMLAudioElement;

    if (!audioElement) return;

    // Pobranie duration z Message jeśli jest AudioMessage
    const messageDuration = message.type === 'audio' ? message.duration : undefined;

    // Krok 1: Wymuś załadowanie metadanych, jeśli jeszcze nie załadowane
    if (audioElement.readyState < 1) {
        // Użyj closure, aby poprawnie wywołać funkcję po załadowaniu
        const onLoaded = () => {
             // Po załadowaniu zainicjuj stan
            initializeAudioState(audioId, audioElement, messageDuration);
            toggleAudioPlayback(message); // Ponownie wywołaj, gdy metadane są gotowe
            audioElement.removeEventListener('loadedmetadata', onLoaded);
        };
        audioElement.addEventListener('loadedmetadata', onLoaded);
        audioElement.load();
        return;
    }

    // Krok 2: Upewnij się, że stan jest zainicjowany
    initializeAudioState(audioId, audioElement, messageDuration);
    const currentState = audioStates.value[audioId];

    // Krok 3: Pauzowanie wszystkich innych odtwarzaczy
    Object.keys(audioStates.value).forEach(id => {
        const otherId = parseInt(id);
        if (otherId !== audioId && audioStates.value[otherId].isPlaying) {
            const otherAudio = document.getElementById(`audio-${otherId}`) as HTMLAudioElement;
            otherAudio?.pause();
            audioStates.value[otherId].isPlaying = false;
        }
    });

    // Krok 4: Logika odtwarzania/pauzowania bieżącego elementu
    if (currentState.isPlaying) {
        audioElement.pause();
        currentState.isPlaying = false;
    } else {
        // Resetowanie do początku, jeśli dotarliśmy do końca
        if (audioElement.currentTime === audioElement.duration) {
            audioElement.currentTime = 0;
            currentState.currentTime = 0;
        }

        audioElement.play();
        currentState.isPlaying = true;

        // Dodanie słuchaczy zdarzeń do aktualizacji stanu Vue
        audioElement.ontimeupdate = () => {
            currentState.currentTime = audioElement.currentTime;
        };

        audioElement.onended = () => {
            currentState.isPlaying = false;
            currentState.currentTime = 0; // Resetowanie czasu po zakończeniu
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

const isEmojiOnly = (content: string): boolean => {
  if (!content.trim()) return false;
  const strippedContent = content.trim().replace(/(\u00a9|\u00ae|[\u2000-\u3300]|\ud83c[\ud000-\udfff]|\ud83d[\ud000-\udfff]|\ud83e[\ud000-\udfff])/g, '');
  return strippedContent.trim().length === 0 && content.trim().length > 0;
};

onMounted(()=>{
  scrollToBottom();
  window.addEventListener('keydown', onKeyDown);
})
onUnmounted(() => {
    window.removeEventListener('keydown', onKeyDown);

    messages.value.forEach(msg => {
        if (msg.type === 'audio' && msg.audioUrl) {
            URL.revokeObjectURL(msg.audioUrl);
        }
    });
});
</script>
<template>
  <div class=" flex items-center relative justify-center py-4 px-2">
    <div class="w-full max-w-[328px] h-[455px] bg-theme-bg-secondary rounded-xl shadow-2xl flex flex-col overflow-hidden">
            <MessageBoxHeader title="Alan, Jacek" :users="['Alan', 'Jacek']" />

      <main ref="chatContainer" class="flex flex-col grow p-4 space-y-4 overflow-y-auto custom-scrollbar bg-theme-bg-secondary">
        <div v-for="(message, index) in messages" :key="message.id" >
          <div v-if="getDisplayTime(index)" class="text-xs text-theme-text-secondary text-center my-2 select-none">{{ getDisplayTime(index) }}</div>
          <div  class="flex items-end "
               :class="{ 'justify-start': message.sender === 'other', 'justify-end': message.sender === 'me' }" >
            <div v-if="message.sender === 'other' && !isEmojiOnly(message.content)"
                 class="w-7 h-7 rounded-full bg-blue-100 dark:bg-blue-900 flex items-center justify-center mr-2 relative self-start flex-shrink-0">
              <span class="text-base">😊</span>
            </div>
            <div
            v-tooltip="{content:formatTime(message.time),placement:message.sender === 'other' ? 'left' : 'right' }"
            class="max-w-[90%] w-full flex flex-col"
                 :class="{ 'items-start': message.sender === 'other', 'items-end': message.sender === 'me' }">
              <div v-if="message.isReply"
                   class="mb-0 p-2 rounded-lg max-w-full bg-theme-bg-secondary border border-theme-border text-theme-text border-l-4">
                    <p class="text-xs font-semibold text-theme-text-secondary">{{ message.replyToSender }} odpowiedział Ci</p>
                    <p class="text-sm text-theme-text-secondary line-clamp-3 truncate">{{ message.replyToContentSnippet }}</p>
              </div>

              <div v-if="isEmojiOnly(message.content) "
                   :class="{
                        'emoji-size-small': message.iconSizeState === 'small',
                        'emoji-size-medium': message.iconSizeState === 'medium',
                        'emoji-size-large': message.iconSizeState === 'large',
                        'emoji-size-default': message.iconSizeState === 'default' || !message.iconSizeState
                    }"
                   class="p-0 wrap-break-word max-w-[62%]">
                <p>{{ message.content }}</p>
              </div>
 <div v-else-if="message.type === 'audio'"
                     class="flex items-center w-full min-w-[180px] space-x-2 p-2 rounded-full h-10"
                     :class="{
                        'bg-purple-600 text-white rounded-tl-none': message.sender === 'other',
                        'bg-blue-500 text-white rounded-br-none': message.sender === 'me'
                     }"
                     style="padding: 6px 10px;"
                >
                    <audio :src="message.audioUrl" :id="`audio-${message.id}`" class="hidden" preload="metadata"></audio>

                    <button
                        @click="toggleAudioPlayback(message)"
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
    <div
        class="absolute inset-0 bg-white rounded-full transition-width duration-75"
        :style="{ width: `${(audioStates[message.id]?.currentTime || 0) / (getMessageDuration(message) || 1) * 100}%` }"
    ></div>

    <div class="absolute inset-0 flex items-center justify-between space-x-0.5 h-full px-1">
        <div v-for="n in 8" :key="n"
             class="rounded-full shrink-0 z-10"
             :class="{
                 // Kolor fali: fioletowy/niebieski, jeśli jest w białej strefie postępu
                 'bg-purple-600': message.sender === 'other' && (audioStates[message.id]?.currentTime || 0) / (getMessageDuration(message) || 1) * 100 > ([0, 12, 25, 37, 50, 62, 75, 87][n-1] + 6),
                 'bg-blue-500': message.sender === 'me' && (audioStates[message.id]?.currentTime || 0) / (getMessageDuration(message) || 1) * 100 > ([0, 12, 25, 37, 50, 62, 75, 87][n-1] + 6),
                 // Kolor domyślny fali (biały)
                 'bg-white': (message.sender === 'other' || message.sender === 'me') && (audioStates[message.id]?.currentTime || 0) / (getMessageDuration(message) || 1) * 100 <= ([0, 12, 25, 37, 50, 62, 75, 87][n-1] + 6)
             }"
             :style="{ height: `${[10, 20, 14, 25, 20, 15, 20, 10][n-1]}px`, width: '3px' }"
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

                <div v-else-if="message.type === 'image' || message.type === 'gif'" class="mb-2">
                    <img
                        :src="message.imageUrl"
                        :alt="message.type === 'gif' ? 'Animated GIF' : 'Image'"
                        class="max-w-full h-auto rounded-lg cursor-pointer"
                        :class="{ 'border-2 border-purple-400': message.type === 'gif' }"
                        @click="openLightbox(message.imageUrl)"
                    />
                </div>
              <div v-else
                   class="relative p-3 text-sm rounded-xl shadow-md break-words max-w-[62%]"
                   :class="{
                        'bg-purple-600 text-white rounded-tl-none': message.sender === 'other',
                        'bg-theme-bg-secondary text-theme-text border border-theme-border rounded-br-none': message.sender === 'me'
                   }">



                <p >{{ message.content }}</p>

                <div v-if="message.sender === 'other' && message.isReply"
                     class="absolute -bottom-1.5 -left-2 w-5 h-5 bg-blue-100 dark:bg-blue-900 rounded-full border-2 border-white dark:border-gray-600 flex items-center justify-center shadow">
                    <span class="text-xs">😊</span>
                </div>
                <div v-if="message.sender === 'me'"
                     class="absolute -bottom-2.5 -right-2.5 w-5 h-5 bg-theme-bg border border-theme-border rounded-full flex items-center justify-center shadow-md">
                    <ThumbUpIcon :size="14" class="text-blue-500" />
                </div>
              </div>
            </div>
          </div>
        </div>
      </main>

      <MessageBoxFooter
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

.emoji-size-default {
    font-size: 1.75rem;
}
.emoji-size-small {
    font-size: 45px;
}
.emoji-size-medium {
    font-size: 60px;
}
.emoji-size-large {
    font-size: 80px;
}
.custom-scrollbar::-webkit-scrollbar {
    width: 6px;
    background-color: transparent;
}
.custom-scrollbar::-webkit-scrollbar-thumb {
    background-color: #d1d5db;
    border-radius: 3px;
}
.custom-scrollbar:hover::-webkit-scrollbar-thumb {
    background-color: #9ca3af;
}
.bg-purple-600 {
    background-color: #8B5CF6;
}
.v-popper__arrow-container {
    display: none !important;
}

.lightbox-bg-gradient {
    background: radial-gradient(circle, rgba(0,0,0,0.95) 0%, rgba(0,0,0,0.85) 100%);
}

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

.delay-1 { animation-delay: 0.1s; }
.delay-2 { animation-delay: 0.2s; }
.delay-3 { animation-delay: 0.3s; }

</style>

