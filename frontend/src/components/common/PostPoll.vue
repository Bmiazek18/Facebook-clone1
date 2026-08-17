<template>
  <div>
    <div class="poll-container w-full px-4">
      <div class="options-list space-y-2">
        <div
          v-for="option in poll.options"
          :key="option.id"
          class="group relative border rounded-xl overflow-hidden transition-all duration-200 select-none"
          :class="[
            hasVoted
              ? 'border-gray-200 dark:border-gray-700'
              : 'border-gray-300 hover:border-gray-400 cursor-pointer bg-white dark:bg-theme-bg-secondary',
          ]"
          @click="vote(option.id)"
        >
          <div
            v-if="hasVoted"
            class="absolute top-0 left-0 h-full bg-blue-100 dark:bg-blue-900/40 transition-all duration-500 ease-out"
            :style="{ width: getPercentage(option) + '%' }"
          ></div>

          <div class="relative z-10 flex items-center p-3.5 w-full">
            <div
              class="flex-shrink-0 w-5 h-5 rounded-md flex items-center justify-center border mr-3 transition-colors duration-200"
              :class="[
                isSelected(option)
                  ? 'bg-gray-900 border-gray-900 dark:bg-white dark:border-white text-white dark:text-black'
                  : 'border-gray-400 dark:border-gray-500 bg-transparent group-hover:border-gray-500',
              ]"
            >
              <svg
                v-if="isSelected(option)"
                xmlns="http://www.w3.org/2000/svg"
                class="h-3.5 w-3.5"
                viewBox="0 0 20 20"
                fill="currentColor"
              >
                <path
                  fill-rule="evenodd"
                  d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z"
                  clip-rule="evenodd"
                />
              </svg>
            </div>

            <span
              class="font-medium text-sm sm:text-base leading-snug"
              :class="[
                hasVoted && isSelected(option)
                  ? 'text-gray-900 dark:text-white font-semibold'
                  : 'text-gray-800 dark:text-gray-200',
              ]"
            >
              {{ option.text }}
            </span>

            <div
              v-if="hasVoted"
              class="ml-auto flex items-center gap-1 pl-2"
              :class="{ 'cursor-pointer': hasVoted }"
              @click.stop="showVoters(option)"
            >
              <span class="text-blue-600 dark:text-blue-400 font-bold text-sm">
                {{ getPercentage(option) }}%
              </span>
              <svg
                xmlns="http://www.w3.org/2000/svg"
                class="h-4 w-4 text-gray-400 dark:text-gray-500"
                viewBox="0 0 20 20"
                fill="currentColor"
              >
                <path
                  fill-rule="evenodd"
                  d="M7.293 14.707a1 1 0 010-1.414L10.586 10 7.293 6.707a1 1 0 011.414-1.414l4 4a1 1 0 010 1.414l-4 4a1 1 0 01-1.414 0z"
                  clip-rule="evenodd"
                />
              </svg>
            </div>
          </div>
        </div>
      </div>
    </div>

    <BaseModal
      v-if="isVotersModalOpen"
      @close="closeVotersModal"
      :title="`Użytkownicy, którzy wybrali: ${selectedOptionText}`"
    >
      <div class="p-4">
        <ul class="space-y-2">
          <li
            v-for="voterId in selectedOptionVoters"
            :key="voterId"
            class="flex items-center text-theme-text"
          >
            <img :src="getUserById(Number(voterId))?.avatar" class="w-8 h-8 rounded-full mr-2" />
            {{ getUserById(Number(voterId))?.name }}
          </li>
        </ul>
      </div>
    </BaseModal>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import type { Poll } from '@/types/Post'
import { useAuthStore } from '@/stores/auth'
import { usePostsStore } from '@/composables/feed/useAppState'
import BaseModal from '@/components/common/BaseModal.vue'
import { getUserById } from '@/utils/users'

const authStore = useAuthStore()
const postsStore = usePostsStore()

const isVotersModalOpen = ref(false)
const selectedOptionVoters = ref<string[]>([])
const selectedOptionText = ref('')

const props = defineProps<{
  poll: Poll
  postId: string
}>()

const currentUser = computed(() => authStore.currentUser)

const totalVotes = computed(() => {
  return props.poll.options.reduce((sum, option) => sum + option.votes.length, 0)
})

// Czy użytkownik zagłosował w ogóle w tej ankiecie?
const hasVoted = computed(() => {
  if (!currentUser.value) return false
  return props.poll.options.some((option) =>
    option.votes.includes(currentUser.value!.id.toString()),
  )
})

// Sprawdza, czy konkretna opcja została wybrana przez użytkownika
const isSelected = (option: Poll['options'][0]) => {
  if (!currentUser.value) return false
  return option.votes.includes(currentUser.value.id.toString())
}

const getPercentage = (option: Poll['options'][0]) => {
  if (totalVotes.value === 0) return 0
  return Math.round((option.votes.length / totalVotes.value) * 100)
}

const vote = (optionId: string) => {
  if (currentUser.value) {
    postsStore.voteOnPoll(props.postId, optionId, currentUser.value.id.toString())
  }
}

const showVoters = (option: Poll['options'][0]) => {
  selectedOptionVoters.value = option.votes
  selectedOptionText.value = option.text
  isVotersModalOpen.value = true
}

const closeVotersModal = () => {
  isVotersModalOpen.value = false
  selectedOptionVoters.value = []
  selectedOptionText.value = ''
}
</script>
