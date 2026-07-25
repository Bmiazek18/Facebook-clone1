export interface Notification {
  id: string
  userId: string
  title: string
  message: string
  createdAt: string
  read: boolean
  sender?: {
    id: string
    firstName: string
    lastName: string
    avatarId?: string
  }
}
