import gql from 'graphql-tag'

export const GET_INBOX = gql`
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
`

export const MARK_INBOX_AS_READ = gql`
  mutation MarkInboxAsRead($userId: ID!, $conversationId: ID!) {
    markInboxAsRead(userId: $userId, conversationId: $conversationId)
  }
`
