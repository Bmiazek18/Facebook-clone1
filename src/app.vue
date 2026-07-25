<template>
  <MainNavLayout v-if="showMainLayout && !isPopupRoute" />

  <NuxtPage />

  <!-- Global Incoming Call Modal -->
  <IncomingCallModal
    v-if="conversationsStore.incomingCall"
    :isOpen="true"
    :callerName="conversationsStore.incomingCall.callerName"
    :callerAvatar="conversationsStore.incomingCall.callerAvatar"
    @close="conversationsStore.incomingCall = null"
    @reject="handleIncomingCallReject"
    @accept="handleIncomingCallAccept"
  />

  <!-- Kontener na dokowane okienka czatu -->
  <div
    v-if="!isInChatView && !isPopupRoute"
    class="fixed flex flex-row-reverse items-end bottom-0 right-[80px] gap-2.5 z-40 pointer-events-none"
  >
    <!-- Główne okna czatu (MessageBox) z Pinii -->
    <MessageBox
      v-for="boxId in chatStore.getBoxIds"
      :key="boxId"
      :boxId="boxId"
      class="pointer-events-auto"
    />

    <!-- Okno "Nowa wiadomość" (pojawia się obok czatów) -->
    <NewChatBox
      v-if="isNewChatBoxOpen"
      @close="isNewChatBoxOpen = false"
      @select-user="handleStartNewChat"
      class="pointer-events-auto"
    />
  </div>

  <ProfileIcon v-if="showMainLayout && !isInChatView && !isPopupRoute" @click="isNewChatBoxOpen = true" />

  <ClientOnly>
    <FingerprintLoader />
  </ClientOnly>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted, onUnmounted } from 'vue'
import { useOnline } from '@vueuse/core'
import { useMutation } from '@vue/apollo-composable'
import gql from 'graphql-tag'

import MainNavLayout from './layouts/MainNavLayouts.vue'
import ProfileIcon from './components/profile/ProfileIcon.vue'
import FingerprintLoader from './components/common/FingerprintLoader.vue'
import MessageBox from '@/components/chat/messageBox/index.vue'
import NewChatBox from '@/components/chat/NewChatBox.vue'

import 'floating-vue/dist/style.css'
import { useTheme } from '@/composables/shared/useTheme'
import { useNotify } from '@/composables/shared/useNotify'
import { useChatStore } from '@/stores/chat'
import { useAuthStore } from '@/stores/auth'
import { useConversationsStore } from '@/stores/conversations'
import IncomingCallModal from '@/components/chat/IncomingCallModal.vue'

const chatStore = useChatStore()
const notify = useNotify()
const isOnline = useOnline()
const route = useRoute()
const authStore = useAuthStore()
const conversationsStore = useConversationsStore()
const router = useRouter()

// Endpoint dla tradycyjnych endpointów REST (np. bilety, logi rozmów)
const apiUrl = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'

// ==========================================
// APOLLO MUTATIONS (Zamiast surowego fetch)
// ==========================================
const SET_USER_ACTIVE_MUTATION = gql`
  mutation SetUserActive($userId: ID!) {
    setUserActive(userId: $userId)
  }
`
const { mutate: setUserActive } = useMutation(SET_USER_ACTIVE_MUTATION)

const RECORD_SEARCH_MUTATION = gql`
  mutation RecordSearch($searchedUserId: ID!) {
    recordSearch(searchedUserId: $searchedUserId)
  }
`
const { mutate: recordSearch } = useMutation(RECORD_SEARCH_MUTATION)

// ==========================================
// LOGIKA POŁĄCZEŃ I CZATU
// ==========================================
const handleIncomingCallAccept = () => {
  if (!conversationsStore.incomingCall) return
  const call = conversationsStore.incomingCall
  conversationsStore.incomingCall = null

  const routeData = router.resolve({
    name: 'video-call',
    query: {
      type: call.callType,
      boxId: call.callerId,
      callerId: call.callerId,
      conversationId: call.conversationId,
    },
  })
  window.open(
    routeData.href,
    `Rozmowa_${call.conversationId}`,
    'popup=yes,width=900,height=650,left=300,top=150,resizable=yes,location=no,toolbar=no,menubar=no',
  )
}

const handleIncomingCallReject = async () => {
  if (!conversationsStore.incomingCall) return
  const call = conversationsStore.incomingCall
  conversationsStore.incomingCall = null

  try {
    await $fetch(`${apiUrl}/api/chat/calls/log`, {
      method: 'POST',
      query: {
        conversationId: call.conversationId,
        senderId: authStore.currentUser?.id || authStore.currentUserId,
        callerId: call.callerId,
        duration: 0,
        status: 'rejected',
        participantIds: [authStore.currentUser?.id || authStore.currentUserId, call.callerId].join(','),
      }
    })
  } catch (err) {
    console.error('Failed to log call rejection:', err)
  }

  if (conversationsStore.isMqttConnected) {
    const payload = {
      type: 'call_rejected',
      conversationId: call.conversationId,
      senderId: authStore.currentUser?.id || authStore.currentUserId,
      callerId: call.callerId,
      participantIds: [authStore.currentUser?.id || authStore.currentUserId, call.callerId]
    }
    conversationsStore.publishMqtt('chat/messages/inbound', payload)
  }
}

watch(() => conversationsStore.incomingCall, (newVal) => {
  console.log('app.vue: Global incomingCall state changed:', newVal)
})

// --- STAN OKNA NOWEJ WIADOMOŚCI ---
const isNewChatBoxOpen = ref(false)

const handleStartNewChat = (user: any) => {
  isNewChatBoxOpen.value = false
  chatStore.addMessageBox(user.id)
}

const openNewChatListener = () => {
  isNewChatBoxOpen.value = true
}

onMounted(() => {
  if (typeof window !== 'undefined') {
    window.addEventListener('open-new-chat', openNewChatListener)

    // Set authenticated user from the jwt_token cookie set by BFF
    const tokenCookie = document.cookie.split('; ').find(row => row.startsWith('jwt_token='))
    if (tokenCookie) {
      const tokenVal = tokenCookie.split('=')[1]
      if (tokenVal && authStore.currentUserId !== tokenVal) {
        console.log('Setting authenticated user ID in store from cookie:', tokenVal)
        authStore.setCurrentUser(tokenVal)
      }
    }
  }
})

useTheme()

// ==========================================
// SSE NOTIFICATIONS LISTENER
// ==========================================
let eventSource: EventSource | null = null

const setupNotificationListener = async (userId: string | number) => {
  if (eventSource) {
    eventSource.close()
    eventSource = null
  }

  if (!userId) return

  if (typeof window !== 'undefined') {
    try {
      const headers: Record<string, string> = {}
      const token = localStorage.getItem('keycloak-token') || localStorage.getItem('auth-token')
      if (token) {
        headers['Authorization'] = `Bearer ${token}`
      }

      const res = await fetch(`${apiUrl}/api/tickets/generate?userId=${userId}`, {
        method: 'POST',
        headers
      })
      if (!res.ok) {
        throw new Error('Failed to generate ticket')
      }
      const data = await res.json()
      const ticket = data.ticket

      console.log(`Connecting to SSE notifications stream for user ID: ${userId}`)
      eventSource = new EventSource(`${apiUrl}/api/notifications/subscribe/${userId}?ticket=${ticket}`)

      eventSource.addEventListener('notification', (event) => {
        try {
          const data = JSON.parse(event.data)
          notify.notification({
            title: data.title || 'Powiadomienie',
            header: data.message || '',
            avatar: `https://ui-avatars.com/api/?name=${encodeURIComponent(data.title)}&background=random&color=fff`,
          })

          if (typeof window !== 'undefined') {
            window.dispatchEvent(new CustomEvent('new-notification', { detail: data }))
          }
        } catch (err) {
          console.error('Failed to parse SSE notification data:', err)
        }
      })

      eventSource.onerror = (err) => {
        console.warn('SSE EventSource error, will retry...', err)
      }
    } catch (err) {
      console.error('Failed to initialize SSE connection:', err)
    }
  }
}

// ==========================================
// HEARTBEAT & PROFILE TRACKING (Przez Apollo)
// ==========================================
let activeInterval: any = null

const sendActiveStatus = async () => {
  const userId = authStore.currentUserId
  if (!userId || userId === 0) return
  try {
    await setUserActive({ userId: String(userId) })
  } catch (err) {
    console.error('Failed to send active status signal via Apollo:', err)
  }
}



watch(
  () => authStore.currentUserId,
  (newId) => {
    setupNotificationListener(newId)
    if (typeof window !== 'undefined') {
      if (activeInterval) clearInterval(activeInterval)
      sendActiveStatus()
      activeInterval = setInterval(sendActiveStatus, 30000)
    }
  },
  { immediate: true },
)

onUnmounted(() => {
  if (typeof window !== 'undefined') {
    window.removeEventListener('open-new-chat', openNewChatListener)
  }
  if (eventSource) {
    eventSource.close()
  }
  if (activeInterval) {
    clearInterval(activeInterval)
  }
})

// Obsługa stanu sieci
watch(
  isOnline,
  (online, oldOnline) => {
    if (oldOnline === undefined) {
      if (!online) notify.offline()
      return
    }

    if (online) {
      notify.online()
    } else {
      notify.offline()
    }
  },
  { immediate: true },
)

// Logika widoków
const isPopupRoute = computed(() => {
  const metaVal = (route?.meta as Record<string, unknown>)?.isPopup
  return metaVal === true
})



const showMainLayout = computed(() => {
  const metaVal = (route?.meta as Record<string, unknown>)?.showMainLayout
  return metaVal !== false
})

const isInChatView = computed(() => {
  return (route?.path || '').startsWith('/chat')
})
</script>
