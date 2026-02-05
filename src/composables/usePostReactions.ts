import { computed } from 'vue'
import { usePostsStore } from '@/stores/posts'
import type { ReactionType } from '@/types/Post'

export const reactionIcons: Record<string, { src: string, bg: string, emoji?: string }> = {
  like: { src: 'https://fonts.gstatic.com/s/e/notoemoji/latest/1f44d/512.gif', bg: 'bg-blue-500', emoji: '👍' },
  love: { src: 'https://fonts.gstatic.com/s/e/notoemoji/latest/2764_fe0f/512.gif', bg: 'bg-red-500', emoji: '❤️' },
  haha: { src: 'https://fonts.gstatic.com/s/e/notoemoji/latest/1f606/512.gif', bg: 'bg-yellow-400', emoji: '😆' },
  wow: { src: 'https://fonts.gstatic.com/s/e/notoemoji/latest/1f62f/512.gif', bg: 'bg-yellow-400', emoji: '😮' },
  sad: { src: 'https://fonts.gstatic.com/s/e/notoemoji/latest/1f622/512.gif', bg: 'bg-yellow-400', emoji: '😢' },
  angry: { src: 'https://fonts.gstatic.com/s/e/notoemoji/latest/1f621/512.gif', bg: 'bg-orange-500', emoji: '😡' }
}

export function usePostReactions(postId?: string) {
  const postsStore = usePostsStore()

  const post = computed(() => postId ? postsStore.getPostById(postId) : undefined)

  const userReaction = computed(() => {
    if (!post.value?.reactions) return null;
    const currentUserId = postsStore.currentUser.id;

    for (const [type, userIds] of Object.entries(post.value.reactions)) {
        if (userIds && userIds.includes(currentUserId)) {
            return type as ReactionType;
        }
    }
    return null;
  })

  const likesCount = computed(() => {
    if (!post.value?.reactions) return 0;
    let count = 0;
    Object.values(post.value.reactions).forEach(ids => {
        if(ids) count += ids.length;
    });
    return count;
  })

  const topReactions = computed<ReactionType[]>(() => {
    if (!post.value?.reactions || Object.keys(post.value.reactions).length === 0) return [];

    const reactionsMap = new Map<ReactionType, number>();

    for (const [type, userIds] of Object.entries(post.value.reactions)) {
        if (userIds && userIds.length > 0) {
            reactionsMap.set(type as ReactionType, userIds.length);
        }
    }

    // Sort reactions by count in descending order
    const sortedReactions = Array.from(reactionsMap.entries())
        .sort(([, countA], [, countB]) => countB - countA)
        .map(([type]) => type);

    // Get top 3
    return sortedReactions.slice(0, 3);
  });

  const handleReaction = (type: ReactionType) => {
    if (!post.value || !postId) return;


    postsStore.handlePostReaction(postId, type);

  }

  return {
    userReaction,
    likesCount,
    handleReaction,
    reactionIcons,
    topReactions
  }
}
