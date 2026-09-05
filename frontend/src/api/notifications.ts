import { apiClient } from './client'
import gql from 'graphql-tag'
import type { Notification } from '@/types/Notification'

export const GET_NOTIFICATIONS_QUERY = gql`
  query GetNotifications($userId: ID!) {
    getNotifications(userId: $userId) {
      id
      userId
      title
      message
      createdAt
      read
      sender {
        id
        firstName
        lastName
        avatarId
        avatar
      }
    }
  }
`

export const MARK_NOTIFICATION_AS_READ_MUTATION = gql`
  mutation MarkNotificationAsRead($id: ID!) {
    markNotificationAsRead(id: $id)
  }
`

export const notificationsApi = {
  async getNotifications(userId: string | number): Promise<Notification[]> {
    const data = await apiClient.query<{ getNotifications: Notification[] }>(
      GET_NOTIFICATIONS_QUERY,
      { userId: String(userId) },
      { fetchPolicy: 'network-only' }
    )
    return (data?.getNotifications || []).map((n) => ({ ...n }))
  },

  async markAsRead(id: string | number): Promise<boolean> {
    const data = await apiClient.mutate<{ markNotificationAsRead: boolean }>(
      MARK_NOTIFICATION_AS_READ_MUTATION,
      { id: String(id) }
    )
    return !!data?.markNotificationAsRead
  }
}
