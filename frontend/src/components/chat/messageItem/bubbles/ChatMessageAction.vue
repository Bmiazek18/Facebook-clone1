<script setup lang="ts">
import type { Message } from '@/types/Message'
import { useChatThemeStore } from '@/stores/chatTheme'
import { storeToRefs } from 'pinia'

const props = defineProps<{
  message: Message
}>()

const emit = defineEmits<{
  (e: 'open-modal', type: 'CHANGE_E' | 'CHANGE_NICKNAME' | 'CHANGE_THEME'): void
}>()

const themeStore = useChatThemeStore()
const { themes } = storeToRefs(themeStore)

const getThemeTitle = (themeId: string | undefined) => {
  if (!themeId) return ''
  const theme = themes.value.find((t) => t.id === themeId)
  return theme ? theme.title : themeId
}
</script>

<template>
  <div class="flex w-full items-center justify-center py-2 text-[12px] text-black">
    <div class="text-center leading-relaxed">
      <!-- Szybka reakcja -->
      <span v-if="message.subType === 'CHANGE_E'">
        Ustawiłeś szybką reakcję jako {{ message.payload }}.
      </span>

      <!-- Zmiana pseudonimu -->
      <span v-else-if="message.subType === 'CHANGE_NICKNAME'">
        Twój pseudonim został zdefiniowany jako {{ message.payload }}.
      </span>

      <!-- Zmiana motywu -->
      <span v-else-if="message.subType === 'CHANGE_THEME'">
        Zmieniłeś motyw na {{ getThemeTitle(message.payload) }}.
      </span>

      <!-- Przycisk Zmień (z akcentem kolorystycznym ze zrzutu) -->
      <button
        @click="emit('open-modal', message.subType)"
        class="ml-0.5 font-medium text-[#c22828] hover:underline transition-colors cursor-pointer"
      >
        Zmień
      </button>
    </div>
  </div>
</template>
