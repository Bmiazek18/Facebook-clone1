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
