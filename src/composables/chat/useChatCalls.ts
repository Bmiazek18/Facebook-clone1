import { ref } from 'vue'

export function useChatCalls() {
  const incomingCall = ref<{
    conversationId: string
    callerId: string
    callType: 'audio' | 'video'
    callerName: string
    callerAvatar: string
  } | null>(null)

  function initiateCall(conversationId: string, callerId: string, callType: 'audio' | 'video', callerName: string, callerAvatar: string) {
    incomingCall.value = {
      conversationId,
      callerId,
      callType,
      callerName,
      callerAvatar
    }
  }

  function endCall() {
    incomingCall.value = null
  }

  return {
    incomingCall,
    initiateCall,
    endCall
  }
}
