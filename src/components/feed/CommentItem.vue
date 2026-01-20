<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import ThumbUp from 'vue-material-design-icons/ThumbUp.vue'
import ChevronDown from 'vue-material-design-icons/ChevronDown.vue'
import Heart from 'vue-material-design-icons/Heart.vue'


import CommentReplyInput from '@/components/feed/CommentReplyInput.vue'
import ReactionButton from '@/components/feed/ReactionButton.vue'
import ProfilePopper from '@/components/profile/ProfilePopper.vue'
// Removed incompatible usePostReactions
import type { Comment } from '@/types/Post'
import { useCommentsStore } from '@/stores/comments'
import { usePostsStore } from '@/stores/posts'
import { getUserById } from '@/data/users'
import { processContent } from '@/utils/contentProcessor'
import { Dropdown as VDropdown } from 'floating-vue'
import { reactionIcons } from '@/composables/usePostReactions'

const props = defineProps<{
    comment: Comment
    postAvatarSrc: string
    depth: number,
    postId: string
}>()

const router = useRouter()
const commentsStore = useCommentsStore()
const postsStore = usePostsStore()

const author = computed(() => getUserById(props.comment.authorId));

const userReaction = computed(() => props.comment.userReaction || null); // Assuming updated via store prop

const handleReaction = (reaction: string | null) => {
    const oldReaction = userReaction.value;
    // Optimistic update should be handled by store/parent if possible, or just emit
    postsStore.handleCommentReaction(props.postId, props.comment.id, reaction, oldReaction);
};

const totalLikes = computed(() => {
  if (!props.comment.reactions) return props.comment.likesCount;
  return Object.values(props.comment.reactions).reduce((sum, userIds) => sum + userIds.length, 0);
});


const replyPlaceholder = "Napisz odpowiedź..."

const showReplies = ref(false)

const showReplyInput = computed(() => commentsStore.activeReplyInput === props.comment.id)

const hasReplies = computed(() => props.comment.replies && props.comment.replies.length > 0)

const showMoreReplies = () => {
    showReplies.value = true
}

const startReply = () => {
    if (props.comment.authorId === undefined) {
        console.error("Cannot reply to a comment without an authorId");
        return;
    }
    if (commentsStore.activeReplyInput === props.comment.id) {
        commentsStore.clearReplyingTo()
    } else {
        commentsStore.setReplyingTo(
            { id: props.comment.authorId, name: author.value?.name || 'Unknown' },
            props.comment.id
        )
    }
}

const handleCommentSubmitted = () => {
    commentsStore.clearReplyingTo()
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
    <div :class="{ 'mt-4': isRootComment, 'mt-2': !isRootComment }">
        <div class="flex -ml-9 "
            :style="{ marginLeft: isRootComment ? '0px' : `-36px` }">
            <!-- Linia łącząca dla odpowiedzi -->
            <div v-if="!isRootComment" class="w-8 mr-2 relative flex-shrink-0">
                <div class="absolute -ml-10 w-[21px] h-4 border-b-2 border-l-2 border-gray-300 right-0 rounded-bl-[10px]"></div>
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
v-if="showReplies || showReplyInput || hasReplies"
                    class="absolute top-10 left-1/2 -translate-x-1/2 w-0 border-l-2 border-gray-300"

                    :style="{ height:  hasReplies && !showReplies ? 'calc(100% - 60px)' : 'calc(100% - 110px)' }"
                ></div>
            </div>

            <!-- Treść komentarza -->
            <div class="flex-grow ml-2">
                <div
                    :class="{
                        'bg-gray-100 w-fit p-2 rounded-xl dark:bg-[#333333]': !(props.comment.image || props.comment.gif),
                        'p-2': (props.comment.image || props.comment.gif)
                    }"
                >
                    <ProfilePopper :name="author?.name || 'Unknown'">
                        <span
                            :class="[
                                'text-theme-text hover:underline cursor-pointer block leading-4 mb-0.5',
                                isRootComment ? 'font-extrabold text-[13px]' : 'font-semibold text-[12px]'
                            ]"
                        >
                            {{ author?.name || 'Unknown' }}
                        </span>
                    </ProfilePopper>

                    <p class="text-[15px] text-theme-text">
                        <template v-for="(part, index) in processedContent" :key="index">
                            <router-link
                                v-if="part.type === 'mention'"
                                :to="{ name: 'userProfile', params: { userId: part.userId } }"
                                class="text-blue-500 hover:underline font-semibold"
                            >
                                {{ getUserById(parseInt(part.userId || ''))?.name }}
                            </router-link>
                            <router-link
                                v-else-if="part.type === 'hashtag'"
                                :to="{ name: 'hashtag', params: { hashtag: part.hashtag } }"
                                class="text-blue-500 hover:underline"
                            >
                                {{ part.value }}
                            </router-link>
                            <span v-else>{{ part.value }}</span>
                        </template>
                    </p>
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

                <!-- Akcje komentarza -->
                <div class="flex items-center space-x-2 text-xs font-semibold text-gray-500 mt-1">
                    <ReactionButton @react="handleReaction" display="compact" :userReaction="userReaction" />
                    <span
                        @click="startReply"
                        class="cursor-pointer hover:underline"
                    >
                        {{ $t('actions.reply') }}
                    </span>
                    <span>{{ props.comment.date }}</span>



                    <!-- Licznik reakcji -->
                    <VDropdown v-if="totalLikes > 0" placement="top-start" :distance="5">
                        <div class="flex items-center ml-1 cursor-pointer">
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
                            <span class="ml-1">{{ totalLikes }}</span>
                        </div>
                        <template #popper>
                            <div class="bg-white dark:bg-gray-800 shadow-lg rounded-lg p-2">
                                <template v-if="totalLikes < 5">
                                    <div v-for="(userIds, reaction) in comment.reactions" :key="reaction">
                                        <div v-if="userIds.length > 0" class="flex items-center gap-2 p-1">
                                            <span class="text-lg">{{ reactionIcons[reaction]?.emoji }}</span>
                                            <div class="text-sm">
                                                <div v-for="userId in userIds" :key="userId">
                                                    {{ getUserById(userId)?.name }}
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </template>
                                <template v-else>
                                    <div class="flex items-center gap-2">
                                        <div v-for="(userIds, reaction) in comment.reactions" :key="reaction">
                                            <div v-if="userIds.length > 0" class="flex items-center gap-1">
                                                <img :src="reactionIcons[reaction]?.src" class="w-4 h-4" />
                                                <span class="text-sm font-bold">{{ userIds.length }}</span>
                                            </div>
                                        </div>
                                    </div>
                                </template>
                            </div>
                        </template>
                    </VDropdown>
                </div>

                <!-- Przycisk pokaż odpowiedzi -->
                <div v-if="hasReplies && !showReplies" class="flex mt-2 items-center -ml-[36px]">
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

                <!-- Input odpowiedzi gdy nie ma pokazanych odpowiedzi -->
                <div v-if="showReplyInput && !showReplies" class="flex mt-2 ml-[-30px] items-start">
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

                <!-- Wyświetlanie odpowiedzi -->
                <div v-if="showReplies && props.comment.replies && props.comment.replies.length > 0" class="mt-2">
                    <CommentItem
                        v-for="reply in props.comment.replies"
                        :key="reply.id"
                        :comment="reply"
                        :postAvatarSrc="postAvatarSrc"
                        :depth="props.depth + 1"
                        :postId="props.postId"
                    />

                    <!-- Input odpowiedzi po pokazaniu odpowiedzi -->
                    <div class="flex mt-2 items-start" :style="{ marginLeft: `-${  33 + 1 * (props.depth > 1 ? 10 : 1) }px` }">
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
    </div>
</template>
