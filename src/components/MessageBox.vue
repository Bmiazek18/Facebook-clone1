<script setup lang="ts">
import { ref, onUnmounted, computed, nextTick, onMounted, watch } from 'vue';
import { storeToRefs } from 'pinia'; // Dodano dla lepszej reaktywności
import MultiMediaLightbox from './MessageBox/MediaLightbox.vue';
import MessageBoxHeader from './MessageBoxHeader.vue';
import MessageBoxFooter from './MessageBoxFooter.vue';
import MessageItem from './MessageItem.vue';
import { useConversationsStore } from '@/stores/conversations';


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

const props = withDefaults(defineProps<{
  boxId?: string | number;
  mode?: 'card' | 'full';
  messages?: Message[]
}>(), {
  mode: 'card'
});

const convStore = useConversationsStore();
// Bezpieczne pobieranie danych ze store
const { themes, selectedTheme } = storeToRefs(convStore);

// --- LOGIKA DANYCH (MESSAGES) ---

// Używamy localMessages jako bufora, jeśli parent podaje props.messages (tryb "Dumb")
const localMessages = ref<Message[]>([]);

// Główna lista wiadomości - decyduje czy brać ze Store czy z Props/Local
const messagesList = computed((): Message[] => {
  if (props.boxId) {
    // Tryb "Smart": Pobierz z Pinia
    return convStore.getMessagesByChatId(Number(props.boxId)) as Message[];
  }
  // Tryb "Dumb": Pobierz z props (lub lokalnej kopii dla D&D)
  return props.messages?.length ? props.messages : localMessages.value;
});

// Synchronizacja props -> local (jeśli parent zaktualizuje propsy)
watch(() => props.messages, (newVal) => {
  if (newVal) localMessages.value = [...newVal];
}, { immediate: true });


// --- LOGIKA MOTYWU (THEME) ---

const boxTheme = computed(() => {
  // Jeśli brak boxId, użyj globalnego motywu
  if (!props.boxId) return selectedTheme.value;

  const chatId = Number(props.boxId);
  const settings = convStore.settings.find(x => x.chatId === chatId);

  if (!settings || settings.themeId === undefined) return selectedTheme.value;

  // Obsługa legacy (indeks) i nowego formatu (string ID)
  if (typeof settings.themeId === 'number') {
    return themes.value[settings.themeId] || selectedTheme.value;
  }

  return themes.value.find((t: any) => t.id === settings.themeId) || selectedTheme.value;
});


// --- SCROLLOWANIE ---
const chatContainer = ref<HTMLElement | null>(null);

function scrollToBottom() {
  nextTick(() => {
    if (chatContainer.value) {
      chatContainer.value.scrollTop = chatContainer.value.scrollHeight;
    chatContainer.value.scrollTo({ top: chatContainer.value.scrollHeight, behavior: 'smooth' });
    }
  });
}

// --- LIGHTBOX & MEDIA ---
const isLightboxOpen = ref(false);
const currentMediaIndex = ref(0);


const filteredMedia = computed<MediaItem[]>(() => {
  // FlatMap jest czystszy, ale pętla for-of jest szybsza. Zostawiam Twoją logikę, jest OK.
  const result: MediaItem[] = [];

  for (const msg of messagesList.value) {
    if (msg.type === 'video') {

      result.push({ id: msg.id, sender: msg.sender, type: 'video', content: msg.content, time: msg.time, videoUrl: (msg as VideoMessage).videoUrl });
    } else if (msg.type === 'image' || msg.type === 'gif') {
      const imgMsg = msg as ImageMessage & { mediaUrls?: string[] };
      if (imgMsg.mediaUrls?.length) {
        imgMsg.mediaUrls.forEach(url => result.push({ id: imgMsg.id, sender: imgMsg.sender, type: imgMsg.type, content: imgMsg.content, time: imgMsg.time, imageUrl: url }));
      } else if (imgMsg.imageUrl) {
        result.push({ id: imgMsg.id, sender: imgMsg.sender, type: imgMsg.type, content: imgMsg.content, time: imgMsg.time, imageUrl: imgMsg.imageUrl });
      }
    }
  }
  return result;
});

const openLightbox = (url: string) => {
  const idx = filteredMedia.value.findIndex(m => m.videoUrl === url || m.imageUrl === url);
  if (idx !== -1) {
    currentMediaIndex.value = idx;
    isLightboxOpen.value = true;
  }
};


// --- AUDIO PLAYER ---
const audioStates = ref<Record<number, AudioState>>({});

const toggleAudioPlayback = (message: Message) => {
  const audioId = message.id;
  // UWAGA: Używanie ID w DOM jest ryzykowne. Upewnij się, że MessageItem generuje unikalne ID np. `audio-${boxId}-${msgId}`
  const audioElement = document.getElementById(`audio-${audioId}`) as HTMLAudioElement;

  if (!audioElement) return;

  // Pauzowanie innych
  Object.keys(audioStates.value).forEach((key) => {
    const id = Number(key);
    if (id !== audioId && audioStates.value[id]?.isPlaying) {
      const otherAudio = document.getElementById(`audio-${id}`) as HTMLAudioElement;
      if (otherAudio) otherAudio.pause();
      audioStates.value[id].isPlaying = false;
    }
  });

  // Inicjalizacja stanu jeśli nie istnieje
  if (!audioStates.value[audioId]) {
    audioStates.value[audioId] = { isPlaying: false, duration: audioElement.duration || 0, currentTime: 0 };
  }

  const state = audioStates.value[audioId];

  if (state.isPlaying) {
    audioElement.pause();
    state.isPlaying = false;
  } else {
    audioElement.play().catch(console.error);
    state.isPlaying = true;

    // Event listenery powinny być dodawane raz, ale w tym modelu (external control) jest to trudne.
    // Zabezpieczamy przed dublowaniem
    audioElement.ontimeupdate = () => { state.currentTime = audioElement.currentTime; };
    audioElement.onended = () => { state.isPlaying = false; state.currentTime = 0; };
  }
};


// --- UTILS (Formatowanie daty) ---
// Przeniesione na dół lub do computed dla czytelności
const MAX_TIME_DIFF_MS = 5 * 60 * 1000;

function getDisplayTime(index: number): string | null {
  const currentMsg = messagesList.value[index];
  if (!currentMsg) return null;

  const prevMsg = messagesList.value[index - 1];
  const isFirst = index === 0 || !prevMsg;

  // Jeśli to pierwsza wiadomość LUB różnica czasu jest duża LUB to inny dzień
  if (isFirst ||
      (currentMsg.time - prevMsg.time >= MAX_TIME_DIFF_MS) ||
      !isSameDay(currentMsg.time, prevMsg.time)) {

    const dateStr = new Date(currentMsg.time);
    const timeStr = dateStr.toLocaleTimeString('pl-PL', { hour: '2-digit', minute: '2-digit' });

    if (isFirst || !isSameDay(currentMsg.time, prevMsg?.time ?? 0)) {
       const dayName = dateStr.toLocaleDateString('pl-PL', { weekday: 'short' }).replace('.', '');
       return `${dayName.charAt(0).toUpperCase() + dayName.slice(1)}, ${timeStr}`;
    }
    return timeStr;
  }
  return null;
}

function getMessagePositionInGroup(index: number): 'single' | 'first' | 'middle' | 'last' {
  const current = messagesList.value[index];
  const prev = messagesList.value[index - 1];
  const next = messagesList.value[index + 1];

  const isSameSenderPrev = prev && prev.sender === current.sender && (current.time - prev.time) < MAX_TIME_DIFF_MS;
  const isSameSenderNext = next && next.sender === current.sender && (next.time - current.time) < MAX_TIME_DIFF_MS;

  if (isSameSenderPrev && isSameSenderNext) return 'middle';
  if (isSameSenderPrev) return 'last';
  if (isSameSenderNext) return 'first';
  return 'single';
}

function isSameDay(t1: number, t2: number) {
  return new Date(t1).toDateString() === new Date(t2).toDateString();
}


// --- DRAG & DROP & FILES ---
const isDragging = ref(false);

const handleDrop = (event: DragEvent) => {
  isDragging.value = false;
  const files = event.dataTransfer?.files;
  if (!files?.length) return;

  Array.from(files).forEach(file => {
    const url = URL.createObjectURL(file);
    const baseMsg = { sender: 'me', time: Date.now() }; // Common fields

    // Logika dodawania do store LUB localMessages
    const pushMsg = (msg: any) => {
      if (props.boxId) convStore.addMessage(Number(props.boxId), msg);
      else localMessages.value.push({ id: Date.now() + Math.random(), ...msg });
    };

    if (file.type.startsWith('image/')) {
       pushMsg({ ...baseMsg, type: 'image', content: 'Wysłano obraz', imageUrl: url });
    } else if (file.type.startsWith('video/')) {
       pushMsg({ ...baseMsg, type: 'video', content: file.name, videoUrl: url });
    } else {
       pushMsg({ ...baseMsg, type: 'file', content: `Plik: ${file.name}`, fileUrl: url, fileName: file.name, fileSize: file.size });
    }
  });
  scrollToBottom();
};

// --- REPLY & REACTIONS ---
const replyTarget = ref<Message | null>(null);

const handleAddMessage = (msg: Message) => {
  if (props.boxId) {
    // Rozbudowana logika dla store
    const newMessage = {
      ...msg,
      isReply: !!replyTarget.value,
      replyToSender: replyTarget.value?.sender === 'me' ? 'Ty' : replyTarget.value?.sender,
      replyToContentSnippet: replyTarget.value?.content?.slice(0, 100)
    };
    convStore.addMessage(Number(props.boxId), newMessage);
    replyTarget.value = null;
  } else {
    // Prosta logika lokalna
    localMessages.value.push(msg);
  }
  scrollToBottom();
};

const handleAddReaction = (messageId: number, emoji: string) => {
    // Jeśli używamy store, musimy zaktualizować obiekt w store, nie lokalną kopię
    if (props.boxId) {
       const msgs = convStore.getMessagesByChatId(Number(props.boxId));
       const target = msgs.find(m => m.id === messageId) as any;
       if (target) {
         if(!target.reactions) target.reactions = [];
         target.reactions.push(emoji);
       }
    } else {
       // Fallback dla local
       const target = localMessages.value.find(m => m.id === messageId) as any;
       if(target) {
          if(!target.reactions) target.reactions = [];
          target.reactions.push(emoji);
       }
    }
}


// --- LIFECYCLE ---
onMounted(() => {
  scrollToBottom();
  window.addEventListener('keydown', (e) => {
    if (e.key === 'Escape' && isLightboxOpen.value) isLightboxOpen.value = false;
  });
});

onUnmounted(() => {
  // Cleanup object URLs to avoid memory leaks
  messagesList.value.forEach((msg: any) => {
    if (msg.audioUrl?.startsWith('blob:')) URL.revokeObjectURL(msg.audioUrl);
  });
});

// --- UI COMPUTEDS ---
const headerTitle = computed(() => {
  if (!props.boxId) return 'Czat';
  const chat = convStore.chats.find(c => c.id === Number(props.boxId));
  return chat ? chat.name : `Czat ${props.boxId}`;
});

const isFull = computed(() => props.mode === 'full');
</script>

<template>
  <div
    :class="[
      isFull ? 'relative flex-1 flex flex-col h-full w-full' : 'flex items-center relative justify-center py-4 px-2',
    ]"
    @dragover.prevent="isDragging = true"
    @dragleave.prevent="isDragging = false"
    @drop.prevent="handleDrop"
  >
    <div
      :class="[
        isFull ? 'w-full h-full rounded-none shadow-none' : 'w-full max-w-[328px] h-[455px] rounded-xl shadow-2xl',
        'bg-white flex flex-col overflow-hidden relative transition-all duration-300'
      ]"
    >

      <MessageBoxHeader
        :title="headerTitle"
        :users="[headerTitle]"
        :boxId="props.boxId ?? 1"
      />

      <div v-if="isDragging" class="absolute inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-sm pointer-events-none">
        <div class="text-white text-lg font-semibold px-6 py-4 rounded-xl border-2 border-dashed border-white/50 bg-white/10">
          Upuść plik tutaj
        </div>
      </div>

      <main
        ref="chatContainer"
        class="relative flex flex-col justify-end grow px-4 pt-4 space-y-4 overflow-y-auto custom-scrollbar bg-gray-50 transition-all duration-300"
        :style="boxTheme?.backgroundImage ? { backgroundImage: `url(${boxTheme.backgroundImage})`, backgroundSize: 'cover', backgroundPosition: 'center' } : {}"
      >
        <div v-if="boxTheme?.gradientClass" :class="['absolute inset-0 pointer-events-none opacity-30', boxTheme.gradientClass]"></div>

        <div v-for="(message, index) in messagesList" :key="message.id" class="relative z-10 mb-1">
          <div v-if="getDisplayTime(index)" class="text-[11px] font-medium text-gray-400 text-center my-3 select-none uppercase tracking-wide opacity-80">
            {{ getDisplayTime(index) }}
          </div>

          <MessageItem
            :message="message"
            :index="index"
            :audioStates="audioStates"
            :positionInGroup="getMessagePositionInGroup(index)"
            @open-lightbox="openLightbox"
            @reply="replyTarget = $event"
            @toggle-audio-playback="toggleAudioPlayback"
            @add-reaction="({ messageId, emoji }) => handleAddReaction(messageId, emoji)"
          />
        </div>
      </main>

      <MessageBoxFooter
        :reply="replyTarget"
        :boxId="props.boxId"
        @clearReply="replyTarget = null"
        @add-message="handleAddMessage"
      />

      <MultiMediaLightbox
        v-if="isLightboxOpen"
        v-model="isLightboxOpen"
        :media="filteredMedia"
        :startIndex="currentMediaIndex"
      />
    </div>
  </div>
</template>

<style scoped>
.custom-scrollbar::-webkit-scrollbar { width: 5px; }
.custom-scrollbar::-webkit-scrollbar-track { background: transparent; }
.custom-scrollbar::-webkit-scrollbar-thumb { background-color: #cbd5e1; border-radius: 10px; }
.custom-scrollbar:hover::-webkit-scrollbar-thumb { background-color: #94a3b8; }
</style>
