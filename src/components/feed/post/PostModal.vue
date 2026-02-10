<script setup lang="ts">
import { computed } from 'vue'
import { usePostsStore } from '@/stores/posts'
import { useCommentsStore } from '@/stores/comments'
import HoverScrollbar from '@/components/common/HoverScrollbar.vue'
import EmptyState from '../comment/EmptyState.vue'
import CommentItem from '@/components/feed/comment/CommentItem.vue'
import PostItem from './PostItem.vue'
import type { Post } from '@/types/Post';
import CommentReplyInput from '@/components/feed/comment/CommentReplyInput.vue'
import CommentFilter from '@/components/profile/CommentFilter.vue';
import { getUserById } from '@/data/users';
import { useLinkModal } from '@/composables/useLinkModal';
import BaseModal from '../../common/BaseModal.vue';
import LinkModal from '../LinkModal.vue';

const props = defineProps<{
    post: Post
}>()

const postsStore = usePostsStore()
const commentsStore = useCommentsStore()
const { isLinkModalVisible, linkModalData, showLinkModal, closeLinkModal } = useLinkModal()

const author = computed(() => getUserById(props.post.authorId));

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
</script>

<template>
  <div class="flex flex-col w-full lg:w-[700px] h-[90vh] lg:h-[90vh] bg-theme-bg-secondary overflow-hidden">

    <HoverScrollbar class="flex-1 min-h-0 w-full">
      <PostItem :post="props.post" />

      <div class="p-2 sm:p-3 md:p-4">
        <CommentFilter />

        <div v-if="props.post.comments && props.post.comments.length > 0">
          <CommentItem
            v-for="comment in props.post.comments"
            :key="comment.id"
            :comment="comment"
            :postAutor="author?.id"
            :depth="0"
            :postId="props.post.id"
            @reply="handleCommentReply"
            @open-link="handleOpenLinkModal"
          />
          <div class="h-4"></div>
        </div>

        <EmptyState
          v-else
          title="Brak komentarzy"
          description="Bądź pierwszą osobą, która skomentuje ten post."
        />
      </div> </HoverScrollbar>

    <div class="p-2 sm:p-3 md:p-4 border-t border-theme-border shrink-0 bg-theme-bg-secondary z-10">
      <CommentReplyInput
        :post-id="props.post.id"
      />
    </div>
      <BaseModal title="Informacje o tej zawartości"  @close="closeLinkModal" v-if="isLinkModalVisible">
        <LinkModal :targetUrl="linkModalData" />
      </BaseModal>
  </div>
</template>
