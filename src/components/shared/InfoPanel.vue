<template>
  <div
    class="w-full lg:max-w-[490px] flex flex-col lg:min-w-[370px] bg-white border-t lg:border-t-0 lg:border-l border-gray-200 h-full"
  >
    <!-- Navbar Right -->
    <div class="w-full flex justify-end py-2 px-5 border-b border-gray-200">
      <NavbarRight />
    </div>

    <div class="flex-1 flex flex-row-reverse overflow-hidden">
      <div class="hidden md:block w-[90px] border-l border-gray-200"></div>

      <div class="w-full">
        <HoverScrollbar class="grow overflow-y-auto">
          <!-- User info -->
          <div class="px-4 pt-4 pb-2">
            <div class="flex items-start justify-between">
              <div class="flex items-center gap-2.5">
                <img
                  class="rounded-full w-10 h-10 object-cover border border-gray-200 cursor-pointer hover:brightness-95"
                  :src="author?.avatar"
                  alt="Avatar"
                />
                <div class="flex flex-col">
                  <div
                    class="font-semibold text-[15px] text-gray-900 leading-5 cursor-pointer hover:underline"
                  >
                    {{ author?.name }}
                  </div>
                  <div class="flex items-center text-[13px] text-gray-600 font-normal mt-0.5">
                    <span class="hover:underline cursor-pointer">{{ item.date || 'Teraz' }}</span>
                    <span class="mx-1 font-bold">·</span>
                    <Earth :size="13" fillColor="#65686C" />
                  </div>
                </div>
              </div>
              <button
                v-if="showFollowButton && !item.isFollowing"
                class="text-blue-600 hover:bg-blue-50 font-semibold px-3 py-1.5 rounded-md text-sm transition-colors"
              >
                Obserwuj
              </button>
              <button
                v-else
                class="text-gray-600 hover:bg-gray-100 rounded-full p-2 -mr-2 transition-colors"
              >
                <DotsHorizontal :size="20" fillColor="#65686C" />
              </button>
            </div>
          </div>

          <!-- Content/Caption -->
          <div class="px-4 pb-3 text-[15px] text-gray-900 whitespace-pre-wrap leading-relaxed">
            {{ item.caption || item.content }}
          </div>

          <!-- Hashtags (for reels) -->
          <div v-if="item.hashtags" class="px-4 pb-3 text-[15px] text-blue-600 leading-relaxed">
            {{ item.hashtags }}
          </div>

          <!-- Music (for reels) -->
          <div
            v-if="item.music"
            class="px-4 pb-3 flex items-center gap-2 text-[13px] text-gray-600"
          >
            <MusicNote :size="16" />
            <span>{{ item.music }}</span>
          </div>

          <!-- Stats -->
          <div
            class="mx-4 flex items-center justify-between py-3 border-b border-gray-200 text-gray-600 text-sm"
          >
            <div class="flex items-center">
              <div
                class="bg-blue-500 rounded-full p-1 mr-1.5 flex items-center justify-center w-[18px] h-[18px]"
              >
                <ThumbUp fillColor="#FFFFFF" :size="10" />
              </div>
              <span class="hover:underline cursor-pointer">{{ likes }}</span>
            </div>
            <div class="flex items-center gap-3">
              <span class="cursor-pointer hover:underline">
                {{ commentsCount }} {{ commentsCount === 1 ? 'komentarz' : 'komentarzy' }}
              </span>
              <span class="cursor-pointer hover:underline">
                {{ sharesCount }} {{ sharesCount === 1 ? 'udostępnienie' : 'udostępnień' }}
              </span>
            </div>
          </div>

          <!-- Action buttons -->
          <div
            class="mx-4 py-1 border-b border-gray-200 flex items-center justify-between text-gray-600 text-sm font-semibold"
          >
            <div
              class="flex-1 flex justify-center hover:bg-gray-100 rounded-md py-1.5 cursor-pointer transition-colors"
            >
              <div class="flex items-center gap-2">
                <component
                  :is="useReactionButton ? 'ReactionButton' : 'ThumbUpOutline'"
                  :size="20"
                  fillColor="#65686C"
                />
                <span v-if="!useReactionButton">{{ $t('actions.like') }}</span>
              </div>
            </div>
            <div
              class="flex-1 flex justify-center hover:bg-gray-100 rounded-md py-1.5 cursor-pointer transition-colors"
            >
              <div class="flex items-center gap-2">
                <CommentTextMultiple :size="20" fillColor="#65686C" />
                <span>{{ $t('actions.comment') }}</span>
              </div>
            </div>
            <div
              class="flex-1 flex justify-center hover:bg-gray-100 rounded-md py-1.5 cursor-pointer transition-colors"
            >
              <div class="flex items-center gap-2">
                <ShareVariant :size="20" fillColor="#65686C" />
                <span>{{ $t('actions.send') }}</span>
              </div>
            </div>
          </div>

          <div v-if="!hideComments" class="w-fit m-0 justify-between items-center">
            <CommentFilter />
          </div>

          <!-- Comments section -->
          <div v-if="!hideComments" class="pt-1 px-4 pb-20">
            <div v-if="!hasComments" class="text-center py-8 text-gray-500">
              <p class="text-sm">Brak komentarzy</p>
              <p class="text-xs mt-1">Bądź pierwszą osobą, która skomentuje</p>
            </div>
            <CommentItem
              v-else
              v-for="comment in comments"
              :key="comment.id"
              :comment="comment"
              :post-avatar-src="author?.avatar"
              :depth="0"
              :post-id="item.id"
            />
          </div>
        </HoverScrollbar>

        <!-- Comment input -->
        <div
          v-if="!hideComments"
          class="p-4 border-t border-gray-200 flex items-center bg-white sticky bottom-0 z-10"
        >
          <CommentReplyInput
            v-if="useCommentReplyInput"
            :post-avatar-src="author?.avatar"
            :placeholder="$t('post.writeCommentAs', { name: author?.name })"
            :post-id="item.id"
          />
          <template v-else>
            <img
              class="rounded-full w-8 h-8 object-cover border border-gray-200 mr-2"
              src="https://i.pravatar.cc/150?u=current-user"
              alt="Your avatar"
            />
            <div class="flex-1 relative">
              <input
                v-model="commentInput"
                type="text"
                :placeholder="$t('post.writeComment')"
                class="w-full bg-gray-100 rounded-full px-4 py-2 text-sm outline-none focus:bg-gray-200 transition-colors"
                @keyup.enter="submitComment"
              />
            </div>
          </template>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import Earth from 'vue-material-design-icons/Earth.vue'
import DotsHorizontal from 'vue-material-design-icons/DotsHorizontal.vue'
import ThumbUp from 'vue-material-design-icons/ThumbUp.vue'
import CommentTextMultiple from 'vue-material-design-icons/CommentTextMultiple.vue'
import ShareVariant from 'vue-material-design-icons/ShareVariant.vue'
import MusicNote from 'vue-material-design-icons/MusicNote.vue'
import HoverScrollbar from '@/components/common/HoverScrollbar.vue'
import NavbarRight from '@/layouts/Navbar/NavbarRight.vue'
import CommentFilter from '@/components/profile/CommentFilter.vue'
import CommentItem from '@/components/feed/comment/CommentItem.vue'
import CommentReplyInput from '@/components/feed/comment/CommentReplyInput.vue'
import { getUserById } from '@/utils/users'

interface Props {
  item: any // Can be Reel or Post
  showFollowButton?: boolean
  hideComments?: boolean
  useReactionButton?: boolean
  useCommentReplyInput?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  showFollowButton: false,
  hideComments: false,
  useReactionButton: false,
  useCommentReplyInput: false,
})

const commentInput = ref('')

const author = computed(() => {
  if (props.item.authorId) return getUserById(props.item.authorId)
  return { name: 'Unknown', avatar: '' }
})

const likes = computed(() => {
  if (props.item.likes) return props.item.likes
  if (props.item.reactions) {
    return Object.values(props.item.reactions).reduce(
      (acc: number, val: any) => acc + (val?.length || 0),
      0,
    )
  }
  return 0
})

const commentsCount = computed(
  () => props.item.commentsCount || props.item.comments || props.item.stats?.comments || 0,
)
const sharesCount = computed(
  () => props.item.sharesCount || props.item.shares || props.item.stats?.shares || 0,
)

const hasComments = computed(() => {
  return props.item.comments && props.item.comments.length > 0
})

const comments = computed(() => {
  return props.item.comments || []
})

const submitComment = () => {
  if (commentInput.value.trim()) {
    // Future: Add comment logic
    console.log('Comment:', commentInput.value)
    commentInput.value = ''
  }
}
</script>
