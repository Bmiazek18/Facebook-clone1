<template>
    <div
        style="margin-top: 0 !important;"
        class="fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-80 overflow-y-auto"
    >
        <div
            class="bg-theme-bg-secondary shadow-2xl w-full relative flex h-full"
            :class="{
                'rounded-lg': !isFullScreen,
                'max-h-full': isFullScreen
            }"
        >
            <router-link
                to="/"
                class="absolute top-4 left-4 p-2 text-theme-text rounded-full hover:bg-white/10 z-50"
                :aria-label="$t('common.close')"
            >
                <Close :size="28" fillColor="#FFFFFF" />
            </router-link>

            <GalleryImageViewer

                v-model:is-full-screen="isFullScreen"
                :image="(currentComment ? { src: currentComment.image || '', altText: 'Comment Image' } : currentImage)"
                :has-prev="hasPrevImage"
                :has-next="hasNextImage"
                @prev="goToPrevImage"
                @next="goToNextImage"
            />

            <div
                v-if="!isFullScreen && (currentPost || currentComment)"
                class="w-full max-w-[490px] flex flex-col min-w-[370px] bg-theme-bg-secondary border-l border-gray-200"
            >
            <div class="w-full flex justify-end-safe py-2 px-5 border-b border-gray-200 ">
<NavbarRight/>
            </div>
<div class="flex-1 flex flex-row-reverse overflow-hidden ">
  <div class="w-[90px] border-l border-gray-200"></div>
<div class="w-full">
                <HoverScrollbar class=" grow overflow-y-auto">
                    <div  class=" px-4 pt-4 pb-2">
                        <div class="flex items-start justify-between">
                            <div class="flex items-center gap-2.5">
                                <img
                                    class="rounded-full w-10 h-10 object-cover border border-gray-200 cursor-pointer hover:brightness-95"
                                    :src="(currentComment || currentPost).authorAvatar"
                                    alt="Avatar"
                                >
                                <div class="flex flex-col">
                                    <div class="font-semibold text-[15px] text-theme-text leading-5 cursor-pointer hover:underline">
                                        {{ (currentComment || currentPost).authorName }}
                                    </div>
                                    <div class="flex items-center text-[13px] text-theme-text-secondary font-normal mt-0.5">
                                        <span class="hover:underline cursor-pointer">{{ (currentComment || currentPost).date }}</span>
                                        <span class="mx-1 font-bold">·</span>
                                        <Earth :size="13" fillColor="#65686C"/>
                                    </div>
                                </div>
                            </div>
                            <button class="text-theme-text-secondary hover:bg-gray-100 rounded-full p-2 -mr-2 transition-colors">
                                <DotsHorizontal :size="20" fillColor="#65686C" />
                            </button>
                        </div>
                    </div>

                    <div class="px-4 pb-3 text-[15px] text-theme-text whitespace-pre-wrap leading-relaxed">
                        {{ currentComment ? currentComment.content : currentPost.content }}
                    </div>

                    <div class="mx-4 flex items-center justify-between py-3 border-b border-gray-200 text-theme-text-secondary text-sm">
                        <div class="flex items-center">
                            <div class="bg-blue-500 rounded-full p-1 mr-1.5 flex items-center justify-center w-[18px] h-[18px]">
                                <ThumbUp fillColor="#FFFFFF" :size="10"/>
                            </div>
                            <span class="hover:underline cursor-pointer">{{ (currentPost || currentComment).likesCount }}</span>
                        </div>
                        <div class="flex items-center gap-3">
                             <span class="cursor-pointer hover:underline">{{ $t('comments.count', { count: (currentPost || currentComment).commentsCount }) }}</span>
                             <span class="cursor-pointer hover:underline">2 udostępnienia</span>
                        </div>
                    </div>

                    <div class="mx-4 py-1 border-b border-gray-200 flex items-center justify-between text-theme-text-secondary text-sm font-semibold">
                        <div class="flex-1 flex justify-center hover:bg-gray-100 rounded-md py-1.5 cursor-pointer transition-colors">
                            <div class="flex items-center gap-2">
                                <ReactionButton/>
                            </div>
                        </div>
                        <div class="flex-1 flex justify-center hover:bg-gray-100 rounded-md py-1.5 cursor-pointer transition-colors">
                            <div class="flex items-center gap-2">
                                <CommentTextMultiple :size="20" fillColor="#65686C" />
                                <span>{{ $t('actions.comment') }}</span>
                            </div>
                        </div>
                        <div class="flex-1 flex justify-center hover:bg-gray-100 rounded-md py-1.5 cursor-pointer transition-colors">
                            <div class="flex items-center gap-2">
                                <Share :size="20" fillColor="#65686C" />
                                <span>{{ $t('actions.send') }}</span>
                            </div>
                        </div>
                    </div>

                    <div v-if="!currentComment" class="flex justify-between items-center px-4 pt-3 mb-2">
                        <CommentFilter />
                    </div>

                    <div v-if="!currentComment" class="pt-1 px-4">
                          <CommentItem
                            v-for="comment in currentPost.comments"
                            :key="comment.id"
                            :comment="comment"
                            :post-avatar-src="(currentComment || currentPost).authorAvatar"
                            :depth="0"
                            :post-id="currentPost.id"
                        />
                    </div>
                </HoverScrollbar>

                <div v-if="!currentComment" class="p-4 border-t border-gray-200 flex items-center bg-theme-bg-secondary sticky bottom-0 z-10">
                    <CommentReplyInput
                        :post-avatar-src="(currentPost || currentComment).authorAvatar"
                        :placeholder="`Napisz komentarz jako ${(currentPost || currentComment).authorName}...`"
                        :post-id="currentPost.id"
                    />
                </div>
                </div>
                </div>
                </div>
            </div>
        </div>

</template>
<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import Close from 'vue-material-design-icons/Close.vue'
import ThumbUp from 'vue-material-design-icons/ThumbUp.vue'
import Earth from 'vue-material-design-icons/Earth.vue'
import DotsHorizontal from 'vue-material-design-icons/DotsHorizontal.vue'
import CommentTextMultiple from 'vue-material-design-icons/CommentTextMultiple.vue'
import Share from 'vue-material-design-icons/Share.vue'
import HoverScrollbar from '@/components/HoverScrollbar.vue'
import ReactionButton from '@/components/ReactionButton.vue'
import CommentItem from '@/components/CommentItem.vue'
import GalleryImageViewer from '@/components/gallery/GalleryImageViewer.vue'
import { usePostsStore } from '@/stores/posts';
import CommentReplyInput from '@/components/CommentReplyInput.vue'
import NavbarRight from '@/components/NavbarRight.vue'
import type {  Comment } from '@/types/Post';
import type { ImageTagType } from '@/types//ImageTag';
import CommentFilter from '@/components/CommentFilter.vue'
const route = useRoute()
const router = useRouter()
const postsStore = usePostsStore()

const isFullScreen = ref(false)

// Get postId and imageIndex from route params
const postId = computed(() => String(route.params.postId || route.query.postId))
const imageIndexParam = computed(() => Number(route.params.imageIndex) || 0)


const commentId = computed(() => String(route.params.commentId) || undefined);

const currentImageIndex = ref(imageIndexParam.value)

const currentComment = ref<Comment | null>(null); // New ref for the current comment

// Get the current post data
const currentPost = computed(() => postsStore.posts.find(p => p.id === postId.value))

// Current image URL
const currentImage = computed((): { src: string; altText?: string; tags?: ImageTagType[]; } | undefined => {

    return currentPost.value?.images[currentImageIndex.value]
})

// Navigation helpers
const hasPrevImage = computed(() => currentImageIndex.value > 0)
const hasNextImage = computed(() => {
    if (!currentPost.value) return false
    return currentImageIndex.value < (currentPost.value.images?.length || 0) - 1
})

const goToPrevImage = () => {
    if (hasPrevImage.value) {
        currentImageIndex.value--
        router.replace({ params: { postId: postId.value, imageIndex: currentImageIndex.value } })
    }
}

const goToNextImage = () => {
    if (hasNextImage.value) {
        currentImageIndex.value++
        router.replace({ params: { postId: postId.value, imageIndex: currentImageIndex.value } })
    }
}

const findCommentById = (comments: Comment[] | undefined, idToFind: number): Comment | null => {
  if (!comments || isNaN(idToFind)) return null;
  for (const comment of comments) {
    if (comment.id === idToFind) {
      return comment;
    }
    if (comment.replies && comment.replies.length > 0) {
      const found = findCommentById(comment.replies, idToFind);
      if (found) {
        return found;
      }
    }
  }
  return null;
};

// Watch route params changes
watch(() => route.params.imageIndex, (newIndex) => {
    currentImageIndex.value = Number(newIndex) || 0
})

// Watch for commentId and currentPost changes to populate currentComment
watch([commentId, currentPost], ([newCommentId, newCurrentPost]) => {
    if (newCommentId) { // Only proceed if a commentId is provided
        const idToSearch = Number(commentId.value);
        if (!isNaN(idToSearch)) {
            if (newCurrentPost) {
                // Search for the comment within the current post's comments
                currentComment.value = findCommentById(newCurrentPost.comments || [], idToSearch);
                if (!currentComment.value) {
                    // Comment ID provided but not found in post, redirect to post view
                    router.replace({ params: { postId: postId.value, imageIndex: currentImageIndex.value } });
                }
            } else {
                // Comment ID provided but no post found, redirect to clear commentId
                router.replace({ params: { postId: postId.value, imageIndex: currentImageIndex.value } });
                currentComment.value = null;
            }
        } else {
            // Comment ID is invalid (NaN), redirect to post view
            router.replace({ params: { postId: postId.value, imageIndex: currentImageIndex.value } });
            currentComment.value = null;
        }
    } else {
        // No commentId provided, so no specific comment to find
        currentComment.value = null;
    }
}, { immediate: true });

</script>
