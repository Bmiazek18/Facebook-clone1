import type { Reel } from '@/types/Reel'

export const CURATED_REELS: Reel[] = []

export function processPostsIntoReels(posts: any[], currentUserId: string): Reel[] {
  return (posts || [])
    .map((post) => {
      if (!post || !post.media || !Array.isArray(post.media)) return null

      // Znajdź element wideo w mediach posta
      const videoMedia = post.media.find((m: any) => {
        if (!m || !m.src) return false
        const srcLower = String(m.src).toLowerCase()
        const typeLower = String(m.type || '').toLowerCase()
        return (
          typeLower === 'video' ||
          srcLower.endsWith('.mp4') ||
          srcLower.endsWith('.webm') ||
          srcLower.endsWith('.mov') ||
          srcLower.includes('/videos/') ||
          srcLower.includes('/video/') ||
          srcLower.includes('/files/')
        )
      })

      if (!videoMedia || !videoMedia.src) return null

      const src = videoMedia.src

      // Oblicz reakcje
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
        authorId: post.authorId || post.author?.id,
        videoSrc: src,
        poster: videoMedia.poster || '',
        caption: post.content || '',
        likes: String(likesList.length || 0),
        isLiked: isLiked,
        commentsCount: post.commentCount || 0,
        sharesCount: post.shareCount || 0,
        music: 'Oryginalny dźwięk',
        comments: post.comments || [],
        isFollowing: false,
        _originalPost: post,
      } as Reel
    })
    .filter((reel): reel is Reel => reel !== null)
}
