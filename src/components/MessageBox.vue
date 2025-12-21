<script setup lang="ts">
import { ref, onUnmounted, computed, nextTick, onMounted, watch } from 'vue';
import { storeToRefs } from 'pinia'; // Dodano dla lepszej reaktywności
import MultiMediaLightbox from './MessageBox/MediaLightbox.vue';
import MessageBoxHeader from './MessageBoxHeader.vue';
import MessageBoxFooter from './MessageBoxFooter.vue';
import MessageItem from './MessageItem.vue';
import { useConversationsStore } from '@/stores/conversations';
import { useAudioPlayer } from '@/composables/useAudioPlayer';
import { useMessageGrouper } from '@/composables/useMessageGrouper';
import { useLightbox } from '@/composables/useLightbox';

import type { Theme } from '@/stores/messengerTheme';
import type { Message, ImageMessage, VideoMessage } from '@/types/Message';

const props = withDefaults(defineProps<{
  boxId?: string | number;
  mode?: 'card' | 'full';
  messages?: Message[]
}>(), {
  mode: 'card'
});

const convStore = useConversationsStore();
const { themes, selectedTheme } = storeToRefs(convStore);

const localMessages = ref<Message[]>([]);

const messagesList = computed((): Message[] => {
  if (props.boxId) {
    return convStore.getMessagesByChatId(Number(props.boxId)) as Message[];
  }
  return props.messages?.length ? props.messages : localMessages.value;
});

watch(() => props.messages, (newVal) => {
  if (newVal) localMessages.value = [...newVal];
}, { immediate: true });

const boxTheme = computed(() => {
  if (!props.boxId) return selectedTheme.value;

  const chatId = Number(props.boxId);
  const settings = convStore.settings.find(x => x.chatId === chatId);

  if (!settings || settings.themeId === undefined) return selectedTheme.value;

  if (typeof settings.themeId === 'number') {
    return themes.value[settings.themeId] || selectedTheme.value;
  }

  return themes.value.find((t: Theme) => t.id === settings.themeId) || selectedTheme.value;
});

const chatContainer = ref<HTMLElement | null>(null);

function scrollToBottom() {
  nextTick(() => {
    if (chatContainer.value) {
      chatContainer.value.scrollTop = chatContainer.value.scrollHeight;
    chatContainer.value.scrollTo({ top: chatContainer.value.scrollHeight, behavior: 'smooth' });
    }
  });
}

function scrollToMessage(messageId: number) {
  nextTick(() => {
    const domId = `msg-${props.boxId ?? '0'}-${messageId}`;
    const el = document.getElementById(domId);
    const container = chatContainer.value;
    if (!el || !container) return;
    const top = el.offsetTop - (container.clientHeight / 2) + (el.clientHeight / 2);
    container.scrollTo({ top, behavior: 'smooth' });
    el.classList.add('ring-2', 'ring-indigo-300');
    setTimeout(() => el.classList.remove('ring-2', 'ring-indigo-300'), 1800);
  });
}

const { audioStates, toggleAudioPlayback } = useAudioPlayer(props.boxId);
const { getDisplayTime, getMessagePositionInGroup } = useMessageGrouper(messagesList);
const { isLightboxOpen, currentMediaIndex, filteredMedia, openLightbox } = useLightbox(messagesList);


const isDragging = ref(false);

const handleDrop = (event: DragEvent) => {
  isDragging.value = false;
  const files = event.dataTransfer?.files;
  if (!files?.length) return;

  Array.from(files).forEach(file => {
    const url = URL.createObjectURL(file);
    const baseMsg = { sender: 'me', time: Date.now() };

    const pushMsg = (msg: Message) => {
      if (props.boxId) convStore.addMessage(Number(props.boxId), msg);
      else localMessages.value.push({ id: Date.now() + Math.random(), ...msg });
    };

    if (file.type.startsWith('image/')) {
       pushMsg({ ...baseMsg, type: 'image', content: 'Wysłano obraz', imageUrl: url } as ImageMessage);
    } else if (file.type.startsWith('video/')) {
       pushMsg({ ...baseMsg, type: 'video', content: file.name, videoUrl: url } as VideoMessage);
    } else {
       pushMsg({ ...baseMsg, type: 'file', content: `Plik: ${file.name}`, fileUrl: url, fileName: file.name, fileSize: file.size } as Message);
    }
  });
  scrollToBottom();
};

const replyTarget = ref<Message | null>(null);

const handleAddMessage = (msg: Message) => {
  if (props.boxId) {
    const newMessage = {
      ...msg,
      isReply: !!replyTarget.value,
      replyToSender: replyTarget.value?.sender === 'me' ? 'Ty' : replyTarget.value?.sender,
      replyToContentSnippet: replyTarget.value?.content?.slice(0, 100)
    };
    convStore.addMessage(Number(props.boxId), newMessage);
    replyTarget.value = null;
  } else {
    localMessages.value.push(msg);
  }
  scrollToBottom();
};

const handleAddReaction = (messageId: number, emoji: string) => {
    if (props.boxId) {
       const msgs = convStore.getMessagesByChatId(Number(props.boxId));
       const target = msgs.find(m => m.id === messageId);
       if (target) {
         if(!target.reactions) target.reactions = [];
         target.reactions.push(emoji);
       }
    } else {
       const target = localMessages.value.find(m => m.id === messageId);
       if(target) {
          if(!target.reactions) target.reactions = [];
          target.reactions.push(emoji);
       }
    }
}

onMounted(() => {
  scrollToBottom();
  window.addEventListener('keydown', (e) => {
    if (e.key === 'Escape' && isLightboxOpen.value) isLightboxOpen.value = false;
  });
});

onUnmounted(() => {
  messagesList.value.forEach((msg: Message) => {
    if ('audioUrl' in msg && msg.audioUrl?.startsWith('blob:')) URL.revokeObjectURL(msg.audioUrl);
  });
});

const headerTitle = computed(() => {
  if (!props.boxId) return 'Czat';
  const chat = convStore.chats.find(c => c.id === Number(props.boxId));
  return chat ? chat.name : `Czat ${props.boxId}`;
});

const isFull = computed(() => props.mode === 'full');

defineExpose({ scrollToMessage });
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

  <div v-for="(message, index) in messagesList" :key="message.id" :id="`msg-${props.boxId ?? '0'}-${message.id}`" class="relative z-10 mb-1">
          <div v-if="getDisplayTime(index)" class="text-[11px] font-medium text-gray-400 text-center my-3 select-none uppercase tracking-wide opacity-80">
            {{ getDisplayTime(index) }}
          </div>

                  <MessageItem
                    :message="message"
                    :index="index"
                    :audioStates="audioStates"
                    :positionInGroup="getMessagePositionInGroup(index)"
                    :boxId="props.boxId"
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
