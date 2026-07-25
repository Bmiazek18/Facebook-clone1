import { ref } from 'vue'
import { type ChatMessage } from '@/types/Message'
import {
  saveMessage as dbSaveMessage,
  saveMessages as dbSaveMessages,
  getMessagesForChat as dbGetMessagesForChat,
  getLastMessageForChat as dbGetLastMessageForChat,
  clearAllMessages as dbClearAllMessages
} from '@/utils/indexedDb'

export function useChatStorage() {
  const loading = ref(false)

  async function loadLocalMessages(chatId: string) {
    loading.value = true
    try {
      return await dbGetMessagesForChat(chatId)
    } finally {
      loading.value = false
    }
  }

  async function storeMessage(msg: ChatMessage) {
    await dbSaveMessage(msg)
  }

  async function storeMessages(msgs: ChatMessage[]) {
    await dbSaveMessages(msgs)
  }

  async function loadLastMessage(chatId: string) {
    return await dbGetLastMessageForChat(chatId)
  }

  async function clearOfflineStorage() {
    await dbClearAllMessages()
  }

  return {
    loading,
    loadLocalMessages,
    storeMessage,
    storeMessages,
    loadLastMessage,
    clearOfflineStorage
  }
}
