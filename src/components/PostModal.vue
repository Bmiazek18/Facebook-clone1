<script setup lang="ts">

import HoverScrollbar from './HoverScrollbar.vue'

import CommentItem from './CommentItem.vue'


import PostItem from './PostItem.vue'
import type { Post } from '@/types/Post';
import CommentReplyInput from './CommentReplyInput.vue'
import CommentFilter from './CommentFilter.vue';

const props = defineProps<{
    post: Post
}>()
</script>

<template>
    <div class="flex flex-col w-[700px] h-[90vh] bg-white dark:bg-[#242526] overflow-hidden">
 <HoverScrollbar  class="flex-1 min-h-0 w-full">


<PostItem :post="props.post" />
                <div class="p-3 sm:p-4">
                  <CommentFilter />
                    <CommentItem
                        v-for="comment in props.post.comments"
                        :key="comment.id"
                        :comment="comment"
                        :postAvatarSrc="props.post.authorAvatar"
                        :depth="0"
                        :postId="props.post.id"
                    />
                     <div class="h-4"></div>
                </div>

        </HoverScrollbar>

        <div class="p-3 sm:p-4 border-t border-theme-border shrink-0 bg-white dark:bg-[#242526] z-10">
           <CommentReplyInput
                :post-avatar-src="props.post.authorAvatar"
                placeholder="Napisz komentarz..."
                :post-id="props.post.id"
            />
        </div>

    </div>
</template>

<style scoped>

:deep(.force-scroll-height) > div {
    height: 100%;
}
</style>
