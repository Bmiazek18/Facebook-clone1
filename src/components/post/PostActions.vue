<script setup lang="ts">
import { ref } from 'vue'
import MessageOutline from 'vue-material-design-icons/MessageOutline.vue'
import ShareIcon from 'vue-material-design-icons/ShareVariant.vue'
import Pencil from 'vue-material-design-icons/Pencil.vue'
import BookOpenPageVariant from 'vue-material-design-icons/BookOpenPageVariant.vue'
import ChatProcessingOutline from 'vue-material-design-icons/ChatProcessingOutline.vue' // New import
import ReactionButton from '../ReactionButton.vue'
import { useI18n } from 'vue-i18n'
import { useTheme } from '@/composables/useTheme'

const { t } = useI18n()
const { isDark } = useTheme()

const isShareMenuOpen = ref(false)

const toggleShareMenu = () => {
  isShareMenuOpen.value = !isShareMenuOpen.value
}

const closeShareMenu = () => {
  isShareMenuOpen.value = false
}

const emit = defineEmits<{
  (e: 'react', reaction: string | null): void
  (e: 'comment'): void
  (e: 'shareAsPost'): void
  (e: 'shareToStory'): void
  (e: 'shareToMessage'): void // New emit event
}>()

const handleShareAsPost = () => {
  closeShareMenu()
  emit('shareAsPost')
}

const handleShareToStory = () => {
  closeShareMenu()
  emit('shareToStory')
}

const handleShareToMessage = () => { // New handler
  closeShareMenu()
  emit('shareToMessage')
}
</script>

<template>
  <div class="px-2 py-1 flex items-center justify-between relative z-10">
    <div class="flex-1">
      <ReactionButton @react="$emit('react', $event)" />
    </div>
    <button
      @click="$emit('comment')"
      class="flex-1 flex items-center justify-center gap-2 h-9 rounded hover:bg-theme-hover transition-colors cursor-pointer text-theme-text-secondary font-semibold text-[15px]"
    >
      <MessageOutline :size="20" class="text-gray-500 dark:text-gray-400" />
      <span>{{ t('home.comment') }}</span>
    </button>
    <div class="flex-1 relative">
      <button
        class="w-full flex items-center justify-center gap-2 h-9 rounded hover:bg-theme-hover transition-colors cursor-pointer text-theme-text-secondary font-semibold text-[15px]"
        @click="toggleShareMenu"
      >
        <ShareIcon :size="20" class="text-gray-500 dark:text-gray-400" />
        <span>{{ t('actions.share') }}</span>
      </button>

      <!-- Share Menu -->
      <Transition
        enter-active-class="transition ease-out duration-100"
        enter-from-class="transform opacity-0 scale-95"
        enter-to-class="transform opacity-100 scale-100"
        leave-active-class="transition ease-in duration-75"
        leave-from-class="transform opacity-100 scale-100"
        leave-to-class="transform opacity-0 scale-95"
      >
        <div
          v-if="isShareMenuOpen"
          class="absolute bottom-full right-0 mb-2 w-[300px] bg-theme-bg-secondary rounded-lg shadow-xl border border-theme-border z-50 overflow-hidden"
        >
          <div class="p-2">
            <button
              @click="handleShareAsPost"
              class="w-full px-3 py-2 flex items-center gap-3 hover:bg-theme-hover rounded-lg transition-colors text-left"
            >
              <div class="w-9 h-9 bg-gray-200 dark:bg-gray-700 rounded-full flex items-center justify-center">
                <Pencil :size="20" :fillColor="isDark ? '#E4E6EB' : '#050505'" />
              </div>
              <div class="flex-1">
                <p class="text-[15px] font-medium text-theme-text">{{ t('post.shareInFeed') }}</p>
                <p class="text-xs text-theme-text-secondary">{{ t('post.shareYourPost') }}</p>
              </div>
            </button>
            <button
              @click="handleShareToStory"
              class="w-full px-3 py-2 flex items-center gap-3 hover:bg-theme-hover rounded-lg transition-colors text-left mt-1"
            >
              <div class="w-9 h-9 bg-gray-200 dark:bg-gray-700 rounded-full flex items-center justify-center">
                <BookOpenPageVariant :size="20" :fillColor="isDark ? '#E4E6EB' : '#050505'" />
              </div>
              <div class="flex-1">
                <p class="text-[15px] font-medium text-theme-text">{{ t('post.shareInStory') }}</p>
              </div>
            </button>
            <button
              @click="handleShareToMessage"
              class="w-full px-3 py-2 flex items-center gap-3 hover:bg-theme-hover rounded-lg transition-colors text-left mt-1"
            >
              <div class="w-9 h-9 bg-gray-200 dark:bg-gray-700 rounded-full flex items-center justify-center">
                <ChatProcessingOutline :size="20" :fillColor="isDark ? '#E4E6EB' : '#050505'" />
              </div>
              <div class="flex-1">
                <p class="text-[15px] font-medium text-theme-text">{{ t('post.shareInMessage') }}</p>
              </div>
            </button>
          </div>
        </div>
      </Transition>
    </div>

    <!-- Backdrop to close menu -->
    <div v-if="isShareMenuOpen" class="fixed inset-0 z-[-1]" @click="closeShareMenu"></div>
  </div>
</template>
