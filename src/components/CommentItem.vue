<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router' // New import
import ThumbUp from 'vue-material-design-icons/ThumbUp.vue'
import ChevronDown from 'vue-material-design-icons/ChevronDown.vue'
import Heart from 'vue-material-design-icons/Heart.vue' // Add Heart icon for 'love' reaction


import CommentReplyInput from './CommentReplyInput.vue'
import ProfilePopper from './ProfilePopper.vue'
import { usePostReactions } from '@/composables/usePostReactions' // Re-use usePostReactions for comments
import type { Comment } from '@/types/Post' // Import Comment interface

const props = defineProps<{
    comment: Comment
    postAvatarSrc: string
    depth: number,
    postId: string
}>()

const router = useRouter() // New router instance

const replyPlaceholder = "Napisz odpowiedź..."

const showReplyInput = ref(false)
const showReplies = ref(false)

const hasReplies = computed(() => props.comment.replies && props.comment.replies.length > 0)

const showMoreReplies = () => {
    showReplies.value = true
}
const toggleReplyInput = () => {
    showReplyInput.value = !showReplyInput.value

}

const handleCommentSubmitted = () => {
    showReplyInput.value = false;
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

const isRootComment = props.depth === 0
const isDepthTwo = props.depth === 1
const isDepthThreeOrMore = props.depth >= 2
const avatarSizeClass = isRootComment ? 'w-8 h-8' : 'w-6 h-6'

// --- Reactions for comments ---
// Use a unique ID for comment reactions (e.g., combining post ID and comment ID)
// For simplicity, using comment.id here directly.
const { userReaction, likesCount, handleReaction } = usePostReactions(props.comment.id)

// Konfiguracja wyglądu reakcji (copied from PostItem)
const getReactionConfig = (type: string) => {
  switch (type) {
    case 'like':
      return {
        mode: 'icon',
        component: ThumbUp,
        wrapperClass: 'bg-[#1877F2]',
        color: '#FFFFFF'
      }
    case 'love':
      return {
        mode: 'icon',
        component: Heart,
        wrapperClass: 'bg-[#F3425F]',
        color: '#FFFFFF'
      }
    case 'haha':
      return { mode: 'emoji', char: '😆', wrapperClass: 'bg-white dark:bg-[#242526]' }
    case 'wow':
      return { mode: 'emoji', char: '😮', wrapperClass: 'bg-white dark:bg-[#242526]' }
    case 'sad':
      return { mode: 'emoji', char: '😢', wrapperClass: 'bg-white dark:bg-[#242526]' }
    case 'angry':
      return { mode: 'emoji', char: '😡', wrapperClass: 'bg-white dark:bg-[#242526]' }
    default:
      return {
        mode: 'icon',
        component: ThumbUp,
        wrapperClass: 'bg-[#1877F2]',
        color: '#FFFFFF'
      }
  }
}

</script>

<template>
    <div
        :class="[ { 'mt-4 flex': isRootComment, 'mt-2': !isRootComment }]"
    >
        <div
            :class="[
                'flex',
                { 'flex-col items-center  relative': isRootComment },
                { 'mt-2 items-start -ml-7 relative': isDepthTwo } ,
                 { 'mt-2 items-start -ml-[33px] relative': isDepthThreeOrMore }
            ]"
        >
            <div v-if="!isRootComment" class="w-8 mr-2 relative">
                <div class="absolute w-[21px] h-4 border-b-2 border-l-2 border-gray-300 right-0 rounded-bl-[10px]"></div>
            </div>

<a :class="[avatarSizeClass]">
<img
                :class="['rounded-full mt-1', avatarSizeClass,  ]"
                :src="props.comment.authorAvatar"
                :alt="props.comment.authorName + ' Avatar'"
            >
            </a>



            <div
                v-if=" hasReplies"
                :class="[isRootComment? 'top-[40px]' : 'left-[50px] top-[35px]']"
                class="absolute   h-[calc(100%-100px)] bottom-0 w-0 border-l-2 border-gray-300"
            ></div>

            <div v-if="!isRootComment" class="flex-grow ml-2">
                 <div class="bg-gray-100 w-fit p-2 rounded-xl dark:bg-[#333333]">
                   <ProfilePopper :name="props.comment.authorName">
<span class="font-extrabold text-[13px] text-theme-text hover:underline cursor-pointer">{{ props.comment.authorName }}</span>
            </ProfilePopper>

                    <p class="text-[15px] text-theme-text">{{ props.comment.content }}</p>
                </div>
                <div v-if="props.comment.image || props.comment.gif"
                     :class="{'ml-2 mt-1': props.comment.content && (props.comment.image || props.comment.gif), 'bg-gray-100 w-fit p-2 rounded-xl dark:bg-[#333333]': !props.comment.content}">
                    <img :src="props.comment.image || props.comment.gif"
                         :class="{'rounded-lg max-h-40': !props.comment.content, 'max-h-40': props.comment.content}"
                         @click="viewCommentImage( props.comment.id, props.postId)"
                         class="cursor-pointer" />
                </div>
                 <div class="flex items-center ml-2 space-x-2 text-xs font-semibold text-gray-500 mt-1">
<span class="cursor-pointer hover:underline" @click="handleReaction('like')">{{ $t('reaction.like') }}</span>
                    <span
                        @click="toggleReplyInput"
                        class="cursor-pointer hover:underline"
                    >
                        {{ $t('actions.reply') }}
                    </span>
                    <span>{{ props.comment.date }}</span>
                    <div v-if="likesCount > 0" class="flex items-center ml-1">
                        <div
                            class="rounded-full p-0.5 flex items-center justify-center w-[18px] h-[18px]"
                            :class="getReactionConfig(userReaction || 'like').wrapperClass"
                        >
                            <component
                                v-if="getReactionConfig(userReaction || 'like').mode === 'icon'"
                                :is="getReactionConfig(userReaction || 'like').component"
                                :size="10"
                                :fillColor="getReactionConfig(userReaction || 'like').color"
                            />
                            <span v-else class="text-[10px]">{{ getReactionConfig(userReaction || 'like').char }}</span>
                        </div>
                        <span class="ml-1">{{ likesCount }}</span>
                    </div>
                </div>
<div v-if="showReplyInput" class="flex mt-2 items-start -ml-[33px]">
                   <div class="w-8 mr-2 relative">
                        <div class="absolute w-[21px] h-5 border-b-2 border-l-2 border-gray-300 right-0 rounded-bl-[10px]"></div>
                    </div>
                    <CommentReplyInput
                        @onCommentSubmitted="handleCommentSubmitted"
                        :postAvatarSrc="postAvatarSrc"
                        :placeholder="replyPlaceholder"
                        :postId="props.postId"
                        :parentId="props.comment.id"
                    />
                </div>
                <div v-if="hasReplies && !showReplies" class="flex mt-2 items-center -ml-9">
                    <div class="w-8 ml-[3px] relative">
                        <div class="absolute w-[21px] -mt-7 h-7 border-b-2 border-l-2 border-gray-300 right-0 rounded-bl-[10px]"></div>
                    </div>
                    <button
                        @click="showMoreReplies"
                        class="flex items-center ml-2 text-sm font-semibold text-blue-500 hover:text-blue-700 focus:outline-none"
                    >
                        <ChevronDown :size="18" class="mr-1 transition-transform" />
                        <span>Pokaż {{ props.comment.replies?.length ?? 0 }} odpowiedzi</span>
                    </button>
                </div>


                <div v-if="showReplies && props.comment.replies && props.comment.replies.length > 0" class="mt-2">
                    <CommentItem
                        v-for="subReply in props.comment.replies"
                        :key="subReply.id"
                        :comment="subReply"
                        :postAvatarSrc="postAvatarSrc"
                        :depth="props.depth + 1"
                        :postId="props.postId"
                    />
                   <div class="flex mt-2 items-start -ml-[33px]">
                       <div class="w-8 mr-2 relative">
                            <div class="absolute w-[21px]  h-5 border-b-2 border-l-2  border-gray-300 right-0 rounded-bl-[10px]"></div>
                        </div>
                        <!-- Using the extracted component -->
                        <CommentReplyInput
                            @onCommentSubmitted="handleCommentSubmitted"
                            :postAvatarSrc="postAvatarSrc"
                            :placeholder="replyPlaceholder"
                            :postId="props.postId"
                            :parentId="props.comment.id"
                        />
                    </div>
                </div>
             </div>
        </div>
        <div v-if="isRootComment" class="flex-grow">
            <div class="bg-gray-100 ml-2 w-fit dark:bg-[#333333] p-2 rounded-xl">
                 <ProfilePopper :name="props.comment.authorName">
<span class="font-extrabold text-[13px] text-theme-text hover:underline cursor-pointer">{{ props.comment.authorName }}</span>
            </ProfilePopper>
                <p class="text-[15px] text-theme-text">{{ props.comment.content }}</p>
            </div>
             <div v-if="props.comment.image || props.comment.gif"
                     :class="{'ml-2 mt-1': props.comment.content && (props.comment.image || props.comment.gif), 'bg-gray-100 ml-2 w-fit p-2 rounded-xl dark:bg-[#333333]': !props.comment.content}">
                    <img :src="props.comment.image || props.comment.gif"
                         :class="{'rounded-lg max-h-40': !props.comment.content, 'max-h-40': props.comment.content}"
                         @click="viewCommentImage( props.comment.id, props.postId)"
                         class="cursor-pointer" />
                </div>
            <div class="flex items-center ml-2 space-x-2 text-xs font-semibold text-gray-500 mt-1">
                <span class="cursor-pointer hover:underline" @click="handleReaction('like')">{{ $t('reaction.like') }}</span>
                  <span
                        @click="toggleReplyInput"
                        class="cursor-pointer hover:underline"
                    >
                        {{ $t('actions.reply') }}
                    </span>
                <span>{{ props.comment.date }}</span>
                <div v-if="likesCount > 0" class="flex items-center ml-1">
                    <div
                        class="rounded-full p-0.5 flex items-center justify-center w-[18px] h-[18px]"
                        :class="getReactionConfig(userReaction || 'like').wrapperClass"
                    >
                        <component
                            v-if="getReactionConfig(userReaction || 'like').mode === 'icon'"
                            :is="getReactionConfig(userReaction || 'like').component"
                            :size="10"
                            :fillColor="getReactionConfig(userReaction || 'like').color"
                        />
                        <span v-else class="text-[10px]">{{ getReactionConfig(userReaction || 'like').char }}</span>
                    </div>
                    <span class="ml-1">{{ likesCount }}</span>
                </div>
            </div>

            <div v-if="hasReplies && !showReplies" class="flex mt-2 items-center -ml-7">
                 <div class="w-8 mr-2 relative">
                    <div class="absolute w-[21px] -mt-5 h-5 border-b-2 border-l-2 border-gray-300 right-0 rounded-bl-[10px]"></div>
                </div>
                <button
                    @click="showMoreReplies"
                    class="flex items-center text-sm font-semibold text-blue-500 hover:text-blue-700 focus:outline-none"
                >
                    <ChevronDown :size="18" class="mr-1 transition-transform" />
                    <span>Pokaż {{ props.comment.replies?.length ?? 0 }} odpowiedzi</span>
                </button>
            </div>

                 <div  v-if="showReplyInput && !showReplies" class="flex mt-2 items-start -ml-7">
                   <div class="w-8 mr-2 relative">
                        <div class="absolute w-[21px] h-5 border-b-2 border-l-2 border-gray-300 right-0 rounded-bl-[10px]"></div>
                    </div>
                    <CommentReplyInput
                        @onCommentSubmitted="handleCommentSubmitted"
                        :postAvatarSrc="postAvatarSrc"
                        :placeholder="replyPlaceholder"
                        :postId="props.postId"
                        :parentId="props.comment.id"
                    />
                </div>
            <div v-if="showReplies && props.comment.replies && props.comment.replies.length > 0" class="mt-3">
                <CommentItem
                    v-for="reply in props.comment.replies"
                    :key="reply.id"
                    :comment="reply"
                    :postAvatarSrc="postAvatarSrc"
                    :depth="props.depth + 1"
                    :postId="props.postId"
                />
                <div   class="flex mt-2 items-start -ml-7">
                   <div class="w-8 mr-2 relative">
                        <div class="absolute w-[21px] h-5 border-b-2 border-l-2 border-gray-300 right-0 rounded-bl-[10px]"></div>
                    </div>
                    <CommentReplyInput
                        @onCommentSubmitted="handleCommentSubmitted"
                        :postAvatarSrc="postAvatarSrc"
                        :placeholder="replyPlaceholder"
                        :postId="props.postId"
                        :parentId="props.comment.id"
                    />
                </div>
            </div>
        </div>
    </div>
</template>
