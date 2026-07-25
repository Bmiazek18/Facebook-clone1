<script setup lang="ts">
import '@/assets/animations/slideTransition.css'
import { ref, watch, computed, nextTick, onUnmounted } from 'vue'
import { useRoute } from 'vue-router'
import { useBreakpoints } from '@vueuse/core'

definePageMeta({
  key: 'chat-page'
})

import MessageMenu from '@/layouts/Navbar/MessageMenu.vue'
import MessageBox from '@/components/chat/messageBox/index.vue'
import ChatInfoPanel from '@/components/chat/info/ChatInfoPanel.vue'
import { useConversationsStore } from '@/stores/conversations'

const routeProps = withDefaults(defineProps<{ chatId?: string }>(), { chatId: undefined })
const route = useRoute()

// 1. ZMIANA: Traktujemy chatId zawsze jako String (wsparcie dla ScyllaDB UUID)
const chatId = computed(() => {
  const rawId = route.params.chatId ?? routeProps.chatId ?? ''
  return String(rawId)
})

const convStore = useConversationsStore()

const showMobileChat = ref(false)
const showMobileInfo = ref(false)

const breakpoints = useBreakpoints({
  tablet: 768,
})
const isMobile = breakpoints.smaller('tablet')

watch(
  [chatId, () => convStore.currentUserUuid],
  ([newId, newUserId]) => {
    console.log('[[chatId]].vue: Watch triggered with chatId:', newId, 'currentUserUuid:', newUserId)
    // Sprawdzamy czy mamy prawidłowe UUID użytkownika i wybrane ID czatu
    if (newId && newUserId && newUserId !== '1' && newUserId !== '0') {
      showMobileChat.value = true
      showMobileInfo.value = false

      // 2. ZMIANA: Natychmiast ustawiamy aktywny czat, żeby zablokować toasty z powiadomieniami dla tego czatu!
      convStore.activeChatId = String(newId)

      console.log('[[chatId]].vue: Fetching messages for activeChatId:', convStore.activeChatId)
      // Pobieramy historię
      convStore.fetchMessages(String(newId))
    } else {
      convStore.activeChatId = null
    }

    if (newId) {
      const s = convStore.settings.find((x) => String(x.chatId) === String(newId))
      if (s?.themeId !== undefined) {
        const idx = Number(s.themeId)
        const themesArr = convStore.themes as { id: string }[] | undefined
        const mappedId =
          themesArr && themesArr[idx]?.id
            ? themesArr[idx].id
            : ((themesArr && themesArr[0]?.id) ?? String(s.themeId))
        convStore.setSelectedTheme(mappedId)
      }
      if (s?.emoji) convStore.setSelectedEmoji(s.emoji)
    }
  },
  { immediate: true },
)

const messageBoxRef = ref<InstanceType<typeof MessageBox> | null>(null)
const chatInfoPanelRef = ref<InstanceType<typeof ChatInfoPanel> | null>(null)

function openModal(modalType: 'CHANGE_E' | 'CHANGE_NICKNAME' | 'CHANGE_THEME') {
  if (!chatInfoPanelRef.value) return
  if (modalType === 'CHANGE_NICKNAME') {
    chatInfoPanelRef.value.openEditNicknamesModal()
  } else if (modalType === 'CHANGE_THEME') {
    chatInfoPanelRef.value.openThemeModal()
  } else if (modalType === 'CHANGE_E') {
    chatInfoPanelRef.value.openEmojiModal()
  }
}

function onSearchGoTo(payload: { id: number; chatId?: string | number }) {
  nextTick(() => {
    try {
      messageBoxRef.value?.scrollToMessage(payload.id)
    } catch {}
  })
}

onUnmounted(() => {
  convStore.activeChatId = null
})
</script>

<template>
  <div
    class="flex h-[calc(100vh-64px)] mt-13.5 w-full bg-theme-bg-secondary overflow-hidden text-theme-text"
  >
    <div :class="chatId && showMobileChat ? 'hidden md:block' : 'block'" class="w-full md:w-auto">
      <MessageMenu :is-embedded="true" />
    </div>

    <div
      v-if="chatId"
      :class="showMobileChat ? 'flex' : 'hidden md:flex'"
      class="flex-1 overflow-hidden bg-theme-bg relative p-3 gap-4"
    >
      <!-- MessageBox (Lewa kolumna) -->
      <div
        v-if="!isMobile || !showMobileInfo"
        class="flex-1 flex flex-col min-w-0 relative bg-theme-bg rounded-xl shadow-sm"
      >
        <MessageBox
          :key="chatId"
          ref="messageBoxRef"
          :boxId="chatId"
          mode="full"
          :hide-header-icons="true"
          @open-modal="openModal"
          @back-to-list="showMobileChat = false"
          @show-info="showMobileInfo = !showMobileInfo"
        />
      </div>

      <!-- ChatInfoPanel (Prawa kolumna) -->
      <div
        v-if="showMobileInfo"
        class="w-full md:w-[calc(33.7331%-16px)] shrink-0 h-full flex flex-col min-w-[300px] max-w-[480px]"
      >
        <ChatInfoPanel
          ref="chatInfoPanelRef"
          :chat-id="chatId"
          @go-to-message="onSearchGoTo"
          @back="showMobileInfo = false"
        />
      </div>
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
