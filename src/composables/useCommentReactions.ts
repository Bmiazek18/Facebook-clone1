import { computed } from 'vue'
import { usePostsStore } from '@/stores/posts'
import type { ReactionType } from '@/types/Post'

export const useCommentReactions = (postId: string, commentId: number) => {
  const postsStore = usePostsStore()

  const comment = computed(() => {
    const post = postsStore.getPostById(postId)
    if (!post || !post.comments) return null
    return postsStore.findComment(post.comments, commentId)
  })

  const userReaction = computed(() => comment.value?.userReaction || null)

  const handleReaction = (reaction: ReactionType | null) => {
    postsStore.handleCommentReaction(postId, commentId, reaction)
  }

  return {
    userReaction,
    handleReaction,
  }
}
