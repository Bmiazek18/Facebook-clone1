import { computed, isRef } from 'vue'
import { useMutation, useApolloClient } from '@vue/apollo-composable'
import { gql } from 'graphql-tag'
import { useAuthStore } from '@/stores/auth'
import type { ReactionType } from '@/types/Post'

export const reactionIcons: Record<string, { src: string; bg: string; emoji?: string }> = {
  like: {
    src: 'https://fonts.gstatic.com/s/e/notoemoji/latest/1f44d/512.gif',
    bg: 'bg-blue-500',
    emoji: '👍',
  },
  love: {
    src: 'https://fonts.gstatic.com/s/e/notoemoji/latest/2764_fe0f/512.gif',
    bg: 'bg-red-500',
    emoji: '❤️',
  },
  haha: {
    src: 'https://fonts.gstatic.com/s/e/notoemoji/latest/1f606/512.gif',
    bg: 'bg-yellow-400',
    emoji: '😆',
  },
  wow: {
    src: 'https://fonts.gstatic.com/s/e/notoemoji/latest/1f62f/512.gif',
    bg: 'bg-yellow-400',
    emoji: '😮',
  },
  sad: {
    src: 'https://fonts.gstatic.com/s/e/notoemoji/latest/1f622/512.gif',
    bg: 'bg-yellow-400',
    emoji: '😢',
  },
  angry: {
    src: 'https://fonts.gstatic.com/s/e/notoemoji/latest/1f621/512.gif',
    bg: 'bg-orange-500',
    emoji: '😡',
  },
}

const REACT_TO_POST_MUTATION = gql`
  mutation ReactToPost($input: PostReactionInput!) {
    reactToPost(input: $input)
  }
`

const POST_REACTIONS_FRAGMENT = gql`
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

export function usePostReactions(postInput: any) {
  const authStore = useAuthStore()
  const { mutate: reactToPost } = useMutation(REACT_TO_POST_MUTATION)
  const { client } = useApolloClient()

  const currentUserId = computed(() => String(authStore.currentUserId))

  const postComputed = computed(() => (isRef(postInput) ? postInput.value : postInput))

  const userReaction = computed(() => {
    const post = postComputed.value
    if (!post?.reactions) return null
    const userId = currentUserId.value

    if (Array.isArray(post.reactions)) {
      const reaction = post.reactions.find(
        (item: any) =>
          Array.isArray(item.userIds) &&
          item.userIds.some((id: string | number) => String(id) === userId),
      )
      return (reaction?.reactionType?.toLowerCase() as ReactionType) || null
    }

    for (const [type, userIds] of Object.entries(post.reactions)) {
      if (Array.isArray(userIds) && userIds.some((id) => String(id) === userId)) {
        return type as ReactionType
      }
    }
    return null
  })

  const likesCount = computed(() => {
    const post = postComputed.value
    if (!post?.reactions) return 0
    if (Array.isArray(post.reactions)) {
      return post.reactions.reduce(
        (count: number, item: any) =>
          count + (Array.isArray(item.userIds) ? item.userIds.length : 0),
        0,
      )
    }
    let count = 0
    Object.values(post.reactions).forEach((ids: any) => {
      if (ids) count += ids.length
    })
    return count
  })

  const topReactions = computed<ReactionType[]>(() => {
    const post = postComputed.value
    if (!post?.reactions || Object.keys(post.reactions).length === 0) return []

    const reactionsMap = new Map<ReactionType, number>()

    if (Array.isArray(post.reactions)) {
      post.reactions.forEach((item: any) => {
        if (Array.isArray(item.userIds) && item.userIds.length > 0) {
          reactionsMap.set(item.reactionType as ReactionType, item.userIds.length)
        }
      })
      return Array.from(reactionsMap.entries())
        .sort(([, countA], [, countB]) => countB - countA)
        .map(([type]) => type)
        .slice(0, 3)
    }

    for (const [type, userIds] of Object.entries(post.reactions)) {
      if (Array.isArray(userIds) && userIds.length > 0) {
        reactionsMap.set(type as ReactionType, userIds.length)
      }
    }

    return Array.from(reactionsMap.entries())
      .sort(([, countA], [, countB]) => countB - countA)
      .map(([type]) => type)
      .slice(0, 3)
  })

  const handleReaction = async (type: ReactionType | null) => {
    const post = postComputed.value
    if (!post) return

    const userId = String(currentUserId.value)
    let previousReactionType: string | null = null

    const currentReactions: Record<string, string[]> = {}
    const currentReactionUserNames: Record<string, string[]> = {}

    if (post.reactions) {
      if (Array.isArray(post.reactions)) {
        for (const item of post.reactions) {
          const typeLower = item.reactionType?.toLowerCase()
          if (typeLower) {
            const uIds = (item.userIds || []).map(String)
            currentReactions[typeLower] = uIds
            currentReactionUserNames[typeLower] = (item.users || []).map((u: any) =>
              [u.firstName, u.lastName].filter(Boolean).join(' ') || 'Użytkownik'
            )
            if (uIds.includes(userId)) {
              previousReactionType = typeLower
            }
          }
        }
      } else {
        for (const [rType, userIds] of Object.entries(post.reactions)) {
          if (Array.isArray(userIds)) {
            const uIds = userIds.map(String)
            currentReactions[rType] = uIds
            if (uIds.includes(userId)) {
              previousReactionType = rType
            }
          }
        }
        if (post.reactionUserNames) {
          for (const [rType, names] of Object.entries(post.reactionUserNames)) {
            if (Array.isArray(names)) {
              currentReactionUserNames[rType] = names.map(String)
            }
          }
        }
      }
    }

    const currentRawReactions = post.rawReactions ? [...post.rawReactions] : []
    const nextReactions: Record<string, string[]> = {}
    const nextReactionUserNames: Record<string, string[]> = {}

    Object.keys(currentReactions).forEach((rType) => {
      const filteredIds = (currentReactions[rType] || []).map(String).filter((id: string) => id !== userId)
      if (filteredIds.length > 0) {
        nextReactions[rType] = filteredIds
      }
    })

    const currentUserName = [authStore.currentUser?.firstName, authStore.currentUser?.lastName]
      .filter(Boolean)
      .join(' ') || 'Użytkownik'

    Object.keys(currentReactionUserNames).forEach((rType) => {
      const filteredNames = (currentReactionUserNames[rType] || []).filter(
        (name: string) => name !== currentUserName,
      )
      if (filteredNames.length > 0) {
        nextReactionUserNames[rType] = filteredNames
      }
    })

    if (type) {
      if (!nextReactions[type]) {
        nextReactions[type] = []
      }
      nextReactions[type].push(userId)

      if (!nextReactionUserNames[type]) {
        nextReactionUserNames[type] = []
      }
      nextReactionUserNames[type].push(currentUserName)
    }

    const nextRawReactions: any[] = []
    Object.keys(nextReactions).forEach((rType) => {
      const uIds = nextReactions[rType]
      const names = nextReactionUserNames[rType] || []
      
      const usersList = uIds.map((id, index) => {
        const namePart = names[index] || 'Użytkownik'
        const split = namePart.split(' ')
        return {
          __typename: 'User',
          id: String(id),
          firstName: split[0] || 'Użytkownik',
          lastName: split.slice(1).join(' ') || '',
          avatarId: null,
          avatar: String(id) === userId ? (authStore.currentUser?.avatar || null) : null,
        }
      })
      
      nextRawReactions.push({
        __typename: 'ReactionDetail',
        reactionType: rType.toUpperCase(),
        userIds: uIds.map(String),
        users: usersList
      })
    })

    if (Array.isArray(post.reactions)) {
      post.reactions = nextRawReactions
    } else {
      post.reactions = nextReactions
      post.reactionUserNames = nextReactionUserNames
    }
    post.rawReactions = nextRawReactions

    // Optimistically update Apollo Cache
    try {
      client.cache.writeFragment({
        id: `Post:${post.id}`,
        fragment: POST_REACTIONS_FRAGMENT,
        data: {
          __typename: 'Post',
          id: post.id,
          reactions: nextRawReactions,
        },
      })
    } catch (cacheErr) {
      console.warn('Failed to write optimistic update to Apollo Cache:', cacheErr)
    }

    try {
      await reactToPost({
        input: {
          postId: String(post.id),
          userId: String(userId),
          reactionType: type,
          previousReactionType,
        },
      })
    } catch (e) {
      console.error(`Failed to save reaction for post ${post.id}:`, e)
      post.reactions = currentReactions
      post.reactionUserNames = currentReactionUserNames
      post.rawReactions = currentRawReactions
      
      try {
        client.cache.writeFragment({
          id: `Post:${post.id}`,
          fragment: POST_REACTIONS_FRAGMENT,
          data: {
            __typename: 'Post',
            id: post.id,
            reactions: currentRawReactions,
          },
        })
      } catch (cacheErr) {
        console.warn('Failed to write rollback to Apollo Cache:', cacheErr)
      }
    }
  }

  return {
    userReaction,
    likesCount,
    handleReaction,
    reactionIcons,
    topReactions,
  }
}
