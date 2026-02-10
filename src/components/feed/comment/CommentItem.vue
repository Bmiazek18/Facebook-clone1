<script setup lang="ts">
import { ref, computed, } from 'vue'
import CommentReplyInput from './CommentReplyInput.vue'
import ReactionButton from '@/components/feed/ReactionButton.vue'
import ProfilePopper from '@/components/profile/ProfilePopper.vue'
import type { Comment } from '@/types/Post'
import { getUserById } from '@/data/users'
import { processContent } from '@/utils/contentProcessor'
import FormattedDate from '@/components/common/FormattedDate.vue'
import CommentContent from './CommentContent.vue'
import CommentLinkPreview from './CommentLinkPreview.vue'
import CommentReactions from './CommentReactions.vue'
import AuthorBadge from '../post/AuthorBadge.vue'
import { useRouter } from 'vue-router'
import { useCommentReactions } from '@/composables/useCommentReactions'

const props = defineProps<{
    comment: Comment
    postAutor: string
    depth: number,
    postId: string
}>()

const emit = defineEmits(['reply', 'open-link'])
const router = useRouter()

const author = computed(() => getUserById(props.comment.authorId));

const { userReaction, handleReaction } = useCommentReactions(props.postId, props.comment.id);

const totalLikes = computed(() => {
  if (!props.comment.reactions) return props.comment.likesCount;
  return Object.values(props.comment.reactions).reduce((sum, userIds) => sum + userIds.length, 0);
});




const showReplies = ref(false)
const isReplying = ref(false)



const hasReplies = computed(() => props.comment.replies && props.comment.replies.length > 0)

const showMoreReplies = () => {
    showReplies.value = true
}

const startReply = () => {
    if (props.comment.authorId === undefined) {
        console.error("Cannot reply to a comment without an authorId");
        return;
    }
    isReplying.value = !isReplying.value
    emit('reply', {
        author: { id: props.comment.authorId, name: author.value?.name || 'Unknown' },
        commentId: props.comment.id
    });
}

const handleCommentSubmitted = () => {
    isReplying.value = false
}

const viewCommentImage = ( commentId: number, postId: string) => {
  router.push({
    name: 'comment',
    params: {
      postId: postId,
      commentId: commentId
    },

  });
};

const processedContent = computed(() => {
  return processContent(props.comment.content);
});


const isRootComment = props.depth === 0


const isCommentActiveForReply = computed(() => {
    return isReplying.value;
});

const commentWrapperClass = computed(() => {
    return {
        'w-fit p-1 rounded-xl': props.comment.content.length > 0,
        'px-2': (props.comment.image || props.comment.gif),
        'bg-[#E7F3FF]': isCommentActiveForReply.value,
        'dark:bg-[rgba(24,119,242,0.31)]': isCommentActiveForReply.value,
        'bg-theme-comment-bg w-fit px-2 rounded-xl': !isCommentActiveForReply.value && props.comment.content.length > 0
    }
});
</script>
<template>


    <div :class="{ 'mt-4': isRootComment, 'mt-2': !isRootComment }">
        <div class="flex "
            :style="{ marginLeft: isRootComment ? '0px' : `-32px` }">
            <!-- Linia łącząca dla odpowiedzi -->
            <div v-if="!isRootComment" class="w-8 mr-2 relative flex-shrink-0">
                <div class="absolute h-4 border-b-2 border-l-2 border-theme-secondary right-0 rounded-bl-[10px]"
                     :class="props.depth > 1 ? 'w-[21px]' : 'w-[25px]'"></div>
            </div>

            <!-- Avatar -->
            <div class="relative ">
                <a :class="[isRootComment ? 'w-8 h-8' : 'w-6 h-6']">
                    <img
                        :class="['rounded-full mt-1', isRootComment ? 'w-8 h-8' : 'w-6 h-6']"
                        :src="author?.avatar"
                        :alt="(author?.name || 'User') + ' Avatar'"
                    >
                </a>

                <!-- Linia pionowa dla odpowiedzi -->
               <div
v-if="(showReplies || isReplying || hasReplies) && props.depth <2"
                    class="absolute top-10 left-1/2 -translate-x-1/2 w-0 border-l-2 border-theme-secondary"

                    :style="{ height:  (hasReplies&& (isReplying||!showReplies)) && (showReplies || !isReplying) &&(!showReplies|| !isReplying) ? 'calc(100% - 60px)' : 'calc(100% - 110px)' }"
                ></div>
            </div>
            <!-- Treść komentarza -->
            <div class="flex-grow ml-2">
                <div :class="commentWrapperClass">
                    <div class="flex flex-col ">
                        <AuthorBadge v-if="postAutor == author?.id"/>
                        <ProfilePopper :userId="author?.id" mention class="font-semibold"/>
                    </div>
                    <CommentContent :content="processedContent" />
                </div>

                <!-- Obrazek lub GIF -->
                <div
                    v-if="props.comment.image || props.comment.gif"
                    :class="{'mt-1': props.comment.content}"
                >
                    <img
                        :src="props.comment.image || props.comment.gif"
                        class="rounded-lg max-h-40 mt-1 cursor-pointer"
                        @click="viewCommentImage(props.comment.id, props.postId)"
                    />
                </div>

<CommentLinkPreview v-if="props.comment.linkPreview" :link-preview="props.comment.linkPreview"  />
                <!-- Akcje komentarza -->
                <div class="flex items-center space-x-2 mt-1 font-semibold text-[12px] text-theme-text-secondary">
                  <FormattedDate :date="props.comment.date" short/>
                    <ReactionButton @react="handleReaction" display="compact" :user-reaction="userReaction" />
                    <span
                        @click="startReply"
                        class="cursor-pointer hover:underline"
                    >
                        {{ $t('actions.reply') }}
                    </span>
                    <CommentReactions :comment="comment" :total-likes="totalLikes" :user-reaction="userReaction" />
                </div>

                <!-- Przycisk pokaż odpowiedzi -->
                <div v-if="hasReplies && !showReplies && props.depth <=2" class="flex mt-2 items-center" :class="isRootComment ? '-ml-[36px]' : '-ml-[32px]'" >
                    <div class="w-8 mr-2 relative">
                        <div class="absolute w-[21px] -mt-5 h-5 border-b-2 border-l-2 border-theme-secondary right-0 rounded-bl-[10px]"></div>
                    </div>
                    <button
                        @click="showMoreReplies"
                        class="flex items-center text-[15px] font-semibold text-theme-text-secondary  focus:outline-none"
                    >

                        <span>Wyswietl wszystkie {{ props.comment.replies?.length ?? 0 }} odpowiedzi</span>
                    </button>
                </div>

                <!-- Input odpowiedzi gdy nie ma pokazanych odpowiedzi -->
                <div v-if="isReplying && !showReplies &&props.depth <2" class="flex mt-2  items-start" :class=" showReplies|| props.depth ==0? 'ml-[-30px]': 'ml-[-26px]'">
                    <div class="w-8 mr-2 relative">
                        <div class="absolute h-5 border-b-2 border-l-2 border-theme-secondary right-0 rounded-bl-[10px]"
                             :class="props.depth > 1 ? 'w-[21px]' : 'w-[25px]'"></div>
                    </div>
                    <CommentReplyInput
                        @onCommentSubmitted="handleCommentSubmitted"
                        :postId="props.postId"
                        :parentId="props.comment.id"
                    />
                </div>

                <!-- Wyświetlanie odpowiedzi -->
                <div v-if="(showReplies && props.comment.replies && props.comment.replies.length > 0)" class="mt-2">
                    <CommentItem
                        v-for="reply in props.comment.replies"
                        :key="reply.id"
                        :comment="reply"
                        @reply="$emit('reply', $event)"
                        :depth="props.depth + 1"
                        :postId="props.postId"
                    />

                    <!-- Input odpowiedzi po pokazaniu odpowiedzi -->
                    <div class="flex mt-2 items-start" :class=" props.depth < 2? 'ml-[-30px]': 'ml-[-26px]'">
                        <div class="w-8 mr-2 relative">
                             <div class="absolute h-5 border-b-2 border-l-2 border-theme-secondary right-0 rounded-bl-[10px]"
       :class="props.depth == 1  ? 'w-[21px]' : 'w-[25px]'"></div>
                        </div>
                        <CommentReplyInput
                            @onCommentSubmitted="handleCommentSubmitted"
                            :postId="props.postId"
                            :parentId="props.comment.id"
                        />
                    </div>
                </div>
            </div>
        </div>
    </div>
    </template>
