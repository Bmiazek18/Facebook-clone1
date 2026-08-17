<template>
  <div class="flex h-screen overflow-hidden bg-theme-bg text-theme-text">
    <GroupsSidebar />

    <main class="flex-1 h-full mt-[56px] overflow-y-auto relative">
      <div class="max-w-[700px] mx-auto mt-3 px-4">
        <p class="text-theme-text-secondary">Ostatnia aktywnośćś</p>
        <PostItem v-for="post in posts" :key="post.id" :post="post" class="my-4" />
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import GroupsSidebar from '@/components/groups/GroupsSidebar.vue'
import PostItem from '@/components/feed/post/PostItem.vue'
import { computed } from 'vue'
import { usePostsStore } from '@/composables/feed/useAppState'

const postsStore = usePostsStore()
const posts = computed(() => postsStore.posts.filter((post) => post.targetType === 'Group'))
</script>
