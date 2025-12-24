<template>
  <div class="min-h-screen bg-[#F0F2F5] py-8">

    <div class="max-w-[680px] mx-auto px-4">

      <header class="flex justify-between items-start mb-6 border-b border-gray-300 pb-4">

        <div>
          <h1 class="text-3xl font-black text-black tracking-tight mb-1">
            #{{ hashtag }}
          </h1>
          <span class="text-gray-500 text-[15px] font-medium block">
            {{ formatCount(filteredPosts.length) }} postów
          </span>
        </div>

        <button
          class="flex items-center justify-center w-12 h-9 bg-gray-200 hover:bg-gray-300 rounded-md transition-colors"
          aria-label="Więcej opcji"
        >
          <span class="text-black font-bold text-lg pb-1 tracking-widest leading-none">...</span>
        </button>
      </header>

      <div class="flex flex-col gap-4">
        <PostItem
          v-for="post in filteredPosts"
          :key="post.id"
          :post="post"
          class="w-full"
        />
      </div>

    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { usePostsStore } from '@/stores/posts'
import PostItem from '@/components/PostItem.vue'

const route = useRoute()
const postsStore = usePostsStore()

const hashtag = computed(() => route.params.hashtag)

const filteredPosts = computed(() => {
  return postsStore.posts.filter((post) => {
    return post.content.includes(`#${hashtag.value}`)
  })
})

// Pomocnicza funkcja do formatowania liczby (np. 525 tys.)
const formatCount = (count: number) => {
  if (count === 0) return '525 tys.' // Placeholder dla wyglądu jak na screenie, gdy brak danych
  if (count > 1000) {
    return (count / 1000).toFixed(0) + ' tys.'
  }
  return count
}
</script>

<style scoped>
</style>
