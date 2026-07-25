export interface Group {
  id: string
  name: string
  image: string
  description?: string
  members?: number
  privacy?: 'public' | 'private'
  images?: string[]
  lastActive?: string
}
