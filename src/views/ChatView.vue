<template>
  <div class="flex h-[calc(100vh-64px)] w-full bg-white overflow-hidden font-sans text-gray-900">

    <MessageMenu />

    <div class="flex-1 flex overflow-hidden bg-gray-200 relative p-3 gap-3">

      <div class="flex-1 flex flex-col min-w-0 relative bg-white rounded-xl shadow-sm ">
        <MessageBox ref="messageBoxRef" :boxId="chatId" mode="full" @open-modal="openModal" />
      </div>

      <ChatInfoPanel ref="chatInfoPanelRef" :chat-id="chatId" @go-to-message="onSearchGoTo" />

    </div>
  </div>
  </template>

<script setup lang="ts">
import '@/assets/animations/slideTransition.css';
import { ref, watch, computed, nextTick } from 'vue';

import MessageMenu from '@/components/MessageMenu.vue';
import MessageBox from '@/components/MessageBox.vue';
import ChatInfoPanel from '@/components/ChatView/ChatInfoPanel.vue';
import { useConversationsStore } from '@/stores/conversations';
import { useRoute } from 'vue-router';
// chatSettings moved into conversations store (convStore.settings)
// use conversations store directly for theme/emoji

// accept optional route prop chatId so this view can render messages for a given chat
const routeProps = withDefaults(defineProps<{ chatId?: string | number }>(), { chatId: undefined });
const route = useRoute();
const chatId = computed(() => Number(route.params.chatId ?? routeProps.chatId ?? ''));
const convStore = useConversationsStore();



// apply chat-specific theme and emoji when chat changes
watch(chatId, (newId) => {
  const s = convStore.settings.find(x => x.chatId === Number(newId));
  if (s?.themeId !== undefined) {
    // chatSettings.themeId stores a numeric index; map it to the real theme id string
    const idx = Number(s.themeId);
    const themesArr = convStore.themes as { id: string }[] | undefined;
    const mappedId = themesArr && themesArr[idx]?.id ? themesArr[idx].id : (themesArr && themesArr[0]?.id) ?? String(s.themeId);
    convStore.setSelectedTheme(mappedId);
  }
  if (s?.emoji) convStore.setSelectedEmoji(s.emoji);
});


const messageBoxRef = ref<InstanceType<typeof MessageBox> | null>(null);
const chatInfoPanelRef = ref<InstanceType<typeof ChatInfoPanel> | null>(null);

function openModal(modalType: 'CHANGE_E' | 'CHANGE_NICKNAME' | 'CHANGE_THEME') {
  if (!chatInfoPanelRef.value) return;
  if (modalType === 'CHANGE_NICKNAME') {
    chatInfoPanelRef.value.openEditNicknamesModal();
  } else if (modalType === 'CHANGE_THEME') {
    chatInfoPanelRef.value.openThemeModal();
  } else if (modalType === 'CHANGE_E') {
    chatInfoPanelRef.value.openEmojiModal();
  }
}

function onSearchGoTo(payload: { id: number; chatId?: string | number }) {
  // close right panel and scroll to message in MessageBox
  nextTick(() => {
    try {
      messageBoxRef.value?.scrollToMessage(payload.id);
    } catch {
      // ignore errors
    }
  });
}
</script>

<style scoped>
.custom-scrollbar::-webkit-scrollbar {
  width: 6px;
}
.custom-scrollbar::-webkit-scrollbar-track {
  background: transparent;
}
.custom-scrollbar::-webkit-scrollbar-thumb {
  background-color: rgba(0, 0, 0, 0.2);
  border-radius: 20px;
}
.custom-scrollbar:hover::-webkit-scrollbar-thumb {
  background-color: rgba(0, 0, 0, 0.3);
}
</style>
