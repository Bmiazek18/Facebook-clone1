<script setup lang="ts">
import { computed, type PropType } from 'vue'
import { useI18n } from 'vue-i18n'
import { getUserById } from '@/utils/users'
import type { User } from '@/utils/users'
import type { ReactionType, StoryInteraction } from '@/types/Story'
import Close from 'vue-material-design-icons/Close.vue'

const props = defineProps({
  show: {
    type: Boolean,
    default: false,
  },
  interactions: {
    type: Array as PropType<StoryInteraction[]>,
    default: () => [],
  },
})

const emit = defineEmits(['update:show', 'update:isPaused'])

const { t } = useI18n()

const reactionEmojis: Record<ReactionType, string> = {
  like: '👍',
  love: '❤️',
  haha: '😆',
  wow: '😮',
  sad: '😢',
  angry: '😡',
}

const viewersWithReactions = computed(() => {
  return props.interactions
    .map((interaction) => {
      const viewer = getUserById(Number(interaction.userId))
      return { viewer, reaction: interaction.reaction }
    })
    .filter((item): item is { viewer: User; reaction: ReactionType | null } => !!item.viewer)
})

const close = () => {
  emit('update:show', false)
  emit('update:isPaused', false)
}
</script>

<template>
  <div
    v-if="show"
    class="absolute inset-0 z-50 flex flex-col items-center justify-end bg-black/60 backdrop-blur-sm"
    @click.self="close"
  >
    <div
      class="bg-white dark:bg-[#242526] w-full md:w-[400px] rounded-t-xl h-[60vh] flex flex-col shadow-2xl animate-slide-up"
    >
      <div
        class="p-4 border-b border-gray-200 dark:border-gray-700 flex items-center justify-between"
      >
        <h3 class="font-bold text-lg dark:text-white">{{ t('createLive.viewers') }}</h3>
        <button @click="close" class="p-2 hover:bg-gray-100 dark:hover:bg-gray-700 rounded-full">
          <Close :size="24" class="dark:text-white" />
        </button>
      </div>
      <div class="flex-1 overflow-y-auto p-2">
        <div
          v-if="viewersWithReactions.length === 0"
          class="flex flex-col items-center justify-center h-full text-gray-500"
        >
          <p>{{ t('search.noResults') }}</p>
        </div>
        <div v-else class="flex flex-col gap-2">
          <div
            v-for="{ viewer, reaction } in viewersWithReactions"
            :key="viewer.id"
            class="flex items-center justify-between p-2 hover:bg-gray-100 dark:hover:bg-gray-700 rounded-lg cursor-pointer"
          >
            <div class="flex items-center gap-3">
              <img :src="viewer.avatar" class="w-10 h-10 rounded-full object-cover" />
              <span class="font-medium dark:text-white">{{ viewer.name }}</span>
            </div>
            <div v-if="reaction" class="text-2xl">
              {{ reactionEmojis[reaction] }}
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
<style scoped>
@keyframes slide-up {
  from {
    transform: translateY(100%);
  }
  to {
    transform: translateY(0);
  }
}
.animate-slide-up {
  animation: slide-up 0.3s ease-out;
}
</style>
