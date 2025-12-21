<script setup lang="ts">
import { ref, computed } from 'vue'
import ThumbUp from 'vue-material-design-icons/ThumbUp.vue'
import ChevronDown from 'vue-material-design-icons/ChevronDown.vue'

import CommentReplyInput from './CommentReplyInput.vue'

export interface Comment {
    id: number
    userName: string
    text: string
    date: string
    likesCount: number
    avatarSrc: string
    replies: Comment[]
}

import CommentItem from './CommentItem.vue'
import ProfilePopper from './ProfilePopper.vue'

const props = defineProps<{
    comment: Comment
    postAvatarSrc: string
    depth: number
}>()

const replyPlaceholder = "Napisz odpowiedź..."

const showReplyInput = ref(false)
const showReplies = ref(false)

const hasReplies = computed(() => props.comment.replies.length > 0)

const showMoreReplies = () => {
    showReplies.value = true
}
const toggleReplyInput = () => {
    showReplyInput.value = !showReplyInput.value

}

const isRootComment = props.depth === 0
const isDepthTwo = props.depth === 1
const isDepthThreeOrMore = props.depth >= 2
const avatarSizeClass = isRootComment ? 'w-8 h-8' : 'w-6 h-6'

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
            <ProfilePopper :name="props.comment.userName">
<a :class="[avatarSizeClass]">
<img
                :class="['rounded-full mt-1', avatarSizeClass,  ]"
                :src="props.comment.avatarSrc"
                :alt="props.comment.userName + ' Avatar'"
            >
            </a>
            </ProfilePopper>


            <div
                v-if=" props.comment.replies.length > 0"
                :class="[isRootComment? 'top-[40px]' : 'left-[50px] top-[35px]']"
                class="absolute   h-[calc(100%-70px)] bottom-0 w-0 border-l-2 border-gray-300"
            ></div>

            <div v-if="!isRootComment" class="flex-grow ml-2">
                 <div class="bg-gray-100 w-fit p-2 rounded-xl dark:bg-[#333333]">
                   <ProfilePopper :name="props.comment.userName">
<span class="font-extrabold text-[13px] text-theme-text hover:underline cursor-pointer">{{ props.comment.userName }}</span>
            </ProfilePopper>

                    <p class="text-[15px] text-theme-text">{{ props.comment.text }}</p>
                </div>
                 <div class="flex items-center ml-2 space-x-2 text-xs font-semibold text-gray-500 mt-1">
                    <span class="cursor-pointer hover:underline">{{ $t('reaction.like') }}</span>
                    <span
                        @click="toggleReplyInput"
                        class="cursor-pointer hover:underline"
                    >
                        {{ $t('actions.reply') }}
                    </span>
                    <span>{{ props.comment.date }}</span>
                    <div v-if="props.comment.likesCount > 0" class="flex items-center ml-1">
                        <ThumbUp fillColor="#1D72E2" :size="12" class="mt-[-2px]"/>
                        <span>{{ props.comment.likesCount }}</span>
                    </div>
                </div>
<div v-if="showReplyInput" class="flex mt-2 items-start -ml-[33px]">
                   <div class="w-8 mr-2 relative">
                        <div class="absolute w-[21px] h-5 border-b-2 border-l-2 border-gray-300 right-0 rounded-bl-[10px]"></div>
                    </div>
                    <CommentReplyInput
                        :postAvatarSrc="postAvatarSrc"
                        :placeholder="replyPlaceholder"
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
                        <span>Pokaż {{ props.comment.replies.length }} odpowiedzi</span>
                    </button>
                </div>


                <div v-if="showReplies && props.comment.replies.length > 0" class="mt-2">
                    <CommentItem
                        v-for="subReply in props.comment.replies"
                        :key="subReply.id"
                        :comment="subReply"
                        :postAvatarSrc="postAvatarSrc"
                        :depth="props.depth + 1"
                    />
                   <div class="flex mt-2 items-start -ml-[33px]">
                       <div class="w-8 mr-2 relative">
                            <div class="absolute w-[21px]  h-5 border-b-2 border-l-2  border-gray-300 right-0 rounded-bl-[10px]"></div>
                        </div>
                        <!-- Using the extracted component -->
                        <CommentReplyInput
                            :postAvatarSrc="postAvatarSrc"
                            :placeholder="replyPlaceholder"
                        />
                    </div>
                </div>
             </div>
        </div>
        <div v-if="isRootComment" class="flex-grow">
            <div class="bg-gray-100 ml-2 w-fit dark:bg-[#333333] p-2 rounded-xl">
                 <ProfilePopper :name="props.comment.userName">
<span class="font-extrabold text-[13px] text-theme-text hover:underline cursor-pointer">{{ props.comment.userName }}</span>
            </ProfilePopper>
                <p class="text-[15px] text-theme-text">{{ props.comment.text }}</p>
            </div>
            <div class="flex items-center ml-2 space-x-2 text-xs font-semibold text-gray-500 mt-1">
                <span class="cursor-pointer hover:underline">{{ $t('reaction.like') }}</span>
                  <span
                        @click="toggleReplyInput"
                        class="cursor-pointer hover:underline"
                    >
                        {{ $t('actions.reply') }}
                    </span>
                <span>{{ props.comment.date }}</span>
                <div v-if="props.comment.likesCount > 0" class="flex items-center ml-1">
                    <ThumbUp fillColor="#1D72E2" :size="12" class="mt-[-2px]"/>
                    <span>{{ props.comment.likesCount }}</span>
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
                    <span>Pokaż {{ props.comment.replies.length }} odpowiedzi</span>
                </button>
            </div>

                 <div  v-if="showReplyInput && !showReplies" class="flex mt-2 items-start -ml-7">
                   <div class="w-8 mr-2 relative">
                        <div class="absolute w-[21px] h-5 border-b-2 border-l-2 border-gray-300 right-0 rounded-bl-[10px]"></div>
                    </div>
                    <CommentReplyInput
                        :postAvatarSrc="postAvatarSrc"
                        :placeholder="replyPlaceholder"
                    />
                </div>
            <div v-if="showReplies && props.comment.replies.length > 0" class="mt-3">
                <CommentItem
                    v-for="reply in props.comment.replies"
                    :key="reply.id"
                    :comment="reply"
                    :postAvatarSrc="postAvatarSrc"
                    :depth="props.depth + 1"
                />
                <div   class="flex mt-2 items-start -ml-7">
                   <div class="w-8 mr-2 relative">
                        <div class="absolute w-[21px] h-5 border-b-2 border-l-2 border-gray-300 right-0 rounded-bl-[10px]"></div>
                    </div>
                    <CommentReplyInput
                        :postAvatarSrc="postAvatarSrc"
                        :placeholder="replyPlaceholder"
                    />
                </div>
            </div>
        </div>
    </div>
</template>
