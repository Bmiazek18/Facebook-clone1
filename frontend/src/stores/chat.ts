import { ref, computed } from 'vue'
import { defineStore } from 'pinia'

type ChatId = string | number

export const useChatStore = defineStore(
  'chat',
  () => {
    const activeBoxIds = ref<ChatId[]>([])
    const minimizedBoxCache = ref<ChatId[]>([])
    const unreadBoxIds = ref<ChatId[]>([])

    const getBoxIds = computed(() => activeBoxIds.value)

    const isMinimized = computed(() => (id: ChatId) => {
      const cleanId = String(id).replace(/^user_/, '')
      return minimizedBoxCache.value.some((b) => String(b).replace(/^user_/, '') === cleanId)
    })

    const isUnread = computed(() => (id: ChatId) => {
      const cleanId = String(id).replace(/^user_/, '')
      return unreadBoxIds.value.some((b) => String(b).replace(/^user_/, '') === cleanId)
    })

    function markBoxAsUnread(id: ChatId) {
      const cleanId = String(id).replace(/^user_/, '')
      if (!unreadBoxIds.value.some((b) => String(b).replace(/^user_/, '') === cleanId)) {
        unreadBoxIds.value.push(id)
      }
    }

    function markBoxAsRead(id: ChatId) {
      const cleanId = String(id).replace(/^user_/, '')
      unreadBoxIds.value = unreadBoxIds.value.filter(
        (b) => String(b).replace(/^user_/, '') !== cleanId,
      )
    }

    function addMessageBox(id: ChatId, isUnreadFlag = false) {
      const cleanId = String(id).replace(/^user_/, '')
      if (!activeBoxIds.value.some((b) => String(b).replace(/^user_/, '') === cleanId)) {
        if (activeBoxIds.value.length >= 5) {
          activeBoxIds.value.shift()
        }
        activeBoxIds.value.push(id)
      }

      const cacheIndex = minimizedBoxCache.value.findIndex(
        (b) => String(b).replace(/^user_/, '') === cleanId,
      )
      if (cacheIndex > -1) {
        minimizedBoxCache.value.splice(cacheIndex, 1)
      }

      if (isUnreadFlag) {
        markBoxAsUnread(id)
      } else {
        markBoxAsRead(id)
      }
    }

    function removeMessageBox(id: ChatId) {
      const cleanId = String(id).replace(/^user_/, '')
      activeBoxIds.value = activeBoxIds.value.filter(
        (boxId) => String(boxId).replace(/^user_/, '') !== cleanId,
      )
      minimizedBoxCache.value = minimizedBoxCache.value.filter(
        (boxId) => String(boxId).replace(/^user_/, '') !== cleanId,
      )
      markBoxAsRead(id)
    }

    function removeAllBoxes() {
      activeBoxIds.value = []
      minimizedBoxCache.value = []
      unreadBoxIds.value = []
    }

    function toggleMinimize(id: ChatId) {
      const cleanId = String(id).replace(/^user_/, '')
      const index = activeBoxIds.value.findIndex(
        (b) => String(b).replace(/^user_/, '') === cleanId,
      )

      if (index > -1) {
        activeBoxIds.value.splice(index, 1)
        if (!minimizedBoxCache.value.some((b) => String(b).replace(/^user_/, '') === cleanId)) {
          minimizedBoxCache.value.push(id)
        }
      } else {
        activeBoxIds.value.push(id)
        const cacheIndex = minimizedBoxCache.value.findIndex(
          (b) => String(b).replace(/^user_/, '') === cleanId,
        )
        if (cacheIndex > -1) {
          minimizedBoxCache.value.splice(cacheIndex, 1)
        }
      }
    }

    return {
      activeBoxIds,
      minimizedBoxCache,
      unreadBoxIds,
      getBoxIds,
      isMinimized,
      isUnread,
      markBoxAsUnread,
      markBoxAsRead,
      addMessageBox,
      removeMessageBox,
      removeAllBoxes,
      toggleMinimize,
    }
  },
  {
    persist: {
      storage: {
        getItem: (key: string) => (import.meta.client ? localStorage.getItem(key) : null),
        setItem: (key: string, value: string) => {
          if (import.meta.client) {
            localStorage.setItem(key, value)
          }
        },
      },
      key: 'chat-boxes-state',
      pick: ['activeBoxIds', 'minimizedBoxCache', 'unreadBoxIds'],
    },
  },
)
