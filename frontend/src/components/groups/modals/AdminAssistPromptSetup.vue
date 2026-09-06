<script setup lang="ts">
import { ref } from 'vue'
import RobotOutlineIcon from 'vue-material-design-icons/RobotOutline.vue'
import SparklesIcon from 'vue-material-design-icons/Star.vue'

const promptText = ref('')
const selectedAction = ref('reject')

const emit = defineEmits<{
  (e: 'save', payload: { prompt: string; action: string }): void
  (e: 'close'): void
}>()

const handleSave = () => {
  emit('save', {
    prompt: promptText.value,
    action: selectedAction.value
  })
  emit('close')
}
</script>

<template>
  <div class="flex flex-col w-[550px] max-w-full mx-auto bg-white dark:bg-[#242526] font-sans text-[#050505] dark:text-[#e4e6eb] p-6 rounded-xl">
    <div class="flex items-center gap-3 mb-4">
      <div class="w-10 h-10 rounded-full bg-blue-500/10 flex items-center justify-center text-[#1877f2] dark:text-[#4599ff] shrink-0">
        <RobotOutlineIcon :size="24" />
      </div>
      <div>
        <h2 class="text-xl font-bold">{{ $t('groups.asystentAiKonfiguracjaPodpowiedzi') }}</h2>
        <p class="text-xs text-[#65676b] dark:text-[#b0b3b8]">{{ $t('groups.automatyzacjaModeracjiZWykorzystaniem') }}</p>
      </div>
    </div>

    <div class="space-y-4">
      <!-- Akcja asystenta -->
      <div>
        <label class="block text-sm font-semibold mb-2">{{ $t('groups.dzialanieAsystenta') }}</label>
        <select v-model="selectedAction" class="w-full bg-[#f0f2f5] dark:bg-[#3a3b3c] border border-gray-300 dark:border-[#525355] rounded-xl px-4 py-3 text-[15px] focus:outline-none focus:ring-1 focus:ring-[#1877f2] dark:text-[#e4e6eb]">
          <option value="reject">{{ $t('groups.odrzucNadchodzacyPost') }}</option>
          <option value="approve">{{ $t('groups.automatycznieZatwierdzPost') }}</option>
          <option value="flag">{{ $t('groups.przekazDoWeryfikacjiPrzez') }}</option>
        </select>
      </div>

      <!-- Treść podpowiedzi (AI Prompt) -->
      <div>
        <div class="flex justify-between items-center mb-2">
          <label class="block text-sm font-semibold">{{ $t('groups.kryteriaAiPrompt') }}</label>
          <span class="flex items-center gap-1 text-xs text-[#1877f2] dark:text-[#4599ff] font-medium">
            <SparklesIcon :size="14" />{{ $t('groups.wspieranePrzezAi') }}</span>
        </div>
        <textarea
          v-model="promptText"
          rows="5"
          :placeholder="$t('groups.npOdrzucPostyZawierajace')"
          class="w-full border border-gray-300 dark:border-[#525355] rounded-xl px-4 py-3 text-[15px] focus:outline-none focus:ring-1 focus:ring-[#1877f2] dark:bg-[#242526] resize-none leading-relaxed dark:text-[#e4e6eb] placeholder-gray-400"
        ></textarea>
      </div>
    </div>

    <!-- Przyciski akcji -->
    <div class="flex items-center justify-end gap-3 mt-6">
      <button
        @click="emit('close')"
        class="px-4 py-2.5 rounded-lg text-[15px] font-semibold text-[#1877f2] dark:text-[#4599ff] hover:bg-gray-100 dark:hover:bg-[#3a3b3c] transition-colors"
      >{{ $t('common.cancel') }}</button>
      <button
        @click="handleSave"
        :disabled="!promptText.trim()"
        :class="[
          'px-6 py-2.5 rounded-lg text-[15px] font-semibold transition-colors',
          promptText.trim() ? 'bg-[#1877f2] hover:bg-[#166fe5] text-white cursor-pointer' : 'bg-gray-200 dark:bg-[#3a3b3c] text-gray-400 cursor-not-allowed'
        ]"
      >{{ $t('groups.zapiszKryterium') }}</button>
    </div>
  </div>
</template>
