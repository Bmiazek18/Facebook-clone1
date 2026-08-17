export enum ChatType {
  Private = 'private',
  Group = 'group',
}

export interface GroupMember {
  id: string | number
  name: string
  nickname?: string
  addedByUserId?: string | number
  avatarUrl?: string
}

export interface Chat {
  id: string | number
  name: string
  avatarUrl: string
  lastMessage: string
  timeAgo: string
  unread: boolean
  isActive: boolean
  isPinch?: boolean
  isMeta?: boolean
  type: ChatType
  groupMembers?: GroupMember[]
  extraAvatars?: string[]
  otherUserNickname?: string
}

export interface ChatSetting {
  chatId: number
  themeId?: number
  emoji?: string
  title?: string
}
