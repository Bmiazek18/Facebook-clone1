<!-- components/chat/modals/ChatMuteModal.vue -->
<template>
  <BaseModal title="Wycisz powiadomienia" @close="emit('close')">
    <div class="px-4 py-3 flex flex-col space-y-4">
      <div class="space-y-3">
        <label v-for="option in muteOptions" :key="option.value" class="flex items-center space-x-3 cursor-pointer">
          <input
            type="radio"
            :value="option.value"
            v-model="muteDuration"
            class="w-4 h-4 text-blue-600 focus:ring-blue-500"
          />
          <span class="text-sm font-medium text-gray-900">{{ option.label }}</span>
        </label>
      </div>

      <p class="text-xs text-gray-500 leading-relaxed">
        Okna czatu będą zamknięte i nie będziesz otrzymywać powiadomień push na urządzeniach.
      </p>

      <div class="flex space-x-3 pt-2">
        <button
          @click="emit('close')"
          class="flex-1 py-2.5 rounded-lg bg-gray-100 hover:bg-gray-200 text-sm font-semibold text-gray-700 transition"
        >
          Anuluj
        </button>
        <button
          @click="handleSave"
          class="flex-1 py-2.5 rounded-lg bg-blue-600 hover:bg-blue-700 text-sm font-semibold text-white transition"
        >
          Wycisz
        </button>
      </div>
    </div>
  </BaseModal>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import BaseModal from '@/components/common/BaseModal.vue'

const emit = defineEmits<{
  (e: 'close'): void
  (e: 'save', duration: string): void
}>()

const muteDuration = ref('15m')

const muteOptions = [
  { value: '15m', label: 'Na 15 minut' },
  { value: '1h', label: 'Na godzinę' },
  { value: '8h', label: 'Na 8 godz.' },
  { value: '24h', label: 'Na 24 godziny' },
  { value: 'forever', label: 'Do chwili ponownego włączenia' },
]

function handleSave() {
  emit('save', muteDuration.value)
  emit('close')
}
</script>
