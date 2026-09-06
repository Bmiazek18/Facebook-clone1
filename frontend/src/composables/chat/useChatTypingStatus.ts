import { ref, watch, onUnmounted, type Ref } from 'vue'
import { useConversationsStore } from '@/stores/conversations'

export function useChatTypingStatus(boxId: Ref<string | number | undefined>, textInput: Ref<string>) {
  const convStore = useConversationsStore()
  let typingTimeout: any = null
  let isCurrentlyTyping = false

  const sendTypingStatus = (isTyping: boolean) => {
    if (!boxId.value) return
    const cleanId = String(boxId.value).replace(/^user_/, '')
    const conversationId = convStore.getSymmetricConversationId(boxId.value)
    const cleanSenderId = String(convStore.currentUserUuid).replace(/^user_/, '')
    convStore.publishMqtt('chat/messages/user/' + cleanId, {
      type: 'typing',
      conversationId,
      senderId: cleanSenderId,
      isTyping,
    }, { qos: 0 })
  }

  watch(textInput, (newVal) => {
    if (newVal.trim().length > 0) {
      if (!isCurrentlyTyping) {
        isCurrentlyTyping = true
        sendTypingStatus(true)
      }
      if (typingTimeout) clearTimeout(typingTimeout)
      typingTimeout = setTimeout(() => {
        isCurrentlyTyping = false
        sendTypingStatus(false)
      }, 2500)
    } else {
      if (isCurrentlyTyping) {
        isCurrentlyTyping = false
        if (typingTimeout) clearTimeout(typingTimeout)
        sendTypingStatus(false)
      }
    }
  })

  const onFocus = () => {
    if (!isCurrentlyTyping) {
      isCurrentlyTyping = true
      sendTypingStatus(true)
    }
    if (typingTimeout) clearTimeout(typingTimeout)
    typingTimeout = setTimeout(() => {
      isCurrentlyTyping = false
      sendTypingStatus(false)
    }, 4000)
  }

  const onBlur = () => {
    if (isCurrentlyTyping) {
      isCurrentlyTyping = false
      if (typingTimeout) clearTimeout(typingTimeout)
      sendTypingStatus(false)
    }
  }

  onUnmounted(() => {
    if (typingTimeout) clearTimeout(typingTimeout)
    if (isCurrentlyTyping) {
      sendTypingStatus(false)
    }
  })

  return {
    onFocus,
    onBlur,
    sendTypingStatus,
  }
}
