<template>
  <div class="px-3 py-2 border border-theme-border rounded-2xl bg-white">
    <PostItem :post/>

    <CommentItem
             v-for="comment in post.comments"
             :key="comment.id"
             :comment="comment"
             :postAvatarSrc="currentUserAvatar"
             :depth="0"
             :postId="post.id"
             @react="handleCommentReaction"
             @reply="handleCommentReply"
             @open-link="handleOpenLinkModal"
        />

    <div class="flex items-start mt-2">
         <CommentReplyInput :postId="post.id" />
    </div>
    <BaseModal title="Informacje o tej zawartości"  @close="closeLinkModal" v-if="isLinkModalVisible">
      <LinkModal :targetUrl="linkModalData" />
    </BaseModal>
  </div>
</template>

<script setup lang="ts">

import CommentItem from '@/components/feed/CommentItem.vue';
import type { Post } from '@/types/Post';
import CommentReplyInput from '@/components/feed/CommentReplyInput.vue';
import { computed } from 'vue';
import { usePostsStore } from '@/stores/posts';
import { useCommentsStore } from '@/stores/comments';
import { getUserById } from '@/data/users';
import { useLinkModal } from '@/composables/useLinkModal';
import BaseModal from '@/components/common/BaseModal.vue';
import LinkModal from '@/components/feed/LinkModal.vue';
import PostItem from './feed/post/PostItem.vue';

const currentUserAvatar = "https://i.pravatar.cc/150?u=me";

const props = defineProps<{
  post: Post;
}>();

const postsStore = usePostsStore();
const commentsStore = useCommentsStore();
const { isLinkModalVisible, linkModalData, showLinkModal, closeLinkModal } = useLinkModal();

const handleCommentReaction = (event: { commentId: string; reaction: string | null; oldReaction: string | null }) => {
    postsStore.handleCommentReaction(props.post.id, event.commentId, event.reaction, event.oldReaction)
}

const handleCommentReply = (event: { author: { id: number, name: string }, commentId: string }) => {
    if (commentsStore.activeReplyInput === event.commentId) {
        commentsStore.clearReplyingTo()
    } else {
        commentsStore.setReplyingTo(event.author, event.commentId)
    }
}

const handleOpenLinkModal = (url: string) => {
    showLinkModal(url)
}

const author = computed(() => getUserById(props.post.authorId));
</script>

<style scoped>
/* Dodatkowe micro-style jeśli Tailwind nie wystarcza */
</style>
