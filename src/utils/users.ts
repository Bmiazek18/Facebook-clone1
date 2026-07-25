import { usersCache, useUserCache } from '@/composables/shared/useUserCache'

import type { User } from '@/types/User'

export type { User }

export function getUserById(id: string | number): User | undefined {
  if (!id) return undefined
  const cleanId = String(id).replace('user_', '')

  const cachedUser = usersCache.value[cleanId]
  if (cachedUser) {
    return {
      id: cachedUser.id,
      name: cachedUser.name,
      avatar: cachedUser.avatar,
      bio: '',
      location: '',
      website: '',
      joinDate: '',
      followersCount: 0,
      followingCount: 0,
      friendsCount: 0,
      postsCount: 0,
      cover: '',
      status: 'offline'
    } as User
  }
  // Fetch in background to trigger reactivity if it is a valid UUID
  const isUuid = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i.test(cleanId)
  if (isUuid) {
    const { getOrFetchUser } = useUserCache()
    getOrFetchUser(cleanId)
  }

  // Fallback default mock users
  const defaultUsers: Record<string, { name: string, avatar: string }> = {
    '1e4332f6-5a7a-3210-b5fb-fb92c7c60cce': {
      name: 'Jan Wiśniewski',
      avatar: 'https://randomuser.me/api/portraits/men/32.jpg'
    },
    '469e30bb-139b-43a8-8fa6-cbedce352bfa': {
      name: 'Piotr Kowalski',
      avatar: 'https://randomuser.me/api/portraits/men/84.jpg'
    }
  }

  const def = defaultUsers[cleanId]
  if (def) {
    return {
      id: cleanId,
      name: def.name,
      avatar: def.avatar,
      bio: 'Bio',
      location: '',
      website: '',
      joinDate: '',
      followersCount: 0,
      followingCount: 0,
      friendsCount: 0,
      postsCount: 0,
      cover: '',
      status: 'offline'
    } as User
  }

  return undefined
}

export function getAllUsers(): User[] {
  return Object.values(usersCache.value).map(cachedUser => ({
    id: cachedUser.id,
    name: cachedUser.name,
    avatar: cachedUser.avatar,
    bio: '',
    location: '',
    website: '',
    joinDate: '',
    followersCount: 0,
    followingCount: 0,
    friendsCount: 0,
    postsCount: 0,
    cover: '',
    status: 'offline'
  } as User))
}
