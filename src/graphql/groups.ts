import { graphql } from '@/gql'

export const GET_GROUPS = graphql(`
  query GetGroups($limit: Int, $offset: Int) {
    getGroups(limit: $limit, offset: $offset) {
      id
      name
      description
      privacy
      image
      membersCount
      lastActive
    }
  }
`)

export const GET_GROUP_BY_ID = graphql(`
  query GetGroupById($id: ID!) {
    getGroupById(id: $id) {
      id
      name
      description
      privacy
      image
      membersCount
      lastActive
    }
  }
`)

export const CREATE_GROUP = graphql(`
  mutation CreateGroup($input: CreateGroupInput!) {
    createGroup(input: $input) {
      id
      name
      description
      privacy
      image
      membersCount
      lastActive
    }
  }
`)

export const JOIN_GROUP = graphql(`
  mutation JoinGroup($groupId: ID!, $userId: ID!) {
    joinGroup(groupId: $groupId, userId: $userId)
  }
`)

export const LEAVE_GROUP = graphql(`
  mutation LeaveGroup($groupId: ID!, $userId: ID!) {
    leaveGroup(groupId: $groupId, userId: $userId)
  }
`)

export const GET_GROUP_FEED = graphql(`
  query GetGroupFeed($groupId: ID!, $limit: Int, $offset: Int) {
    getGroupFeed(groupId: $groupId, limit: $limit, offset: $offset) {
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
    }
  }
`)

export const VOTE_ON_POLL_MUTATION = graphql(`
  mutation VoteOnPoll($postId: ID!, $optionId: ID!, $userId: ID!) {
    voteOnPoll(postId: $postId, optionId: $optionId, userId: $userId) {
      id
      content
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
`)
