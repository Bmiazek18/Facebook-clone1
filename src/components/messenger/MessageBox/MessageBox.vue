<script setup lang="ts">
import { ref, onUnmounted, computed, nextTick, onMounted, watch, provide } from 'vue';
import { storeToRefs } from 'pinia';
import MultiMediaLightbox from './MediaLightbox.vue';
import MessageBoxHeader from '@/components/messenger/MessageBoxHeader.vue';
import MessageBoxFooter from '@/components/messenger/MessageBoxFooter/MessageBoxFooter.vue';
import MessageItem from '@/components/messenger/MessageItem.vue';
import { useConversationsStore } from '@/stores/conversations';
import { useAudioPlayer } from '@/composables/useAudioPlayer';
import { useMessageGrouper } from '@/composables/useMessageGrouper';
import { useLightbox } from '@/composables/useLightbox';

// IMPORT ANIMACJI
import { useFlipAnimation } from '@/composables/useFlipAnimation';

import type { Theme } from '@/stores/messengerTheme';
import type { Message, ImageMessage, VideoMessage } from '@/types/Message';

const emit = defineEmits(['open-modal', 'back-to-list', 'show-info']);
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

// Get all available messages
const allMessagesList = computed((): Message[] => {
  if (props.boxId) {
    return convStore.getMessagesByChatId(Number(props.boxId)) as Message[];
  }
  return props.messages?.length ? props.messages : localMessages.value;
});

// Get limited messages for display
const messagesList = computed((): Message[] => {
  const allMessages = allMessagesList.value;
  const totalMessages = allMessages.length;

  if (totalMessages <= visibleMessagesCount.value) {
    return allMessages;
  }

  // Return only the last N messages where N is visibleMessagesCount
  return allMessages.slice(totalMessages - visibleMessagesCount.value);
});

// Check if there are more messages to load
const hasMoreToLoad = computed(() => {
  return allMessagesList.value.length > visibleMessagesCount.value;
});


const chatContainer = ref<HTMLElement | null>(null);

// Infinity scroll state
const isLoadingOlder = ref(false);
const messagesPerPage = 20;
const visibleMessagesCount = ref(messagesPerPage);
let scrollHandler: ((event: Event) => void) | null = null;

// Debounce function for scroll handler
const debounce = (func: (...args: any[]) => void, wait: number) => {
  let timeout: number;
  return (...args: any[]) => {
    clearTimeout(timeout);
    timeout = setTimeout(() => func.apply(this, args), wait);
  };
};

const lastRead = ref<Record<string, number>>({
  'user_1': 0,
  'user_2': 0,
  'ghost_tester': 0
});


const { capturePositions, onAvatarEnter, onAvatarLeave } = useFlipAnimation(chatContainer);

// Udostępniamy funkcje animacji dzieciom (MessageItem) przez Provide/Inject
provide('flip-animation', {
  onAvatarEnter,
  onAvatarLeave
});


  const msgs = messagesList.value;
  if (msgs.length > 0) {
    const lastMsgId = msgs[msgs.length - 1].id;
    const lastMsgId2 = msgs.length > 1 ? msgs[msgs.length - 2].id : lastMsgId;

    lastRead.value['user_1'] = lastMsgId;
    lastRead.value['user_2'] = lastMsgId2;
  }


// Watcher pozycji (flush: 'pre' jest kluczowe)
watch(lastRead, () => {
  capturePositions();
}, { deep: true, flush: 'pre' });


// ==========================================
// 4. SYMULACJA CIĄGŁA (Skrypt testowy)
// ==========================================
let simulationInterval: any = null;
const TEST_USER_ID = 'ghost_tester';

onMounted(() => {
  // Immediate scroll to bottom
  scrollToBottom();

  // Delayed scroll to ensure DOM is fully rendered
  setTimeout(() => {
    scrollToBottom();
  }, 100);

  // Add scroll listener for infinity scroll
  const container = chatContainer.value;
  if (container) {
    const handleScroll = () => {
      // Check if scrolled to top (with small threshold)
      if (container.scrollTop <= 50 && hasMoreToLoad.value && !isLoadingOlder.value) {
        loadOlderMessages();
      }
    };

    scrollHandler = debounce(handleScroll, 150);
    container.addEventListener('scroll', scrollHandler);
  }  // Opóźniony start symulacji
  setTimeout(() => {
    console.log(`🚀 [Box ${props.boxId}] Start symulacji Cursor-based...`);
    let currentIndex = 0;

    simulationInterval = setInterval(() => {
      const msgs = messagesList.value;
      if (!msgs || msgs.length === 0) return;

      const targetMessageId = msgs[currentIndex].id;


      capturePositions();

      lastRead.value[TEST_USER_ID] = targetMessageId;

      currentIndex++;
      if (currentIndex >= msgs.length) currentIndex = 0;

    }, 1500);
  }, 2000);

  window.addEventListener('keydown', (e) => {
    if (e.key === 'Escape' && isLightboxOpen.value) isLightboxOpen.value = false;
  });
});

onUnmounted(() => {
  if (simulationInterval) clearInterval(simulationInterval);

  // Remove scroll listener
  const container = chatContainer.value;
  if (container && scrollHandler) {
    container.removeEventListener('scroll', scrollHandler);
  }

  messagesList.value.forEach((msg: Message) => {
    if ('audioUrl' in msg && msg.audioUrl?.startsWith('blob:')) URL.revokeObjectURL(msg.audioUrl);
  });
});

// --- Reszta standardowej logiki ---

watch(() => props.messages, (newVal) => {
  if (newVal) {
    localMessages.value = [...newVal];
    // Only scroll to bottom if not loading older messages
    if (!isLoadingOlder.value) {
      scrollToBottom();
    }
  }
}, { immediate: true });

// Watch for changes in messagesList and scroll to bottom (but not when loading older)
watch(messagesList, () => {
  if (!isLoadingOlder.value) {
    scrollToBottom();
  }
}, { flush: 'post' });

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

// (chatContainer zdefiniowany wyżej)

function scrollToBottom() {
  nextTick(() => {
    if (chatContainer.value) {
      chatContainer.value.scrollTop = chatContainer.value.scrollHeight;
    }
  });
}

// Load older messages for infinity scroll
const loadOlderMessages = async () => {
  if (isLoadingOlder.value || !hasMoreToLoad.value) return;

  isLoadingOlder.value = true;

  try {
    // Save current scroll position to restore it after loading
    const container = chatContainer.value;
    const oldScrollHeight = container?.scrollHeight || 0;
    const oldScrollTop = container?.scrollTop || 0;

    // Simulate loading delay
    await new Promise(resolve => setTimeout(resolve, 500));

    // Increase the number of visible messages
    visibleMessagesCount.value += messagesPerPage;

    // Restore scroll position with multiple attempts to ensure it works
    nextTick(() => {
      if (container) {
        const newScrollHeight = container.scrollHeight;
        const heightDifference = newScrollHeight - oldScrollHeight;

        // Adjust scroll position to account for new content at the top
        container.scrollTop = oldScrollTop + heightDifference;

        // Additional attempt after a short delay to ensure DOM is fully updated
        setTimeout(() => {
          const finalScrollHeight = container.scrollHeight;
          const finalHeightDifference = finalScrollHeight - oldScrollHeight;
          container.scrollTop = oldScrollTop + finalHeightDifference;
        }, 10);
      }
    });

  } catch (error) {
    console.error('Error loading older messages:', error);
  } finally {
    isLoadingOlder.value = false;
  }
};function scrollToMessage(messageId: number) {
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

  // Only scroll to bottom if not loading older messages
  if (!isLoadingOlder.value) {
    scrollToBottom();
  }
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

  // Ensure scroll to bottom after message is added (but not when loading older)
  if (!isLoadingOlder.value) {
    nextTick(() => {
      scrollToBottom();
    });
  }
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
      isFull ? 'relative flex-1 flex flex-col h-full w-full' : 'flex items-center relative justify-center py-4 px-2 ',
    ]"
    @dragover.prevent="isDragging = true"
    @dragleave.prevent="isDragging = false"
    @drop.prevent="handleDrop"
  >
    <div
      :class="[
        isFull ? 'w-full h-full rounded-none shadow-none' : 'w-full max-w-[328px] h-[455px] rounded-xl shadow-2xl',
        'bg-white flex flex-col relative transition-all duration-300'
      ]"
    >

      <MessageBoxHeader
        :title="headerTitle"
        :users="[headerTitle]"
        :boxId="props.boxId ?? 1"
        @back="emit('back-to-list')"
        @show-info="emit('show-info')"
      />

      <div v-if="isDragging" class="absolute inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-sm pointer-events-none">
        <div class="text-white text-lg font-semibold px-6 py-4 rounded-xl border-2 border-dashed border-white/50 bg-white/10">
          Upuść plik tutaj
        </div>
      </div>

      <main
        ref="chatContainer"
        class="relative flex flex-col-reverse grow overflow-y-auto custom-scrollbar bg-gray-50 transition-all duration-300 min-h-0"
        :style="boxTheme?.backgroundImage ? { backgroundImage: `url(${boxTheme.backgroundImage})`, backgroundSize: 'cover', backgroundPosition: 'center' } : {}"
      >
        <div v-if="boxTheme?.gradientClass" :class="['absolute inset-0 pointer-events-none opacity-30', boxTheme.gradientClass]"></div>

        <!-- Messages container -->
        <div class="px-4 pb-4 space-y-4">
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
              :last-read-map="lastRead"
              @open-lightbox="openLightbox"
              @reply="replyTarget = $event"
              @toggle-audio-playback="toggleAudioPlayback"
              @add-reaction="({ messageId, emoji }) => handleAddReaction(messageId, emoji)"
              @open-modal="emit('open-modal', $event)"
            />
          </div>
        </div>

        <!-- Loading indicator for older messages -->
        <div v-if="isLoadingOlder" class="px-4 py-3 text-center">
          <div class="flex items-center justify-center space-x-2 text-gray-500">
            <div class="w-4 h-4 border-2 border-gray-300 border-t-blue-500 rounded-full animate-spin"></div>
            <span class="text-sm">Ładowanie starszych wiadomości...</span>
          </div>
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
.custom-scrollbar {
  overflow-y: scroll !important;
}

.custom-scrollbar::-webkit-scrollbar {
  width: 6px;
}

.custom-scrollbar::-webkit-scrollbar-track {
  background: rgba(0, 0, 0, 0.05);
  border-radius: 3px;
}

.custom-scrollbar::-webkit-scrollbar-thumb {
  background-color: #cbd5e1;
  border-radius: 3px;
  transition: background-color 0.2s ease;
}

.custom-scrollbar:hover::-webkit-scrollbar-thumb {
  background-color: #94a3b8;
}

/* Firefox scrollbar styling */
.custom-scrollbar {
  scrollbar-width: thin;
  scrollbar-color: #cbd5e1 rgba(0, 0, 0, 0.05);
}

/* Force scroll container height */
main.custom-scrollbar {
  max-height: 100%;
  height: 100%;
}
</style>
