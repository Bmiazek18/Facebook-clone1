import gql from 'graphql-tag'

export const CREATE_EVENT = gql`
  mutation CreateEvent($input: CreateEventInput!) {
    createEvent(input: $input) {
      id
      userId
      name
      title
      startDate
      startTime
      endDate
      endTime
      type
      privacy
      description
      images
      location
      locationName
      address
      showGuestList
      hosts
      date
      responses
      guestsGoing
      guestsInterested
      coordinates
      frequency
    }
  }
`

export const GET_EVENT_BY_ID = gql`
  query GetEventById($id: ID!) {
    getEventById(id: $id) {
      id
      userId
      name
      title
      startDate
      startTime
      endDate
      endTime
      type
      privacy
      description
      images
      location
      locationName
      address
      showGuestList
      hosts
      date
      responses
      guestsGoing
      guestsInterested
      coordinates
      frequency
    }
  }
`

export const GET_EVENTS = gql`
  query GetEvents($limit: Int, $offset: Int) {
    getEvents(limit: $limit, offset: $offset) {
      id
      userId
      name
      title
      startDate
      startTime
      endDate
      endTime
      type
      privacy
      description
      images
      location
      locationName
      address
      showGuestList
      hosts
      date
      responses
      guestsGoing
      guestsInterested
      coordinates
      frequency
    }
  }
`

export const SEARCH_EVENTS = gql`
  query SearchEvents($query: String!) {
    searchEvents(query: $query) {
      id
      userId
      name
      title
      startDate
      startTime
      endDate
      endTime
      type
      privacy
      description
      images
      location
      locationName
      address
      showGuestList
      hosts
      date
      responses
      guestsGoing
      guestsInterested
      coordinates
      frequency
    }
  }
`
