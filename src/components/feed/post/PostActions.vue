<script setup lang="ts">
import { ref } from 'vue'
import MessageOutline from 'vue-material-design-icons/MessageOutline.vue'
import ShareIcon from 'vue-material-design-icons/ShareVariant.vue'
import Pencil from 'vue-material-design-icons/Pencil.vue'
import BookOpenPageVariant from 'vue-material-design-icons/BookOpenPageVariant.vue'
import ChatProcessingOutline from 'vue-material-design-icons/ChatProcessingOutline.vue'
import ReactionButton from '@/components/feed/ReactionButton.vue'
import { useI18n } from 'vue-i18n'
import { usePostReactions } from '@/composables/usePostReactions'

const props = defineProps<{
  postId: string
}>()

const { userReaction, handleReaction } = usePostReactions(props.postId)
const { t } = useI18n()

const emit = defineEmits<{
  (e: 'comment'): void
  (e: 'shareAsPost'): void
  (e: 'shareToStory'): void
  (e: 'shareToMessage'): void
}>()

// Funkcja pomocnicza do zamykania menu po wyborze opcji
const handleAction = (callback: () => void, hide: () => void) => {
  callback()
  hide()
}
</script>

<template>
  <div class="px-2 py-1 flex items-center justify-between relative z-10">
    <div class="flex-1">
      <ReactionButton :user-reaction="userReaction" full @react="handleReaction" />
    </div>

    <button
      @click="$emit('comment')"
      class="flex-1 flex items-center justify-center gap-2 h-9 rounded hover:bg-theme-hover transition-colors cursor-pointer text-theme-text-secondary font-semibold text-[15px]"
    >
      <MessageOutline :size="20" class="text-gray-500 dark:text-gray-400" />
      <span>{{ t('home.comment') }}</span>
    </button>

    <div class="flex-1">
      <VMenu
        placement="top-end"
        :distance="12"
        :triggers="['click']"
        container="body"
      >
        <button
          class="w-full flex items-center justify-center gap-2 h-9 rounded hover:bg-theme-hover transition-colors cursor-pointer text-theme-text-secondary font-semibold text-[15px]"
        >
          <ShareIcon :size="20" class="text-gray-500 dark:text-gray-400" />
          <span>{{ t('actions.share') }}</span>
        </button>

        <template #popper="{ hide }">
          <div class="w-[300px] bg-theme-bg-secondary p-2 rounded-lg shadow-xl border border-theme-border overflow-hidden">
            <button
              @click="handleAction(() => emit('shareAsPost'), hide)"
              class="w-full px-3 py-2 flex items-center gap-3 hover:bg-theme-hover rounded-lg transition-colors text-left group"
            >
              <div class="w-9 h-9 bg-gray-200 dark:bg-gray-700 rounded-full flex items-center justify-center">
                <Pencil :size="20" class="icon-theme" />
              </div>
              <div class="flex-1">
                <p class="text-[15px] font-medium text-theme-text">{{ t('post.shareInFeed') }}</p>
                <p class="text-xs text-theme-text-secondary">{{ t('post.shareYourPost') }}</p>
              </div>
            </button>

            <button
              @click="handleAction(() => emit('shareToStory'), hide)"
              class="w-full px-3 py-2 flex items-center gap-3 hover:bg-theme-hover rounded-lg transition-colors text-left mt-1"
            >
              <div class="w-9 h-9 bg-gray-200 dark:bg-gray-700 rounded-full flex items-center justify-center">
                <BookOpenPageVariant :size="20" class="icon-theme" />
              </div>
              <div class="flex-1">
                <p class="text-[15px] font-medium text-theme-text">{{ t('post.shareInStory') }}</p>
              </div>
            </button>

            <button
              @click="handleAction(() => emit('shareToMessage'), hide)"
              class="w-full px-3 py-2 flex items-center gap-3 hover:bg-theme-hover rounded-lg transition-colors text-left mt-1"
            >
              <div class="w-9 h-9 bg-gray-200 dark:bg-gray-700 rounded-full flex items-center justify-center">
                <ChatProcessingOutline :size="20" class="icon-theme" />
              </div>
              <div class="flex-1">
                <p class="text-[15px] font-medium text-theme-text">{{ t('post.shareInMessage') }}</p>
              </div>
            </button>
          </div>
        </template>
      </VMenu>
    </div>
  </div>
</template>

<style scoped>
.icon-theme {
  color: #050505;
}

:deep(.dark) .icon-theme {
  color: #E4E6EB;
}

:deep(svg) {
  fill: currentColor;
}
</style>
