<script setup lang="ts">
import { computed,ref } from 'vue';
import { useRoute } from 'vue-router';
import CreateBox from '@/components/createPost/CreateBox.vue';
import PostItem from '@/components/feed/post/PostItem.vue';
import { usePostsStore } from '@/stores/posts';
import { useI18n } from 'vue-i18n';
import CommentFilter from '@/components/profile/CommentFilter.vue';
import type { Group } from '@/types/Group';

const { t } = useI18n();
const route = useRoute();
const postsStore = usePostsStore();

defineProps<{
  groupDetails?: Group;
  stickyTop?: number;
}>();

const groupPosts = computed(() => {
    const groupId = route.params.id as string;
    return postsStore.posts.filter(post => post.targetType === 'Group'&& post.targetId === groupId);
});

const handleDeletePost = (postId: string) => {
    postsStore.removePost(postId);
};
import GroupInfoSidebar from '@/components/groups/GroupInfoSidebar.vue';


const rightSectionRef = ref<HTMLDivElement | null>(null);
</script>

<template>
  <div class="grid grid-cols-1 lg:grid-cols-12 gap-4">
    <div class="lg:col-span-7 space-y-4">

        <CreateBox class="rounded-lg shadow-sm bg-theme-bg-secondary border border-theme-border" :target-id="route.params.id" target-type="Group" />
        <CommentFilter/>
        <PostItem
            v-for="post in groupPosts"
            :key="post.id"
            :post="post"
            isGroup
            class="rounded-lg shadow-sm bg-theme-bg-secondary border border-theme-border"
            @delete="handleDeletePost"
        />
        <div v-if="groupPosts.length === 0" class="text-center py-10 rounded-lg shadow-sm bg-theme-bg-secondary text-theme-text-secondary">
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
