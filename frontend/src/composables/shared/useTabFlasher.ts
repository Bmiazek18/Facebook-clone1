import { ref } from 'vue'

export function useTabFlasher() {
  let titleFlashInterval: any = null
  let originalTitle = 'Facebook'

  const startTitleFlashing = () => {
    if (typeof document === 'undefined') return
    if (titleFlashInterval) return

    originalTitle = document.title || 'Facebook'
    let showMessage = true
    titleFlashInterval = setInterval(() => {
      if (showMessage) {
        document.title = '💬 Nowa wiadomość!'
      } else {
        document.title = originalTitle
      }
      showMessage = !showMessage
    }, 1200)
  }

  const stopTitleFlashing = () => {
    if (titleFlashInterval) {
      clearInterval(titleFlashInterval)
      titleFlashInterval = null
    }
    if (typeof document !== 'undefined' && document.title !== originalTitle) {
      document.title = originalTitle
    }
  }

  const handleNewChatMessage = () => {
    if (typeof document !== 'undefined' && !document.hasFocus()) {
      startTitleFlashing()
    }
  }

  const initTabFlasher = () => {
    if (typeof window !== 'undefined') {
      window.addEventListener('focus', stopTitleFlashing)
      window.addEventListener('click', stopTitleFlashing)
      window.addEventListener('keydown', stopTitleFlashing)
      window.addEventListener('new-chat-message', handleNewChatMessage)
    }
  }

  const destroyTabFlasher = () => {
    if (typeof window !== 'undefined') {
      window.removeEventListener('focus', stopTitleFlashing)
      window.removeEventListener('click', stopTitleFlashing)
      window.removeEventListener('keydown', stopTitleFlashing)
      window.removeEventListener('new-chat-message', handleNewChatMessage)
    }
    stopTitleFlashing()
  }

  return {
    initTabFlasher,
    destroyTabFlasher,
    stopTitleFlashing
  }
}
