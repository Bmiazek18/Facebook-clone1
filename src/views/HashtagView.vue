<template>
  <div class="min-h-screen bg-theme-bg py-8">
    <div class="max-w-[680px] mx-auto px-4">
      <header class="flex justify-between items-start mb-6 border-b border-theme-border pb-4">
        <div>
          <h1 class="text-3xl font-black text-theme-text tracking-tight mb-1">#{{ hashtag }}</h1>
          <span class="text-theme-text-secondary text-[15px] font-medium block">
            {{ formatCount(filteredPosts.length) }} {{ t('hashtag.posts') }}
          </span>
        </div>

        <button
          class="flex items-center justify-center w-12 h-9 bg-gray-200 dark:bg-gray-700 hover:bg-gray-300 dark:hover:bg-gray-600 rounded-md transition-colors"
          :aria-label="t('hashtag.moreOptions')"
        >
          <span class="text-theme-text font-bold text-lg pb-1 tracking-widest leading-none"
            >...</span
          >
        </button>
      </header>

      <div class="flex flex-col gap-4">
        <PostItem v-for="post in filteredPosts" :key="post.id" :post="post" class="w-full" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import PostItem from '@/components/feed/post/PostItem.vue'
import { useI18n } from 'vue-i18n'
import { useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useQuery } from '@vue/apollo-composable'
import { gql } from 'graphql-tag'

const { t } = useI18n()
const route = useRoute()
const authStore = useAuthStore()

const hashtag = computed(() => route.params.hashtag)

const GET_FEED_BY_HASHTAG = gql`
  query GetFeedByHashtag($currentUserId: ID!, $hashtag: String!) {
    getFeed(currentUserId: $currentUserId, hashtag: $hashtag) {
      id
      authorId
      author {
        id
        firstName
        lastName
        avatarId
        avatar
      }
      content
      date
      timestamp
      isAnonymous
      commentCount
      shareCount
      media {
        src
        altText
        backgroundColor
      }
      reactions {
        reactionType
        userIds
      }
    }
  }
`

const { result } = useQuery(GET_FEED_BY_HASHTAG, () => ({
  currentUserId: String(authStore.currentUserId),
  hashtag: String(hashtag.value)
}), {
  fetchPolicy: 'network-only'
})

const filteredPosts = computed(() => {
  const rawPosts = result.value?.getFeed ?? []
  return rawPosts.map((post: any) => {
    let formattedReactions: Record<string, number[]> = {}
    if (Array.isArray(post.reactions)) {
      post.reactions.forEach((r: any) => {
        formattedReactions[r.reactionType.toLowerCase()] = r.userIds.map(Number)
      })
    } else if (post.reactions) {
      formattedReactions = post.reactions
    }
    return {
      ...post,
      reactions: formattedReactions
    }
  })
})

const formatCount = (count: number) => {
  if (count === 0) return `525 ${t('hashtag.thousand')}`
  if (count > 1000) {
    return (count / 1000).toFixed(0) + ` ${t('hashtag.thousand')}`
  }
  return count
}
</script>

<style scoped></style>
