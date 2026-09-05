import gql from 'graphql-tag'

export const GET_FRIENDS = gql`
  query GetFriends($userId: ID!, $filterType: String, $limit: Int, $offset: Int) {
    getFriends(userId: $userId, filterType: $filterType, limit: $limit, offset: $offset) {
      id
      firstName
      lastName
      avatar
      birthDate
      gender
      city
      location
      hometown
      school
      highSchool
      work
      job
      company
    }
  }
`

export const GET_FRIEND_SUGGESTIONS = gql`
  query GetFriendSuggestions($currentUserId: ID!) {
    getFriendSuggestions(currentUserId: $currentUserId) {
      userId
      user {
        id
        firstName
        lastName
        avatar
      }
    }
  }
`
