<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useSidebar } from '@/composables/useSidebar'
import { useAuthStore } from '@/stores/auth'
import { usersApi } from '@/api/users'

const router = useRouter()
const route = useRoute()
const { isExpanded, toggleSidebar } = useSidebar()
const authStore = useAuthStore()

interface ChatThread {
  thread_id: string
  title: string
}
const recentChats = ref<ChatThread[]>([])
const activeThreadId = ref<string | null>(null)
const userProfile = ref<any>(null)

const currentUserId = computed(() => String(authStore.currentUserId || ''))

const userDisplayName = computed(() => {
  if (userProfile.value?.firstName || userProfile.value?.lastName) {
    return [userProfile.value.firstName, userProfile.value.lastName].filter(Boolean).join(' ')
  }
  if (authStore.currentUser?.name) {
    return authStore.currentUser.name
  }
  return 'Użytkownik'
})

const userUsername = computed(() => {
  if (userProfile.value?.username) return userProfile.value.username
  return userDisplayName.value.toLowerCase().replace(/\s+/g, '.')
})

const userAvatar = computed(() => {
  return (
    userProfile.value?.avatar ||
    authStore.currentUser?.avatar ||
    '/default-avatar.png'
  )
})

const userLocation = computed(() => {
  return (
    userProfile.value?.city ||
    userProfile.value?.location ||
    userProfile.value?.hometown ||
    'Polska'
  )
})

const loadUserProfile = async () => {
  if (!currentUserId.value) return
  try {
    const u = await usersApi.getUserById(currentUserId.value)
    if (u) {
      userProfile.value = u
    }
  } catch (err) {
    console.warn('Failed to load user profile in meta-ai sidebar:', err)
  }
}

const fetchChats = async () => {
  try {
    const uid = currentUserId.value
    const url = uid ? `/api/chat-threads?user_id=${encodeURIComponent(uid)}` : '/api/chat-threads'
    const response = await fetch(url)
    if (response.ok) {
      const data = await response.json()
      recentChats.value = data.threads || []
    }
  } catch (error) {
    console.error('Błąd pobierania listy czatów:', error)
  }
}

const selectChat = async (threadId: string) => {
  activeThreadId.value = threadId
  await router.push(`/meta-ai/${threadId}`)
}

const createNewChat = async () => {
  activeThreadId.value = null
  await router.push('/meta-ai')
}

onMounted(() => {
  loadUserProfile()
  fetchChats()
  if (route.params.id) {
    activeThreadId.value = route.params.id as string
  }
})

watch(currentUserId, () => {
  loadUserProfile()
  fetchChats()
})

watch(() => route.params.id, (newId) => {
  if (newId) {
    activeThreadId.value = newId as string
  } else {
    activeThreadId.value = null
  }
})

defineExpose({ fetchChats })
</script>

<template>
  <aside
    :class="[
      'fixed left-0 top-0 z-50 flex h-screen flex-col text-[#ececec] transition-all duration-200 ease-in-out select-none font-sans border-r border-white/[0.04]',
      isExpanded ? 'w-[260px] bg-[#171717]' : 'w-[68px] bg-[#121212]'
    ]"
  >
    <!-- GÓRNA SEKCJA -->
    <div class="flex flex-col pt-3 shrink-0" :class="isExpanded ? 'px-3 gap-2' : 'px-0 items-center gap-5'">

      <!-- LOGO + PRZEŁĄCZNIK SIDEBARA -->
      <div class="flex items-center w-full px-2 h-10" :class="isExpanded ? 'justify-between' : 'justify-center'">
        <div class="flex items-center cursor-pointer" @click="createNewChat">
          <!-- Kolorowy symbol kropkowy / spinner gradientowy jak na screenie -->
          <div class="relative w-6 h-6 flex items-center justify-center">
            <span class="absolute w-2 h-2 rounded-full bg-[#ec4899] -top-0.5 left-2 blur-[0.5px]"></span>
            <span class="absolute w-2 h-2 rounded-full bg-[#a855f7] top-1 -right-0.5 blur-[0.5px]"></span>
            <span class="absolute w-2 h-2 rounded-full bg-[#8b5cf6] bottom-1 -right-0.5 blur-[0.5px]"></span>
            <span class="absolute w-2 h-2 rounded-full bg-[#3b82f6] -bottom-0.5 left-2 blur-[0.5px]"></span>
            <span class="absolute w-2 h-2 rounded-full bg-[#06b6d4] bottom-1 -left-0.5 blur-[0.5px]"></span>
            <span class="absolute w-2 h-2 rounded-full bg-[#d946ef] top-1 -left-0.5 blur-[0.5px]"></span>
          </div>
        </div>

        <button
          class="rounded-md hover:bg-white/[0.06] p-1.5 flex items-center justify-center transition-colors text-[#a3a3a3] hover:text-[#ececec]"
          @click="toggleSidebar"
          title="Przełącz pasek boczny"
        >
          <Icon name="lucide:panel-left" size="18" />
        </button>
      </div>

      <!-- GŁÓWNE PRZYCISKI NAWIGACJI -->
      <div class="flex flex-col w-full mt-1" :class="isExpanded ? 'gap-0.5' : 'gap-4 items-center'">

        <!-- NOWY CZAT -->
        <button
          @click="createNewChat"
          :class="[
            'flex items-center justify-between text-[#ececec] hover:bg-white/[0.06] transition-colors rounded-lg w-full px-2.5 h-[38px]',
            !isExpanded && 'w-9 h-9 justify-center px-0'
          ]"
        >
          <div class="flex items-center gap-3 min-w-0">
            <Icon name="lucide:square-pen" :size="isExpanded ? '18' : '18'" class="shrink-0 text-[#e5e5e5]" />
            <span v-if="isExpanded" class="text-[14px] font-normal truncate">Nowy czat</span>
          </div>
          <span v-if="isExpanded" class="text-[12px] text-[#737373] tracking-wider font-light">⇧⌘O</span>
        </button>

        <!-- SZUKAJ -->
        <button
          :class="[
            'flex items-center justify-between text-[#ececec] hover:bg-white/[0.06] transition-colors rounded-lg w-full px-2.5 h-[38px]',
            !isExpanded && 'w-9 h-9 justify-center px-0'
          ]"
        >
          <div class="flex items-center gap-3 min-w-0">
            <Icon name="lucide:search" :size="isExpanded ? '18' : '18'" class="shrink-0 text-[#e5e5e5]" />
            <span v-if="isExpanded" class="text-[14px] font-normal truncate">Szukaj</span>
          </div>
          <span v-if="isExpanded" class="text-[12px] text-[#737373] tracking-wider font-light">⌘K</span>
        </button>

        <!-- MULTIMEDIA -->
        <button
          :class="[
            'flex items-center text-[#ececec] hover:bg-white/[0.06] transition-colors rounded-lg w-full px-2.5 h-[38px]',
            isExpanded ? 'gap-3' : 'w-9 h-9 justify-center px-0'
          ]"
        >
          <Icon name="lucide:image" :size="isExpanded ? '18' : '18'" class="shrink-0 text-[#e5e5e5]" />
          <span v-if="isExpanded" class="text-[14px] font-normal truncate">Multimedia</span>
        </button>

        <!-- ARTEFAKTY -->
        <button
          :class="[
            'flex items-center text-[#ececec] hover:bg-white/[0.06] transition-colors rounded-lg w-full px-2.5 h-[38px]',
            isExpanded ? 'gap-3' : 'w-9 h-9 justify-center px-0'
          ]"
        >
          <Icon name="lucide:shapes" :size="isExpanded ? '18' : '18'" class="shrink-0 text-[#e5e5e5]" />
          <span v-if="isExpanded" class="text-[14px] font-normal truncate">Artefakty</span>
        </button>

        <!-- ZAPLANOWANE -->
        <button
          :class="[
            'flex items-center text-[#ececec] hover:bg-white/[0.06] transition-colors rounded-lg w-full px-2.5 h-[38px]',
            isExpanded ? 'gap-3' : 'w-9 h-9 justify-center px-0'
          ]"
        >
          <Icon name="lucide:calendar-days" :size="isExpanded ? '18' : '18'" class="shrink-0 text-[#e5e5e5]" />
          <span v-if="isExpanded" class="text-[14px] font-normal truncate">Zaplanowane</span>
        </button>

        <!-- VIBES -->
        <button
          :class="[
            'flex items-center text-[#ececec] hover:bg-white/[0.06] transition-colors rounded-lg w-full px-2.5 h-[38px]',
            isExpanded ? 'gap-3' : 'w-9 h-9 justify-center px-0'
          ]"
        >
          <Icon name="lucide:play-square" :size="isExpanded ? '18' : '18'" class="shrink-0 text-[#e5e5e5]" />
          <span v-if="isExpanded" class="text-[14px] font-normal truncate">Vibes</span>
        </button>

      </div>
    </div>

    <!-- HISTORIA / LISTA CZATÓW -->
    <div v-if="isExpanded" class="custom-scroll mt-5 flex-1 overflow-y-auto px-2">
      <div class="mb-2 px-2.5 text-[12px] font-medium text-[#737373]">
        Historia
      </div>

      <div class="space-y-[1px]">
        <div
          v-for="chat in recentChats"
          :key="chat.thread_id"
          @click="selectChat(chat.thread_id)"
          :class="[
            'group flex cursor-pointer items-center transition-colors rounded-lg px-2.5 py-2 justify-between h-[36px]',
            activeThreadId === chat.thread_id ? 'bg-white/[0.08]' : 'hover:bg-white/[0.04]'
          ]"
        >
          <span class="truncate text-[13.5px] text-[#d4d4d4] font-normal">
            {{ chat.title }}
          </span>
        </div>
      </div>
    </div>

    <div v-else class="flex-1"></div>

    <!-- DOLNA SEKCJA: PROFIL + LOKALIZACJA -->
    <div class="mt-auto shrink-0 flex flex-col bg-transparent pb-3" :class="isExpanded ? 'px-3' : 'px-2 items-center'">

      <!-- LOKALIZACJA -->
      <div v-if="isExpanded" class="flex flex-col text-[11px] text-[#737373] px-2 py-2 border-b border-white/[0.05] mb-2 font-light">
        <div class="flex items-center gap-1.5">
          <span class="w-1.5 h-1.5 rounded-full bg-[#22c55e]"></span>
          <span class="text-[#a3a3a3] hover:text-white cursor-pointer underline">{{ userLocation }}</span>
        </div>
        <span class="text-[#525252] text-[10.5px] mt-0.5">Z Twojego profilu</span>
      </div>

      <!-- KONTO UŻYTKOWNIKA -->
      <div
        @click="router.push(currentUserId ? `/profile/${currentUserId}` : '/profile')"
        :class="[
          'flex items-center justify-between rounded-lg hover:bg-white/[0.05] p-2 transition-colors cursor-pointer w-full',
          !isExpanded && 'justify-center p-1'
        ]"
        :title="userDisplayName"
      >
        <div class="flex items-center gap-3 min-w-0">
          <div class="w-8 h-8 rounded-full overflow-hidden bg-[#262626] shrink-0 border border-white/10 flex items-center justify-center">
            <img
              :src="userAvatar"
              :alt="userDisplayName"
              class="w-full h-full object-cover"
            />
          </div>
          <span v-if="isExpanded" class="text-[13.5px] font-normal text-[#ececec] truncate">
            {{ userUsername }}
          </span>
        </div>

        <button
          v-if="isExpanded"
          class="text-[#a3a3a3] hover:text-[#ececec] transition-colors p-1"
          @click.stop="router.push(currentUserId ? `/profile/${currentUserId}` : '/profile')"
        >
          <Icon name="lucide:bell" size="18" />
        </button>
      </div>

    </div>
  </aside>
</template>

<style scoped>
.custom-scroll::-webkit-scrollbar {
  width: 4px;
}
.custom-scroll::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.1);
  border-radius: 9999px;
}
</style>
