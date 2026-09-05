import { computed, isRef } from 'vue'
import { gql } from 'graphql-tag'
import { useAuthStore } from '@/stores/auth'
import type { ReactionType } from '@/types/Post'
import { getApolloClient } from '@/utils/apollo'

const REACT_TO_COMMENT_MUTATION = gql`
  mutation ReactToComment($input: CommentReactionInput!) {
    reactToComment(input: $input)
  }
`

export const useCommentReactions = (commentInput: any) => {
  const authStore = useAuthStore()

  const currentUserId = computed(() => String(authStore.currentUserId))

  const commentComputed = computed(() => (isRef(commentInput) ? commentInput.value : commentInput))

  const userReaction = computed(() => {
    const comment = commentComputed.value
    if (!comment) return null
    if (comment.userReaction) return comment.userReaction
    
    // Fallback search in reaction map if available
    if (comment.reactions) {
      const userId = String(currentUserId.value)
      for (const [type, userIds] of Object.entries(comment.reactions)) {
        if (Array.isArray(userIds) && userIds.map(String).includes(userId)) {
          return type as ReactionType
        }
      }
    }
    return null
  })

  const handleReaction = async (reaction: ReactionType | null) => {
    const comment = commentComputed.value
    if (!comment) return

    const userId = String(currentUserId.value)
    const currentReactions = { ...comment.reactions }
    const currentUserReaction = comment.userReaction

    const nextReactions: Record<string, string[]> = {}
    Object.keys(currentReactions).forEach((type) => {
      const filteredIds = (currentReactions[type] || []).map(String).filter((id: string) => id !== userId)
      if (filteredIds.length > 0) {
        nextReactions[type] = filteredIds
      }
    })

    if (reaction) {
      if (!nextReactions[reaction]) {
        nextReactions[reaction] = []
      }
      nextReactions[reaction].push(userId)
    }
    comment.reactions = nextReactions
    comment.userReaction = reaction || undefined

    try {
      const client = getApolloClient()
      await client.mutate({
        mutation: REACT_TO_COMMENT_MUTATION,
        variables: {
          input: {
            commentId: String(comment.id),
            userId: String(userId),
            reactionType: reaction,
          },
        },
      })
    } catch (e) {
      console.error(`Failed to save reaction for comment ${comment.id}:`, e)
      comment.reactions = currentReactions
      comment.userReaction = currentUserReaction
    }
  }

  return {
    userReaction,
    handleReaction,
  }
}
