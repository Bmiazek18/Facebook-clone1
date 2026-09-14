<template>
  <div class="mx-3 mb-3 mt-2 rounded-lg overflow-hidden" v-if="likesCount > 0 || commentsCount > 0 || sharesCount > 0">
    <div class="flex items-center justify-between text-theme-text-secondary text-[15px]">
      <div
        class="flex items-center gap-1.5 cursor-pointer group"
        v-if="likesCount > 0"
        @click="emit('show-reaction-details')"
      >
        <div class="flex items-center relative">
          <div
            v-for="(reactionType, index) in topReactions"
            :key="reactionType"
            class="relative z-10 rounded-full w-[18px] h-[18px] flex items-center justify-center ring-2 ring-white dark:ring-[#242526]"
            :class="[getReactionConfig(reactionType).wrapperClass, index > 0 ? '-ml-1' : '']"
            :style="{ zIndex: topReactions.length - index }"
          >
            <VTooltip>
              <component
                v-if="getReactionConfig(reactionType).mode === 'icon'"
                :is="getReactionConfig(reactionType).component"
                :size="11"
                :fillColor="getReactionConfig(reactionType).color"
              />
              <span v-else class="text-[20px] select-none">
                {{ getReactionConfig(reactionType).char }}
              </span>

              <!-- Zmodyfikowany wygląd tooltipa pod zrzut ekranu -->
              <template #popper>
                <div class="flex flex-col text-[13px]   rounded-md">
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
                  >{{ $t('feed.iGetreactiontooltipdataReactiontypeAs') }}</span>
                </div>
              </template>
            </VTooltip>
          </div>
        </div>

        <span class="text-theme-text-secondary group-hover:underline leading-snug ml-0.5">
          <template v-if="userReaction">
            <span v-if="likesCount === 1">{{ t('post.likedByYou') }}</span>
            <span v-else-if="likesCount === 2">{{ t('post.likedByYouAndOneOther') }}</span>
            <span v-else>{{ t('post.likedByYouAndOthers', { count: likesCount - 1 }) }}</span>
          </template>

          <template v-else>
            <span v-if="likesCount > 0">{{ likesCount }}</span>
          </template>
        </span>
      </div>

      <div v-else></div>

      <div v-if=" commentsCount >0 || sharesCount > 0" class="flex items-center gap-3 text-theme-text-secondary">
        <span v-if="commentsCount > 0" @click="emit('show-comments')" class="hover:underline cursor-pointer">{{
          t('post.commentsCount', { count: commentsCount })
        }}</span>
        <VTooltip v-if="sharesCount > 0" @apply-show="loadShareUsers">
          <span class="hover:underline cursor-pointer" @mouseenter="loadShareUsers">{{
            t(props.hasPoll ? 'poll.totalVotes' : 'post.sharesCount', { count: sharesCount })
          }}</span>

          <template #popper>
            <div class="flex flex-col text-[13px] rounded-md min-w-[120px] p-1">
              <strong class="font-bold text-white mb-1">
                {{ $t('post.shares') || 'Udostępnienia' }}
              </strong>
              <div v-if="isLoadingSharers" class="flex items-center gap-2 py-1.5 text-[#B0B3B8] text-[12px]">
                <LoadingSpinner size="14px" color="#1877F2" />
                <span>{{ $t('common.loading') || 'Ładowanie...' }}</span>
              </div>
              <template v-else-if="sharerNames.length > 0">
                <span
                  v-for="name in sharerNames"
                  :key="name"
                  class="text-[#E4E6EB] leading-tight py-[1.5px]"
                >
                  {{ name }}
                </span>
                <span
                  v-if="remainingSharersCount > 0"
                  class="mt-1 text-[#B0B3B8] leading-tight text-[12px]"
                >
                  {{ `i ${remainingSharersCount} innych...` }}
                </span>
              </template>
              <div v-else class="text-[#B0B3B8] text-[12px] py-[1px]">
                {{ $t('post.unknownUser') || 'Użytkownicy' }}
              </div>
            </div>
          </template>
        </VTooltip>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import LoadingSpinner from '@/components/common/LoadingSpinner.vue'

import type { ReactionType } from '@/types/Post'
import { getUserById } from '@/utils/users'
import { useReactionConfig } from '@/composables/feed/useReactionConfig'
import { usePostsStore } from '@/stores/posts'
import { useUserCache } from '@/composables/shared/useUserCache'

const props = defineProps<{
  postId: string | number
  commentsCount: number
  sharesCount: number
  userReaction: string | null
  likesCount: number
  topReactions: string[] // Add topReactions prop
  reactions: Partial<Record<ReactionType, number[]>> // New prop
  reactionUserNames?: Partial<Record<ReactionType, string[]>> // New prop
  hasPoll?: boolean // New prop
}>()

const { t } = useI18n()
const { getReactionConfig } = useReactionConfig()
const postsStore = usePostsStore()
const { getOrFetchUser, preloadUsers } = useUserCache()

const emit = defineEmits<{
  (e: 'show-reaction-details'): void
  (e: 'show-comments'): void
}>()

const sharerNames = ref<string[]>([])
const isLoadingSharers = ref(false)
const hasLoadedSharers = ref(false)

const remainingSharersCount = computed(() => {
  const total = props.sharesCount
  return Math.max(0, total - sharerNames.value.length)
})

const loadShareUsers = async () => {
  if (hasLoadedSharers.value && sharerNames.value.length > 0) return
  isLoadingSharers.value = true

  try {
    const pid = String(props.postId)
    const sharingPosts = postsStore.posts.filter((p: any) => 
      (p.targetType === 'post' && String(p.targetId) === pid) ||
      (p.sharedContent?.type === 'post' && String(p.sharedContent.originalId) === pid) ||
      (p.sharedPost?.id && String(p.sharedPost.id) === pid)
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
  const resolvedNames = props.reactionUserNames?.[reactionType]
  if (resolvedNames && resolvedNames.length > 0) {
    if (resolvedNames.length <= 19) {
      return { names: resolvedNames, moreCount: 0 }
    } else {
      return {
        names: resolvedNames.slice(0, 19),
        moreCount: resolvedNames.length - 19,
      }
    }
  }

  const userIds = props.reactions[reactionType]
  if (!userIds) return { names: [], moreCount: 0 }

  const names = userIds.map((id) => getUserById(id)?.name).filter((name) => !!name) as string[]

  if (names.length <= 19) {
    return { names, moreCount: 0 }
  } else {
    return {
      names: names.slice(0, 19),
      moreCount: names.length - 19,
    }
  }
}
</script>
