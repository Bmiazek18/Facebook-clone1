<script setup lang="ts">
import { computed } from 'vue'
import type { StoryItem, ReactionType } from '@/types/Story'
import { getUserById, type User } from '@/utils/users'
import Close from 'vue-material-design-icons/Close.vue'
import { reactionIcons } from '@/composables/feed/usePostReactions' // Assuming these are reusable

const props = defineProps<{
  story: StoryItem
  show: boolean
}>()

const emit = defineEmits(['close'])

const reactionsByType = computed(() => {
  const result = new Map<ReactionType, User[]>()
  if (!props.story.interactions) return []

  for (const interaction of props.story.interactions) {
    if (interaction.reaction) {
      if (!result.has(interaction.reaction)) {
        result.set(interaction.reaction, [])
      }
      const user = getUserById(Number(interaction.userId))
      if (user) {
        result.get(interaction.reaction)!.push(user)
      }
    }
  }

  return Array.from(result, ([reaction, users]) => ({ reaction, users }))
})
</script>

<template>
  <div
    v-if="show"
    class="absolute inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-sm"
    @click.self="emit('close')"
  >
    <div
      class="bg-white dark:bg-[#242526] w-full max-w-md rounded-lg shadow-xl flex flex-col max-h-[80vh]"
    >
      <div
        class="p-4 border-b border-gray-200 dark:border-gray-700 flex items-center justify-between"
      >
        <h3 class="font-bold text-lg dark:text-white">Reactions</h3>
        <button
          @click="emit('close')"
          class="p-2 hover:bg-gray-100 dark:hover:bg-gray-700 rounded-full"
        >
          <Close :size="24" class="dark:text-white" />
        </button>
      </div>
      <div class="flex-1 overflow-y-auto p-4">
        <div v-if="reactionsByType.length === 0" class="text-center text-gray-500">
          No reactions yet.
        </div>
        <div v-else>
          <div v-for="{ reaction, users } in reactionsByType" :key="reaction" class="mb-4">
            <div class="flex items-center mb-2">
              <img :src="reactionIcons[reaction]?.src" class="w-6 h-6 mr-2" />
              <h4 class="font-semibold text-lg dark:text-white">
                {{ reaction.charAt(0).toUpperCase() + reaction.slice(1) }}
              </h4>
            </div>
            <ul>
              <li
                v-for="user in users"
                :key="user.id"
                class="flex items-center gap-3 p-2 rounded-lg"
              >
                <img :src="user.avatar" class="w-10 h-10 rounded-full" />
                <span class="font-medium dark:text-white">{{ user.name }}</span>
              </li>
            </ul>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
