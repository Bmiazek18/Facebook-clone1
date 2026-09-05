import gql from 'graphql-tag'

export const GET_GROUPS = gql`
  query GetGroups($limit: Int, $offset: Int) {
    getGroups(limit: $limit, offset: $offset) {
      id
      name
      description
      privacy
      image
      membersCount
      lastActive
      newPostsToday
      newPostsMonth
      newMembersWeek
      createdAge
    }
  }
`

export const GET_GROUP_BY_ID = gql`
  query GetGroupById($id: ID!) {
    getGroupById(id: $id) {
      id
      name
      description
      privacy
      image
      membersCount
      lastActive
      newPostsToday
      newPostsMonth
      newMembersWeek
      createdAge
    }
  }
`

export const GET_GROUP_OVERVIEW = gql`
  query GetGroupOverview($groupId: ID!) {
    getGroupOverview(groupId: $groupId) {
      groupId
      reportedItemsCount
      moderationAlertsCount
      pendingPostsCount
      pendingRequestsCount
      groupStatusViolationCount
      postsCount7Days
      commentsCount7Days
      reactionsCount7Days
      postsTrend
      commentsTrend
      reactionsTrend
      activeMembersChart
      chartCategories
    }
  }
`

export const CREATE_GROUP = gql`
  mutation CreateGroup($input: CreateGroupInput!) {
    createGroup(input: $input) {
      id
      name
      description
      privacy
      image
      membersCount
      lastActive
      newPostsToday
      newPostsMonth
      newMembersWeek
      createdAge
    }
  }
`

export const JOIN_GROUP = gql`
  mutation JoinGroup($groupId: ID!, $userId: ID!) {
    joinGroup(groupId: $groupId, userId: $userId)
  }
`

export const LEAVE_GROUP = gql`
  mutation LeaveGroup($groupId: ID!, $userId: ID!) {
    leaveGroup(groupId: $groupId, userId: $userId)
  }
`

export const GET_GROUP_MEMBERSHIP = gql`
  query GetGroupMembership($groupId: ID!, $userId: ID!) {
    getGroupMembership(groupId: $groupId, userId: $userId)
  }
`

export const GET_PENDING_REQUESTS = gql`
  query GetPendingRequests($groupId: ID!) {
    getPendingRequests(groupId: $groupId)
  }
`

export const GET_GROUP_MEMBERS = gql`
  query GetGroupMembers($groupId: ID!) {
    getGroupMembers(groupId: $groupId) {
      userId
      role
      joinedAt
      isFriend
    }
  }
`

export const APPROVE_GROUP_REQUEST = gql`
  mutation ApproveGroupRequest($groupId: ID!, $userId: ID!, $adminId: ID!) {
    approveGroupRequest(groupId: $groupId, userId: $userId, adminId: $adminId)
  }
`

export const REJECT_GROUP_REQUEST = gql`
  mutation RejectGroupRequest($groupId: ID!, $userId: ID!, $adminId: ID!) {
    rejectGroupRequest(groupId: $groupId, userId: $userId, adminId: $adminId)
  }
`

export const REMOVE_GROUP_MEMBER = gql`
  mutation RemoveGroupMember($groupId: ID!, $userId: ID!, $adminId: ID!) {
    removeGroupMember(groupId: $groupId, userId: $userId, adminId: $adminId)
  }
`

export const UPDATE_GROUP_MEMBER_ROLE = gql`
  mutation UpdateGroupMemberRole($groupId: ID!, $userId: ID!, $role: GroupRole!, $adminId: ID!) {
    updateGroupMemberRole(groupId: $groupId, userId: $userId, role: $role, adminId: $adminId)
  }
`

export const GET_GROUP_RULES = gql`
  query GetGroupRules($groupId: ID!) {
    getGroupRules(groupId: $groupId) {
      id
      title
      description
      orderIndex
    }
  }
`

export const CREATE_GROUP_RULE = gql`
  mutation CreateGroupRule($groupId: ID!, $title: String!, $description: String!) {
    createGroupRule(groupId: $groupId, title: $title, description: $description) {
      id
      title
      description
      orderIndex
    }
  }
`

export const UPDATE_GROUP_RULES_ORDER = gql`
  mutation UpdateGroupRulesOrder($groupId: ID!, $ruleIds: [ID!]!) {
    updateGroupRulesOrder(groupId: $groupId, ruleIds: $ruleIds)
  }
`

export const DELETE_GROUP_RULE = gql`
  mutation DeleteGroupRule($ruleId: ID!) {
    deleteGroupRule(ruleId: $ruleId)
  }
`

export const GET_GROUP_ACTIVITY_LOGS = gql`
  query GetGroupActivityLogs($groupId: ID!) {
    getGroupActivityLogs(groupId: $groupId) {
      id
      groupId
      actorId
      actorName
      text
      note
      time
      date
    }
  }
`

export const LOG_GROUP_ACTIVITY = gql`
  mutation LogGroupActivity($groupId: ID!, $text: String!, $note: String, $actorId: ID!, $actorName: String!) {
    logGroupActivity(groupId: $groupId, text: $text, note: $note, actorId: $actorId, actorName: $actorName) {
      id
      groupId
      actorId
      actorName
      text
      note
      time
      date
    }
  }
`

export const GET_GROUP_FEED = gql`
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
`

export const VOTE_ON_POLL_MUTATION = gql`
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
`
