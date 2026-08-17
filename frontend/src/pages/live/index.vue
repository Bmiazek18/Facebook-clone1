<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import { useAuthStore } from '@/stores/auth'
import HeartIcon from 'vue-material-design-icons/Heart.vue'
import ThumbUpIcon from 'vue-material-design-icons/ThumbUp.vue'
import EmoticonLaughIcon from 'vue-material-design-icons/EmoticonLaugh.vue'
import EmoticonWowIcon from 'vue-material-design-icons/EmoticonWow.vue'
import SendIcon from 'vue-material-design-icons/Send.vue'
import EyeIcon from 'vue-material-design-icons/Eye.vue'
import VideoOffIcon from 'vue-material-design-icons/VideoOff.vue'

const authStore = useAuthStore()
const config = useRuntimeConfig()
const agoraClient = ref<any | null>(null)
const remoteUser = ref<any | null>(null)
const channelName = 'global-live-stream'
const isStreamOnline = ref(false)
const remoteVideoRef = ref<HTMLDivElement | null>(null)

const comments = ref([
  { id: 1, user: 'Anna Nowak', avatar: 'https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=100', text: 'Super transmisja! Pozdrowienia!' },
  { id: 2, user: 'Jan Kowalski', avatar: 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=100', text: 'Jakiej kamery używasz?' },
  { id: 3, user: 'Kasia Zielińska', avatar: 'https://images.unsplash.com/photo-1438761681033-6461ffad8d80?w=100', text: 'Ooo, ekstra! 😮' }
])
const newComment = ref('')
const chatContainer = ref<HTMLDivElement | null>(null)

// Reactions logic
interface FloatingReaction {
  id: number
  type: string
  left: number
  sway: number
  scale: number
}
const reactions = ref<FloatingReaction[]>([])
let reactionId = 0

const emitReaction = (type: string) => {
  const left = Math.random() * 60 + 20 // 20% to 80%
  const sway = Math.random() * 60 - 30 // -30px to 30px
  const scale = Math.random() * 0.4 + 0.8 // 0.8 to 1.2
  const id = reactionId++
  
  reactions.value.push({ id, type, left, sway, scale })
  setTimeout(() => {
    reactions.value = reactions.value.filter(r => r.id !== id)
  }, 2500)
}

const sendUserComment = () => {
  if (!newComment.value.trim()) return
  const currentUserName = authStore.currentUser?.name || 'Użytkownik'
  const currentUserAvatar = authStore.currentUser?.avatar || `https://ui-avatars.com/api/?name=${encodeURIComponent(currentUserName)}`
  
  comments.value.push({
    id: Date.now(),
    user: currentUserName,
    avatar: currentUserAvatar,
    text: newComment.value.trim()
  })
  
  newComment.value = ''
  scrollToBottom()
}

const scrollToBottom = () => {
  nextTick(() => {
    if (chatContainer.value) {
      chatContainer.value.scrollTop = chatContainer.value.scrollHeight
    }
  })
}

// Helper to generate numeric UID from user UUID
const getNumericUid = (uuidStr: string): number => {
  let hash = 0
  for (let i = 0; i < uuidStr.length; i++) {
    const character = uuidStr.charCodeAt(i)
    hash = (hash << 5) - hash + character
    hash = hash & hash
  }
  return Math.abs(hash)
}

// Simulated active viewers
const viewersCount = ref(12)

const initializeViewer = async () => {
  try {
    const AgoraRTC = (await import('agora-rtc-sdk-ng')).default
    agoraClient.value = AgoraRTC.createClient({ mode: 'live', codec: 'vp8' })
    await agoraClient.value.setClientRole('audience')

    const currentUserId = String(authStore.currentUser?.id || authStore.currentUserId || 'viewer-' + Math.random().toString(36).substr(2, 9))
    const numericUid = getNumericUid(currentUserId)

    // Fetch token
    const tokenResponse = await fetch(`${config.public.apiUrl}/api/chat/calls/token?channelName=${channelName}&uid=${numericUid}`)
    const tokenData = await tokenResponse.json()

    if (!tokenData.token) {
      throw new Error('Agora token acquisition failed')
    }

    // Join
    await agoraClient.value.join(tokenData.appId, channelName, tokenData.token, numericUid)

    // Listen to user published
    agoraClient.value.on('user-published', async (user: any, mediaType: 'video' | 'audio') => {
      await agoraClient.value.subscribe(user, mediaType)
      if (mediaType === 'video') {
        remoteUser.value = user
        isStreamOnline.value = true
        viewersCount.value = Math.floor(Math.random() * 15) + 8
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

    agoraClient.value.on('user-unpublished', (user: any, mediaType: string) => {
      if (mediaType === 'video') {
        isStreamOnline.value = false
        remoteUser.value = null
        viewersCount.value = 0
      }
    })

    agoraClient.value.on('user-left', () => {
      isStreamOnline.value = false
      remoteUser.value = null
      viewersCount.value = 0
    })

    // Check if there is already a streaming host in the channel
    if (agoraClient.value.remoteUsers.length > 0) {
      const activeHost = agoraClient.value.remoteUsers.find((u: any) => u.hasVideo || u.videoTrack)
      if (activeHost) {
        await agoraClient.value.subscribe(activeHost, 'video')
        remoteUser.value = activeHost
        isStreamOnline.value = true
        viewersCount.value = Math.floor(Math.random() * 25) + 15
        setTimeout(() => {
          if (remoteVideoRef.value) {
            activeHost.videoTrack?.play(remoteVideoRef.value)
          }
        }, 100)
        
        if (activeHost.hasAudio) {
          await agoraClient.value.subscribe(activeHost, 'audio')
          activeHost.audioTrack?.play()
        }
      }
    }
  } catch (err) {
    console.error('Failed to initialize Agora viewer:', err)
  }
}

// Simulation of random chat comments and viewer numbers
const simulatedUsernames = ['Michał Kot', 'Zofia Bąk', 'Piotr Wilk', 'Karolina Lew', 'Kamil Ryba', 'Ola Ptak', 'Kasia Wilk', 'Tomasz Lis']
const simulatedComments = [
  'Świetna jakość! 🤩',
  'Pozdrowienia z Krakowa!',
  'Cześć wszystkim! 👋',
  'Super projekt!',
  'Wszystko widać i słychać bez problemu.',
  'Hahaha 😂',
  'Wow! 😮',
  'Super sprawa z tym streamem przez Agorę!'
]

let simulationInterval: any = null
let viewersInterval: any = null

onMounted(() => {
  initializeViewer()
  scrollToBottom()

  simulationInterval = setInterval(() => {
    if (!isStreamOnline.value) return
    const user = simulatedUsernames[Math.floor(Math.random() * simulatedUsernames.length)]
    const text = simulatedComments[Math.floor(Math.random() * simulatedComments.length)]
    comments.value.push({
      id: Date.now(),
      user,
      avatar: `https://ui-avatars.com/api/?name=${encodeURIComponent(user)}&background=random&color=fff`,
      text
    })
    
    // Auto-reactions emulation
    if (Math.random() > 0.4) {
      const types = ['like', 'love', 'laugh', 'wow']
      emitReaction(types[Math.floor(Math.random() * types.length)])
    }

    if (comments.value.length > 30) {
      comments.value.shift()
    }
    scrollToBottom()
  }, 4500)

  viewersInterval = setInterval(() => {
    if (isStreamOnline.value) {
      viewersCount.value += Math.random() > 0.5 ? 1 : -1
      if (viewersCount.value < 1) viewersCount.value = 1
    }
  }, 8000)
})

onUnmounted(async () => {
  if (agoraClient.value) {
    await agoraClient.value.leave()
  }
  if (simulationInterval) clearInterval(simulationInterval)
  if (viewersInterval) clearInterval(viewersInterval)
})
</script>

<template>
  <div class="h-screen w-full flex bg-[#18191a] text-white overflow-hidden mt-14">
    <!-- LEFT PANEL: Video Player -->
    <div class="flex-1 flex flex-col justify-between p-6 relative overflow-hidden h-[calc(100vh-56px)]">
      <!-- Top info bar -->
      <div class="flex justify-between items-center z-20 w-full">
        <div class="flex items-center gap-3">
          <div class="w-10 h-10 rounded-full overflow-hidden bg-zinc-800 border border-zinc-700">
            <img src="https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=100" class="w-full h-full object-cover" />
          </div>
          <div>
            <h2 class="font-bold text-[15px] leading-tight text-white shadow-sm">Bartosz (Live Stream)</h2>
            <div class="flex items-center gap-2 mt-0.5 text-xs text-zinc-300">
              <span class="px-1.5 py-0.5 bg-[#E41E3F] rounded text-[10px] font-bold uppercase tracking-wider text-white">NA ŻYWO</span>
              <span class="flex items-center gap-1 bg-black/40 px-2 py-0.5 rounded-full" v-if="isStreamOnline">
                <EyeIcon :size="12" class="text-zinc-200" />
                {{ viewersCount }} widzów
              </span>
            </div>
          </div>
        </div>
      </div>

      <!-- Video Canvas Container -->
      <div class="absolute inset-0 bg-[#0c0d0e] flex items-center justify-center z-10">
        <!-- Remote Video Render element -->
        <div 
          v-show="isStreamOnline"
          ref="remoteVideoRef" 
          class="w-full h-full object-cover"
        ></div>

        <!-- Offline state placeholder -->
        <div v-if="!isStreamOnline" class="flex flex-col items-center justify-center p-6 text-center select-none max-w-sm">
          <div class="w-20 h-20 rounded-full bg-zinc-800/80 flex items-center justify-center text-zinc-500 mb-5 animate-pulse border border-zinc-700">
            <VideoOffIcon :size="40" />
          </div>
          <h3 class="text-lg font-bold text-zinc-200">Transmisja na żywo jest offline</h3>
          <p class="text-xs text-zinc-400 mt-2 leading-relaxed">
            Obecnie nikt nie nadaje na tym kanale. Gdy nadawca ("glowny") rozpocznie transmisję, wideo pojawi się tutaj automatycznie.
          </p>
        </div>

        <!-- Floating Reactions overlay -->
        <div class="absolute inset-0 pointer-events-none z-30 overflow-hidden">
          <div 
            v-for="r in reactions" 
            :key="r.id"
            class="floating-reaction"
            :style="{ 
              left: `${r.left}%`, 
              transform: `scale(${r.scale})`,
              '--sway-x': `${r.sway}px`
            }"
          >
            <span v-if="r.type === 'like'" class="emoji-bubble bg-blue-500">👍</span>
            <span v-else-if="r.type === 'love'" class="emoji-bubble bg-red-500">❤️</span>
            <span v-else-if="r.type === 'laugh'" class="emoji-bubble bg-yellow-500">😂</span>
            <span v-else-if="r.type === 'wow'" class="emoji-bubble bg-yellow-600">😮</span>
          </div>
        </div>
      </div>

      <!-- Video Player Control overlays / reactions bar -->
      <div class="z-20 w-full flex justify-between items-center mt-auto" v-if="isStreamOnline">
        <!-- Empty space -->
        <div></div>

        <!-- Reactions Selector Bar -->
        <div class="bg-black/60 backdrop-blur-md px-4 py-2.5 rounded-full flex gap-3 shadow-lg border border-white/10">
          <button @click="emitReaction('like')" class="reaction-btn hover:scale-125 transition-transform" title="Lubię to">
            <span class="text-xl">👍</span>
          </button>
          <button @click="emitReaction('love')" class="reaction-btn hover:scale-125 transition-transform" title="Super">
            <span class="text-xl">❤️</span>
          </button>
          <button @click="emitReaction('laugh')" class="reaction-btn hover:scale-125 transition-transform" title="Haha">
            <span class="text-xl">😂</span>
          </button>
          <button @click="emitReaction('wow')" class="reaction-btn hover:scale-125 transition-transform" title="Wow">
            <span class="text-xl">😮</span>
          </button>
        </div>
      </div>
    </div>

    <!-- RIGHT PANEL: Live Chat and Social panel -->
    <div class="w-[360px] bg-[#242526] border-l border-zinc-800 flex flex-col justify-between shrink-0 h-[calc(100vh-56px)] z-25">
      <!-- Header -->
      <div class="p-4 border-b border-zinc-800 flex items-center justify-between">
        <h3 class="font-bold text-[16px] text-zinc-100">Czat na żywo</h3>
        <span class="px-2 py-0.5 bg-zinc-800 text-zinc-400 text-xs rounded font-medium">Agora Audio/Video</span>
      </div>

      <!-- Comments Stream list -->
      <div 
        ref="chatContainer"
        class="flex-1 overflow-y-auto p-4 space-y-4 custom-scrollbar"
      >
        <div 
          v-for="comment in comments" 
          :key="comment.id"
          class="flex items-start gap-2.5"
        >
          <div class="w-8 h-8 rounded-full overflow-hidden bg-zinc-800 shrink-0 border border-zinc-700">
            <img :src="comment.avatar" class="w-full h-full object-cover" />
          </div>
          <div class="bg-zinc-800/60 rounded-xl px-3 py-2 max-w-[80%]">
            <div class="text-[12px] font-bold text-zinc-300">{{ comment.user }}</div>
            <div class="text-[13px] text-zinc-200 mt-0.5 break-words leading-relaxed">{{ comment.text }}</div>
          </div>
        </div>
      </div>

      <!-- Send comment input form -->
      <div class="p-4 border-t border-zinc-800 bg-[#242526]">
        <form @submit.prevent="sendUserComment" class="flex gap-2">
          <input
            v-model="newComment"
            type="text"
            placeholder="Napisz komentarz..."
            class="flex-1 bg-zinc-800 border border-zinc-700 rounded-full px-4 py-2 text-[14px] text-white focus:outline-none focus:border-[#1877f2] placeholder-zinc-500"
          />
          <button 
            type="submit"
            class="w-9 h-9 rounded-full bg-[#1877f2] hover:bg-[#166fe5] active:scale-95 flex items-center justify-center shrink-0 text-white transition-all shadow"
          >
            <SendIcon :size="16" />
          </button>
        </form>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* Scrollbar styling */
.custom-scrollbar::-webkit-scrollbar {
  width: 6px;
}
.custom-scrollbar::-webkit-scrollbar-thumb {
  background: #3e4042;
  border-radius: 3px;
}
.custom-scrollbar::-webkit-scrollbar-track {
  background: transparent;
}

/* Floating animation styles */
@keyframes float-up {
  0% {
    transform: translateY(100%) scale(0.6);
    opacity: 0;
  }
  10% {
    opacity: 1;
  }
  90% {
    opacity: 1;
  }
  100% {
    transform: translateY(-450px) scale(1.3) translateX(var(--sway-x));
    opacity: 0;
  }
}

.floating-reaction {
  position: absolute;
  bottom: 60px;
  animation: float-up 2.5s cubic-bezier(0.08, 0.82, 0.17, 1) forwards;
  pointer-events: none;
}

.emoji-bubble {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.3);
  font-size: 16px;
  color: white;
}

.reaction-btn {
  background: transparent;
  border: none;
  cursor: pointer;
  outline: none;
}
</style>
