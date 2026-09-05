import { defineStore } from 'pinia'
import { useChatCalls } from '@/composables/chat/useChatCalls'

export const useCallsStore = defineStore('calls', () => {
  const chatCalls = useChatCalls()

  return {
    incomingCall: chatCalls.incomingCall,
    initiateCall: chatCalls.initiateCall,
    endCall: chatCalls.endCall,
    acceptCall: chatCalls.acceptCall,
    rejectCall: chatCalls.rejectCall,
  }
})
