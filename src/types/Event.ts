export interface Event {
  id: string
  userId: string
  name: string
  title?: string
  startDate: string
  startTime?: string
  endDate?: string
  endTime?: string
  type: 'online' | 'offline'
  privacy: 'public' | 'private' | 'group'
  description?: string
  images: string[]
  location?: string
  locationName?: string
  address?: string
  showGuestList?: boolean
  hosts?: string[]
  date?: string
  responses?: number
  guestsGoing?: number
  guestsInterested?: number
  coordinates?: number[]
  frequency?: string
}
