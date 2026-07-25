<script setup lang="ts">
import { ref, toRef } from 'vue'
import MessageOutline from 'vue-material-design-icons/MessageOutline.vue'
import ShareIcon from 'vue-material-design-icons/ShareVariant.vue'
import Pencil from 'vue-material-design-icons/Pencil.vue'
import PlusCircleOutline from 'vue-material-design-icons/PlusCircleOutline.vue'
import ChatProcessingOutline from 'vue-material-design-icons/ChatProcessingOutline.vue'
import LinkVariant from 'vue-material-design-icons/LinkVariant.vue'
import CodeTags from 'vue-material-design-icons/CodeTags.vue'
import Earth from 'vue-material-design-icons/Earth.vue'
import ChevronRight from 'vue-material-design-icons/ChevronRight.vue'
import ReactionButton from '@/components/feed/ReactionButton.vue'
import { useI18n } from 'vue-i18n'
import { usePostReactions } from '@/composables/feed/usePostReactions'

const props = defineProps<{
  post: any
}>()

const { userReaction, handleReaction } = usePostReactions(toRef(props, 'post'))
const { t } = useI18n()

const emit = defineEmits<{
  (e: 'comment'): void
  (e: 'shareAsPost'): void
  (e: 'shareToStory'): void
  (e: 'shareToMessage'): void
  (e: 'moreOptions'): void
  (e: 'copyLink'): void
  (e: 'embed'): void
  (e: 'shareVia'): void
}>()

// Funkcja pomocnicza do zamykania menu po wyborze opcji
const handleAction = (callback: () => void, hide: () => void) => {
  callback()
  hide()
}

// Konfiguracja opcji menu dokładnie wg zrzutu ekranu
const shareMenuItems = [
  {
    key: 'shareInFeed',
    labelKey: 'post.shareInFeed',
    defaultLabel: 'Udostępnij w Aktualnościach',
    icon: Pencil,
    emitEvent: () => emit('shareAsPost'),
  },
  {
    key: 'shareToStory',
    labelKey: 'post.shareToStory',
    defaultLabel: 'Udostępnij w swojej relacji (Ustawienie n...',
    icon: PlusCircleOutline,
    emitEvent: () => emit('shareToStory'),
  },
  {
    key: 'shareInMessage',
    labelKey: 'post.shareInMessage',
    defaultLabel: 'Wyślij w Messengerze',
    icon: ChatProcessingOutline,
    emitEvent: () => emit('shareToMessage'),
  },
  {
    key: 'moreOptions',
    labelKey: 'post.moreOptions',
    defaultLabel: 'Więcej opcji',
    icon: Pencil,
    hasChevron: true,
    emitEvent: () => emit('moreOptions'),
  },
  {
    key: 'copyLink',
    labelKey: 'post.copyLink',
    defaultLabel: 'Kopiuj link',
    icon: LinkVariant,
    emitEvent: () => emit('copyLink'),
  },
  {
    key: 'embed',
    labelKey: 'post.embed',
    defaultLabel: 'Osadź',
    icon: CodeTags,
    emitEvent: () => emit('embed'),
  },
  {
    key: 'shareVia',
    labelKey: 'post.shareVia',
    defaultLabel: 'Udostępnij przez:',
    icon: Earth,
    hasChevron: true,
    emitEvent: () => emit('shareVia'),
  },
]
</script>

<template>
  <div class="px-2 py-1 flex items-center justify-between relative z-10">
    <div class="flex-1">
      <ReactionButton :user-reaction="userReaction" full @react="handleReaction" />
    </div>

    <button
      @click="$emit('comment')"
      class="flex-1 flex items-center justify-center gap-2 h-9 rounded hover:bg-theme-hover transition-colors cursor-pointer text-theme-text-secondary text-[15px]"
    >
      <MessageOutline :size="20" class="text-gray-500 dark:text-gray-400" />
      <span>{{ t('home.comment') }}</span>
    </button>

    <div class="flex-1">
      <VMenu placement="top-end"
  :distance="12"
  :triggers="['click']"
  :popper-triggers="[]"
  :delay="{ show: 0, hide: 0 }"
  :auto-hide="true"
  container="body">
        <button
          class="w-full flex items-center justify-center gap-2 h-9 rounded hover:bg-theme-hover transition-colors cursor-pointer text-theme-text-secondary text-[15px]"
        >
          <ShareIcon :size="20" class="text-gray-500 dark:text-gray-400" />
          <span>{{ t('actions.share') }}</span>
        </button>

        <template #popper="{ hide }">
          <nav
            class="w-[360px] max-w-[90vw] bg-theme-bg-secondary p-2 rounded-2xl shadow-xl border border-gray-100 dark:border-gray-800"
          >
            <ul class="p-0 m-0 list-none space-y-0.5">
              <li v-for="item in shareMenuItems" :key="item.key">
                <button
                  @click="handleAction(item.emitEvent, hide)"
                  class="w-full px-2 py-2 cursor-pointer flex items-center justify-between hover:bg-theme-hover rounded-xl transition-colors text-left text-theme-text group"
                >
                  <div class="flex items-center gap-3 min-w-0 pr-2">
                    <component
                      :is="item.icon"
                      :size="20"
                      class="text-gray-800 dark:text-gray-200 shrink-0"
                    />
                    <span
                      class="text-[15px] leading-tight font-medium truncate text-gray-900 dark:text-gray-100"
                    >
                      {{ t(item.labelKey, item.defaultLabel) }}
                    </span>
                  </div>

                  <ChevronRight
                    v-if="item.hasChevron"
                    :size="20"
                    class="text-gray-500 dark:text-gray-400 shrink-0"
                  />
                </button>
              </li>
            </ul>
          </nav>
        </template>
      </VMenu>
    </div>
  </div>
</template>

<style scoped>
:deep(svg) {
  fill: currentColor;
}
</style>
