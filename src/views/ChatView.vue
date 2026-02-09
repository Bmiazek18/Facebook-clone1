<script setup lang="ts">
import '@/assets/animations/slideTransition.css';
import { ref, watch, computed, nextTick } from 'vue';
import { useBreakpoints } from '@vueuse/core';

import MessageMenu from '@/layouts/Navbar/MessageMenu.vue';
import MessageBox from '@/components/chat/MessageBox/MessageBox.vue';
import ChatInfoPanel from '@/components/chat/ChatInfoPanel.vue';
import { useConversationsStore } from '@/stores/conversations';
import { useRoute } from 'vue-router';
// chatSettings moved into conversations store (convStore.settings)
// use conversations store directly for theme/emoji

// accept optional route prop chatId so this view can render messages for a given chat
const routeProps = withDefaults(defineProps<{ chatId?: string | number }>(), { chatId: undefined });
const route = useRoute();
const chatId = computed(() => Number(route.params.chatId ?? routeProps.chatId ?? ''));
const convStore = useConversationsStore();

// Stan dla mobile - pokazuj MessageBox gdy użytkownik kliknie w czat
const showMobileChat = ref(false);
// Stan dla mobile - pokazuj ChatInfoPanel na pełnym ekranie
const showMobileInfo = ref(true);

const breakpoints = useBreakpoints({
  tablet: 768,
});
const isMobile = breakpoints.smaller('tablet');


// Gdy chatId się zmieni na mobile, pokazuj MessageBox
watch(chatId, (newId) => {
  if (newId) {
    showMobileChat.value = true;
    showMobileInfo.value = true; // Reset info panel przy zmianie czatu
  }

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

<template>
  <div class="flex h-[calc(100vh-64px)] mt-[54px] w-full bg-theme-bg-secondary overflow-hidden font-sans text-theme-text">

    <!-- MessageMenu - na mobile widoczne tylko gdy showMobileChat = false -->
    <div :class="chatId && showMobileChat ? 'hidden md:block' : 'block'" class="w-full md:w-auto">
      <MessageMenu :is-embedded="true" />
    </div>

    <!-- MessageBox i ChatInfoPanel - na mobile widoczne tylko gdy showMobileChat = true -->
    <div
      v-if="chatId"
      :class="showMobileChat ? 'flex' : 'hidden md:flex'"
      class="flex-1 overflow-hidden bg-theme-bg relative p-3 gap-3"
    >

      <!-- MessageBox -->
      <div
        v-if="!isMobile || !showMobileInfo"
        class="flex-1 flex flex-col min-w-0 relative bg-theme-bg rounded-xl shadow-sm"
      >
        <MessageBox
          ref="messageBoxRef"
          :boxId="chatId"
          mode="full"
          :hide-header-icons="true"
          @open-modal="openModal"
          @back-to-list="showMobileChat = false"
          @show-info="showMobileInfo = !showMobileInfo"
        />
      </div>

      <!-- ChatInfoPanel -->
      <ChatInfoPanel
        v-if="showMobileInfo"
        ref="chatInfoPanelRef"
        :chat-id="chatId"
        @go-to-message="onSearchGoTo"
        @back="showMobileInfo = false"
      />



    </div>
  </div>
  </template>

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
