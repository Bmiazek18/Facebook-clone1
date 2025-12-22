import { ref } from 'vue'
import { defineStore, storeToRefs } from 'pinia'
import rawChats from '@/data/rawChats'
import initialMessages from '@/data/messages'
import { type ChatMessage, type LinkMessage } from '@/types/Message'
import chatSettings from '@/data/chatSettings'
import { useMessengerThemeStore, type Theme } from '@/stores/messengerTheme'

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
    let newMsg: ChatMessage;

    if (msg.type === 'link') {
      const linkMsg = msg as Partial<LinkMessage>;
      if (!linkMsg.url) {
        console.error('Link message must have a URL.');
        return;
      }

      linkMsg.content = linkMsg.url; // Set content to the URL for display purposes
      newMsg = {
        id: Date.now(),
        chatId: Number(chatId),
        sender: 'me',
        time: Date.now(),
        ...linkMsg,
      } as LinkMessage;
    } else {
      newMsg = {
        id: Date.now(),
        chatId: Number(chatId),
        sender: 'me',
        time: Date.now(),
        ...msg,
      } as ChatMessage;
    }

    messages.value.push(newMsg);
    _updateLastMessage(chatId, newMsg);

    return newMsg;
  }

  function recomputeLastMessage(chatId: number) {
    const msgs = getMessagesByChatId(chatId)
    if (!msgs.length) return

    const sorted = [...msgs].sort((a, b) => a.time - b.time)
    const last = sorted[sorted.length - 1]
    if (last) {
      _updateLastMessage(chatId, last)
    }
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
    const idx = themes.value.findIndex((t: Theme) => t.id === themeId)
    const s = _getOrCreateSettings(chatId)
    s.themeId = idx >= 0 ? idx : undefined
  }


  function updateGroupMembersNicknames(chatId: number, updatedMembers: GroupMember[]) {
    console.log('updateGroupMembersNicknames called for chatId:', chatId);
    const chat = chats.value.find(c => c.id === Number(chatId));
    if (chat && chat.type === 'group' && chat.groupMembers) {
      console.log('Chat found before update:', JSON.parse(JSON.stringify(chat))); // Deep copy for logging
      chat.groupMembers = updatedMembers;
      console.log('Chat found after update:', JSON.parse(JSON.stringify(chat))); // Deep copy for logging
    } else {
      console.log('Chat not found or not a group chat for chatId:', chatId);
    }
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
    updateGroupMembersNicknames,
    setSelectedTheme: themeStore.setSelectedTheme,
    setSelectedEmoji: themeStore.setSelectedEmoji,
  }
})

export default useConversationsStore
