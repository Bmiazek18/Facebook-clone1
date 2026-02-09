<template>
  <div class="mx-3 mb-3 mt-2 rounded-lg overflow-hidden">
    <div class="flex items-center justify-between text-theme-text-secondary text-[15px]">

      <div class="flex items-center gap-1.5 cursor-pointer group" v-if="likesCount > 0" @click="emit('show-reaction-details')">

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
    <span v-else class="text-[20px]  select-none">
      {{ getReactionConfig(reactionType).char }}
    </span>

    <template #popper>
      <div class="flex flex-col text-sm">
        <strong class="font-bold border-bottom border-gray-600 mb-1">
          {{ getReactionConfig(reactionType as ReactionType).label }}
        </strong>
        <span v-for="name in getReactionUserNames(reactionType as ReactionType)" :key="name">
          {{ name }}
        </span>
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
            <span>{{ likesCount}}</span>
          </template>
        </span>
      </div>

      <div v-else></div>

      <div class="flex items-center gap-3 text-theme-text-secondary">
        <span class="hover:underline cursor-pointer">{{ t('post.commentsCount', { count: commentsCount }) }}</span>
        <span class="hover:underline cursor-pointer">{{ t(props.hasPoll ? 'poll.totalVotes' : 'post.sharesCount', { count: sharesCount }) }}</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">

import { useI18n } from 'vue-i18n'

import type { ReactionType } from '@/types/Post';
import { getUserById } from '@/data/users';
import { useReactionConfig } from '@/composables/useReactionConfig'

const props = defineProps<{
  postId: string | number
  commentsCount: number
  sharesCount: number
  userReaction: string | null
  likesCount: number
  topReactions: string[] // Add topReactions prop
  reactions: Partial<Record<ReactionType, number[]>> // New prop
  hasPoll?: boolean // New prop
}>()

const { t } = useI18n()
const { getReactionConfig } = useReactionConfig()


const emit = defineEmits<{
  (e: 'show-reaction-details'): void
}>()


const getReactionUserNames = (reactionType: ReactionType): string[] => {
  const userIds = props.reactions[reactionType];
  if (!userIds) return [];

  return userIds
    .map(id => getUserById(id)?.name)
    .filter(name => !!name) as string[];
};
</script>
