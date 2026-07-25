import { useApolloClient } from '@vue/apollo-composable'
import { gql } from 'graphql-tag'
import { useChatStorage } from '@/composables/chat/useChatStorage'
import { useUserCache } from '@/composables/shared/useUserCache'
import { useChatMqtt } from '@/composables/chat/useChatMqtt'
import { getSymmetricUuid } from '@/utils/uuid'
import { type ChatMessage } from '@/types/Message'

const GET_CHAT_WITH_USER_QUERY = gql`
  query GetChatWithUser($userId: ID!, $conversationId: ID!) {
    getChatWithUser(userId: $userId, conversationId: $conversationId) {
      user {
        id
        firstName
        lastName
        avatarId
      }
      messages {
        messageId
        senderId
        text
        time
        reactionsJson
        replyToId
        replyToText
        replyToSenderId
        imageUrl
        audioUrl
        duration
        fileUrl
        fileName
        fileSize
        linkUrl
      }
    }
  }
`

export function useChatApi(currentUserUuid: { value: string }, chats: any, messages: any, lastReadMaps: any) {
  const chatStorage = useChatStorage()
  const userCache = useUserCache()
  const chatMqtt = useChatMqtt()
  const apolloClient = useApolloClient().resolveClient()

  function getSymmetricConversationId(chatId: string | number): string {
    const isGroup = chats.value.some((c: any) => String(c.id) === String(chatId) && c.type === 'group')
    if (isGroup) {
      return String(chatId)
    }
    return getSymmetricUuid(String(currentUserUuid.value), String(chatId))
  }

  async function fetchInbox(userId: string) {
    if (!import.meta.client) return
    try {
      const apiUrl = import.meta.env.VITE_API_URL || 'http://localhost:8080'
      const data = await $fetch<any[]>(`${apiUrl}/api/chat/inbox?userId=${userId}`)
      if (data && Array.isArray(data)) {
        const mappedChats = await Promise.all(data.map(async (item) => {
          const targetId = item.recipientId || item.conversationId
          const recipient = await userCache.getOrFetchUser(String(targetId))
          return {
            id: targetId as any,
            name: recipient ? recipient.name : `Konwersacja ${String(targetId).substring(0, 8)}`,
            avatarUrl: recipient?.avatar || `${apiUrl}/api/users/avatar/default-avatar.svg`,
            lastMessage: item.lastMessageText || '',
            timeAgo: item.lastMessageTime ? new Date(item.lastMessageTime).toLocaleTimeString() : '',
            unread: false,
            isActive: false,
            type: (item.recipientId ? 'private' : 'group') as any,
          }
        }))
        chats.value = mappedChats
      }
    } catch (err) {
      console.error('[fetchInbox] Failed to fetch inbox list:', err)
    }
  }

  async function fetchMessages(chatId: string, activeChatId: any) {
    if (!import.meta.client) return
    activeChatId.value = chatId

    userCache.getOrFetchUser(chatId).then((fetchedUser) => {
      if (fetchedUser) {
        const chat = chats.value.find((c: any) => String(c.id) === String(chatId))
        if (chat) {
          chat.name = fetchedUser.name
          if (fetchedUser.avatar) chat.avatarUrl = fetchedUser.avatar
        }
      }
    }).catch(() => {})

    const conversationId = getSymmetricConversationId(chatId)
    console.log('[useChatApi fetchMessages] Loading history for chatId:', chatId, 'conversationId:', conversationId)

    try {
      const localMsgs = await chatStorage.loadLocalMessages(String(chatId))
      if (activeChatId.value === chatId) {
        messages.value = localMsgs
      }
    } catch (dbErr) {
      console.error('Failed to load messages from IndexedDB:', dbErr)
    }

    const chat = chats.value.find((c: any) => String(c.id) === String(chatId))
    if (chat) {
      chat.unread = false
    }

    const apiUrl = import.meta.env.VITE_API_URL || 'http://localhost:8080'

    $fetch(`${apiUrl}/api/chat/read`, {
      method: 'POST',
      query: {
        userId: currentUserUuid.value,
        conversationId: conversationId,
      },
    }).catch((err) => {
      console.error('[fetchMessages] Failed to mark chat as read on backend:', err)
    })

    $fetch<Record<string, string>>(`${apiUrl}/api/chat/receipts`, {
      query: { conversationId },
    }).then((receipts) => {
      const formattedReceipts: Record<string, string> = {}
      for (const [uId, mId] of Object.entries(receipts || {})) {
        formattedReceipts[`user_${uId}`] = mId
      }
      lastReadMaps.value[chatId] = formattedReceipts
    }).catch((err) => {
      console.error('[fetchMessages] Failed to fetch read receipts:', err)
    })

    try {
      const { data } = await apolloClient.query({
        query: GET_CHAT_WITH_USER_QUERY,
        variables: {
          userId: chatId,
          conversationId: conversationId,
        },
        fetchPolicy: 'network-only',
      })

      console.log('[useChatApi fetchMessages] GraphQL query finished. Data received:', data)
      const chatData = data?.getChatWithUser
      if (chatData) {
        const recipientUser = chatData.user
        if (recipientUser) {
          const chat = chats.value.find((c: any) => String(c.id) === String(chatId))
          const fullName = [recipientUser.firstName, recipientUser.lastName].filter(Boolean).join(' ') || 'Użytkownik'
          const avatarUrl = recipientUser.avatarId
            ? `${apiUrl}/api/users/avatar/${recipientUser.avatarId}`
            : `${apiUrl}/api/users/avatar/default-avatar.svg`

          if (chat) {
            chat.name = fullName
            chat.avatarUrl = avatarUrl
          } else {
            chats.value.push({
              id: chatId as any,
              name: fullName,
              avatarUrl: avatarUrl,
              lastMessage: '',
              timeAgo: 'Teraz',
              unread: false,
              isActive: true,
              type: 'private' as any,
            })
          }
        }

        if (chatData.messages && Array.isArray(chatData.messages)) {
          const syncedMsgs: ChatMessage[] = []

          chatData.messages.forEach((item: any) => {
            let parsedReactions = {}
            try {
              if (item.reactionsJson) {
                parsedReactions = JSON.parse(item.reactionsJson)
              }
            } catch (e) {
              console.error('Failed to parse reactionsJson:', e)
            }

            let parsedType = item.audioUrl ? 'audio' : (item.imageUrl ? 'image' : (item.fileUrl ? 'file' : (item.linkUrl ? 'link' : 'text')))
            let subType: any = undefined
            let payloadValue: any = undefined

            if (item.text && item.text.startsWith('SYSTEM_ACTION:')) {
              const parts = item.text.split(':')
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
            }

            const pushedMsg: ChatMessage = {
              id: item.messageId,
              chatId: String(chatId),
              sender: item.senderId === currentUserUuid.value ? 'me' : 'them',
              content: item.text,
              time: item.time,
              type: parsedType as any,
              subType: subType,
              payload: payloadValue,
              imageUrl: item.imageUrl || undefined,
              audioUrl: item.audioUrl || undefined,
              duration: item.duration || (parsedType === 'call' ? Number(payloadValue) : undefined),
              fileUrl: item.fileUrl || undefined,
              fileName: item.fileName || undefined,
              fileSize: item.fileSize || undefined,
              linkUrl: item.linkUrl || undefined,
              url: item.linkUrl || undefined,
              reactions: parsedReactions,
              isReply: !!item.replyToId,
              replyToId: item.replyToId,
              replyToText: item.replyToText,
              replyToSenderId: item.replyToSenderId,
              replyToSender: item.replyToId
                ? (item.replyToSenderId === currentUserUuid.value ? 'Ty' : (chats.value.find((c: any) => String(c.id) === String(chatId))?.name || 'Użytkownik'))
                : undefined,
              replyToContentSnippet: item.replyToText,
              isPinned: !!item.isPinned,
            }
            syncedMsgs.push(pushedMsg)
          })

          try {
            await chatStorage.storeMessages(syncedMsgs)
          } catch (dbErr) {
            console.error('Failed to persist history to IndexedDB:', dbErr)
          }

          console.log('[useChatApi fetchMessages] Checking activeChatId matching:', { activeChatIdValue: activeChatId.value, chatId })
          if (String(activeChatId.value) === String(chatId)) {
            const updatedLocalMsgs = await chatStorage.loadLocalMessages(String(chatId))
            console.log('[useChatApi fetchMessages] Loading updated local messages count:', updatedLocalMsgs.length)
            messages.value = updatedLocalMsgs
          } else {
            console.warn('[useChatApi fetchMessages] activeChatId did NOT match chatId when GraphQL query finished! activeChatId:', activeChatId.value, 'chatId:', chatId)
          }

          if (chatData.messages.length > 0) {
            const lastMsg = chatData.messages[chatData.messages.length - 1]
            const lastMsgId = lastMsg.messageId

            if (!lastReadMaps.value[chatId]) {
              lastReadMaps.value[chatId] = {}
            }
            lastReadMaps.value[chatId][`user_${currentUserUuid.value}`] = lastMsgId

            chatMqtt.publishMqtt('chat/messages/inbound', {
              type: 'read',
              conversationId: conversationId,
              senderId: currentUserUuid.value,
              lastReadMessageId: lastMsgId,
            })
          }
        }
      }
    } catch (err) {
      console.error('[fetchMessages] Failed to fetch chat with user via GraphQL:', err)
    }
  }

  function addMessage(chatId: string | number, msg: Partial<ChatMessage>) {
    const recipientUuid = String(chatId)
    const participantIds = [currentUserUuid.value, recipientUuid]
    const conversationId = getSymmetricConversationId(chatId)

    const token = typeof window !== 'undefined' ? localStorage.getItem('keycloak-token') : null
    const headers: Record<string, string> = {}
    if (token) {
      headers['Authorization'] = `Bearer ${token}`
    }

    const formData = new URLSearchParams()
    formData.append('conversationId', conversationId)
    if (msg.content) formData.append('text', msg.content)
    if (msg.replyToId) formData.append('replyToId', msg.replyToId)
    if (msg.replyToContentSnippet) formData.append('replyToText', msg.replyToContentSnippet)
    if (msg.replyToSenderId) formData.append('replyToSenderId', msg.replyToSenderId)
    if (msg.imageUrl) formData.append('imageUrl', msg.imageUrl)
    if (msg.audioUrl) formData.append('audioUrl', msg.audioUrl)
    if (msg.duration) formData.append('duration', String(msg.duration))
    if (msg.fileUrl) formData.append('fileUrl', msg.fileUrl)
    if (msg.fileName) formData.append('fileName', msg.fileName)
    if (msg.fileSize) formData.append('fileSize', String(msg.fileSize))
    if (msg.linkUrl) formData.append('linkUrl', msg.linkUrl)
    participantIds.forEach(id => formData.append('participantIds', id))

    const apiUrl = import.meta.env.VITE_API_URL || 'http://localhost:8080'

    $fetch(`${apiUrl}/api/chat/messages/send`, {
      method: 'POST',
      headers,
      body: formData,
    }).then(() => {
      console.log('Frontend REST: Message sent successfully')
    }).catch((err) => {
      console.error('Failed to send message via REST:', err)
    })
  }

  function addReaction(chatId: string | number, messageId: string, emoji: string) {
    const message = messages.value.find(
      (m: any) => String(m.chatId) === String(chatId) && m.id === messageId,
    )
    if (message) {
      if (!message.reactions) {
        message.reactions = {}
      }
      const list = message.reactions[emoji] || []
      const uId = `user_${currentUserUuid.value}`
      if (list.includes(uId)) {
        message.reactions[emoji] = list.filter((id: any) => id !== uId)
      } else {
        message.reactions[emoji] = [...list, uId]
      }
      if (message.reactions[emoji].length === 0) {
        delete message.reactions[emoji]
      }

      chatStorage.storeMessage(message).catch((err) => {
        console.error('Failed to update reaction in IndexedDB locally:', err)
      })

      const conversationId = getSymmetricConversationId(chatId)
      const token = typeof window !== 'undefined' ? localStorage.getItem('keycloak-token') : null
      const headers: Record<string, string> = {}
      if (token) {
        headers['Authorization'] = `Bearer ${token}`
      }

      const apiUrl = import.meta.env.VITE_API_URL || 'http://localhost:8080'
      $fetch(`${apiUrl}/api/chat/messages/react`, {
        method: 'POST',
        headers,
        query: {
          conversationId,
          messageId,
          reactionEmoji: emoji,
        },
      }).catch((err) => {
        console.error('Failed to send reaction via REST:', err)
      })

      const mqttClientId = chatMqtt.mqttClientId
      const recipientUuid = String(chatId)
      const participantIds = [currentUserUuid.value, recipientUuid]
      chatMqtt.publishMqtt('chat/messages/inbound', {
        type: 'reaction',
        conversationId: conversationId,
        senderId: currentUserUuid.value,
        targetMessageId: messageId,
        reactionEmoji: emoji,
        participantIds: participantIds,
        senderClientId: mqttClientId.value,
      })
    }
  }

  return {
    getSymmetricConversationId,
    fetchInbox,
    fetchMessages,
    addMessage,
    addReaction,
  }
}
