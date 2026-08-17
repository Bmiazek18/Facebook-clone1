<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRoute } from 'vue-router'
import CreateBox from '@/components/create/createPost/CreateBox.vue'
import PostItem from '@/components/feed/post/PostItem.vue'
import { usePostsStore } from '@/composables/feed/useAppState'
import { useI18n } from 'vue-i18n'
import CommentFilter from '@/components/profile/CommentFilter.vue'
import type { Group } from '@/types/Group'

import { useQuery } from '@vue/apollo-composable'
import { gql } from 'graphql-tag'
import { watch } from 'vue'

const { t } = useI18n()
const route = useRoute()
const postsStore = usePostsStore()

defineProps<{
  groupDetails?: Group
  stickyTop?: number
}>()

import { GET_GROUP_FEED } from '@/graphql/groups'

const groupId = computed(() => route.params.id as string)
const { result: feedResult, refetch } = useQuery(GET_GROUP_FEED, () => ({
  groupId: groupId.value,
  limit: 20,
  offset: 0
}), {
  fetchPolicy: 'network-only'
})

const groupPosts = computed(() => {
  const queryPosts = feedResult.value?.getGroupFeed ?? []
  const localPosts = postsStore.posts.filter(
    (p) => p.targetType === 'Group' && String(p.targetId) === String(groupId.value)
  )

  const merged = [...localPosts]
  queryPosts.forEach((qp) => {
    if (!merged.some((mp) => String(mp.id) === String(qp.id))) {
      merged.push(qp)
    }
  })

  return merged.sort((a, b) => {
    const timeA = typeof a.timestamp === 'number' ? a.timestamp : new Date(a.timestamp).getTime()
    const timeB = typeof b.timestamp === 'number' ? b.timestamp : new Date(b.timestamp).getTime()
    return timeB - timeA
  })
})

watch(groupId, (newId) => {
  if (newId) {
    refetch({ groupId: newId, limit: 20, offset: 0 })
  }
})

const handleDeletePost = (postId: string) => {
  postsStore.removePost(postId)
}
import GroupInfoSidebar from '@/components/groups/GroupInfoSidebar.vue'

const rightSectionRef = ref<HTMLDivElement | null>(null)
</script>

<template>
  <div class="grid grid-cols-1 lg:grid-cols-12 gap-4">
    <div class="lg:col-span-7 space-y-4">
      <CreateBox
        class="rounded-lg shadow-sm bg-theme-bg-secondary border border-theme-border"
        :target-id="route.params.id as string"
        target-type="Group"
      />
      <CommentFilter />
      <PostItem
        v-for="post in groupPosts"
        :key="post.id"
        :post="post"
        isGroup
        class="rounded-lg shadow-sm bg-theme-bg-secondary border border-theme-border"
        @delete="handleDeletePost"
      />
      <div
        v-if="groupPosts.length === 0"
        class="text-center py-10 rounded-lg shadow-sm bg-theme-bg-secondary text-theme-text-secondary"
      >
        {{ t('groups.noPosts') }}
      </div>
    </div>
    <div
      ref="rightSectionRef"
      class="lg:col-span-5 space-y-4 mt-4 sticky self-start"
      :style="{ top: `${stickyTop}px` }"
    >
      <GroupInfoSidebar :group-details="groupDetails" />
    </div>
  </div>
</template>
