import gql from 'graphql-tag'

export const GET_SEARCH_HISTORY = gql`
  query GetSearchHistory($userId: ID) {
    getSearchHistory(userId: $userId) {
      id
      firstName
      lastName
      avatarId
      avatar
      inHistory
      newPostsCount
    }
  }
`

export const SEARCH_USERS = gql`
  query SearchUsers($query: String!, $currentUserId: ID!) {
    searchUsers(query: $query, currentUserId: $currentUserId) {
      id
      firstName
      lastName
      avatar
      newPostsCount
    }
  }
`

export const RECORD_SEARCH = gql`
  mutation RecordSearch($searchedUserId: ID!, $searchingUserId: ID) {
    recordSearch(searchedUserId: $searchedUserId, searchingUserId: $searchingUserId)
  }
`

export const DELETE_SEARCH_HISTORY_ITEM = gql`
  mutation DeleteSearchHistoryItem($searchedUserId: ID!, $searchingUserId: ID) {
    deleteSearchHistoryItem(searchedUserId: $searchedUserId, searchingUserId: $searchingUserId)
  }
`
