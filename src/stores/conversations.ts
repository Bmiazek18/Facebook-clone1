import { ref } from 'vue'
import { defineStore, storeToRefs } from 'pinia'
import rawChats from '@/data/rawChats'
import initialMessages, { type ChatMessage } from '@/data/messages'
import chatSettings from '@/data/chatSettings'
import { useMessengerThemeStore } from '@/stores/messengerTheme'

export const useConversationsStore = defineStore('conversations', () => {
  const chats = ref(structuredClone(rawChats))
  const messages = ref(structuredClone(initialMessages) as ChatMessage[])
  const settings = ref(structuredClone(chatSettings))

  const themeStore = useMessengerThemeStore()
  const { selectedThemeId, selectedEmoji, themes, selectedTheme } = storeToRefs(themeStore)

  function getMessagesByChatId(chatId: number) {
    return messages.value.filter(m => m.chatId === Number(chatId))
  }

  function addMessage(chatId: number, msg: Partial<ChatMessage>) {
    const newMsg: ChatMessage = {
      id: Date.now(),
      chatId: Number(chatId),
      sender: 'me',
      time: Date.now(),
      ...msg,
    } as ChatMessage

    messages.value.push(newMsg)
    _updateLastMessage(chatId, newMsg)

    return newMsg
  }

  function recomputeLastMessage(chatId: number) {
    const msgs = getMessagesByChatId(chatId)
    if (!msgs.length) return

    const sorted = [...msgs].sort((a, b) => a.time - b.time)
    const last = sorted[sorted.length - 1]
    _updateLastMessage(chatId, last)
  }

  function _updateLastMessage(chatId: number, msg: ChatMessage) {
    const chat = chats.value.find(c => c.id === Number(chatId))
    if (chat) {
      chat.lastMessage = msg.sender === 'me' ? `Ty: ${msg.content}` : `${msg.content}`
    }
  }

  function updateChatName(chatId: number, newName: string) {
    const chat = chats.value.find(c => c.id === Number(chatId))
    if (chat) {
      chat.name = newName
    }
  }

  function setChatEmoji(chatId: number, emoji: string) {
    const s = _getOrCreateSettings(chatId)
    s.emoji = emoji
  }

  function setChatThemeById(chatId: number, themeId: string) {
    const idx = themes.value.findIndex((t: any) => t.id === themeId)
    const s = _getOrCreateSettings(chatId)
    s.themeId = idx >= 0 ? idx : undefined
  }

  function _getOrCreateSettings(chatId: number) {
    let s = settings.value.find(x => x.chatId === Number(chatId))
    if (!s) {
      s = { chatId: Number(chatId) }
      settings.value.push(s)
    }
    return s
  }

  return {
    chats,
    messages,
    settings,
    themes,
    selectedThemeId,
    selectedEmoji,
    selectedTheme,
    getMessagesByChatId,
    addMessage,
    recomputeLastMessage,
    updateChatName,
    setChatEmoji,
    setChatThemeById,
    setSelectedTheme: themeStore.setSelectedTheme,
    setSelectedEmoji: themeStore.setSelectedEmoji,
  }
})

export default useConversationsStore
