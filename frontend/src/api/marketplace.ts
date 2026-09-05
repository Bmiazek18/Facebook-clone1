import { apiClient } from './client'
import gql from 'graphql-tag'

export const GET_NEARBY_LISTINGS_QUERY = gql`
  query GetNearbyListings($lat: Float!, $lon: Float!, $radius: Float, $query: String) {
    getNearbyListings(lat: $lat, lon: $lon, radius: $radius, query: $query) {
      id
      title
      price
      category
      condition
      description
      latitude
      longitude
      createdAt
    }
  }
`

export const GET_LISTING_QUERY = gql`
  query GetListing($id: ID!) {
    getListing(id: $id) {
      id
      title
      price
      category
      condition
      description
      latitude
      longitude
      createdAt
    }
  }
`

export const CREATE_LISTING_MUTATION = gql`
  mutation CreateListing($input: CreateListingInput!) {
    createListing(input: $input) {
      id
      title
      price
      category
      condition
      description
      latitude
      longitude
      createdAt
    }
  }
`

export const marketplaceApi = {
  async getNearbyListings(lat: number, lon: number, radius = 50, query?: string) {
    const data = await apiClient.query<{ getNearbyListings: any[] }>(
      GET_NEARBY_LISTINGS_QUERY,
      { lat, lon, radius, query: query || null },
      { fetchPolicy: 'network-only' }
    )
    return data?.getNearbyListings || []
  },

  async getListing(id: string | number) {
    const data = await apiClient.query<{ getListing: any }>(
      GET_LISTING_QUERY,
      { id: String(id) },
      { fetchPolicy: 'network-only' }
    )
    return data?.getListing || null
  },

  async createListing(input: {
    title: string
    price: number
    category: string
    condition: string
    description?: string
    latitude: number
    longitude: number
  }) {
    const data = await apiClient.mutate<{ createListing: any }>(
      CREATE_LISTING_MUTATION,
      { input }
    )
    return data?.createListing || null
  }
}
