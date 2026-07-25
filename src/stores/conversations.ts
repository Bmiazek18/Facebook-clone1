import { ref, watch, computed } from 'vue'
import { defineStore, storeToRefs } from 'pinia'
import { type Chat } from '@/types/Chat'
const rawChats: Chat[] = []
import { type ChatMessage } from '@/types/Message'
import { useChatThemeStore } from '@/stores/chatTheme'
import { useAuthStore } from '@/stores/auth'
import { useChatStore } from '@/stores/chat'
import { useNotify } from '@/composables/shared/useNotify'
import { useChatMqtt } from '@/composables/chat/useChatMqtt'
import { useChatStorage } from '@/composables/chat/useChatStorage'
import { useUserCache } from '@/composables/shared/useUserCache'
import { useChatCalls } from '@/composables/chat/useChatCalls'
import { useChatSettings } from '@/composables/chat/useChatSettings'
import { useChatApi } from '@/composables/chat/useChatApi'

export const useConversationsStore = defineStore('conversations', () => {
  const chats = ref(structuredClone(rawChats))
  const messages = ref<ChatMessage[]>([])
  const typingUsers = ref<Record<string, boolean>>({})
  const typingTimeouts: Record<string, any> = {}

  const themeStore = useChatThemeStore()
  const { selectedThemeId, selectedEmoji, themes, selectedTheme } = storeToRefs(themeStore)

  const chatMqtt = useChatMqtt()
  const chatStorage = useChatStorage()
  const userCache = useUserCache()
  const chatCalls = useChatCalls()
  const chatSettings = useChatSettings()

  const isMqttConnected = chatMqtt.isMqttConnected
  const mqttClientId = chatMqtt.mqttClientId
  const activeChatId = ref<string | null>(null)
  const lastReadMaps = ref<Record<string, Record<string, string>>>({})
  
  const incomingCall = chatCalls.incomingCall
  const settings = chatSettings.settings
  const usersCache = userCache.usersCache
  const getOrFetchUser = userCache.getOrFetchUser

  const authStore = useAuthStore()
  const currentUserUuid = computed(() =>
    String(authStore.currentUser?.id || authStore.currentUserId || '1'),
  )

  const chatApi = useChatApi(currentUserUuid, chats, messages, lastReadMaps)

  const fetchInbox = chatApi.fetchInbox
  const fetchMessages = (chatId: string) => chatApi.fetchMessages(chatId, activeChatId)
  const addMessage = chatApi.addMessage
  const addReaction = chatApi.addReaction
  const getSymmetricConversationId = chatApi.getSymmetricConversationId

  if (import.meta.client) {
    const connectMqtt = () => {
      chatMqtt.connectMqtt(currentUserUuid.value, (topic, payload) => {
        if (payload.type === 'typing') {
          const senderId = payload.senderId
          if (senderId !== currentUserUuid.value) {
            typingUsers.value[senderId] = !!payload.isTyping
            if (payload.isTyping) {
              if (typingTimeouts[senderId]) {
                clearTimeout(typingTimeouts[senderId])
              }
              typingTimeouts[senderId] = setTimeout(() => {
                typingUsers.value[senderId] = false
              }, 5000)
            }
          }
          return
        }

        if (payload.type === 'call_initiated') {
          console.log('Receiver MQTT: Detected call_initiated event!', payload)
          if (payload.callerId !== currentUserUuid.value) {
            getOrFetchUser(payload.callerId).then((caller) => {
              chatCalls.initiateCall(
                payload.conversationId,
                payload.callerId,
                payload.callType || 'video',
                caller ? caller.name : 'Użytkownik',
                caller?.avatar || `${import.meta.env.VITE_API_URL || 'http://localhost:8080'}/api/users/avatar/default-avatar.svg`
              )
            }).catch(() => {})
          }
          return
        }

        if (payload.type === 'call_rejected' || payload.type === 'call_ended') {
          console.log('Receiver MQTT: Call ended or rejected', payload)
          chatCalls.endCall()
          return
        }

        if (payload.type === 'chat_setting_changed') {
          console.log('Receiver MQTT: Detected chat_setting_changed event!', payload)
          const convId = payload.conversationId
          const chat = chats.value.find(c => getSymmetricConversationId(c.id) === convId)
          if (chat) {
            const s = chatSettings._getOrCreateSettings(chat.id)
            if (payload.emoji !== undefined) {
              s.emoji = payload.emoji
            }
            if (payload.themeId !== undefined && payload.themeId !== null) {
              s.themeId = payload.themeId
            }
          }
          return
        }

        if (payload.type === 'chat_nickname_changed') {
          console.log('Receiver MQTT: Detected chat_nickname_changed event!', payload)
          const convId = payload.conversationId
          const chat = chats.value.find(c => getSymmetricConversationId(c.id) === convId)
          if (chat && chat.type === 'private') {
            chat.otherUserNickname = payload.nickname
          }
          return
        }

        if (payload.type === 'read') {
          const chatUserUuid = payload.senderId
          const convId = payload.conversationId
          const chat = chats.value.find(c => getSymmetricConversationId(c.id) === convId)
          if (chat) {
            if (!lastReadMaps.value[chat.id]) {
              lastReadMaps.value[chat.id] = {}
            }
            lastReadMaps.value[chat.id][`user_${payload.senderId}`] = payload.lastReadMessageId
          }
          return
        }

        if (payload.type === 'message_pinned') {
          const msgId = payload.messageId
          const isPinned = payload.isPinned
          const msg = messages.value.find((m) => m.id === msgId)
          if (msg) {
            msg.isPinned = isPinned
            chatStorage.storeMessage(msg).catch((err) => {
              console.error('Failed to update pin in IndexedDB from MQTT:', err)
            })
          }
          return
        }

        if (payload.type === 'reaction') {
          if (payload.senderClientId === mqttClientId.value) {
            return
          }
          const targetMsgId = payload.targetMessageId
          const emoji = payload.reactionEmoji
          const senderId = payload.senderId

          const isPrivate = !payload.participantIds || payload.participantIds.length <= 2
          const logicalChatId = isPrivate
            ? (senderId === currentUserUuid.value
                ? (payload.participantIds?.find((id: any) => String(id) !== String(currentUserUuid.value)) || senderId)
                : senderId)
            : payload.conversationId

          const toggleReactionObject = (msg: ChatMessage) => {
            if (!msg.reactions) {
              msg.reactions = {}
            }
            const list = msg.reactions[emoji] || []
            const uId = `user_${senderId}`
            if (list.includes(uId)) {
              msg.reactions[emoji] = list.filter((id) => id !== uId)
            } else {
              msg.reactions[emoji] = [...list, uId]
            }
            if (msg.reactions[emoji].length === 0) {
              delete msg.reactions[emoji]
            }
          }

          const targetMsg = messages.value.find((m) => m.id === targetMsgId)
          if (targetMsg) {
            toggleReactionObject(targetMsg)
            chatStorage.storeMessage(targetMsg).catch((err) => {
              console.error('Failed to update reaction in IndexedDB from MQTT:', err)
            })
          }

          const chat = chats.value.find((c) => String(c.id) === String(logicalChatId))
          if (chat && senderId !== currentUserUuid.value) {
            chat.lastMessage = `Zareagował ${emoji} na Twoją wiadomość`
          }
          return
        }

        const msgId = payload.messageId
        const convId = payload.conversationId
        const senderId = payload.senderId
        const text = payload.text

        const isPrivate = !payload.participantIds || payload.participantIds.length <= 2
        const logicalChatId = isPrivate
          ? (senderId === currentUserUuid.value
              ? (payload.participantIds?.find((id: any) => String(id) !== String(currentUserUuid.value)) || senderId)
              : senderId)
          : convId

        if (messages.value.some((m) => m.id === msgId)) {
          return
        }

        let parsedType = payload.audioUrl ? 'audio' : (payload.imageUrl ? 'image' : (payload.fileUrl ? 'file' : (payload.linkUrl ? 'link' : 'text')))
        let subType: any = undefined
        let payloadValue: any = undefined

        if (text && text.startsWith('SYSTEM_ACTION:')) {
          const parts = text.split(':')
          if (parts[1] === 'call_rejected') {
            parsedType = 'call_rejected'
          } else if (parts[1] === 'call_ended') {
            parsedType = 'call'
            payloadValue = parts.slice(2).join(':')
          } else {
            parsedType = 'action'
            subType = parts[1]
            payloadValue = parts.slice(2).join(':')
          }

          const s = chatSettings._getOrCreateSettings(logicalChatId)
          if (subType === 'CHANGE_E') {
            s.emoji = payloadValue
          } else if (subType === 'CHANGE_THEME') {
            const idx = themes.value.findIndex(t => t.title === payloadValue)
            s.themeId = idx >= 0 ? idx : 0
          } else if (subType === 'CHANGE_NICKNAME') {
            const chat = chats.value.find(c => String(c.id) === String(logicalChatId))
            if (chat && chat.type === 'private') {
              chat.otherUserNickname = payloadValue
            }
          }
        }

        const newMsg: ChatMessage = {
          id: msgId,
          chatId: String(logicalChatId),
          sender: senderId === currentUserUuid.value ? 'me' : 'them',
          content: text,
          time: Date.now(),
          type: parsedType as any,
          reactions: payload.reactionsJson ? JSON.parse(payload.reactionsJson) : {},
          audioUrl: payload.audioUrl,
          imageUrl: payload.imageUrl,
          duration: payload.duration,
          fileUrl: payload.fileUrl,
          fileName: payload.fileName,
          fileSize: payload.fileSize,
          linkUrl: payload.linkUrl,
          url: payload.linkUrl,
          replyToId: payload.replyToId,
          replyToSenderId: payload.replyToSenderId,
          replyToContentSnippet: payload.replyToText,
        }

        chatStorage.storeMessage(newMsg).catch((err) => console.error('Failed to save MQTT message to IndexedDB:', err))

        if (String(logicalChatId) === String(activeChatId.value)) {
          messages.value.push(newMsg)
        }

        if (!lastReadMaps.value[logicalChatId]) {
          lastReadMaps.value[logicalChatId] = {}
        }
        lastReadMaps.value[logicalChatId][`user_${senderId}`] = msgId

        const chat = chats.value.find((c) => String(c.id) === String(logicalChatId))
        if (chat) {
          chat.lastMessage = newMsg.sender === 'me' ? `Ty: ${newMsg.content}` : `${newMsg.content}`

          if (newMsg.sender !== 'me') {
            const muted = chatSettings.isChatMuted(logicalChatId)
            if (muted) {
              const chatStore = useChatStore()
              chatStore.removeMessageBox(logicalChatId)
            } else {
              if (String(activeChatId.value) !== String(logicalChatId)) {
                chat.unread = true
              }

              const isInChatView = typeof window !== 'undefined' && window.location.pathname.startsWith('/chat')
              if (!isInChatView) {
                const chatStore = useChatStore()
                chatStore.addMessageBox(logicalChatId)

                try {
                  const notify = useNotify()
                  notify.notification({
                    title: chat.name || 'Nowa wiadomość',
                    header: newMsg.content,
                    avatar: chat.avatarUrl || `${import.meta.env.VITE_API_URL || 'http://localhost:8080'}/api/users/avatar/default-avatar.svg`,
                  })
                } catch (nErr) {
                  console.error('Failed to dispatch toast notification:', nErr)
                }
              }
            }
          }

          if (newMsg.sender !== 'me' && String(activeChatId.value) === String(logicalChatId)) {
            const apiUrl = import.meta.env.VITE_API_URL || 'http://localhost:8080'
            $fetch(`${apiUrl}/api/chat/read`, {
              method: 'POST',
              query: {
                userId: currentUserUuid.value,
                conversationId: convId,
              },
            }).catch(err => console.error(err))

            if (!lastReadMaps.value[logicalChatId]) {
              lastReadMaps.value[logicalChatId] = {}
            }
            lastReadMaps.value[logicalChatId][`user_${currentUserUuid.value}`] = msgId

            chatMqtt.publishMqtt('chat/messages/inbound', {
              type: 'read',
              conversationId: convId,
              senderId: currentUserUuid.value,
              lastReadMessageId: msgId,
            })
          }
        } else {
          getOrFetchUser(String(logicalChatId)).then((recipient) => {
            const muted = chatSettings.isChatMuted(logicalChatId)
            const newChatName = recipient ? recipient.name : `Konwersacja ${String(logicalChatId).substring(0, 8)}`
            const newChatAvatar = recipient ? recipient.avatar : `${import.meta.env.VITE_API_URL || 'http://localhost:8080'}/api/users/avatar/default-avatar.svg`

            chats.value.push({
              id: logicalChatId as any,
              name: newChatName,
              avatarUrl: newChatAvatar,
              lastMessage: newMsg.sender === 'me' ? `Ty: ${newMsg.content}` : `${newMsg.content}`,
              timeAgo: 'Teraz',
              unread: newMsg.sender !== 'me' && String(activeChatId.value) !== String(logicalChatId) && !muted,
              isActive: true,
              type: 'private' as any,
            })

            if (newMsg.sender !== 'me') {
              if (muted) {
                const chatStore = useChatStore()
                chatStore.removeMessageBox(logicalChatId)
              } else {
                const isInChatView = typeof window !== 'undefined' && window.location.pathname.startsWith('/chat')
                if (!isInChatView) {
                  const chatStore = useChatStore()
                  chatStore.addMessageBox(logicalChatId)

                  try {
                    const notify = useNotify()
                    notify.notification({
                      title: newChatName,
                      header: newMsg.content,
                      avatar: newChatAvatar,
                    })
                  } catch (nErr) {
                    console.error('Failed to dispatch toast notification for new chat:', nErr)
                  }
                }
              }
            }
          }).catch(() => {})
        }
      })
    }

    watch(
      () => authStore.currentUserId,
      (newVal) => {
        if (newVal) {
          messages.value = []
          chats.value = []
          fetchInbox(String(newVal))
          connectMqtt()
        }
      },
      { immediate: true },
    )
  }

  function getMessagesByChatId(chatId: string | number) {
    console.log(`Store [getMessagesByChatId] called. Filter target: ${chatId}, Total messages in store: ${messages.value.length}`)
    const filtered = messages.value.filter((m) => String(m.chatId) === String(chatId))
    console.log(`Store [getMessagesByChatId] Filtered count: ${filtered.length}`)
    return filtered
  }

  function recomputeLastMessage(chatId: string | number) {
    const chat = chats.value.find((c) => String(c.id) === String(chatId))
    if (!chat) return
    const chatMsgs = getMessagesByChatId(chatId)
    if (chatMsgs.length === 0) {
      chat.lastMessage = ''
      chat.timeAgo = ''
      return
    }
    const last = chatMsgs[chatMsgs.length - 1]
    const textVal = last.type === 'audio'
      ? 'Nagranie głosowe'
      : (last.type === 'image'
        ? 'Zdjęcie'
        : (last.type === 'file'
          ? `Plik: ${last.fileName}`
          : (last.type === 'link'
            ? `Link: ${last.linkUrl || last.content}`
            : last.content)))
    chat.lastMessage = last.sender === 'me' ? `Ty: ${textVal}` : `${textVal}`
    chat.timeAgo = last.time ? new Date(last.time).toLocaleTimeString() : ''
  }

  function updateChatName(chatId: string | number, newName: string) {
    const chat = chats.value.find((c) => String(c.id) === String(chatId))
    if (chat) {
      chat.name = newName
    }
  }

  function getAuthHeaders(): Record<string, string> {
    const token = typeof window !== 'undefined' ? localStorage.getItem('keycloak-token') : null
    const headers: Record<string, string> = {}
    if (token) {
      headers['Authorization'] = `Bearer ${token}`
    }
    return headers
  }

  function setChatEmoji(chatId: string | number, emoji: string) {
    const s = chatSettings._getOrCreateSettings(chatId)
    s.emoji = emoji

    const conversationId = getSymmetricConversationId(chatId)
    const headers = getAuthHeaders()
    const apiUrl = import.meta.env.VITE_API_URL || 'http://localhost:8080'
    chatSettings.saveCustomization(apiUrl, headers, conversationId, emoji, undefined)
  }

  function setChatThemeById(chatId: string | number, themeId: string) {
    const idx = themes.value.findIndex((t) => t.id === themeId)
    const s = chatSettings._getOrCreateSettings(chatId)
    const finalIdx = idx >= 0 ? idx : 0
    s.themeId = finalIdx

    const conversationId = getSymmetricConversationId(chatId)
    const headers = getAuthHeaders()
    const apiUrl = import.meta.env.VITE_API_URL || 'http://localhost:8080'
    chatSettings.saveCustomization(apiUrl, headers, conversationId, undefined, finalIdx)
  }

  function updatePrivateChatNickname(chatId: string | number, nickname: string) {
    const chat = chats.value.find((c) => String(c.id) === String(chatId))
    if (chat) {
      chat.otherUserNickname = nickname

      const conversationId = getSymmetricConversationId(chatId)
      const headers = getAuthHeaders()
      const apiUrl = import.meta.env.VITE_API_URL || 'http://localhost:8080'
      chatSettings.saveNickname(apiUrl, headers, conversationId, String(chatId), nickname)
    }
  }

  function updateGroupMembersNicknames(chatId: string | number, nicknames: Record<string, string>) {
    const chat = chats.value.find((c) => String(c.id) === String(chatId))
    if (chat && chat.type === 'group' && chat.groupMembers) {
      chat.groupMembers = chat.groupMembers.map(m => {
        const custom = nicknames[String(m.id)]
        return {
          ...m,
          nickname: custom !== undefined ? custom : (m.nickname || '')
        }
      })

      const conversationId = getSymmetricConversationId(chatId)
      const headers = getAuthHeaders()
      const apiUrl = import.meta.env.VITE_API_URL || 'http://localhost:8080'
      chatSettings.saveGroupNicknames(apiUrl, headers, conversationId, nicknames)
    }
  }

  async function fetchChatSettings(chatId: string | number) {
    if (!import.meta.client) return
    const conversationId = getSymmetricConversationId(chatId)
    const apiUrl = import.meta.env.VITE_API_URL || 'http://localhost:8080'
    try {
      const data = await $fetch<any>(`${apiUrl}/api/chat/settings`, {
        query: { conversationId }
      })
      if (data) {
        const s = chatSettings._getOrCreateSettings(chatId)
        if (data.emoji) {
          s.emoji = data.emoji
        }
        if (data.themeId !== null && data.themeId !== undefined) {
          s.themeId = data.themeId
        }

        if (data.nicknames) {
          const chat = chats.value.find((c) => String(c.id) === String(chatId))
          if (chat) {
            if (chat.type === 'group' && chat.groupMembers) {
              chat.groupMembers = chat.groupMembers.map(m => {
                const customNick = data.nicknames[String(m.id)]
                return {
                  ...m,
                  nickname: customNick !== undefined ? customNick : (m.nickname || '')
                }
              })
            } else {
              const customNick = data.nicknames[String(chatId)]
              if (customNick !== undefined) {
                chat.otherUserNickname = customNick
              }
            }
          }
        }
      }
    } catch (err) {
      console.error('Failed to load chat settings from ScyllaDB:', err)
    }
  }

  async function togglePinMessage(chatId: string | number, messageId: string | number) {
    const message = messages.value.find((m) => m.id === messageId)
    if (!message) return

    const newPinnedStatus = !message.isPinned
    const conversationId = getSymmetricConversationId(chatId)
    const apiUrl = import.meta.env.VITE_API_URL || 'http://localhost:8080'

    try {
      await $fetch(`${apiUrl}/api/chat/messages/pin`, {
        method: 'POST',
        query: {
          conversationId,
          messageId: String(messageId),
          isPinned: newPinnedStatus,
        },
      })
    } catch (err) {
      console.error('Failed to toggle pin state on backend:', err)
      return
    }

    message.isPinned = newPinnedStatus

    try {
      await chatStorage.storeMessage(message)
    } catch (dbErr) {
      console.error('Failed to update pinned state in IndexedDB:', dbErr)
    }
  }

  const unreadCount = computed(() => {
    return chats.value.filter((c) => c.unread).length
  })

  function publishMqtt(topic: string, payload: any, options?: any) {
    return chatMqtt.publishMqtt(topic, payload, options)
  }

  async function clearState() {
    chats.value = []
    messages.value = []
    activeChatId.value = null
    lastReadMaps.value = {}
    
    chatCalls.endCall()
    chatMqtt.disconnectMqtt()

    if (typeof window !== 'undefined' && window.indexedDB) {
      try {
        await chatStorage.clearOfflineStorage()
        window.indexedDB.deleteDatabase('facebook_clone_chat_db')
        window.indexedDB.deleteDatabase('ChatDB')
        console.log('IndexedDB databases deleted successfully.')
      } catch (err) {
        console.error('Failed to delete IndexedDB databases:', err)
      }
    }
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
    updatePrivateChatNickname,
    updateGroupMembersNicknames,
    fetchChatSettings,
    setSelectedTheme: themeStore.setSelectedTheme,
    setSelectedEmoji: themeStore.setSelectedEmoji,
    addReaction,
    togglePinMessage,
    muteChat: chatSettings.muteChat,
    isChatMuted: chatSettings.isChatMuted,
    fetchInbox,
    fetchMessages,
    unreadCount,
    currentUserUuid,
    activeChatId,
    lastReadMaps,
    incomingCall,
    publishMqtt,
    isMqttConnected,
    getSymmetricConversationId,
    typingUsers,
    clearState,
  }
})

export default useConversationsStore
