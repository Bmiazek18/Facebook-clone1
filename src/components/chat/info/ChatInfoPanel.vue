<template>
  <div
    v-if="showInfoPanel"
    class="w-full overflow-hidden flex flex-col bg-theme-bg-secondary h-full rounded-xl shadow-sm"
  >
    <!-- Mobilny nagłówek -->
    <div class="lg:hidden flex items-center p-3 border-b border-theme-border bg-theme-bg">
      <button @click="emit('back')" class="hover:bg-theme-bg-hover rounded-full p-2 mr-2">
        <ArrowLeftIcon :size="24" class="text-theme-text" />
      </button>
      <h1 class="text-lg font-semibold text-theme-text">Informacje o czacie</h1>
    </div>

    <div ref="wrapperRef" class="transition-wrapper bg-theme-bg-secondary h-full overflow-hidden relative">
      <transition :name="transitionName" mode="out-in" @after-enter="updateHeight">
        <div :key="panelView" class="h-full w-full">
          <!-- WIDOK GŁÓWNY -->
          <div v-if="panelView === 'home'" data-view class="h-full flex flex-col overflow-y-auto custom-scrollbar">

            <!-- GÓRNY PROFIL -->
            <ChatProfileHeader
              :avatar-url="chatMeta.avatarUrl"
              :name="chatMeta.otherUserNickname || chatMeta.name"
              :time-ago="chatMeta.timeAgo"
              @open-mute="openMuteModal"
              @open-search="panelView = 'search'"
            />

            <!-- AKORDEONY -->
            <AccordionSection title="Informacje o czacie" default-open>
              <div class="px-4 py-2 flex items-center hover:bg-theme-bg-hover cursor-pointer transition rounded-md mx-2 mb-2">
                <PinIcon :size="20" class="text-theme-text mr-3" />
                <span class="text-[14px] font-medium text-theme-text">Wyświetl przypięte wiadomości</span>
              </div>
            </AccordionSection>

            <AccordionSection title="Dostosuj czat">
              <div class="flex flex-col space-y-0.5 pb-2 mb-2">
                <!-- Widoczne tylko w czatach grupowych -->
                <template v-if="chatMeta.type === ChatType.Group">
                  <div @click="openRenameModal" class="px-4 py-2.5 flex items-center hover:bg-theme-bg-hover cursor-pointer transition rounded-md mx-2">
                    <PencilIcon :size="20" class="text-theme-text mr-3" />
                    <span class="text-[14px] font-medium text-theme-text">Zmień nazwę czatu</span>
                  </div>
                  <div class="px-4 py-2.5 flex items-center hover:bg-theme-bg-hover cursor-pointer transition rounded-md mx-2">
                    <ImageIcon :size="20" class="text-theme-text mr-3" />
                    <span class="text-[14px] font-medium text-theme-text">Zmień zdjęcie</span>
                  </div>
                </template>

                <!-- 3 Opcje widoczne w czacie indywidualnym -->
                <div @click="openThemeModal" class="px-4 py-2.5 flex items-center hover:bg-theme-bg-hover cursor-pointer transition rounded-md mx-2">
                  <div class="w-5 h-5 mr-3 rounded-full bg-gradient-to-br from-red-400 to-pink-600 relative flex items-center justify-center">
                    <div class="w-2 h-2 bg-black/20 rounded-full"></div>
                  </div>
                  <span class="text-[14px] font-medium text-theme-text">Zmień motyw</span>
                </div>
                <div @click="openEmojiModal" class="px-4 py-2.5 flex items-center hover:bg-theme-bg-hover cursor-pointer transition rounded-md mx-2">
                  <PawIcon :size="20" class="text-[#5F4B3C] mr-3" />
                  <span class="text-[14px] font-medium text-theme-text">Zmień ikonę emoji</span>
                </div>
                <div @click="openEditNicknamesModal" class="px-4 py-2.5 flex items-center hover:bg-theme-bg-hover cursor-pointer transition rounded-md mx-2">
                  <FormatLetterCaseIcon :size="20" class="text-theme-text mr-3" />
                  <span class="text-[14px] font-medium text-theme-text">Edytuj nicki</span>
                </div>
              </div>
            </AccordionSection>

            <AccordionSection title="Multimedia, pliki i linki">
              <div class="flex flex-col space-y-0.5 pb-2 mb-2">
                <div @click="panelView = 'media'" class="px-4 py-2.5 flex items-center hover:bg-theme-bg-hover cursor-pointer transition rounded-md mx-2">
                  <ImageMultipleIcon :size="20" class="text-theme-text mr-3" />
                  <span class="text-[14px] font-medium text-theme-text">Multimedia</span>
                </div>
                <div class="px-4 py-2.5 flex items-center hover:bg-theme-bg-hover cursor-pointer transition rounded-md mx-2">
                  <FileDocumentIcon :size="20" class="text-theme-text mr-3" />
                  <span class="text-[14px] font-medium text-theme-text">Pliki</span>
                </div>
                <div class="px-4 py-2.5 flex items-center hover:bg-theme-bg-hover cursor-pointer transition rounded-md mx-2">
                  <LinkVariantIcon :size="20" class="text-theme-text mr-3" />
                  <span class="text-[14px] font-medium text-theme-text">Linki</span>
                </div>
              </div>
            </AccordionSection>

            <AccordionSection title="Prywatność i pomoc">
              <div class="flex flex-col space-y-0.5 pb-2 mb-2">
                <div @click="openMuteModal" class="px-4 py-2.5 flex items-center hover:bg-theme-bg-hover cursor-pointer transition rounded-md mx-2">
                  <BellIcon :size="20" class="text-theme-text mr-3" />
                  <span class="text-[14px] font-medium text-theme-text">Wstrzymaj powiadomienia</span>
                </div>
                <div class="px-4 py-2.5 flex items-center hover:bg-theme-bg-hover cursor-pointer transition rounded-md mx-2">
                  <ShieldOutlineIcon :size="20" class="text-theme-text mr-3" />
                  <span class="text-[14px] font-medium text-theme-text">Uprawnienia do obsługi wiadomości</span>
                </div>
                <div class="px-4 py-2.5 flex items-center hover:bg-theme-bg-hover cursor-pointer transition rounded-md mx-2">
                  <HistoryIcon :size="20" class="text-theme-text mr-3" />
                  <span class="text-[14px] font-medium text-theme-text">Znikające wiadomości</span>
                </div>
                <div class="px-4 py-2 flex items-center hover:bg-theme-bg-hover cursor-pointer transition rounded-md mx-2">
                  <EyeIcon :size="20" class="text-theme-text mr-3" />
                  <div class="flex flex-col">
                    <span class="text-[14px] font-medium text-theme-text">Potwierdzenia odczytu</span>
                    <span class="text-[12px] text-theme-text-muted">Wł.</span>
                  </div>
                </div>
                <div class="px-4 py-2.5 flex items-center hover:bg-theme-bg-hover cursor-pointer transition rounded-md mx-2">
                  <LockIcon :size="20" class="text-theme-text mr-3" />
                  <span class="text-[14px] font-medium text-theme-text">Zweryfikuj pełne szyfrowanie</span>
                </div>
                <div class="px-4 py-2.5 flex items-center hover:bg-theme-bg-hover cursor-pointer transition rounded-md mx-2">
                  <CancelIcon :size="20" class="text-theme-text mr-3" />
                  <span class="text-[14px] font-medium text-theme-text">Ogranicz</span>
                </div>
                <div class="px-4 py-2.5 flex items-center hover:bg-theme-bg-hover cursor-pointer transition rounded-md mx-2">
                  <MinusCircleIcon :size="20" class="text-theme-text mr-3" />
                  <span class="text-[14px] font-medium text-theme-text">Zablokuj</span>
                </div>
                <div class="px-4 py-2 flex items-center hover:bg-theme-bg-hover cursor-pointer transition rounded-md mx-2">
                  <AlertOctagonIcon :size="20" class="text-theme-text mr-3" />
                  <div class="flex flex-col">
                    <span class="text-[14px] font-medium text-theme-text">Zgłoś</span>
                    <span class="text-[12px] text-theme-text-muted">Przekaż opinię i zgłoś konwersację</span>
                  </div>
                </div>
              </div>
            </AccordionSection>

            <AccordionSection v-if="chatMeta.type === ChatType.Group" title="Uczestniki czatu">
              <div class="flex flex-col space-y-1 mt-1 mb-2">
                <div
                  v-for="(member, i) in chatMeta.groupMembers"
                  :key="i"
                  class="px-3 py-2 flex items-center hover:bg-theme-bg-hover cursor-pointer rounded-md mx-2 group"
                >
                  <img :src="'https://i.pravatar.cc/150?img=' + member.id" class="w-9 h-9 rounded-full mr-3 object-cover" />
                  <div class="flex-1 min-w-0">
                    <h4 class="text-[14px] font-medium text-theme-text">
                      {{ member.nickname || member.name }}
                      <span v-if="member.nickname" class="text-[12px] text-theme-text-muted truncate">({{ member.name }})</span>
                    </h4>
                    <p v-if="member.addedByUserId" class="text-[12px] text-theme-text-muted truncate">
                      Dodany przez: {{ getUserById(member.addedByUserId)?.name || 'Nieznany Użytkownik' }}
                    </p>
                  </div>
                  <div class="w-8 h-8 flex items-center justify-center rounded-full hover:bg-theme-bg-hover transition">
                    <DotsHorizontalIcon :size="20" class="text-theme-text-muted" />
                  </div>
                </div>
              </div>
            </AccordionSection>

          </div>

          <ChatMediaPanel v-else-if="panelView === 'media'" data-view @close="panelView = 'home'" />
          <MessageSearch
            v-else-if="panelView === 'search'"
            data-view
            :boxId="chatId"
            @close="panelView = 'home'"
            @go-to-message="payload => emit('goToMessage', payload)"
          />
        </div>
      </transition>
    </div>
  </div>

  <!-- MODALE -->
  <ChatRenameModal
    v-if="activeModal === 'rename'"
    :initial-name="chatMeta.name"
    @save="name => convStore.updateChatName(props.chatId, name)"
    @close="closeModal"
  />

  <ChatMuteModal
    v-if="activeModal === 'mute'"
    @save="duration => convStore.muteChat(props.chatId, duration)"
    @close="closeModal"
  />

  <BaseModal v-if="activeModal === 'theme'" title="Wybierz motyw czatu" @close="closeModal">
    <MessangerTheme @apply="closeThemeModalAndSave" />
  </BaseModal>

<BaseModal v-if="activeModal === 'emoji'" title="Ikona emoji" @close="closeModal">
  <!-- Górny panel: Bieżące emoji + przycisk Usuń -->
  <div class="px-5 pt-4 pb-3">
    <span class="block text-sm font-semibold text-gray-900 mb-1">
      Bieżące emoji
    </span>

    <div class="flex items-center justify-between">
      <div class="text-3xl">
        {{ convStore.selectedEmoji || '👍' }}
      </div>

      <button
        @click="closeModal"
        class="inline-flex items-center gap-1.5 px-4 py-2 bg-gray-100 hover:bg-gray-200 text-gray-800 text-sm font-medium rounded-xl transition-colors"
      >
        <span>✕</span>
        <span>Usuń</span>
      </button>
    </div>
  </div>

  <!-- Picker emoji -->
  <LazyEmojiPicker @select="onEmojiSelect" class="w-full"/>
</BaseModal>

  <EditNicknamesModal
    v-if="activeModal === 'nicknames'"
    :chat-id="props.chatId"
    :chat-type="chatMeta.type"
    :chat-name="chatMeta.name"
    :avatar-url="chatMeta.avatarUrl"
    :members="chatMeta.type === ChatType.Group ? chatMeta.groupMembers || [] : []"
    :current-private-nickname="chatMeta.type === ChatType.Private ? chatMeta.otherUserNickname || chatMeta.name : undefined"
    @update-nicknames="handleUpdateNicknames"
    @close="closeModal"
  />
</template>

<script setup lang="ts">
import '@/assets/animations/slideTransition.css'
import { ref, watch, computed, nextTick } from 'vue'
import { useSlideTransition } from '@/composables/ui/useSlideTransition'

// Ikonki
import ArrowLeftIcon from 'vue-material-design-icons/ArrowLeft.vue'
import PinIcon from 'vue-material-design-icons/Pin.vue'
import PencilIcon from 'vue-material-design-icons/Pencil.vue'
import PawIcon from 'vue-material-design-icons/Paw.vue'
import FormatLetterCaseIcon from 'vue-material-design-icons/FormatLetterCase.vue'
import ImageIcon from 'vue-material-design-icons/Image.vue'
import DotsHorizontalIcon from 'vue-material-design-icons/DotsHorizontal.vue'
import ImageMultipleIcon from 'vue-material-design-icons/ImageMultiple.vue'
import FileDocumentIcon from 'vue-material-design-icons/FileDocument.vue'
import LinkVariantIcon from 'vue-material-design-icons/LinkVariant.vue'
import EyeIcon from 'vue-material-design-icons/Eye.vue'
import ShieldOutlineIcon from 'vue-material-design-icons/ShieldOutline.vue'
import HistoryIcon from 'vue-material-design-icons/History.vue'
import LockIcon from 'vue-material-design-icons/Lock.vue'
import CancelIcon from 'vue-material-design-icons/Cancel.vue'
import MinusCircleIcon from 'vue-material-design-icons/MinusCircle.vue'
import AlertOctagonIcon from 'vue-material-design-icons/AlertOctagon.vue'
import BellIcon from 'vue-material-design-icons/Bell.vue'

// Komponenty pomocnicze
import AccordionSection from './AccordionSection.vue'
import ChatProfileHeader from '@/components/chat/info/ChatProfileHeader.vue'
import ChatMuteModal from './modals/ChatMuteModal.vue'
import ChatRenameModal from './modals/ChatRenameModal.vue'
import ChatMediaPanel from '@/components/chat/info/ChatMediaPanel.vue'
import MessageSearch from '@/components/chat/info/MessageSearch.vue'
import BaseModal from '@/components/common/BaseModal.vue'
import MessangerTheme from '@/components/chat/shared/MessangerTheme.vue'
import LazyEmojiPicker from '@/components/common/LazyEmojiPicker.vue'
import EditNicknamesModal from '@/components/profile/EditNicknamesModal.vue'

// Store & Data
import { useConversationsStore } from '@/stores/conversations'
import { useAuthStore } from '@/stores/auth'
import { ChatType, type GroupMember } from '@/types/Chat'
import { getUserById } from '@/utils/users'

const props = defineProps<{ chatId: string | number }>()
const emit = defineEmits<{
  (e: 'goToMessage', payload: { id: number; chatId?: string | number }): void
  (e: 'back'): void
}>()

const convStore = useConversationsStore()
const authStore = useAuthStore()
const showInfoPanel = ref(true)
const panelView = ref<'home' | 'media' | 'search'>('home')

const chatMeta = computed(() => {
  const meta = convStore.chats.find((c) => String(c.id) === String(props.chatId))
  return meta || {
    id: props.chatId,
    name: `Czat ${props.chatId}`,
    avatarUrl: '',
    timeAgo: '',
    type: ChatType.Private,
    groupMembers: [],
    otherUserNickname: '',
  }
})

// Modale
type ModalType = 'rename' | 'theme' | 'emoji' | 'nicknames' | 'mute' | null
const activeModal = ref<ModalType>(null)

const closeModal = () => { activeModal.value = null }
const openThemeModal = () => { activeModal.value = 'theme' }
const openEmojiModal = () => { activeModal.value = 'emoji' }
const openEditNicknamesModal = () => { activeModal.value = 'nicknames' }
const openMuteModal = () => { activeModal.value = 'mute' }
const openRenameModal = () => { activeModal.value = 'rename' }

const closeThemeModalAndSave = () => {
  try {
    const themeId = convStore.selectedThemeId as string
    convStore.setChatThemeById(props.chatId, themeId)
    const theme = convStore.themes.find((t) => t.id === themeId)
    if (theme) {
      convStore.messages.push({
        chatId: props.chatId,
        type: 'action',
        time: new Date(),
        subType: 'CHANGE_THEME',
        payload: theme.title,
      })
    }
  } catch {}
  closeModal()
}

const onEmojiSelect = (e: { native: string }) => {
  try {
    convStore.setChatEmoji(props.chatId, e.native)
    convStore.messages.push({
      chatId: props.chatId,
      type: 'action',
      subType: 'CHANGE_E',
      payload: e.native,
    })
  } catch {}
  closeModal()
}

function handleUpdateNicknames(updatedData: any) {
  const conversationId = convStore.getSymmetricConversationId(props.chatId)

  if (chatMeta.value.type === ChatType.Group) {
    if (Array.isArray(updatedData)) {
      convStore.updateGroupMembersNicknames(props.chatId, updatedData)
    } else if (updatedData && typeof updatedData === 'object') {
      const membersCopy = [...(chatMeta.value.groupMembers || [])]
      const member = membersCopy.find(m => String(m.id) === String(updatedData.userId))
      if (member) {
        member.nickname = updatedData.nickname
        convStore.updateGroupMembersNicknames(props.chatId, membersCopy)
      }
    }
  } else {
    // Private chat
    if (updatedData && typeof updatedData === 'object') {
      if (String(updatedData.userId) === String(props.chatId)) {
        // Other user
        convStore.updatePrivateChatNickname(props.chatId, updatedData.nickname)
      } else {
        // Logged-in user self nickname
        const currentUserId = authStore.currentUser?.id || authStore.currentUserId
        $fetch('http://localhost:8080/api/chat/settings/nickname', {
          method: 'POST',
          query: {
            conversationId: conversationId,
            userId: String(currentUserId),
            nickname: updatedData.nickname
          }
        }).catch(err => console.error('Failed to save self nickname to ScyllaDB:', err))

        convStore.messages.push({
          chatId: props.chatId,
          type: 'action',
          subType: 'CHANGE_NICKNAME',
          payload: updatedData.nickname,
          time: new Date()
        })
      }
    } else if (typeof updatedData === 'string') {
      convStore.updatePrivateChatNickname(props.chatId, updatedData)
    }
  }
  closeModal()
}

// Slidery / Animacje
const { wrapperRef, updateHeight } = useSlideTransition()
const previousPanelView = ref(panelView.value)

const transitionName = computed(() => {
  const order = { home: 0, media: 1, search: 2 }
  return (order[panelView.value] ?? 0) >= (order[previousPanelView.value] ?? 0) ? 'slide-left' : 'slide-right'
})

watch(panelView, (newVal, oldVal) => {
  previousPanelView.value = oldVal
  nextTick(() => updateHeight())
})

defineExpose({ openEditNicknamesModal, openThemeModal, openEmojiModal, openMuteModal })
</script>

<style scoped>
.custom-scrollbar::-webkit-scrollbar { width: 6px; }
.custom-scrollbar::-webkit-scrollbar-track { background: transparent; }
.custom-scrollbar::-webkit-scrollbar-thumb {
  background-color: rgba(0, 0, 0, 0.2);
  border-radius: 20px;
}
.custom-scrollbar:hover::-webkit-scrollbar-thumb { background-color: rgba(0, 0, 0, 0.3); }
</style>
