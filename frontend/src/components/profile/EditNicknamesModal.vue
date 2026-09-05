<template>
  <BaseModal title="Nicki" @close="emit('close')">
    <div class="p-2 w-[550px] flex flex-col space-y-1">
      <div
        v-for="user in participants"
        :key="user.id"
        class="flex items-center justify-between p-2.5  rounded-xl transition group"
      >
        <!-- AVATAR + TEKST / INPUT -->
        <div class="flex items-center space-x-3.5 min-w-0 flex-1 mr-3">
          <img
            :src="user.avatarUrl || 'https://i.pravatar.cc/150?img=' + user.id"
            class="w-12 h-12 rounded-full object-cover flex-shrink-0"
          />

          <!-- TRYB EDYCJI: Pokazujemy rounded input -->
          <div v-if="editingUserId === user.id" class="flex-1">
            <input
              v-model="editingValue"
              type="text"
              ref="inputRef"
              @keyup.enter="saveNickname(user)"
              @keyup.esc="cancelEditing"
              class="w-full bg-[#f0f2f5] dark:bg-theme-bg text-theme-text font-medium px-4 py-2.5 rounded-full text-[15px] outline-none border border-transparent focus:border-theme-border transition"
              placeholder="Wpisz nick..."
            />
          </div>

          <!-- TRYB PODGLĄDU: Pokazujemy Nick + Imię -->
          <div v-else class="flex flex-col min-w-0 cursor-pointer" @click="startEditing(user)">
            <span class="text-[16px] font-bold text-theme-text truncate leading-snug">
              {{ user.nickname || user.name }}
            </span>
            <span class="text-[13px] text-theme-text-muted truncate">
              {{ user.name }}
            </span>
          </div>
        </div>

        <!-- PRZYCISKI AKCJI (Ołówek vs Fajeczka) -->
        <div class="flex-shrink-0">
          <!-- W trakcie edycji: Przycisk ZAPISZ (Checkmark) -->
          <button
            v-if="editingUserId === user.id"
            @click="saveNickname(user)"
            class="p-2 text-theme-text hover:bg-theme-bg rounded-full transition flex items-center justify-center active:scale-95"
            title="Zapisz"
          >
            <CheckIcon :size="24" class="text-black dark:text-white" />
          </button>

          <!-- W trybie zwykłym: Przycisk EDYTUJ (Pencil) -->
          <button
            v-else
            @click="startEditing(user)"
            class="p-2 text-theme-text hover:bg-theme-bg rounded-full transition flex items-center justify-center"
            title="Edytuj nick"
          >
            <PencilIcon :size="20" />
          </button>
        </div>

      </div>
    </div>
  </BaseModal>
</template>

<script setup lang="ts">
import { ref, computed, nextTick } from 'vue'
import BaseModal from '@/components/common/BaseModal.vue'
import PencilIcon from 'vue-material-design-icons/Pencil.vue'
import CheckIcon from 'vue-material-design-icons/Check.vue'
import { ChatType, type ChatGroupMember as GroupMember } from '@/types/Chat'

interface Participant {
  id: string | number
  name: string
  nickname: string
  avatarUrl?: string
}

import { useAuthStore } from '@/stores/auth'

const props = defineProps<{
  chatType: ChatType
  chatName: string
  avatarUrl?: string
  chatId?: string | number
  members?: GroupMember[]
  currentPrivateNickname?: string
}>()

const emit = defineEmits<{
  (e: 'update-nicknames', data: any): void
  (e: 'close'): void
}>()

const authStore = useAuthStore()

// Stan edycji wiersza
const editingUserId = ref<string | number | null>(null)
const editingValue = ref('')
const inputRef = ref<HTMLInputElement[] | null>(null)

// Lista uczestników
const participants = ref<Participant[]>([])

// Inicjalizacja danych
if (props.chatType === ChatType.Group) {
  participants.value = (props.members || []).map(m => ({
    id: m.id,
    name: m.name,
    nickname: m.nickname || m.name,
    avatarUrl: m.avatarUrl
  }))
} else {
  // Dynamic private chat participants
  const otherUserId = props.chatId || 1
  const currentUserId = authStore.currentUser?.id || authStore.currentUserId || 2
  const currentUserName = authStore.currentUser?.name || 'Ja'
  const currentUserAvatar = authStore.currentUser?.avatar || '/default-avatar.png'

  participants.value = [
    {
      id: otherUserId,
      name: props.chatName || 'Rozmówca',
      nickname: props.currentPrivateNickname || props.chatName || 'Rozmówca',
      avatarUrl: props.avatarUrl || '/default-avatar.png'
    },
    {
      id: currentUserId,
      name: currentUserName,
      nickname: currentUserName,
      avatarUrl: currentUserAvatar
    }
  ]
}

// Rozpoczęcie edycji Wiersza
const startEditing = (user: Participant) => {
  editingUserId.value = user.id
  editingValue.value = user.nickname || user.name

  // Auto focus na pole input
  nextTick(() => {
    if (inputRef.value && inputRef.value.length > 0) {
      inputRef.value[0].focus()
      inputRef.value[0].select()
    }
  })
}

// Anulowanie edycji
const cancelEditing = () => {
  editingUserId.value = null
  editingValue.value = ''
}

// Zapisanie zmian
const saveNickname = (user: Participant) => {
  const newNickname = editingValue.value.trim()
  user.nickname = newNickname || user.name

  // Emitujemy dane do rodzica
  emit('update-nicknames', {
    userId: user.id,
    nickname: user.nickname
  })

  editingUserId.value = null
}
</script>
