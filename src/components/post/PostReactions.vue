<template>
  <div class="mx-3 mb-3 mt-2 rounded-lg overflow-hidden">
    <div class="flex items-center justify-between text-theme-text-secondary text-[15px]">

      <div class="flex items-center gap-1.5 cursor-pointer group" v-if="likesCount > 0">

        <div class="flex items-center relative pl-1">

          <div
            v-if="likesCount > 1 || (likesCount === 1 && !userReaction)"
            class="relative z-10 rounded-full w-5 h-5 flex items-center justify-center bg-[#1877F2]"
          >
             <ThumbUp :size="12" fillColor="#FFFFFF" />
          </div>

          <div
            v-if="userReaction"
            class="relative z-0 rounded-full w-5 h-5 flex items-center justify-center ring-2 ring-white dark:ring-[#242526]"
            :class="[
              getReactionConfig(userReaction).wrapperClass,
              likesCount > 1 ? '-ml-1' : ''
            ]"
          >
            <component
              v-if="getReactionConfig(userReaction).mode === 'icon'"
              :is="getReactionConfig(userReaction).component"
              :size="10"
              :fillColor="getReactionConfig(userReaction).color"
            />

            <span
              v-else
              class="text-[20px] leading-none  select-none"
            >
              {{ getReactionConfig(userReaction).char }}
            </span>
          </div>
        </div>

        <span class="text-theme-text-secondary group-hover:underline leading-snug ml-0.5">
          <template v-if="userReaction">
            <span v-if="likesCount === 1">{{ t('post.likedByYou') }}</span>
            <span v-else-if="likesCount === 2">{{ t('post.likedByYouAnd', { name: 'Anna Kowalska' }) }}</span>
            <span v-else>{{ t('post.likedByYouAndOthers', { name: 'Anna Kowalska', count: likesCount - 2 }) }}</span>
          </template>

          <template v-else>
            <span>{{ t('post.likedBy', { name: 'Anna Kowalska' }) }}</span>
            <span v-if="likesCount > 1"> {{ t('post.andOthers', { count: likesCount - 1 }) }}</span>
          </template>
        </span>
      </div>

      <div v-else></div>

      <div class="flex items-center gap-3 text-theme-text-secondary">
        <span class="hover:underline cursor-pointer">{{ t('post.commentsCount', { count: commentsCount }) }}</span>
        <span class="hover:underline cursor-pointer">{{ t('post.sharesCount', { count: sharesCount }) }}</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">

import { useI18n } from 'vue-i18n'

import ThumbUp from 'vue-material-design-icons/ThumbUp.vue'
import Heart from 'vue-material-design-icons/Heart.vue'

import { usePostReactions } from '@/composables/usePostReactions'

 defineProps<{
  postId: string | number
  commentsCount: number
  sharesCount: number
}>()

const { t } = useI18n()


const { userReaction, likesCount, handleReaction } = usePostReactions(24)


const getReactionConfig = (type: string) => {
  switch (type) {

    case 'like':
      return {
        mode: 'icon',
        component: ThumbUp,
        wrapperClass: 'bg-[#1877F2]',
        color: '#FFFFFF'
      }
    case 'love':
      return {
        mode: 'icon',
        component: Heart,
        wrapperClass: 'bg-[#F3425F]',
        color: '#FFFFFF'
      }

    case 'haha':
      return { mode: 'emoji', char: '😆', wrapperClass: 'bg-white dark:bg-[#242526]' }
    case 'wow':
      return { mode: 'emoji', char: '😮', wrapperClass: 'bg-white dark:bg-[#242526]' }
    case 'sad':
      return { mode: 'emoji', char: '😢', wrapperClass: 'bg-white dark:bg-[#242526]' }
    case 'angry':
      return { mode: 'emoji', char: '😡', wrapperClass: 'bg-white dark:bg-[#242526]' }


    default:
      return {
        mode: 'icon',
        component: ThumbUp,
        wrapperClass: 'bg-[#1877F2]',
        color: '#FFFFFF'
      }
  }
}


defineExpose({
  handleReaction
})
</script>
