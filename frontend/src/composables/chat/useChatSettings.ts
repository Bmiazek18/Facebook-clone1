import { ref } from 'vue'
import { useChatStore } from '@/stores/chat'
import { chatApi } from '@/api/chat'

export interface ChatSettings {
  chatId: string | number
  themeId: number
  emoji: string
  mutedUntil?: number
  activeEffects?: string[]
}

// Shared across all callers (composable must not create a fresh ref each time)
const settings = ref<ChatSettings[]>([])

export function useChatSettings() {
  function _getOrCreateSettings(chatId: string | number) {
    let s = settings.value.find((x) => String(x.chatId) === String(chatId))
    if (!s) {
      s = { chatId: chatId as any, themeId: 0, emoji: '👍', activeEffects: [] }
      settings.value.push(s)
    }
    return s
  }

  function muteChat(chatId: string | number, duration: string) {
    const s = _getOrCreateSettings(chatId)
    let durationMs = 0
    if (duration === '15m') durationMs = 15 * 60 * 1000
    else if (duration === '1h') durationMs = 60 * 60 * 1000
    else if (duration === '8h') durationMs = 8 * 60 * 60 * 1000
    else if (duration === '24h') durationMs = 24 * 60 * 60 * 1000
    else if (duration === 'forever') durationMs = 100 * 365 * 24 * 60 * 60 * 1000

    s.mutedUntil = Date.now() + durationMs

    const chatStore = useChatStore()
    chatStore.removeMessageBox(chatId)
  }

  function isChatMuted(chatId: string | number) {
    const s = settings.value.find((x) => String(x.chatId) === String(chatId))
    if (!s || !s.mutedUntil) return false
    return s.mutedUntil > Date.now()
  }

  function saveCustomization(apiUrl: string, headers: Record<string, string>, conversationId: string, emoji?: string, themeId?: number, participantIds?: string[]) {
    const senderId = String(headers['X-User-Id'] || headers['x-user-id'] || '').replace('user_', '')
    const cleanConversationId = String(conversationId).replace('user_', '')
    const cleanParticipantIds = participantIds ? participantIds.map(pid => String(pid).replace('user_', '')) : []

    return chatApi.updateCustomization({
      senderId,
      conversationId: cleanConversationId,
      themeId: themeId !== undefined ? themeId : null,
      emoji: emoji || null,
      participantIds: cleanParticipantIds
    }).catch(err => console.error('Failed to save customization via GraphQL:', err))
  }

  function saveNickname(apiUrl: string, headers: Record<string, string>, conversationId: string, userId: string, nickname: string, participantIds?: string[]) {
    const senderId = String(headers['X-User-Id'] || headers['x-user-id'] || '').replace('user_', '')
    const cleanConversationId = String(conversationId).replace('user_', '')
    const cleanUserId = String(userId).replace('user_', '')
    const cleanParticipantIds = participantIds ? participantIds.map(pid => String(pid).replace('user_', '')) : []

    return chatApi.updateNickname({
      senderId,
      conversationId: cleanConversationId,
      userId: cleanUserId,
      nickname: nickname || "",
      participantIds: cleanParticipantIds
    }).catch(err => console.error('Failed to save nickname via GraphQL:', err))
  }

  function saveGroupNicknames(apiUrl: string, headers: Record<string, string>, conversationId: string, nicknames: Record<string, string>, participantIds?: string[]) {
    const promises = Object.entries(nicknames).map(([userId, nickname]) => {
      return saveNickname(apiUrl, headers, conversationId, userId, nickname, participantIds)
    })
    return Promise.all(promises)
  }

  return {
    settings,
    _getOrCreateSettings,
    muteChat,
    isChatMuted,
    saveCustomization,
    saveNickname,
    saveGroupNicknames
  }
}
