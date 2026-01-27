<template>
  <div class="flex h-screen overflow-hidden bg-theme-bg text-theme-text">

    <aside class="w-[360px] h-full overflow-y-auto border-r border-gray-300 bg-white shrink-0 hidden lg:block">
      <GroupsSidebar />
    </aside>

    <main class="flex-1 h-full overflow-y-auto relative">
      <div class="max-w-[700px] mx-auto py-6 px-4">
        <PostItem v-for="post in posts" :key="post.id" :post="post" />
      </div>
    </main>

  </div>
</template>

<script setup lang="ts">
import GroupsSidebar from '@/components/groups/GroupsSidebar.vue';
import PostItem from '@/components/feed/post/PostItem.vue';
import { ref } from 'vue';
import type { Post } from '@/types/Post';

// Mock data import
import { posts as mockPosts } from '@/data/posts';

const posts = ref<Post[]>(mockPosts.filter(post => post.groupId));
</script>
