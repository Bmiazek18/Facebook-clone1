<template>
  <div>

    <Suspense>

      <template #default>
        <div>

          <MainNavHeader v-if="showMainLayout && !isPopupRoute" />


          <NuxtLayout>
            <NuxtPage />
          </NuxtLayout>

          <IncomingCallModal
            v-if="conversationsStore.incomingCall"
            :isOpen="true"
            :callerName="conversationsStore.incomingCall.callerName"
            :callerAvatar="conversationsStore.incomingCall.callerAvatar"
            @close="conversationsStore.incomingCall = null"
            @reject="conversationsStore.rejectIncomingCall"
            @accept="conversationsStore.acceptIncomingCall"
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

          <!-- <ClientOnly>
            <FingerprintLoader />
          </ClientOnly> -->
        </div>
      </template>


      <template #fallback>
        <div v-if="isCallRoute" class="fixed inset-0 bg-black z-[999999]"></div>
        <FacebookSplash v-else />
      </template>
    </Suspense>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted, onUnmounted } from 'vue'
import { useOnline, useEventListener } from '@vueuse/core'

import MainNavHeader from '@/components/navbar/MainNavHeader.vue'
import ProfileIcon from '@/components/profile/ProfileIcon.vue'
import FingerprintLoader from '@/components/common/FingerprintLoader.vue'
import MessageBox from '@/components/chat/messageBox/index.vue'
import NewChatBox from '@/components/chat/NewChatBox.vue'
import IncomingCallModal from '@/components/chat/modals/IncomingCallModal.vue'
import FacebookSplash from '@/components/FacebookSplash.vue'

import 'floating-vue/dist/style.css'
import { useTheme } from '@/composables/shared/useTheme'
import { useNotify } from '@/composables/shared/useNotify'
import { useChatStore } from '@/stores/chat'
import { useAuthStore } from '@/stores/auth'
import { useConversationsStore } from '@/stores/conversations'
import { useGenerateTicket } from '@/composables/shared/useGenerateTicket'
import { useLinkGuard } from '@/composables/shared/useLinkGuard'
import { useTabFlasher } from '@/composables/shared/useTabFlasher'
import { useWebPush } from '@/composables/shared/useWebPush'

const { initTabFlasher, destroyTabFlasher } = useTabFlasher()
const { registerWebPush } = useWebPush()

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

useTheme()

// --- STAN OKNA NOWEJ WIADOMOŚCI ---
const isNewChatBoxOpen = ref(false)

const handleStartNewChat = (user: any) => {
  isNewChatBoxOpen.value = false
  chatStore.addMessageBox(user.id)
}

const openNewChatListener = () => {
  isNewChatBoxOpen.value = true
}

watch(
  () => authStore.currentUserId,
  (newId) => {
    if (import.meta.client && newId) {
      registerWebPush(newId)
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
const { initLinkGuard } = useLinkGuard()

// ==========================================
// LIFECYCLE HOOKS
// ==========================================
onMounted(() => {
  if (import.meta.client) {
    // Register Web Push Service Worker
    if ('serviceWorker' in navigator) {
      navigator.serviceWorker.register('/sw.js')
        .then(reg => console.log('[ServiceWorker] Registered with scope:', reg.scope))
        .catch(err => console.error('[ServiceWorker] Registration failed:', err))
    }

    useEventListener(window, 'open-new-chat', openNewChatListener)

    // Inicjalizacja sprawdzania linków zewnętrznych (LinkGuard)
    initLinkGuard()

    // Inicjalizacja migania karty (Facebook style)
    initTabFlasher()

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
    // Sprzątanie migania karty
    destroyTabFlasher()
  }
})

// ==========================================
// COMPUTED ROUTE VALUES
// ==========================================
const isPopupRoute = computed(() => route?.meta?.isPopup === true)
const showMainLayout = computed(() => route?.meta?.showMainLayout !== false)
const isInChatView = computed(() => (route?.path || '').startsWith('/chat'))
</script>
