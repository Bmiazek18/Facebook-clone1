import { defineNuxtRouteMiddleware, useHead } from '#imports'
import { useAuthStore } from '@/stores/auth'
import { useGroupsStore } from '@/stores/groups'
import { useConversationsStore } from '@/stores/conversations'

export default defineNuxtRouteMiddleware((to) => {
  // Prefer explicit page meta title, then route meta.title
  let pageTitle = (to.meta && (to.meta.title as string)) || ''

  if (!pageTitle) {
    const path = to.path
    const authStore = useAuthStore()
    const groupsStore = useGroupsStore()
    const convStore = useConversationsStore()

    if (path === '/') {
      pageTitle = 'Facebook'
    } else if (path.startsWith('/chat')) {
      const chatId = to.params.chatId as string
      if (chatId) {
        const chat = convStore.chats.find((c) => String(c.id) === String(chatId))
        if (chat?.name) {
          pageTitle = `${chat.name} | Facebook`
        } else {
          // Return early to let components handle it when data is loaded
          return
        }
      } else {
        pageTitle = 'Czat | Facebook'
      }
    } else if (path.startsWith('/friends')) {
      pageTitle = 'Znajomi | Facebook'
    } else if (path.startsWith('/groups')) {
      const groupId = to.params.id as string
      if (groupId) {
        const group = groupsStore.getGroupById(groupId)
        if (group?.name) {
          pageTitle = `${group.name} | Facebook`
        } else {
          // Return early to let components handle it when data is loaded
          return
        }
      } else {
        pageTitle = 'Grupy | Facebook'
      }
    } else if (path.startsWith('/marketplace')) {
      pageTitle = 'Marketplace | Facebook'
    } else if (path.startsWith('/search')) {
      pageTitle = 'Szukaj | Facebook'
    } else if (path.startsWith('/video-call')) {
      pageTitle = 'Rozmowa wideo | Facebook'
    } else if (path.startsWith('/video') || path.startsWith('/watch')) {
      pageTitle = 'Wideo | Facebook'
    } else if (path.startsWith('/events')) {
      pageTitle = 'Wydarzenia | Facebook'
    } else if (path.startsWith('/stories')) {
      pageTitle = 'Relacje | Facebook'
    } else if (path.startsWith('/profile')) {
      const userId = to.params.userId as string
      if (!userId) {
        const name = authStore.currentUser?.name || [authStore.currentUser?.firstName, authStore.currentUser?.lastName].filter(Boolean).join(' ')
        pageTitle = name ? `${name} | Facebook` : 'Profil | Facebook'
      } else {
        // Return early to let ProfileView fetch user and set title
        return
      }
    } else if (path.startsWith('/add-group')) {
      pageTitle = 'Utwórz grupę | Facebook'
    } else if (path.startsWith('/live')) {
      pageTitle = 'Transmisja na żywo | Facebook'
    } else {
      pageTitle = 'Facebook'
    }
  }

  useHead({ title: pageTitle })
})

