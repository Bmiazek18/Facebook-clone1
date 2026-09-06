<template>
  <div v-if="post" class="py-2 border border-theme-border rounded-2xl bg-white">
    <PostItem :post="post" />
    <div class="px-2 pb-2">
      <CommentItem
        v-for="comment in (post.comments || [])"
        :key="comment.id"
        :comment="comment"
        :postAutor="post.author?.name || ''"
        :depth="0"
        :postId="post.id"
        @react="handleCommentReaction"
        @reply="handleCommentReply"
        @open-link="handleOpenLinkModal"
      />

      <div class="flex items-start mt-2">
        <CommentReplyInput :postId="post.id" />
      </div>
    </div>

    <BaseModal
      :title="$t('birthday.informacjeOTejZawartosci')"
      @close="closeLinkModal"
      v-if="isLinkModalVisible"
    >
      <LinkModal :targetUrl="linkModalData || ''" />
    </BaseModal>
  </div>
</template>

<script setup lang="ts">
import CommentItem from '@/components/feed/comment/CommentItem.vue'
import type { Post } from '@/types/Post'
import CommentReplyInput from '@/components/feed/comment/CommentReplyInput.vue'
import { computed } from 'vue'
import { usePostsStore } from '@/stores/posts'
import { useAuthStore } from '@/stores/auth'
import { useCommentsStore } from '@/stores/comments'
import { getUserById } from '@/utils/users'
import { useLinkModal } from '@/composables/ui/useLinkModal'
import BaseModal from '@/components/common/BaseModal.vue'
import LinkModal from '@/components/feed/LinkModal.vue'
import PostItem from './feed/post/PostItem.vue'

const props = withDefaults(
  defineProps<{
    post?: Post
  }>(),
  {
    post: () => ({} as any),
  }
)

const postsStore = usePostsStore()
const authStore = useAuthStore()
const commentsStore = useCommentsStore()
const { isLinkModalVisible, linkModalData, showLinkModal, closeLinkModal } = useLinkModal()

const currentUserAvatar = computed(() => authStore.currentUser?.avatar || '/default-avatar.png')

const handleCommentReaction = (event: {
  commentId: string
  reaction: string | null
  oldReaction: string | null
}) => {
  // Comment reactions are handled inside CommentItem composable
}

const handleCommentReply = (event: { author: { id: number; name: string }; commentId: string }) => {
  if (commentsStore.activeReplyInput === Number(event.commentId)) {
    commentsStore.clearReplyingTo()
  } else {
    commentsStore.setReplyingTo(event.author, Number(event.commentId))
  }
}

const handleOpenLinkModal = (url: string) => {
  showLinkModal(url)
}

const author = computed(() => (props.post?.authorId ? getUserById(props.post.authorId) : null))
</script>

<style scoped>
/* Dodatkowe micro-style jeśli Tailwind nie wystarcza */
</style>
