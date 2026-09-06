<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute } from '#imports'
import AgoraRTC from 'agora-rtc-sdk-ng'
import type {
  IAgoraRTCClient,
  IMicrophoneAudioTrack,
  ILocalVideoTrack,
  IAgoraRTCRemoteUser,
} from 'agora-rtc-sdk-ng'
import CallSummary from '../components/CallSummary.vue'
import CallRingingScreen from '@/components/call/CallRingingScreen.vue'
import CallControlBar from '@/components/call/CallControlBar.vue'
import AddCallParticipantModal, { type ContactItem } from '@/components/call/AddCallParticipantModal.vue'
import { useVirtualBackground } from '@/composables/call/useVirtualBackground'
import { useAuthStore } from '@/stores/auth'
import { useConversationsStore } from '@/stores/conversations'

const route = useRoute()

const connectionType = ref(route.query.type || 'video')
const rawBoxId = route.query.boxId || 'domyslny-kanal'
const rawConversationId = route.query.conversationId || route.query.boxId || 'domyslny-kanal'

const boxId = ref(String(rawBoxId).replace(/^user_/, ''))
const conversationId = ref(String(rawConversationId).replace(/^user_/, ''))

let heartbeatTimer: any = null
let checkAloneTimer: any = null

function getNumericUid(uuidStr: string): number {
  let hash = 0
  for (let i = 0; i < uuidStr.length; i++) {
    const character = uuidStr.charCodeAt(i)
    hash = (hash << 5) - hash + character
    hash = hash & hash
  }
  return Math.abs(hash)
}

let agoraClient: IAgoraRTCClient | null = null
let localAudioTrack: IMicrophoneAudioTrack | null = null
let localVideoTrack: ILocalVideoTrack | null = null
let screenVideoTrack: any = null
let callStartTime: number | null = null
let hasLoggedCall = false

// Dodany stan połączenia: dzwonienie / połączono
const callState = ref<'ringing' | 'connected'>('ringing')

const remoteVideoUser = ref<IAgoraRTCRemoteUser | null>(null)
const remoteVideoRef = ref<HTMLDivElement | null>(null)

const videoRef = ref<HTMLVideoElement | null>(null)
const canvasContainerRef = ref<HTMLDivElement | null>(null)
const fileInputRef = ref<HTMLInputElement | null>(null)

const videoEnabled = ref(false)
const microphoneEnabled = ref(true)
const showCamera = ref(true)
const remoteUserSpeaking = ref(false)
const isSharingScreen = ref(false)
const statusMessage = ref('Inicjalizacja systemu...')

const currentStep = ref<'call' | 'summary'>('call')

const conversationsStore = useConversationsStore()
const authStore = useAuthStore()

// --- VIRTUAL BACKGROUND COMPOSABLE ---
const {
  currentFilter,
  filterMenuOpen,
  initThree,
  setFilterType,
  handleImageUpload,
  renderLoop,
  stopRenderLoop,
  getCanvasStream,
} = useVirtualBackground()

const myName = computed(() => {
  return authStore.currentUser?.name || 'Ja'
})

const myAvatar = computed(() => {
  return authStore.currentUser?.avatar || '/default-avatar.png'
})

const recipientId = computed(() => String(boxId.value).replace(/^user_/, ''))

const recipientName = computed(() => {
  const user = conversationsStore.usersCache[recipientId.value]
  if (user) return user.name
  conversationsStore.getOrFetchUser(recipientId.value)
  return 'Ładowanie...'
})

const recipientAvatar = computed(() => {
  const user = conversationsStore.usersCache[recipientId.value]
  if (user && user.avatar) return user.avatar
  conversationsStore.getOrFetchUser(recipientId.value)
  return '/default-avatar.png'
})

// --- DODAWANIE OSÓB DO ROZMOWY I TWORZENIE GRUPY ---
const isAddUserModalOpen = ref(false)
const isAddingUser = ref(false)
const callNotificationToast = ref<string | null>(null)

const contactsList = ref<ContactItem[]>([
  {
    id: '7f23f5b8-87fb-4250-9ba9-6b5ed04afff0',
    name: 'dsd User',
    avatarUrl: 'https://i.pravatar.cc/150?img=1'
  },
  {
    id: 'd8604ec9-2999-4730-9409-d4c13a78a68e',
    name: 'E2EE Partner',
    avatarUrl: 'https://i.pravatar.cc/150?img=2'
  },
  {
    id: '0d4b14bc-1337-490f-ba79-27b62f4fdaf6',
    name: 'Bmiazek User',
    avatarUrl: 'https://i.pravatar.cc/150?img=3'
  },
  {
    id: '41da76f0-fc3e-362a-a939-e634bfb6a342',
    name: 'Piotr Kowalski',
    avatarUrl: 'https://i.pravatar.cc/150?img=4'
  },
  {
    id: '9a936f54-ceff-3813-9eba-fd21984efcf4',
    name: 'Tomasz Lewandowski',
    avatarUrl: 'https://i.pravatar.cc/150?img=5'
  }
])

const callParticipants = ref<string[]>([
  String(authStore.currentUser?.id || authStore.currentUserId || '').replace(/^user_/, ''),
  recipientId.value
])

const inviteUserToCall = async (user: ContactItem) => {
  try {
    isAddingUser.value = true
    const currentUserId = String(authStore.currentUser?.id || authStore.currentUserId || '').replace(/^user_/, '')
    const newUserId = String(user.id).replace(/^user_/, '')

    if (!callParticipants.value.includes(newUserId)) {
      callParticipants.value.push(newUserId)
    }

    const allParticipantIds = [...new Set(callParticipants.value)].filter(Boolean)
    const activeCallChannel = conversationId.value.toString()

    let groupConvId = activeCallChannel
    const isPrivateCall = !groupConvId.startsWith('group_') && allParticipantIds.length > 2
    if (isPrivateCall) {
      groupConvId = `group_${Date.now()}_${Math.random().toString(36).substring(2, 7)}`
    }

    const groupMembers = allParticipantIds.map(pId => {
      const cached = conversationsStore.usersCache[pId]
      const foundContact = contactsList.value.find(c => c.id === pId)
      return {
        id: pId,
        name: cached?.name || foundContact?.name || (pId === currentUserId ? myName.value : 'Użytkownik'),
        avatar: cached?.avatar || foundContact?.avatarUrl || '/default-avatar.png'
      }
    })

    let existingChat = conversationsStore.chats.find(c => String(c.id) === String(groupConvId))
    if (!existingChat) {
      existingChat = {
        id: groupConvId,
        type: 'group' as any,
        name: groupMembers.filter(m => m.id !== currentUserId).map(m => m.name).join(', ') || 'Grupa wideo',
        avatarUrl: '/default-avatar.png',
        groupMembers: groupMembers as any,
        unread: false,
        isActive: true,
        timeAgo: 'Teraz',
      }
      conversationsStore.chats.unshift(existingChat)
    } else {
      existingChat.groupMembers = groupMembers as any
      existingChat.name = groupMembers.filter(m => m.id !== currentUserId).map(m => m.name).join(', ') || existingChat.name
    }

    const apiBase = (import.meta.env?.VITE_BFF_API_URL as string) || 'http://localhost:8080'
    await $fetch(`${apiBase}/api/chat/calls/invite`, {
      method: 'POST',
      query: {
        conversationId: groupConvId,
        senderId: currentUserId,
        invitedUserId: newUserId,
        channelName: activeCallChannel,
        participantIds: allParticipantIds.join(',')
      }
    }).catch(err => console.warn('Failed to post invite to API:', err))

    if (conversationsStore.isMqttConnected) {
      for (const pId of allParticipantIds) {
        conversationsStore.publishMqtt(`chat/messages/user/${pId}`, {
          type: 'call_group',
          systemActionType: 'call_started',
          systemActionPayload: `channel:${activeCallChannel}`,
          conversationId: groupConvId,
          channelName: activeCallChannel,
          senderId: currentUserId,
          callerId: currentUserId,
          callType: connectionType.value || 'video',
          participantIds: allParticipantIds,
          text: `SYSTEM_ACTION:call_started:channel:${activeCallChannel}`
        })
      }
    }

    callNotificationToast.value = `Dodano ${user.name} do rozmowy i utworzono grupę!`
    setTimeout(() => {
      callNotificationToast.value = null
    }, 4000)

    isAddUserModalOpen.value = false
  } catch (err) {
    console.error('Błąd podczas dodawania użytkownika do rozmowy:', err)
  } finally {
    isAddingUser.value = false
  }
}

const changeFilter = (type: 'none' | 'blur' | 'image') => {
  if (type === 'image' && fileInputRef.value) {
    fileInputRef.value.click()
    return
  }
  setFilterType(type)
}

const initializeAgoraAndAI = async () => {
  agoraClient = AgoraRTC.createClient({ mode: 'rtc', codec: 'vp8' })
  agoraClient.enableAudioVolumeIndicator()

  const callerId = String(route.query.callerId || '').replace(/^user_/, '')
  const currentUserId = String(authStore.currentUser?.id || authStore.currentUserId || '').replace(/^user_/, '')
  const apiBase = (import.meta.env?.VITE_BFF_API_URL as string) || 'http://localhost:8080'
  
  if (callerId && callerId !== currentUserId) {
    callState.value = 'connected'
    if (!callStartTime) {
      callStartTime = Date.now()
    }
  }

  agoraClient.on('volume-indicator', (volumes) => {
    volumes.forEach((vol) => {
      if (vol.uid !== agoraClient?.uid && vol.level > 10) {
        remoteUserSpeaking.value = true
        setTimeout(() => {
          remoteUserSpeaking.value = false
        }, 800)
      }
    })
  })

  agoraClient.on('user-published', async (user, mediaType) => {
    await agoraClient?.subscribe(user, mediaType)

    callState.value = 'connected'
    if (!callStartTime) {
      callStartTime = Date.now()
    }

    if (mediaType === 'video') {
      remoteVideoUser.value = user
      setTimeout(() => {
        if (remoteVideoRef.value) {
          user.videoTrack?.play(remoteVideoRef.value)
        }
      }, 100)
    }

    if (mediaType === 'audio') {
      user.audioTrack?.play()
    }
  })

  agoraClient.on('user-unpublished', (user, mediaType) => {
    if (mediaType === 'video' && remoteVideoUser.value?.uid === user.uid) {
      remoteVideoUser.value = null
    }
  })

  agoraClient.on('user-left', async () => {
    remoteVideoUser.value = null
    await handleDisconnect()
  })

  const channel = conversationId.value.toString()
  const uid = getNumericUid(currentUserId || 'default')

  const tokenPromise = $fetch<{
    token: string
    channelName: string
    uid: number
    appId: string
  }>(`${apiBase}/api/chat/agora/token`, {
    params: { channelName: channel, uid },
  })

  const audioTrackPromise = AgoraRTC.createMicrophoneAudioTrack().catch((err) => {
    console.warn('Mikrofon niedostępny:', err)
    return null
  })

  const agoraJoinPromise = tokenPromise.then(async (tokenData) => {
    if (!agoraClient) return tokenData
    await agoraClient.join(tokenData.appId, tokenData.channelName, tokenData.token || null, tokenData.uid)
    if (agoraClient.remoteUsers.length > 0) {
      callState.value = 'connected'
      if (!callStartTime) {
        callStartTime = Date.now()
      }
    }

    heartbeatTimer = setInterval(async () => {
      try {
        await $fetch(`${apiBase}/api/chat/calls/heartbeat`, {
          method: 'POST',
          query: { conversationId: conversationId.value.toString() }
        })
      } catch (hbErr) {
        console.error('Failed to send call heartbeat:', hbErr)
      }
    }, 12000)

    checkAloneTimer = setInterval(async () => {
      if (callState.value === 'connected' && agoraClient && agoraClient.remoteUsers.length === 0) {
        console.log('Jesteś sam w pokoju po połączeniu. Rozłączanie...')
        await handleDisconnect()
      }
    }, 2000)

    return tokenData
  }).catch((tokenErr) => {
    console.error('Failed to fetch Agora token / join:', tokenErr)
    statusMessage.value = 'Błąd pobierania tokenu autoryzacji.'
    return null
  })

  Promise.all([agoraJoinPromise, audioTrackPromise]).then(async ([, micTrack]) => {
    if (micTrack && agoraClient) {
      localAudioTrack = micTrack
      try {
        await agoraClient.publish([localAudioTrack])
        statusMessage.value = 'Połączono (audio gotowe)'
      } catch (pubErr) {
        console.warn('Błąd publikacji mikrofonu:', pubErr)
      }
    }
  })

  try {
    const stream = await navigator.mediaDevices.getUserMedia({
      video: { width: { ideal: 640 }, height: { ideal: 480 }, facingMode: 'user' },
      audio: false,
    })

    if (videoRef.value) {
      videoRef.value.srcObject = stream
      await new Promise((resolve) => {
        videoRef.value!.onloadedmetadata = () => resolve(true)
      })
      await videoRef.value.play()
    }

    if (canvasContainerRef.value && videoRef.value) {
      initThree(canvasContainerRef.value, videoRef.value)
    }

    videoEnabled.value = true
    if (videoRef.value) {
      renderLoop(videoRef.value)
    }

    const canvasStream = getCanvasStream()
    if (canvasStream) {
      const processedVideoTrack = canvasStream.getVideoTracks()[0]
      if (processedVideoTrack) {
        localVideoTrack = AgoraRTC.createCustomVideoTrack({ mediaStreamTrack: processedVideoTrack })
        await localVideoTrack.setEncoderConfiguration({
          width: 640,
          height: 480,
          frameRate: 30,
          bitrateMin: 400,
          bitrateMax: 1200,
        })

        await agoraJoinPromise
        if (agoraClient && localVideoTrack && videoEnabled.value && !isSharingScreen.value) {
          await agoraClient.publish([localVideoTrack])
        }
      }
    }

    statusMessage.value = 'Połączono bezpiecznie!'
  } catch (videoErr) {
    console.warn('Kamera niedostępna lub błąd:', videoErr)
    statusMessage.value = 'Połączono (audio)'
  }
}

const toggleScreenShare = async () => {
  if (!agoraClient) return

  try {
    if (!isSharingScreen.value) {
      statusMessage.value = 'Uruchamianie udostępniania ekranu...'

      screenVideoTrack = await AgoraRTC.createScreenVideoTrack(
        { encoderConfig: '1080p_1' },
        'auto',
      )

      screenVideoTrack.on('track-ended', () => {
        stopScreenShare()
      })

      if (localVideoTrack) {
        await agoraClient.unpublish([localVideoTrack])
      }
      await agoraClient.publish([screenVideoTrack])

      isSharingScreen.value = true
      statusMessage.value = 'Udostępniasz ekran'
    } else {
      await stopScreenShare()
    }
  } catch (err) {
    console.error('Błąd udostępniania ekranu:', err)
    statusMessage.value = 'Nie udało się udostępnić ekranu.'
  }
}

const stopScreenShare = async () => {
  if (!agoraClient || !isSharingScreen.value) return

  try {
    if (screenVideoTrack) {
      screenVideoTrack.close()
      await agoraClient.unpublish([screenVideoTrack])
      screenVideoTrack = null
    }

    if (localVideoTrack && videoEnabled.value) {
      await agoraClient.publish([localVideoTrack])
    }

    isSharingScreen.value = false
    statusMessage.value = 'Połączono bezpiecznie!'
  } catch (err) {
    console.error('Błąd zatrzymywania udostępniania ekranu:', err)
  }
}

const toggleVideo = async () => {
  if (videoEnabled.value) {
    videoEnabled.value = false
    stopRenderLoop()
    if (agoraClient && localVideoTrack && !isSharingScreen.value) {
      await agoraClient.unpublish([localVideoTrack])
    }
  } else {
    videoEnabled.value = true
    if (videoRef.value) {
      renderLoop(videoRef.value)
    }
    if (agoraClient && localVideoTrack && !isSharingScreen.value) {
      await agoraClient.publish([localVideoTrack])
    }
  }
}

const toggleMute = async () => {
  if (localAudioTrack) {
    await localAudioTrack.setEnabled(!microphoneEnabled.value)
    microphoneEnabled.value = !microphoneEnabled.value
  }
}

const handleDisconnect = async () => {
  if (heartbeatTimer) {
    clearInterval(heartbeatTimer)
    heartbeatTimer = null
  }
  if (checkAloneTimer) {
    clearInterval(checkAloneTimer)
    checkAloneTimer = null
  }

  const duration = callStartTime ? Math.round((Date.now() - callStartTime) / 1000) : 0
  const currentUserId = String(authStore.currentUser?.id || authStore.currentUserId || '').replace(/^user_/, '')
  const callerId = String(route.query.callerId || '').replace(/^user_/, '')
  const isCaller = !callerId || callerId === currentUserId
  const actualCallerId = isCaller ? currentUserId : callerId

  const cleanConvId = conversationId.value.toString().replace(/^user_/, '')
  const cleanBoxId = boxId.value.toString().replace(/^user_/, '')
  const apiBase = (import.meta.env?.VITE_BFF_API_URL as string) || 'http://localhost:8080'

  try {
    await $fetch(`${apiBase}/api/chat/calls/end`, {
      method: 'POST',
      query: { conversationId: cleanConvId }
    })
  } catch (endErr) {
    console.error('Failed to end call state in Redis:', endErr)
  }

  if (!hasLoggedCall) {
    hasLoggedCall = true
    try {
      await $fetch(`${apiBase}/api/chat/calls/log`, {
        method: 'POST',
        query: {
          conversationId: cleanConvId,
          senderId: currentUserId,
          callerId: actualCallerId,
          duration: duration,
          status: duration > 0 ? 'completed' : 'rejected',
          participantIds: [currentUserId, cleanBoxId].join(',')
        }
      })
    } catch (logErr) {
      console.error('Failed to log call outcome to DB:', logErr)
    }
  }

  videoEnabled.value = false
  stopRenderLoop()

  if (screenVideoTrack) screenVideoTrack.close()
  if (localAudioTrack) {
    localAudioTrack.stop()
    localAudioTrack.close()
  }
  if (localVideoTrack) {
    localVideoTrack.stop()
    localVideoTrack.close()
  }
  if (agoraClient) await agoraClient.leave()

  if (videoRef.value && videoRef.value.srcObject) {
    const stream = videoRef.value.srcObject as MediaStream
    stream.getTracks().forEach((track) => track.stop())
  }

  currentStep.value = 'summary'
}

onMounted(() => {
  initializeAgoraAndAI()
})

onUnmounted(() => {
  if (currentStep.value === 'call') {
    handleDisconnect()
  }
})

const reconnectCall = () => {
  window.location.reload()
}

const closeAppWindow = () => {
  window.close()
}
</script>

<template>
  <div v-if="currentStep === 'call'" class="relative w-full h-screen bg-black text-white overflow-hidden">
    <!-- 1. EKRAN "DZWONIENIE..." -->
    <CallRingingScreen
      v-if="callState === 'ringing'"
      :recipient-name="recipientName"
      :recipient-avatar="recipientAvatar"
      :video-enabled="videoEnabled"
      :microphone-enabled="microphoneEnabled"
      @open-add-user="isAddUserModalOpen = true"
      @toggle-video="toggleVideo"
      @toggle-mute="toggleMute"
      @disconnect="handleDisconnect"
    />

    <!-- 2. WŁAŚCIWY EKRAN ROZMOWY (włączany, gdy callState === 'connected') -->
    <div class="absolute inset-0 flex items-center justify-center">
      <input ref="fileInputRef" type="file" accept="image/*" class="hidden" @change="handleImageUpload" />

      <!-- Minimalistyczne info o połączeniu -->
      <div v-show="callState === 'connected'" class="absolute top-4 left-4 flex items-center gap-3 z-20">
        <div class="w-10 h-10 rounded-full overflow-hidden bg-zinc-800">
          <img :src="recipientAvatar" :alt="$t('chat.avatar')" class="w-full h-full object-cover" />
        </div>
        <div>
          <h2 class="text-sm font-semibold tracking-wide">{{ recipientName }}</h2>
          <div class="text-xs text-zinc-400 flex items-center gap-1 mt-0.5">
            <span class="inline-block w-2 h-2 rounded-full bg-green-500 animate-pulse"></span>
            {{ statusMessage }}
          </div>
        </div>
      </div>

      <div class="relative h-full w-full max-w-[500px] bg-black flex items-center justify-center overflow-hidden">
        <img
          :src="recipientAvatar"
          :alt="$t('call.blurBackground')"
          class="absolute inset-0 w-full h-full object-cover blur-3xl scale-110 opacity-40"
        />
        <div class="absolute inset-0 bg-black/50"></div>

        <div
          v-show="remoteVideoUser"
          ref="remoteVideoRef"
          class="absolute inset-0 w-full h-full object-cover z-10"
        ></div>

        <div
          v-show="!remoteVideoUser"
          class="w-24 h-24 rounded-full overflow-hidden shadow-lg bg-zinc-800 flex items-center justify-center relative z-10 transition-all duration-200 ease-in-out"
          :class="remoteUserSpeaking ? 'ring-[6px] ring-green-500 scale-105' : 'ring-1 ring-zinc-600/30'"
        >
          <img :src="recipientAvatar" :alt="$t('chat.avatar')" class="w-full h-full object-cover" />
        </div>

        <div
          v-show="remoteVideoUser && remoteUserSpeaking"
          class="absolute inset-0 border-4 border-green-500 pointer-events-none z-20 transition-all"
        ></div>
      </div>

      <!-- Kamera lokalna -->
      <div
        v-show="connectionType === 'video' && callState === 'connected'"
        class="absolute bottom-6 right-6 z-20 flex items-center gap-3"
      >
        <button
          @click="showCamera = !showCamera"
          class="w-8 h-8 bg-zinc-800/80 hover:bg-zinc-700 backdrop-blur-md rounded-full flex items-center justify-center transition-colors text-white cursor-pointer"
        >
          <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
            <path v-if="showCamera" stroke-linecap="round" stroke-linejoin="round" d="M9 5l7 7-7 7" />
            <path v-else stroke-linecap="round" stroke-linejoin="round" d="M15 19l-7-7 7-7" />
          </svg>
        </button>

        <div class="w-48 aspect-[16/10] bg-zinc-900 rounded-xl overflow-hidden shadow-2xl border border-white/10 relative" v-show="showCamera">
          <video
            ref="videoRef"
            autoplay
            playsinline
            muted
            width="640"
            height="480"
            style="display: none"
          ></video>
          <div ref="canvasContainerRef" v-show="videoEnabled && !isSharingScreen" class="w-full h-full"></div>

          <div v-show="isSharingScreen" class="absolute inset-0 flex flex-col items-center justify-center gap-2 bg-zinc-950 text-green-400 p-2">
            <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6 animate-pulse" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
              <path stroke-linecap="round" stroke-linejoin="round" d="M9.75 17L9 20l-1 1h8l-1-1-.75-3M3 13h18M5 17h14a2 2 0 002-2V5a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z" />
            </svg>
            <p class="text-[10px] uppercase font-bold tracking-wider">{{ $t('call.ekranWysylany') }}</p>
          </div>

          <div v-show="!videoEnabled && !isSharingScreen" class="absolute inset-0 flex flex-col items-center justify-center gap-2 p-4 bg-zinc-950 text-zinc-500 rounded-xl">
            <p class="text-xs">{{ $t('call.kameraWylaczona') }}</p>
          </div>
        </div>
      </div>

      <!-- Pasek sterowania rozmową -->
      <CallControlBar
        v-show="callState === 'connected'"
        :microphone-enabled="microphoneEnabled"
        :video-enabled="videoEnabled"
        :is-sharing-screen="isSharingScreen"
        :filter-menu-open="filterMenuOpen"
        :current-filter="currentFilter"
        @toggle-mute="toggleMute"
        @toggle-video="toggleVideo"
        @toggle-screenshare="toggleScreenShare"
        @open-add-user="isAddUserModalOpen = true"
        @toggle-filter-menu="filterMenuOpen = !filterMenuOpen"
        @change-filter="changeFilter"
        @disconnect="handleDisconnect"
      />
    </div>

    <!-- Modal dodawania uczestnika -->
    <AddCallParticipantModal
      :is-open="isAddUserModalOpen"
      :contacts="contactsList"
      :current-participants="callParticipants"
      :is-adding="isAddingUser"
      @close="isAddUserModalOpen = false"
      @invite="inviteUserToCall"
    />

    <!-- Toast powiadomienia -->
    <transition
      enter-active-class="transition duration-300 ease-out"
      enter-from-class="transform translate-y-4 opacity-0"
      enter-to-class="transform translate-y-0 opacity-100"
      leave-active-class="transition duration-200 ease-in"
      leave-from-class="transform translate-y-0 opacity-100"
      leave-to-class="transform translate-y-4 opacity-0"
    >
      <div
        v-if="callNotificationToast"
        class="fixed top-5 right-5 z-[210] bg-[#0084FF] text-white px-4 py-3 rounded-xl shadow-2xl flex items-center gap-2 font-medium text-sm border border-white/10"
      >
        <svg class="w-5 h-5 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
        </svg>
        {{ callNotificationToast }}
      </div>
    </transition>
  </div>

  <CallSummary v-else @reconnect="reconnectCall" @close="closeAppWindow" />
</template>

<style scoped>
:deep(canvas) {
  width: 100% !important;
  height: 100% !important;
  object-fit: cover;
  border-radius: 0.75rem;
}
</style>
