<template>
  <div
    class="bg-white dark:bg-theme-bg-secondary rounded-2xl border border-gray-100 dark:border-theme-border w-full max-w-lg mx-auto overflow-hidden"
  >
    <div class="flex items-center justify-between p-4 pb-2">
      <h2 class="text-[15px] font-semibold text-gray-900 dark:text-theme-text">
        {{ $t('post.createPoll') || 'Dodaj ankietę' }}
      </h2>
      <button
        @click="cancel"
        class="p-2 bg-gray-100 dark:bg-theme-bg-tertiary rounded-full text-gray-500 hover:bg-gray-200 dark:hover:bg-theme-bg-hover transition-colors"
      >
        <svg
          xmlns="http://www.w3.org/2000/svg"
          class="h-5 w-5"
          viewBox="0 0 20 20"
          fill="currentColor"
        >
          <path
            fill-rule="evenodd"
            d="M4.293 4.293a1 1 0 011.414 0L10 8.586l4.293-4.293a1 1 0 111.414 1.414L11.414 10l4.293 4.293a1 1 0 01-1.414 1.414L10 11.414l-4.293 4.293a1 1 0 01-1.414-1.414L8.586 10 4.293 5.707a1 1 0 010-1.414z"
            clip-rule="evenodd"
          />
        </svg>
      </button>
    </div>

    <div class="p-4 space-y-3">
      <div v-for="(option, index) in options" :key="index" class="flex items-center gap-2">
        <input
          v-model="option.text"
          type="text"
          :placeholder="`${$t('post.pollOption') || 'Opcja'} ${index + 1}`"
          class="flex-grow p-3.5 border border-gray-300 dark:border-theme-border rounded-xl bg-white dark:bg-theme-bg-tertiary text-gray-900 dark:text-theme-text focus:outline-none focus:ring-2 focus:ring-blue-500 transition-shadow"
          @input="updateStore"
        />

        <button
          @click="removeOption(index)"
          class="flex-shrink-0 w-8 h-8 flex items-center justify-center bg-gray-200 dark:bg-theme-bg-tertiary rounded-full text-gray-500 hover:bg-gray-300 dark:hover:bg-red-900/30 hover:text-red-600 transition-colors"
          title="Usuń opcję"
        >
          <svg
            xmlns="http://www.w3.org/2000/svg"
            class="h-4 w-4"
            viewBox="0 0 20 20"
            fill="currentColor"
          >
            <path
              fill-rule="evenodd"
              d="M4.293 4.293a1 1 0 011.414 0L10 8.586l4.293-4.293a1 1 0 111.414 1.414L11.414 10l4.293 4.293a1 1 0 01-1.414 1.414L10 11.414l-4.293 4.293a1 1 0 01-1.414-1.414L8.586 10 4.293 5.707a1 1 0 010-1.414z"
              clip-rule="evenodd"
            />
          </svg>
        </button>
      </div>

      <div class="flex gap-2 pt-2">
        <button
          v-if="options.length < 10"
          @click="addOption"
          class="flex-grow flex items-center justify-center py-3 px-4 bg-gray-200 dark:bg-theme-bg-tertiary text-gray-800 dark:text-theme-text font-semibold rounded-xl hover:bg-gray-300 dark:hover:bg-theme-bg-hover transition-colors"
        >
          <svg
            xmlns="http://www.w3.org/2000/svg"
            class="h-5 w-5 mr-2"
            viewBox="0 0 20 20"
            fill="currentColor"
          >
            <path
              fill-rule="evenodd"
              d="M10 3a1 1 0 011 1v5h5a1 1 0 110 2h-5v5a1 1 0 11-2 0v-5H4a1 1 0 110-2h5V4a1 1 0 011-1z"
              clip-rule="evenodd"
            />
          </svg>
          {{ $t('post.addOption') || 'Dodaj opcję' }}
        </button>

        <button
          class="flex-shrink-0 w-12 flex items-center justify-center bg-gray-200 dark:bg-theme-bg-tertiary text-gray-800 dark:text-theme-text rounded-xl hover:bg-gray-300 dark:hover:bg-theme-bg-hover transition-colors"
        >
          <svg
            xmlns="http://www.w3.org/2000/svg"
            class="h-6 w-6"
            fill="none"
            viewBox="0 0 24 24"
            stroke="currentColor"
          >
            <path
              stroke-linecap="round"
              stroke-linejoin="round"
              stroke-width="2"
              d="M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z"
            />
            <path
              stroke-linecap="round"
              stroke-linejoin="round"
              stroke-width="2"
              d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"
            />
          </svg>
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useCreatePostStore } from '@/stores/createPost'

const createPostStore = useCreatePostStore()

const emit = defineEmits(['back', 'navigate'])

// Inicjalizacja danych
const question = ref(createPostStore.postData.poll?.question || '')
const options = ref(
  createPostStore.postData.poll?.options && createPostStore.postData.poll.options.length > 0
    ? createPostStore.postData.poll.options
    : [{ text: '' }, { text: '' }],
)

const addOption = () => {
  if (options.value.length < 10) {
    options.value.push({ text: '' })
    updateStore()
  }
}

const removeOption = (index: number) => {
  if (options.value.length > 2) {
    options.value.splice(index, 1)
    updateStore()
  }
}

const cancel = () => {
  emit('back')
}

const updateStore = () => {
  createPostStore.postData.poll = {
    question: question.value,
    options: options.value,
  }
}
</script>
