<template>
  <div class="p-4 bg-white">
    <div class="flex items-start justify-between mb-2">
      <div class="flex items-center">
        <div class="relative">
             <img
            :src="post.authorAvatar"
            class="w-10 h-10 rounded-full object-cover border border-gray-200 cursor-pointer"
            />
            </div>

        <div class="ml-2.5">
          <div class="text-[15px] font-semibold text-[#050505] leading-tight flex flex-wrap items-center">
            <span class="hover:underline cursor-pointer">{{ post.authorName }}</span>
            <span class="mx-1 text-gray-500 font-normal text-xs">▶</span>
            <span class="hover:underline cursor-pointer">Bartosz Miazek</span>
          </div>
          <div class="text-[13px] text-gray-500 flex items-center hover:underline cursor-pointer">
            {{ post.date }}
            <span class="mx-1">·</span>
            <svg viewBox="0 0 16 16" width="12" height="12" fill="currentColor" class="text-gray-500" title="Znajomi">
                <g fill-rule="evenodd" transform="translate(-448 -544)">
                    <g fill-rule="nonzero">
                        <path d="M455.5 555.5c-.828 0-1.5-.672-1.5-1.5s.672-1.5 1.5-1.5 1.5.672 1.5 1.5-.672 1.5-1.5 1.5zm0-4c-1.38 0-2.5 1.12-2.5 2.5s1.12 2.5 2.5 2.5 2.5-1.12 2.5-2.5-1.12-2.5-2.5-2.5zm4 0c-.828 0-1.5.672-1.5 1.5s.672 1.5 1.5 1.5 1.5-.672 1.5-1.5-.672-1.5-1.5-1.5zm0-1c-1.38 0-2.5 1.12-2.5 2.5s1.12 2.5 2.5 2.5 2.5-1.12 2.5-2.5-1.12-2.5-2.5-2.5zm-8 1c-.828 0-1.5.672-1.5 1.5s.672 1.5 1.5 1.5 1.5-.672 1.5-1.5-.672-1.5-1.5-1.5zm0-1c-1.38 0-2.5 1.12-2.5 2.5s1.12 2.5 2.5 2.5 2.5-1.12 2.5-2.5-1.12-2.5-2.5-2.5z"></path>
                        <path d="M458.5 557.5h-1c-.276 0-.5.224-.5.5s.224.5.5.5h1c1.103 0 2 .897 2 2v.5c0 .276.224.5.5.5s.5-.224.5-.5v-.5c0-1.654-1.346-3-3-3zm-9 0h1c.276 0 .5.224.5.5s-.224.5-.5.5h-1c-1.103 0-2 .897-2 2v.5c0 .276-.224.5-.5.5s-.5-.224-.5-.5v-.5c0-1.654 1.346-3 3-3zm4.5-1h-1c-1.654 0-3 1.346-3 3v.5c0 .276.224.5.5.5s.5-.224.5-.5v-.5c0-1.103.897-2 2-2h1c1.103 0 2 .897 2 2v.5c0 .276.224.5.5.5s.5-.224.5-.5v-.5c0-1.654-1.346-3-3-3z"></path>
                    </g>
                </g>
            </svg>
          </div>
        </div>
      </div>
      <div class="hover:bg-gray-100 p-2 rounded-full cursor-pointer transition-colors -mr-2">
         <DotsHorizontalIcon class="text-gray-500" :size="20" />
      </div>
    </div>

    <div class="text-[15px] text-[#050505] mb-2 leading-normal">
      {{ post.content }}
    </div>

    <div v-if="post.reactionCount > 0 || post.commentCount > 0" class="flex items-center justify-between text-[13px] text-gray-500 py-2.5">
        <div class="flex items-center">
            <div v-if="post.reactionCount > 0" class="z-10 bg-[#fb404b] rounded-full p-[3px] border-2 border-white flex items-center justify-center -mr-1">
                <HeartIcon class="text-white fill-current w-2.5 h-2.5" />
            </div>
             <span v-if="post.isLiked" class="hover:underline cursor-pointer ml-2">Bartosz Miazek</span>
        </div>

        <div v-if="post.commentCount > 0" class="hover:underline cursor-pointer">
             {{ post.commentCount }} komentarz<span v-if="post.commentCount > 1">y</span>
        </div>
    </div>

    <div class="border-t border-gray-300 mx-1"></div>

    <div class="flex items-center justify-between px-1 py-1">
      <button
        class="flex-1 flex items-center justify-center space-x-2 py-1.5 rounded hover:bg-gray-100 transition-colors cursor-pointer group"
      >
        <HeartIcon v-if="post.likedType === 'super'" class="w-5 h-5 text-[#fb404b] fill-current" />
        <HeartOutlineIcon v-else class="w-5 h-5 text-gray-500 group-hover:text-gray-600" />

        <span class="text-[15px] font-semibold" :class="post.likedType === 'super' ? 'text-[#fb404b]' : 'text-gray-600 group-hover:text-gray-700'">
            {{ post.likedType === 'super' ? 'Super' : 'Lubię to!' }}
        </span>
      </button>

      <button class="flex-1 flex items-center justify-center space-x-2 py-1.5 rounded hover:bg-gray-100 transition-colors cursor-pointer group">
        <CommentOutlineIcon class="w-5 h-5 text-gray-500 group-hover:text-gray-600" />
        <span class="text-[15px] font-semibold text-gray-600 group-hover:text-gray-700">Komentarz</span>
      </button>

      <button class="flex-1 flex items-center justify-center space-x-2 py-1.5 rounded hover:bg-gray-100 transition-colors cursor-pointer group">
        <ShareOutlineIcon class="w-5 h-5 text-gray-500 group-hover:text-gray-600" />
        <span class="text-[15px] font-semibold text-gray-600 group-hover:text-gray-700">Udostępnij</span>
      </button>
    </div>

    <div v-if="post.comments.length || true" class="border-t border-gray-300 mx-1 mb-2"></div>

    <CommentItem
             v-for="comment in post.comments"
             :key="comment.id"
             :comment="comment"
             :postAvatarSrc="currentUserAvatar"
             :depth="0"
        />

    <div class="flex items-start mt-2">
         <CommentReplyInput :postAvatarSrc="currentUserAvatar" placeholder="Napisz komentarz..." />
    </div>

  </div>
</template>

<script setup lang="ts">
import { defineProps } from 'vue';
import DotsHorizontalIcon from 'vue-material-design-icons/DotsHorizontal.vue';
import HeartOutlineIcon from 'vue-material-design-icons/HeartOutline.vue';
import HeartIcon from 'vue-material-design-icons/Heart.vue';
import CommentOutlineIcon from 'vue-material-design-icons/CommentOutline.vue';
import ShareOutlineIcon from 'vue-material-design-icons/ShareOutline.vue'; // Uwaga: ShareVariant często wygląda inaczej, ShareOutline jest bliższe
import CommentItem from './CommentItem.vue';
import type { Comment } from './CommentItem.vue';
import CommentReplyInput from './CommentReplyInput.vue';
const currentUserAvatar = "https://i.pravatar.cc/150?u=me";


export interface Post {
  id: number;
  authorName: string;
  authorAvatar: string;
  date: string;
  content: string;
  isLiked: boolean;
  likedType?: 'like' | 'super' | null;
  reactionCount: number;
  commentCount: number;
  comments: Comment[];
}

defineProps<{
  post: Post;
}>();
</script>

<style scoped>
/* Dodatkowe micro-style jeśli Tailwind nie wystarcza */
</style>
