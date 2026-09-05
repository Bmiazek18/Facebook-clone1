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
import { computed, ref, onMounted, watch } from 'vue'
import PostItem from '@/components/feed/post/PostItem.vue'
import { useI18n } from 'vue-i18n'
import { useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { feedApi } from '@/api/feed'

const { t } = useI18n()
const route = useRoute()
const authStore = useAuthStore()

const hashtag = computed(() => route.params.hashtag)
const rawPosts = ref<any[]>([])

const fetchHashtagFeed = async () => {
  if (!hashtag.value) return
  try {
    const posts = await feedApi.getFeed(authStore.currentUserId, String(hashtag.value))
    rawPosts.value = posts || []
  } catch (err) {
    console.error('Failed to fetch hashtag feed:', err)
  }
}

onMounted(() => {
  fetchHashtagFeed()
})

watch(() => [hashtag.value, authStore.currentUserId], () => {
  fetchHashtagFeed()
})

const filteredPosts = computed(() => {
  return rawPosts.value.map((post: any) => {
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
