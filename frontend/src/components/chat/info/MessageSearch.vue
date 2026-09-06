<template>
  <div class="w-full h-full max-w-md mx-auto overflow-hidden flex flex-col bg-white dark:bg-[#1c1d1e] text-theme-text">

    <!-- NAGŁÓWEK -->
    <div class="px-4 py-3 flex items-center gap-4">
      <button
        @click="emit('close')"
        class="p-1 -ml-1 rounded-full transition text-black dark:text-white"
      >
        <svg
          xmlns="http://www.w3.org/2000/svg"
          fill="none"
          viewBox="0 0 24 24"
          stroke-width="2.5"
          stroke="currentColor"
          class="w-6 h-6"
        >
          <path stroke-linecap="round" stroke-linejoin="round" d="M6 18L18 6M6 6l12 12" />
        </svg>
      </button>
      <h1 class="text-xl font-semibold text-black dark:text-white tracking-tight">Szukaj</h1>
    </div>

    <!-- POLE WYSZUKIWANIA -->
    <div class="px-4 pb-2">
      <div class="flex items-center bg-gray-100 dark:bg-gray-800 rounded-[20px] px-3 py-1.5">

        <!-- Ikona lupy -->
        <svg
          xmlns="http://www.w3.org/2000/svg"
          class="h-5 w-5 text-gray-500 shrink-0"
          fill="none"
          viewBox="0 0 24 24"
          stroke="currentColor"
        >
          <path
            stroke-linecap="round"
            stroke-linejoin="round"
            stroke-width="2"
            d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"
          />
        </svg>

        <!-- Pole input -->
        <input
          v-model="searchQuery"
          type="text"
          placeholder="Szukaj w konwersacji"
          class="bg-transparent border-none outline-none text-[15px] text-black dark:text-white w-full placeholder-gray-500 h-8 ml-2"
          @input="handleSearchInput"
          @keyup.enter="startSearch"
        />

        <!-- Informacja o wynikach (ukryta podczas wyszukiwania) -->
        <div
          v-if="searchResults.length > 0 && !isSearching"
          class="text-gray-500 text-[14px] whitespace-nowrap ml-2 mr-2"
        >
          Wyniki: {{ searchResults.length }}
        </div>

        <LoadingSpinner :color="'#64B5F6'" v-if="isSearching" class="mr-2 shrink-0" />

        <!-- Przycisk czyszczenia (X) -->
        <button
          v-if="searchQuery"
          @click="clearSearch"
          class="shrink-0 bg-gray-300 dark:bg-gray-600 rounded-full p-1 transition flex items-center justify-center text-gray-600 dark:text-gray-300"
        >
          <svg
            xmlns="http://www.w3.org/2000/svg"
            class="h-3 w-3"
            viewBox="0 0 20 20"
            fill="currentColor"
          >
            <path
              fill-rule="evenodd"
              d="M4.293 4.293a1 1 0 011.414 0L10 8.586l4.293-4.293a1 1 0 111.414 1.414L11.414 10l4.293 4.293a1 1 0 01-1.414 1.414L10 11.414l-4.293 4.293a1 1 0 01-1.414-1.414L8.586 10 4.293 5.707a1 1 0 010-1.414z"
              clip-rule="evenodd"
            />
          </svg>
        </button>
      </div>
    </div>

    <!-- LISTA WYNIKÓW I STANY PUSTE -->
    <div class="flex-1 overflow-y-auto pt-2 pb-4 scrollbar-hide">

      <!-- STAN: WYSZUKIWANIE -->
      <div v-if="isSearching" class="flex justify-center mt-6 text-gray-500 text-[15px]">
        Szukanie wiadomości...
      </div>

      <!-- STAN: WYNIKI WYSZUKIWANIA -->
      <div v-else-if="searchResults.length > 0">
        <div
          v-for="msg in searchResults"
          :key="msg.id"
          class="flex items-center gap-3 py-2.5 px-4 hover:bg-gray-50 dark:hover:bg-gray-800/50 cursor-pointer transition"
          @click="goToMessage(msg)"
        >
          <div class="relative shrink-0">
            <img
              :src="chatAvatarUrl"
              alt="avatar"
              class="w-12 h-12 rounded-full object-cover"
            />
          </div>

          <div class="flex-1 min-w-0 flex flex-col justify-center">
            <h3 class="text-[16px] font-medium text-black dark:text-white leading-tight mb-0.5">
              {{ msg.sender }}
            </h3>

            <div class="text-[14px] text-gray-500 truncate leading-snug flex items-center">
              <span class="truncate max-w-[80%]" v-html="highlightText(msg.content)"></span>
              <span class="ml-1 shrink-0 whitespace-nowrap"> &middot; {{ formatTime(msg.time) }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- ZACHĘTA DO SZUKANIA (jeśli jest wpisany tekst, ale nie szukamy) -->
      <div
        v-else-if="searchQuery && searchResults.length === 0"
        class="flex justify-center mt-6 text-gray-500 text-[15px]"
      >
        Naciśnij Enter, aby wyszukać.
      </div>

    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onUnmounted } from 'vue'
import { useConversationsStore } from '@/stores/conversations'
import { decryptMessage } from '@/utils/e2ee'
import type { ChatMessage } from '@/types/Message'
import LoadingSpinner from '~/components/common/LoadingSpinner.vue';

const emit = defineEmits<{
  (e: 'close'): void
  (e: 'go-to-message', payload: { id: string | number; chatId?: string | number }): void
}>()

const props = withDefaults(defineProps<{ boxId?: string | number }>(), { boxId: undefined })

const convStore = useConversationsStore()

import { getSymmetricUuid } from '@/utils/uuid'

const searchQuery = ref<string>('')
const searchResults = ref<any[]>([])
const isSearching = ref(false)

let searchCancelToken = { cancelled: false }

const chatMeta = computed(() => {
  const meta = convStore.chats.find((c) => String(c.id) === String(props.boxId))
  return meta || { avatarUrl: '' }
})

const chatAvatarUrl = computed(() => {
  return chatMeta.value.avatarUrl || '/default-avatar.png'
})

function getSymmetricConversationId(chatId: string | number): string {
  const chat = convStore.chats.find((c: any) => String(c.id) === String(chatId))
  const isGroup = chat ? chat.type === 'group' : false
  if (isGroup) return String(chatId)

  const currentUserUuidRaw = localStorage.getItem('auth-current-user-id') || localStorage.getItem('user-uuid') || ''
  const currentUserUuid = String(currentUserUuidRaw).replace('user_', '')
  const cleanId = String(chatId).replace('user_', '')
  if (!currentUserUuid) return cleanId

  return getSymmetricUuid(currentUserUuid, cleanId)
}

async function startSearch() {
  if (!searchQuery.value.trim() || !props.boxId) return

  searchCancelToken.cancelled = true
  searchCancelToken = { cancelled: false }
  const currentToken = searchCancelToken

  isSearching.value = true
  searchResults.value = []

  try {
    const conversationId = getSymmetricConversationId(props.boxId)
    const apiUrl = import.meta.env.VITE_API_URL || 'http://localhost:8080'

    const token = localStorage.getItem('keycloak-token')
    const headers: Record<string, string> = {}
    if (token) headers['Authorization'] = `Bearer ${token}`

    const serverMsgs = await $fetch<any[]>(`${apiUrl}/api/chat/messages?conversationId=${conversationId}`, { headers })

    if (currentToken.cancelled) return

    if (!serverMsgs || serverMsgs.length === 0) {
      isSearching.value = false
      return
    }

    const query = searchQuery.value.toLowerCase()
    const totalMsgs = serverMsgs.length
    const chunkSize = 30
    const totalChunks = Math.ceil(totalMsgs / chunkSize)

    const chat = convStore.chats.find((c: any) => String(c.id) === String(props.boxId))
    const isPrivate = chat ? chat.type !== 'group' : true
    const currentUserUuidRaw = localStorage.getItem('auth-current-user-id') || localStorage.getItem('user-uuid') || ''
    const currentUserUuid = String(currentUserUuidRaw).replace('user_', '')

    for (let i = 0; i < totalChunks; i++) {
      if (currentToken.cancelled) return

      const start = i * chunkSize
      const end = Math.min(start + chunkSize, totalMsgs)
      const chunk = serverMsgs.slice(start, end)

      const decryptedChunk = await Promise.all(
        chunk.map(async (msg: any) => {
          try {
            const decryptedText = await decryptMessage(msg.text, String(props.boxId), isPrivate)
            return {
              id: msg.messageId,
              sender: msg.senderId === currentUserUuid ? 'Ty' : (chat?.name || 'Rozmówca'),
              senderId: msg.senderId,
              content: decryptedText,
              time: msg.time
            }
          } catch (e) {
            return {
              id: msg.messageId,
              sender: msg.senderId === currentUserUuid ? 'Ty' : (chat?.name || 'Rozmówca'),
              senderId: msg.senderId,
              content: msg.text,
              time: msg.time
            }
          }
        })
      )

      if (currentToken.cancelled) return

      for (const msg of decryptedChunk) {
        if (msg.content && msg.content.toLowerCase().includes(query)) {
          searchResults.value.push(msg)
        }
      }

      await new Promise((resolve) => setTimeout(resolve, 30))
    }
  } catch (err) {
    console.error('Progressive E2EE search failed:', err)
  } finally {
    if (!currentToken.cancelled) {
      isSearching.value = false
    }
  }
}

function handleSearchInput() {
  // Jeśli pole jest puste, czyścimy wyniki i ewentualnie anulujemy trwające wyszukiwanie
  if (!searchQuery.value.trim()) {
    searchResults.value = []
    isSearching.value = false
    searchCancelToken.cancelled = true
  }
}

function escapeHtml(str: string): string {
  return String(str)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#039;')
}

function escapeRegex(str: string): string {
  return str.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')
}

function highlightText(text: string): string {
  if (!text) return ''
  const escaped = escapeHtml(text)
  const query = searchQuery.value.trim()
  if (!query) return escaped
  try {
    const regex = new RegExp(`(${escapeRegex(query)})`, 'gi')
    return escaped.replace(regex, '<strong class="font-bold text-black dark:text-white">$1</strong>')
  } catch {
    return escaped
  }
}

function clearSearch() {
  searchQuery.value = ''
  searchResults.value = []
  isSearching.value = false
  searchCancelToken.cancelled = true
}

function formatTime(t: number | string | undefined) {
  if (!t) return ''
  const date = typeof t === 'number' ? new Date(t) : new Date(String(t))

  return date.toLocaleDateString(undefined, {
    day: 'numeric',
    month: 'short',
    year: 'numeric'
  })
}

function goToMessage(msg: any) {
  emit('go-to-message', { id: msg.id, chatId: props.boxId })
}

onUnmounted(() => {
  searchCancelToken.cancelled = true
})
</script>

<style scoped>
.scrollbar-hide::-webkit-scrollbar {
  display: none;
}
.scrollbar-hide {
  -ms-overflow-style: none;
  scrollbar-width: none;
}
</style>
