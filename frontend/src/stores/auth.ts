import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { User } from '@/utils/users'
import { getUserById } from '@/utils/users'
import { useConversationsStore } from '@/stores/conversations'
import { usersCache } from '@/composables/shared/useUserCache'
import type { Page } from '@/types/Page'

const DEFAULT_USER_ID = '1e4332f6-5a7a-3210-b5fb-fb92c7c60cce' // Jan Wiśniewski

export const useAuthStore = defineStore('auth', () => {
  const getInitialOriginalUserId = (): string => {
    if (typeof window !== 'undefined') {
      const stored = localStorage.getItem('auth-original-user-id')
      if (stored && /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i.test(stored)) {
        return stored
      }
      const legacyCurrent = localStorage.getItem('auth-current-user-id')
      if (legacyCurrent && /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i.test(legacyCurrent)) {
        return legacyCurrent
      }
    }
    return DEFAULT_USER_ID
  }

  const getInitialActingAsPage = (): boolean => {
    if (typeof window !== 'undefined') {
      return localStorage.getItem('auth-is-acting-as-page') === 'true'
    }
    return false
  }

  const getInitialActivePageId = (): string | null => {
    if (typeof window !== 'undefined') {
      return localStorage.getItem('auth-active-page-id')
    }
    return null
  }

  const getInitialUserPages = (): Page[] => {
    if (typeof window !== 'undefined') {
      try {
        const stored = localStorage.getItem('auth-user-pages')
        if (stored) {
          const parsed = JSON.parse(stored)
          if (Array.isArray(parsed)) {
            parsed.forEach((p: Page) => {
              if (p.id && p.name) {
                usersCache.value[p.id] = {
                  id: p.id,
                  name: p.name,
                  avatar: p.avatar || '/default-avatar.png'
                }
              }
            })
            return parsed
          }
        }
      } catch (e) {
        console.error('Failed to parse cached user pages:', e)
      }
    }
    return []
  }

  const getInitialActivePageToken = (): string | null => {
    if (typeof window !== 'undefined') {
      return localStorage.getItem('auth-page-token')
    }
    return null
  }

  const originalUserId = ref<string>(getInitialOriginalUserId())
  const isActingAsPage = ref<boolean>(getInitialActingAsPage())
  const activePageId = ref<string | null>(getInitialActivePageId())
  const activePageToken = ref<string | null>(getInitialActivePageToken())
  const userPages = ref<Page[]>(getInitialUserPages())

  // Current effective user ID (page ID if acting as page, otherwise original user ID)
  const currentUserId = computed<string | number>({
    get: () => {
      if (isActingAsPage.value && activePageId.value) {
        return activePageId.value
      }
      return originalUserId.value
    },
    set: (val: string | number) => {
      const strVal = String(val)
      originalUserId.value = strVal
      if (typeof window !== 'undefined') {
        localStorage.setItem('auth-original-user-id', strVal)
        localStorage.setItem('auth-current-user-id', strVal)
      }
    }
  })

  // Get current active actor data (User or Page)
  const currentUser = computed((): User | undefined => {
    return getUserById(currentUserId.value)
  })

  // Get original user data
  const originalUser = computed((): User | undefined => {
    return getUserById(originalUserId.value)
  })

  // Get currently active page object
  const activePage = computed((): Page | null => {
    if (activePageId.value) {
      const found = userPages.value.find((p) => p.id === activePageId.value)
      if (found) return found
    }
    return userPages.value[0] || null
  })

  // True if user has any page or is currently in page mode
  const hasPageAccount = computed((): boolean => {
    return isActingAsPage.value || userPages.value.length > 0
  })

  // The alternate account to switch to in ProfileMenu
  const pageAccount = computed(() => {
    if (isActingAsPage.value) {
      // When acting as Page, the other account is the original User
      const orig = originalUser.value
      return {
        id: originalUserId.value,
        name: orig?.name || 'Mój Profil',
        avatar: orig?.avatar || '/default-avatar.png',
        isPage: false
      }
    } else {
      // When acting as User, the other account is the Page
      const pg = activePage.value
      if (pg) {
        return {
          id: pg.id,
          name: pg.name,
          avatar: pg.avatar || '/default-avatar.png',
          isPage: true
        }
      }
      return null
    }
  })

  const savePagesToStorage = () => {
    if (typeof window !== 'undefined') {
      try {
        localStorage.setItem('auth-user-pages', JSON.stringify(userPages.value))
      } catch (e) {
        console.error('Failed to save user pages to localStorage:', e)
      }
    }
  }

  // Register page in local state and cache
  const addPage = (page: Page) => {
    const existingIndex = userPages.value.findIndex((p) => p.id === page.id)
    if (existingIndex >= 0) {
      userPages.value[existingIndex] = { ...userPages.value[existingIndex], ...page }
    } else {
      userPages.value.unshift(page)
    }

    usersCache.value[page.id] = {
      id: page.id,
      name: page.name,
      avatar: page.avatar || '/default-avatar.png'
    }

    savePagesToStorage()
  }

  // Switch context to a specific page with Token Exchange
  const switchToPage = async (pageOrId: string | Page) => {
    const pageId = typeof pageOrId === 'string' ? pageOrId : pageOrId.id
    if (typeof pageOrId !== 'string') {
      addPage(pageOrId)
    }

    const page = userPages.value.find((p) => p.id === pageId)
    if (page) {
      usersCache.value[page.id] = {
        id: page.id,
        name: page.name,
        avatar: page.avatar || '/default-avatar.png'
      }
    }

    isActingAsPage.value = true
    activePageId.value = pageId

    // Request signed Page Access Token from backend (Token Exchange)
    try {
      const res: any = await fetch(`/api/pages/${pageId}/token`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'X-User-Id': originalUserId.value,
        }
      }).then((r) => r.json())

      if (res?.accessToken) {
        activePageToken.value = res.accessToken
        if (typeof window !== 'undefined') {
          localStorage.setItem('auth-page-token', res.accessToken)
        }
      }
    } catch (e) {
      console.warn('Failed to fetch Page Access Token from backend:', e)
    }

    if (typeof window !== 'undefined') {
      localStorage.setItem('auth-is-acting-as-page', 'true')
      localStorage.setItem('auth-active-page-id', pageId)
      localStorage.setItem('auth-current-user-id', pageId)
      try {
        const conversationsStore = useConversationsStore()
        conversationsStore.clearState()
      } catch (err) {
        console.error('Failed to clear state on page switch:', err)
      }
    }
  }

  // Fetch all pages owned by current user
  const fetchUserPages = async () => {
    if (!originalUserId.value || originalUserId.value === '0') return
    try {
      const res = await fetch(`/api/users/${originalUserId.value}/pages`, {
        headers: {
          'X-User-Id': originalUserId.value,
        }
      })
      if (res.ok) {
        const pages: Page[] = await res.json()
        if (Array.isArray(pages)) {
          pages.forEach((p) => addPage(p))

          // If acting as page, ensure token is loaded from Redis if missing
          if (isActingAsPage.value && activePageId.value && !activePageToken.value) {
            try {
              const activeRes: any = await fetch(`/api/pages/active-token`, {
                headers: { 'X-User-Id': originalUserId.value }
              }).then(r => r.json())
              if (activeRes?.active && activeRes.accessToken) {
                activePageToken.value = activeRes.accessToken
                if (typeof window !== 'undefined') {
                  localStorage.setItem('auth-page-token', activeRes.accessToken)
                }
              }
            } catch {}
          }
        }
      }
    } catch (err) {
      console.warn('Could not fetch user pages from backend:', err)
    }
  }

  // Switch context back to original user
  const switchToUser = async () => {
    isActingAsPage.value = false
    activePageId.value = null
    activePageToken.value = null

    // Clear active page session in Redis on backend
    try {
      await fetch(`/api/pages/active-token`, {
        method: 'DELETE',
        headers: {
          'X-User-Id': originalUserId.value,
        }
      })
    } catch {}

    if (typeof window !== 'undefined') {
      localStorage.setItem('auth-is-acting-as-page', 'false')
      localStorage.removeItem('auth-active-page-id')
      localStorage.removeItem('auth-page-token')
      localStorage.setItem('auth-current-user-id', originalUserId.value)
      try {
        const conversationsStore = useConversationsStore()
        conversationsStore.clearState()
      } catch (err) {
        console.error('Failed to clear state on user switch:', err)
      }
    }
  }

  // Toggle between User and Page
  const switchAccount = () => {
    if (isActingAsPage.value) {
      switchToUser()
    } else if (userPages.value.length > 0) {
      switchToPage(userPages.value[0])
    }
  }

  // Set current user explicitly (login or switch user)
  const setCurrentUser = async (userId: string | number, preservePageActor = false) => {
    const strId = String(userId)
    originalUserId.value = strId

    if (preservePageActor && isActingAsPage.value && activePageId.value) {
      if (typeof window !== 'undefined') {
        localStorage.setItem('auth-original-user-id', strId)
      }
      fetchUserPages()
      return
    }

    isActingAsPage.value = false
    activePageId.value = null
    activePageToken.value = null

    if (typeof window !== 'undefined') {
      localStorage.setItem('auth-original-user-id', strId)
      localStorage.setItem('auth-current-user-id', strId)
      localStorage.setItem('auth-is-acting-as-page', 'false')
      localStorage.removeItem('auth-active-page-id')
      localStorage.removeItem('auth-page-token')
      try {
        const conversationsStore = useConversationsStore()
        conversationsStore.clearState()
        console.log('State cleared on user change successfully.')
      } catch (err) {
        console.error('Failed to clear state on user change:', err)
      }
    }
    fetchUserPages()
  }

  // Logout (reset to null/0)
  const logout = () => {
    originalUserId.value = '0'
    isActingAsPage.value = false
    activePageId.value = null
    userPages.value = []

    if (typeof window !== 'undefined') {
      localStorage.removeItem('auth-original-user-id')
      localStorage.removeItem('auth-current-user-id')
      localStorage.removeItem('auth-is-acting-as-page')
      localStorage.removeItem('auth-active-page-id')
      localStorage.removeItem('auth-user-pages')
      try {
        const conversationsStore = useConversationsStore()
        conversationsStore.clearState()
        console.log('State cleared on logout successfully.')
      } catch (err) {
        console.error('Failed to clear state on logout:', err)
      }
    }
  }

  return {
    currentUserId,
    originalUserId,
    isActingAsPage,
    activePageId,
    activePageToken,
    userPages,
    currentUser,
    originalUser,
    activePage,
    hasPageAccount,
    pageAccount,
    addPage,
    switchToPage,
    switchToUser,
    switchAccount,
    fetchUserPages,
    setCurrentUser,
    logout,
  }
})
