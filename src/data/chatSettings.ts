
export interface ChatSetting {
  chatId: number;
  themeId?: number; // index or id of theme in messenger themes
  emoji?: string; // default emoji for this chat
  title?: string; // optional override title
}

// minimal settings for some chats
const chatSettings: ChatSetting[] = [
  { chatId: 1, themeId: 1, emoji: '😄' },
  { chatId: 2, themeId: 0, emoji: '🤠' },
  { chatId: 3, themeId: 2, emoji: '🎧' },
  { chatId: 4, themeId: 3, emoji: '📊' },
  { chatId: 5, themeId: 4, emoji: '📁' },
  { chatId: 6, themeId: 0, emoji: '📸' },
  { chatId: 7, themeId: 2, emoji: '⚽' },
  { chatId: 8, themeId: 1, emoji: '🔥' },
  { chatId: 9, themeId: 0, emoji: '😂' },
  { chatId: 10, themeId: 2, emoji: '📝' },
  { chatId: 11, themeId: 1, emoji: '🙌' },
  { chatId: 12, themeId: 3, emoji: '🍕' },
  { chatId: 13, themeId: 0, emoji: '👋' },
];

export default chatSettings;
