import { defineStore } from 'pinia'
import { CHAT_THEMES, DEFAULT_CHAT_THEME, getChatThemeById } from '@/constants/chatThemes'
import type { Theme } from '@/types/Theme'

export { CHAT_THEMES, DEFAULT_CHAT_THEME, getChatThemeById }

export const useChatThemeStore = defineStore('chatTheme', {
  state: () => ({
    themes: CHAT_THEMES,
    selectedThemeId: 'winter' as string,
    selectedEmoji: '👍' as string,
  }),
  getters: {
    selectedTheme: (state) => getChatThemeById(state.selectedThemeId),
  },
  actions: {
    setSelectedTheme(id: string) {
      this.selectedThemeId = id
    },
    setSelectedEmoji(emoji: string) {
      this.selectedEmoji = emoji
    },
  },
})

export default useChatThemeStore
