import { ref } from 'vue'

export function useChatCalls() {
  const router = useRouter()
  const config = useRuntimeConfig()
  const apiUrl = config.public.apiUrl

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

  function acceptCall() {
    if (!incomingCall.value) return
    const call = incomingCall.value
    incomingCall.value = null

    const routeData = router.resolve({
      name: 'video-call',
      query: {
        type: call.callType,
        boxId: call.callerId,
        callerId: call.callerId,
        conversationId: call.conversationId,
      },
    })

    if (typeof window !== 'undefined') {
      window.open(
        routeData.href,
        `Rozmowa_${call.conversationId}`,
        'popup=yes,width=900,height=650,left=300,top=150,resizable=yes,location=no,toolbar=no,menubar=no',
      )
    }
  }

  async function rejectCall(
    currentUserId: string,
    isMqttConnected: boolean,
    publishMqtt: (topic: string, payload: any) => void
  ) {
    if (!incomingCall.value) return
    const call = incomingCall.value
    incomingCall.value = null

    try {
      await $fetch(`${apiUrl}/api/chat/calls/log`, {
        method: 'POST',
        query: {
          conversationId: call.conversationId,
          senderId: currentUserId,
          callerId: call.callerId,
          duration: 0,
          status: 'rejected',
          participantIds: [currentUserId, call.callerId].join(','),
        },
      })
    } catch (err) {
      console.error('Failed to log call rejection:', err)
    }

    if (isMqttConnected) {
      const payload = {
        type: 'call_rejected',
        conversationId: call.conversationId,
        senderId: currentUserId,
        callerId: call.callerId,
        participantIds: [currentUserId, call.callerId],
      }
      publishMqtt('chat/messages/user/' + call.callerId, payload)
    }
  }

  return {
    incomingCall,
    initiateCall,
    endCall,
    acceptCall,
    rejectCall
  }
}
