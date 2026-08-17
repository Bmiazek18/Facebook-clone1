<template>
  <div>
    <!-- Używamy Suspense do automatycznego zarządzania stanem asynchronicznym -->
    <Suspense>
      <!-- SLOT DOMYŚLNY: Renderuje się gdy wszystkie zapytania `await` na stronie spłyną -->
      <template #default>
        <div>
          <!-- Główny szablon nawigacji -->
          <MainNavLayout v-if="showMainLayout && !isPopupRoute" />

          <!-- Główny widok podstron Nuxta -->
          <NuxtLayout>
            <NuxtPage />
          </NuxtLayout>

          <!-- Globalny Modal Połączeń Przychodzących -->
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
            <MessageBox
              v-for="boxId in chatStore.getBoxIds"
              :key="boxId"
              :boxId="boxId"
              class="pointer-events-auto"
            />

            <NewChatBox
              v-if="isNewChatBoxOpen"
              @close="isNewChatBoxOpen = false"
              @select-user="handleStartNewChat"
              class="pointer-events-auto"
            />
          </div>

          <!-- Przycisk profilu / nowego czatu -->
          <ProfileIcon
            v-if="showMainLayout && !isInChatView && !isPopupRoute"
            @click="isNewChatBoxOpen = true"
          />

          <ClientOnly>
            <FingerprintLoader />
          </ClientOnly>
        </div>
      </template>

      <!-- SLOT FALLBACK: Wyświetla się AUTOMATYCZNIE, dopóki zapytania SSR/API trwają -->
      <template #fallback>
        <div v-if="isCallRoute" class="fixed inset-0 bg-black z-[999999]"></div>
        <FacebookSplash v-else />
      </template>
    </Suspense>
  </div>
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
import IncomingCallModal from '@/components/chat/IncomingCallModal.vue'
import FacebookSplash from '@/components/FacebookSplash.vue'

import 'floating-vue/dist/style.css'
import { useTheme } from '@/composables/shared/useTheme'
import { useNotify } from '@/composables/shared/useNotify'
import { useChatStore } from '@/stores/chat'
import { useAuthStore } from '@/stores/auth'
import { useConversationsStore } from '@/stores/conversations'
import { useGenerateTicket } from '@/composables/shared/useGenerateTicket'
import { useLinkGuard } from '@/composables/shared/useLinkGuard'

// ==========================================
// USUWANIE STATYCZNEGO HTML DLA ZEROWEGO MIGNIĘCIA
// ==========================================
const nuxtApp = useNuxtApp()

nuxtApp.hook('app:suspense:resolve', () => {
  if (import.meta.client) {
    const staticSplash = document.getElementById('static-facebook-splash')
    if (staticSplash) {
      staticSplash.remove()
    }
  }
})

// Stores i composables
const chatStore = useChatStore()
const notify = useNotify()
const isOnline = useOnline()
const route = useRoute()
const router = useRouter()

const isCallRoute = computed(() => {
  return (
    route?.path?.includes('video-call') ||
    route?.name === 'video-call' ||
    (typeof window !== 'undefined' && window.location.pathname.includes('video-call'))
  )
})
const authStore = useAuthStore()
const conversationsStore = useConversationsStore()
const { generateTicket } = useGenerateTicket()

// Endpoint REST
const config = useRuntimeConfig()
const apiUrl = config.public.apiUrl

// ==========================================
// APOLLO MUTATIONS
// ==========================================
const SET_USER_ACTIVE_MUTATION = gql`
  mutation SetUserActive($userId: ID!) {
    setUserActive(userId: $userId)
  }
`
const { mutate: setUserActive } = useMutation(SET_USER_ACTIVE_MUTATION)

useTheme()

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

  if (import.meta.client) {
    window.open(
      routeData.href,
      `Rozmowa_${call.conversationId}`,
      'popup=yes,width=900,height=650,left=300,top=150,resizable=yes,location=no,toolbar=no,menubar=no',
    )
  }
}

const handleIncomingCallReject = async () => {
  if (!conversationsStore.incomingCall) return
  const call = conversationsStore.incomingCall
  conversationsStore.incomingCall = null

  const currentUserId = authStore.currentUser?.id || authStore.currentUserId

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

  if (conversationsStore.isMqttConnected) {
    const payload = {
      type: 'call_rejected',
      conversationId: call.conversationId,
      senderId: currentUserId,
      callerId: call.callerId,
      participantIds: [currentUserId, call.callerId],
    }
    conversationsStore.publishMqtt('chat/messages/user/' + call.callerId, payload)
  }
}

// --- STAN OKNA NOWEJ WIADOMOŚCI ---
const isNewChatBoxOpen = ref(false)

const handleStartNewChat = (user: any) => {
  isNewChatBoxOpen.value = false
  chatStore.addMessageBox(user.id)
}

const openNewChatListener = () => {
  isNewChatBoxOpen.value = true
}

// ==========================================
// SSE NOTIFICATIONS LISTENER
// ==========================================
let eventSource: EventSource | null = null

const setupNotificationListener = async (userId: string | number) => {
  if (eventSource) {
    eventSource.close()
    eventSource = null
  }

  if (!userId || !import.meta.client) return

  try {
    const ticket = await generateTicket(String(userId))

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

        window.dispatchEvent(new CustomEvent('new-notification', { detail: data }))
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

// ==========================================
// HEARTBEAT & PROFILE TRACKING
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
    if (import.meta.client && newId) {
      setupNotificationListener(newId)
      if (activeInterval) clearInterval(activeInterval)
      sendActiveStatus()
      activeInterval = setInterval(sendActiveStatus, 30000)
    }
  },
  { immediate: true },
)

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

// Globalny interceptor kliknięć w linki zewnętrzne
const { verifyAndNavigate } = useLinkGuard()

const handleGlobalLinkClick = async (event: MouseEvent) => {
  const target = event.target as HTMLElement
  const anchor = target.closest('a')
  if (!anchor) return

  const href = anchor.getAttribute('href')
  if (!href) return

  // Interceptujemy tylko zewnętrzne linki bezwzględne
  if (href.startsWith('http://') || href.startsWith('https://')) {
    // Pomijamy jeśli to już jest gotowy link przekierowujący (l.php)
    if (href.includes('/l.php?')) return

    event.preventDefault()
    event.stopPropagation()
    await verifyAndNavigate(href)
  }
}

// ==========================================
// LIFECYCLE HOOKS
// ==========================================
onMounted(() => {
  if (import.meta.client) {
    window.addEventListener('open-new-chat', openNewChatListener)
    document.addEventListener('click', handleGlobalLinkClick, true)

    const tokenCookie = document.cookie.split('; ').find((row) => row.startsWith('jwt_token='))
    if (tokenCookie) {
      const tokenVal = tokenCookie.split('=')[1]
      if (tokenVal && authStore.originalUserId !== tokenVal) {
        authStore.setCurrentUser(tokenVal, true)
      }
    }
  }
})

onUnmounted(() => {
  if (import.meta.client) {
    window.removeEventListener('open-new-chat', openNewChatListener)
    document.removeEventListener('click', handleGlobalLinkClick, true)
  }
  if (eventSource) {
    eventSource.close()
  }
  if (activeInterval) {
    clearInterval(activeInterval)
  }
})

// ==========================================
// COMPUTED ROUTE VALUES
// ==========================================
const isPopupRoute = computed(() => route?.meta?.isPopup === true)
const showMainLayout = computed(() => route?.meta?.showMainLayout !== false)
const isInChatView = computed(() => (route?.path || '').startsWith('/chat'))
</script>
