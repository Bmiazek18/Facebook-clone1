<template>
  <div class="w-full max-w-[550px] mx-auto p-4 bg-theme-bg dark:bg-[#1c1d1e] text-theme-text font-sans rounded-xl">
    <div v-if="pinnedMessages.length === 0" class="flex flex-col items-center justify-center py-12 text-center text-theme-text-secondary">
      <svg xmlns="http://www.w3.org/2000/svg" class="h-12 w-12 mb-3 opacity-60 text-theme-text-secondary" fill="none" viewBox="0 0 24 24" stroke="currentColor">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M15.172 7l-6.586 6.586a2 2 0 102.828 2.828l6.414-6.586a4 4 0 00-5.656-5.656l-6.415 6.585a6 6 0 108.486 8.486L20.5 13" />
      </svg>
      <p class="text-sm font-medium">{{ $t('chat.brakPrzypietychWiadomosciW') }}</p>
    </div>

    <div v-else class="space-y-4 max-h-[400px] overflow-y-auto pr-1 scrollbar-thin">
      <div
        v-for="msg in pinnedMessages"
        :key="msg.id"
        class="flex items-start justify-between p-3.5 rounded-xl bg-theme-bg-hover hover:bg-black/5 dark:hover:bg-white/5 transition duration-200 group border border-theme-border/50"
      >
        <div class="flex items-start space-x-3 min-w-0 flex-1 cursor-pointer" @click="goToMessage(msg.id)">
          <img
            :src="getAvatar(msg)"
            :alt="$t('chat.avatar')"
            class="w-10 h-10 rounded-full object-cover shrink-0 border border-theme-border shadow-sm bg-gray-100"
          />

          <div class="flex flex-col min-w-0 flex-1">
            <div class="flex items-baseline space-x-2">
              <span class="text-[14px] font-bold text-theme-text">
                {{ getName(msg) }}
              </span>
              <span class="text-[11px] text-theme-text-secondary">
                {{ formatTime(msg.time) }}
              </span>
            </div>

            <div class="text-[14px] text-theme-text-secondary mt-1 break-words line-clamp-3">
              {{ msg.content }}
            </div>
          </div>
        </div>

        <button
          @click="unpinMessage(msg.id)"
          class="ml-3 shrink-0 p-2 rounded-full hover:bg-theme-bg/80 text-theme-text-secondary hover:text-red-500 transition-colors shadow-sm bg-theme-bg border border-theme-border"
          :title="$t('chat.odepnijWiadomosc')"
        >
          <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" class="w-4 h-4 transform rotate-45">
            <path stroke-linecap="round" stroke-linejoin="round" d="M19.5 12h-15m0 0l6.75 6.75M4.5 12l6.75-6.75" />
          </svg>
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, inject } from 'vue'
import { useConversationsStore } from '@/stores/conversations'

const props = defineProps<{
  boxId: string | number
}>()

const emit = defineEmits<{
  (e: 'close'): void
}>()

const convStore = useConversationsStore()

const chatActions = inject<{
  scrollToMessage: (messageId: number | string) => void
  pin: (messageId: number | string) => void
}>('chatActions')

const pinnedMessages = computed(() => {
  const allMsgs = convStore.getMessagesByChatId(props.boxId) || []
  return allMsgs.filter((m: any) => m.isPinned)
})

const chat = computed(() => {
  return convStore.chats.find((c) => String(c.id) === String(props.boxId))
})

const getAvatar = (msg: any) => {
  if (msg.sender === 'me') {
    return '/default-avatar.png'
  }
  return chat.value?.avatarUrl || '/default-avatar.png'
}

const getName = (msg: any) => {
  return msg.sender === 'me' ? 'Ty' : (chat.value?.name || 'Rozmówca')
}

const formatTime = (t: number | string | undefined) => {
  if (!t) return ''
  const date = typeof t === 'number' ? new Date(t) : new Date(String(t))
  return date.toLocaleDateString(undefined, {
    day: 'numeric',
    month: 'short',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const goToMessage = (msgId: string | number) => {
  chatActions?.scrollToMessage(Number(msgId))
  emit('close')
}

const unpinMessage = (msgId: string | number) => {
  chatActions?.pin(Number(msgId))
}
</script>

<style scoped>
.scrollbar-thin::-webkit-scrollbar {
  width: 5px;
}
.scrollbar-thin::-webkit-scrollbar-track {
  background: transparent;
}
.scrollbar-thin::-webkit-scrollbar-thumb {
  background: rgba(155, 155, 155, 0.2);
  border-radius: 4px;
}
.scrollbar-thin::-webkit-scrollbar-thumb:hover {
  background: rgba(155, 155, 155, 0.4);
}
</style>
