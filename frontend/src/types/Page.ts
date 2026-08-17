export interface Page {
  id: string
  ownerId?: string
  name: string
  category: string
  bio?: string
  website?: string
  phoneCode?: string
  phone?: string
  email?: string
  address?: string
  city?: string
  zip?: string
  hours?: 'none' | 'always' | 'selected' | string
  avatar?: string
  cover?: string
  pageNotifications?: boolean
  promotionalEmails?: boolean
  createdAt?: string
  updatedAt?: string
}

export interface CreatePagePayload {
  ownerId?: string
  pageName: string
  name?: string
  category: string
  bio?: string
  website?: string
  phoneCode?: string
  phone?: string
  email?: string
  address?: string
  city?: string
  zip?: string
  hours?: 'none' | 'always' | 'selected' | string
  profileImage?: string | null
  coverImage?: string | null
  pageNotifications?: boolean
  promotionalEmails?: boolean
}
