import { apiClient } from './client'
import gql from 'graphql-tag'
import { GET_INBOX, MARK_INBOX_AS_READ } from '@/graphql/chat'

export const GET_CHAT_WITH_USER_QUERY = gql`
  query GetChatWithUser($userId: ID!, $conversationId: ID!) {
    getChatWithUser(userId: $userId, conversationId: $conversationId) {
      user {
        id
        firstName
        lastName
        avatarId
        avatar
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
        systemActionType
        systemActionPayload
      }
      settings {
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
  }
`

export const GET_CHAT_SETTINGS_QUERY = gql`
  query GetChatSettings($conversationId: ID!) {
    getChatSettings(conversationId: $conversationId) {
      isGroup
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
    }
  }
`

export const SEND_CHAT_MESSAGE_MUTATION = gql`
  mutation SendChatMessage($input: SendChatMessageInput!) {
    sendChatMessage(input: $input) {
      messageId
    }
  }
`

export const REACT_TO_CHAT_MESSAGE_MUTATION = gql`
  mutation ReactToChatMessage($senderId: ID!, $conversationId: ID!, $messageId: ID!, $reactionEmoji: String!, $participantIds: [ID!]!) {
    reactToChatMessage(senderId: $senderId, conversationId: $conversationId, messageId: $messageId, reactionEmoji: $reactionEmoji, participantIds: $participantIds)
  }
`

export const UPDATE_CHAT_CUSTOMIZATION_MUTATION = gql`
  mutation UpdateChatCustomization($senderId: ID!, $conversationId: ID!, $themeId: Int, $emoji: String, $participantIds: [ID!]!) {
    updateChatCustomization(senderId: $senderId, conversationId: $conversationId, themeId: $themeId, emoji: $emoji, participantIds: $participantIds)
  }
`

export const UPDATE_CHAT_NICKNAME_MUTATION = gql`
  mutation UpdateChatNickname($senderId: ID!, $conversationId: ID!, $userId: ID!, $nickname: String, $participantIds: [ID!]!) {
    updateChatNickname(senderId: $senderId, conversationId: $conversationId, userId: $userId, nickname: $nickname, participantIds: $participantIds)
  }
`

export const PIN_CHAT_MESSAGE_MUTATION = gql`
  mutation PinChatMessage($conversationId: ID!, $messageId: ID!, $isPinned: Boolean!, $participantIds: [ID!]!) {
    pinChatMessage(conversationId: $conversationId, messageId: $messageId, isPinned: $isPinned, participantIds: $participantIds)
  }
`

export const LEAVE_CHAT_MUTATION = gql`
  mutation LeaveChat($userId: ID!, $conversationId: ID!) {
    leaveChat(userId: $userId, conversationId: $conversationId)
  }
`

export const chatApi = {
  async getInbox(userId: string | number) {
    try {
      const cleanUserId = String(userId).replace('user_', '')
      const data = await apiClient.query<{ getInbox: any[] }>(
        GET_INBOX,
        { userId: cleanUserId },
        { fetchPolicy: 'network-only', errorPolicy: 'all' }
      )
      return data?.getInbox || []
    } catch (err: any) {
      console.warn('[chatApi.getInbox] Chat service unavailable:', err?.message || err)
      return []
    }
  },

  async markInboxAsRead(userId: string | number, conversationId: string) {
    try {
      const cleanUserId = String(userId).replace('user_', '')
      const data = await apiClient.mutate<{ markInboxAsRead: boolean }>(
        MARK_INBOX_AS_READ,
        {
          userId: cleanUserId,
          conversationId: String(conversationId)
        },
        { errorPolicy: 'all' }
      )
      return data?.markInboxAsRead ?? false
    } catch (err: any) {
      console.warn('[chatApi.markInboxAsRead] Chat service unavailable:', err?.message || err)
      return false
    }
  },

  async getChatWithUser(userId: string, conversationId: string) {
    try {
      const data = await apiClient.query<{ getChatWithUser: any }>(
        GET_CHAT_WITH_USER_QUERY,
        { userId, conversationId },
        { fetchPolicy: 'network-only', errorPolicy: 'all' }
      )
      return data?.getChatWithUser || null
    } catch (err: any) {
      console.warn('[chatApi.getChatWithUser] Chat service unavailable:', err?.message || err)
      return null
    }
  },

  async getChatSettings(conversationId: string) {
    try {
      const data = await apiClient.query<{ getChatSettings: any }>(
        GET_CHAT_SETTINGS_QUERY,
        { conversationId: String(conversationId) },
        { fetchPolicy: 'network-only', errorPolicy: 'all' }
      )
      return data?.getChatSettings || null
    } catch (err: any) {
      console.warn('[chatApi.getChatSettings] Chat service unavailable:', err?.message || err)
      return null
    }
  },

  async sendMessage(input: any) {
    const data = await apiClient.mutate<{ sendChatMessage: { messageId: string } }>(
      SEND_CHAT_MESSAGE_MUTATION,
      { input }
    )
    return data?.sendChatMessage?.messageId
  },

  async reactToMessage(input: {
    senderId: string
    conversationId: string
    messageId: string
    reactionEmoji: string
    participantIds: string[]
  }) {
    const data = await apiClient.mutate<{ reactToChatMessage: boolean }>(
      REACT_TO_CHAT_MESSAGE_MUTATION,
      input
    )
    return data?.reactToChatMessage
  },

  async updateCustomization(input: {
    senderId: string
    conversationId: string
    themeId?: number | null
    emoji?: string | null
    participantIds: string[]
  }) {
    const data = await apiClient.mutate<{ updateChatCustomization: any }>(
      UPDATE_CHAT_CUSTOMIZATION_MUTATION,
      {
        senderId: input.senderId,
        conversationId: input.conversationId,
        themeId: input.themeId !== undefined ? input.themeId : null,
        emoji: input.emoji || null,
        participantIds: input.participantIds
      }
    )
    return data?.updateChatCustomization
  },

  async updateNickname(input: {
    senderId: string
    conversationId: string
    userId: string
    nickname: string
    participantIds: string[]
  }) {
    const data = await apiClient.mutate<{ updateChatNickname: any }>(
      UPDATE_CHAT_NICKNAME_MUTATION,
      {
        senderId: input.senderId,
        conversationId: input.conversationId,
        userId: input.userId,
        nickname: input.nickname || '',
        participantIds: input.participantIds
      }
    )
    return data?.updateChatNickname
  },

  async pinMessage(input: {
    conversationId: string
    messageId: string
    isPinned: boolean
    participantIds: string[]
  }) {
    const data = await apiClient.mutate<{ pinChatMessage: boolean }>(
      PIN_CHAT_MESSAGE_MUTATION,
      input
    )
    return data?.pinChatMessage
  },

  async leaveChat(userId: string, conversationId: string) {
    const data = await apiClient.mutate<{ leaveChat: boolean }>(
      LEAVE_CHAT_MUTATION,
      { userId, conversationId }
    )
    return data?.leaveChat
  }
}
