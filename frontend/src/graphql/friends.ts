import gql from 'graphql-tag'

export const GET_FRIENDS = gql`
  query GetFriends($userId: ID!) {
    getFriends(userId: $userId) {
      id
      firstName
      lastName
      avatar
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
