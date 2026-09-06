<!-- components/chat/modals/ChatMuteModal.vue -->
<template>
  <BaseModal :title="$t('chat.muteNotifications')" @close="emit('close')">
    <div class="px-5 py-3 flex flex-col gap-4">
      <!-- Lista opcji radio -->
      <div class="flex flex-col gap-1 my-1">
        <button
          v-for="option in muteOptions"
          :key="option.value"
          @click="muteDuration = option.value"
          class="w-full flex items-center gap-3 py-2.5 px-3 rounded-lg hover:bg-gray-100 dark:hover:bg-theme-hover transition duration-150 text-left cursor-pointer"
        >
          <!-- Radio Indicator (teraz po lewej) -->
          <div
            :class="[
              'w-5 h-5 rounded-full border-2 flex items-center justify-center transition-all shrink-0',
              muteDuration === option.value
                ? 'border-[#0064e0]'
                : 'border-gray-400 dark:border-gray-500',
            ]"
          >
            <div
              v-if="muteDuration === option.value"
              class="w-2.5 h-2.5 bg-[#0064e0] rounded-full"
            ></div>
          </div>

          <!-- Tekst opcji -->
          <div class="flex flex-col">
            <span class="text-[15px] font-semibold text-theme-text leading-tight">
              {{ option.label }}
            </span>
          </div>
        </button>
      </div>

      <!-- Opis informacyjny -->
      <p class="text-[13px] text-gray-600 dark:text-gray-400 leading-snug px-1">{{ $t('chat.oknaCzatuBedaZamkniete') }}</p>

      <!-- Przyciski akcji -->
      <div class="flex space-x-3 pt-2">
        <button
          @click="emit('close')"
          class="flex-1 py-2 rounded-xl bg-gray-200 hover:bg-gray-300 dark:bg-gray-700 dark:hover:bg-gray-600 text-[15px] font-semibold text-gray-900 dark:text-white transition cursor-pointer"
        >{{ $t('common.cancel') }}</button>
        <button
          @click="handleSave"
          class="flex-1 py-2 rounded-xl bg-[#0064e0] hover:bg-[#0053ba] text-[15px] font-semibold text-white transition cursor-pointer"
        >{{ $t('actions.mute') }}</button>
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
