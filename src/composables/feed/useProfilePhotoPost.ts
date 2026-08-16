import { useMutation } from '@vue/apollo-composable'
import { gql } from 'graphql-tag'
import { usePostsStore } from '@/composables/feed/useAppState'
import { useAuthStore } from '@/stores/auth'

export type ProfilePhotoKind = 'avatar' | 'cover'

export const PROFILE_AVATAR_ALT = 'profile_avatar'
export const PROFILE_COVER_ALT = 'profile_cover'

export function profilePhotoAlt(kind: ProfilePhotoKind) {
  return kind === 'cover' ? PROFILE_COVER_ALT : PROFILE_AVATAR_ALT
}

export function profilePhotoContent(kind: ProfilePhotoKind) {
  return kind === 'cover' ? 'Zaktualizowano zdjęcie w tle' : 'Zaktualizowano zdjęcie profilowe'
}

function normalizeMediaKey(src: string): string {
  if (!src) return ''
  try {
    const u = new URL(src, 'http://localhost')
    const path = u.pathname
    // /api/users/avatar/{id} or /files/{id} etc.
    const parts = path.split('/').filter(Boolean)
    return (parts[parts.length - 1] || path).split('?')[0].toLowerCase()
  } catch {
    return src.split('/').pop()?.split('?')[0]?.toLowerCase() || src.toLowerCase()
  }
}

export function mediaMatchesSrc(mediaSrc: string, targetSrc: string): boolean {
  if (!mediaSrc || !targetSrc) return false
  if (mediaSrc === targetSrc) return true
  return normalizeMediaKey(mediaSrc) === normalizeMediaKey(targetSrc)
}

export function findProfilePhotoPost(opts: {
  userId: string | number
  kind: ProfilePhotoKind
  src?: string
  posts?: any[]
}) {
  const list = opts.posts || usePostsStore().posts
  const alt = profilePhotoAlt(opts.kind)
  const userId = String(opts.userId)

  const matches = list.filter((p: any) => {
    if (String(p.authorId) !== userId) return false
    const media = Array.isArray(p.media) ? p.media : []
    return media.some((m: any) => {
      if (opts.src && mediaMatchesSrc(m.src, opts.src)) return true
      return m.altText === alt
    })
  })

  if (!matches.length) return null

  // Prefer exact src match, then latest by timestamp
  if (opts.src) {
    const bySrc = matches.filter((p: any) =>
      p.media?.some((m: any) => mediaMatchesSrc(m.src, opts.src!)),
    )
    if (bySrc.length) {
      return [...bySrc].sort((a, b) => (b.timestamp || 0) - (a.timestamp || 0))[0]
    }
  }

  return [...matches].sort((a, b) => (b.timestamp || 0) - (a.timestamp || 0))[0]
}

const CREATE_PROFILE_PHOTO_POST = gql`
  mutation CreateProfilePhotoPost($input: CreatePostInput!) {
    createPost(input: $input) {
      id
      content
      authorId
      date
      timestamp
      commentCount
      shareCount
      media {
        src
        altText
        backgroundColor
      }
      reactions {
        reactionType
        userIds
      }
    }
  }
`

export function useProfilePhotoPost() {
  const { mutate: createPostMutate } = useMutation(CREATE_PROFILE_PHOTO_POST)
  const postsStore = usePostsStore()
  const authStore = useAuthStore()

  const createProfilePhotoPost = async (kind: ProfilePhotoKind, mediaSrc: string) => {
    const authorId = String(authStore.currentUserId || '')
    if (!authorId || !mediaSrc) return null

    const result = await createPostMutate({
      input: {
        content: profilePhotoContent(kind),
        authorId,
        media: [{ src: mediaSrc, altText: profilePhotoAlt(kind), tags: [] }],
        isAnonymous: false,
        visibility: 'PUBLIC',
        allowedUserIds: [],
        taggedUsersIds: [],
      },
    })

    const created = result?.data?.createPost
    if (created) {
      const currentUser = authStore.currentUser
      postsStore.addPost({
        ...created,
        author: {
          id: authorId,
          firstName: (currentUser as any)?.firstName || currentUser?.name?.split(' ')[0] || '',
          lastName: (currentUser as any)?.lastName || currentUser?.name?.split(' ').slice(1).join(' ') || '',
          avatar: currentUser?.avatar || mediaSrc,
          name: currentUser?.name || 'Użytkownik',
        },
        authorId,
        comments: [],
        stats: {
          likes: 0,
          comments: created.commentCount ?? 0,
          shares: created.shareCount ?? 0,
        },
        reactions: created.reactions || [],
      })
    }

    return created || null
  }

  const resolveProfilePhotoPost = async (opts: {
    userId: string | number
    kind: ProfilePhotoKind
    src: string
  }) => {
    const existing = findProfilePhotoPost(opts)
    if (existing) return existing

    // Brak posta (stare zdjęcie) — utwórz, jeśli to właściciel
    if (String(opts.userId) === String(authStore.currentUserId) && opts.src) {
      return createProfilePhotoPost(opts.kind, opts.src)
    }

    return null
  }

  return {
    createProfilePhotoPost,
    resolveProfilePhotoPost,
    findProfilePhotoPost,
  }
}
