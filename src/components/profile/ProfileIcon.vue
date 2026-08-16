<template>
  <!-- Główne zagnieżdżenie z klasą 'group' dla przycisku z 3 kropkami -->
  <div class="fixed bottom-4 right-4 z-50 flex flex-col items-end group">

    <!-- Przycisk z 3 kropkami oraz Tooltip -->
    <div
      v-if="chatStore.minimizedBoxCache && chatStore.minimizedBoxCache.length > 0"
      class="relative mb-3 flex justify-center w-12"
    >
      <button
        @click="isMenuOpen = !isMenuOpen"
        class="w-10 h-10 rounded-full bg-white shadow-md flex items-center justify-center hover:bg-gray-50 focus:outline-none border border-gray-100 text-gray-700 hover:text-black cursor-pointer transform scale-0 opacity-0 group-hover:scale-100 group-hover:opacity-100 hover:scale-110 transition-all duration-200 ease-out origin-center"
        aria-label="Opcje czatów"
      >
        <DotsHorizontalIcon :size="20" />
      </button>

      <!-- Tooltip / Popup Menu -->
      <transition
        enter-active-class="transition duration-200 ease-out"
        enter-from-class="transform scale-0 opacity-0"
        enter-to-class="transform scale-100 opacity-100"
        leave-active-class="transition duration-150 ease-in"
        leave-from-class="transform scale-100 opacity-100"
        leave-to-class="transform scale-0 opacity-0"
      >
        <div
          v-if="isMenuOpen"
          class="absolute right-0 top-full mt-3 w-72 bg-white rounded-2xl shadow-xl border border-gray-100 p-2 z-50 origin-top-right"
        >
          <div class="flex flex-col space-y-1">
            <button
              @click="closeAllChats"
              class="flex items-center space-x-3 w-full p-2.5 rounded-xl hover:bg-gray-100 transition-colors text-left cursor-pointer"
            >
              <div class="w-6 h-6 rounded-full border-2 border-black flex items-center justify-center shrink-0">
                <CloseIcon :size="14" class="text-black stroke-[1]" />
              </div>
              <span class="text-sm font-semibold text-black">Zamknij wszystkie czaty</span>
            </button>

            <button
              @click="minimizeAllChats"
              class="flex items-center space-x-3 w-full p-2.5 rounded-xl hover:bg-gray-100 transition-colors text-left cursor-pointer"
            >
              <div class="w-6 h-6 rounded-full border-2 border-black flex items-center justify-center shrink-0">
                <MinusIcon :size="14" class="text-black stroke-[1]" />
              </div>
              <span class="text-sm font-semibold text-black">Zminimalizuj otwarte czaty</span>
            </button>
          </div>

          <div class="absolute -top-2 right-4 w-4 h-4 bg-white rotate-45 border-l border-t border-gray-100"></div>
        </div>
      </transition>
    </div>

    <div>
      <transition-group name="list" tag="div" class="flex flex-col-reverse pb-3">
        <div
          v-for="boxId in chatStore.minimizedBoxCache"
          :key="boxId"
          class="relative mt-3 group/item flex items-center justify-center"
        >
          <!-- Dynamiczny dymek (aktywuje się przez group-hover/item) -->
          <div
            class="absolute right-[calc(100%+14px)] top-1/2 -translate-y-1/2 w-max max-w-[240px] bg-white rounded-xl shadow-md px-4 py-2 opacity-0 pointer-events-none transition-opacity duration-200 group-hover/item:opacity-100 z-50 flex flex-col"
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
            class="w-12 h-12 shadow-md rounded-full bg-blue-600 text-white flex items-center justify-center transition duration-300 ease-in-out hover:shadow-lg focus:outline-none focus:ring-4 focus:ring-blue-500 focus:ring-opacity-50 relative cursor-pointer"
            :aria-label="'Restore Chat ' + boxId"
          >
            <div class="w-full h-full rounded-full overflow-hidden">
              <img :src="getChatAvatar(boxId)" alt="avatar" class="w-full h-full object-cover" />
            </div>

            <!-- Aktywny status -->
            <span
              v-if="isChatActive(boxId)"
              class="absolute bottom-0 right-0 w-3 h-3 bg-[#24832c] border-2 border-white rounded-full z-10"
            ></span>

            <!-- Nieprzeczytana kropka -->
            <span
              v-if="isChatUnread(boxId)"
              class="absolute bottom-0 right-0 w-3.5 h-3.5 bg-blue-500 border-2 border-white rounded-full z-20"
            ></span>
          </button>

          <!-- Przycisk x (zamknij) widoczny na hover pojedynczego dymka -->
          <button
            @click.stop="closeMinimized(boxId)"
            class="absolute -top-1 cursor-pointer -right-1 w-5 h-5 bg-white border border-gray-100 rounded-full flex items-center justify-center shadow-md text-gray-700 opacity-0 transition-all duration-300 group-hover/item:opacity-100 hover:bg-gray-100 hover:text-black focus:outline-none z-10"
            aria-label="Zamknij"
          >
            <CloseIcon :size="12" />
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
import { ref } from 'vue'
import { useChatStore } from '@/stores/chat'
import { useConversationsStore } from '@/stores/conversations'
import PencilIcon from 'vue-material-design-icons/Pencil.vue'
import CloseIcon from 'vue-material-design-icons/Close.vue'
import MinusIcon from 'vue-material-design-icons/Minus.vue'
import DotsHorizontalIcon from 'vue-material-design-icons/DotsHorizontal.vue'

const chatStore = useChatStore()
const conversationsStore = useConversationsStore()

const isMenuOpen = ref(false)

const getChat = (boxId: string | number) => {
  return conversationsStore.chats.find(c => String(c.id) === String(boxId))
}

const getChatAvatar = (boxId: string | number) => {
  const c = getChat(boxId)
  return c?.avatarUrl || '/default-avatar.png'
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
}

const closeAllChats = () => {
  if (chatStore.removeAllBoxes) {
    chatStore.removeAllBoxes()
  } else {
    chatStore.minimizedBoxCache = []
  }
  isMenuOpen.value = false
}

const minimizeAllChats = () => {
  if (chatStore.minimizeAll) {
    chatStore.minimizeAll()
  }
  isMenuOpen.value = false
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
