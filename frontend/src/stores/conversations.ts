import { ref, watch, computed } from 'vue'
import { defineStore, storeToRefs } from 'pinia'
import { type Chat } from '@/types/Chat'
const rawChats: Chat[] = []
import { type ChatMessage } from '@/types/Message'
import { useChatThemeStore } from '@/stores/chatTheme'
import type { Theme } from '@/types/Theme'
import { useAuthStore } from '@/stores/auth'
import { useChatStore } from '@/stores/chat'
import { useNotify } from '@/composables/shared/useNotify'
import { useChatMqtt } from '@/composables/chat/useChatMqtt'
import { useChatStorage } from '@/composables/chat/useChatStorage'
import { useUserCache } from '@/composables/shared/useUserCache'
import { useChatCalls } from '@/composables/chat/useChatCalls'
import { useChatSettings } from '@/composables/chat/useChatSettings'
import { useChatApi } from '@/composables/chat/useChatApi'
import { getApolloClient } from '@/utils/apollo'
import { gql } from 'graphql-tag'
import { MARK_INBOX_AS_READ } from '@/graphql/chat'
import { decryptMessage, encryptMessage } from '@/utils/e2ee'
import { signalStore } from '@/utils/e2ee/signalStore.client'
import { getSymmetricUuid } from '@/utils/uuid'
import { formatSystemActionText } from '@/utils/contentProcessor'
import { playNotificationSound } from '@/utils/audio'

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

  if (import.meta.client) {
    import('@/utils/e2ee').then(({ initIdentityKeys }) => {
      initIdentityKeys().catch((err) => console.error('Failed to initialize E2EE keys:', err))
    })

    const connectMqtt = () => {
      chatMqtt.connectMqtt(currentUserUuid.value, async (topic, payload) => {
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
            getOrFetchUser(payload.callerId).then((caller) => {
              chatCalls.initiateCall(
                payload.conversationId,
                payload.callerId,
                payload.callType || 'video',
                caller ? caller.name : 'Użytkownik',
                caller?.avatar || '/default-avatar.png'
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
          const senderId = payload.senderId
          
          let targetChatIds: string[] = []
          const chat = chats.value.find(c => getSymmetricConversationId(c.id) === convId || String(c.id) === String(convId))
          if (chat) {
            targetChatIds.push(String(chat.id))
          }
          if (convId) targetChatIds.push(String(convId))
          if (senderId && senderId !== currentUserUuid.value) targetChatIds.push(String(senderId))

          let resolvedThemeId = payload.themeId
          let resolvedThemeObj: Theme | undefined = undefined
          if (payload.themeKey) {
            resolvedThemeObj = themes.value.find(t => t.id === payload.themeKey)
            if (resolvedThemeObj) {
              const idx = themes.value.findIndex(t => t.id === payload.themeKey)
              resolvedThemeId = idx >= 0 ? idx : 0
            }
          } else if (resolvedThemeId !== undefined && resolvedThemeId !== null) {
            resolvedThemeObj = typeof resolvedThemeId === 'number' ? themes.value[resolvedThemeId] : themes.value.find(t => t.id === resolvedThemeId)
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
            const exists = targetMsg.pollData.options.some((o: any) => String(o.id) === String(newOpt.id) || o.text === newOpt.text)
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
          ? (senderId === currentUserUuid.value
              ? (payload.participantIds?.find((id: any) => String(id) !== String(currentUserUuid.value)) || senderId)
              : senderId)
          : convId

        const targetId = isPrivate ? (senderId || logicalChatId) : convId
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

        let parsedType = payload.audioUrl ? 'audio' : (payload.imageUrl ? 'image' : (payload.fileUrl ? 'file' : (payload.linkUrl ? 'link' : 'text')))
        let subType: any = undefined
        let payloadValue: any = undefined

        if (payload.imageUrl && (
          /\.gif(\?|$)/i.test(payload.imageUrl)
          || decryptedText === 'Wysłano GIF'
          || (typeof decryptedText === 'string' && decryptedText.includes('Wysłano GIF'))
        )) {
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
              pollData = typeof payload.systemActionPayload === 'string' ? JSON.parse(payload.systemActionPayload) : payload.systemActionPayload
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
              (m as any).payload === payloadValue
          )
          if (localIdx !== -1) {
            messages.value.splice(localIdx, 1)
          }
        }

        if (parsedType === 'action' && subType && payloadValue !== undefined) {
          const matchingIds = [String(logicalChatId), String(payload.conversationId), String(senderId)].filter(Boolean)
          const chat = chats.value.find(c => getSymmetricConversationId(c.id) === payload.conversationId || String(c.id) === String(logicalChatId))
          if (chat) matchingIds.push(String(chat.id))

          for (const tId of new Set(matchingIds)) {
            const s = chatSettings._getOrCreateSettings(tId)
            if (subType === 'CHANGE_E') {
              s.emoji = payloadValue
            } else if (subType === 'CHANGE_THEME') {
              const idx = themes.value.findIndex(t => t.id === payloadValue || t.title === payloadValue)
              s.themeId = idx >= 0 ? idx : 0
            }
          }

          if (subType === 'CHANGE_E') {
            themeStore.setSelectedEmoji(payloadValue)
          } else if (subType === 'CHANGE_THEME') {
            const idx = themes.value.findIndex(t => t.id === payloadValue || t.title === payloadValue)
            if (idx >= 0) themeStore.setSelectedTheme(themes.value[idx].id)
          } else if (subType === 'CHANGE_NICKNAME') {
            if (chat && chat.type === 'private') {
              chat.otherUserNickname = payloadValue
            }
          }
        }

        // Calculate emoji size state dynamically for incoming MQTT messages
        let iconSizeState: 'default' | 'small' | 'medium' | 'large' = 'default'
        if (payload.duration === 1) iconSizeState = 'default'
        else if (payload.duration === 2) iconSizeState = 'small'
        else if (payload.duration === 3) iconSizeState = 'medium'
        else if (payload.duration === 4) iconSizeState = 'large'
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
          sharedPostId: parsedType === 'feed-link' && payload.linkUrl
            ? (String(payload.linkUrl).match(/\/post\/([^/?#]+)/)?.[1] || undefined)
            : undefined,
          replyToId: payload.replyToId,
          replyToSenderId: payload.replyToSenderId,
          replyToContentSnippet: payload.replyToText,
          isPrivate,
          iconSizeState,
        }

        chatStorage.storeMessage(newMsg).catch((err) => console.error('Failed to save MQTT message to IndexedDB:', err))

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

              const isInChatView = typeof window !== 'undefined' && window.location.pathname.startsWith('/chat')
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
            const apolloClient = getApolloClient()
            apolloClient.mutate({
              mutation: MARK_INBOX_AS_READ,
              variables: {
                userId: String(currentUserUuid.value),
                conversationId: String(convId)
              }
            }).catch(err => console.error('Failed to mark message as read:', err))

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
          getOrFetchUser(String(logicalChatId)).then((recipient) => {
            const muted = chatSettings.isChatMuted(logicalChatId)
            const newChatName = recipient ? recipient.name : `Konwersacja ${String(logicalChatId).substring(0, 8)}`
            const newChatAvatar = recipient?.avatar || '/default-avatar.png'

            const cleanActiveIdForUnread = String(activeChatId.value || '').replace('user_', '')
            const cleanLogicalIdForUnread = String(logicalChatId).replace('user_', '')

            chats.value.push({
              id: logicalChatId as any,
              name: newChatName,
              avatarUrl: newChatAvatar,
              lastMessage: (newMsg.type === 'action' || newMsg.content.startsWith('SYSTEM_ACTION:'))
                ? formatSystemActionText(newMsg.content)
                : (newMsg.sender === 'me' ? `Ty: ${newMsg.content}` : `${newMsg.content}`),
              timeAgo: 'Teraz',
              unread: newMsg.sender !== 'me' && cleanActiveIdForUnread !== cleanLogicalIdForUnread && !muted,
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
    const cleanTargetId = String(chatId).replace('user_', '')
    const filtered = messages.value.filter((m) => String(m.chatId).replace('user_', '') === cleanTargetId)
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
    const myId = typeof window !== 'undefined' ? localStorage.getItem('auth-current-user-id') || localStorage.getItem('user-uuid') || '' : ''
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
    const participantIds = chat && chat.type === 'group' && chat.groupMembers
      ? chat.groupMembers.map((m: any) => String(m.id))
      : [String(currentUserUuid.value), String(chatId).replace('user_', '')]

    chatSettings.saveCustomization(apiUrl, headers, conversationId, emoji, undefined, participantIds)

    // Natychmiastowe powiadomienie MQTT o zmianie emoji
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
    const participantIds = chat && chat.type === 'group' && chat.groupMembers
      ? chat.groupMembers.map((m: any) => String(m.id))
      : [String(currentUserUuid.value), String(chatId).replace('user_', '')]

    chatSettings.saveCustomization(apiUrl, headers, conversationId, undefined, finalIdx, participantIds)

    // Natychmiastowe powiadomienie MQTT o zmianie motywu dla wszystkich uczestników
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
      chatSettings.saveNickname(apiUrl, headers, conversationId, String(chatId), nickname, participantIds)
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
      const participantIds = chat.groupMembers.map((m: any) => String(m.id))
      chatSettings.saveGroupNicknames(apiUrl, headers, conversationId, nicknames, participantIds)
    }
  }

  async function fetchChatSettings(chatId: string | number) {
    if (!import.meta.client) return
    const conversationId = getSymmetricConversationId(chatId)
    const apolloClient = getApolloClient()
    try {
      const res = await apolloClient.query({
        query: gql`
          query GetChatSettings($conversationId: ID!) {
            getChatSettings(conversationId: $conversationId) {
              themeId
              emoji
              nicknames {
                userId
                nickname
              }
              participants {
                id
                firstName
                lastName
                avatar
              }
              isGroup
            }
          }
        `,
        variables: { conversationId },
        fetchPolicy: 'cache-first'
      })
      const data = res.data?.getChatSettings
      if (data) {
        const s = chatSettings._getOrCreateSettings(chatId)
        if (data.emoji) {
          s.emoji = data.emoji
          themeStore.setSelectedEmoji(data.emoji)
        }
        if (data.themeId !== null && data.themeId !== undefined) {
          s.themeId = data.themeId
          const theme = typeof data.themeId === 'number' ? themes.value[data.themeId] : themes.value.find((t: any) => t.id === data.themeId)
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

        if (data.nicknames) {
          const nicknameRecord: Record<string, string> = {}
          data.nicknames.forEach((item: any) => {
            nicknameRecord[String(item.userId)] = item.nickname
          })

          if (chat) {
            if (chat.type === 'group' && chat.groupMembers) {
              chat.groupMembers = chat.groupMembers.map(m => {
                const customNick = nicknameRecord[String(m.id)]
                return {
                  ...m,
                  nickname: customNick !== undefined ? customNick : (m.nickname || '')
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
    const apolloClient = getApolloClient()

    try {
      await apolloClient.mutate({
        mutation: gql`
          mutation PinChatMessage($conversationId: ID!, $messageId: ID!, $isPinned: Boolean!, $participantIds: [ID!]!) {
            pinChatMessage(conversationId: $conversationId, messageId: $messageId, isPinned: $isPinned, participantIds: $participantIds)
          }
        `,
        variables: {
          conversationId,
          messageId: String(messageId),
          isPinned: newPinnedStatus,
          participantIds: [cleanCurrentUserUuid, cleanChatId],
        }
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

  async function voteChatPoll(chatId: string | number, messageId: string | number, optionIds: (string | number)[]) {
    const cleanSenderId = String(currentUserUuid.value).replace(/^user_/, '')
    const targetMsg = messages.value.find((m) => String(m.id) === String(messageId))
    const cleanOptionIds = optionIds.map(String)

    if (targetMsg && targetMsg.pollData) {
      targetMsg.pollData.options.forEach((opt: any) => {
        if (!opt.voterIds) opt.voterIds = []
        const isSelected = cleanOptionIds.includes(String(opt.id))
        const wasVoted = opt.voterIds.includes(cleanSenderId)
        if (isSelected && !wasVoted) {
          opt.voterIds.push(cleanSenderId)
        } else if (!isSelected && wasVoted) {
          opt.voterIds = opt.voterIds.filter((id: string) => id !== cleanSenderId)
        }
        opt.votes = opt.voterIds.length
        opt.votedByMe = isSelected
      })
      await chatStorage.storeMessage(targetMsg).catch(console.error)
    }

    // Broadcast vote to all participants via MQTT
    const chat = chats.value.find((c) => String(c.id) === String(chatId))
    const participantIds: string[] = []
    if (chat && chat.groupMembers) {
      chat.groupMembers.forEach((m: any) => {
        const pId = String(m.userId || m.id).replace(/^user_/, '')
        if (pId) participantIds.push(pId)
      })
    } else {
      const cleanChatId = String(chatId).replace(/^user_/, '')
      participantIds.push(cleanChatId)
    }
    if (!participantIds.includes(cleanSenderId)) {
      participantIds.push(cleanSenderId)
    }

    const convId = getSymmetricConversationId(chatId)
    participantIds.forEach((pId) => {
      publishMqtt(`chat/messages/user/${pId}`, {
        type: 'poll_voted',
        conversationId: convId,
        messageId: String(messageId),
        optionIds: cleanOptionIds,
        voterId: cleanSenderId,
      }, { qos: 1 })
    })
  }

  async function addChatPollOption(chatId: string | number, messageId: string | number, option: any) {
    const cleanSenderId = String(currentUserUuid.value).replace(/^user_/, '')
    const targetMsg = messages.value.find((m) => String(m.id) === String(messageId))

    if (targetMsg && targetMsg.pollData) {
      const exists = targetMsg.pollData.options.some((o: any) => String(o.id) === String(option.id) || o.text === option.text)
      if (!exists) {
        targetMsg.pollData.options.push(option)
        await chatStorage.storeMessage(targetMsg).catch(console.error)
      }
    }

    const chat = chats.value.find((c) => String(c.id) === String(chatId))
    const participantIds: string[] = []
    if (chat && chat.groupMembers) {
      chat.groupMembers.forEach((m: any) => {
        const pId = String(m.userId || m.id).replace(/^user_/, '')
        if (pId) participantIds.push(pId)
      })
    } else {
      const cleanChatId = String(chatId).replace(/^user_/, '')
      participantIds.push(cleanChatId)
    }
    if (!participantIds.includes(cleanSenderId)) {
      participantIds.push(cleanSenderId)
    }

    const convId = getSymmetricConversationId(chatId)
    participantIds.forEach((pId) => {
      publishMqtt(`chat/messages/user/${pId}`, {
        type: 'poll_option_added',
        conversationId: convId,
        messageId: String(messageId),
        option,
      }, { qos: 1 })
    })
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
        addedByUserId: String(currentUserUuid.value)
      })
    }

    const otherMembers = chat.groupMembers.filter((m: any) => String(m.id) !== String(currentUserUuid.value))
    if (otherMembers.length > 0) {
      chat.name = otherMembers.map((m: any) => m.name).join(', ')
    }

    const participantIds = chat.groupMembers.map((m: any) => String(m.id).replace('user_', ''))
    const cleanCurrentUserUuid = String(currentUserUuid.value).replace('user_', '')
    const cleanSelectedUserId = String(selectedUser.id).replace('user_', '')

    const apolloClient = getApolloClient()

    await apolloClient.mutate({
      mutation: gql`
        mutation SendChatMessage($input: SendChatMessageInput!) {
          sendChatMessage(input: $input) {
            messageId
          }
        }
      `,
      variables: {
        input: {
          senderId: cleanCurrentUserUuid,
          conversationId: String(chatId),
          text: `SYSTEM_ACTION:ADD_MEMBER:${cleanSelectedUserId}`,
          participantIds: participantIds
        }
      }
    })

    const myKey = await signalStore.getCustomValue<string>(`sender_key_${chatId}_${currentUserUuid.value}`)
    if (myKey) {
      const encryptedBackup = await encryptMessage(`SYSTEM_ACTION:BACKUP_SENDER_KEY:${chatId}:${myKey}`, selectedUser.id, true)
      await apolloClient.mutate({
        mutation: gql`
          mutation SendChatMessage($input: SendChatMessageInput!) {
            sendChatMessage(input: $input) {
              messageId
            }
          }
        `,
        variables: {
          input: {
            senderId: cleanCurrentUserUuid,
            conversationId: getSymmetricUuid(cleanCurrentUserUuid, cleanSelectedUserId),
            text: encryptedBackup,
            participantIds: [cleanCurrentUserUuid, cleanSelectedUserId]
          }
        }
      })
      console.log(`[addGroupMember] Successfully sent encrypted backup sender_key to ${selectedUser.name}`)
    }
  }

  async function leaveGroup(chatId: string | number) {
    const apolloClient = getApolloClient()
    const cleanCurrentUserUuid = String(currentUserUuid.value).replace('user_', '')
    const cleanChatId = String(chatId).replace('user_', '')
    try {
      await apolloClient.mutate({
        mutation: gql`
          mutation LeaveChat($userId: ID!, $conversationId: ID!) {
            leaveChat(userId: $userId, conversationId: $conversationId)
          }
        `,
        variables: {
          userId: cleanCurrentUserUuid,
          conversationId: cleanChatId
        }
      })

      chats.value = chats.value.filter(c => String(c.id) !== String(chatId))
      if (String(activeChatId.value) === String(chatId)) {
        activeChatId.value = null
      }
      console.log(`[leaveGroup] Successfully left group ${chatId}`)
    } catch (err) {
      console.error('Failed to leave group:', err)
    }
  }

  const acceptIncomingCall = () => chatCalls.acceptCall()
  const rejectIncomingCall = () => chatCalls.rejectCall(
    currentUserUuid.value,
    isMqttConnected.value,
    publishMqtt
  )

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
    rejectIncomingCall
  }
})

export default useConversationsStore
