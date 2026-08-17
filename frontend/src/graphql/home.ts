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
        avatar
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
      authorGroupRole
      allowedUserIds
      taggedUsers {
        id
        firstName
        lastName
      }
      media {
        src
        altText
        backgroundColor
        tags {
          id
          x
          y
          userId
          user {
            id
            firstName
            lastName
          }
        }
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
      context {
        feeling {
          emoji
          label
        }
        location {
          title
          subtitle
          type
          lat
          lon
        }
        poll {
          question
          options {
            id
            text
            votes
          }
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
          avatar
        }
        content
        date
        timestamp
        isAnonymous
        taggedUsers {
          id
          firstName
          lastName
        }
        media {
          src
          altText
          backgroundColor
          tags {
            id
            x
            y
            userId
            user {
              id
              firstName
              lastName
            }
          }
        }
        context {
          feeling {
            emoji
            label
          }
          location {
            title
            subtitle
            type
            lat
            lon
          }
          poll {
            question
            options {
              id
              text
              votes
            }
          }
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
        avatar
      }
      mediaUrl
      thumbMediaUrl
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
      avatar
    }
  }
`)
