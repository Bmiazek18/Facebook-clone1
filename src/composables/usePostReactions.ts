import { ref, computed } from 'vue'

export interface ReactionIcon {
  src: string
  bg: string
}

const reactionIcons: Record<string, ReactionIcon> = {
  like: { src: 'https://fonts.gstatic.com/s/e/notoemoji/latest/1f44d/512.gif', bg: 'bg-blue-500' },
  love: { src: 'https://fonts.gstatic.com/s/e/notoemoji/latest/2764_fe0f/512.gif', bg: 'bg-red-500' },
  haha: { src: 'https://fonts.gstatic.com/s/e/notoemoji/latest/1f606/512.gif', bg: 'bg-yellow-400' },
  wow: { src: 'https://fonts.gstatic.com/s/e/notoemoji/latest/1f62f/512.gif', bg: 'bg-yellow-400' },
  sad: { src: 'https://fonts.gstatic.com/s/e/notoemoji/latest/1f622/512.gif', bg: 'bg-yellow-400' },
  angry: { src: 'https://fonts.gstatic.com/s/e/notoemoji/latest/1f621/512.gif', bg: 'bg-orange-500' }
}

const defaultReactionIcon: ReactionIcon = { src: 'https://fonts.gstatic.com/s/e/notoemoji/latest/1f44d/512.gif', bg: 'bg-blue-500' }

export function usePostReactions(initialLikesCount = 0) {
  const userReaction = ref<string | null>(null)
  const baseLikesCount = initialLikesCount

  const likesCount = computed(() => baseLikesCount + (userReaction.value ? 1 : 0))

  const hasReacted = computed(() => userReaction.value !== null)

  const getReactionIcon = (reaction: string | null): ReactionIcon => {
    if (!reaction) return defaultReactionIcon
    return reactionIcons[reaction] ?? defaultReactionIcon
  }

  const currentReactionIcon = computed(() => getReactionIcon(userReaction.value))

  const handleReaction = (reaction: string | null) => {
    userReaction.value = reaction
  }

  const toggleLike = () => {
    userReaction.value = userReaction.value === 'like' ? null : 'like'
  }

  return {
    userReaction,
    likesCount,
    hasReacted,
    currentReactionIcon,
    getReactionIcon,
    handleReaction,
    toggleLike,
    reactionIcons
  }
}
