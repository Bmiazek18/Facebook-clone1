<script setup lang="ts">
import { computed, ref } from 'vue'
import { useCommentsStore } from '@/stores/comments'
import HoverScrollbar from '@/components/common/HoverScrollbar.vue'
import EmptyState from '../comment/EmptyState.vue'
import CommentItem from '@/components/feed/comment/CommentItem.vue'
import PostItem from './PostItem.vue'
import type { Post } from '@/types/Post'
import CommentReplyInput from '@/components/feed/comment/CommentReplyInput.vue'
import CommentFilter from '@/components/profile/CommentFilter.vue'
import { getUserById } from '@/utils/users'
import { useLinkModal } from '@/composables/ui/useLinkModal'
import BaseModal from '../../common/BaseModal.vue'
import LinkModal from '../LinkModal.vue'
import { useComments } from '@/composables/feed/useComments'

// Import szkieletu komentarzy
import CommentsSkeleton from '@/components/common/CommentsSkeleton.vue'

const props = defineProps<{
  post: Post
}>()

const commentsStore = useCommentsStore()
const { isLinkModalVisible, linkModalData, showLinkModal, closeLinkModal } = useLinkModal()

const author = computed(() => getUserById(props.post.authorId))

const handleCommentReply = (event: { author: { id: number; name: string }; commentId: string }) => {
  if (commentsStore.activeReplyInput === event.commentId) {
    commentsStore.clearReplyingTo()
  } else {
    commentsStore.setReplyingTo(event.author, event.commentId)
  }
}

const handleOpenLinkModal = (url: string) => {
  showLinkModal(url)
}

const { fetchCommentsForPost } = useComments()
const isLoadingMore = ref(false)

const loadAllComments = async () => {
  isLoadingMore.value = true
  try {
    await fetchCommentsForPost(props.post)
  } finally {
    isLoadingMore.value = false
  }
}
</script>

<template>
  <div class="flex flex-col w-full lg:w-[700px] h-[90vh] lg:h-[90vh] bg-theme-bg-secondary overflow-hidden">

    <HoverScrollbar class="flex-1 min-h-0 w-full">
      <PostItem :post="props.post" :hide-close-button="true" :is-in-modal="true" :shouldPostActionVisible="false" />

      <div class="p-2 sm:p-3 md:p-4">

        <template v-if="true">
          <CommentsSkeleton />
        </template>

        <template v-else-if="props.post.comments.length > 0">
          <CommentFilter />

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
        </template>

        <template v-else>
          <EmptyState
            title="Brak komentarzy"
            description="Bądź pierwszą osobą, która skomentuje ten post."
          />
        </template>

      </div>
    </HoverScrollbar>

    <div class="p-2 sm:p-3 md:p-4 shrink-0 bg-theme-bg-secondary z-10">
      <CommentReplyInput :post-id="props.post.id" />
    </div>

    <BaseModal
      title="Informacje o tej zawartości"
      @close="closeLinkModal"
      v-if="isLinkModalVisible"
    >
      <LinkModal :targetUrl="linkModalData" />
    </BaseModal>

  </div>
</template>
