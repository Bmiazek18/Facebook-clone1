<template>
  <div
    class="w-full h-[calc(100vh-56px)] lg:max-w-122.5 flex flex-col lg:min-w-92.5 theme-bg-theme-secondary[displayReadBy-FINAL-DEBUG-AFTER] border-t lg:border-t-0 lg:border-l border-gray-200 h-full"
  >
    <div class="flex-1 flex flex-row-reverse overflow-hidden">
      <div class="hidden md:block w-22.5 border-l border-gray-200"></div>

      <div class="w-full">
        <HoverScrollbar class="grow overflow-y-auto">
          <!-- User info -->
          <div class="px-4 pt-4 pb-2">
            <div class="flex items-start justify-between">
              <div class="flex items-center gap-2.5">
                <img
                  class="rounded-full w-10 h-10 object-cover border border-gray-200 cursor-pointer hover:brightness-95"
                  :src="reelUser?.avatar"
                  :alt="$t('chat.avatar')"
                />
                <div class="flex flex-col">
                  <div
                    class="font-semibold text-[15px] text-gray-900 leading-5 cursor-pointer hover:underline"
                  >
                    {{ reelUser?.name }}
                  </div>
                  <div
                    v-if="reel.music"
                    class="pb-3 flex items-center gap-2 text-[13px] text-gray-600"
                  >
                    <MusicNote :size="16" />
                    <span>{{ reel.music }}</span>
                  </div>
                </div>
              </div>
              <button
                v-if="!props.reel.isFollowing"
                @click="handleFollowToggle"
                class="text-blue-600 hover:bg-blue-50 font-semibold px-3 py-1.5 rounded-md text-sm transition-colors"
              >{{ $t('feed.obserwuj2') }}</button>
              <button
                v-else
                @click="handleFollowToggle"
                class="text-gray-600 hover:bg-gray-100 rounded-full p-2 -mr-2 transition-colors"
              >
                <DotsHorizontal :size="20" fillColor="#65686C" />
              </button>
            </div>
          </div>

          <div
            class="px-4 pb-3 text-[15px] border-b border-gray-200 text-gray-800 whitespace-pre-wrap leading-relaxed"
          >
            <template v-for="(part, index) in processedCaption" :key="index">
              <span v-if="part.type === 'text'">{{ part.value }}</span>
              <NuxtLink
                v-else-if="part.type === 'hashtag'"
                :to="`/hashtag/${part.hashtag}`"
                class="text-blue-500 hover:underline"
              >
                {{ part.value }}
              </NuxtLink>
              <NuxtLink
                v-else-if="part.type === 'mention'"
                :to="`/profile/${part.userId}`"
                class="text-blue-500 hover:underline"
              >
                {{ part.value }}
              </NuxtLink>
              <a
                v-else-if="part.type === 'link'"
                :href="part.url"
                target="_blank"
                rel="noopener noreferrer"
                class="text-blue-500 hover:underline"
              >
                {{ part.value }}
              </a>
            </template>
          </div>

          <!-- Comment filter -->
          <div class="flex justify-between items-center px-4 pt-3 mb-2">
            <CommentFilter />
          </div>

          <!-- Comments section -->
          <div class="pt-1 px-4 pb-4">
            <EmptyState v-if="!hasComments" />
            <CommentItem
              v-else
              v-for="comment in reelComments"
              :key="comment.id"
              :comment="comment"
              :post-avatar-src="reelUser?.avatar"
              :depth="0"
              :post-id="reel.id"
              @reply="handleCommentReply"
              @open-link="handleOpenLinkModal"
            />
          </div>
        </HoverScrollbar>

        <div
          class="p-4 border-t border-theme-border flex items-center bg-theme-bg-secondary sticky bottom-13 z-10"
        >
          <CommentReplyInput :post-id="reel.id" />
        </div>
      </div>
    </div>
    <BaseModal
      :title="$t('birthday.informacjeOTejZawartosci')"
      @close="closeLinkModal"
      v-if="isLinkModalVisible"
    >
      <LinkModal :targetUrl="linkModalData" />
    </BaseModal>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { processContent, type ProcessedContent } from '@/utils/contentProcessor'
import type { Reel } from '@/types/Reel'
import DotsHorizontal from 'vue-material-design-icons/DotsHorizontal.vue'
import MusicNote from 'vue-material-design-icons/MusicNote.vue'
import ThumbUp from 'vue-material-design-icons/ThumbUp.vue'
import CommentTextMultiple from 'vue-material-design-icons/CommentTextMultiple.vue'
import Share from 'vue-material-design-icons/Share.vue'
import HoverScrollbar from '@/components/common/HoverScrollbar.vue'
import CommentFilter from '@/components/profile/CommentFilter.vue'
import { getUserById } from '@/utils/users'
import { useCommentsStore } from '@/stores/comments'
import CommentItem from '@/components/feed/comment/CommentItem.vue'
import CommentReplyInput from '../comment/CommentReplyInput.vue'
import { useLinkModal } from '@/composables/ui/useLinkModal'
import BaseModal from '@/components/common/BaseModal.vue'
import LinkModal from '@/components/feed/LinkModal.vue'
import EmptyState from '../comment/EmptyState.vue'
import { useComments } from '@/composables/feed/useComments'

const props = defineProps<{
  reel: Reel
}>()

const commentsStore = useCommentsStore()
const { fetchCommentsForPost } = useComments()
const { isLinkModalVisible, linkModalData, showLinkModal, closeLinkModal } = useLinkModal()

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

const commentInput = ref('')
const reelComments = computed(() => {
  return props.reel._originalPost?.comments || props.reel.comments || []
})
const hasComments = computed(() => reelComments.value.length > 0)

const reelUser = computed(() => getUserById(props.reel.authorId))

const processedCaption = computed(() => {
  return processContent(props.reel.caption)
})

const handleFollowToggle = () => {
  if (reelUser.value) {
    console.log('Toggle follow author:', reelUser.value.id)
  }
}

watch(
  () => props.reel,
  async (newReel) => {
    if (newReel && newReel._originalPost) {
      await fetchCommentsForPost(newReel._originalPost)
    }
  },
  { immediate: true }
)

const submitComment = () => {
  if (commentInput.value.trim()) {
    console.log('Comment:', commentInput.value)
    commentInput.value = ''
  }
}
</script>
