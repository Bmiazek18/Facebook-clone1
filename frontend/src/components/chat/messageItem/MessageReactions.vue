<script setup lang="ts">
import { ref, computed, watch, nextTick } from 'vue'
import ReplyIcon from 'vue-material-design-icons/Reply.vue'
import VerticalDotsIcon from 'vue-material-design-icons/DotsVertical.vue'
import EmoticonHappyOutlineIcon from 'vue-material-design-icons/EmoticonHappyOutline.vue'
import PlusIcon from 'vue-material-design-icons/Plus.vue'
import MagnifyIcon from 'vue-material-design-icons/Magnify.vue'

// Komponenty zewnętrzne
import BaseModal from '@/components/common/BaseModal.vue'
import ShareModal from './modals/ShareModal.vue'
import LazyEmojiPicker from '@/components/common/LazyEmojiPicker.vue'
import { useConversationsStore } from '@/stores/conversations'

const showModalInPicker = ref(false)
const props = defineProps<{
  messageId: number | string
  reactions: Record<string, number | string[]> | undefined
  isMe: boolean
}>()

const emit = defineEmits<{
  (e: 'add-reaction', payload: { messageId: number | string; emoji: string }): void
  (e: 'reply', messageId: number | string): void
  (e: 'confirm-unsend', payload: { messageId: number | string; mode: 'everyone' | 'me' }): void
  (e: 'forward', payload: { messageId: number | string; targetChatId: string | number }): void
  (e: 'pin', messageId: number | string): void
}>()

const convStore = useConversationsStore()
const reactionsDropdownRef = ref<any>(null)
const reactionsList = ['👍', '❤️', '😂', '😮', '😢', '👏']
const isUnsendModalOpen = ref(false)
const selectedUnsendMode = ref<'everyone' | 'me'>('everyone')
const isShareModalOpen = ref(false)
const searchForwardQuery = ref('')
const sentChats = ref<Record<string | number, boolean>>({})

const isReactionsDropdownOpen = ref(false)
const isActionsDropdownOpen = ref(false)

const showFullPicker = ref(false)

const toggleFullPicker = () => {
  isReactionsDropdownOpen.value = false
  setTimeout(() => {
    showFullPicker.value = true
    isReactionsDropdownOpen.value = true
  }, 100)
}

watch(isReactionsDropdownOpen, (val) => {
  if (!val && showFullPicker.value) {
    setTimeout(() => {
      showFullPicker.value = false
    }, 300)
  }
})

const isAnyDropdownOpen = computed(
  () => isReactionsDropdownOpen.value || isActionsDropdownOpen.value,
)

const selectReaction = (emoji: string) => {
  emit('add-reaction', { messageId: props.messageId, emoji })
  isReactionsDropdownOpen.value = false
}

const handleConfirmUnsend = () => {
  emit('confirm-unsend', {
    messageId: props.messageId,
    mode: selectedUnsendMode.value,
  })
  isUnsendModalOpen.value = false
}

// Filtrowanie czatów do przekazania
const filteredForwardChats = computed(() => {
  const query = searchForwardQuery.value.toLowerCase().trim()
  if (!query) return convStore.chats
  return convStore.chats.filter(c => c.name.toLowerCase().includes(query))
})

const handleSendForward = (chatId: string | number) => {
  emit('forward', { messageId: props.messageId, targetChatId: chatId })
  sentChats.value[chatId] = true
  setTimeout(() => {
    sentChats.value[chatId] = false
  }, 2000)
}
</script>

<template>
  <div
    class="flex items-center gap-1 mx-1 transition-opacity"
    :class="[
      isMe ? 'flex-row-reverse' : '',
      isAnyDropdownOpen ? 'opacity-100' : 'opacity-0 group-hover:opacity-100',
    ]"
  >
    <VDropdown
      ref="reactionsDropdownRef"
      :placement="'top'"
      :distance="showFullPicker ? 12 : 4"
      :theme="showFullPicker ? 'custom-messenger-theme' : 'no-arrow'"
      :triggers="['click']"
      v-model:shown="isReactionsDropdownOpen"
      :auto-hide="!showModalInPicker"
    >
      <button
        class="w-7 h-7 rounded-full bg-white hover:bg-gray-100 flex items-center justify-center shadow-sm border border-gray-100 transition-colors"
      >
        <EmoticonHappyOutlineIcon :size="18" class="text-black" />
      </button>

      <template #popper>
        <div
          v-if="!showFullPicker"
          class="flex items-center rounded-[24px] h-[52px] px-2 py-1.5 bg-white border border-gray-200"
        >
          <span
            v-for="emoji in reactionsList"
            :key="emoji"
            class="cursor-pointer text-[28px] hover:scale-125 transition-transform px-0.5 flex items-center justify-center"
            @click.stop="selectReaction(emoji)"
          >
            {{ emoji }}
          </span>

          <button
            @click.stop="toggleFullPicker"
            class="ml-1 w-[38px] h-[38px] rounded-full bg-gray-100 hover:bg-gray-200 flex items-center justify-center transition-colors shrink-0"
          >
            <PlusIcon :size="24" class="text-black" />
          </button>
        </div>

        <div
          v-else
          class="emoji-picker-container border border-gray-100 dark:border-none overflow-hidden"
        >
          <LazyEmojiPicker
            :customReactions="reactionsList"
            @modal-state="(state) => (showModalInPicker = state)"
            @select="(e: any) => selectReaction(e.native)"
          />
        </div>
      </template>
    </VDropdown>

    <button
      @click="emit('reply', props.messageId)"
      class="w-7 h-7 rounded-full bg-white hover:bg-gray-100 flex items-center justify-center shadow-sm border border-gray-100 transition-colors"
    >
      <ReplyIcon :size="18" class="text-black" />
    </button>

    <VDropdown
      :placement="isMe ? 'top-end' : 'top-start'"
      :distance="10"
      :triggers="['click']"
      v-model:shown="isActionsDropdownOpen"
    >
      <button
        class="w-7 h-7 rounded-full bg-white hover:bg-gray-100 flex items-center justify-center shadow-sm border border-gray-100 transition-colors"
      >
        <VerticalDotsIcon :size="18" class="text-black" />
      </button>

      <template #popper="{ hide }">
        <div
          class="flex flex-col min-w-[160px] p-1.5 bg-white rounded-xl shadow-2xl border border-gray-50"
        >
          <button
            @click="
              isUnsendModalOpen = true,
              hide()
            "
            class="w-full text-left px-4 py-2.5 text-[15px] font-medium text-gray-700 hover:bg-gray-100 rounded-lg transition-colors"
          >{{ $t('chat.cofnijWyslanie') }}</button>
          <button
            @click="
              isShareModalOpen = true,
              hide()
            "
            class="w-full text-left px-4 py-2.5 text-[15px] font-medium text-gray-700 hover:bg-gray-100 rounded-lg transition-colors"
          >{{ $t('chat.przeslij') }}</button>
          <button
            @click="emit('pin', props.messageId)"
            class="w-full text-left px-4 py-2.5 text-[15px] font-medium text-gray-700 hover:bg-gray-100 rounded-lg transition-colors"
          >{{ $t('actions.pin') }}</button>
        </div>
      </template>
    </VDropdown>

    <!-- Modal: Cofnij wysłanie -->
    <BaseModal
      v-if="isUnsendModalOpen"
      @close="isUnsendModalOpen = false"
      :title="$t('chat.wPrzypadkuKogoChcesz')"
      :showCloseButton="false"
    >
      <div class="w-[700px] p-6">
        <div class="flex justify-end gap-2 mt-4">
          <button @click="isUnsendModalOpen = false" class="px-4 py-2 font-bold text-blue-600">{{ $t('common.cancel') }}</button>
          <button
            @click="handleConfirmUnsend"
            class="px-6 py-2 font-bold text-white bg-blue-600 rounded-lg"
          >{{ $t('notifications_page.delete') }}</button>
        </div>
      </div>
    </BaseModal>

    <!-- Modal: Prześlij (zgodny z przesłanym zrzutem ekranu) -->
    <BaseModal
      v-if="isShareModalOpen"
      @close="isShareModalOpen = false"
      :title="$t('actions.forward')"
    >
      <div class="w-[520px] max-h-[80vh] flex flex-col p-4">
        <!-- Pasek wyszukiwania -->
        <div class="relative mb-4">
          <span class="absolute inset-y-0 left-0 flex items-center pl-3 pointer-events-none text-gray-400">
            <MagnifyIcon :size="20" />
          </span>
          <input
            v-model="searchForwardQuery"
            type="text"
            :placeholder="$t('chat.szukajOsobIGrup')"
            class="w-full pl-10 pr-4 py-2.5 bg-gray-100 dark:bg-gray-800 rounded-full text-sm outline-none border-none focus:ring-2 focus:ring-blue-500 transition"
          />
        </div>

        <div class="text-xs font-semibold text-gray-500 uppercase tracking-wider mb-2">{{ $t('emojiPicker.recent') }}</div>

        <!-- Lista czatów -->
        <div class="overflow-y-auto max-h-[400px] space-y-1 custom-scrollbar pr-1">
          <div
            v-for="chat in filteredForwardChats"
            :key="chat.id"
            class="flex items-center justify-between p-2 hover:bg-gray-100 dark:hover:bg-gray-800 rounded-xl transition cursor-pointer"
          >
            <div class="flex items-center space-x-3">
              <img
                :src="chat.avatarUrl || 'https://i.pravatar.cc/150?img=' + chat.id"
                class="w-12 h-12 rounded-full object-cover"
                :alt="$t('chat.avatar')"
              />
              <span class="font-semibold text-gray-900 dark:text-gray-100 text-[15px]">{{ chat.name }}</span>
            </div>
            <button
              @click="handleSendForward(chat.id)"
              class="px-5 py-1.5 rounded-full text-sm font-semibold transition shadow-sm"
              :class="sentChats[chat.id] ? 'bg-green-600 text-white' : 'bg-blue-50 hover:bg-blue-100 text-blue-600 dark:bg-blue-950 dark:hover:bg-blue-900 dark:text-blue-300'"
            >
              {{ sentChats[chat.id] ? 'Wysłano' : 'Wyślij' }}
            </button>
          </div>
        </div>
      </div>
    </BaseModal>
  </div>
</template>

<style>
.emoji-picker-container {
  width: 320px;
  overflow: visible;
  border-radius: 80px !important;
}
.v-popper__inner {
  overflow: visible !important;
  border-radius: 20px !important;
}
.v-popper--theme-custom-messenger-theme {
  background: transparent !important;
  border: none !important;
  box-shadow: none !important;
  padding: 0 !important;
  z-index: 50 !important;
}
.v-popper--theme-custom-messenger-theme .v-popper__arrow-outer,
.v-popper--theme-custom-messenger-theme .v-popper__arrow-inner {
  border: none !important;
  background: transparent !important;
}
.v-popper--theme-custom-messenger-theme .v-popper__arrow-inner {
  display: none !important;
}
.v-popper--theme-custom-messenger-theme .v-popper__arrow-container {
  width: 25px !important;
  height: 12px !important;
  margin-top: -2px !important;
  z-index: 1 !important;
  margin-left: 7px !important;
}
.v-popper--theme-no-arrow .v-popper__arrow-container {
  display: none !important;
}
.v-popper--theme-custom-messenger-theme .v-popper__arrow-outer {
  display: block !important;
  width: 25px !important;
  height: 12px !important;
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 25 12' fill='%23fff'%3E%3Cpath d='M24.453.001c-2.791.32-5.922 1.53-7.78 3.455l-9.62 7.023c-2.45 2.54-5.78 1.645-5.78-2.487V1.983C1.273 1.089.746.32 0 0h24.453v.001Z'/%3E%3C/svg%3E") !important;
  background-size: contain !important;
  background-repeat: no-repeat !important;
  background-position: center !important;
}
.v-popper--theme-custom-messenger-theme .v-popper__inner {
  box-shadow: 0 8px 12px 0 rgba(0, 0, 0, 0.1) !important;
  border-radius: 12px;
}
</style>
