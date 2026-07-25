import { ref } from 'vue'
import { useChatStore } from '@/stores/chat'

export interface ChatSettings {
  chatId: string | number
  themeId: number
  emoji: string
  mutedUntil?: number
  activeEffects?: string[]
}

export function useChatSettings() {
  const settings = ref<ChatSettings[]>([])

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

  function saveCustomization(apiUrl: string, headers: Record<string, string>, conversationId: string, emoji?: string, themeId?: number) {
    const query: Record<string, any> = { conversationId }
    if (emoji !== undefined) query.emoji = emoji
    if (themeId !== undefined) query.themeId = themeId

    return $fetch(`${apiUrl}/api/chat/settings/customization`, {
      method: 'POST',
      headers,
      query
    }).catch(err => console.error('Failed to save customization to ScyllaDB:', err))
  }

  function saveNickname(apiUrl: string, headers: Record<string, string>, conversationId: string, userId: string, nickname: string) {
    return $fetch(`${apiUrl}/api/chat/settings/nickname`, {
      method: 'POST',
      headers,
      query: {
        conversationId,
        userId,
        nickname
      }
    }).catch(err => console.error('Failed to save nickname to ScyllaDB:', err))
  }

  function saveGroupNicknames(apiUrl: string, headers: Record<string, string>, conversationId: string, nicknames: Record<string, string>) {
    const formData = new URLSearchParams()
    formData.append('conversationId', conversationId)
    Object.entries(nicknames).forEach(([uId, nick]) => {
      formData.append(`nicknames[${uId}]`, nick)
    })

    return $fetch(`${apiUrl}/api/chat/settings/nickname`, {
      method: 'POST',
      headers,
      body: formData
    }).catch(err => console.error('Failed to save group nicknames to ScyllaDB:', err))
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
