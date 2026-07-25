<template>
  <div class="fixed bottom-4 right-4 z-50">
    <div>
      <transition-group name="list" tag="div" class="flex flex-col-reverse pb-3">
        <div
          v-for="boxId in chatStore.minimizedBoxCache"
          :key="boxId"
          class="relative mt-3 group flex items-center justify-center"
        >

          <!-- Dynamiczny dymek z informacjami z inboxa -->
          <div
            class="absolute right-[calc(100%+14px)] top-1/2 -translate-y-1/2 w-max max-w-[240px] bg-white rounded-xl shadow-md px-4 py-2 opacity-0 pointer-events-none transition-opacity duration-300 group-hover:opacity-100 z-50 flex flex-col"
          >
            <span class="font-bold text-gray-900 text-sm truncate">{{ getChatName(boxId) }}</span>
            <span
              class="text-xs truncate"
              :class="isChatUnread(boxId) ? 'text-black font-semibold' : 'text-gray-500'"
            >
              {{ getChatLastMessage(boxId) }}
            </span>

            <div class="absolute -right-1.5 top-1/2 -translate-y-1/2 w-3 h-3 bg-white rotate-45 rounded-sm"></div>
          </div>

          <button
            @click="restoreMessageBox(boxId)"
            class="w-12 h-12 shadow-md rounded-full bg-blue-600 text-white flex items-center justify-center transition duration-300 ease-in-out hover:shadow-lg focus:outline-none focus:ring-4 focus:ring-blue-500 focus:ring-opacity-50 relative"
            :aria-label="'Restore Chat ' + boxId"
          >
            <div class="w-full h-full rounded-full overflow-hidden">
              <img :src="getChatAvatar(boxId)" alt="avatar" class="w-full h-full object-cover" />
            </div>

            <!-- Aktywny status (zielone kółko) -->
            <span
              v-if="isChatActive(boxId)"
              class="absolute bottom-0 right-0 w-3 h-3 bg-[#24832c] border-2 border-white rounded-full z-10"
            ></span>

            <!-- Nieprzeczytana kropka na awatarze -->
            <span
              v-if="isChatUnread(boxId)"
              class="absolute bottom-0 right-0 w-3.5 h-3.5 bg-blue-500 border-2 border-white rounded-full z-20"
            ></span>
          </button>

          <button
            @click.stop="closeMinimized(boxId)"
            class="absolute -top-1 cursor-pointer -right-1 w-5 h-5 bg-white border border-gray-100 rounded-full flex items-center justify-center shadow-md text-gray-700 opacity-0 transition-all duration-300 group-hover:opacity-100 hover:bg-gray-100 hover:text-black focus:outline-none z-10"
            aria-label="Zamknij"
          >
            <svg xmlns="http://www.w3.org/2000/svg" class="h-3 w-3" viewBox="0 0 20 20" fill="currentColor">
              <path fill-rule="evenodd" d="M4.293 4.293a1 1 0 011.414 0L10 8.586l4.293-4.293a1 1 0 111.414 1.414L11.414 10l4.293 4.293a1 1 0 01-1.414 1.414L10 11.414l-4.293 4.293a1 1 0 01-1.414-1.414L8.586 10 4.293 5.707a1 1 0 010-1.414z" clip-rule="evenodd" />
            </svg>
          </button>

        </div>
      </transition-group>
    </div>

    <button
      class="w-12 h-12 cursor-pointer rounded-full shadow-xl flex items-center bg-theme-bg-secondary justify-center transition duration-300 ease-in-out hover:shadow-2xl focus:outline-none focus:ring-4 focus:ring-blue-500 focus:ring-opacity-50"
      aria-label="Add New Element"
    >
      <PencilIcon :size="26" class="text-theme-text" />
    </button>
  </div>
</template>

<script setup lang="ts">
import { useChatStore } from '@/stores/chat'
import { useConversationsStore } from '@/stores/conversations'
import PencilIcon from 'vue-material-design-icons/Pencil.vue'

const chatStore = useChatStore()
const conversationsStore = useConversationsStore()

const getChat = (boxId: string | number) => {
  return conversationsStore.chats.find(c => String(c.id) === String(boxId))
}

const getChatAvatar = (boxId: string | number) => {
  const c = getChat(boxId)
  return c?.avatarUrl || 'http://localhost:8080/api/users/avatar/default-avatar.svg'
}

const getChatName = (boxId: string | number) => {
  const c = getChat(boxId)
  return c?.name || 'Użytkownik'
}

const getChatLastMessage = (boxId: string | number) => {
  const c = getChat(boxId)
  return c?.lastMessage || 'Brak wiadomości'
}

const isChatUnread = (boxId: string | number) => {
  const c = getChat(boxId)
  return c?.unread || false
}

const isChatActive = (boxId: string | number) => {
  const c = getChat(boxId)
  return c?.isActive || false
}

const restoreMessageBox = (boxId: string | number) => {
  chatStore.toggleMinimize(boxId)
}


const closeMinimized = (boxId: string | number) => {
  chatStore.removeMessageBox(boxId)
  console.log('Zamknięto zminimalizowany dymek:', boxId)
}
</script>

<style scoped>
.list-enter-active,
.list-leave-active {
  transition: all 0.5s ease;
}
.list-enter-from,
.list-leave-to {
  opacity: 0;
  transform: translateY(30px);
}

.list-leave-active {
  position: absolute;
}
</style>
