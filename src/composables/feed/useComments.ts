import { useMutation, useApolloClient } from '@vue/apollo-composable'
import { gql } from 'graphql-tag'
import type { Comment } from '@/types/Post'
import { useAuthStore } from '@/stores/auth'

const ADD_COMMENT_MUTATION = gql`
  mutation AddComment($input: AddCommentInput!) {
    addComment(input: $input) {
      id
      userId
      content
      createdAt
      parentId
      mediaUrl
    }
  }
`

const GET_COMMENTS_QUERY = gql`
  query Comments($postId: ID!, $limit: Int) {
    comments(postId: $postId, limit: $limit) {
      id
      userId
      author {
        id
        firstName
        lastName
        avatarId
      }
      parentId
      content
      createdAt
      reactions {
        reactionType
        userIds
      }
    }
  }
`

function findComment(comments: Comment[], commentId: number | string): Comment | null {
  for (const comment of comments) {
    if (String(comment.id) === String(commentId)) return comment
    if (comment.replies) {
      const found = findComment(comment.replies, commentId)
      if (found) return found
    }
  }
  return null
}

function buildCommentTree(flatComments: any[], currentUserId: string): Comment[] {
  const map: Record<number, Comment> = {}
  const roots: Comment[] = []
  const userId = Number(currentUserId)

  flatComments.forEach((c: any) => {
    let userReaction: any = undefined
    if (c.reactions) {
      if (Array.isArray(c.reactions)) {
        userReaction = c.reactions.find(
          (reaction: any) =>
            Array.isArray(reaction.userIds) &&
            reaction.userIds.some((id: string | number) => String(id) === String(userId)),
        )?.reactionType
      } else {
        for (const [type, userIds] of Object.entries(c.reactions)) {
          if (Array.isArray(userIds) && userIds.includes(userId)) {
            userReaction = type
            break
          }
        }
      }
    }

    map[c.id] = {
      id: Number(c.id),
      authorId: c.userId,
      author: c.author || null,
      content: c.content,
      image: c.image || undefined,
      timestamp: c.createdAt ? new Date(c.createdAt).getTime() : Date.now(),
      date: c.createdAt || new Date().toISOString(),
      parentId: c.parentId ? Number(c.parentId) : null,
      replies: [],
      likesCount: 0,
      reactions: c.reactions || {},
      userReaction: userReaction,
    }
  })

  flatComments.forEach((c: any) => {
    const comment = map[c.id]
    if (c.parentId) {
      const parent = map[c.parentId]
      if (parent) {
        if (!parent.replies) parent.replies = []
        parent.replies.push(comment)
      }
    } else {
      roots.push(comment)
    }
  })

  return roots
}

export function useComments() {
  const authStore = useAuthStore()
  const { client } = useApolloClient()
  const { mutate: addCommentMutation } = useMutation(ADD_COMMENT_MUTATION)

  async function fetchCommentsForPost(post: any, limit?: number) {
    if (!post) return
    try {
      const { data } = await client.query({
        query: GET_COMMENTS_QUERY,
        variables: {
          postId: String(post.id),
          limit: limit != null ? limit : null,
        },
        fetchPolicy: 'network-only',
      })
      if (data?.comments) {
        post.comments = buildCommentTree(data.comments, String(authStore.currentUserId))
      }
    } catch (e) {
      console.error(`Failed to fetch comments for post ${post.id}:`, e)
    }
  }

  async function addComment(post: any, commentInput: any, parentId: number | null) {
    if (!post) return
    const userId = Number(authStore.currentUserId)

    try {
      const { data } = await addCommentMutation({
        input: {
          postId: String(post.id),
          userId: String(userId),
          parentId: parentId == null ? null : String(parentId),
          content: commentInput.content,
          mediaUrl: commentInput.image || commentInput.gif || null,
        },
      })
      const savedComment = data?.addComment
      if (savedComment) {
        const finalComment: Comment = {
          id: Number(savedComment.id),
          authorId: Number(savedComment.userId),
          content: savedComment.content,
          timestamp: new Date(savedComment.createdAt).getTime(),
          date: savedComment.createdAt,
          parentId: savedComment.parentId ? Number(savedComment.parentId) : null,
          replies: [],
          likesCount: 0,
          reactions: {},
          image: savedComment.mediaUrl || undefined,
          userReaction: undefined,
        }

        if (parentId) {
          const parentComment = findComment(post.comments || [], parentId)
          if (parentComment) {
            if (!parentComment.replies) parentComment.replies = []
            parentComment.replies.push(finalComment)
          }
        } else {
          if (!post.comments) post.comments = []
          post.comments.push(finalComment)
        }

        if (post.stats) {
          post.stats.comments = (post.stats.comments || 0) + 1
        }
      }
    } catch (e) {
      console.error(`Failed to add comment to post ${post.id}:`, e)
    }
  }

  return {
    fetchCommentsForPost,
    addComment,
  }
}
