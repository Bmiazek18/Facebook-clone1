import { defineStore } from 'pinia'
import { ref } from 'vue'
import { useApolloClient } from '@vue/apollo-composable'
import { usePostsStore } from '@/composables/feed/useAppState'
import type { Post } from '@/types/Post'
import type { Event } from '@/types/Event'
import { CREATE_EVENT, GET_EVENT_BY_ID, GET_EVENTS, SEARCH_EVENTS } from '@/graphql/events'
import { useAuthStore } from '@/stores/auth'

export const useEventsStore = defineStore('events', () => {
  const events = ref<Event[]>([])
  const loading = ref(false)
  const searchQuery = ref('')
  const searchResults = ref<Event[]>([])

  const apollo = useApolloClient()

  // Load all events on initialization or on call
  const fetchEvents = async () => {
    loading.value = true
    try {
      const client = apollo.resolveClient()
      const { data } = await client.query({
        query: GET_EVENTS,
        fetchPolicy: 'network-only'
      })
      events.value = (data?.getEvents || []) as Event[]
    } catch (err) {
      console.error('Failed to fetch events:', err)
    } finally {
      loading.value = false
    }
  }

  const addEvent = async (eventInput: Event) => {
    try {
      const auth = useAuthStore()
      const client = apollo.resolveClient()
      const { data } = await client.mutate({
        mutation: CREATE_EVENT,
        variables: {
          input: {
            id: eventInput.id,
            userId: String(auth.currentUser?.id || '1'),
            name: eventInput.name,
            title: eventInput.title || eventInput.name,
            startDate: eventInput.startDate,
            startTime: eventInput.startTime || '',
            endDate: eventInput.endDate || '',
            endTime: eventInput.endTime || '',
            type: eventInput.type,
            privacy: eventInput.privacy,
            description: eventInput.description || '',
            images: eventInput.images || [],
            location: eventInput.location || '',
            locationName: eventInput.locationName || eventInput.location || '',
            address: eventInput.address || '',
            showGuestList: eventInput.showGuestList !== undefined ? eventInput.showGuestList : true,
            hosts: eventInput.hosts || [],
            date: eventInput.date || '',
            coordinates: eventInput.coordinates || [],
            frequency: eventInput.frequency || 'Nigdy'
          }
        }
      })

      if (data?.createEvent) {
        const newEvent = data.createEvent as Event
        events.value.push(newEvent)

        // Automatycznie twórz post z wydarzeniem
        const postsStore = usePostsStore()
        const currentUser = postsStore.currentUser

        const newPost: Post = {
          id: `${Date.now()}-event`,
          content: '', // Pusty content, bo całe info jest w evencie
          images: [],
          videoUrl: undefined,
          authorName: currentUser.name,
          authorAvatar: currentUser.avatar,
          authorId: currentUser.id,
          date: new Date().toISOString(),
          likesCount: 0,
          commentsCount: 0,
          sharesCount: 0,
          taggedUsers: [],
          location: undefined,
          gif: undefined,
          isLiked: false,
          likedType: null,
          reactionCount: 0,
          commentCount: 0,
          comments: [],
          selectedCardBgId: 0,
          privacy: (newEvent.privacy === 'public' ? 'public' : 'private'),
          timestamp: Date.now(),
          sharedEventId: newEvent.id,
          createdEvent: true, // Flaga oznaczająca że to post o utworzeniu eventu
        }

        postsStore.addPost(newPost)
        return newEvent
      }
    } catch (err) {
      console.error('Failed to create event:', err)
    }
  }

  const getEventById = (id: string): Event | undefined => {
    // If we have it in cache, return it
    const local = events.value.find((e) => e.id === id)
    if (local) return local
    
    // Otherwise fetch it asynchronously from the backend
    fetchEventById(id)
    return undefined
  }

  const fetchEventById = async (id: string) => {
    try {
      const client = apollo.resolveClient()
      const { data } = await client.query({
        query: GET_EVENT_BY_ID,
        variables: { id }
      })
      if (data?.getEventById) {
        const found = data.getEventById as Event
        const index = events.value.findIndex((e) => e.id === id)
        if (index !== -1) {
          events.value[index] = found
        } else {
          events.value.push(found)
        }
      }
    } catch (err) {
      console.error('Failed to fetch event by id:', err)
    }
  }

  const searchEvents = async (query: string) => {
    if (!query) {
      searchResults.value = []
      return
    }
    try {
      const client = apollo.resolveClient()
      const { data } = await client.query({
        query: SEARCH_EVENTS,
        variables: { query },
        fetchPolicy: 'network-only'
      })
      searchResults.value = (data?.searchEvents || []) as Event[]
    } catch (err) {
      console.error('Failed to search events:', err)
    }
  }

  const getEventsByUserId = (userId: string): Event[] => {
    return events.value.filter((event) => event.userId === userId)
  }

  // Load initial events
  fetchEvents()

  return {
    events,
    loading,
    searchQuery,
    searchResults,
    fetchEvents,
    addEvent,
    getEventById,
    fetchEventById,
    getEventsByUserId,
    searchEvents,
  }
})
