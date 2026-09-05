import gql from 'graphql-tag'

export const GET_INITIAL_SHELL_DATA = gql`
  query GetInitialShellData($currentUserId: ID!) {
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
`

export const GET_FEED_POSTS = gql`
  query GetFeedPosts($currentUserId: ID!, $limit: Int!, $offset: Int!) {
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
        }
      }
      reactions {
        reactionType
        userIds
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
  }
`

export const GET_HOME_DATA = gql`
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
        }
      }
      reactions {
        reactionType
        userIds
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
`
