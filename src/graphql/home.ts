import { graphql } from '@/gql'

export const GET_HOME_DATA = graphql(`
  query GetHomeData($currentUserId: ID!, $limit: Int!, $offset: Int!) {
    getFeed(currentUserId: $currentUserId, limit: $limit, offset: $offset) {
      id
      authorId
      author {
        id
        firstName
        lastName
        avatarId
      }
      content
      date
      timestamp
      isAnonymous
      targetId
      targetType
      commentCount
      shareCount
      visibility
      allowedUserIds
      media {
        src
        altText
      }
      reactions {
        reactionType
        userIds
        users {
          id
          firstName
          lastName
        }
      }
      sharedPost {
        id
        authorId
        author {
          id
          firstName
          lastName
          avatarId
        }
        content
        date
        timestamp
        isAnonymous
        media {
          src
          altText
        }
      }
    }
    getActiveStories(currentUserId: $currentUserId) {
      id
      authorId
      author {
        id
        firstName
        lastName
        avatarId
      }
      mediaUrl
      mediaType
      text
      createdAt
      expiresAt
    }
    getBirthdayUsers(currentUserId: $currentUserId) {
      userId
      birthDate
      user {
        id
        firstName
        lastName
      }
    }
    getFriends(userId: $currentUserId) {
      id
      firstName
      lastName
      avatarId
    }
  }
`)
