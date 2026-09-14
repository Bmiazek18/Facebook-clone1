<script setup lang="ts">
import { ref, computed } from 'vue'
import { useChatThemeStore } from '@/stores/chatTheme'
import type { Theme } from '@/types/Theme'
import { storeToRefs } from 'pinia'
import { useChatStore } from '@/stores/chat'
import { useConversationsStore } from '@/stores/conversations'

import ChevronDownIcon from 'vue-material-design-icons/ChevronDown.vue'
import ArrowLeftIcon from 'vue-material-design-icons/ArrowLeft.vue'
import PhoneIcon from 'vue-material-design-icons/Phone.vue'
import VideoOutlineIcon from 'vue-material-design-icons/VideoOutline.vue'
import MinusIcon from 'vue-material-design-icons/Minus.vue'
import CloseIcon from 'vue-material-design-icons/Close.vue'
import Information from 'vue-material-design-icons/Information.vue'
import PinIcon from 'vue-material-design-icons/Pin.vue'
import IncomingCallModal from '@/components/chat/modals/IncomingCallModal.vue'
import ChatSettingModal from './modals/ChatSettingModal.vue'
import BaseModal from '~/components/common/BaseModal.vue'
import PinnedModal from './modals/PinnedModal.vue'

const props = defineProps<{
  title: string
  subtitle?: string
  avatarUrl?: string
  users: string[]
  boxId: string | number
  hideIcons?: boolean
  themes?: Theme
  isHasPinned?: boolean
  pinnedMessage?: { sender: string; content: string }
  isOnline?: boolean
  isUnread?: boolean
}>()

const emit = defineEmits<{
  (e: 'back'): void
  (e: 'show-info'): void
  (e: 'click-pinned'): void // <--- Opcjonalny emit do obsługi kliknięcia w przypiętą wiadomość
  (e: 'mark-as-read'): void
}>()

const { selectedTheme } = storeToRefs(useChatThemeStore())
const activeTheme = computed(() => props.themes || selectedTheme.value)

const chatStore = useChatStore()
const conversationsStore = useConversationsStore()
const config = useRuntimeConfig()
const router = useRouter()
const showPinnedModal = ref(false)
const close = (boxId: string | number) => {
  chatStore.removeMessageBox(boxId)
}
const minimize = (boxId: string | number) => {
  chatStore.toggleMinimize(boxId)
}

const isCallIncoming = ref(false)
const isChatSettingModalOpen = ref(false)

const otworzOknoRozmowy = async (typPolaczenia: 'audio' | 'video') => {
  emit('mark-as-read')
  try {
    await $fetch(`${config.public.apiUrl}/api/chat/calls/start`, {
      method: 'POST',
      query: {
        conversationId: props.boxId.toString(),
        callerId: conversationsStore.currentUserUuid,
        callType: typPolaczenia,
      },
    })
  } catch (err) {
    console.error('Failed to register call start in Redis:', err)
  }

  const conversationId = conversationsStore.getSymmetricConversationId(props.boxId)

  // 2. Wysłanie sygnału połączenia przychodzącego przez MQTT
  if (conversationsStore.isMqttConnected) {
    const cleanRecipientId = String(props.boxId).replace(/^user_/, '')
    const cleanSenderId = String(conversationsStore.currentUserUuid).replace(/^user_/, '')
    const payload = {
      type: 'call_initiated',
      conversationId: conversationId.toString(),
      callerId: cleanSenderId,
      callType: typPolaczenia,
      participantIds: [cleanSenderId, cleanRecipientId],
    }
    conversationsStore.publishMqtt('chat/messages/user/' + cleanRecipientId, payload)
    console.log('Published call_initiated MQTT event:', payload)
  }

  const routeData = router.resolve({
    name: 'video-call',
    query: {
      type: typPolaczenia,
      boxId: props.boxId.toString(),
      callerId: conversationsStore.currentUserUuid,
      conversationId: conversationId.toString(),
    },
  })

  window.open(
    routeData.href,
    `Rozmowa_${conversationId}`,
    'popup=yes,width=900,height=650,left=300,top=150,resizable=yes,location=no,toolbar=no,menubar=no',
  )
}
</script>

<template>
  <div
    class="flex flex-col rounded-t-xl overflow-hidden border-b border-black/5 cursor-pointer"
    @click="emit('mark-as-read')"
  >
    <!-- GŁÓWNY NAGŁÓWEK -->
    <header
      class="flex items-center justify-between px-3 py-2.5 transition-colors duration-300 select-none"
      :class="[
        hideIcons ? 'h-[64px]' : 'h-[48px]',
        isUnread
          ? 'bg-[#0866ff] dark:bg-[#0055d4] text-white shadow-sm'
          : 'bg-theme-bg-secondary'
      ]"
      :style="isUnread ? {} : { backgroundColor: activeTheme.headerColor || 'transparent' }"
    >
      <div class="flex items-center gap-2 min-w-0">
        <button
          v-tooltip.top="'Wróć do listy'"
          @click.stop="emit('back')"
          class="md:hidden shrink-0 rounded-full p-1 transition-colors"
          :class="isUnread ? 'hover:bg-white/20' : 'hover:bg-black/5'"
        >
          <ArrowLeftIcon
            :size="hideIcons ? 32 : 24"
            :fillColor="isUnread ? '#ffffff' : activeTheme.iconColor"
          />
        </button>

        <div
          :class="{ 'w-8 h-8': !hideIcons, 'w-10 h-10': hideIcons }"
          class="relative shrink-0 rounded-full bg-black/5 flex items-center justify-center border border-black/10"
        >
          <img v-if="avatarUrl" :src="avatarUrl" :alt="$t('chat.avatar')" class="w-full h-full object-cover rounded-full" />
          <span v-else class="text-xl">🧑‍🤝‍🧑</span>
          <span
            v-if="isOnline || subtitle?.includes('teraz')"
            class="absolute bottom-0 right-0 w-2.5 h-2.5 bg-green-500 border-2 border-white dark:border-[#242526] rounded-full"
          ></span>
        </div>

        <ChatSettingModal
          :is-open="isChatSettingModalOpen"
          @close="isChatSettingModalOpen = false"
          :chatId="boxId"
        >
          <div
            @click.stop="isChatSettingModalOpen = true; emit('mark-as-read')"
            class="flex items-center min-w-0 py-1 cursor-pointer rounded-lg transition-colors"
            :class="[
              hideIcons ? 'gap-2' : 'gap-1.5',
              isUnread ? 'hover:bg-white/10 px-1' : 'hover:bg-black/5'
            ]"
          >
            <div class="flex flex-col min-w-0 leading-tight">
              <div class="flex items-center gap-1.5 min-w-0">
                <span
                  class="truncate transition-colors"
                  :class="[
                    hideIcons ? 'text-[16px]' : 'text-[15px]',
                    isUnread ? 'font-bold text-white' : 'font-semibold'
                  ]"
                  :style="isUnread ? { color: '#ffffff' } : { color: activeTheme.headerTextColor || '#111827' }"
                >
                  {{ title }}
                </span>
                <span
                  v-if="isUnread"
                  class="w-2 h-2 rounded-full bg-white shrink-0 animate-pulse"
                ></span>
              </div>
              <span
                v-if="subtitle"
                class="truncate transition-colors"
                :class="hideIcons ? 'text-[13px]' : 'text-[12px]'"
                :style="isUnread ? { color: 'rgba(255, 255, 255, 0.85)' } : { color: activeTheme.headerTextColor, opacity: 0.6 }"
              >
                {{ subtitle }}
              </span>
            </div>

            <button
              v-if="!hideIcons"
              class="shrink-0 p-1 rounded-full transition-colors flex items-center justify-center"
              :class="isUnread ? 'hover:bg-white/20' : 'hover:bg-black/5'"
            >
              <ChevronDownIcon
                :size="20"
                :fillColor="isUnread ? '#ffffff' : (activeTheme.primaryColor || activeTheme.iconColor)"
              />
            </button>
          </div>
        </ChatSettingModal>
      </div>

      <div
        class="flex items-center shrink-0"
        :class="{ 'space-x-1': !hideIcons, 'space-x-3': hideIcons }"
      >
        <button
          v-tooltip.top="'Rozpocznij połączenie głosowe'"
          @click.stop="otworzOknoRozmowy('audio')"
          class="transition-opacity flex items-center justify-center rounded-full p-1"
          :class="isUnread ? 'text-white hover:bg-white/20 opacity-90 hover:opacity-100' : 'opacity-50 hover:opacity-100 hover:bg-black/5'"
        >
          <PhoneIcon
            :size="hideIcons ? 24 : 18"
            :fillColor="isUnread ? '#ffffff' : (activeTheme.headerTextColor || activeTheme.iconColor)"
          />
        </button>

        <button
          v-tooltip.top="'Rozpocznij połączenie wideo'"
          @click.stop="otworzOknoRozmowy('video')"
          class="transition-opacity flex items-center justify-center rounded-full p-1"
          :class="isUnread ? 'text-white hover:bg-white/20 opacity-90 hover:opacity-100' : 'opacity-50 hover:opacity-100 hover:bg-black/5'"
        >
          <VideoOutlineIcon
            :size="hideIcons ? 24 : 18"
            :fillColor="isUnread ? '#ffffff' : (activeTheme.headerTextColor || activeTheme.iconColor)"
          />
        </button>

        <button
          v-tooltip.top="'Informacje o czacie'"
          v-if="hideIcons"
          @click.stop="emit('show-info')"
          class="hover:opacity-80 transition-opacity flex items-center justify-center rounded-full p-1"
          :class="isUnread ? 'hover:bg-white/20' : 'hover:bg-black/5'"
        >
          <Information
            :size="hideIcons ? 24 : 20"
            :fillColor="isUnread ? '#ffffff' : (activeTheme.primaryColor || activeTheme.iconColor)"
          />
        </button>

        <button
          v-tooltip.top="'Minimalizuj'"
          v-if="!hideIcons"
          @click.stop="minimize(boxId)"
          class="hidden md:flex items-center justify-center transition-all rounded-full p-1"
          :class="isUnread ? 'text-white hover:bg-white/20' : 'hover:opacity-80 hover:bg-black/5'"
        >
          <MinusIcon
            :size="22"
            :fillColor="isUnread ? '#ffffff' : (activeTheme.primaryColor || activeTheme.iconColor)"
          />
        </button>

        <button
          v-tooltip.top="'Zamknij czat'"
          v-if="!hideIcons"
          @click.stop="close(boxId)"
          class="hidden md:flex items-center justify-center transition-all rounded-full p-1"
          :class="isUnread ? 'text-white hover:bg-white/20' : 'hover:opacity-80 hover:bg-black/5'"
        >
          <CloseIcon
            :size="22"
            :fillColor="isUnread ? '#ffffff' : (activeTheme.primaryColor || activeTheme.iconColor)"
          />
        </button>
      </div>
    </header>

    <!-- PASEK PRZYPIĘTEJ WIADOMOŚCI (Renderowany gdy isHasPinned = true, z przezroczystym tłem) -->
    <div
      v-if="isHasPinned"
      @click="showPinnedModal = true"
      class="flex items-center px-3 py-2 bg-transparent hover:bg-black/5 dark:hover:bg-white/5 transition-colors cursor-pointer border-t border-black/5 dark:border-white/5"
    >
      <div class="shrink-0 mr-3 flex items-center justify-center">
        <PinIcon :size="20" class="text-gray-600 dark:text-gray-300 transform rotate-45" />
      </div>

      <div class="flex flex-col min-w-0 flex-1 leading-tight">
        <span class="text-[12px] text-gray-500 dark:text-gray-400 truncate">
          {{ pinnedMessage?.sender || 'Rozmówca' }}
        </span>
        <span class="text-[14px] font-semibold text-gray-900 dark:text-gray-100 truncate mt-0.5">
          {{ pinnedMessage?.content || '' }}
        </span>
      </div>

      <div class="shrink-0 ml-3 flex items-center justify-center text-gray-500">
        <ChevronDownIcon :size="20" />
      </div>
    </div>
  </div>
  <BaseModal :title="$t('chat.przypieteWiadomosci')" v-if="showPinnedModal" @close="showPinnedModal = false">
    <PinnedModal :boxId="boxId" @close="showPinnedModal = false" />
  </BaseModal>
</template>
