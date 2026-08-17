<script setup lang="ts">
import { ref, computed } from 'vue'
import Plus from 'vue-material-design-icons/Plus.vue'
import Close from 'vue-material-design-icons/Close.vue'
import Poll from 'vue-material-design-icons/Poll.vue'

interface Props {
  show: boolean
  themeColor?: string
}

const props = withDefaults(defineProps<Props>(), {
  show: false,
  themeColor: '#1877F2',
})

const emit = defineEmits<{
  (e: 'close'): void
  (
    e: 'submit',
    pollData: {
      question: string
      options: { id: string; text: string; votes: number; votedByMe: boolean; voterIds: string[] }[]
      allowMultiple: boolean
      allowAddOption: boolean
    }
  ): void
}>()

const question = ref('')
const options = ref<string[]>(['', ''])
const allowMultiple = ref(true)
const allowAddOption = ref(true)

function addOption() {
  options.value.push('')
}

function removeOption(index: number) {
  if (options.value.length > 2) {
    options.value.splice(index, 1)
  }
}

const isValid = computed(() => {
  if (!question.value.trim()) return false
  const validOpts = options.value.filter((o) => o.trim().length > 0)
  return validOpts.length >= 2
})

function handleSubmit() {
  if (!isValid.value) return
  const validOpts = options.value
    .map((o) => o.trim())
    .filter((o) => o.length > 0)
    .map((text, idx) => ({
      id: `opt_${Date.now()}_${idx}`,
      text,
      votes: 0,
      votedByMe: false,
      voterIds: [],
    }))

  emit('submit', {
    question: question.value.trim(),
    options: validOpts,
    allowMultiple: allowMultiple.value,
    allowAddOption: allowAddOption.value,
  })

  // Reset
  question.value = ''
  options.value = ['', '']
  allowMultiple.value = true
  allowAddOption.value = true
  emit('close')
}

function handleClose() {
  emit('close')
}
</script>

<template>
  <div
    v-if="show"
    class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-sm antialiased"
    @click.self="handleClose"
  >
    <div
      class="bg-white dark:bg-[#242526] rounded-2xl shadow-2xl w-full max-w-md overflow-hidden border border-gray-200 dark:border-gray-700 flex flex-col max-h-[90vh]"
    >
      <!-- Header -->
      <div class="px-5 py-4 border-b border-gray-200 dark:border-gray-700 flex items-center justify-between">
        <div class="flex items-center gap-2">
          <div
            class="w-9 h-9 rounded-full flex items-center justify-center text-white"
            :style="{ backgroundColor: themeColor }"
          >
            <Poll :size="20" />
          </div>
          <h3 class="text-lg font-bold text-gray-900 dark:text-white">Utwórz ankietę</h3>
        </div>
        <button
          @click="handleClose"
          class="p-2 hover:bg-gray-100 dark:hover:bg-gray-700 rounded-full text-gray-500 transition-colors"
        >
          <Close :size="20" />
        </button>
      </div>

      <!-- Body -->
      <div class="p-5 overflow-y-auto space-y-4 flex-1">
        <!-- Pytanie -->
        <div>
          <label class="block text-xs font-bold text-gray-500 dark:text-gray-400 uppercase mb-1">
            Pytanie ankiety
          </label>
          <input
            v-model="question"
            type="text"
            placeholder="O co chcesz zapytać grupę?"
            class="w-full px-3.5 py-2.5 rounded-xl border border-gray-300 dark:border-gray-600 bg-gray-50 dark:bg-gray-800 text-gray-900 dark:text-white text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 transition-all font-medium"
            autofocus
          />
        </div>

        <!-- Opcje -->
        <div>
          <label class="block text-xs font-bold text-gray-500 dark:text-gray-400 uppercase mb-1.5">
            Opcje odpowiedzi (min. 2)
          </label>
          <div class="space-y-2">
            <div
              v-for="(option, idx) in options"
              :key="idx"
              class="flex items-center gap-2"
            >
              <input
                v-model="options[idx]"
                type="text"
                :placeholder="`Opcja ${idx + 1}`"
                class="flex-1 px-3.5 py-2 rounded-xl border border-gray-300 dark:border-gray-600 bg-gray-50 dark:bg-gray-800 text-gray-900 dark:text-white text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 transition-all"
                @keyup.enter="idx === options.length - 1 ? addOption() : null"
              />
              <button
                v-if="options.length > 2"
                @click="removeOption(idx)"
                class="p-2 text-gray-400 hover:text-red-500 rounded-lg hover:bg-gray-100 dark:hover:bg-gray-700 transition-colors"
                title="Usuń opcję"
              >
                <Close :size="18" />
              </button>
            </div>
          </div>

          <button
            @click="addOption"
            type="button"
            class="mt-2.5 flex items-center gap-1.5 text-sm font-semibold hover:opacity-80 transition-opacity"
            :style="{ color: themeColor }"
          >
            <Plus :size="18" />
            <span>Dodaj kolejną opcję</span>
          </button>
        </div>

        <!-- Ustawienia -->
        <div class="pt-3 border-t border-gray-200 dark:border-gray-700 space-y-3">
          <label class="flex items-center justify-between cursor-pointer select-none">
            <span class="text-sm font-medium text-gray-800 dark:text-gray-200">
              Zezwalaj na wybór wielu odpowiedzi
            </span>
            <input
              v-model="allowMultiple"
              type="checkbox"
              class="w-4 h-4 rounded border-gray-300 text-blue-600 focus:ring-blue-500"
            />
          </label>

          <label class="flex items-center justify-between cursor-pointer select-none">
            <span class="text-sm font-medium text-gray-800 dark:text-gray-200">
              Zezwalaj innym na dodawanie opcji
            </span>
            <input
              v-model="allowAddOption"
              type="checkbox"
              class="w-4 h-4 rounded border-gray-300 text-blue-600 focus:ring-blue-500"
            />
          </label>
        </div>
      </div>

      <!-- Footer -->
      <div class="px-5 py-3.5 bg-gray-50 dark:bg-gray-800 border-t border-gray-200 dark:border-gray-700 flex justify-end gap-2">
        <button
          @click="handleClose"
          type="button"
          class="px-4 py-2 text-sm font-semibold text-gray-700 dark:text-gray-300 hover:bg-gray-200 dark:hover:bg-gray-700 rounded-xl transition-colors"
        >
          Anuluj
        </button>
        <button
          @click="handleSubmit"
          :disabled="!isValid"
          type="button"
          class="px-5 py-2 text-sm font-semibold text-white rounded-xl transition-all shadow-sm disabled:opacity-40 disabled:cursor-not-allowed"
          :style="{ backgroundColor: themeColor }"
        >
          Utwórz ankietę
        </button>
      </div>
    </div>
  </div>
</template>
