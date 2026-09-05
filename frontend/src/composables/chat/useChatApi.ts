import { useChatStorage } from '@/composables/chat/useChatStorage'
import { useUserCache } from '@/composables/shared/useUserCache'
import { useChatMqtt } from '@/composables/chat/useChatMqtt'
import { getSymmetricUuid } from '@/utils/uuid'
import { type ChatMessage } from '@/types/Message'
import { chatApi } from '@/api/chat'
import { encryptMessage, decryptMessage } from '@/utils/e2ee'
import { formatSystemActionText } from '@/utils/contentProcessor'

export function useChatApi(currentUserUuid: { value: string }, chats: any, messages: any, lastReadMaps: any) {
  const chatStorage = useChatStorage()
  const userCache = useUserCache()
  const chatMqtt = useChatMqtt()
  // Same-origin BFF adds the access token from the HTTP-only session cookie.
  const apiUrl = import.meta.env.VITE_BFF_API_URL || ''

  function getSymmetricConversationId(chatId: string | number): string {
    const isGroup = chats.value.some((c: any) => String(c.id) === String(chatId) && c.type === 'group')
    if (isGroup) {
      return String(chatId)
    }
    const cleanUserId = String(currentUserUuid.value).replace('user_', '')
    const cleanChatId = String(chatId).replace('user_', '')
    return getSymmetricUuid(cleanUserId, cleanChatId)
  }

  function mergeMessagesForChat(chatId: string, incomingMessages: ChatMessage[]) {
    const cleanChatId = String(chatId).replace('user_', '')
    const messagesFromOtherChats = messages.value.filter(
      (message: ChatMessage) => String(message.chatId).replace('user_', '') !== cleanChatId,
    )
    messages.value = [...messagesFromOtherChats, ...incomingMessages]
  }

  async function fetchInbox(userId: string) {
    if (!import.meta.client) return
    const cleanUserId = String(userId).replace('user_', '')
    try {
      const data = await chatApi.getInbox(cleanUserId)
      if (data && Array.isArray(data)) {
        const mappedChats = await Promise.all(data.map(async (item) => {
          const recipientId = item.recipientId ? String(item.recipientId).trim() : ''
          const isPrivate = !!recipientId
          // Never treat conversationId as a user id — group chats have no recipientId.
          const chatId = isPrivate ? recipientId : String(item.conversationId)
          const recipient = isPrivate ? await userCache.getOrFetchUser(recipientId) : null
          let rawLastMsg = item.lastMessageText || ''

          if (rawLastMsg && rawLastMsg.startsWith('e2ee:')) {
            try {
              const senderUserId = item.lastMessageSenderId ? String(item.lastMessageSenderId).replace(/^user_/, '') : chatId
              rawLastMsg = await decryptMessage(rawLastMsg, senderUserId, isPrivate)
            } catch {
              rawLastMsg = 'Zaszyfrowana wiadomość'
            }
          }

          if (rawLastMsg && rawLastMsg.startsWith('SYSTEM_ACTION:')) {
            rawLastMsg = formatSystemActionText(rawLastMsg)
          }

          const cleanSenderId = String(item.lastMessageSenderId || '').replace('user_', '')
          const cleanCurrentId = String(currentUserUuid.value).replace('user_', '')
          if (rawLastMsg && cleanSenderId && cleanSenderId === cleanCurrentId) {
            rawLastMsg = `Ty: ${rawLastMsg}`
          }

          return {
            id: chatId as any,
            name: recipient
              ? recipient.name
              : (isPrivate ? 'Użytkownik' : `Grupa ${String(item.conversationId).substring(0, 8)}`),
            avatarUrl: recipient?.avatar || '/default-avatar.png',
            lastMessage: rawLastMsg,
            timeAgo: item.lastActivity ? new Date(item.lastActivity).toLocaleTimeString() : '',
            unread: item.isUnread,
            isActive: false,
            type: (isPrivate ? 'private' : 'group') as any,
          }
        }))
        chats.value = mappedChats
      }
    } catch (err) {
      console.error('[fetchInbox] Failed to fetch inbox list:', err)
    }
  }

  async function fetchMessages(chatId: string, activeChatId: any, activateChat = true) {
    if (!import.meta.client) return
    if (activateChat) activeChatId.value = chatId

    const cleanUserId = String(chatId).replace('user_', '')
    const cleanCurrentUserUuid = String(currentUserUuid.value).replace('user_', '')

    let isGroup = false
    const existingChat = chats.value.find((c: any) => String(c.id) === String(chatId))
    if (existingChat) {
      isGroup = existingChat.type === 'group'
    } else {
      try {
        const res = await chatApi.getChatSettings(String(chatId))
        isGroup = !!res?.isGroup
      } catch (err) {
        console.warn('Failed to fetch chat settings to determine group status:', err)
      }
    }

    const conversationId = isGroup ? String(chatId) : getSymmetricUuid(cleanCurrentUserUuid, cleanUserId)
    console.log('[useChatApi fetchMessages] Loading history for chatId:', chatId, 'conversationId:', conversationId, 'isGroup:', isGroup)

    if (!isGroup) {
      userCache.getOrFetchUser(chatId).then((fetchedUser) => {
        if (fetchedUser) {
          const chat = chats.value.find((c: any) => String(c.id) === String(chatId))
          if (chat) {
            chat.name = fetchedUser.name
            if (fetchedUser.avatar) chat.avatarUrl = fetchedUser.avatar
          }
        }
      }).catch(() => {})
    }

    try {
      const localMsgs = await chatStorage.loadLocalMessages(String(chatId))
      mergeMessagesForChat(chatId, localMsgs)
    } catch (dbErr) {
      console.error('Failed to load messages from IndexedDB:', dbErr)
    }

    const chat = chats.value.find((c: any) => String(c.id) === String(chatId))
    if (chat) {
      chat.unread = false
    }

    chatApi.markInboxAsRead(cleanCurrentUserUuid, conversationId)
      .catch((err) => {
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
      const chatData = await chatApi.getChatWithUser(cleanUserId, conversationId)
      if (chatData) {
        const settings = chatData.settings
        const isGroup = settings?.isGroup ?? (chats.value.some((c: any) => String(c.id) === String(chatId) && c.type === 'group') || !chatData.user?.firstName)

        const recipientUser = chatData.user
        const fullName = [recipientUser?.firstName, recipientUser?.lastName].filter(Boolean).join(' ') || 'Użytkownik'
        const avatarUrl = recipientUser?.avatar || '/default-avatar.png'

        let chat = chats.value.find((c: any) => String(c.id) === String(chatId))
        if (!chat) {
          chat = {
            id: chatId as any,
            name: isGroup ? `Grupa ${String(chatId).substring(0, 8)}` : fullName,
            avatarUrl: avatarUrl,
            lastMessage: '',
            timeAgo: 'Teraz',
            unread: false,
            isActive: true,
            type: (isGroup ? 'group' : 'private') as any,
          }
          chats.value.push(chat)
        }

        if (chat && settings) {
          if (settings.isGroup !== undefined) {
            chat.type = settings.isGroup ? 'group' : 'private'
          }
          if (settings.participants) {
            const membersList = settings.participants.map((p: any) => ({
              id: p.id,
              name: [p.firstName, p.lastName].filter(Boolean).join(' ') || 'Użytkownik',
              avatarUrl: p.avatar || '/default-avatar.png'
            }))
            chat.groupMembers = membersList
            if (chat.type === 'group') {
              const otherMembers = membersList.filter((m: any) => String(m.id) !== String(currentUserUuid.value))
              if (otherMembers.length > 0) {
                chat.name = otherMembers.map((m: any) => m.name).join(', ')
              }
            }
          }
        }

        if (chatData.messages && Array.isArray(chatData.messages)) {
          const isPrivate = chat ? chat.type !== 'group' : true
          const syncedMsgs: ChatMessage[] = []

          for (const item of chatData.messages) {
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

            if (item.imageUrl && /\.gif(\?|$)/i.test(item.imageUrl)) {
              parsedType = 'gif'
            }
if (item.linkUrl && /\/post\//.test(item.linkUrl)) {
              parsedType = 'feed-link'
            }

            let pollData: any = undefined
            if (item.systemActionType === 'poll' || item.systemActionType?.startsWith('poll')) {
              parsedType = 'poll'
              try {
                pollData = typeof item.systemActionPayload === 'string' ? JSON.parse(item.systemActionPayload) : item.systemActionPayload
              } catch (e) {
                console.warn('Failed to parse pollData:', e)
              }
            } else if (item.systemActionType) {
              if (item.systemActionType === 'call_rejected') {
                parsedType = 'call_rejected'
              } else if (item.systemActionType === 'call_ended') {
                parsedType = 'call'
                payloadValue = item.systemActionPayload
              } else if (item.systemActionType === 'call_started') {
                parsedType = 'call'
                subType = 'call_started'
                payloadValue = item.systemActionPayload
              } else {
                parsedType = 'action'
                subType = item.systemActionType
                payloadValue = item.systemActionPayload
              }
            } else if (item.text && item.text.startsWith('SYSTEM_ACTION:')) {
              const parts = item.text.split(':')
              if (parts[1] === 'call_rejected') {
                parsedType = 'call_rejected'
              } else if (parts[1] === 'call_ended') {
                parsedType = 'call'
                payloadValue = parts.slice(2).join(':')
              } else if (parts[1] === 'call_started') {
                parsedType = 'call'
                subType = 'call_started'
                payloadValue = parts.slice(2).join(':')
              } else {
                parsedType = 'action'
                subType = parts[1]
                payloadValue = parts.slice(2).join(':')
              }
            }

            const targetChatId = isPrivate
              ? String(item.senderId || item.sender_id || chatId).replace(/^user_/, '')
              : String(chatId).replace(/^user_/, '')
            const decryptedText = await decryptMessage(item.text, targetChatId, isPrivate)

            if (item.text?.startsWith('POLL:')) {
              parsedType = 'poll'
              try {
                pollData = JSON.parse(item.text.slice(5))
              } catch (e) {
                console.warn('Failed to parse pollData from text:', e)
              }
            } else if (decryptedText?.startsWith('POLL:')) {
              parsedType = 'poll'
              try {
                pollData = JSON.parse(decryptedText.slice(5))
              } catch (e) {
                console.warn('Failed to parse pollData from decryptedText:', e)
              }
            }

            if (parsedType === 'image' && (
              /\.gif(\?|$)/i.test(String(item.imageUrl || ''))
              || decryptedText === 'Wysłano GIF'
              || decryptedText.includes('Wysłano GIF')
            )) {
              parsedType = 'gif'
            }

            // Fallback if systemAction fields were missing but decrypted text is a system action
            if (parsedType !== 'action' && decryptedText && decryptedText.startsWith('SYSTEM_ACTION:')) {
              const parts = decryptedText.split(':')
              if (parts[1] === 'call_rejected') {
                parsedType = 'call_rejected'
              } else if (parts[1] === 'call_ended') {
                parsedType = 'call'
                payloadValue = parts.slice(2).join(':')
              } else if (parts[1] === 'call_started') {
                parsedType = 'call'
                subType = 'call_started'
                payloadValue = parts.slice(2).join(':')
              } else {
                parsedType = 'action'
                subType = parts[1]
                payloadValue = parts.slice(2).join(':')
              }
            }

            let iconSizeState: 'default' | 'small' | 'medium' | 'large' = 'default'
            if (item.duration === 1) iconSizeState = 'default'
            else if (item.duration === 2) iconSizeState = 'small'
            else if (item.duration === 3) iconSizeState = 'medium'
            else if (item.duration === 4) iconSizeState = 'large'
            else if (decryptedText) {
              const cleanContent = decryptedText.replace(/\s+/g, '')
              const emojiRegex = /[\u2700-\u27BF]|[\uE000-\uF8FF]|\uD83C[\uDC00-\uDFFF]|\uD83D[\uDC00-\uDFFF]|[\u2011-\u26FF]|\uD83E[\uDD10-\uDDFF]/g
              const nonEmoji = cleanContent.replace(emojiRegex, '')
              if (nonEmoji.length === 0 && cleanContent.length > 0) {
                const segmenter = new Intl.Segmenter(undefined, { granularity: 'grapheme' })
                const emojiCount = Array.from(segmenter.segment(cleanContent)).length
                if (emojiCount === 1) iconSizeState = 'large'
                else if (emojiCount === 2) iconSizeState = 'medium'
                else if (emojiCount === 3) iconSizeState = 'small'
              }
            }

            const pushedMsg: ChatMessage = {
              id: item.messageId,
              chatId: String(chatId),
              sender: item.senderId === currentUserUuid.value ? 'me' : 'them',
              content: decryptedText || (pollData?.question ?? ''),
              time: item.time,
              type: parsedType as any,
              subType: subType,
              payload: payloadValue,
              pollData: pollData,
              imageUrl: item.imageUrl || undefined,
              audioUrl: item.audioUrl || undefined,
              duration: item.duration || (parsedType === 'call' ? Number(payloadValue) : undefined),
              fileUrl: item.fileUrl || undefined,
              fileName: item.fileName || undefined,
              fileSize: item.fileSize || undefined,
              linkUrl: item.linkUrl || undefined,
              url: item.linkUrl || undefined,
              sharedPostId: parsedType === 'feed-link' && item.linkUrl
                ? (String(item.linkUrl).match(/\/post\/([^/?#]+)/)?.[1] || undefined)
                : undefined,
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
              isPrivate,
              iconSizeState,
            }
            syncedMsgs.push(pushedMsg)
          }

          try {
            await chatStorage.storeMessages(syncedMsgs)
          } catch (dbErr) {
            console.error('Failed to persist history to IndexedDB:', dbErr)
          }

          const updatedLocalMsgs = await chatStorage.loadLocalMessages(String(chatId))
          console.log('[useChatApi fetchMessages] Loading updated local messages count:', updatedLocalMsgs.length)
          mergeMessagesForChat(chatId, updatedLocalMsgs)

          if (chatData.messages.length > 0) {
            const lastMsg = chatData.messages[chatData.messages.length - 1]
            const lastMsgId = lastMsg.messageId

            if (!lastReadMaps.value[chatId]) {
              lastReadMaps.value[chatId] = {}
            }
            lastReadMaps.value[chatId][`user_${currentUserUuid.value}`] = lastMsgId

            const cleanRecipientId = String(chatId).replace(/^user_/, '')
            chatMqtt.publishMqtt('chat/messages/user/' + cleanRecipientId, {
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

  async function addMessage(chatId: string | number, msg: Partial<ChatMessage>) {
    const recipientUuid = String(chatId).replace('user_', '')
    const cleanCurrentUserUuid = String(currentUserUuid.value).replace('user_', '')
    const participantIds = [cleanCurrentUserUuid, recipientUuid]
    const conversationId = getSymmetricConversationId(chatId)

    const chat = chats.value.find((c: any) => String(c.id) === String(chatId))
    const isPrivate = chat ? chat.type !== 'group' : true

    // Renderuj wiadomość od razu. Dotychczas widok czekał wyłącznie na
    // komunikat MQTT, więc przy rozłączonym/opóźnionym MQTT wysłana wiadomość
    // znikała aż do ponownego pobrania historii.
    const temporaryId = `pending-${Date.now()}-${Math.random().toString(36).slice(2)}`
    const optimisticMessage = {
      ...msg,
      id: temporaryId,
      chatId: recipientUuid,
      sender: 'me',
      content: msg.content ?? '',
      time: msg.time ?? Date.now(),
      type: msg.type ?? 'text',
      isPrivate,
    } as ChatMessage
    messages.value.push(optimisticMessage)

    const token = typeof window !== 'undefined' ? localStorage.getItem('keycloak-token') : null
    const headers: Record<string, string> = {}
    if (token) {
      headers['Authorization'] = `Bearer ${token}`
    }

    const encryptedContent = await encryptMessage(msg.content ?? '', String(recipientUuid), isPrivate)

    try {
      const cleanCurrentUserUuid = String(currentUserUuid.value).replace('user_', '')
      const cleanChatId = String(chatId).replace('user_', '')

      const sentMessageId = await chatApi.sendMessage({
        senderId: cleanCurrentUserUuid,
        conversationId: conversationId,
        text: msg.type === 'poll'
          ? `POLL:${JSON.stringify(msg.pollData || {})}`
          : (msg.content ? encryptedContent : null),
        replyToId: msg.replyToId || null,
        replyToText: msg.replyToContentSnippet || null,
        replyToSenderId: msg.replyToSenderId || null,
        imageUrl: msg.imageUrl || null,
        audioUrl: msg.audioUrl || null,
        duration: msg.duration !== undefined && msg.duration !== null ? Number(msg.duration) : null,
        fileUrl: msg.fileUrl || null,
        fileName: msg.fileName || null,
        fileSize: msg.fileSize ? Number(msg.fileSize) : null,
        linkUrl: msg.linkUrl || null,
        participantIds: isPrivate
          ? [cleanCurrentUserUuid, cleanChatId]
          : (chat?.groupMembers?.map((m: any) => String(m.id || m.userId).replace(/^user_/, '')) || [cleanCurrentUserUuid, cleanChatId]),
      })

      const temporaryIndex = messages.value.findIndex((message: ChatMessage) => message.id === temporaryId)
      const mqttMessage = messages.value.find((message: ChatMessage) => String(message.id) === sentMessageId)

      // MQTT może nadejść przed odpowiedzią HTTP. Wtedy usuwamy wyłącznie
      // wersję tymczasową; w przeciwnym razie nadajemy jej właściwe ID.
      if (mqttMessage) {
        if (temporaryIndex !== -1) messages.value.splice(temporaryIndex, 1)
      } else if (temporaryIndex !== -1 && sentMessageId) {
        messages.value[temporaryIndex] = {
          ...optimisticMessage,
          id: sentMessageId,
        }
        chatStorage.storeMessage(messages.value[temporaryIndex]).catch((err) => {
          console.error('Failed to save sent message to IndexedDB:', err)
        })
      }

      console.log('Frontend GraphQL: Message sent successfully')
    } catch (err) {
      const temporaryIndex = messages.value.findIndex((message: ChatMessage) => message.id === temporaryId)
      if (temporaryIndex !== -1) messages.value.splice(temporaryIndex, 1)
      console.error('Failed to send message via GraphQL:', err)
    }
  }

  function addReaction(chatId: string | number, messageId: string | number, emoji: string) {
    const message = messages.value.find(
      (m: any) => String(m.chatId) === String(chatId) && String(m.id) === String(messageId),
    )
    if (message) {
      const conversationId = getSymmetricConversationId(chatId)
      const cleanCurrentUserUuid = String(currentUserUuid.value).replace('user_', '')
      const cleanChatId = String(chatId).replace('user_', '')

      chatApi.reactToMessage({
        senderId: cleanCurrentUserUuid,
        conversationId: String(conversationId),
        messageId: String(messageId),
        reactionEmoji: emoji,
        participantIds: [cleanCurrentUserUuid, cleanChatId],
      }).then(() => {
        console.log('Reaction sent via GraphQL')
      }).catch((err) => {
        console.error('Failed to send reaction via GraphQL:', err)
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
