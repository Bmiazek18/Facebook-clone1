export type ViewMode = 'desktop' | 'mobile'

export interface PageForm {
  pageName: string
  category: string
  bio: string
  website: string
  phoneCode: string
  phone: string
  email: string
  address: string
  city: string
  zip: string
  hours: 'none' | 'always' | 'selected'
  profileImage: string | null
  coverImage: string | null
  pageNotifications: boolean
  promotionalEmails: boolean
}
