import { apiClient } from './client'
import { CREATE_EVENT, GET_EVENT_BY_ID, GET_EVENTS, SEARCH_EVENTS } from '@/graphql/events'
import type { Event } from '@/types/Event'

export const eventsApi = {
  async getEvents(): Promise<Event[]> {
    const data = await apiClient.query<{ getEvents: Event[] }>(GET_EVENTS, undefined, {
      fetchPolicy: 'network-only'
    })
    return data?.getEvents || []
  },

  async getEventById(id: string): Promise<Event | null> {
    const data = await apiClient.query<{ getEventById: Event }>(GET_EVENT_BY_ID, { id })
    return data?.getEventById || null
  },

  async createEvent(input: any): Promise<Event | null> {
    const data = await apiClient.mutate<{ createEvent: Event }>(CREATE_EVENT, { input })
    return data?.createEvent || null
  },

  async searchEvents(query: string): Promise<Event[]> {
    if (!query) return []
    const data = await apiClient.query<{ searchEvents: Event[] }>(SEARCH_EVENTS, { query }, {
      fetchPolicy: 'network-only'
    })
    return data?.searchEvents || []
  }
}
