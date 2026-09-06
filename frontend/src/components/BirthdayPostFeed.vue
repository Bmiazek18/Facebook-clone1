<template>
  <div class="flex justify-center bg-[#f0f2f5] min-h-screen pt-8">
    <div class="w-full">
      <div class="bg-white rounded-lg shadow-sm border border-gray-300 overflow-hidden">
        <div class="px-4 pt-4 pb-1 flex items-start justify-between">
          <div class="text-[15px] text-gray-500 leading-snug">{{ $t('birthday.birthdaypostsLengthZnajomychOpublikowalo') }}</div>
          <DotsHorizontalIcon class="text-gray-500 cursor-pointer -mt-1" />
        </div>

        <div class="px-4 py-2" v-for="post in birthdayPosts" :key="post.id">
          <BirthdayPost :post="post" />
        </div>

        <div
          v-if="birthdayPosts.length > 2"
          class="p-3 text-center border-t border-gray-200 hover:bg-gray-50 cursor-pointer transition-colors"
        >
          <span class="text-[15px] font-semibold text-gray-600"
            >{{ $t('birthday.wyswietlJeszczeBirthdaypostsLength') }}</span
          >
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { usePostsStore } from '@/composables/feed/useAppState'
import BirthdayPost from './BirthdayPostItem.vue'
import DotsHorizontalIcon from 'vue-material-design-icons/DotsHorizontal.vue'

const postsStore = usePostsStore()
const route = useRoute()

const targetId = computed(() => route.params.userId as string)
const effectiveTargetId = computed(() => (route.params.userId as string) || '1')

const birthdayPosts = computed(() => {
  return postsStore.posts.filter(
    (p) =>
      p.isBirthday &&
      (p.targetId === effectiveTargetId.value || p.authorId === parseInt(effectiveTargetId.value)),
  )
})
</script>
