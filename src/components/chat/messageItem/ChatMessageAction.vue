<script setup lang="ts">
import type { Message } from '@/types/Message'

const props = defineProps<{
  message: Message
}>()

const emit = defineEmits<{
  (e: 'open-modal', type: 'CHANGE_E' | 'CHANGE_NICKNAME' | 'CHANGE_THEME'): void
}>()
</script>

<template>
  <div class="flex w-full items-center justify-center text-sm text-gray-500">
    <span v-if="message.subType === 'CHANGE_E'">
      Emotka została zmieniona na {{ message.payload }}</span
    >
    <span v-else-if="message.subType === 'CHANGE_NICKNAME'">
      Nick został zmieniony na {{ message.payload }}</span
    >
    <span v-else-if="message.subType === 'CHANGE_THEME'"
      >Zmieniłeś motyw na {{ message.payload }}.</span
    >
    <button
      @click="emit('open-modal', message.subType)"
      class="ml-1 font-bold text-gray-900 hover:underline"
    >
      Zmień
    </button>
  </div>
</template>
