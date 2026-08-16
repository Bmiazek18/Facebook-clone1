import type { Reel } from '@/types/Reel'

export function processPostsIntoReels(posts: any[], currentUserId: string): Reel[] {
  return posts
    .filter((post) => {
      // Post must have exactly one media
      if (!post.media || post.media.length !== 1) return false
      const mediaItem = post.media[0]
      
      // Check if media is video. Usually, in feed, video files have suffix .mp4, or type field.
      // We check if type is 'video' or if URL contains .mp4
      const isVideo = mediaItem.type?.toLowerCase() === 'video' || mediaItem.src?.toLowerCase().endsWith('.mp4') || mediaItem.src?.includes('/files/')
      return mediaItem.src && isVideo
    })
    .map((post) => {
      const src = post.media[0].src
      
      // Calculate reactions
      // Reactions structure in backend query: reactions { reactionType, userIds }
      // Reactions structure in frontend post object: reactions = { like: [userIds], ... }
      let likesList: string[] = []
      let userReactionType: string | null = null
      
      if (Array.isArray(post.reactions)) {
        post.reactions.forEach((r: any) => {
          const ids = (r.userIds || []).map(String)
          if (r.reactionType === 'like' || r.reactionType === 'LIKE') {
            likesList = ids
          }
          if (ids.includes(String(currentUserId))) {
            userReactionType = r.reactionType
          }
        })
      } else if (post.reactions) {
        // If already formatted as record
        likesList = (post.reactions.like || []).map(String)
        for (const [type, userIds] of Object.entries(post.reactions)) {
          if (Array.isArray(userIds) && userIds.map(String).includes(String(currentUserId))) {
            userReactionType = type
            break
          }
        }
      }
      
      const isLiked = likesList.includes(String(currentUserId))

      return {
        id: String(post.id),
        authorId: post.authorId,
        videoSrc: src,
        poster: '',
        caption: post.content || '',
        likes: String(likesList.length),
        isLiked: isLiked,
        commentsCount: post.commentCount || 0,
        sharesCount: post.shareCount || 0,
        music: 'Oryginalny dźwięk',
        comments: post.comments || [],
        isFollowing: false,
        _originalPost: post,
      } as Reel
    })
}
