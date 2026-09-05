export type ListingCondition = "NEW" | "USED_LIKE_NEW" | "USED_GOOD" | "USED_FAIR" | "NEW_WITH_TAGS"
export type ListingCategory = "VEHICLES" | "PROPERTY_RENTALS" | "APPAREL" | "ELECTRONICS" | "ENTERTAINMENT" | "FAMILY" | "FREE" | "GARDEN" | "HOBBIES" | "HOME_GOODS" | "HOME_SALES" | "MUSICAL_INSTRUMENTS" | "OFFICE_SUPPLIES" | "PET_SUPPLIES" | "SPORTING_GOODS" | "TOYS" | "OTHER"

export interface MarketplaceListing {
  id: string | number
  title: string
  price: number
  category: string
  condition: string
  description?: string
  latitude: number
  longitude: number
  createdAt?: string
  images?: string[]
  location?: string
  subInfo?: string
  isFree?: boolean
  seller?: {
    name: string
    avatar: string
    memberSince?: string
  }
}

export interface CreateListingInput {
  title: string
  price: number
  category: string
  condition: string
  description?: string
  latitude: number
  longitude: number
}
