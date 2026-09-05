import { feedApi } from '@/api/feed'
import type { Comment } from '@/types/Post'
import { useAuthStore } from '@/stores/auth'
import { usersCache } from '@/composables/shared/useUserCache'

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
  const userId = currentUserId

  flatComments.forEach((c: any) => {
    if (c.mentionedUsers && Array.isArray(c.mentionedUsers)) {
      c.mentionedUsers.forEach((u: any) => {
        if (u && u.id) {
          usersCache.value[String(u.id)] = {
            id: String(u.id),
            name: `${u.firstName} ${u.lastName}`.trim() || 'Użytkownik',
            avatar: u.avatar || '/default-avatar.png',
          }
        }
      })
    }
    let formattedReactions: Record<string, string[]> = {}
    let userReaction: any = undefined
    if (c.reactions) {
      if (Array.isArray(c.reactions)) {
        c.reactions.forEach((r: any) => {
          const type = r.reactionType
          formattedReactions[type] = (r.userIds || []).map(String)
          if (Array.isArray(r.userIds) && r.userIds.some((id: string | number) => String(id) === String(userId))) {
            userReaction = type
          }
        })
      } else {
        formattedReactions = c.reactions
        for (const [type, userIds] of Object.entries(c.reactions)) {
          if (Array.isArray(userIds) && userIds.map(String).includes(String(userId))) {
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
      reactions: formattedReactions,
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

  async function fetchCommentsForPost(post: any, limit?: number) {
    if (!post) return
    try {
      const comments = await feedApi.getComments(String(post.id), limit)
      if (comments) {
        post.comments = buildCommentTree(comments, String(authStore.currentUserId))
      }
    } catch (e) {
      console.error(`Failed to fetch comments for post ${post.id}:`, e)
    }
  }

  async function addComment(post: any, commentInput: any, parentId: number | null) {
    if (!post) return
    const userId = authStore.currentUserId

    // Build the optimistic comment
    const nameParts = (authStore.currentUser?.name || '').split(' ')
    const firstName = nameParts[0] || ''
    const lastName = nameParts.slice(1).join(' ') || ''

    const tempId = commentInput.id || Date.now()
    const optimisticComment: Comment = {
      id: tempId,
      authorId: userId,
      author: authStore.currentUser
        ? {
            id: authStore.currentUser.id,
            firstName,
            lastName,
            avatar: authStore.currentUser.avatar || null,
          }
        : null,
      content: commentInput.content,
      timestamp: commentInput.timestamp || Date.now(),
      date: commentInput.date || new Date().toISOString(),
      parentId: parentId,
      replies: [],
      likesCount: 0,
      reactions: {},
      image: commentInput.image || undefined,
      userReaction: undefined,
    }

    // Add optimistic comment to the local UI immediately
    if (parentId) {
      const parentComment = findComment(post.comments || [], parentId)
      if (parentComment) {
        if (!parentComment.replies) parentComment.replies = []
        parentComment.replies.push(optimisticComment)
      }
    } else {
      if (!post.comments) post.comments = []
      post.comments.push(optimisticComment)
    }

    if (post.stats) {
      post.stats.comments = (post.stats.comments || 0) + 1
    }

    try {
      const savedComment = await feedApi.addComment({
        postId: String(post.id),
        userId: String(userId),
        parentId: parentId == null ? null : String(parentId),
        content: commentInput.content,
        mediaUrl: commentInput.image || commentInput.gif || null,
      })
      if (savedComment) {
        // Find the optimistic comment we just added, and update its ID and other fields with actual server values
        const targetList = parentId 
          ? findComment(post.comments || [], parentId)?.replies 
          : post.comments

        if (targetList) {
          const optIndex = targetList.findIndex(c => c.id === tempId)
          if (optIndex !== -1) {
            targetList[optIndex].id = Number(savedComment.id)
            targetList[optIndex].date = savedComment.createdAt
            targetList[optIndex].timestamp = new Date(savedComment.createdAt).getTime()
            if (savedComment.mediaUrl) {
              targetList[optIndex].image = savedComment.mediaUrl
            }
          }
        }
      }
    } catch (e) {
      console.error(`Failed to add comment to post ${post.id}:`, e)
      // Rollback on error
      const targetList = parentId 
        ? findComment(post.comments || [], parentId)?.replies 
        : post.comments

      if (targetList) {
        const optIndex = targetList.findIndex(c => c.id === tempId)
        if (optIndex !== -1) {
          targetList.splice(optIndex, 1)
        }
      }
      if (post.stats) {
        post.stats.comments = Math.max(0, (post.stats.comments || 0) - 1)
      }
    }
  }

  return {
    fetchCommentsForPost,
    addComment,
  }
}
