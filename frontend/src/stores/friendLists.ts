import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { useAuthStore } from '@/stores/auth'

export interface CustomFriendList {
  id: string
  name: string
  description?: string
  icon?: string
  isSystem?: boolean
  memberIds: string[]
  createdAt: number
}

const DEFAULT_LISTS: Omit<CustomFriendList, 'createdAt'>[] = [
  {
    id: 'close_friends',
    name: 'Bliscy znajomi',
    description: 'Najbliżsi znajomi, z którymi dzielisz się wyjątkowymi chwilami.',
    icon: 'star',
    isSystem: true,
    memberIds: [],
  },
  {
    id: 'restricted',
    name: 'Ograniczony dostęp',
    description: 'Znajomi z ograniczonym dostępem. Widzą tylko posty publiczne lub te, w których ich oznaczasz.',
    icon: 'lock',
    isSystem: true,
    memberIds: [],
  },
  {
    id: 'acquaintances',
    name: 'Dalsi znajomi',
    description: 'Osoby, z którymi chcesz rzadziej dzielić się publikowanymi postami.',
    icon: 'group',
    isSystem: true,
    memberIds: [],
  },
]

export const useFriendListsStore = defineStore('friendLists', () => {
  const authStore = useAuthStore()
  const lists = ref<CustomFriendList[]>([])
  const isLoaded = ref(false)
  const isLoading = ref(false)

  const storageKey = computed(() => {
    const uid = authStore.currentUserId || 'default'
    return `custom_friend_lists_${uid}`
  })

  const loadFromLocalStorage = () => {
    if (typeof window === 'undefined') return
    try {
      const raw = localStorage.getItem(storageKey.value)
      if (raw) {
        const parsed = JSON.parse(raw)
        lists.value = parsed
      } else {
        lists.value = DEFAULT_LISTS.map((dl) => ({ ...dl, createdAt: Date.now() }))
      }
    } catch {
      lists.value = DEFAULT_LISTS.map((dl) => ({ ...dl, createdAt: Date.now() }))
    }
  }

  const saveToLocalStorage = () => {
    if (typeof window === 'undefined') return
    try {
      localStorage.setItem(storageKey.value, JSON.stringify(lists.value))
    } catch {}
  }

  const loadLists = async (forceBackend = false) => {
    if (!isLoaded.value) {
      loadFromLocalStorage()
    }

    if (isLoading.value) return
    isLoading.value = true

    try {
      const res = await $fetch<{ success: boolean; lists: CustomFriendList[] }>('/api/friend-lists', {
        headers: authStore.currentUserId ? { 'x-user-id': String(authStore.currentUserId) } : {},
      })
      if (res?.success && Array.isArray(res.lists)) {
        lists.value = res.lists
        saveToLocalStorage()
      }
    } catch (e) {
      console.warn('FriendListsStore: Backend unavailable, using local cache:', e)
    } finally {
      isLoaded.value = true
      isLoading.value = false
    }
  }

  const createList = async (name: string, memberIds: string[] = []): Promise<CustomFriendList> => {
    const trimmed = name.trim()
    if (!trimmed) throw new Error('Nazwa listy nie może być pusta.')

    const localNewList: CustomFriendList = {
      id: `custom_${Date.now()}_${Math.random().toString(36).substr(2, 6)}`,
      name: trimmed,
      description: 'Niestandardowa lista utworzona przez Ciebie.',
      icon: 'custom',
      isSystem: false,
      memberIds: [...new Set(memberIds.map(String))],
      createdAt: Date.now(),
    }

    // Optimistic update
    lists.value.push(localNewList)
    saveToLocalStorage()

    try {
      const res = await $fetch<{ success: boolean; list: CustomFriendList; lists: CustomFriendList[] }>('/api/friend-lists', {
        method: 'POST',
        headers: authStore.currentUserId ? { 'x-user-id': String(authStore.currentUserId) } : {},
        body: {
          name: trimmed,
          memberIds: localNewList.memberIds,
        },
      })
      if (res?.success && res.list) {
        // Replace local ID with persisted ID if changed
        const idx = lists.value.findIndex((l) => l.id === localNewList.id)
        if (idx !== -1) {
          lists.value[idx] = res.list
        }
        if (res.lists) {
          lists.value = res.lists
        }
        saveToLocalStorage()
        return res.list
      }
    } catch (e) {
      console.error('Failed to create friend list on backend:', e)
    }

    return localNewList
  }

  const deleteList = async (listId: string) => {
    const list = lists.value.find((l) => l.id === listId)
    if (list?.isSystem) {
      throw new Error('Nie można usunąć listy systemowej.')
    }

    // Optimistic delete
    lists.value = lists.value.filter((l) => l.id !== listId)
    saveToLocalStorage()

    try {
      const res = await $fetch<{ success: boolean; lists: CustomFriendList[] }>(`/api/friend-lists/${listId}`, {
        method: 'DELETE',
        headers: authStore.currentUserId ? { 'x-user-id': String(authStore.currentUserId) } : {},
      })
      if (res?.success && res.lists) {
        lists.value = res.lists
        saveToLocalStorage()
      }
    } catch (e) {
      console.error('Failed to delete friend list from backend:', e)
    }
  }

  const renameList = async (listId: string, newName: string) => {
    const trimmed = newName.trim()
    if (!trimmed) return
    const list = lists.value.find((l) => l.id === listId)
    if (list) {
      list.name = trimmed
      saveToLocalStorage()

      try {
        await $fetch('/api/friend-lists', {
          method: 'PUT',
          headers: authStore.currentUserId ? { 'x-user-id': String(authStore.currentUserId) } : {},
          body: {
            listId,
            action: 'rename',
            newName: trimmed,
          },
        })
      } catch (e) {
        console.error('Failed to rename friend list on backend:', e)
      }
    }
  }

  const addMemberToList = async (listId: string, userId: string | number) => {
    const list = lists.value.find((l) => l.id === listId)
    if (list) {
      const uId = String(userId)
      if (!list.memberIds.includes(uId)) {
        list.memberIds.push(uId)
        saveToLocalStorage()

        try {
          await $fetch('/api/friend-lists', {
            method: 'PUT',
            headers: authStore.currentUserId ? { 'x-user-id': String(authStore.currentUserId) } : {},
            body: {
              listId,
              action: 'add',
              targetUserId: uId,
            },
          })
        } catch (e) {
          console.error('Failed to add member to friend list on backend:', e)
        }
      }
    }
  }

  const removeMemberFromList = async (listId: string, userId: string | number) => {
    const list = lists.value.find((l) => l.id === listId)
    if (list) {
      const uId = String(userId)
      list.memberIds = list.memberIds.filter((id) => id !== uId)
      saveToLocalStorage()

      try {
        await $fetch('/api/friend-lists', {
          method: 'PUT',
          headers: authStore.currentUserId ? { 'x-user-id': String(authStore.currentUserId) } : {},
          body: {
            listId,
            action: 'remove',
            targetUserId: uId,
          },
        })
      } catch (e) {
        console.error('Failed to remove member from friend list on backend:', e)
      }
    }
  }

  const toggleMemberInList = async (listId: string, userId: string | number) => {
    const list = lists.value.find((l) => l.id === listId)
    if (list) {
      const uId = String(userId)
      if (list.memberIds.includes(uId)) {
        list.memberIds = list.memberIds.filter((id) => id !== uId)
      } else {
        list.memberIds.push(uId)
      }
      saveToLocalStorage()

      try {
        await $fetch('/api/friend-lists', {
          method: 'PUT',
          headers: authStore.currentUserId ? { 'x-user-id': String(authStore.currentUserId) } : {},
          body: {
            listId,
            action: 'toggle',
            targetUserId: uId,
          },
        })
      } catch (e) {
        console.error('Failed to toggle member in friend list on backend:', e)
      }
    }
  }

  const getListById = (listId: string) => {
    return lists.value.find((l) => l.id === listId)
  }

  const isMemberOf = (listId: string, userId: string | number) => {
    const list = lists.value.find((l) => l.id === listId)
    return list ? list.memberIds.includes(String(userId)) : false
  }

  return {
    lists,
    isLoaded,
    isLoading,
    loadLists,
    createList,
    deleteList,
    renameList,
    addMemberToList,
    removeMemberFromList,
    toggleMemberInList,
    getListById,
    isMemberOf,
  }
})
