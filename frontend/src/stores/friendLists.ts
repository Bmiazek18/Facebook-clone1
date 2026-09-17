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

  const storageKey = computed(() => {
    const uid = authStore.currentUserId || 'default'
    return `custom_friend_lists_${uid}`
  })

  const loadLists = () => {
    if (typeof window === 'undefined') return
    try {
      const raw = localStorage.getItem(storageKey.value)
      if (raw) {
        const parsed = JSON.parse(raw)
        // Ensure default system lists exist
        const existingMap = new Map(parsed.map((l: CustomFriendList) => [l.id, l]))
        const merged: CustomFriendList[] = DEFAULT_LISTS.map((dl) => {
          if (existingMap.has(dl.id)) {
            const existing = existingMap.get(dl.id) as CustomFriendList
            return {
              ...dl,
              memberIds: existing.memberIds || [],
              createdAt: existing.createdAt || Date.now(),
            }
          }
          return { ...dl, createdAt: Date.now() }
        })

        // Add user-created custom lists
        parsed.forEach((l: CustomFriendList) => {
          if (!merged.some((m) => m.id === l.id)) {
            merged.push(l)
          }
        })
        lists.value = merged
      } else {
        lists.value = DEFAULT_LISTS.map((dl) => ({ ...dl, createdAt: Date.now() }))
        saveLists()
      }
    } catch (e) {
      console.error('Failed to load custom friend lists from localStorage:', e)
      lists.value = DEFAULT_LISTS.map((dl) => ({ ...dl, createdAt: Date.now() }))
    } finally {
      isLoaded.value = true
    }
  }

  const saveLists = () => {
    if (typeof window === 'undefined') return
    try {
      localStorage.setItem(storageKey.value, JSON.stringify(lists.value))
    } catch (e) {
      console.error('Failed to save custom friend lists to localStorage:', e)
    }
  }

  const createList = (name: string, memberIds: string[] = []): CustomFriendList => {
    const trimmed = name.trim()
    if (!trimmed) throw new Error('Nazwa listy nie może być pusta.')
    const newList: CustomFriendList = {
      id: `custom_${Date.now()}_${Math.random().toString(36).substr(2, 6)}`,
      name: trimmed,
      description: 'Niestandardowa lista utworzona przez Ciebie.',
      icon: 'custom',
      isSystem: false,
      memberIds: [...new Set(memberIds.map(String))],
      createdAt: Date.now(),
    }
    lists.value.push(newList)
    saveLists()
    return newList
  }

  const deleteList = (listId: string) => {
    const list = lists.value.find((l) => l.id === listId)
    if (list?.isSystem) {
      throw new Error('Nie można usunąć listy systemowej.')
    }
    lists.value = lists.value.filter((l) => l.id !== listId)
    saveLists()
  }

  const renameList = (listId: string, newName: string) => {
    const trimmed = newName.trim()
    if (!trimmed) return
    const list = lists.value.find((l) => l.id === listId)
    if (list) {
      list.name = trimmed
      saveLists()
    }
  }

  const addMemberToList = (listId: string, userId: string | number) => {
    const list = lists.value.find((l) => l.id === listId)
    if (list) {
      const uId = String(userId)
      if (!list.memberIds.includes(uId)) {
        list.memberIds.push(uId)
        saveLists()
      }
    }
  }

  const removeMemberFromList = (listId: string, userId: string | number) => {
    const list = lists.value.find((l) => l.id === listId)
    if (list) {
      const uId = String(userId)
      list.memberIds = list.memberIds.filter((id) => id !== uId)
      saveLists()
    }
  }

  const toggleMemberInList = (listId: string, userId: string | number) => {
    const list = lists.value.find((l) => l.id === listId)
    if (list) {
      const uId = String(userId)
      if (list.memberIds.includes(uId)) {
        list.memberIds = list.memberIds.filter((id) => id !== uId)
      } else {
        list.memberIds.push(uId)
      }
      saveLists()
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
    loadLists,
    saveLists,
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
