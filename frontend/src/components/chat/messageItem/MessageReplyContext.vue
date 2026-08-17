<script setup lang="ts">
import ReplyIcon from 'vue-material-design-icons/Reply.vue'
import type { Message } from '@/types/Message'
import { useConversationsStore } from '@/stores/conversations'
import { computed } from 'vue'

const props = defineProps<{
  reply: Message
}>()

const emit = defineEmits(['scrollToReplied'])

const convStore = useConversationsStore()

const labelText = computed(() => {
  const replyMsg = props.reply
  if (replyMsg.sender === 'me') {
    if (replyMsg.replyToSender === 'Ty') {
      return 'Odpowiedziałeś sobie'
    }
    return `Odpowiedziałeś użytkownikowi ${replyMsg.replyToSender || 'Użytkownik'}`
  } else {
    const chatName = convStore.chats.find((c) => String(c.id) === String(replyMsg.chatId))?.name || 'Użytkownik'
    if (replyMsg.replyToSender === 'Ty') {
      return `Użytkownik ${chatName} odpowiedział Ci`
    }
    return `Użytkownik ${chatName} odpowiedział użytkownikowi ${replyMsg.replyToSender || 'Użytkownik'}`
  }
})
</script>

<template>
  <div @click="emit('scrollToReplied')" class="cursor-pointer">
    <div
      class="flex flex-col"
      :class="{
        'items-start ml-10': reply.sender === 'them',
        'items-end': reply.sender === 'me',
      }"
    >
      <span class="flex flex-row text-[12px] align-center">
        <ReplyIcon :size="12" />
        <p class="font-semibold">{{ labelText }}</p>
      </span>

      <div
        class="pb-2 text-xs -mb-[15px] bg-gray-100/70 rounded-lg backdrop-blur-sm max-w-[80%] overflow-hidden"
        style="word-break: break-word"
      >
        <p class="text-gray-700 p-3 text-[11px]">{{ reply.replyToContentSnippet }}</p>
      </div>
    </div>
  </div>
</template>
