<template>
  <div class="w-full lg:max-w-[490px] flex flex-col lg:min-w-[370px] bg-white border-t lg:border-t-0 lg:border-l border-gray-200 h-full">
    <!-- Navbar Right -->
    <div class="w-full h-[65px] flex justify-end py-2 px-5 border-b border-gray-200">

    </div>

    <div class="flex-1 flex flex-row-reverse overflow-hidden">
      <div class="hidden md:block w-[90px] border-l border-gray-200"></div>

      <div class="w-full">
        <HoverScrollbar class="grow overflow-y-auto">
          <!-- User info -->
          <div class="px-4 pt-4 pb-2 border-b border-gray-200">
            <div class="flex items-start justify-between">
              <div class="flex items-center gap-2.5">
                <img
                  class="rounded-full w-10 h-10 object-cover border border-gray-200 cursor-pointer hover:brightness-95"
                  :src="reel.user.avatar"
                  alt="Avatar"
                >
                <div class="flex flex-col">
                  <div class="font-semibold text-[15px] text-gray-900 leading-5 cursor-pointer hover:underline">
                    {{ reel.user.name }}
                  </div>
                  <div v-if="reel.music" class=" pb-3 flex items-center gap-2 text-[13px] text-gray-600">
            <MusicNote :size="16" />
            <span>{{ reel.music }}</span>
          </div>
                </div>
              </div>
              <button
                v-if="!reel.user.isFollowing"
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





          <!-- Comment filter -->
          <div class="flex justify-between items-center px-4 pt-3 mb-2">
            <CommentFilter />
          </div>

          <!-- Comments section -->
          <div class="pt-1 px-4 pb-20">
            <div v-if="!hasComments" class="text-center py-8 text-gray-500">
              <p class="text-sm">Brak komentarzy</p>
              <p class="text-xs mt-1">Bądź pierwszą osobą, która skomentuje</p>
            </div>
            <!-- Future: Add CommentItem components here -->
          </div>
        </HoverScrollbar>

        <!-- Comment input -->
        <div class="p-4 border-t border-gray-200 flex items-center bg-white sticky bottom-0 z-10">
          <img
            class="rounded-full w-8 h-8 object-cover border border-gray-200 mr-2"
            src="https://i.pravatar.cc/150?u=current-user"
            alt="Your avatar"
          >
          <div class="flex-1 relative">
            <input
              v-model="commentInput"
              type="text"
              placeholder="Napisz komentarz..."
              class="w-full bg-gray-100 rounded-full px-4 py-2 text-sm outline-none focus:bg-gray-200 transition-colors"
              @keyup.enter="submitComment"
            >
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import type { Reel } from '@/stores/reels';
import Earth from 'vue-material-design-icons/Earth.vue';
import DotsHorizontal from 'vue-material-design-icons/DotsHorizontal.vue';
import ThumbUp from 'vue-material-design-icons/ThumbUp.vue';
import ThumbUpOutline from 'vue-material-design-icons/ThumbUpOutline.vue';
import CommentTextMultiple from 'vue-material-design-icons/CommentTextMultiple.vue';
import ShareVariant from 'vue-material-design-icons/ShareVariant.vue';
import MusicNote from 'vue-material-design-icons/MusicNote.vue';
import HoverScrollbar from '@/components/HoverScrollbar.vue';
import NavbarRight from '@/components/NavbarRight.vue';
import CommentFilter from '@/components/CommentFilter.vue';

defineProps<{
  reel: Reel;
}>();

const commentInput = ref('');
const hasComments = computed(() => false); // Future: Check if reel has comments

const submitComment = () => {
  if (commentInput.value.trim()) {
    // Future: Add comment logic
    console.log('Comment:', commentInput.value);
    commentInput.value = '';
  }
};
</script>

<style scoped>
/* Custom scrollbar styles if needed */
</style>
