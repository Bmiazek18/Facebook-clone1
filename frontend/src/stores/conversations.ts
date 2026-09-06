import { ref, watch, computed } from 'vue'
import { defineStore, storeToRefs } from 'pinia'
import { type Chat } from '@/types/Chat'
const rawChats: Chat[] = []
import { type ChatMessage } from '@/types/Message'
import { useChatThemeStore } from '@/stores/chatTheme'
import { useAuthStore } from '@/stores/auth'
import { useNotify } from '@/composables/shared/useNotify'
import { useChatMqtt } from '@/composables/chat/useChatMqtt'
import { useChatStorage } from '@/composables/chat/useChatStorage'
import { useUserCache } from '@/composables/shared/useUserCache'
import { useChatCalls } from '@/composables/chat/useChatCalls'
import { useChatSettings } from '@/composables/chat/useChatSettings'
import { useChatApi } from '@/composables/chat/useChatApi'
import { useChatMqttDispatcher } from '@/composables/chat/useChatMqttDispatcher'
import { useChatPolls } from '@/composables/chat/useChatPolls'
import { encryptMessage } from '@/utils/e2ee'
import { signalStore } from '@/utils/e2ee/signalStore.client'
import { getSymmetricUuid } from '@/utils/uuid'
import { formatSystemActionText } from '@/utils/contentProcessor'

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
  const notify = useNotify()

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
  const fetchMessages = async (chatId: string) => {
    const settingsPromise = fetchChatSettings(chatId)
    await chatApi.fetchMessages(chatId, activeChatId)
    await settingsPromise
  }
  const fetchMessagesForBox = async (chatId: string) => {
    const settingsPromise = fetchChatSettings(chatId)
    await chatApi.fetchMessages(chatId, activeChatId, false)
    await settingsPromise
  }
  const addMessage = chatApi.addMessage
  const addReaction = chatApi.addReaction
  const getSymmetricConversationId = chatApi.getSymmetricConversationId

  function publishMqtt(topic: string, payload: any, options?: any) {
    return chatMqtt.publishMqtt(topic, payload, options)
  }

  // MQTT Dispatcher Composable
  const { handleIncomingMqttPayload } = useChatMqttDispatcher({
    currentUserUuid,
    chats,
    messages,
    lastReadMaps,
    typingUsers,
    typingTimeouts,
    activeChatId,
    mqttClientId,
    themes,
    chatStorage,
    chatCalls,
    chatSettings,
    themeStore,
    notify,
    getOrFetchUser,
    chatApi,
    chatMqtt,
    getSymmetricConversationId,
    fetchChatSettings,
  })

  // Polls Composable
  const { voteChatPoll, addChatPollOption } = useChatPolls(
    messages,
    chats,
    currentUserUuid,
    chatStorage,
    getSymmetricConversationId,
    publishMqtt,
  )

  if (import.meta.client) {
    import('@/utils/e2ee').then(({ initIdentityKeys }) => {
      initIdentityKeys().catch((err) => console.error('Failed to initialize E2EE keys:', err))
    })

    const connectMqtt = () => {
      chatMqtt.connectMqtt(currentUserUuid.value, handleIncomingMqttPayload)
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
    const cleanTargetId = String(chatId).replace('user_', '')
    return messages.value.filter(
      (m) => String(m.chatId).replace('user_', '') === cleanTargetId,
    )
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
    let textVal = last.content
    if (last.type === 'audio') {
      textVal = 'Nagranie głosowe'
    } else if (last.type === 'image') {
      textVal = 'Zdjęcie'
    } else if (last.type === 'file') {
      textVal = `Plik: ${last.fileName}`
    } else if (last.type === 'link' || last.type === 'feed-link' || last.type === 'post_link') {
      textVal = `Link: ${(last as any).linkUrl || (last as any).url || last.content}`
    } else if (last.type === 'action' || textVal.startsWith('SYSTEM_ACTION:')) {
      textVal = formatSystemActionText(textVal)
    }

    if (last.type === 'action' || last.content.startsWith('SYSTEM_ACTION:')) {
      chat.lastMessage = textVal
    } else {
      chat.lastMessage = last.sender === 'me' ? `Ty: ${textVal}` : `${textVal}`
    }
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
    const myId =
      typeof window !== 'undefined'
        ? localStorage.getItem('auth-current-user-id') || localStorage.getItem('user-uuid') || ''
        : ''
    if (myId) {
      headers['X-User-Id'] = String(myId).replace('user_', '')
    }
    return headers
  }

  function setChatEmoji(chatId: string | number, emoji: string) {
    const s = chatSettings._getOrCreateSettings(chatId)
    s.emoji = emoji
    themeStore.setSelectedEmoji(emoji)

    const conversationId = getSymmetricConversationId(chatId)
    const headers = getAuthHeaders()
    const apiUrl = import.meta.env.VITE_API_URL || 'http://localhost:8080'
    const chat = chats.value.find((c) => String(c.id) === String(chatId))
    const participantIds =
      chat && chat.type === 'group' && chat.groupMembers
        ? chat.groupMembers.map((m: any) => String(m.id))
        : [String(currentUserUuid.value), String(chatId).replace('user_', '')]

    chatSettings.saveCustomization(
      apiUrl,
      headers,
      conversationId,
      emoji,
      undefined,
      participantIds,
    )

    for (const pId of participantIds) {
      chatMqtt.publishMqtt(`chat/messages/user/${pId}`, {
        type: 'chat_setting_changed',
        conversationId,
        emoji,
        senderId: currentUserUuid.value,
        participantIds,
      })
    }
  }

  function setChatThemeById(chatId: string | number, themeId: string) {
    const idx = themes.value.findIndex((t) => t.id === themeId)
    const s = chatSettings._getOrCreateSettings(chatId)
    const finalIdx = idx >= 0 ? idx : 0
    s.themeId = finalIdx
    if (idx >= 0) {
      themeStore.setSelectedTheme(themeId)
    }

    const conversationId = getSymmetricConversationId(chatId)
    const headers = getAuthHeaders()
    const apiUrl = import.meta.env.VITE_API_URL || 'http://localhost:8080'
    const chat = chats.value.find((c) => String(c.id) === String(chatId))
    const participantIds =
      chat && chat.type === 'group' && chat.groupMembers
        ? chat.groupMembers.map((m: any) => String(m.id))
        : [String(currentUserUuid.value), String(chatId).replace('user_', '')]

    chatSettings.saveCustomization(
      apiUrl,
      headers,
      conversationId,
      undefined,
      finalIdx,
      participantIds,
    )

    for (const pId of participantIds) {
      chatMqtt.publishMqtt(`chat/messages/user/${pId}`, {
        type: 'chat_setting_changed',
        conversationId,
        themeId: finalIdx,
        themeKey: themeId,
        senderId: currentUserUuid.value,
        participantIds,
      })
    }
  }

  function updatePrivateChatNickname(chatId: string | number, nickname: string) {
    const chat = chats.value.find((c) => String(c.id) === String(chatId))
    if (chat) {
      chat.otherUserNickname = nickname

      const conversationId = getSymmetricConversationId(chatId)
      const headers = getAuthHeaders()
      const apiUrl = import.meta.env.VITE_API_URL || 'http://localhost:8080'
      const participantIds = [String(currentUserUuid.value), String(chatId).replace('user_', '')]
      chatSettings.saveNickname(
        apiUrl,
        headers,
        conversationId,
        String(chatId),
        nickname,
        participantIds,
      )
    }
  }

  function updateGroupMembersNicknames(
    chatId: string | number,
    nicknames: Record<string, string>,
  ) {
    const chat = chats.value.find((c) => String(c.id) === String(chatId))
    if (chat && chat.type === 'group' && chat.groupMembers) {
      chat.groupMembers = chat.groupMembers.map((m) => {
        const custom = nicknames[String(m.id)]
        return {
          ...m,
          nickname: custom !== undefined ? custom : m.nickname || '',
        }
      })

      const conversationId = getSymmetricConversationId(chatId)
      const headers = getAuthHeaders()
      const apiUrl = import.meta.env.VITE_API_URL || 'http://localhost:8080'
      const participantIds = chat.groupMembers.map((m: any) => String(m.id))
      chatSettings.saveGroupNicknames(apiUrl, headers, conversationId, nicknames, participantIds)
    }
  }

  async function fetchChatSettings(chatId: string | number) {
    if (!import.meta.client) return
    const conversationId = getSymmetricConversationId(chatId)
    try {
      const data = await chatApi.getChatSettings(conversationId)
      if (data) {
        const s = chatSettings._getOrCreateSettings(chatId)
        if (data.emoji) {
          s.emoji = data.emoji
          themeStore.setSelectedEmoji(data.emoji)
        }
        if (data.themeId !== null && data.themeId !== undefined) {
          s.themeId = data.themeId
          const theme =
            typeof data.themeId === 'number'
              ? themes.value[data.themeId]
              : themes.value.find((t: any) => t.id === data.themeId)
          if (theme) {
            themeStore.setSelectedTheme(theme.id)
          }
        }

        const chat = chats.value.find((c) => String(c.id) === String(chatId))
        if (chat) {
          if (data.isGroup !== undefined) {
            chat.type = data.isGroup ? 'group' : 'private'
          }
          if (data.participants) {
            const membersList = data.participants.map((p: any) => ({
              id: p.id,
              name: [p.firstName, p.lastName].filter(Boolean).join(' ') || 'Użytkownik',
              avatarUrl: p.avatar || '/default-avatar.png',
            }))
            chat.groupMembers = membersList
            if (chat.type === 'group') {
              const otherMembers = membersList.filter(
                (m: any) => String(m.id) !== String(currentUserUuid.value),
              )
              if (otherMembers.length > 0) {
                chat.name = otherMembers.map((m: any) => m.name).join(', ')
              }
            }
          }
        }

        if (data.nicknames) {
          const nicknameRecord: Record<string, string> = {}
          data.nicknames.forEach((item: any) => {
            nicknameRecord[String(item.userId)] = item.nickname
          })

          if (chat) {
            if (chat.type === 'group' && chat.groupMembers) {
              chat.groupMembers = chat.groupMembers.map((m) => {
                const customNick = nicknameRecord[String(m.id)]
                return {
                  ...m,
                  nickname: customNick !== undefined ? customNick : m.nickname || '',
                }
              })
            } else {
              const customNick = nicknameRecord[String(chatId)]
              if (customNick !== undefined) {
                chat.otherUserNickname = customNick
              }
            }
          }
        }
      }
    } catch (err) {
      console.error('Failed to load chat settings via GraphQL:', err)
    }
  }

  async function togglePinMessage(chatId: string | number, messageId: string | number) {
    const message = messages.value.find((m) => m.id === messageId)
    if (!message) return

    const newPinnedStatus = !message.isPinned
    const conversationId = getSymmetricConversationId(chatId)
    const cleanCurrentUserUuid = String(authStore.currentUserId || '1').replace('user_', '')
    const cleanChatId = String(chatId).replace('user_', '')

    try {
      await chatApi.pinMessage({
        conversationId,
        messageId: String(messageId),
        isPinned: newPinnedStatus,
        participantIds: [cleanCurrentUserUuid, cleanChatId],
      })
    } catch (err) {
      console.error('Failed to toggle pin state on backend via GraphQL:', err)
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

  async function clearState() {
    chats.value = []
    messages.value = []
    activeChatId.value = null
    lastReadMaps.value = {}
    settings.value = []

    chatCalls.endCall()
    chatMqtt.disconnectMqtt()

    if (typeof window !== 'undefined') {
      localStorage.removeItem('my_ik_public_b64')
      if (window.indexedDB) {
        try {
          await chatStorage.clearOfflineStorage()
          window.indexedDB.deleteDatabase('facebook_clone_chat_db')
          window.indexedDB.deleteDatabase('ChatDB')
          window.indexedDB.deleteDatabase('facebook_clone_keys_db')
          console.log('IndexedDB databases deleted successfully.')
        } catch (err) {
          console.error('Failed to delete IndexedDB databases:', err)
        }
      }
    }
  }

  async function addGroupMember(chatId: string | number, selectedUser: any) {
    const chat = chats.value.find((c) => String(c.id) === String(chatId))
    if (!chat || chat.type !== 'group') return

    if (!chat.groupMembers) chat.groupMembers = []
    if (!chat.groupMembers.some((m: any) => String(m.id) === String(selectedUser.id))) {
      chat.groupMembers.push({
        id: selectedUser.id,
        name: selectedUser.name,
        avatar: selectedUser.avatarUrl || '/default-avatar.png',
        addedByUserId: String(currentUserUuid.value),
      })
    }

    const otherMembers = chat.groupMembers.filter(
      (m: any) => String(m.id) !== String(currentUserUuid.value),
    )
    if (otherMembers.length > 0) {
      chat.name = otherMembers.map((m: any) => m.name).join(', ')
    }

    const participantIds = chat.groupMembers.map((m: any) => String(m.id).replace('user_', ''))
    const cleanCurrentUserUuid = String(currentUserUuid.value).replace('user_', '')
    const cleanSelectedUserId = String(selectedUser.id).replace('user_', '')

    await chatApi.sendMessage({
      senderId: cleanCurrentUserUuid,
      conversationId: String(chatId),
      text: `SYSTEM_ACTION:ADD_MEMBER:${cleanSelectedUserId}`,
      participantIds: participantIds,
    })

    const myKey = await signalStore.getCustomValue<string>(
      `sender_key_${chatId}_${currentUserUuid.value}`,
    )
    if (myKey) {
      const encryptedBackup = await encryptMessage(
        `SYSTEM_ACTION:BACKUP_SENDER_KEY:${chatId}:${myKey}`,
        selectedUser.id,
        true,
      )
      await chatApi.sendMessage({
        senderId: cleanCurrentUserUuid,
        conversationId: getSymmetricUuid(cleanCurrentUserUuid, cleanSelectedUserId),
        text: encryptedBackup,
        participantIds: [cleanCurrentUserUuid, cleanSelectedUserId],
      })
      console.log(
        `[addGroupMember] Successfully sent encrypted backup sender_key to ${selectedUser.name}`,
      )
    }
  }

  async function leaveGroup(chatId: string | number) {
    const cleanCurrentUserUuid = String(currentUserUuid.value).replace('user_', '')
    const cleanChatId = String(chatId).replace('user_', '')
    try {
      await chatApi.leaveChat(cleanCurrentUserUuid, cleanChatId)

      chats.value = chats.value.filter((c) => String(c.id) !== String(chatId))
      if (String(activeChatId.value) === String(chatId)) {
        activeChatId.value = null
      }
      console.log(`[leaveGroup] Successfully left group ${chatId}`)
    } catch (err) {
      console.error('Failed to leave group:', err)
    }
  }

  const acceptIncomingCall = () => chatCalls.acceptCall()
  const rejectIncomingCall = () =>
    chatCalls.rejectCall(currentUserUuid.value, isMqttConnected.value, publishMqtt)

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
    fetchMessagesForBox,
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
    usersCache,
    getOrFetchUser,
    addGroupMember,
    leaveGroup,
    voteChatPoll,
    addChatPollOption,
    acceptIncomingCall,
    rejectIncomingCall,
  }
})

export default useConversationsStore
