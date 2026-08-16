import { graphql } from '@/gql'

export const GET_INBOX = graphql(`
  query GetInbox($userId: ID!) {
    getInbox(userId: $userId) {
      conversationId
      lastActivity
      lastMessageText
      isUnread
      recipientId
      lastMessageSenderId
    }
  }
`)

export const MARK_INBOX_AS_READ = graphql(`
  mutation MarkInboxAsRead($userId: ID!, $conversationId: ID!) {
    markInboxAsRead(userId: $userId, conversationId: $conversationId)
  }
`)
