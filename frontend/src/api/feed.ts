import { apiClient } from './client'
import gql from 'graphql-tag'
import { GET_HOME_DATA } from '@/graphql/home'

export const CREATE_POST_MUTATION = gql`
  mutation CreatePost($input: CreatePostInput!) {
    createPost(input: $input) {
      id
      authorId
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
        media {
          src
          altText
          backgroundColor
        }
      }
    }
  }
`

export const REACT_TO_POST_MUTATION = gql`
  mutation ReactToPost($input: PostReactionInput!) {
    reactToPost(input: $input)
  }
`

export const POST_REACTIONS_FRAGMENT = gql`
  fragment PostReactionsFragment on Post {
    id
    reactions {
      reactionType
      userIds
      users {
        id
        firstName
        lastName
        avatarId
        avatar
      }
    }
  }
`

export const VOTE_ON_POLL_MUTATION = gql`
  mutation VoteOnPoll($postId: ID!, $optionId: String!, $userId: ID!) {
    voteOnPoll(postId: $postId, optionId: $optionId, userId: $userId) {
      id
      context {
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

export const GET_COMMENTS_QUERY = gql`
  query GetComments($postId: ID!, $limit: Int) {
    comments(postId: $postId, limit: $limit) {
      id
      userId
      postId
      parentId
      content
      createdAt
      mediaUrl
      author {
        id
        firstName
        lastName
        avatar
        avatarId
      }
      reactions {
        reactionType
        userIds
      }
      mentionedUsers {
        id
        firstName
        lastName
        avatar
        avatarId
      }
    }
  }
`

export const ADD_COMMENT_MUTATION = gql`
  mutation AddComment($input: AddCommentInput!) {
    addComment(input: $input) {
      id
      userId
      postId
      parentId
      content
      createdAt
      mediaUrl
      author {
        id
        firstName
        lastName
        avatar
        avatarId
      }
      reactions {
        reactionType
        userIds
      }
      mentionedUsers {
        id
        firstName
        lastName
        avatar
        avatarId
      }
    }
  }
`

export const REACT_TO_COMMENT_MUTATION = gql`
  mutation ReactToComment($input: CommentReactionInput!) {
    reactToComment(input: $input)
  }
`

export const CREATE_STORY_MUTATION = gql`
  mutation CreateStory($input: CreateStoryInput!) {
    createStory(input: $input) {
      id
      authorId
      mediaUrl
      thumbMediaUrl
      mediaType
      text
      createdAt
      expiresAt
      author {
        id
        firstName
        lastName
        avatarId
        avatar
      }
    }
  }
`

export const GET_ACTIVE_STORIES_QUERY = gql`
  query GetActiveStories($currentUserId: ID!) {
    getActiveStories(currentUserId: $currentUserId) {
      id
      authorId
      mediaUrl
      thumbMediaUrl
      mediaType
      text
      createdAt
      expiresAt
      author {
        id
        firstName
        lastName
        avatarId
        avatar
      }
    }
  }
`

export const feedApi = {
  async getHomeData(currentUserId: string | number, limit = 10, offset = 0) {
    const data = await apiClient.query(GET_HOME_DATA, {
      currentUserId: String(currentUserId),
      limit,
      offset
    }, { fetchPolicy: 'network-only' })
    return data
  },

  async createPost(input: any, updateCallback?: (cache: any, result: any) => void) {
    const data = await apiClient.mutate<{ createPost: any }>(
      CREATE_POST_MUTATION,
      { input },
      { update: updateCallback }
    )
    return data?.createPost || null
  },

  async reactToPost(input: {
    postId: string
    userId: string
    reactionType: string | null
    previousReactionType?: string | null
  }) {
    const data = await apiClient.mutate<{ reactToPost: boolean }>(
      REACT_TO_POST_MUTATION,
      { input }
    )
    return data?.reactToPost
  },

  async voteOnPoll(postId: string, optionId: string, userId: string) {
    const data = await apiClient.mutate<{ voteOnPoll: any }>(
      VOTE_ON_POLL_MUTATION,
      { postId, optionId, userId }
    )
    return data?.voteOnPoll || null
  },

  async getComments(postId: string, limit?: number | null) {
    const data = await apiClient.query<{ comments: any[] }>(
      GET_COMMENTS_QUERY,
      { postId, limit: limit != null ? limit : null },
      { fetchPolicy: 'network-only' }
    )
    return data?.comments || []
  },

  async addComment(input: any) {
    const data = await apiClient.mutate<{ addComment: any }>(
      ADD_COMMENT_MUTATION,
      { input }
    )
    return data?.addComment || null
  },

  async reactToComment(input: { commentId: string; userId: string; reactionType: string | null }) {
    const data = await apiClient.mutate<{ reactToComment: boolean }>(
      REACT_TO_COMMENT_MUTATION,
      { input }
    )
    return data?.reactToComment
  },

  async createStory(input: { authorId: string; mediaUrl: string; mediaType: string; text: string }) {
    const data = await apiClient.mutate<{ createStory: any }>(
      CREATE_STORY_MUTATION,
      { input }
    )
    return data?.createStory || null
  },

  async getActiveStories(currentUserId: string | number) {
    const data = await apiClient.query<{ getActiveStories: any[] }>(
      GET_ACTIVE_STORIES_QUERY,
      { currentUserId: String(currentUserId) },
      { fetchPolicy: 'network-only' }
    )
    return data?.getActiveStories || []
  },

  async markStoryAsViewed(storyId: string | number, viewerId: string | number) {
    const data = await apiClient.mutate<{ markStoryAsViewed: boolean }>(
      gql`
        mutation MarkStoryAsViewed($storyId: ID!, $viewerId: ID!) {
          markStoryAsViewed(storyId: $storyId, viewerId: $viewerId)
        }
      `,
      {
        storyId: String(storyId),
        viewerId: String(viewerId)
      }
    )
    return data?.markStoryAsViewed
  },

  async getPostById(postId: string | number) {
    const data = await apiClient.query<{ getPostById: any }>(
      gql`
        query GetPostById($postId: ID!) {
          getPostById(postId: $postId) {
            id
            authorId
            author {
              id
              firstName
              lastName
              avatar
            }
            content
            date
            timestamp
            media {
              src
              altText
            }
          }
        }
      `,
      { postId: String(postId) },
      { fetchPolicy: 'cache-first' }
    )
    return data?.getPostById || null
  },

  async translateText(text: string, targetLanguage = 'pl') {
    const data = await apiClient.mutate<{ translateText: string }>(
      gql`
        mutation TranslateText($text: String!, $targetLanguage: String!) {
          translateText(text: $text, targetLanguage: $targetLanguage)
        }
      `,
      { text, targetLanguage }
    )
    return data?.translateText || null
  },

  async getFeed(currentUserId: string | number, hashtag?: string) {
    const data = await apiClient.query<{ getFeed: any[] }>(
      gql`
        query GetFeed($currentUserId: ID!, $hashtag: String) {
          getFeed(currentUserId: $currentUserId, hashtag: $hashtag) {
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
            commentCount
            shareCount
            media {
              src
              altText
              backgroundColor
            }
            reactions {
              reactionType
              userIds
            }
          }
        }
      `,
      {
        currentUserId: String(currentUserId),
        hashtag: hashtag || null
      },
      { fetchPolicy: 'network-only' }
    )
    return data?.getFeed || []
  },

  async getPost(id: string | number) {
    const data = await apiClient.query<{ getPostById: any }>(
      gql`
        query GetPostByIdSingle($id: ID!) {
          getPostById(postId: $id) {
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
            commentCount
            shareCount
            media {
              src
              altText
              backgroundColor
            }
            reactions {
              reactionType
              userIds
            }
          }
        }
      `,
      { id: String(id) },
      { fetchPolicy: 'network-only' }
    )
    return data?.getPostById || null
  }
}
