import type { Reel } from '@/types/Reel'

export const CURATED_REELS: Reel[] = [
  {
    id: 'reel_curated_1',
    authorId: 18,
    videoSrc: 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4',
    poster: 'https://picsum.photos/800/1200?random=101',
    caption: 'Ogniste barwy natury i spektakularne widoki! 🌄🔥 #natura #podroze #adventure #wild',
    likes: '1420',
    isLiked: false,
    commentsCount: 89,
    sharesCount: 34,
    music: 'Oryginalny dźwięk – Jan Kowalski',
    comments: [],
    _originalPost: {
      id: 'reel_curated_1',
      authorId: '18',
      author: { id: '18', name: 'Jan Kowalski', avatar: 'https://picsum.photos/200/200?random=18' },
      content: 'Ogniste barwy natury i spektakularne widoki! 🌄🔥 #natura #podroze #adventure #wild',
      date: new Date().toISOString(),
      media: [{ type: 'video', src: 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4' }],
      reactions: { like: ['19', '20', '30', '31'] },
      commentCount: 89,
      shareCount: 34
    }
  },
  {
    id: 'reel_curated_2',
    authorId: 19,
    videoSrc: 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4',
    poster: 'https://picsum.photos/800/1200?random=102',
    caption: 'Ucieczka za miasto – relaks wśród jezior i lasów 🛶🌲 #escape #chillout #naturevibes',
    likes: '2840',
    isLiked: true,
    commentsCount: 156,
    sharesCount: 92,
    music: 'Spokojna Przystań – Anna Nowak',
    comments: [],
    _originalPost: {
      id: 'reel_curated_2',
      authorId: '19',
      author: { id: '19', name: 'Anna Nowak', avatar: 'https://picsum.photos/200/200?random=19' },
      content: 'Ucieczka za miasto – relaks wśród jezior i lasów 🛶🌲 #escape #chillout #naturevibes',
      date: new Date().toISOString(),
      media: [{ type: 'video', src: 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4' }],
      reactions: { like: ['18', '20', '32', '33'] },
      commentCount: 156,
      shareCount: 92
    }
  },
  {
    id: 'reel_curated_3',
    authorId: 20,
    videoSrc: 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4',
    poster: 'https://picsum.photos/800/1200?random=103',
    caption: 'Najlepsze momenty z weekendowego festiwalu! 🎸🎉 #fun #festiwal #muzyka #impreza',
    likes: '3910',
    isLiked: false,
    commentsCount: 240,
    sharesCount: 115,
    music: 'Live Concert Mix – Piotr Wiśniewski',
    comments: [],
    _originalPost: {
      id: 'reel_curated_3',
      authorId: '20',
      author: { id: '20', name: 'Piotr Wiśniewski', avatar: 'https://picsum.photos/200/200?random=20' },
      content: 'Najlepsze momenty z weekendowego festiwalu! 🎸🎉 #fun #festiwal #muzyka #impreza',
      date: new Date().toISOString(),
      media: [{ type: 'video', src: 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4' }],
      reactions: { like: ['18', '19', '30'] },
      commentCount: 240,
      shareCount: 115
    }
  },
  {
    id: 'reel_curated_4',
    authorId: 30,
    videoSrc: 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerJoyBlazes.mp4',
    poster: 'https://picsum.photos/800/1200?random=104',
    caption: 'Niesamowita energia podczas treningu cardio i street workoutu! 💪🔥 #workout #trening #fit',
    likes: '5120',
    isLiked: false,
    commentsCount: 312,
    sharesCount: 180,
    music: 'Beast Mode Motivation – Michał Woźniak',
    comments: [],
    _originalPost: {
      id: 'reel_curated_4',
      authorId: '30',
      author: { id: '30', name: 'Michał Woźniak', avatar: 'https://picsum.photos/200/200?random=30' },
      content: 'Niesamowita energia podczas treningu cardio i street workoutu! 💪🔥 #workout #trening #fit',
      date: new Date().toISOString(),
      media: [{ type: 'video', src: 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerJoyBlazes.mp4' }],
      reactions: { like: ['18', '20', '31'] },
      commentCount: 312,
      shareCount: 180
    }
  },
  {
    id: 'reel_curated_5',
    authorId: 29,
    videoSrc: 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerMeltdowns.mp4',
    poster: 'https://picsum.photos/800/1200?random=105',
    caption: 'Magiczne krajobrazy Arktyki i topniejące lodowce 🧊❄️ #planet #geografia #arktyka',
    likes: '1980',
    isLiked: false,
    commentsCount: 94,
    sharesCount: 65,
    music: 'Ambient Chill – Katarzyna Zielińska',
    comments: [],
    _originalPost: {
      id: 'reel_curated_5',
      authorId: '29',
      author: { id: '29', name: 'Katarzyna Zielińska', avatar: 'https://picsum.photos/200/200?random=29' },
      content: 'Magiczne krajobrazy Arktyki i topniejące lodowce 🧊❄️ #planet #geografia #arktyka',
      date: new Date().toISOString(),
      media: [{ type: 'video', src: 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerMeltdowns.mp4' }],
      reactions: { like: ['19', '30'] },
      commentCount: 94,
      shareCount: 65
    }
  },
  {
    id: 'reel_curated_6',
    authorId: 31,
    videoSrc: 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/SubaruOutbackOnStreetAndDirt.mp4',
    poster: 'https://picsum.photos/800/1200?random=106',
    caption: 'Offroad w błocie i górskich serpentynach! 🚙💨 #offroad #motoryzacja #4x4',
    likes: '4450',
    isLiked: false,
    commentsCount: 218,
    sharesCount: 130,
    music: 'Engine Symphony – Agnieszka Dąbrowska',
    comments: [],
    _originalPost: {
      id: 'reel_curated_6',
      authorId: '31',
      author: { id: '31', name: 'Agnieszka Dąbrowska', avatar: 'https://picsum.photos/200/200?random=31' },
      content: 'Offroad w błocie i górskich serpentynach! 🚙💨 #offroad #motoryzacja #4x4',
      date: new Date().toISOString(),
      media: [{ type: 'video', src: 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/SubaruOutbackOnStreetAndDirt.mp4' }],
      reactions: { like: ['18', '20', '32'] },
      commentCount: 218,
      shareCount: 130
    }
  },
  {
    id: 'reel_curated_7',
    authorId: 32,
    videoSrc: 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/WeAreGoingOnBullrun.mp4',
    poster: 'https://picsum.photos/800/1200?random=107',
    caption: 'Wyprawa życia przez bezdroża i kaniony 🏎️🏜️ #rally #bullrun #speed #adrenalina',
    likes: '3670',
    isLiked: false,
    commentsCount: 184,
    sharesCount: 88,
    music: 'Electro Horizon – Tomasz Lewandowski',
    comments: [],
    _originalPost: {
      id: 'reel_curated_7',
      authorId: '32',
      author: { id: '32', name: 'Tomasz Lewandowski', avatar: 'https://picsum.photos/200/200?random=32' },
      content: 'Wyprawa życia przez bezdroża i kaniony 🏎️🏜️ #rally #bullrun #speed #adrenalina',
      date: new Date().toISOString(),
      media: [{ type: 'video', src: 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/WeAreGoingOnBullrun.mp4' }],
      reactions: { like: ['18', '19', '30'] },
      commentCount: 184,
      shareCount: 88
    }
  },
  {
    id: 'reel_curated_8',
    authorId: 33,
    videoSrc: 'https://www.w3schools.com/html/mov_bbb.mp4',
    poster: 'https://picsum.photos/800/1200?random=108',
    caption: 'Wiosenne przebudzenie i leśne przygody! 🐰🌸 #wiosna #natura #zwierzaki',
    likes: '2210',
    isLiked: false,
    commentsCount: 102,
    sharesCount: 45,
    music: 'Forest Birds Melody – Małgorzata Kamińska',
    comments: [],
    _originalPost: {
      id: 'reel_curated_8',
      authorId: '33',
      author: { id: '33', name: 'Małgorzata Kamińska', avatar: 'https://picsum.photos/200/200?random=33' },
      content: 'Wiosenne przebudzenie i leśne przygody! 🐰🌸 #wiosna #natura #zwierzaki',
      date: new Date().toISOString(),
      media: [{ type: 'video', src: 'https://www.w3schools.com/html/mov_bbb.mp4' }],
      reactions: { like: ['20', '31'] },
      commentCount: 102,
      shareCount: 45
    }
  }
]

export function processPostsIntoReels(posts: any[], currentUserId: string): Reel[] {
  const dynamicReels: Reel[] = (posts || [])
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

  // Połącz dynamiczne rolki z postów z przygotowanymi rolkami demonstracyjnymi (unikając duplikatów)
  const existingIds = new Set(dynamicReels.map((r) => r.id))
  const complementaryReels = CURATED_REELS.filter((r) => !existingIds.has(r.id))

  return [...dynamicReels, ...complementaryReels]
}
