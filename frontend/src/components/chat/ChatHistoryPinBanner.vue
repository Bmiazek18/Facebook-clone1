<template>
  <div v-if="isVisible">
    <!-- Wariant 1: Compact / Inline -->
    <div
      v-if="variant === 'compact'"
      class="w-full bg-[#F0F2F5] dark:bg-[#3A3B3C] rounded-2xl px-3.5 py-2.5 flex items-center justify-between gap-2.5 transition-colors select-none"
    >
      <div class="flex items-center gap-2.5 min-w-0">
        <div class="text-[#65676B] dark:text-[#B0B3B8] shrink-0">
          <svg class="w-5 h-5" viewBox="0 0 24 24" fill="currentColor">
            <path
              d="M12 4V1L8 5l4 4V6c3.31 0 6 2.69 6 6 0 1.01-.25 1.97-.7 2.8l1.46 1.46C19.54 15.03 20 13.57 20 12c0-4.42-3.58-8-8-8zm0 14c-3.31 0-6-2.69-6-6 0-1.01.25-1.97.7-2.8L5.24 7.74C4.46 8.97 4 10.43 4 12c0 4.42 3.58 8 8 8v3l4-4-4-4v3z"
            />
          </svg>
        </div>

        <p class="text-[13px] text-[#050505] dark:text-[#E4E6EB] leading-snug font-normal">
          <span>{{ t('chat.preventHistoryLossInline') || 'Zapobiegnij utracie historii czatów.' }}</span>
          {{ ' ' }}
          <button
            @click="handleCreatePin"
            type="button"
            class="text-[#0064E0] dark:text-[#4599FF] font-semibold hover:underline inline cursor-pointer text-left"
          >
            {{ t('chat.createPin') || 'Utwórz kod PIN' }}
          </button>
        </p>
      </div>

      <button
        @click="handleClose"
        type="button"
        class="text-[#65676B] hover:text-[#050505] dark:text-[#B0B3B8] dark:hover:text-white p-1 rounded-full transition-colors cursor-pointer shrink-0"
        :title="t('common.close') || 'Zamknij'"
      >
        <svg class="w-4 h-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round">
          <line x1="18" y1="6" x2="6" y2="18"></line>
          <line x1="6" y1="6" x2="18" y2="18"></line>
        </svg>
      </button>
    </div>

    <!-- Wariant 2: Card / Pełny (domyślny) -->
    <div
      v-else
      class="w-full max-w-[420px] bg-white dark:bg-[#242526] rounded-2xl shadow-sm border border-[#E4E6EB] dark:border-[#393A3B] p-4 relative select-none"
    >
      <button
        @click="handleClose"
        type="button"
        class="absolute top-3.5 right-3.5 text-[#65676B] hover:text-[#050505] dark:text-[#B0B3B8] dark:hover:text-white p-1 rounded-full transition-colors cursor-pointer"
        :title="t('common.close') || 'Zamknij'"
      >
        <svg class="w-5 h-5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round">
          <line x1="18" y1="6" x2="6" y2="18"></line>
          <line x1="6" y1="6" x2="18" y2="18"></line>
        </svg>
      </button>

      <div class="flex items-start gap-3 pr-6 mb-4">
        <div class="text-[#65676B] dark:text-[#B0B3B8] shrink-0 mt-0.5">
          <svg class="w-6 h-6" viewBox="0 0 24 24" fill="currentColor">
            <path
              d="M12 4V1L8 5l4 4V6c3.31 0 6 2.69 6 6 0 1.01-.25 1.97-.7 2.8l1.46 1.46C19.54 15.03 20 13.57 20 12c0-4.42-3.58-8-8-8zm0 14c-3.31 0-6-2.69-6-6 0-1.01.25-1.97.7-2.8L5.24 7.74C4.46 8.97 4 10.43 4 12c0 4.42 3.58 8 8 8v3l4-4-4-4v3z"
            />
          </svg>
        </div>

        <div>
          <h3 class="text-[17px] font-bold text-[#050505] dark:text-[#E4E6EB] leading-tight mb-1">
            {{ t('chat.preventHistoryLossTitle') || 'Uniknij utraty historii czatu' }}
          </h3>
          <p class="text-[14px] text-[#65676B] dark:text-[#B0B3B8] leading-[1.35]">
            {{
              t('chat.preventHistoryLossDesc') ||
              'Utwórz kod PIN, aby uzyskać dostęp do historii czatu przy użyciu innej przeglądarki lub urządzenia.'
            }}
          </p>
        </div>
      </div>

      <button
        @click="handleCreatePin"
        type="button"
        class="w-full bg-[#0866FF] hover:bg-[#0057e0] active:bg-[#004ec9] text-white font-semibold text-[15px] py-2.5 px-4 rounded-xl transition-colors duration-150 flex items-center justify-center cursor-pointer shadow-none"
      >
        {{ t('chat.createPin') || 'Utwórz kod PIN' }}
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useI18n } from 'vue-i18n'

withDefaults(
  defineProps<{
    variant?: 'card' | 'compact'
  }>(),
  {
    variant: 'card'
  }
)

const emit = defineEmits<{
  (e: 'create-pin'): void
  (e: 'close'): void
}>()

const { t } = useI18n()
const isVisible = ref(true)

const handleCreatePin = () => {
  emit('create-pin')
}

const handleClose = () => {
  isVisible.value = false
  emit('close')
}
</script>
