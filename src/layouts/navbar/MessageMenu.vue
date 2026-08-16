<template>
  <div
    class="w-full md:w-[360px] mx-auto bg-theme-bg-secondary flex flex-col overflow-hidden min-h-0 max-h-[calc(100vh-4rem)]"
    :class="{ 'rounded-b-2xl shadow-2xl': !isEmbedded }"
  >
    <header class="p-4 flex justify-between items-center bg-theme-bg-secondary z-10 shrink-0">
      <div class="flex items-center space-x-2">
        <h1 class="text-2xl font-bold text-theme-text">{{ $t('header.title') }}</h1>
      </div>
      <div class="flex space-x-3 text-theme-text-secondary items-center">
        <!-- Zaktualizowany VDropdown -->
        <VDropdown :distance="12" placement="bottom-end">
          <button class="flex items-center justify-center p-1 rounded-full hover:bg-theme-hover transition-colors">
            <DotsHorizontalIcon class="h-6 w-6 cursor-pointer" />
          </button>
          <!-- Odbieramy funkcję "hide" z poppera i nasłuchujemy na nasz emit -->
          <template #popper="{ hide }">
            <SettingsMenu @open-alert="openAlertModal(hide)" />
          </template>
        </VDropdown>

        <NuxtLink to="/chat" class="flex items-center justify-center p-1 rounded-full hover:bg-theme-hover transition-colors">
          <ArrowExpandIcon class="h-6 w-6 cursor-pointer" />
        </NuxtLink>

        <button class="flex items-center justify-center p-1 rounded-full hover:bg-theme-hover transition-colors">
          <PencilOutlineIcon class="h-6 w-6 cursor-pointer" />
        </button>
      </div>
    </header>

    <div class="px-4 pb-3 shrink-0 flex items-center space-x-2">
      <button
        v-if="isSearchFocused"
        @click="closeSearch"
        class="p-1.5 rounded-full hover:bg-theme-hover text-theme-text cursor-pointer transition-colors shrink-0"
      >
        <ArrowLeftIcon class="h-5 w-5" />
      </button>

      <div class="flex items-center bg-theme-bg rounded-full p-2 flex-1 min-w-0">
        <MagnifyIcon v-if="!isSearchFocused" class="h-5 w-5 text-theme-text-secondary mx-2 shrink-0" />

        <input
          type="text"
          v-model="searchQuery"
          @focus="isSearchFocused = true"
          :placeholder="$t('common.search')"
          class="w-full bg-theme-bg border-none p-0 focus:ring-0 placeholder-theme-text-secondary text-sm text-theme-text outline-none px-2"
        />
      </div>
    </div>

    <template v-if="!isSearchFocused">
      <div class="flex items-center px-4 pb-3 space-x-1 shrink-0">
        <button
          @click="activeTab = 'all'"
          :class="
            activeTab === 'all'
              ? 'bg-[#e7f3ff] text-[#0064e0] dark:bg-blue-950 dark:text-blue-200 font-semibold'
              : 'text-gray-700 dark:text-gray-300 hover:bg-theme-hover font-medium'
          "
          class="py-1.5 px-3.5 rounded-full text-[14px] transition-colors duration-150 cursor-pointer"
        >
          {{ $t('chat.allChats') }}
        </button>

        <button
          @click="activeTab = 'unread'"
          :class="
            activeTab === 'unread'
              ? 'bg-[#e7f3ff] text-[#0064e0] dark:bg-blue-950 dark:text-blue-200 font-semibold'
              : 'text-gray-700 dark:text-gray-300 hover:bg-theme-hover font-medium'
          "
          class="py-1.5 px-3.5 rounded-full text-[14px] transition-colors duration-150 cursor-pointer"
        >
          {{ $t('chat.unread') }}
        </button>

        <div class="ml-auto p-1.5 rounded-full hover:bg-theme-hover transition-colors cursor-pointer text-gray-600 dark:text-gray-300 flex items-center justify-center">
          <DotsHorizontalIcon class="h-5 w-5" />
        </div>
      </div>
      <E2eeBackup/>
      <div class="flex-1 overflow-y-auto min-h-0 overscroll-y-contain">
        <ul class="px-4 space-y-1">
          <li v-for="chat in filteredChats" :key="chat.id">
            <button
              @click="handleClick(chat.id)"
              class="w-full group flex items-center py-2 px-1 hover:bg-theme-hover rounded-lg cursor-pointer transition duration-100 text-left"
              :class="{ 'bg-blue-100 dark:bg-blue-700': chat.id.toString() === currentRouteChatId }"
            >
              <div class="relative shrink-0 mr-3 w-12 h-12">
                <template v-if="chat.isGroup && chat.extraAvatars && chat.extraAvatars.length >= 2">
                  <img
                    :src="chat.extraAvatars[0]"
                    alt="Awatar"
                    class="absolute z-999 bottom-0 left-0 h-8 w-8 rounded-full object-cover border border-theme-border bg-theme-bg ring-2 ring-[#fff]"
                  />
                  <img
                    :src="chat.extraAvatars[1]"
                    alt="Awatar"
                    class="absolute top-0 right-0 h-8 w-8 rounded-full object-cover bg-theme-bg border border-theme-border"
                  />
                </template>
                <template v-else>
                  <img
                    :src="chat.avatarUrl"
                    alt="Awatar"
                    class="h-12 w-12 rounded-full object-cover bg-theme-bg border border-theme-border"
                  />
                </template>
                <span
                  v-if="chat.isActive"
                  class="absolute bottom-0 right-0 block h-3 w-3 rounded-full ring-2 ring-white bg-green-500"
                ></span>
              </div>

              <div class="grow min-w-0">
                <p class="text-theme-text truncate" :class="{ 'font-bold': chat.unread }">
                  {{ chat.name }}
                </p>
                <p
                  class="text-sm truncate"
                  :class="{
                    'font-bold text-theme-text': chat.unread,
                    'text-theme-text-secondary': !chat.unread,
                  }"
                >
                  <span v-html="chat.lastMessage"></span> · {{ chat.timeAgo }}
                </p>
              </div>

              <div class="shrink-0 ml-3 relative flex items-center space-x-1">
                <div v-if="chat.extraAvatars" class="flex -space-x-1 overflow-hidden">
                  <img
                    v-for="(avatar, index) in chat.extraAvatars"
                    :key="index"
                    :src="avatar"
                    class="inline-block h-5 w-5 rounded-full ring-2 ring-white bg-gray-300"
                  />
                </div>
                <div v-if="chat.unread" class="w-2 h-2 bg-blue-500 rounded-full shrink-0"></div>

                <HandRightIcon v-if="chat.isPinch" class="h-5 w-5 text-theme-text-secondary" />
                <VDropdown
                  :distance="30"
                  @show="() => setDropdownOpen(chat.id, true)"
                  @hide="() => setDropdownOpen(chat.id, false)"
                  @click.stop
                >
                  <div
                    :class="[
                      'group-hover:flex hover:bg-theme-hover absolute right-3 top-1/2 -translate-y-1/2 shadow-md border bg-theme-bg border-gray-300 items-center justify-center w-9 h-9 rounded-full',
                      openDropdowns[chat.id] ? 'flex' : 'hidden',
                    ]"
                  >
                    <DotsHorizontalIcon class="cursor-pointer" />
                  </div>
                  <template #popper="{ hide }">
                    <ContexMenu @select-action="(action) => handleMenuAction(chat.id, action, hide)" />
                  </template>
                </VDropdown>
              </div>
            </button>
          </li>
        </ul>
      </div>
    </template>

    <div v-else class="flex-1 overflow-y-auto px-4 min-h-0">
      <div v-if="recentSearches.length > 0 && !searchQuery" class="mb-4">
        <h3 class="text-sm font-semibold text-theme-text-secondary mb-2">Ostatnie wyszukiwania</h3>
        <ul>
          <li
            v-for="item in recentSearches"
            :key="item.id"
            class="flex items-center justify-between py-2 hover:bg-theme-hover rounded-lg px-1 cursor-pointer"
          >
            <div class="flex items-center space-x-3 grow min-w-0" @click="handleClick(item.id)">
              <img :src="item.avatarUrl" class="w-10 h-10 rounded-full object-cover shrink-0" />
              <span class="text-sm font-medium text-theme-text truncate">{{ item.name }}</span>
            </div>
            <button @click.stop="removeRecent(item.id)" class="text-gray-400 hover:text-gray-600 p-1 shrink-0">
              <CloseIcon class="h-5 w-5" />
            </button>
          </li>
        </ul>
      </div>

      <div>
        <h3 class="text-sm font-semibold text-theme-text-secondary mb-2">Twoje kontakty</h3>
        <ul>
          <li
            v-for="contact in filteredContacts"
            :key="contact.id"
            @click="handleClick(contact.id)"
            class="flex items-center space-x-3 py-2 hover:bg-theme-hover rounded-lg px-1 cursor-pointer"
          >
            <div class="relative w-10 h-10 shrink-0">
              <img
                v-if="contact.avatarUrl"
                :src="contact.avatarUrl"
                class="w-10 h-10 rounded-full object-cover"
              />
              <div
                v-else
                class="w-10 h-10 rounded-full bg-gray-300 dark:bg-gray-700 flex items-center justify-center text-gray-500"
              >
                <AccountIcon class="h-6 w-6" />
              </div>
            </div>
            <span class="text-sm font-medium text-theme-text truncate">{{ contact.name }}</span>
          </li>
        </ul>
      </div>
    </div>

    <ChatMuteModal
      v-if="activeMuteChatId !== null"
      @save="(duration) => handleMuteChat(activeMuteChatId, duration)"
      @close="activeMuteChatId = null"
    />

    <!-- Przeniesiony BaseModal -->
    <BaseModal v-if="isAlertModalOpen" no-header @close="isAlertModalOpen = false">
      <AlertLoginModal @close="isAlertModalOpen = false"/>
    </BaseModal>
  </div>
</template>

<script setup lang="ts">
import { ref, type Ref, computed } from 'vue'

defineProps({
  isEmbedded: {
    type: Boolean,
    default: false,
  },
})

// IKONY
import DotsHorizontalIcon from 'vue-material-design-icons/DotsHorizontal.vue'
import ArrowExpandIcon from 'vue-material-design-icons/ArrowExpand.vue'
import PencilOutlineIcon from 'vue-material-design-icons/PencilOutline.vue'
import MagnifyIcon from 'vue-material-design-icons/Magnify.vue'
import ArrowLeftIcon from 'vue-material-design-icons/ArrowLeft.vue'
import CloseIcon from 'vue-material-design-icons/Close.vue'
import AccountIcon from 'vue-material-design-icons/Account.vue'
import HandRightIcon from 'vue-material-design-icons/HandPointingRight.vue'

// IMPORTY KOMPONENTÓW I MODALI
import ContexMenu from '@/components/chat/ContextMenu.vue'
import ChatMuteModal from '@/components/chat/info/modals/ChatMuteModal.vue'
import SettingsMenu from './SettingsMenu.vue'
import E2eeBackup from '~/components/chat/E2eeBackup.vue'
import BaseModal from '~/components/common/BaseModal.vue'
import AlertLoginModal from './AlertLoginModal.vue'

// STORE & TYPES
import { useConversationsStore } from '@/stores/conversations'
import { useChatStore } from '@/stores/chat'
import type { Chat } from '@/types/Chat'

const activeTab: Ref<'all' | 'unread'> = ref('all')
const activeMuteChatId = ref<string | number | null>(null)

// STAN MODALA
const isAlertModalOpen = ref(false)

// FUNKCJA OTWIERAJĄCA MODAL I ZAMYKAJĄCA DROPDOWN
const openAlertModal = (hideDropdown: () => void) => {
  hideDropdown()
  isAlertModalOpen.value = true
}

// STANY DLA WYSZUKIWARKI
const isSearchFocused = ref(false)
const searchQuery = ref('')

const closeSearch = () => {
  isSearchFocused.value = false
  searchQuery.value = ''
}

// Przykładowe ostatnie wyszukiwania
const recentSearches = ref([
  { id: '1', name: 'Bartosz Miazek', avatarUrl: 'https://i.pravatar.cc/150?img=12' },
])

const removeRecent = (id: string) => {
  recentSearches.value = recentSearches.value.filter(item => item.id !== id)
}

const handleMenuAction = (chatId: string | number, action: string, hide: () => void) => {
  hide()
  if (action === 'mute-notifications') {
    activeMuteChatId.value = chatId
  }
}

const handleMuteChat = (chatId: string | number, duration: string) => {
  convStore.muteChat(chatId, duration)
  activeMuteChatId.value = null
}

const convStore = useConversationsStore()
const chatStore = useChatStore()
const chats = computed(() => convStore.chats as Chat[])

const openDropdowns = ref<Record<number, boolean>>({})
const setDropdownOpen = (id: number, value: boolean) => {
  openDropdowns.value[id] = value
}

// Filtrowanie zwykłej listy
const filteredChats = computed(() => {
  if (activeTab.value === 'unread') {
    return chats.value.filter((n) => n.unread)
  }
  return chats.value
})

// Filtrowanie kontaktów w trybie wyszukiwania
const filteredContacts = computed(() => {
  if (!searchQuery.value) return chats.value
  return chats.value.filter(chat =>
    chat.name.toLowerCase().includes(searchQuery.value.toLowerCase())
  )
})

const router = useRouter()
const route = useRoute()

const isInChatView = computed(() => route.path.startsWith('/chat'))
const currentRouteChatId = computed(() => route.params.chatId as string)

const handleClick = (chatId: string | number): void => {
  if (isInChatView.value) {
    router.push(`/chat/${chatId}`).catch(() => {})
  } else {
    chatStore.addMessageBox(chatId)
  }
}
</script>
