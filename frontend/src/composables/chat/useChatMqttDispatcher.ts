import type { Ref } from 'vue'
import { type Chat } from '@/types/Chat'
import { type ChatMessage } from '@/types/Message'
import type { Theme } from '@/types/Theme'
import { useChatStore } from '@/stores/chat'
import { useNotify } from '@/composables/shared/useNotify'
import { decryptMessage } from '@/utils/e2ee'
import { formatSystemActionText } from '@/utils/contentProcessor'
import { playNotificationSound } from '@/utils/audio'

export interface ChatMqttDispatcherContext {
  currentUserUuid: Ref<string>
  chats: Ref<Chat[]>
  messages: Ref<ChatMessage[]>
  lastReadMaps: Ref<Record<string, Record<string, string>>>
  typingUsers: Ref<Record<string, boolean>>
  typingTimeouts: Record<string, any>
  activeChatId: Ref<string | null>
  mqttClientId: Ref<string | null>
  themes: Ref<Theme[]>
  chatStorage: any
  chatCalls: any
  chatSettings: any
  themeStore: any
  notify: any
  getOrFetchUser: (id: string) => Promise<any>
  chatApi: any
  chatMqtt: any
  getSymmetricConversationId: (id: string | number) => string
  fetchChatSettings: (id: string | number) => Promise<void>
}

export function useChatMqttDispatcher(ctx: ChatMqttDispatcherContext) {
  const {
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
  } = ctx

  const handleIncomingMqttPayload = async (topic: string, payload: any) => {
    if (topic.endsWith('/notifications')) {
      console.log('[MQTT Notification] Received new notification payload:', payload)
      if (payload.type === 'NOTIFICATION_READ') {
        if (typeof window !== 'undefined') {
          window.dispatchEvent(new CustomEvent('notification-read-sync', { detail: payload }))
        }
        return
      }

      playNotificationSound()

      notify.notification({
        title: payload.title || 'Powiadomienie',
        header: payload.message || '',
        avatar: `https://ui-avatars.com/api/?name=${encodeURIComponent(payload.title)}&background=random&color=fff`,
      })
      if (typeof window !== 'undefined') {
        window.dispatchEvent(new CustomEvent('new-notification', { detail: payload }))
      }
      return
    }

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
        getOrFetchUser(payload.callerId)
          .then((caller) => {
            chatCalls.initiateCall(
              payload.conversationId,
              payload.callerId,
              payload.callType || 'video',
              caller ? caller.name : 'Użytkownik',
              caller?.avatar || '/default-avatar.png',
            )
          })
          .catch(() => {})
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
      const senderId = payload.senderId

      const targetChatIds: string[] = []
      const chat = chats.value.find(
        (c) => getSymmetricConversationId(c.id) === convId || String(c.id) === String(convId),
      )
      if (chat) {
        targetChatIds.push(String(chat.id))
      }
      if (convId) targetChatIds.push(String(convId))
      if (senderId && senderId !== currentUserUuid.value) targetChatIds.push(String(senderId))

      let resolvedThemeId = payload.themeId
      let resolvedThemeObj: Theme | undefined = undefined
      if (payload.themeKey) {
        resolvedThemeObj = themes.value.find((t) => t.id === payload.themeKey)
        if (resolvedThemeObj) {
          const idx = themes.value.findIndex((t) => t.id === payload.themeKey)
          resolvedThemeId = idx >= 0 ? idx : 0
        }
      } else if (resolvedThemeId !== undefined && resolvedThemeId !== null) {
        resolvedThemeObj =
          typeof resolvedThemeId === 'number'
            ? themes.value[resolvedThemeId]
            : themes.value.find((t) => t.id === resolvedThemeId)
      }

      for (const targetId of new Set(targetChatIds)) {
        const s = chatSettings._getOrCreateSettings(targetId)
        if (payload.emoji !== undefined) {
          s.emoji = payload.emoji
        }
        if (resolvedThemeId !== undefined && resolvedThemeId !== null) {
          s.themeId = resolvedThemeId
        }
      }

      if (resolvedThemeObj) {
        themeStore.setSelectedTheme(resolvedThemeObj.id)
      }
      if (payload.emoji !== undefined) {
        themeStore.setSelectedEmoji(payload.emoji)
      }
      return
    }

    if (payload.type === 'chat_nickname_changed') {
      console.log('Receiver MQTT: Detected chat_nickname_changed event!', payload)
      const convId = payload.conversationId
      const chat = chats.value.find((c) => getSymmetricConversationId(c.id) === convId)
      if (chat && chat.type === 'private') {
        chat.otherUserNickname = payload.nickname
      }
      return
    }

    if (payload.type === 'read') {
      const convId = payload.conversationId
      const chat = chats.value.find((c) => getSymmetricConversationId(c.id) === convId)
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
        chatStorage.storeMessage(msg).catch((err: any) => {
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
        ? senderId === currentUserUuid.value
          ? payload.participantIds?.find((id: any) => String(id) !== String(currentUserUuid.value)) ||
            senderId
          : senderId
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
        chatStorage.storeMessage(targetMsg).catch((err: any) => {
          console.error('Failed to update reaction in IndexedDB from MQTT:', err)
        })
      }

      const chat = chats.value.find((c) => String(c.id) === String(logicalChatId))
      if (chat && senderId !== currentUserUuid.value) {
        chat.lastMessage = `Zareagował ${emoji} na Twoją wiadomość`
      }
      return
    }

    if (payload.type === 'poll_voted') {
      const targetMsgId = payload.messageId
      const optionIds = (payload.optionIds || []).map(String)
      const voterId = String(payload.voterId || payload.senderId).replace(/^user_/, '')
      const targetMsg = messages.value.find((m) => String(m.id) === String(targetMsgId))
      if (targetMsg && targetMsg.pollData) {
        targetMsg.pollData.options.forEach((opt: any) => {
          if (!opt.voterIds) opt.voterIds = []
          const isSelected = optionIds.includes(String(opt.id))
          const wasVoted = opt.voterIds.includes(voterId)
          if (isSelected && !wasVoted) {
            opt.voterIds.push(voterId)
          } else if (!isSelected && wasVoted) {
            opt.voterIds = opt.voterIds.filter((id: string) => id !== voterId)
          }
          opt.votes = opt.voterIds.length
          if (voterId === String(currentUserUuid.value).replace(/^user_/, '')) {
            opt.votedByMe = isSelected
          }
        })
        chatStorage.storeMessage(targetMsg).catch(console.error)
      }
      return
    }

    if (payload.type === 'poll_option_added') {
      const targetMsgId = payload.messageId
      const newOpt = payload.option
      const targetMsg = messages.value.find((m) => String(m.id) === String(targetMsgId))
      if (targetMsg && targetMsg.pollData && newOpt) {
        const exists = targetMsg.pollData.options.some(
          (o: any) => String(o.id) === String(newOpt.id) || o.text === newOpt.text,
        )
        if (!exists) {
          targetMsg.pollData.options.push(newOpt)
          chatStorage.storeMessage(targetMsg).catch(console.error)
        }
      }
      return
    }

    const msgId = payload.messageId
    const convId = payload.conversationId
    const senderId = payload.senderId
    const text = payload.text

    const isPrivate = !payload.participantIds || payload.participantIds.length <= 2
    const logicalChatId = isPrivate
      ? senderId === currentUserUuid.value
        ? payload.participantIds?.find((id: any) => String(id) !== String(currentUserUuid.value)) ||
          senderId
        : senderId
      : convId

    const targetId = isPrivate ? senderId || logicalChatId : convId
    const decryptedText = await decryptMessage(text, String(targetId), isPrivate)

    if (decryptedText.startsWith('SYSTEM_ACTION:ADD_MEMBER:')) {
      fetchChatSettings(logicalChatId)
    }

    if (messages.value.some((m) => m.id === msgId)) {
      return
    }

    if (senderId !== currentUserUuid.value) {
      playNotificationSound()
      if (typeof window !== 'undefined') {
        window.dispatchEvent(new CustomEvent('new-chat-message', { detail: payload }))
      }
    }

    let parsedType = payload.audioUrl
      ? 'audio'
      : payload.imageUrl
        ? 'image'
        : payload.fileUrl
          ? 'file'
          : payload.linkUrl
            ? 'link'
            : 'text'
    let subType: any = undefined
    let payloadValue: any = undefined

    if (
      payload.imageUrl &&
      (/\.gif(\?|$)/i.test(payload.imageUrl) ||
        decryptedText === 'Wysłano GIF' ||
        (typeof decryptedText === 'string' && decryptedText.includes('Wysłano GIF')))
    ) {
      parsedType = 'gif'
    }

    if (payload.linkUrl && /\/post\//.test(payload.linkUrl)) {
      parsedType = 'feed-link'
    }

    let pollData: any = payload.pollData
    if (payload.type === 'poll' || payload.systemActionType === 'poll') {
      parsedType = 'poll'
      if (!pollData && payload.systemActionPayload) {
        try {
          pollData =
            typeof payload.systemActionPayload === 'string'
              ? JSON.parse(payload.systemActionPayload)
              : payload.systemActionPayload
        } catch (e) {
          console.warn('Failed to parse pollData in MQTT:', e)
        }
      }
    } else if (payload.systemActionType) {
      if (payload.systemActionType === 'call_rejected') {
        parsedType = 'call_rejected'
      } else if (payload.systemActionType === 'call_ended') {
        parsedType = 'call'
        payloadValue = payload.systemActionPayload
      } else if (payload.systemActionType === 'call_started') {
        parsedType = 'call'
        subType = 'call_started'
        payloadValue = payload.systemActionPayload
      } else {
        parsedType = 'action'
        subType = payload.systemActionType
        payloadValue = payload.systemActionPayload
      }
    } else if (decryptedText && decryptedText.startsWith('SYSTEM_ACTION:')) {
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
    } else if (text?.startsWith('POLL:')) {
      parsedType = 'poll'
      try {
        pollData = JSON.parse(text.slice(5))
      } catch {}
    } else if (decryptedText?.startsWith('POLL:')) {
      parsedType = 'poll'
      try {
        pollData = JSON.parse(decryptedText.slice(5))
      } catch {}
    }

    // Replace optimistic local action notification with the persisted server message
    if (parsedType === 'action' && subType) {
      const localIdx = messages.value.findIndex(
        (m) =>
          String(m.chatId) === String(logicalChatId) &&
          m.type === 'action' &&
          String(m.id).startsWith('local-action') &&
          (m as any).subType === subType &&
          (m as any).payload === payloadValue,
      )
      if (localIdx !== -1) {
        messages.value.splice(localIdx, 1)
      }
    }

    if (parsedType === 'action' && subType && payloadValue !== undefined) {
      const matchingIds = [
        String(logicalChatId),
        String(payload.conversationId),
        String(senderId),
      ].filter(Boolean)
      const chat = chats.value.find(
        (c) =>
          getSymmetricConversationId(c.id) === payload.conversationId ||
          String(c.id) === String(logicalChatId),
      )
      if (chat) matchingIds.push(String(chat.id))

      for (const tId of new Set(matchingIds)) {
        const s = chatSettings._getOrCreateSettings(tId)
        if (subType === 'CHANGE_E') {
          s.emoji = payloadValue
        } else if (subType === 'CHANGE_THEME') {
          const idx = themes.value.findIndex((t) => t.id === payloadValue || t.title === payloadValue)
          s.themeId = idx >= 0 ? idx : 0
        }
      }

      if (subType === 'CHANGE_E') {
        themeStore.setSelectedEmoji(payloadValue)
      } else if (subType === 'CHANGE_THEME') {
        const idx = themes.value.findIndex((t) => t.id === payloadValue || t.title === payloadValue)
        if (idx >= 0) themeStore.setSelectedTheme(themes.value[idx].id)
      } else if (subType === 'CHANGE_NICKNAME') {
        if (chat && chat.type === 'private') {
          chat.otherUserNickname = payloadValue
        }
      }
    }

    let iconSizeState: 'default' | 'small' | 'medium' | 'large' = 'default'
    if (payload.duration === 1) iconSizeState = 'default'
    else if (payload.duration === 2) iconSizeState = 'small'
    else if (payload.duration === 3) iconSizeState = 'medium'
    else if (payload.duration === 4) iconSizeState = 'large'
    else if (decryptedText) {
      const cleanContent = decryptedText.replace(/\s+/g, '')
      const emojiRegex =
        /[\u2700-\u27BF]|[\uE000-\uF8FF]|\uD83C[\uDC00-\uDFFF]|\uD83D[\uDC00-\uDFFF]|[\u2011-\u26FF]|\uD83E[\uDD10-\uDDFF]/g
      const nonEmoji = cleanContent.replace(emojiRegex, '')
      if (nonEmoji.length === 0 && cleanContent.length > 0) {
        const segmenter = new Intl.Segmenter(undefined, { granularity: 'grapheme' })
        const emojiCount = Array.from(segmenter.segment(cleanContent)).length
        if (emojiCount === 1) iconSizeState = 'large'
        else if (emojiCount === 2) iconSizeState = 'medium'
        else if (emojiCount === 3) iconSizeState = 'small'
      }
    }

    const newMsg: ChatMessage = {
      id: msgId,
      chatId: String(logicalChatId),
      sender: senderId === currentUserUuid.value ? 'me' : 'them',
      content: decryptedText,
      time: Date.now(),
      type: parsedType as any,
      subType,
      payload: payloadValue,
      pollData: pollData,
      reactions: payload.reactionsJson ? JSON.parse(payload.reactionsJson) : {},
      audioUrl: payload.audioUrl,
      imageUrl: payload.imageUrl,
      duration: payload.duration || (parsedType === 'call' ? Number(payloadValue) : undefined),
      fileUrl: payload.fileUrl,
      fileName: payload.fileName,
      fileSize: payload.fileSize,
      linkUrl: payload.linkUrl,
      url: payload.linkUrl,
      sharedPostId:
        parsedType === 'feed-link' && payload.linkUrl
          ? String(payload.linkUrl).match(/\/post\/([^/?#]+)/)?.[1] || undefined
          : undefined,
      replyToId: payload.replyToId,
      replyToSenderId: payload.replyToSenderId,
      replyToContentSnippet: payload.replyToText,
      isPrivate,
      iconSizeState,
    }

    chatStorage
      .storeMessage(newMsg)
      .catch((err: any) => console.error('Failed to save MQTT message to IndexedDB:', err))

    messages.value.push(newMsg)

    if (!lastReadMaps.value[logicalChatId]) {
      lastReadMaps.value[logicalChatId] = {}
    }
    lastReadMaps.value[logicalChatId][`user_${senderId}`] = msgId

    const chat = chats.value.find((c) => String(c.id) === String(logicalChatId))
    if (chat) {
      let previewText = newMsg.content
      if (newMsg.type === 'action' || previewText.startsWith('SYSTEM_ACTION:')) {
        previewText = formatSystemActionText(previewText)
        chat.lastMessage = previewText
      } else {
        chat.lastMessage = newMsg.sender === 'me' ? `Ty: ${previewText}` : `${previewText}`
      }

      if (newMsg.sender !== 'me') {
        const muted = chatSettings.isChatMuted(logicalChatId)
        if (muted) {
          const chatStore = useChatStore()
          chatStore.removeMessageBox(logicalChatId)
        } else {
          const cleanActiveId = String(activeChatId.value || '').replace('user_', '')
          const cleanLogicalId = String(logicalChatId).replace('user_', '')
          if (cleanActiveId !== cleanLogicalId) {
            chat.unread = true
          }

          const isInChatView =
            typeof window !== 'undefined' && window.location.pathname.startsWith('/chat')
          if (!isInChatView) {
            const chatStore = useChatStore()
            chatStore.addMessageBox(logicalChatId)

            try {
              const notify = useNotify()
              notify.notification({
                title: chat.name || 'Nowa wiadomość',
                header: newMsg.content,
                avatar: chat.avatarUrl || '/default-avatar.png',
              })
            } catch (nErr) {
              console.error('Failed to dispatch toast notification:', nErr)
            }
          }
        }
      }

      const cleanActiveIdForRead = String(activeChatId.value || '').replace('user_', '')
      const cleanLogicalIdForRead = String(logicalChatId).replace('user_', '')
      if (newMsg.sender !== 'me' && cleanActiveIdForRead === cleanLogicalIdForRead) {
        chatApi
          .markInboxAsRead(String(currentUserUuid.value), String(convId))
          .catch((err: any) => console.error('Failed to mark message as read:', err))

        if (!lastReadMaps.value[logicalChatId]) {
          lastReadMaps.value[logicalChatId] = {}
        }
        lastReadMaps.value[logicalChatId][`user_${currentUserUuid.value}`] = msgId

        const cleanRecipientId = String(logicalChatId).replace(/^user_/, '')
        chatMqtt.publishMqtt('chat/messages/user/' + cleanRecipientId, {
          type: 'read',
          conversationId: convId,
          senderId: currentUserUuid.value,
          lastReadMessageId: msgId,
        })
      }
    } else {
      getOrFetchUser(String(logicalChatId))
        .then((recipient) => {
          const muted = chatSettings.isChatMuted(logicalChatId)
          const newChatName = recipient
            ? recipient.name
            : `Konwersacja ${String(logicalChatId).substring(0, 8)}`
          const newChatAvatar = recipient?.avatar || '/default-avatar.png'

          const cleanActiveIdForUnread = String(activeChatId.value || '').replace('user_', '')
          const cleanLogicalIdForUnread = String(logicalChatId).replace('user_', '')

          chats.value.push({
            id: logicalChatId as any,
            name: newChatName,
            avatarUrl: newChatAvatar,
            lastMessage:
              newMsg.type === 'action' || newMsg.content.startsWith('SYSTEM_ACTION:')
                ? formatSystemActionText(newMsg.content)
                : newMsg.sender === 'me'
                  ? `Ty: ${newMsg.content}`
                  : `${newMsg.content}`,
            timeAgo: 'Teraz',
            unread:
              newMsg.sender !== 'me' &&
              cleanActiveIdForUnread !== cleanLogicalIdForUnread &&
              !muted,
            isActive: true,
            type: 'private' as any,
          })

          if (newMsg.sender !== 'me') {
            if (muted) {
              const chatStore = useChatStore()
              chatStore.removeMessageBox(logicalChatId)
            } else {
              const isInChatView =
                typeof window !== 'undefined' && window.location.pathname.startsWith('/chat')
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
        })
        .catch(() => {})
    }
  }

  return {
    handleIncomingMqttPayload,
  }
}
