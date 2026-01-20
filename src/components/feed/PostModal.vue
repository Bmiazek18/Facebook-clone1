<script setup lang="ts">

import HoverScrollbar from '@/components/common/HoverScrollbar.vue'

import CommentItem from '@/components/feed/CommentItem.vue'


import PostItem from './post/PostItem.vue'
import type { Post } from '@/types/Post';
import CommentReplyInput from '@/components/feed/CommentReplyInput.vue'
import CommentFilter from '@/components/feed/CommentFilter.vue';
import { getUserById } from '@/data/users';
import { computed } from 'vue';

const props = defineProps<{
    post: Post
}>()

const author = computed(() => getUserById(props.post.authorId));
</script>

<template>
    <div class="flex flex-col w-full lg:w-[700px] h-[90vh] lg:h-[90vh] bg-white dark:bg-[#242526] overflow-hidden">
 <HoverScrollbar  class="flex-1 min-h-0 w-full">


<PostItem :post="props.post" />
                <div class="p-2 sm:p-3 md:p-4">
                  <CommentFilter />
                    <CommentItem
                        v-for="comment in props.post.comments"
                        :key="comment.id"
                        :comment="comment"
                        :postAvatarSrc="author?.avatar"
                        :depth="0"
                        :postId="props.post.id"
                    />
                     <div class="h-4"></div>
                </div>

        </HoverScrollbar>

        <div class="p-2 sm:p-3 md:p-4 border-t border-theme-border shrink-0 bg-white dark:bg-[#242526] z-10">
           <CommentReplyInput
                :post-avatar-src="author?.avatar"
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
