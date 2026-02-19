<script setup lang="ts">
import { ref, onUnmounted, computed, nextTick, onMounted, watch, provide } from 'vue';
import { storeToRefs } from 'pinia';
import { useVirtualizer } from '@tanstack/vue-virtual';

import MultiMediaLightbox from './MediaLightbox.vue';
import MessageBoxHeader from '@/components/chat/MessageBoxHeader.vue';
import MessageBoxFooter from '@/components/chat/messageBoxFooter/index.vue';
import MessageItem from '@/components/chat/MessageItem.vue';
import { useConversationsStore } from '@/stores/conversations';
import { useMessageGrouper } from '@/composables/useMessageGrouper';
import { useLightbox } from '@/composables/useLightbox';
import { useFlipAnimation } from '@/composables/useFlipAnimation';

import type { Theme } from '@/stores/chatTheme';
import type { Message, ImageMessage, VideoMessage } from '@/types/Message';

const emit = defineEmits(['open-modal', 'back-to-list', 'show-info']);
const props = withDefaults(defineProps<{
  boxId?: string | number;
  mode?: 'card' | 'full';
  messages?: Message[],
  hideHeaderIcons?: boolean;
}>(), {
  mode: 'card',
  hideHeaderIcons: false
});

const convStore = useConversationsStore();
const { themes, selectedTheme } = storeToRefs(convStore);

const localMessages = ref<Message[]>([]);

const allMessagesList = computed((): Message[] => {
  if (props.boxId) {
    return convStore.getMessagesByChatId(Number(props.boxId)) as Message[];
  }
  return props.messages?.length ? props.messages : localMessages.value;
});

const isLoadingOlder = ref(false);
const messagesPerPage = 20;
const visibleMessagesCount = ref(messagesPerPage);

// Zwykła lista: najstarsze widoczne na górze, najnowsze na dole
const messagesList = computed((): Message[] => {
  const allMessages = allMessagesList.value;
  const totalMessages = allMessages.length;

  if (totalMessages <= visibleMessagesCount.value) {
    return allMessages;
  }
  return allMessages.slice(totalMessages - visibleMessagesCount.value);
});

const hasMoreToLoad = computed(() => {
  return allMessagesList.value.length > visibleMessagesCount.value;
});

// ==========================================
// WIRTUALIZATOR TANSTACK
// ==========================================
const chatContainer = ref<HTMLElement | null>(null);
const showScrollToBottomBtn = ref(false);

const virtualizerOptions = computed(() => ({
  count: messagesList.value.length,
  getScrollElement: () => chatContainer.value,
  estimateSize: () => 80,
  overscan: 30, // Szeroki margines chroni przed "gołym" białym ekranem podczas szybkiego scrolla
  getItemKey: (index: number) => messagesList.value[index]?.id ?? index,

  // Tarcza ochronna przeciw asynchronicznie ładującym się obrazkom
  shouldAdjustScrollPositionOnItemSizeChange: (item) => {
    if (!chatContainer.value) return false;
    const vItems = virtualizer.value.getVirtualItems();
    if (!vItems.length) return false;

    // Szukamy wiadomości, na którą obecnie patrzysz (przecina górną krawędź okna)
    const firstVisible = vItems.find(i => (i.start + i.size) > chatContainer.value!.scrollTop);
    if (!firstVisible) return false;

    // Korygujemy w locie TYLKO jeśli zmienił się rozmiar wiadomości nad Tobą (lub tej, na którą patrzysz)
    return item.index <= firstVisible.index;
  }
}));

const virtualizer = useVirtualizer(virtualizerOptions);

const checkAndFillContainer = async () => {
  if (!chatContainer.value || !hasMoreToLoad.value || isLoadingOlder.value) return;

  if (virtualizer.value.getTotalSize() <= chatContainer.value.clientHeight) {
    await loadOlderMessages();
    nextTick(() => {
      checkAndFillContainer();
    });
  }
};

const handleScroll = (e: Event) => {
  const target = e.target as HTMLElement;

  // Ładowanie jak na Messengerze: reagujemy, gdy użytkownik "uderzy" w sam sufit (<= 10px)
  if (target.scrollTop <= 10 && hasMoreToLoad.value && !isLoadingOlder.value) {
    loadOlderMessages();
  }

  // Przycisk "w dół": Pokaż, jeśli uciekliśmy od najnowszej wiadomości o 300px
  const distanceFromBottom = target.scrollHeight - target.scrollTop - target.clientHeight;
  showScrollToBottomBtn.value = distanceFromBottom > 300;
};

// ==========================================
// ŁADOWANIE STARSZYCH - CZYSTA MATEMATYKA (Brak Przeskoków)
// ==========================================
const loadOlderMessages = async () => {
  if (isLoadingOlder.value || !hasMoreToLoad.value || !chatContainer.value) return;

  const el = chatContainer.value;
  isLoadingOlder.value = true;

  try {
    // 1. Symulacja API / opóźnienia
    await new Promise(resolve => setTimeout(resolve, 500));

    // 2. Mierzymy całkowity rozmiar listy i pozycję scrolla ZANIM cokolwiek dodamy
    const previousTotalSize = virtualizer.value.getTotalSize();
    const previousScrollTop = el.scrollTop;

    // 3. Dodajemy stare wiadomości
    visibleMessagesCount.value += messagesPerPage;

    // 4. Czekamy, aż Vue wyrenderuje te nowe dane w DOM
    await nextTick();

    // 5. Mierzymy NOWĄ wysokość listy
    const newTotalSize = virtualizer.value.getTotalSize();

    // 6. Różnica to DOKŁADNIE to, o ile w pikselach rozrosła się góra czatu
    const heightDifference = newTotalSize - previousTotalSize;

    // 7. PERFEKCYJNA KOMPENSACJA:
    // Dodajemy "dołożoną" wysokość do obecnego scrolla.
    // Dzięki temu widok stoi w miejscu, zmienia się tylko pozycja suwaka przeglądarki.
    el.scrollTop = previousScrollTop + heightDifference;

  } catch (error) {
    console.error('Błąd wczytywania historii:', error);
  } finally {
    isLoadingOlder.value = false;
  }
};

const lastRead = ref<Record<string, number>>({ 'user_1': 0, 'user_2': 0, 'ghost_tester': 0 });

const { capturePositions, onAvatarEnter, onAvatarLeave } = useFlipAnimation(chatContainer);
provide('flip-animation', { onAvatarEnter, onAvatarLeave });

const msgs = messagesList.value;
if (msgs.length > 0) {
  const lastMsgId = msgs[msgs.length - 1].id;
  const lastMsgId2 = msgs.length > 1 ? msgs[msgs.length - 2].id : lastMsgId;
  lastRead.value['user_1'] = lastMsgId;
  lastRead.value['user_2'] = lastMsgId2;
}

watch(lastRead, () => capturePositions(), { deep: true, flush: 'pre' });

let simulationInterval: number | null = null;
const TEST_USER_ID = 'ghost_tester';

onMounted(async () => {
  scrollToBottom('auto');
  setTimeout(() => scrollToBottom('auto'), 100);
  await nextTick();
  checkAndFillContainer();

  setTimeout(() => {
    let currentIndex = 0;
    simulationInterval = setInterval(() => {
      const msgs = messagesList.value;
      if (!msgs || msgs.length === 0) return;
      if (currentIndex >= msgs.length) currentIndex = 0;
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

onUnmounted(() => { if (simulationInterval) clearInterval(simulationInterval); });

watch(() => props.messages, (newVal) => {
  if (newVal) {
    localMessages.value = [...newVal];
    // Zjedź automatycznie TYLKO jeśli użytkownik i tak czyta najnowsze (nie przegląda historii)
    if (!showScrollToBottomBtn.value && !isLoadingOlder.value) {
      scrollToBottom('smooth');
    }
  }
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

function scrollToBottom(behavior: 'auto' | 'smooth' = 'auto') {
  nextTick(() => {
    const total = messagesList.value.length;
    if (total > 0) {
      virtualizer.value.scrollToIndex(total - 1, { align: 'end', behavior });
    }
  });
}

function scrollToMessage(messageId: number) {
  const index = messagesList.value.findIndex(m => m.id === messageId);
  if (index !== -1) {
    // 1. Zaczynamy płynnie scrollować do wiadomości (na środek ekranu)
    virtualizer.value.scrollToIndex(index, { align: 'center', behavior: 'smooth' });


    setTimeout(() => {
      highlightedMessageId.value = messageId;

      // 3. Po 2 sekundach gasimy animację
      setTimeout(() => { highlightedMessageId.value = null; }, 2000);
    }, 500);
  }
}

const highlightedMessageId = ref<number | null>(null);
const { getDisplayTime, getMessagePositionInGroup } = useMessageGrouper(messagesList);
const { isLightboxOpen, currentMediaIndex, filteredMedia, openLightbox } = useLightbox(messagesList);
const isDragging = ref(false);
const pinnedMessages = ref<number[]>([]);

const handlePin = (messageId: number) => {
  const index = pinnedMessages.value.indexOf(messageId);
  if (index > -1) {
    pinnedMessages.value.splice(index, 1);
  } else {
    pinnedMessages.value.push(messageId);
  }
};

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

  if (!isLoadingOlder.value) scrollToBottom('smooth');
};

const replyTarget = ref<Message | null>(null);

const handleAddMessage = (msg: Message) => {
  if (props.boxId) {
    const newMessage = {
      ...msg,
      isReply: !!replyTarget.value,
      replyToId: replyTarget.value?.id,
      replyToSender: replyTarget.value?.sender === 'me' ? 'Ty' : replyTarget.value?.sender,
      replyToContentSnippet: replyTarget.value?.content?.slice(0, 100)
    };
    convStore.addMessage(Number(props.boxId), newMessage);
    replyTarget.value = null;
  } else {
    localMessages.value.push(msg);
  }
  // Wymuszony zjazd na sam dół po wysłaniu własnej wiadomości
  nextTick(() => scrollToBottom('smooth'));
};

const handleAddReaction = (messageId: number, emoji: string) => {
  if (props.boxId) {
    convStore.addReaction(Number(props.boxId), messageId, emoji);
  } else {
    const target = localMessages.value.find(m => m.id === messageId);
    if(target) {
      if(!target.reactions) target.reactions = [];
      target.reactions.push(emoji);
    }
  }
}

const headerTitle = computed(() => {
  if (!props.boxId) return 'Czat';
  const chat = convStore.chats.find(c => c.id === Number(props.boxId));
  return chat ? chat.name : `Czat ${props.boxId}`;
});

const isFull = computed(() => props.mode === 'full');
const lastMessageId = computed(() => {
  const allMsgs = allMessagesList.value;
  return allMsgs.length > 0 ? allMsgs[allMsgs.length - 1].id : null;
});

defineExpose({ scrollToMessage });
</script>

<template>
  <div
    :class="[isFull ? 'relative flex-1 flex flex-col h-full w-full' : 'flex items-center w-[328px] box-content relative justify-center pt-4 px-2 ']"
    @dragover.prevent="isDragging = true"
    @dragleave.prevent="isDragging = false"
    @drop.prevent="handleDrop"
  >
    <div
      :class="[
        isFull ? 'w-full h-full rounded-t-xl shadow-none' : 'w-full max-w-[328px]  h-[455px] rounded-xl shadow-2xl',
        'bg-theme-bg-secondary flex flex-col relative transition-all duration-300 overflow-hidden'
      ]"
    >
      <MessageBoxHeader :title="headerTitle" :users="[headerTitle]" :boxId="props.boxId ?? 1" :hideIcons="props.hideHeaderIcons" :themes="boxTheme" @back="emit('back-to-list')" @show-info="emit('show-info')" />

      <div class="relative flex-1 flex flex-col min-h-0">

        <main
          ref="chatContainer"
          @scroll="handleScroll"
          class="relative flex-1 flex flex-col overflow-y-auto custom-scrollbar bg-theme-bg min-h-0"
          :style="[
            boxTheme?.backgroundImage ? { backgroundImage: `url(${boxTheme.backgroundImage})`, backgroundSize: 'cover', backgroundPosition: 'center' } : {},
            { overflowAnchor: 'none' }
          ]"
        >
          <div
            v-if="isLoadingOlder"
            class="absolute top-0 left-0 w-full px-4 py-3 text-center z-10 transition-opacity duration-300"
          >
            <div class="inline-flex items-center justify-center space-x-2 text-theme-text-secondary bg-theme-bg/90 backdrop-blur-md shadow-sm px-4 py-1.5 rounded-full border border-theme-border/50">
              <div class="w-4 h-4 border-2 border-theme-border border-t-theme-primary rounded-full animate-spin"></div>
              <span class="text-xs font-semibold uppercase tracking-wide">Ładowanie historii...</span>
            </div>
          </div>

          <div
            class="w-full relative shrink-0 mt-auto pb-4"
            :style="{ height: `${virtualizer.getTotalSize()}px` }"
          >
            <div
              v-for="virtualItem in virtualizer.getVirtualItems()"
              :key="virtualItem.key"
              :ref="virtualizer.measureElement"
              :data-index="virtualItem.index"
              class="absolute top-0 left-0 w-full"
              :style="{ transform: `translateY(${virtualItem.start}px)` }"
            >
              <div class="px-2 ">
                <div v-if="getDisplayTime(virtualItem.index)" class="text-[11px] font-medium text-center my-3 select-none uppercase tracking-wide opacity-80" :style="{ color: boxTheme.timestampColor }">
                  {{ getDisplayTime(virtualItem.index) }}
                </div>

                <MessageItem
                  :message="messagesList[virtualItem.index]"
                  :theme="boxTheme"
                  :metadata="{
                    position: getMessagePositionInGroup(virtualItem.index),
                    isLatest: messagesList[virtualItem.index].id === lastMessageId
                  }"
                  :id="`msg-${props.boxId ?? '0'}-${messagesList[virtualItem.index].id}`"
                  :isHighlighted="messagesList[virtualItem.index].id === highlightedMessageId"
                  :isPinned="pinnedMessages.includes(messagesList[virtualItem.index].id)"
                  @open-lightbox="openLightbox"
                  :last-read-map="lastRead"
                  @reply="replyTarget = $event"
                  @add-reaction="({ messageId, emoji }) => handleAddReaction(messageId, emoji)"
                  @open-modal="emit('open-modal', $event)"
                  @scroll-to-message="scrollToMessage"
                  @pin="handlePin"
                />
              </div>
            </div>
          </div>
        </main>

        <transition name="fade-slide">
          <button
            v-if="showScrollToBottomBtn"
            @click="scrollToBottom()"
            class="absolute bottom-4 left-1/2 -translate-x-1/2 p-3 z-50 rounded-full shadow-lg flex items-center justify-center text-blue-500 bg-theme-bg hover:bg-blue-600 transition-colors backdrop-blur-md"
            aria-label="Przewiń na dół"
          >
            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2.5" d="M19 14l-7 7m0 0l-7-7m7 7V3"></path>
            </svg>
          </button>
        </transition>

      </div> <MessageBoxFooter :reply="replyTarget" :boxId="props.boxId" :themes="boxTheme" @clearReply="replyTarget = null" @add-message="handleAddMessage" />
      <MultiMediaLightbox v-if="isLightboxOpen" v-model="isLightboxOpen" :media="filteredMedia" :startIndex="currentMediaIndex" />
    </div>
  </div>
</template>

<style scoped>
.custom-scrollbar {
  overflow-y: scroll !important;
}
main.custom-scrollbar {
  max-height: 100%;
  height: 100%;
  /* Pozwala przeglądarce lepiej zarządzać kotwiczeniem treści */
  overflow-anchor: auto !important;
}
.custom-scrollbar::-webkit-scrollbar {
  width: 6px;
}

.custom-scrollbar::-webkit-scrollbar-track {
  background: rgba(var(--theme-bg-rgb), 0.05);
  border-radius: 3px;
}

.custom-scrollbar::-webkit-scrollbar-thumb {
  background-color: var(--theme-text-secondary);
  border-radius: 3px;
  transition: background-color 0.2s ease;
}

.custom-scrollbar:hover::-webkit-scrollbar-thumb {
  background-color: var(--theme-text);
}

.custom-scrollbar {
  scrollbar-width: thin;
  scrollbar-color: var(--theme-text-secondary) rgba(var(--theme-bg-rgb), 0.05);
}

main.custom-scrollbar {
  max-height: 100%;
  height: 100%;
}

.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
}

.fade-slide-enter-from,
.fade-slide-leave-to {
  opacity: 0;
  transform: translateY(15px) scale(0.9);
}
</style>
