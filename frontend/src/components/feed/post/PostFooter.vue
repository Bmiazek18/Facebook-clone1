<script setup lang="ts">
import { ref, computed, toRef } from 'vue'
import { useI18n } from 'vue-i18n'
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
import LoadingSpinner from '@/components/common/LoadingSpinner.vue'
import { usePostReactions } from '@/composables/feed/usePostReactions'
import { useImpressionTracker } from '@/composables/analytics/useImpressionTracker'
import { useReactionConfig } from '@/composables/feed/useReactionConfig'
import { usePostsStore } from '@/stores/posts'
import { useUserCache } from '@/composables/shared/useUserCache'
import { getUserById } from '@/utils/users'
import type { ReactionType, Post } from '@/types/Post'

const props = withDefaults(
  defineProps<{
    post?: Post | any
    shouldPostActionVisible?: boolean
    hasPoll?: boolean
  }>(),
  {
    post: () => ({} as any),
    shouldPostActionVisible: true,
    hasPoll: false,
  },
)

const emit = defineEmits<{
  (e: 'comment'): void
  (e: 'show-reaction-details'): void
  (e: 'show-comments'): void
  (e: 'shareAsPost'): void
  (e: 'shareToStory'): void
  (e: 'shareToMessage'): void
  (e: 'moreOptions'): void
  (e: 'copyLink'): void
  (e: 'embed'): void
  (e: 'shareVia'): void
}>()

const { t } = useI18n()
const { getReactionConfig } = useReactionConfig()
const { userReaction, likesCount, topReactions, handleReaction } = usePostReactions(toRef(props, 'post'))
const { trackCopyLink } = useImpressionTracker()
const postsStore = usePostsStore()
const { getOrFetchUser, preloadUsers } = useUserCache()

// Stats
const commentsCount = computed(() => props.post?.commentCount ?? props.post?.stats?.comments ?? 0)
const sharesCount = computed(() => {
  const activePoll = props.post?.poll || props.post?.context?.poll
  if (activePoll?.options) {
    return activePoll.options.reduce((sum: number, opt: any) => sum + (opt.votes?.length || 0), 0)
  }
  return props.post?.shareCount ?? props.post?.stats?.shares ?? 0
})

// Sharer names
const sharerNames = ref<string[]>([])
const isLoadingSharers = ref(false)
const hasLoadedSharers = ref(false)

const remainingSharersCount = computed(() => {
  const total = sharesCount.value
  return Math.max(0, total - sharerNames.value.length)
})

const loadShareUsers = async () => {
  if (hasLoadedSharers.value && sharerNames.value.length > 0) return
  isLoadingSharers.value = true

  try {
    const pid = String(props.post?.id || '')
    const sharingPosts = postsStore.posts.filter((p: any) =>
      (p.targetType === 'post' && String(p.targetId) === pid) ||
      (p.sharedContent?.type === 'post' && String(p.sharedContent.originalId) === pid) ||
      (p.sharedPost?.id && String(p.sharedPost.id) === pid),
    )

    const names: string[] = []
    const authorIdsToFetch: string[] = []

    for (const sp of sharingPosts) {
      if (sp.isAnonymous) {
        names.push(t('post.anonymousUser') || 'Anonim')
        continue
      }
      if (sp.author?.name) {
        names.push(sp.author.name)
      } else if (sp.author?.firstName || sp.author?.lastName) {
        names.push([sp.author.firstName, sp.author.lastName].filter(Boolean).join(' '))
      } else if (sp.authorId) {
        authorIdsToFetch.push(String(sp.authorId))
      }
    }

    if (authorIdsToFetch.length > 0) {
      await preloadUsers(authorIdsToFetch)
      for (const aid of authorIdsToFetch) {
        const u = await getOrFetchUser(aid)
        if (u?.name) {
          names.push(u.name)
        }
      }
    }

    const uniqueNames = Array.from(new Set(names))
    if (uniqueNames.length > 0) {
      sharerNames.value = uniqueNames
    }
    hasLoadedSharers.value = true
  } catch (err) {
    console.error('Failed to load share users:', err)
  } finally {
    isLoadingSharers.value = false
  }
}

const getReactionTooltipData = (reactionType: ReactionType) => {
  const resolvedNames = props.post?.reactionUserNames?.[reactionType]
  if (resolvedNames && resolvedNames.length > 0) {
    if (resolvedNames.length <= 19) {
      return { names: resolvedNames, moreCount: 0 }
    }
    return {
      names: resolvedNames.slice(0, 19),
      moreCount: resolvedNames.length - 19,
    }
  }

  const userIds = props.post?.reactions?.[reactionType]
  if (!userIds) return { names: [], moreCount: 0 }

  const names = userIds.map((id: any) => getUserById(id)?.name).filter(Boolean) as string[]

  if (names.length <= 19) {
    return { names, moreCount: 0 }
  }
  return {
    names: names.slice(0, 19),
    moreCount: names.length - 19,
  }
}

// Share menu
const handleAction = (callback: () => void, hide: () => void) => {
  callback()
  hide()
}

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
    emitEvent: () => {
      emit('copyLink')
      if (props.post?.id) {
        trackCopyLink(String(props.post.id), props.post.author?.id || props.post.authorId)
      }
    },
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
  <div class="post-footer w-full">
    <div
      v-if="shouldPostActionVisible"
      class="px-4 py-2 flex items-center justify-between select-none text-theme-text-secondary"
    >
      <!-- LEWA STRONA: Przyciski akcji (Like z licznikiem, Komentarz, Udostępnienie) -->
      <div class="flex items-center gap-5">
        <!-- Reakcja / Like + Licznik -->
        <div class="flex items-center gap-2">
          <ReactionButton :user-reaction="userReaction" @react="handleReaction" />
          <span
            v-if="likesCount > 0"
            @click="emit('show-reaction-details')"
            class="text-[15px] font-medium hover:underline cursor-pointer"
          >
            {{ likesCount }}
          </span>
        </div>

        <!-- Komentarz -->
        <button
          @click="$emit('comment')"
          class="flex items-center justify-center hover:text-theme-text transition-colors cursor-pointer"
          :title="t('home.comment')"
        >
          <MessageOutline :size="22" class="text-gray-600 dark:text-gray-300" />
        </button>

        <!-- Udostępnienie -->
        <VMenu
          placement="top-start"
          :distance="12"
          :triggers="['click']"
          :popper-triggers="[]"
          :delay="{ show: 0, hide: 0 }"
          :auto-hide="true"
          container="body"
        >
          <button
            class="flex items-center justify-center hover:text-theme-text transition-colors cursor-pointer"
            :title="t('actions.share')"
          >
            <ShareIcon :size="22" class="text-gray-600 dark:text-gray-300" />
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

      <!-- PRAWA STRONA: Kolorowe nakładające się ikony reakcji z tooltipem -->
      <div
        v-if="likesCount > 0"
        class="flex items-center cursor-pointer"
        @click="emit('show-reaction-details')"
      >
        <div class="flex items-center relative">
          <div
            v-for="(reactionType, index) in topReactions"
            :key="reactionType"
            class="relative z-10 rounded-full w-[20px] h-[20px] flex items-center justify-center ring-2 ring-white dark:ring-[#242526]"
            :class="[getReactionConfig(reactionType).wrapperClass, index > 0 ? '-ml-1.5' : '']"
            :style="{ zIndex: topReactions.length - index }"
          >
            <VTooltip>
              <component
                v-if="getReactionConfig(reactionType).mode === 'icon'"
                :is="getReactionConfig(reactionType).component"
                :size="12"
                :fillColor="getReactionConfig(reactionType).color"
              />
              <span v-else class="text-[20px] select-none">
                {{ getReactionConfig(reactionType).char }}
              </span>

              <template #popper>
                <div class="flex flex-col text-[13px] rounded-md">
                  <strong class="font-bold text-white mb-0.5">
                    {{ getReactionConfig(reactionType as ReactionType).label }}
                  </strong>
                  <span
                    v-for="name in getReactionTooltipData(reactionType as ReactionType).names"
                    :key="name"
                    class="text-[#E4E6EB] leading-tight py-[1px]"
                  >
                    {{ name }}
                  </span>
                  <span
                    v-if="getReactionTooltipData(reactionType as ReactionType).moreCount > 0"
                    class="mt-1 text-[#E4E6EB] leading-tight"
                  >
                    {{ `i ${getReactionTooltipData(reactionType as ReactionType).moreCount} innych...` }}
                  </span>
                </div>
              </template>
            </VTooltip>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
:deep(svg) {
  fill: currentColor;
}
</style>
